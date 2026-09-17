package com.rs.game.npc.kalphiteking;

import com.rs.Settings;
import com.rs.cores.CoresManager;
import com.rs.cores.FixedLengthRunnable;
import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.ForceMovement;
import com.rs.game.Graphics;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.NewProjectile;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.map.bossInstance.impl.KalphiteKingInstance;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.combat.NPCCombatDefinition;
import com.rs.game.npc.combat.NPCCombatDefinitionConstants;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.content.HybridTokenDistributor;
import com.rs.game.player.content.contracts.ContractHandler;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Logger;
import com.rs.utils.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("serial")
public class KalphiteKing extends NPC {
    public static KalphiteKingAttacks[][] ATTACKS = {
            {KalphiteKingAttacks.DISMEMBER_ATTACK, KalphiteKingAttacks.QUAKE_ATTACK, KalphiteKingAttacks.SLAUGHTER_ATTACK},
            {KalphiteKingAttacks.SINGLE_ATTACK, KalphiteKingAttacks.BLUE_ATTACK, KalphiteKingAttacks.BLEED_ATTACK,
                    KalphiteKingAttacks.SINGLE_ATTACK, KalphiteKingAttacks.DOUBLE_ATTACK},
            {KalphiteKingAttacks.BINDING_SHOT_ATTACK, KalphiteKingAttacks.FRAGMENTATION_SHOT_ATTACK, KalphiteKingAttacks.RICOCHET_ATTACK,
                    KalphiteKingAttacks.BOMBARDMENT_ATTACK, KalphiteKingAttacks.INCENDIARY_SHOT_ATTACK}};
    public static KalphiteKingAttacks[][] SPECIAL_ATTACKS = {
            {KalphiteKingAttacks.SHOVE_ATTACK, KalphiteKingAttacks.BARGE_ATTACK, KalphiteKingAttacks.BEETLEJUICE_ATTACK},
            {KalphiteKingAttacks.STUN_ATTACK, KalphiteKingAttacks.DIG_ATTACK, KalphiteKingAttacks.BARGE_ATTACK},
            {KalphiteKingAttacks.STUN_ATTACK, KalphiteKingAttacks.BEETLEJUICE_ATTACK, KalphiteKingAttacks.DIG_ATTACK}};

    public enum KalphiteKingAttacks {
        DISMEMBER_ATTACK() {
            @Override
            public int sendAttack(final KalphiteKing kalphiteKing, final Entity target) {
                kalphiteKing.setNextAnimation(new Animation(19449));
                final Hit hit = CombatScript.getMeleeHit(kalphiteKing,
                        CombatScript.getMaxHit(kalphiteKing, 300, NPCCombatDefinitionConstants.MELEE, target));
                CombatScript.delayHit(kalphiteKing, 1, target, hit);
                return 6;
            }
        },
        QUAKE_ATTACK() {
            @Override
            public int sendAttack(final KalphiteKing kalphiteKing, final Entity target) {
                kalphiteKing.setNextAnimation(new Animation(19435));
                kalphiteKing.setNextGraphics(new Graphics(3734));
                for (final Entity t : kalphiteKing.getPossibleTargets()) {
                    if (Utils.isOnRange(kalphiteKing, t, 3)) {
                        final Hit hit = CombatScript.getMeleeHit(kalphiteKing,
                                CombatScript.getRandomMaxHit(kalphiteKing, 300, NPCCombatDefinitionConstants.MELEE, t));
                        CombatScript.delayHit(kalphiteKing, 1, t, hit);
                        if (hit.getDamage() > 0) {
                            if (t instanceof Player) {
                                ((Player) t).getSkills().drainLevel(Skills.DEFENCE, hit.getDamage() / 200);
                            }
                        }
                    }
                }
                return 6;
            }
        },
        SLAUGHTER_ATTACK() {
            @Override
            public int sendAttack(final KalphiteKing kalphiteKing, final Entity target) {
                kalphiteKing.setNextAnimation(new Animation(19449));
                final Hit hit = CombatScript.getMeleeHit(kalphiteKing,
                        CombatScript.getMaxHit(kalphiteKing, 300, NPCCombatDefinitionConstants.MELEE, target));
                CombatScript.delayHit(kalphiteKing, 1, target, hit);
                return 6;
            }
        },
        BINDING_SHOT_ATTACK() {
            @Override
            public int sendAttack(final KalphiteKing kalphiteKing, final Entity target) {
                kalphiteKing.setNextAnimation(new Animation(19450));
                for (final Entity t : kalphiteKing.getPossibleTargets()) {
                    final NewProjectile projectile = new NewProjectile(kalphiteKing.getMiddleWorldTile(), t, 3747, 30, 20, 40, 15, 40, 0);
                    final int duration = projectile.getTime() / 600;
                    final Hit hit = CombatScript.getRangeHit(kalphiteKing,
                            CombatScript.getMaxHit(kalphiteKing, 300, NPCCombatDefinitionConstants.RANGE, t));
                    World.sendProjectile(projectile);
                    WorldTasksManager.schedule(new WorldTask() {

                        @Override
                        public void run() {
                            if (kalphiteKing.isDead() || kalphiteKing.hasFinished() || t.isDead() || t.hasFinished()) {
                                return;
                            }
                            CombatScript.delayHit(kalphiteKing, 0, t, hit);
                            if (hit.getDamage() > 0) {
                                t.addFreezeDelay(7200, true);
                            }
                        }
                    }, duration);
                }
                return 6;
            }
        },

