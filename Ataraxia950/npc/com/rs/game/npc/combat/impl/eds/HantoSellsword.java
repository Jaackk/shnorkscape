package com.rs.game.npc.combat.impl.eds;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.ForceTalk;
import com.rs.game.Graphics;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.combat.NPCCombatDefinitionConstants;
import com.rs.game.npc.eds.EliteDungeonNPC;
import com.rs.game.player.Player;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

import io.netty.util.internal.ThreadLocalRandom;

public class HantoSellsword extends CombatScript {

    @Override
    public int attack(NPC n, Entity target) {
        if (!(target instanceof Player))
            return 0;
        if (!(n instanceof EliteDungeonNPC))
            return 0;
        EliteDungeonNPC npc = (EliteDungeonNPC) n;
        if (!npc.hasChangedRenderAnimation())
            npc.setNextRenderAnimation(2688);
        long specialDelay = npc.getTemporaryAttributtes().get("SpecialAttackDelay") == null ? 0 : (long) npc.getTemporaryAttributtes().get("SpecialAttackDelay");
        boolean specialAttack = (specialDelay == 0 || Utils.currentTimeMillis() > specialDelay) && ThreadLocalRandom.current().nextDouble(1) <= 0.25;
        if (specialAttack) {
            WorldTasksManager.schedule(new WorldTask() {
                private int ticks;

                @Override
                public void run() {
                    if (npc.hasFinished() || npc.isDead() || ticks >= 10) {
                        npc.getTemporaryAttributtes().remove("cantDoAnimationOrGFX");
                        npc.setNextGraphics(new Graphics(-1));
                        npc.setNextRenderAnimation(2688);
                        npc.setRun(true);
                        stop();
                        return;
                    } else if (ticks == 0) {
                        npc.setNextForceTalk(new ForceTalk("Die by the blade!"));
                        npc.setRun(false);
                        npc.setNextRenderAnimation(2989);
                        npc.getTemporaryAttributtes().put("cantDoAnimationOrGFX", Boolean.TRUE);
                    } else {
                        npc.setNextGraphics(new Graphics(4415));
                        for (Entity e : npc.getPossibleTargets()) {
                            if (e == null || e.hasFinished() || e.isDead() || !(e instanceof Player))
                                continue;
                            if (Utils.isOnRange(npc, e, 0)) {
                                final int damage = Utils.random(200, 401);
                                e.applyHit(new Hit(npc, damage, HitLook.REGULAR_DAMAGE));
                            }
                        }
                    }
                    ticks++;
                }
            }, 0, 0);
            npc.getTemporaryAttributtes().put("SpecialAttackDelay", Utils.currentTimeMillis() + 5000L);
            return 10;
        }
        npc.setNextAnimation(new Animation(npc.getAttackEmote()));
        int damage = CombatScript.getMaxHit(npc, npc.getMaxHit(), NPCCombatDefinitionConstants.MELEE, target);
        CombatScript.delayHit(npc, 0, target, CombatScript.getMeleeHit(npc, damage));
        return npc.getAttackSpeed();
    }

    @Override
    public Object[] getKeys() {
        return new Object[] { "Hanto sellsword" };
    }

}
