package com.carddemo.posttran;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import org.junit.jupiter.api.Test;

class PostTransactionJobEquivalenceTest {
    private static final Path DATA = Path.of("..", "..", "app", "data", "ASCII").normalize();
    private static final Path GOLDEN = Path.of("src", "test", "resources", "golden");
    private static final Clock FIXED_CLOCK =
            Clock.fixed(Instant.parse("2026-09-18T08:43:36.750Z"), ZoneOffset.UTC);

    @Test
    void matchesGnuCobolGoldenFiles() throws IOException {
        List<DailyTransaction> daily = Files.readAllLines(DATA.resolve("dailytran.txt"))
                .stream().map(DailyTransaction::parse).toList();
        IndexedStore<Transaction> tranfile = new IndexedStore<>(Transaction::parse);
        IndexedStore<CardXref> xref = load(DATA.resolve("cardxref.txt"), CardXref::parse);
        IndexedStore<Account> accounts = load(DATA.resolve("acctdata.txt"), Account::parse);
        IndexedStore<TranCatBalance> tcatbal = load(DATA.resolve("tcatbal.txt"), TranCatBalance::parse);

        PostTransactionJob.Result result = new PostTransactionJob(FIXED_CLOCK)
                .run(daily, tranfile, xref, accounts, tcatbal);

        assertEquals(300, result.processed());
        assertEquals(38, result.rejected());
        assertEquals(4, result.returnCode());
        assertEquals(Files.readString(GOLDEN.resolve("stdout.txt")), String.join("\n", result.displayLines()) + "\n");
        assertEquals(Files.readString(GOLDEN.resolve("tranfile.txt")),
                String.join("\n", maskProcessingTimestamps(tranfile.dumpFixedWidth())) + "\n");
        assertEquals(Files.readString(GOLDEN.resolve("acctfile.txt")),
                String.join("\n", accounts.dumpFixedWidth()) + "\n");
        assertEquals(Files.readString(GOLDEN.resolve("tcatbal.txt")),
                String.join("\n", tcatbal.dumpFixedWidth()) + "\n");
        byte[] rejects = String.join("", result.rejectRecords()).getBytes(StandardCharsets.ISO_8859_1);
        assertArrayEquals(Files.readAllBytes(GOLDEN.resolve("dalyrejs.dat")), rejects);
    }

    @Test
    void identifiesValidationReasonsAndExpirationOverwritesOverlimit() throws IOException {
        DailyTransaction input = Files.readAllLines(DATA.resolve("dailytran.txt"))
                .stream().map(DailyTransaction::parse).findFirst().orElseThrow();
        IndexedStore<Transaction> transactions = new IndexedStore<>(Transaction::parse);
        IndexedStore<CardXref> xref = load(DATA.resolve("cardxref.txt"), CardXref::parse);
        IndexedStore<Account> accounts = load(DATA.resolve("acctdata.txt"), Account::parse);
        IndexedStore<TranCatBalance> balances = load(DATA.resolve("tcatbal.txt"), TranCatBalance::parse);

        PostTransactionJob.Result invalidCard = new PostTransactionJob(FIXED_CLOCK).run(
                List.of(input), transactions, new IndexedStore<>(CardXref::parse), accounts, balances);
        assertEquals("0100", invalidCard.rejectRecords().getFirst().substring(350, 354));

        CardXref card = xref.read(input.cardNumber()).orElseThrow();
        Account account = accounts.read(card.accountId()).orElseThrow();
        Account missing = new Account("99999999999", account.activeStatus(), account.currentBalance(),
                account.creditLimit(), account.cashCreditLimit(), account.openDate(),
                account.expirationDate(), account.reissueDate(), account.cycleCredit(),
                account.cycleDebit(), account.addressZip(), account.groupId(), account.filler());
        IndexedStore<Account> missingAccounts = new IndexedStore<>(Account::parse);
        missingAccounts.write(missing);
        PostTransactionJob.Result invalidAccount = new PostTransactionJob(FIXED_CLOCK).run(
                List.of(input), new IndexedStore<>(Transaction::parse), xref, missingAccounts, balances);
        assertEquals("0101", invalidAccount.rejectRecords().getFirst().substring(350, 354));

        BigDecimal lowLimit = account.cycleCredit().subtract(account.cycleDebit())
                .add(input.amount()).subtract(BigDecimal.ONE);
        Account overlimitExpired = new Account(account.id(), account.activeStatus(), account.currentBalance(),
                lowLimit, account.cashCreditLimit(), account.openDate(), "1900-01-01",
                account.reissueDate(), account.cycleCredit(), account.cycleDebit(),
                account.addressZip(), account.groupId(), account.filler());
        IndexedStore<Account> edgeAccounts = new IndexedStore<>(Account::parse);
        edgeAccounts.write(overlimitExpired);
        PostTransactionJob.Result edge = new PostTransactionJob(FIXED_CLOCK).run(
                List.of(input), new IndexedStore<>(Transaction::parse), xref, edgeAccounts,
                new IndexedStore<>(TranCatBalance::parse));
        assertEquals("0103", edge.rejectRecords().getFirst().substring(350, 354));
    }

    private static <R extends FixedRecord> IndexedStore<R> load(Path path,
                                                                 java.util.function.Function<String, R> parser)
            throws IOException {
        IndexedStore<R> store = new IndexedStore<>(parser);
        store.loadFixedWidth(path);
        return store;
    }

    private static List<String> maskProcessingTimestamps(List<String> records) {
        return records.stream()
                .map(record -> record.substring(0, 304) + " ".repeat(26) + record.substring(330))
                .toList();
    }
}