        FRAGMENTATION_SHOT_ATTACK() {
            @Override
            public int sendAttack(final KalphiteKing kalphiteKing, final Entity target) {
                kalphiteKing.setNextAnimation(new Animation(19450));
                for (final Entity t : kalphiteKing.getPossibleTargets()) {
                    final NewProjectile projectile = new NewProjectile(kalphiteKing.getMiddleWorldTile(), t, 3747, 30, 20, 40, 15, 40, 0);
                    final int duration = projectile.getTime() / 600;
                    final Hit hit = CombatScript.getRangeHit(kalphiteKing,
                            CombatScript.getMaxHit(kalphiteKing, 500, NPCCombatDefinitionConstants.RANGE, t));
                    World.sendProjectile(projectile);
                    CombatScript.delayHit(kalphiteKing, duration, t, hit);
                }
                return 6;
            }
        },
        RICOCHET_ATTACK() {
            @Override
            public int sendAttack(final KalphiteKing kalphiteKing, final Entity target) {
                kalphiteKing.setNextAnimation(new Animation(19450));
                NewProjectile projectile = new NewProjectile(kalphiteKing.getMiddleWorldTile(), target, 3747, 30, 20, 40, 15, 40, 0);
                int duration = projectile.getTime() / 600;
                Hit hit = CombatScript.getRangeHit(kalphiteKing,
                        CombatScript.getMaxHit(kalphiteKing, 500, NPCCombatDefinitionConstants.RANGE, target));
                CombatScript.delayHit(kalphiteKing, duration, target, hit);
                World.sendProjectile(projectile);
                for (final Entity t : kalphiteKing.getPossibleTargets()) {
                    if (t == target || !t.withinDistance(target, 3)) {
                        continue;
                    }
                    projectile = new NewProjectile(target, t, 3747, 20, 20, 0, 0, 35, 0);
                    duration = projectile.getTime() / 600;
                    hit = CombatScript.getRangeHit(kalphiteKing, CombatScript.getMaxHit(kalphiteKing, 500, NPCCombatDefinitionConstants.RANGE, t));
                    World.sendProjectile(projectile);
                    CombatScript.delayHit(kalphiteKing, duration, t, hit);
                }
                return 6;
            }
        },
        BOMBARDMENT_ATTACK() {// gfx 3525

            @Override
            public int sendAttack(final KalphiteKing kalphiteKing, final Entity target) {
                kalphiteKing.setNextAnimation(new Animation(19450));
                final NewProjectile projectile = new NewProjectile(kalphiteKing.getMiddleWorldTile(), target, 65535, 30, 20, 40, 15, 40, 0);
                final int duration = projectile.getTime() / 600;
                target.setNextGraphics(new Graphics(3525, 50, 130));
                World.sendProjectile(projectile);
                final Hit hit = CombatScript.getRangeHit(kalphiteKing,
                        CombatScript.getMaxHit(kalphiteKing, 300, NPCCombatDefinitionConstants.RANGE, target));
                WorldTasksManager.schedule(new WorldTask() {
                    @Override
                    public void run() {
                        target.setNextGraphics(new Graphics(3525));
                        CombatScript.delayHit(kalphiteKing, 0, target, hit);
                        for (final Entity t : kalphiteKing.getPossibleTargets()) {
                            if (t == target || !t.withinDistance(target, 2)) {
                                continue;
                            }
                            final Hit hit = CombatScript.getRangeHit(kalphiteKing,
                                    CombatScript.getMaxHit(kalphiteKing, 300, NPCCombatDefinitionConstants.RANGE, t));
                            CombatScript.delayHit(kalphiteKing, 0, t, hit);
                        }
                    }
                }, duration);

                return 6;
            }
        },

