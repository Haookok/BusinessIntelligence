package com.businessintelligence.DTO;

import com.businessintelligence.DTO.HotNewsItemDTO;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class HotNewsDTO {
    private Date date;
    private List<HotNewsItemDTO> hotNewsList;
}
