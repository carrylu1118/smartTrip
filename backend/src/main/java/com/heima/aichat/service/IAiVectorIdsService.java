package com.heima.aichat.service;

import com.heima.aichat.entity.po.AiVectorIds;

import java.util.List;

/**
 * 向量ID映射 服务接口
 */
public interface IAiVectorIdsService {

    AiVectorIds getByTypeAndSourceId(String type, String sourceId);

    List<AiVectorIds> listByType(String type);

    boolean save(AiVectorIds record);

    boolean removeByDocumentId(String documentId);
}
