package com.rs.game.player.client;

import com.rs.cache.Cache;
import com.rs.cache.loaders.AnimationDefinitions;
import com.rs.cache.loaders.rs3.RS3ClientScriptMap;
import com.rs.cache.loaders.rs3.RS3GeneralRequirementMap;
import com.rs.game.player.Player;
import java.util.IdentityHashMap;
import java.util.Map;

/** RS3 standard air spell progression. No RS2 catalytic runes or base cast XP.
 * https://runescape.wiki/w/Standard_spellbook and https://runescape.wiki/w/Air_rune */
public final class Native950AutoSpells {
    private static final Map<Player,Spell> SELECTED=new IdentityHashMap<>();
    private Native950AutoSpells() { }
    public enum Spell {
        STRIKE("Air Strike",14,1,16,1), BOLT("Air Bolt",23,17,40,2), BLAST("Air Blast",37,41,61,3),
        WAVE("Air Wave",58,62,80,4), SURGE("Air Surge",73,81,99,5);
        public final String name;
        public final int key,level,damageCap,airRunes;
        Spell(String name,int key,int level,int cap,int runes) { this.name=name;this.key=key;this.level=level;damageCap=cap;airRunes=runes; }
    }
    public static Spell select(int magicLevel) {
        Spell best=Spell.STRIKE;
        for(Spell spell:Spell.values())if(magicLevel>=spell.level)best=spell;
        return best;
    }
    public static synchronized Spell select(Player player) {
        Spell selected=SELECTED.get(player);
        return selected!=null&&player.getSkills().getLevel(6)>=selected.level?selected:select(player.getSkills().getLevel(6));
    }
    static synchronized String choose(Player player,int key) {
        for(Spell spell:Spell.values())if(spell.key==key){
            if(player.getSkills().getLevel(6)<spell.level)return "You need level "+spell.level+" Magic to select "+spell.name+".";
            SELECTED.put(player,spell);return spell.name+" selected for native auto-casting.";
        }
        return "That spell is not in the supported native combat spellbook yet.";
    }
    static synchronized String choose(Player player,String name) {
        String normalized=name==null?"":name.toLowerCase(java.util.Locale.ROOT).replace(" ","");
        if(normalized.startsWith("air"))normalized=normalized.substring(3);
        for(Spell spell:Spell.values())if(spell.name.toLowerCase(java.util.Locale.ROOT).replace("air ","").equals(normalized))
            return choose(player,spell.key);
        return "Use ;;spell strike|bolt|blast|wave|surge.";
    }
    /** Exact paired-950 standard-spell cast sequence; callers retain their weapon animation as a fallback. */
    static int animation(Player player) {
        if(Cache.STORE==null)return -1;
        int structure=RS3ClientScriptMap.getMap(6740).getIntValue(select(player).key);
        if(structure<0)return -1;
        int animation=RS3GeneralRequirementMap.getMap(structure).getIntValue(2914);
        if(animation<0)return -1;
        return AnimationDefinitions.getAnimationDefinitions(animation).decodeFailure==null?animation:-1;
    }
    static synchronized void clear(Player player){SELECTED.remove(player);}
    static int damageTier(int playerLevel,int weaponTier,Spell spell) {
        return Math.max(1,Math.min(Math.min(playerLevel,weaponTier),spell.damageCap));
    }
}
