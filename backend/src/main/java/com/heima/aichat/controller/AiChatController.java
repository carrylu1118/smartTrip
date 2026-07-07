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

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * AI 聊天接口 — 供前端「智能问路」页面调用
 */
@RestController
@RequestMapping("/ai/chat")
@Api(value = "AI聊天Controller", tags = {"AI聊天"})
public class AiChatController {

    private static final Logger log = LoggerFactory.getLogger(AiChatController.class);

    @Autowired
    private AiChatService aiChatService;

    private final ExecutorService executor = Executors.newCachedThreadPool();

    /**
     * 发送消息，获取 AI 回复（非流式，兼容保留）
     */
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
     * 流式 AI 对话 — SSE (Server-Sent Events)
     *
     * <p>请求体同 POST /ai/chat，响应为 text/event-stream
     * <p>每个 data 事件为一个 token 文本
     * <p>done 事件表示流结束，data 含 conversationId
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

        executor.execute(() -> {
            try {
                aiChatService.chatStream(conversationId, userId, message.trim(),
                    token -> safeSend(emitter, SseEmitter.event().data(token)),
                    fullReply -> {
                        safeSend(emitter, SseEmitter.event().name("done")
                                .data("{\"conversationId\":\"" + (conversationId == null ? "" : conversationId) + "\"}"));
                        emitter.complete();
                    },
                    emitter::completeWithError
                );
            } catch (Exception e) {
                log.error("Stream error", e);
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }

    /**
     * 获取会话历史消息
     */
    @ApiOperation(value = "获取会话历史", tags = {"AI聊天"})
    @GetMapping("/history/{conversationId}")
    public ResponseVO history(@PathVariable String conversationId) {
        List<ChatMessagePO> messages = aiChatService.getHistory(conversationId);
        return ResponseVO.success(messages);
    }

    /**
     * 获取用户的所有会话ID列表
     */
    @ApiOperation(value = "获取用户会话列表", tags = {"AI聊天"})
    @GetMapping("/conversations")
    public ResponseVO conversations(HttpServletRequest request) {
        String userId = getUserId(request);
        List<String> ids = aiChatService.getConversations(userId);
        return ResponseVO.success(ids);
    }

    /**
     * 获取当前用户所有聊天记录（跨会话），时间倒序排列
     */
    @ApiOperation(value = "获取用户全部聊天记录", tags = {"AI聊天"})
    @GetMapping("/messages")
    public ResponseVO allMessages(HttpServletRequest request) {
        String userId = getUserId(request);
        List<ChatMessagePO> messages = aiChatService.getAllHistory(userId);
        return ResponseVO.success(messages);
    }

    // ---- 内部 ----

    private void safeSend(SseEmitter emitter, SseEmitter.SseEventBuilder event) {
        try {
            emitter.send(event);
        } catch (IOException e) {
            log.warn("SSE send failed", e);
        }
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