        INCENDIARY_SHOT_ATTACK() {// gfx 3521

            @Override
            public int sendAttack(final KalphiteKing kalphiteKing, final Entity target) {
                kalphiteKing.setNextAnimation(new Animation(19450));
                for (final Entity t : kalphiteKing.getRandomTargets(3)) {
                    final NewProjectile projectile = new NewProjectile(kalphiteKing.getMiddleWorldTile(), t, 3747, 30, 20, 40, 15, 40, 0);
                    final Hit hit = CombatScript.getRangeHit(kalphiteKing,
                            CombatScript.getMaxHit(kalphiteKing, Utils.random(50, 100), NPCCombatDefinitionConstants.RANGE, t));
                    final int duration = projectile.getTime() / 600;
                    World.sendProjectile(projectile);
                    WorldTasksManager.schedule(new WorldTask() {
                        int count = 0;

                        @Override
                        public void run() {
                            if (kalphiteKing.isDead() || kalphiteKing.hasFinished() || t.isDead() || t.hasFinished()) {
                                stop();
                                return;
                            }
                            if (count == 0) {
                                CombatScript.delayHit(kalphiteKing, 0, t, hit);
                                t.setNextGraphics(new Graphics(3521));
                            } else if (count == 3) {
                                World.sendGraphics(null, new Graphics(3522), new WorldTile(t));
                                t.applyHit(new Hit(kalphiteKing, Utils.random(150, 501), HitLook.RANGE_DAMAGE));
                                stop();
                            }
                            count++;
                        }
                    }, duration, 1);
                }
                return 6;
            }
        },
        SINGLE_ATTACK() {
            @Override
            public int sendAttack(final KalphiteKing kalphiteKing, final Entity target) {
                kalphiteKing.setNextAnimation(new Animation(19448));
                kalphiteKing.setNextGraphics(new Graphics(3742));
                for (final Entity t : kalphiteKing.getPossibleTargets()) {
                    final WorldTile startTile = new WorldTile(t);
                    final NewProjectile projectile = new NewProjectile(kalphiteKing.getMiddleWorldTile(), startTile, 3743, 30, 20, 55, 35, 35, 0);
                    final int duration = projectile.getTime() / 600;
                    World.sendProjectile(projectile);
                    WorldTasksManager.schedule(new WorldTask() {
                        int count = 0;

                        @Override
                        public void run() {
                            if (count == 0) {
                                World.sendStillProjectile(startTile, startTile, 3743, 20, 65, 100, 0, 0, 0);
                            } else if (count == 2) {
                                World.sendGraphics(null, new Graphics(3752), startTile);
                                for (final Entity t : kalphiteKing.getPossibleTargets()) {
                                    if (!t.withinDistance(startTile, 2)) {
                                        continue;
                                    }
                                    final Hit hit = CombatScript.getMagicHit(kalphiteKing,
                                            CombatScript.getMaxHit(kalphiteKing, 500, NPCCombatDefinitionConstants.MAGE, t));
                                    CombatScript.delayHit(kalphiteKing, 0, t, hit);
                                }
                                stop();
                            }
                            count++;
                        }

                    }, duration, 1);
                }
                return 6;
            }
        },
        BLUE_ATTACK() {
            @Override
            public int sendAttack(final KalphiteKing kalphiteKing, final Entity target) {
                kalphiteKing.setNextAnimation(new Animation(19448));
                kalphiteKing.setNextGraphics(new Graphics(3757));
                for (final Entity t : kalphiteKing.getPossibleTargets()) {
                    final NewProjectile projectile = new NewProjectile(kalphiteKing.getMiddleWorldTile(), t, 3758, 30, 20, 55, 35, 35, 0);
                    final int duration = projectile.getTime() / 600;
                    final Hit hit = CombatScript.getMagicHit(kalphiteKing,
                            CombatScript.getMaxHit(kalphiteKing, 450, NPCCombatDefinitionConstants.MAGE, t));
                    World.sendProjectile(projectile);
                    WorldTasksManager.schedule(new WorldTask() {

                        @Override
                        public void run() {
                            if (kalphiteKing.isDead() || kalphiteKing.hasFinished() || t.isDead() || t.hasFinished()) {
                                return;
                            }
                            CombatScript.delayHit(kalphiteKing, 0, t, hit);
                            t.setNextGraphics(new Graphics(3759));
                            if (hit.getDamage() > 0) {
                                t.addFreezeDelay(3000, true);
                            }
                        }
                    }, duration);
                }
                return 6;
            }
        },
        BLEED_ATTACK() {
            @Override
            public int sendAttack(final KalphiteKing kalphiteKing, final Entity target) {// since
                // we
                // have
                // no
                // way
                // to
                // free
                // bleed
                final KalphiteKingAttacks attack = KalphiteKingAttacks.SINGLE_ATTACK;
                return attack.sendAttack(kalphiteKing, target);
            }
        },
        DOUBLE_ATTACK() {
            @Override
            public int sendAttack(final KalphiteKing kalphiteKing, final Entity target) {
                kalphiteKing.setNextAnimation(new Animation(19448));
                kalphiteKing.setNextGraphics(new Graphics(3742));
                for (final Entity t : kalphiteKing.getPossibleTargets()) {
                    for (int i = 0; i < 2; i++) {
                        final WorldTile startTile = new WorldTile(t.getX() + (i == 0 ? -1 : 1), t.getY(), t.getPlane());
                        final NewProjectile projectile = new NewProjectile(kalphiteKing.getMiddleWorldTile(), startTile, 3743, 30, 20, 55, 35, 35,
                                0);
                        final int duration = projectile.getTime() / 600;
                        World.sendProjectile(projectile);
                        WorldTasksManager.schedule(new WorldTask() {
                            int count = 0;

                            @Override
                            public void run() {
                                if (count == 0) {
                                    World.sendStillProjectile(startTile, startTile, 3743, 20, 65, 100, 0, 0, 0);
                                } else if (count == 2) {
                                    World.sendGraphics(null, new Graphics(3752), startTile);
                                    for (final Entity t : kalphiteKing.getPossibleTargets()) {
                                        if (!t.withinDistance(startTile, 2)) {
                                            continue;
                                        }
                                        final Hit hit = CombatScript.getMagicHit(kalphiteKing,
                                                CombatScript.getMaxHit(kalphiteKing, 500, NPCCombatDefinitionConstants.MAGE, t));
                                        CombatScript.delayHit(kalphiteKing, 0, t, hit);
                                    }
                                    stop();
                                }
                                count++;
                            }

                        }, duration, 1);
                    }
                }
                return 6;
            }
        },
        SHOVE_ATTACK() {
            @Override
            public int sendAttack(final KalphiteKing kalphiteKing, final Entity target) {
                final int dirToShove = kalphiteKing.faceOrthogonal(target);
                kalphiteKing.setNextAnimation(new Animation(19449));
                for (final Entity t : kalphiteKing.getPossibleTargets()) {
                    if (!Utils.isOnRange(kalphiteKing, t, 1) || (!Utils.isInDirection(kalphiteKing, t, dirToShove))) {
                        continue;
                    }
                    final Player p = (Player) t;
                    final Hit hit = CombatScript.getMeleeHit(kalphiteKing,
                            CombatScript.getMaxHit(kalphiteKing, 200, NPCCombatDefinitionConstants.MELEE, t));
                    CombatScript.delayHit(kalphiteKing, 1, t, hit);
                    final WorldTile to = Utils.getFreeTile(new WorldTile(t), dirToShove, 2, 1);
                    p.lock();
                    p.setNextAnimationForce(new Animation(10070));
                    p.setNextForceMovement(new ForceMovement(to, 1, Utils.getOppositeDirection(dirToShove)));
                    WorldTasksManager.schedule(new WorldTask() {

                        @Override
                        public void run() {
                            p.unlock();
                            p.setNextWorldTile(to);
                            kalphiteKing.setTarget(target);
                        }
                    });
                }
                return 6;
            }
        },
        BARGE_ATTACK() {
            @Override
            public int sendAttack(final KalphiteKing kalphiteKing, final Entity target) {
                final int dirToBarge = kalphiteKing.faceOrthogonal(target);
                final int size = kalphiteKing.getSize();
                final WorldTile toBarge = Utils.getFreeTile(kalphiteKing, dirToBarge, 8, size);
                final int distance = Utils.getDistance(kalphiteKing, toBarge);
                if (distance < 4) {
                    kalphiteKing.setNextAnimation(new Animation(19447));
                    kalphiteKing.setNextGraphics(new Graphics(3735));
                    WorldTasksManager.schedule(new WorldTask() {

                        @Override
                        public void run() {
                            for (final Entity t : kalphiteKing.getPossibleTargets()) {
                                if (Utils.isOnRange(kalphiteKing, t, 2)) {
                                    final Hit hit = new Hit(kalphiteKing, Utils.random(200, 401), HitLook.REGULAR_DAMAGE);
                                    CombatScript.delayHit(kalphiteKing, 0, t, hit);
                                }
                            }
                            kalphiteKing.setTarget(target);
                            kalphiteKing.setCantFollowUnderCombat(false);
                        }

                    }, 1);
                    return 6;
                }
                final int totalDelay = 10 - (8 - distance);
                kalphiteKing.setCantInteract(true);
                final WorldTile startTile = new WorldTile(kalphiteKing);
                kalphiteKing.setNextAnimation(new Animation(19461 - (8 - distance)));
                kalphiteKing.setNextForceMovement(new ForceMovement(startTile, 5, toBarge, totalDelay, dirToBarge));
                CoresManager.getServiceProvider().scheduleFixedLengthTask(new FixedLengthRunnable() {

                    private int count = 0;

                    @Override
                    public boolean repeat() {
                        try {
                            if (count >= 4 && count < totalDelay) {
                                for (final Entity t : kalphiteKing.getPossibleTargets()) {
                                    final double stepModifier = (((double) distance) / ((double) (totalDelay - 4)));
                                    final int stepsMoved = (int) Math.round((count - 3) * stepModifier);
                                    final WorldTile currentKingTile = Utils.getNextWorldTile(startTile, dirToBarge, stepsMoved);
                                    final WorldTile getMouthCoords = new WorldTile(
                                            currentKingTile.getX()
                                                    + (dirToBarge == ForceMovement.EAST ? +6 : dirToBarge == ForceMovement.WEST ? -1 : +2),
                                            currentKingTile.getY() + (dirToBarge == ForceMovement.SOUTH ? -1
                                                    : dirToBarge == ForceMovement.NORTH ? +6 : +2),
                                            currentKingTile.getPlane());
                                    if (((Player) t).isLocked() || !Utils.colides(currentKingTile, t, size, 1)
                                            || !Utils.isInDirection(startTile, t, dirToBarge)) {
                                        continue;
                                    }
                                    ((Player) t).lock();
                                    t.setNextAnimation(new Animation(10070));
                                    final int dis = distance - stepsMoved;
                                    final WorldTile to = Utils.getFreeTile(getMouthCoords, dirToBarge, dis, 1);
                                    t.setNextForceMovement(
                                            new ForceMovement(t, 0, to, (totalDelay - count) - 1, Utils.getOppositeDirection(dirToBarge)));
                                    WorldTasksManager.schedule(new WorldTask() {
                                        @Override
                                        public void run() {
                                            ((Player) t).unlock();
                                            t.setNextWorldTile(to);
                                            final Hit hit = new Hit(kalphiteKing, Utils.random(200, 401), HitLook.REGULAR_DAMAGE);
                                            CombatScript.delayHit(kalphiteKing, 0, t, hit);
                                        }
                                    }, (totalDelay - count) - 1);
                                }
                            } else if (count == totalDelay) {
                                kalphiteKing.setCantInteract(false);
                                kalphiteKing.setCantFollowUnderCombat(false);
                                kalphiteKing.setNextWorldTile(toBarge);
                                kalphiteKing.checkAgressivity();
                                return false;
                            }
                            count++;
                        } catch (final Exception e) {
                            Logger.getGlobal().catching(e);
                        }
                        return true;
                    }

                }, 480, 600, TimeUnit.MILLISECONDS);
                return totalDelay + 4;
            }

        },

