package com.businessintelligence.entity;

import lombok.Data;
import jakarta.persistence.*;

@Data
@Entity
@Table(name = "t_news_browse_record")
public class NewsBrowseRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "news_id", nullable = false)
    private Integer newsId;

    @Column(name = "start_ts", nullable = false)
    private Integer startTs;

    @Column(name = "duration", nullable = false)
    private Integer duration;

    @Column(name = "start_day", nullable = false)
    private Integer startDay;

    @ManyToOne
    @JoinColumn(name = "news_id", insertable = false, updatable = false)
    private News news;
} 