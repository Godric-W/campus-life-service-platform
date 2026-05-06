package com.god.common.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class DateTimeUtil {

    public static final String DEFAULT_PATTERN = "yyyy-MM-dd HH:mm:ss";
    public static final DateTimeFormatter DEFAULT_FORMATTER = DateTimeFormatter.ofPattern(DEFAULT_PATTERN);

    private DateTimeUtil() {
    }

    public static String format(LocalDateTime dateTime) {
        return dateTime == null ? null : DEFAULT_FORMATTER.format(dateTime);
    }

    public static LocalDateTime parse(String dateTime) {
        return dateTime == null || dateTime.isBlank() ? null : LocalDateTime.parse(dateTime, DEFAULT_FORMATTER);
    }
}
