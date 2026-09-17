package com.rs.game.npc.Trex;


import com.rs.Settings;
import com.rs.cache.loaders.ItemDefinitions;
import com.rs.game.*;
import com.rs.game.activites.pest.PestControl;
import com.rs.game.hitbar.HitBar;
import com.rs.game.npc.NPC;

import com.rs.game.npc.solak.Solak;
import com.rs.game.player.Player;
import com.rs.game.player.controllers.TrexController;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class Trex extends NPC {

    private static final long serialVersionUID = -7373466967844952156L;
    private int phase;
    private transient Trex current;
    private long flameHitCycle;

    private Trex trex;
    private final transient List<Trex.Flame> flames;

    public Trex(int id, WorldTile tile, int mapAreaNameHash, boolean canBeAttackFromOutOfArea, boolean spawned) {
        super(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
        flames = new ArrayList<Trex.Flame>();

        setCapDamage(1000);
        setForceTargetDistance(500);
        setLureDelay(0);
        setForceTargetDistance(14);

        setRun(true);
        setHitpoints(11000);
        phase = 1;
        getCombatDefinitions().setHitpoints(11000);

    }
    @Override
    public void handleIngoingHit(final Hit hit) {
        if (Settings.USE_DAMAGE_CAP) {
            if (hit.getDamage() > 1000)
                hit.setDamage(1000);
        }
        if (hit.getSource() instanceof Player) {
            final Player p = (Player) hit.getSource();
            if (p != null && !ItemDefinitions.getItemDefinitions(p.getEquipment().getWeaponId()).getName().contains("scythe") && !hit.isInstantKill() && !hit.isSpecialHit()) {
                hit.setDamage(hit.getDamage() / 2);
            }
        } else {
            hit.setDamage(hit.getDamage() / 2);
        }
        super.handleIngoingHit(hit);
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
    public static class Flame {
        private final WorldTile tile;
        private final long lifeCycle;
        private final int startDamage;
        public Flame(WorldTile tile) {
            this.tile = tile;
            this.lifeCycle = Utils.currentTimeMillis();
            startDamage = Utils.random(50, 90);
        }
        public int getDamage() {
            long time = Utils.currentTimeMillis() - lifeCycle;
            int ticksPassed = (int) (time / 600);
            if (ticksPassed <= 0)
                return 0;
            int damage = startDamage + ((ticksPassed - 1) * 30);
            return damage >= 20 ? 30 : damage;
        }
        public void remove(Trex boss) {

            World.sendGraphics(boss, new Graphics(-1), tile);
        }
    }

    public void addFlame(WorldTile location) {
        Trex.Flame f = new Trex.Flame(location);
        flames.add(f);
        World.sendGraphics(this, new Graphics(4480), location);
        final Trex thisNPC = this;
        WorldTasksManager.schedule(new WorldTask() {
            @Override
            public void run() {
                if (isDead() || hasFinished() || !flames.contains(f)) {
                    stop();
                    return;
                }
                World.sendGraphics(thisNPC, new Graphics(4480), location);
            }
        }, 40, 40);
    }
    @Override
    public double getMagePrayerMultiplier() {
        return 0.67;
    }

    @Override
    public double getMeleePrayerMultiplier() {
        return 0.67;
    }
    public void magic1st() {
        phase = 2;
    }
    public int getPhase() {
        return phase;
    }

    private int spawnCount;
    public List<Trex.Flame> clearArea() {

        for (Trex.Flame flame : flames) {
            if (flame != null)
                flame.remove(this);
            if (trex == null || trex.hasFinished() || trex.isDead())
                continue;
            flames.clear();
        }
        flames.clear();
        return flames;
    }
    @Override
    public void processNPC() {
        super.processNPC();
        if (flameHitCycle == 0 || Utils.currentTimeMillis() >= flameHitCycle) {
            for (Trex.Flame flame : flames) {
                if (flame != null && flame.getDamage() > 0) {
                    for (Entity e : getPossibleTargets()) {
                        if (e == null || e.hasFinished() || e.isDead() || !Utils.isOnRange(flame.tile, e, 1, 1, 1))
                            continue;
                        e.applyHit(new Hit(this, flame.getDamage(), Hit.HitLook.REGULAR_DAMAGE));
                    }
                }
            }
            flameHitCycle = Utils.currentTimeMillis() + 600;
        }
        if ((getHPPercentage() < 75 && spawnCount < 1)
                || (getHPPercentage() < 25 && spawnCount < 2))
            if (isDead())
                return;
        checkReset();
    }
    public int getBossMapId() {

        return 26435;
    }
    public void checkReset() {
        int maxhp = getMaxHitpoints();
        if (maxhp > getHitpoints() && !isUnderCombat() && getPossibleTargets().isEmpty())
            setHitpoints(maxhp);
    }

    @Override
    public int getMaxHitpoints() {
        return getCombatDefinitions().getHitpoints();
    }

    public void nextPhase() {
        phase++;
    }
    public void setPhase(int phase) {
        this.phase = phase;
    }
    public int getAttackDistance() {
        return 14;
    }

    public Trex getTrex() {
        return trex;
    }
    @Override
    public void processHit(Hit hit) {
        super.processHit(hit);

        for (Player p : World.getPlayers()) { // lets just loop
            if (p == null || p.isDead() || p.hasFinished() || !(p.getControlerManager().getControler() instanceof TrexController))
                continue;
            TrexController c = (TrexController) p.getControlerManager().getControler();

            c.updateInterface();
        }
    }
    @Override
    public void finish() {
        clearArea();
        super.finish();
    }
    @Override
    public void spawn() {
        super.spawn();
        start();
        phase = 1;
    }
    private void start() {
        spawnCount = 1;
        setForceTargetDistance(14);

    }
    @Override
    public boolean restoreHitPoints() {
        boolean restore = super.restoreHitPoints();

        for (Player p : World.getPlayers()) { // lets just loop
            if (p == null || p.isDead() || p.hasFinished() || !(p.getControlerManager().getControler() instanceof TrexController))
                continue;
            TrexController c = (TrexController) p.getControlerManager().getControler();


            c.updateInterface();
            return restore;

        }
        return restore;
    }
    @Override
    public boolean isFreezeImmune() {
        return true;
    }
    @Override
    public boolean isStunImmune() {
        return true;
    }
    @Override
    public void sendDeath(final Entity source) {
        super.sendDeath(source);
        clearArea();
        if (source instanceof Player) {
            Player plr = (Player) source;
            if (plr.isGroupIronman()) {
                plr.gimTracker.incrementBpGained(6);
            }

        }
    }
    public int getHPPercentage() {
        return getHitpoints() * 100 / getMaxHitpoints();
    }
    public ArrayList<Entity> getPossibleTargets() {
        return super.getPossibleTargets(false, true);
    }

}

