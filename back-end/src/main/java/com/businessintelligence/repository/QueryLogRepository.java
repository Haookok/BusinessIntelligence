package com.businessintelligence.repository;

import com.businessintelligence.entity.QueryLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QueryLogRepository extends JpaRepository<QueryLog, Long> {
    // 新增：按执行时间倒序排列的查询方法
    List<QueryLog> findAllByOrderByExecutionTimeDesc();
}
