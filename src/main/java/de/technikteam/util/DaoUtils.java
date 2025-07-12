package de.technikteam.util;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

/**
 * A utility class providing common helper methods for Data Access Objects
 * (DAOs).
 */
public final class DaoUtils {

	/**
	 * Private constructor to prevent instantiation of this utility class.
	 */
	private DaoUtils() {
	}

	/**
	 * Checks if a ResultSet contains a column with the given name. This check is
	 * case-insensitive. It is useful for handling optional columns from complex
	 * JOINs without causing a SQLException.
	 *
	 * @param rs         The ResultSet to check.
	 * @param columnName The name of the column to look for.
	 * @return true if the column exists in the ResultSet metadata, false otherwise.
	 * @throws SQLException If a database access error occurs while retrieving
	 *                      metadata.
	 */
	public static boolean hasColumn(ResultSet rs, String columnName) throws SQLException {
		ResultSetMetaData rsmd = rs.getMetaData();
		int columnCount = rsmd.getColumnCount();
		for (int i = 1; i <= columnCount; i++) {
			if (columnName.equalsIgnoreCase(rsmd.getColumnName(i))) {
				return true;
			}
		}
		return false;
	}
}