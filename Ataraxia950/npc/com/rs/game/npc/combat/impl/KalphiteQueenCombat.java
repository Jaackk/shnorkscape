package com.rs.game.npc.combat.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.Graphics;
import com.rs.game.Projectile;
import com.rs.game.World;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.combat.NPCCombatDefinition;
import com.rs.game.npc.combat.NPCCombatDefinitionConstants;
import com.rs.game.player.Player;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

public class KalphiteQueenCombat extends CombatScript {

    @Override
    public int attack(final NPC npc, final Entity target) {
        final NPCCombatDefinition defs = npc.getCombatDefinitions();
        int attackStyle = Utils.random(3);
        boolean secondForm = npc.getId() != 1158 && npc.getId() != 16707;
        if (attackStyle == 0) {
            int distanceX = target.getX() - npc.getX();
            int distanceY = target.getY() - npc.getY();
            int size = npc.getSize();
            if (distanceX > size || distanceX < -1 || distanceY > size || distanceY < -1)
                attackStyle = Utils.random(2); // set mage
            else {
                npc.setNextAnimation(new Animation(secondForm ? 24277 : 24275));
                delayHit(npc, 0, target, getMeleeHit(npc, getRandomMaxHit(npc, defs.getMaxHit(), NPCCombatDefinitionConstants.MELEE, target)));
                return defs.getAttackDelay();
            }
        }
        if (attackStyle == 1) { // range easy one
            npc.setNextAnimation(new Animation(secondForm ? 24284 : 24282));
            for (final Entity t : npc.getPossibleTargets()) {
                delayHit(npc, 2, t, getRangeHit(npc, getRandomMaxHit(npc, defs.getMaxHit(), NPCCombatDefinitionConstants.RANGE, t, true)));
                boolean drainPrayer = (t instanceof Player) && (Math.random() <= 0.40);
                if (drainPrayer)
                    WorldTasksManager.schedule(new WorldTask() {
                        @Override
                        public void run() {
                            if (t.hasFinished() || t.isDead())
                                return;
                            ((Player) t).getPrayer().drainPrayer(10, true);
                        }
                    }, 2);
                World.sendProjectile(npc, t, 288, 46, 31, 50, 30, 16, 0);
            }
        } else {
            npc.setNextAnimation(new Animation(secondForm ? 24285 : 24281));
            npc.setNextGraphics(new Graphics(npc.getId() == 1158 ? 278 : 279));
            WorldTasksManager.schedule(new WorldTask() {

                @Override
                public void run() {
                    attackMageTarget(new ArrayList<Player>(), npc, npc, target);
                }

            });
        }
        return defs.getAttackDelay();
    }

    private void attackMageTarget(final List<Player> arrayList, Entity fromEntity, final NPC startTile, Entity t) {
        final Entity target = t == null ? getTarget(arrayList, fromEntity, startTile) : t;
        if (target == null)
            return;
        if (target instanceof Player)
            arrayList.add((Player) target);
        World.sendProjectile(fromEntity, target, 280, fromEntity == startTile ? 70 : 20, 20, 60, 30, 0, 0);
        delayHit(startTile, 0, target, getMagicHit(startTile, getRandomMaxHit(startTile, startTile.getMaxHit(), NPCCombatDefinitionConstants.MAGE, target, true)));
        WorldTasksManager.schedule(new WorldTask() {

            @Override
            public void run() {
                target.setNextGraphics(new Graphics(281));
                attackMageTarget(arrayList, target, startTile, null);
            }
        });
    }

    @Override
    public Object[] getKeys() {
        return new Object[] { "Kalphite Queen" };
    }

    public static Player getTarget(List<Player> list, final Entity fromEntity, NPC startTile) {
        if (fromEntity == null)
            return null;
        ArrayList<Player> added = new ArrayList<Player>();
        for (Entity entity : startTile.getPossibleTargets()) {
            if (!(entity instanceof Player))
                continue;
            Player player = (Player) entity;
            if (player == null || list.contains(player) || !player.withinDistance(fromEntity) || !player.withinDistance(startTile))
                continue;
            added.add(player);
        }
        if (added.isEmpty())
            return null;
        Collections.sort(added, new Comparator<Player>() {

            @Override
            public int compare(Player o1, Player o2) {
                if (o1 == null)
                    return 1;
                if (o2 == null)
                    return -1;
                if (Utils.getDistance(o1, fromEntity) > Utils.getDistance(o2, fromEntity))
                    return 1;
                else if (Utils.getDistance(o1, fromEntity) < Utils.getDistance(o2, fromEntity))
                    return -1;
                else
                    return 0;
            }
        });
        return added.get(0);

    }

    public static void attackMageTarget(final List<Player> arrayList, Entity fromEntity, final NPC startTile, Entity t, final int projectile, final int gfx) {
        final Entity target = t == null ? getTarget(arrayList, fromEntity, startTile) : t;
        int maxHit = 200;
        if (fromEntity instanceof NPC)
            maxHit = ((NPC) fromEntity).getMaxHit();
        if (target == null)
            return;
        if (target instanceof Player)
            arrayList.add((Player) target);
        Projectile proj = World.sendProjectileNew(fromEntity, target, projectile, fromEntity == startTile ? 70 : 20, 20, 30, 6, 0, 0);
        int endTime = Utils.projectileTimeToCycles(proj.getEndTime()) - 1;
        delayHit(startTile, endTime, target, getMagicHit(startTile, getRandomMaxHit(startTile, maxHit, NPCCombatDefinitionConstants.MAGE, target, true)));
        WorldTasksManager.schedule(new WorldTask() {

            @Override
            public void run() {
                target.setNextGraphics(new Graphics(gfx));
                attackMageTarget(arrayList, target, startTile, null, projectile, gfx);
            }
        }, endTime);
    }
}
