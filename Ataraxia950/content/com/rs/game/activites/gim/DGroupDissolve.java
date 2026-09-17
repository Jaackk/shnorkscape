package com.rs.game.activites.gim;

import com.rs.game.player.dialogue.Dialogue;

/**
 * A dialogue presented when a pending group is dissolved.
 *
 * @author lare96 <http://github.com/lare96>
 */
public final class DGroupDissolve extends Dialogue {

    /**
     * The reason for the dissolve.
     */
    private final String reason;

    /**
     * Creates a new {@link DGroupDissolve}.
     */
    public DGroupDissolve(String reason) {
        this.reason = reason;
    }

    @Override
    public void start() {
        sendNPCDialogue(6139, CALM, "The group was dissolved because " + reason + ".");
        stage = 0;
    }

    @Override
    public void run(int interfaceId, int componentId) {
        switch (stage) {
            case 0:
                end();
                break;
        }
    }

    @Override
    public void finish() {
    }
}