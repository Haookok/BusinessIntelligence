package com.businessintelligence.DTO;

import lombok.Data;

@Data
public class RecommendRequest {
    private Integer newsId;       // 当前浏览新闻ID
    private Integer userId;       // 用户ID
    private Integer timestamp;       // 时间戳，用于获取历史浏览记录
}