package com.rs.game.player.client;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.channels.FileChannel;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

/**
 * Account-scoped storage for the revision-950 client's permanent integer-variable domain.
 *
 * <p>This deliberately stores the native values themselves, not an interpretation of panel
 * geometry. The login service overlays the returned map onto its healthy bootstrap defaults.
 */
public final class Native950ServerpermStore {
    private static final byte[] MAGIC = {'N', '9', '5', '0', 'S', 'P', 'V', '1'};
    private static final int VERSION = 1;
    private static final int DIGEST_BYTES = 32;
    /** Bounds both the durable file and a session's unacknowledged native batch. */
    public static final int MAX_ENTRIES = 4096;
    private static final int MAX_FILE_BYTES = 32768;

    private final Path directory;
    private final Set<Integer> allowedIds;

    public Native950ServerpermStore(Path directory, Set<Integer> allowedIds) {
        this.directory = Objects.requireNonNull(directory, "directory").toAbsolutePath().normalize();
        this.allowedIds = Collections.unmodifiableSet(new java.util.HashSet<Integer>(
                Objects.requireNonNull(allowedIds, "allowedIds")));
        if (this.allowedIds.isEmpty()) throw new IllegalArgumentException("No permitted server-permanent variables");
    }

    /** Loads one valid native map, or throws without changing the existing on-disk file. */
    public synchronized Map<Integer, Integer> load(String username) throws IOException {
        String canonical = Native950Save.canonicalUsername(username);
        Path path = profilePath(canonical);
        if (!Files.exists(path, LinkOption.NOFOLLOW_LINKS)) return Collections.emptyMap();
        byte[] all = Files.readAllBytes(path);
        if (all.length < MAGIC.length + 4 + 2 + 2 + DIGEST_BYTES || all.length > MAX_FILE_BYTES)
            throw new IOException("Invalid native serverperm file size");
        byte[] body = new byte[all.length - DIGEST_BYTES];
        System.arraycopy(all, 0, body, 0, body.length);
        byte[] expected = digest(body);
        for (int i = 0; i < DIGEST_BYTES; i++) if (expected[i] != all[body.length + i])
            throw new IOException("Native serverperm checksum mismatch");
        try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(body))) {
            byte[] magic = new byte[MAGIC.length];
            in.readFully(magic);
            if (!java.util.Arrays.equals(MAGIC, magic)) throw new IOException("Unknown native serverperm format");
            if (in.readInt() != VERSION) throw new IOException("Unsupported native serverperm version");
            int nameLength = in.readUnsignedShort();
            if (nameLength < 1 || nameLength > 64) throw new IOException("Invalid native serverperm account length");
            byte[] name = new byte[nameLength];
            in.readFully(name);
            if (!canonical.equals(new String(name, StandardCharsets.US_ASCII)))
                throw new IOException("Native serverperm file belongs to another account");
            int count = in.readUnsignedShort();
            if (count > MAX_ENTRIES || in.available() != count * 6)
                throw new IOException("Invalid native serverperm record shape");
            Map<Integer, Integer> values = new LinkedHashMap<Integer, Integer>();
            for (int i = 0; i < count; i++) {
                int id = in.readUnsignedShort();
                if (!allowedIds.contains(id) || values.put(id, in.readInt()) != null)
                    throw new IOException("Invalid native serverperm record");
            }
            return Collections.unmodifiableMap(values);
        }
    }

    /** Invalid or stale private state must not prevent a healthy default bootstrap. */
    public synchronized Map<Integer, Integer> loadOrEmpty(String username) {
        try {
            return load(username);
        } catch (IOException failure) {
            System.out.println("[Ataraxia950] Ignoring invalid native serverperm state for "
                    + Native950Save.canonicalUsername(username) + ": " + failure.getMessage());
            return Collections.emptyMap();
        }
    }

    /**
     * Applies the native client's saved values without changing the shape of the healthy
     * bootstrap. Values absent from {@code persisted} deliberately retain their current default.
     */
    public static void overlay(Map<Integer, Integer> defaults, Map<Integer, Integer> persisted) {
        Objects.requireNonNull(defaults, "defaults");
        Objects.requireNonNull(persisted, "persisted");
        defaults.putAll(persisted);
    }

    /** Atomically replaces one account's validated permanent-variable map. */
    public synchronized void save(String username, Map<Integer, Integer> values) throws IOException {
        String canonical = Native950Save.canonicalUsername(username);
        Objects.requireNonNull(values, "values");
        if (values.size() > MAX_ENTRIES) throw new IOException("Too many native serverperm values");
        TreeMap<Integer, Integer> ordered = new TreeMap<Integer, Integer>();
        for (Map.Entry<Integer, Integer> entry : values.entrySet()) {
            Integer id = entry.getKey();
            if (id == null || entry.getValue() == null || id < 0 || id > 65535 || !allowedIds.contains(id))
                throw new IOException("Refusing invalid native serverperm value");
            ordered.put(id, entry.getValue());
        }
        byte[] body;
        try (ByteArrayOutputStream bytes = new ByteArrayOutputStream(); DataOutputStream out = new DataOutputStream(bytes)) {
            out.write(MAGIC);
            out.writeInt(VERSION);
            byte[] name = canonical.getBytes(StandardCharsets.US_ASCII);
            out.writeShort(name.length);
            out.write(name);
            out.writeShort(ordered.size());
            for (Map.Entry<Integer, Integer> entry : ordered.entrySet()) {
                out.writeShort(entry.getKey());
                out.writeInt(entry.getValue());
            }
            out.flush();
            body = bytes.toByteArray();
        }
        if (body.length + DIGEST_BYTES > MAX_FILE_BYTES) throw new IOException("Native serverperm file exceeds limit");
        byte[] encoded = new byte[body.length + DIGEST_BYTES];
        System.arraycopy(body, 0, encoded, 0, body.length);
        System.arraycopy(digest(body), 0, encoded, body.length, DIGEST_BYTES);
        Files.createDirectories(directory);
        Path target = profilePath(canonical);
        Path temporary = Files.createTempFile(directory, ".native950-serverperm-", ".tmp");
        try {
            try (FileChannel channel = FileChannel.open(temporary, StandardOpenOption.WRITE,
                    StandardOpenOption.TRUNCATE_EXISTING, LinkOption.NOFOLLOW_LINKS)) {
                java.nio.ByteBuffer buffer = java.nio.ByteBuffer.wrap(encoded);
                while (buffer.hasRemaining()) channel.write(buffer);
                channel.force(true);
            }
            try {
                Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException unsupported) {
                throw new IOException("Native serverperm storage requires atomic replacement", unsupported);
            }
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    private Path profilePath(String canonical) {
        byte[] hash = digest(canonical.getBytes(StandardCharsets.US_ASCII));
        StringBuilder file = new StringBuilder(80);
        for (byte value : hash)
            file.append(Character.forDigit((value >>> 4) & 15, 16)).append(Character.forDigit(value & 15, 16));
        return directory.resolve(file.append(".serverperm950").toString());
    }

    private static byte[] digest(byte[] bytes) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(bytes);
        } catch (NoSuchAlgorithmException impossible) {
            throw new AssertionError(impossible);
        }
    }
}
