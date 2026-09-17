package com.rs.game.player.controllers;

import java.util.ArrayList;
import java.util.List;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.ForceTalk;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.npc.NPC;
import com.rs.game.npc.eds.EliteDungeonBoss;
import com.rs.game.npc.eds.EliteDungeonNPC;
import com.rs.game.npc.eds.MasutaTheAscended;
import com.rs.game.npc.eds.SeiryuTheAzureSerpent;
import com.rs.game.npc.eds.TheSanctumGuardian;
import com.rs.game.player.PerkManager.DonationPerk;
import com.rs.game.player.content.eds.EliteDungeon;
import com.rs.game.player.content.eds.EliteDungeon.Room;
import com.rs.game.player.content.eds.EliteDungeonsConstants;
import com.rs.game.player.content.eds.EliteDungeonsConstants.Doors;
import com.rs.game.player.content.eds.EliteDungeonsConstants.DungeonRooms;
import com.rs.game.player.content.eds.EliteDungeonsConstants.EliteDungeonTeleport;
import com.rs.game.player.content.eds.EliteDungeonsConstants.EliteDungeonsTeleports;
import com.rs.game.player.dialogue.impl.OptionSelectionD.OptionSelector;
import com.rs.game.player.dialogue.impl.OptionSelectionD.SelectionEvent;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;

import lombok.Getter;

public class EliteDungeonController extends Controller {
    private int eliteDungeonType;
    @Getter
    private transient EliteDungeonBoss current;

    @Override
    public void start() {
        EliteDungeon dungeon = player.getEliteDungeonsManager().getParty().getDungeon();
        if (dungeon != null) {
            eliteDungeonType = dungeon.getEliteDungeonType();
            setArguments(new Object[] { eliteDungeonType });
        } else {
            if (getArguments() != null && getArguments().length == 1) {
                eliteDungeonType = (int) getArguments()[0];
            }
        }
    }

    @Override
    public boolean canHit(Entity entity) {
        if (entity instanceof EliteDungeonBoss && current != entity) {
            current = (EliteDungeonBoss) entity;
            player.getInterfaceManager().closeOverlay(true);
            sendInterfaces();
        }
        if (current != null && current instanceof SeiryuTheAzureSerpent && current.getId() != 25593) {
            current = null;
            player.getInterfaceManager().closeOverlay(true);
            return false;
        }
        return super.canHit(entity);
    }

    @Override
    public void sendInterfaces() {
        if (current == null)
            return;
        updateInterface();
        player.getInterfaceManager().sendOverlay(1648, true);
        player.getTemporaryAttributtes().remove("waterbuff");
    }

    @Override
    public boolean keepCombating(boolean mainHand, Entity target) {
        if (target instanceof EliteDungeonBoss && current != target) {
            current = (EliteDungeonBoss) target;
            player.getInterfaceManager().closeOverlay(true);
            sendInterfaces();
        }
        if (current != null && current instanceof SeiryuTheAzureSerpent && current.getId() != 25593) {
            current = null;
            player.getInterfaceManager().closeOverlay(true);
            return false;
        }
        return super.keepCombating(mainHand, target);
    }

    public void updateInterface() {
        if (current == null)
            return;
        player.getPackets().sendConfig(5776, current.getBossMapId());
        player.getVarBitManager().sendVarBit(32672, current.getMaxHitpoints() * 10);
        player.getVarBitManager().sendVarBit(28663, current.getHitpoints() * 10);
    }

    @Override
    public void process() {
        if (current == null)
            return;
        if (current != null && current.getCombat().getTarget() == null) {
            if (current instanceof SeiryuTheAzureSerpent && ((SeiryuTheAzureSerpent) current).isNevermindChecks() && !((SeiryuTheAzureSerpent) current).isSentDeath())
                return;
            current = null;
            player.getInterfaceManager().closeOverlay(true);
            return;
        }
        if (current.isDead() || current.hasFinished() || (current.getId() >= 25594 && current.getId() <= 25595)) {
            current = null;
            player.getInterfaceManager().closeOverlay(true);
        }
    }

