package de.technikteam.service;

import com.google.inject.Singleton;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@Singleton
public class ConfigurationService {
	private static final Logger logger = LogManager.getLogger(ConfigurationService.class);
	private final Properties properties = new Properties();

	public ConfigurationService() {
		try (InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties")) {
			if (input == null) {
				logger.error("Unable to find config.properties on the classpath.");
				throw new IllegalStateException("config.properties not found.");
			}
			properties.load(input);
			logger.info("ConfigurationService initialized and loaded properties successfully.");
		} catch (IOException ex) {
			logger.fatal("Failed to load configuration properties.", ex);
			throw new RuntimeException("Failed to load configuration properties.", ex);
		}
	}

	public String getProperty(String key) {
		return properties.getProperty(key);
	}

	public String getProperty(String key, String defaultValue) {
		return properties.getProperty(key, defaultValue);
	}
}