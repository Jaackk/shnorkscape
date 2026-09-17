package com.rs.tools;

import com.rs.cache.Cache;
import com.rs.cache.filestore.store.Index;
import com.rs.cache.loaders.IComponentDefinitions;
import com.rs.network.io.InputStream;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Read-only cache archaeology probe for the Management Windows chrome
 * (enum 7699 / struct 22 index) used by Native950Settings and
 * Native950SkillGuide. THIS TOOL DOES NOT WRITE TO THE CACHE. It only reads
 * via the existing Cache.STORE flat-cache reader (same mechanism
 * Native950Settings.verify() already uses for its SHA-256 pins) and writes a
 * plain-text report to info/chrome-probe.txt.
 *
 * Binary layout for enum (index 17) and struct (index 22) config archives is
 * reproduced from OpenNXT's own codecs:
 *   OpenNXT/src/main/kotlin/com/opennxt/resources/config/enums/EnumFilesystemCodec.kt
 *   OpenNXT/src/main/kotlin/com/opennxt/resources/config/structs/StructFilesystemCodec.kt
 * Archive/file addressing (id >> 8, id & 255 for enums; id >> 5, id & 31 for
 * structs) is cross-checked against Native950Settings.verify()'s own
 * already-pinned group/file numbers, e.g. pin(17, 30, 19, ...) for enum 7699
 * (7699 >> 8 == 30, 7699 & 255 == 19) and pin(22, 665, 21, ...) for struct
 * 21301 (21301 >> 5 == 665, 21301 & 31 == 21) and pin(22, 661, 26, ...) for
 * struct 21178 (21178 >> 5 == 661, 21178 & 31 == 26).
 *
 * Run with:
 *   java -cp <project-classpath> com.rs.tools.Native950ChromeProbe <950-flat-cache-path>
 *
 * Uses Cache.initFlatReadOnly (the same read-only entry point every other
 * Native950*Smoke/Probe tool in this codebase uses) rather than Cache.init(),
 * so there is no writable dat2/index handle opened at all.
 */
public final class Native950ChromeProbe {

    // The two enums already implicated by the skill-guide/settings evidence.
    private static final int[] ENUM_IDS = {7699, 7716};

    // Structs already named in the notes/M7b + M7d evidence and in
    // Native950Settings/Native950SkillGuide, dumped up front for convenience
    // even before the enum walk reaches them.
    private static final int[] KNOWN_STRUCT_IDS = {
            21301, // Management Windows (enum 7716 key 1001)
            21304, // Central Interface (enum 7716 key 1007)
            40393, // Central Interface (Large) (enum 7716 key 1047)
            21178, // Settings (enum 7699 key 9)
            21179, // Gameplay page struct
            21182, // Graphics page struct
            21181, // Controls page struct
            21183, // Audio page struct
            44487, // Ribbon page struct
            52418, // Accessibility page struct
            // Hero's own page-interface structs (enum 7699 key 0, struct 21142):
            // 3448=21143, 3449=21144, 3450=21148, 3451=21146, 3452=50663.
            // struct 21144 turned up in the interface-1218 scan as param 3456=1218.
            21143, 21144, 21148, 21146, 50663,
            // The other five hits from the interface-1218 scan, for context.
            29860, 36847, 45661, 47398, 50475,
    };

    private static final int TARGET_INTERFACE = 1218; // the skill guide

