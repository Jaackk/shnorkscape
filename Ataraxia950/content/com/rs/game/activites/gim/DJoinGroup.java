package com.rs.game.activites.gim;

import com.rs.game.player.Player;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.InputStringEvent;

/**
 * A dialogue that enables players to join GIM groups.
 *
 * @author lare96 <http://github.com/lare96>
 */
public final class DJoinGroup extends Dialogue {

    @Override
    public void start() {
        sendInput();
    }

    @Override
    public void run(int interfaceId, int componentId) {
        switch (stage) {
            case 0:
                sendInput();
                break;
            case 1:
                GIM.updateWaitForPartners(player);
                break;
        }
    }

    @Override
    public void finish() {
        Dialogue.closeNoContinueDialogue(player);
    }

    /**
     * Sends the group name input.
     */
    private void sendInput() {
        player.getInterfaceManager().closeScreenInterface();
        player.sendInputString("Please enter the name of the group", new InputStringEvent() {
            @Override
            public void run(Player player) {
                String groupKey = getString().toLowerCase().trim();
                if (GIM.addMember(groupKey, player)) {
                    sendNPCDialogue(6139, CALM, "You have successfully joined a group! Please wait for other members to join. Logging out will remove you from the group.");
                    stage = 1;
                } else {
                    sendNPCDialogue(6139, CALM, "There is no group with the name '" + groupKey + "'.");
                    stage = 0;
                }
            }
        });
    }
}