package com.rs.game.npc.eds;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.rs.Settings;
import com.rs.cores.CoresManager;
import com.rs.cores.FixedLengthRunnable;
import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.Graphics;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.Projectile;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.hitbar.HitBar;
import com.rs.game.item.Item;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.combat.NPCCombatDefinitionConstants;
import com.rs.game.player.Player;
import com.rs.game.player.PerkManager.DonationPerk;
import com.rs.game.player.actions.herblore.herbicide.Herbicide;
import com.rs.game.player.content.eds.EliteDungeonsConstants;
import com.rs.game.player.content.eds.rooms.EliteDungeonHandledRoom;
import com.rs.game.player.content.eds.rooms.ed1.Room2;
import com.rs.game.player.content.items.Bonecrusher;
import com.rs.game.player.content.items.CoinAccumulator;
import com.rs.game.player.content.items.Ectoplasmator;
import com.rs.game.player.content.petperks.PetPerk;
import com.rs.game.player.content.petperks.PetPerkUtils;
import com.rs.game.player.controllers.EliteDungeonController;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Colors;
import com.rs.utils.Logger;
import com.rs.utils.Utils;
import com.rs.utils.data.parsers.npcs.pojos.NPCDrop;

import lombok.val;

public class TheSanctumGuardian extends EliteDungeonBoss {

    private static final long serialVersionUID = -5730621439526514730L;

    private int attackProgress;
    private transient List<Flame> flames;
    private long flameHitCycle;
    private transient List<TheSanctumGuardianMinion> minions;
    private int waterTorrentCount;
    public boolean dead;

    public TheSanctumGuardian(int id, WorldTile tile, EliteDungeonHandledRoom room) {
        super(id, tile, room);
        flames = new ArrayList<Flame>();
        minions = new ArrayList<TheSanctumGuardianMinion>();
        waterTorrentCount = 0;
        attackProgress = 0;
        dead = false;
        setCantFollowUnderCombat(true);
    }

    @Override
    public void spawn() {
        setCantFollowUnderCombat(true);
        super.spawn();
        flames = new ArrayList<Flame>();
        minions = new ArrayList<TheSanctumGuardianMinion>();
        waterTorrentCount = 0;
        attackProgress = 0;
        dead = false;
        getTemporaryAttributtes().remove("insideTorrent");
    }

    @Override
    public void reset() {
        super.reset();
        setCantFollowUnderCombat(true);
        waterTorrentCount = 0;
        attackProgress = 0;
        dead = false;
        setNextFaceWorldTile(getRespawnTile().transform(4, getSize() + 1, 0));
    }

    @Override
    public void processNPC() {
        if (isDead() || hasFinished()) {
            Room2 room = (Room2) getRoom();
            if (room == null || room.getRoom() == null || !room.getRoom().isLoaded())
                return;
            if (room.getBossBlock1().isSpawned()) {
                room.getBossBlock1().getLinkedNPCs().remove(this);
            }
            if (room.getBossBlock2().isSpawned()) {
                room.getBossBlock2().getLinkedNPCs().remove(this);
            }
            if (room.getBossBlock1().isSpawned() && room.isUnlocked(room.getBossBlock1()))
                room.getBossBlock1().remove();
            if (room.getBossBlock2().isSpawned() && room.isUnlocked(room.getBossBlock2()))
                room.getBossBlock2().remove();
            return;
        }
        super.processNPC();
        processBossBlock();
        if (flameHitCycle == 0 || Utils.currentTimeMillis() >= flameHitCycle) {
            for (Flame flame : flames) {
                if (flame != null && flame.getDamage() > 0) {
                    for (Entity e : getPossibleTargets()) {
                        if (e == null || e.hasFinished() || e.isDead() || !Utils.isOnRange(flame.tile, e, 1, 1, 1))
                            continue;
                        e.applyHit(new Hit(this, flame.getDamage(), HitLook.REGULAR_DAMAGE));
                    }
                }
            }
            flameHitCycle = Utils.currentTimeMillis() + 600;
        }
    }

    @Override
    public void handleIngoingHit(Hit hit) {
        super.handleIngoingHit(hit);
        if (getCombat().getTarget() == null) {
            List<Entity> targets = getPossibleTargets();
            if (!targets.isEmpty())
                getCombat().setTarget(targets.get(Utils.random(targets.size())));
        }
    }

