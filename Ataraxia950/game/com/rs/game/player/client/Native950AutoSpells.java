package com.rs.game.player.client;

/** RS3 standard air spell progression. No RS2 catalytic runes or base cast XP.
 * https://runescape.wiki/w/Standard_spellbook and https://runescape.wiki/w/Air_rune */
public final class Native950AutoSpells {
    private Native950AutoSpells() { }
    public enum Spell {
        STRIKE("Air Strike",1,16,1), BOLT("Air Bolt",17,40,2), BLAST("Air Blast",41,61,3),
        WAVE("Air Wave",62,80,4), SURGE("Air Surge",81,99,5);
        public final String name;
        public final int level, damageCap, airRunes;
        Spell(String name,int level,int cap,int runes) { this.name=name;this.level=level;damageCap=cap;airRunes=runes; }
    }
    public static Spell select(int magicLevel) {
        Spell best=Spell.STRIKE;
        for(Spell spell:Spell.values())if(magicLevel>=spell.level)best=spell;
        return best;
    }
    static int damageTier(int playerLevel,int weaponTier,Spell spell) {
        return Math.max(1,Math.min(Math.min(playerLevel,weaponTier),spell.damageCap));
    }
}
