package com.rs.tools.modern;

import com.google.gson.GsonBuilder;
import com.rs.cache.Cache;
import com.rs.cache.filestore.store.Index;
import com.rs.cache.filestore.store.ReferenceTable;
import com.rs.cache.loaders.AnimationDefinitions;
import com.rs.cache.loaders.ItemDefinitions;
import com.rs.cache.loaders.NPCDefinitions;
import com.rs.cache.loaders.ObjectDefinitions;
import com.rs.cache.loaders.RenderAnimDefinitions;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * P3 gate: decodes EVERY definition file of the 947 flat cache through the strict
 * loaders (items 19, NPCs 18, animations 20, BAS 2/32, objects 16) and prints one
 * JSON report per family. A decoder that skips an unknown opcode or reads through
 * EOF would still "succeed" on a tolerant stream; here every family runs on the
 * strict stream with a trailing-byte check, so ok==total is real evidence that the
 * opcode table is complete for that family.
 *
 * Usage: DefinitionScanProbe &lt;flat cache directory&gt;
 * Exit code 1 when items ok &lt; total-5, objects ok &lt; total, or the production
 * getters disagree with the strict decoder on the failure-path anchors (item 29492,
 * NPC 233, animation 63): a rejected file must be served as the undecodable shell
 * with a registry entry, never silently.
 * Nothing in the cache is modified (read-only flat store).
 */
public final class DefinitionScanProbe {

    /** Decodes one raw file strictly, throwing on any problem. */
    private interface Decoder {
        void decode(int id, byte[] data, StringBuilder trace);
    }

    private static final Pattern OPCODE = Pattern.compile("opcode (\\d+)");
    private static final int FIRST_FAILURES = 20;

    public static void main(String[] args) throws Exception {
        if (args.length != 1) throw new IllegalArgumentException("Usage: DefinitionScanProbe <flat cache directory>");
        Cache.initFlatReadOnly(Paths.get(args[0]));
        if (!Cache.isFlatReadOnly()) throw new IllegalStateException("Flat read-only store expected");

        List<Object> report = new ArrayList<Object>();
        Map<String, Object> items = scan("items", 19, -1, 8, new Decoder() {
            public void decode(int id, byte[] data, StringBuilder trace) { ItemDefinitions.decodeStrict947(id, data, trace); }
        });
        Map<String, Object> npcs = scan("npcs", 18, -1, 7, new Decoder() {
            public void decode(int id, byte[] data, StringBuilder trace) { NPCDefinitions.decodeStrict947(id, data, trace); }
        });
        Map<String, Object> anims = scan("animations", 20, -1, 7, new Decoder() {
            public void decode(int id, byte[] data, StringBuilder trace) { AnimationDefinitions.decodeStrict947(id, data, trace); }
        });
        Map<String, Object> bas = scan("renderAnims", 2, 32, 0, new Decoder() {
            public void decode(int id, byte[] data, StringBuilder trace) { RenderAnimDefinitions.decodeStrict947(id, data, trace); }
        });
        Map<String, Object> objects = scan("objects", 16, -1, 8, new Decoder() {
            public void decode(int id, byte[] data, StringBuilder trace) {
                // ObjectDefinitions has no raw entry point; the flat loader throws on failure.
                ObjectDefinitions def = ObjectDefinitions.getObjectDefinitions(id);
                if (!def.loaded) throw new IllegalStateException("Object " + id + " not loaded");
            }
        });
        report.add(items); report.add(npcs); report.add(anims); report.add(bas); report.add(objects);

        System.out.println("=== DEFINITION SCAN REPORT ===");
        System.out.println(new GsonBuilder().setPrettyPrinting().create().toJson(report));
        System.out.println("=== SUMMARY ===");
        for (Object family : report) {
            Map<?, ?> f = (Map<?, ?>) family;
            System.out.println(f.get("family") + " index " + f.get("index") + ": ok " + f.get("ok") + "/" + f.get("total")
                    + " failed " + f.get("failed") + " unknownOpcodes " + f.get("unknownOpcodeHistogram")
                    + " other " + f.get("otherFailures") + " " + f.get("elapsedMs") + "ms");
        }

        printAnchors();
        printRawItems(new int[] { 995, 1277, 1733, 51196, 29492 });
        boolean failurePathOk = checkFailurePathThroughTheProductionLoaders();

        int itemsOk = (Integer) items.get("ok"), itemsTotal = (Integer) items.get("total");
        int objectsOk = (Integer) objects.get("ok"), objectsTotal = (Integer) objects.get("total");
        boolean pass = itemsOk >= itemsTotal - 5 && objectsOk >= objectsTotal && failurePathOk;
        System.out.println("GATE items " + itemsOk + "/" + itemsTotal + " objects " + objectsOk + "/" + objectsTotal
                + " failurePath " + (failurePathOk ? "ok" : "BROKEN") + " => " + (pass ? "PASS" : "FAIL"));
        System.exit(pass ? 0 : 1);
    }

