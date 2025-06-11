package com.businessintelligence.repository;

import com.businessintelligence.entity.News;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface NewsAnalysisRepository extends JpaRepository<News, Long> {

    // 查询综合热度最高的10个新闻
    @Query(value = "WITH " +
            "base_data AS (" +
            "    SELECT " +
            "        b.news_id, " +
            "        n.category, " +
            "        FROM_UNIXTIME(b.start_ts, '%Y-%m-%d') AS date, " +
            "        b.duration, " +
            "        b.user_id " +
            "    FROM t_news_browse_record b " +
            "    JOIN t_news n ON b.news_id = n.news_id " +
            "    WHERE " +
            "        b.start_ts < ?1 " +  // 指定时间点的时间戳
            "        AND b.start_ts >= (?1 - 10 * 24 * 60 * 60) " +  // 前10天 " +
            "), " +
            "daily_metrics AS (" +
            "    SELECT " +
            "        news_id, " +
            "        category, " +
            "        date, " +
            "        COUNT(DISTINCT user_id) AS daily_browse_count, " +
            "        SUM(duration) AS daily_duration " +
            "    FROM base_data " +
            "    GROUP BY news_id, category, date " +
            "), " +
            "daily_heat AS (" +
            "    SELECT " +
            "        news_id, " +
            "        category, " +
            "        date, " +
            "        daily_browse_count * 0.5 + " +  // 浏览人数权重50%
            "        (daily_duration / NULLIF(daily_browse_count, 0) / 60) * 0.3 + " +  // 人均时长权重30%，避免除零错误
            "        1 AS daily_heat " +  // 基础热度值
            "    FROM daily_metrics " +
            "), " +
            "news_agg_metrics AS (" +
            "    SELECT " +
            "        news_id, " +
            "        category, " +
            "        SUM(daily_browse_count) AS total_browse_count, " +
            "        SUM(daily_duration) AS total_duration, " +
            "        AVG(daily_duration / NULLIF(daily_browse_count, 0)) AS avg_browse_duration, " +
            "        COUNT(DISTINCT date) AS active_days " +
            "    FROM daily_metrics " +
            "    GROUP BY news_id, category " +
            "), " +
            "heat_growth AS (" +
            "    SELECT " +
            "        a.news_id, " +
            "        a.date AS curr_date, " +  // 避免使用关键字current_date
            "        b.date AS prev_date, " +
            "        a.daily_heat, " +
            "        b.daily_heat AS prev_heat, " +
            "        IFNULL((a.daily_heat - IFNULL(b.daily_heat, 0)) / NULLIF(b.daily_heat, 0), 0) AS daily_growth_rate " +
            "    FROM daily_heat a " +
            "    LEFT JOIN daily_heat b ON " +
            "        a.news_id = b.news_id " +
            "        AND b.date = DATE_SUB(a.date, INTERVAL 1 DAY) " +  // 使用DATE_SUB替代DATE_ADD，更清晰
            "), " +
            "news_growth_rate AS (" +
            "    SELECT " +
            "        news_id, " +
            "        AVG(daily_growth_rate) AS avg_growth_rate " +
            "    FROM heat_growth " +
            "    GROUP BY news_id " +
            ") " +
            "SELECT " +
            "    m.news_id, " +
            "    m.category, " +
            "    m.total_browse_count, " +
            "    m.total_duration, " +
            "    m.avg_browse_duration, " +
            "    g.avg_growth_rate AS growth_rate, " +
            "    g.avg_growth_rate * 50 + " +  // 增长率权重50%
            "    (m.total_browse_count / 1000) * 0.3 + " +  // 总浏览量权重30%
            "    m.avg_browse_duration * 0.2 AS comprehensive_heat " +  // 平均时长权重20%
            "FROM news_agg_metrics m " +
            "JOIN news_growth_rate g ON m.news_id = g.news_id " +
            "ORDER BY comprehensive_heat DESC " +
            "LIMIT 10", nativeQuery = true)
    List<Map<String, Object>> findTop10HotNews(long timestamp);

    // 查询指定新闻在特定时间点前10天的热度变化趋势
    @Query(value = "WITH " +
            "base_data AS (" +
            "    SELECT " +
            "        b.news_id, " +
            "        FROM_UNIXTIME(b.start_ts, '%Y-%m-%d') AS date, " +
            "        b.duration, " +
            "        b.user_id " +
            "    FROM t_news_browse_record b " +
            "    WHERE " +
            "        b.news_id = ?1 " +  // 指定新闻ID
            "        AND b.start_ts < ?2 " +  // 指定时间点的时间戳
            "        AND b.start_ts >= (?2 - 10 * 24 * 60 * 60) " +  // 前10天 " +
            "), " +
            "daily_metrics AS (" +
            "    SELECT " +
            "        news_id, " +
            "        date, " +
            "        COUNT(DISTINCT user_id) AS daily_browse_count, " +
            "        SUM(duration) AS daily_duration " +
            "    FROM base_data " +
            "    GROUP BY news_id, date " +
            "), " +
            "daily_heat AS (" +
            "    SELECT " +
            "        news_id, " +
            "        date, " +
            "        daily_browse_count * 0.5 + " +  // 浏览人数权重50%
            "        (daily_duration / NULLIF(daily_browse_count, 0) / 60) * 0.3 + " +  // 人均时长权重30%，避免除零错误
            "        1 AS daily_heat " +  // 基础热度值
            "    FROM daily_metrics " +
            "), " +
            "heat_growth AS (" +
            "    SELECT " +
            "        a.news_id, " +
            "        a.date AS curr_date, " +  // 避免使用关键字current_date
            "        b.date AS prev_date, " +
            "        a.daily_heat, " +
            "        b.daily_heat AS prev_heat, " +
            "        IFNULL((a.daily_heat - IFNULL(b.daily_heat, 0)) / NULLIF(b.daily_heat, 0), 0) AS daily_growth_rate " +
            "    FROM daily_heat a " +
            "    LEFT JOIN daily_heat b ON " +
            "        a.news_id = b.news_id " +
            "        AND b.date = DATE_SUB(a.date, INTERVAL 1 DAY) " +  // 使用DATE_SUB替代DATE_ADD，更清晰
            "), " +
            "daily_comprehensive_heat AS (" +
            "    SELECT " +
            "        h.news_id, " +
            "        h.curr_date AS date, " +  // 保持一致的字段名
            "        h.daily_heat, " +
            "        h.daily_growth_rate, " +
            "        h.daily_growth_rate * 50 + " +  // 增长率权重50%
            "        (m.daily_browse_count / 100) * 0.3 + " +  // 日浏览量权重30%
            "        (m.daily_duration / NULLIF(m.daily_browse_count, 0)) * 0.2 AS comprehensive_heat " +  // 日均时长权重20%，避免除零错误
            "    FROM heat_growth h " +
            "    JOIN daily_metrics m ON h.news_id = m.news_id AND h.curr_date = m.date " +
            ") " +
            "SELECT " +
            "    news_id, " +
            "    STR_TO_DATE(date, '%Y-%m-%d') AS date, " +
            "    daily_heat, " +
            "    daily_growth_rate AS growth_rate, " +
            "    comprehensive_heat " +
            "FROM daily_comprehensive_heat " +
            "ORDER BY date ASC", nativeQuery = true)
    List<Map<String, Object>> findNewsTrend(int newsId, long timestamp);
}