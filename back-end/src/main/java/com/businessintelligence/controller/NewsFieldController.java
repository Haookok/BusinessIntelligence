package com.businessintelligence.controller;

import com.businessintelligence.DTO.FieldValuesDTO;
import com.businessintelligence.service.NewsFieldService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/news-fields")
@RequiredArgsConstructor
public class NewsFieldController {

    private final NewsFieldService newsFieldService;

    @GetMapping("/categories")
    public FieldValuesDTO getAllCategories() {
        return newsFieldService.getAllCategories();
    }

    @GetMapping("/topics")
    public FieldValuesDTO getAllTopics() {
        return newsFieldService.getAllTopics();
    }

    @GetMapping("/headlines")
    public FieldValuesDTO getAllHeadlines() {
        return newsFieldService.getAllHeadlines();
    }
}
