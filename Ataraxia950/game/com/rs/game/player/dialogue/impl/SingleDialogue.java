package com.rs.game.player.dialogue.impl;

import com.rs.game.player.dialogue.Dialogue;

import java.util.function.Consumer;

/**
 * @author lare96 <http://github.com/lare96>
 */
public class SingleDialogue extends Dialogue {

    private final Consumer<Dialogue> action;
    private final String[] text;

    public SingleDialogue(Consumer<Dialogue> action, String[] text) {
        this.action = action;
        this.text = text;
    }

    @Override
    public void start() {
        sendDialogue(player, text);
    }

    @Override
    public void run(int interfaceId, int componentId) {
        action.accept(this);
    }

    @Override
    public void finish() {

    }
}