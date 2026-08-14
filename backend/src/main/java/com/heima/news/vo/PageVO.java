package com.heima.news.vo;

import com.heima.news.entity.NewsMsg;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 资讯分页结果
 */
@Data
public class PageVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private long total;
    private int page;
    private int size;
    private List<NewsMsg> list;
}
