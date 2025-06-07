package de.technikteam.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DatabaseManager {
	// Logger instance for this class
	private static final Logger logger = LogManager.getLogger(DatabaseManager.class);

	private static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
	private static final String DB_URL = "jdbc:mysql://localhost:3306/technik_team_db?useSSL=false&serverTimezone=UTC";
	private static final String USER = "technik_user";
	private static final String PASS = "ein_sicheres_passwort";

	private static Connection connection = null;

	private DatabaseManager() {
	}

	public static Connection getConnection() {
		try {
			if (connection == null || connection.isClosed()) {
				logger.info("Database connection is closed or null. Creating a new one.");
				Class.forName(JDBC_DRIVER);
				connection = DriverManager.getConnection(DB_URL, USER, PASS);
				logger.info("Successfully established a new database connection.");
			}
		} catch (SQLException e) {
			logger.error("Database connection failed!", e);
			throw new RuntimeException("Failed to connect to the database", e);
		} catch (ClassNotFoundException e) {
			logger.error("MySQL JDBC Driver not found!", e);
			throw new RuntimeException("JDBC Driver not found", e);
		}
		return connection;
	}

	public static void closeConnection() {
		if (connection != null) {
			try {
				connection.close();
				logger.info("Database connection closed successfully.");
			} catch (SQLException e) {
				logger.error("Failed to close database connection.", e);
			}
		}
	}
}