package com.rs.game.player.dialogue.impl.skillingteleports;

import com.rs.game.WorldTile;
import com.rs.game.player.content.Magic;
import com.rs.game.player.controllers.RunespanController;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.Colors;

/**
 * @author lare96 <http://github.com/lare96>
 */
public final class RunecraftingTeleportsD extends Dialogue {

    @Override
    public void start() {
        sendOptionsDialogue("Select an option",
                "Runespan",
                "Abyss");
        stage = 0;
    }

    @Override
    public void run(int interfaceId, int componentId) {
        switch (stage) {
            case 0:
                if (componentId == OPTION_1) {
                    if (!player.isCanPvp()) {
                        RunespanController.enterRunespan(player);
                    } else {
                        player.sendMessage(Colors.RED + "You can't teleport from the wilderness with this!");
                    }
                    end();
                } else if (componentId == OPTION_2) {
                    end();
                    Magic.vineTeleport(player, new WorldTile(3039, 4834, 0));
                }
                break;
        }
    }

    @Override
    public void finish() {
        player.getInterfaceManager().closeChatBoxInterface();
    }
}