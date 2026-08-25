// SPDX-FileCopyrightText: 2026 RainxButterfly
// SPDX-License-Identifier: AGPL-3.0-or-later
package cn.eta.team.eta.tenant;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

/**
 * 数据源配置：
 * 默认数据源存放用户表（eta_user），用于注册/登录认证；
 * 每个登录用户有独立的 H2 文件数据库，通过 {@link TenantRoutingDataSource} 动态路由。
 */
@Configuration
public class DataSourceConfig {

    @Value("${spring.datasource.url}")
    private String defaultUrl;

    @Value("${spring.datasource.username:sa}")
    private String username;

    @Value("${spring.datasource.password:}")
    private String password;

    @Value("${spring.datasource.driver-class-name:org.h2.Driver}")
    private String driverClassName;

    @Bean
    public DataSource defaultDataSource() {
        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl(defaultUrl);
        ds.setUsername(username);
        ds.setPassword(password);
        ds.setDriverClassName(driverClassName);
        return ds;
    }

    @Bean
    @Primary
    public TenantRoutingDataSource routingDataSource(DataSource defaultDataSource) {
        TenantRoutingDataSource routing = new TenantRoutingDataSource();
        routing.setDefaultTargetDataSource(defaultDataSource);
        return routing;
    }
}
