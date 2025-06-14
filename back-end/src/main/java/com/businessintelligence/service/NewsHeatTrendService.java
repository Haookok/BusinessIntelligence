package com.businessintelligence.service;

import com.businessintelligence.DTO.HotNewsDTO;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface NewsHeatTrendService {
    //计算并写入所有新闻热度趋势数据
    void refreshNewsHeatTrend(Integer newsId);
    void refreshNewsHeatTrendIncremental(long fromTimestamp);
    //特定新闻十天内热度变化
    List<Map<String, Object>> queryTrendBeforeDate(int newsId, Date referenceDate);
    //最热的10个新闻
    HotNewsDTO queryTop10NewsHeatTrendByDay(Date targetDate);
}