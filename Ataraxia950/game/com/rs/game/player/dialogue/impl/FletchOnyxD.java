package com.rs.game.player.dialogue.impl;

import com.rs.game.player.actions.fletching.BoltTipFletching;
import com.rs.game.player.actions.fletching.defs.BoltTips;
import com.rs.game.player.dialogue.Dialogue;

public class FletchOnyxD extends Dialogue {

    @Override
    public void start() {
        sendOptionsDialogue("Are you sure you want to fletch this onyx into bolt tips?", "Yes", "No");
    }

    @Override
    public void run(int interfaceId, int componentId) {
        switch (stage) {
            case -1:
                switch (componentId) {
                    case OPTION_1:
                        BoltTipFletching.boltFletch(player, BoltTips.ONYX);
                        end();
                        break;
                    case OPTION_2:
                        end();
                        break;
                }
                break;
        }
    }

    @Override
    public void finish() {
    }
}
