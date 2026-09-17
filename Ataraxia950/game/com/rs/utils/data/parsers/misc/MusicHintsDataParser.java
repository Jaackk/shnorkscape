package com.rs.utils.data.parsers.misc;

import com.rs.external.api.json.JsonParser;
import com.rs.utils.DataPaths;
import com.rs.utils.Logger;
import com.rs.utils.data.parsers.misc.pojos.MusicHint;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

public class MusicHintsDataParser {
    private static final Int2ObjectOpenHashMap<String> MUSIC_HINTS = new Int2ObjectOpenHashMap<>();
    /** Relative to DataPaths.root(); resolved when init() runs so the 947 JVM can point at the staged tree. */
    private static final String DEFINITIONS_FILE_PATH = "musics/hints.json";

    public static void init() {
        loadMusicHints();
    }

    private static void loadMusicHints() {
        JsonParser parser = new JsonParser(DataPaths.resolve(DEFINITIONS_FILE_PATH), MusicHint[].class);
        MusicHint[] hints = parser.getFileLoaded();
        for (MusicHint hint : hints) {
            MUSIC_HINTS.put(hint.getRegionId(), hint.getHint());
        }
        Logger.getGlobal().info("Loaded " + MUSIC_HINTS.size() + " music hints.");
    }

    public static String getHint(int musicId) {
        String hint = MUSIC_HINTS.get(musicId);
        if (hint == null)
            return "somewhere.";
        return hint;
    }

    // converter:
    /*
    MusicHints.init();
        try (Writer writer = new FileWriter("Output.json")) {
            writer.write("[\n");
            MusicHints.musicHints.forEach((key, value) -> {
                try {
                    writer.write("\t{\n");
                    writer.write("\t\t\"regionId\": " + key + ",\n");
                    writer.write("\t\t\"hint\": \"" + value + "\"\n");
                    writer.write("\t},\n");
                } catch (IOException e) {
                    Logger.getGlobal().catching(e);
                }
            });
            writer.write("]");
        } catch (IOException e) {
            Logger.getGlobal().catching(e);
        }
     */
}
