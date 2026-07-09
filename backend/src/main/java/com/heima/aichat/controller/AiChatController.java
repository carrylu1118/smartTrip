package com.heima.aichat.controller;

import com.heima.aichat.entity.ChatMessagePO;
import com.heima.aichat.service.AiChatService;
import com.heima.commons.domin.vo.response.ResponseVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/ai/chat")
@Tag(name = "AI聊天", description = "AI 对话接口 — 供「智能问路」页面调用")
public class AiChatController {

    private static final Logger log = LoggerFactory.getLogger(AiChatController.class);

    @Autowired
    private AiChatService aiChatService;

    @Operation(summary = "流式对话 (SSE)")
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
        log.info("AI stream: userId={}, cid={}, msg={}", userId, conversationId, message);

        SseEmitter emitter = new SseEmitter(120000L);
        Flux<String> flux = aiChatService.chatStream(conversationId, userId, message.trim());

        flux.subscribe(
            token -> {
                try {
                    emitter.send(SseEmitter.event().data(token));
                } catch (IOException e) {
                    emitter.completeWithError(e);
                }
            },
            emitter::completeWithError,
            emitter::complete
        );

        return emitter;
    }

    @Operation(summary = "获取会话历史消息")
    @GetMapping("/history/{conversationId}")
    public ResponseVO history(@PathVariable String conversationId) {
        List<ChatMessagePO> messages = aiChatService.getHistory(conversationId);
        return ResponseVO.success(messages);
    }

    @Operation(summary = "获取用户会话列表")
    @GetMapping("/conversations")
    public ResponseVO conversations(HttpServletRequest request) {
        String userId = getUserId(request);
        List<Map<String, Object>> convs = aiChatService.getConversations(userId);
        return ResponseVO.success(convs);
    }

    @Operation(summary = "获取用户全部聊天记录 (跨会话)")
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
