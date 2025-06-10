package com.businessintelligence.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@IdClass(NewsBrowseRecordId.class) // 💡 关键：声明使用联合主键类
@Table(name = "t_news_browse_record")
public class NewsBrowseRecord {

    @Id
    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Id
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
