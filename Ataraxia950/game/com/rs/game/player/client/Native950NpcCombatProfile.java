package com.rs.game.player.client;

/** Immutable resolved melee metadata. HP and maxHit retain Ataraxia engine units. */
public final class Native950NpcCombatProfile {
    public final int npcId, size, combatLevel, hp, attackLevel, defenceLevel;
    public final int maxHit, attackSpeed, deathTicks, respawnTicks;
    public final int attackAnim, blockAnim, deathAnim, meleeAttackBonus, meleeDefenceBonus;

    public final String name;
    public final boolean cacheAttackSpeed, cacheAttackBonus, cacheDefenceBonus;
    public final int deathAnimationTicks;

    Native950NpcCombatProfile(int npcId, int size, int combatLevel, int hp,
            int attackLevel, int defenceLevel, int maxHit, int attackSpeed,
            int deathTicks, int respawnTicks, int attackAnim, int blockAnim, int deathAnim,
            int meleeAttackBonus, int meleeDefenceBonus) {
        this(npcId,size,combatLevel,hp,attackLevel,defenceLevel,maxHit,attackSpeed,deathTicks,respawnTicks,
                attackAnim,blockAnim,deathAnim,meleeAttackBonus,meleeDefenceBonus,"creature",false,false,false,0);
    }
    Native950NpcCombatProfile(int npcId, int size, int combatLevel, int hp,
            int attackLevel, int defenceLevel, int maxHit, int attackSpeed, int deathTicks, int respawnTicks,
            int attackAnim, int blockAnim, int deathAnim, int meleeAttackBonus, int meleeDefenceBonus,
            String name, boolean cacheAttackSpeed, boolean cacheAttackBonus, boolean cacheDefenceBonus,
            int deathAnimationTicks) {
        this.name=name;this.cacheAttackSpeed=cacheAttackSpeed;this.cacheAttackBonus=cacheAttackBonus;
        this.cacheDefenceBonus=cacheDefenceBonus;this.deathAnimationTicks=deathAnimationTicks;
        this.npcId=npcId; this.size=size; this.combatLevel=combatLevel; this.hp=hp;
        this.attackLevel=attackLevel; this.defenceLevel=defenceLevel; this.maxHit=maxHit;
        this.attackSpeed=attackSpeed; this.deathTicks=deathTicks; this.respawnTicks=respawnTicks;
        this.attackAnim=attackAnim; this.blockAnim=blockAnim; this.deathAnim=deathAnim;
        this.meleeAttackBonus=meleeAttackBonus; this.meleeDefenceBonus=meleeDefenceBonus;
    }
}
