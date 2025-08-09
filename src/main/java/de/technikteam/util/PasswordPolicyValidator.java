package de.technikteam.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * A utility class to enforce a consistent, server-side password policy.
 */
public final class PasswordPolicyValidator {

	private static final int MIN_LENGTH = 10;
	private static final Pattern HAS_UPPERCASE = Pattern.compile("[A-Z]");
	private static final Pattern HAS_LOWERCASE = Pattern.compile("[a-z]");
	private static final Pattern HAS_DIGIT = Pattern.compile("[0-9]");
	private static final Pattern HAS_SPECIAL_CHAR = Pattern.compile("[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]");

	/**
	 * Private constructor to prevent instantiation of this utility class.
	 */
	private PasswordPolicyValidator() {
	}

	/**
	 * A simple record to hold the result of a password validation check.
	 */
	public static class ValidationResult {
		private final boolean isValid;
		private final String message;

		public ValidationResult(boolean isValid, String message) {
			this.isValid = isValid;
			this.message = message;
		}

		public boolean isValid() {
			return isValid;
		}

		public String getMessage() {
			return message;
		}
	}

	/**
	 * Validates a password against the application's defined security policy.
	 *
	 * @param password The password to validate.
	 * @return A {@link ValidationResult} object containing the result and a
	 *         descriptive message.
	 */
	public static ValidationResult validate(String password) {
		if (password == null || password.trim().isEmpty()) {
			return new ValidationResult(false, "Das Passwort darf nicht leer sein.");
		}

		List<String> errors = new ArrayList<>();

		if (password.length() < MIN_LENGTH) {
			errors.add("mindestens " + MIN_LENGTH + " Zeichen lang sein");
		}
		if (!HAS_UPPERCASE.matcher(password).find()) {
			errors.add("mindestens einen Großbuchstaben enthalten");
		}
		if (!HAS_LOWERCASE.matcher(password).find()) {
			errors.add("mindestens einen Kleinbuchstaben enthalten");
		}
		if (!HAS_DIGIT.matcher(password).find()) {
			errors.add("mindestens eine Ziffer enthalten");
		}
		if (!HAS_SPECIAL_CHAR.matcher(password).find()) {
			errors.add("mindestens ein Sonderzeichen enthalten");
		}

		if (errors.isEmpty()) {
			return new ValidationResult(true, "Passwort ist gültig.");
		} else {
			return new ValidationResult(false, "Das Passwort muss " + String.join(", ", errors) + ".");
		}
	}
}