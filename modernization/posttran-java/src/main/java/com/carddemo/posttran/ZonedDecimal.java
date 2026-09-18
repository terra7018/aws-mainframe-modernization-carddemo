package com.carddemo.posttran;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class ZonedDecimal {
    private ZonedDecimal() {
    }

    public static BigDecimal parse(String field, int scale) {
        if (field == null || field.isEmpty() || field.trim().isEmpty()) {
            return BigDecimal.ZERO.setScale(scale);
        }
        String value = field;
        char last = value.charAt(value.length() - 1);
        int sign = 1;
        int digit = Character.digit(last, 10);
        if (last == '{') {
            digit = 0;
        } else if (last >= 'A' && last <= 'I') {
            digit = last - 'A' + 1;
        } else if (last == '}') {
            sign = -1;
            digit = 0;
        } else if (last >= 'J' && last <= 'R') {
            sign = -1;
            digit = last - 'J' + 1;
        } else if (digit < 0) {
            throw new IllegalArgumentException("invalid zoned decimal sign: " + last);
        }
        String prefix = value.substring(0, value.length() - 1);
        if (!prefix.chars().allMatch(Character::isDigit)) {
            throw new IllegalArgumentException("invalid zoned decimal digits: " + field);
        }
        String digits = prefix + digit;
        BigDecimal result = new BigDecimal(digits).movePointLeft(scale);
        return sign < 0 ? result.negate() : result;
    }

    public static String format(BigDecimal value, int digits, int scale) {
        if (value == null) {
            throw new IllegalArgumentException("value must not be null");
        }
        BigDecimal scaled = value.setScale(scale, RoundingMode.UNNECESSARY);
        boolean negative = scaled.signum() < 0;
        BigDecimal absolute = scaled.abs().movePointRight(scale);
        String unscaled = absolute.toBigIntegerExact().toString();
        if (unscaled.length() > digits + scale) {
            throw new ArithmeticException("zoned decimal overflow for " + digits + " digits");
        }
        unscaled = "0".repeat(digits + scale - unscaled.length()) + unscaled;
        char last = unscaled.charAt(unscaled.length() - 1);
        int digit = last - '0';
        if (negative) {
            last = digit == 0 ? '}' : (char) ('J' + digit - 1);
        } else {
            last = digit == 0 ? '{' : (char) ('A' + digit - 1);
        }
        return unscaled.substring(0, unscaled.length() - 1) + last;
    }

    public static long parseUnsigned(String field) {
        if (field == null || field.isBlank()) {
            return 0;
        }
        try {
            return Long.parseLong(field);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("invalid unsigned numeric field: " + field, e);
        }
    }

    public static String formatUnsigned(long value, int digits) {
        if (value < 0) {
            throw new IllegalArgumentException("unsigned value is negative");
        }
        String result = Long.toString(value);
        if (result.length() > digits) {
            throw new ArithmeticException("unsigned numeric overflow for " + digits + " digits");
        }
        return "0".repeat(digits - result.length()) + result;
    }
}
