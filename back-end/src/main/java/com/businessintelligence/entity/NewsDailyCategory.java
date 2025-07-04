package com.businessintelligence.entity;

import lombok.Data;
import jakarta.persistence.*;

@Data
@Entity
@Table(name = "t_news_daily_category")
public class NewsDailyCategory {
    @EmbeddedId
    private NewsDailyCategoryId id;

    @Column(name = "browse_count")
    private Integer browseCount;

    @Column(name = "browse_duration")
    private Integer browseDuration;

    @Data
    @Embeddable
    public static class NewsDailyCategoryId implements java.io.Serializable {
        @Column(name = "category", nullable = false)
        private String category;
    }
} 