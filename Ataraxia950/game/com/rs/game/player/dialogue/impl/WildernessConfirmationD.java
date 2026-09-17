package com.rs.game.player.dialogue.impl;

import com.rs.game.WorldTile;
import com.rs.game.player.content.Magic;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.Colors;

/**
 * ataraxia-server
 * paolo 31/07/2019
 * #Shnek6969
 */
public class WildernessConfirmationD extends Dialogue {

    private WorldTile tile;

    @Override
    public void start() {
        tile = (WorldTile) parameters[0];
        sendOptionsDialogue("This location is located in the Wilderness this is a "+ Colors.RED+" dangerous</col> zone, are you sure?",
                "Yes teleport me into the Wilderness.", "No I don't want to enter the Wilderness.");
        stage = 1;
    }

    @Override
    public void run(int interfaceId, int componentId) {
        if(componentId == OPTION_1){
            Magic.vineTeleport(player, tile);
        }
        end();
    }

    @Override
    public void finish() {

    }
}
