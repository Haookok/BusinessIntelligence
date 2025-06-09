package com.businessintelligence.repository;

import com.businessintelligence.entity.QueryLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Repository
public interface QueryLogRepository extends JpaRepository<QueryLog, Long> {
    
    @Query("SELECT AVG(q.executionTime) as avgTime, " +
           "COUNT(q) as queryCount " +
           "FROM QueryLog q " +
           "WHERE q.createdAt >= :startTime " +
           "AND q.createdAt <= :endTime")
    Map<String, Object> findQueryPerformanceStats(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    List<QueryLog> findByUserIdOrderByCreatedAtDesc(Integer userId);
} 