    public static void main(String[] args) throws IOException {
        if (args.length != 1) throw new IllegalArgumentException("Usage: Native950ChromeProbe <950-flat-cache-path>");
        Cache.initFlatReadOnly(Paths.get(args[0]).toAbsolutePath().normalize());
        if (!Cache.isFlatReadOnly()) {
            System.out.println("[probe] WARNING: cache is not the read-only flat store; aborting to avoid any write path.");
            return;
        }

        Index enumIndex = Cache.STORE.getIndexes()[17];
        Index structIndex = Cache.STORE.getIndexes()[22];

        File outFile = new File("info/chrome-probe.txt");
        outFile.getParentFile().mkdirs();
        try (PrintWriter out = new PrintWriter(outFile, "UTF-8")) {
            out.println("Native950ChromeProbe report (read-only cache archaeology)");
            out.println("===========================================================");

            Map<Integer, Map<Integer, Object>> decodedEnums = new LinkedHashMap<>();
            for (int enumId : ENUM_IDS) {
                out.println();
                out.println("#### enum " + enumId + " ####");
                Map<Integer, Object> values = decodeEnum(enumIndex, enumId, out);
                decodedEnums.put(enumId, values);
            }

            out.println();
            out.println("#### struct dumps reachable from the above enums ####");
            for (Map<Integer, Object> values : decodedEnums.values()) {
                for (Map.Entry<Integer, Object> e : values.entrySet()) {
                    if (!(e.getValue() instanceof Integer)) continue;
                    int structId = (Integer) e.getValue();
                    out.println();
                    out.println("-- enum key " + e.getKey() + " -> struct " + structId + " --");
                    decodeStruct(structIndex, structId, out, TARGET_INTERFACE);
                }
            }

            out.println();
            out.println("#### explicitly named structs (in case not all were enum values above) ####");
            for (int structId : KNOWN_STRUCT_IDS) {
                out.println();
                out.println("-- struct " + structId + " --");
                decodeStruct(structIndex, structId, out, TARGET_INTERFACE);
            }

            out.println();
            out.println("#### full struct-index scan for any int param encoding interface " + TARGET_INTERFACE + " ####");
            out.println("(matches: raw value == " + TARGET_INTERFACE
                    + ", OR packed component reference with high 16 bits == " + TARGET_INTERFACE + ")");
            scanStructsForInterface(structIndex, TARGET_INTERFACE, out);

            out.println();
            out.println("#### full struct-index scan for any int param encoding interface 1448 (Management Windows shell) ####");
            scanStructsForInterface(structIndex, 1448, out);

            out.println();
            out.println("#### component dump: interface 1477 components 705-720 (the Management Windows chrome range) ####");
            for (int c = 705; c <= 720; c++) dumpComponent(out, 1477, c);

            out.println();
            out.println("#### component dump: interface 1218 (the skill guide) ####");
            for (int c = 0; c < 30; c++) dumpComponent(out, 1218, c);
        }
        System.out.println("[probe] wrote " + outFile.getAbsolutePath());
    }

    // ---- enum decoding (index 17), format per OpenNXT EnumFilesystemCodec ----

    private static Map<Integer, Object> decodeEnum(Index enumIndex, int enumId, PrintWriter out) {
        int archive = enumId >>> 8;
        int file = enumId & 255;
        byte[] data = enumIndex.getFile(archive, file);
        Map<Integer, Object> values = new TreeMap<>();
        if (data == null) {
            out.println("  (missing: index 17 archive " + archive + " file " + file + ")");
            return values;
        }
        out.println("  cache location: index 17 / archive " + archive + " / file " + file
                + " (" + data.length + " bytes)");
        ByteReader b = new ByteReader(data);
        Integer defaultInt = null;
        String defaultString = null;
        char keyTypeChar = '?';
        char valueTypeChar = '?';
        try {
            while (b.hasRemaining()) {
                int opcode = b.u8();
                if (opcode == 0) break;
                switch (opcode) {
                    case 1:
                        keyTypeChar = (char) b.u8();
                        break;
                    case 2:
                        valueTypeChar = (char) b.u8();
                        break;
                    case 3:
                        defaultString = b.string();
                        break;
                    case 4:
                        defaultInt = b.i32();
                        break;
                    case 5:
                    case 6:
                    case 7:
                    case 8: {
                        boolean stringValues = (opcode == 5 || opcode == 7);
                        if (opcode == 7 || opcode == 8) b.skip(2); // array-size hint, unused here
                        int size = b.u16();
                        for (int i = 0; i < size; i++) {
                            int key = (opcode == 5 || opcode == 6) ? b.i32() : b.u16();
                            Object value = stringValues ? b.string() : (Object) b.i32();
                            values.put(key, value);
                        }
                        break;
                    }
                    case 101:
                        keyTypeChar = (char) b.smallSmart();
                        break;
                    case 102:
                        valueTypeChar = (char) b.smallSmart();
                        break;
                    default:
                        out.println("  !! unknown enum opcode " + opcode + " at offset " + (b.pos - 1)
                                + "; aborting further decode of this enum");
                        printValues(out, values);
                        return values;
                }
            }
        } catch (RuntimeException ex) {
            out.println("  !! decode exception: " + ex + " (partial results below)");
        }
        out.println("  keyType=" + keyTypeChar + " valueType=" + valueTypeChar
                + " defaultInt=" + defaultInt + " defaultString=" + defaultString);
        out.println("  " + values.size() + " entries:");
        printValues(out, values);
        return values;
    }

