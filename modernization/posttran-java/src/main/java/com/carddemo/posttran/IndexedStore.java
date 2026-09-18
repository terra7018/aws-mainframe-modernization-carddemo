package com.carddemo.posttran;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.Function;

public final class IndexedStore<R extends FixedRecord> {
    private final TreeMap<String, R> records = new TreeMap<>();
    private final Function<String, R> parser;

    public IndexedStore(Function<String, R> parser) {
        this.parser = parser;
    }

    public Optional<R> read(String key) {
        return Optional.ofNullable(records.get(key));
    }

    public void write(R record) {
        if (records.containsKey(record.key())) {
            throw new IllegalStateException("duplicate indexed key " + record.key());
        }
        records.put(record.key(), record);
    }

    public void rewrite(R record) {
        if (!records.containsKey(record.key())) {
            throw new IllegalStateException("missing indexed key " + record.key());
        }
        records.put(record.key(), record);
    }

    public void loadFixedWidth(Path path) throws IOException {
        records.clear();
        if (!Files.exists(path)) {
            return;
        }
        for (String line : Files.readAllLines(path)) {
            if (!line.isEmpty()) {
                R record = parser.apply(line);
                records.put(record.key(), record);
            }
        }
    }

    public List<String> dumpFixedWidth() {
        return new ArrayList<>(records.values().stream().map(FixedRecord::toFixedWidth).toList());
    }

    public Map<String, R> values() {
        return Map.copyOf(records);
    }
}
