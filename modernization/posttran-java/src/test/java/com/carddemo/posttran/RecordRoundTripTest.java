package com.carddemo.posttran;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Function;

import org.junit.jupiter.api.Test;

class RecordRoundTripTest {
    private static final Path DATA = Path.of("..", "..", "app", "data", "ASCII").normalize();

    @Test
    void dailyTransactionsRoundTrip() throws IOException {
        assertRoundTrip(DATA.resolve("dailytran.txt"), DailyTransaction::parse, 350);
    }

    @Test
    void cardXrefsRoundTripWithCobolPadding() throws IOException {
        assertRoundTrip(DATA.resolve("cardxref.txt"), CardXref::parse, 50);
    }

    @Test
    void accountsRoundTrip() throws IOException {
        assertRoundTrip(DATA.resolve("acctdata.txt"), Account::parse, 300);
    }

    @Test
    void categoryBalancesRoundTrip() throws IOException {
        assertRoundTrip(DATA.resolve("tcatbal.txt"), TranCatBalance::parse, 50);
    }

    private static <R extends FixedRecord> void assertRoundTrip(
            Path path, Function<String, R> parser, int length) throws IOException {
        List<String> lines = Files.readAllLines(path);
        for (String line : lines) {
            assertEquals(length, parser.apply(line).toFixedWidth().length());
            assertEquals(FixedWidth.padRight(line, length), parser.apply(line).toFixedWidth());
        }
    }
}
