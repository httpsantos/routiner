package com.rodrigoom.routiner;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Utils {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static long getCurrentTime() {
        return System.currentTimeMillis();
    }

    public static String formatDate(LocalDateTime date) {
        return date.format(formatter);
    }

    public static boolean isValidNumber(String stringId) {
        try {
            Integer.parseInt(stringId);
            return true;
        }
        catch (NumberFormatException ignored) {}

        return false;
    }
}
