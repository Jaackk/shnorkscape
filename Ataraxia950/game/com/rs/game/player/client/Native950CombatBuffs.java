package com.rs.game.player.client;

import com.rs.game.Hit;
import com.rs.game.Entity;
import com.rs.game.WorldTile;
import com.rs.game.player.Player;
import java.util.*;
import java.util.function.BiConsumer;

/** World-thread authoritative lifetimes. Presentation never determines expiry. */
final class Native950CombatBuffs {
    enum Type {
        BERSERK(14707,33), DEATHS_SWIFTNESS(19251,50), SUNSHINE(19254,50),
        ANTICIPATION(14710,16), FREEDOM(14711,10), RESONANCE(14713,10),
        PREPARATION(14714,16), REFLECT(14716,16), DEBILITATE(14717,13),
        BARRICADE(14719,16), REJUVENATE(14720,16), IMMORTALITY(14721,50),
        NATURAL_INSTINCT(19252,34), DEVOTION(25028,16), REVENGE(14718,32), DIVERT(45045,10), CHAOS_ROAR(46279,12),
        LIVING_DEATH(48324,50), SEARING_WINDS(52799,10), SHADOW_IMBUED(52796,50), LIMITLESS(37203,10);
        final int structure,duration;
        Type(int structure,int duration){this.structure=structure;this.duration=duration;}
        static Type forStructure(int id){for(Type type:values())if(type.structure==id)return type;return null;}
    }
    private static final class State {
        long end;int stacks,bonusDamage;final long started;final WorldTile origin;final Entity source;
        State(long started,long end,WorldTile origin,Entity source){this.started=started;this.end=end;this.origin=new WorldTile(origin);this.source=source;}
    }
    private final Map<Player,EnumMap<Type,State>> states=new IdentityHashMap<>();
    private final Map<Player,boolean[]> immunity=new IdentityHashMap<>();
    void apply(Player player,Type type,long tick){
        apply(player,type,tick,type.duration,null);
    }
    void apply(Player player,Type type,long tick,int duration,Entity source){
        states.computeIfAbsent(player,p->new EnumMap<>(Type.class)).put(type,new State(tick,tick+duration,player,source));
        Native950CombatEffectUi.effect(player,type,duration);
        if(type==Type.ANTICIPATION||type==Type.FREEDOM){
            immunity.computeIfAbsent(player,p->new boolean[]{p.isStunImmune(),p.isFreezeImmune()});
            player.setStunImmune(true);
            if(type==Type.FREEDOM)player.setFreezeImmune(true);
        }
    }
    boolean consume(Player player,Type type,long tick){
        if(!active(player,type,tick))return false;
        states.get(player).remove(type);Native950CombatEffectUi.effect(player,type,0);return true;
    }
    void setBonusDamage(Player player,Type type,int amount){states.get(player).get(type).bonusDamage=Math.max(0,amount);}
    void extend(Player player,Type type,long tick,int duration){if(active(player,type,tick)){State state=states.get(player).get(type);state.end+=duration;Native950CombatEffectUi.effect(player,type,(int)(state.end-tick));}}
    void onKill(Player player,long tick){
        if(active(player,Type.DEVOTION,tick)){
            State state=states.get(player).get(Type.DEVOTION);
            state.end=Math.min(state.started+32,state.end+8);
            Native950CombatEffectUi.effect(player,Type.DEVOTION,(int)(state.end-tick),(int)(tick-state.started));
        }
    }
    void pulse(long tick){
        for(Player player:new ArrayList<>(states.keySet())){
            if(active(player,Type.REJUVENATE,tick)){
                State state=states.get(player).get(Type.REJUVENATE);
                if(tick>state.started)player.heal(Math.max(1,player.getMaxHitpoints()/40));
            }
        }
    }
    void checkEquipment(java.util.function.Predicate<Player> shield,BiConsumer<Player,Type> removed){
        for(Map.Entry<Player,EnumMap<Type,State>> player:states.entrySet()){
            if(shield.test(player.getKey()))continue;
            Iterator<Type> effects=player.getValue().keySet().iterator();
            while(effects.hasNext()){
                Type type=effects.next();
                if(Native950AbilityCatalog.get(type.structure).shieldRequired()){
                    effects.remove();Native950CombatEffectUi.effect(player.getKey(),type,0);removed.accept(player.getKey(),type);
                }
            }
        }
    }
    boolean active(Player player,Type type,long tick){
        Map<Type,State> buffs=states.get(player);State state=buffs==null?null:buffs.get(type);
        return state!=null&&tick<state.end&&!player.isDead()&&!player.hasFinished();
    }
    int outgoing(Player player,Hit.HitLook look,int damage,long tick){
        if(look==Hit.HitLook.RANGE_DAMAGE&&active(player,Type.SEARING_WINDS,tick))damage+=states.get(player).get(Type.SEARING_WINDS).bonusDamage;
        if(active(player,Type.REVENGE,tick))damage=scale(damage,100+20*states.get(player).get(Type.REVENGE).stacks);
        if(look==Hit.HitLook.MELEE_DAMAGE&&active(player,Type.BERSERK,tick))return scale(damage,175);
        Type area=look==Hit.HitLook.RANGE_DAMAGE?Type.DEATHS_SWIFTNESS:look==Hit.HitLook.MAGIC_DAMAGE?Type.SUNSHINE:null;
        if(area!=null&&active(player,area,tick)){
            WorldTile origin=states.get(player).get(area).origin;
            if(player.getPlane()==origin.getPlane()&&Math.max(Math.abs(player.getX()-origin.getX()),Math.abs(player.getY()-origin.getY()))<=3)
                return scale(damage,150);
        }
        return damage;
    }
    int incoming(Player player,int damage,long tick){return incoming(player,null,damage,tick);}
    void receivedAttack(Player player,long tick){
        if(active(player,Type.REVENGE,tick)){
            State state=states.get(player).get(Type.REVENGE);state.stacks=Math.min(10,state.stacks+1);
        }
    }
    int incoming(Player player,Entity source,int damage,long tick){
        if(active(player,Type.BARRICADE,tick))return 0;
        if(active(player,Type.BERSERK,tick))damage=scale(damage,125);
        if(active(player,Type.ANTICIPATION,tick))damage=scale(damage,90);
        if(active(player,Type.IMMORTALITY,tick))damage=scale(damage,75);
        if(active(player,Type.REFLECT,tick))damage=scale(damage,50);
        if(active(player,Type.DEBILITATE,tick)&&states.get(player).get(Type.DEBILITATE).source==source)damage=scale(damage,50);
        return damage;
    }
    int adrenalineGain(Player player,int gain,long tick){return active(player,Type.NATURAL_INSTINCT,tick)?gain*2:gain;}
    private static int scale(int damage,int percent){return (int)Math.min(Integer.MAX_VALUE,Math.max(0,(long)damage)*percent/100);}
    void remove(Player player,BiConsumer<Player,Type> removed){
        Map<Type,State> previous=states.remove(player);
        restoreImmunity(player,Long.MAX_VALUE);
        if(previous!=null)for(Type type:previous.keySet()){Native950CombatEffectUi.effect(player,type,0);removed.accept(player,type);}
    }
    void expire(long tick,BiConsumer<Player,Type> removed){
        Iterator<Map.Entry<Player,EnumMap<Type,State>>> players=states.entrySet().iterator();
        while(players.hasNext()){
            Map.Entry<Player,EnumMap<Type,State>> entry=players.next();
            Iterator<Map.Entry<Type,State>> buffs=entry.getValue().entrySet().iterator();
            while(buffs.hasNext()){
                Map.Entry<Type,State> effect=buffs.next();
                if(tick>=effect.getValue().end||entry.getKey().isDead()||entry.getKey().hasFinished()){
                    Type type=effect.getKey();buffs.remove();Native950CombatEffectUi.effect(entry.getKey(),type,0);removed.accept(entry.getKey(),type);
                }
            }
            restoreImmunity(entry.getKey(),tick);
            if(entry.getValue().isEmpty())players.remove();
        }
    }
    private void restoreImmunity(Player player,long tick){
        boolean[] original=immunity.get(player);if(original==null)return;
        boolean anticipation=active(player,Type.ANTICIPATION,tick),freedom=active(player,Type.FREEDOM,tick);
        player.setStunImmune(original[0]||anticipation||freedom);
        player.setFreezeImmune(original[1]||freedom);
        if(!anticipation&&!freedom)immunity.remove(player);
    }
    void clear(BiConsumer<Player,Type> removed){
        for(Player player:new ArrayList<>(states.keySet()))remove(player,removed);
    }
}
