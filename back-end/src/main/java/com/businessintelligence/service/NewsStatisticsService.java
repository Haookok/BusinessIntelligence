package com.businessintelligence.service;

import com.businessintelligence.DTO.NewsBrowseQueryDTO;
import com.businessintelligence.DTO.NewsBrowseResultDTO;

public interface NewsStatisticsService {
    NewsBrowseResultDTO queryBrowseStatistics(NewsBrowseQueryDTO dto);
}
