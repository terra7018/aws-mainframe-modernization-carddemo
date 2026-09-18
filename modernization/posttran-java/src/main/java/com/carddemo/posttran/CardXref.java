package com.carddemo.posttran;

public record CardXref(String cardNumber, String customerId, String accountId, String filler)
        implements FixedRecord {
    public static final int LENGTH = 50;

    public static CardXref parse(String line) {
        String record = FixedWidth.padRight(line, LENGTH);
        return new CardXref(
                FixedWidth.field(record, 0, 16),
                FixedWidth.field(record, 16, 9),
                FixedWidth.field(record, 25, 11),
                FixedWidth.field(record, 36, 14));
    }

    @Override
    public String key() {
        return cardNumber;
    }

    @Override
    public String toFixedWidth() {
        return new FixedWidth.Builder()
                .exact(cardNumber, 16).exact(customerId, 9).exact(accountId, 11)
                .exact(filler, 14).build(LENGTH);
    }
}
