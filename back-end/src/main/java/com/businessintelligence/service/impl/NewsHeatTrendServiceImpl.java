package com.businessintelligence.service.impl;

import com.businessintelligence.repository.NewsBrowseRecordRepository;
import com.businessintelligence.repository.NewsTrendRepository;
import com.businessintelligence.service.NewsHeatTrendService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class NewsHeatTrendServiceImpl implements NewsHeatTrendService {

    private final NewsTrendRepository newsTrendRepository;
    private final NewsBrowseRecordRepository newsBrowseRecordRepository;

    public NewsHeatTrendServiceImpl(NewsTrendRepository newsTrendRepository,
                                    NewsBrowseRecordRepository newsBrowseRecordRepository) {
        this.newsTrendRepository = newsTrendRepository;
        this.newsBrowseRecordRepository = newsBrowseRecordRepository;
    }

    @Override
    @Transactional
    public void refreshNewsHeatTrend(Integer newsId) {
        newsTrendRepository.insertOrUpdateAllNewsTrend(newsId);
    }

    @Override
    public void refreshNewsHeatTrendIncremental(long fromTimestamp) {
        // 查出最近一段时间有浏览记录的新闻ID
        List<Integer> activeNewsIds = newsBrowseRecordRepository.findActiveNewsIds(fromTimestamp);
        if (activeNewsIds == null || activeNewsIds.isEmpty()) {
            return; // 没有活跃新闻，直接返回
        }

        for (Integer newsId : activeNewsIds) {
            // 调用自定义SQL接口，传入 newsId 和截止时间
            newsTrendRepository.insertOrUpdateAllNewsTrend(newsId);
        }
    }

    @Override
    public List<Map<String, Object>> queryTrendBeforeDate(int newsId, Date referenceDate) {
        return newsTrendRepository.findTrendBeforeDate(newsId, referenceDate);
    }

    @Override
    public List<Map<String, Object>> queryTop10NewsHeatTrendByDay(Date targetDate) {
        return newsTrendRepository.findTop10NewsByComprehensiveHeat(targetDate);
    }
}
