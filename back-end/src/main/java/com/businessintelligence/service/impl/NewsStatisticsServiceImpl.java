package com.businessintelligence.service.impl;

import com.businessintelligence.DTO.NewsBrowseQueryDTO;
import com.businessintelligence.DTO.NewsBrowseResultDTO;
import com.businessintelligence.entity.News;
import com.businessintelligence.service.NewsStatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class NewsStatisticsServiceImpl implements NewsStatisticsService {

    private final NamedParameterJdbcTemplate namedJdbcTemplate;

    @Override
    public NewsBrowseResultDTO queryBrowseStatistics(NewsBrowseQueryDTO dto) {
        // 统计SQL
        StringBuilder statSql = new StringBuilder();
        statSql.append("SELECT COUNT(*) AS browse_count, SUM(nbr.duration) AS total_duration ")
                .append("FROM t_news_browse_record nbr ")
                .append("JOIN t_news n ON nbr.news_id = n.news_id WHERE 1=1 ");

        // 新闻列表SQL
        StringBuilder newsSql = new StringBuilder();
        newsSql.append("SELECT n.* FROM t_news n WHERE EXISTS (")
                .append("SELECT 1 FROM t_news_browse_record nbr WHERE nbr.news_id = n.news_id ");

        Map<String, Object> params = new HashMap<>();

        // 拼接条件 - 统计SQL和EXISTS子查询
        if (dto.getStartTime() != null) {
            statSql.append("AND nbr.start_ts >= :startTime ");
            newsSql.append("AND nbr.start_ts >= :startTime ");
            params.put("startTime", dto.getStartTime());
        }
        if (dto.getEndTime() != null) {
            statSql.append("AND nbr.start_ts <= :endTime ");
            newsSql.append("AND nbr.start_ts <= :endTime ");
            params.put("endTime", dto.getEndTime());
        }
        if (dto.getUserId() != null) {
            statSql.append("AND nbr.user_id = :userId ");
            newsSql.append("AND nbr.user_id = :userId ");
            params.put("userId", dto.getUserId());
        }
        if (dto.getUserIdList() != null && !dto.getUserIdList().isEmpty()) {
            statSql.append("AND nbr.user_id IN (:userIdList) ");
            newsSql.append("AND nbr.user_id IN (:userIdList) ");
            params.put("userIdList", dto.getUserIdList());
        }

        newsSql.append(") "); // 关闭EXISTS子查询

        // 拼接条件 - 外层查询
        if (dto.getTopic() != null && !dto.getTopic().isEmpty()) {
            statSql.append("AND n.topic = :topic ");
            newsSql.append("AND n.topic = :topic ");
            params.put("topic", dto.getTopic());
        }
        if (dto.getMinTitleLength() != null) {
            newsSql.append("AND LENGTH(n.headline) >= :minTitleLength ");
            params.put("minTitleLength", dto.getMinTitleLength());
        }
        if (dto.getMaxTitleLength() != null) {
            newsSql.append("AND LENGTH(n.headline) <= :maxTitleLength ");
            params.put("maxTitleLength", dto.getMaxTitleLength());
        }
        if (dto.getMinContentLength() != null) {
            newsSql.append("AND LENGTH(n.content) >= :minContentLength ");
            params.put("minContentLength", dto.getMinContentLength());
        }
        if (dto.getMaxContentLength() != null) {
            newsSql.append("AND LENGTH(n.content) <= :maxContentLength ");
            params.put("maxContentLength", dto.getMaxContentLength());
        }

        Integer pageVal = dto.getPage();
        int page = (pageVal != null && pageVal > 0) ? pageVal : 1;

        Integer sizeVal = dto.getSize();
        int size = (sizeVal != null && sizeVal > 0) ? sizeVal : 10;

        int offset = (page - 1) * size;


        newsSql.append(" ORDER BY n.news_id DESC LIMIT :limit OFFSET :offset ");
        params.put("limit", size);
        params.put("offset", offset);

        // 查询统计数据
        Map<String, Object> resultMap = namedJdbcTemplate.queryForMap(statSql.toString(), params);

        // 查询新闻列表，映射为实体
        List<News> newsList = namedJdbcTemplate.query(newsSql.toString(), params, (ResultSet rs, int rowNum) -> {
            News news = new News();
            news.setNewsId(rs.getInt("news_id"));
            news.setHeadline(rs.getString("headline"));
            news.setContent(rs.getString("content"));
            news.setCategory(rs.getString("category"));
            news.setTopic(rs.getString("topic"));
            news.setTotalBrowseNum(rs.getInt("total_browse_num"));
            news.setTotalBrowseDuration(rs.getInt("total_browse_duration"));
            // 根据你的实体，继续设置其他字段
            return news;
        });

        // 构造返回DTO
        NewsBrowseResultDTO result = new NewsBrowseResultDTO();
        result.setBrowseCount(((Number) resultMap.getOrDefault("browse_count", 0)).longValue());
        result.setTotalDuration(((Number) resultMap.getOrDefault("total_duration", 0)).longValue());
        result.setNewsList(newsList);
        result.setPage(page);
        result.setSize(size);

        return result;
    }
}