    @Override
    public void finish() {
        clearArea();
        super.finish();
    }

    public void clearArea() {
        if (flames != null) {
            for (Flame flame : flames) {
                if (flame != null)
                    flame.remove(this);
            }
            flames.clear();
        }
        if (minions != null) {
            for (TheSanctumGuardianMinion minion : minions) {
                if (minion != null && !minion.hasFinished() && !minion.isDead())
                    minion.finish();
            }
            minions.clear();
        }
    }

    @Override
    public boolean checkAgressivity() {
        return false;
    }

    @Override
    public void setNextAnimationForce(Animation nextAnimation) {
        if (getTemporaryAttributtes() == null || getTemporaryAttributtes().get("insideTorrent") != null)
            return;
        super.setNextAnimationForce(nextAnimation);
    }

    @Override
    public void setNextAnimation(Animation animation) {
        if (getTemporaryAttributtes() == null || getTemporaryAttributtes().get("insideTorrent") != null)
            return;
        super.setNextAnimation(animation);
    }

    @Override
    public void setNextAnimationNoPriority(Animation nextAnimation) {
        if (getTemporaryAttributtes() == null || getTemporaryAttributtes().get("insideTorrent") != null)
            return;
        super.setNextAnimationNoPriority(nextAnimation);
    }

    @Override
    public void setNextGraphics(Graphics nextGraphics) {
        if (getTemporaryAttributtes() == null || getTemporaryAttributtes().get("insideTorrent") != null)
            return;
        super.setNextGraphics(nextGraphics);
    }

    @Override
    public void setNextFaceEntity(Entity entity) {
        if (getTemporaryAttributtes() == null || getTemporaryAttributtes().get("insideTorrent") != null)
            return;
        super.setNextFaceEntity(entity);
    }

    @Override
    public void setNextFaceWorldTile(WorldTile nextFaceWorldTile) {
        if (hasFinished())
            return;
        if (getTemporaryAttributtes() == null || getTemporaryAttributtes().get("insideTorrent") != null)
            return;
        super.setNextFaceWorldTile(nextFaceWorldTile);
    }

    public void addFlame(WorldTile location) {
        Flame f = new Flame(location);
        flames.add(f);
        World.sendGraphics(this, new Graphics(6984), location);
        final TheSanctumGuardian thisNPC = this;
        WorldTasksManager.schedule(new WorldTask() {

            @Override
            public void run() {
                if (isDead() || hasFinished() || !flames.contains(f) || stopAttack()) {
                    stop();
                    return;
                }
                World.sendGraphics(thisNPC, new Graphics(6984), location);
            }

        }, 40, 40);
    }

    public static class Flame {
        private final WorldTile tile;
        private final long lifeCycle;
        private final int startDamage;

        public Flame(WorldTile tile) {
            this.tile = tile;
            this.lifeCycle = Utils.currentTimeMillis();
            startDamage = Utils.random(50, 71);
        }

        public int getDamage() {
            long time = Utils.currentTimeMillis() - lifeCycle;
            int ticksPassed = (int) (time / 600);
            if (ticksPassed <= 0)
                return 0;
            int damage = startDamage + ((ticksPassed - 1) * 30);
            return damage >= 400 ? 400 : damage;
        }

        public void remove(TheSanctumGuardian npc) {
            World.sendGraphics(npc, new Graphics(-1), tile);
        }
    }

