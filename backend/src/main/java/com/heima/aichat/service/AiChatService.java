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

    static final String SYSTEM_PROMPT =
            "你是智驾游的AI出行助手，名叫「小智」。你可以帮助用户解答出行、路线规划、交通等问题。" +
                    "当用户要求推荐旅行社、特色美食、地标名片时，请从知识库中查询有没有相关的信息，如果有请返回。" +
            "回答时请保持简洁、友好、专业。用中文回答。";

    @Autowired
    private ChatModel chatModel;

    @Autowired
    private ChatMessageMapper chatMessageMapper;

    @Autowired
    private VectorStore vectorStore;

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
        return MapUtil.<String,Object>builder().put("conversationId", conversationId).put("reply", reply).build();
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
                .user(userMessage)
                .messages(history)
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
                    log.error("Stream error", err);
                    saveMessage(cid, userId, "assistant", fullReply + " [异常] " + err.getMessage());
                    return Flux.just("抱歉，AI 服务暂时不可用。");
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
    private String buildKnowledgeCtx(String userMessage) {
        return vectorStore.similaritySearch(
                        SearchRequest.builder()
                                .query(userMessage)
                                .topK(5)
                                .similarityThreshold(0.5d)
                                .build()
                ).stream()
                .map(v -> "【" + v.getMetadata().get("source") + "】" + v.getText())
                .collect(Collectors.joining("\n\n"));
    }


}
