package com.businessintelligence.DTO;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
@Data
public class NewsQueryDTO {
    private String category;            // 新闻类别
    private Integer minTitleLength;
    private Integer maxTitleLength;
    private Integer minContentLength;
    private Integer maxContentLength;
    private Long userId;                // 单个用户
    private List<Long> userIds;         // 多个用户
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}

