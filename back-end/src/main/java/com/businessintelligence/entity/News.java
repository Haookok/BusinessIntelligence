package com.businessintelligence.entity;

import lombok.Data;
import javax.persistence.*;

@Data
@Entity
@Table(name = "t_news")
public class News {
    @Id
    @Column(name = "news_id")
    private Integer newsId;

    @Column(name = "headline")
    private String headline;

    @Column(name = "content", columnDefinition = "MEDIUMTEXT")
    private String content;

    @Column(name = "category", nullable = false)
    private String category;

    @Column(name = "topic", nullable = false)
    private String topic;

    @Column(name = "total_browse_num", nullable = false)
    private Integer totalBrowseNum;

    @Column(name = "total_browse_duration", nullable = false)
    private Integer totalBrowseDuration;
} 