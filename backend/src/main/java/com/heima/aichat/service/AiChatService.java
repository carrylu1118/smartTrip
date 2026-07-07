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

import java.util.*;
import java.util.function.Consumer;

/**
 * AI 聊天服务 — 对接通义千问 DashScope
 *
 * <p>功能：
 * <ul>
 *   <li>调用千问大模型进行对话</li>
 *   <li>会话记忆持久化到 MySQL（通过 {@link ChatMessageMapper}）</li>
 *   <li>每次请求自动注入最近 N 条历史消息作为上下文</li>
 * </ul>
 *
 * <p>预留扩展点：
 * <ul>
 *   <li>{@link #invokeTools} — Tools 调用（Function Calling）</li>
 *   <li>{@link KnowledgeBaseService} — Redis 向量知识库检索</li>
 * </ul>
 */
@Service
public class AiChatService {

    private static final Logger log = LoggerFactory.getLogger(AiChatService.class);
    private static final int MAX_HISTORY = 20;           // 每次注入上下文的最大历史条数
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

    /**
     * 执行一次对话
     *
     * @param conversationId 会话ID（为空则新建）
     * @param userId         用户ID
     * @param userMessage    用户输入
     * @return 包含 AI 回复和 conversationId 的结果 Map
     */
    public Map<String, Object> chat(String conversationId, String userId, String userMessage) {
        // 1. 会话管理：无则新建
        if (conversationId == null || conversationId.isEmpty()) {
            conversationId = UUID.randomUUID().toString().replace("-", "");
        }

        // 2. 保存用户消息
        saveMessage(conversationId, userId, "user", userMessage);

        // 3. 构建千问请求消息列表（系统提示 + 历史 + 当前）
        List<Message> messages = buildMessages(conversationId, userMessage);

        // 4. 调用千问
        String aiReply = callQwen(messages);

        // 5. 保存 AI 回复
        saveMessage(conversationId, userId, "assistant", aiReply);

        // 6. 返回
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("conversationId", conversationId);
        result.put("reply", aiReply);
        return result;
    }

    /**
     * 加载会话历史消息
     */
    public List<ChatMessagePO> getHistory(String conversationId) {
        return chatMessageMapper.selectByConversationId(conversationId, 100);
    }

    /**
     * 流式对话 — DashScope incrementalOutput 模式，逐 token 回调
     *
     * @param conversationId 会话ID（为空则新建）
     * @param userId         用户ID
     * @param userMessage    用户输入
     * @param onToken        每个 token 到达时回调
     * @param onComplete     流结束回调（传入完整回复文本）
     * @param onError        异常回调
     * @return conversationId
     */
    public String chatStream(String conversationId, String userId, String userMessage,
                             Consumer<String> onToken,
                             Consumer<String> onComplete,
                             Consumer<Throwable> onError) {
        if (conversationId == null || conversationId.isEmpty()) {
            conversationId = UUID.randomUUID().toString().replace("-", "");
        }
        final String cid = conversationId;

        saveMessage(cid, userId, "user", userMessage);

        List<Message> messages = buildMessages(cid, userMessage);

        try {
            Generation gen = new Generation();
            GenerationParam param = GenerationParam.builder()
                    .apiKey(apiKey)
                    .model(model)
                    .messages(messages)
                    .temperature(temperature.floatValue())
                    .maxTokens(maxTokens)
                    .incrementalOutput(true)
                    .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                    .build();

            StringBuilder fullReply = new StringBuilder();
            Flowable<GenerationResult> flowable = gen.streamCall(param);
            flowable.blockingForEach(result -> {
                String chunk = result.getOutput().getChoices().get(0).getMessage().getContent();
                if (chunk != null) {
                    fullReply.append(chunk);
                    onToken.accept(chunk);
                }
            });

            String reply = fullReply.toString();
            saveMessage(cid, userId, "assistant", reply);
            onComplete.accept(reply);
        } catch (NoApiKeyException e) {
            log.error("DashScope API Key not configured", e);
            onToken.accept("AI 服务未配置 API Key，请设置 ai.dashscope.api-key");
            onComplete.accept("");
        } catch (Exception e) {
            log.error("DashScope stream error", e);
            onError.accept(e);
        }
        return cid;
    }

    /**
     * 获取用户的会话列表
     */
    public List<String> getConversations(String userId) {
        return chatMessageMapper.selectConversationIdsByUser(userId);
    }

    /**
     * 获取用户全部聊天记录（跨会话），按时间倒序
     */
    public List<ChatMessagePO> getAllHistory(String userId) {
        return chatMessageMapper.selectAllByUserId(userId);
    }

    // ---- 内部方法 ----

    private void saveMessage(String conversationId, String userId, String role, String content) {
        ChatMessagePO msg = new ChatMessagePO(
                UUID.randomUUID().toString().replace("-", ""),
                conversationId, userId, role, content
        );
        chatMessageMapper.insert(msg);
    }

    private List<Message> buildMessages(String conversationId, String currentMsg) {
        List<Message> messages = new ArrayList<>();

        // 系统提示
        messages.add(Message.builder().role(Role.SYSTEM.getValue()).content(SYSTEM_PROMPT).build());

        // 知识库检索（预留：如果知识库服务已配置，注入相关知识）
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

        // 历史消息（最近 N 条）
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
                    .apiKey(apiKey)
                    .model(model)
                    .messages(messages)
                    .temperature(temperature.floatValue())
                    .maxTokens(maxTokens)
                    .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                    .build();

            GenerationResult result = gen.call(param);
            return result.getOutput().getChoices().get(0).getMessage().getContent();
        } catch (NoApiKeyException e) {
            log.error("DashScope API Key not configured", e);
            return "AI 服务未配置 API Key，请在配置文件中设置 ai.dashscope.api-key";
        } catch (ApiException | InputRequiredException e) {
            log.error("DashScope API error", e);
            return "抱歉，AI 服务暂时不可用，请稍后重试。错误: " + e.getMessage();
        }
    }

    /**
     * [预留] Tools 调用（Function Calling）
     * 未来可用于：查天气、查路况、规划路线等
     */
    @SuppressWarnings("unused")
    private String invokeTools(List<Message> messages) {
        // TODO: 实现 Function Calling
        // 1. 定义可用的 Tool 列表（如路线查询、天气查询等）
        // 2. 调用千问 with tools 参数
        // 3. 解析 tool_calls 响应
        // 4. 执行对应的本地方法
        // 5. 将结果回传给千问获取最终回复
        return null;
    }
}
