package com.businessintelligence.DTO;

import lombok.Data;

@Data
public class MinutelyBrowseDTO {
    private long timestamp;
    private long count;

    public MinutelyBrowseDTO(long timestamp, long count) {
        this.timestamp = timestamp;
        this.count = count;
    }
}