package de.technikteam.listener;

import com.google.inject.Injector;
import de.technikteam.config.GuiceConfig;
import de.technikteam.dao.DatabaseManager;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class AppContextListener implements ServletContextListener {

	private static final Logger logger = LogManager.getLogger(AppContextListener.class);

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		logger.info(">>>>>>>>>> TechnikTeam Application Context Initialized <<<<<<<<<<");
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		logger.info(">>>>>>>>>> TechnikTeam Application Context Being Destroyed. Cleaning up resources... <<<<<<<<<<");

		// --- Step 1: Cleanly shut down the Hikari Connection Pool ---
		Injector injector = GuiceConfig.getInjectorInstance();
		if (injector != null) {
			try {
				DatabaseManager dbManager = injector.getInstance(DatabaseManager.class);
				if (dbManager != null) {
					logger.info("Attempting to shut down the database connection pool...");
					dbManager.closeDataSource();
				}
			} catch (Exception e) {
				logger.error("Error while shutting down the connection pool.", e);
			}
		} else {
			logger.warn("Guice Injector was not available. Could not shut down connection pool gracefully.");
		}

		// --- Step 2: Deregister JDBC drivers (legacy cleanup) ---
		Enumeration<java.sql.Driver> drivers = DriverManager.getDrivers();
		while (drivers.hasMoreElements()) {
			java.sql.Driver driver = drivers.nextElement();
			if (driver.getClass().getClassLoader() == getClass().getClassLoader()) {
				try {
					DriverManager.deregisterDriver(driver);
					logger.info("Deregistering JDBC driver: {}", driver);
				} catch (SQLException e) {
					logger.error("Error deregistering JDBC driver: {}", driver, e);
				}
			} else {
				logger.trace("Not deregistering JDBC driver {} as it does not belong to this webapp's classloader.",
						driver);
			}
		}
	}
}