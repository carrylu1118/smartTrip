package com.heima.aichat.handler;

import com.heima.aichat.entity.po.AiMsg;
import com.heima.aichat.entity.po.AiVectorIds;
import com.heima.aichat.service.IAiMsgService;
import com.heima.aichat.service.IAiVectorIdsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 资讯消息向量化处理器
 * TODO: 任务5.2.2 - 完成消费端AI消息处理（资讯类消息）
 * <p>将 AiMsg 的 title + content 拼接后 Embedding 存入 Redis 向量库，
 * 通过 AiVectorIds 表维护 MySQL ID 与向量库 documentId 的映射关系。
 */
@Component("HITCH_AI_MSG")
public class MsgHandler implements MqHandler {

    private static final Logger log = LoggerFactory.getLogger(MsgHandler.class);
    private static final String TYPE = "AI_MSG";

    @Autowired
    private VectorStore vectorStore;

    @Autowired
    private IAiMsgService aiMsgService;

    @Autowired
    private IAiVectorIdsService aiVectorIdsService;

    @Override
    public void add(String ids) {

        //根据id，用aiMsgService，从数据库中查出AiMsg的记录

        //使用org.springframework.ai.document.Document对象完成向量化对象的封装
        //Document doc = new Document(xxx)

        //使用vectorStore.add(xxx)完成向量化入库

        //Document存储后，会自动在对象里生成向量化redis里的id
        //使用aiVectorIdsService保存到mysql中间表，将来删除要用到！

    }

    @Override
    public void update(String ids) {
        // 先删旧的向量，再重新添加
        delete(ids);
        add(ids);
    }

    @Override
    public void delete(String ids) {
        //根据传过来的mysql ids值，使用aiVectorIdsService从中间件里查出document_id

        //使用vectorStore.delete删除redis里的向量数据

        //使用aiVectorIdsService删除mysql中间表里的数据

    }

    // ---- 内部 ----

    //工具：如果ids传过来是英文逗号分割的多个id，调用此方法先切割成单个
    //一般用不到，因为ids基本就是1个id
    private List<Integer> parseIds(String ids) {
        return Arrays.stream(ids.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Integer::parseInt)
                .toList();
    }

    //工具：把消息表里有价值的字段组装成一句话，整体写入向量库
    private String buildEmbeddingText(AiMsg msg) {
        StringBuilder sb = new StringBuilder();
        if (msg.getTitle() != null && !msg.getTitle().isEmpty()) {
            sb.append("标题：").append(msg.getTitle()).append("\n");
        }
        if (msg.getCategory() != null) {
            sb.append("分类：").append(msg.getCategory()).append("\n");
        }
        if (msg.getContent() != null && !msg.getContent().isEmpty()) {
            sb.append(msg.getContent());
        }
        return sb.toString();
    }
}
