package com.businessintelligence.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

@Entity
@Table(name = "t_news_heat_trend")
@IdClass(NewsHeatTrendEntity.NewsHeatTrendId.class)
public class NewsHeatTrendEntity {

    @Id
    @Column(name = "news_id")
    private Integer newsId;

    @Id
    @Temporal(TemporalType.DATE)
    @Column(name = "date")
    private Date date;

    @Column(name = "daily_heat")
    private Double dailyHeat;

    @Column(name = "growth_rate")
    private Double growthRate;

    @Column(name = "comprehensive_heat")
    private Double comprehensiveHeat;

    public NewsHeatTrendEntity() {}

    // Getter和Setter

    public Integer getNewsId() {
        return newsId;
    }

    public void setNewsId(Integer newsId) {
        this.newsId = newsId;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Double getDailyHeat() {
        return dailyHeat;
    }

    public void setDailyHeat(Double dailyHeat) {
        this.dailyHeat = dailyHeat;
    }

    public Double getGrowthRate() {
        return growthRate;
    }

    public void setGrowthRate(Double growthRate) {
        this.growthRate = growthRate;
    }

    public Double getComprehensiveHeat() {
        return comprehensiveHeat;
    }

    public void setComprehensiveHeat(Double comprehensiveHeat) {
        this.comprehensiveHeat = comprehensiveHeat;
    }

    // 复合主键类
    public static class NewsHeatTrendId implements Serializable {
        private Integer newsId;
        private Date date;

        public NewsHeatTrendId() {}

        public NewsHeatTrendId(Integer newsId, Date date) {
            this.newsId = newsId;
            this.date = date;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof NewsHeatTrendId)) return false;
            NewsHeatTrendId that = (NewsHeatTrendId) o;
            return Objects.equals(newsId, that.newsId) && Objects.equals(date, that.date);
        }

        @Override
        public int hashCode() {
            return Objects.hash(newsId, date);
        }
    }
}
