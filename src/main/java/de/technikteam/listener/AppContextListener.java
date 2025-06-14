package de.technikteam.listener;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;

// Wir benötigen den Import für den AbandonedConnectionCleanupThread nicht mehr.
// import com.mysql.cj.jdbc.AbandonedConnectionCleanupThread; 

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
		logger.info("TechnikTeam Application Context Initialized.");
		// Hier können Aktionen beim Start der Anwendung ausgeführt werden.
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		logger.info("TechnikTeam Application Context Destroyed. Cleaning up resources...");

		// Dies ist der wichtigste und offiziell unterstützte Schritt, um Speicherlecks
		// durch JDBC-Treiber beim Herunterfahren der Anwendung zu verhindern.
		// Wir iterieren durch alle geladenen Treiber und deregistrieren denjenigen,
		// der vom ClassLoader unserer Anwendung geladen wurde.
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