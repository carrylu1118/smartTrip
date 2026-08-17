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

    // TODO: 任务4.2.3 - 完成ChatClient配置
    public Flux<String> chatStream(String conversationId, String userId, String userMessage) {
        conversationId = ensureCid(conversationId);
        final String cid = conversationId;

        //保存新的用户消息到历史记录表（角色：user）
        saveMessage(conversationId,userId,"user",userMessage);
        //查询最近的20条记录  参考buildHistory()
        List<Message> history = buildHistory(cid);
        // 用于收集流式输出的完整内容
        StringBuilder assistantResponse = new StringBuilder();

        //拼接到chatClient
        return chatClient.prompt()
            .messages(history)   //历史消息
            .user(userMessage)    //用户当前消息
            .stream()
            .content()
            .map(chunk -> {
                //流式输出的每个小片段
                //需要使用外部变量收集这些小片段，最后整体给doOnComplete存库用
                if(chunk != null){
                    assistantResponse.append(chunk);
                    return chunk;
                }
                return chunk != null ? chunk : "";
            })
            .doOnComplete(() -> {
                //全部流式输出完成后的动作：保存新的消息到历史记录表（角色：assistant）
                String fullResponse = assistantResponse.toString();
                if(!fullResponse.isEmpty()){
                    saveMessage(cid,userId,"assistant",fullResponse);
                }
                log.info("流式输出完成: cid={}", cid);
            })
            .onErrorResume(err -> {
                log.info("AI 服务调用失败");
                // 如果出错，保存错误信息到数据库
                String errorMsg = "抱歉，AI 服务暂时不可用。";
                saveMessage(cid,userId,"assistant",errorMsg);
                return Flux.just(errorMsg);
            });
    }

    public List<ChatMessagePO> getHistory(String conversationId) {
        return chatMessageMapper.selectByConversationId(conversationId, 50);
    }

    public List<Map<String, Object>> getConversations(String userId) {
        return chatMessageMapper.selectConversationIdsByUser(userId);
    }

    public List<ChatMessagePO> getAllHistory(String userId) {
        return chatMessageMapper.selectAllByUserId(userId);
    }

    // ---- 内部 ----

    //工具：检查conversationId是否为空，如果空，生成一个新的并返回
    private String ensureCid(String cid) {
        return (cid == null || cid.isEmpty())
                ? UUID.randomUUID().toString().replace("-", "") : cid;
    }

    //保存对话记录到mysql
    private void saveMessage(String cid, String uid, String role, String content) {
        chatMessageMapper.insert(new ChatMessagePO(
                UUID.randomUUID().toString().replace("-", ""), cid, uid, role, content));
    }

    // TODO: 任务4.2.4 - 完成会话上下文封装
    private List<Message> buildHistory(String conversationId) {
        //根据conversationId，使用chatMessageMapper查询历史消息
        List<ChatMessagePO> Pos = chatMessageMapper.selectByConversationId(conversationId, MAX_HISTORY);
        if(Pos == null || Pos.isEmpty()){
            return new ArrayList<>();
        }
        //遍历返回的列表，逐个检查消息的role
        List<Message> messages = new ArrayList<>();
        for(ChatMessagePO Po : Pos){
            //注意返回的ChatMessagePO.role决定了你要使用哪种消息对象封装返回
            if("user".equals(Po.getRole())){
                //user -> UserMessage
                messages.add(new UserMessage(Po.getContent()));
            }else if("assistant".equals(Po.getRole())){
                //assistant -> AssistantMessage
                messages.add(new AssistantMessage(Po.getContent()));
            }
        }
        //隐藏小技巧：history会话上下文是有长度限制的，建议20条
        return messages;
    }

}
