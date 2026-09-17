package com.rs.game.player.content.death;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.rs.external.api.json.JsonParser;
import com.rs.game.item.Item;
import com.rs.game.player.DataInterface;
import com.rs.game.player.Player;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.Colors;
import com.rs.utils.Logger;
import com.rs.utils.Utils;

import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicLong;

public final class DeathStatistics {

    private transient final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private transient final Path statsFile = Paths.get("data", "misc", "death_stats.json");

    public void save() {
        try {
            if (!Files.exists(statsFile))
                Files.createFile(statsFile);
            try (FileWriter fw = new FileWriter(statsFile.toFile())) {
                fw.write(gson.toJson(DeathStatistics.this));
            }
        } catch (Exception e) {
            Logger.getGlobal().catching(e);
        }
    }

    public void load() {
        if (Files.exists(statsFile)) {
            JsonParser parser = new JsonParser(statsFile.toFile().getPath(), DeathStatistics.class);
            DeathStatistics loaded = parser.getFileLoaded();
            totalDeaths = loaded.totalDeaths;
            totalGpTaken = loaded.totalGpTaken;
        }
    }

    public void openStats(Player player) {
        player.getDialogueManager().startDialogue("SingleNPCDialogue", 14386, Dialogue.CALM, new String[]{
                "I have taken a total of " + Colors.RED + Utils.formatNumber(totalGpTaken.get()) + "gp</col>.",
                "Players have died a total of " + Colors.RED + totalDeaths + " times</col>."
        });
    }

    public void openClaim(Player player) {
        if (player.deathItemsManager.getClaimableItemCount() > 0) {
            player.getDialogueManager().startDialogue(new Dialogue() {
                @Override
                public void start() {
                    sendOptionsDialogue("Select an option.",
                            "View claimable items",
                            "Claim items");
                }

                @Override
                public void run(int interfaceId, int componentId) {
                    end();
                    if (componentId == OPTION_1) {
                        int size = player.deathItemsManager.getClaimableItemCount();
                        DataInterface inter = new DataInterface("Claimable death items");
                        inter.add(Colors.DARK_RED + "Total items: " + size);
                        inter.blankLine();
                        if (size > 0) {
                            for (Item item : player.deathItemsManager.getClaimableItems()) {
                                inter.add(item.getName() + " (x" + item.getAmount() + ")");
                            }
                        } else {
                            inter.add(Colors.DARK_RED + "No items to claim!");
                        }
                        inter.show(player);
                    } else if (componentId == OPTION_2) {
                        player.deathItemsManager.withdrawClaimedItems();
                    }
                }

                @Override
                public void finish() {

                }
            });
        } else {
            player.getDialogueManager().startDialogue("SingleNPCDialogue", 14386, Dialogue.CALM, new String[]{
                    "You do not have any more items to claim."
            });
        }
    }

    private static final DeathStatistics instance = new DeathStatistics();

    public static DeathStatistics getInstance() {
        return instance;
    }

    public AtomicLong totalGpTaken = new AtomicLong(0);
    public AtomicLong totalDeaths = new AtomicLong(0);
}
