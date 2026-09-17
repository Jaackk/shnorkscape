package com.rs.game.npc.telos;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.NPCCombatDefinition;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

import java.util.ArrayList;

@SuppressWarnings("serial")
public class AnimaGolem extends NPC {
    private final Telos telos;
    private long chargeCycle;
    private int respawnPercentage;

    public AnimaGolem(int id, WorldTile tile, Telos telos) {
        super(id, tile, -1, true, true);
        chargeCycle = Utils.currentTimeMillis() + 3000;
        this.telos = telos;
        setNextAnimation(new Animation(29000));
        setHitpoints(getMaxHitpoints());
        setNextFaceEntity(telos);
    }

    public int getRespawnPercentage() {
        return respawnPercentage;
    }

    public void increaseRespawnPercentage(int amount) {
        respawnPercentage = respawnPercentage + amount >= 100 ? 100 : respawnPercentage + amount;
        if (respawnPercentage == 100) {
            setNextNPCTransformation(22918);
            setHitpoints(getMaxHitpoints());
            setNextFaceEntity(telos);
        } else
            addAnimaBar();
    }

    public void addAnimaBar() {
        // getNextHitBars().add(new AnimaHitBar(this));
    }

    @Override
    public boolean canMove(int dir) {
        return false;
    }

    @Override
    public void processNPC() {
        if (chargeCycle != 0 && Utils.currentTimeMillis() >= chargeCycle) {
            if (isActive()) {
                if (telos != null && !telos.isDead()) {
                    setNextFaceEntity(telos);
                    setNextAnimation(new Animation(28998));
                    int delay = Utils.projectileTimeToCycles(World.sendProjectileNew(this, telos, 6265, 400, 39, 30, 2, 16, 5).getEndTime()) - 1;
                    WorldTasksManager.schedule(new WorldTask() {
                        @Override
                        public void run() {
                            telos.increaseAnima(5);
                        }

                    }, delay);
                }
            } else {
                increaseRespawnPercentage(8);
            }
            chargeCycle = Utils.currentTimeMillis() + 2000;
        }
    }

    @Override
    public void sendDeath(Entity source) {
        final NPCCombatDefinition defs = getCombatDefinitions();
        resetWalkSteps();
        setNextAnimation(null);
        if (!isDead())
            setHitpoints(0);
        setNextFaceEntity(null);
        WorldTasksManager.schedule(new WorldTask() {
            int loop = 0;

            @Override
            public void run() {
                if (loop == 0) {
                    setNextAnimation(new Animation(defs.getDeathEmote()));
                } else if (loop >= 1) {
                    setNextNPCTransformation(22919);
                    if (source instanceof Telos)
                        finish();
                    telos.setAnima(telos.getAnima() - 35 <= 0 ? 0 : telos.getAnima() - 35);
                    respawnPercentage = 0;
                    source.deathResetCombat();
                    stop();
                }
                loop++;
                if (loop > 15)
                    stop();
            }
        }, 0, 1);
    }

    @Override
    public boolean checkAgressivity() {
        return false;
    }

    @Override
    public ArrayList<Entity> getPossibleTargets() {
        return getPossibleTargets(false, true);
    }

    @Override
    public ArrayList<Entity> getPossibleTargets(boolean checkNPCs, boolean checkPlayers) {
        return new ArrayList<Entity>();
    }

    @Override
    public void setTarget(Entity entity) {

    }

    @Override
    public boolean isDead() {
        return false;
    }

    private boolean isActive() {
        return getId() == 22918;
    }

    @Override
    public int getMaxHitpoints() {
        if (telos == null || telos.getInstance() == null)
            return 1;
        int enrage = telos.getPlayer() == null ? 0 : telos.getPlayer().getTelosEnrage();
        return telos.getPhase() <= 3 ? (800 + (enrage * 2)) : ((int) (450 + (enrage * 2.25)));
    }
}
