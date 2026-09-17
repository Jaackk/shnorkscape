package com.rs.game.npc.others;

import com.google.common.primitives.Longs;
import com.rs.Settings;
import com.rs.cores.CoresManager;
import com.rs.game.Entity;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.WorldTile;
import com.rs.game.activities.instances.DagannothKingsInstance;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Logger;

import java.util.concurrent.TimeUnit;

/**
 * Creates the Dagannoth King NPC's.
 *
 * @author Noel
 */
public class DagannothKing extends NPC {

    /**
     * Generated serial UID.
     */
    private static final long serialVersionUID = 2820530565457808392L;

    private final DagannothKingsInstance instance;

    public DagannothKing(final int id, final WorldTile tile, final int mapAreaNameHash, final boolean canBeAttackFromOutOfArea, final boolean spawned, final DagannothKingsInstance instance) {
        super(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
        setForceTargetDistance(7);
        setLureDelay(1000);
        setForceAgressive(true);
        this.instance = instance;
    }

    @Override
    public void setRespawnTask() {
        if (!hasFinished()) {
            reset();
            setLocation(getRespawnTile());
            finish();
        }
        if (instance != null && !instance.isOwnerInstance())
            return;
        int respawnDelay = getCombatDefinitions().getRespawnDelay();
        if (instance != null) {
            respawnDelay = (instance.getRespawnSpeed() * 1000) / 600;
        }
        if (Settings.DEBUG) {
            Logger.getGlobal().info("Respawn task initiated: [" + getName() + "]; time: [" + respawnDelay + "].");
        }
        WorldTasksManager.schedule(new WorldTask() {
            @Override
            public void run() {
                if (instance != null && !instance.isOwnerInstance())
                    return;
                spawn();
                stop();
            }
        }, respawnDelay);
    }

    @Override
    public void processNPC() {
        if (isDead() || isLocked()) {
            return;
        }
        if (!withinDistance(getRespawnTile(), 7)) {
            removeTarget();
            forceWalkRespawnTile();
        }
        super.processNPC();
    }

    /**
     * 2881 supreme 2882 prime 2883 rex
     */
    @Override
    public void handleIngoingHit(final Hit hit) {
        if (hit.getLook() != HitLook.MELEE_DAMAGE && hit.getLook() != HitLook.RANGE_DAMAGE && hit.getLook() != HitLook.MAGIC_DAMAGE) {
            return;
        }
        if ((getId() == 2881 && hit.getLook() != HitLook.MELEE_DAMAGE) || (getId() == 2882 && hit.getLook() != HitLook.RANGE_DAMAGE) || (getId() == 2883 && hit.getLook() != HitLook.MAGIC_DAMAGE)) {
            hit.setDamage(hit.getDamage() / 5);
        }
        super.handleIngoingHit(hit);
    }

    @Override
    public void sendDeath(Entity source) {
        if (source instanceof Player) {
            Player plr = (Player) source;
            if (plr.isGroupIronman()) {
                plr.awardDkPoint += 0.5;
                if (plr.awardDkPoint >= 1.0) {
                    plr.gimTracker.incrementBpGained(1);
                    plr.awardDkPoint = 0.0;
                }
            }
        }
        super.sendDeath(source);
    }
}