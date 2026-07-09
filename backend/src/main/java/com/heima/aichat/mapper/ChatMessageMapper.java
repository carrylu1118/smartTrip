package com.heima.aichat.mapper;

import com.heima.aichat.entity.ChatMessagePO;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * AI 聊天消息 Mapper — 会话记忆持久化
 */
public interface ChatMessageMapper {

    int insert(ChatMessagePO record);

    /**
     * 按会话查询最近 N 条消息，用于构建上下文
     */
    List<ChatMessagePO> selectByConversationId(@Param("conversationId") String conversationId,
                                               @Param("limit") int limit);

    /**
     * 查询用户的会话列表（去重 conversation_id，按最新消息时间排序）
     */
    List<Map<String, Object>> selectConversationIdsByUser(@Param("userId") String userId);

    int deleteByConversationId(@Param("conversationId") String conversationId);

    /**
     * 查询用户所有聊天记录（跨会话），按时间倒序
     */
    List<ChatMessagePO> selectAllByUserId(@Param("userId") String userId);
}
