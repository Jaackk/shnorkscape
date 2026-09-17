package com.rs.game.player.client;

import com.rs.game.npc.NPC;
import com.rs.game.player.Player;

/** Connects the native hit commit to the original 910 combat XP and Skills framework. */
public final class Native950CombatExperience {
    private Native950CombatExperience() { }

    /**
     * Invoke once after the player hit commits, with damage capped to the NPC's
     * remaining HP. Units are engine HP (10), not displayed life points (100).
     * Misses and NPC retaliation award nothing. No XP is added again on death.
     * CombatDefinitions retains the original selected-skill split; Skills retains
     * account rates, its custom curve, experience caps, level-ups and saved arrays.
     */
    public static void awardMelee(Player player, NPC target, int actualDamage) {
        if (player == null || !player.isNative950() || target == null || actualDamage <= 0)
            return;
        com.rs.cache.loaders.ItemDefinitions weapon=com.rs.cache.Cache.STORE==null||player.getEquipment().getWeaponId()<0?null:
                Native950CacheItems.definition(player.getEquipment().getWeaponId());
        award(player,target,actualDamage,weapon==null?Native950CombatStyles.MELEE:Native950CombatStyles.classify(weapon));
    }
    /** Capture the attack style before consuming an equipped thrown stack. The last dart must
     * still train Ranged after equipment is empty; selection arrays and XP rates remain910's. */
    static void award(Player player,NPC target,int actualDamage,int style){
        if(player==null||!player.isNative950()||target==null||actualDamage<=0)return;
        if(style!=Native950CombatStyles.RANGED&&style!=Native950CombatStyles.NECROMANCY){
            player.getCombatDefinitions().giveXp(target,actualDamage/2.5,actualDamage/7.5);return;
        }
        player.getSkills().addXp(com.rs.game.player.Skills.HITPOINTS,actualDamage/7.5);
        if(style==Native950CombatStyles.NECROMANCY){player.getSkills().addXp(28,actualDamage/2.5);return;}
        boolean[] selected=player.getCombatDefinitions().getRangedCombatExperienceGain();
        int count=0;for(boolean train:selected)if(train)count++;
        if(count==0)return;
        for(int i=0;i<selected.length;i++)if(selected[i])
            player.getSkills().addXp(i==0?com.rs.game.player.Skills.RANGE:com.rs.game.player.Skills.DEFENCE,actualDamage/2.5/count);
    }
}
