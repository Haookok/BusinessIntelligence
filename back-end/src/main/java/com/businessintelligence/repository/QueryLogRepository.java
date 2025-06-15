package com.businessintelligence.repository;

import com.businessintelligence.entity.QueryLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QueryLogRepository extends JpaRepository<QueryLog, Long> {
}
