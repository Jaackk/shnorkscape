package com.rs.utils.data.parsers.items;

import java.util.ArrayList;
import java.util.List;

import com.rs.external.api.json.JsonParser;
import com.rs.utils.DataPaths;
import com.rs.utils.Logger;
import com.rs.utils.data.parsers.items.pojos.TreasureHunterReward;

public class TreasureHunterRewardParser {
    private static List<TreasureHunterReward> treasureHunterRewards = new ArrayList<TreasureHunterReward>();
    /** Relative to DataPaths.root(); resolved when init() runs so the 947 JVM can point at the staged tree. */
    private static final String DEFINITIONS_FILE_PATH = "items/treasurehunterrewards.json";

    public static void init() {
        loadTreasureHunterRewards();
    }

    private static void loadTreasureHunterRewards() {
        JsonParser parser = new JsonParser(DataPaths.resolve(DEFINITIONS_FILE_PATH), TreasureHunterReward[].class);
        TreasureHunterReward[] itemsData = parser.getFileLoaded();
        treasureHunterRewards = new ArrayList<TreasureHunterReward>();
        for (TreasureHunterReward d : itemsData)
            treasureHunterRewards.add(d);
        Logger.getGlobal().info("Loaded " + treasureHunterRewards.size() + " treasure hunter rewards.");
    }
    
    public static void resetTreasureHunterRewards() {
        treasureHunterRewards.clear();
        init();
    }
    
    public static List<TreasureHunterReward> getTreasureHunterRewards() {
        return treasureHunterRewards;
    }

}
