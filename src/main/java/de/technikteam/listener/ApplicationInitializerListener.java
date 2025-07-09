package de.technikteam.listener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import de.technikteam.dao.DatabaseManager;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

/**
 * An application lifecycle listener that runs when the application starts. It
 * performs two critical initialization tasks: 1. Manually loads the MySQL JDBC
 * driver to ensure it's available for the application. This is a robust
 * practice that prevents connectivity issues if the server's automatic service
 * discovery fails. 2. Explicitly triggers the initialization of the
 * `DatabaseManager` and its connection pool, and ensures the pool is closed on
 * application shutdown.
 */
@WebListener
public class ApplicationInitializerListener implements ServletContextListener {

	private static final Logger logger = LogManager.getLogger(ApplicationInitializerListener.class);

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		logger.info("Application Initializer: Context is being initialized...");

		try {
			logger.info("Attempting to manually load MySQL JDBC driver...");
			Class.forName("com.mysql.cj.jdbc.Driver");
			logger.info("MySQL JDBC driver loaded successfully.");

			logger.info("Triggering database connection pool initialization...");
			DatabaseManager.initDataSource();

		} catch (ClassNotFoundException e) {
			logger.fatal("FATAL: MySQL JDBC driver not found in classpath. Application will fail.", e);
			throw new RuntimeException("Failed to load JDBC driver", e);
		} catch (Exception e) {
			logger.fatal("FATAL: Database pool could not be initialized. Application startup aborted.", e);
			throw new RuntimeException("Failed to initialize database pool", e);
		}
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		logger.info("Application Initializer: Context is being destroyed.");
		DatabaseManager.closeDataSource();
	}
}