    public void processBossBlock() {
        if (getRoom() == null || !(getRoom() instanceof Room2))
            return;
        List<Entity> targets = getPossibleTargets();
        Room2 room = (Room2) getRoom();
        if (room.getBossBlock1().isSpawned()) {
            if ((getCombat().getTarget() == null && targets.isEmpty()) || (getCombat().getTarget() != null && !isInsideFightArea(getCombat().getTarget()))) {
                if (!targets.isEmpty()) {
                    getCombat().setTarget(targets.get(Utils.random(targets.size())));
                    room.getBossBlock1().addNPC(this);
                    room.getBossBlock1().spawn(true);
                } else if (room.getBossBlock1().getLinkedNPCs().contains(this)) {
                    room.getBossBlock1().getLinkedNPCs().remove(this);
                    getCombat().removeTarget();
                    reset();
                    setNextFaceEntity(null);
                    setNextFaceWorldTile(getRespawnTile().transform(4, getSize() + 1, 0));
                    setNextAnimation(new Animation(-1));
                    setNextGraphics(new Graphics(-1));
                    clearArea();
                    setNextWorldTile(getRespawnTile());
                    room.checkRemoveBlocks();
                }
            } else if (getCombat().getTarget() != null && isInsideFightArea(getCombat().getTarget())) {
                room.getBossBlock1().addNPC(this);
                room.getBossBlock1().spawn(true);
            }
            if (room.isUnlocked(room.getBossBlock1()))
                room.getBossBlock1().remove();
        } else {
            if (getCombat().getTarget() != null && isInsideFightArea(getCombat().getTarget())) {
                room.getBossBlock1().addNPC(this);
                room.getBossBlock1().spawn(true);
            }
        }
        if (room.getBossBlock2().isSpawned()) {
            if ((getCombat().getTarget() == null && targets.isEmpty()) || (getCombat().getTarget() != null && !isInsideFightArea(getCombat().getTarget()))) {
                if (!targets.isEmpty()) {
                    getCombat().setTarget(targets.get(Utils.random(targets.size())));
                    room.getBossBlock2().addNPC(this);
                    room.getBossBlock2().spawn(true);
                } else if (room.getBossBlock2().getLinkedNPCs().contains(this)) {
                    room.getBossBlock2().getLinkedNPCs().remove(this);
                    getCombat().removeTarget();
                    reset();
                    setNextFaceEntity(null);
                    setNextFaceWorldTile(getRespawnTile().transform(4, getSize() + 1, 0));
                    setNextAnimation(new Animation(-1));
                    setNextGraphics(new Graphics(-1));
                    clearArea();
                    setNextWorldTile(getRespawnTile());
                    room.checkRemoveBlocks();
                }
            } else if (getCombat().getTarget() != null && isInsideFightArea(getCombat().getTarget())) {
                room.getBossBlock2().addNPC(this);
                room.getBossBlock2().spawn(true);
            }
            if (room.isUnlocked(room.getBossBlock2()))
                room.getBossBlock2().remove();
        } else {
            if (getCombat().getTarget() != null && isInsideFightArea(getCombat().getTarget())) {
                room.getBossBlock2().addNPC(this);
                room.getBossBlock2().spawn(true);
            }
        }
    }

    @Override
    public boolean restoreHitPoints() {
        if (getRoom() == null)
            return false;
        for (Player p : getRoom().getRoom().getPlayers()) {
            if (p == null || p.isDead() || p.hasFinished() || !(p.getControlerManager().getControler() instanceof EliteDungeonController))
                continue;
            EliteDungeonController c = (EliteDungeonController) p.getControlerManager().getControler();
            if (c.getCurrent() != this)
                continue;
            c.updateInterface();
        }
        return false;
    }

    @Override
    public int getBossMapId() {
        return 40394;
    }

    public boolean stopAttack() {
        Entity target = getCombat().getTarget();
        return (target == null && getPossibleTargets().isEmpty()) || (target != null && !isInsideFightArea(target) && getPossibleTargets().isEmpty()) || dead;
    }

    @Override
    public void sendDeath(Entity source) {
        if (dead)
            return;
        dead = true;
        super.sendDeath(source);
    }

