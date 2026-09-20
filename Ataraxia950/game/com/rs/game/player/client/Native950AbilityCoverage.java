package com.rs.game.player.client;

import com.google.gson.GsonBuilder;
import com.rs.cache.Cache;
import com.rs.cache.filestore.store.Index;
import com.rs.cache.loaders.rs3.RS3ClientScriptMap;
import com.rs.cache.loaders.rs3.RS3GeneralRequirementMap;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.*;

/** Offline cache-wide inventory, not a gameplay admission list or claim of visual coverage. */
public final class Native950AbilityCoverage {
    private Native950AbilityCoverage() { }

    public static void main(String[] args) throws Exception {
        if (args.length != 2) throw new IllegalArgumentException("Usage: Native950AbilityCoverage <950-cache> <report.json>");
        if (!NativeCacheVerification.isEnforced()) throw new IllegalStateException("Cache verification must remain enabled");
        Cache.initFlatReadOnly(Paths.get(args[0]));
        Native950AbilityAssets.verify();
        Map<Integer, Map<String, Object>> rows = new TreeMap<>();
        Index structures = Cache.STORE.getIndexes()[22];
        for (int id : ids(structures, 5)) {
            RS3GeneralRequirementMap definition = RS3GeneralRequirementMap.getMap(id);
            if (!abilityShaped(definition.getValues())) continue;
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("struct", id);
            row.put("name", definition.getStringValue(2794));
            row.put("key", definition.getIntValue(2793));
            row.put("tier", definition.getIntValue(2799));
            row.put("level", definition.getIntValue(2807));
            row.put("cooldown", definition.getIntValue(2796));
            row.put("sha256", hash(structures.getFile(id >> 5, id & 31)));
            row.put("enumMembership", new ArrayList<Map<String, Object>>());
            row.put("animationEnum", definition.getValue(2915));
            row.put("targetGraphic", definition.getValue(2933));
            Native950AbilityCatalog.Definition implemented = Native950AbilityCatalog.get(id);
            row.put("status", implemented == null ? "missing" : "partial");
            row.put("serverRoutedBook", implemented == null ? null : implemented.book);
            row.put("blockers", implemented == null
                    ? Arrays.asList("No native combat catalog implementation; enum membership alone does not authorize execution")
                    : Arrays.asList("Combat Alpha damage/effect model; not full retail semantics", "Visual and complete effect acceptance not established by this inventory"));
            row.put("liveVerifiedByThisTool", false);
            rows.put(id, row);
        }
        Index enums = Cache.STORE.getIndexes()[17];
        for (int id : ids(enums, 8)) {
            RS3ClientScriptMap map = RS3ClientScriptMap.getMap(id);
            // STRUCT is legacy character J, but numeric ScriptVarType 73 in opcode 102.
            if (!map.hasStructValues() || map.getValues() == null) continue;
            for (Map.Entry<Long, Object> entry : new TreeMap<>(map.getValues()).entrySet()) {
                if (!(entry.getValue() instanceof Integer)) continue;
                Map<String, Object> row = rows.get((Integer) entry.getValue());
                if (row == null) continue;
                Map<String, Object> membership = new LinkedHashMap<>();
                membership.put("enum", id); membership.put("key", entry.getKey());
                membership.put("sha256", hash(enums.getFile(id >> 8, id & 255)));
                @SuppressWarnings("unchecked") List<Map<String, Object>> memberships = (List<Map<String, Object>>) row.get("enumMembership");
                memberships.add(membership);
            }
        }
        for (Native950AbilityCatalog.Definition d : Native950AbilityCatalog.DEFINITIONS) {
            if (!rows.containsKey(d.struct)) throw new IllegalStateException("Inventory missed implemented ability " + d.struct);
            @SuppressWarnings("unchecked") List<Map<String,Object>> memberships=(List<Map<String,Object>>) rows.get(d.struct).get("enumMembership");
            if (memberships.stream().noneMatch(m -> ((Integer)m.get("enum"))==Native950ActionBar.enumFor(d.book)
                    && ((Long)m.get("key"))==d.key))
                throw new IllegalStateException("Inventory missed known native book membership " + d.struct);
        }
        Map<String, Integer> counts = new TreeMap<>();
        for (Map<String, Object> row : rows.values()) counts.merge((String) row.get("status"), 1, Integer::sum);
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("revision", 950); report.put("schemaVersion", 1);
        report.put("scope", "All cache index-22 structures with typed name/key/tier/cooldown ability parameters; includes unmounted, alternate and potentially obsolete definitions, not just visible player books");
        report.put("membershipRule", "All index-17 struct-reference enums: legacy opcode 2 character J=74 or modern opcode 102 ScriptVarType=73; membership is evidence, not executable admission");
        report.put("nativeWrites", false); report.put("playerSavesAccessed", false);
        report.put("counts", counts); report.put("total", rows.size()); report.put("abilities", rows.values());
        Files.write(Paths.get(args[1]), new GsonBuilder().serializeNulls().disableHtmlEscaping().setPrettyPrinting().create().toJson(report).getBytes(StandardCharsets.UTF_8));
        System.out.println("ABILITY INVENTORY: " + rows.size() + " cache definitions; " + counts);
    }

    static boolean abilityShaped(Map<Long, Object> values) {
        return values != null && values.get(2794L) instanceof String && !((String) values.get(2794L)).trim().isEmpty()
                && values.get(2793L) instanceof Integer && values.get(2799L) instanceof Integer
                && values.get(2796L) instanceof Integer;
    }

    private static List<Integer> ids(Index index, int shift) {
        if (index == null || index.getTable() == null) throw new IllegalStateException("Missing authoritative cache index");
        List<Integer> result = new ArrayList<>();
        for (int group = 0; group <= index.getLastArchiveId(); group++) {
            if (!index.archiveExists(group)) continue;
            for (int file : index.getTable().getArchives()[group].getValidFileIds()) {
                if (file < 0 || file >= (1 << shift)) throw new IllegalStateException("Unexpected grouped cache ID");
                if (index.getFile(group, file) == null) throw new IllegalStateException("Missing declared cache file");
                result.add((group << shift) | file);
            }
        }
        Collections.sort(result); return result;
    }

    private static String hash(byte[] raw) throws Exception {
        if (raw == null) throw new IllegalStateException("Missing cache evidence");
        StringBuilder out = new StringBuilder();
        for (byte b : MessageDigest.getInstance("SHA-256").digest(raw)) out.append(String.format("%02x", b & 255));
        return out.toString();
    }
}
