package com.rs.game.activites.gim.season;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.rs.cores.CoresManager;
import com.rs.external.api.json.JsonParser;
import com.rs.game.activites.gim.GIMGroup;
import com.rs.game.activites.gim.bank.GIMBank;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.content.pet.Pets;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Logger;
import com.rs.utils.Utils;

import java.io.FileWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A class that handles rewards for GIM season winners.
 *
 * @author lare96 <http://github.com/lare96>
 */
public final class GIMRewardsManager {
    /*
        Rewards for the next year:
        MONTH 1 ~ Cheerleader pet
        MONTH 2 ~ ???
        MONTH 3 ~ ???
        MONTH 4 ~ ???

     */

    /**
     * The royal letterID. It tells the players that the developers can make a custom pet for them. They can choose
     * its name and armor.
     */
    private final int royalLetterId = 23080;

    /**
     * The reward box ID.
     */
    private final int rewardBoxId = 10025;

    /**
     * The items that will be awarded with the GIM reward box.
     */
    private final ImmutableList<Item> rewardItems = ImmutableList.of(
            new Item(1842)
    );

    /**
     * The path to the rewards file.
     */
    private final Path rewardsPath = Paths.get("data", "gim", "rewards.json");

    /**
     * Users who still need to collect their rewards.
     */
    private final Set<String> rewards = new HashSet<>();

    /**
     * The Gson instance.
     */
    private final Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();

    /**
     * Loads the pending user rewards into memory.
     */
    public void load() {
        Utils.createFileIfNotExists(rewardsPath);
        String[] loadedBank = new JsonParser(rewardsPath.toString(), String[].class).getFileLoaded();
        if (loadedBank != null) {
            synchronized (rewards) {
                rewards.addAll(Arrays.asList(loadedBank));
            }
        }
    }

    /**
     * Register a group for rewards.
     */
    public void register(GIMGroup group) {
        synchronized (rewards) {
            rewards.addAll(group.getMembers());
            save();
        }
    }

    /**
     * Allows a player from a winning group to claim their rewards.
     */
    public void claim(Player player) {
        player.lock();
        player.getDialogueManager().finishDialogue();
        Dialogue.sendNPCDialogueNoContinue(player, 12320, Dialogue.NORMAL, "Hold on while I verify your request...");
        WorldTasksManager.schedule(new WorldTask() {
            @Override
            public void run() {
                Dialogue.closeNoContinueDialogue(player);
                player.unlock();
                if (rewards.remove(player.getUsername())) {
                    save();
                    player.addItem(rewardBoxId, 1);
                    Dialogue.sendSingleNPCDialogue(player, 12320, Dialogue.NORMAL, "Congratulations, I've given you your GIM reward box! I wonder what's inside...");
                } else {
                    Dialogue.sendSingleNPCDialogue(player, 12320, Dialogue.NORMAL, "There are no rewards for you to claim.");
                }
            }
        }, 3);
    }

    /**
     * Opens the GIM reward box.
     */
    public void openRewardBox(Player player) {
        if (player.getInventory().containsItem(rewardBoxId, 1)) {
            player.getInventory().deleteItem(rewardBoxId, 1);
            for (Item item : rewardItems) {
                player.addItem(item);
            }
            player.getInterfaceManager().closeScreenInterface();
            player.sendMessage("You open the <img=33> GIM reward box...");
        }
    }

    /**
     * Opens the royal letter.
     */
    public void openRoyalLetter(Player player) {
    }

    /**
     * Saves the pending rewards.
     */
    private void save() {
        try (FileWriter fw = new FileWriter(rewardsPath.toFile())) {
            gson.toJson(rewards, fw);
        } catch (Exception e) {
            Logger.getGlobal().catching(e);
        }
    }

    public int getRewardBoxId() {
        return rewardBoxId;
    }

    public int getRoyalLetterId() {
        return royalLetterId;
    }
}
