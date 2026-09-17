package com.rs.utils.mysql;

public class DatabaseUtil {

    public static String buildBatch(final String query, final int rows, final int columns) {
        final String column = buildColumnStructure(columns);
        final StringBuilder sb = new StringBuilder((column.length() + 2) * (rows - 1) + query.length());

        sb.append(query);

        for (int index = 1; index < rows; index++)
            sb.append(", " + column);

        return sb.toString();
    }

    public static String buildColumnStructure(final int columns) {
        final StringBuilder sb = new StringBuilder(3 * (columns - 1) + 1);

        for (int index = 1; index <= columns; index++)
            sb.append(index == columns ? "?" : "?, ");

        return "( " + sb + " )";
    }

}
