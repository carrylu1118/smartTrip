package com.heima.aichat.service.impl;

import com.heima.aichat.entity.po.AiMsg;
import com.heima.aichat.mapper.AiMsgMapper;
import com.heima.aichat.service.IAiMsgService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 资讯库 服务实现
 */
@Service
public class AiMsgServiceImpl implements IAiMsgService {

    @Autowired
    private AiMsgMapper aiMsgMapper;

    @Override
    public AiMsg getById(Integer id) {
        return aiMsgMapper.selectById(id);
    }

    @Override
    public List<AiMsg> list(AiMsg condition) {
        return aiMsgMapper.selectList(condition);
    }

    @Override
    public boolean save(AiMsg record) {
        return aiMsgMapper.insert(record) > 0;
    }

    @Override
    public boolean updateById(AiMsg record) {
        return aiMsgMapper.updateById(record) > 0;
    }

    @Override
    public boolean removeById(Integer id) {
        return aiMsgMapper.deleteById(id) > 0;
    }
}
