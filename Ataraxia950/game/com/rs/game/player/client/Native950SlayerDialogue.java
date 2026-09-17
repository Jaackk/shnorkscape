package com.rs.game.player.client;

import com.rs.game.npc.NPC;
import com.rs.game.player.dialogue.Dialogue;

/** Minimal master conversation through the already verified native dialogue presentation. */
public final class Native950SlayerDialogue extends Dialogue {
    private final NPC master;
    private final boolean quickAssignment;
    private boolean options;
    private int page;
    public Native950SlayerDialogue(NPC master, boolean quickAssignment) {
        this.master = master; this.quickAssignment = quickAssignment;
    }
    @Override public void start() {
        if (!Native950Slayer.nearMaster(player, master)) { end(); return; }
        if (quickAssignment) { sendDialogue(Native950Slayer.assign(player, master)); return; }
        options = true;
        page=0;
        sendOptionsDialogue("Slayer master", "I need an assignment.", "How is my task progressing?", "Rewards and task management.", "Nothing, thanks.");
    }
    @Override public void run(int interfaceId, int componentId) {
        if (!Native950Slayer.nearMaster(player, master)) { end(); return; }
        if (!options) { end(); return; }
        options = false;
        if(page==1){
            if(componentId==OPTION_1){page=2;options=true;sendOptionsDialogue("Cancel current task for 30 points?","Yes, preserve my streak and cancel.","No, keep my task.");}
            else if(componentId==OPTION_2){page=3;options=true;sendOptionsDialogue("Buy 10,000 base Slayer XP for 400 points?","Yes, buy the experience.","No, keep my points.");}
            else end();
            return;
        }
        if(page==2||page==3){if(componentId==OPTION_1)sendDialogue(page==2?Native950Slayer.cancelTask(player,master):Native950Slayer.buyExperience(player,master));else end();return;}
        if (componentId == OPTION_1) sendDialogue(Native950Slayer.assign(player, master));
        else if (componentId == OPTION_2) sendDialogue(player.getNative950Slayer().description()
                + " Completed tasks: " + player.getNative950Slayer().completed()
                + ". Slayer points: " + player.getNative950Slayer().points() + ".");
        else if(componentId==OPTION_3){page=1;options=true;sendOptionsDialogue("Slayer points: "+player.getNative950Slayer().points(),"Cancel current task (30 points).","Buy 10,000 Slayer XP (400 points; level 35).","Nothing, thanks.");}
        else end();
    }
    @Override public void finish() { options = false; }
}
