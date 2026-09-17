package com.rs.game.player.client;

import com.rs.game.player.dialogue.Dialogue;

/** Normal tutor, ring and entrance routes use the existing verified native choice dialogue. */
public final class Native950DungeoneeringDialogue extends Dialogue {
    private boolean choosing;
    @Override public void start() {
        if (!Native950Dungeoneering.usable(player)) { end(); return; }
        choosing = true;
        sendOptionsDialogue("Dungeoneering", "Start a solo dungeon / travel to Daemonheim.",
                "Show my progress and tokens.", "Leave my current dungeon.", "Nothing, thanks.",
                "Start a three-wave frozen challenge (level 20).");
    }
    @Override public void run(int interfaceId, int componentId) {
        if (!choosing || !Native950Dungeoneering.usable(player)) { end(); return; }
        choosing = false;
        if (componentId == OPTION_1) {
            if (!Native950Dungeoneering.lobby(player)) { end(); Native950Dungeoneering.travel(player); }
            else { String result = Native950Dungeoneering.start(player); end(); player.sendMessage(result); }
        } else if (componentId == OPTION_2) sendDialogue(Native950Dungeoneering.status(player));
        else if (componentId == OPTION_3) { end(); Native950Dungeoneering.exit(player, false); }
        else if(componentId==OPTION_5){String result=Native950Dungeoneering.startChallenge(player);end();player.sendMessage(result);}
        else end();
    }
    @Override public void finish() { choosing = false; }
}