        BEETLEJUICE_ATTACK() {
            @Override
            public int sendAttack(final KalphiteKing kalphiteKing, final Entity target) {
                kalphiteKing.setNextAnimation(new Animation(19464));
                kalphiteKing.setNextGraphics(new Graphics(3738));
                final Hit hit = CombatScript.getRegularHit(kalphiteKing, (int) (target.getHitpoints() * 0.5));
                if (hit.getDamage() <= 0) {
                    hit.setDamage(1);
                }
                final NewProjectile projectile = new NewProjectile(kalphiteKing.getMiddleWorldTile(), target, 3739, 30, 20, 60, 15, 20, 0);
                final int duration = projectile.getTime() / 600;
                World.sendProjectile(projectile);
                ((Player) target).stopAll();
                ((Player) target).lock();
                ((Player) target).getPackets().sendGameMessage("You have been frozen by the kalphite king's attack.");
                WorldTasksManager.schedule(new WorldTask() {

                    @Override
                    public void run() {
                        CombatScript.delayHit(kalphiteKing, 0, target, hit);
                        ((Player) target).unlock();
                        target.setNextGraphics(new Graphics(3740));
                    }
                }, duration + 2);
                return 7;
            }

        },

        STUN_ATTACK() {
            @Override
            public int sendAttack(final KalphiteKing kalphiteKing, final Entity target) {
                final int attackStyle = kalphiteKing.getId() - 16697;
                kalphiteKing.setNextAnimation(new Animation(attackStyle == 1 ? 19448 : 19450));
                for (final Entity t : kalphiteKing.getPossibleTargets()) {
                    final Hit hit = new Hit(kalphiteKing, Utils.random(40, 130), attackStyle == 1 ? HitLook.MAGIC_DAMAGE : HitLook.RANGE_DAMAGE);
                    final NewProjectile projectile = new NewProjectile(kalphiteKing, t, 65535, 30, 20, attackStyle == 1 ? 55 : 40, 15, 30, 0);
                    final int duration = projectile.getTime() / 600;
                    WorldTasksManager.schedule(new WorldTask() {

                        @Override
                        public void run() {
                            CombatScript.delayHit(kalphiteKing, 0, t, hit);
                            t.addFreezeDelay(3000, true);
                        }
                    }, duration + 1);
                }
                return 6;
            }

        },

