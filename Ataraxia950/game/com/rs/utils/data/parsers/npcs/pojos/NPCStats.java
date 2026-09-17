package com.rs.utils.data.parsers.npcs.pojos;

import lombok.Getter;
import lombok.Setter;

public class NPCStats {
    @Getter
    @Setter
    private int attackLevel, rangeLevel, magicLevel, defenceLevel, weaknessAffinity, meleeAffinity, rangeAffinity, magicAffinity;

    public NPCStats(int attackLevel, int rangeLevel, int magicLevel, int defenceLevel, int weaknessAffinity, int meleeAffinity, int rangeAffinity, int magicAffinity) {
        this.attackLevel = attackLevel;
        this.rangeLevel = rangeLevel;
        this.magicLevel = magicLevel;
        this.defenceLevel = defenceLevel;
        this.weaknessAffinity = weaknessAffinity == 0 ? 90 : weaknessAffinity;
        this.meleeAffinity = meleeAffinity == 0 ? 55 : meleeAffinity;
        this.rangeAffinity = rangeAffinity == 0 ? 55 : rangeAffinity;
        this.magicAffinity = magicAffinity == 0 ? 55 : magicAffinity;
    }

    public void fixStats() {
        this.weaknessAffinity = weaknessAffinity == 0 ? 90 : weaknessAffinity;
        this.meleeAffinity = meleeAffinity == 0 ? 55 : meleeAffinity;
        this.rangeAffinity = rangeAffinity == 0 ? 55 : rangeAffinity;
        this.magicAffinity = magicAffinity == 0 ? 55 : magicAffinity;
    }

}
