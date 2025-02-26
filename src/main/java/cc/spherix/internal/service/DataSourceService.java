package cc.spherix.internal.service;

import cc.spherix.internal.config.HikariCPConfiguration;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.jspecify.annotations.NonNull;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class DataSourceService {

    private static final Map<HikariCPConfiguration, DataSourceService> HIKARI_SOURCES = new HashMap<>();

    protected HikariCPConfiguration configuration;
    protected final HikariDataSource dataSource;

    private DataSourceService(@NonNull HikariCPConfiguration configuration) {
        this.configuration = configuration;

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(configuration.getJdbc());
        config.setUsername(configuration.getUsername());
        config.setPassword(configuration.getPassword());
        config.setMaximumPoolSize(configuration.getMaxPoolSize());
        config.setConnectionTimeout(configuration.getConnectionTimeout());
        config.setIdleTimeout(configuration.getIdleTimeout());
        config.setMaxLifetime(configuration.getMaxLifetime());
        config.setKeepaliveTime(configuration.getKeepAliveTime());

        dataSource = new HikariDataSource(config);
    }

    /**
     * Получаем коннект из пула
     */
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    /**
     * Закрываем пул
     */
    public void stop() {
        dataSource.close();
    }

    public static void stopAll() {
        HIKARI_SOURCES.forEach((config, service) -> service.stop());
    }

    public static DataSourceService of(HikariCPConfiguration configuration) {
        return HIKARI_SOURCES.computeIfAbsent(configuration, DataSourceService::new);
    }
}
