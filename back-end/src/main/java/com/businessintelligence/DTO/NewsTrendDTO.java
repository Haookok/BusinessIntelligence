package com.businessintelligence.DTO;

import lombok.Data;

import java.time.LocalDate;

@Data
public class NewsTrendDTO {
    private Integer newsId;
    private LocalDate date;
    private Double dailyHeat;
    private Double growthRate;
    private Double comprehensiveHeat;
}