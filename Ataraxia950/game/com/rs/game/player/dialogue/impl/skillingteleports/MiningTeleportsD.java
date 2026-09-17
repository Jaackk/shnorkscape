package com.rs.game.player.dialogue.impl.skillingteleports;

import com.rs.game.WorldTile;
import com.rs.game.player.content.Magic;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.Colors;

/**
 * @author lare96 <http://github.com/lare96>
 */
public final class MiningTeleportsD extends Dialogue {

    @Override
    public void start() {
        sendOptionsDialogue("Select an option",
                "Al-Kharid",
                "Karamja",
                "Living rock caverns",
                "Red standstone",
                Colors.RED + "Next...");
        stage = 0;
    }

    @Override
    public void run(int interfaceId, int componentId) {
        switch (stage) {
            case 0:
                if (componentId == OPTION_1) {
                    Magic.vineTeleport(player, new WorldTile(3300, 3312, 0));
                    end();
                } else if (componentId == OPTION_2) {
                    Magic.vineTeleport(player, new WorldTile(2849, 3033, 0));
                    end();
                } else if (componentId == OPTION_3) {
                    Magic.vineTeleport(player, new WorldTile(3652, 5122, 0));
                    end();
                } else if (componentId == OPTION_4) {
                    Magic.vineTeleport(player, new WorldTile(2590, 2880, 0));
                    end();
                } else if (componentId == OPTION_5) {
                    sendOptionsDialogue("Select an option",
                            "Mining guild",
                            "Desert quarry",
                            "Rune essence mine",
                            "Shillo village mine (gem rocks)",
                            Colors.RED + "Next...");
                    stage = 1;
                }
                break;
            case 1:
                if (componentId == OPTION_1) {
                    Magic.vineTeleport(player, new WorldTile(3022, 3337, 0));
                    end();
                } else if (componentId == OPTION_2) {
                    Magic.vineTeleport(player, new WorldTile(3160, 2911, 0));
                    end();
                } else if (componentId == OPTION_3) {
                    Magic.vineTeleport(player, new WorldTile(2911, 4832, 0));
                    end();
                } else if (componentId == OPTION_4) {
                    Magic.vineTeleport(player, new WorldTile(2824, 2997, 0));
                    end();
                } else if (componentId == OPTION_5) {
                    sendOptionsDialogue("Select an option",
                            "Priffdinas seren stones",
                            "Priffdinas ore mine",
                            "South-east varrock",
                            Colors.RED + "Home...");
                    stage = 2;
                }
                break;
            case 2:
                if (componentId == OPTION_1) {
                    if (player.hasAccessToPrifddinas()) {
                        Magic.vineTeleport(player, new WorldTile(2220, 3298, 1));
                    } else {
                        player.sendMessage("You do not meet the requirements to access Priffdinas.");
                    }
                    end();
                } else if (componentId == OPTION_2) {
                    if (player.hasAccessToPrifddinas()) {
                        Magic.vineTeleport(player, new WorldTile(2214, 3325, 1));
                    } else {
                        player.sendMessage("You do not meet the requirements to access Priffdinas.");
                    }
                    end();
                } else if (componentId == OPTION_3) {
                    Magic.vineTeleport(player, new WorldTile(3285, 3371, 0));
                    end();
                } else if (componentId == OPTION_4) {
                    start();
                }
                break;
        }
    }

    @Override
    public void finish() {
        player.getInterfaceManager().closeChatBoxInterface();
    }
}