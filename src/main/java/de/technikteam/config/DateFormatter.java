package de.technikteam.config;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * A utility class with static methods to format java.time.LocalDateTime objects
 * into German-style date (dd.MM.yyyy) and date-time (dd.MM.yyyy, HH:mm) strings
 * for consistent display in the user interface. It also provides a method to
 * format a date range intelligently.
 */
public class DateFormatter {

	private static final DateTimeFormatter GERMAN_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy, HH:mm",
			Locale.GERMANY);

	private static final DateTimeFormatter GERMAN_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy",
			Locale.GERMANY);

	private static final DateTimeFormatter GERMAN_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm", Locale.GERMANY);

	/**
	 * Formats a LocalDateTime object into a German date and time string.
	 * 
	 * @param ldt The LocalDateTime to format.
	 * @return The formatted string (e.g., "10.06.2025, 17:45"), or an empty string
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

	/**
	 * Formats a start and end LocalDateTime into a human-readable German range.
	 * Examples: - "10.06.2025, 17:45 Uhr" (if end is null) - "10.06.2025, 17:45 -
	 * 19:00 Uhr" (if on the same day) - "10.06.2025, 17:45 Uhr - 11.06.2025, 18:00
	 * Uhr" (if on different days)
	 * 
	 * @param start The start time.
	 * @param end   The end time.
	 * @return The formatted string.
	 */
	public static String formatDateTimeRange(LocalDateTime start, LocalDateTime end) {
		if (start == null) {
			return "";
		}

		if (end == null) {
			return formatDateTime(start) + " Uhr";
		}

		if (start.toLocalDate().equals(end.toLocalDate())) {
			return formatDate(start) + ", " + start.format(GERMAN_TIME_FORMATTER) + " - "
					+ end.format(GERMAN_TIME_FORMATTER) + " Uhr";
		}

		return formatDateTime(start) + " Uhr - " + formatDateTime(end) + " Uhr";
	}
}