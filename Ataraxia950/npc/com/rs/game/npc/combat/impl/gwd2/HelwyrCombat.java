package com.rs.game.npc.combat.impl.gwd2;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.ForceTalk;
import com.rs.game.Graphics;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.activities.instances.HelwyrInstance;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.combat.NPCCombatDefinitionConstants;
import com.rs.game.npc.gwd2.helwyr.CMHelwyr;
import com.rs.game.npc.gwd2.helwyr.CywirAlpha;
import com.rs.game.npc.gwd2.helwyr.Helwyr;
import com.rs.game.player.Player;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Tom
 * @date April 8, 2017 Updated by Kris to improve performance. Update v2: Added
 *       challenge mode.
 */
public final class HelwyrCombat extends CombatScript {

    @Override
    public Object[] getKeys() {
        return new Object[] { 22438, 22440 };
    }

    @Override
    public int attack(NPC npc, Entity target) {
        Helwyr helwyr = (Helwyr) npc;
        if (npc.getId() == 22440)
            return challengeAttack((CMHelwyr) helwyr, target);
        final int phase = helwyr.getPhase();
        helwyr.nextPhase();
        if (helwyr.getPhase() < 0 || helwyr.getPhase() > 15)
            helwyr.setPhase(0);
        switch (phase) {
        case 0:
            return nature(helwyr, target);
        case 4:
            return bleed(helwyr, target);
        case 8:
            return frenzy(helwyr, target);
        case 12:
            if (helwyr.getInstance().getAliveWolves() >= 4) {
                helwyr.setPhase(1);
                return nature(helwyr, target);
            }
            return howl(helwyr, target);
        default:
            return bite(helwyr, target);
        }
    }

    private final int challengeAttack(final CMHelwyr helwyr, final Entity target) {
        final int phase = helwyr.getPhase();
        helwyr.nextPhase();
        if (helwyr.getPhase() < 0 || helwyr.getPhase() > 18)
            helwyr.setPhase(0);
        switch (phase) {
        case 0:
            return nature(helwyr, target);
        case 4:
            return bleed(helwyr, target);
        case 8:
            return frenzy(helwyr, target);
        case 12:
            return helwyr.getHowlStage() >= 2 ? bleed(helwyr, target) : howl(helwyr, target);
        case 15:
            return mushroomExplosion(helwyr, target);
        default:
            return bite(helwyr, target);
        }
    }

    private final int mushroomExplosion(final CMHelwyr helwyr, Entity target) {
        WorldTasksManager.schedule(new WorldTask() {
            private int ticks;
            private final List<WorldObject> shrooms = new ArrayList<WorldObject>();

            @Override
            public void run() {

                loop: for (WorldObject o : shrooms) {
                    if (o != null) {
                        if (World.containsObjectWithId(o, o.getId())) {
                            for (WorldTile t : helwyr.getInstance().getTiles()) {
                                if (t.getTileHash() == o.getTileHash())
                                    continue loop;
                            }
                            if (World.containsObjectWithId(o, o.getId()))
                                World.removeObject(o);
                        }
                        helwyr.getInstance().getPlayers().forEach(p -> {
                            p.getPackets().sendGraphics(new Graphics(6125), o);
                            if (p.getDistance(o) < 2)
                                p.applyHit(new Hit(helwyr, Utils.random(500, 600), HitLook.REGULAR_DAMAGE));
                            if (p.getFamiliar() != null && p.getFamiliar().getDistance(o) < 2 && p.getFamiliar().getDefinitions().hasAttackOption())
                                p.getFamiliar().applyHit(new Hit(helwyr, Utils.random(500, 600), HitLook.REGULAR_DAMAGE));
                        });
                    }
                }
                shrooms.clear();
                if (helwyr.isDead() || helwyr.hasFinished()) {
                    stop();
                    return;
                }
                if (ticks != 5) {
                    for (int x = 0; x < 5; x++) {
                        if (x == ticks)
                            continue;
                        loop: for (int i = 5 * x; i < (5 * x) + 5; i++) {
                            final WorldTile tile = helwyr.getInstance().getWorldTile(HelwyrInstance.MUSHROOM_TILES[i][0], HelwyrInstance.MUSHROOM_TILES[i][1]);
                            final WorldObject o = new WorldObject(101900, 10, 3, tile);
                            for (WorldTile t : helwyr.getInstance().getTiles()) {
                                if (t.getTileHash() == tile.getTileHash())
                                    continue loop;
                            }
                            World.spawnObject(o);
                            shrooms.add(o);
                        }
                    }
                } else
                    stop();
                ticks++;
            }
        }, 0, 3);
        return 5;
    }

