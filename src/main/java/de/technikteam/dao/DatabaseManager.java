package de.technikteam.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DatabaseManager {
	private static final Logger logger = LogManager.getLogger(DatabaseManager.class);

	private static final String DB_URL = "jdbc:mysql://localhost:3306/technik_team_db?useUnicode=true&characterEncoding=UTF-8&useSSL=false&serverTimezone=UTC";
	private static final String USER = "admin";
	private static final String PASS = "cvoqgcecxa";

	private static Connection connection = null;

	private DatabaseManager() {
	}

	public static Connection getConnection() {
		try {
			if (connection == null || connection.isClosed()) {
				logger.info("Database connection is closed or null. Attempting to create a new one.");
				Class.forName("com.mysql.cj.jdbc.Driver");
				connection = DriverManager.getConnection(DB_URL, USER, PASS);

				// =================================================================
				// DER ENTSCHEIDENDE BEWEIS
				// =================================================================
				// Wir loggen den exakten Katalognamen (Datenbanknamen) der Verbindung.
				String dbName = connection.getCatalog();
				logger.info("================================================================");
				logger.info("SUCCESSFULLY CONNECTED TO DATABASE: '{}'", dbName);
				logger.info("JDBC URL USED: {}", DB_URL);
				logger.info("================================================================");
			}
		} catch (SQLException e) {
			logger.error("DATABASE CONNECTION FAILED!", e);
			throw new RuntimeException("Failed to connect to the database", e);
		} catch (ClassNotFoundException e) {
			logger.error("MYSQL JDBC DRIVER NOT FOUND!", e);
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