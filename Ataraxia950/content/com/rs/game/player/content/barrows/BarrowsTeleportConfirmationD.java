package com.rs.game.player.content.barrows;

import com.rs.game.WorldTile;
import com.rs.game.player.content.Magic;
import com.rs.game.player.dialogue.Dialogue;

/**
 * @author lare96 <http://github.com/lare96>
 */
public final class BarrowsTeleportConfirmationD extends Dialogue {
    @Override
    public void start() {
        sendDialogue("You have not yet looted the chest. Are you sure you would like to teleport back?");
        stage = 0;
    }

    @Override
    public void run(int interfaceId, int componentId) {
switch (stage) {
    case 0:
        sendOptionsDialogue("Select an option.", "Yes", "No");
        break;
    case 1:
        if(componentId == OPTION_1) {
            Magic.vineTeleport(player, new WorldTile(3563, 3288, 0));
            player.sendMessage("You are teleported outside by a mysterious force.");
        }
        end();
        break;
}
    }

    @Override
    public void finish() {

    }
}