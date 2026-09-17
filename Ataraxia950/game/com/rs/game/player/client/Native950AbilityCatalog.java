package com.rs.game.player.client;

import com.rs.cache.loaders.AnimationDefinitions;
import com.rs.cache.loaders.ItemDefinitions;
import com.rs.cache.loaders.rs3.RS3ClientScriptMap;
import com.rs.cache.loaders.rs3.RS3GeneralRequirementMap;
import com.rs.game.player.Player;
import java.util.*;

/** Paired950 definitions. A tooltip coefficient is not a verified server damage formula. */
final class Native950AbilityCatalog {
    enum Effect { STUN, EXECUTE, CRITICAL_BUFF, BURN, FLOW, MOVEMENT }
    static final class Definition {
        final int struct,book,key,skill,level,cooldown,minPercent,maxPercent;
        final String name;
        final Effect effect;
        Definition(int struct,int book,int key,String name,int skill,int level,int cooldown,int min,int max,Effect effect){
            this.struct=struct;this.book=book;this.key=key;this.name=name;this.skill=skill;this.level=level;
            this.cooldown=cooldown;minPercent=min;maxPercent=max;this.effect=effect;
        }
        boolean targetRequired(){return effect!=Effect.MOVEMENT;}
        int style(){return book==1?0:book==5?1:2;}
    }
    static final List<Definition> DEFINITIONS=Collections.unmodifiableList(Arrays.asList(
        new Definition(14682,1,3,"Backhand",0,31,25,95,105,Effect.STUN),
        new Definition(14700,1,9,"Punish",0,60,40,110,130,Effect.EXECUTE),
        new Definition(14701,1,10,"Fury",0,21,25,110,130,Effect.CRITICAL_BUFF),
        new Definition(14727,6,3,"Impact",6,31,25,65,75,Effect.STUN),
        new Definition(14729,6,5,"Combust",6,38,30,27,33,Effect.BURN),
        new Definition(19342,6,165,"Sonic Wave",6,6,25,90,110,Effect.FLOW),
        new Definition(14726,6,2,"Surge",16,5,34,0,0,Effect.MOVEMENT)));
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
    private Native950AbilityCatalog(){}
}
