package com.businessintelligence.Infrastracture.util;

import com.businessintelligence.service.impl.NewsRecommendationServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class DatabaseQueryExecutor {

    private final JdbcTemplate jdbcTemplate;
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(NewsRecommendationServiceImpl.class);

    @Autowired
    public DatabaseQueryExecutor(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Map<String, Object>> executeQuery(String sql) {
        // 安全验证：防止SQL注入
        if (!isSafeQuery(sql)) {
            throw new RuntimeException("不安全的SQL查询");
        }

        // 执行查询
        return jdbcTemplate.queryForList(sql);
    }

    private boolean isSafeQuery(String sql) {
        String lowerCaseSql = sql.toLowerCase();

        // 检查是否为SELECT查询（支持CTE语法）
        if (!lowerCaseSql.contains("select") || lowerCaseSql.startsWith("with")) {
            // 特殊处理CTE语法：允许WITH开头但必须包含SELECT
            if (lowerCaseSql.startsWith("with") && lowerCaseSql.contains("select")) {
                log.info("检测到CTE语法的SELECT查询，允许执行");
                return true;
            }
            log.info("不是有效的SELECT查询: {}", sql);
            return false;
        }

        // 防止危险操作
        if (lowerCaseSql.contains("delete") || lowerCaseSql.contains("update") ||
                lowerCaseSql.contains("drop") || lowerCaseSql.contains("truncate") ||
                lowerCaseSql.contains("alter")) {
            return false;
        }

        // 仅允许查询t_news表
        if (!lowerCaseSql.contains("from t_news")) {
            return false;
        }

        return true;
    }
}