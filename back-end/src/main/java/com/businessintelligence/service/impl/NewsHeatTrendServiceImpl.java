package com.businessintelligence.service.impl;

import com.businessintelligence.DTO.HotNewsItemDTO;
import com.businessintelligence.DTO.HotNewsDTO;
import com.businessintelligence.entity.News;
import com.businessintelligence.repository.NewsBrowseRecordRepository;
import com.businessintelligence.repository.NewsRepository;
import com.businessintelligence.repository.NewsTrendRepository;
import com.businessintelligence.service.NewsHeatTrendService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class NewsHeatTrendServiceImpl implements NewsHeatTrendService {

    private final NewsTrendRepository newsTrendRepository;
    private final NewsBrowseRecordRepository newsBrowseRecordRepository;
    private final NewsRepository newsRepository;

    public NewsHeatTrendServiceImpl(NewsTrendRepository newsTrendRepository,
                                    NewsBrowseRecordRepository newsBrowseRecordRepository, NewsRepository newsRepository) {
        this.newsTrendRepository = newsTrendRepository;
        this.newsBrowseRecordRepository = newsBrowseRecordRepository;
        this.newsRepository = newsRepository;
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
    public HotNewsDTO queryTop10NewsHeatTrendByDay(Date targetDate) {
        List<Map<String, Object>> rawData = newsTrendRepository.findTop10NewsByComprehensiveHeat(targetDate);

        List<HotNewsItemDTO> hotNewsList = new ArrayList<>();
        Date date = null;

        for (Map<String, Object> map : rawData) {
            if (date == null) {
                date = (Date) map.get("date");
            }

            Integer newsId = (Integer) map.get("newsId");
            News news = newsRepository.findById(newsId).orElse(null);
            if (news != null) {
                HotNewsItemDTO itemDTO = new HotNewsItemDTO();
                itemDTO.setNews(news);
                itemDTO.setDailyHeat(map.get("dailyHeat") != null ? Double.parseDouble(map.get("dailyHeat").toString()) : null);
                itemDTO.setGrowthRate(map.get("growthRate") != null ? Double.parseDouble(map.get("growthRate").toString()) : null);
                itemDTO.setComprehensiveHeat(map.get("comprehensiveHeat") != null ? Double.parseDouble(map.get("comprehensiveHeat").toString()) : null);
                hotNewsList.add(itemDTO);
            }
        }

        HotNewsDTO dto = new HotNewsDTO();
        dto.setDate(date);
        dto.setHotNewsList(hotNewsList);
        return dto;
    }

}
