package com.rs.game.player.dialogue.impl.skillingteleports;

import com.rs.game.WorldTile;
import com.rs.game.player.content.Magic;
import com.rs.game.player.dialogue.Dialogue;

/**
 * @author lare96 <http://github.com/lare96>
 */
public final class CraftingTeleportsD extends Dialogue {

    @Override
    public void start() {
        sendOptionsDialogue("Select an option",
                "Crafting guild",
                "Priffdinas crafting area");
        stage = 0;
    }

    @Override
    public void run(int interfaceId, int componentId) {
        switch (stage) {
            case 0:
                if (componentId == OPTION_1) {
                    Magic.vineTeleport(player, new WorldTile(2933, 3289, 0));
                } else if (componentId == OPTION_2) {
                    if(player.hasAccessToPrifddinas()) {
                        Magic.vineTeleport(player, new WorldTile(2145, 3339, 1));
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