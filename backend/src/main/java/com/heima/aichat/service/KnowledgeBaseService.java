package com.heima.aichat.service;

/**
 * [预留] Redis 向量知识库服务
 *
 * <p>设计意图：使用 Redis Stack 的向量相似度搜索能力，
 * 将出行相关的知识文档（路线规划、常见问题、站点信息等）
 * 进行 Embedding 后存入 Redis，在用户提问时检索相关内容注入上下文。
 *
 * <p>实现步骤（未来）：
 * <ol>
 *   <li>引入 Redis Stack（含 RediSearch 模块）或 Jedis 向量操作</li>
 *   <li>使用文本 Embedding 模型（如 DashScope text-embedding）将知识文档向量化</li>
 *   <li>通过 FT.CREATE / FT.SEARCH 建立向量索引并检索</li>
 *   <li>将检索到的 Top-K 文本片段注入 System Prompt</li>
 * </ol>
 *
 * <p>当前为占位实现，返回 null 表示未启用知识库。
 * 配置知识库内容后返回非 null 字符串即可生效。
 */
// @Service  // 启用时取消注释
public class KnowledgeBaseService {

    /**
     * 根据用户查询检索相关知识
     *
     * @param query 用户原始提问
     * @return 相关知识文本；返回 null 或空字符串表示未命中
     */
    public String search(String query) {
        // TODO: 实现 Redis 向量检索
        // 1. 将 query 转为 Embedding 向量
        // 2. 在 Redis 向量索引中执行 KNN 搜索
        // 3. 返回匹配的文本片段
        return null;
    }

    /**
     * 向知识库添加文档
     *
     * @param content 文档内容
     */
    @SuppressWarnings("unused")
    public void addDocument(String content) {
        // TODO: 实现文档入库
        // 1. 将 content Embedding 为向量
        // 2. 存入 Redis 向量索引
    }
}
