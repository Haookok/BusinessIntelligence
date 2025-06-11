package com.businessintelligence.controller;

import com.businessintelligence.DTO.RecommendRequest;
import com.businessintelligence.entity.News;
import com.businessintelligence.service.NewsRecommendationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.OPTIONS})
@RequestMapping("/api/recommendations")
public class NewsRecommendationController {

    private final NewsRecommendationService recommendationService;

    public NewsRecommendationController(NewsRecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @PostMapping
    public List<News> getRecommendations(@RequestBody RecommendRequest request) {
        return recommendationService.getRecommendations(request);
    }
}