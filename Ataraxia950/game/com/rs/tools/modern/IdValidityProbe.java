package com.rs.tools.modern;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.rs.cache.Cache;
import com.rs.cache.filestore.store.Index;
import com.rs.cache.filestore.store.ReferenceTable;
import com.rs.cache.filestore.store.Store;
import com.rs.cache.loaders.ItemDefinitions;
import com.rs.cache.loaders.NPCDefinitions;
import com.rs.cache.loaders.ObjectDefinitions;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Builds the 910 -&gt; 950 ID validity table the backlog's P3 specified and nobody built
 * (MIGRATION-BACKLOG.md row "Committed 910-&gt;950 ID validity table"), and which M4 needs
 * before a single NPC may be spawned from {@code data/npcs/spawns.json}.
 *
 * <p>Ataraxia's JSON data is keyed by <b>910</b> definition ids. The 950 cache reuses those
 * numbers for different content, so an id can load cleanly and still be the wrong thing:
 * NPC 42 is a Sheep in the 910 cache and "Bill" in the 950 one; items 1079 and 1333 keep
 * the names "Rune platelegs" and "Rune scimitar" but lose their equipment slot. Name
 * equality alone is therefore not enough - the fields that decide whether the id is still
 * usable are compared as well.
 *
 * <p><b>Two caches, three runs.</b> {@code Cache.STORE} is a single static store and the
 * definition loaders size their tables from whichever cache initialised them, so the two
 * sides are dumped by separate JVMs and joined by a third, cache-free run:
 *
 * <pre>
 *   IdValidityProbe dump-legacy &lt;910 cache dir with trailing separator&gt; &lt;data root&gt; &lt;out.json&gt;
 *   IdValidityProbe dump-modern &lt;950 flat cache dir&gt;                    &lt;data root&gt; &lt;out.json&gt;
 *   IdValidityProbe classify &lt;legacy.json&gt; &lt;modern.json&gt; &lt;data root&gt; &lt;table.json&gt; &lt;report.md&gt;
 * </pre>
 *
 * Neither cache is written: the legacy store is only read through {@code Index.getFile},
 * and the modern one is opened read-only.
 *
 * <p><b>Verdicts.</b> Exactly two of them are safe, and the second one is narrow.
 * <ul>
 * <li>{@code same} - the 950 definition exists and every compared field agrees. Safe.</li>
 * <li>{@code restyled} - every compared field agrees <i>and</i> the two names are equal
 *     ignoring case, but not equal exactly: "Hill Giant" -&gt; "Hill giant", "Rock Crab"
 *     -&gt; "Rock crab". Jagex moved NPC names to sentence case between the two caches, and
 *     a letter's case cannot change what a definition denotes while its size, its equipment
 *     slot and every dispatched option slot stay identical. Safe, by owner decision on
 *     2026-09-07. This is the <b>only</b> loosening: one character class, one comparison,
 *     and every field check {@code same} runs is run here too and can still send the id to
 *     {@code repurposed}.</li>
 * <li>{@code renamed} - the 950 definition exists and looks like the same thing under a
 *     genuinely different string (advisory split, see below). Refused.</li>
 * <li>{@code repurposed} - the 950 definition exists and names something else, or keeps
 *     the name but changed a field that matters (equipment slot, type). Refused.</li>
 * <li>{@code missing} - no 950 definition, or one the strict decoder rejected. Refused.</li>
 * <li>{@code unverifiable} - no 910 definition to compare against, so nothing can be
 *     concluded. Refused.</li>
 * </ul>
 * The renamed/repurposed split is a readability aid for the human report and is computed
 * from a string-similarity rule; safety never depends on it, because both are refused.
 * {@code restyled} is not part of that split: it is decided by
 * {@link String#equalsIgnoreCase}, which is exact, not by the similarity heuristic.
 */
public final class IdValidityProbe {

    /**
     * Table format version; {@code Native950IdValidity} refuses anything else. Bumped to 2
     * when the {@code restyled} bucket was added, so a table generated before it cannot be
     * loaded by a server that would trust that bucket, nor the reverse.
     */
    public static final int FORMAT = 2;
    public static final int REVISION = 950;

    public static final String NPC = "npc", ITEM = "item", OBJECT = "object";
    public static final String SAME = "same", RESTYLED = "restyled", RENAMED = "renamed",
            REPURPOSED = "repurposed", MISSING = "missing", UNVERIFIABLE = "unverifiable";

    /** Both caches carry five option slots per definition, and both dumps record all five. */
    public static final int OPTION_SLOTS = 5;
    /**
     * The NPC option slots a click can actually be dispatched through.
     * {@code Native950ActionRouter.legacyNpcOption} forwards native options 1..4 onto the
     * 910 {@code NPCHandler} option of the same number and refuses native option 5, so a
     * slot-5 difference cannot reach a 910 branch at all. Slot 5 is therefore compared by
     * nobody: NPC 494 Banker is exactly that case (910 {@code Bank||Talk-to|Collect|} and
     * 950 {@code Bank||Talk to|Collect|Load Last Preset from}), and it is the anchor every
     * smoke spawns.
     */
    public static final int FORWARDED_NPC_OPTION_SLOTS = 4;

    private static final Charset UTF8 = Charset.forName("UTF-8");

    public static void main(String[] args) throws Exception {
        if (args.length == 0) throw new IllegalArgumentException(usage());
        String command = args[0];
        if (command.equals("dump-legacy")) {
            require(args.length == 4, usage());
            ReferenceTable.NEW_PROTOCOL = true;
            Cache.STORE = new Store(withSeparator(args[1]));
            dump("legacy", args[1], Paths.get(args[2]), Paths.get(args[3]));
        } else if (command.equals("dump-modern")) {
            require(args.length == 4, usage());
            Cache.initFlatReadOnly(Paths.get(args[1]));
            require(Cache.isFlatReadOnly(), "The 950 side must be the flat read-only store");
            dump("modern", args[1], Paths.get(args[2]), Paths.get(args[3]));
        } else if (command.equals("classify")) {
            require(args.length == 6, usage());
            classify(Paths.get(args[1]), Paths.get(args[2]), Paths.get(args[3]),
                    Paths.get(args[4]), Paths.get(args[5]));
        } else {
            throw new IllegalArgumentException(usage());
        }
    }

    private static String usage() {
        return "Usage:\n"
                + "  IdValidityProbe dump-legacy <910 cache dir> <ataraxia data root> <out.json>\n"
                + "  IdValidityProbe dump-modern <950 flat cache dir> <ataraxia data root> <out.json>\n"
                + "  IdValidityProbe classify <legacy.json> <modern.json> <data root> <table.json> <report.md>";
    }

    // ---------------------------------------------------------------- dump

    /**
     * Writes one side of the comparison. NPCs and items are dumped whole, because the
     * headline "how much of the index still means the same thing" figure needs the whole
     * index; objects are dumped only for the ids Ataraxia's data actually references,
     * because nothing else reads an object id through this table yet.
     */
    private static void dump(String side, String cachePath, Path dataRoot, Path out) throws Exception {
        long start = System.currentTimeMillis();
        JsonObject root = new JsonObject();
        root.addProperty("side", side);
        root.addProperty("cache", cachePath);
        root.addProperty("format", FORMAT);

        JsonObject kinds = new JsonObject();
        kinds.add(NPC, dumpNpcs());
        kinds.add(ITEM, dumpItems());
        kinds.add(OBJECT, dumpObjects(referenced(dataRoot).get(OBJECT)));
        root.add("kinds", kinds);

        write(out, new GsonBuilder().create().toJson(root));
        System.out.println("[IdValidityProbe] " + side + " dump written to " + out.toAbsolutePath()
                + " in " + (System.currentTimeMillis() - start) + "ms");
        for (Map.Entry<String, JsonElement> entry : kinds.entrySet())
            System.out.println("  " + entry.getKey() + ": "
                    + entry.getValue().getAsJsonObject().getAsJsonObject("entries").size() + " definitions");
    }

    private static JsonObject dumpNpcs() {
        JsonObject entries = new JsonObject();
        for (int id : idsInIndex(18, 7)) {
            NPCDefinitions def = NPCDefinitions.getNPCDefinitions(id);
            if (def == null || def.decodeFailure != null || !usableName(def.name, id, "NPC")) continue;
            JsonObject o = new JsonObject();
            o.addProperty("n", def.name);
            o.addProperty("sz", def.size);
            o.addProperty("mv", def.movementCapabilities & 0xff);
            o.addProperty("lv", def.combatLevel);
            o.addProperty("op", options(def.menuOptions));
            entries.add(Integer.toString(id), o);
        }
        return kind(entries);
    }

    private static JsonObject dumpItems() {
        JsonObject entries = new JsonObject();
        for (int id : idsInIndex(19, 8)) {
            ItemDefinitions def = ItemDefinitions.getItemDefinitions(id);
            if (def == null || def.decodeFailure != null || !usableName(def.name, id, "Item")) continue;
            JsonObject o = new JsonObject();
            o.addProperty("n", def.name);
            o.addProperty("es", def.equipSlot);
            o.addProperty("et", def.equipType);
            o.addProperty("io", options(def.inventoryOptions));
            o.addProperty("go", options(def.groundOptions));
            entries.add(Integer.toString(id), o);
        }
        return kind(entries);
    }

    private static JsonObject dumpObjects(Set<Integer> ids) {
        JsonObject entries = new JsonObject();
        for (int id : ids) {
            if (id < 0) continue;
            ObjectDefinitions def;
            try {
                def = ObjectDefinitions.getObjectDefinitions(id);
            } catch (RuntimeException failure) {
                continue;
            }
            if (def == null || !usableName(def.name, id, "Object")) continue;
            JsonObject o = new JsonObject();
            o.addProperty("n", def.name);
            o.addProperty("op", options(def.options));
            entries.add(Integer.toString(id), o);
        }
        return kind(entries);
    }

    private static JsonObject kind(JsonObject entries) {
        JsonObject o = new JsonObject();
        o.addProperty("count", entries.size());
        o.add("entries", entries);
        return o;
    }

    /**
     * The definition ids of one cache index, as {@code archive &lt;&lt; shift | file}. Only
     * files the reference table declares are visited, so a gap is a gap and not a
     * decode failure.
     */
    private static List<Integer> idsInIndex(int indexId, int shift) {
        Index index = Cache.STORE.getIndexes()[indexId];
        if (index == null || index.getTable() == null)
            throw new IllegalStateException("Cache index " + indexId + " is missing");
        List<Integer> ids = new ArrayList<Integer>();
        for (int archiveId : index.getTable().getValidArchiveIds()) {
            if (!index.archiveExists(archiveId)) continue;
            for (int fileId : index.getTable().getArchives()[archiveId].getValidFileIds())
                ids.add(Integer.valueOf((archiveId << shift) | fileId));
        }
        Collections.sort(ids);
        return ids;
    }

    /**
     * Rejects the loaders' placeholder names. A definition served as "Missing NPC (id)",
     * "Undecodable Item (id)", "Error NPC (id)" or the literal "null" carries no evidence
     * about the id and must never look like a decoded definition to the classifier.
     */
    private static boolean usableName(String name, int id, String family) {
        if (name == null) return false;
        String trimmed = name.trim();
        if (trimmed.length() == 0 || trimmed.equals("null")) return false;
        return !trimmed.equals("Missing " + family + " (" + id + ")")
                && !trimmed.equals("Undecodable " + family + " (" + id + ")")
                && !trimmed.equals("Error " + family + " (" + id + ")")
                && !trimmed.equals("Unknown NPC");
    }

    private static String options(String[] values) {
        if (values == null) return "";
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            if (i > 0) text.append('|');
            if (values[i] != null) text.append(values[i]);
        }
        return text.toString();
    }

    // ---------------------------------------------------------------- referenced ids

    /** Every data file that keys rows by a 910 definition id, and which kind each key is. */
    private static final String[][] SOURCES = {
        { "npcs/spawns.json", NPC, "npcId" },
        { "npcs/combatDefs.json", NPC, "npcId" },
        { "npcs/npcstats.json", NPC, "npcId" },
        { "npcs/bonuses.json", NPC, "npcId" },
        { "npcs/weaknesses.json", NPC, "npcId" },
        { "npcs/drops.json", NPC, "npcId" },
        { "npcs/drops.json", ITEM, "itemId" },
        { "items/itemBonuses.json", ITEM, "itemId" },
        { "items/itemsDisassembleData.json", ITEM, "itemId" },
        { "items/shops.json", ITEM, "$shopItems" },
        { "map/customObjectSpawns.json", OBJECT, "objectId" },
        { "map/customObjectSpawns2.json", OBJECT, "objectId" },
    };

    /** The id sets Ataraxia's own data actually uses, per kind. */
    private static Map<String, Set<Integer>> referenced(Path dataRoot) throws Exception {
        Map<String, Set<Integer>> found = new LinkedHashMap<String, Set<Integer>>();
        found.put(NPC, new TreeSet<Integer>());
        found.put(ITEM, new TreeSet<Integer>());
        found.put(OBJECT, new TreeSet<Integer>());
        for (Map.Entry<String, Set<Integer>> entry : perSource(dataRoot).entrySet()) {
            String kindName = entry.getKey().substring(entry.getKey().lastIndexOf(' ') + 1);
            found.get(kindName).addAll(entry.getValue());
        }
        return found;
    }

    /**
     * The id set of each data file separately, keyed "&lt;relative path&gt; &lt;kind&gt;". The
     * milestone's headline question ("how many of spawns.json's ids are usable") is a
     * per-file question, so the classifier reports per file as well as per kind.
     */
    private static Map<String, Set<Integer>> perSource(Path dataRoot) throws Exception {
        Map<String, Set<Integer>> found = new LinkedHashMap<String, Set<Integer>>();
        for (String[] source : SOURCES) {
            Path file = dataRoot.resolve(source[0]);
            String key = source[0] + " " + source[1];
            if (!Files.exists(file)) {
                System.out.println("[IdValidityProbe] data file absent, skipped: " + file);
                found.put(key, Collections.<Integer>emptySet());
                continue;
            }
            Set<Integer> ids = new TreeSet<Integer>();
            collect(parse(file), source[2], ids);
            found.put(key, ids);
        }
        return found;
    }

    /**
     * Collects every value of {@code key} anywhere in the tree. {@code $shopItems} is the
     * one shape that needs naming, because a shop row's item id is a bare "id" inside its
     * "items" array and "id" alone would also pick up shop ids.
     */
    private static void collect(JsonElement element, String key, Set<Integer> into) {
        if (element == null || element.isJsonNull()) return;
        if (element.isJsonArray()) {
            for (JsonElement child : element.getAsJsonArray()) collect(child, key, into);
            return;
        }
        if (!element.isJsonObject()) return;
        JsonObject object = element.getAsJsonObject();
        if (key.equals("$shopItems")) {
            if (object.has("items") && object.get("items").isJsonArray())
                for (JsonElement row : object.getAsJsonArray("items"))
                    if (row.isJsonObject() && row.getAsJsonObject().has("id"))
                        into.add(Integer.valueOf(row.getAsJsonObject().get("id").getAsInt()));
        } else if (object.has(key) && object.get(key).isJsonPrimitive()) {
            into.add(Integer.valueOf(object.get(key).getAsInt()));
        }
        for (Map.Entry<String, JsonElement> entry : object.entrySet()) collect(entry.getValue(), key, into);
    }

    // ---------------------------------------------------------------- classify

    private static void classify(Path legacyDump, Path modernDump, Path dataRoot, Path table, Path report)
            throws Exception {
        JsonObject legacy = parse(legacyDump).getAsJsonObject();
        JsonObject modern = parse(modernDump).getAsJsonObject();
        require("legacy".equals(legacy.get("side").getAsString()), "First dump must be the legacy side");
        require("modern".equals(modern.get("side").getAsString()), "Second dump must be the modern side");
        Map<String, Set<Integer>> referenced = referenced(dataRoot);

        JsonObject out = new JsonObject();
        out.addProperty("_note", "910 -> 950 id validity table. Generated by com.rs.tools.modern.IdValidityProbe"
                + " from the 910 cache Ataraxia's JSON data was authored against and the 950 cache the native"
                + " client serves. Only 'same' and 'restyled' are safe to act on; renamed, repurposed, missing"
                + " and unverifiable are all refused. 'restyled' means the names differ only in letter case"
                + " while every compared field agrees, which is the modern cache use of sentence case; it is decided by"
                + " an exact case-insensitive comparison, not by the advisory renamed/repurposed similarity"
                + " rule, and no safety decision depends on that rule.");
        out.addProperty("format", FORMAT);
        out.addProperty("revision", REVISION);
        out.addProperty("legacyCache", legacy.get("cache").getAsString());
        out.addProperty("modernCache", modern.get("cache").getAsString());
        out.add("safeVerdicts", array(SAME, RESTYLED));

        JsonObject kinds = new JsonObject();
        Map<String, Map<Integer, String>> verdictsByKind = new LinkedHashMap<String, Map<Integer, String>>();
        StringBuilder text = new StringBuilder();
        text.append("# 910 -> 950 ID validity report\n\n");
        text.append("Generated by `com.rs.tools.modern.IdValidityProbe classify`.\n\n");
        text.append("* legacy cache: `").append(legacy.get("cache").getAsString()).append("`\n");
        text.append("* modern cache: `").append(modern.get("cache").getAsString()).append("`\n\n");
        text.append("`same` and `restyled` are safe. `restyled` is a case-only name change with every\n");
        text.append("compared field still equal - the modern cache use of sentence case, e.g. \"Hill Giant\" ->\n");
        text.append("\"Hill giant\". `renamed` and `repurposed` are both refused; the split between them is a\n");
        text.append("readability aid computed from a string-similarity rule, not a safety input.\n\n");

        for (String kindName : new String[] { NPC, ITEM, OBJECT }) {
            JsonObject legacyEntries = legacy.getAsJsonObject("kinds").getAsJsonObject(kindName).getAsJsonObject("entries");
            JsonObject modernEntries = modern.getAsJsonObject("kinds").getAsJsonObject(kindName).getAsJsonObject("entries");
            Set<Integer> reference = referenced.get(kindName);
            // NPCs carry the whole index so isSafe can answer for any id a tool spawns;
            // items and objects carry only the ids Ataraxia's own data references.
            boolean wholeIndex = kindName.equals(NPC);
            Map<Integer, String> verdicts = new TreeMap<Integer, String>();
            kinds.add(kindName, classifyKind(kindName, legacyEntries, modernEntries, reference, wholeIndex, text, verdicts));
            verdictsByKind.put(kindName, verdicts);
        }
        out.add("kinds", kinds);
        out.add("sources", sourceList());
        out.add("bySource", bySource(perSource(dataRoot), verdictsByKind, text));

        write(table, new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create().toJson(out));
        write(report, text.toString());
        System.out.println("[IdValidityProbe] table  -> " + table.toAbsolutePath());
        System.out.println("[IdValidityProbe] report -> " + report.toAbsolutePath());
    }

    /**
     * One line per data file: how many distinct ids it uses and how they classify. This is
     * the table the milestone report quotes, because "how many of spawns.json's 2,394 ids
     * are usable" is a per-file question and the kind-level totals mix six npc files.
     */
    private static JsonObject bySource(Map<String, Set<Integer>> perSource,
                                       Map<String, Map<Integer, String>> verdictsByKind, StringBuilder text) {
        JsonObject out = new JsonObject();
        text.append("## by data file\n\n");
        text.append("| data file | kind | ids | same | restyled | renamed | repurposed | missing"
                + " | unverifiable |\n");
        text.append("|---|---|---|---|---|---|---|---|---|\n");
        for (Map.Entry<String, Set<Integer>> entry : perSource.entrySet()) {
            int space = entry.getKey().lastIndexOf(' ');
            String file = entry.getKey().substring(0, space), kindName = entry.getKey().substring(space + 1);
            Map<Integer, String> verdicts = verdictsByKind.get(kindName);
            Map<String, List<Integer>> buckets = freshBuckets();
            for (Integer id : entry.getValue()) {
                String verdict = verdicts.get(id);
                buckets.get(verdict == null ? UNVERIFIABLE : verdict).add(id);
            }
            JsonObject counted = counts(buckets, -1, -1);
            counted.addProperty("kind", kindName);
            out.add(file + " [" + kindName + "]", counted);
            text.append("| `").append(file).append("` | ").append(kindName);
            text.append(" | ").append(entry.getValue().size())
                .append(" | ").append(buckets.get(SAME).size())
                .append(" | ").append(buckets.get(RESTYLED).size())
                .append(" | ").append(buckets.get(RENAMED).size())
                .append(" | ").append(buckets.get(REPURPOSED).size())
                .append(" | ").append(buckets.get(MISSING).size())
                .append(" | ").append(buckets.get(UNVERIFIABLE).size()).append(" |\n");
        }
        text.append('\n');
        return out;
    }

    private static JsonObject classifyKind(String kindName, JsonObject legacyEntries, JsonObject modernEntries,
                                           Set<Integer> reference, boolean wholeIndex, StringBuilder text,
                                           Map<Integer, String> verdictsOut) {
        Set<Integer> universe = new TreeSet<Integer>();
        for (String key : legacyEntries.keySet()) universe.add(Integer.valueOf(key));
        for (String key : modernEntries.keySet()) universe.add(Integer.valueOf(key));
        universe.addAll(reference);

        Map<String, List<Integer>> scanned = freshBuckets();
        Map<String, List<Integer>> refBuckets = freshBuckets();
        TreeMap<Integer, String> reasons = new TreeMap<Integer, String>();

        for (Integer id : universe) {
            JsonObject before = legacyEntries.has(id.toString()) ? legacyEntries.getAsJsonObject(id.toString()) : null;
            JsonObject after = modernEntries.has(id.toString()) ? modernEntries.getAsJsonObject(id.toString()) : null;
            String[] verdictAndReason = verdict(kindName, before, after);
            String verdict = verdictAndReason[0];
            verdictsOut.put(id, verdict);
            scanned.get(verdict).add(id);
            if (reference.contains(id)) refBuckets.get(verdict).add(id);
            if (!verdict.equals(SAME) && (wholeIndex || reference.contains(id)))
                reasons.put(id, verdictAndReason[1]);
        }

        JsonObject out = new JsonObject();
        out.add("scanned", counts(scanned, legacyEntries.size(), modernEntries.size()));
        out.add("referenced", counts(refBuckets, -1, -1));
        out.addProperty("coverage", wholeIndex ? "whole-index" : "referenced-ids-only");

        // Rows: whole index for NPCs, referenced ids only for items and objects.
        Map<String, List<Integer>> published = wholeIndex ? scanned : refBuckets;
        out.addProperty("same", ranges(published.get(SAME)));
        out.addProperty("missing", ranges(published.get(MISSING)));
        out.addProperty("unverifiable", ranges(published.get(UNVERIFIABLE)));
        JsonObject restyled = new JsonObject(), renamed = new JsonObject(), repurposed = new JsonObject();
        for (Integer id : published.get(RESTYLED)) restyled.addProperty(id.toString(), reasons.get(id));
        for (Integer id : published.get(RENAMED)) renamed.addProperty(id.toString(), reasons.get(id));
        for (Integer id : published.get(REPURPOSED)) repurposed.addProperty(id.toString(), reasons.get(id));
        // 'restyled' is published as an id -> reason map like the refused buckets, not as ranges:
        // it is small, and every safe id it carries should be able to name what it was called.
        out.add("restyled", restyled);
        out.add("renamed", renamed);
        out.add("repurposed", repurposed);

        text.append("## ").append(kindName).append("\n\n");
        text.append("| scope | total | same | restyled | renamed | repurposed | missing"
                + " | unverifiable |\n");
        text.append("|---|---|---|---|---|---|---|---|\n");
        row(text, "whole index", scanned);
        row(text, "referenced by Ataraxia data", refBuckets);
        text.append("\nLegacy definitions decoded: ").append(legacyEntries.size())
            .append("; 950 definitions decoded: ").append(modernEntries.size()).append(".\n\n");
        List<Integer> restyledIds = refBuckets.get(RESTYLED);
        if (!restyledIds.isEmpty()) {
            text.append("Case-only renames referenced by the data, admitted as `restyled` (first 40 by id):\n\n");
            text.append("| id | 910 -> 950 |\n|---|---|\n");
            int listed = 0;
            for (Integer id : restyledIds) {
                if (listed++ >= 40) break;
                text.append("| ").append(id).append(" | ").append(reasons.get(id)).append(" |\n");
            }
            text.append('\n');
        }
        List<Integer> notable = refBuckets.get(REPURPOSED);
        if (!notable.isEmpty()) {
            text.append("Most notable repurposed ids referenced by the data (first 40 by id):\n\n");
            text.append("| id | 910 -> 950 |\n|---|---|\n");
            int shown = 0;
            for (Integer id : notable) {
                if (shown++ >= 40) break;
                text.append("| ").append(id).append(" | ").append(reasons.get(id)).append(" |\n");
            }
            text.append('\n');
        }
        return out;
    }

    private static void row(StringBuilder text, String label, Map<String, List<Integer>> buckets) {
        int total = 0;
        for (List<Integer> bucket : buckets.values()) total += bucket.size();
        text.append("| ").append(label).append(" | ").append(total)
            .append(" | ").append(buckets.get(SAME).size())
            .append(" | ").append(buckets.get(RESTYLED).size())
            .append(" | ").append(buckets.get(RENAMED).size())
            .append(" | ").append(buckets.get(REPURPOSED).size())
            .append(" | ").append(buckets.get(MISSING).size())
            .append(" | ").append(buckets.get(UNVERIFIABLE).size()).append(" |\n");
    }

    private static Map<String, List<Integer>> freshBuckets() {
        Map<String, List<Integer>> buckets = new LinkedHashMap<String, List<Integer>>();
        for (String verdict : new String[] { SAME, RESTYLED, RENAMED, REPURPOSED, MISSING, UNVERIFIABLE })
            buckets.put(verdict, new ArrayList<Integer>());
        return buckets;
    }

    private static JsonObject counts(Map<String, List<Integer>> buckets, int legacyDefs, int modernDefs) {
        JsonObject o = new JsonObject();
        int total = 0;
        for (List<Integer> bucket : buckets.values()) total += bucket.size();
        o.addProperty("total", total);
        for (Map.Entry<String, List<Integer>> entry : buckets.entrySet())
            o.addProperty(entry.getKey(), entry.getValue().size());
        if (legacyDefs >= 0) o.addProperty("legacyDefinitions", legacyDefs);
        if (modernDefs >= 0) o.addProperty("modernDefinitions", modernDefs);
        return o;
    }

    /**
     * The verdict for one id, plus the human reason. The compared fields are the ones that
     * decide whether the id still denotes a usable thing:
     * <ul>
     * <li>every kind: the name, exactly - or, for {@link #RESTYLED}, ignoring case only. The
     *     field checks below run identically in both cases, so admitting a case change never
     *     skips a check;</li>
     * <li>items: {@code equipSlot} and {@code equipType} as well, because 1079 Rune
     *     platelegs and 1333 Rune scimitar keep their names in 950 and lose their slot,
     *     and both option arrays slot for slot, because an option index is what a click
     *     is dispatched by (item 6 "Cannon base" loses "Set-up", item 35 "Excalibur"
     *     swaps slots 1 and 2, item 995 "Coins" replaces both of its first two);</li>
     * <li>NPCs: the size as well, because a footprint change moves the entity's collision
     *     and the addition record's size field, plus the four FORWARDED menu option slots,
     *     because {@code Native950ActionRouter.legacyNpcOption} maps native option n onto
     *     910 {@code NPCHandler} option n: NPC 2333 "Vasquen" keeps its name and swaps
     *     "Talk-to" and "Pay", so a click on "Pay" would have run the 910 "Talk-to"
     *     branch. Slot 5 is not compared - see {@link #FORWARDED_NPC_OPTION_SLOTS}.</li>
     * </ul>
     *
     * <p>Object options are deliberately NOT compared: the object ids this table carries are
     * the ones {@code map/customObjectSpawns*.json} names, no consumer reads an object
     * verdict yet, and the objects the client actually renders come from the 950 map itself,
     * which is self-consistent.
     */
    // Public so the tests can drive the comparison directly with two hand-written
    // definitions; nothing at runtime calls it from outside this class.
    public static String[] verdict(String kindName, JsonObject before, JsonObject after) {
        if (after == null)
            return new String[] { MISSING, before == null ? "no definition in either cache"
                    : "950 has no definition; 910 had " + q(text(before, "n")) };
        if (before == null)
            return new String[] { UNVERIFIABLE, "no 910 definition to compare against; 950 has "
                    + q(text(after, "n")) };
        String legacyName = text(before, "n"), modernName = text(after, "n");
        boolean sameName = legacyName.equals(modernName);
        // A name that differs only in letter case is the modern cache use of sentence case, and case
        // alone cannot change what a definition denotes. It is admitted - but only after every
        // field check below has run, exactly as for an identical name, so a restyled id whose
        // size or option array also moved still lands in 'repurposed'.
        boolean caseOnly = !sameName && legacyName.equalsIgnoreCase(modernName)
                && legacyName.length() > 0;
        boolean comparable = sameName || caseOnly;
        String kept = sameName ? q(legacyName) + " kept its name but "
                : q(legacyName) + " -> " + q(modernName) + " differs only in case but ";
        if (comparable && kindName.equals(ITEM)) {
            int es0 = number(before, "es"), es1 = number(after, "es");
            int et0 = number(before, "et"), et1 = number(after, "et");
            if (es0 != es1)
                return new String[] { REPURPOSED, kept + "equipSlot changed " + es0 + " -> " + es1 };
            if (et0 != et1)
                return new String[] { REPURPOSED, kept + "equipType changed " + et0 + " -> " + et1 };
            String drift = optionDrift(before, after, "io", OPTION_SLOTS, "inventory option");
            if (drift == null) drift = optionDrift(before, after, "go", OPTION_SLOTS, "ground option");
            if (drift != null)
                return new String[] { REPURPOSED, kept + drift };
        }
        if (comparable && kindName.equals(NPC)) {
            int sz0 = number(before, "sz"), sz1 = number(after, "sz");
            if (sz0 != sz1)
                return new String[] { REPURPOSED, kept + "size changed " + sz0 + " -> " + sz1 };
            String drift = optionDrift(before, after, "op", FORWARDED_NPC_OPTION_SLOTS, "menu option");
            if (drift != null)
                return new String[] { REPURPOSED, kept + drift };
        }
        if (sameName) return new String[] { SAME, "" };
        if (caseOnly)
            return new String[] { RESTYLED, q(legacyName) + " -> " + q(modernName) + " (case only)" };
        String reason = q(legacyName) + " -> " + q(modernName);
        return new String[] { similar(legacyName, modernName) ? RENAMED : REPURPOSED, reason };
    }

    /**
     * The first option slot whose meaning moved between the two caches, as a human reason, or
     * null when every compared slot still says the same thing. Comparison is position-wise and
     * under {@link #normalise}, so "Talk-to" and "Talk to" are the same option and an emptied
     * or newly filled slot is a difference: both directions matter, because the client renders
     * the menu from the 950 array while the handler is dispatched from the 910 one.
     *
     * @param slots how many leading slots are compared; the rest are ignored
     */
    private static String optionDrift(JsonObject before, JsonObject after, String key,
                                      int slots, String label) {
        String[] was = optionSlots(before, key), now = optionSlots(after, key);
        for (int slot = 0; slot < slots && slot < OPTION_SLOTS; slot++) {
            if (normalise(was[slot]).equals(normalise(now[slot]))) continue;
            return label + " " + (slot + 1) + " changed " + q(was[slot]) + " -> " + q(now[slot]);
        }
        return null;
    }

    /** One dumped definition's option array, split back into exactly {@link #OPTION_SLOTS}. */
    static String[] optionSlots(JsonObject definition, String key) {
        String[] parts = text(definition, key).split("\\|", -1);
        String[] slots = new String[OPTION_SLOTS];
        for (int slot = 0; slot < OPTION_SLOTS; slot++)
            slots[slot] = slot < parts.length ? parts[slot] : "";
        return slots;
    }

    /**
     * Advisory only: decides whether two different names read as the same thing restyled.
     * One normalised name containing the other, or a token overlap of at least half, counts
     * as a rename. Both verdicts are refused by the loader, so a wrong answer here costs
     * readability and nothing else.
     */
    static boolean similar(String a, String b) {
        String na = normalise(a), nb = normalise(b);
        if (na.length() == 0 || nb.length() == 0) return false;
        if (na.equals(nb)) return true;
        if (na.contains(nb) || nb.contains(na)) return true;
        // "Rockslug" vs "Rock slug": only the word break moved.
        if (na.replace(" ", "").equals(nb.replace(" ", ""))) return true;
        Set<String> ta = tokens(na), tb = tokens(nb);
        if (ta.isEmpty() || tb.isEmpty()) return false;
        int shared = 0;
        for (String token : ta) if (tb.contains(token)) shared++;
        int union = ta.size() + tb.size() - shared;
        return union > 0 && shared * 2 >= union;
    }

    private static String normalise(String value) {
        StringBuilder out = new StringBuilder();
        String lower = value.toLowerCase();
        boolean space = false;
        for (int i = 0; i < lower.length(); i++) {
            char c = lower.charAt(i);
            if ((c >= 'a' && c <= 'z') || (c >= '0' && c <= '9')) { out.append(c); space = false; }
            else if (!space && out.length() > 0) { out.append(' '); space = true; }
        }
        return out.toString().trim();
    }

    private static Set<String> tokens(String normalised) {
        Set<String> out = new LinkedHashSet<String>();
        for (String token : normalised.split(" ")) if (token.length() > 0) out.add(token);
        return out;
    }

    // ---------------------------------------------------------------- shared helpers

    /**
     * Renders a sorted id list as inclusive ranges, "0-40,42,44-99". The whole-index NPC
     * "same" set is over twenty thousand ids and a bare array would dominate the checked-in
     * table; {@code Native950IdValidity} parses exactly this form back.
     */
    static String ranges(List<Integer> ids) {
        if (ids.isEmpty()) return "";
        List<Integer> sorted = new ArrayList<Integer>(ids);
        Collections.sort(sorted);
        StringBuilder out = new StringBuilder();
        int from = sorted.get(0).intValue(), previous = from;
        for (int i = 1; i <= sorted.size(); i++) {
            int current = i < sorted.size() ? sorted.get(i).intValue() : Integer.MIN_VALUE;
            if (i < sorted.size() && current == previous + 1) { previous = current; continue; }
            if (out.length() > 0) out.append(',');
            out.append(from);
            if (previous != from) out.append('-').append(previous);
            from = previous = current;
        }
        return out.toString();
    }

    private static JsonArray array(String... values) {
        JsonArray out = new JsonArray();
        for (String value : values) out.add(value);
        return out;
    }

    private static JsonArray sourceList() {
        JsonArray out = new JsonArray();
        Set<String> seen = new LinkedHashSet<String>();
        for (String[] source : SOURCES) if (seen.add(source[0] + " (" + source[1] + ")"))
            out.add(source[0] + " (" + source[1] + ")");
        return out;
    }

    private static String text(JsonObject o, String key) {
        return o.has(key) && !o.get(key).isJsonNull() ? o.get(key).getAsString() : "";
    }

    private static int number(JsonObject o, String key) {
        return o.has(key) && !o.get(key).isJsonNull() ? o.get(key).getAsInt() : Integer.MIN_VALUE;
    }

    private static String q(String value) { return "\"" + value + "\""; }

    private static JsonElement parse(Path file) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(Files.newInputStream(file), UTF8));
        try {
            return JsonParser.parseReader(reader);
        } finally {
            reader.close();
        }
    }

    private static void write(Path file, String content) throws Exception {
        if (file.getParent() != null) Files.createDirectories(file.getParent());
        Writer writer = new BufferedWriter(new OutputStreamWriter(Files.newOutputStream(file), UTF8));
        try {
            writer.write(content);
        } finally {
            writer.close();
        }
    }

    private static String withSeparator(String path) {
        return path.endsWith("/") || path.endsWith("\\") ? path : path + java.io.File.separator;
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalArgumentException(message);
    }

    private IdValidityProbe() { }

    /** The exact verdict names, in decreasing order of trust. */
    public static List<String> verdicts() {
        return Collections.unmodifiableList(
                Arrays.asList(SAME, RESTYLED, RENAMED, REPURPOSED, MISSING, UNVERIFIABLE));
    }
}