        DIG_ATTACK() {
            @Override
            public int sendAttack(final KalphiteKing kalphiteKing, final Entity target) {
                final int size = kalphiteKing.getSize();
                kalphiteKing.setCantInteract(true);
                kalphiteKing.resetReceivedDamage();
                kalphiteKing.setNextAnimation(new Animation(19453));
                kalphiteKing.setNextGraphics(new Graphics(3746));
                WorldTasksManager.schedule(new WorldTask() {
                    int count = 0;

                    @Override
                    public void run() {
                        if (kalphiteKing == null || kalphiteKing.isDead()) {
                            stop();
                            return;
                        }
                        Entity t = target;
                        final ArrayList<Entity> possibleTargets = kalphiteKing.getPossibleTargets();
                        if (possibleTargets.isEmpty()) {
                            kalphiteKing.setFinished(false);
                            stop();
                            return;
                        }
                        if (t == null || t.isDead() || !possibleTargets.contains(t)) {
                            t = possibleTargets.get(Utils.random(possibleTargets.size()));
                        }
                        if (count == 0) {
                            kalphiteKing.setFinished(true);
                        } else if (count == 6) {
                            kalphiteKing.setFinished(false);
                            kalphiteKing.setTarget(null);
                            kalphiteKing.setNextWorldTile(t.transform(-(size / 2), -(size / 2), 0));
                            kalphiteKing.setNextAnimation(new Animation(19451));
                        } else if (count == 7) {
                            for (final Entity targets : kalphiteKing.getPossibleTargets()) {
                                if (!Utils.colides(targets, kalphiteKing)) {
                                    continue;
                                }
                                targets.setNextAnimation(new Animation(20338));
                                final WorldTile toTile = kalphiteKing.findBestLocation(new WorldTile(targets));
                                if (toTile != null) {
                                    targets.setNextForceMovement(new ForceMovement(toTile, 4, getMoveDirection(targets)));
                                    ((Player) targets).lock(3);
                                    targets.setNextWorldTile(toTile);
                                }
                                CombatScript.delayHit(kalphiteKing, 1, targets,
                                        CombatScript.getRegularHit(kalphiteKing, Utils.random(400, 550)));
                            }

                        } else if (count == 9) {
                            kalphiteKing.setCantInteract(false);
                            kalphiteKing.setNextAnimation(new Animation(-1));
                            kalphiteKing.setTarget(t);
                            stop();
                        }
                        count++;
                    }
                }, 6, 1);
                return 13;
            }

        };

