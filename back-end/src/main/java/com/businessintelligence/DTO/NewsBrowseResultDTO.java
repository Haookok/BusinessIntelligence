package com.businessintelligence.DTO;

import com.businessintelligence.entity.News;
import lombok.Data;
import java.util.List;

@Data
public class NewsBrowseResultDTO {
    private long browseCount;
    private long totalDuration;
    private List<News> newsList;
    private Integer page;
    private Integer size;
    private long totalRecords;
    private Long totalPages; // 添加总页数属性
}
