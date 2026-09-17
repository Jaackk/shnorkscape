package com.rs.game.npc.godwars.saradomin;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.Hit;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.activites.GodWarsBosses;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.NPCCombatDefinition;
import com.rs.game.npc.others.SecondaryBar;
import com.rs.game.player.Player;
import com.rs.game.player.content.achievementsystem.AchievementList;
import com.rs.game.player.content.contracts.ContractHandler;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Logger;
import com.rs.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class CommanderZilyana extends NPC {

    private static final long serialVersionUID = 5917906988910885013L;

    public CommanderZilyana(int id, WorldTile tile, int mapAreaNameHash, boolean canBeAttackFromOutOfArea, boolean spawned, boolean hardMode) {
        super(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
        setCapDamage(1000);
        setLureDelay(1000);
        setForceTargetDistance(64);
        setForceFollowClose(true);
        setIntelligentRouteFinder(true);
        this.hardMode = hardMode;
        setBonuses();
    }

    @Override
    public double getMeleePrayerMultiplier() {
        return hardMode ? 0.125 : 0;
    }

    @Override
    public double getMagePrayerMultiplier() {
        return hardMode ? 0.125 : 0;
    }

    public boolean isSecondHalf() {
        return secondHalf;
    }

    public boolean isFinalBlow() {
        return this.getHitpoints() <= 50 && secondHalf;
    }

    public void dealFinalBlow() {
        if (!hardMode)
            return;
        if (!laying)
            return;
        resetWalkSteps();
        getCombat().removeTarget();
        setNextAnimation(null);
        drop();
        reset();
        setLocation(getRespawnTile());
        finish();
    }

    private final boolean hardMode;
    private boolean secondHalf;
    private boolean laying;

    public boolean isHardMode() {
        return hardMode;
    }

    @Override
    public void handleIngoingHit(final Hit hit) {
        if (!laying)
            super.handleIngoingHit(hit);
        if (secondHalf) {
            if ((this.getHitpoints() - hit.getDamage()) <= 50 && !laying) {
                this.setCannotMove(true);
                laying = true;
                setCantInteract(true);
                NPC npc = this;
                setTarget(null);
                npc.setNextAnimation(new Animation(npc.getCombatDefinitions().getDeathEmote()));
                WorldTasksManager.schedule(new WorldTask() {
                    int ticks;

                    @Override
                    public void run() {
                        if (hasFinished()) {
                            npc.setCannotMove(false);
                            stop();
                            return;
                        }
                        if (ticks == 1) {
                            npc.setHitpoints(50);
                            setCantInteract(false);
                            npc.setNextAnimation(new Animation(-1));
                            npc.setNextRenderAnimation(2851);
                            setNextSecondaryBar(new SecondaryBar(0, 480, 1, false));
                        } else if (ticks == 10) {
                            laying = false;
                            npc.setCannotMove(false);
                            setTarget(hit.getSource());
                            npc.setHitpoints(2500);
                            npc.setNextRenderAnimation(npc.getDefinitions().renderEmote);
                            npc.setNextAnimation(new Animation(-1));
                            stop();
                            return;
                        }
                        ticks++;
                    }
                }, 0, 1);
                return;
            }
        }
    }

    @Override
    public void spawn() {
        super.spawn();
        setNextAnimation(new Animation(19876));
        setCantInteract(true);
        setForceAgressive(false);
        WorldTasksManager.schedule(new WorldTask() {
            @Override
            public void run() {
                setCantInteract(false);
                setForceAgressive(true);
            }
        }, 2);
    }

    @Override
    public ArrayList<Entity> getPossibleTargets() {
        ArrayList<Entity> possibleTarget = new ArrayList<Entity>();
        for (int regionId : getMapRegionsIds()) {
            List<Integer> playerIndexes = World.getRegion(regionId).getPlayerIndexes();
            if (playerIndexes != null) {
                for (int npcIndex : playerIndexes) {
                    Player player = World.getPlayers().get(npcIndex);
                    if (player == null || player.isDead() || player.hasFinished() || !player.isRunning() || !player.withinDistance(this, 64) || ((!isAtMultiArea() || !player.isAtMultiArea()) && player.getAttackedBy() != this && player.getAttackedByDelay() > Utils.currentTimeMillis()) || !clipedProjectile(player, false))
                        continue;
                    possibleTarget.add(player);
                }
            }
        }
        return possibleTarget;
    }

    /*
     * gotta override else setRespawnTask override doesnt work
     */
    @Override
    public void sendDeath(Entity source) {
        if (laying)
            return;
        if (!secondHalf && hardMode) {
            secondHalf = true;
            setTarget(null);
            setCantInteract(true);
            WorldTasksManager.schedule(new WorldTask() {
                @Override
                public void run() {
                    setHitpoints(5000);
                    setTarget(source);
                    setCantInteract(false);
                    stop();
                }
            }, 2);
            return;
        }
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
                            plr.gimTracker.incrementBpGained(3);
                        }
                        plr.getAchievements().updateProgress(1, AchievementList.KILL_250_GWD1_BOSSES);
                        plr.getActivityTimersManager().finishBossTimer(CommanderZilyana.this);
                        ContractHandler.updateContract(plr, CommanderZilyana.this);
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
        secondHalf = false;
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
                try {
                    setFinished(false);
                    World.addNPC(npc);
                    npc.setLastRegionId(0);
                    World.updateEntityRegion(npc);
                    loadMapRegions();
                    checkMultiArea();
                    setBonuses();
                    GodWarsBosses.respawnSaradominMinions();
                } catch (Exception e) {
                    Logger.getGlobal().catching(e);
                } catch (Error e) {
                    Logger.getGlobal().catching(e);
                }
            }
        }, getCombatDefinitions().getRespawnDelay());
    }

    @Override
    public void setBonuses() {
        super.setBonuses();
        if (hardMode) {
            int[] bonuses = new int[getBonuses().length];
            for (int i = 0; i < bonuses.length; i++)
                bonuses[i] = (int) (getBonus(i) * (i <= 4 ? 2 : 1.1));
            setBonuses(bonuses);
        }
    }

}