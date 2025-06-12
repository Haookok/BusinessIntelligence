package com.businessintelligence.service.impl;

import com.businessintelligence.DTO.FieldValuesDTO;
import com.businessintelligence.service.NewsFieldService;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NewsFieldServiceImpl implements NewsFieldService {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public FieldValuesDTO getAllCategories() {
        List<String> categories = jdbcTemplate.queryForList(
                "SELECT DISTINCT category FROM t_news", String.class);
        FieldValuesDTO dto = new FieldValuesDTO();
        dto.setValues(categories);
        return dto;
    }

    @Override
    public FieldValuesDTO getAllTopics() {
        List<String> topics = jdbcTemplate.queryForList(
                "SELECT DISTINCT topic FROM t_news", String.class);
        FieldValuesDTO dto = new FieldValuesDTO();
        dto.setValues(topics);
        return dto;
    }

    @Override
    public FieldValuesDTO getAllHeadlines() {
        List<String> headlines = jdbcTemplate.queryForList(
                "SELECT DISTINCT headline FROM t_news", String.class);
        FieldValuesDTO dto = new FieldValuesDTO();
        dto.setValues(headlines);
        return dto;
    }
}
