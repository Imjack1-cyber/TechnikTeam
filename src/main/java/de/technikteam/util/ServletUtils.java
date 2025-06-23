package de.technikteam.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import jakarta.servlet.http.Part;

/**
 * A utility class for common Servlet helper methods.
 */
public class ServletUtils {

	/**
	 * Private constructor to prevent instantiation.
	 */
	private ServletUtils() {
	}

	/**
	 * Extracts the string value from a `multipart/form-data` part.
	 *
	 * @param part The Part object to extract the value from.
	 * @return The string value of the part, or null if the part is null.
	 * @throws IOException if an I/O error occurs.
	 */
	public static String getPartValue(Part part) throws IOException {
		if (part == null) {
			return null;
		}
		try (InputStream inputStream = part.getInputStream();
				Scanner scanner = new Scanner(inputStream, StandardCharsets.UTF_8.name())) {
			return scanner.useDelimiter("\\A").hasNext() ? scanner.next() : "";
		}
	}
}