package com.heima.news.vo;

import com.heima.news.entity.NewsMsg;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 资讯首页聚合数据
 */
@Data
public class HomeVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 顶部轮播（旅行社） */
    private List<NewsMsg> banners;
    /** 分类板块 */
    private List<SectionVO> sections;
}
