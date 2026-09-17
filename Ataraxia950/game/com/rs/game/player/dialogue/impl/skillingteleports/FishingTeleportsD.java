package com.rs.game.player.dialogue.impl.skillingteleports;

import com.rs.game.WorldTile;
import com.rs.game.player.content.Magic;
import com.rs.game.player.dialogue.Dialogue;

/**
 * @author lare96 <http://github.com/lare96>
 */
public final class FishingTeleportsD extends Dialogue {

    @Override
    public void start() {
        sendOptionsDialogue("Select an option",
                "Fishing guild",
                "Living rock cavern",
                "Priffdinas fishing area",
                "Piscatoris fishing colony");
        stage = 0;
    }

    @Override
    public void run(int interfaceId, int componentId) {
        switch (stage) {
            case 0:
                if (componentId == OPTION_1) {
                    Magic.vineTeleport(player, new WorldTile(2596, 3410, 0));
                } else if (componentId == OPTION_2) {
                    Magic.vineTeleport(player, new WorldTile(3014, 9831, 0));
                } else if (componentId == OPTION_3) {
                    if (player.hasAccessToPrifddinas()) {
                        Magic.vineTeleport(player, new WorldTile(2265, 3403, 1));
                    } else {
                        player.sendMessage("You do not meet the requirements to access Priffdinas.");
                    }
                } else if (componentId == OPTION_4) {
                    Magic.vineTeleport(player, new WorldTile(2330, 3690, 0));
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