    /** Item the strict decoder rejects today (opcode 167 at 60); the loader must serve it as undecodable. */
    private static final int FAILING_ITEM = 29492;
    /** NPC and animation ids the strict decoders reject today; same contract as the item. */
    private static final int FAILING_NPC = 233, FAILING_ANIMATION = 63;

    /**
     * The pillar's guarantee: a definition the strict 947 decoder rejects is never served
     * silently by the PRODUCTION getters (ItemDefinitions.getItemDefinitions & co.). For
     * each anchor the raw strict decode says whether the file decodes; the production
     * loader must agree: on failure it serves the "Undecodable X (id)" shell with
     * loaded=false, the failure reason and a registry entry (the same cached instance);
     * on success it serves a loaded definition with no registry entry. Tied to the raw
     * decode rather than hard-wired to "must fail" so a future decoder fix keeps the gate
     * meaningful instead of breaking it.
     */
    private static boolean checkFailurePathThroughTheProductionLoaders() {
        System.out.println("=== FAILURE PATH (production getters must agree with the strict decoder) ===");
        boolean pass = true;

        Index items = Cache.STORE.getIndexes()[19];
        boolean itemRawFails = rawFails(new Decoder() {
            public void decode(int id, byte[] data, StringBuilder trace) { ItemDefinitions.decodeStrict947(id, data, trace); }
        }, FAILING_ITEM, items.getFile(FAILING_ITEM >>> 8, FAILING_ITEM & 0xff));
        ItemDefinitions item = ItemDefinitions.getItemDefinitions(FAILING_ITEM);
        boolean itemRegistered = ItemDefinitions.getDecodeFailures().containsKey(FAILING_ITEM);
        boolean itemOk = itemRawFails
                ? ("Undecodable Item (" + FAILING_ITEM + ")").equals(item.name) && !item.loaded && item.decodeFailure != null
                        && itemRegistered && item == ItemDefinitions.getItemDefinitions(FAILING_ITEM)
                        && item.decodeFailure.equals(ItemDefinitions.getDecodeFailures().get(FAILING_ITEM))
                : item.loaded && item.decodeFailure == null && !itemRegistered;
        System.out.println("item " + FAILING_ITEM + " rawStrictFails=" + itemRawFails + " name=" + q(item.name) + " loaded=" + item.loaded
                + " decodeFailure=" + q(item.decodeFailure) + " registered=" + itemRegistered + " => " + (itemOk ? "ok" : "MISMATCH"));
        pass &= itemOk;

        Index npcs = Cache.STORE.getIndexes()[18];
        boolean npcRawFails = rawFails(new Decoder() {
            public void decode(int id, byte[] data, StringBuilder trace) { NPCDefinitions.decodeStrict947(id, data, trace); }
        }, FAILING_NPC, npcs.getFile(FAILING_NPC >>> 7, FAILING_NPC & 0x7f));
        NPCDefinitions npc = NPCDefinitions.getNPCDefinitions(FAILING_NPC);
        boolean npcRegistered = NPCDefinitions.getDecodeFailures().containsKey(FAILING_NPC);
        boolean npcOk = npcRawFails
                ? ("Undecodable NPC (" + FAILING_NPC + ")").equals(npc.name) && npc.decodeFailure != null && npcRegistered
                        && npc == NPCDefinitions.getNPCDefinitions(FAILING_NPC)
                : npc.decodeFailure == null && !npcRegistered && !npc.name.startsWith("Undecodable");
        System.out.println("npc " + FAILING_NPC + " rawStrictFails=" + npcRawFails + " name=" + q(npc.name)
                + " decodeFailure=" + q(npc.decodeFailure) + " registered=" + npcRegistered + " => " + (npcOk ? "ok" : "MISMATCH"));
        pass &= npcOk;

        Index anims = Cache.STORE.getIndexes()[20];
        boolean animRawFails = rawFails(new Decoder() {
            public void decode(int id, byte[] data, StringBuilder trace) { AnimationDefinitions.decodeStrict947(id, data, trace); }
        }, FAILING_ANIMATION, anims.getFile(FAILING_ANIMATION >>> 7, FAILING_ANIMATION & 0x7f));
        AnimationDefinitions anim = AnimationDefinitions.getAnimationDefinitions(FAILING_ANIMATION);
        boolean animRegistered = AnimationDefinitions.getDecodeFailures().containsKey(FAILING_ANIMATION);
        boolean animOk = anim != null && (animRawFails
                ? anim.decodeFailure != null && animRegistered && anim == AnimationDefinitions.getAnimationDefinitions(FAILING_ANIMATION)
                : anim.decodeFailure == null && !animRegistered);
        System.out.println("animation " + FAILING_ANIMATION + " rawStrictFails=" + animRawFails + " served=" + (anim != null)
                + " decodeFailure=" + q(anim == null ? null : anim.decodeFailure) + " registered=" + animRegistered
                + " => " + (animOk ? "ok" : "MISMATCH"));
        pass &= animOk;

        int expectedItems = itemRawFails ? 1 : 0, expectedNpcs = npcRawFails ? 1 : 0, expectedAnims = animRawFails ? 1 : 0;
        boolean sizesOk = ItemDefinitions.getDecodeFailures().size() == expectedItems
                && NPCDefinitions.getDecodeFailures().size() == expectedNpcs
                && AnimationDefinitions.getDecodeFailures().size() == expectedAnims;
        System.out.println("registry sizes after anchors: items=" + ItemDefinitions.getDecodeFailures().size() + " (expected " + expectedItems
                + ") npcs=" + NPCDefinitions.getDecodeFailures().size() + " (expected " + expectedNpcs
                + ") animations=" + AnimationDefinitions.getDecodeFailures().size() + " (expected " + expectedAnims
                + ") renderAnims=" + RenderAnimDefinitions.getDecodeFailures().size() + " => " + (sizesOk ? "ok" : "MISMATCH"));
        pass &= sizesOk;
        return pass;
    }

