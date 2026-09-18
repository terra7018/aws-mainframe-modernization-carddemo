package com.carddemo.posttran;

import java.math.BigDecimal;

public record Account(
        String id,
        String activeStatus,
        BigDecimal currentBalance,
        BigDecimal creditLimit,
        BigDecimal cashCreditLimit,
        String openDate,
        String expirationDate,
        String reissueDate,
        BigDecimal cycleCredit,
        BigDecimal cycleDebit,
        String addressZip,
        String groupId,
        String filler) implements FixedRecord {
    public static final int LENGTH = 300;

    public static Account parse(String line) {
        String record = FixedWidth.padRight(line, LENGTH);
        return new Account(
                FixedWidth.field(record, 0, 11),
                FixedWidth.field(record, 11, 1),
                ZonedDecimal.parse(FixedWidth.field(record, 12, 12), 2),
                ZonedDecimal.parse(FixedWidth.field(record, 24, 12), 2),
                ZonedDecimal.parse(FixedWidth.field(record, 36, 12), 2),
                FixedWidth.field(record, 48, 10),
                FixedWidth.field(record, 58, 10),
                FixedWidth.field(record, 68, 10),
                ZonedDecimal.parse(FixedWidth.field(record, 78, 12), 2),
                ZonedDecimal.parse(FixedWidth.field(record, 90, 12), 2),
                FixedWidth.field(record, 102, 10),
                FixedWidth.field(record, 112, 10),
                FixedWidth.field(record, 122, 178));
    }

    @Override
    public String key() {
        return id;
    }

    @Override
    public String toFixedWidth() {
        return new FixedWidth.Builder()
                .exact(id, 11).exact(activeStatus, 1)
                .money(currentBalance, 10, 2).money(creditLimit, 10, 2)
                .money(cashCreditLimit, 10, 2)
                .exact(openDate, 10).exact(expirationDate, 10).exact(reissueDate, 10)
                .money(cycleCredit, 10, 2).money(cycleDebit, 10, 2)
                .exact(addressZip, 10).exact(groupId, 10).exact(filler, 178)
                .build(LENGTH);
    }

    public Account post(BigDecimal amount) {
        BigDecimal balance = currentBalance.add(amount);
        BigDecimal newCycleCredit = cycleCredit;
        BigDecimal newCycleDebit = cycleDebit;
        if (amount.signum() >= 0) {
            newCycleCredit = newCycleCredit.add(amount);
        } else {
            newCycleDebit = newCycleDebit.add(amount);
        }
        return new Account(id, activeStatus, balance, creditLimit, cashCreditLimit, openDate,
                expirationDate, reissueDate, newCycleCredit, newCycleDebit, addressZip, groupId, filler);
    }
}
