package com.rs.game.activities.seasonalevents.christmas;

import com.rs.Settings;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.Utils;

import java.awt.Dialog;
import java.util.concurrent.ThreadLocalRandom;

import static com.rs.game.activities.seasonalevents.christmas.ChristmasSeasonalEvent.SNOWMAN_EXAMINE;

/**
 * @author lare96
 */
public final class InspectSnowmanDialogue extends Dialogue {
    @Override
    public void start() {
        int chance = Settings.TEST_SERVER_MODE ? 5 : 100;
        if (ThreadLocalRandom.current().nextInt(chance) == 0) {
            sendPlayerDialogue(NORMAL, "Imagine if it was alive...");
            stage = 0;
        } else {
            sendPlayerDialogue(player, NORMAL, Utils.randomFrom(SNOWMAN_EXAMINE));
            stage = -1;
        }
    }

    @Override
    public void run(int interfaceId, int componentId) {
        if (stage == 0) {
            sendNPCDialogue(6746, ANGRY, "I AM ALIVE!");
            stage = 1;
        } else if (stage == 1) {
            sendPlayerDialogue(SCARED, "WHAT?!?");
            stage = -1;
        } else {
            end();
        }
    }

    @Override
    public void finish() {

    }
}
