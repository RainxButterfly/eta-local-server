// SPDX-FileCopyrightText: 2026 RainxButterfly
// SPDX-License-Identifier: AGPL-3.0-or-later
package cn.eta.team.eta.tenant;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;

/**
 * 用户数据库初始化器：用户注册时为其创建独立的 H2 文件数据库并建表。
 */
@Component
public class UserDatabaseInitializer {

    private final TenantRoutingDataSource routingDataSource;

    @Value("${eta.user-db.path:./data/users}")
    private String userDbPath;

    public UserDatabaseInitializer(TenantRoutingDataSource routingDataSource) {
        this.routingDataSource = routingDataSource;
    }

    /**
     * 为指定用户创建独立 H2 文件数据库，执行建表脚本，并注册到动态数据源路由。
     * 幂等：已存在的数据库不会重复创建。
     */
    public void createDatabase(String userId) {
        Path dir = Paths.get(userDbPath);
        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            throw new RuntimeException("创建用户数据库目录失败: " + dir, e);
        }

        String url = "jdbc:h2:file:" + dir.toAbsolutePath() + "/" + userId
                + ";MODE=MySQL;AUTO_SERVER=TRUE;DB_CLOSE_DELAY=-1";

        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl(url);
        ds.setUsername("sa");
        ds.setPassword("");
        ds.setDriverClassName("org.h2.Driver");

        runSchemaScript(ds);
        routingDataSource.addTenantDataSource(userId, ds);
    }

    /**
     * 确保用户数据库已加载到路由表中（登录时调用，幂等）。
     * 已存在则重新执行建表脚本以补齐新增表，不存在则创建。
     */
    public void ensureDatabase(String userId) {
        if (routingDataSource.getResolvedDataSources().containsKey(userId)) {
            DataSource ds = (DataSource) routingDataSource.getResolvedDataSources().get(userId);
            runSchemaScript(ds);
            return;
        }
        createDatabase(userId);
    }

    /**
     * 轻量版：仅确保用户库已注册到路由表，不重复执行建表脚本。
     * 适合在每个认证请求的 Filter 中调用，处理 JWT 跨应用重启的场景。
     */
    public void ensureRegistered(String userId) {
        if (!routingDataSource.getResolvedDataSources().containsKey(userId)) {
            createDatabase(userId);
        }
    }

    public DataSource getDataSource(String userId) {
        return (DataSource) routingDataSource.getResolvedDataSources().get(userId);
    }

    public void closeAndRemoveDataSource(String userId) {
        DataSource ds = (DataSource) routingDataSource.getResolvedDataSources().get(userId);
        if (ds instanceof HikariDataSource hds) {
            hds.close();
        }
        routingDataSource.removeTenantDataSource(userId);
    }

    private void runSchemaScript(DataSource ds) {
        try (Connection conn = ds.getConnection()) {
            ScriptUtils.executeSqlScript(conn, new ClassPathResource("schema-user.sql"));
        } catch (Exception e) {
            throw new RuntimeException("执行用户数据库建表脚本失败", e);
        }
    }
}
