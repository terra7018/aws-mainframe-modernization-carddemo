package com.carddemo.posttran;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.util.ArrayList;
import java.util.List;

public final class Main {
    private Main() {
    }

    public static void main(String[] args) throws Exception {
        Path dailyPath = requiredPath("DD_DALYTRAN");
        Path tranPath = requiredPath("DD_TRANFILE");
        Path xrefPath = requiredPath("DD_XREFFILE");
        Path rejectsPath = requiredPath("DD_DALYREJS");
        Path accountsPath = requiredPath("DD_ACCTFILE");
        Path tcatPath = requiredPath("DD_TCATBALF");

        IndexedStore<Transaction> tranfile = new IndexedStore<>(Transaction::parse);
        IndexedStore<CardXref> xref = new IndexedStore<>(CardXref::parse);
        IndexedStore<Account> accounts = new IndexedStore<>(Account::parse);
        IndexedStore<TranCatBalance> tcatbal = new IndexedStore<>(TranCatBalance::parse);
        tranfile.loadFixedWidth(tranPath);
        xref.loadFixedWidth(xrefPath);
        accounts.loadFixedWidth(accountsPath);
        tcatbal.loadFixedWidth(tcatPath);

        PostTransactionJob.Result result = new PostTransactionJob(Clock.systemDefaultZone())
                .run(readDaily(dailyPath), tranfile, xref, accounts, tcatbal);

        Files.createDirectories(parent(rejectsPath));
        byte[] rejectBytes = result.rejectRecords().stream()
                .map(s -> s.getBytes(StandardCharsets.ISO_8859_1))
                .reduce(new byte[0], Main::concat);
        Files.write(rejectsPath, rejectBytes);
        writeDump(tranPath, tranfile.dumpFixedWidth());
        writeDump(accountsPath, accounts.dumpFixedWidth());
        writeDump(tcatPath, tcatbal.dumpFixedWidth());
        result.displayLines().forEach(System.out::println);
        System.exit(result.returnCode());
    }

    private static Path requiredPath(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("missing environment variable " + name);
        }
        return Path.of(value);
    }

    private static List<DailyTransaction> readDaily(Path path) throws IOException {
        byte[] bytes = Files.readAllBytes(path);
        String content = new String(bytes, StandardCharsets.ISO_8859_1);
        List<DailyTransaction> records = new ArrayList<>();
        if (content.indexOf('\n') >= 0) {
            for (String line : content.split("\\R")) {
                if (!line.isEmpty()) {
                    records.add(DailyTransaction.parse(line));
                }
            }
        } else {
            if (content.length() % DailyTransaction.LENGTH != 0) {
                throw new IllegalArgumentException("raw daily transaction input is not a multiple of 350 bytes");
            }
            for (int offset = 0; offset < content.length(); offset += DailyTransaction.LENGTH) {
                records.add(DailyTransaction.parse(content.substring(offset, offset + DailyTransaction.LENGTH)));
            }
        }
        return records;
    }

    private static void writeDump(Path path, List<String> records) throws IOException {
        Files.createDirectories(parent(path));
        Files.writeString(path, String.join("\n", records) + (records.isEmpty() ? "" : "\n"),
                StandardCharsets.ISO_8859_1);
    }

    private static Path parent(Path path) {
        return path.toAbsolutePath().getParent();
    }

    private static byte[] concat(byte[] first, byte[] second) {
        byte[] result = new byte[first.length + second.length];
        System.arraycopy(first, 0, result, 0, first.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }
}
