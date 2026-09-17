package com.rs.game.player.dialogue.impl.skillingteleports;

import com.rs.game.WorldTile;
import com.rs.game.player.content.Magic;
import com.rs.game.player.dialogue.Dialogue;

/**
 * @author lare96 <http://github.com/lare96>
 */
public final class AgilityTeleportsD extends Dialogue {

    @Override
    public void start() {
        sendOptionsDialogue("Select an option",
                "Gnome agility course",
                "Barbarian outpost agility course",
                "Wilderness agility course",
                "Agility pyramid",
                "Priffdinas agility course");
        stage = 0;
    }

    @Override
    public void run(int interfaceId, int componentId) {
        switch (stage) {
            case 0:
                if (componentId == OPTION_1) {
                    Magic.vineTeleport(player, new WorldTile(2470, 3436, 0));
                } else if (componentId == OPTION_2) {
                    Magic.vineTeleport(player, new WorldTile(2552, 3563, 0));
                } else if (componentId == OPTION_3) {
                    Magic.vineTeleport(player, new WorldTile(2998, 3911, 0));
                } else if (componentId == OPTION_4) {
                    Magic.vineTeleport(player, new WorldTile(3358, 2828, 0));
                } else if (componentId == OPTION_5) {
                    if(player.hasAccessToPrifddinas()) {
                        Magic.vineTeleport(player, new WorldTile(2178, 3398, 1));
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