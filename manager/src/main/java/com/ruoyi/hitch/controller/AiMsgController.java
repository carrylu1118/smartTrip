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
import com.ruoyi.hitch.domain.AiMsg;
import com.ruoyi.hitch.service.IAiMsgService;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 资讯库Controller
 *
 * @author Shawn
 * @date 2026-08-13
 */
@Controller
@RequestMapping("/hitch/msg")
public class AiMsgController extends BaseController {
    private String prefix = "hitch/msg";

    @Autowired
    private IAiMsgService aiMsgService;

    @RequiresPermissions("hitch:msg:view")
    @GetMapping()
    public String msg() {
        return prefix + "/msg";
    }

    /**
     * 查询资讯库列表
     */
    @RequiresPermissions("hitch:msg:list")
    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo list(AiMsg aiMsg) {
        startPage();
        List<AiMsg> list = aiMsgService.selectAiMsgList(aiMsg);
        return getDataTable(list);
    }

    /**
     * 导出资讯库列表
     */
    @RequiresPermissions("hitch:msg:export")
    @Log(title = "资讯库", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    @ResponseBody
    public AjaxResult export(AiMsg aiMsg) {
        List<AiMsg> list = aiMsgService.selectAiMsgList(aiMsg);
        ExcelUtil<AiMsg> util = new ExcelUtil<AiMsg>(AiMsg.class);
        return util.exportExcel(list, "资讯库数据");
    }

    /**
     * 新增资讯库
     */
    @GetMapping("/add")
    public String add() {
        return prefix + "/add";
    }

    /**
     * 新增保存资讯库
     */
    @RequiresPermissions("hitch:msg:add")
    @Log(title = "资讯库", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    @ResponseBody
    public AjaxResult addSave(AiMsg aiMsg) {
        int rows = aiMsgService.insertAiMsg(aiMsg);
        //发送消息给rabbit
        rabbitSendService.sendAddMsg(aiMsg.getId() + "");
        return toAjax(rows);
    }

    /**
     * 修改资讯库
     */
    @RequiresPermissions("hitch:msg:edit")
    @GetMapping("/edit/{id}")
    public String edit(@PathVariable("id") Long id, ModelMap mmap) {
        AiMsg aiMsg = aiMsgService.selectAiMsgById(id);
        mmap.put("aiMsg", aiMsg);
        return prefix + "/edit";
    }

    /**
     * 修改保存资讯库
     */
    @RequiresPermissions("hitch:msg:edit")
    @Log(title = "资讯库", businessType = BusinessType.UPDATE)
    @PostMapping("/edit")
    @ResponseBody
    public AjaxResult editSave(AiMsg aiMsg) {
        int rows = aiMsgService.updateAiMsg(aiMsg);
        rabbitSendService.sendUpdateMsg(aiMsg.getId() + "");
        return toAjax(rows);
    }

    /**
     * 删除资讯库
     */
    @RequiresPermissions("hitch:msg:remove")
    @Log(title = "资讯库", businessType = BusinessType.DELETE)
    @PostMapping("/remove")
    @ResponseBody
    public AjaxResult remove(String ids) {
        int rows = aiMsgService.deleteAiMsgByIds(ids);
        rabbitSendService.sendDeleteMsg(ids);
        return toAjax(rows);
    }
}
