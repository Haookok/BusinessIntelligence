package com.businessintelligence.repository;

import com.businessintelligence.entity.News;
import com.businessintelligence.entity.NewsBrowseRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface NewsBrowseRecordRepository extends JpaRepository<NewsBrowseRecord, Long> {
    
    // 2.3 查询用户兴趣变化
    @Query("SELECT r.userId, n.category, COUNT(r) as browseCount, " +
           "AVG(r.duration) as avgDuration " +
           "FROM NewsBrowseRecord r " +
           "JOIN News n ON r.newsId = n.newsId " +
           "WHERE r.userId = :userId " +
           "GROUP BY r.userId, n.category")
    List<Map<String, Object>> findUserInterestChanges(@Param("userId") Integer userId);

    // 2.6 实时新闻推荐
    @Query("SELECT n FROM News n " +
           "WHERE n.category IN (" +
           "   SELECT DISTINCT n2.category FROM News n2 " +
           "   JOIN NewsBrowseRecord r ON n2.newsId = r.newsId " +
           "   WHERE r.userId = :userId " +
           "   AND r.startTs >= :startTime" +
           ") " +
           "AND n.newsId NOT IN (" +
           "   SELECT r2.newsId FROM NewsBrowseRecord r2 " +
           "   WHERE r2.userId = :userId" +
           ") " +
           "ORDER BY n.totalBrowseNum DESC")
    List<News> findRecommendedNews(@Param("userId") Integer userId, @Param("startTime") Integer startTime);

    // 按时间段统计用户浏览记录
    @Query("SELECT r.userId, n.category, COUNT(r) as browseCount, " +
           "AVG(r.duration) as avgDuration " +
           "FROM NewsBrowseRecord r " +
           "JOIN News n ON r.newsId = n.newsId " +
           "WHERE r.startTs BETWEEN :startTime AND :endTime " +
           "GROUP BY r.userId, n.category")
    List<Map<String, Object>> findUserBrowsingStatsByTimeRange(
            @Param("startTime") Integer startTime,
            @Param("endTime") Integer endTime);


    @Query("SELECT b FROM NewsBrowseRecord b " +
            "WHERE b.userId = ?1 AND b.startTs <= ?2 " +
            "ORDER BY b.startTs DESC")
    List<NewsBrowseRecord> findTop20ByUserIdAndStartTsGreaterThanOrderByStartTsDesc(
            Integer userId, Integer timestamp);
} 