    public enum TheSanctumGuardianAttacks {
        ATTACK() {
            public int sendAttack(TheSanctumGuardian boss, Player target) {
                int attackProgress = boss.attackProgress;
                TheSanctumGuardianAttacks[] attacks = { AUTO_ATTACK, AUTO_ATTACK, AUTO_ATTACK, AUTO_ATTACK, WATER_SPLASH, AUTO_ATTACK, WATER_TORRENT, AUTO_ATTACK, PURPLE_FLAME, AUTO_ATTACK, WATER_SPLASH, AUTO_ATTACK };
                TheSanctumGuardianAttacks attack = attacks[attackProgress];
                if (boss.waterTorrentCount == 3) {
                    boss.waterTorrentCount = 0;
                    attack = SPAWN_MINIONS;
                } else
                    boss.attackProgress = boss.attackProgress + 1 >= attacks.length ? 6 : boss.attackProgress + 1;
                return attack.sendAttack(boss, target);
            }
        },
        AUTO_ATTACK() {
            public int sendAttack(TheSanctumGuardian boss, Player target) {
                boolean meleeAttack = Utils.getRandom(Utils.isOnRange(boss, target, 1) ? 2 : 10) == 0;
                boss.setNextAnimation(new Animation(meleeAttack ? 26556 : 26571));
                if (meleeAttack) {
                    for (Entity e : boss.getPossibleTargets()) {
                        if (e == null || e.hasFinished() || e.isDead() || !Utils.isOnRange(boss, e, 1))
                            continue;
                        int damage = CombatScript.getMaxHit(boss, boss.getMaxHit(), NPCCombatDefinitionConstants.MELEE, e);
                        CombatScript.delayHit(boss, 0, e, CombatScript.getMeleeHit(boss, damage));
                    }
                } else {
                    WorldTile tile = new WorldTile(target);
                    Projectile projectile = World.sendProjectileCycles(boss, tile, 6962, 54, 30, 20, 120, Utils.random(5), 350);
                    long projectileCycles = Utils.projectileTimeToMiliseconds(projectile);
                    CoresManager.getServiceProvider().scheduleFixedLengthTask(new FixedLengthRunnable() {
                        @Override
                        public boolean repeat() {
                            try {
                                if (boss.isDead() || boss.hasFinished() || boss.stopAttack())
                                    return false;
                                for (Entity e : boss.getPossibleTargets()) {
                                    if (e == null || e.hasFinished() || e.isDead() || !Utils.isOnRange(tile, e, 0, 1, 1))
                                        continue;
                                    int damage = CombatScript.getMaxHit(boss, boss.getMaxHit(), NPCCombatDefinitionConstants.RANGE, e);
                                    CombatScript.delayHit(boss, 0, e, CombatScript.getRangeHit(boss, damage));
                                }
                                return false;
                            } catch (Exception e) {
                                Logger.getGlobal().catching(e);
                                return false;
                            }
                        }
                    }, projectileCycles, 600, TimeUnit.MILLISECONDS);
                }
                return 4;
            }
        },
        WATER_SPLASH() {
            public int sendAttack(TheSanctumGuardian boss, Player target) {
                boss.setNextAnimation(new Animation(26568));
                boss.setNextGraphics(new Graphics(5565));
                CoresManager.getServiceProvider().scheduleFixedLengthTask(new FixedLengthRunnable() {
                    @Override
                    public boolean repeat() {
                        try {
                            if (boss.isDead() || boss.hasFinished() || boss.stopAttack())
                                return false;
                            for (Entity e : boss.getPossibleTargets()) {
                                if (e == null || e.hasFinished() || e.isDead() || !Utils.isOnRange(boss, e, 5))
                                    continue;
                                int damage = CombatScript.getMaxHit(boss, 500, NPCCombatDefinitionConstants.MELEE, e);
                                CombatScript.delayHit(boss, 0, e, CombatScript.getMeleeHit(boss, damage));
                            }
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
        WATER_TORRENT() {
            public int sendAttack(TheSanctumGuardian boss, Player target) {
                boss.waterTorrentCount++;
                boss.getCombat().removeTarget();
                boss.setNextFaceEntity(null);
                boss.setNextFaceWorldTile(boss.transform(4, boss.getSize() + 1, 0));
                boss.getTemporaryAttributtes().remove("insideTorrent");
                boss.setNextAnimation(new Animation(26565));
                boss.setNextGraphics(new Graphics(5564));
                boss.getTemporaryAttributtes().put("insideTorrent", Boolean.TRUE);
                WorldTile middleTile = boss.getMiddleWorldTile();
                byte[][] dirs = new byte[][] { { 0, 1 }, { -1, 1 }, { -1, 0 }, { -1, -1 }, { 0, -1 }, { 1, -1 }, { 1, 0 }, { 1, 1 }, { 0, 1 } };
                for (Entity e : boss.getPossibleTargets()) {
                    if (e == null || e.hasFinished() || e.isDead())
                        continue;
                    e.getTemporaryAttributtes().remove("skiphitcurrent");
                }
                CoresManager.getServiceProvider().scheduleFixedLengthTask(new FixedLengthRunnable() {
                    int loop;
                    int currentDir;

                    @Override
                    public boolean repeat() {
                        try {
                            if (boss.isDead() || boss.hasFinished() || boss.stopAttack()) {
                                boss.setNextFaceWorldTile(boss.transform(4, boss.getSize() + 1, 0));
                                boss.getTemporaryAttributtes().remove("insideTorrent");
                                boss.setNextFaceEntity(null);
                                boss.setNextAnimation(new Animation(-1));
                                boss.setNextGraphics(new Graphics(-1));
                                return false;
                            }
                            if (loop == 0 || loop % 300 == 0) {
                                for (int x = middleTile.getX() - 25; x <= middleTile.getX() + 25; x++) {
                                    for (int y = middleTile.getY() - 25; y <= middleTile.getY() + 25; y++) {
                                        WorldTile tile = new WorldTile(x, y, boss.getPlane());
                                        if (Utils.isOnRange(boss, tile, 7, 9, 1) && Utils.getAngle(dirs[currentDir][0], dirs[currentDir][1]) == Utils.getAngle(tile.getX() - middleTile.getX(), tile.getY() - middleTile.getY())) {
                                            for (Entity e : boss.getPossibleTargets()) {
                                                if (e == null || e.hasFinished() || e.isDead())
                                                    continue;
                                                if (Utils.isOnRange(tile, e, 1, 1, 1) && e.getTemporaryAttributtes().get("skiphitcurrent") == null) {
                                                    e.getTemporaryAttributtes().put("skiphitcurrent", Boolean.TRUE);
                                                    e.applyHit(new Hit(boss, Utils.random(500, 701), HitLook.REGULAR_DAMAGE));
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            loop++;
                            if (loop % 600 == 0) {
                                for (Entity e : boss.getPossibleTargets()) {
                                    if (e == null || e.hasFinished() || e.isDead())
                                        continue;
                                    e.getTemporaryAttributtes().remove("skiphitcurrent");
                                }
                                currentDir++;
                            }
                            if (loop >= 5200) {
                                boss.getTemporaryAttributtes().remove("insideTorrent");
                                if ((target != null && !target.isDead() && !target.hasFinished())) {
                                    boss.getCombat().setTarget(target);
                                    boss.setNextFaceEntity(target);
                                } else if (!boss.getPossibleTargets().isEmpty()) {
                                    boss.getCombat().setTarget(boss.getPossibleTargets().get(Utils.random(boss.getPossibleTargets().size())));
                                    boss.setNextFaceEntity(boss.getCombat().getTarget());
                                }
                                return false;
                            }
                        } catch (Exception e) {
                            Logger.getGlobal().catching(e);
                            return false;
                        }
                        return true;
                    }
                }, 800, 1, TimeUnit.MILLISECONDS);
                return 19;
            }
        },
        PURPLE_FLAME() {
            public int sendAttack(TheSanctumGuardian boss, Player target) {
                boss.setNextAnimation(new Animation(26560));
                boss.setNextGraphics(new Graphics(5566));
                CoresManager.getServiceProvider().scheduleFixedLengthTask(new FixedLengthRunnable() {
                    int loop = 0;
                    PurpleFlameHitBar bar;
                    long cycle;

                    @Override
                    public boolean repeat() {
                        try {
                            if (boss.isDead() || boss.hasFinished() || boss.stopAttack())
                                return false;
                            if (loop == 0) {
                                target.setNextGraphics(new Graphics(5582));
                                long totalTime = 6000;
                                cycle = totalTime + Utils.currentTimeMillis();
                                bar = new PurpleFlameHitBar(totalTime, cycle);
                            }
                            if (loop == 0 || loop % 50 == 0)
                                target.getNextHitBars().add(bar);
                            if (Utils.currentTimeMillis() > cycle) {
                                if (boss.isDead() || boss.hasFinished() || boss.stopAttack())
                                    return false;
                                boss.addFlame(new WorldTile(target));
                                return false;
                            }
                            loop++;
                        } catch (Exception e) {
                            Logger.getGlobal().catching(e);
                            return false;
                        }
                        return true;
                    }
                }, 1200, 1, TimeUnit.MILLISECONDS);
                return 5;
            }
        },
        SPAWN_MINIONS() {
            public int sendAttack(TheSanctumGuardian boss, Player target) {
                TheSanctumGuardianMinion m1 = new TheSanctumGuardianMinion(25588, boss.getRoom().getTile(new WorldTile(4745, 9150, 1)), boss);
                TheSanctumGuardianMinion m2 = new TheSanctumGuardianMinion(25588, boss.getRoom().getTile(new WorldTile(4742, 9150, 1)), boss);
                m1.spawn();
                m2.spawn();
                m1.setNextFaceWorldTile(m1.transform(0, -1, 0));
                m2.setNextFaceWorldTile(m1.transform(0, -1, 0));
                boss.minions.add(m1);
                boss.minions.add(m2);
                return 2;
            }
        };

        public int sendAttack(TheSanctumGuardian boss, Player target) {
            return 0;
        }
    }

    @Override
    public ArrayList<Entity> getPossibleTargets(boolean checkNPCs, boolean checkPlayers) {
        final ArrayList<Entity> possibleTarget = new ArrayList<Entity>();
        if (getRoom() == null)
            return possibleTarget;
        if (getCombat().getTarget() != null && isInsideFightArea(getCombat().getTarget()))
            possibleTarget.add(getCombat().getTarget());
        for (Player player : getRoom().getRoom().getPlayers()) {
            if (player == null || player.hasFinished() || player.isDead() || !Utils.isOnRange(this, player, 12) || player.getEliteDungeonsManager().isHidden() || possibleTarget.contains(player) || !isInsideFightArea(player))
                continue;
            possibleTarget.add(player);
        }
        return possibleTarget;

    }

    public boolean isInsideFightArea(WorldTile tile) {
        if (getRoom() == null)
            return false;
        WorldTile min1 = getRoom().getTile(new WorldTile(4726, 9127, 1));
        WorldTile max1 = getRoom().getTile(new WorldTile(4760, 9152, 1));
        return (tile.getX() >= min1.getX() && tile.getY() >= min1.getY() && tile.getX() <= max1.getX() && tile.getY() <= max1.getY());
    }

    @Override
    public ArrayList<Entity> getPossibleTargets() {
        return getPossibleTargets(false, true);
    }

    public static class PurpleFlameHitBar extends HitBar {
        private long totalTime = 0;
        private final long timeToExplode;

        public PurpleFlameHitBar(long totalTime, long timeToExplode) {
            this.totalTime = totalTime;
            this.timeToExplode = timeToExplode;
        }

        @Override
        public int getType() {
            return 9;
        }

        @Override
        public int getPercentage() {
            if (Utils.currentTimeMillis() > timeToExplode)
                return 0;
            long timeRemaining = timeToExplode - Utils.currentTimeMillis();
            int percentage = (int) (((((double) timeRemaining / (double) totalTime) * 100) * 255) / 100);
            return percentage;
        }

        @Override
        public boolean display(Player player) {
            return getPercentage() != 0;
        }

    }

    public static class TheSanctumGuardianMinion extends EliteDungeonNPC {

        private static final long serialVersionUID = -2102221543103343515L;

        private final transient TheSanctumGuardian boss;

        public TheSanctumGuardianMinion(int id, WorldTile tile, TheSanctumGuardian boss) {
            super(id, tile, boss.getRoom());
            this.boss = boss;
        }

        @Override
        public void processNPC() {
            if (isDead() || hasFinished())
                return;
            if (boss == null || boss.isDead() || boss.hasFinished()) {
                finish();
                return;
            }
            super.processNPC();
        }

        @Override
        public ArrayList<Entity> getPossibleTargets(boolean checkNPCs, boolean checkPlayers) {
            if (boss == null || boss.isDead() || boss.hasFinished())
                return new ArrayList<Entity>();
            return boss.getPossibleTargets(checkNPCs, checkPlayers);
        }

        @Override
        public ArrayList<Entity> getPossibleTargets() {
            if (boss == null || boss.isDead() || boss.hasFinished())
                return new ArrayList<Entity>();
            return boss.getPossibleTargets();
        }

    }

    @Override
    public void drop(Player killer) {
        String name = getName().toLowerCase();
        if (name == null)
            return;
        List<Player> players = getDropPlayers();
        if (killer != null && !players.contains(killer))
            players.add(killer);
        for (Player player : players) {
            if (player == null || player.hasFinished() || player.isDead())
                continue;
            int extra = player.getPerkManager().hasPerkActive(DonationPerk.DUNGEONS_MASTER) ? 1250 : 0;
            player.getDungeoneeringManager().setTokens(player.getDungeoneeringManager().getTokens() + 5000 + extra);
            player.getPackets().sendGameMessage(!player.getPerkManager().hasPerkActive(DonationPerk.DUNGEONS_MASTER) ? "You received 5,000 dungeoneering tokens." : "You received 6,250 dungeoneering tokens, An extra 1250 tokens for having the dungeons master perk.");
            player.getInventionManager().processScavengingPerk();
            increaseKillStatistics(player, getName());
            handleRingOfDeath(player);
            sendDrop(player, Math.random() <= 0.05 ? new NPCDrop(43067, 0.05, 1, 1) : new NPCDrop(42954, 0.05, 5, 34), false);
            if (Math.random() <= 0.05)
                sendDrop(player, new NPCDrop(18778, 5, 1, 1), false);
            final NPCDrop[] possibleDrops = EliteDungeonsConstants.mobDrops;
            for (final NPCDrop drop : possibleDrops) {
                if (drop == null || drop.getRate() < 100)
                    continue;
                if (!Ectoplasmator.scatterAshes(player, this, drop.getItemId()))
                    sendDrop(player, drop, false);
            }
            int amountCharms = player.getInventory().getAmountOf(43066);
            NPCDrop luckyCharm = Math.random() <= (amountCharms == 0 ? 0.005 : 0.04) ? NPCDrop.selectRandomNPCDrop(EliteDungeonsConstants.luckyCharmDrops) : null;
            NPCDrop mobDrop = luckyCharm != null ? luckyCharm : NPCDrop.selectRandomNPCDrop(possibleDrops);
            if (luckyCharm != null && amountCharms > 0)
                player.getInventory().deleteItem(43066, 1);
            sendDrop(player, mobDrop, false);
            if (Math.random() <= 0.005)
                sendDrop(player, new NPCDrop(18778, 5, 1, 1), false);
        }
    }

    @Override
    protected void sendDrop(Player player, NPCDrop drop, boolean lootbeam) {
        WorldTile tile = getRespawnTile();
        final Item item = new Item(drop.getItemId());
        if (player.getInventory().containsItem(19675, 1)) {
            if (Herbicide.handleDrop(player, item)) {
                return;
            }
        }
        if (player.getInventory().containsItem(18337, 1)) {
            if (Bonecrusher.handleDrop(player, item)) {
                return;
            }
        }
        if (!(drop.getItemId() == 995 && CoinAccumulator.handleCoinAccumulator(player, this, drop.getMinAmount() + Utils.getRandom(drop.getExtraAmount())))) {
            int id = drop.getItemId();
            int amount = drop.getMinAmount() + Utils.getRandom(drop.getExtraAmount());
            if (Settings.DOUBLE_DROPS) {
                amount *= 2;
            }
            if (player.getEquipment().getRingId() == 773 || player.getEquipment().getRingId() == 41069) {
                if (Utils.randomDouble() <= Settings.PERFECT_RING_DROP_DUPLICATION_RATE) {
                    amount *= 2;
                    player.sendMessage(Colors.DCYAN + "<shad=000000>Your drop has been doubled thanks to your ring!");
                }
            }
            if (player.getCurrentPet() != null) {
                if (player.getCurrentPet().getPerks().contains(PetPerk.DOUBLE_TROUBLE)) {
                    if (Utils.random(100) <= PetPerkUtils.getConModifierForPerk(player, PetPerk.DOUBLE_TROUBLE) * 10) {
                        player.sendMessage(Colors.DCYAN + "<shad=000000>Your drop suddenly vanishes because of your pet perk!");
                        return;
                    } else if (Utils.random(100) <= PetPerkUtils.getProModifierForPerk(player, PetPerk.DOUBLE_TROUBLE) * 10) {
                        amount *= 2;
                        player.sendMessage(Colors.DCYAN + "<shad=000000>Your drop has been doubled thanks to your pet perk!");
                    }
                }
            }
            if (!player.getEliteDungeonsManager().isAutoLoot() && Math.random() <= 0.05) {
                amount *= 2;
                player.getPackets().sendGameMessage("You received 2x " + item.getName() + ".");
            }
            val loot = new Item(id, amount);
            if (!player.getEliteDungeonsManager().isAutoLoot() || !player.getEliteDungeonsManager().addReward(loot)) {
                if (item.getAmount() > 1 && !item.getDefinitions().isStackable() && !item.getDefinitions().isNoted()) {
                    for (int i = 0; i < item.getAmount(); i++)
                        World.updateGroundItem(new Item(loot.getId(), 1), tile, player, 60, 0, false);
                } else
                    World.updateGroundItem(loot, tile, player, 60, 0, false);
            }
        }
        player.getDropCollectionHandler().handleBossKills(new Item(drop.getItemId()), id);
    }

}
