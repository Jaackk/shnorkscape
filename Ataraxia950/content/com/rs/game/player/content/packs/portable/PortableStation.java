package com.rs.game.player.content.packs.portable;

import com.rs.game.Animation;
import com.rs.game.Region;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.activites.gim.GIM;
import com.rs.game.activites.gim.GIMGroup;
import com.rs.game.item.Item;
import com.rs.game.player.Inventory;
import com.rs.game.player.Player;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.DialogueOptionEvent;
import com.rs.utils.InputIntegerEvent;
import com.rs.utils.Utils;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author _jordan <jordan.abraham1997@gmail.com>
 * <p>
 * Created on Oct 9, 2018.
 */
public class PortableStation {

    public static final class PortableWorldTile {

        private final int x;
        private final int y;
        private final int z;

        public PortableWorldTile(WorldTile tile) {
            x = tile.getX();
            y = tile.getY();
            z = tile.getPlane();
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y, z);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj instanceof PortableWorldTile) {
                PortableWorldTile other = (PortableWorldTile) obj;
                return x == other.x && y == other.y && z == other.z;
            }
            return false;
        }

        public WorldTile toWorldTile() {
            return new WorldTile(x, y, z);
        }
    }

    /**
     * Active portables, tile -> username.
     */
    @Getter
    public static final Map<PortableWorldTile, String> activePortables = new HashMap<>();

    public static boolean isPortableItem(int itemId) {
        return (PortableType.isPortableItem(itemId));
    }

    public static boolean isPortableObject(WorldObject object) {
        return (PortableType.isPortableObject(object.getId()));
    }

    public static void deploy(Player player, Item item) {
        if (player == null || item == null)
            return;

        if (!checkAll(player, item))
            return;

        WorldTile tile = new WorldTile(player.getX() + 1, player.getY(), player.getPlane());
        if (!canPlace(player, tile.getPlane(), tile.getX(), tile.getY())) {
            tile = new WorldTile(player.getX() - 1, tile.getY(), tile.getPlane());
            if (!canPlace(player, tile.getPlane(), tile.getX(), tile.getY())) {
                tile = new WorldTile(player.getX(), tile.getY() + 1, tile.getPlane());
                if (!canPlace(player, tile.getPlane(), tile.getX(), tile.getY())) {
                    tile = new WorldTile(tile.getX(), player.getY() - 1, tile.getPlane());
                    if (!canPlace(player, tile.getPlane(), tile.getX(), tile.getY())) {
                        player.sendMessage("You cannot place a portable skilling station here.");
                        return;
                    }
                }
            }
        }
        final WorldTile targetTile = tile;

        int itemId = item.getId();
        PortableType portable = PortableType.getPortable(itemId);
        if (portable == null)
            return;

        for (PortableWorldTile key : activePortables.keySet()) {
            if (targetTile.withinDistance(key.toWorldTile(), 10)) {
                player.sendMessage("You cannot place your portable so close to another person's portable.");
                return;
            }
        }

        player.sendInputInteger("How many do you wish to place? One portable lasts for 5 minutes.", new InputIntegerEvent() {

            @Override
            public void run(Player player) {
                Inventory inventory = player.getInventory();
                if (inventory == null)
                    return;

                int input = getInteger() > inventory.getAmountOf(itemId) ? inventory.getAmountOf(itemId) : getInteger();
                if (input < 1)
                    return;

                // greater than 1 hour worth.
                if (input * (500 * 600) > 3600000 || input * (500 * 600) < 1) {
                    sendOptionalDialogue(targetTile, player, portable, itemId);
                } else {
                    if (!inventory.containsItem(item.getId(), input))
                        return;

                    deployPortableWithDelay(targetTile, player, portable, itemId, input);
                }
            }
        });

    }

    private static void sendOptionalDialogue(WorldTile targetTile, Player player, PortableType portable, int itemId) {
        player.sendOptionsDialogue("The maximum time for a portable station is 1 hour. Do you wish to continue?", new String[]{"Yes", "No"}, new DialogueOptionEvent() {

            @Override
            public void run(Player player) {
                int option = getOption();

                if (option == OPTION_1) {// yes
                    int input = 3600000 / (500 * 600); // 12 portables = 1 hour.
                    if (!player.getInventory().containsItem(itemId, input))
                        return;

                    deployPortableWithDelay(targetTile, player, portable, itemId, input);
                }
            }
        });
    }

    private static void deployPortableWithDelay(WorldTile targetTile, Player player, PortableType portable, int itemId, int amount) {
        player.lock();
        WorldTasksManager.schedule(new WorldTask() {

            @Override
            public void run() {
                WorldObject object = new WorldObject(portable.getObjectId(), 10, 0, targetTile.getX(), targetTile.getY(), targetTile.getPlane());
                player.faceObject(object);
                player.setNextFaceWorldTile(targetTile);
                object.setRotation(Utils.getFaceDirection(player.getDirection()));
                World.spawnObject(object);
                PortableWorldTile portableWorldTile = new PortableWorldTile(targetTile);
                activePortables.put(portableWorldTile, player.getUsername());
                WorldTasksManager.schedule(new WorldTask() {
                    @Override
                    public void run() {
                        stop();
                        if (!World.isSpawnedObject(object)) {
                            return;
                        }
                        World.removeObject(object);
                        activePortables.remove(portableWorldTile);
                    }
                }, amount * 500);
                player.setNextAnimation(new Animation(21217));
                player.getInventory().deleteItem(itemId, amount);
                player.unlock();
            }

        }, 1);
    }

    private static boolean checkAll(Player player, Item item) {
        if (!item.getDefinitions().containsInventoryOption(0, "Deploy"))
            return false;
        if (player.getInterfaceManager().containsScreenInter() || player.getInterfaceManager().containsInventoryInter()) {
            player.sendMessage("Please finish what you're doing before doing this action.");
            return false;
        }
        if (!player.getInventory().containsItem(item.getId(), 1))
            return false;
        return isPortableItem(item.getId());
    }

    private static boolean canPlace(Player player, int plane, int x, int y) {
        return World.canMoveNPC(plane, x, y, 2) && World.isTileFree(plane, x, y, 2) && World.getObject(new WorldTile(x, y, plane)) == null && World.getObjectWithSlot(player, Region.OBJECT_SLOT_FLOOR) == null && player.getControlerManager().getControler() == null && !player.getHouse().containsPlayer(player);
    }

    public static void handleObjectClick1(Player player, WorldObject object) {
        if (!isPortableObject(object))
            return;

        PortableType portable = PortableType.getPortableObject(object.getId());
        if (portable == null || !checkGim(player, object))
            return;


        player.clickedObject = object;
        portable.handleObjectClick1(player, object);
    }

    public static void handleObjectClick2(Player player, WorldObject object) {
        if (!isPortableObject(object))
            return;

        PortableType portable = PortableType.getPortableObject(object.getId());
        if (portable == null || !checkGim(player, object))
            return;

        player.clickedObject = object;
        portable.handleObjectClick2(player, object);
    }

    public static void handleObjectClick3(Player player, WorldObject object) {
        if (!isPortableObject(object))
            return;

        PortableType portable = PortableType.getPortableObject(object.getId());
        if (portable == null || !checkGim(player, object))
            return;

        player.clickedObject = object;
        portable.handleObjectClick3(player, object);
    }

    public static void handleObjectClick4(Player player, WorldObject object) {
        if (!isPortableObject(object))
            return;

        PortableType portable = PortableType.getPortableObject(object.getId());
        if (portable == null || !checkGim(player, object))
            return;

        player.clickedObject = object;
        portable.handleObjectClick4(player, object);
    }

    private static boolean checkGim(Player player, WorldObject object) {
        if (!player.isGroupIronman())
            return true;
        String username = activePortables.get(new PortableWorldTile(object));
        if (username != null) {
            GIMGroup group = GIM.getGroupForMember(username);
            if (group == null || !group.getGroupName().equals(player.gimName)) {
                player.sendMessage("This was not deployed by one of your group members.");
                return false;
            }
        }
        return true;
    }
}
