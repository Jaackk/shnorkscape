package com.rs.game.player.controllers.castlewars;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.activites.CastleWars;
import com.rs.game.item.Item;
import com.rs.game.npc.NPC;
import com.rs.game.npc.others.CastleWarBarricade;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.WaterFilling;
import com.rs.game.player.Equipment;
import com.rs.game.player.Inventory;
import com.rs.game.player.Player;
import com.rs.game.player.controllers.Controller;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.network.packet.impl.ObjectHandler;
import com.rs.utils.Utils;

import java.util.List;

public class CastleWarsPlaying extends Controller {

    private static final int ZAMORAK_EXIT_PORTAL = 83621;
    private static final int ZAMORAK_BASE_DOORS = 83570;
    private static final int ZAMORAK_LARGE_DOOR_LEFT = 83558, ZAMORAK_LARGE_DOOR_RIGHT = 83560, ZAMORAK_SIDE_DOOR = 83565;
    private static final int SARADOMIN_BASE_DOORS = 83496;
    private static final int SARADOMIN_LARGE_DOOR_LEFT = 83488, SARADOMIN_LARGE_DOOR_RIGHT = 83486, SARADOMIN_SIDE_DOOR = 83498;
    private static final int ZAMORAK_LADDER_TO_TUNNELS = 83552, ZAMORAK_BASE_TRAPDOOR = 83827;
    private static final int ZAMORAK_STAIRS = 83571, ZAMORAK_STEPS = 83572, ZAMORAK_STEPS_UP = 83573;
    private static final int ZAMORAK_STEPS_BACK_UP = 83574, ZAMORAK_TOP_STEPS = 83576;
    private static final int SARADOMIN_STEPS = 83500, SARADOMIN_STEPS_UP = 83502, SARADOMIN_SIDE_STEPS = 83503, SARADOMIN_TOP_STEPS = 83504;
    private static final int STEPPING_STONE = 83731, OLD_STEPPING_STONE = 4411;
    private static final int UNDERGROUND_LADDER_TO_CENTER = 83645, OLD_UNDERGROUND_LADDER_TO_CENTER = 36644, CENTER_TRAPDOOR_TO_TUNNELS = 36691;
    private static final int ZAMORAK_ALTAR = 83824, ZAMORAK_TAP = 83752;
    private static final int SARADOMIN_FLAG = 83484, SARADOMIN_FLAG_ALT = 83483, ZAMORAK_FLAG = 83556, ZAMORAK_FLAG_ALT = 83555;
    private static final int SARADOMIN_EMPTY_FLAG_STAND = 83553, ZAMORAK_EMPTY_FLAG_STAND = 83554;
    private static final int CASTLE_WARS_TUNNEL_ROCKS = 112887, OLD_CASTLE_WARS_TUNNEL_ROCKS = 4437, OLD_CASTLE_WARS_SMALL_TUNNEL_ROCKS = 4438;
    private static final int BANDAGE_TABLE = 83629, BARRICADE_TABLE = 83625, TOOLKIT_TABLE = 83623, ROPE_TABLE = 83626;
    private static final int EXPLOSIVE_POTION_TABLE = 83627, PICKAXE_TABLE = 83628, ROCK_TABLE = 83624, FLARE_TABLE = 83630;
    private static final int SARADOMIN_BANDAGE_TABLE = 83518, SARADOMIN_BARRICADE_TABLE = 83514, SARADOMIN_TOOLKIT_TABLE = 83512, SARADOMIN_ROPE_TABLE = 83515;
    private static final int SARADOMIN_EXPLOSIVE_POTION_TABLE = 83516, SARADOMIN_PICKAXE_TABLE = 83517, SARADOMIN_ROCK_TABLE = 83513, SARADOMIN_FLARE_TABLE = 83519;
    private static final int BANDAGES = 4049, BARRICADE = 4053, TOOLKIT = 4051, CLIMBING_ROPE = 4047;
    private static final int EXPLOSIVE_POTION = 4045, CASTLE_WARS_ROCK = 4043, BRONZE_PICKAXE = 1265, FLARE = 18704;
    private static final int[] PICKAXES = {
            44834, 45642, 45154, 29522, 32646, 46372, 20786, 15259, 13661, 29662, 29654, 20785, 1275,
            20783, 1271, 20784, 1273, 20782, 1269, 20781, 1267, 20780, 1265
    };
    private static final int[] PICKAXE_ANIMATIONS = {
            32618, 32611, 32606, 32606, 25062, 32603, 12190, 12190, 10222, 32566, 32566, 32566, 32566,
            32562, 32562, 32558, 32558, 32552, 32552, 32548, 32548, 32540, 32540
    };

