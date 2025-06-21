package de.technikteam.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * A custom serializer for the Gson library. It converts java.time.LocalDate
 * objects into the standard YYYY-MM-DD string format, which is ideal for JSON
 * data and HTML date input fields.
 */
public class LocalDateAdapter implements JsonSerializer<LocalDate> {

	@Override
	public JsonElement serialize(LocalDate date, Type typeOfSrc, JsonSerializationContext context) {
		// We simply convert the LocalDate to its standard ISO_LOCAL_DATE string format
		// ("YYYY-MM-DD").
		// We also handle the case where the date might be null.
		return date == null ? null : new JsonPrimitive(date.format(DateTimeFormatter.ISO_LOCAL_DATE));
	}
}