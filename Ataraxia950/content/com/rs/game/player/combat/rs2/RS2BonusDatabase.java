package com.rs.game.player.combat.rs2;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import com.rs.utils.DataPaths;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

/**
 * Loads pre-EOC item bonus data from on-disk JSON sources at server startup
 * and exposes both id-keyed and name-keyed lookups. Three sources:
 *
 *   - Phase C overrides (data/rs2_combat/items_overrides.json) - hand-curated
 *     authoritative values for items where the databases miss or disagree.
 *     Keys can be Ataraxia item ids (numeric strings) or display names;
 *     ignored keys begin with "_". Highest priority.
 *   - 2009scape (RS2-era, authoritative for items in the 2009 game). Slimmed
 *     from their item_configs.json into data/rs2_combat/items_2009scape.json.
 *   - osrsbox-db (post-2007 OSRS, fallback for items 2009scape doesn't cover).
 *     Optional - run OsrsboxDataDownloader once to populate
 *     data/rs2_combat/items_osrsbox.json. Lookups silently no-op if missing.
 *
 * Database files share the same shape: a JSON object mapping item display
 * name to a 15-int array in the 2009scape bonus order:
 *   [stab_atk, slash_atk, crush_atk, magic_atk, range_atk,
 *    stab_def, slash_def, crush_def, magic_def, range_def,
 *    summoning_bonus,  // index 10 - ignored, RS3-era only
 *    str_bonus, prayer_bonus, range_str, magic_damage_percent]
 *
 * Names are normalised on load and on lookup so charge suffixes, off-hand
 * prefixes, and poisoned variants resolve to the canonical entry.
 *
 * Overrides are stored in a ConcurrentHashMap and exposed via registerOverride
 * / clearOverrides so admin tooling can hot-patch values without a restart.
 */
public final class RS2BonusDatabase {

    // Resolve at each load through the bootstrap data root; classic cwd/data remains supported.
    private static final String PATH_2009SCAPE = "rs2_combat/items_2009scape.json";
    private static final String PATH_OSRSBOX   = "rs2_combat/items_osrsbox.json";
    private static final String PATH_OVERRIDES = "rs2_combat/items_overrides.json";

    private static final Map<String, ClassicBonuses> RS2_2009SCAPE;
    private static final Map<String, ClassicBonuses> OSRSBOX;
    private static final Map<Integer, ClassicBonuses> OVERRIDES_BY_ID   = new ConcurrentHashMap<>();
    private static final Map<String, ClassicBonuses>  OVERRIDES_BY_NAME = new ConcurrentHashMap<>();

    static {
        RS2_2009SCAPE = loadOrEmpty(PATH_2009SCAPE);
        OSRSBOX       = loadOrEmpty(PATH_OSRSBOX);
        loadOverridesInto(PATH_OVERRIDES, OVERRIDES_BY_ID, OVERRIDES_BY_NAME);
        System.out.println("[RS2BonusDatabase] loaded " + RS2_2009SCAPE.size()
                + " 2009scape entries, " + OSRSBOX.size() + " osrsbox entries, "
                + OVERRIDES_BY_ID.size() + " id-overrides, "
                + OVERRIDES_BY_NAME.size() + " name-overrides");
    }

    private RS2BonusDatabase() {
    }

    /**
     * Returns 2009scape's authoritative bonuses for the item, or null if not
     * present. Match is case-insensitive after normalisation.
     */
    public static ClassicBonuses lookup2009scape(String displayName) {
        if (displayName == null || RS2_2009SCAPE.isEmpty()) return null;
        return RS2_2009SCAPE.get(normalise(displayName));
    }

    /**
     * Returns osrsbox's bonuses for the item, or null if not present.
     * OSRS values may diverge from RS2-era values for items added after 2007;
     * prefer lookup2009scape for an authoritative match where possible.
     */
    public static ClassicBonuses lookupOsrsbox(String displayName) {
        if (displayName == null || OSRSBOX.isEmpty()) return null;
        return OSRSBOX.get(normalise(displayName));
    }

    /**
     * Convenience: try 2009scape, then osrsbox. Returns null if neither has
     * the item.
     */
    public static ClassicBonuses lookup(String displayName) {
        ClassicBonuses fromRs2 = lookup2009scape(displayName);
        if (fromRs2 != null) return fromRs2;
        return lookupOsrsbox(displayName);
    }

    /**
     * Phase C: returns a hand-curated override for the item, or null. Tries
     * the id-keyed map first, then falls back to the normalised name match.
     */
    public static ClassicBonuses lookupOverride(int itemId, String displayName) {
        ClassicBonuses byId = OVERRIDES_BY_ID.get(itemId);
        if (byId != null) return byId;
        if (displayName == null || OVERRIDES_BY_NAME.isEmpty()) return null;
        return OVERRIDES_BY_NAME.get(normalise(displayName));
    }

    /**
     * Programmatically install or replace a Phase C override at runtime.
     * Useful for admin reload commands and tests.
     */
    public static void registerOverride(int itemId, ClassicBonuses bonuses) {
        if (bonuses == null) return;
        OVERRIDES_BY_ID.put(itemId, bonuses);
        ClassicItemBonusResolver.clearCache();
    }

    /**
     * Drops all Phase C overrides. Pair with reloadOverridesFromDisk() to
     * pick up edits to items_overrides.json without restarting.
     */
    public static void clearOverrides() {
        OVERRIDES_BY_ID.clear();
        OVERRIDES_BY_NAME.clear();
        ClassicItemBonusResolver.clearCache();
    }

