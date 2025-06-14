package com.businessintelligence.service;


import com.businessintelligence.Infrastracture.page.PageResult;
import com.businessintelligence.config.SqlLogEvent;
import com.businessintelligence.entity.QueryLog;
import java.util.List;

public interface QueryLogService {
    // 同步操作方法
    QueryLog saveLog(Long executionTime, String querySql);
    List<QueryLog> getAllLogs();

    PageResult<QueryLog> getLogsByPage(int page, int size);

    PageResult<QueryLog> getLogsByExecutionTimeDescByPage(int page, int size);


    // 异步事件处理方法（仅声明，不实现）
    void handleSqlLogEvent(SqlLogEvent event);
}