        public int sendAttack(final KalphiteKing kalphiteKing, final Entity target) {
            return 0;
        }

    }

    private final transient KalphiteKingInstance instance;
    private int attackRotation, sequentialRotation, shieldId;
    private ExiledKalphiteMarauder[] minions;

    public KalphiteKing(final int id, final WorldTile tile, final KalphiteKingInstance instance) {
        super(id, tile, -1, true);
        this.instance = instance;
        setCapDamage(1000);
        setForceMultiArea(true);
        setNoDistanceCheck(true);
        setIntelligentRouteFinder(true);
    }

    public int faceOrthogonal(final Entity target) {
        final byte[] or = Utils.getOrthogonalDirection(this, target);
        final int orthogonalDir = Utils.getFaceDirection(Utils.getAngle(or[0], or[1]));
        final WorldTile faceTile = transform((orthogonalDir == ForceMovement.EAST ? 10 : orthogonalDir == ForceMovement.WEST ? -10 : 0),
                (orthogonalDir == ForceMovement.NORTH ? 10 : orthogonalDir == ForceMovement.SOUTH ? -10 : 0), 0);
        setTarget(null);
        resetWalkSteps();
        setNextFaceWorldTile(faceTile);
        return orthogonalDir;
    }

    private WorldTile findBestLocation(final WorldTile playerL) {
        final ArrayList<WorldTile> unCheckedTeleTiles = new ArrayList<>();
        for (int x = (getX() - getSize()); x <= getX() + getSize(); x++) {
            for (int y = (getY() - getSize()); y <= getY() + getSize(); y++) {
                if (Utils.colides(new WorldTile(x, y, getPlane()), getLastWorldTile(), 1, 5)) {
                    continue;
                }
                unCheckedTeleTiles.add(new WorldTile(x, y, getPlane()));
            }
        }
        final ArrayList<WorldTile> checkedTeleTiles = new ArrayList<>();
        for (final WorldTile checkTile : unCheckedTeleTiles) {
            if (!World.canMoveNPC(checkTile.getPlane(), checkTile.getX(), checkTile.getY(), 1)) {
                continue;
            }
            checkedTeleTiles.add(checkTile);
        }
        int farestDistance = Integer.MAX_VALUE;
        int index = 0;
        for (int i = 0; i < checkedTeleTiles.size(); i++) {
            final WorldTile checkedTile = checkedTeleTiles.get(i);
            final int distance = Utils.getDistance(checkedTile, playerL);
            if (distance < farestDistance) {
                index = i;
                farestDistance = distance;
            }
        }
        return checkedTeleTiles.isEmpty() ? null : checkedTeleTiles.get(index);
    }

