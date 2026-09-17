package com.rs.game.npc.combat.impl;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.ForceMovement;
import com.rs.game.Graphics;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.combat.NPCCombatDefinition;
import com.rs.game.npc.combat.NPCCombatDefinitionConstants;
import com.rs.game.npc.kalgerion.Kalgerion;
import com.rs.game.player.Player;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

public class KalgerionCombat extends CombatScript {

    @Override
    public int attack(NPC npc, Entity target) {
        final NPCCombatDefinition defs = npc.getCombatDefinitions();
        Kalgerion kalgerion = (Kalgerion) npc;

        switch (Utils.random(20)) {
        case 10:
            truePower(npc, target);
            break;
        case 15:
            drag(npc, target);
            break;
        }

        if (!kalgerion.drag && !kalgerion.truePower)
            meleeAttack(npc, target);

        return defs.getAttackDelay();
    }

    @Override
    public Object[] getKeys() {
        return new Object[] { 14973, 14974, 14975, 14976, 14977 };
    }

    public void meleeAttack(NPC npc, Entity target) {
        if (npc.withinDistance(target, 2)) {
            NPCCombatDefinition defs = npc.getCombatDefinitions();
            npc.setNextAnimation(new Animation(23206));
            delayHit(npc, 0, target, getMeleeHit(npc, getRandomMaxHit(npc, defs.getMaxHit(), NPCCombatDefinitionConstants.MELEE, target)));
        } else if (npc.withinDistance(target, 8)) {
            NPCCombatDefinition defs = npc.getCombatDefinitions();
            npc.setNextAnimation(new Animation(16990));
            target.applyHit(new Hit(target, getRandomMaxHit(npc, defs.getMaxHit(), NPCCombatDefinitionConstants.MELEE, target), HitLook.MELEE_DAMAGE));
        }
    }

    public void truePower(NPC npc, Entity target) {
        Kalgerion kalgerion = (Kalgerion) npc;
        kalgerion.truePower = true;

        WorldTasksManager.schedule(new WorldTask() {
            int tick;

            @Override
            public void run() {
                switch (tick) {
                case 0:
                    npc.setNextAnimation(new Animation(19576));
                    break;
                case 1:
                    if (npc.withinDistance(target, 8))
                        target.applyHit(new Hit(target, Utils.random(50, 150), HitLook.REGULAR_DAMAGE));
                    break;
                case 2:
                    kalgerion.truePower = false;
                    stop();
                    return;
                }

                tick++;
            }

        }, 1, 1);
    }

    public void drag(NPC npc, Entity target) {
        Player player = (Player) target;
        Kalgerion kalgerion = (Kalgerion) npc;
        kalgerion.drag = true;

        WorldTasksManager.schedule(new WorldTask() {
            int stage;

            @Override
            public void run() {
                switch (stage) {
                case 0:
                    player.faceEntity(kalgerion);
                    npc.setNextAnimation(new Animation(19572));
                    player.lock();
                    player.setNextGraphics(new Graphics(3537, 5, 0));
                    WorldTile dragTile = kalgerion.getDragWorldTile(target);
                    player.setNextForceMovement(new ForceMovement(player, 0, dragTile, 1, npc.getDirection()));

                    WorldTasksManager.schedule(new WorldTask() {
                        @Override
                        public void run() {
                            player.setNextWorldTile(new WorldTile(dragTile));
                        }
                    });
                    npc.faceEntity(player);
                    break;

                case 1:
                    npc.setNextAnimation(new Animation(19571));
                    break;

                case 2:
                    target.applyHit(new Hit(target, Utils.random(450, 500), HitLook.REGULAR_DAMAGE));

                    double healAmount = kalgerion.getMaxHitpoints() * 0.15;
                    int healValue = (int) healAmount;

                    npc.applyHit(new Hit(npc, healValue, HitLook.HEALED_DAMAGE));
                    npc.setHitpoints(npc.getHitpoints() + healValue);

                    kalgerion.drag = false;

                    player.unlock();
                    stop();
                    return;
                }

                stage++;
            }

        }, 1, 1);
    }

}
