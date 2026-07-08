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
 *
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
        for (Integer id : parseIds(ids)) {
            AiMsg msg = aiMsgService.getById(id);
            if (msg == null) {
                log.warn("AiMsg not found: id={}", id);
                continue;
            }

            // 拼接多字段用于 Embedding：标题 + 分类 + 内容
            String embeddingText = buildEmbeddingText(msg);
            Document doc = new Document(embeddingText, Map.of(
                    "sourceId", String.valueOf(msg.getId()),
                    "type", TYPE,
                    "category", String.valueOf(msg.getCategory() != null ? msg.getCategory() : 0)
            ));
            vectorStore.add(List.of(doc));
            log.info("Vector doc added for AiMsg id={}", id);

            // 记录映射
            AiVectorIds mapping = new AiVectorIds();
            mapping.setType(TYPE);
            mapping.setSourceId(String.valueOf(msg.getId()));
            mapping.setDocumentId(doc.getId());
            aiVectorIdsService.save(mapping);
        }
    }

    @Override
    public void update(String ids) {
        // 先删旧的向量，再重新添加
        delete(ids);
        add(ids);
    }

    @Override
    public void delete(String ids) {
        for (Integer id : parseIds(ids)) {
            AiVectorIds mapping = aiVectorIdsService.getByTypeAndSourceId(TYPE, String.valueOf(id));
            if (mapping == null) {
                continue;
            }
            try {
                vectorStore.delete(List.of(mapping.getDocumentId()));
                log.info("Vector doc deleted: docId={}", mapping.getDocumentId());
            } catch (Exception e) {
                log.warn("Vector delete failed for docId={}: {}", mapping.getDocumentId(), e.getMessage());
            }
            aiVectorIdsService.removeByDocumentId(mapping.getDocumentId());
        }
    }

    // ---- 内部 ----

    private List<Integer> parseIds(String ids) {
        return Arrays.stream(ids.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Integer::parseInt)
                .toList();
    }

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
