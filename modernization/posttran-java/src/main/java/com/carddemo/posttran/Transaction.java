package com.carddemo.posttran;

import java.math.BigDecimal;

public record Transaction(
        String id,
        String typeCode,
        String categoryCode,
        String source,
        String description,
        BigDecimal amount,
        String merchantId,
        String merchantName,
        String merchantCity,
        String merchantZip,
        String cardNumber,
        String originalTimestamp,
        String processingTimestamp,
        String filler) implements FixedRecord {
    public static final int LENGTH = 350;

    public static Transaction parse(String line) {
        String record = FixedWidth.padRight(line, LENGTH);
        return new Transaction(
                FixedWidth.field(record, 0, 16),
                FixedWidth.field(record, 16, 2),
                FixedWidth.field(record, 18, 4),
                FixedWidth.field(record, 22, 10),
                FixedWidth.field(record, 32, 100),
                ZonedDecimal.parse(FixedWidth.field(record, 132, 11), 2),
                FixedWidth.field(record, 143, 9),
                FixedWidth.field(record, 152, 50),
                FixedWidth.field(record, 202, 50),
                FixedWidth.field(record, 252, 10),
                FixedWidth.field(record, 262, 16),
                FixedWidth.field(record, 278, 26),
                FixedWidth.field(record, 304, 26),
                FixedWidth.field(record, 330, 20));
    }

    @Override
    public String key() {
        return id;
    }

    @Override
    public String toFixedWidth() {
        return new FixedWidth.Builder()
                .exact(id, 16).exact(typeCode, 2).exact(categoryCode, 4).exact(source, 10)
                .exact(description, 100).money(amount, 9, 2).exact(merchantId, 9)
                .exact(merchantName, 50).exact(merchantCity, 50).exact(merchantZip, 10)
                .exact(cardNumber, 16).exact(originalTimestamp, 26).exact(processingTimestamp, 26)
                .exact(filler, 20).build(LENGTH);
    }
}
