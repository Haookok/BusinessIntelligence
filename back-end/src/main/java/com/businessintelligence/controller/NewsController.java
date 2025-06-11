package com.businessintelligence.controller;

import com.businessintelligence.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/news")
public class NewsController {

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

} 