package com.businessintelligence.service.impl;

import com.businessintelligence.config.SqlLogEvent;
import com.businessintelligence.entity.QueryLog;
import com.businessintelligence.repository.QueryLogRepository;
import com.businessintelligence.service.QueryLogService;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class QueryLogServiceImpl implements QueryLogService {

    private final QueryLogRepository queryLogRepository;

    public QueryLogServiceImpl(QueryLogRepository queryLogRepository) {
        this.queryLogRepository = queryLogRepository;
    }

    // 同步操作实现
    @Override
    @Transactional
    public QueryLog saveLog(Long executionTime, String querySql) {
        QueryLog log = new QueryLog();
        log.setCreatedAt(LocalDateTime.now());
        log.setExecutionTime(executionTime);
        log.setQuerySql(querySql);
        return queryLogRepository.save(log);
    }

    @Override
    public List<QueryLog> getAllLogs() {
        return queryLogRepository.findAll();
    }

    @Override
    public List<QueryLog> getAllLogsByExecutionTimeDesc() {
        return queryLogRepository.findAllByOrderByExecutionTimeDesc();
    }

    // 异步事件处理实现（带事件监听注解）
    @Override
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