package com.rs.game.player.client;

import com.rs.cache.Cache;
import com.rs.cache.loaders.AnimationDefinitions;
import com.rs.cache.loaders.rs3.RS3ClientScriptMap;
import com.rs.cache.loaders.rs3.RS3GeneralRequirementMap;
import com.rs.network.protocol.modern950.Native950Packets;
import io.netty.channel.Channel;
import com.rs.game.player.Player;
import java.util.IdentityHashMap;
import java.util.Map;

/** RS3 standard air spell progression. No RS2 catalytic runes or base cast XP.
 * https://runescape.wiki/w/Standard_spellbook and https://runescape.wiki/w/Air_rune */
public final class Native950AutoSpells {
    private static final Map<Player,Spell> SELECTED=new IdentityHashMap<>();
    private static final String SETTING="combat.autocast";
    private Native950AutoSpells() { }
    public enum Spell {
        STRIKE("Air Strike",14,1,16,1), BOLT("Air Bolt",23,17,40,2), BLAST("Air Blast",37,41,61,3),
        WAVE("Air Wave",58,62,80,4), SURGE("Air Surge",73,81,99,5);
        public final String name;
        public final int key,level,damageCap,airRunes;
        Spell(String name,int key,int level,int cap,int runes) { this.name=name;this.key=key;this.level=level;damageCap=cap;airRunes=runes; }
    }
    /**
     * Standard-spell presentation is owned by the spell structure rather than the
     * equipped weapon. These values are read from the paired, startup-verified
     * 950 cache at the moment of a cast.
     */
    static final class Presentation {
        final int animation, projectile, impact;
        Presentation(int animation,int projectile,int impact){this.animation=animation;this.projectile=projectile;this.impact=impact;}
        boolean available(){return animation>=0&&projectile>=0&&impact>=0;}
    }
    public static Spell select(int magicLevel) {
        Spell best=Spell.STRIKE;
        for(Spell spell:Spell.values())if(magicLevel>=spell.level)best=spell;
        return best;
    }
    public static synchronized Spell select(Player player) {
        Spell selected=SELECTED.get(player);
        return selected!=null?selected:select(player.getSkills().getLevel(6));
    }
    static Spell forKey(int key){for(Spell spell:Spell.values())if(spell.key==key)return spell;return null;}
    public static synchronized void writeSettings(Player player,Map<String,Integer> settings){
        Spell selected=SELECTED.get(player);
        if(selected!=null)settings.put(SETTING,selected.key);
    }
    public static synchronized void restore(Player player,Map<String,Integer> settings){
        Spell selected=forKey(settings.getOrDefault(SETTING,-1));
        if(selected==null)SELECTED.remove(player);else SELECTED.put(player,selected);
    }
    static synchronized String choose(Player player,int key) { return choose(player,null,key); }
    /** The standard spellbook and the 950 combat owner both use the spell key as varbit 43's value. */
    static synchronized String choose(Player player,Channel channel,int key) {
        for(Spell spell:Spell.values())if(spell.key==key){
            if(player.getSkills().getLevel(6)<spell.level)return "You need level "+spell.level+" Magic to select "+spell.name+".";
            Spell before=select(player);
            SELECTED.put(player,spell);syncSelection(player,channel);
            Native950BugTest.event(player,"magic","selection-changed","before",before.name,"after",spell.name,"key",key);
            return spell.name+" selected for native auto-casting.";
        }
        return "That spell is not in the supported native combat spellbook yet.";
    }
    static synchronized String choose(Player player,String name) { return choose(player,null,name); }
    static synchronized String choose(Player player,Channel channel,String name) {
        String normalized=name==null?"":name.toLowerCase(java.util.Locale.ROOT).replace(" ","");
        if(normalized.startsWith("air"))normalized=normalized.substring(3);
        for(Spell spell:Spell.values())if(spell.name.toLowerCase(java.util.Locale.ROOT).replace("air ","").equals(normalized))
            return choose(player,channel,spell.key);
        return "Use ;;spell strike|bolt|blast|wave|surge.";
    }
    static synchronized void syncSelection(Player player,Channel channel) {
        if(player==null||channel==null)return;
        Spell spell=select(player);
        Native950BugTest.event(player,"magic","selection-sync","spell",spell.name,"varbit",43,"value",spell.key);
        channel.write(Native950Packets.varbitSmall(43,spell.key));
    }
    /** Exact paired-950 standard-spell cast sequence; callers retain their weapon animation as a fallback. */
    static int animation(Player player) {return presentation(player).animation;}
    static Presentation presentation(Player player) {
        if(Cache.STORE==null)return new Presentation(-1,-1,-1);
        int structure=RS3ClientScriptMap.getMap(6740).getIntValue(select(player).key);
        if(structure<0)return new Presentation(-1,-1,-1);
        Object cast=RS3GeneralRequirementMap.getMap(structure).getValue(player.getEquipment().hasTwoHandedWeapon()?2919:2914);
        int animation=cast instanceof Integer?(Integer)cast:-1;
        if(animation<0)return new Presentation(-1,-1,-1);
        AnimationDefinitions sequence=AnimationDefinitions.getAnimationDefinitions(animation);
        if(sequence.decodeFailure!=null)return new Presentation(-1,-1,-1);
        return presentation(animation,sequence.clientScriptData);
    }
    // Each presentation layer has independent evidence. A missing impact must not
    // erase a valid casting sequence or manufacture sequence zero from a default.
    static Presentation presentation(int animation,Map<Integer,Object> params) {
        Object projectile=params==null?null:params.get(2940),impact=params==null?null:params.get(2933);
        return new Presentation(animation,projectile instanceof Integer?(Integer)projectile:-1,
                impact instanceof Integer?(Integer)impact:-1);
    }
    static synchronized void clear(Player player){SELECTED.remove(player);}
    static int damageTier(int playerLevel,int weaponTier,Spell spell) {
        return Math.max(1,Math.min(Math.min(playerLevel,weaponTier),spell.damageCap));
    }
}
