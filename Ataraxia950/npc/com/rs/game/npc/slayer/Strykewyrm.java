package com.rs.game.npc.slayer;

import com.rs.cache.loaders.NPCDefinitions;
import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.Graphics;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.NPCCombatDefinitionConstants;
import com.rs.game.player.Player;
import com.rs.game.player.combat.PlayerCombat;
import com.rs.game.player.content.RouteEvent;
import com.rs.game.player.controllers.Wilderness;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles all 4 Strykewyrms.
 *
 * @author Noel
 */
public class Strykewyrm extends NPC {

    private static final long serialVersionUID = -4803894841210918619L;
    private final int stompId;

    public Strykewyrm(int id, WorldTile tile, int mapAreaNameHash, boolean canBeAttackFromOutOfArea) {
        super(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, true);
        stompId = id;
    }

    @Override
    public boolean checkAgressivity() {
        final ArrayList<Entity> possibleTarget = getPossibleTargets();
        if (possibleTarget.isEmpty()) {
            return false;
        }
        final Entity target = possibleTarget.get(Utils.random(possibleTarget.size()));
        if (!(target instanceof Player)) {
            return false;
        }
        final Player player = ((Player) target);
        boolean pzDisable = player.inPzInstance && player.isUnderCombat();
        if (player.getAggressiveDelay() > 0 && getMaxHitpoints() > 1 && !isCantInteract() && !pzDisable) {
            if (!PlayerCombat.canAttackNpc(player, this) || !Strykewyrm.handleStomping(player, this, true)) {
                return false;
            }
            if (getId() == 9462 || getId() == 9464 || getId() == 9466 || getId() == 2417) {
                Strykewyrm.handleStomping(player, this, false);
                return true;
            }
            setTarget(target);
            target.setAttackedBy(target);
            target.setFindTargetDelay(Utils.currentTimeMillis() + 600);
            return true;
        }

        if (isForceAgressive() || getCombatDefinitions().getAggressivenessType() == NPCCombatDefinitionConstants.AGRESSIVE) {
            if (!Strykewyrm.handleStomping(player, this, true))
                return false;
            if (getId() == 9462 || getId() == 9464 || getId() == 9466 || getId() == 2417) {
                Strykewyrm.handleStomping(player, this, false);
                return true;
            }
            setTarget(target);
            target.setAttackedBy(target);
            target.setFindTargetDelay(Utils.currentTimeMillis() + 5000);
            return true;
        }
        return false;
    }

    @Override
    public ArrayList<Entity> getPossibleTargets(boolean checkNPCs, boolean checkPlayers) {
        return getPossibleTargets();
    }

    @Override
    public ArrayList<Entity> getPossibleTargets() {
        final int size = getSize();
        final int agroRatio = 1;
        final ArrayList<Entity> possibleTarget = new ArrayList<Entity>();
        for (final int regionId : getMapRegionsIds()) {
            final List<Integer> playerIndexes = World.getRegion(regionId).getPlayerIndexes();
            if (playerIndexes != null) {
                for (final int playerIndex : playerIndexes) {
                    final Player player = World.getPlayers().get(playerIndex);
                    int aggroDistance = getForceTargetDistance() > 0 ? getForceTargetDistance() : agroRatio;
                    if (player != null && player.getAggressiveDelay() > 0) {
                        if (!PlayerCombat.canAttackNpc(player, this) || !Strykewyrm.handleStomping(player, this, true)) {
                            continue;
                        }
                        aggroDistance = Math.max(aggroDistance, 8);
                        setForceMultiAttacked(true);
                        if (!player.getAggressiveOnYou().contains(this)) {
                            player.getAggressiveOnYou().add(this);
                        }
                    }

                    if (player == null || !PlayerCombat.canAttackNpc(player, this) || !Strykewyrm.handleStomping(player, this, true) || player.isDead() || player.hasFinished() || !player.isRunning() || player.getPlane() != getPlane() || player.getAppearence().isHidden() || !Utils.isOnRange(getX(), getY(), size, player.getX(), player.getY(), player.getSize(), aggroDistance) || (!isForceMultiAttacked() && (!isAtMultiArea() || !player.isAtMultiArea()) && (player.getAttackedBy() != this && (player.getAttackedByDelay() > Utils.currentTimeMillis() || player.getFindTargetDelay() > Utils.currentTimeMillis()))) || !clipedProjectile(player, false) && player.getTileHash() != getMiddleWorldTile().getTileHash() || (!isForceAgressive() && !Wilderness.isAtWild(this) && player.getSkills().getCombatLevelWithSummoning() >= getCombatLevel() * 2 && player.getAggressiveDelay() == 0)) {
                        continue;
                    }
                    possibleTarget.add(player);
                }
            }

        }
        return possibleTarget;
    }

    /**
     * Handles the stomping.
     *
     * @param player The player interacting.
     * @param npc    The mound stomped.
     */

    public static boolean handleStomping(final Player player, final NPC npc) {
        return handleStomping(player, npc, false);
    }

