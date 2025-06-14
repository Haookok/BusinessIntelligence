package com.businessintelligence.DTO;

import lombok.Data;

import java.util.List;

@Data
public class NewsBrowseQueryDTO {
    private Long startTime;            // 起始时间戳
    private Long endTime;              // 结束时间戳
    private String topic;              // 新闻主题
    private Integer minTitleLength;    // 最小标题长度
    private Integer maxTitleLength;    // 最大标题长度
    private Integer minContentLength;  // 最小内容长度
    private Integer maxContentLength;  // 最大内容长度
    private Integer userId;            // 指定用户
    private List<Integer> userIdList;  // 多个用户
    private Integer page;
    private Integer size;

}
