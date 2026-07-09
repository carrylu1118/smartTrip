package com.heima.aichat.service;

import cn.hutool.core.map.MapUtil;
import com.heima.aichat.entity.ChatMessagePO;
import com.heima.aichat.mapper.ChatMessageMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
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

    @Autowired
    private ChatMessageMapper chatMessageMapper;

    @Autowired
    private ChatClient chatClient;

    // ---- 流式 ----
    public Flux<String> chatStream(String conversationId, String userId, String userMessage) {
        conversationId = ensureCid(conversationId);
        final String cid = conversationId;
        saveMessage(cid, userId, "user", userMessage);

        List<Message> history = buildHistory(cid);
        StringBuilder fullReply = new StringBuilder();

        return chatClient.prompt()
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
                .onErrorResume(err -> {
                    err.printStackTrace();
                    return Flux.just("抱歉，AI 服务暂时不可用。");
                });
    }

    public List<ChatMessagePO> getHistory(String conversationId) {
        return chatMessageMapper.selectByConversationId(conversationId, 50);
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

}
