package com.ruoyi.hitch.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * AI聊天消息记录对象 t_chat_message
 * 
 * @author Shawn
 * @date 2026-08-17
 */
public class ChatMessage extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键 */
    private String id;

    /** 会话ID */
    @Excel(name = "会话ID")
    private String conversationId;

    /** 用户ID */
    @Excel(name = "用户ID")
    private String userId;

    /** 角色: user / assistant / system */
    @Excel(name = "角色: user / assistant / system")
    private String role;

    /** 消息内容 */
    @Excel(name = "消息内容")
    private String content;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "创建时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date createdTime;

    public void setId(String id) 
    {
        this.id = id;
    }

    public String getId() 
    {
        return id;
    }
    public void setConversationId(String conversationId) 
    {
        this.conversationId = conversationId;
    }

    public String getConversationId() 
    {
        return conversationId;
    }
    public void setUserId(String userId) 
    {
        this.userId = userId;
    }

    public String getUserId() 
    {
        return userId;
    }
    public void setRole(String role) 
    {
        this.role = role;
    }

    public String getRole() 
    {
        return role;
    }
    public void setContent(String content) 
    {
        this.content = content;
    }

    public String getContent() 
    {
        return content;
    }
    public void setCreatedTime(Date createdTime) 
    {
        this.createdTime = createdTime;
    }

    public Date getCreatedTime() 
    {
        return createdTime;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("conversationId", getConversationId())
            .append("userId", getUserId())
            .append("role", getRole())
            .append("content", getContent())
            .append("createdTime", getCreatedTime())
            .toString();
    }
}
