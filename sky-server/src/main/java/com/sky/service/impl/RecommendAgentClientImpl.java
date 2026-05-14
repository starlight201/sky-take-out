package com.sky.service.impl;

import com.sky.properties.AgentProperties;
import com.sky.service.RecommendAgentClient;
import com.sky.vo.RecommendAgentResponseVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.OutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@Service
public class RecommendAgentClientImpl implements RecommendAgentClient {

    @Autowired
    private AgentProperties agentProperties;

    @Autowired
    private RestTemplateBuilder restTemplateBuilder;

    @Autowired
    private ObjectMapper objectMapper;

    private RestTemplate restTemplate;

    @PostConstruct
    public void init() {
        this.restTemplate = restTemplateBuilder
                .setConnectTimeout(Duration.ofMillis(agentProperties.getConnectTimeout()))
                .setReadTimeout(Duration.ofMillis(agentProperties.getReadTimeout()))
                .build();
    }

    public RecommendAgentResponseVO recommend(Long userId, String query) {
        Map<String, Object> request = new HashMap<>();
        request.put("userId", userId);
        request.put("query", query);
        String url = agentProperties.getBaseUrl() + "/api/recommend";
        return restTemplate.postForObject(url, request, RecommendAgentResponseVO.class);
    }

    public void streamRecommend(Long userId, String query, Consumer<String> lineConsumer) {
        try {
            Map<String, Object> request = new HashMap<>();
            request.put("userId", userId);
            request.put("query", query);
            byte[] body = objectMapper.writeValueAsBytes(request);

            URL url = new URL(agentProperties.getBaseUrl() + "/api/recommend/stream");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(agentProperties.getConnectTimeout());
            connection.setReadTimeout(0);
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
            connection.setRequestProperty("Accept", "text/event-stream");
            try (OutputStream outputStream = connection.getOutputStream()) {
                outputStream.write(body);
            }

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    lineConsumer.accept(line);
                }
            } finally {
                connection.disconnect();
            }
        } catch (Exception e) {
            throw new RuntimeException("调用推荐 Agent 流式接口失败", e);
        }
    }
}
