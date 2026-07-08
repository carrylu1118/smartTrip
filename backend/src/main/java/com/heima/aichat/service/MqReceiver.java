package com.heima.aichat.service;

import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@RabbitListener(queuesToDeclare = {
        @Queue("HITCH_AI_MSG"),
        @Queue("HITCH_AI_FILE")})
public class MqReceiver {
    private final static Logger logger = LoggerFactory.getLogger(MqReceiver.class);



    @RabbitHandler
    public void processMessage(String message) {
        logger.info("user hit : message={}", message);
        MessageDto messageDto = JSON.parseObject(message, MessageDto.class);
        logger.info("dto={}",messageDto);

    }

}
