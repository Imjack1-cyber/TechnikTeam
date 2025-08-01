package de.technikteam.util;

/**
 * DEPRECATED: This class is no longer used for security-sensitive sanitization.
 * The OWASP Java HTML Sanitizer is now used instead to provide robust
 * protection against Cross-Site Scripting (XSS) attacks. This class is retained
 * for historical purposes or non-security-related transformations if needed.
 */
public final class MarkdownUtil {

	/**
	 * Private constructor to prevent instantiation of this utility class.
	 */
	private MarkdownUtil() {
	}

	/**
	 * A simple, non-security-focused method to perform basic transformations.
	 *
	 * @param markdown The raw string.
	 * @return The transformed string.
	 */
	public static String transform(String markdown) {
		if (markdown == null || markdown.isEmpty()) {
			return markdown;
		}
		// Example transformation: none, as this is deprecated for security.
		return markdown;
	}
}