    private static void printValues(PrintWriter out, Map<Integer, Object> values) {
        for (Map.Entry<Integer, Object> e : values.entrySet()) {
            out.println("    key " + e.getKey() + " -> " + e.getValue());
        }
    }

    // ---- struct decoding (index 22), format per OpenNXT StructFilesystemCodec ----

    private static void decodeStruct(Index structIndex, int structId, PrintWriter out, int targetInterface) {
        int archive = structId >>> 5;
        int file = structId & 31;
        byte[] data = structIndex.getFile(archive, file);
        if (data == null) {
            out.println("  (missing: index 22 archive " + archive + " file " + file + ")");
            return;
        }
        out.println("  cache location: index 22 / archive " + archive + " / file " + file
                + " (" + data.length + " bytes)");
        Map<Integer, Object> params = decodeStructParams(data);
        if (params == null) {
            out.println("  !! could not decode (unexpected opcode)");
            return;
        }
        out.println("  " + params.size() + " params:");
        for (Map.Entry<Integer, Object> e : params.entrySet()) {
            Object v = e.getValue();
            if (v instanceof Integer) {
                int iv = (Integer) v;
                String note = "";
                if (iv == targetInterface) note = "  <-- exact match on interface " + targetInterface;
                else if ((iv >>> 16) == targetInterface) note = "  <-- packed component " + targetInterface + ":" + (iv & 0xFFFF);
                out.println("    param " + e.getKey() + " = " + iv + note);
            } else {
                out.println("    param " + e.getKey() + " = \"" + v + "\"");
            }
        }
    }

    /** Returns null if the file doesn't look like a struct (opcode 249 stream). */
    private static Map<Integer, Object> decodeStructParams(byte[] data) {
        Map<Integer, Object> params = new TreeMap<>();
        ByteReader b = new ByteReader(data);
        try {
            while (b.hasRemaining()) {
                int opcode = b.u8();
                if (opcode == 0) break;
                if (opcode != 249) return null;
                int size = b.u8();
                for (int i = 0; i < size; i++) {
                    boolean isString = b.u8() == 1;
                    int key = b.medium();
                    Object value = isString ? b.string() : (Object) b.i32();
                    params.put(key, value);
                }
            }
        } catch (RuntimeException ex) {
            return params.isEmpty() ? null : params;
        }
        return params;
    }

    private static void scanStructsForInterface(Index structIndex, int targetInterface, PrintWriter out) {
        int hits = 0;
        int scanned = 0;
        int[] validArchives = structIndex.getTable().getValidArchiveIds();
        for (int archiveId : validArchives) {
            int[] validFiles = structIndex.getTable().getArchives()[archiveId].getValidFileIds();
            for (int fileId : validFiles) {
                byte[] data = structIndex.getFile(archiveId, fileId);
                if (data == null) continue;
                scanned++;
                Map<Integer, Object> params = decodeStructParams(data);
                if (params == null) continue;
                int structId = archiveId * 32 + fileId;
                for (Map.Entry<Integer, Object> e : params.entrySet()) {
                    if (!(e.getValue() instanceof Integer)) continue;
                    int iv = (Integer) e.getValue();
                    if (iv == targetInterface) {
                        out.println("  struct " + structId + " param " + e.getKey()
                                + " = " + iv + "  (exact interface id match)");
                        hits++;
                    } else if ((iv >>> 16) == targetInterface) {
                        out.println("  struct " + structId + " param " + e.getKey()
                                + " = " + iv + "  (packed component " + targetInterface + ":" + (iv & 0xFFFF) + ")");
                        hits++;
                    }
                }
            }
        }
        out.println("  scanned " + scanned + " struct files across " + validArchives.length + " archives; " + hits + " hits");
    }

