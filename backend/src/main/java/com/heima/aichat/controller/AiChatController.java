package com.heima.aichat.controller;

import com.heima.aichat.entity.ChatMessagePO;
import com.heima.aichat.service.AiChatService;
import com.heima.commons.domin.vo.response.ResponseVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/ai/chat")
@Api(value = "AI聊天Controller", tags = {"AI聊天"})
public class AiChatController {

    private static final Logger log = LoggerFactory.getLogger(AiChatController.class);

    @Autowired
    private AiChatService aiChatService;

    /** 非流式（保留） */
    @ApiOperation(value = "AI对话接口", tags = {"AI聊天"})
    @PostMapping
    public ResponseVO chat(@RequestBody Map<String, String> body, HttpServletRequest request) {
        String message = body.get("message");
        if (message == null || message.trim().isEmpty()) {
            return ResponseVO.error("消息不能为空");
        }
        String conversationId = body.get("conversationId");
        String userId = getUserId(request);
        log.info("AI chat: userId={}, conversationId={}, message={}", userId, conversationId, message);
        Map<String, Object> result = aiChatService.chat(conversationId, userId, message.trim());
        return ResponseVO.success(result);
    }

    /**
     * 流式 AI 对话 — SseEmitter 逐 token 推送
     */
    @ApiOperation(value = "AI流式对话(SSE)", tags = {"AI聊天"})
    @PostMapping("/stream")
    public SseEmitter chatStream(@RequestBody Map<String, String> body, HttpServletRequest request) {
        String message = body.get("message");
        if (message == null || message.trim().isEmpty()) {
            SseEmitter err = new SseEmitter();
            err.completeWithError(new IllegalArgumentException("消息不能为空"));
            return err;
        }

        String conversationId = body.get("conversationId");
        String userId = getUserId(request);
        log.info("AI stream: userId={}, conversationId={}, message={}", userId, conversationId, message);

        SseEmitter emitter = new SseEmitter(120000L);

        Flux<String> flux = aiChatService.chatStream(conversationId, userId, message.trim());

        // subscribe 是非阻塞的，回调跑在 DashScope Flowable 自己的线程上
        flux.subscribe(
            token -> {
                try {
                    if (token.startsWith("[DONE:")) {
                        String cid = token.substring(6, token.length() - 1);
                        emitter.send(SseEmitter.event().name("done").data("{\"conversationId\":\"" + cid + "\"}"));
                        emitter.complete();
                    } else {
                        emitter.send(SseEmitter.event().data(token));
                    }
                } catch (IOException e) {
                    emitter.completeWithError(e);
                }
            },
            error -> emitter.completeWithError(error),
            () -> {} // 正常情况由 [DONE:] token 触发 complete
        );

        return emitter;
    }

    @ApiOperation(value = "获取会话历史", tags = {"AI聊天"})
    @GetMapping("/history/{conversationId}")
    public ResponseVO history(@PathVariable String conversationId) {
        List<ChatMessagePO> messages = aiChatService.getHistory(conversationId);
        return ResponseVO.success(messages);
    }

    @ApiOperation(value = "获取用户会话列表", tags = {"AI聊天"})
    @GetMapping("/conversations")
    public ResponseVO conversations(HttpServletRequest request) {
        String userId = getUserId(request);
        List<String> ids = aiChatService.getConversations(userId);
        return ResponseVO.success(ids);
    }

    @ApiOperation(value = "获取用户全部聊天记录", tags = {"AI聊天"})
    @GetMapping("/messages")
    public ResponseVO allMessages(HttpServletRequest request) {
        String userId = getUserId(request);
        List<ChatMessagePO> messages = aiChatService.getAllHistory(userId);
        return ResponseVO.success(messages);
    }

    private String getUserId(HttpServletRequest request) {
        Object userObj = request.getSession().getAttribute("user");
        if (userObj != null) {
            try {
                return String.valueOf(userObj.getClass().getMethod("getId").invoke(userObj));
            } catch (Exception e) {
                log.warn("Failed to get user ID from session", e);
            }
        }
        String token = request.getHeader("SESSION_TOKEN_KEY");
        return token != null ? token : "anonymous";
    }
}
