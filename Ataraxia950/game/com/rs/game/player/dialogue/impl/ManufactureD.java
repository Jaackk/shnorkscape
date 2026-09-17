package com.rs.game.player.dialogue.impl;

import com.rs.game.player.actions.invention.Manufacture;
import com.rs.game.player.content.RS3SkillsDialogue;
import com.rs.game.player.content.RS3SkillsDialogue.SkillDialogueResult;
import com.rs.game.player.dialogue.Dialogue;

public class ManufactureD extends Dialogue {

    @Override
    public void start() {
        RS3SkillsDialogue.sendSkillDialogueByProduce(player, 36389);
    }

    @Override
    public void run(int interfaceId, int componentId) {
        SkillDialogueResult result = RS3SkillsDialogue.getResult(player, componentId == RS3SkillsDialogue.CONTINUE_OPTION);
        if (componentId == RS3SkillsDialogue.CONTINUE_OPTION) {
            player.getActionManager().setAction(new Manufacture(result.getProduce(), result.getQuantity()));
            end();
        }
    }

    @Override
    public void finish() {

    }

}
