package com.businessintelligence.service;

import com.businessintelligence.config.SqlLogEvent;
import com.businessintelligence.entity.QueryLog;
import com.businessintelligence.repository.QueryLogRepository;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class QueryLogService {

    private final QueryLogRepository queryLogRepository;

    public QueryLogService(QueryLogRepository queryLogRepository) {
        this.queryLogRepository = queryLogRepository;
    }

    @Transactional
    public QueryLog saveLog(Long executionTime, String querySql) {
        QueryLog log = new QueryLog();
        log.setCreatedAt(LocalDateTime.now());
        log.setExecutionTime(executionTime);
        log.setQuerySql(querySql);
        return queryLogRepository.save(log);
    }

    public List<QueryLog> getAllLogs() {
        return queryLogRepository.findAll();
    }

    // 新增：监听SQL日志事件，异步保存日志
    @Async
    @EventListener
    @Transactional
    public void handleSqlLogEvent(SqlLogEvent event) {
        try {
            saveLog(event.getExecutionTime(), event.getSql());
        } catch (Exception e) {
            // 记录异常但不抛出，防止影响主流程
            e.printStackTrace();
        }
    }
}
