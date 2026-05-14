package com.sky.controller.user;

import com.sky.context.BaseContext;
import com.sky.dto.RecommendAgentRequestDTO;
import com.sky.result.Result;
import com.sky.service.RecommendAgentClient;
import com.sky.vo.RecommendAgentResponseVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

@RestController
@RequestMapping("/user/recommend-agent")
@Slf4j
public class RecommendAgentController {

    @Autowired
    private RecommendAgentClient recommendAgentClient;

    @PostMapping
    public Result<RecommendAgentResponseVO> recommend(@RequestBody RecommendAgentRequestDTO requestDTO) {
        if (requestDTO == null || !StringUtils.hasText(requestDTO.getQuery())) {
            return Result.error("推荐需求不能为空");
        }
        Long userId = BaseContext.getCurrentId();
        log.info("user recommend request, userId={}, query={}", userId, requestDTO.getQuery());
        return Result.success(recommendAgentClient.recommend(userId, requestDTO.getQuery()));
    }

    @GetMapping("/stream")
    public SseEmitter recommendStream(@RequestParam String query) {
        SseEmitter emitter = new SseEmitter(0L);
        Long userId = BaseContext.getCurrentId();
        CompletableFuture.runAsync(() -> {
            AtomicReference<String> eventName = new AtomicReference<>("message");
            AtomicReference<StringBuilder> data = new AtomicReference<>(new StringBuilder());
            try {
                recommendAgentClient.streamRecommend(userId, query, line -> relaySseLine(emitter, eventName, data, line));
                emitter.complete();
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        });
        return emitter;
    }

    private void relaySseLine(SseEmitter emitter,
                              AtomicReference<String> eventName,
                              AtomicReference<StringBuilder> data,
                              String line) {
        if (line == null || line.startsWith(":")) {
            return;
        }
        try {
            if (!StringUtils.hasText(line)) {
                if (data.get().length() > 0) {
                    emitter.send(SseEmitter.event()
                            .name(eventName.get())
                            .data(data.get().toString()));
                    data.set(new StringBuilder());
                    eventName.set("message");
                }
                return;
            }
            if (line.startsWith("event:")) {
                eventName.set(line.substring("event:".length()).trim());
            } else if (line.startsWith("data:")) {
                if (data.get().length() > 0) {
                    data.get().append('\n');
                }
                data.get().append(line.substring("data:".length()).trim());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
