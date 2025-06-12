// src/main/java/com/businessintelligence/service/impl/NewsAnalysisServiceImpl.java
package com.businessintelligence.service.impl;

import com.businessintelligence.DTO.HotNewsDTO;
import com.businessintelligence.DTO.NewsTrendDTO;
import com.businessintelligence.repository.NewsAnalysisRepository;
import com.businessintelligence.service.NewsAnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
public class NewsAnalysisServiceImpl implements NewsAnalysisService {

    @Autowired
    private NewsAnalysisRepository newsAnalysisRepository;
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(NewsRecommendationServiceImpl.class);


    @Override
    public List<HotNewsDTO> getTop10HotNews(long timestamp) {
        log.info("查询综合热度最高的10个新闻，时间戳: {}", timestamp);

        List<Map<String, Object>> resultMaps = newsAnalysisRepository.findTop10HotNews(timestamp);

        log.info("查询结果数量: {}", resultMaps != null ? resultMaps.size() : 0);
        return resultMaps.stream()
                .map(this::mapToHotNewsDTO)
                .collect(Collectors.toList());
    }

    private HotNewsDTO mapToHotNewsDTO(Map<String, Object> map) {
        HotNewsDTO dto = new HotNewsDTO();

        dto.setNewsId(getInteger(map, "news_id"));
        dto.setCategory(getString(map, "category"));
        dto.setTotalBrowseCount(getLong(map, "total_browse_count"));
        dto.setTotalDuration(getLong(map, "total_duration"));
        dto.setAvgBrowseDuration(getDouble(map, "avg_browse_duration"));
        dto.setGrowthRate(getDouble(map, "growth_rate"));
        dto.setComprehensiveHeat(getDouble(map, "comprehensive_heat"));

        return dto;
    }

    // 类型转换工具方法
    private Integer getInteger(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof BigDecimal) {
            return ((BigDecimal) value).intValue();
        } else if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return null;
    }

    private Long getLong(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof BigDecimal) {
            return ((BigDecimal) value).longValue();
        } else if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return null;
    }

    private Double getDouble(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof BigDecimal) {
            return ((BigDecimal) value).doubleValue();
        } else if (value instanceof Number) {
            return ((Number) value).doubleValue();
        } else if (value instanceof String) {
            try {
                return Double.parseDouble((String) value);
            } catch (NumberFormatException e) {
                log.error("数值转换错误，字段: {}, 值: {}", key, value, e);
            }
        }
        return 0.0;
    }

    private String getString(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : null;
    }

    @Override
    public List<NewsTrendDTO> getNewsTrend(int newsId, long timestamp) {
        List<Map<String, Object>> resultMaps = newsAnalysisRepository.findNewsTrend(newsId, timestamp);

        return resultMaps.stream()
                .map(map -> {
                    NewsTrendDTO dto = new NewsTrendDTO();
                    dto.setNewsId((Integer) map.get("news_id"));

                    // 处理日期转换
                    if (map.get("date") instanceof java.sql.Date) {
                        dto.setDate(((java.sql.Date) map.get("date")).toLocalDate());
                    } else if (map.get("date") instanceof LocalDate) {
                        dto.setDate((LocalDate) map.get("date"));
                    }

                    // 处理BigDecimal到Double的转换
                    dto.setDailyHeat(
                            map.get("daily_heat") != null ?
                                    ((BigDecimal) map.get("daily_heat")).doubleValue() : 0.0
                    );

                    dto.setGrowthRate(
                            map.get("growth_rate") != null ?
                                    ((BigDecimal) map.get("growth_rate")).doubleValue() : 0.0
                    );

                    dto.setComprehensiveHeat(
                            map.get("comprehensive_heat") != null ?
                                    ((BigDecimal) map.get("comprehensive_heat")).doubleValue() : 0.0
                    );

                    return dto;
                })
                .collect(Collectors.toList());
    }
}