package com.businessintelligence.repository;

import com.businessintelligence.entity.News;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NewsLiveRepository extends JpaRepository<News, Long> {

    /**
     * 查询指定新闻 ID 的浏览数据，返回从指定时间戳之后的每秒浏览次数。
     * 查询结果按分钟聚合分段，但仍统计每秒数据（按 second_ts 分组）。
     *
     */
    @Query(value = """
    SELECT 
        FLOOR(start_ts / 60) * 60 AS minute_ts,
        COUNT(*) as count
    FROM t_news_browse_record
    WHERE news_id = :newsId
    GROUP BY minute_ts
    ORDER BY minute_ts
    """, nativeQuery = true)
    List<Object[]> getSecondlyBrowseInitial(@Param("newsId") Integer newsId);

    @Query(value = """
    SELECT 
        FLOOR(start_ts / 60) * 60 AS minute_ts,
        COUNT(*) as count
    FROM t_news_browse_record
    WHERE news_id = :newsId AND start_ts > FROM_UNIXTIME(:since)
    GROUP BY minute_ts
    ORDER BY minute_ts
    """, nativeQuery = true)
    List<Object[]> getSecondlyBrowseSince(@Param("newsId") Integer newsId, @Param("since") long since);

}
