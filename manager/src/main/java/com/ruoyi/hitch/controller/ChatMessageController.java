package com.ruoyi.hitch.controller;

import java.util.List;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.hitch.domain.ChatMessage;
import com.ruoyi.hitch.service.IChatMessageService;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * AI聊天消息记录Controller
 * 
 * @author Shawn
 * @date 2026-08-17
 */
@Controller
@RequestMapping("/hitch/message")
public class ChatMessageController extends BaseController
{
    private String prefix = "hitch/message";

    @Autowired
    private IChatMessageService chatMessageService;

    @RequiresPermissions("hitch:message:view")
    @GetMapping()
    public String message()
    {
        return prefix + "/message";
    }

    /**
     * 查询AI聊天消息记录列表
     */
    @RequiresPermissions("hitch:message:list")
    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo list(ChatMessage chatMessage)
    {
        startPage();
        List<ChatMessage> list = chatMessageService.selectChatMessageList(chatMessage);
        return getDataTable(list);
    }

    /**
     * 导出AI聊天消息记录列表
     */
    @RequiresPermissions("hitch:message:export")
    @Log(title = "AI聊天消息记录", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    @ResponseBody
    public AjaxResult export(ChatMessage chatMessage)
    {
        List<ChatMessage> list = chatMessageService.selectChatMessageList(chatMessage);
        ExcelUtil<ChatMessage> util = new ExcelUtil<ChatMessage>(ChatMessage.class);
        return util.exportExcel(list, "AI聊天消息记录数据");
    }

    /**
     * 新增AI聊天消息记录
     */
    @GetMapping("/add")
    public String add()
    {
        return prefix + "/add";
    }

    /**
     * 新增保存AI聊天消息记录
     */
    @RequiresPermissions("hitch:message:add")
    @Log(title = "AI聊天消息记录", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    @ResponseBody
    public AjaxResult addSave(ChatMessage chatMessage)
    {
        return toAjax(chatMessageService.insertChatMessage(chatMessage));
    }

    /**
     * 修改AI聊天消息记录
     */
    @RequiresPermissions("hitch:message:edit")
    @GetMapping("/edit/{id}")
    public String edit(@PathVariable("id") String id, ModelMap mmap)
    {
        ChatMessage chatMessage = chatMessageService.selectChatMessageById(id);
        mmap.put("chatMessage", chatMessage);
        return prefix + "/edit";
    }

    /**
     * 修改保存AI聊天消息记录
     */
    @RequiresPermissions("hitch:message:edit")
    @Log(title = "AI聊天消息记录", businessType = BusinessType.UPDATE)
    @PostMapping("/edit")
    @ResponseBody
    public AjaxResult editSave(ChatMessage chatMessage)
    {
        return toAjax(chatMessageService.updateChatMessage(chatMessage));
    }

    /**
     * 删除AI聊天消息记录
     */
    @RequiresPermissions("hitch:message:remove")
    @Log(title = "AI聊天消息记录", businessType = BusinessType.DELETE)
    @PostMapping( "/remove")
    @ResponseBody
    public AjaxResult remove(String ids)
    {
        return toAjax(chatMessageService.deleteChatMessageByIds(ids));
    }
}
