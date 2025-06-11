// src/main/java/com/businessintelligence/controller/NewsAnalysisController.java
package com.businessintelligence.controller;

import com.businessintelligence.DTO.HotNewsDTO;
import com.businessintelligence.DTO.NewsTrendDTO;
import com.businessintelligence.service.NewsAnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@RestController
@RequestMapping("/api/news-analysis")
public class NewsAnalysisController {

    @Autowired
    private NewsAnalysisService newsAnalysisService;

    // 获取综合热度最高的10个新闻
    @GetMapping("/top-hot-news")
    public ResponseEntity<List<HotNewsDTO>> getTop10HotNews(
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
            LocalDateTime timestamp) {

        // 如果未提供时间戳，则使用当前时间
        long timestampInSeconds = timestamp != null
                ? timestamp.toEpochSecond(ZoneOffset.ofHours(8))
                : LocalDateTime.now().toEpochSecond(ZoneOffset.ofHours(8));

        List<HotNewsDTO> hotNewsList = newsAnalysisService.getTop10HotNews(timestampInSeconds);
        return ResponseEntity.ok(hotNewsList);
    }

    // 获取指定新闻在特定时间点前10天的热度变化趋势
    @GetMapping("/news-trend/{newsId}")
    public ResponseEntity<List<NewsTrendDTO>> getNewsTrend(
            @PathVariable Integer newsId,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
            LocalDateTime timestamp) {

        // 如果未提供时间戳，则使用当前时间
        long timestampInSeconds = timestamp != null
                ? timestamp.toEpochSecond(ZoneOffset.ofHours(8))
                : LocalDateTime.now().toEpochSecond(ZoneOffset.ofHours(8));

        List<NewsTrendDTO> trendList = newsAnalysisService.getNewsTrend(newsId, timestampInSeconds);
        return ResponseEntity.ok(trendList);
    }
}