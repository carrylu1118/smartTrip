package com.heima.aichat.service;

import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.heima.aichat.entity.ChatMessagePO;
import com.heima.aichat.mapper.ChatMessageMapper;
import io.reactivex.Flowable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.*;

/**
 * AI 聊天服务 — 对接通义千问 DashScope
 */
@Service
public class AiChatService {

    private static final Logger log = LoggerFactory.getLogger(AiChatService.class);
    private static final int MAX_HISTORY = 20;
    private static final String SYSTEM_PROMPT =
            "你是智驾游的AI出行助手，名叫「小智」。你可以帮助用户解答出行、路线规划、交通等问题。" +
            "回答时请保持简洁、友好、专业。用中文回答。";

    @Value("${ai.dashscope.api-key}")
    private String apiKey;

    @Value("${ai.chat.model:qwen-plus}")
    private String model;

    @Value("${ai.chat.temperature:0.7}")
    private Double temperature;

    @Value("${ai.chat.max-tokens:2000}")
    private Integer maxTokens;

    @Autowired
    private ChatMessageMapper chatMessageMapper;

    @Autowired(required = false)
    private KnowledgeBaseService knowledgeBaseService;

    /** 非流式对话（保留） */
    public Map<String, Object> chat(String conversationId, String userId, String userMessage) {
        if (conversationId == null || conversationId.isEmpty()) {
            conversationId = UUID.randomUUID().toString().replace("-", "");
        }
        saveMessage(conversationId, userId, "user", userMessage);
        List<Message> messages = buildMessages(conversationId, userMessage);
        String aiReply = callQwen(messages);
        saveMessage(conversationId, userId, "assistant", aiReply);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("conversationId", conversationId);
        result.put("reply", aiReply);
        return result;
    }

    /**
     * 流式对话 — 返回 Reactor Flux&lt;String&gt;，由 Spring WebFlux 自动序列化为 SSE
     */
    public Flux<String> chatStream(String conversationId, String userId, String userMessage) {
        if (conversationId == null || conversationId.isEmpty()) {
            conversationId = UUID.randomUUID().toString().replace("-", "");
        }
        final String cid = conversationId;

        saveMessage(cid, userId, "user", userMessage);

        List<Message> messages;
        GenerationParam param;
        try {
            messages = buildMessages(cid, userMessage);
            param = GenerationParam.builder()
                    .apiKey(apiKey)
                    .model(model)
                    .messages(messages)
                    .temperature(temperature.floatValue())
                    .maxTokens(maxTokens)
                    .incrementalOutput(true)
                    .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                    .build();
        } catch (Exception e) {
            log.error("Build stream param error", e);
            return Flux.just("抱歉，AI 服务初始化失败。" + e.getMessage(),
                    "[DONE:" + cid + "]");
        }

        StringBuilder fullReply = new StringBuilder();

        try {
            Generation gen = new Generation();
            Flowable<GenerationResult> flowable = gen.streamCall(param);

            // RxJava Flowable → Reactor Flux
            return Flux.from(flowable)
                    .map(result -> {
                        String chunk = result.getOutput().getChoices().get(0).getMessage().getContent();
                        if (chunk != null) fullReply.append(chunk);
                        return chunk != null ? chunk : "";
                    })
                    .doOnComplete(() -> {
                        String reply = fullReply.toString();
                        saveMessage(cid, userId, "assistant", reply);
                        log.info("Stream complete: conversationId={}, len={}", cid, reply.length());
                    })
                    .concatWith(Flux.just("[DONE:" + cid + "]"))
                    .onErrorResume(err -> {
                        log.error("DashScope stream error", err);
                        String fallback = "抱歉，AI 服务暂时不可用。";
                        if (err instanceof NoApiKeyException) {
                            fallback = "AI 服务未配置 API Key，请设置 ai.dashscope.api-key";
                        }
                        saveMessage(cid, userId, "assistant", fullReply + fallback);
                        return Flux.just(fallback, "[DONE:" + cid + "]");
                    });

        } catch (NoApiKeyException e) {
            log.error("DashScope API Key not configured", e);
            return Flux.just("AI 服务未配置 API Key，请设置 ai.dashscope.api-key",
                    "[DONE:" + cid + "]");
        } catch (Exception e) {
            log.error("DashScope stream error", e);
            return Flux.error(e);
        }
    }

    public List<ChatMessagePO> getHistory(String conversationId) {
        return chatMessageMapper.selectByConversationId(conversationId, 100);
    }

    public List<String> getConversations(String userId) {
        return chatMessageMapper.selectConversationIdsByUser(userId);
    }

    public List<ChatMessagePO> getAllHistory(String userId) {
        return chatMessageMapper.selectAllByUserId(userId);
    }

    // ---- 内部 ----

    private void saveMessage(String conversationId, String userId, String role, String content) {
        ChatMessagePO msg = new ChatMessagePO(
                UUID.randomUUID().toString().replace("-", ""),
                conversationId, userId, role, content);
        chatMessageMapper.insert(msg);
    }

    private List<Message> buildMessages(String conversationId, String currentMsg) {
        List<Message> messages = new ArrayList<>();
        messages.add(Message.builder().role(Role.SYSTEM.getValue()).content(SYSTEM_PROMPT).build());

        if (knowledgeBaseService != null) {
            try {
                String knowledge = knowledgeBaseService.search(currentMsg);
                if (knowledge != null && !knowledge.isEmpty()) {
                    messages.add(Message.builder().role(Role.SYSTEM.getValue())
                            .content("以下是可能相关的知识库信息，请参考：\n" + knowledge).build());
                }
            } catch (Exception e) {
                log.warn("Knowledge base search failed, skipping: {}", e.getMessage());
            }
        }

        List<ChatMessagePO> history = chatMessageMapper.selectByConversationId(conversationId, MAX_HISTORY);
        for (ChatMessagePO msg : history) {
            String role = "assistant".equals(msg.getRole()) ? Role.ASSISTANT.getValue() : Role.USER.getValue();
            messages.add(Message.builder().role(role).content(msg.getContent()).build());
        }
        return messages;
    }

    private String callQwen(List<Message> messages) {
        try {
            Generation gen = new Generation();
            GenerationParam param = GenerationParam.builder()
                    .apiKey(apiKey).model(model).messages(messages)
                    .temperature(temperature.floatValue()).maxTokens(maxTokens)
                    .resultFormat(GenerationParam.ResultFormat.MESSAGE).build();
            GenerationResult result = gen.call(param);
            return result.getOutput().getChoices().get(0).getMessage().getContent();
        } catch (NoApiKeyException e) {
            log.error("DashScope API Key not configured", e);
            return "AI 服务未配置 API Key，请在配置文件中设置 ai.dashscope.api-key";
        } catch (ApiException | InputRequiredException e) {
            log.error("DashScope API error", e);
            return "抱歉，AI 服务暂时不可用，请稍后重试。";
        }
    }
}
