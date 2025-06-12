// src/main/java/com/businessintelligence/service/NewsAnalysisService.java
package com.businessintelligence.service;

import com.businessintelligence.DTO.HotNewsDTO;
import com.businessintelligence.DTO.NewsTrendDTO;

import java.util.List;

public interface NewsAnalysisService {

    // 获取综合热度最高的10个新闻
    List<HotNewsDTO> getTop10HotNews(long timestamp);

    // 获取指定新闻在特定时间点前10天的热度变化趋势
    List<NewsTrendDTO> getNewsTrend(int newsId, long timestamp);
}