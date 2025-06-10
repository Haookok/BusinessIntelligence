package com.businessintelligence.controller;

import com.businessintelligence.entity.QueryLog;
import com.businessintelligence.service.QueryLogService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
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
}
