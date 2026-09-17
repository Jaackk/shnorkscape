package com.rs.game.npc.themagister;

import com.rs.cores.CoresManager;
import com.rs.cores.FixedLengthRunnable;
import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.ForceMovement;
import com.rs.game.ForceTalk;
import com.rs.game.Graphics;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.map.bossInstance.impl.TheMagisterInstance;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.combat.NPCCombatDefinitionConstants;
import com.rs.game.player.Player;
import com.rs.game.player.content.HybridTokenDistributor;
import com.rs.game.player.content.contracts.ContractHandler;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Colors;
import com.rs.utils.Logger;
import com.rs.utils.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("serial")
public class TheMagister extends NPC {

    public static final String[] messages = new String[] { "Your soul - it contains such power! I must have it!", "Let us see how you fare against corruption.", "You will not defeat me!" };

    private final TheMagisterInstance instance;
    private ImperialAkh[] imperialAkhs;
    private SoulObelisk soulObelisk;
    private int autoAttacks;
    private int stage;
    private CorruptedSoulObelisk corruptedSoulObelisk;
    private boolean dieing;
    
    public TheMagister(int id, WorldTile tile, TheMagisterInstance instance) {
        super(id, tile, -1, true, true);
        setForceMultiArea(true);
        setIntelligentRouteFinder(true);
        setForceAgressive(true);
        setForceTargetDistance(64);
        this.instance = instance;
        imperialAkhs = new ImperialAkh[2];
        autoAttacks = 3;
    }

    @Override
    public boolean checkAgressivity() {
        if (getHitpoints() == 0)
            return false;
        ArrayList<Entity> possibleTarget = getPossibleTargets();
        if (!possibleTarget.isEmpty()) {
            Entity target = possibleTarget.get(Utils.random(possibleTarget.size()));
            setTarget(target);
            target.setAttackedBy(target);
            target.setFindTargetDelay(Utils.currentTimeMillis() + 10000);
            return true;
        }
        return false;
    }

    public ArrayList<Entity> getPossibleTargets() {
        return getPossibleTargets(false, true);
    }

    @Override
    public ArrayList<Entity> getPossibleTargets(boolean checkNPCs, boolean checkPlayers) {
        ArrayList<Entity> possibleTarget = new ArrayList<Entity>();
        for (Player player : instance.getPlayers()) {
            if (player == null || player.isDead())
                continue;
            possibleTarget.add(player);
        }
        return possibleTarget;
    }

    @Override
    public void spawn() {
        super.spawn();
        instance.getSoulObelisk().setId(109158);
        World.spawnObject(instance.getSoulObelisk());
        setCantInteract(true);
        setNextForceTalk(new ForceTalk("You think you can challenge me! I hold the Crossing!"));
        setNextAnimation(new Animation(30821));
        setNextGraphics(new Graphics(6744));
        WorldTasksManager.schedule(new WorldTask() {

            @Override
            public void run() {
                setNextAnimation(new Animation(-1));
                setNextGraphics(new Graphics(-1));
                setCantInteract(false);
                instance.updateInterface(true);
            }
        }, 2);
    }

    @Override
    public void processHit(Hit hit) {
        super.processHit(hit);
        if ((stage == 0 && this.getHitpoints() <= (this.getMaxHitpoints() * 0.75)) || (stage == 1 && this.getHitpoints() <= (this.getMaxHitpoints() * 0.50)) || (stage == 2 && this.getHitpoints() <= (this.getMaxHitpoints() * 0.25))) {
            increaseStage();
        }
        instance.updateInterface(false);
    }

    private void increaseStage() {
        setNextForceTalk(new ForceTalk(messages[stage]));
        stage++;
        if (stage == 1) {
            soulObelisk = new SoulObelisk(24769, new WorldTile(instance.getSoulObelisk()), this);
            instance.sendMessage("<col=ff0000>The soul obelisk begins to drain your soul. Attack it to restore your soul!");
        } else if (stage == 2) {
            corruptedSoulObelisk = new CorruptedSoulObelisk(24770, instance.getSoulObelisk().transform(0, -20, 0), this);
            corruptedSoulObelisk.spawn();
            instance.sendMessage("Let us see how you fare against corruption.");
        }
    }

