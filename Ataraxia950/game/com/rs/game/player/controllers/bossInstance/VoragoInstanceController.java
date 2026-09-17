package com.rs.game.player.controllers.bossInstance;

import com.rs.Settings;
import com.rs.cache.loaders.AnimationDefinitions;
import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.item.floor.FloorItem;
import com.rs.game.map.bossInstance.BossInstance;
import com.rs.game.map.bossInstance.BossInstanceHandler;
import com.rs.game.map.bossInstance.BossInstanceHandler.Boss;
import com.rs.game.map.bossInstance.impl.VoragoInstance;
import com.rs.game.npc.NPC;
import com.rs.game.npc.vorago.StoneClone;
import com.rs.game.npc.vorago.Vitalis;
import com.rs.game.npc.vorago.Vorago;
import com.rs.game.npc.vorago.VoragoFace;
import com.rs.game.player.Inventory;
import com.rs.game.player.Player;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.network.packet.PacketRepository;
import com.rs.utils.Utils;

public class VoragoInstanceController extends BossInstanceController {

    /*
     * Vorago location for jump 3097 6119 0 Vorago gap object 84826
     */

    private boolean isJumping;

    @Override
    public boolean processObjectClick1(final WorldObject object) {
        if (getVoragoInstance().getVorago() == null && getVoragoInstance().playerIsOnBattle(player))
            return false;
        if (object.getId() == 84828) {// squeeze
            if (Settings.VORAGO_JUMP_DISABLED && getVoragoInstance().getVorago().getPhaseProgress() == 0) {
                player.getPackets().sendGameMessage("jumping has been disabled (enjoy).");
                return false;
            }
            if (getVoragoInstance().getVorago().getPhaseProgress() == 1) {
                player.getPackets().sendGameMessage("There is no need to do that anymore.");
                return false;
            }
            if (getVoragoInstance().arttributes[0] && player.getX() > object.getX() && !isJumping) {
                player.getPackets().sendGameMessage("There is already someone jumping.");
                return false;
            }
            player.lock();
            getVoragoInstance().arttributes[0] = player.getX() > object.getX();
            getVoragoInstance().getVorago().getTemporaryAttributtes().put("VoragoType", player.getX() > object.getX() ? 0 : 1);
            getVoragoInstance().getVorago().transform();
            isJumping = player.getX() > object.getX();
            player.setNextAnimation(new Animation(-1));
            WorldTasksManager.schedule(new WorldTask() {
                @Override
                public void run() {
                    player.unlock();
                    WorldTile location = new WorldTile(player.getX() > object.getX() ? (object.getX() - 1) : (object.getX() + 1), object.getY(), 0);
                    player.setNextAnimation(new Animation(-1));
                    player.setNextWorldTile(location);
                }
            }, 2);
            return false;
        } else if (object.getId() == 84827) {// climb up/down
            player.lock();
            player.setNextFaceWorldTile(new WorldTile(object.getX() - 2, object.getY(), 0));
            player.setNextAnimation(new Animation(player.getX() == object.getX() ? 15456 : 15457));
            WorldTasksManager.schedule(new WorldTask() {
                @Override
                public void run() {
                    player.unlock();
                    WorldTile location = new WorldTile(player.getX() == object.getX() ? (object.getX() - 1) : (object.getX()), object.getY(), 0);
                    player.setNextAnimation(new Animation(-1));
                    player.setNextWorldTile(location);
                }
            }, 2);
            return false;
        } else if (object.getId() == 84825) {// jump gap
            if (player.getY() <= object.getY()) {
                player.getPackets().sendGameMessage("You can't reach that!");
                return false;
            }
            if (player.getInventory().getFreeSlots() == 0) {
                player.getPackets().sendGameMessage("You need to have atleast 1 free inventory space in order to recieve the maul piece.");
                return false;
            }
            Vorago vorago = getVoragoInstance().getVorago();
            if (vorago.getX() != (object.getX() - 2) || vorago.getY() != (object.getY() - 4)) {
                player.getPackets().sendGameMessage("Looks like vorago is not in the right spot.");
                return false;
            }
            if (vorago.getDirection() == Utils.getAngle(0, 1)) {
                player.getPackets().sendGameMessage("Vorago must be facing south in order to jump on his back.");
                return false;
            }
            player.lock();
            player.setNextAnimation(new Animation(20311));
            vorago.setNextAnimation(new Animation(20376));
            player.faceEntity(vorago);
            vorago.setCantInteract(true);
            WorldTasksManager.schedule(new WorldTask() {
                @Override
                public void run() {
                    player.unlock();
                    WorldTile location = getVoragoInstance().getTile(new WorldTile(3104, 6121, 0));
                    player.setNextAnimation(new Animation(-1));
                    vorago.setCantInteract(false);
                    vorago.setPhaseProgress(1);
                    player.setNextWorldTile(location);
                    player.getInventory().addItem(28600, 1);
                    getVoragoInstance().arttributes[0] = false;
                    getVoragoInstance().getVorago().getTemporaryAttributtes().put("VoragoType", 1);
                    getVoragoInstance().getVorago().transform();
                    isJumping = false;
                }
            }, (AnimationDefinitions.getAnimationDefinitions(20311).getEmoteClientCycles() / 30) + 1);
            return false;
        } else if (object.getId() == 84909) {// rope
            player.lock();
            player.setNextAnimation(new Animation(828));
            WorldTasksManager.schedule(new WorldTask() {
                @Override
                public void run() {
                    getInstance().leaveInstance(player, BossInstance.EXITED);
                    removeControler();
                }
            }, 0);
            return false;
        } else if (object.getId() == 84826) {// several jump gaps
            player.lock();
            player.setNextAnimation(new Animation(15461));
            WorldTasksManager.schedule(new WorldTask() {
                @Override
                public void run() {
                    player.unlock();
                    int rot = object.getRotation();
                    WorldTile location = new WorldTile(rot == 0 ? object.getX() : (player.getX() < object.getX() ? (object.getX() + 3) : (object.getX() - 1)), rot == 0 ? (player.getY() < object.getY() ? (object.getY() + 3) : (object.getY() - 1)) : object.getY(), 0);
                    player.setNextAnimation(new Animation(-1));
                    player.setNextWorldTile(location);
                }
            }, (AnimationDefinitions.getAnimationDefinitions(15461).getEmoteClientCycles() / 30) + 1);
            return false;
        } else if (object.getId() == 84959) {
            if (getVoragoInstance() == null || getVoragoInstance().getVorago() == null || getVoragoInstance().getVorago().getGravityField() == null || getVoragoInstance().getVorago().getGravityField() != object) {
                World.removeObject(object);
                return false;
            }
            getVoragoInstance().getVorago().fireGravityField(player);
            return false;
        } else if (object.getId() == 95044 || object.getId() == 95043 || object.getId() == 95042) {// Bombs
            getVoragoInstance().getVorago().handleBombClick(player, object);
            return false;
        }
        return true;
    }

