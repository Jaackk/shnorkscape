package com.rs.game.player.dialogue.impl.skillingteleports;

import com.rs.game.WorldTile;
import com.rs.game.player.content.Magic;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.Colors;

/**
 * @author lare96 <http://github.com/lare96>
 */
public final class HunterTeleportsD extends Dialogue {

    @Override
    public void start() {
        sendOptionsDialogue("Select an option", "Falconry", "Feldip hills", "Puro puro", "Isafdar", Colors.RED + "Next...");
        stage = 0;
    }

    @Override
    public void run(int interfaceId, int componentId) {
        switch (stage) {
        case 0:
            if (componentId == OPTION_1) {
                Magic.vineTeleport(player, new WorldTile(2362, 3623, 0));
                end();
            } else if (componentId == OPTION_2) {
                Magic.vineTeleport(player, new WorldTile(2526, 2916, 0));
                end();
            } else if (componentId == OPTION_3) {
                end();
                if (!player.isCanPvp()) {
                    player.getControlerManager().startControler("PuroPuro");
                } else {
                    player.sendMessage(Colors.RED + "You cannot teleport from the wilderness with this!");
                }
            } else if (componentId == OPTION_4) {
                Magic.vineTeleport(player, new WorldTile(2255, 3183, 0));
                end();
            } else if (componentId == OPTION_5) {
                sendOptionsDialogue("Select an option", "Tree Gnome Stronghold hunter area", "Port Phasmatys hunter area", "Rellekka hunter area", "Desert quarry hunter area", Colors.RED + "Next...");
                stage = 1;
                //
            }
            break;
        case 1:
            if (componentId == OPTION_1) {
                Magic.vineTeleport(player, new WorldTile(2457, 3538, 0));
                end();
            } else if (componentId == OPTION_2) {
                Magic.vineTeleport(player, new WorldTile(3660, 3429, 0));
                end();
            } else if (componentId == OPTION_3) {
                Magic.vineTeleport(player, new WorldTile(2729, 3864, 0));
                end();
            } else if (componentId == OPTION_4) {
                Magic.vineTeleport(player, new WorldTile(3169, 2867, 0));
                end();
            } else if (componentId == OPTION_5) {
                sendOptionsDialogue("Select an option", "Priffdinas hunter area", "North of the Tyras Camp", Colors.RED + "Next...");
                stage = 2;
            }
            break;
        case 2:
            if (componentId == OPTION_1) {
                if (player.hasAccessToPrifddinas()) {
                    Magic.vineTeleport(player, new WorldTile(2235, 3422, 1));
                } else {
                    player.sendMessage("You do not meet the requirements to access Priffdinas.");
                }
                end();
            } else if (componentId == OPTION_2) {
                Magic.vineTeleport(player, new WorldTile(2167, 3180, 0));
                end();
            } else if (componentId == OPTION_3) {
                sendOptionsDialogue("Select an option", "Falconry", "Feldip hills", "Puro puro", "Isafdar", Colors.RED + "Next...");
                stage = 0;
            }
            break;
        }
    }

    @Override
    public void finish() {
        player.getInterfaceManager().closeChatBoxInterface();
    }
}