    @Override
    public void handleIngoingHit(Hit hit) {
        super.handleIngoingHit(hit);
    }

    @Override
    public void setNextForceTalk(ForceTalk nextForceTalk) {
        super.setNextForceTalk(nextForceTalk);
        if (instance.getPlayer() != null && !instance.getPlayer().hasFinished())
            instance.getPlayer().getPackets().sendGameMessage("<col=ff0000>The Magister: <col=ffffff>" + nextForceTalk.getText());
    }

    @Override
    public void sendDeath(Entity source) {
        if (dieing)
            return;
        dieing = true;
        resetWalkSteps();
        getCombat().removeTarget();
        if (source instanceof Player) {
            source.deathResetCombat();
        }
        setNextAnimation(new Animation(-1));
        setNextGraphics(new Graphics(-1));
        clearArea();
        Player player = instance.getPlayer();
        player.getInterfaceManager().closeOverlay(true);
        WorldTasksManager.schedule(new WorldTask() {
            int loop;

            @Override
            public void run() {
                if (loop == 0) {
                    setNextAnimation(new Animation(30798));
                } else if (loop >= 3) {
                    if (source instanceof Player) {
                        if(player.isGroupIronman()) {
                            player.gimTracker.incrementBpGained(6);
                        }
                        player.getControlerManager().processNPCDeath(TheMagister.this);
                        ContractHandler.updateContract(((Player) source), TheMagister.this);
                        player.getActivityTimersManager().finishBossTimer(TheMagister.this);
                        HybridTokenDistributor.rollForToken(player, HybridTokenDistributor.Activity.THE_MAGISTER);
                    }
                    drop();
                    reset();
                    setLocation(getRespawnTile());
                    finish();
                    player.sendMessage(Colors.wrap(Colors.RED, getName()) + " Kill count: " + Colors.wrap(Colors.RED, String.valueOf(player.getKillStatistics(136))));
                    dieing = false;
                    stop();
                }
                loop++;
            }
        }, 0, 1);
    }

    public void clearArea() {
        World.removeObject(instance.getSoulObelisk());
        instance.getSoulObelisk().setId(109157);
        World.spawnObject(instance.getSoulObelisk());
        for (ImperialAkh akh : imperialAkhs) {
            if (akh != null && !akh.hasFinished())
                akh.sendDeath(null);
        }
        if (corruptedSoulObelisk != null && !corruptedSoulObelisk.hasFinished())
            corruptedSoulObelisk.sendDeath(null);
        imperialAkhs = new ImperialAkh[2];
        if (soulObelisk != null && !soulObelisk.hasFinished())
            soulObelisk.finish();
        soulObelisk = null;
        if (instance.getPlayer() != null && !instance.getPlayer().hasFinished())
            instance.getPlayer().removeHPReduction(100);
    }

    public void respawn() {
        if (instance == null || instance.getTheMagister() != this)
            return;
        if (!hasFinished()) {
            reset();
            setLocation(this.getRespawnTile());
            finish();
        }
        spawn();
    }

    @Override
    public boolean restoreHitPoints() {
        boolean restore = super.restoreHitPoints();
        instance.updateInterface(false);
        return restore;
    }

    @Override
    public void setNextAnimationForce(Animation nextAnimation) {
        if (getTemporaryAttributtes().get("cantDoAnimation") != null)
            return;
        super.setNextAnimationForce(nextAnimation);
    }

    @Override
    public void setNextAnimation(Animation animation) {
        if (getTemporaryAttributtes().get("cantDoAnimation") != null)
            return;
        super.setNextAnimation(animation);
    }

    @Override
    public void setNextAnimationNoPriority(Animation nextAnimation) {
        if (getTemporaryAttributtes().get("cantDoAnimation") != null)
            return;
        super.setNextAnimationNoPriority(nextAnimation);
    }

    @Override
    public double getMagePrayerMultiplier() {
        return 0.8;
    }

