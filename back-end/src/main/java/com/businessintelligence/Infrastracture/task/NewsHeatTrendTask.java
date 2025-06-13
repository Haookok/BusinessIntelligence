package com.businessintelligence.Infrastracture.task;

import com.businessintelligence.service.NewsHeatTrendService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class NewsHeatTrendTask {

    private final NewsHeatTrendService newsHeatTrendService;

    // 用来保存上次执行时间，单位秒
    private long lastRefreshTimestamp;

    public NewsHeatTrendTask(NewsHeatTrendService newsHeatTrendService) {
        this.newsHeatTrendService = newsHeatTrendService;
        // 初始化为当前时间减一分钟，防止首次无效
        this.lastRefreshTimestamp = System.currentTimeMillis() / 1000 - 60;
    }

    // 每分钟执行一次
    @Scheduled(cron = "0 * * * * ?")
    public void scheduledIncrementalRefresh() {
        System.out.println("[定时任务] 执行增量刷新：" );
        newsHeatTrendService.refreshNewsHeatTrendIncremental(lastRefreshTimestamp);
    }
}