    @Override
    public boolean processObjectClick1(WorldObject object) {
        EliteDungeon dungeon = player.getEliteDungeonsManager().getParty().getDungeon();
        if (dungeon == null)
            return true;
        if (object.getId() == 5786) {
            player.getPackets().sendGameMessage("That door is locked.");
            return false;
        }
        if (object.getId() == 111439 || object.getId() == 111440) {
            boolean outSide = player.getY() > object.getY();
            if (outSide && (current == null || !(current instanceof SeiryuTheAzureSerpent) || !((SeiryuTheAzureSerpent) current).isKnockedOut()))
                return false;
            player.lock();
            WorldTasksManager.schedule(new WorldTask() {

                @Override
                public void run() {
                    player.setNextWorldTile(object.transform(0, outSide ? -5 : 5, 0));
                    player.unlock();
                }
            }, 1);
            return false;
        }
        if (object.getDefinitions().getActualName().equalsIgnoreCase("treasure chest")) {
            player.getEliteDungeonsManager().openRewardsChest();
            return false;
        }
        Room currentRoom = dungeon.getPlayerRoom(player);
        int sourceRoomIndex = currentRoom != null && currentRoom.getRoomData() != null ? currentRoom.getRoomData().getRoomIndex() : -1;
        for (Doors door : Doors.values()) {
            if (door.matches(dungeon.getEliteDungeonType(), sourceRoomIndex, object.getId())) {
                if (!object.getDefinitions().getName().equalsIgnoreCase("Hidden staircase")) {
                    int rot = object.getRotation();
                    if (rot == 1 || rot == 3) {
                        if (player.getY() != object.getY() + (rot == 1 ? 1 : -1)) {
                            player.getPackets().sendGameMessage("You can't reach that.");
                            return false;
                        }
                    } else if (rot == 0 || rot == 2) {
                        if (player.getX() != object.getX() + (rot == 2 ? 1 : -1)) {
                            player.getPackets().sendGameMessage("You can't reach that.");
                            return false;
                        }
                    }
                } else
                    player.getTemporaryAttributtes().remove("waterbuff");
                if (door.getRoom() == null) {
                    dungeon.leaveDungeon(player);
                    return false;
                }
                int roomIndex = door.getRoom().getRoomIndex();
                WorldTile customTile = door.isUseExitTile() && currentRoom != null && currentRoom.getRoomData() != null ? currentRoom.getRoomData().getExitTile() : null;
                if (!dungeon.enterRoom(player, roomIndex, customTile)) {
                    player.getDialogueManager().startDialogue("SimpleMessage", "That room is loading, please try again in a moment.");
                    return false;
                }
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean login() {
        if (getArguments() != null && getArguments().length == 1) {
            eliteDungeonType = (int) getArguments()[0];
        }
        player.setNextWorldTile(EliteDungeonsConstants.getOutsideTile(eliteDungeonType));
        player.setForceMultiArea(false);
        removeControler();
        return true;
    }

    @Override
    public boolean logout() {
        return false;
    }

    @Override
    public boolean sendDeath() {
        EliteDungeon dungeon = player.getEliteDungeonsManager().getParty().getDungeon();
        if (dungeon == null)
            return true;
        dungeon.leaveDungeon(player, true);
        player.lock(8);
        player.stopAll();
        removeControler();
        WorldTasksManager.schedule(new WorldTask() {
            int loop;

            @Override
            public void run() {
                if (loop == 0) {
                    player.setNextAnimation(player.getDeathAnimation());
                    player.getTemporaryAttributtes().put("skipElite", Boolean.TRUE);
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
    public void processIngoingHit(Hit hit) {
        if (hit != null && hit.getSource() instanceof MasutaTheAscended && hit.getLook() == HitLook.MAGIC_DAMAGE) {
            int buffStacks = MasutaTheAscended.getThrashingWaterBuff(player);
            double debuffAmount = buffStacks * 0.05;
            if (debuffAmount >= 0.70)
                debuffAmount = 0.70;
            int damage = hit.getDamage() - (int) (hit.getDamage() * debuffAmount);
            hit.setDamage(damage <= 0 ? 0 : damage);
        }
        super.processIngoingHit(hit);
    }

    @Override
    public void processIncommingHit(Hit hit, Entity target) {
        super.processIncommingHit(hit, target);
    }

    @Override
    public boolean processButtonClick(int interfaceId, int componentId, int slotId, int itemId, int packetId) {
        if (interfaceId == 168) {
            player.getEliteDungeonsManager().HandleButtons(interfaceId, componentId, slotId, itemId, packetId);
            return false;
        }
        return true;
    }

    @Override
    public boolean processObjectClick2(WorldObject object) {
        if (object.getDefinitions().getActualName().equalsIgnoreCase("treasure chest")) {
            player.getEliteDungeonsManager().toggleAutoLoot();
            return false;
        }
        EliteDungeon dungeon = player.getEliteDungeonsManager().getParty().getDungeon();
        if (dungeon == null)
            return true;
        Room currentRoom = dungeon.getPlayerRoom(player);
        int sourceRoomIndex = currentRoom != null && currentRoom.getRoomData() != null ? currentRoom.getRoomData().getRoomIndex() : -1;
        for (Doors door : Doors.values()) {
            if (door.matches(dungeon.getEliteDungeonType(), sourceRoomIndex, object.getId())) {
                if (!object.getDefinitions().getName().equalsIgnoreCase("Hidden staircase")) {
                    int rot = object.getRotation();
                    if (rot == 1 || rot == 3) {
                        if (player.getY() != object.getY() + (rot == 1 ? 1 : -1)) {
                            player.getPackets().sendGameMessage("You can't reach that.");
                            return false;
                        }
                    } else if (rot == 0 || rot == 2) {
                        if (player.getX() != object.getX() + (rot == 2 ? 1 : -1)) {
                            player.getPackets().sendGameMessage("You can't reach that.");
                            return false;
                        }
                    }
                }
                dungeon.leaveDungeon(player);
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean processObjectClick3(WorldObject object) {
        if (object.getDefinitions().getActualName().equalsIgnoreCase("treasure chest")) {
            EliteDungeon dungeon = player.getEliteDungeonsManager().getParty().getDungeon();
            if (dungeon == null || !dungeon.isInside(player))
                return false;
            EliteDungeonsTeleports teleportData = EliteDungeonsTeleports.getTeleportsByDungonType(dungeon.getEliteDungeonType());
            if (teleportData == null || teleportData.getTeleports().length == 0) {
                player.getPackets().sendGameMessage("There are no elite dungeon teleports available here.");
                return false;
            }
            EliteDungeonTeleport[] teleports = teleportData.getTeleports();
            List<String> options = new ArrayList<String>();
            for (int i = 0; i < dungeon.getMaxTeleportsSize() && i < teleports.length; i++)
                options.add(teleports[i].getName());
            options.add("No where");
            player.getDialogueManager().startDialogue("OptionSelectionD", "Where do you want to go?", options.toArray(new String[options.size()]), options.toArray(new String[options.size()]), (SelectionEvent) (selector, option) -> {
                int page = selector.getPage();
                switch (page) {
                case 0:
                    selector.close();
                    switch (option) {
                    case OptionSelector.OPTION_1:
                        if (dungeon.getMaxTeleportsSize() >= 1)
                            sendTeleport(teleports[0]);
                        break;
                    case OptionSelector.OPTION_2:
                        if (dungeon.getMaxTeleportsSize() >= 2 && teleports.length >= 2)
                            sendTeleport(teleports[1]);
                        break;
                    case OptionSelector.OPTION_3:
                        if (dungeon.getMaxTeleportsSize() >= 3 && teleports.length >= 3)
                            sendTeleport(teleports[2]);
                        break;
                    case OptionSelector.OPTION_4:
                        if (dungeon.getMaxTeleportsSize() >= 4 && teleports.length >= 4)
                            sendTeleport(teleports[3]);
                        break;
                    }
                    break;
                }
            });
            return false;
        }
        return true;
    }

    public void sendTeleport(EliteDungeonTeleport teleport) {
        EliteDungeon dungeon = player.getEliteDungeonsManager().getParty().getDungeon();
        if (dungeon == null || !dungeon.isInside(player))
            return;
        int roomIndex = teleport.getRoomIndex();
        if (!dungeon.enterRoom(player, roomIndex, teleport.getLocation(), true)) {
            player.getDialogueManager().startDialogue("SimpleMessage", "That room is loading, please try again in a moment.");
            return;
        }
    }

    private transient EliteDungeonTeleport teleport;
    private transient int teleportDungeonType = -1;

    @Override
    public void moved() {
        if (player.getEliteDungeonsManager().getParty() == null)
            return;
        EliteDungeon dungeon = player.getEliteDungeonsManager().getParty().getDungeon();
        if (dungeon == null || !dungeon.isInside(player))
            return;
        Room room = dungeon.getPlayerRoom(player);
        if (room == null)
            return;
        int roomIndex = room.getRoomData().getRoomIndex();
        EliteDungeonsTeleports teleportData = EliteDungeonsTeleports.getTeleportsByDungonType(dungeon.getEliteDungeonType());
        if (teleportData == null)
            return;
        if (teleport == null || teleport.getRoomIndex() != roomIndex || teleportDungeonType != dungeon.getEliteDungeonType()) {
            teleport = EliteDungeonsTeleports.getTeleportByDungonTypeAndRoomIndex(dungeon.getEliteDungeonType(), room.getRoomData().getRoomIndex());
            teleportDungeonType = dungeon.getEliteDungeonType();
        }
        if (teleport == null)
            return;
        if (player.withinDistance(room.getTile(teleport.getLocation()), 10)) {
            EliteDungeonTeleport[] teleports = teleportData.getTeleports();
            for (int i = 0; i < teleports.length; i++) {
                if (teleports[i].getRoomIndex() == roomIndex && dungeon.getMaxTeleportsSize() < i + 1) {
                    dungeon.setMaxTeleportsSize(i + 1);
                    break;
                }
            }
        }
    }

    @Override
    public boolean processMagicTeleport(WorldTile toTile) {
        EliteDungeon dungeon = player.getEliteDungeonsManager().getParty().getDungeon();
        if (dungeon == null || !dungeon.isInside(player))
            return true;
        dungeon.leaveDungeon(player, true);
        return true;
    }

    @Override
    public boolean processItemTeleport(WorldTile toTile) {
        EliteDungeon dungeon = player.getEliteDungeonsManager().getParty().getDungeon();
        if (dungeon == null || !dungeon.isInside(player))
            return true;
        dungeon.leaveDungeon(player, true);
        return true;
    }

    @Override
    public boolean processObjectTeleport(WorldTile toTile) {
        EliteDungeon dungeon = player.getEliteDungeonsManager().getParty().getDungeon();
        if (dungeon == null || !dungeon.isInside(player))
            return true;
        dungeon.leaveDungeon(player, true);
        return true;
    }

    @Override
    public boolean processNPCClick1(NPC npc) {
        if (npc.getId() == 25595) {
            player.getDialogueManager().startDialogue("SimpleMessage", "Seiryu acknowledges you gratefully as he slowly recovers.");
            return false;
        }
        return true;
    }

    @Override
    public boolean processNPCClick2(NPC npc) {
        if (npc instanceof EliteDungeonNPC && npc.getDefinitions().hasOption("Mark")) {
            EliteDungeon dungeon = player.getEliteDungeonsManager().getParty().getDungeon();
            if (dungeon == null || !dungeon.isInside(player))
                return false;
            if (!dungeon.getParty().isLeader(player)) {
                player.getPackets().sendGameMessage("Only your party's leader can mark a target!");
                return false;
            }
            dungeon.setMark(npc, !player.getHintIconsManager().hasHintIcon(6));
            return false;
        }
        return super.processNPCClick2(npc);
    }

    @Override
    public boolean processItemOnNPC(NPC npc, Item item) {
        if (npc.getId() == 25595 && item.getId() == 314 && npc instanceof SeiryuTheAzureSerpent) {
            player.getInventory().deleteItem(314, 1);
            npc.setNextForceTalk(new ForceTalk("ACHOOOO!"));
            return false;
        } else if (npc instanceof TheSanctumGuardian && item.getId() == 43067) {
            if (npc.isDead() || npc.hasFinished() || ((TheSanctumGuardian) npc).dead)
                return true;
            player.getInventory().deleteItem(43067, 1);
            npc.setNextForceTalk(new ForceTalk("A fishy treat!? It may pass!"));
            npc.sendDeath(player);
            return false;
        }
        return true;
    }

    @Override
    public boolean processObjectClick4(WorldObject object) {
        if (object.getDefinitions().getActualName().equalsIgnoreCase("treasure chest")) {
            EliteDungeon dungeon = player.getEliteDungeonsManager().getParty() != null ? player.getEliteDungeonsManager().getParty().getDungeon() : null;
            if (dungeon == null || dungeon.getEliteDungeonType() == EliteDungeonsConstants.TEMPLE_OF_AMINISHI) {
                int amountRequired = player.getPerkManager().hasPerkActive(DonationPerk.DUNGEONS_MASTER) ? 20 : 30;
                if (player.getKillStatistics(140) < amountRequired) {
                    player.getDialogueManager().startDialogue("SimpleMessage", "You can't use this bank without having atleast " + amountRequired + " Seiryu the Azure Serpent kills.");
                    return false;
                }
            }
            if (player.isUnderCombat(5)) {
                player.sendMessage("You can't use bank until 5 seconds after the end of combat.");
                return false;
            }
            if (!player.promptList()) {
                player.getBank().openBank();
            } else {
                player.getDialogueManager().startDialogue("BankList", false);
            }
            return false;
        }
        return true;
    }

}
