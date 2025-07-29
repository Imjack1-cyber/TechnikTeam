package de.technikteam.dao;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import de.technikteam.service.ConfigurationService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.SQLException;

@Component
public class DatabaseManager {

	private static final Logger logger = LogManager.getLogger(DatabaseManager.class);
	private final HikariDataSource dataSource;

	@Autowired
	public DatabaseManager(ConfigurationService configService) {
		logger.info("Initializing Spring-managed Database Connection Pool...");

		try {
			HikariConfig hikariConfig = new HikariConfig();
			hikariConfig.setJdbcUrl(configService.getProperty("db.url"));
			hikariConfig.setUsername(configService.getProperty("db.user"));
			hikariConfig.setPassword(configService.getProperty("db.password"));

			hikariConfig.setMaximumPoolSize(15);
			hikariConfig.setMinimumIdle(5);
			hikariConfig.setConnectionTimeout(30000);
			hikariConfig.setIdleTimeout(600000);
			hikariConfig.setMaxLifetime(1800000);

			this.dataSource = new HikariDataSource(hikariConfig);

			logger.info("================================================================");
			logger.info("SPRING DATABASE CONNECTION POOL INITIALIZED SUCCESSFULLY.");
			logger.info("================================================================");
		} catch (Exception e) {
			logger.fatal("Failed to initialize Spring-managed database pool!", e);
			throw new RuntimeException("Could not initialize database pool", e);
		}
	}

	public Connection getConnection() throws SQLException {
		if (dataSource == null) {
			logger.error("DataSource is null. The DatabaseManager was not initialized correctly.");
			throw new SQLException("Database connection pool is not available.");
		}
		return dataSource.getConnection();
	}

	public void closeDataSource() {
		if (dataSource != null && !dataSource.isClosed()) {
			logger.info("Closing database connection pool...");
			dataSource.close();
			logger.info("Database connection pool closed successfully.");
		}
	}
}