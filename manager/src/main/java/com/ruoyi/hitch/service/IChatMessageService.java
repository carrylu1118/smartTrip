package com.ruoyi.hitch.service;

import java.util.List;
import com.ruoyi.hitch.domain.ChatMessage;

/**
 * AI聊天消息记录Service接口
 * 
 * @author Shawn
 * @date 2026-08-17
 */
public interface IChatMessageService 
{
    /**
     * 查询AI聊天消息记录
     * 
     * @param id AI聊天消息记录主键
     * @return AI聊天消息记录
     */
    public ChatMessage selectChatMessageById(String id);

    /**
     * 查询AI聊天消息记录列表
     * 
     * @param chatMessage AI聊天消息记录
     * @return AI聊天消息记录集合
     */
    public List<ChatMessage> selectChatMessageList(ChatMessage chatMessage);

    /**
     * 新增AI聊天消息记录
     * 
     * @param chatMessage AI聊天消息记录
     * @return 结果
     */
    public int insertChatMessage(ChatMessage chatMessage);

    /**
     * 修改AI聊天消息记录
     * 
     * @param chatMessage AI聊天消息记录
     * @return 结果
     */
    public int updateChatMessage(ChatMessage chatMessage);

    /**
     * 批量删除AI聊天消息记录
     * 
     * @param ids 需要删除的AI聊天消息记录主键集合
     * @return 结果
     */
    public int deleteChatMessageByIds(String ids);

    /**
     * 删除AI聊天消息记录信息
     * 
     * @param id AI聊天消息记录主键
     * @return 结果
     */
    public int deleteChatMessageById(String id);
}
