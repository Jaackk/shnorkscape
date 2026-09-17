package com.rs.tools.modern;

import com.google.gson.GsonBuilder;
import com.rs.cache.modern.FlatCacheRepository;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.*;

/** Validates modern cache access without booting the game or loading accounts. */
public final class CacheMigrationProbe {
    public static void main(String[] args) throws Exception {
        if (args.length != 1) throw new IllegalArgumentException("Usage: CacheMigrationProbe <OpenRS2 flat cache directory>");
        FlatCacheRepository cache = new FlatCacheRepository(Paths.get(args[0]));
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("cachePath", cache.getRoot().toString());
        report.put("sourceReadOnly", true);
        report.put("scope", "Reference tables and representative raw files; gameplay definition compatibility is not implied");
        List<Object> indexes = new ArrayList<>();
        long totalGroups = 0;
        for (FlatCacheRepository.Index index : cache.getIndexes().values()) {
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("index", index.id);
            entry.put("version", index.version);
            entry.put("crc32", Integer.toUnsignedString(index.crc));
            entry.put("groups", index.getGroups().size());
            entry.put("referenceSha256", sha256(Files.readAllBytes(cache.getRoot().resolve("255").resolve(index.id + ".dat"))));
            indexes.add(entry);
            totalGroups += index.getGroups().size();
        }
        report.put("indexes", indexes);
        report.put("totalGroups", totalGroups);
        List<Object> samples = new ArrayList<>();
        // Known map region layout and resource IDs used by the working947 client.
        sample(cache, samples, "Lumbridge map", 5, 50 | (50 << 7), -1);
        sample(cache, samples, "coins", 19, 995 >>> 8, 995 & 255);
        sample(cache, samples, "logs", 19, 1511 >>> 8, 1511 & 255);
        sample(cache, samples, "NPC definitions", 18, 1 >>> 7, 1 & 127);
        sample(cache, samples, "object definitions", 16, 1276 >>> 8, 1276 & 255);
        sample(cache, samples, "world interface", 3, 1477, -1);
        sample(cache, samples, "minimap interface", 3, 1465, -1);
        report.put("samples", samples);
        System.out.println(new GsonBuilder().setPrettyPrinting().create().toJson(report));
    }

    private static void sample(FlatCacheRepository cache, List<Object> samples, String name, int index, int group, int file) throws Exception {
        Map<Integer, byte[]> files = cache.readGroup(index, group);
        if (file >= 0 && !files.containsKey(file)) throw new IllegalStateException("Missing sample " + name);
        Map<String, Object> entry = new LinkedHashMap<>();
        entry.put("name", name); entry.put("index", index); entry.put("group", group);
        List<Object> contents = new ArrayList<>();
        for (Map.Entry<Integer, byte[]> part : files.entrySet()) {
            if (file >= 0 && part.getKey() != file) continue;
            Map<String, Object> content = new LinkedHashMap<>();
            content.put("file", part.getKey()); content.put("bytes", part.getValue().length);
            content.put("sha256", sha256(part.getValue())); contents.add(content);
        }
        entry.put("files", contents); samples.add(entry);
    }

    private static String sha256(byte[] data) throws Exception {
        StringBuilder out = new StringBuilder();
        for (byte b : MessageDigest.getInstance("SHA-256").digest(data)) out.append(String.format("%02x", b & 255));
        return out.toString();
    }
}
