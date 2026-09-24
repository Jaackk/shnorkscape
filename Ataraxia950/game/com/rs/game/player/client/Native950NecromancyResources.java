package com.rs.game.player.client;

import com.rs.cache.Cache;
import com.rs.game.player.Player;
import java.util.IdentityHashMap;
import java.util.Map;

/** Session-owned resources. Exact950 CS17445/2660 read these varps for admission. */
final class Native950NecromancyResources {
    static final int NECROSIS_VAR=10986, SOULS_VAR=11035;
    private static final class State { int necrosis,souls,scythe; long lastCombat,scytheUntil; boolean engaged; }
    private final Map<Player,State> states=new IdentityHashMap<>();
    void combatActivity(Player player,long tick){State s=states.get(player);if(s!=null)s.lastCombat=tick;}
    int necrosis(Player player){State s=states.get(player);return s==null?0:s.necrosis;}
    int souls(Player player){State s=states.get(player);return s==null?0:s.souls;}
    int fingerCost(Player player){return Math.max(0,60-10*Math.min(6,necrosis(player)));}
    int effective(Player player,int struct){State s=states.get(player);return struct==48311&&s!=null&&s.scythe>0?48311+s.scythe:struct;}
    void gainSoul(Player player,long tick){State s=states.computeIfAbsent(player,p->new State());s.souls=Math.max(s.souls,Math.min(soulCap(player),s.souls+1));s.lastCombat=tick;publish(player,s);}
    String refusal(Player player,int struct){
        if((struct==48312||struct==48313)&&effective(player,48311)!=struct)return "That Spectral Scythe recast is no longer available.";
        int minimum=struct==48299?1:struct==48301?2:0;
        return souls(player)<minimum?"That ability requires "+minimum+" residual soul"+(minimum==1?".":"s."):null;
    }
    // CS17459: equipment inventory94 slot5, item param8928 == passive struct48397.
    static int soulCap(Player player){
        int offhand=player.getEquipment().getShieldId();
        return Cache.STORE!=null&&offhand>=0&&Native950CacheItems.definition(offhand).getCSOpcode(8928)==48397?5:3;
    }
    void cast(Player player,int struct,long tick,boolean livingDeath){
        if(struct>=48311&&struct<=48313){
            State s=states.computeIfAbsent(player,p->new State());s.scythe=struct==48313?0:struct-48310;s.scytheUntil=tick+25;s.lastCombat=tick;publishScythe(player,s);return;
        }
        if(struct!=48296&&struct!=48297&&struct!=48298&&struct!=48299&&struct!=48301)return;
        State s=states.computeIfAbsent(player,p->new State());
        if(struct==48296)s.necrosis=Math.min(12,s.necrosis+4); // CS18658 + CS17458.
        if(struct==48297)s.necrosis=Math.max(0,s.necrosis-6); // CS18659 + CS17445.
        if(struct==48298)s.souls=Math.max(s.souls,Math.min(soulCap(player),s.souls+1)); // CS18660.
        if(struct==48299)s.souls--;
        if(struct==48301)s.souls=0;
        s.lastCombat=tick;
        publish(player,s);
    }
    void basicAttack(Player player,long tick,boolean livingDeath){
        if(!livingDeath)return;
        State s=states.computeIfAbsent(player,p->new State());
        s.necrosis=Math.min(12,s.necrosis+2);s.lastCombat=tick;publish(player,s); // CS18671.
    }
    void pulse(long tick){pulse(tick,p->false);}
    void pulse(long tick,java.util.function.Predicate<Player> inCombat){
        for(Player player:new java.util.ArrayList<>(states.keySet())){
            State s=states.get(player);
            boolean engaged=inCombat.test(player);
            if(engaged||s.engaged)s.lastCombat=tick; // Start the grace period at actual combat exit.
            s.engaged=engaged;
            if(s.scythe>0&&(tick>=s.scytheUntil||player.isDead()||player.hasFinished())){s.scythe=0;publishScythe(player,s);}
            int before=s.souls;
            // Necrosis has no timer; residual souls expire after six seconds outside combat.
            // SHNORKSCAPE rule: equipment swaps never discard earned souls, even above the new gain cap.
            s.souls=tick-s.lastCombat>=10?0:s.souls;
            if(before!=s.souls){
                Native950BugTest.event(player,"combat","soul-count-lifecycle","before",before,"after",s.souls,"tick",tick,"lastCombatTick",s.lastCombat,"reason","out-of-combat-expiry");
                publish(player,s);
            }
            if(s.souls>0&&player.getRealChannel()!=null)player.getRealChannel().write(
                com.rs.network.protocol.modern950.Native950Packets.runClientScript(4252,48334,(int)Math.max(0,10-(tick-s.lastCombat))));
        }
    }
    void clear(Player player){State prior=states.remove(player);publish(player,new State());if(prior!=null&&prior.scythe!=0)publishScythe(player,new State());}
    void clear(){for(Player player:new java.util.ArrayList<>(states.keySet()))clear(player);}
    private void publishScythe(Player p,State s){
        p.getVarsManager().sendVar(11051,s.scythe==1?1:0);p.getVarsManager().sendVar(11054,s.scythe==2?1:0);
        p.getNative950ActionBar().refreshTransforms(p);
    }
    private void publish(Player player,State s){
        int priorNecrosis=player.getVarsManager().getValue(NECROSIS_VAR);
        int priorSouls=player.getVarsManager().getValue(SOULS_VAR);
        player.getVarsManager().sendVar(NECROSIS_VAR,s.necrosis);
        player.getVarsManager().sendVar(SOULS_VAR,s.souls);
        if(s.souls>0||player.getNative950SoulVisual()>=0)player.setNative950SoulVisual(s.souls);
        if((priorNecrosis>0)!=(s.necrosis>0))Native950CombatEffectUi.toggle(player,48333,s.necrosis>0);
        if(s.souls>0&&player.getRealChannel()!=null)player.getRealChannel().write(
            com.rs.network.protocol.modern950.Native950Packets.runClientScript(4252,48334,10));
        if((priorSouls>0)!=(s.souls>0))Native950CombatEffectUi.toggle(player,48334,s.souls>0);
        Native950BugTest.event(player,"combat","necromancy-resources","necrosis",s.necrosis,"souls",s.souls);
    }
}
