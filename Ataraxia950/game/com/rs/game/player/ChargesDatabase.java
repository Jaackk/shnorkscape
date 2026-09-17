package com.rs.game.player;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.rs.utils.Logger;

import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * @author lare96 <http://github.com/lare96>
 */
public final class ChargesDatabase {

    public static final class ChargesData {
        public final int id;
        public final int defaultCharges;
        public final int degradeId;
        public final int deathDegradeId;
        public final boolean degradesInCombat;
        public final boolean degradesWhileWearing;

        public ChargesData(JsonObject reader) {
            id = reader.get("id").getAsInt();
            defaultCharges = reader.get("default_charges").getAsInt();
            degradeId = reader.get("degrade_id").getAsInt();
            deathDegradeId = reader.get("death_degrade_id").getAsInt();
            degradesInCombat = reader.get("degrades_in_combat?").getAsBoolean();
            degradesWhileWearing = reader.get("degrades_while_wearing?").getAsBoolean();
        }
    }

    public static final Map<Integer, ChargesData> data = new HashMap<>();

    public static void load() {
        Path path = Paths.get("data/items/degradables.json");
        if(!Files.exists(path)) {
            return;
        }
        try (FileReader in = new FileReader(path.toFile())) {
            JsonParser parser = new JsonParser();
            JsonArray array = (JsonArray) parser.parse(in);

            for (int i = 0; i < array.size(); i++) {
                JsonObject reader = (JsonObject) array.get(i);
                data.put(reader.get("id").getAsInt(), new ChargesData(reader));
            }
        } catch (Exception e) {
            Logger.getGlobal().catching(e);
        }
    }
}
