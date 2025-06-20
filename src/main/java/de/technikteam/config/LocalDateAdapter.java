package de.technikteam.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * A Gson adapter to correctly serialize java.time.LocalDate objects.
 * This converts a LocalDate into a simple "YYYY-MM-DD" string, which is
 * ideal for JSON and for use with HTML <input type="date"> elements.
 */
public class LocalDateAdapter implements JsonSerializer<LocalDate> {

    @Override
    public JsonElement serialize(LocalDate date, Type typeOfSrc, JsonSerializationContext context) {
        // We simply convert the LocalDate to its standard ISO_LOCAL_DATE string format ("YYYY-MM-DD").
        // We also handle the case where the date might be null.
        return date == null ? null : new JsonPrimitive(date.format(DateTimeFormatter.ISO_LOCAL_DATE));
    }
}