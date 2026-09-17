package com.rs.game.player.client;

import com.rs.cache.loaders.AnimationDefinitions;
import com.rs.cache.loaders.ItemDefinitions;
import com.rs.cache.loaders.rs3.RS3ClientScriptMap;
import com.rs.cache.loaders.rs3.RS3GeneralRequirementMap;
import com.rs.game.player.Player;
import java.util.*;

/** Paired950 definitions. A tooltip coefficient is not a verified server damage formula. */
final class Native950AbilityCatalog {
    enum Effect { DIRECT, STUN, EXECUTE, BLEED, FLOW, MOVEMENT }
    static final class Definition {
        final int struct,book,key,skill,level,cooldown,minPercent,maxPercent,tier,hits;
        final String name;
        final Effect effect;
        final boolean offhandRequired,twoHandedRequired;
        Definition(int struct,int book,int key,String name,int skill,int level,int cooldown,int min,int max,
                   int tier,int hits,Effect effect,boolean offhandRequired,boolean twoHandedRequired){
            this.struct=struct;this.book=book;this.key=key;this.name=name;this.skill=skill;this.level=level;
            this.cooldown=cooldown;minPercent=min;maxPercent=max;this.tier=tier;this.hits=hits;this.effect=effect;
            this.offhandRequired=offhandRequired;this.twoHandedRequired=twoHandedRequired;
        }
        boolean targetRequired(){return effect!=Effect.MOVEMENT;}
        int style(){return book==1?0:book==5?1:2;}
        int adrenalineCost(){return tier==2?15:tier==4?100:0;}
        int adrenalineGain(){return tier==1?8:0;}
    }
    static final List<Definition> DEFINITIONS=Collections.unmodifiableList(Arrays.asList(
        // The tier, level, cooldown, hand requirements and placement below are cache-verified.
        // Coefficients are deliberately conservative Combat Alpha values, not retail damage claims.
        d(14678,1,1,"Barge",0,65,34,100,120,1,1,Effect.DIRECT),
        d(14679,1,2,"Adaptive Strike",0,7,9,90,110,1,1,Effect.DIRECT),
        d(14682,1,3,"Backhand",0,31,25,95,105,1,1,Effect.STUN),
        d(14684,1,4,"Flurry",0,45,34,45,55,2,3,Effect.DIRECT,true,false),
        d(14686,1,5,"Overpower",0,15,50,180,220,4,1,Effect.DIRECT),
        d(14688,1,6,"Meteor Strike",0,90,100,240,280,4,1,Effect.DIRECT),
        d(44244,1,8,"Dismember",0,50,40,70,85,2,1,Effect.BLEED),
        d(14700,1,9,"Punish",0,60,40,110,130,1,1,Effect.EXECUTE),
        d(14701,1,10,"Fury",0,21,25,105,125,1,3,Effect.DIRECT),
        d(14704,1,11,"Assault",0,3,10,55,70,2,4,Effect.DIRECT),
        d(14685,1,12,"Hurricane",0,37,34,65,80,2,2,Effect.DIRECT,false,true),
        d(14707,1,13,"Berserk",0,76,100,220,260,4,1,Effect.DIRECT),

        d(14663,5,1,"Piercing Shot",4,13,5,90,110,1,1,Effect.DIRECT),
        d(14664,5,2,"Binding Shot",4,31,25,90,100,1,1,Effect.STUN),
        d(14666,5,4,"Snipe",4,7,100,150,180,2,1,Effect.DIRECT),
        d(14668,5,5,"Ricochet",4,67,17,85,105,1,1,Effect.DIRECT),
        d(14669,5,6,"Snap Shot",4,2,0,70,90,2,2,Effect.DIRECT),
        d(14670,5,7,"Rapid Fire",4,62,34,45,55,2,4,Effect.DIRECT),
        d(14674,5,9,"Deadshot",4,21,50,210,250,4,1,Effect.BLEED),
        d(19251,5,10,"Death's Swiftness",4,76,100,220,260,4,1,Effect.DIRECT),

        d(14727,6,3,"Impact",6,31,25,65,75,1,1,Effect.STUN),
        d(14728,6,4,"Chain",6,51,17,90,110,1,1,Effect.DIRECT),
        d(14729,6,5,"Combust",6,38,30,45,55,1,1,Effect.BLEED),
        d(14730,6,6,"Dragon Breath",6,19,12,100,120,1,1,Effect.DIRECT),
        d(14731,6,7,"Asphyxiate",6,59,34,45,55,2,4,Effect.DIRECT),
        d(14733,6,9,"Wild Magic",6,3,9,75,95,2,2,Effect.DIRECT),
        d(14735,6,10,"Tsunami",6,90,100,230,270,4,1,Effect.DIRECT),
        d(14736,6,11,"Omnipower",6,12,50,65,80,4,4,Effect.DIRECT),
        d(19342,6,165,"Sonic Wave",6,6,25,90,110,1,1,Effect.FLOW),
        d(19343,6,166,"Concentrated Blast",6,66,9,70,90,1,3,Effect.DIRECT),
        d(14726,6,2,"Surge",16,5,34,0,0,1,0,Effect.MOVEMENT)));
    private static Definition d(int struct,int book,int key,String name,int skill,int level,int cooldown,int min,int max,
                                int tier,int hits,Effect effect){return d(struct,book,key,name,skill,level,cooldown,min,max,tier,hits,effect,false,false);}
    private static Definition d(int struct,int book,int key,String name,int skill,int level,int cooldown,int min,int max,
                                int tier,int hits,Effect effect,boolean offhand,boolean twoHanded){
        return new Definition(struct,book,key,name,skill,level,cooldown,min,max,tier,hits,effect,offhand,twoHanded);
    }
    static Definition get(int struct){for(Definition d:DEFINITIONS)if(d.struct==struct)return d;return null;}
    static void verify(){
        for(Definition d:DEFINITIONS){
            RS3GeneralRequirementMap s=RS3GeneralRequirementMap.getMap(d.struct);
            if(!d.name.equals(s.getStringValue(2794))||s.getIntValue(2793)!=d.key
                ||s.getIntValue(2807)!=d.level||s.getIntValue(2796)!=d.cooldown
                ||Native950ActionBar.struct(Native950ActionBar.pack(d.book,d.key))!=d.struct)
                throw new IllegalStateException("Changed950 ability definition: "+d.name);
        }
    }
    static int animation(Player p,int struct){
        RS3GeneralRequirementMap d=RS3GeneralRequirementMap.getMap(struct);
        int enumId=d.getIntValue(2915);
        if(enumId<=0)return -1;
        ItemDefinitions weapon=p.getEquipment().getWeaponId()<0?null:Native950CacheItems.definition(p.getEquipment().getWeaponId());
        int family=weapon==null?0:weapon.getCSOpcode(686);
        int id=RS3ClientScriptMap.getMap(enumId).getIntValue(family);
        if(id<0)return -1;
        AnimationDefinitions seq=AnimationDefinitions.getAnimationDefinitions(id);
        return seq.decodeFailure==null?id:-1;
    }
    static int sequenceParam(int animation,int param){
        Map<Integer,Object> values=AnimationDefinitions.getAnimationDefinitions(animation).clientScriptData;
        Object value=values==null?null:values.get(param);return value instanceof Integer?(Integer)value:-1;
    }
    /** Mirrors Entity#setNextAnimation's cache-frame duration in the native world's 600ms ticks. */
    static int animationTicks(int animation){
        if(animation<0)return 1;
        return animationTicksForMillis(AnimationDefinitions.getAnimationDefinitions(animation).getEmoteTime());
    }
    static int animationTicksForMillis(int millis){return Math.max(1,(Math.max(0,millis)+599)/600);}
    static int secondaryHitDelay(int animationTicks,int hitIndex,int hits){
        if(hitIndex<1||hitIndex>=hits)throw new IllegalArgumentException("Invalid secondary hit index");
        return Math.max(1,(animationTicks*hitIndex+hits-1)/hits);
    }
    static int targetGraphic(int struct){
        if(com.rs.cache.Cache.STORE==null)return -1;
        return RS3GeneralRequirementMap.getMap(struct).getIntValue(2933);
    }
    private Native950AbilityCatalog(){}
}