    /**
     * Sends the basic bite attack.
     */
    private final int bite(Helwyr npc, Entity target) {
        npc.setNextAnimation(new Animation(28205));
        delayHit(npc, 0, target, getMeleeHit(npc, getRandomMaxHit(npc, npc.getMaxHit(), NPCCombatDefinitionConstants.MELEE, target)));
        return 4;
    }

    /**
     * Sends three mushrooms to the arena.
     */
    private final int nature(Helwyr helwyr, Entity target) {
        helwyr.setNextForceTalk(new ForceTalk("Nature, lend me your aid!"));
        helwyr.setNextAnimation(new Animation(28207));
        final int amount = helwyr.getId() == 22440 ? 6 : 3;
        final WorldTile[] tiles = new WorldTile[amount];
        for (int i = 0; i < amount; i++) {
            if (helwyr.getInstance().getAvailableTiles().size() == 0)
                break;
            WorldTile tile = helwyr.getInstance().getAvailableTiles().get(Utils.random(helwyr.getInstance().getAvailableTiles().size()));
            helwyr.getInstance().addMushroom(tiles[i] = tile);
            World.sendProjectile(helwyr, tile, 6122, 70, 10, 50, 2, 10, 0);
        }
        WorldTasksManager.schedule(new WorldTask() {
            private final WorldObject[] objects = new WorldObject[amount];
            private boolean second;

            @Override
            public void run() {
                if (helwyr.isDead() || helwyr.hasFinished()) {
                    for (int i = 0; i < amount; i++) {
                        if (tiles[i] == null || objects[i] == null)
                            continue;
                        for (Player p : helwyr.getInstance().getPlayers())
                            p.getPackets().sendGraphics(new Graphics(6125), tiles[i]);
                        helwyr.getInstance().removeMushroom(tiles[i]);
                        if (World.containsObjectWithId(objects[i], objects[i].getId()))
                            World.removeObject(objects[i]);
                    }
                    stop();
                    return;
                }
                if (!second) {
                    for (int i = 0; i < amount; i++) {
                        if (tiles[i] == null)
                            continue;
                        for (Player p : helwyr.getInstance().getPlayers())
                            p.getPackets().sendGraphics(new Graphics(6124), tiles[i]);
                        World.spawnObject(objects[i] = new WorldObject(101900, 10, 3, tiles[i]));
                    }
                } else {
                    for (int i = 0; i < amount; i++) {
                        if (tiles[i] == null || objects[i] == null)
                            continue;
                        for (Player p : helwyr.getInstance().getPlayers())
                            p.getPackets().sendGraphics(new Graphics(6125), tiles[i]);
                        helwyr.getInstance().removeMushroom(tiles[i]);
                        if (World.containsObjectWithId(objects[i], objects[i].getId()))
                            World.removeObject(objects[i]);
                    }
                    stop();
                }
                second = true;
            }
        }, 0, 97);
        return 4;
    }

    /**
     * Sends the bleed swipe attack.
     */
    private final int bleed(Helwyr npc, Entity target) {
        npc.setNextForceTalk(new ForceTalk("YOU. WILL. BLEED!"));
        npc.setNextAnimation(new Animation(28214));
        npc.resetWalkSteps();
        npc.setCannotMove(true);
        npc.setNextGraphics(new Graphics(6126));
        final WorldTile bleedTile = new WorldTile(target.getX(), target.getY(), 1);
        WorldTasksManager.schedule(new WorldTask() {
            private int ticks;

            @Override
            public void run() {
                if (ticks == 3) {
                    npc.getInstance().getPlayers().forEach(p -> {
                        if (p.getDistance(bleedTile) < 2) {
                            p.applyHit(new Hit(target, npc.getId() == 22440 ? Utils.random(300, 900) : Utils.random(150, 500), HitLook.MELEE_DAMAGE));
                            addBleedEffect(npc, p, true);
                            if (npc.getId() == 22440) {
                                p.sendMessage("Helwyr's claws injure you.");
                                p.getPrayer().closeAllPrayers();
                                p.setPrayerDelay(5000);
                            }
                        }
                        if (p.getFamiliar() != null && p.getFamiliar().getDistance(bleedTile) < 2 && p.getFamiliar().getDefinitions().hasAttackOption())
                            p.getFamiliar().applyHit(new Hit(npc, npc.getId() == 22440 ? Utils.random(300, 900) : Utils.random(150, 500), HitLook.MELEE_DAMAGE));
                    });
                } else if (ticks == 4) {
                    npc.setCannotMove(false);
                    npc.setTarget(target);
                    stop();
                }
                ticks++;
            }

        }, 0, 0);
        return 7;
    }

