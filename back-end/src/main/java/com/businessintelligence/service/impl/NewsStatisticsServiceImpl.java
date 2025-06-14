package com.businessintelligence.service.impl;

import com.businessintelligence.DTO.NewsBrowseQueryDTO;
import com.businessintelligence.Infrastracture.page.PageResult;
import com.businessintelligence.entity.News;
import com.businessintelligence.service.NewsStatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class NewsStatisticsServiceImpl implements NewsStatisticsService {

    private final NamedParameterJdbcTemplate namedJdbcTemplate;

    @Override
    public PageResult<News> queryBrowseStatistics(NewsBrowseQueryDTO dto) {
        // 构造参数
        Map<String, Object> params = new HashMap<>();

        // WHERE 条件（用于 count 查询和列表查询）
        StringBuilder whereSql = new StringBuilder();
        whereSql.append("WHERE EXISTS (")
                .append("SELECT 1 FROM t_news_browse_record nbr WHERE nbr.news_id = n.news_id ");

        if (dto.getStartTime() != null) {
            whereSql.append("AND nbr.start_ts >= :startTime ");
            params.put("startTime", dto.getStartTime());
        }
        if (dto.getEndTime() != null) {
            whereSql.append("AND nbr.start_ts <= :endTime ");
            params.put("endTime", dto.getEndTime());
        }
        if (dto.getUserId() != null) {
            whereSql.append("AND nbr.user_id = :userId ");
            params.put("userId", dto.getUserId());
        }
        if (dto.getUserIdList() != null && !dto.getUserIdList().isEmpty()) {
            whereSql.append("AND nbr.user_id IN (:userIdList) ");
            params.put("userIdList", dto.getUserIdList());
        }

        whereSql.append(") "); // 关闭 EXISTS

        // 外层条件
        if (dto.getTopic() != null && !dto.getTopic().isEmpty()) {
            whereSql.append("AND n.topic = :topic ");
            params.put("topic", dto.getTopic());
        }
        if (dto.getMinTitleLength() != null) {
            whereSql.append("AND LENGTH(n.headline) >= :minTitleLength ");
            params.put("minTitleLength", dto.getMinTitleLength());
        }
        if (dto.getMaxTitleLength() != null) {
            whereSql.append("AND LENGTH(n.headline) <= :maxTitleLength ");
            params.put("maxTitleLength", dto.getMaxTitleLength());
        }
        if (dto.getMinContentLength() != null) {
            whereSql.append("AND LENGTH(n.content) >= :minContentLength ");
            params.put("minContentLength", dto.getMinContentLength());
        }
        if (dto.getMaxContentLength() != null) {
            whereSql.append("AND LENGTH(n.content) <= :maxContentLength ");
            params.put("maxContentLength", dto.getMaxContentLength());
        }

        // 获取分页参数
        int page = (dto.getPage() != null && dto.getPage() > 0) ? dto.getPage() : 1;
        int size = (dto.getSize() != null && dto.getSize() > 0) ? dto.getSize() : 20;
        int offset = (page - 1) * size;

        // 查询总条数
        String countSql = "SELECT COUNT(*) FROM t_news n " + whereSql;
        long total = namedJdbcTemplate.queryForObject(countSql, params, Long.class);

        // 查询列表
        String dataSql = "SELECT n.* FROM t_news n " + whereSql +
                " ORDER BY n.news_id DESC LIMIT :limit OFFSET :offset";
        params.put("limit", size);
        params.put("offset", offset);

        List<News> newsList = namedJdbcTemplate.query(dataSql, params, (ResultSet rs, int rowNum) -> {
            News news = new News();
            news.setNewsId(rs.getInt("news_id"));
            news.setHeadline(rs.getString("headline"));
            news.setContent(rs.getString("content"));
            news.setCategory(rs.getString("category"));
            news.setTopic(rs.getString("topic"));
            news.setTotalBrowseNum(rs.getInt("total_browse_num"));
            news.setTotalBrowseDuration(rs.getInt("total_browse_duration"));
            return news;
        });

        return new PageResult<>(total, page, size, newsList);
    }

}
