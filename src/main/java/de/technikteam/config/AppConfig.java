package de.technikteam.config;

/**
 * A simple configuration class that holds a single, application-wide constant: 
 * the absolute file path for the directory where all user-uploaded files are stored.
 * This centralized approach makes it easy to change the upload location without
 * modifying multiple files.
 */
public class AppConfig {
	public static final String UPLOAD_DIRECTORY = "C:\\dev\\eclipse\\workspace\\TechnikTeam\\resources\\uploads";
}