package de.technikteam.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.time.LocalDateTime;

/**
 * A custom serializer for the Gson library. It converts java.time.LocalDateTime
 * objects into the standard ISO string format (e.g., "2025-06-20T19:00:00"),
 * which is ideal for JSON data exchange and easily parsed by JavaScript.
 */
public class LocalDateTimeAdapter implements JsonSerializer<LocalDateTime> {

	@Override
	public JsonElement serialize(LocalDateTime src, Type typeOfSrc, JsonSerializationContext context) {
		// We convert the LocalDateTime to its standard ISO string format.
		// This also gracefully handles the case where the date object might be null.
		return src == null ? null : new JsonPrimitive(src.toString());
	}
}