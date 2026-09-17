package com.rs.game.npc.combat.impl.themagister;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.World;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.combat.NPCCombatDefinitionConstants;
import com.rs.game.npc.themagister.ImperialAkh;
import com.rs.game.player.Player;
import com.rs.utils.Utils;

public class ImperialAkhCombat extends CombatScript {

    @Override
    public int attack(NPC npc, Entity target) {
        if (!(npc instanceof ImperialAkh))
            return 0;
        if (!(target instanceof Player)) {
            npc.switchTarget(true);
            return 0;
        }
        ImperialAkh akh = (ImperialAkh) npc;
        if (akh.getId() == 24766) {
            if (!Utils.isOnRange(akh, target, 0))
                return 0;
            akh.setNextAnimation(new Animation(akh.getCombatDefinitions().getAttackAnim()));
            int damage = CombatScript.getMaxHit(akh, akh.getMaxHit(), NPCCombatDefinitionConstants.MELEE, target);
            CombatScript.delayHit(akh, 0, target, CombatScript.getMeleeHit(akh, damage));
            return 4;
        } else if (akh.getId() == 24767) {
            if (!Utils.isOnRange(akh, target, 6))
                return 0;
            akh.setNextAnimation(new Animation(akh.getCombatDefinitions().getAttackAnim()));
            World.sendProjectileCycles(npc, target, 2733, 40, 25, 30, 70, 0, 50);
            int damage = CombatScript.getMaxHit(akh, akh.getMaxHit(), NPCCombatDefinitionConstants.MAGE, target);
            CombatScript.delayHit(akh, 2, target, CombatScript.getMagicHit(akh, damage));
            return 4;
        } else if (akh.getId() == 24768) {
            if (!Utils.isOnRange(akh, target, 6))
                return 0;
            akh.setNextAnimation(new Animation(akh.getCombatDefinitions().getAttackAnim()));
            World.sendProjectileCycles(npc, target, 1066, 40, 25, 40, 70, 0, 20);
            int damage = CombatScript.getMaxHit(akh, akh.getMaxHit(), NPCCombatDefinitionConstants.RANGE, target);
            CombatScript.delayHit(akh, 2, target, CombatScript.getRangeHit(akh, damage));
            return 4;
        }
        return 0;
    }

    @Override
    public Object[] getKeys() {
        return new Object[] { 24766, 24767, 24768 };
    }

}
