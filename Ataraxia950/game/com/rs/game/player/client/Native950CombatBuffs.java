package com.rs.game.player.client;

import com.rs.game.Hit;
import com.rs.game.WorldTile;
import com.rs.game.player.Player;
import java.util.*;
import java.util.function.BiConsumer;

/** World-thread authoritative lifetimes. Presentation never determines expiry. */
final class Native950CombatBuffs {
    enum Type {
        BERSERK(14707,33), DEATHS_SWIFTNESS(19251,50);
        final int structure,duration;
        Type(int structure,int duration){this.structure=structure;this.duration=duration;}
    }
    private static final class State {
        final long end;final WorldTile origin;
        State(long end,WorldTile origin){this.end=end;this.origin=new WorldTile(origin);}
    }
    private final Map<Player,EnumMap<Type,State>> states=new IdentityHashMap<>();
    void apply(Player player,Type type,long tick){
        states.computeIfAbsent(player,p->new EnumMap<>(Type.class)).put(type,new State(tick+type.duration,player));
    }
    boolean active(Player player,Type type,long tick){
        Map<Type,State> buffs=states.get(player);State state=buffs==null?null:buffs.get(type);
        return state!=null&&tick<state.end&&!player.isDead()&&!player.hasFinished();
    }
    int outgoing(Player player,Hit.HitLook look,int damage,long tick){
        if(look==Hit.HitLook.MELEE_DAMAGE&&active(player,Type.BERSERK,tick))return scale(damage,175);
        if(look==Hit.HitLook.RANGE_DAMAGE&&active(player,Type.DEATHS_SWIFTNESS,tick)){
            WorldTile origin=states.get(player).get(Type.DEATHS_SWIFTNESS).origin;
            if(player.getPlane()==origin.getPlane()&&Math.max(Math.abs(player.getX()-origin.getX()),Math.abs(player.getY()-origin.getY()))<=3)
                return scale(damage,150);
        }
        return damage;
    }
    int incoming(Player player,int damage,long tick){return active(player,Type.BERSERK,tick)?scale(damage,125):damage;}
    private static int scale(int damage,int percent){return (int)Math.min(Integer.MAX_VALUE,Math.max(0,(long)damage)*percent/100);}
    void remove(Player player,BiConsumer<Player,Type> removed){
        Map<Type,State> previous=states.remove(player);
        if(previous!=null)for(Type type:previous.keySet())removed.accept(player,type);
    }
    void expire(long tick,BiConsumer<Player,Type> removed){
        Iterator<Map.Entry<Player,EnumMap<Type,State>>> players=states.entrySet().iterator();
        while(players.hasNext()){
            Map.Entry<Player,EnumMap<Type,State>> entry=players.next();
            Iterator<Map.Entry<Type,State>> buffs=entry.getValue().entrySet().iterator();
            while(buffs.hasNext()){
                Map.Entry<Type,State> effect=buffs.next();
                if(tick>=effect.getValue().end||entry.getKey().isDead()||entry.getKey().hasFinished()){
                    Type type=effect.getKey();buffs.remove();removed.accept(entry.getKey(),type);
                }
            }
            if(entry.getValue().isEmpty())players.remove();
        }
    }
    void clear(BiConsumer<Player,Type> removed){
        for(Player player:new ArrayList<>(states.keySet()))remove(player,removed);
    }
}
