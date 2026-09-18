package com.carddemo.posttran;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public final class PostTransactionJob {
    private static final DateTimeFormatter PROCESSING_TIMESTAMP =
            DateTimeFormatter.ofPattern("yyyy-MM-dd-HH.mm.ss.SS'0000'");
    private final Clock clock;

    public PostTransactionJob(Clock clock) {
        this.clock = clock;
    }

    public Result run(List<DailyTransaction> daily,
                      IndexedStore<Transaction> tranfile,
                      IndexedStore<CardXref> xref,
                      IndexedStore<Account> accounts,
                      IndexedStore<TranCatBalance> tcatbal) {
        List<String> displays = new ArrayList<>();
        List<String> rejects = new ArrayList<>();
        displays.add("START OF EXECUTION OF PROGRAM CBTRN02C");
        int processed = 0;
        for (DailyTransaction input : daily) {
            processed++;
            RejectReason reason = null;
            CardXref card = xref.read(input.cardNumber()).orElse(null);
            Account account = null;
            if (card == null) {
                reason = RejectReason.INVALID_CARD;
            } else {
                account = accounts.read(card.accountId()).orElse(null);
                if (account == null) {
                    reason = RejectReason.ACCOUNT_NOT_FOUND;
                } else {
                    BigDecimal temporaryBalance = account.cycleCredit()
                            .subtract(account.cycleDebit())
                            .add(input.amount());
                    if (account.creditLimit().compareTo(temporaryBalance) < 0) {
                        reason = RejectReason.OVERLIMIT;
                    }
                    if (account.expirationDate().compareTo(input.originalTimestamp().substring(0, 10)) < 0) {
                        reason = RejectReason.AFTER_EXPIRATION;
                    }
                }
            }
            if (reason != null) {
                rejects.add(reject(input, reason));
                continue;
            }

            String timestamp = LocalDateTime.now(clock).format(PROCESSING_TIMESTAMP);
            Transaction posted = input.asPosted(timestamp);
            String balanceKey = card.accountId() + input.typeCode() + input.categoryCode();
            TranCatBalance balance = tcatbal.read(balanceKey).orElse(null);
            if (balance == null) {
                displays.add("TCATBAL record not found for key : " + balanceKey + ".. Creating.");
                tcatbal.write(TranCatBalance.created(card.accountId(), input.typeCode(),
                        input.categoryCode(), input.amount()));
            } else {
                tcatbal.rewrite(balance.add(input.amount()));
            }
            accounts.rewrite(account.post(input.amount()));
            tranfile.write(posted);
        }
        displays.add(String.format("TRANSACTIONS PROCESSED :%09d", processed));
        displays.add(String.format("TRANSACTIONS REJECTED  :%09d", rejects.size()));
        displays.add("END OF EXECUTION OF PROGRAM CBTRN02C");
        return new Result(processed, rejects.size(), rejects.isEmpty() ? 0 : 4, rejects, displays);
    }

    private static String reject(DailyTransaction input, RejectReason reason) {
        return input.toFixedWidth()
                + String.format("%04d%-76s", reason.code(), reason.description());
    }

    public record Result(int processed, int rejected, int returnCode,
                         List<String> rejectRecords, List<String> displayLines) {
        public Result {
            rejectRecords = List.copyOf(rejectRecords);
            displayLines = List.copyOf(displayLines);
        }
    }
}
