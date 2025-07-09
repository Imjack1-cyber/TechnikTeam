package de.technikteam.listener;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mysql.cj.jdbc.AbandonedConnectionCleanupThread;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

/**
 * This is an application lifecycle listener that performs crucial cleanup tasks
 * when the web application is shut down or undeployed from the server. Its
 * primary purpose is to manually deregister the JDBC driver that was loaded by
 * this application's classloader. This prevents potential memory leaks in
 * application servers like Tomcat. It also explicitly shuts down the MySQL
 * cleanup thread.
 */
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

		try {
			logger.info("Attempting to shut down MySQL abandoned connection cleanup thread...");
			AbandonedConnectionCleanupThread.checkedShutdown();
			logger.info("MySQL cleanup thread shutdown signal sent.");
		} catch (Exception e) {
			logger.error("Error shutting down MySQL cleanup thread.", e);
		}

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