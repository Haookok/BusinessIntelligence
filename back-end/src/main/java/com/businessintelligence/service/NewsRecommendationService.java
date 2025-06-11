package com.businessintelligence.service;

import com.businessintelligence.DTO.RecommendRequest;
import com.businessintelligence.entity.News;

import java.util.List;

public interface NewsRecommendationService {
    List<News> getRecommendations(RecommendRequest request);
}