    @Override
    public double getMeleePrayerMultiplier() {
        return 0.8;
    }

    @Override
    public double getRangePrayerMultiplier() {
        return 0.8;
    }

    public TheMagisterInstance getInstance() {
        return instance;
    }

    @Override
    public void reset() {
        autoAttacks = 3;
        stage = 0;
        imperialAkhs = new ImperialAkh[2];
        soulObelisk = null;
        super.reset();
    }

    private void spawnUnstableMixture(WorldTile tile) {
        WorldObject unstableMixture = new WorldObject(109154, 10, 0, tile);
        NPC hitBarNPC = new NPC(24771, tile, -1, false, true);
        long totalTime = Utils.random(2400, 4800);
        long cycle = totalTime + Utils.currentTimeMillis();
        UnstableMixtureHitBar bar = new UnstableMixtureHitBar(totalTime, cycle);
        hitBarNPC.getNextHitBars().add(bar);
        World.spawnObject(unstableMixture);
        CoresManager.getServiceProvider().scheduleFixedLengthTask(new FixedLengthRunnable() {
            int loop = 0;

            @Override
            public boolean repeat() {
                try {
                    if (Utils.currentTimeMillis() > cycle || !World.containsObjectWithId(unstableMixture, unstableMixture.getId())) {
                        hitBarNPC.finish();
                        if (World.containsObjectWithId(unstableMixture, unstableMixture.getId())) {
                            World.removeObject(unstableMixture);
                            World.sendGraphics(null, new Graphics(6718), tile);
                            if (Utils.isOnRange(TheMagister.this, tile, 1, 1, 1)) {
                                TheMagister.this.applyHit(new Hit(TheMagister.this, 400, HitLook.UNBLOCKABLE_REGULAR_DAMAGE));
                            }
                            if (Utils.isOnRange(instance.getPlayer(), tile, 1, 1, 1)) {
                                instance.getPlayer().applyHit(new Hit(TheMagister.this, 400, HitLook.UNBLOCKABLE_REGULAR_DAMAGE));
                            }
                            if (instance.getPlayer().getFamiliar() != null && !instance.getPlayer().getFamiliar().hasFinished() && Utils.isOnRange(instance.getPlayer().getFamiliar(), tile, 1, 1, 1))
                                instance.getPlayer().getFamiliar().applyHit(new Hit(TheMagister.this, 100, HitLook.UNBLOCKABLE_REGULAR_DAMAGE));
                            for (ImperialAkh akh : imperialAkhs) {
                                if (akh != null && !akh.hasFinished() && !akh.isDead())
                                    akh.applyHit(new Hit(TheMagister.this, 400, HitLook.UNBLOCKABLE_REGULAR_DAMAGE));
                            }
                        }
                        return false;
                    }
                    if (loop == 0 || (loop % 50) == 0) {
                        hitBarNPC.getNextHitBars().add(bar);
                    }
                    loop++;
                    return true;
                } catch (Exception e) {
                    Logger.getGlobal().catching(e);
                    return false;
                }
            }
        }, 1, 1, TimeUnit.MILLISECONDS);
    }

    public int getAliveImperialAkhsCount() {
        int count = 0;
        for (ImperialAkh akh : imperialAkhs) {
            if (akh != null && !akh.hasFinished() && !akh.isDead())
                count++;
        }
        return count;
    }

    public int getAttackSpeed() {
        return 5;
    }

    public int getAutoAttacks() {
        return autoAttacks;
    }

    public void setAutoAttacks(int autoAttacks) {
        this.autoAttacks = autoAttacks;
    }

    public int getStage() {
        return stage;
    }

    public boolean isDieing() {
        return dieing;
    }

