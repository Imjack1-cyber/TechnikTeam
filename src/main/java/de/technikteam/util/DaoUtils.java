package de.technikteam.util;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

/**
 * A utility class for common Data Access Object (DAO) helper methods.
 */
public class DaoUtils {

    /**
     * Private constructor to prevent instantiation.
     */
    private DaoUtils() {
    }

    /**
     * Checks if a ResultSet contains a column with the given name
     * (case-insensitive).
     *
     * @param rs         The ResultSet to check.
     * @param columnName The name of the column.
     * @return true if the column exists, false otherwise.
     * @throws SQLException If a database error occurs.
     */
    public static boolean hasColumn(ResultSet rs, String columnName) throws SQLException {
        ResultSetMetaData rsmd = rs.getMetaData();
        int columns = rsmd.getColumnCount();
        for (int x = 1; x <= columns; x++) {
            if (columnName.equalsIgnoreCase(rsmd.getColumnName(x))) {
                return true;
            }
        }
        return false;
    }
}