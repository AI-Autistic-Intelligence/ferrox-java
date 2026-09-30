package dev.ferrox.utils.export;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Field;
import java.util.List;

public class CsvExportUtils {

    private static final String DELIMITER = ",";
    private static final String NEW_LINE = "\n";
    private static final String QUOTE = "\"";

    /**
     * Serializes a list of objects into CSV format and writes it to the provided Writer.
     *
     * @param data   The list of objects to serialize.
     * @param type   The class type of the objects.
     * @param writer The writer to output the CSV to.
     * @param <T>    The generic type.
     */
    public static <T> void writeCsv(List<T> data, Class<T> type, Writer writer) {
        if (data == null || data.isEmpty()) {
            return;
        }

        try {
            Field[] fields = type.getDeclaredFields();
            
            // Write Header
            for (int i = 0; i < fields.length; i++) {
                writer.append(escapeSpecialCharacters(fields[i].getName()));
                if (i < fields.length - 1) {
                    writer.append(DELIMITER);
                }
            }
            writer.append(NEW_LINE);

            // Write Data
            for (T item : data) {
                for (int i = 0; i < fields.length; i++) {
                    fields[i].setAccessible(true);
                    Object value = fields[i].get(item);
                    String strValue = value != null ? value.toString() : "";
                    writer.append(escapeSpecialCharacters(strValue));
                    if (i < fields.length - 1) {
                        writer.append(DELIMITER);
                    }
                }
                writer.append(NEW_LINE);
            }
        } catch (IOException | IllegalAccessException e) {
            throw new DataExportException("Failed to export data to CSV", e);
        }
    }

    private static String escapeSpecialCharacters(String data) {
        String escapedData = data.replaceAll("\\R", " ");
        if (data.contains(DELIMITER) || data.contains(QUOTE) || data.contains("'")) {
            data = data.replace(QUOTE, QUOTE + QUOTE);
            escapedData = QUOTE + data + QUOTE;
        }
        return escapedData;
    }
}
