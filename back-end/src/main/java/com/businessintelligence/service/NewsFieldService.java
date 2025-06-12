package com.businessintelligence.service;

import com.businessintelligence.DTO.FieldValuesDTO;

public interface NewsFieldService {
    FieldValuesDTO getAllCategories();
    FieldValuesDTO getAllTopics();
    FieldValuesDTO getAllHeadlines();
}
