package com.rs.game.player.dialogue.impl;

import com.rs.game.player.dialogue.Dialogue;

import java.util.function.Consumer;

public final class SinglePlayerDialogue extends Dialogue {

    private final int animationId;
    private final String[] text;
    private final Consumer<Dialogue> action;

    public SinglePlayerDialogue(int animationId, Consumer<Dialogue> action, String... text) {
        this.animationId = animationId;
        this.action = action;
        this.text = text;
    }
    public SinglePlayerDialogue(int animationId,  String... text) {
        this(animationId, null, text);
    }
    @Override
    public void start() {
        sendPlayerDialogue(animationId, text);
    }

    @Override
    public void run(int interfaceId, int componentId) {
        if (action == null) {
            end();
        } else {
            action.accept(this);
        }
    }

    @Override
    public void finish() {

    }
}