    /** True when the raw strict decode of one file throws (a missing file counts as a failure too). */
    private static boolean rawFails(Decoder decoder, int id, byte[] data) {
        if (data == null) return true;
        try {
            decoder.decode(id, data, new StringBuilder());
            return false;
        } catch (RuntimeException e) {
            return true;
        }
    }

    /**
     * Scans one family. archive &gt;= 0 restricts the scan to that archive with the file
     * id as the definition id (BAS); otherwise id = archive &lt;&lt; shift | file.
     */
    private static Map<String, Object> scan(String family, int indexId, int archive, int shift, Decoder decoder) {
        long start = System.currentTimeMillis();
        Index index = Cache.STORE.getIndexes()[indexId];
        if (index == null || index.getTable() == null) throw new IllegalStateException("Index " + indexId + " missing for " + family);
        ReferenceTable table = index.getTable();
        int[] archives = archive >= 0 ? new int[] { archive } : table.getValidArchiveIds();
        int total = 0, ok = 0, failed = 0, missing = 0;
        TreeMap<Integer, Integer> unknownOpcodes = new TreeMap<Integer, Integer>();
        TreeMap<Integer, Integer> opcodeFileCounts = new TreeMap<Integer, Integer>();
        TreeMap<String, Integer> otherFailures = new TreeMap<String, Integer>();
        List<Object> firstFailures = new ArrayList<Object>();
        StringBuilder trace = new StringBuilder();
        for (int archiveId : archives) {
            if (!index.archiveExists(archiveId)) continue;
            for (int fileId : table.getArchives()[archiveId].getValidFileIds()) {
                int id = archive >= 0 ? fileId : (archiveId << shift) | fileId;
                total++;
                byte[] data = index.getFile(archiveId, fileId);
                if (data == null) { missing++; continue; }
                trace.setLength(0);
                try {
                    decoder.decode(id, data, trace);
                    ok++;
                } catch (RuntimeException e) {
                    failed++;
                    String message = rootMessage(e);
                    Matcher m = OPCODE.matcher(message);
                    if (m.find()) bump(unknownOpcodes, Integer.parseInt(m.group(1)));
                    else bump(otherFailures, message.replaceAll("\\d+", "#"));
                    if (firstFailures.size() < FIRST_FAILURES) {
                        // Raw prefix + the opcodes reached, so the next RE pass starts from evidence.
                        Map<String, Object> entry = new LinkedHashMap<String, Object>();
                        entry.put("id", id); entry.put("bytes", data.length); entry.put("error", message);
                        entry.put("opcodes", trace.toString().trim());
                        entry.put("hexPrefix", hex(Arrays.copyOf(data, Math.min(data.length, 64))));
                        firstFailures.add(entry);
                    }
                }
                countOpcodes(trace, opcodeFileCounts);
            }
        }
        Map<String, Object> out = new LinkedHashMap<String, Object>();
        out.put("family", family);
        out.put("index", archive >= 0 ? indexId + "/" + archive : String.valueOf(indexId));
        out.put("total", total);
        out.put("ok", ok);
        out.put("failed", failed);
        out.put("missing", missing);
        out.put("unknownOpcodeHistogram", unknownOpcodes);
        out.put("otherFailures", otherFailures);
        out.put("firstFailures", firstFailures);
        out.put("opcodeFileCounts", opcodeFileCounts);
        out.put("elapsedMs", System.currentTimeMillis() - start);
        return out;
    }

