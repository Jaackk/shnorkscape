package com.rs.game.activities.snowball.game;

import com.rs.cores.CoresManager;
import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.Hit;
import com.rs.game.NewProjectile;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.activites.CastleWars;
import com.rs.game.item.Item;
import com.rs.game.player.Equipment;
import com.rs.game.player.Player;
import com.rs.game.player.content.Foods;
import com.rs.game.player.controllers.Controller;
import com.rs.utils.Utils;
import lombok.val;
import lombok.var;

public class SnowballFightGameController extends Controller {

    public static final int INTERFACE_ID = 59;
    private static final int SNOWBALL_ITEM_ID = 33590;
    private static final String THROWING_SNOWBALL_ATTRIBUTE_KEY = "throwing_snowball";

    @Override
    public void start() {
        addThrowSnowballPlayerOption();
        sendInterfaces();
    }

    public void leaveController(boolean startXmasController) {
        removeControler();
        if (startXmasController) {
            player.getControlerManager().startControler("XmasController", true);
        }
        if (World.getSnowballFightGame() != null) {
            World.getSnowballFightGame().leaveGame(player);
        } else {
            removeCloak();
        }
    }

    private void removeCloak() {
        CastleWars.setCape(player, null);
    }

    private void addThrowSnowballPlayerOption() {
        player.getPackets().sendPlayerOption("Throw at", 1, true);
    }

    @Override
    public boolean sendDeath() {
        val game = World.getSnowballFightGame();
        game.handleDeath(player);
        return false;
    }
    
    @Override
    public boolean processPlayerOption1(Entity target) {
    	if (!(target instanceof Player)) {
            return false;
        }

        if (hasSnowballs()) {
            if (canThrowSnowball((Player) target)) {
                throwSnowball((Player) target);
            }
        } else {
            sendNoSnowballsMessage();
        }
        return false;
    }

    private boolean canThrowSnowball(Player p2) {
        return !isThrowingSnowball() && isInSnowballballFightController(p2) && !isSameTeam(p2);
    }

    private boolean isSameTeam(Player p2) {
        val team = (SnowballFightTeam) player.getTemporaryAttributtes().get(SnowballFightGame.TEAM_ATTRIBUTE_KEY);
        return team.getPlayers().contains(p2);
    }

    private boolean isInSnowballballFightController(Player player) {
        return player.getControlerManager().getControler() instanceof SnowballFightGameController;
    }

    @Override
    public boolean processObjectClick1(WorldObject object) {
        val snowPileObjectId = 28296;
        if (object.getId() == snowPileObjectId) {
            pickupSnowballs(object);
            return false;
        }
        return super.processObjectClick1(object);
    }

    @Override
    public boolean canRemoveEquip(int slotId, int itemId) {
        if (slotId == Equipment.SLOT_CAPE) {
            player.sendMessage("You can't remove your team color!");
            return false;
        }
        return super.canRemoveEquip(slotId, itemId);
    }

    @Override
    public boolean canEquip(int slotId, int itemId) {
        if (slotId == Equipment.SLOT_CAPE) {
            player.sendMessage("You can't remove your team color!");
            return false;
        }
        return super.canEquip(slotId, itemId);
    }

    private void pickupSnowballs(WorldObject object) {
        if (Utils.currentTimeMillis() - player.getXmas().snowballTick < 2) {
            return;
        }
        if (!player.getInventory().hasFreeSlots()) {
            player.sendMessage("You do not have enough inventory space!");
            return;
        }
        if (player.withinDistance(new WorldTile(object.getX(), object.getY(), 0))) {
            player.faceObject(object);
            player.getInventory().addItem(new Item(33590, 10));
            player.sendMessage("You pick up snowballs from the pile!", true);
            player.getXmas().snowballTick = Utils.currentTimeMillis();
        } else {
            player.addWalkSteps(object.getX(), object.getY());
        }
    }

    private boolean isThrowingSnowball() {
        return (boolean) player.getTemporaryAttributtes().getOrDefault(THROWING_SNOWBALL_ATTRIBUTE_KEY, false);
    }

    private void throwSnowball(Player p2) {
        val snowballAnimationId = 7530;
        val snowballProjectileGraphicsId = 1281;
        val playerHeight = 30;
        val snowballProjectile = new NewProjectile(player, p2, snowballProjectileGraphicsId, playerHeight, playerHeight);

        removeSnowball();
        player.stopAll(true);
        player.faceEntity(p2);
        player.setNextAnimation(new Animation(snowballAnimationId));
        player.getTemporaryAttributtes().put(THROWING_SNOWBALL_ATTRIBUTE_KEY, true);
        CoresManager.getServiceProvider().executeWithDelay(() -> World.sendProjectile(snowballProjectile), 2);
        CoresManager.getServiceProvider().executeWithDelay(() -> {
            var damage = p2.getMaxHitpoints() * (0.1 + (0.025 * (player.getXmas().damage - 1)));
            World.getSnowballFightGame().addDamage(player, damage);
            if (damage > p2.getMaxHitpoints()) {
                damage = p2.getMaxHitpoints();
            }
            p2.applyHit(new Hit((int) damage, Hit.HitLook.REGULAR_DAMAGE));
            player.getTemporaryAttributtes().put(THROWING_SNOWBALL_ATTRIBUTE_KEY, false);
        }, player.withinDistance(p2, 3) ? 3 : 4);
    }

    private void sendNoSnowballsMessage() {
        player.sendMessage("You don't have any snowballs to throw!");
    }

    private boolean hasSnowballs() {
        return player.getEquipment().getWeaponId() == SNOWBALL_ITEM_ID || player.getInventory().containsItem(SNOWBALL_ITEM_ID, 1);
    }

    @Override
    public void sendInterfaces() {
        player.getInterfaceManager().sendOverlay(INTERFACE_ID, false);
        for (int componentId = 11; componentId <= 18; componentId++) {
            player.getPackets().sendHideIComponent(INTERFACE_ID, componentId, true);
        }
        player.getPackets().sendHideIComponent(INTERFACE_ID, 9, true);
        player.getPackets().sendHideIComponent(INTERFACE_ID, 4, true);
        World.getSnowballFightGame().sendInterfaceInformation(player);
    }

    private void removeSnowball() {
        if (player.getEquipment().getWeaponId() == SNOWBALL_ITEM_ID) {
            player.getEquipment().removeAmmo(SNOWBALL_ITEM_ID, 1, player.secondary);
            player.getEquipment().refresh(Equipment.SLOT_WEAPON);
        } else if (player.getInventory().containsItem(SNOWBALL_ITEM_ID, 1)) {
            player.getInventory().deleteItem(SNOWBALL_ITEM_ID, 1);
        }
    }

    @Override
    public boolean canEat(Foods.Food food) {
        player.sendMessage("You can't eat food here!");
        return false;
    }

    @Override
    public boolean logout() {
        leaveController(true);
        return super.logout();
    }

    @Override
    public boolean login() {
        leaveController(true);
        return super.login();
    }

    @Override
    public boolean processMagicTeleport(WorldTile toTile) {
        leaveController(false);
        return super.processMagicTeleport(toTile);
    }

    @Override
    public boolean processItemTeleport(WorldTile toTile) {
        leaveController(false);
        return super.processItemTeleport(toTile);
    }

    @Override
    public boolean processObjectTeleport(WorldTile toTile) {
        leaveController(false);
        return super.processObjectTeleport(toTile);
    }

    @Override
    public void magicTeleported(int type) {
        leaveController(false);
    }
}