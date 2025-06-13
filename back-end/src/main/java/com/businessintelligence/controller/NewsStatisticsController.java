package com.businessintelligence.controller;

import com.businessintelligence.DTO.NewsBrowseQueryDTO;
import com.businessintelligence.DTO.NewsBrowseResultDTO;
import com.businessintelligence.service.NewsStatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/news/statistics")
@RequiredArgsConstructor
public class NewsStatisticsController {

    private final NewsStatisticsService statisticsService;

    @PostMapping("/browse")
    public NewsBrowseResultDTO queryBrowse(@RequestBody NewsBrowseQueryDTO dto) {
        return statisticsService.queryBrowseStatistics(dto);
    }
}
