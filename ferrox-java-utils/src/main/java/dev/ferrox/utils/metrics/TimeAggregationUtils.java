package dev.ferrox.utils.metrics;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.Locale;

public class TimeAggregationUtils {

    /**
     * Formats a date to YYYY-MM for monthly grouping.
     */
    public static String toMonthString(LocalDate date) {
        if (date == null) return null;
        return date.format(DateTimeFormatter.ofPattern("yyyy-MM"));
    }

    /**
     * Formats a date to YYYY-WW for weekly grouping.
     */
    public static String toWeekString(LocalDate date) {
        if (date == null) return null;
        WeekFields weekFields = WeekFields.of(Locale.getDefault());
        int year = date.getYear();
        int week = date.get(weekFields.weekOfWeekBasedYear());
        return String.format("%d-W%02d", year, week);
    }

    /**
     * Gets the start of the month for a given date.
     */
    public static LocalDate startOfMonth(LocalDate date) {
        if (date == null) return null;
        return date.with(TemporalAdjusters.firstDayOfMonth());
    }

    /**
     * Gets the end of the month for a given date.
     */
    public static LocalDate endOfMonth(LocalDate date) {
        if (date == null) return null;
        return date.with(TemporalAdjusters.lastDayOfMonth());
    }
}
