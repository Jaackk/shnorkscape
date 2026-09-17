package com.rs.game.player.dialogue.impl.skillingteleports;

import com.rs.game.WorldTile;
import com.rs.game.player.content.Magic;
import com.rs.game.player.dialogue.Dialogue;

/**
 * @author lare96 <http://github.com/lare96>
 */
public final class WoodcuttingTeleportsD extends Dialogue {
    @Override
    public void start() {
        sendOptionsDialogue("Select an option",
                "Jungle",
                "Seer's village",
                "Isafdar",
                "South of Varrock",
                "Priffdinas");
        stage = 0;
    }

    @Override
    public void run(int interfaceId, int componentId) {
        switch (stage) {
            case 0:
                if (componentId == OPTION_1) {
                    Magic.vineTeleport(player, new WorldTile(2817, 3083, 0));
                } else if (componentId == OPTION_2) {
                    Magic.vineTeleport(player, new WorldTile(2726, 3477, 0));
                } else if (componentId == OPTION_3) {
                    Magic.vineTeleport(player, new WorldTile(2293, 3142, 0));
                } else if (componentId == OPTION_4) {
                    Magic.vineTeleport(player, new WorldTile(3257, 3369, 0));
                } else if (componentId == OPTION_5) {
                    if(player.hasAccessToPrifddinas()) {
                        Magic.vineTeleport(player, new WorldTile(2247, 3382, 1));
                    } else {
                        player.sendMessage("You do not meet the requirements to access Priffdinas.");
                    }
                }
                end();
                break;
        }
    }

    @Override
    public void finish() {
        player.getInterfaceManager().closeChatBoxInterface();
    }
}