package com.heima.news.entity;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 资讯消息（对应视图 v_ai_msg）
 */
@Data
public class NewsMsg implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;
    private Integer category;
    private String title;
    private String pic;
    private String content;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private String dictName;
    private String dictType;
    private String dictLabel;
    private String dictValue;
    private String isDefault;
    private String remark;
}
