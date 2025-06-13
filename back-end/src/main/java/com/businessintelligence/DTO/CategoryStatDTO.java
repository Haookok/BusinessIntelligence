package com.businessintelligence.DTO;
import lombok.Data;

@Data
public class CategoryStatDTO {
    private String category;
    private long count;
    private long timestamp;

    public CategoryStatDTO( String category, long count, long timestamp) {
        this.category = category;
        this.count = count;
        this.timestamp = timestamp;
    }
}


