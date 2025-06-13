package com.businessintelligence.service.impl;
import com.businessintelligence.DTO.NewsQueryDTO;
import com.businessintelligence.DTO.RecommendRequest;
import com.businessintelligence.entity.NewsBrowseRecord;
import com.businessintelligence.entity.News;
import com.businessintelligence.repository.NewsRepository;
import com.businessintelligence.service.BrowseRecordService;
import com.businessintelligence.Infrastracture.util.DatabaseQueryExecutor;
import com.businessintelligence.Infrastracture.client.LLMClient;
import com.businessintelligence.service.NewsRecommendationService;
import com.businessintelligence.service.NewsService;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
@Service
public class NewsServiceImpl implements NewsService {

    private final NewsRepository newsRepository;

    public NewsServiceImpl(NewsRepository newsRepository) {
        this.newsRepository = newsRepository;
    }


}

