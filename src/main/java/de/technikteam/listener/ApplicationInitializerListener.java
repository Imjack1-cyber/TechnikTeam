package de.technikteam.listener;

import de.technikteam.service.ConfigurationService;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.output.MigrateResult;

@WebListener
public class ApplicationInitializerListener implements ServletContextListener {

    private static final Logger logger = LogManager.getLogger(ApplicationInitializerListener.class);

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        logger.info("Application Initializer: Context is being initialized...");

        try {
            ConfigurationService configService = new ConfigurationService();

            logger.info("Initializing Flyway for database migration...");
            Flyway flyway = Flyway.configure()
                    .dataSource(
                            configService.getProperty("db.url"),
                            configService.getProperty("db.user"),
                            configService.getProperty("db.password")
                    )
                    .locations("classpath:db/migration")
                    .loggers("slf4j")
                    .baselineOnMigrate(true)
                    .load();

            logger.info("Starting database migration...");
            MigrateResult result = flyway.migrate();
            
            logger.info("Flyway migration finished. {} migrations applied in {} ms.", result.migrationsExecuted, result.getTotalMigrationTime());

            if (!result.success) {
                logger.fatal("Flyway migration failed! Application startup aborted.");
                throw new RuntimeException("Database migration failed. Check logs for details.");
            }
            logger.info("Database schema is up to date (Version: {}).", result.targetSchemaVersion);

        } catch (Exception e) {
            logger.fatal("FATAL: A critical error occurred during application initialization.", e);
            throw new RuntimeException("Failed to initialize application.", e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        logger.info("Application Initializer: Context is being destroyed.");
    }
}