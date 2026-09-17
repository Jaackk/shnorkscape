package com.rs.game.npc.themagister;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.Graphics;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.player.Player;
import com.rs.game.player.content.contracts.ContractHandler;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

import java.util.ArrayList;

@SuppressWarnings("serial")
public class CorruptedSoulObelisk extends NPC {

    private final TheMagister theMagister;
    private long drainHpDelay;
    private final WorldObject corruptedSoulObelisk;
    private boolean spawned;

    public CorruptedSoulObelisk(int id, WorldTile tile, TheMagister theMagister) {
        super(id, tile, -1, true, true, false);
        setForceMultiArea(true);
        setCantFollowUnderCombat(true);
        this.theMagister = theMagister;
        drainHpDelay = Utils.currentTimeMillis() + 4800;
        corruptedSoulObelisk = new WorldObject(109496, 10, 1, tile);
    }

    @Override
    public boolean checkAgressivity() {
        return false;
    }

    @Override
    public ArrayList<Entity> getPossibleTargets(boolean checkNPCs, boolean checkPlayers) {
        ArrayList<Entity> possibleTarget = new ArrayList<Entity>();
        return possibleTarget;
    }

    @Override
    public ArrayList<Entity> getPossibleTargets() {
        return getPossibleTargets(false, true);
    }

    @Override
    public void processNPC() {
        if (isDead() || isLocked() || hasFinished() || !spawned) {
            return;
        }
        if (theMagister == null || theMagister.hasFinished()) {
            finish();
            return;
        }
        if (Utils.currentTimeMillis() > drainHpDelay && theMagister != null && !theMagister.hasFinished() && theMagister.getInstance().getPlayer() != null && !theMagister.getInstance().getPlayer().hasFinished()) {
            Player player = theMagister.getInstance().getPlayer();
            CombatScript.delayHit(theMagister, 0, player, new Hit(theMagister, 60, HitLook.UNBLOCKABLE_REGULAR_DAMAGE));
            World.sendProjectileCycles(player, transform(1, 0, 0), 6720, 25, 5, 0, 100, 30, 0);
            drainHpDelay = Utils.currentTimeMillis() + 10000;
        }
    }

    public TheMagister getTheMagister() {
        return theMagister;
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
        if (theMagister != null && !theMagister.hasFinished() && theMagister.getInstance().getPlayer() != null && !theMagister.getInstance().getPlayer().hasFinished())
            theMagister.getInstance().getPlayer().getPackets().sendObjectAnimation(corruptedSoulObelisk, new Animation(30921));
        WorldTasksManager.schedule(new WorldTask() {
            int loop;

            @Override
            public void run() {
                if (loop == 0) {
                    setNextAnimation(new Animation(-1));
                } else if (loop >= 2) {
                    if (source instanceof Player) {
                        ((Player) source).getControlerManager().processNPCDeath(CorruptedSoulObelisk.this);
                        ContractHandler.updateContract(((Player) source), CorruptedSoulObelisk.this);
                        ((Player) source).getActivityTimersManager().finishBossTimer(CorruptedSoulObelisk.this);
                    }
                    reset();
                    setLocation(getRespawnTile());
                    finish();
                    stop();
                }
                loop++;
            }
        }, 0, 1);
    }

    @Override
    public void finish() {
        clearArea();
        super.finish();
    }

    protected void clearArea() {
        if (World.containsObjectWithId(corruptedSoulObelisk, corruptedSoulObelisk.getId()))
            World.removeObject(corruptedSoulObelisk);
    }

    @Override
    public void spawn() {
        World.spawnObject(corruptedSoulObelisk);
        WorldTasksManager.schedule(new WorldTask() {

            @Override
            public void run() {
                corruptedSoulObelisk.setId(109497);
                World.spawnObject(corruptedSoulObelisk);
                initEntity();
                CorruptedSoulObelisk.super.spawn();
                spawned = true;
            }
        }, 2);
    }

}
