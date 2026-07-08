package com.heima.aichat.handler;

import com.heima.aichat.entity.po.AiFiles;
import com.heima.aichat.service.IAiFilesService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Slf4j
@Component("HITCH_AI_FILE")
public class FileHandler implements MqHandler{
    @Autowired
    private VectorStore vectorStore;
    @Autowired
    private IAiFilesService aiFilesService;

    @Override
    public void add(String ids) {
        if (!StringUtils.isNumeric(ids)){
            throw new RuntimeException("参数错误，id非数字："+ids);
        }
//        AiFiles aiFiles = aiFilesService.getById(Integer.valueOf(ids));
//        log.info("向向量库添加文件：{}",aiFiles);
//        Document document = new Document(aiFiles.getName(),aiFiles.getUrl());
//        vectorStore.add(aiFiles.getName(),aiFiles.getUrl());
    }

    @Override
    public void update(String ids) {

    }

    @Override
    public void delete(String ids) {

    }
}
