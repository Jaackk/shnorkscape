package com.rs.game.npc.combat.impl.gwd2;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.Graphics;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.NPCCombatDefinitionConstants;
import com.rs.game.npc.gwd2.vindicta.Vindicta;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class GorvekCombat extends VindictaCombat {

    @Override
    public Object[] getKeys() {
        return new Object[] { 22463 };
    }

    @Override
    public int attack(NPC npc, Entity target) {
        if (!(npc instanceof Vindicta))
            return 0;
        Vindicta vindicta = (Vindicta) npc;
        final int phase = vindicta.getPhase();
        final int gorvekPhase = vindicta.getGorvekPhase();
        vindicta.nextPhase();
        if (vindicta.getPhase() < 0 || vindicta.getPhase() > gorvekPhase) {
            vindicta.setPhase(0);
            vindicta.setGorvekPhase(Utils.random(5, 10));
        }
        if (phase == gorvekPhase)
            return dragonfireWall(vindicta, target);
        return meleeAttack(vindicta, target);
    }

    private final int meleeAttack(Vindicta npc, Entity target) {
        npc.setNextAnimation(new Animation(28525));
        delayHit(npc, 0, target, getMeleeHit(npc, getRandomMaxHit(npc, 80, NPCCombatDefinitionConstants.MELEE, target)));
        return 2;
    }

    private final WorldTile getTile(Vindicta npc, Entity target) {
        WorldTile tile;
        final WorldTile zero = npc.getInstance().getWorldTile(0, 0);
        final WorldTile n = new WorldTile(npc.getCoordFaceX(npc.getSize()), npc.getCoordFaceY(npc.getSize()), npc.getPlane());
        for (;;) {
            tile = new WorldTile(zero.getX() + Utils.random(17, 34), zero.getY() + Utils.random(9, 34), zero.getPlane());
            if (tile.getDistance(n) > 4)
                break;
        }
        return tile;
    }

    private final boolean addFire(Vindicta npc, WorldTile target, WorldTile entity) {
        if (!World.canMoveNPC(target.getPlane(), target.getX(), target.getY(), 1))
            return false;
        final WorldTile n = new WorldTile(npc.getCoordFaceX(npc.getSize()), npc.getCoordFaceY(npc.getSize()), npc.getPlane());
        if (entity.getX() > n.getX() && target.getX() < n.getX())
            return false;
        if (entity.getX() < n.getX() && target.getX() > n.getX())
            return false;
        if (entity.getY() > n.getY() && target.getY() < n.getY())
            return false;
        if (entity.getY() < n.getY() && target.getY() > n.getY())
            return false;
        if (target.getDistance(n) <= 1)
            return false;
        final WorldTile corner = npc.getInstance().getWorldTile(0, 0);
        return target.getX() >= corner.getX() + 16 && target.getX() <= corner.getX() + 39 && target.getY() >= corner.getY() + 8 && target.getY() <= corner.getY() + 39;
    }

    private final int dragonfireWall(Vindicta npc, Entity target) {
        final WorldTile destination = getTile(npc, target);
        npc.resetWalkSteps();
        npc.resetCombat();
        npc.setNextGraphics(new Graphics(6118));
        npc.setCannotMove(true);
        npc.setNextAnimation(new Animation(28275));
        npc.getTemporaryAttributtes().put("rangedDelay", Utils.currentTimeMillis() + 15000);
        WorldTasksManager.schedule(new WorldTask() {
            private int ticks;
            private WorldTile[] array;

            @Override
            public void run() {
                if (ticks == 2) {
                    npc.resetWalkSteps();
                    npc.getInstance().getPlayers().forEach(p -> p.stopAll());
                    npc.setNextWorldTile(destination);
                    npc.setNextFaceWorldTile(new WorldTile(target));
                    npc.setNextGraphics(new Graphics(6118));
                    npc.setNextAnimation(new Animation(28276));
                } else if (ticks == 5) {
                    final WorldTile dest = new WorldTile(npc.getCoordFaceX(npc.getSize()), npc.getCoordFaceY(npc.getSize()), npc.getPlane());
                    if (dest.getDistance(target) < 3) {
                        npc.setCannotMove(false);
                        npc.getCombat().setTarget(target);
                        meleeAttack(npc, target);
                        stop();
                        return;
                    }
                    npc.setNextAnimationForce(new Animation(28274));
                } else if (ticks == 6) {
                    final List<WorldTile> tiles = new ArrayList<WorldTile>();
                    final WorldTile dest = new WorldTile(npc.getCoordFaceX(npc.getSize()), npc.getCoordFaceY(npc.getSize()), npc.getPlane());
                    final WorldTile zero = npc.getInstance().getWorldTile(0, 0);
                    final int minBoundaryX = zero.getX() + 15;
                    final int minBoundaryY = zero.getY() + 7;
                    final int startX = dest.getX();
                    final int startY = dest.getY();
                    final int maxBoundaryX = zero.getX() + 40;
                    final int maxBoundaryY = zero.getY() + 40;
                    float m = (float) (target.getY() - startY) / (float) (target.getX() - startX);
                    final float intercept = ((float) target.getY() - m * (float) target.getX());
                    float y = (float) target.getY();
                    float x = (float) target.getX();
                    if (x - startX == 0) {
                        boolean down = y < startY;
                        y = down ? minBoundaryY : maxBoundaryY;
                    } else {
                        boolean left = x < startX;
                        for (; left ? x >= minBoundaryX : x <= maxBoundaryX; x = left ? x - 1 : x + 1) {
                            if (m * x + intercept < maxBoundaryY)
                                y = (m * x) + intercept;
                            else
                                break;
                        }
                    }
                    final List<WorldTile> t = Utils.calculateLine(startX, startY, Math.round(x), Math.round(y), npc.getPlane());
                    t.forEach(tile -> {
                        if (addFire(npc, tile, target))
                            tiles.add(tile);
                    });
                    npc.setCannotMove(false);
                    array = tiles.toArray(new WorldTile[tiles.size()]);
                    npc.addFires(array);
                    npc.resetWalkSteps();
                    npc.getCombat().setTarget(target);
                } else if (ticks == 55) {
                    npc.removeFires(array);
                    stop();
                }
                ticks++;
            }
        }, 0, 0);
        return 10;
    }
}
