package com.heima.news.service.impl;

import com.heima.news.entity.NewsMsg;
import com.heima.news.mapper.NewsMapper;
import com.heima.news.service.NewsService;
import com.heima.news.vo.HomeVO;
import com.heima.news.vo.PageVO;
import com.heima.news.vo.SectionVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 资讯服务实现
 */
@Service
public class NewsServiceImpl implements NewsService {

    /** 轮播分类：旅行社 */
    private static final int BANNER_CATEGORY = 4;
    private static final int BANNER_LIMIT = 5;
    private static final int SECTION_LIMIT = 4;

    /** 板块分类：地标名片 / 旅游资讯 / 特色美食 */
    private static final int[] SECTION_CATEGORIES = {1, 2, 3};

    @Autowired
    private NewsMapper newsMapper;

    @Override
    public HomeVO getHome() {
        HomeVO home = new HomeVO();
        home.setBanners(newsMapper.selectTopByCategory(BANNER_CATEGORY, BANNER_LIMIT));

        List<SectionVO> sections = Arrays.stream(SECTION_CATEGORIES)
                .mapToObj(this::buildSection)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        home.setSections(sections);
        return home;
    }

    private SectionVO buildSection(int category) {
        List<NewsMsg> list = newsMapper.selectTopByCategory(category, SECTION_LIMIT);
        if (list.isEmpty()) {
            return null;
        }
        SectionVO section = new SectionVO();
        section.setCategory(category);
        section.setLabel(list.get(0).getDictLabel());
        section.setList(list);
        return section;
    }

    @Override
    public PageVO list(Integer category, int page, int size) {
        PageVO vo = new PageVO();
        vo.setPage(page);
        vo.setSize(size);
        vo.setTotal(newsMapper.countByCategory(category));
        vo.setList(newsMapper.selectPageByCategory(category, (page - 1) * size, size));
        return vo;
    }

    @Override
    public NewsMsg getById(Integer id) {
        return newsMapper.selectById(id);
    }
}