    public void switchAttackStyle() {
        final int currentAttackStyle = getId() - 16697;
        final int[] gfxId = {3750, 3749, 3751};
        final int nextAttackStyle = (currentAttackStyle + Utils.random(2) + 1) % 3;
        setNextNPCTransformation(16697 + nextAttackStyle);
        setNextGraphics(new Graphics(gfxId[nextAttackStyle]));
        attackRotation = 0;
        sequentialRotation = 0;
        setCantFollowUnderCombat(false);
        setForceFollowClose(nextAttackStyle == 0);
    }

    public boolean spawnMinions() {
        if (getHitpoints() <= (getMaxHitpoints() * 0.75)) {
            if (minions == null) {
                minions = new ExiledKalphiteMarauder[15];
            }
            final boolean under25Health = getHitpoints() <= (getMaxHitpoints() * 0.25);
            boolean spawned = false;
            for (int i = under25Health ? 5 : 0; i < (under25Health ? 15 : 5); i++) {
                final WorldTile spawnTile = instance.getTile(new WorldTile(2962, 1750, 0)).transform(Utils.random(21), Utils.random(21), 0);
                if (minions[i] != null) {
                    break;
                }
                minions[i] = new ExiledKalphiteMarauder(16706, spawnTile, instance);
                minions[i].setNextAnimation(new Animation(19492));
                minions[i].setNextGraphics(new Graphics(3748));
                for (final Entity target : getPossibleTargets()) {
                    if (Utils.colides(minions[i], target)) {
                        CombatScript.delayHit(minions[i], 1, target, new Hit(minions[i], 100, HitLook.MELEE_DAMAGE));
                    }
                }
                spawned = true;
            }
            return spawned;
        }
        return false;
    }

    public void activateShield() {
        if (getHitpoints() <= getMaxHitpoints() * 0.55 && shieldId == 0) {
            shieldId = 3736 + Utils.random(2);
            CoresManager.getServiceProvider().executeWithDelay(() -> shieldId = 0, Utils.random(5500, 6500), TimeUnit.MILLISECONDS);
        }
    }

    private static int getMoveDirection(final Entity player) {
        switch (player.getDirection()) {
            case 10240:// north-east
                return ForceMovement.NORTH_EAST;
            case 6144:// north-west
                return ForceMovement.NORTH_WEST;
            case 2048:// south-west
                return ForceMovement.SOUTH_WEST;
            case 14336:// south-east
                return ForceMovement.SOUTH_EAST;
            case 4096:// west
                return ForceMovement.WEST;
            case 12288:// east
                return ForceMovement.EAST;
            case 8192:// north
                return ForceMovement.NORTH;
            case 0:// south
            default:
                return ForceMovement.SOUTH;
        }
    }

    public ArrayList<Entity> getRandomTargets(final int maxSize) {
        final ArrayList<Entity> possibleTargets = getPossibleTargets();
        Collections.shuffle(possibleTargets);
        final ArrayList<Entity> randomTargets = new ArrayList<Entity>(maxSize);
        for (int i = 0; i < maxSize; i++) {
            if (i > (possibleTargets.size() - 1)) {
                continue;
            }
            randomTargets.add(possibleTargets.get(i));
        }
        return randomTargets;
    }

    @Override
    public boolean switchTarget() {
        if (getPossibleTargets().size() != 0) {
            final Random random = new Random();
            final ArrayList<Entity> targets = getPossibleTargets();
            final Entity target = targets.get(random.nextInt(targets.size()));
            setTarget(target);
            return true;
        }
        return false;
    }

    @Override
    public ArrayList<Entity> getPossibleTargets(final boolean checkNPCs, final boolean checkPlayers) {
        final ArrayList<Entity> possibleTarget = new ArrayList<Entity>();
        for (final Player player : instance.getPlayers()) {
            if (player == null || player.isDead() || player.hasFinished() || !player.isRunning() || player.getAppearence().isHidden()) {
                continue;
            }
            possibleTarget.add(player);
        }
        return possibleTarget;
    }

