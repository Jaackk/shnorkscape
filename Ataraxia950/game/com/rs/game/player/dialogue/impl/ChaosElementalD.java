package com.rs.game.player.dialogue.impl;

import com.rs.game.WorldTile;
import com.rs.game.player.content.Magic;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.Colors;

/**
 * @author lare96 <http://github.com/lare96>
 */
public class ChaosElementalD extends Dialogue {



    @Override
    public void start() {
        sendDialogue("This destination is located in the " + Colors.RED + "deep wild</col>.",
                "Are you sure you would like to teleport there?");
    }

    @Override
    public void run(int interfaceId, int componentId) {
        switch (stage) {
            case -1:
                stage = 0;
                sendOptionsDialogue("Select an option.", "Yes", "No");
                break;
            case 0:
                finish();
                if (componentId == OPTION_1) {
                    Magic.vineTeleport(player, new WorldTile(3251, 3915, 0));
                }
                break;
        }
    }

    @Override
    public void finish() {
        player.getInterfaceManager().closeChatBoxInterface();
    }
}