    public enum TheMagisterAttacks {
        ATTACK() {

            public int sendAttack(TheMagister theMagister, Player target) {
                int autoAttacks = theMagister.getAutoAttacks();
                if (autoAttacks > 0) {
                    theMagister.setAutoAttacks(autoAttacks - 1 <= 0 ? 0 : autoAttacks - 1);
                    return AUTO_ATTACK.sendAttack(theMagister, target);
                } else {
                    List<TheMagisterAttacks> possibleAttacks = new ArrayList<TheMagisterAttacks>(Arrays.asList(TheMagisterAttacks.values()));
                    possibleAttacks.removeIf(ATTACK::equals);
                    possibleAttacks.removeIf(AUTO_ATTACK::equals);
                    if (theMagister.getAliveImperialAkhsCount() > 0)
                        possibleAttacks.removeIf(IMPERIAL_AKHS::equals);
                    TheMagisterAttacks attack = possibleAttacks.get(Utils.random(possibleAttacks.size()));
                    theMagister.setAutoAttacks(3);
                    return attack.sendAttack(theMagister, target);
                }
            }
        },

        AUTO_ATTACK() {

            public int sendAttack(TheMagister theMagister, Player target) {
                theMagister.setNextAnimation(new Animation(theMagister.getCombatDefinitions().getAttackAnim()));
                theMagister.setNextGraphics(new Graphics(6704));
                World.sendProjectileCycles(theMagister, target, 6705, 40, 25, 30, 40, 5, 50);
                long projectileCycles = 600;
                CoresManager.getServiceProvider().scheduleFixedLengthTask(new FixedLengthRunnable() {
                    @Override
                    public boolean repeat() {
                        try {
                            if (theMagister.isDead() || theMagister.hasFinished() || target.hasFinished() || target.isDead())
                                return false;
                            target.setNextGraphics(new Graphics(6706));
                            int maxhit = theMagister.getMaxHit();
                            if (theMagister.getStage() == 3)
                                maxhit *= 1.5;
                            int damage = CombatScript.getMaxHit(theMagister, maxhit, NPCCombatDefinitionConstants.MAGE, target);
                            CombatScript.delayHit(theMagister, 0, target, CombatScript.getMagicHit(theMagister, damage));
                            return false;
                        } catch (Exception e) {
                            Logger.getGlobal().catching(e);
                            return false;
                        }
                    }
                }, projectileCycles, 600, TimeUnit.MILLISECONDS);
                return theMagister.getAttackSpeed();
            }
        },

        SOUL_SIPHON() {
            public int sendAttack(TheMagister theMagister, Player target) {
                theMagister.setNextForceTalk(new ForceTalk("Your sacrifice allows me to live!"));
                theMagister.setNextAnimation(new Animation(18394));
                theMagister.getTemporaryAttributtes().put("cantDoAnimation", Boolean.TRUE);
                target.getTemporaryAttributtes().put("SoulSiphon", theMagister);
                CoresManager.getServiceProvider().scheduleFixedLengthTask(new FixedLengthRunnable() {
                    int hitsCount = 0;

                    @Override
                    public boolean repeat() {
                        try {
                            if (hitsCount >= 3 || theMagister.isDead() || theMagister.hasFinished() || target.hasFinished() || target.isDead() || target.getTemporaryAttributtes().get("SoulSiphon") == null) {
                                theMagister.getTemporaryAttributtes().remove("cantDoAnimation");
                                target.getTemporaryAttributtes().remove("SoulSiphon");
                                return false;
                            }
                            int damage = Utils.random(100, 200);
                            CombatScript.delayHit(theMagister, 0, target, CombatScript.getMagicHit(theMagister, damage));
                            theMagister.heal(damage, 0, 0, true);
                            hitsCount++;
                            return true;
                        } catch (Exception e) {
                            Logger.getGlobal().catching(e);
                            return false;
                        }
                    }
                }, 1000, 1000, TimeUnit.MILLISECONDS);
                return 9;
            }
        },

        UNSTABLE_MIXTURE() {
            public int sendAttack(TheMagister theMagister, Player target) {
                theMagister.setNextForceTalk(new ForceTalk("Dodge this!"));
                theMagister.setNextAnimation(new Animation(30795));
                theMagister.setNextGraphics(new Graphics(6716));
                WorldTile tile = getRandomTile(theMagister, target);
                World.sendProjectileCycles(theMagister, tile, 6717, 40, 25, 30, 40, 5, 50);
                CoresManager.getServiceProvider().scheduleFixedLengthTask(new FixedLengthRunnable() {
                    @Override
                    public boolean repeat() {
                        try {
                            if (theMagister.isDead() || theMagister.hasFinished() || target.hasFinished() || target.isDead())
                                return false;
                            theMagister.spawnUnstableMixture(tile);
                            return false;
                        } catch (Exception e) {
                            Logger.getGlobal().catching(e);
                            return false;
                        }
                    }
                }, 600, 600, TimeUnit.MILLISECONDS);
                return 5;
            }
        },

