package com.rs.game.player.dialogue.impl.skillingteleports;

import com.rs.game.WorldTile;
import com.rs.game.player.content.Magic;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.Colors;

/**
 * @author lare96 <http://github.com/lare96>
 */
public final class FarmingTeleportsD extends Dialogue {

    @Override
    public void start() {
        sendOptionsDialogue("Select an option",
                "Herb patches",
                "Tree patches",
                "Fruit tree patches");
        stage = 0;
    }

    @Override
    public void run(int interfaceId, int componentId) {
        switch (stage) {
            case 0:
                if (componentId == OPTION_1) {
                    sendOptionsDialogue("Select an option",
                            "Falador herb patch",
                            "Catherby herb patch",
                            "Ardougne herb patch",
                            "Port Phasmatys herb patch",
                            "Priffdinas herb patch");
                    stage = 1;
                } else if (componentId == OPTION_2) {
                    sendOptionsDialogue("Select an option",
                            "Taverly tree patch",
                            "Varrock tree patch",
                            "Falador tree patch",
                            "Gnome stronghold tree patch",
                            Colors.RED + "Next...");
                    stage = 2;
                } else if (componentId == OPTION_3) {
                    sendOptionsDialogue("Select an option",
                            "Catherby fruit tree patch",
                            "Tree Gnome fruit tree patch",
                            "Karamja fruit tree patch",
                            "Lletya fruit tree patch",
                            Colors.RED + "Next...");
                    stage = 4;
                }
                break;
            case 1:
                if (componentId == OPTION_1) {
                    Magic.vineTeleport(player, new WorldTile(3055, 3310, 0));
                } else if (componentId == OPTION_2) {
                    Magic.vineTeleport(player, new WorldTile(2785, 3464, 0));
                } else if (componentId == OPTION_3) {
                    Magic.vineTeleport(player, new WorldTile(2664, 3374, 0));
                } else if (componentId == OPTION_4) {
                    Magic.vineTeleport(player, new WorldTile(3599, 3523, 0));
                } else if (componentId == OPTION_5) {
                    if (player.hasAccessToPrifddinas()) {
                        Magic.vineTeleport(player, new WorldTile(2249, 3383, 1));
                    } else {
                        player.sendMessage("You do not meet the requirements to access Priffdinas.");
                    }
                }
                end();
                break;
            case 2:
                if (componentId == OPTION_1) {
                    Magic.vineTeleport(player, new WorldTile(2923, 3429, 0));
                    end();
                } else if (componentId == OPTION_2) {
                    Magic.vineTeleport(player, new WorldTile(3227, 3464, 0));
                    end();
                } else if (componentId == OPTION_3) {
                    Magic.vineTeleport(player, new WorldTile(3004, 3377, 0));
                    end();
                } else if (componentId == OPTION_4) {
                    Magic.vineTeleport(player, new WorldTile(2432, 3418, 0));
                    end();
                } else if (componentId == OPTION_5) {
                    sendOptionsDialogue("Select an option",
                            "Lumbridge tree patch",
                            "Priffdinas tree patch");
                    stage = 3;
                }
                break;
            case 3:
                if (componentId == OPTION_1) {
                    Magic.vineTeleport(player, new WorldTile(3226, 3248, 0));
                } else if (componentId == OPTION_2) {
                    if(player.hasAccessToPrifddinas()) {
                        Magic.vineTeleport(player, new WorldTile(2231, 3300, 1));
                    } else {
                        player.sendMessage("You do not meet the requirements to access Priffdinas.");
                    }
                }
                end();
                break;
            case 4:
                if (componentId == OPTION_1) {
                    Magic.vineTeleport(player, new WorldTile(2857, 3433, 0));
                    end();
                } else if (componentId == OPTION_2) {
                    Magic.vineTeleport(player, new WorldTile(2476, 3448, 0));
                    end();
                } else if (componentId == OPTION_3) {
                    Magic.vineTeleport(player, new WorldTile(2765, 3209, 0));
                    end();
                } else if (componentId == OPTION_4) {
                    Magic.vineTeleport(player, new WorldTile(2345, 3165, 0));
                    end();
                } else if (componentId == OPTION_5) {
                    sendOptionsDialogue("Select an option",
                            "Gnome Village fruit tree patch",
                            "Priffdinas fruit tree patch");
                    stage = 5;
                }
                break;
            case 5:
                if (componentId == OPTION_1) {
                    Magic.vineTeleport(player, new WorldTile(2491, 3177, 0));
                } else if (componentId == OPTION_2) {
                    if(player.hasAccessToPrifddinas()) {
                        Magic.vineTeleport(player, new WorldTile(2218, 3433, 1));
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