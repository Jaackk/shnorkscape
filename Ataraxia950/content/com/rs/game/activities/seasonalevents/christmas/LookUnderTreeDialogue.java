package com.rs.game.activities.seasonalevents.christmas;

import com.rs.cores.CoresManager;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.JsonSerializable;

import java.util.concurrent.ThreadLocalRandom;

import static com.rs.game.activities.seasonalevents.christmas.ChristmasSeasonalEvent.TREE_PRESENT_ITEM;
import static com.rs.game.activities.seasonalevents.christmas.ChristmasSeasonalEvent.TREE_PRESENT_PATH;
import static com.rs.game.activities.seasonalevents.christmas.ChristmasSeasonalEvent.treePresents;

/**
 * @author lare96
 */
public final class LookUnderTreeDialogue extends Dialogue {

    private final int freeSlots;
    private int slotsNeeded;

    public LookUnderTreeDialogue(int freeSlots) {
        this.freeSlots = freeSlots;
    }

    @Override
    public void start() {
        sendPlayerDialogue(HAPPY, "Hey, he did leave something here for me!");
        slotsNeeded = ThreadLocalRandom.current().nextInt(1, 3);
        if (slotsNeeded > freeSlots) {
            slotsNeeded = freeSlots;
        }
        player.getInventory().addItem(TREE_PRESENT_ITEM, slotsNeeded);
        treePresents.add(player.getUsername());
        CoresManager.getServiceProvider().executeNow(() -> JsonSerializable.save(TREE_PRESENT_PATH, treePresents));
        stage = 0;
    }

    @Override
    public void run(int interfaceId, int componentId) {
        if (stage == 0) {
            sendItemDialogue(TREE_PRESENT_ITEM, slotsNeeded, "You find a present under the tree.");
            stage = 1;
        } else {
            end();
        }
    }

    @Override
    public void finish() {

    }
}
