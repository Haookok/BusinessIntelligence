package com.businessintelligence.entity;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "t_query_log")
public class QueryLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "query_sql", columnDefinition = "TEXT")
    private String querySql;

    @Column(name = "execution_time")
    private Long executionTime;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "user_id")
    private Integer userId;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
} 