    /**
     * Re-reads data/rs2_combat/items_overrides.json and atomically swaps the
     * override maps. Existing entries not present in the new file are removed.
     */
    public static void reloadOverridesFromDisk() {
        Map<Integer, ClassicBonuses> freshById = new HashMap<>();
        Map<String, ClassicBonuses>  freshByName = new HashMap<>();
        loadOverridesInto(PATH_OVERRIDES, freshById, freshByName);
        OVERRIDES_BY_ID.clear();
        OVERRIDES_BY_NAME.clear();
        OVERRIDES_BY_ID.putAll(freshById);
        OVERRIDES_BY_NAME.putAll(freshByName);
        ClassicItemBonusResolver.clearCache();
    }

    static String normalise(String name) {
        if (name == null) return "";
        String s = name.trim().toLowerCase();

        // Off-hand prefix - Ataraxia adds these for dual-wield items but
        // 2009scape / OSRS data is keyed on the mainhand name.
        if (s.startsWith("off-hand ")) s = s.substring(9);
        if (s.startsWith("offhand "))  s = s.substring(8);

        // Poisoned weapon suffixes
        s = stripSuffix(s, "(p++)");
        s = stripSuffix(s, "(p+)");
        s = stripSuffix(s, "(p)");

        // Charge / state suffixes
        s = stripSuffix(s, "(charged)");
        s = stripSuffix(s, "(uncharged)");
        s = stripSuffix(s, "(broken)");
        s = stripSuffix(s, "(degraded)");
        s = stripSuffix(s, "(full)");

        // Trailing numeric charge counter, e.g. "Black salamander 1000"
        int lastSpace = s.lastIndexOf(' ');
        if (lastSpace > 0 && lastSpace < s.length() - 1) {
            String tail = s.substring(lastSpace + 1);
            if (tail.matches("\\d+")) {
                s = s.substring(0, lastSpace);
            }
        }

        return s.trim();
    }

    private static String stripSuffix(String s, String suffix) {
        if (s.endsWith(suffix)) return s.substring(0, s.length() - suffix.length()).trim();
        return s;
    }

    static Map<String, ClassicBonuses> loadOrEmpty(String relPath) {
        Path path = DataPaths.path(relPath);
        if (!Files.exists(path)) {
            System.out.println("[RS2BonusDatabase] " + relPath + " not present, skipping");
            return Collections.emptyMap();
        }
        try (Reader reader = new FileReader(path.toFile())) {
            Gson gson = new Gson();
            Map<String, int[]> raw = gson.fromJson(
                    reader,
                    new TypeToken<Map<String, int[]>>() {}.getType());
            if (raw == null) return Collections.emptyMap();
            Map<String, ClassicBonuses> out = new HashMap<>(raw.size() * 2);
            for (Map.Entry<String, int[]> e : raw.entrySet()) {
                ClassicBonuses bonuses = fromArray(e.getValue());
                if (bonuses == null) continue;
                out.put(normalise(e.getKey()), bonuses);
            }
            return out;
        } catch (IOException ex) {
            System.err.println("[RS2BonusDatabase] failed to load " + relPath + ": " + ex.getMessage());
            return Collections.emptyMap();
        }
    }

    private static ClassicBonuses fromArray(int[] arr) {
        if (arr == null || arr.length != 15) return null;
        return new ClassicBonuses(
                arr[0],  arr[1],  arr[2],  arr[3],  arr[4],   // attacks
                arr[5],  arr[6],  arr[7],  arr[8],  arr[9],   // defences
                arr[11],                                       // str (skip arr[10] = summoning)
                arr[13],                                       // range str
                arr[14],                                       // magic damage %
                arr[12]);                                      // prayer
    }

    /**
     * Loads items_overrides.json into the supplied maps. Tolerates mixed-shape
     * values: keys starting with "_" are documentation and skipped; numeric
     * keys go into the id-map; other keys are treated as item names and
     * stored in the name-map after normalisation.
     */
    static void loadOverridesInto(
            String relPath,
            Map<Integer, ClassicBonuses> outById,
            Map<String, ClassicBonuses> outByName) {
        Path path = DataPaths.path(relPath);
        if (!Files.exists(path)) {
            System.out.println("[RS2BonusDatabase] " + relPath + " not present, no overrides loaded");
            return;
        }
        try (Reader reader = new FileReader(path.toFile())) {
            Gson gson = new Gson();
            Map<String, JsonElement> raw = gson.fromJson(
                    reader,
                    new TypeToken<Map<String, JsonElement>>() {}.getType());
            if (raw == null) return;
            for (Map.Entry<String, JsonElement> e : raw.entrySet()) {
                String key = e.getKey();
                if (key.startsWith("_")) continue;
                JsonElement value = e.getValue();
                if (value == null || !value.isJsonArray()) continue;
                int[] arr = jsonArrayToInts(value.getAsJsonArray());
                ClassicBonuses bonuses = fromArray(arr);
                if (bonuses == null) continue;
                try {
                    int id = Integer.parseInt(key);
                    outById.put(id, bonuses);
                } catch (NumberFormatException nfe) {
                    outByName.put(normalise(key), bonuses);
                }
            }
        } catch (IOException ex) {
            System.err.println("[RS2BonusDatabase] failed to load " + relPath + ": " + ex.getMessage());
        }
    }

    private static int[] jsonArrayToInts(JsonArray arr) {
        if (arr == null || arr.size() != 15) return null;
        int[] out = new int[15];
        try {
            for (int i = 0; i < 15; i++) {
                out[i] = arr.get(i).getAsInt();
            }
        } catch (NumberFormatException | UnsupportedOperationException | IllegalStateException ex) {
            return null;
        }
        return out;
    }
}