    public static boolean handleStomping(final Player player, final NPC npc, boolean check) {
        if (npc.isCantInteract())
            return false;
        
        if (!npc.withinDistance(player, 14))
            return false;
        if (player.getTemporaryAttributtes().get("stomping") != null)
            return false;

        if (!npc.isAtMultiArea() || !player.isAtMultiArea()) {
            if (player.getAttackedBy() != npc && player.getAttackedByDelay() > Utils.currentTimeMillis()) {
                if (!check)
                    player.getPackets().sendGameMessage("You are already in combat.", true);
                return false;
            }
            if (npc.getAttackedBy() != player && npc.getAttackedByDelay() > Utils.currentTimeMillis()) {
                if (npc.getAttackedBy() instanceof NPC) {
                    if (!check)
                        npc.setAttackedBy(player);
                } else {
                    if (!check)
                        player.sendMessage("That npc is already in combat.", true);
                    return false;
                }
            }
        }
        switch (npc.getId()) {
        case 2417:
            if (!hasLevel(player, npc, 94, check))
                return false;
        case 9462:// ice
            if (!hasLevel(player, npc, 93, check))
                return false;
        case 9464:
            if (!hasLevel(player, npc, 77, check))
                return false;
        case 9466:
            if (!hasLevel(player, npc, 73, check))
                return false;
            boolean noTask = npc.getId() != 2417;
            if (player.getTask() != null) {
                if (player.getTask().getName(player).equalsIgnoreCase("Desert strykewyrm") && npc.getId() == 9464)
                    noTask = false;
                if (player.getTask().getName(player).equalsIgnoreCase("Ice strykewyrm") && npc.getId() == 9462)
                    noTask = false;
                if (player.getTask().getName(player).equalsIgnoreCase("Jungle strykewyrm") && npc.getId() == 9466)
                    noTask = false;
            } else
                noTask = true;

            if (npc.getId() == 2417) // lava stryke code
                noTask = false;
            if (noTask) {
                if (!check) {
                    if (player.getRouteEvent() == null)
                        player.setRouteEvent(new RouteEvent(npc, () -> {
                            player.faceEntity(npc);
                            player.setNextAnimation(new Animation(4278));
                            WorldTasksManager.schedule(new WorldTask() {
                                @Override
                                public void run() {
                                    player.sendMessage("Nothing seems to happen.");
                                    player.setNextAnimation(new Animation(857));
                                    player.unlock();
                                }
                            }, 1);
                        }));
                    else {
                        player.faceEntity(npc);
                        player.setNextAnimation(new Animation(4278));
                        WorldTasksManager.schedule(new WorldTask() {
                            @Override
                            public void run() {
                                player.sendMessage("Nothing seems to happen.");
                                player.setNextAnimation(new Animation(857));
                                player.unlock();
                            }
                        }, 1);
                    }
                }
                return false;
            }
            if (!check) {
                if (player.getRouteEvent() == null) {
                    player.setRouteEvent(new RouteEvent(npc, () -> {
                        if (npc.isCantInteract())
                            return;
                        if (!npc.withinDistance(player, 14))
                            return;
                        if (player.getTemporaryAttributtes().get("stomping") != null)
                            return;
                        player.getTemporaryAttributtes().put("stomping", Boolean.TRUE);
                        player.faceEntity(npc);
                        npc.setCantInteract(true);
                        player.setNextAnimation(new Animation(4278));
                        WorldTasksManager.schedule(new WorldTask() {
                            int loop = 0;

                            @Override
                            public void run() {
                                if (loop == 1) {
                                    npc.setNextAnimation(new Animation(12795));
                                    npc.transformIntoNPC(npc.getId() == 2417 ? 20630 : npc.getId() + 1);
                                    npc.setNextGraphics(new Graphics(npc.getId() == 9463 ? 2318 : npc.getId() == 9465 ? 2316 : 2317));
                                }
                                if (loop == 2) {
                                    npc.setCantInteract(false);
                                    npc.getCombat().setTarget(player);
                                    npc.setAttackedBy(player);
                                    player.getTemporaryAttributtes().remove("stomping");
                                    stop();
                                }
                                loop++;
                            }
                        }, 0, 1);
                    }));
                } else {
                    player.faceEntity(npc);
                    player.getTemporaryAttributtes().put("stomping", Boolean.TRUE);
                    npc.setCantInteract(true);
                    player.setNextAnimation(new Animation(4278));
                    WorldTasksManager.schedule(new WorldTask() {
                        int loop = 0;

                        @Override
                        public void run() {
                            if (loop == 1) {
                                npc.setNextAnimation(new Animation(12795));
                                npc.transformIntoNPC(npc.getId() == 2417 ? 20630 : npc.getId() + 1);
                                npc.setNextGraphics(new Graphics(npc.getId() == 9463 ? 2318 : npc.getId() == 9465 ? 2316 : 2317));
                            }
                            if (loop == 2) {
                                npc.setCantInteract(false);
                                npc.getCombat().setTarget(player);
                                npc.setAttackedBy(player);
                                player.getTemporaryAttributtes().remove("stomping");
                                stop();
                            }
                            loop++;
                        }
                    }, 0, 1);
                }
            }
            return true;
        }
        return false;
    }

    private static boolean hasLevel(Player player, NPC npc, int skill, boolean check) {
        NPCDefinitions defs = NPCDefinitions.getNPCDefinitions(npc.getId() + 1);
        if (player.getSkills().getLevelForXp(18) < skill) {
            if (!check)
                player.getPackets().sendGameMessage("You need a Slayer level of " + skill + " to attack a " + defs.getName() + ".");
            return false;
        }
        return true;
    }

    @Override
    public void processNPC() {
        super.processNPC();
        if (getId() != stompId && !isCantInteract() && !isUnderCombat()) {
            setNextAnimation(new Animation(12796));
            setCantInteract(true);
            WorldTasksManager.schedule(new WorldTask() {
                @Override
                public void run() {
                    transformIntoNPC(getId() == 20630 ? 2417 : getId() - 1);
                    setCantInteract(false);
                    stop();
                }
            }, 2);
        }
    }

    @Override
    public void reset() {
        setNPC(stompId);
        super.reset();
    }

    @Override
    public void sendDeath(final Entity source) {
        resetWalkSteps();
        WorldTasksManager.schedule(new WorldTask() {
            int loop;

            @Override
            public void run() {
                if (loop == 0) {
                    setNextAnimation(new Animation(12793));
                } else if (loop == 3) {
                    getCombat().removeTarget();
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
}
