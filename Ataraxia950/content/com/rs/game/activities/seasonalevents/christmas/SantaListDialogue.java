package com.rs.game.activities.seasonalevents.christmas;

import com.rs.game.player.dialogue.Dialogue;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;

/**
 * @author lare96
 */
public final class SantaListDialogue extends Dialogue {

    @Override
    public void start() {
        if (!ChristmasSeasonalEvent.hasStartedEvent(player)) {
            sendPlayerDialogue(NORMAL, "I should talk to Santa before looking at his list.");
            stage = -1;
            return;
        }

        sendOptionsDialogue("Select an option.",
                "View hints",
                "View nice list",
                "View naughty list");
        stage = 0;
    }

    @Override
    public void run(int interfaceId, int componentId) {
       end();
        if (stage == 0) {
            player.lock();
            WorldTasksManager.schedule(new WorldTask() {
                @Override
                public void run() {
                    player.unlock();
                    if (componentId == OPTION_1) {
                        ChristmasSeasonalEvent.viewHints(player);
                    } else if (componentId == OPTION_2) {
                        ChristmasSeasonalEvent.viewNiceHighscores(player);
                    } else if(componentId == OPTION_3) {
                        ChristmasSeasonalEvent.viewNaughtyHighscores(player);
                    }
                }
            }, 1);
        }
    }

    @Override
    public void finish() {

    }
}