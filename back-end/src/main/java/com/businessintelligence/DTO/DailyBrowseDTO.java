package com.businessintelligence.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DailyBrowseDTO {
    private String date; // yyyy-MM-dd
    private Long count;
}
