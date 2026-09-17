package com.rs.game.player.bots;

import com.rs.game.Entity;
import com.rs.game.ForceTalk;
import com.rs.game.Region;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.activites.creations.StealingCreation;
import com.rs.game.item.Item;
import com.rs.game.item.floor.FloorItem;
import com.rs.game.npc.NPC;
import com.rs.game.player.Inventory;
import com.rs.game.player.Player;
import com.rs.game.player.combat.PlayerCombat;
import com.rs.game.player.content.Foods;
import com.rs.game.player.content.RouteEvent;
import com.rs.game.route.RouteFinder;
import com.rs.game.route.strategy.FixedTileStrategy;
import com.rs.network.packet.impl.ButtonHandler;
import com.rs.utils.Utils;

import java.util.HashSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public final class BotApi {

    private static final int DEFAULT_WALK_STEPS = 25;

    private final BotPlayer bot;

    BotApi(BotPlayer bot) {
        this.bot = bot;
    }

    public BotPlayer player() {
        return bot;
    }

    public boolean walkTo(WorldTile tile) {
        return walkTo(tile, DEFAULT_WALK_STEPS);
    }

    public boolean walkTo(WorldTile tile, int maxSteps) {
        if (tile == null || bot.isLocked() || bot.isDead()) {
            return false;
        }
        bot.enableRunMode();
        bot.resetWalkSteps();
        return bot.addWalkSteps(tile.getX(), tile.getY(), Math.max(1, maxSteps), true);
    }

    public boolean walkNear(WorldTile center, int radius) {
        return walkNear(center, radius, DEFAULT_WALK_STEPS);
    }

    public boolean walkNear(WorldTile center, int radius, int maxSteps) {
        if (center == null) {
            return false;
        }
        int offsetX = ThreadLocalRandom.current().nextInt(-radius, radius + 1);
        int offsetY = ThreadLocalRandom.current().nextInt(-radius, radius + 1);
        return walkTo(center.transform(offsetX, offsetY, 0), maxSteps);
    }

    public boolean routeTo(WorldTile tile) {
        if (tile == null || bot.isLocked() || bot.isDead() || tile.getPlane() != bot.getPlane()) {
            return false;
        }
        bot.enableRunMode();
        int steps = RouteFinder.findRoute(RouteFinder.WALK_ROUTEFINDER, bot.getX(), bot.getY(), bot.getPlane(),
                bot.getSize(), new FixedTileStrategy(tile.getX(), tile.getY()), true);
        if (steps == -1) {
            return false;
        }
        bot.resetWalkSteps();
        if (steps == 0) {
            return true;
        }
        int[] bufferX = RouteFinder.getLastPathBufferX();
        int[] bufferY = RouteFinder.getLastPathBufferY();
        for (int step = steps - 1; step >= 0; step--) {
            if (!bot.addWalkSteps(bufferX[step], bufferY[step], DEFAULT_WALK_STEPS, true)) {
                break;
            }
        }
        return bot.hasWalkSteps();
    }

    public boolean routeNear(WorldTile center, int radius) {
        if (center == null) {
            return false;
        }
        int safeRadius = Math.max(0, radius);
        for (int distance = 0; distance <= safeRadius; distance++) {
            for (int offsetX = -distance; offsetX <= distance; offsetX++) {
                for (int offsetY = -distance; offsetY <= distance; offsetY++) {
                    if (Math.max(Math.abs(offsetX), Math.abs(offsetY)) != distance) {
                        continue;
                    }
                    if (routeTo(center.transform(offsetX, offsetY, 0))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public WorldObject findNearestObject(int radius, int... objectIds) {
        if (objectIds == null || objectIds.length == 0) {
            return null;
        }
        Set<Integer> ids = new HashSet<>();
        for (int id : objectIds) {
            ids.add(id);
        }
        WorldObject nearest = null;
        int nearestDistance = Integer.MAX_VALUE;
        for (int regionId : bot.getMapRegionsIds()) {
            Region region = World.getRegion(regionId, true);
            List<WorldObject> objects = region.getAllObjects();
            if (objects == null) {
                continue;
            }
            for (WorldObject object : objects) {
                if (object == null || object.getPlane() != bot.getPlane() || !ids.contains(object.getId())) {
                    continue;
                }
                int distance = Utils.getDistance(bot.getX(), bot.getY(), object.getX(), object.getY());
                if (distance <= radius && distance < nearestDistance) {
                    nearest = object;
                    nearestDistance = distance;
                }
            }
        }
        return nearest;
    }

    public WorldObject findNearestObject(int radius, ObjectFilter filter) {
        WorldObject nearest = null;
        int nearestDistance = Integer.MAX_VALUE;
        for (WorldObject object : findObjects(radius, filter)) {
            int distance = Utils.getDistance(bot.getX(), bot.getY(), object.getX(), object.getY());
            if (distance < nearestDistance) {
                nearest = object;
                nearestDistance = distance;
            }
        }
        return nearest;
    }

    public List<WorldObject> findObjects(int radius, ObjectFilter filter) {
        List<WorldObject> matches = new ArrayList<>();
        for (int regionId : bot.getMapRegionsIds()) {
            Region region = World.getRegion(regionId, true);
            List<WorldObject> objects = region.getAllObjects();
            if (objects == null) {
                continue;
            }
            for (WorldObject object : objects) {
                if (object == null || object.getPlane() != bot.getPlane()) {
                    continue;
                }
                if (filter != null && !filter.accept(object)) {
                    continue;
                }
                int distance = Utils.getDistance(bot.getX(), bot.getY(), object.getX(), object.getY());
                if (distance <= radius) {
                    matches.add(object);
                }
            }
        }
        return matches;
    }

    public Player findNearestPlayer(int radius, PlayerFilter filter) {
        Player nearest = null;
        int nearestDistance = Integer.MAX_VALUE;
        for (Player player : findPlayers(radius, filter)) {
            int distance = Utils.getDistance(bot.getX(), bot.getY(), player.getX(), player.getY());
            if (distance < nearestDistance) {
                nearest = player;
                nearestDistance = distance;
            }
        }
        return nearest;
    }

    public List<Player> findPlayers(int radius, PlayerFilter filter) {
        List<Player> matches = new ArrayList<>();
        for (Player player : World.getPlayers()) {
            if (player == null || player == bot || player.hasFinished() || player.getPlane() != bot.getPlane()) {
                continue;
            }
            if (filter != null && !filter.accept(player)) {
                continue;
            }
            int distance = Utils.getDistance(bot.getX(), bot.getY(), player.getX(), player.getY());
            if (distance <= radius) {
                matches.add(player);
            }
        }
        return matches;
    }

    public NPC findNearestNpc(int radius, NpcFilter filter) {
        NPC nearest = null;
        int nearestDistance = Integer.MAX_VALUE;
        for (NPC npc : World.getNPCs()) {
            if (npc == null || npc.hasFinished() || npc.isDead() || npc.getPlane() != bot.getPlane()) {
                continue;
            }
            if (filter != null && !filter.accept(npc)) {
                continue;
            }
            int distance = Utils.getDistance(bot.getX(), bot.getY(), npc.getX(), npc.getY());
            if (distance <= radius && distance < nearestDistance) {
                nearest = npc;
                nearestDistance = distance;
            }
        }
        return nearest;
    }

    public FloorItem findNearestGroundItem(int radius, int... itemIds) {
        if (itemIds == null || itemIds.length == 0) {
            return null;
        }
        Set<Integer> ids = new HashSet<>();
        for (int itemId : itemIds) {
            ids.add(itemId);
        }
        FloorItem nearest = null;
        int nearestDistance = Integer.MAX_VALUE;
        for (int regionId : bot.getMapRegionsIds()) {
            Region region = World.getRegion(regionId, true);
            List<FloorItem> items = region.getGroundItemsSafe();
            if (items == null) {
                continue;
            }
            for (FloorItem item : items) {
                if (item == null || item.getTile() == null || item.getTile().getPlane() != bot.getPlane()
                        || !ids.contains(item.getId()) || !canSeeGroundItem(item)) {
                    continue;
                }
                int distance = Utils.getDistance(bot.getX(), bot.getY(), item.getTile().getX(), item.getTile().getY());
                if (distance <= radius && distance < nearestDistance) {
                    nearest = item;
                    nearestDistance = distance;
                }
            }
        }
        return nearest;
    }

    public boolean pickupGroundItem(FloorItem item) {
        if (item == null || item.getTile() == null || bot.isLocked() || bot.isDead()
                || !canSeeGroundItem(item)) {
            return false;
        }
        if (!bot.withinDistance(item.getTile(), 1)) {
            return routeTo(item.getTile());
        }
        bot.stopAll();
        return World.removeGroundItem(bot, item);
    }

    public boolean interactObject(WorldObject object, int option) {
        if (object == null || bot.isLocked() || bot.isDead()) {
            return false;
        }
        bot.stopAll();
        bot.setRouteEvent(new RouteEvent(object, () -> {
            bot.faceObject(object);
            processObjectClick(object, option);
        }, true));
        return true;
    }

    public boolean useInventoryItem(int itemId) {
        if (itemId <= 0 || bot.isLocked() || bot.isDead()) {
            return false;
        }
        int slot = bot.getInventory().getItemSlot(itemId);
        if (slot < 0) {
            return false;
        }
        return !bot.getControlerManager().processButtonClick(Inventory.INVENTORY_INTERFACE, 0, slot, itemId, 0);
    }

    public boolean useItemOnObject(int itemId, WorldObject object) {
        if (itemId <= 0 || object == null || bot.isLocked() || bot.isDead()) {
            return false;
        }
        int slot = bot.getInventory().getItemSlot(itemId);
        if (slot < 0) {
            return false;
        }
        final Item item = bot.getInventory().getItem(slot);
        if (item == null || item.getId() != itemId) {
            return false;
        }
        bot.stopAll();
        bot.setRouteEvent(new RouteEvent(object, () -> {
            bot.faceObject(object);
            bot.getControlerManager().handleItemOnObject(object, item);
        }, true));
        return true;
    }

    public boolean attack(Entity target) {
        if (target == null || target == bot || target.hasFinished() || target.isDead()) {
            return false;
        }
        if (target instanceof Player) {
            Player player = (Player) target;
            if (!bot.getControlerManager().canPlayerOption1(player)) {
                return false;
            }
        }
        if (!bot.getControlerManager().processPlayerOption1(target)) {
            return false;
        }
        bot.stopAll(true);
        return bot.getActionManager().setAction(new PlayerCombat(target));
    }

    public boolean pickpocket(Player target) {
        return StealingCreation.startPickpocket(bot, target, false);
    }

    private boolean processObjectClick(WorldObject object, int option) {
        switch (option) {
            case 1:
                return bot.getControlerManager().processObjectClick1(object);
            case 2:
                return bot.getControlerManager().processObjectClick2(object);
            case 3:
                return bot.getControlerManager().processObjectClick3(object);
            case 4:
                return bot.getControlerManager().processObjectClick4(object);
            case 5:
                return bot.getControlerManager().processObjectClick5(object);
            default:
                return false;
        }
    }

    /**
     * Forces the bot to say a short message above its head. Mirrors the public
     * speech a real player produces. Returns false if the bot cannot currently
     * talk (locked, dead, finished) so callers can skip downstream side effects.
     */
    public boolean forceTalk(String message) {
        if (message == null || message.isEmpty() || bot.isDead() || bot.hasFinished() || bot.isLocked()) {
            return false;
        }
        bot.setNextForceTalk(new ForceTalk(message));
        return true;
    }

    public boolean faceTile(WorldTile tile) {
        if (tile == null || bot.isDead() || bot.hasFinished()) {
            return false;
        }
        bot.setNextFaceWorldTile(tile);
        return true;
    }

    public int findFoodSlot() {
        Item[] items = bot.getInventory().getItems().getItemsCopy();
        for (int slot = 0; slot < items.length; slot++) {
            Item item = items[slot];
            if (item != null && Foods.isConsumable(item)) {
                return slot;
            }
        }
        return -1;
    }

    public boolean hasFood() {
        return findFoodSlot() >= 0;
    }

    /**
     * Eats the first food item found in the inventory if the bot's HP is at or
     * below the supplied percent threshold. Returns true only when food was
     * actually consumed so callers can chain follow-up reactions like talking.
     */
    public boolean tryEatFood(int hpPercentThreshold) {
        if (bot.isDead() || bot.hasFinished()) {
            return false;
        }
        int max = bot.getMaxHitpoints();
        if (max <= 0) {
            return false;
        }
        int hp = bot.getHitpoints();
        int percent = (hp * 100) / max;
        if (percent > hpPercentThreshold) {
            return false;
        }
        if (bot.getFoodDelay() > Utils.currentTimeMillis()) {
            return false;
        }
        int slot = findFoodSlot();
        if (slot < 0) {
            return false;
        }
        Item food = bot.getInventory().getItem(slot);
        if (food == null) {
            return false;
        }
        return Foods.eat(bot, food, slot);
    }

    /**
     * Toggles a single normal-prayer slot if the bot has prayer points left
     * and the level requirement is met. Used for protect-from-melee flicking
     * in PvP. Returns false silently if anything blocks activation.
     */
    public boolean togglePrayer(int prayerSlot) {
        if (bot.isDead() || bot.hasFinished()) {
            return false;
        }
        if (bot.getPrayer() == null || bot.getPrayer().getPrayerpoints() <= 0) {
            return false;
        }
        if (!bot.getPrayer().hasRequiredLevel(prayerSlot, false, false)) {
            return false;
        }
        bot.getPrayer().switchPrayer(prayerSlot, false);
        return true;
    }

    public boolean hasPrayerPoints(int minPoints) {
        return bot.getPrayer() != null && bot.getPrayer().getPrayerpoints() >= minPoints;
    }

    public boolean hasPrayersOn() {
        return bot.getPrayer() != null && bot.getPrayer().hasPrayersOn();
    }

    /**
     * Triggers the special attack flag on the equipped weapon if the bot has
     * enough special-attack energy and isn't already speccing this swing.
     */
    public boolean tryUseSpecial(int minPercent) {
        if (bot.getCombatDefinitions() == null) {
            return false;
        }
        if (bot.getCombatDefinitions().isUsingSpecialAttack()) {
            return false;
        }
        if (bot.getCombatDefinitions().getSpecialAttackPercentage() < minPercent) {
            return false;
        }
        bot.getCombatDefinitions().switchUsingSpecialAttack();
        return true;
    }

    /**
     * Equips an item from the bot's inventory by item id. Useful for letting a
     * gathering bot wield a freshly processed tool without going through the UI.
     */
    public boolean equipItemId(int itemId) {
        if (itemId <= 0 || bot.isDead() || bot.hasFinished()) {
            return false;
        }
        int slot = bot.getInventory().getItemSlot(itemId);
        if (slot < 0) {
            return false;
        }
        return ButtonHandler.sendWear(bot, slot, itemId);
    }

    /**
     * Stops everything the bot is doing - useful for combat retargets and
     * panic disengage.
     */
    public void cancelAll() {
        bot.getActionManager().forceStop();
        bot.setRouteEvent(null);
        bot.resetWalkSteps();
    }

    public boolean withinDistance(WorldTile tile, int distance) {
        if (tile == null) {
            return false;
        }
        return Utils.getDistance(bot.getX(), bot.getY(), tile.getX(), tile.getY()) <= distance;
    }

    private boolean canSeeGroundItem(FloorItem item) {
        if (!item.isInvisible()) {
            return true;
        }
        return bot.getUsername() != null && bot.getUsername().equals(item.getOwner());
    }

    public interface PlayerFilter {
        boolean accept(Player player);
    }

    public interface ObjectFilter {
        boolean accept(WorldObject object);
    }

    public interface NpcFilter {
        boolean accept(NPC npc);
    }
}