    @Override
    public boolean checkAgressivity() {
        final ArrayList<Entity> possibleTarget = getPossibleTargets();
        if (!possibleTarget.isEmpty()) {
            final Entity target = possibleTarget.get(Utils.random(possibleTarget.size()));
            setTarget(target);
            target.setAttackedBy(target);
            target.setFindTargetDelay(Utils.currentTimeMillis() + 10000);
            return true;
        }
        return false;
    }

    public int getAttackRotation() {
        return attackRotation;
    }

    public void setAttackRotation(final int attackRotation) {
        this.attackRotation = attackRotation;
    }

    public int getSequentialRotation() {
        return sequentialRotation;
    }

    public void setSequentialRotation(final int sequentialRotation) {
        this.sequentialRotation = sequentialRotation;
    }

    public int getShieldId() {
        return shieldId;
    }

    public KalphiteKingInstance getKKInstance() {
        return instance;
    }

    @Override
    public void finish() {
        super.finish();
        if (minions != null) {
            for (NPC n : minions) {
                if (n != null) {
                    n.finish();
                }
            }
            minions = null;
        }
    }

    @Override
    public void handleIngoingHit(final Hit hit) {
        if (shieldId != 0) {
            setNextGraphics(new Graphics(shieldId));
            if (shieldId == 3737) {
                hit.setHealHit();
            }
        }
        super.handleIngoingHit(hit);
    }

    @Override
    public Hit handleOutgoingHit(final Hit hit, final Entity target) {
        if (shieldId == 3736) {
            final Hit h = new Hit(null, hit.getDamage(), HitLook.HEALED_DAMAGE);
            h.setHealHit();
            applyHit(h);
        }
        return hit;
    }

    @Override
    public void sendDeath(final Entity source) {
        final NPCCombatDefinition defs = getCombatDefinitions();
        resetWalkSteps();
        getCombat().removeTarget();
        if (source instanceof Player) {
            source.deathResetCombat();
        }
        setNextAnimation(null);
        WorldTasksManager.schedule(new WorldTask() {
            int loop;

            @Override
            public void run() {
                if (loop == 0) {
                    setNextAnimationForce(new Animation(19480));
                } else if (loop >= defs.getDeathDelay()) {
                    if (source instanceof Player) {
                        Player plr = (Player) source;
                        if (plr.isGroupIronman()) {
                            plr.gimTracker.incrementBpGained(5);
                        }
                        plr.getControlerManager().processNPCDeath(KalphiteKing.this);
                        plr.getActivityTimersManager().finishBossTimer(KalphiteKing.this);
                        ContractHandler.updateContract(plr, KalphiteKing.this);
                        HybridTokenDistributor.rollForToken(plr, HybridTokenDistributor.Activity.KALPHITE_KING);
                    }
                    setId(16697);
                    drop();
                    setLocation(getRespawnTile());
                    finish();
                    setRespawnTask();
                    stop();
                }
                loop++;
            }
        }, 0, 1);
    }

    @Override
    public void reset() {
        super.reset();
        attackRotation = 0;
        sequentialRotation = 0;
        setCantFollowUnderCombat(false);
    }

    @Override
    public void setRespawnTask() {
        if (getKKInstance() != null && getKKInstance().isFinished() && getKKInstance().getSettings().hasTimeRemaining()) {
            return;
        }
        if (!hasFinished()) {
            reset();
            setLocation(getRespawnTile());
            finish();
        }
        int respawnDelay = 60;
        respawnDelay /= getKKInstance().getSettings().getSpawnSpeed();
        if (Settings.DEBUG) {
            Logger.getGlobal().info("Respawn task initiated: [" + getName() + "]; time: [" + respawnDelay + "].");
        }
        WorldTasksManager.schedule(new WorldTask() {
            int count = 0;

            @Override
            public void run() {
                if (count == 0) {
                    reset();
                    final int random = Utils.random(3);
                    final int id = 16697 + random;
                    setFinished(false);
                    setNPC(id);
                    World.addNPC(KalphiteKing.this);
                    setLastRegionId(0);
                    World.updateEntityRegion(KalphiteKing.this);
                    loadMapRegions();
                    checkMultiArea();
                    setNextAnimation(new Animation(19451));
                    setForceFollowClose(id == 16697);
                    setDirection(Utils.getAngle(0, -1));
                    setCantInteract(true);
                } else if (count >= 3) {
                    setCantInteract(false);
                    setNextAnimation(new Animation(-1));
                    stop();
                }
                count++;
            }

        }, respawnDelay, 1);
    }

    @Override
    public double getMeleePrayerMultiplier() {
        return 0.35;
    }

    @Override
    public double getMagePrayerMultiplier() {
        return 0.35;
    }

    @Override
    public double getRangePrayerMultiplier() {
        return 0.35;
    }

}
