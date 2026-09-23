package com.rs.game.player.client;

import com.rs.cache.loaders.AnimationDefinitions;
import com.rs.cache.loaders.ItemDefinitions;
import com.rs.cache.loaders.rs3.RS3ClientScriptMap;
import com.rs.cache.loaders.rs3.RS3GeneralRequirementMap;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import java.util.*;

/** Paired950 definitions. A tooltip coefficient is not a verified server damage formula. */
final class Native950AbilityCatalog {
    enum Effect { DIRECT, STUN, EXECUTE, BLEED, BUFF, FLOW, MOVEMENT, PROVOKE, CEASE, FOOD, CONJURE, COMMAND }
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
        boolean targetRequired(){return effect!=Effect.CONJURE&&effect!=Effect.COMMAND&&struct!=48305&&effect!=Effect.MOVEMENT&&effect!=Effect.BUFF&&effect!=Effect.CEASE&&effect!=Effect.FOOD;}
        boolean revolutionEligible(){return effect!=Effect.MOVEMENT&&book!=3&&book!=4;}
        int style(){return book==1?0:book==5?1:book==6?2:book==7?3:-1;}
        boolean shieldRequired(){return struct==14713||struct==14714||struct==14715||struct==14716
                ||struct==14718||struct==14719||struct==14720||struct==14721||struct==45045;}
        // Paired 950 params 2798/2800 use tenths of one percent. Tier 2 is
        // enhanced, not the pre-modernisation 50%-admission/15%-cost threshold.
        int adrenalineCost(){
            if(Native950Conjures.handles(struct))return 0;
            if(struct==28177||struct==28180)return 0;
            if(struct==52796)return 40;
            if(struct==48299||struct==48301||struct==48309)return 0;
            if(struct==44244||struct==14666)return 0;
            if(struct==31985||struct==31986||struct==48308)return 20;
            if(struct==48311)return 10;
            if(struct==48312)return 20;
            if(struct==48313)return 30;
            if(struct==14686||struct==14688||struct==14674||struct==14736||struct==14709||struct==48297||struct==48314)return 60;
            return tier==2?25:tier==3?15:tier==4?100:0;
        }
        int adrenalineRequired(){return tier==3?50:adrenalineCost();}
        int adrenalineGain(){return struct==14679?12:tier==1?9:0;}
        boolean channelled(){return struct==14684||struct==14701||struct==14704||struct==14666||struct==14670||struct==14731||struct==19343||struct==48309||struct==28180;}
        int hitSpacing(){return struct==14684||struct==14670||struct==19343?1:2;}
        /** EOC effect cadence, never inferred from a sequence's optional legacy frame table. */
        int hitDelay(int hit){
            if(hit<0||hit>=hits)throw new IllegalArgumentException("Invalid hit index");
            if(struct==14666)return 2; // Paired channel interval plus the 1.8-second wind-up.
            if(struct==14701)return hit==2?5:hit*2;
            if(channelled())return hit*hitSpacing();
            return hit;
        }
        int channelTicks(){return channelled()?hitDelay(hits-1)+1:0;}
    }
    static final List<Definition> DEFINITIONS=Collections.unmodifiableList(Arrays.asList(
        // The tier, level, cooldown, hand requirements and placement below are cache-verified.
        // Coefficients are deliberately conservative Combat Alpha values, not retail damage claims.
        d(14678,1,1,"Barge",0,65,34,100,120,1,1,Effect.DIRECT),
        d(14679,1,2,"Adaptive Strike",0,7,9,90,110,1,1,Effect.DIRECT),
        d(14682,1,3,"Backhand",0,31,25,95,105,1,1,Effect.STUN),
        d(14684,1,4,"Flurry",0,45,34,45,55,2,8,Effect.DIRECT,true,false),
        d(14686,1,5,"Overpower",0,15,50,180,220,4,1,Effect.DIRECT),
        d(14688,1,6,"Meteor Strike",0,90,100,240,280,4,1,Effect.DIRECT),
        d(44244,1,8,"Dismember",0,50,40,70,85,2,1,Effect.BLEED),
        d(14700,1,9,"Punish",0,60,40,110,130,1,1,Effect.EXECUTE),
        d(14701,1,10,"Fury",0,21,25,105,125,1,3,Effect.DIRECT),
        d(14704,1,11,"Assault",0,3,10,55,70,2,4,Effect.DIRECT),
        d(14685,1,12,"Hurricane",0,37,34,65,80,2,2,Effect.DIRECT,false,true),
        d(14707,1,13,"Berserk",0,76,100,0,0,4,0,Effect.BUFF),

        d(14663,5,1,"Piercing Shot",4,13,5,45,55,1,2,Effect.DIRECT),
        d(14664,5,2,"Binding Shot",4,31,25,90,100,1,1,Effect.STUN),
        d(14666,5,4,"Snipe",4,7,100,150,180,2,1,Effect.DIRECT),
        d(14668,5,5,"Ricochet",4,67,17,85,105,1,1,Effect.DIRECT),
        d(14669,5,6,"Snap Shot",4,2,0,70,90,2,2,Effect.DIRECT),
        d(14670,5,7,"Rapid Fire",4,62,34,45,55,2,8,Effect.DIRECT),
        d(14674,5,9,"Deadshot",4,21,50,115,115,4,4,Effect.DIRECT),
        d(19251,5,10,"Death's Swiftness",4,76,100,0,0,4,0,Effect.BUFF),
        d(52799,5,11,"Galeshot",4,58,34,90,110,1,1,Effect.DIRECT),
        d(28177,5,12,"Shadow Tendrils",4,75,75,200,240,2,1,Effect.DIRECT),
        d(52796,5,19,"Imbue: Shadows",4,90,100,0,0,2,0,Effect.BUFF),

        d(14727,6,3,"Impact",6,31,25,65,75,1,1,Effect.STUN),
        d(14728,6,4,"Chain",6,51,17,90,110,1,1,Effect.DIRECT),
        d(14729,6,5,"Combust",6,38,30,45,55,1,1,Effect.BLEED),
        d(14730,6,6,"Dragon Breath",6,19,12,100,120,1,1,Effect.DIRECT),
        d(14731,6,7,"Asphyxiate",6,59,34,45,55,2,4,Effect.DIRECT),
        d(14733,6,9,"Wild Magic",6,3,9,75,95,2,2,Effect.DIRECT),
        d(14735,6,10,"Tsunami",6,90,100,230,270,4,1,Effect.DIRECT),
        d(14736,6,11,"Omnipower",6,12,50,460,460,4,1,Effect.DIRECT),
        d(19342,6,165,"Sonic Wave",6,6,25,90,110,1,1,Effect.FLOW),
        d(19343,6,166,"Concentrated Blast",6,66,9,70,90,1,3,Effect.DIRECT),
        d(28180,6,169,"Smoke Tendrils",6,75,75,65,80,2,4,Effect.DIRECT),
        d(14726,6,2,"Surge",16,5,34,0,0,7,0,Effect.MOVEMENT),
        d(47129,1,7,"Dive",16,30,34,0,0,7,0,Effect.MOVEMENT),
        d(1488,1,29,"Bladed Dive",0,65,34,75,95,1,1,Effect.MOVEMENT,true,false),
        d(14665,5,3,"Escape",16,5,34,0,0,7,0,Effect.MOVEMENT),
        d(19254,6,164,"Sunshine",6,76,100,0,0,4,0,Effect.BUFF),
        d(14709,1,14,"Pulverise",0,71,100,240,280,4,1,Effect.DIRECT,false,true),
        d(14671,5,8,"Bombardment",4,36,0,100,120,2,1,Effect.DIRECT),
        d(31985,6,172,"Corruption Blast",6,70,25,70,90,2,1,Effect.BLEED),
        d(31986,5,13,"Corruption Shot",4,70,25,70,90,2,1,Effect.BLEED),
        d(52781,1,16,"Rend",0,18,17,90,110,1,1,Effect.DIRECT),
        d(14710,3,1,"Anticipation",1,3,41,0,0,1,0,Effect.BUFF),
        d(14711,3,2,"Freedom",1,34,50,0,0,1,0,Effect.BUFF),
        d(14712,3,3,"Provoke",1,24,17,0,0,1,0,Effect.PROVOKE),
        d(45340,3,18,"Cease",1,1,0,0,0,7,0,Effect.CEASE),
        d(14713,3,4,"Resonance",1,48,50,0,0,1,0,Effect.BUFF),
        d(14714,3,5,"Preparation",1,67,34,0,0,1,0,Effect.BUFF),
        d(14715,3,6,"Bash",1,8,25,80,100,1,1,Effect.DIRECT),
        d(14716,3,7,"Reflect",1,37,50,0,0,3,0,Effect.BUFF),
        d(14717,3,8,"Debilitate",1,55,50,20,100,3,1,Effect.DIRECT),
        d(14719,3,10,"Barricade",1,81,100,0,0,4,0,Effect.BUFF),
        d(14720,3,11,"Rejuvenate",1,52,500,0,0,4,0,Effect.BUFF),
        d(14721,3,12,"Immortality",1,29,200,0,0,4,0,Effect.BUFF),
        d(19252,3,13,"Natural Instinct",1,85,200,0,0,4,0,Effect.BUFF),
        d(25028,3,14,"Devotion",1,1,100,0,0,3,0,Effect.BUFF),
        d(24188,4,10,"Sacrifice",3,1,50,80,100,1,1,Effect.DIRECT),
        d(37203,4,38,"Limitless",3,1,150,0,0,7,0,Effect.BUFF),
        d(44225,4,41,"Eat Food",3,0,0,0,0,7,0,Effect.FOOD),
        d(14718,3,9,"Revenge",1,15,75,0,0,3,0,Effect.BUFF),
        d(45045,3,17,"Divert",1,48,50,0,0,1,0,Effect.BUFF),
        d(46279,1,15,"Chaos Roar",0,92,100,0,0,1,0,Effect.BUFF),

        // Exact cache950 enum16973 entries, sharing the authoritative resource owner.
        d(48302,7,4,"Conjure Skeleton Warrior",28,2,0,0,0,2,0,Effect.CONJURE),
        d(48303,7,4,"Command Skeleton Warrior",28,20,25,0,0,2,0,Effect.COMMAND),
        d(48304,7,8,"Conjure Putrid Zombie",28,40,50,0,0,2,0,Effect.CONJURE),
        d(48305,7,8,"Command Putrid Zombie",28,60,0,0,0,2,0,Effect.COMMAND),
        d(48306,7,9,"Conjure Vengeful Ghost",28,40,0,0,0,2,0,Effect.CONJURE),
        d(48307,7,9,"Command Vengeful Ghost",28,60,0,0,0,2,0,Effect.COMMAND),
        d(31820,7,16,"Conjure Phantom Guardian",28,70,0,0,0,2,0,Effect.CONJURE),
        d(32342,7,16,"Command Phantom Guardian",28,80,15,0,0,2,0,Effect.COMMAND),
        d(33965,7,15,"Conjure Undead Army",28,99,0,0,0,2,0,Effect.CONJURE),
        d(48296,7,2,"Touch of Death",Skills.NECROMANCY,13,24,90,110,1,1,Effect.DIRECT),
        d(48297,7,3,"Finger of Death",Skills.NECROMANCY,8,0,270,330,2,1,Effect.DIRECT),
        d(48298,7,5,"Soul Sap",Skills.NECROMANCY,54,9,90,110,1,1,Effect.DIRECT),
        d(48299,7,6,"Soul Strike",Skills.NECROMANCY,54,0,135,165,2,1,Effect.STUN),
        d(48301,7,7,"Volley of Souls",Skills.NECROMANCY,66,0,135,165,2,1,Effect.DIRECT),
        d(48309,7,11,"Blood Siphon",Skills.NECROMANCY,36,75,22,28,2,5,Effect.DIRECT),
        d(48324,7,14,"Living Death",Skills.NECROMANCY,76,150,0,0,4,0,Effect.BUFF),
        d(48308,7,10,"Bloat",Skills.NECROMANCY,48,0,60,75,2,1,Effect.BLEED),
        d(48311,7,12,"Spectral Scythe",Skills.NECROMANCY,62,25,72,88,2,1,Effect.DIRECT),
        d(48312,7,12,"Spectral Scythe",Skills.NECROMANCY,62,0,180,220,2,1,Effect.DIRECT),
        d(48313,7,12,"Spectral Scythe",Skills.NECROMANCY,62,0,225,275,2,1,Effect.DIRECT),
        d(48314,7,13,"Death Skulls",Skills.NECROMANCY,28,100,225,275,4,1,Effect.DIRECT)));
    private static Definition d(int struct,int book,int key,String name,int skill,int level,int cooldown,int min,int max,
                                int tier,int hits,Effect effect){return d(struct,book,key,name,skill,level,cooldown,min,max,tier,hits,effect,false,false);}
    private static Definition d(int struct,int book,int key,String name,int skill,int level,int cooldown,int min,int max,
                                int tier,int hits,Effect effect,boolean offhand,boolean twoHanded){
        return new Definition(struct,book,key,name,skill,level,cooldown,min,max,tier,hits,effect,offhand,twoHanded);
    }
    static int base(int struct){return struct==48312||struct==48313?48311:Native950Conjures.base(struct);}
    static Definition get(int struct){for(Definition d:DEFINITIONS)if(d.struct==struct)return d;return null;}
    static void verify(){
        for(Definition d:DEFINITIONS){
            RS3GeneralRequirementMap s=RS3GeneralRequirementMap.getMap(d.struct);
            if(!d.name.equals(s.getStringValue(2794))||s.getIntValue(2793)!=d.key
                ||s.getIntValue(2807)!=d.level||s.getIntValue(2796)!=d.cooldown
                ||s.getIntValue(2799)!=d.tier||s.getIntValue(2798)!=10*d.adrenalineCost()
                ||s.getIntValue(2800)!=10*d.adrenalineGain()
                ||(s.getIntValue(2811)==1)!=d.offhandRequired
                ||(s.getIntValue(2812)==1)!=d.twoHandedRequired
                ||(s.getIntValue(2813)==1)!=d.shieldRequired()
                ||Native950ActionBar.struct(Native950ActionBar.pack(d.book,d.key))!=base(d.struct))
                throw new IllegalStateException("Changed950 ability definition: "+d.name);
            if(d.channelled()&&d.struct!=14701&&d.struct!=14666
                    &&(s.getIntValue(8884)!=d.hitSpacing()||s.getIntValue(8885)!=d.hits-1))
                throw new IllegalStateException("Changed950 channel cadence: "+d.name);
        }
        for(Native950CombatBuffs.Type type:Native950CombatBuffs.Type.values())
            if(type!=Native950CombatBuffs.Type.DEBILITATE&&RS3GeneralRequirementMap.getMap(type.structure).getIntValue(3740)!=type.duration)
                throw new IllegalStateException("Changed950 buff duration: "+type);
    }
    static final class AnimationResolution {
        final int id; final String source;
        AnimationResolution(int id,String source){this.id=id;this.source=source;}
    }
    static AnimationResolution animationResolution(Player p,int struct){
        if(com.rs.cache.Cache.STORE==null)return new AnimationResolution(-1,"none:cache-unavailable");
        RS3GeneralRequirementMap d=RS3GeneralRequirementMap.getMap(struct);
        int enumId=d.getIntValue(2915);
        ItemDefinitions weapon=p.getEquipment().getWeaponId()<0?null:Native950CacheItems.definition(p.getEquipment().getWeaponId());
        int family=weapon==null?0:weapon.getCSOpcode(686);
        RS3ClientScriptMap table=enumId>0?RS3ClientScriptMap.getMap(enumId):null;
        Object flat=d.getValues()==null?null:d.getValues().get(2914L);
        if(struct==48324&&flat==null)flat=d.getValues().get(2535L); // Exact Living Death sequence field.
        if(struct==48301&&flat==null&&sequenceParam(35469,2933)==targetGraphic(struct))flat=35469;
        // Undercut named Volley35469 corroborated by950 SeqType2933 == Struct48301 impact7879.
        int id=keyedAnimation(flat,table==null?null:table.getValues(),table==null?-1:table.getDefaultIntValue(),family);
        if(id<0)return new AnimationResolution(-1,"none:weapon-family-"+family+"-has-no-entry");
        AnimationDefinitions seq=AnimationDefinitions.getAnimationDefinitions(id);
        return seq.decodeFailure==null?new AnimationResolution(id,enumId>0?"cache:enum-"+enumId+"-weapon-family-"+family:"cache:struct-"+struct+"-presentation-binding")
                :new AnimationResolution(-1,"none:sequence-"+id+"-decode-failed");
    }
    /** Exact typed absence is not sequence zero. Enum family/default precedes a flat binding. */
    static int keyedAnimation(Object flat,Map<Long,Object> entries,int fallback,int family){
        Object entry=entries==null?null:entries.get((long)family);
        if(entry instanceof Integer&&((Integer)entry)>=0)return (Integer)entry;
        if(fallback>=0)return fallback;
        return flat instanceof Integer&&((Integer)flat)>=0?(Integer)flat:-1;
    }
    static int animation(Player p,int struct){return animationResolution(p,struct).id;}
    /**
     * Describes what the paired cache and live QA establish about an ability's
     * visual effect. Some 950 sequences own their effect internally and therefore
     * correctly have no separate graphic parameter.
     */
    static String presentationEvidence(int struct,int animation,int graphic,int targetGraphic){
        if(graphic>=0||targetGraphic>0)return "cache-separate-graphic";
        if((struct==14730||struct==14733)&&animation>=0)return "live-verified-animation-integrated";
        if(animation>=0)return "animation-only-unverified";
        return "missing";
    }
    static int sequenceParam(int animation,int param){
        if(animation<0||com.rs.cache.Cache.STORE==null)return -1;
        Map<Integer,Object> values=AnimationDefinitions.getAnimationDefinitions(animation).clientScriptData;
        Object value=values==null?null:values.get(param);return value instanceof Integer?(Integer)value:-1;
    }
    /** Mirrors Entity#setNextAnimation's cache-frame duration in the native world's 600ms ticks. */
    static int animationTicks(int animation){
        if(animation<0)return 1;
        return animationTicksForMillis(AnimationDefinitions.getAnimationDefinitions(animation).getEmoteTime());
    }
    static int animationTicksForMillis(int millis){return Math.max(1,(Math.max(0,millis)+599)/600);}
    static int targetGraphic(int struct,int animation){int direct=targetGraphic(struct);return direct>=0?direct:sequenceParam(animation,2933);}
    static int targetGraphic(int struct){
        if(com.rs.cache.Cache.STORE==null)return -1;
        return structParam(struct,2933);
    }
    static int casterGraphic(int struct,int animation){
        int direct=structParam(struct,2920);
        return direct>=0?direct:animation<0?-1:sequenceParam(animation,2920);
    }
    static int projectileGraphic(int struct,int animation){
        int direct=structParam(struct,2940);
        return direct>=0?direct:animation<0?-1:sequenceParam(animation,2940);
    }
    private static int structParam(int struct,int param){
        if(com.rs.cache.Cache.STORE==null)return -1;
        Map<Long,Object> values=RS3GeneralRequirementMap.getMap(struct).getValues();
        Object value=values==null?null:values.get((long)param);
        return value instanceof Integer?(Integer)value:-1;
    }
    private Native950AbilityCatalog(){}
}
