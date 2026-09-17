package com.rs.game.player.client;
import com.rs.game.npc.NPC;
import com.rs.game.player.dialogue.Dialogue;
/** Existing verified dialogue widgets expose owner-only companion controls until the full panel is ported. */
public final class Native950FamiliarDialogue extends Dialogue {
    private final NPC expected;private int page;private boolean choosing;
    public Native950FamiliarDialogue(NPC npc){expected=npc;}
    @Override public void start(){if(!Native950Familiars.controls(player,expected)){end();return;}choosing=true;sendOptionsDialogue("Your familiar","Check time and points.","Call familiar.","Renew using another pouch.","Dismiss familiar.","Close.");}
    @Override public void run(int interfaceId,int componentId){
        if(!choosing||!Native950Familiars.controls(player,expected)){end();return;}choosing=false;
        if(page==1){end();if(componentId==OPTION_1)Native950Familiars.dismiss(player,expected);return;}
        if(page==2){end();if(componentId==OPTION_1)player.sendMessage(Native950Familiars.renew(player,expected));return;}
        if(componentId==OPTION_1){end();player.sendMessage(Native950Familiars.status(player));}
        else if(componentId==OPTION_2){end();player.sendMessage(Native950Familiars.recall(player,expected));}
        else if(componentId==OPTION_3){page=2;choosing=true;sendOptionsDialogue("Consume another matching pouch to renew?","Yes, renew my familiar.","No, keep my pouch.");}
        else if(componentId==OPTION_4){page=1;choosing=true;sendOptionsDialogue("Dismiss your familiar? The pouch will not return.","Yes, dismiss it.","No, keep my familiar.");}
        else end();
    }
    @Override public void finish(){choosing=false;}
}
