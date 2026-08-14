package com.heima.news.service;

import com.heima.news.entity.NewsMsg;
import com.heima.news.vo.HomeVO;
import com.heima.news.vo.PageVO;

public interface NewsService {

    /** 首页数据（轮播 + 分类板块） */
    HomeVO getHome();

    /** 某分类分页 */
    PageVO list(Integer category, int page, int size);

    /** 详情 */
    NewsMsg getById(Integer id);
}
