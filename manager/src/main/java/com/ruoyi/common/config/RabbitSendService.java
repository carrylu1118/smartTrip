package com.ruoyi.common.config;

import com.alibaba.fastjson.JSON;
import com.ruoyi.common.core.domain.MessageDto;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RabbitSendService {
    @Autowired
    private RabbitTemplate rabbitTemplate;
    public void sendMessage(String ids, int operation, String type) {
        MessageDto messageDto = MessageDto.builder()
                .ids(ids)
                .operation(operation)
                .type(type)
                .build();
        rabbitTemplate.convertAndSend(type, JSON.toJSONString(messageDto));
    }
    public void sendAddNotice(String ids){
        sendMessage(ids,1,"CAMPUSAI_NOTICE");
    }
    public void sendUpdateNotice(String ids){
        sendMessage(ids,2,"CAMPUSAI_NOTICE");
    }
    public void sendDeleteNotice(String ids){
        sendMessage(ids,3,"CAMPUSAI_NOTICE");
    }
    public void sendAddMaterials(String ids){
        sendMessage(ids,1,"CAMPUSAI_MATERIALS");
    }
    public void sendUpdateMaterials(String ids){
        sendMessage(ids,2,"CAMPUSAI_MATERIALS");
    }
    public void sendDeleteMaterials(String ids){
        sendMessage(ids,3,"CAMPUSAI_MATERIALS");
    }
}
