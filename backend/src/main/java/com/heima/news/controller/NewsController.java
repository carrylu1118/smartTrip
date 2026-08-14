package com.heima.news.controller;

import com.heima.commons.domin.vo.response.ResponseVO;
import com.heima.news.service.NewsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 资讯接口
 */
@RestController
@RequestMapping("/news")
public class NewsController {

    @Autowired
    private NewsService newsService;

    @GetMapping("/home")
    public ResponseVO home() {
        return ResponseVO.success(newsService.getHome());
    }

    @GetMapping("/list")
    public ResponseVO list(@RequestParam Integer category,
                           @RequestParam(defaultValue = "1") int page,
                           @RequestParam(defaultValue = "10") int size) {
        return ResponseVO.success(newsService.list(category, page, size));
    }

    @GetMapping("/{id}")
    public ResponseVO detail(@PathVariable Integer id) {
        return ResponseVO.success(newsService.getById(id));
    }
}
