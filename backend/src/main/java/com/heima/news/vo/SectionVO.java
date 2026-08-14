package com.heima.news.vo;

import com.heima.news.entity.NewsMsg;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 资讯分类板块
 */
@Data
public class SectionVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer category;
    private String label;
    private List<NewsMsg> list;
}
