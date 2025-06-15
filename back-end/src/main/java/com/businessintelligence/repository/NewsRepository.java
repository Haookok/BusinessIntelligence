package com.businessintelligence.repository;

import com.businessintelligence.DTO.*;
import com.businessintelligence.entity.News;
import com.businessintelligence.entity.NewsBrowseRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository


public interface NewsRepository extends JpaRepository<News, Integer>, JpaSpecificationExecutor<News> {
    //test：秒级浏览量变化情况

    @Query(value = "SELECT start_ts, COUNT(*) as count " +
            "FROM t_news_browse_record " +
            "WHERE news_id = :newsId " +
            "GROUP BY start_ts " +
            "ORDER BY start_ts",
            nativeQuery = true)
    List<Object[]> getSecondlyBrowseByNewsId(@Param("newsId") Integer newsId);


    List<News> findByNewsIdIn(List<Integer> newsIds);
    @Query("SELECT new com.businessintelligence.DTO.NewsLifecycleDTO(n.newsId, n.headline, n.totalBrowseNum, n.totalBrowseDuration) " +
            "FROM News n WHERE n.newsId = :newsId")
    NewsLifecycleDTO findNewsLifecycle(@Param("newsId") Integer newsId);


}
