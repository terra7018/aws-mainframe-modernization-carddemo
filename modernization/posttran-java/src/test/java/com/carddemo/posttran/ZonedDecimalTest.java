package com.carddemo.posttran;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

class ZonedDecimalTest {
    @Test
    void parsesPositiveAndNegativeOverpunch() {
        assertEquals(new BigDecimal("0.00"), ZonedDecimal.parse("0000000000{", 2));
        assertEquals(new BigDecimal("0.07"), ZonedDecimal.parse("0000000000G", 2));
        assertEquals(new BigDecimal("0.00"), ZonedDecimal.parse("0000000000}", 2));
        assertEquals(new BigDecimal("-0.01"), ZonedDecimal.parse("0000000000J", 2));
        assertEquals(new BigDecimal("12.34"), ZonedDecimal.parse("0000000123D", 2));
        assertEquals(new BigDecimal("12.34"), ZonedDecimal.parse("00000001234", 2));
    }

    @Test
    void formatsOverpunchAndRoundTrips() {
        assertEquals("00000000000{", ZonedDecimal.format(new BigDecimal("0.00"), 10, 2));
        assertEquals("00000000000G", ZonedDecimal.format(new BigDecimal("0.07"), 10, 2));
        assertEquals("00000000000J", ZonedDecimal.format(new BigDecimal("-0.01"), 10, 2));
        assertEquals(new BigDecimal("-12.34"),
                ZonedDecimal.parse(ZonedDecimal.format(new BigDecimal("-12.34"), 9, 2), 2));
    }

    @Test
    void checksOverflowAndUnsignedValues() {
        assertEquals("00042", ZonedDecimal.formatUnsigned(42, 5));
        assertEquals(42, ZonedDecimal.parseUnsigned("00042"));
        assertThrows(ArithmeticException.class,
                () -> ZonedDecimal.format(new BigDecimal("100.00"), 2, 2));
        assertThrows(ArithmeticException.class, () -> ZonedDecimal.formatUnsigned(100000, 5));
    }
}
