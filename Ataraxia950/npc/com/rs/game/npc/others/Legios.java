package com.rs.game.npc.others;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.ForceTalk;
import com.rs.game.Graphics;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.map.bossInstance.impl.LegiosInstance;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.player.Player;
import com.rs.game.player.content.HybridTokenDistributor;
import com.rs.game.player.content.contracts.ContractHandler;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class Legios extends NPC {

    private static final long serialVersionUID = 6785701815949268085L;
    private int attackRotation;
    private int stage;
    public static final String[][] messages = new String[][]{{"My power increases!", "Nothing will stop my power!", "You will not survive this!"}, {"Cower in fear!", "The onslaught continues!", "A storm is coming!"}, {"Rorarii, attack!", "Attack, Gladius!", "Capsarius! Heal me!"}, {"You cannot hide forever!", "There is no escape!", "Embrace the Ascended!"}, {"Behold! The Line of Ascension!", "Come - be part of something greater!", "You resist the Ascended?"}, {"Your next step could be your last!", "Yield. It is inevitable.", "It is folly to stand against us!"}};
    private final List<Ascended> ascended;
    private final List<WorldObject> lineOfAscension;
    private long lineHitDelay;
    private final LegiosInstance instance;

    public Legios(int id, WorldTile tile, LegiosInstance instance) {
        super(id, tile, -1, true, true);
        setForceMultiArea(true);
        setIntelligentRouteFinder(true);
        setForceAgressive(true);
        setForceTargetDistance(64);
        ascended = new ArrayList<Ascended>();
        lineOfAscension = new ArrayList<WorldObject>();
        this.instance = instance;
    }

    @Override
    public void processNPC() {
        if (getCombat().getTarget() != null && Utils.currentTimeMillis() > lineHitDelay) {
            if (World.containsObjectWithId(getCombat().getTarget(), 84675) || World.containsObjectWithId(getCombat().getTarget(), 84676))
                CombatScript.delayHit(this, 0, getCombat().getTarget(), new Hit(this, Utils.random(220, 241), HitLook.UNBLOCKABLE_MAGIC_DAMAGE));
            lineHitDelay = Utils.currentTimeMillis() + 600;
        }
        super.processNPC();
    }

    public int getAttackRotation() {
        return attackRotation;
    }

    public void setAttackRotation(int attackRotation) {
        this.attackRotation = attackRotation;
    }

    public int getStage() {
        return stage;
    }

    public List<WorldObject> getLineOfAscension() {
        return lineOfAscension;
    }

    public void increaseStage() {
        setNextForceTalk(new ForceTalk(messages[getId() - 17149][stage]));
        stage++;
        if (getId() == 17151) {
            switch (stage) {
                case 1:
                    for (int i = 0; i < 3; i++)
                        ascended.add(new Ascended(17144, new WorldTile(this), this));
                    break;
                case 2:
                    ascended.add(new Ascended(17145, new WorldTile(this), this));
                    break;
                case 3:
                    ascended.add(new Ascended(17146, new WorldTile(this), this));
                    break;
            }
        } else if (getId() == 17152 && getCombat().getTarget() != null && getCombat().getTarget() instanceof Player) {
            List<WorldTile> possibleTiles = new ArrayList<WorldTile>();
            for (int x = this.getX() - 5; x < this.getX() + 5; x++) {
                for (int y = this.getY() - 5; y < this.getY() + 5; y++) {
                    WorldTile checkTile = new WorldTile(x, y, getPlane());
                    if (!this.matches(checkTile) && Utils.isOnRange(checkTile, this, 0, 1, 1) && World.canMoveNPC(checkTile, 1)) {
                        possibleTiles.add(checkTile);
                    }
                }
            }
            if (!possibleTiles.isEmpty()) {
                ((Player) getCombat().getTarget()).stopAll();
                getCombat().getTarget().setNextWorldTile(possibleTiles.get(Utils.random(possibleTiles.size())));
            }
        } else if (getId() == 17153 && getCombat().getTarget() != null) {
            spawnLineOfAscension(stage != 2);
        }
    }

    @Override
    public double getMagePrayerMultiplier() {
        return 0.5;
    }

    @Override
    public void handleIngoingHit(Hit hit) {
        if (hit.getSource() != null && hit.getDamage() > 0) {
            hit.setDamage((int) ((double) hit.getDamage() - ((Utils.isOnRange(hit.getSource(), this, 0) ? 0 : Utils.isOnRange(hit.getSource(), this, 1) ? 0.1 : Utils.isOnRange(hit.getSource(), this, 2) ? 0.2 : Utils.isOnRange(hit.getSource(), this, 3) ? 0.3 : Utils.isOnRange(hit.getSource(), this, 4) ? 0.4 : 0.5) * (double) hit.getDamage())));
        }
        super.handleIngoingHit(hit);
    }

    @Override
    public void processHit(Hit hit) {
        super.processHit(hit);
        if ((stage == 0 && this.getHitpoints() <= (this.getMaxHitpoints() * 0.75)) || (stage == 1 && this.getHitpoints() <= (this.getMaxHitpoints() * 0.50)) || (stage == 2 && this.getHitpoints() <= (this.getMaxHitpoints() * 0.25))) {
            increaseStage();
        }
    }

    @Override
    public void reset() {
        attackRotation = 0;
        stage = 0;
        super.reset();
    }

    @Override
    public boolean canWalkNPC(int toX, int toY, boolean checkUnder) {
        return super.canWalkNPC(toX, toY, checkUnder);
    }

    public void spawnLineOfAscension(boolean vertical) {
        WorldTile center = getCombat().getTarget();
        WorldTile min = instance.getTile(new WorldTile(1193, 619, 1));
        WorldTile max = instance.getTile(new WorldTile(1213, 640, 1));
        for (int x = vertical ? min.getX() : center.getX(); x <= (vertical ? max.getX() : center.getX()); x++) {
            for (int y = !vertical ? min.getY() : center.getY(); y <= (!vertical ? max.getY() : center.getY()); y++) {
                WorldTile checkTile = new WorldTile(x, y, getPlane());
                WorldObject object = null;
                int rot = vertical ? 1 : 0;
                WorldObject existing = World.getObjectWithId(checkTile, 84675);
                if (existing != null && existing.getRotation() != rot) {
                    object = new WorldObject(84676, 4, 0, checkTile);
                } else {
                    object = new WorldObject(84675, 4, rot, checkTile);
                }
                if (World.containsObjectWithId(checkTile, 84676))
                    object = null;
                if (object != null) {
                    World.spawnObject(object);
                    lineOfAscension.add(object);
                }
            }
        }
    }

    @Override
    public void sendDeath(Entity source) {
        resetWalkSteps();
        getCombat().removeTarget();
        if (source instanceof Player) {
            source.deathResetCombat();
        }
        setNextAnimation(new Animation(-1));
        setNextGraphics(new Graphics(-1));
        clearArea();
        WorldTasksManager.schedule(new WorldTask() {
            int loop;

            @Override
            public void run() {
                if (loop == 0) {
                    setNextAnimation(new Animation(20256));
                } else if (loop >= 3) {
                    if (source instanceof Player) {
                        Player plr = (Player) source;
                        if (plr.isGroupIronman()) {
                            plr.gimTracker.incrementBpGained(4);
                        }
                        plr.getControlerManager().processNPCDeath(Legios.this);
                        ContractHandler.updateContract(plr, Legios.this);
                        plr.getActivityTimersManager().finishBossTimer(Legios.this);
                        HybridTokenDistributor.rollForToken(plr, HybridTokenDistributor.Activity.LEGIONES);
                    }
                    drop();
                    reset();
                    setLocation(getRespawnTile());
                    finish();
                    if (instance == null) {
                        setRespawnTask();
                    }
                    stop();
                }
                loop++;
            }
        }, 0, 1);
    }

    private void clearArea() {
        for (Ascended npc : ascended)
            if (npc != null && !npc.hasFinished())
                npc.sendDeath(null);
        ascended.clear();
        for (WorldObject object : lineOfAscension) {
            if (object != null && World.containsObjectWithId(object, object.getId()))
                World.removeObject(object);
        }
        lineOfAscension.clear();
    }

    public void respawn() {
        if (instance == null || instance.getLegio() != this)
            return;
        if (!hasFinished()) {
            reset();
            setLocation(this.getRespawnTile());
            finish();
        }
        spawn();
    }

}
