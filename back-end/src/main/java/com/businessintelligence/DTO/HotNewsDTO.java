package com.businessintelligence.DTO;

import lombok.Data;

@Data
public class HotNewsDTO {
    private Integer newsId;
    private String category;
    private Long totalBrowseCount;  // 原为Integer，可能因数值过大导致类型不匹配
    private Long totalDuration;     // 原为Integer
    private Double avgBrowseDuration;
    private Double growthRate;
    private Double comprehensiveHeat;

    // getter和setter方法...
}
