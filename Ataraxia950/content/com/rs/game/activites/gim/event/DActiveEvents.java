package com.rs.game.activites.gim.event;

import com.rs.game.activites.gim.GIM;
import com.rs.game.activites.gim.event.GIMEventManager;
import com.rs.game.activites.gim.guide.DGIMGuide;
import com.rs.game.activites.gim.guide.DUnregisteredGIM;
import com.rs.game.player.dialogue.Dialogue;

/**
 * A dialogue that enables GIM to check active events.
 *
 * @author lare96 <http://github.com/lare96>
 */
public final class DActiveEvents extends Dialogue {

    /**
     * If this dialogue should loop.
     */
    private boolean dontLoop;

    /**
     * Makes this dialogue not loop.
     */
    public DActiveEvents dontLoop() {
        dontLoop = true;
        return this;
    }

    @Override
    public void start() {
        if(player.isUnregisteredGIM()) {
            player.getDialogueManager().startDialogue(new DUnregisteredGIM());
            return;
        }
        if (player.isGroupIronman()) {
            if (GIM.getEventManager().isRunning()) {
                sendNPCDialogue(12320, NORMAL, "There is currently a " + GIM.getEventManager().getEventDescription() + " event running.",
                        "It will end on " + GIM.getEventManager().getFormattedEndDate() + ".");
                stage = 0;
            } else {
                sendNPCDialogue(12320, NORMAL, "There aren't any events currently active.");
                stage = 0;
            }
        } else {
            sendNPCDialogue(12320, NORMAL, "You aren't a Group Ironman, stop wasting my time peasant.");
            stage = -1;
        }
    }

    @Override
    public void run(int interfaceId, int componentId) {
        if (stage == -1) {
            end();
        } else if (stage == 0) {
            if (dontLoop) {
                end();
            } else {
                player.getDialogueManager().startDialogue(new DGIMGuide());
            }
        }
    }

    @Override
    public void finish() {

    }
}
