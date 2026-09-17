package com.rs.game.player.combat.rs2;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * One-shot utility that fetches osrsbox-db's items-complete.json, slims it
 * down to the same {name -> 15-int array} shape used by RS2BonusDatabase,
 * and writes it to data/rs2_combat/items_osrsbox.json.
 *
 * Run once per cache update:
 *   java -cp <project-classpath> com.rs.game.player.combat.rs2.OsrsboxDataDownloader
 *
 * The downloaded file is gitignored-ish in size (~150 KB). RS2BonusDatabase
 * silently no-ops if it isn't present, so this is optional - 2009scape's
 * data covers most pre-2007 items already.
 *
 * osrsbox values are most useful for items added 2007 onwards in OSRS that
 * aren't in 2009scape's RS2 dataset (Saradomin sword stat tweaks, dragon
 * claws, etc.). For RS3-only items added 2009-2019, neither source has
 * data and the resolver falls through to the tier curve.
 */
public final class OsrsboxDataDownloader {

    private static final String SOURCE_URL =
            "https://raw.githubusercontent.com/osrsbox/osrsbox-db/master/docs/items-complete.json";
    private static final String OUT_PATH = "data/rs2_combat/items_osrsbox.json";

    private OsrsboxDataDownloader() {
    }

    public static void main(String[] args) throws Exception {
        System.out.println("[osrsbox] fetching " + SOURCE_URL);
        String raw = fetch(SOURCE_URL);
        System.out.println("[osrsbox] downloaded " + raw.length() + " chars, parsing...");

        @SuppressWarnings("deprecation")
        JsonObject root = new JsonParser().parse(raw).getAsJsonObject();
        Map<String, int[]> slim = new LinkedHashMap<>();

        int total = 0;
        int kept = 0;
        for (Map.Entry<String, JsonElement> entry : root.entrySet()) {
            total++;
            JsonObject item = entry.getValue().getAsJsonObject();
            if (!item.has("name") || item.get("name").isJsonNull()) continue;
            if (!item.has("equipable_by_player")
                    || !item.get("equipable_by_player").getAsBoolean()) continue;
            if (!item.has("equipment") || item.get("equipment").isJsonNull()) continue;

            JsonObject eq = item.getAsJsonObject("equipment");
            int[] arr = toBonusArray(eq);
            if (allZero(arr)) continue;

            String name = item.get("name").getAsString();
            slim.put(name, arr);
            kept++;
        }

        System.out.println("[osrsbox] scanned " + total + " items, kept " + kept + " with bonuses");

        // Make sure the parent directory exists.
        java.io.File outFile = new java.io.File(OUT_PATH);
        outFile.getParentFile().mkdirs();
        try (BufferedWriter writer = Files.newBufferedWriter(outFile.toPath(), StandardCharsets.UTF_8)) {
            new Gson().toJson(slim, writer);
        }
        System.out.println("[osrsbox] wrote " + OUT_PATH);
    }

    private static int[] toBonusArray(JsonObject eq) {
        return new int[] {
                intOrZero(eq, "attack_stab"),
                intOrZero(eq, "attack_slash"),
                intOrZero(eq, "attack_crush"),
                intOrZero(eq, "attack_magic"),
                intOrZero(eq, "attack_ranged"),
                intOrZero(eq, "defence_stab"),
                intOrZero(eq, "defence_slash"),
                intOrZero(eq, "defence_crush"),
                intOrZero(eq, "defence_magic"),
                intOrZero(eq, "defence_ranged"),
                0, // summoning bonus - OSRS has no summoning skill
                intOrZero(eq, "melee_strength"),
                intOrZero(eq, "prayer"),
                intOrZero(eq, "ranged_strength"),
                intOrZero(eq, "magic_damage")
        };
    }

    private static int intOrZero(JsonObject obj, String field) {
        if (!obj.has(field) || obj.get(field).isJsonNull()) return 0;
        try {
            return obj.get(field).getAsInt();
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    private static boolean allZero(int[] arr) {
        for (int v : arr) {
            if (v != 0) return false;
        }
        return true;
    }

    private static String fetch(String url) throws Exception {
        URLConnection conn = new URL(url).openConnection();
        conn.setRequestProperty("User-Agent", "Ataraxia-PS/Phase-B");
        conn.setRequestProperty("Accept-Encoding", "gzip");
        conn.setConnectTimeout(15000);
        conn.setReadTimeout(60000);
        java.io.InputStream is = conn.getInputStream();
        if ("gzip".equalsIgnoreCase(conn.getContentEncoding())) {
            is = new GZIPInputStream(is);
        }
        StringBuilder out = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                out.append(line).append('\n');
            }
        }
        return out.toString();
    }
}
