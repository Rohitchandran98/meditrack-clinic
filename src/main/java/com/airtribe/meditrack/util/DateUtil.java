package com.airtribe.meditrack.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Date/time parsing and formatting utilities.
 */
public final class DateUtil {

    public static final DateTimeFormatter DATETIME_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    public static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private DateUtil() {}

    public static LocalDateTime parse(String s) {
        try {
            return LocalDateTime.parse(s.trim(), DATETIME_FMT);
        } catch (DateTimeParseException e) {
            // Try date-only input and default to 09:00
            try {
                return LocalDate.parse(s.trim(), DATE_FMT).atTime(9, 0);
            } catch (DateTimeParseException ex) {
                throw new IllegalArgumentException(
                        "Invalid date/time format. Expected: yyyy-MM-dd HH:mm or yyyy-MM-dd", ex);
            }
        }
    }

    public static String format(LocalDateTime dt) {
        return dt.format(DATETIME_FMT);
    }

    public static boolean isFuture(LocalDateTime dt) {
        return dt.isAfter(LocalDateTime.now());
    }

    /** Returns the next N appointment slots starting from now, spaced 30 min apart. */
    public static java.util.List<String> suggestSlots(int count) {
        java.util.List<String> slots = new java.util.ArrayList<>();
        LocalDateTime base = LocalDateTime.now().plusHours(1)
                .withMinute(0).withSecond(0).withNano(0);
        for (int i = 0; i < count; i++) {
            slots.add(format(base.plusMinutes(30L * i)));
        }
        return slots;
    }
}
