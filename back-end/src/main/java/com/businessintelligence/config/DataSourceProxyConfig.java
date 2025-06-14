package com.businessintelligence.config;

import net.ttddyy.dsproxy.ExecutionInfo;
import net.ttddyy.dsproxy.QueryInfo;
import net.ttddyy.dsproxy.listener.QueryExecutionListener;
import net.ttddyy.dsproxy.support.ProxyDataSourceBuilder;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import com.zaxxer.hikari.HikariDataSource;

import java.util.List;

@Configuration
public class DataSourceProxyConfig {

    private final ApplicationEventPublisher eventPublisher;

    public DataSourceProxyConfig(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }


    @Bean
    public DataSource originalDataSource() {
        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl("jdbc:mysql://81.68.224.23:3306/BIDB?useSSL=false&serverTimezone=UTC");
        ds.setUsername("remote_user");
        ds.setPassword("remote~Ruanshu1");
        ds.setDriverClassName("com.mysql.cj.jdbc.Driver");
        return ds;
    }

    // 代理数据源，声明为Primary，替代默认数据源
    @Bean
    @Primary
    public DataSource dataSource(DataSource originalDataSource) {
        return ProxyDataSourceBuilder
                .create(originalDataSource)
                .name("SQL-LOGGER")
                .listener(new QueryExecutionListener() {
                    @Override
                    public void beforeQuery(ExecutionInfo execInfo, java.util.List<QueryInfo> queryInfoList) {
                    }
                    @Override
                    public void afterQuery(ExecutionInfo execInfo, List<QueryInfo> queryInfoList) {
                        long elapsed = execInfo.getElapsedTime();
                        for (QueryInfo queryInfo : queryInfoList) {
                            String sql = queryInfo.getQuery().toLowerCase();

                            // 过滤掉针对日志表的SQL，避免递归
                            if (sql.contains("t_query_log")) {
                                continue;
                            }

                            eventPublisher.publishEvent(new SqlLogEvent(this, elapsed, sql));
                        }
                    }

                })
                .build();
    }
}
