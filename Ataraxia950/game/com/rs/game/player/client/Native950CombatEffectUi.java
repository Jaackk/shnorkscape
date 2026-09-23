package com.rs.game.player.client;

import com.rs.game.player.Player;
import com.rs.network.protocol.modern950.Native950Packets;

/** Native struct-driven buff/debuff display; authoritative lifetimes remain in combat owners. */
final class Native950CombatEffectUi {
    private Native950CombatEffectUi() { }
    static int buff(Native950CombatBuffs.Type type) {
        switch(type) {
            case BERSERK:return 52793;
            case LIVING_DEATH:return 48339;
            case REVENGE:return 3636;
            case LIMITLESS:return 37214;
            case SEARING_WINDS:return 52801;
            case SHADOW_IMBUED:return 52802;
            default:return type.structure;
        }
    }
    static void effect(Player player,Native950CombatBuffs.Type type,int ticks) {effect(player,type,ticks,0);}
    static void effect(Player player,Native950CombatBuffs.Type type,int ticks,int elapsed) {
        if(player.getRealChannel()==null)return;
        if(type==Native950CombatBuffs.Type.LIVING_DEATH)
            player.getVarsManager().sendVar(11059,ticks>0?type.structure:0);
        if(type==Native950CombatBuffs.Type.ANTICIPATION||type==Native950CombatBuffs.Type.DEVOTION){
            toggle(player,buff(type),false);
            // CS9379 measures from CS6570 activation time, so extensions include elapsed ticks.
            if(ticks>0){player.getRealChannel().write(Native950Packets.runClientScript(9379,type.structure,0,ticks+elapsed));toggle(player,buff(type),true);}
        }else timed(player,buff(type),ticks);
    }
    static void timed(Player player,int structure,int ticks) {
        if(player.getRealChannel()==null)return;
        // Rebuild only this effect when its authoritative end changes; no workspace or bar reset.
        toggle(player,structure,false);
        if(ticks>0){
            player.getRealChannel().write(Native950Packets.runClientScript(4252,structure,ticks));
            toggle(player,structure,true);
        }
    }
    static void toggle(Player player,int structure,boolean active) {
        if(player.getRealChannel()!=null)player.getRealChannel().write(Native950Packets.runClientScript(10624,structure,active?1:0));
    }
}
