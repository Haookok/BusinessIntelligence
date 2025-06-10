package com.businessintelligence.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
public class SecondlyBrowseDTO {
    private long timestamp;
    private long count;

    public SecondlyBrowseDTO(long timestamp, long count) {
        this.timestamp = timestamp;
        this.count = count;
    }
}