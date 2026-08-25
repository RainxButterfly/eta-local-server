// SPDX-FileCopyrightText: 2026 RainxButterfly
// SPDX-License-Identifier: AGPL-3.0-or-later
package cn.eta.team.eta.tenant;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import javax.sql.DataSource;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TenantRoutingDataSource extends AbstractRoutingDataSource {

    private final Map<Object, Object> targetDataSources = new ConcurrentHashMap<>();

    public TenantRoutingDataSource() {
        setTargetDataSources(targetDataSources);
    }

    @Override
    protected Object determineCurrentLookupKey() {
        return TenantContext.get();
    }

    public void addTenantDataSource(String userId, DataSource dataSource) {
        targetDataSources.put(userId, dataSource);
        setTargetDataSources(targetDataSources);
        afterPropertiesSet();
    }

    public void removeTenantDataSource(String userId) {
        targetDataSources.remove(userId);
        setTargetDataSources(targetDataSources);
        afterPropertiesSet();
    }
}