    private final void addBleedEffect(Helwyr npc, Player p, final boolean bleedAttack) {
        final int bleed = p.getTemporaryAttributtes().get("bleed") == null ? 0 : (int) p.getTemporaryAttributtes().get("bleed");
        if (!bleedAttack && bleed == 0)
            return;
        p.getTemporaryAttributtes().put("bleed", npc.getId() == 22440 ? (bleed + 10 > 50 ? 50 : bleed + 10) : (bleed + 5 > 25 ? 25 : bleed + 5));
        p.getTemporaryAttributtes().put("bleedTime", Utils.currentTimeMillis());
        if (bleed != 0) {
            p.getPackets().sendPlayerMessage(1, 15263739, "Helwyr's continued attacks cause you to lose even more blood!", true);
        } else {
            p.getPackets().sendPlayerMessage(1, 15263739, "Helwyr's attacks cause you to lose blood.", true);
        }
    }

    private final int frenzy(Helwyr npc, Entity target) {
        npc.setNextForceTalk(new ForceTalk("You cannot escape me. Aaaargh!"));
        npc.setNextAnimation(new Animation(28215));
        npc.resetWalkSteps();
        npc.getCombat().reset();
        npc.setCannotMove(true);
        WorldTasksManager.schedule(new WorldTask() {
            private int ticks;
            private int direction = (int) (Math.round(npc.getDirection() / 45.51) / 45);
            private final List<Player> players = new ArrayList<Player>();

            @Override
            public void run() {
                if (ticks > 0)
                    direction = (direction - 2 < 0 ? (8 + (direction - 2)) : direction - 2);
                final byte[] dirs = Utils.DIRS[direction];
                final WorldTile tile = ticks == 0 ? new WorldTile(target) : new WorldTile(npc.getCoordFaceX(npc.getSize()) + (dirs[0] * 3), npc.getCoordFaceY(npc.getSize()) + (dirs[1] * 3), npc.getPlane());
                npc.getInstance().getPlayers().forEach(p -> {
                    if (p.getDistance(tile) < 3) {
                        delayHit(npc, 0, target, getMeleeHit(npc, getRandomMaxHit(npc, npc.getId() == 22440 ? 800 : 400, NPCCombatDefinitionConstants.MELEE, target)));
                        if (!players.contains(p)) {
                            addBleedEffect(npc, p, false);
                            players.add(p);
                        }
                    }
                    if (p.getFamiliar() != null && p.getFamiliar().getDistance(tile) < 3 && p.getFamiliar().getDefinitions().hasAttackOption())
                        p.getFamiliar().applyHit(new Hit(npc, getRandomMaxHit(npc, npc.getId() == 22440 ? 800 : 400, NPCCombatDefinitionConstants.MELEE, target), HitLook.MELEE_DAMAGE));
                });
                if (ticks++ == 4) {
                    npc.setCannotMove(false);
                    npc.getCombat().setTarget(target);
                    stop();
                    return;
                }
            }
        }, 0, 1);
        return 12;
    }

    private final int howl(Helwyr npc, Entity target) {
        npc.setNextAnimation(new Animation(28213));
        npc.setNextGraphics(new Graphics(6127));
        if (npc.getId() == 22440)
            ((CMHelwyr) npc).incrementHowlStage();
        WorldTasksManager.schedule(new WorldTask() {
            @Override
            public void run() {
                final int amount = (npc.getInstance().getAliveWolves() == 3 ? 1 : 2);
                for (int i = 0; i < amount; i++)
                    npc.getInstance().getWolves().add(new CywirAlpha(npc.getId() == 22440 ? 22441 : 22439, npc.getInstance().getWorldTile(Utils.random(27, 45), Utils.random(27, 45)), -1, true, true));
            }
        });
        return 4;
    }
}