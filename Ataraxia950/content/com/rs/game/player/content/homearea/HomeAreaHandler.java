package com.rs.game.player.content.homearea;

import com.rs.cache.loaders.NPCDefinitions;
import com.rs.cache.loaders.ObjectDefinitions;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import com.rs.game.player.content.packs.portable.PortableStation;

/**
 * @author Xenthium.
 */

public class HomeAreaHandler {

    private static final int REGION_ID = 16475;

    public static boolean playerIsAtHome(Player player) {
        return player.getRegionId() == REGION_ID;
    }

    public static boolean playerCanInteractWithObject(Player player, WorldObject object) {
        if (!playerIsAtHome(player)) {
            return true;
        }
        if (player.canByPassDonorObjects())
        	return true;
        for (HomeAreaObjectData dzObject : HomeAreaObjectData.values()) {
            if (dzObject.getObjectName().equalsIgnoreCase(object.getDefinitions().getName())) {
                if (object.getDefinitions().getName().startsWith("Divine ")) { // Handles the difference between spawned & player-placed divine locations.
                    if (object.getOwner() != null) {
                        return true;
                    }
                } else if (PortableStation.isPortableObject(object)) {  // Handles the difference between spawned & player-placed portable skilling objects.
                    if (PortableStation.getActivePortables().containsKey(new PortableStation.PortableWorldTile(new WorldTile(object.getX(), object.getY(), object.getPlane())))) {
                        return true;
                    }
                }
                if (player.isGroupIronman() && dzObject.getAmountRequiredToInteract() > 0) {
                    return false;
                }
                return player.getMoneySpent() >= dzObject.getAmountRequiredToInteract();
            }
        }
        return true;
    }

    public static boolean playerCanInteractWithNpc(Player player, NPC npc) {
        if (!playerIsAtHome(player)) {
            return true;
        }
        for (HomeAreaNpcData dzNpc : HomeAreaNpcData.values()) {
            if (dzNpc.getNpcName().equalsIgnoreCase(npc.getName())) {
                if (player.isGroupIronman() && dzNpc.getAmountRequiredToInteract() > 0) {
                    return false;
                }
                return player.getMoneySpent() >= dzNpc.getAmountRequiredToInteract();
            }
        }
        return true;
    }

    private static String getRequiredDonatorTierForNpcOrObject(String name, Type type) {
        switch (type) {
            case NPC:
                for (HomeAreaNpcData npc : HomeAreaNpcData.values()) {
                    if (npc.getNpcName().equalsIgnoreCase(name)) {
                        if (npc.getAmountRequiredToInteract() >= 1_000) {
                            return "Master";
                        } else if (npc.getAmountRequiredToInteract() >= 500) {
                            return "Diamond";
                        } else if (npc.getAmountRequiredToInteract() >= 250) {
                            return "Platinum";
                        } else if (npc.getAmountRequiredToInteract() >= 100) {
                            return "Gold";
                        } else if (npc.getAmountRequiredToInteract() >= 50) {
                            return "Silver";
                        } else {
                            return "Bronze";
                        }
                    }
                }
                break;
            case OBJECT:
                for (HomeAreaObjectData object : HomeAreaObjectData.values()) {
                    if (object.getObjectName().equalsIgnoreCase(name)) {
                        if (object.getAmountRequiredToInteract() >= 1_000) {
                            return "Master";
                        } else if (object.getAmountRequiredToInteract() >= 500) {
                            return "Diamond";
                        } else if (object.getAmountRequiredToInteract() >= 250) {
                            return "Platinum";
                        } else if (object.getAmountRequiredToInteract() >= 100) {
                            return "Gold";
                        } else if (object.getAmountRequiredToInteract() >= 50) {
                            return "Silver";
                        } else {
                            return "Bronze";
                        }
                    }
                }
                break;
        }
        return null;
    }

    public static void sendCanNotUseNpcOrObjectMessageToPlayer(Player player, String name, Type type) {
        String rank = getRequiredDonatorTierForNpcOrObject(name, type);
        if (player.isGroupIronman()) {
            player.sendMessage("Group Ironman accounts are restricted from interacting with this " + type.name().toLowerCase() + ".", false);
            return;
        }
        if (rank != null) {
            player.sendMessage("You need to be a " + rank + " donator or greater to interact with " + (type == Type.OBJECT ? "the " : "") + name + ".", false);
        }
    }

    public static Double getExperienceModifierForPlayer(Player player) {
        int moneySpent = player.getMoneySpent();
        return moneySpent <= 249 ? 1.10 : moneySpent >20 ? 1.10 : 1.0;
    }

    public static String getNameForObject(int objectId) {
        ObjectDefinitions object = ObjectDefinitions.getObjectDefinitions(objectId);
        return object != null ? object.getName() : "";
    }

    public static String getNameForNpc(int npcId) {
        NPCDefinitions npc = NPCDefinitions.getNPCDefinitions(npcId);
        return npc != null ? npc.getName() : "";
    }

    public static boolean processNPCExamine(Player player, NPC npc) {
        for (HomeAreaNpcData dzNpc : HomeAreaNpcData.values()) {
            if (dzNpc.getNpcName().equalsIgnoreCase(npc.getName())) {
                if (!dzNpc.getDescription().equals("")) {
                    player.getPackets().sendNPCMessage(0, 15263739, npc, dzNpc.getDescription());
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean processObjectExamine(Player player, WorldObject object) {
        for (HomeAreaObjectData objectData : HomeAreaObjectData.values()) {
            if (objectData.getObjectName().equalsIgnoreCase(object.getDefinitions().getName())) {
                if (objectData.getDescription() == null) {
                    return false;
                }
                if (!objectData.getDescription().equals("")) {
                    player.getPackets().sendObjectMessage(0, 15263739, object, objectData.getDescription());
                    player.getPackets().sendResetMinimapFlag();
                    return false;
                }
            }
        }
        return true;
    }

    public enum Type {
        NPC, OBJECT
    }

}
