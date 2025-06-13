package com.businessintelligence.repository;

import com.businessintelligence.entity.NewsHeatTrendEntity;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Repository
public interface NewsTrendRepository extends JpaRepository<NewsHeatTrendEntity, NewsHeatTrendEntity.NewsHeatTrendId> {
    //冗余表插入和更新
    @Modifying(clearAutomatically = true)
    @Transactional
    @Query(value =
            "INSERT INTO t_news_heat_trend (news_id, date, daily_heat, growth_rate, comprehensive_heat) " +
                    "WITH " +
                    "base_data AS ( " +
                    "    SELECT " +
                    "        b.news_id, " +
                    "        FROM_UNIXTIME(b.start_ts, '%Y-%m-%d') AS date, " +
                    "        b.duration, " +
                    "        b.user_id " +
                    "    FROM t_news_browse_record b " +
                    "    WHERE " +
                    "        (:newsId IS NULL OR b.news_id = :newsId) " +  // 如果传null就查所有新闻
                    "), " +
                    "daily_metrics AS ( " +
                    "    SELECT " +
                    "        news_id, " +
                    "        date, " +
                    "        COUNT(DISTINCT user_id) AS daily_browse_count, " +
                    "        SUM(duration) AS daily_duration " +
                    "    FROM base_data " +
                    "    GROUP BY news_id, date " +
                    "), " +
                    "daily_heat AS ( " +
                    "    SELECT " +
                    "        news_id, " +
                    "        date, " +
                    "        daily_browse_count * 0.5 + " +
                    "        (daily_duration / NULLIF(daily_browse_count, 0) / 60) * 0.3 + " +
                    "        1 AS daily_heat " +
                    "    FROM daily_metrics " +
                    "), " +
                    "heat_growth AS ( " +
                    "    SELECT " +
                    "        a.news_id, " +
                    "        a.date AS curr_date, " +
                    "        b.date AS prev_date, " +
                    "        a.daily_heat, " +
                    "        b.daily_heat AS prev_heat, " +
                    "        IFNULL((a.daily_heat - IFNULL(b.daily_heat, 0)) / NULLIF(b.daily_heat, 0), 0) AS growth_rate " +
                    "    FROM daily_heat a " +
                    "    LEFT JOIN daily_heat b ON " +
                    "        a.news_id = b.news_id " +
                    "        AND b.date = DATE_SUB(a.date, INTERVAL 1 DAY) " +
                    "), " +
                    "daily_comprehensive_heat AS ( " +
                    "    SELECT " +
                    "        h.news_id, " +
                    "        h.curr_date AS date, " +
                    "        h.daily_heat, " +
                    "        h.growth_rate, " +
                    "        h.growth_rate * 50 + " +
                    "        (m.daily_browse_count / 100) * 0.3 + " +
                    "        (m.daily_duration / NULLIF(m.daily_browse_count, 0)) * 0.2 AS comprehensive_heat " +
                    "    FROM heat_growth h " +
                    "    JOIN daily_metrics m ON h.news_id = m.news_id AND h.curr_date = m.date " +
                    ") " +
                    "SELECT " +
                    "    news_id, " +
                    "    STR_TO_DATE(date, '%Y-%m-%d') AS date, " +
                    "    daily_heat, " +
                    "    growth_rate, " +
                    "    comprehensive_heat " +
                    "FROM daily_comprehensive_heat " +
                    "ON DUPLICATE KEY UPDATE " +
                    "daily_heat = VALUES(daily_heat), " +
                    "growth_rate = VALUES(growth_rate), " +
                    "comprehensive_heat = VALUES(comprehensive_heat) ",
            nativeQuery = true)
    void insertOrUpdateAllNewsTrend(@Param("newsId") Integer newsId);
    //查找单只新闻十天内热度趋势
    @Query(value = """
    SELECT 
        news_id AS newsId,
        date,
        daily_heat AS dailyHeat,
        growth_rate AS growthRate,
        comprehensive_heat AS comprehensiveHeat
    FROM t_news_heat_trend
    WHERE news_id = :newsId
      AND date <= :referenceDate
      AND date >= DATE_SUB(:referenceDate, INTERVAL 10 DAY)
    ORDER BY date ASC
""", nativeQuery = true)
    List<Map<String, Object>> findTrendBeforeDate(
            @Param("newsId") int newsId,
            @Param("referenceDate") Date referenceDate
    );
    @Query(value = "SELECT " +
            "   news_id AS newsId, " +
            "   date, " +
            "   daily_heat AS dailyHeat, " +
            "   growth_rate AS growthRate, " +
            "   comprehensive_heat AS comprehensiveHeat " +
            "FROM t_news_heat_trend " +
            "WHERE DATE(date) = :targetDate " +  // ← 加上 DATE()
            "ORDER BY comprehensive_heat DESC " +
            "LIMIT 10", nativeQuery = true)
    List<Map<String, Object>> findTop10NewsByComprehensiveHeat(@Param("targetDate") Date targetDate);

}
