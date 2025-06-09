package com.businessintelligence.repository;

import com.businessintelligence.entity.News;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface NewsRepository extends JpaRepository<News, Integer> {
    
    // 2.1 查询单个新闻的生命周期
    @Query("SELECT n.newsId, n.headline, n.totalBrowseNum, n.totalBrowseDuration " +
           "FROM News n WHERE n.newsId = :newsId")
    Map<String, Object> findNewsLifecycle(@Param("newsId") Integer newsId);

    // 2.2 查询不同类别新闻的变化情况
    @Query("SELECT n.category, COUNT(n) as count, AVG(n.totalBrowseNum) as avgBrowses, " +
           "AVG(n.totalBrowseDuration) as avgDuration " +
           "FROM News n GROUP BY n.category")
    List<Map<String, Object>> findCategoryStatistics();

    // 2.4 按条件组合查询
    @Query("SELECT n FROM News n WHERE " +
           "(:category IS NULL OR n.category = :category) AND " +
           "(:topic IS NULL OR n.topic = :topic) AND " +
           "(:minLength IS NULL OR LENGTH(n.content) >= :minLength) AND " +
           "(:maxLength IS NULL OR LENGTH(n.content) <= :maxLength)")
    List<News> findNewsByMultipleConditions(
            @Param("category") String category,
            @Param("topic") String topic,
            @Param("minLength") Integer minLength,
            @Param("maxLength") Integer maxLength);

    // 2.5 查询爆款新闻特征
    @Query("SELECT n.category, n.topic, AVG(n.totalBrowseNum) as avgBrowses, " +
           "AVG(n.totalBrowseDuration) as avgDuration, " +
           "AVG(LENGTH(n.content)) as avgContentLength " +
           "FROM News n " +
           "WHERE n.totalBrowseNum > (SELECT AVG(n2.totalBrowseNum) FROM News n2) " +
           "GROUP BY n.category, n.topic")
    List<Map<String, Object>> findViralNewsCharacteristics();
} 