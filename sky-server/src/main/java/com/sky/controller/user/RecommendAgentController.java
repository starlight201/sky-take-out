package com.sky.controller.user;

import com.sky.ai.service.RecommendAgentService;
import com.sky.ai.service.RecommendCoreService;
import com.sky.context.BaseContext;
import com.sky.dto.RecommendRequest;
import com.sky.result.Result;
import com.sky.vo.RecommendAgentResponseVO;
import com.sky.vo.RecommendResponseVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

@RestController
@RequestMapping("/user")
@Slf4j
public class RecommendAgentController {

    @Autowired
    private RecommendCoreService recommendCoreService;

    @Autowired
    private RecommendAgentService recommendAgentService;

    @PostMapping("/recommend")
    public Result<RecommendResponseVO> recommend(@RequestBody RecommendRequest request) {
        // 用户端普通推荐：只返回结构化推荐结果，适合前端自己渲染卡片。
        fillCurrentUser(request);
        return Result.success(recommendCoreService.recommend(request));
    }

    @PostMapping("/agent/recommend")
    public Result<RecommendAgentResponseVO> agentRecommend(@RequestBody RecommendRequest request) {
        // 用户端 Agent 推荐：返回自然语言回答 + 推荐菜品列表。
        fillCurrentUser(request);
        return Result.success(recommendAgentService.recommend(request));
    }

    @GetMapping(value = "/agent/recommend/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@RequestParam(required = false) Long userId,
                             @RequestParam String query,
                             @RequestParam(required = false) Integer topN) {
        /*
         * SSE 流式接口。
         * 当前实现先拿到完整回答，再按小片段推送 delta 事件，前端可以做打字机效果。
         * 后续如果千问 SDK 支持流式响应，也可以把模型 token 直接转发给前端。
         */
        SseEmitter emitter = new SseEmitter(60_000L);
        RecommendRequest request = new RecommendRequest();
        request.setUserId(userId == null ? BaseContext.getCurrentId() : userId);
        request.setQuery(query);
        request.setTopN(topN);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    RecommendAgentResponseVO response = recommendAgentService.recommend(request);
                    sendAnswerDeltas(emitter, response.getAnswer());
                    emitter.send(SseEmitter.event().name("answer").data(response.getAnswer()));
                    emitter.send(SseEmitter.event().name("items").data(response.getRecommendItems()));
                    emitter.send(SseEmitter.event().name("done").data("complete"));
                    emitter.complete();
                } catch (IOException e) {
                    log.error("SSE send failed", e);
                    emitter.completeWithError(e);
                } catch (Exception e) {
                    log.error("recommend stream failed", e);
                    emitter.completeWithError(e);
                }
            }
        }).start();
        return emitter;
    }

    /**
     * 将完整回答拆成多个 delta 事件发送。
     * 这样前端不需要等待整段文字一次性展示，交互体验更接近真实智能助手。
     */
    private void sendAnswerDeltas(SseEmitter emitter, String answer) throws IOException, InterruptedException {
        if (answer == null) {
            return;
        }
        int chunkSize = 8;
        for (int i = 0; i < answer.length(); i += chunkSize) {
            int end = Math.min(i + chunkSize, answer.length());
            emitter.send(SseEmitter.event().name("delta").data(answer.substring(i, end)));
            Thread.sleep(30L);
        }
    }

    /**
     * 用户端请求通常不应相信前端传入的 userId。
     * 如果请求体没有 userId，就从 JWT 拦截器写入的 BaseContext 中读取当前登录用户。
     */
    private void fillCurrentUser(RecommendRequest request) {
        if (request.getUserId() == null) {
            request.setUserId(BaseContext.getCurrentId());
        }
    }
}
