package com.rs.game.player.content.newlottery;

import com.rs.game.player.dialogue.Dialogue;
import lombok.val;

public class DLotterySingleStageDialogue extends Dialogue {

    @Override
    public void start() {
        val expression = (int) parameters[0];
        val text = (String[]) parameters[1];
        sendNPCDialogue(player, Lottery.getSingleton().coordinatorId, expression, text);
    }

    @Override
    public void run(int interfaceId, int componentId) {
        end();
    }

    @Override
    public void finish() {

    }

}
