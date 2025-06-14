package com.businessintelligence.DTO;

import com.businessintelligence.entity.News;
import lombok.Data;

@Data
public class HotNewsItemDTO {
    private News news;
    private Double dailyHeat;
    private Double growthRate;
    private Double comprehensiveHeat;
}
