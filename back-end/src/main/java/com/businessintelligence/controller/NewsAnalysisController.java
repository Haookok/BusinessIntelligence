package com.businessintelligence.controller;

import com.businessintelligence.entity.*;
import com.businessintelligence.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/analysis")
public class NewsAnalysisController {

    @Autowired
    private NewsRepository newsRepository;

    @Autowired
    private NewsBrowseRecordRepository browseRecordRepository;

    @Autowired
    private QueryLogRepository queryLogRepository;

    // 2.1 查询单个新闻生命周期
    @GetMapping("/news/{newsId}/lifecycle")
    public ResponseEntity<?> getNewsLifecycle(@PathVariable Integer newsId) {
        return ResponseEntity.ok(newsRepository.findNewsLifecycle(newsId));
    }

    // 2.2 查询新闻类别统计
    @GetMapping("/category/statistics")
    public ResponseEntity<?> getCategoryStatistics() {
        return ResponseEntity.ok(newsRepository.findCategoryStatistics());
    }

    // 2.3 查询用户兴趣变化
    @GetMapping("/user/{userId}/interests")
    public ResponseEntity<?> getUserInterests(@PathVariable Integer userId) {
        return ResponseEntity.ok(browseRecordRepository.findUserInterestChanges(userId));
    }

    // 2.4 多条件组合查询
    @GetMapping("/news/search")
    public ResponseEntity<?> searchNews(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String topic,
            @RequestParam(required = false) Integer minLength,
            @RequestParam(required = false) Integer maxLength) {
        return ResponseEntity.ok(newsRepository.findNewsByMultipleConditions(
                category, topic, minLength, maxLength));
    }

    // 2.5 查询爆款新闻特征
    @GetMapping("/news/viral-characteristics")
    public ResponseEntity<?> getViralNewsCharacteristics() {
        return ResponseEntity.ok(newsRepository.findViralNewsCharacteristics());
    }

    // 2.6 获取新闻推荐
    @GetMapping("/news/recommendations/{userId}")
    public ResponseEntity<?> getNewsRecommendations(
            @PathVariable Integer userId,
            @RequestParam Integer startTime) {
        return ResponseEntity.ok(browseRecordRepository.findRecommendedNews(userId, startTime));
    }

} 