package com.heima.news.mapper;

import com.heima.news.entity.NewsMsg;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 资讯 Mapper（查询视图 v_ai_msg）
 */
public interface NewsMapper {

    /** 查询某分类 id 倒序前 N 条（用于轮播 / 板块） */
    List<NewsMsg> selectTopByCategory(@Param("category") Integer category, @Param("limit") int limit);

    /** 某分类分页查询（id 倒序） */
    List<NewsMsg> selectPageByCategory(@Param("category") Integer category, @Param("offset") int offset, @Param("limit") int limit);

    /** 某分类文章总数 */
    long countByCategory(@Param("category") Integer category);

    /** 文章详情 */
    NewsMsg selectById(@Param("id") Integer id);
}
