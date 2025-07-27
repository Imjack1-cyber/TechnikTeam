// src/main/java/de/technikteam/service/ConfigurationService.java
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
		loadProperties("config.properties");
		loadProperties("db.properties");
	}

	private void loadProperties(String fileName) {
		try (InputStream input = getClass().getClassLoader().getResourceAsStream(fileName)) {
			if (input == null) {
				logger.error("Unable to find {} on the classpath.", fileName);
				throw new IllegalStateException(fileName + " not found.");
			}
			properties.load(input);
			logger.info("{} loaded successfully.", fileName);
		} catch (IOException ex) {
			logger.fatal("Failed to load configuration properties from {}.", fileName, ex);
			throw new RuntimeException("Failed to load configuration properties from " + fileName, ex);
		}
	}

	public String getProperty(String key) {
		return properties.getProperty(key);
	}

	public String getProperty(String key, String defaultValue) {
		return properties.getProperty(key, defaultValue);
	}
}