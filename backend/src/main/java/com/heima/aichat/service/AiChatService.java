package com.heima.aichat.service;

import com.heima.aichat.entity.ChatMessagePO;
import com.heima.aichat.mapper.ChatMessageMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.*;
import java.util.stream.Collectors;

/**
 * AI 聊天服务 — Spring AI 1.1.2 + 通义千问 (DashScope OpenAI 兼容)
 */
@Service
public class AiChatService {

    private static final Logger log = LoggerFactory.getLogger(AiChatService.class);
    private static final int MAX_HISTORY = 20;

    static final String SYSTEM_PROMPT =
            "你是智驾游的AI出行助手，名叫「小智」。你可以帮助用户解答出行、路线规划、交通等问题。" +
            "回答时请保持简洁、友好、专业。用中文回答。";

    @Autowired
    private ChatModel chatModel;

    @Autowired
    private ChatMessageMapper chatMessageMapper;

    @Autowired(required = false)
    private KnowledgeBaseService knowledgeBaseService;

    // ---- 非流式 ----

    public Map<String, Object> chat(String conversationId, String userId, String userMessage) {
        conversationId = ensureCid(conversationId);
        saveMessage(conversationId, userId, "user", userMessage);

        ChatClient client = ChatClient.create(chatModel);
        String reply = client.prompt()
                .system(SYSTEM_PROMPT + buildKnowledgeCtx(userMessage))
                .messages(buildHistory(conversationId))
                .user(userMessage)
                .call()
                .content();

        saveMessage(conversationId, userId, "assistant", reply);
        return resultMap(conversationId, reply);
    }

    // ---- 流式 ----

    public Flux<String> chatStream(String conversationId, String userId, String userMessage) {
        conversationId = ensureCid(conversationId);
        final String cid = conversationId;
        saveMessage(cid, userId, "user", userMessage);

        List<Message> history = buildHistory(cid);
        StringBuilder fullReply = new StringBuilder();

        return ChatClient.create(chatModel)
                .prompt()
                .system(SYSTEM_PROMPT + buildKnowledgeCtx(userMessage))
                .messages(history)
                .user(userMessage)
                .stream()
                .content()
                .map(chunk -> {
                    if (chunk != null) {
                        fullReply.append(chunk);
                    }
                    return chunk != null ? chunk : "";
                })
                .doOnComplete(() -> {
                    saveMessage(cid, userId, "assistant", fullReply.toString());
                    log.info("Stream done: cid={}, len={}", cid, fullReply.length());
                })
                .concatWith(Flux.just("[DONE:" + cid + "]"))
                .onErrorResume(err -> {
                    log.error("Stream error", err);
                    saveMessage(cid, userId, "assistant", fullReply + " [异常] " + err.getMessage());
                    return Flux.just("抱歉，AI 服务暂时不可用。", "[DONE:" + cid + "]");
                });
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

    private String ensureCid(String cid) {
        return (cid == null || cid.isEmpty())
                ? UUID.randomUUID().toString().replace("-", "") : cid;
    }

    private void saveMessage(String cid, String uid, String role, String content) {
        chatMessageMapper.insert(new ChatMessagePO(
                UUID.randomUUID().toString().replace("-", ""), cid, uid, role, content));
    }

    private List<Message> buildHistory(String conversationId) {
        return chatMessageMapper.selectByConversationId(conversationId, MAX_HISTORY)
                .stream().map(m -> "assistant".equals(m.getRole())
                        ? (Message) new AssistantMessage(m.getContent())
                        : (Message) new UserMessage(m.getContent()))
                .collect(Collectors.toList());
    }

    private String buildKnowledgeCtx(String query) {
        if (knowledgeBaseService == null) {
            return "";
        }
        String ctx = knowledgeBaseService.search(query);
        return (ctx != null && !ctx.isEmpty()) ? "\n[相关知识库信息]\n" + ctx : "";
    }

    private Map<String, Object> resultMap(String cid, String reply) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("conversationId", cid);
        m.put("reply", reply);
        return m;
    }
}
