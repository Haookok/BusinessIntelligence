package com.businessintelligence.controller;

import com.businessintelligence.DTO.HotNewsDTO;
import com.businessintelligence.service.NewsHeatTrendService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/news-heat-trend")
public class NewsHeatTrendController {

    private final NewsHeatTrendService newsHeatTrendService;

    public NewsHeatTrendController(NewsHeatTrendService newsHeatTrendService) {
        this.newsHeatTrendService = newsHeatTrendService;
    }

    /**
     * 手动触发计算新闻热度趋势，测试用
     * @param newsId 可选参数，传新闻ID只计算该新闻，不传或传空表示全部新闻
     */
    @PostMapping("/refresh")
    public ResponseEntity<String> refresh(@RequestParam(required = false) Integer newsId) {
        newsHeatTrendService.refreshNewsHeatTrend(newsId);
        return ResponseEntity.ok("新闻热度趋势已刷新");
    }

    @GetMapping("/query")
    public List<Map<String, Object>> getNewsHeatTrendBeforeDate(
            @RequestParam int newsId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") java.util.Date referenceDate) {
        return newsHeatTrendService.queryTrendBeforeDate(newsId, new java.sql.Date(referenceDate.getTime()));
    }

    @GetMapping("/top10")
    public HotNewsDTO getTop10ByDay(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date targetDate) {

        return newsHeatTrendService.queryTop10NewsHeatTrendByDay(new java.sql.Date(targetDate.getTime()));
    }

}
