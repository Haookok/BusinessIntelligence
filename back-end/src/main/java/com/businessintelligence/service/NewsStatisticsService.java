package com.businessintelligence.service;

import com.businessintelligence.DTO.NewsBrowseQueryDTO;
import com.businessintelligence.Infrastracture.page.PageResult;
import com.businessintelligence.entity.News;

public interface NewsStatisticsService {
    PageResult<News> queryBrowseStatistics(NewsBrowseQueryDTO dto);
}
