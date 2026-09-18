package com.carddemo.posttran;

import java.math.BigDecimal;

public final class FixedWidth {
    private FixedWidth() {
    }

    public static String field(String record, int offset, int length) {
        if (record.length() < offset + length) {
            throw new IllegalArgumentException("record is shorter than requested field");
        }
        return record.substring(offset, offset + length);
    }

    public static String padRight(String value, int length) {
        if (value == null) {
            value = "";
        }
        if (value.length() > length) {
            throw new IllegalArgumentException("field exceeds fixed width " + length);
        }
        return value + " ".repeat(length - value.length());
    }

    public static String required(String value, int length) {
        if (value == null || value.length() != length) {
            throw new IllegalArgumentException("expected field length " + length);
        }
        return value;
    }

    public static final class Builder {
        private final StringBuilder value = new StringBuilder();

        public Builder text(String field, int length) {
            value.append(padRight(field, length));
            return this;
        }

        public Builder exact(String field, int length) {
            value.append(required(field, length));
            return this;
        }

        public Builder unsigned(String field, int digits) {
            value.append(FixedWidth.padRight(field, digits));
            return this;
        }

        public Builder unsigned(long number, int digits) {
            value.append(ZonedDecimal.formatUnsigned(number, digits));
            return this;
        }

        public Builder money(BigDecimal number, int digits, int scale) {
            value.append(ZonedDecimal.format(number, digits, scale));
            return this;
        }

        public String build(int expectedLength) {
            if (value.length() != expectedLength) {
                throw new IllegalStateException(
                        "built record has length " + value.length() + ", expected " + expectedLength);
            }
            return value.toString();
        }
    }
}
