package com.example.utils;

public class DataTypeUtils {

    public static Long getLongValue(Object value) {
        if (value == null) return 0L;
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    public static Integer getIntegerValue(Object value) {
        Long longValue = getLongValue(value);
        if (longValue > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        if (longValue < Integer.MIN_VALUE) {
            return Integer.MIN_VALUE;
        }
        return longValue.intValue();
    }

    public static String getStringValue(Object value) {
        return value == null ? "" : value.toString();
    }
}