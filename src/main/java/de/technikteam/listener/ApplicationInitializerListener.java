package de.technikteam.listener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class ApplicationInitializerListener implements ServletContextListener {

    private static final Logger logger = LogManager.getLogger(ApplicationInitializerListener.class);

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        logger.info("Application context is being initialized...");
        
        try {
            // Manually load the MySQL JDBC driver.
            // This is the fallback for when automatic service discovery fails.
            logger.info("Attempting to manually load MySQL JDBC driver...");
            Class.forName("com.mysql.cj.jdbc.Driver");
            logger.info("MySQL JDBC driver loaded successfully.");
        } catch (ClassNotFoundException e) {
            logger.fatal("FATAL: MySQL JDBC driver not found in WEB-INF/lib. Application will likely fail.", e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        logger.info("Application context is being destroyed.");
        // You can also add logic here to properly shut down the database pool.
        // de.technikteam.dao.DatabaseManager.closeDataSource();
    }
}