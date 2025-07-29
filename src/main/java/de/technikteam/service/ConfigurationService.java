package de.technikteam.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ConfigurationService {
	private static final Logger logger = LogManager.getLogger(ConfigurationService.class);

	@Value("${server.servlet.context-path:#{''}}")
	private String contextPath;

	@Value("${spring.datasource.url}")
	private String dbUrl;

	@Value("${spring.datasource.username}")
	private String dbUser;

	@Value("${spring.datasource.password}")
	private String dbPassword;

	@Value("${jwt.secret}")
	private String jwtSecret;

	@Value("${upload.directory}")
	private String uploadDirectory;

	public String getProperty(String key) {
		switch (key) {
		case "context.path":
			return contextPath;
		case "db.url":
			return dbUrl;
		case "db.user":
			return dbUser;
		case "db.password":
			return dbPassword;
		case "jwt.secret":
			return jwtSecret;
		case "upload.directory":
			return uploadDirectory;
		default:
			logger.warn("Attempted to access unknown property key: {}", key);
			return null;
		}
	}
}