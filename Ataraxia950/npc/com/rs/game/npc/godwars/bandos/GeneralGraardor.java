package com.rs.game.npc.godwars.bandos;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.Hit;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.activites.GodWarsBosses;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.NPCCombatDefinition;
import com.rs.game.player.Player;
import com.rs.game.player.content.achievementsystem.AchievementList;
import com.rs.game.player.content.contracts.ContractHandler;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GeneralGraardor extends NPC {

    /**
     *
     */
    private static final long serialVersionUID = 5055436753343752989L;

    public GeneralGraardor(int id, WorldTile tile, int mapAreaNameHash, boolean canBeAttackFromOutOfArea,
                           boolean spawned, boolean hardMode) {
        super(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
        this.hardMode = hardMode;
        setCapDamage(1000);
        setLureDelay(1000);
        setForceTargetDistance(64);
        setForceFollowClose(true);
        setIntelligentRouteFinder(true);
    }

    /**
     * npc.getPossibleTargets().stream().filter(t -> t instanceof Player).collect(Collectors.toList())
     */

    @Override
    public double getMeleePrayerMultiplier() {
        return hardMode ? 0.125 : 0;
    }

    @Override
    public double getRangePrayerMultiplier() {
        return hardMode ? 0.125 : 0;
    }

    private final HashMap<Entity, Integer> protectDamage = new HashMap<Entity, Integer>();
    private boolean protectMode;
    private final boolean hardMode;
    private WorldTile targetTile;

    public void setTargetTile(WorldTile worldTile) {
        this.targetTile = worldTile;
    }

    public boolean isHardMode() {
        return hardMode;
    }

    public WorldTile getTargetTile() {
        return targetTile;
    }

    public void switchProtectMode() {
        this.protectMode = !protectMode;
    }

    public HashMap<Entity, Integer> getProtectDamageEntities() {
        return protectDamage;
    }

    public void clearDamage() {
        protectDamage.clear();
    }

    public int getProtectDamage(Entity entity) {
        return protectDamage.get(entity) == null ? 0 : protectDamage.get(entity);
    }

    @Override
    public void handleIngoingHit(final Hit hit) {
        super.handleIngoingHit(hit);
        if (protectMode)
            protectDamage.put(hit.getSource(), protectDamage.get(hit.getSource()) == null ? hit.getDamage() : protectDamage.get(hit.getSource()) + hit.getDamage());
    }

    @Override
    public ArrayList<Entity> getPossibleTargets() {
        ArrayList<Entity> possibleTarget = new ArrayList<Entity>();
        for (int regionId : getMapRegionsIds()) {
            List<Integer> playerIndexes = World.getRegion(regionId).getPlayerIndexes();
            if (playerIndexes != null) {
                for (int npcIndex : playerIndexes) {
                    Player player = World.getPlayers().get(npcIndex);
                    if (player == null || player.isDead() || player.hasFinished() || !player.isRunning()
                            || !player.withinDistance(this, 64)
                            || ((!isAtMultiArea() || !player.isAtMultiArea()) && player.getAttackedBy() != this
                            && player.getAttackedByDelay() > System.currentTimeMillis())
                            || !clipedProjectile(player, false))
                        continue;
                    possibleTarget.add(player);
                }
            }
        }
        return possibleTarget;
    }

    @Override
    public void sendDeath(Entity source) {
        final NPCCombatDefinition defs = getCombatDefinitions();
        resetWalkSteps();
        getCombat().removeTarget();
        setNextAnimation(null);
        WorldTasksManager.schedule(new WorldTask() {
            int loop;

            @Override
            public void run() {
                if (loop == 0) {
                    setNextAnimation(new Animation(defs.getDeathEmote()));
                } else if (loop >= defs.getDeathDelay()) {
                    if (source instanceof Player) {
                        Player plr = (Player) source;
                        if (plr.isGroupIronman()) {
                            plr.gimTracker.incrementBpGained(1);
                        }
                        plr.getAchievements().updateProgress(1, AchievementList.KILL_250_GWD1_BOSSES);
                        plr.getActivityTimersManager().finishBossTimer(GeneralGraardor.this);
                        ContractHandler.updateContract(plr, GeneralGraardor.this);
                    }
                    drop();
                    reset();
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
    public void setRespawnTask() {
        final NPC npc = this;
        // Particulary for Dominion Tower.
        if (!GodWarsBosses.isAtGodwars(npc))
            return;
        if (!hasFinished()) {
            reset();
            setLocation(getRespawnTile());
            finish();
        }
        WorldTasksManager.schedule(new WorldTask() {
            @Override
            public void run() {
                setFinished(false);
                World.addNPC(npc);
                npc.setLastRegionId(0);
                World.updateEntityRegion(npc);
                loadMapRegions();
                checkMultiArea();
                GodWarsBosses.respawnBandosMinions();
            }
        }, getCombatDefinitions().getRespawnDelay());
    }
}