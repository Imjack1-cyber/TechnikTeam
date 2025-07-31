package de.technikteam.util;

import java.util.regex.Pattern;

/**
 * A utility class for sanitizing user-provided Markdown content to prevent
 * Cross-Site Scripting (XSS) attacks. It strips dangerous HTML tags and
 * attributes before the content is stored or rendered.
 */
public final class MarkdownUtil {

	// Pattern to find and remove <script>...</script> blocks, case-insensitive and
	// multiline.
	private static final Pattern SCRIPT_PATTERN = Pattern.compile("<script.*?>.*?</script>",
			Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);

	// Pattern to find and remove attributes like onclick, onmouseover, etc.
	private static final Pattern ON_ATTRIBUTE_PATTERN = Pattern
			.compile("\\s(on\\w+)\\s*=\\s*(\"[^\"]*\"|'[^']*'|[^\\s>]+)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);

	// Pattern to find and remove href/src attributes with "javascript:..." URIs.
	private static final Pattern JAVASCRIPT_URI_PATTERN = Pattern.compile("(href|src)\\s*=\\s*(\"|')\\s*javascript:",
			Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);

	/**
	 * Private constructor to prevent instantiation of this utility class.
	 */
	private MarkdownUtil() {
	}

	/**
	 * Sanitizes a string containing Markdown and potentially malicious HTML. This
	 * method should be called on any user-supplied content before it is stored in
	 * the database or broadcast to other clients.
	 *
	 * @param markdown The raw Markdown string from the user.
	 * @return A sanitized string with dangerous HTML elements and attributes
	 *         removed.
	 */
	public static String sanitize(String markdown) {
		if (markdown == null || markdown.isEmpty()) {
			return markdown;
		}

		String sanitized = markdown;

		// Remove <script> tags completely
		sanitized = SCRIPT_PATTERN.matcher(sanitized).replaceAll("");

		// Remove on... attributes (onclick, onmouseover, etc.)
		sanitized = ON_ATTRIBUTE_PATTERN.matcher(sanitized).replaceAll("");

		// Remove javascript:... in href/src attributes
		sanitized = JAVASCRIPT_URI_PATTERN.matcher(sanitized).replaceAll("$1='#'");

		return sanitized;
	}
}