        IMPERIAL_AKHS() {
            public int sendAttack(TheMagister theMagister, Player target) {
                theMagister.setNextForceTalk(new ForceTalk("Those who fall shall rise again!"));
                theMagister.setNextAnimation(new Animation(30794));
                theMagister.setNextGraphics(new Graphics(6714));
                for (int i = 0; i < theMagister.imperialAkhs.length; i++) {
                    if (theMagister.imperialAkhs[i] != null && !theMagister.imperialAkhs[i].hasFinished()) {
                        theMagister.imperialAkhs[i].finish();
                        theMagister.imperialAkhs[i] = null;
                    }
                    theMagister.imperialAkhs[i] = new ImperialAkh(24766 + Utils.random(3), getRandomTile(theMagister, target, 1, 2, i == 1 ? (theMagister.imperialAkhs[0].getRespawnTile()) : null), theMagister);
                }
                return 5;
            }
        },

        POWER_BLAST() {
            public int sendAttack(TheMagister theMagister, Player target) {
                theMagister.setNextAnimation(new Animation(30789));
                theMagister.setNextGraphics(new Graphics(6707));
                World.sendProjectileCycles(theMagister, target, 6708, 0, 0, 80, 100, 0, 0);
                CoresManager.getServiceProvider().scheduleFixedLengthTask(new FixedLengthRunnable() {
                    @Override
                    public boolean repeat() {
                        try {
                            target.setNextGraphics(new Graphics(6709));
                            int damage = Utils.random(180, 301);
                            CombatScript.delayHit(theMagister, 0, target, CombatScript.getRangeHit(theMagister, damage));
                            return false;
                        } catch (Exception e) {
                            Logger.getGlobal().catching(e);
                            return false;
                        }
                    }
                }, 2340, 600, TimeUnit.MILLISECONDS);
                return 5;
            }
        },

        SHADOW_STEP() {
            public int sendAttack(TheMagister theMagister, Player target) {
                boolean standStill = Utils.isOnRange(theMagister, target, 0);
                theMagister.setNextForceTalk(new ForceTalk(standStill ? "Stand still!" : "You dare fight me?"));
                theMagister.setNextAnimation(new Animation(standStill ? 30792 : 30790));
                if (standStill) {
                    theMagister.faceEntity(target);
                    target.addFreezeDelay(3000);
                    int damage = Utils.random(180, 301);
                    CombatScript.delayHit(theMagister, 1, target, CombatScript.getMeleeHit(theMagister, damage));
                    WorldTasksManager.schedule(new WorldTask() {
                        @Override
                        public void run() {
                            final WorldTile tile = TheMagister.getSurgeTile(theMagister, 0, -7, false);
                            theMagister.lock(2);
                            theMagister.setNextForceMovement(new ForceMovement(theMagister, 0, tile, 1, Utils.getAngle(tile.getX() - theMagister.getX(), tile.getY() - theMagister.getY())));
                            WorldTasksManager.schedule(new WorldTask() {

                                @Override
                                public void run() {
                                    theMagister.setNextWorldTile(tile);
                                    theMagister.setNextAnimation(new Animation(30793));
                                }
                            }, 1);
                        }
                    }, 1);
                } else {
                    final WorldTile tile = getOnRangeRandomTile(target, 0);
                    theMagister.cancelFaceEntityNoCheck();
                    theMagister.setNextFaceEntity(null);
                    theMagister.setNextFaceWorldTile(tile);
                    WorldTasksManager.schedule(new WorldTask() {
                        @Override
                        public void run() {
                            theMagister.lock(2);
                            theMagister.setNextForceMovement(new ForceMovement(theMagister, 0, tile, 1, Utils.getAngle(tile.getX() - theMagister.getX(), tile.getY() - theMagister.getY())));
                            WorldTasksManager.schedule(new WorldTask() {
                                boolean face = false;

                                @Override
                                public void run() {
                                    if (!face) {
                                        theMagister.setNextWorldTile(tile);
                                        theMagister.setNextAnimation(new Animation(30791));
                                        if (Utils.isOnRange(target, tile, 0, 1, 1)) {
                                            theMagister.setNextFaceEntity(target);
                                            int damage = Utils.random(180, 301);
                                            CombatScript.delayHit(theMagister, 0, target, CombatScript.getMeleeHit(theMagister, damage));
                                            if (!target.getPrayer().isMeleeProtecting()) {
                                                target.getPrayer().closeProtectionPrayers();
                                                target.setPrayerDelay(3000);
                                            }
                                            stop();
                                        }
                                        face = true;
                                    } else {
                                        theMagister.setNextFaceEntity(target);
                                        stop();
                                    }

                                }
                            }, 1, 1);
                        }
                    }, 1);
                }
                return 9;
            }
        }

