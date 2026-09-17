package com.rs.game.npc.combat.impl.eds;

import java.util.concurrent.TimeUnit;

import com.rs.cores.CoresManager;
import com.rs.cores.FixedLengthRunnable;
import com.rs.game.Entity;
import com.rs.game.Projectile;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.combat.NPCCombatDefinitionConstants;
import com.rs.game.npc.eds.MasutaTheAscended.ThrashingWater;
import com.rs.game.player.Player;
import com.rs.utils.Logger;
import com.rs.utils.Utils;

public class ThrashingWaterCombat extends CombatScript {

    @Override
    public int attack(NPC npc, Entity target) {
        if (!(npc instanceof ThrashingWater))
            return 0;
        if (!(target instanceof Player))
            return 0;
        int distance = Utils.getDistance(new WorldTile(npc), new WorldTile(target));
        if (distance > 7)
            distance = 7;
        Projectile projectile = World.sendProjectileCycles(npc, target, 2705, 30, 30, 10, 60 + (20 * distance), Utils.random(5), 0);
        long projectileCycles = Utils.projectileTimeToMiliseconds(projectile);
        CoresManager.getServiceProvider().scheduleFixedLengthTask(new FixedLengthRunnable() {
            @Override
            public boolean repeat() {
                try {
                    if (target.isDead() || target.hasFinished() || npc == null)
                        return false;
                    int damage = CombatScript.getMaxHit(npc, npc.getMaxHit(), NPCCombatDefinitionConstants.MAGE, target);
                    CombatScript.delayHit(npc, 0, target, CombatScript.getMagicHit(npc, damage));
                    return false;
                } catch (Exception e) {
                    Logger.getGlobal().catching(e);
                    return false;
                }
            }
        }, projectileCycles, 600, TimeUnit.MILLISECONDS);
        return npc.getAttackSpeed() + 2;
    }

    @Override
    public Object[] getKeys() {
        return new Object[] { 25592 };
    }

}
