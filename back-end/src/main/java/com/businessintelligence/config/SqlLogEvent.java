package com.businessintelligence.config;

import org.springframework.context.ApplicationEvent;

public class SqlLogEvent extends ApplicationEvent {

    private final long executionTime;
    private final String sql;

    public SqlLogEvent(Object source, long executionTime, String sql) {
        super(source);
        this.executionTime = executionTime;
        this.sql = sql;
    }

    public long getExecutionTime() {
        return executionTime;
    }

    public String getSql() {
        return sql;
    }
}
