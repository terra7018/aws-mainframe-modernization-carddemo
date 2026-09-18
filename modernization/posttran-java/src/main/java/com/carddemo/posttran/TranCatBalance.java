package com.carddemo.posttran;

import java.math.BigDecimal;

public record TranCatBalance(
        String accountId,
        String typeCode,
        String categoryCode,
        BigDecimal balance,
        String filler) implements FixedRecord {
    public static final int LENGTH = 50;

    public static TranCatBalance parse(String line) {
        String record = FixedWidth.padRight(line, LENGTH);
        return new TranCatBalance(
                FixedWidth.field(record, 0, 11),
                FixedWidth.field(record, 11, 2),
                FixedWidth.field(record, 13, 4),
                ZonedDecimal.parse(FixedWidth.field(record, 17, 11), 2),
                FixedWidth.field(record, 28, 22));
    }

    @Override
    public String key() {
        return accountId + typeCode + categoryCode;
    }

    @Override
    public String toFixedWidth() {
        return new FixedWidth.Builder()
                .exact(accountId, 11).exact(typeCode, 2).exact(categoryCode, 4)
                .money(balance, 9, 2).exact(filler, 22).build(LENGTH);
    }

    public TranCatBalance add(BigDecimal amount) {
        return new TranCatBalance(accountId, typeCode, categoryCode, balance.add(amount), filler);
    }

    public static TranCatBalance created(String accountId, String typeCode, String categoryCode,
                                         BigDecimal amount) {
        return new TranCatBalance(accountId, typeCode, categoryCode, amount,
                "0".repeat(22));
    }
}
