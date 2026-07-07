package com.heima.aichat.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 知识库服务 — 基于 Spring AI VectorStore (Redis)
 *
 * <p>启用步骤：
 * <ol>
 *   <li>部署 Redis Stack（含 RediSearch 模块）</li>
 *   <li>配置 spring.ai.openai.embedding.*</li>
 *   <li>配置 spring.ai.vectorstore.redis.*</li>
 *   <li>取消 search() / addDocument() 中的注释，使用 VectorStore API</li>
 * </ol>
 */
@Service
public class KnowledgeBaseService {

    private static final Logger log = LoggerFactory.getLogger(KnowledgeBaseService.class);

    @Autowired(required = false)
    private VectorStore vectorStore;

    /**
     * 向量相似度检索
     */
    public String search(String query) {
        if (vectorStore == null) {
            return null;
        }
        // TODO: Redis Stack 就绪后启用
        // List<org.springframework.ai.document.Document> docs =
        //     vectorStore.similaritySearch(SearchRequest.query(query).withTopK(3));
        log.debug("VectorStore search not yet enabled, query={}", query);
        return null;
    }

    /**
     * 添加文档到知识库
     */
    public void addDocument(String content) {
        if (vectorStore == null) return;
        // TODO: Redis Stack 就绪后启用
        // vectorStore.add(List.of(new Document(content)));
        log.debug("VectorStore add not yet enabled");
    }
}
