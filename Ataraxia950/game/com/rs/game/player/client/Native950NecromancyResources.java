package com.rs.game.player.client;

import com.rs.cache.Cache;
import com.rs.game.player.Player;
import java.util.IdentityHashMap;
import java.util.Map;

/** Session-owned resources. Exact950 CS17445/2660 read these varps for admission. */
final class Native950NecromancyResources {
    static final int NECROSIS_VAR=10986, SOULS_VAR=11035;
    private static final class State { int necrosis,souls; long lastCombat; }
    private final Map<Player,State> states=new IdentityHashMap<>();
    int necrosis(Player player){State s=states.get(player);return s==null?0:s.necrosis;}
    int souls(Player player){State s=states.get(player);return s==null?0:s.souls;}
    int fingerCost(Player player){return Math.max(0,60-10*Math.min(6,necrosis(player)));}
    String refusal(Player player,int struct){
        int minimum=struct==48299?1:struct==48301?2:0;
        return souls(player)<minimum?"That ability requires "+minimum+" residual soul"+(minimum==1?".":"s."):null;
    }
    // CS17459: equipment inventory94 slot5, item param8928 == passive struct48397.
    static int soulCap(Player player){
        int offhand=player.getEquipment().getShieldId();
        return Cache.STORE!=null&&offhand>=0&&Native950CacheItems.definition(offhand).getCSOpcode(8928)==48397?5:3;
    }
    void cast(Player player,int struct,long tick,boolean livingDeath){
        if(struct!=48296&&struct!=48297&&struct!=48298&&struct!=48299&&struct!=48301)return;
        State s=states.computeIfAbsent(player,p->new State());
        if(struct==48296)s.necrosis=Math.min(12,s.necrosis+4); // CS18658 + CS17458.
        if(struct==48297)s.necrosis=Math.max(0,s.necrosis-6); // CS18659 + CS17445.
        if(struct==48298)s.souls=Math.min(soulCap(player),s.souls+1); // CS18660.
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
            if(inCombat.test(player))s.lastCombat=tick;
            int before=s.souls;
            // Necrosis has no timer; residual souls expire after six seconds outside combat.
            s.souls=tick-s.lastCombat>=10?0:Math.min(s.souls,soulCap(player));
            if(before!=s.souls)publish(player,s);
        }
    }
    void clear(Player player){states.remove(player);publish(player,new State());}
    void clear(){for(Player player:new java.util.ArrayList<>(states.keySet()))clear(player);}
    private void publish(Player player,State s){
        player.getVarsManager().sendVar(NECROSIS_VAR,s.necrosis);
        player.getVarsManager().sendVar(SOULS_VAR,s.souls);
        Native950BugTest.event(player,"combat","necromancy-resources","necrosis",s.necrosis,"souls",s.souls);
    }
}
