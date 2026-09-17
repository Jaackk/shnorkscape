package com.rs.game.player.dialogue.impl;

import com.rs.game.player.dialogue.Dialogue;

public final class NoContinueNpcDialogue extends Dialogue {

    private final int npcId;
    private final String[] text;

    public NoContinueNpcDialogue(int npcId, String... text) {
        this.npcId = npcId;
        this.text = text;
    }

    @Override
    public void start() {
        Dialogue.sendNPCDialogueNoContinue(player, npcId, NORMAL, text);
    }

    @Override
    public void run(int interfaceId, int componentId) {

    }

    @Override
    public void finish() {

    }
}
