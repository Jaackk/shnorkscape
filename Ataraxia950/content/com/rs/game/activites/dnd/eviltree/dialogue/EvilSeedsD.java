package com.rs.game.activites.dnd.eviltree.dialogue;

import com.rs.game.activites.dnd.eviltree.EvilTree;
import com.rs.game.activites.dnd.eviltree.EvilTreeInstanceLevel;
import com.rs.game.map.bossInstance.BossInstance;
import com.rs.game.map.bossInstance.BossInstanceHandler;
import com.rs.game.map.bossInstance.BossInstanceHandler.Boss;
import com.rs.game.map.bossInstance.InstanceSettings;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.Utils;

/**
 * @author lare96 <http://github.com/lare96>
 */
public class EvilSeedsD extends Dialogue {
    private EvilTreeInstanceLevel level;

    @Override
    public void start() {
        BossInstance instance = BossInstanceHandler.findInstance(Boss.Evil_Tree, player.getUsername());
        if (instance == null) {
            sendDialogue("This seed acts as a portal to the Evil Tree homeworld of ScapeRune.",
                    "The Evil Tree Hunter will be the only source of a bank.",
                    "Please choose a difficulty level. You will be rewarded more for higher difficulties.");
            stage = 0;
        } else {
            sendDialogue("You are already have an Evil Tree instance.");
            stage = 1;
        }
    }

    @Override
    public void run(int interfaceId, int componentId) {
        switch (stage) {
            case 0:
                sendOptionsDialogue("Select an option.",
                        "Easy (lowest rewards, max "+EvilTreeInstanceLevel.EASY.maxPlayers+" people)",
                        "Average (better rewards, max "+EvilTreeInstanceLevel.AVERAGE.maxPlayers+" people)",
                        "Hard (best rewards, max "+EvilTreeInstanceLevel.HARD.maxPlayers+" people)");
                stage = 1;
                break;
            case 1:
                if (componentId == OPTION_1) {
                    level = EvilTreeInstanceLevel.EASY;
                } else if (componentId == OPTION_2) {
                    level = EvilTreeInstanceLevel.AVERAGE;
                } else if (componentId == OPTION_3) {
                    level = EvilTreeInstanceLevel.HARD;
                }
                if (level == null) {
                    end();
                    return;
                }
                sendDialogue("Are you sure you would like to start an [" + level.name + "] Evil Tree instance?");
                stage = 2;
                break;
            case 2:
                sendOptionsDialogue("Select an option.", "Yes", "No");
                stage = 3;
                break;
            case 3:
                if (componentId == OPTION_1 && player.getInventory().containsItem(24778, 1)) {
                    player.getInventory().deleteItem(24778, 1);
                    player.etInstanceLvl = level;
                    EvilTree.treeTeleport(player, () -> {
                        InstanceSettings settings = new InstanceSettings(Boss.Evil_Tree);
                        settings.setMaxPlayers(level.maxPlayers + 1);
                        settings.setProtection(BossInstance.FFA);
                        settings.setCreationTime(Utils.currentTimeMillis());
                        BossInstanceHandler.createInstance(player, settings);
                    });
                }
                end();
                break;
        }
    }

    @Override
    public void finish() {

    }
}