    // ---- component dump (index 3), reusing the same decode path as InterfaceFullDumper ----

    private static void dumpComponent(PrintWriter out, int interfaceId, int componentId) {
        byte[] data;
        try {
            data = Cache.STORE.getIndexes()[3].getFile(interfaceId, componentId);
        } catch (Throwable t) {
            out.println("  [comp " + componentId + "] fetch failed: " + t);
            return;
        }
        if (data == null || data.length == 0) {
            out.println("  [comp " + componentId + "] (no data)");
            return;
        }
        IComponentDefinitions def = new IComponentDefinitions();
        def.ihash = componentId + (interfaceId << 16);
        try {
            def.decode(new InputStream(data));
        } catch (Throwable t) {
            out.println("  [comp " + componentId + "] decode failed: " + t.getClass().getSimpleName() + ": " + t.getMessage());
            return;
        }
        out.println("  [comp " + componentId + "] type=" + def.type + " contentType=" + def.contentType
                + " hidden=" + def.hidden + " size=" + def.width + "x" + def.height
                + " pos=" + def.positionX + "," + def.positionY
                + (isNonEmpty(def.text) ? " text=\"" + def.text + "\"" : ""));
        printHook(out, "    onLoad     ", def.onLoadHook);
        printHook(out, "    onClick    ", def.onClickHook);
        printHook(out, "    onVarpTx   ", def.onVarpTransmitHook);
        printHook(out, "    onVarcTx   ", def.onVarcTransmitHook);
        printHook(out, "    onVarcstrTx", def.onVarcstrTransmitHook);
        printHook(out, "    onInvTx    ", def.onInvTransmitHook);
        printHook(out, "    onStatTx   ", def.onStatTransmitHook);
        printHook(out, "    onOpt      ", def.onOptHook);
        printHook(out, "    onTimer    ", def.onTimerHook);
    }

    private static boolean isNonEmpty(String s) { return s != null && !s.isEmpty(); }

    private static void printHook(PrintWriter out, String label, Object[] hook) {
        if (hook == null || hook.length == 0) return;
        StringBuilder sb = new StringBuilder(label).append(" = script=").append(hook[0]);
        if (hook.length > 1) {
            sb.append(" args=[");
            for (int i = 1; i < hook.length; i++) {
                if (i > 1) sb.append(", ");
                sb.append(hook[i]);
            }
            sb.append("]");
        }
        out.println(sb.toString());
    }

    // ---- tiny big-endian byte reader ----

    private static final class ByteReader {
        final byte[] data;
        int pos;

        ByteReader(byte[] data) { this.data = data; }

        boolean hasRemaining() { return pos < data.length; }

        int u8() { return data[pos++] & 0xFF; }

        int u16() { return (u8() << 8) | u8(); }

        int i32() { return (u8() << 24) | (u8() << 16) | (u8() << 8) | u8(); }

        int medium() { return (u8() << 16) | (u8() << 8) | u8(); }

        void skip(int n) { pos += n; }

        int smallSmart() {
            int peek = data[pos] & 0xFF;
            if (peek < 128) return u8();
            return u16() - 32768;
        }

        String string() {
            int start = pos;
            while (pos < data.length && data[pos] != 0) pos++;
            String s = new String(data, start, pos - start, StandardCharsets.ISO_8859_1);
            if (pos < data.length) pos++; // consume terminator
            return s;
        }
    }

    private Native950ChromeProbe() { }
}
