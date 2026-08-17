package com.ruoyi.hitch.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.hitch.mapper.ChatMessageMapper;
import com.ruoyi.hitch.domain.ChatMessage;
import com.ruoyi.hitch.service.IChatMessageService;
import com.ruoyi.common.core.text.Convert;

/**
 * AI聊天消息记录Service业务层处理
 * 
 * @author Shawn
 * @date 2026-08-17
 */
@Service
public class ChatMessageServiceImpl implements IChatMessageService 
{
    @Autowired
    private ChatMessageMapper chatMessageMapper;

    /**
     * 查询AI聊天消息记录
     * 
     * @param id AI聊天消息记录主键
     * @return AI聊天消息记录
     */
    @Override
    public ChatMessage selectChatMessageById(String id)
    {
        return chatMessageMapper.selectChatMessageById(id);
    }

    /**
     * 查询AI聊天消息记录列表
     * 
     * @param chatMessage AI聊天消息记录
     * @return AI聊天消息记录
     */
    @Override
    public List<ChatMessage> selectChatMessageList(ChatMessage chatMessage)
    {
        return chatMessageMapper.selectChatMessageList(chatMessage);
    }

    /**
     * 新增AI聊天消息记录
     * 
     * @param chatMessage AI聊天消息记录
     * @return 结果
     */
    @Override
    public int insertChatMessage(ChatMessage chatMessage)
    {
        return chatMessageMapper.insertChatMessage(chatMessage);
    }

    /**
     * 修改AI聊天消息记录
     * 
     * @param chatMessage AI聊天消息记录
     * @return 结果
     */
    @Override
    public int updateChatMessage(ChatMessage chatMessage)
    {
        return chatMessageMapper.updateChatMessage(chatMessage);
    }

    /**
     * 批量删除AI聊天消息记录
     * 
     * @param ids 需要删除的AI聊天消息记录主键
     * @return 结果
     */
    @Override
    public int deleteChatMessageByIds(String ids)
    {
        return chatMessageMapper.deleteChatMessageByIds(Convert.toStrArray(ids));
    }

    /**
     * 删除AI聊天消息记录信息
     * 
     * @param id AI聊天消息记录主键
     * @return 结果
     */
    @Override
    public int deleteChatMessageById(String id)
    {
        return chatMessageMapper.deleteChatMessageById(id);
    }
}
