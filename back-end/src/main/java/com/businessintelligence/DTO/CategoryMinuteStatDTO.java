package com.businessintelligence.DTO;

import lombok.Data;

@Data
public class CategoryMinuteStatDTO {
    private String category;
    private long count;
    private long timestamp; // 每分钟时间戳

    public CategoryMinuteStatDTO(String category, long count, long timestamp) {
        this.category = category;
        this.count = count;
        this.timestamp = timestamp;
    }
}
