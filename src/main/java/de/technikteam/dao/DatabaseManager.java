package de.technikteam.dao;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class manages the application's connection to the MySQL database. It
 * uses the HikariCP connection pool for efficient and high-performance database
 * communication. A single, static `HikariDataSource` is initialized when the
 * class is first loaded. All Data Access Objects (DAOs) use the static
 * `getConnection()` method to borrow a connection from the pool.
 */
public class DatabaseManager {

	private static final Logger logger = LogManager.getLogger(DatabaseManager.class);
	private static HikariDataSource dataSource;

	// This static block runs only ONCE when the class is first loaded.
	static {
		try {
			logger.info("Initializing database connection pool...");

			HikariConfig config = new HikariConfig();
			// Ensure the JDBC URL uses UTF-8 and a modern timezone for compatibility.
			config.setJdbcUrl(
					"jdbc:mysql://localhost:3306/technik_team_db?useUnicode=true&characterEncoding=UTF-8&useSSL=false&serverTimezone=UTC");
			config.setUsername("technik_user"); // <-- SET YOUR DB USERNAME
			config.setPassword("ein_sicheres_passwort"); // <-- SET YOUR DB PASSWORD

			// --- Pool Configuration (Good Defaults) ---
			config.setMaximumPoolSize(10); // Max number of active connections
			config.setMinimumIdle(5); // Min number of idle connections to keep ready
			config.setConnectionTimeout(30000); // 30 seconds to wait for a connection before timing out
			config.setIdleTimeout(600000); // 10 minutes for an idle connection to be retired
			config.setMaxLifetime(1800000); // 30 minutes max lifetime for any single connection

			// Create the datasource pool from the configuration
			dataSource = new HikariDataSource(config);

			logger.info("================================================================");
			logger.info("DATABASE CONNECTION POOL INITIALIZED SUCCESSFULLY.");
			logger.info("================================================================");

		} catch (Exception e) {
			logger.fatal(
					"Failed to initialize database connection pool! The application cannot function without a database.",
					e);
			// This is a fatal error, so we throw an exception to stop the application from
			// starting incorrectly.
			throw new RuntimeException("Could not initialize database pool", e);
		}
	}

	/**
	 * Gets a connection from the connection pool. This method is fast because it
	 * borrows an existing, ready-to-use connection; it does not create a new one.
	 * 
	 * @return A database connection.
	 * @throws SQLException if a connection cannot be obtained from the pool.
	 */
	public static Connection getConnection() throws SQLException {
		if (dataSource == null) {
			logger.error("Datasource is null. The database manager was not initialized correctly.");
			throw new SQLException("Database connection pool is not available.");
		}
		return dataSource.getConnection();
	}

	/**
	 * Closes the entire connection pool. This should be called only when the
	 * application is shutting down to release all database resources.
	 */
	public static void closeDataSource() {
		if (dataSource != null && !dataSource.isClosed()) {
			logger.info("Closing database connection pool...");
			dataSource.close();
			logger.info("Database connection pool closed successfully.");
		}
	}
}