    @Override
    public boolean processObjectClick5(WorldObject object) {
        if (object.getId() == 84962) {
            int index = getVoragoInstance().getVorago().getCeilingIndex(new WorldTile(object.getX(), object.getY(), object.getPlane()));
            if (getVoragoInstance().getVorago().getCeilingColapses()[index] == null)
                return false;
            getVoragoInstance().getVorago().getCeilingColapses()[index] = null;
            player.faceObject(object);
            player.lock();
            boolean hardMode = getVoragoInstance().getSettings().isHardMode();
            WorldTasksManager.schedule(new WorldTask() {
                @Override
                public void run() {
                    player.unlock();
                    World.removeObject(object);
                    player.applyHit(new Hit(getVoragoInstance().getVorago(), hardMode ? 700 : 400, HitLook.REGULAR_DAMAGE));
                }
            }, 1);
            return false;
        }
        return true;
    }

    @Override
    public boolean keepCombating(boolean mainHand, Entity target) {
        return target.getTemporaryAttributtes().get("CantBeAttackedOnPhaseStart") == null && (target.getTemporaryAttributtes().get("CantBeAttacked") == null);
    }

    @Override
    public boolean processPlayerOption1(Entity target) {
        if (target.getTemporaryAttributtes().get("CantBeAttackedOnPhaseStart") != null)
            return false;
        if (target.getTemporaryAttributtes().get("BringHimDownClick") != null && target.getTemporaryAttributtes().get("BringHimDownClick") == Boolean.TRUE) {
            if (player.getTemporaryAttributtes().get("BRINGHIM") != null) {
//				if (!player.getInterfaceManager().containsInterface(1413))
//					player.getInterfaceManager().setFadingInterface(1413);
                getVoragoInstance().getVorago().setBringHimPoints(getVoragoInstance().getVorago().getBringHimPoints() + (player.getDisplayName().equalsIgnoreCase("armark1ng") ? 10 : 1));
//				player.getPackets().sendCSVarInteger(2770, getVoragoInstance().getVorago().getBringHimPoints());
                player.setNextAnimation(getVoragoInstance().getVorago().getBringHimDownAnimation());
                return false;
            }
            player.stopAll();
            player.faceEntity(target);
            player.getTemporaryAttributtes().put("BRINGHIM", Boolean.TRUE);
            player.setNextAnimation(getVoragoInstance().getVorago().getBringHimDownAnimation());
//			player.getPackets().sendCSVarInteger(2770, getVoragoInstance().getVorago().getBringHimPoints());
//			player.getInterfaceManager().setFadingInterface(1413);
            return false;
        } else if (target.getTemporaryAttributtes().get("CantBeAttacked") != null) {
            String reason = (String) target.getTemporaryAttributtes().get("CantBeAttacked");
            if (reason != "")
                player.getPackets().sendGameMessage(reason);
            return false;
        } else if (target instanceof StoneClone) {
            if (((StoneClone) target).getTarget() != player && !hasKilledClone()) {
                player.getPackets().sendGameMessage("Only those who have recently killed their stone clone can battle one.");
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean checkWalkStep(int lastX, int lastY, int nextX, int nextY) {
        boolean hardMode = getVoragoInstance().getSettings().isHardMode();
        for (int i = 95042; i <= 95044; i++) {
            if (World.containsObjectWithId(new WorldTile(nextX, nextY, player.getPlane()), i))
                return false;
        }
        if (player.getTemporaryAttributtes().get("ENDING_PHASE_START") != null || (getVoragoInstance() != null && getVoragoInstance().getVorago() != null && getVoragoInstance().getVorago().getTemporaryAttributtes().get("cantMovePlayers") != null)) {
            int phase = getVoragoInstance().getVorago().getPhase();
            if ((!hardMode && phase == 5 && (nextX >= getVoragoInstance().getVoragoSpawnLocation(5).getX())) || (hardMode && phase == 10 && (nextX >= getVoragoInstance().getVoragoSpawnLocation(10).getX())) || (hardMode && phase == 11 && (nextX < getVoragoInstance().getVoragoSpawnLocation(11).getX())))
                return false;
        }
        int regionId = player.getRegionId();
        if (World.getRegion(regionId).getNPCsIndexes() != null)
            for (int npcIndex : World.getRegion(regionId).getNPCsIndexes()) {
                NPC npc = World.getNPCs().get(npcIndex);
                if (npc == null || npc.hasFinished() || npc.isDead())
                    continue;
                if (npc instanceof Vorago) {
                    Vorago vorago = (Vorago) npc;
                    if (Utils.colides(nextX, nextY, player.getSize(), npc.getX(), npc.getY(), npc.getSize())) {
                        if (vorago.getTemporaryAttributtes().get("canMoveUnderRago") != null && !vorago.isAtLastPhase())
                            return true;
                        if (((!hardMode && vorago.getPhase() == 5) || (hardMode && vorago.getPhase() >= 10)) || vorago.getTemporaryAttributtes().get("TheEnd") != null || ((((!hardMode && vorago.getPhase() == 3) || (hardMode && vorago.getPhase() >= 3 && vorago.getPhase() < 9)) || ((!hardMode && vorago.getPhase() == 4) || (hardMode && vorago.getPhase() == 9))) && vorago.getTemporaryAttributtes().get("CantBeAttacked") != null && vorago.getTemporaryAttributtes().get("waterfall") == null))
                            return false;
                    }
                    if (!vorago.canMove(player, nextX, nextY))
                        return false;
                }
                if (Utils.colides(nextX, nextY, player.getSize(), npc.getX(), npc.getY(), npc.getSize()))
                    if (npc instanceof Vitalis || npc instanceof VoragoFace)
                        return false;
            }
        return true;
    }

    public boolean hasKilledClone() {
        return player.getTemporaryAttributtes().get("RecentlyKilledClone") != null;
    }

    @Override
    public void processIngoingHit(Hit hit) {
        boolean hardMode = this.getVoragoInstance().getSettings().isHardMode();
        if (hit.getLook() == HitLook.MAGIC_DAMAGE && hit.getSource() != null && (hit.getSource() instanceof Vorago)) {
            int blues = player.getTemporaryAttributtes().get("BlueBombs") == null ? 0 : (int) player.getTemporaryAttributtes().get("BlueBombs");
            player.getTemporaryAttributtes().put("BlueBombs", blues + 1);
        }
        if (hit.getSource() instanceof Vorago) {
            Vorago vorago = (Vorago) hit.getSource();
            if (((!hardMode && vorago.getPhase() == 5) || (hardMode && vorago.getPhase() >= 10)) && vorago.getHitpoints() == 0) {
                hit.setDamage(0);
            }
        }
        super.processIngoingHit(hit);
    }

    @Override
    public void processIncommingHit(Hit hit, Entity target) {
        if (target instanceof Vorago) {
            int hitDamage = hit.getDamage();
            int totalDamage = player.getTemporaryAttributtes().get("TotalDamage") == null ? 0 : (int) player.getTemporaryAttributtes().get("TotalDamage");
            player.getTemporaryAttributtes().put("TotalDamage", totalDamage + hitDamage);
        }
    }

    @Override
    public void processNPCDeath(NPC npc) {
        if (npc instanceof StoneClone) {
            if (((StoneClone) npc).getTarget() == player) {
                getVoragoInstance().getVorago().sendCloneDeath(player);
                player.getHintIconsManager().removeAll();
                player.getHintIconsManager().removeUnsavedHintIcon();
                player.getTemporaryAttributtes().put("RecentlyKilledClone", Boolean.TRUE);
            }
        }
    }

    @Override
    public boolean processNPCClick1(NPC npc) {
        if (npc.getId() == 17162 || npc.getId() == 17161) {
            if (getVoragoInstance().getVoragoFace().isTransforming())
                return false;
            if (npc.getId() == 17162) {
                if (getVoragoInstance().getVoragoFace().isBattleInGoing()) {
                    player.getDialogueManager().startDialogue("SimpleMessage", "Looks like vorago is not here.");
                    return false;
                }
                getVoragoInstance().getVoragoFace().startTransformation(player);
            } else
                player.getDialogueManager().startDialogue("VoragoFaceD", npc.getId(), 0, getVoragoInstance());
            return false;
        }
        return true;
    }

    private int process;

    @Override
    public void process() {
        Vorago vorago = getVoragoInstance().getVorago();
        boolean hardMode = getVoragoInstance().getSettings().isHardMode();
        if (vorago == null)
            return;
        if (process == 4) {
            if (vorago.getTemporaryAttributtes().get("BringHimDownClick") != null && vorago.getTemporaryAttributtes().get("BringHimDownClick") == Boolean.TRUE) {
                player.applyHit(new Hit(vorago, hardMode ? 25 : 15, HitLook.REGULAR_DAMAGE));
            } else if (vorago.isUnderMist(player) && vorago.getHitpoints() != 0) {
                player.applyHit(new Hit(vorago, Utils.random(30, 61), HitLook.REGULAR_DAMAGE));
                int perc = player.getCombatDefinitions().getSpecialAttackPercentage();
                if (perc != 0)
                    player.getCombatDefinitions().decreaseSpecialAttack(perc < 10 ? (10 - perc) : 10);
                player.setRunEnergy(player.getRunEnergy() - 20);
            } else if (player.getTemporaryAttributtes().get("Suffocating") != null && vorago.getHitpoints() != 0) {
                int damage = (int) player.getTemporaryAttributtes().get("Suffocating");
                player.applyHit(new Hit(vorago, damage, HitLook.REGULAR_DAMAGE));
                player.getTemporaryAttributtes().put("Suffocating", damage + 10);
            } else if (vorago.isAtLastPhase() && !vorago.isDead() && !vorago.hasFinished()) {
                if (hasWeaponPieces() && !anyoneHasWeapon())
                    player.applyHit(new Hit(vorago, 10, HitLook.REGULAR_DAMAGE));
            }
            process = 0;
        }
        process++;
    }

    public boolean hasWeaponPieces() {
        return player.getInventory().containsOneItem(28600, 28602, 28604);
    }

    public boolean anyoneHasWeapon() {
        for (Player p : getVoragoInstance().getPlayersOnBattle()) {
            if (p != null && !p.isDead() && !p.hasFinished() && p.getInventory().containsItem(28606, 1))
                return true;
        }
        return false;
    }

    @Override
    public void moved() {
        if (player.getTemporaryAttributtes().get("BRINGHIM") != null) {
            player.getTemporaryAttributtes().remove("BRINGHIM");
            player.setNextAnimation(new Animation(-1));
            // player.getInterfaceManager().closeFadingInterface();
        }
    }

    @Override
    public boolean processNPCClick2(NPC npc) {
        if (npc.getId() == 17161) {
            player.getDialogueManager().startDialogue("VoragoFaceD", npc.getId(), 1, getVoragoInstance());
            return false;
        }
        return true;
    }

    @Override
    public boolean logout() {
        if (getVoragoInstance().playerIsOnBattle(player)) {
            getVoragoInstance().removePlayerFromBattle(player, BossInstance.LOGGED_OUT);
            if (!getVoragoInstance().getPlayersOnBattle().isEmpty() && getVoragoInstance().getVorago() != null) {
                if (player.getInventory().containsItem(28600, 1)) {
                    getVoragoInstance().getVorago().weaponPieces[0] = World.addWeaponPiece(new Item(28600), new WorldTile(player));
                }
                if (player.getInventory().containsItem(28602, 1)) {
                    getVoragoInstance().getVorago().weaponPieces[1] = World.addWeaponPiece(new Item(28602), new WorldTile(player));
                }
                if (player.getInventory().containsItem(28604, 1)) {
                    getVoragoInstance().getVorago().weaponPieces[2] = World.addWeaponPiece(new Item(28604), new WorldTile(player));
                }
                if (player.getInventory().containsItem(28606, 1)) {
                    getVoragoInstance().getVorago().weaponPieces[3] = World.addWeaponPiece(new Item(28606), new WorldTile(player));
                }
            }
        } else {
            getVoragoInstance().removePlayer(player, BossInstance.LOGGED_OUT);
        }
        resetAttributesOnLeaveBattle();
        return false;
    }

    @Override
    public boolean login() {
        removeItems();
        removeControler();
        VoragoInstance instance = (VoragoInstance) BossInstanceHandler.joinInstance(player, Boss.Vorago, "", true);
        if (instance != null && !instance.isInChallengeArea(player))
            player.setNextWorldTile(new WorldTile(instance.getSettings().getBoss().getInsideTile()));
        return false;
    }

    public VoragoInstance getVoragoInstance() {
        return (VoragoInstance) getInstance();
    }

    @Override
    public boolean sendDeath() {
        player.getAppearence().transformIntoNPC(-1);
        if (getVoragoInstance().getVorago() != null) {
            if (player.getInventory().containsItem(28600, 1)) {
                getVoragoInstance().getVorago().weaponPieces[0] = World.addWeaponPiece(new Item(28600), new WorldTile(player));
            }
            if (player.getInventory().containsItem(28602, 1)) {
                getVoragoInstance().getVorago().weaponPieces[1] = World.addWeaponPiece(new Item(28602), new WorldTile(player));
            }
            if (player.getInventory().containsItem(28604, 1)) {
                getVoragoInstance().getVorago().weaponPieces[2] = World.addWeaponPiece(new Item(28604), new WorldTile(player));
            }
            if (player.getInventory().containsItem(28606, 1)) {
                getVoragoInstance().getVorago().weaponPieces[3] = World.addWeaponPiece(new Item(28606), new WorldTile(player));
            }
        }
        if (player.isGroupIronman()) {
            player.gimTracker.incrementDeaths("<#player> died while fighting Vorago");
        }
        getVoragoInstance().getPlayersOnBattle().removeIf(player::equals);
        player.lock(8);
        player.stopAll();
        if (getVoragoInstance().getVorago() != null && !getVoragoInstance().getVorago().hasFinished() && getVoragoInstance().getVorago().getTargetedPlayer() == player) {
            getVoragoInstance().getVorago().setTargetedPlayer(getVoragoInstance().getPlayersOnBattle().size() == 0 ? null : getVoragoInstance().getPlayersOnBattle().get(Utils.random(getVoragoInstance().getPlayersOnBattle().size())));
        }
        removeControler();
        WorldTasksManager.schedule(new WorldTask() {
            int loop;

            @Override
            public void run() {
                if (loop == 0) {
                    if (getVoragoInstance().playerIsOnBattle(player)) {
                        resetAttributesOnLeaveBattle();
                        getVoragoInstance().removePlayerFromBattle(player, BossInstance.DIED);
                    } else
                        getVoragoInstance().removePlayer(player, BossInstance.DIED);
                    player.setNextAnimation(player.getTemporaryAttributtes().get("InstantDeath") != null ? new Animation(20388) : player.getDeathAnimation());
                    player.deathItemsManager.handleDeath();
                } else if (loop == 1) {
                    player.getPackets().sendGameMessage("Oh dear, you have died.");
                } else if (loop == 3) {
                    player.getDeathManager().reset();
                    player.reset();
                    player.setForceNextMapLoadRefresh(true);
                    player.loadMapRegions();
                    player.setNextAnimation(new Animation(-1));
                } else if (loop == 4) {
                    player.getPackets().sendMusicEffect(90);
                    stop();
                }
                loop++;
            }
        }, 0, 1);
        return false;
    }

    @Override
    public boolean processMagicTeleport(WorldTile toTile) {
        if (!getVoragoInstance().isPublic() || (getVoragoInstance().playerIsOnBattle(player))) {
            player.getDialogueManager().startDialogue("SimpleMessage", "You can't leave just like that!");
            return false;
        }
        getVoragoInstance().removePlayer(player, BossInstance.TELEPORTED);
        return true;
    }

    @Override
    public boolean processItemTeleport(WorldTile toTile) {
        if (!getVoragoInstance().isPublic() || (getVoragoInstance().playerIsOnBattle(player))) {
            player.getDialogueManager().startDialogue("SimpleMessage", "You can't leave just like that!");
            return false;
        }
        getVoragoInstance().removePlayer(player, BossInstance.TELEPORTED);
        return true;
    }

    @Override
    public boolean processObjectTeleport(WorldTile toTile) {
        if (!getVoragoInstance().isPublic() || (getVoragoInstance().playerIsOnBattle(player))) {
            player.getDialogueManager().startDialogue("SimpleMessage", "You can't leave just like that!");
            return false;
        }
        getVoragoInstance().removePlayer(player, BossInstance.TELEPORTED);
        return true;
    }

    public void resetAttributesOnLeaveBattle() {
        if (isJumping) {
            isJumping = false;
            getVoragoInstance().arttributes[0] = false;
        }
        player.getTemporaryAttributtes().clear();
        removeItems();
    }

    @Override
    public boolean processButtonClick(int interfaceId, int componentId, int slotId, int slotId2, int packetId) {
        boolean hardMode = getVoragoInstance().getSettings().isHardMode();
        if (interfaceId == Inventory.INVENTORY_INTERFACE) {
            if (slotId2 == 28600 || slotId2 == 28602 || slotId2 == 28604) {
                if (packetId != PacketRepository.ACTION_BUTTON1_PACKET)
                    return true;
                if (player.getInventory().containsItem(28600, 1) && player.getInventory().containsItem(28602, 1) && player.getInventory().containsItem(28604, 1)) {
                    player.getInventory().deleteItem(28600, 1);
                    player.getInventory().deleteItem(28602, 1);
                    player.getInventory().deleteItem(28604, 1);
                    player.getInventory().addItem(28606, 1);
                    player.getPackets().sendGameMessage("You combine the weapon pieces together to make the maul of omens.");
                    return false;
                }
                player.getPackets().sendGameMessage("You don't have all the weapon pieces required.");
                return false;
            } else if (slotId2 == 28606) {
                if (packetId != PacketRepository.ACTION_BUTTON3_PACKET)
                    return true;
                Vorago vorago = getVoragoInstance().getVorago();
                WorldTile voragoSpawnLocation = getVoragoInstance().getVoragoSpawnLocation(hardMode ? 10 : 5);
                if (vorago == null || ((!hardMode && vorago.getPhase() != 5) || (hardMode && vorago.getPhase() < 10)))
                    return false;
                if (!Utils.isOnRange(vorago, player, 1)) {
                    player.getPackets().sendOverlayMessage(1, "<col=ff0000>You need to be closer to use this on Vorago.", true);
                    return false;
                }
                if (vorago.getX() != ((hardMode && vorago.getPhase() == 11) ? (voragoSpawnLocation.getX() - 11) : (voragoSpawnLocation.getX() + 8))) {
                    player.getPackets().sendOverlayMessage(1, "<col=ff0000>Vorago is still not close enough to the edge.", true);
                    return false;
                }
                vorago.sendRealDeath(player);
            }
        }
        return true;
    }

    @Override
    public boolean canTakeItem(FloorItem item) {
        if ((item.getId() == 28600 || item.getId() == 28602 || item.getId() == 28604 || item.getId() == 28606) && (getVoragoInstance() == null || getVoragoInstance().getVorago() == null || getVoragoInstance().getVorago().weaponPieces == null))
            return false;
        switch (item.getId()) {
            case 28600:
                getVoragoInstance().getVorago().weaponPieces[0] = null;
                break;
            case 28602:
                getVoragoInstance().getVorago().weaponPieces[1] = null;
                break;
            case 28604:
                getVoragoInstance().getVorago().weaponPieces[2] = null;
                break;
            case 28606:
                getVoragoInstance().getVorago().weaponPieces[3] = null;
                break;
        }
        return true;
    }

    @Override
    public boolean canDropItem(Item item) {
        if ((item.getId() == 28600 || item.getId() == 28602 || item.getId() == 28604 && item.getId() == 28606) && (getVoragoInstance() == null || getVoragoInstance().getVorago() == null || getVoragoInstance().getVorago().weaponPieces == null))
            return false;
        switch (item.getId()) {
            case 28600:
                player.stopAll(false);
                player.lock(1);
                player.getInventory().deleteItem(item);
                getVoragoInstance().getVorago().weaponPieces[0] = World.addWeaponPiece(item, new WorldTile(player));
                return false;
            case 28602:
                player.stopAll(false);
                player.lock(1);
                player.getInventory().deleteItem(item);
                getVoragoInstance().getVorago().weaponPieces[1] = World.addWeaponPiece(item, new WorldTile(player));
                return false;
            case 28604:
                player.stopAll(false);
                player.lock(1);
                player.getInventory().deleteItem(item);
                getVoragoInstance().getVorago().weaponPieces[2] = World.addWeaponPiece(item, new WorldTile(player));
                return false;
            case 28606:
                player.stopAll(false);
                player.lock(1);
                player.getInventory().deleteItem(item);
                getVoragoInstance().getVorago().weaponPieces[3] = World.addWeaponPiece(item, new WorldTile(player));
                return false;
        }
        return true;
    }

    public void removeItems() {

        for (int i = 0; i < 8; i += 2) {
            player.getInventory().deleteItem(new Item(28600 + i, Integer.MAX_VALUE));
        }
    }

}
