package de.technikteam.config;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * A utility class with static methods to format java.time.LocalDateTime objects
 * into German-style date (dd.MM.yyyy) and date-time (dd.MM.yyyy HH:mm) strings
 * for consistent display in the user interface.
 */
public class DateFormatter {

	// A formatter for a full date and time, e.g., "10.06.2025, 17:45"
	private static final DateTimeFormatter GERMAN_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm",
			Locale.GERMANY);

	// A formatter for just the date, e.g., "10.06.2025"
	private static final DateTimeFormatter GERMAN_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy",
			Locale.GERMANY);

	/**
	 * Formats a LocalDateTime object into a German date and time string.
	 * 
	 * @param ldt The LocalDateTime to format.
	 * @return The formatted string (e.g., "10.06.2025 17:45"), or an empty string
	 *         if input is null.
	 */
	public static String formatDateTime(LocalDateTime ldt) {
		if (ldt == null) {
			return "";
		}
		return ldt.format(GERMAN_DATE_TIME_FORMATTER);
	}

	/**
	 * Formats a LocalDateTime object into a German date string.
	 * 
	 * @param ldt The LocalDateTime to format.
	 * @return The formatted string (e.g., "10.06.2025"), or an empty string if
	 *         input is null.
	 */
	public static String formatDate(LocalDateTime ldt) {
		if (ldt == null) {
			return "";
		}
		return ldt.format(GERMAN_DATE_FORMATTER);
	}
}