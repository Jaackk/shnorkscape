package com.rs.game.npc.combat.impl.spiderboss;

import com.rs.cores.CoresManager;
import com.rs.cores.FixedLengthRunnable;
import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.Graphics;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.Projectile;
import com.rs.game.World;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.combat.NPCCombatDefinitionConstants;
import com.rs.game.npc.spiderboss.AraxxorMinion;
import com.rs.utils.Logger;
import com.rs.utils.Utils;

import java.util.concurrent.TimeUnit;

public class AraxxorMinionCombat extends CombatScript {

    @Override
    public Object[] getKeys() {
        return new Object[] { 19458, 19459, 19460, 19468, 19469 };
    }

    @Override
    public int attack(NPC npc, Entity target) {
        if (!(npc instanceof AraxxorMinion))
            return 0;
        AraxxorMinion minion = (AraxxorMinion) npc;
        if (minion.getId() == 19469)
            return 0;
        if (minion.getId() == 19458 || minion.getId() == 19468) {
            if (!Utils.isOnRange(minion, target, 0))
                return 0;
            minion.setNextAnimation(new Animation(minion.getCombatDefinitions().getAttackEmote()));
            int damage = CombatScript.getMaxHit(minion, minion.getMaxHit(), NPCCombatDefinitionConstants.MELEE, target);
            CombatScript.delayHit(minion, 0, target, new Hit(minion, damage, HitLook.MELEE_DAMAGE));
            return 6;
        } else {
            if (!Utils.isOnRange(minion, target, 7))
                return 0;
            int combatStyle = npc.getId() == 19459 ? NPCCombatDefinitionConstants.MAGE : NPCCombatDefinitionConstants.RANGE;
            minion.setNextAnimation(new Animation(minion.getCombatDefinitions().getAttackEmote()));
            if (combatStyle == NPCCombatDefinitionConstants.MAGE)
                minion.setNextGraphics(new Graphics(4988));
            Projectile projectile = World.sendProjectileCycles(minion, target, combatStyle == NPCCombatDefinitionConstants.RANGE ? 4990 : 4989, 14, 20, 13, combatStyle == NPCCombatDefinitionConstants.RANGE ? 29 : 21, Utils.random(5), 69);
            long projectileCycles = Utils.projectileTimeToMiliseconds(projectile);
            CoresManager.getServiceProvider().scheduleFixedLengthTask(new FixedLengthRunnable() {
                @Override
                public boolean repeat() {
                    try {
                        int damage = CombatScript.getMaxHit(minion, minion.getMaxHit(), combatStyle, target);
                        CombatScript.delayHit(minion, 0, target, new Hit(minion, damage, combatStyle == NPCCombatDefinitionConstants.RANGE ? HitLook.RANGE_DAMAGE : HitLook.MAGIC_DAMAGE));
                        return false;
                    } catch (Exception e) {
                        Logger.getGlobal().catching(e);
                        return false;
                    }
                }

            }, projectileCycles, 600, TimeUnit.MILLISECONDS);
            return 6;
        }
    }

}