    private int team;

    @Override
    public boolean processPlayerOption1(Entity target) {
        if (target instanceof Player) {
            if (canHit(target))
                return true;
            player.getPackets().sendGameMessage("You can't attack your team.");
            return false;
        }
        return true;
    }

    @Override
    public boolean canDropItem(Item item) {
        if (item.getDefinitions().getName().toLowerCase().contains("flag")) {
            player.getPackets().sendGameMessage("You cannot just drop the flag!");
            return false;
        }
        return true;
    }

    @Override
    public boolean canEquip(int slotId, int itemId) {
        if (slotId == Equipment.SLOT_CAPE || slotId == Equipment.SLOT_HAT) {
            player.getPackets().sendGameMessage("You can't remove your team's colours.");
            return false;
        }
        if (slotId == Equipment.SLOT_WEAPON || slotId == Equipment.SLOT_SHIELD) {
            int weaponId = player.getEquipment().getWeaponId();
            if (weaponId == 4037 || weaponId == 4039) {
                player.getPackets().sendGameMessage("You can't remove enemy's flag.");
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean canHit(Entity target) {
        if (target instanceof NPC)
            return true;
        Player p2 = (Player) target;
        return p2.getEquipment().getCapeId() != player.getEquipment().getCapeId();
    }

    @Override
    public boolean canMove(int dir) {
        WorldTile toTile = new WorldTile(player.getX() + Utils.DIRECTION_DELTA_X[dir], player.getY() + Utils.DIRECTION_DELTA_Y[dir], player.getPlane());
        return !CastleWars.isBarricadeAt(toTile);
    }

    private void doBandageEffect(Item item) {
        int gloves = player.getEquipment().getGlovesId();
        player.heal((int) (player.getMaxHitpoints() * (gloves >= 11079 && gloves <= 11084 ? 0.15 : 0.10)));
        int restoredEnergy = (int) (player.getRunEnergy() * 1.3);
        player.setRunEnergy(restoredEnergy > 100 ? 100 : restoredEnergy);
        player.getInventory().deleteItem(item);
    }

    @Override
    public void forceClose() {
        leave();
    }

    public boolean isInSafe() {
        return player.getX() >= 2368 && player.getX() <= 2376 && player.getY() >= 3127 && player.getY() <= 3135 || player.getX() >= 2423 && player.getX() <= 2431 && player.getY() >= 3072 && player.getY() <= 3080;
    }

    private boolean isBlockedBarricadeTile(WorldTile tile) {
        return tile.getX() == 2422 && tile.getY() == 3076
                || tile.getX() == 2426 && tile.getY() == 3080
                || tile.getX() == 2423 && tile.getY() == 3076
                || tile.getX() == 2426 && tile.getY() == 3081
                || tile.getX() == 2373 && tile.getY() == 3127
                || tile.getX() == 2373 && tile.getY() == 3126
                || tile.getX() == 2376 && tile.getY() == 3131
                || tile.getX() == 2377 && tile.getY() == 3131;
    }

    public void leave() {
        player.getInterfaceManager().removeMinigameHudInterface();
        CastleWars.removePlayingPlayer(player, team);
    }

    @Override
    public boolean logout() {
        player.setLocation(new WorldTile(CastleWars.LOBBY, 2));
        return true;
    }

    // You can't leave just like that!

    @Override
    public void magicTeleported(int type) {
        removeControler();
        leave();
    }

    public void passBarrier(WorldObject object) {
        if (object.getRotation() == 0 || object.getRotation() == 2) {
            if (player.getY() != object.getY())
                return;
            player.lock(2);
            player.addWalkSteps(object.getX() == player.getX() ? object.getX() + (object.getRotation() == 0 ? -1 : +1) : object.getX(), object.getY(), -1, false);
        } else if (object.getRotation() == 1 || object.getRotation() == 3) {
            if (player.getX() != object.getX())
                return;
            player.lock(2);
            player.addWalkSteps(object.getX(), object.getY() == player.getY() ? object.getY() + (object.getRotation() == 3 ? -1 : +1) : object.getY(), -1, false);
        }
    }

    private void openCastleWarsDoor(WorldObject object) {
        if (!ObjectHandler.handleDoor(player, object))
            passBarrier(object);
    }

    private boolean passTeamDoor(WorldObject object, int ownerTeam) {
        if (team != ownerTeam) {
            player.getPackets().sendGameMessage("This door is locked to the opposing team.");
            return false;
        }
        openCastleWarsDoor(object);
        return false;
    }

    private boolean handleCastleWarsSupplyTable(int objectId, int amount) {
        switch (objectId) {
            case BANDAGE_TABLE:
            case SARADOMIN_BANDAGE_TABLE:
                if (isInSafe())
                    player.getInventory().addItem(new Item(BANDAGES, amount));
                return true;
            case BARRICADE_TABLE:
            case SARADOMIN_BARRICADE_TABLE:
                player.getInventory().addItem(new Item(BARRICADE, amount));
                return true;
            case TOOLKIT_TABLE:
            case SARADOMIN_TOOLKIT_TABLE:
                player.getInventory().addItem(new Item(TOOLKIT, amount));
                return true;
            case ROPE_TABLE:
            case SARADOMIN_ROPE_TABLE:
                player.getInventory().addItem(new Item(CLIMBING_ROPE, amount));
                return true;
            case EXPLOSIVE_POTION_TABLE:
            case SARADOMIN_EXPLOSIVE_POTION_TABLE:
                player.getInventory().addItem(new Item(EXPLOSIVE_POTION, amount));
                return true;
            case PICKAXE_TABLE:
            case SARADOMIN_PICKAXE_TABLE:
                player.getInventory().addItem(new Item(BRONZE_PICKAXE, amount));
                return true;
            case ROCK_TABLE:
            case SARADOMIN_ROCK_TABLE:
                player.getInventory().addItem(new Item(CASTLE_WARS_ROCK, amount));
                return true;
            case FLARE_TABLE:
            case SARADOMIN_FLARE_TABLE:
                player.getInventory().addItem(new Item(FLARE, amount));
                return true;
        }
        return false;
    }

    private boolean handleZamorakStairs(WorldObject object) {
        int id = object.getId();
        if (id == ZAMORAK_TOP_STEPS && object.getX() == 2374 && object.getY() == 3131 && object.getPlane() == 2) {
            player.useStairs(-1, new WorldTile(2373, 3133, 3), 0, 1);
            return true;
        }
        if (id == ZAMORAK_STAIRS && object.getX() == 2369 && object.getY() == 3126 && object.getPlane() == 2) {
            player.useStairs(-1, new WorldTile(2372, 3126, 1), 0, 1);
            return true;
        }
        if (id == ZAMORAK_STEPS_UP && object.getX() == 2371 && object.getY() == 3126 && object.getPlane() == 1) {
            player.useStairs(-1, new WorldTile(2369, 3127, 2), 0, 1);
            return true;
        }
        if (id == ZAMORAK_STAIRS && object.getX() == 2380 && object.getY() == 3127 && object.getPlane() == 1) {
            player.useStairs(-1, new WorldTile(2380, 3130, 0), 0, 1);
            return true;
        }
        if (id == ZAMORAK_STEPS && object.getX() == 2382 && object.getY() == 3130 && object.getPlane() == 0) {
            player.useStairs(-1, new WorldTile(2383, 3132, 0), 0, 1);
            return true;
        }
        if (id == ZAMORAK_STAIRS && object.getX() == 2374 && object.getY() == 3133 && object.getPlane() == 3) {
            player.useStairs(-1, new WorldTile(2374, 3130, 2), 0, 1);
            return true;
        }
        if (id == ZAMORAK_STEPS_BACK_UP && object.getX() == 2380 && object.getY() == 3129 && object.getPlane() == 0) {
            player.useStairs(-1, new WorldTile(2379, 3127, 1), 0, 1);
            return true;
        }
        if (id == ZAMORAK_STAIRS && object.getX() == 2382 && object.getY() == 3132 && object.getPlane() == 0) {
            player.useStairs(-1, new WorldTile(2382, 3129, 0), 0, 1);
            return true;
        }
        if (id == ZAMORAK_BASE_TRAPDOOR && object.getX() == 2370 && object.getY() == 3133 && object.getPlane() == 2) {
            player.useStairs(827, new WorldTile(2370, 3133, 1), 1, 2);
            return true;
        }
        return false;
    }

    private boolean handleSaradominStairs(WorldObject object) {
        int id = object.getId();
        if (id == SARADOMIN_STEPS && object.getX() == 2417 && object.getY() == 3077 && object.getPlane() == 0) {
            player.useStairs(-1, new WorldTile(2416, 3075, 0), 0, 1);
            return true;
        }
        if (id == ZAMORAK_STAIRS && object.getX() == 2417 && object.getY() == 3075 && object.getPlane() == 0) {
            player.useStairs(-1, new WorldTile(2417, 3078, 0), 0, 1);
            return true;
        }
        if (id == SARADOMIN_SIDE_STEPS && object.getX() == 2419 && object.getY() == 3078 && object.getPlane() == 0) {
            player.useStairs(-1, new WorldTile(2420, 3080, 1), 0, 1);
            return true;
        }
        if (id == ZAMORAK_STAIRS && object.getX() == 2419 && object.getY() == 3080 && object.getPlane() == 1) {
            player.useStairs(-1, new WorldTile(2419, 3077, 0), 0, 1);
            return true;
        }
        if (id == SARADOMIN_STEPS_UP && object.getX() == 2428 && object.getY() == 3081 && object.getPlane() == 1) {
            player.useStairs(-1, new WorldTile(2430, 3080, 2), 0, 1);
            return true;
        }
        if (id == ZAMORAK_STAIRS && object.getX() == 2430 && object.getY() == 3081 && object.getPlane() == 2) {
            player.useStairs(-1, new WorldTile(2427, 3081, 1), 0, 1);
            return true;
        }
        if (id == SARADOMIN_TOP_STEPS && object.getX() == 2425 && object.getY() == 3076 && object.getPlane() == 2) {
            player.useStairs(-1, new WorldTile(2426, 3074, 3), 0, 1);
            return true;
        }
        if (id == ZAMORAK_STAIRS && object.getX() == 2425 && object.getY() == 3074 && object.getPlane() == 3) {
            player.useStairs(-1, new WorldTile(2425, 3077, 2), 0, 1);
            return true;
        }
        return false;
    }

    private boolean handleSteppingStone(WorldObject object) {
        if (object.getId() != STEPPING_STONE && object.getId() != OLD_STEPPING_STONE)
            return false;
        if (object.getX() == player.getX() && object.getY() == player.getY())
            return true;
        player.lock(2);
        player.setNextAnimation(new Animation(741));
        player.addWalkSteps(object.getX(), object.getY(), -1, false);
        return true;
    }

    private void clearTunnelDelay() {
        player.setFreezeDelay(0);
        player.setFrozeBlocked(0);
    }

    private boolean handleTunnelAccess(WorldObject object) {
        int id = object.getId();
        if (id == UNDERGROUND_LADDER_TO_CENTER || id == OLD_UNDERGROUND_LADDER_TO_CENTER) {
            if (object.getX() == 2400 && object.getY() == 9508)
                player.useStairs(828, new WorldTile(2400, 3106, 0), 1, 2);
            else if (object.getX() == 2399 && object.getY() == 9499)
                player.useStairs(828, new WorldTile(2399, 3100, 0), 1, 2);
            else
                return false;
            clearTunnelDelay();
            return true;
        }
        if (id == CENTER_TRAPDOOR_TO_TUNNELS) {
            if (object.getX() == 2399 && object.getY() == 3099)
                player.useStairs(827, new WorldTile(2399, 9500, 0), 1, 2);
            else if (object.getX() == 2400 && object.getY() == 3108)
                player.useStairs(827, new WorldTile(2400, 9507, 0), 1, 2);
            else
                return false;
            clearTunnelDelay();
            return true;
        }
        return false;
    }

    private boolean isTunnelRocks(int id) {
        return id == CASTLE_WARS_TUNNEL_ROCKS || id == OLD_CASTLE_WARS_TUNNEL_ROCKS || id == OLD_CASTLE_WARS_SMALL_TUNNEL_ROCKS;
    }

    private int getPickaxeAnimation() {
        int weaponId = player.getEquipment().getWeaponId();
        for (int i = 0; i < PICKAXES.length; i++) {
            if (weaponId == PICKAXES[i])
                return PICKAXE_ANIMATIONS[i];
        }
        for (int i = 0; i < PICKAXES.length; i++) {
            if (player.getInventory().containsItem(PICKAXES[i], 1) || player.getToolBelt().contains(PICKAXES[i]))
                return PICKAXE_ANIMATIONS[i];
        }
        return -1;
    }

    private boolean handleTunnelRocks(final WorldObject object) {
        if (!isTunnelRocks(object.getId()))
            return false;
        int animation = getPickaxeAnimation();
        if (animation == -1) {
            player.getPackets().sendGameMessage("You need a pickaxe to clear these rocks.");
            return true;
        }
        player.lock(3);
        player.faceObject(object);
        player.setNextAnimation(new Animation(animation));
        WorldTasksManager.schedule(new WorldTask() {
            @Override
            public void run() {
                player.setNextAnimation(new Animation(-1));
                World.removeObjectTemporary(object, 30000, true);
                player.getPackets().sendGameMessage("You clear a path through the rocks.");
                stop();
            }
        }, 2);
        return true;
    }

    private void blastTunnelRocks(final WorldObject object) {
        player.lock(2);
        player.faceObject(object);
        player.getPackets().sendGameMessage("You pour the explosive potion onto the rocks.");
        WorldTasksManager.schedule(new WorldTask() {
            @Override
            public void run() {
                World.removeObjectTemporary(object, 30000, true);
                player.getPackets().sendGameMessage("The rocks collapse, leaving a path through.");
                stop();
            }
        }, 1);
    }

    private boolean restorePrayer() {
        int maxPrayer = player.getSkills().getLevelForXp(Skills.PRAYER) * 10;
        if (player.getPrayer().getPrayerpoints() >= maxPrayer) {
            player.getPackets().sendGameMessage("You already have full prayer points.");
            return false;
        }
        player.lock(1);
        player.setNextAnimation(new Animation(645));
        player.getPrayer().restorePrayer(maxPrayer);
        player.getPackets().sendGameMessage("You've recharged your prayer points.");
        return false;
    }

    private boolean isSaradominFlag(int id) {
        return id == 4902 || id == SARADOMIN_FLAG || id == SARADOMIN_FLAG_ALT;
    }

    private boolean isZamorakFlag(int id) {
        return id == 4903 || id == ZAMORAK_FLAG || id == ZAMORAK_FLAG_ALT;
    }

    private boolean isSaradominEmptyFlagStand(int id) {
        return id == 4377 || id == SARADOMIN_EMPTY_FLAG_STAND;
    }

    private boolean isZamorakEmptyFlagStand(int id) {
        return id == 4378 || id == ZAMORAK_EMPTY_FLAG_STAND;
    }

    /*
     * return process normaly
     */
    @Override
    public boolean processButtonClick(int interfaceId, int componentId, int slotId, int slotId2, int packetId) {
        if (interfaceId == 387) {
            if (componentId == 37)
                return false;
            if (componentId == 9 || componentId == 6) {
                player.getPackets().sendGameMessage("You can't remove your team's colours.");
                return false;
            }
            if (componentId == 15) {
                int weaponId = player.getEquipment().getWeaponId();
                if (weaponId == 4037 || weaponId == 4039) {
                    player.getPackets().sendGameMessage("You can't remove enemy's flag.");
                    return false;
                }
            }
        } else if (interfaceId == Inventory.INVENTORY_INTERFACE) {
            Item item = player.getInventory().getItem(slotId);
            if (item != null) {
                if (item.getId() == 4053) {
                    WorldTile tile = new WorldTile(player);
                    if (isBlockedBarricadeTile(tile) || CastleWars.isBarricadeAt(tile)) {
                        player.getPackets().sendGameMessage("You cannot place a barricade here!");
                        return false;
                    }
                    CastleWars.addBarricade(team, player);
                    return false;
                } else if (item.getId() == 4049 || item.getId() == 4050 || item.getId() == 12853 || item.getId() == 14640 || item.getId() == 14648) {
                    doBandageEffect(item);
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public boolean processItemOnNPC(NPC npc, Item item) {
        if (npc.getId() == 1532 && npc instanceof CastleWarBarricade) {
            CastleWarBarricade barricade = (CastleWarBarricade) npc;
            if (item.getId() == 590) {
                barricade.litFire();
                return false;
            } else if (item.getId() == 4045) {
                player.getInventory().deleteItem(item);
                barricade.explode();
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean handleItemOnObject(WorldObject object, Item item) {
        if (item.getId() == EXPLOSIVE_POTION && isTunnelRocks(object.getId())) {
            player.getInventory().deleteItem(item);
            blastTunnelRocks(object);
            return false;
        }
        return true;
    }

    @Override
    public boolean processItemTeleport(WorldTile toTile) {
        player.getDialogueManager().startDialogue("SimpleMessage", "You can't leave just like that!");
        return false;
    }

    @Override
    public boolean processMagicTeleport(WorldTile toTile) {
        player.getDialogueManager().startDialogue("SimpleMessage", "You can't leave just like that!");
        return false;
    }

    @Override
    public boolean processNPCClick2(NPC n) {
        if (n.getId() == 1532 && n instanceof CastleWarBarricade) {
            if (!player.getInventory().containsItem(590, 1)) {
                player.getPackets().sendGameMessage("You do not have the required items to light this.");
                return false;
            }
            CastleWarBarricade barricade = (CastleWarBarricade) n;
            barricade.litFire();
            return false;
        }
        return true;
    }

    @Override
    public boolean processObjectClick1(WorldObject object) {
        int id = object.getId();
        if (id == 4406 || id == 4407 || id == ZAMORAK_EXIT_PORTAL) {
            removeControler();
            leave();
            return false;
        } else if (id == ZAMORAK_BASE_DOORS) {
            return passTeamDoor(object, CastleWars.ZAMORAK);
        } else if (id == SARADOMIN_BASE_DOORS) {
            return passTeamDoor(object, CastleWars.SARADOMIN);
        } else if (id == ZAMORAK_LARGE_DOOR_LEFT || id == ZAMORAK_LARGE_DOOR_RIGHT || id == ZAMORAK_SIDE_DOOR
                || id == SARADOMIN_LARGE_DOOR_LEFT || id == SARADOMIN_LARGE_DOOR_RIGHT || id == SARADOMIN_SIDE_DOOR) {
            openCastleWarsDoor(object);
            return false;
        } else if (id == ZAMORAK_ALTAR) {
            return restorePrayer();
        } else if (id == ZAMORAK_TAP) {
            if (!WaterFilling.isFilling(player, 1925, false))
                player.getPackets().sendGameMessage("You need an empty bucket to draw water from the tap.");
            return false;
        } else if (id == ZAMORAK_LADDER_TO_TUNNELS) {
            player.useStairs(827, new WorldTile(2369, 9524, 0), 1, 2);
            return false;
        } else if (handleZamorakStairs(object)) {
            return false;
        } else if (handleSaradominStairs(object)) {
            return false;
        } else if (handleTunnelAccess(object)) {
            return false;
        } else if (handleSteppingStone(object)) {
            return false;
        } else if (handleTunnelRocks(object)) {
            return false;
        } else if (handleCastleWarsSupplyTable(id, 1)) {
            return false;
        } else if ((id == 4469 && team == CastleWars.SARADOMIN) || (id == 4470 && team == CastleWars.ZAMORAK)) {
            passBarrier(object);
            return false;
        } else if (isSaradominEmptyFlagStand(id) || isZamorakEmptyFlagStand(id)) { // no flag anymore
            if (isSaradominEmptyFlagStand(id) && team == CastleWars.SARADOMIN) {
                if (player.getEquipment().getWeaponId() == 4039) {
                    CastleWars.addScore(player, team, CastleWars.ZAMORAK);
                    return false;
                }
            } else if (isZamorakEmptyFlagStand(id) && team == CastleWars.ZAMORAK) {
                if (player.getEquipment().getWeaponId() == 4037) {
                    CastleWars.addScore(player, team, CastleWars.SARADOMIN);
                    return false;
                }
            }
            player.getPackets().sendGameMessage("You need to bring a flag back here!");
            return false;
        } else if (isSaradominFlag(id) || isZamorakFlag(id)) { // take flag
            if (isSaradominFlag(id) && team == CastleWars.SARADOMIN) {
                if (player.getEquipment().getWeaponId() == 4039) {
                    CastleWars.addScore(player, team, CastleWars.ZAMORAK);
                    return false;
                }
                player.getPackets().sendGameMessage("Saradomin won't let you take his flag!");
            } else if (isZamorakFlag(id) && team == CastleWars.ZAMORAK) {
                if (player.getEquipment().getWeaponId() == 4037) {
                    CastleWars.addScore(player, team, CastleWars.SARADOMIN);
                    return false;
                }
                player.getPackets().sendGameMessage("Zamorak won't let you take his flag!");
            } else {
                // take flag
                CastleWars.takeFlag(player, team, isSaradominFlag(id) ? CastleWars.SARADOMIN : CastleWars.ZAMORAK, object, false);
            }
            return false;
        } else if (id == 4900 || id == 4901) { // take dropped flag
            CastleWars.takeFlag(player, team, id == 4900 ? CastleWars.SARADOMIN : CastleWars.ZAMORAK, object, true);
            return false;
        } else if (id == 36579 || id == 36586) {
            if (isInSafe()) {
                player.getInventory().addItem(new Item(4049));
            }
            return false;
        } else if (id == 36575 || id == 36582) {
            player.getInventory().addItem(new Item(4053));
            return false;
        } else if (id == 36577 || id == 36584) {
            player.getInventory().addItem(new Item(4045));
            return false;
            // under earth from basess
        } else if (id == 4411) {// stepping stone
            if (object.getX() == player.getX() && object.getY() == player.getY())
                return false;
            player.lock(2);
            player.setNextAnimation(new Animation(741));
            player.addWalkSteps(object.getX(), object.getY(), -1, false);
        } else if (id == 36693) {
            player.useStairs(827, new WorldTile(2430, 9483, 0), 1, 2);
            return false;
        } else if (id == 36694) {
            player.useStairs(827, new WorldTile(2369, 9524, 0), 1, 2);
            return false;
        } else if (id == 36645) {
            player.useStairs(828, new WorldTile(2430, 3081, 0), 1, 2);
            return false;
        } else if (id == 36646) {
            player.useStairs(828, new WorldTile(2369, 3126, 0), 1, 2);
            return false;
        } else if (id == 4415) {
            if (object.getX() == 2417 && object.getY() == 3075 && object.getPlane() == 1)
                player.useStairs(-1, new WorldTile(2417, 3078, 0), 0, 1);
            else if (player.getX() == 2416 && player.getY() == 3075)
                player.useStairs(-1, new WorldTile(2417, 3078, 0), 0, 1);
            else if (player.getX() == 2417 && player.getY() == 3075)
                player.useStairs(-1, new WorldTile(2417, 3078, 0), 0, 1);
            else if (player.getX() == 2383 && player.getY() == 3132)
                player.useStairs(-1, new WorldTile(2382, 3129, 0), 0, 1);
            else if (player.getX() == 2382 && player.getY() == 3132)
                player.useStairs(-1, new WorldTile(2382, 3129, 0), 0, 1);
            else if (object.getX() == 2419 && object.getY() == 3080 && object.getPlane() == 1)
                player.useStairs(-1, new WorldTile(2419, 3077, 0), 0, 1);
            else if (object.getX() == 2430 && object.getY() == 3081 && object.getPlane() == 2)
                player.useStairs(-1, new WorldTile(2427, 3081, 1), 0, 1);
            else if (object.getX() == 2425 && object.getY() == 3074 && object.getPlane() == 3)
                player.useStairs(-1, new WorldTile(2425, 3077, 2), 0, 1);
            else if (object.getX() == 2380 && object.getY() == 3127 && object.getPlane() == 1)
                player.useStairs(-1, new WorldTile(2380, 3130, 0), 0, 1);
            else if (object.getX() == 2382 && object.getY() == 3132 && object.getPlane() == 1)
                player.useStairs(-1, new WorldTile(2382, 3129, 0), 0, 1);
            else if (object.getX() == 2369 && object.getY() == 3126 && object.getPlane() == 2)
                player.useStairs(-1, new WorldTile(2372, 3126, 1), 0, 1);
            else if (object.getX() == 2374 && object.getY() == 3133 && object.getPlane() == 3)
                player.useStairs(-1, new WorldTile(2374, 3130, 2), 0, 1);
            return false;
        } else if (id == 36481) {
            player.useStairs(-1, new WorldTile(2417, 3075, 0), 0, 1);
            return false;
        } else if (id == 36495 && object.getPlane() == 0) {
            player.useStairs(-1, new WorldTile(2420, 3080, 1), 0, 1);
            return false;
        } else if (id == 36480 && object.getPlane() == 1) {
            player.useStairs(-1, new WorldTile(2430, 3080, 2), 0, 1);
            return false;
        } else if (id == 36484 && object.getPlane() == 2) {
            player.useStairs(-1, new WorldTile(2426, 3074, 3), 0, 1);
            return false;
        } else if (id == 36532 && object.getPlane() == 0) {
            player.useStairs(-1, new WorldTile(2379, 3127, 1), 0, 1);
            return false;
        } else if (id == 36540) {
            player.useStairs(-1, new WorldTile(2383, 3132, 0), 0, 1);
            return false;
        } else if (id == 36521 && object.getPlane() == 1) {
            player.useStairs(-1, new WorldTile(2369, 3127, 2), 0, 1);
            return false;
        } else if (id == 36523 && object.getPlane() == 2) {
            player.useStairs(-1, new WorldTile(2373, 3133, 3), 0, 1);
            return false;
        } else if (id == 36644) {
            if (object.getY() == 9508)
                player.useStairs(828, new WorldTile(2400, 3106, 0), 1, 2);
            else if (object.getY() == 9499)
                player.useStairs(828, new WorldTile(2399, 3100, 0), 1, 2);
            player.setFreezeDelay(0);
            player.setFrozeBlocked(0);
            return false;
        } else if (id == 36691) {
            if (object.getY() == 3099)
                player.useStairs(827, new WorldTile(2399, 9500, 0), 1, 2);
            else if (object.getY() == 3108)
                player.useStairs(827, new WorldTile(2400, 9507, 0), 1, 2);
            player.setFreezeDelay(0);
            player.setFrozeBlocked(0);
            return false;
        } /*
           * else if (id == 4438) player.getActionManager().setSkill(new Mining(object,
           * RockDefinitions.SMALLER_ROCKS)); else if (id == 4437)
           * player.getActionManager().setSkill(new Mining(object, RockDefinitions.ROCKS
           * ));
           */ else if (id == 4448) {
            for (List<Player> players : CastleWars.getPlaying()) {
                for (Player player : players) {
                    if (player.withinDistance(object, 1))
                        player.applyHit(new Hit(player, player.getHitpoints(), HitLook.REGULAR_DAMAGE));
                }
            }
            World.spawnObject(new WorldObject(4437, object.getType(), object.getRotation(), object.getX(), object.getY(), object.getPlane()));
        }
        return true;
    }

    @Override
    public boolean processObjectClick2(WorldObject object) {
        int id = object.getId();
        if (handleCastleWarsSupplyTable(id, 5)) {
            return false;
        }
        if (id == 36579 || id == 36586) {
            if (isInSafe()) {
                player.getInventory().addItem(new Item(4049, 5));
            }
            return false;
        } else if (id == 36575 || id == 36582) {
            player.getInventory().addItem(new Item(4053, 5));
            return false;
        } else if (id == 36577 || id == 36584) {
            player.getInventory().addItem(new Item(4045, 5));
            return false;
        }
        return true;
    }

    @Override
    public boolean processObjectTeleport(WorldTile toTile) {
        player.getDialogueManager().startDialogue("SimpleMessage", "You can't leave just like that!");
        return false;
    }

    @Override
    public boolean sendDeath() {
        WorldTasksManager.schedule(new WorldTask() {
            int loop;

            @Override
            public void run() {
                if (loop == 0) {
                    player.setNextAnimation(new Animation(836));
                } else if (loop == 1) {
                    player.getPackets().sendGameMessage("Oh dear, you have died.");
                } else if (loop == 3) {
                    int weaponId = player.getEquipment().getWeaponId();
                    if (weaponId == 4037 || weaponId == 4039) {
                        CastleWars.setWeapon(player, null);
                        CastleWars.dropFlag(player, weaponId == 4037 ? CastleWars.SARADOMIN : CastleWars.ZAMORAK);
                    }
                    player.reset();
                    player.setNextWorldTile(new WorldTile(team == CastleWars.ZAMORAK ? CastleWars.ZAMO_BASE : CastleWars.SARA_BASE, 1));
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
    public void sendInterfaces() {
        player.getInterfaceManager().sendMinigameHudInterface(58);
    }

    @Override
    public void start() {
        team = (int) getArguments()[0];
        sendInterfaces();
    }
}
