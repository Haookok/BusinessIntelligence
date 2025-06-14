package com.businessintelligence.controller;

import com.businessintelligence.Infrastracture.page.PageResult;
import com.businessintelligence.entity.QueryLog;
import com.businessintelligence.service.QueryLogService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
@RequestMapping("/api/querylogs")
public class QueryLogController {

    private final QueryLogService queryLogService;

    public QueryLogController(QueryLogService queryLogService) {
        this.queryLogService = queryLogService;
    }

    // 测试写入日志
    @PostMapping("/test")
    public ResponseEntity<QueryLog> testInsertLog(@RequestParam Long execTime, @RequestParam String sql) {
        QueryLog savedLog = queryLogService.saveLog(execTime, sql);
        return ResponseEntity.ok(savedLog);
    }

    // 查询所有日志
    @GetMapping
    public ResponseEntity<List<QueryLog>> getAllLogs() {
        List<QueryLog> logs = queryLogService.getAllLogs();
        return ResponseEntity.ok(logs);
    }
    @GetMapping("/logs")
    public PageResult<QueryLog> getLogs(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return queryLogService.getLogsByPage(page, size);
    }



    @GetMapping("/sorted-by-execution-time")
    public PageResult<QueryLog> getLogsByExecutionTimeDesc(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return queryLogService.getLogsByExecutionTimeDescByPage(page, size);
    }

}
