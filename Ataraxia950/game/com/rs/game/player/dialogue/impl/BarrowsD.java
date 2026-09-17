package com.rs.game.player.dialogue.impl;

import com.rs.game.WorldTile;
import com.rs.game.player.PerkManager.DonationPerk;
import com.rs.game.player.content.barrows.Barrows;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.Colors;

public class BarrowsD extends Dialogue {

    @Override
    public void finish() {

    }

    @Override
    public void run(int interfaceId, int componentId) {
        if (stage == -1) {
            stage = 0;
            sendOptionsDialogue(SEND_DEFAULT_OPTIONS_TITLE, "Yes, I'm fearless.", "No way, that looks scary!");
        } else if (stage == 0) {
            if (componentId == OPTION_1) {
                if (player.getPerkManager().hasPerkActive(DonationPerk.THE_SKIPPER)) {
                    player.setNextWorldTile(new WorldTile(3552, 9692, 0));
                    player.sendFilteredMessage("You have been transported straight to the chest, thanks to " + Colors.RED + "The Skipper</col> perk");
                } else {
                    player.setNextWorldTile(Barrows.ROOM_CENTER_LOCATIONS[player.getHiddenBrother()]);
                }
                if (player.getControlerManager().getControler() instanceof Barrows)
                    ((Barrows) player.getControlerManager().getControler()).reloadObjects();
            }
            end();
        }
    }

    @Override
    public void start() {
        sendDialogue("You've found a hidden tunnel, do you want to enter?");
    }

}