        ;
        public int sendAttack(TheMagister theMagister, Player target) {
            return 0;
        }

        private static WorldTile getRandomTile(TheMagister theMagister, Player target) {
            return getRandomTile(theMagister, target, 0, 1, null);
        }

        private static WorldTile getRandomTile(TheMagister theMagister, Player target, int skip, int max, WorldTile skipTile) {
            List<WorldTile> possibleTiles = new ArrayList<WorldTile>();
            for (int x = target.getX() - 10; x <= target.getX() + 10; x++) {
                for (int y = target.getY() - 10; y <= target.getY() + 10; y++) {
                    WorldTile checkTile = new WorldTile(x, y, target.getPlane());
                    if (theMagister.instance.insideBattleArea(checkTile) && World.canMoveNPC(checkTile, 1) && (!Utils.isOnRange(checkTile, target, skip, 1, 1)) && Utils.isOnRange(checkTile, target, max, 1, 1) && (skipTile == null || (checkTile != null && !checkTile.matches(skipTile)))) {
                        possibleTiles.add(checkTile);
                    }
                }
            }
            return possibleTiles.get(Utils.random(possibleTiles.size()));
        }

        private static WorldTile getOnRangeRandomTile(Player target, int range) {
            List<WorldTile> possibleTiles = new ArrayList<WorldTile>();
            for (int x = target.getX() - 10; x <= target.getX() + 10; x++) {
                for (int y = target.getY() - 10; y <= target.getY() + 10; y++) {
                    WorldTile checkTile = new WorldTile(x, y, target.getPlane());
                    if (World.canMoveNPC(checkTile, 1) && Utils.isOnRange(checkTile, target, range, 1, 1)) {
                        possibleTiles.add(checkTile);
                    }
                }
            }
            return possibleTiles.get(Utils.random(possibleTiles.size()));
        }
    }

    public static WorldTile getSurgeTile(TheMagister entity, int start, int end, boolean increment) {
        return getSurgeTile(entity, start, end, increment, null);
    }

    public static WorldTile getSurgeTile(TheMagister entity, int start, int end, boolean increment, Entity target) {
        byte[] dirs = Utils.getDirection(entity.getDirection());
        WorldTile lastStep = null;
        for (int steps = start; increment ? steps < end : steps > end; steps += (increment ? 1 : -1)) {
            WorldTile step = new WorldTile(entity.getX() + (dirs[0] * steps), entity.getY() + (dirs[1] * steps), entity.getPlane());
            if (target != null && Utils.colides(target.getX(), target.getY(), target.getSize(), step.getX(), step.getY(), entity.getSize()) || !World.isTileFree(step.getPlane(), step.getX(), step.getY(), entity.getSize()))
                break;
            lastStep = step;
        }
        return lastStep;
    }
    
    @Override
    public boolean isFreezeImmune() {
        return true;
    }

    @Override
    public boolean isStunImmune() {
        return true;
    }

}