    /** Counts, per opcode, the number of files whose trace contains it (the ">= 20 samples" evidence). */
    private static void countOpcodes(StringBuilder trace, Map<Integer, Integer> counts) {
        if (trace.length() == 0) return;
        boolean[] seen = new boolean[256];
        for (String token : trace.toString().trim().split(" ")) {
            int at = token.indexOf('@');
            if (at <= 0) continue;
            int opcode = Integer.parseInt(token.substring(0, at));
            if (opcode < 256 && !seen[opcode]) { seen[opcode] = true; bump(counts, opcode); }
        }
    }

    private static <K> void bump(Map<K, Integer> map, K key) {
        Integer current = map.get(key);
        map.put(key, current == null ? 1 : current + 1);
    }

    private static String rootMessage(Throwable t) {
        Throwable cause = t;
        while (cause.getCause() != null && cause.getCause() != cause) cause = cause.getCause();
        String message = cause.getMessage();
        return message == null ? cause.toString() : message;
    }

    /** Values the integrator compares against the Native950CacheContent pins. */
    private static void printAnchors() {
        System.out.println("=== ANCHORS (items via ItemDefinitions.getItemDefinitions on the flat path) ===");
        for (int id : new int[] { 995, 1511, 315, 1277, 1173, 1139, 4151, 11694 }) {
            ItemDefinitions def = ItemDefinitions.getItemDefinitions(id);
            System.out.println("item " + id + " name=" + q(def.name) + " options=" + Arrays.toString(def.inventoryOptions)
                    + " equipSlot=" + def.equipSlot + " equipType=" + def.equipType + " stackable=" + def.stackable
                    + " value=" + def.value + " value64=" + def.value64 + " flag178=" + def.flag178
                    + " unknownInt69=" + def.unknownInt69 + " description=" + q(def.description)
                    + " loaded=" + def.loaded + " decodeFailure=" + q(def.decodeFailure));
        }
        System.out.println("=== ANCHORS (npcs via NPCDefinitions.getNPCDefinitions on the flat path) ===");
        for (int id : new int[] { 494, 0, 1 }) {
            NPCDefinitions def = NPCDefinitions.getNPCDefinitions(id);
            System.out.println("npc " + id + " name=" + q(def.name) + " options=" + Arrays.toString(def.menuOptions)
                    + " combatLevel=" + def.combatLevel + " size=" + def.size + " renderEmote=" + def.renderEmote
                    + " decodeFailure=" + q(def.decodeFailure));
        }
        System.out.println("registry sizes: items=" + ItemDefinitions.getDecodeFailures().size()
                + " npcs=" + NPCDefinitions.getDecodeFailures().size()
                + " animations=" + AnimationDefinitions.getDecodeFailures().size()
                + " renderAnims=" + RenderAnimDefinitions.getDecodeFailures().size());
    }

    /** Raw hex plus the opcode@offset sequence so opcode-width hypotheses can be checked by eye. */
    private static void printRawItems(int[] ids) {
        System.out.println("=== RAW ITEM FILES (hex, then opcode@offset sequence of the strict decode) ===");
        Index index = Cache.STORE.getIndexes()[19];
        for (int id : ids) {
            byte[] data = index.getFile(id >>> 8, id & 0xff);
            if (data == null) { System.out.println("item " + id + " missing"); continue; }
            StringBuilder trace = new StringBuilder();
            String outcome;
            try {
                ItemDefinitions def = ItemDefinitions.decodeStrict947(id, data, trace);
                outcome = "ok name=" + q(def.name) + " value64=" + def.value64 + " unknownInt69=" + def.unknownInt69 + " description=" + q(def.description);
            } catch (RuntimeException e) {
                outcome = "FAILED " + rootMessage(e);
            }
            System.out.println("item " + id + " bytes=" + data.length + " hex=" + hex(data));
            System.out.println("item " + id + " opcodes=" + trace.toString().trim() + " => " + outcome);
        }
    }

    private static String hex(byte[] data) {
        StringBuilder out = new StringBuilder(data.length * 2);
        for (byte b : data) out.append(String.format("%02x", b & 0xff));
        return out.toString();
    }

    private static String q(String s) {
        return s == null ? "null" : "\"" + s + "\"";
    }
}
