package com.rs.game.player.dialogue.impl;

import com.rs.game.player.dialogue.Dialogue;

import java.util.function.Consumer;

/**
 * @author lare96 <http://github.com/lare96>
 */
public final class SingleNPCDialogue extends Dialogue {
    private Consumer<Dialogue> action;
    @Override
    public void start() {
        int npcId = (int) parameters[0];
        int animationId = (int) parameters[1];
        String[] messages = (String[]) parameters[2];
        if(parameters.length == 4) {
            action = (Consumer<Dialogue>) parameters[3];
        }
        sendNPCDialogue(npcId, animationId, messages);
    }

    @Override
    public void run(int interfaceId, int componentId) {
        if(action == null) {
            end();
        } else {
            action.accept(this);
        }
    }

    @Override
    public void finish() {

    }
}