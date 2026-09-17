package com.rs.game.player.controllers;

import com.rs.game.Animation;
import com.rs.game.Hit;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Colors;
import com.rs.utils.Utils;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Handles the Player Jail Controller.
 *
 * @author Noel
 */
public class JailController extends Controller {

    public static int REGION_ID = 10658;

    @Override
    public void start() {
        sendRandomJail();
    }

    @Override
    public void process() {
        if (Utils.currentTimeMillis() > player.getJailed()) {
            player.sendMessage(Colors.RED + "Your punishment has expired, you have been unjailed.");
            player.setNextWorldTile(new WorldTile(player.getHomeTile()));
            removeControler();
        }
    }

    @Override
    public boolean sendDeath() {
        WorldTasksManager.schedule(new WorldTask() {
            int loop;

            @Override
            public void run() {
                player.stopAll();
                if (loop == 0) {
                    player.setNextAnimation(new Animation(836));
                } else if (loop == 1) {
                    player.sendMessage("Oh dear, you have died.");
                } else if (loop == 3) {
                    player.setNextAnimation(new Animation(-1));
                    player.reset();
                    player.setCanPvp(false);
                    sendRandomJail();
                    player.unlock();
                }
                loop++;
            }
        }, 0, 1);
        return false;
    }

    @Override
    public boolean login() {
        return false;
    }

    @Override
    public boolean logout() {
        return false;
    }

    @Override
    public void processIngoingHit(Hit hit) {
        hit.setDamage(0);
        super.processIngoingHit(hit);
    }

    @Override
    public boolean processMagicTeleport(WorldTile toTile) {
        return false;
    }

    @Override
    public boolean processObjectTeleport(WorldTile toTile) {
        return false;
    }

    @Override
    public boolean processItemTeleport(WorldTile toTile) {
        return false;
    }

    @Override
    public boolean processObjectClick1(WorldObject object) {
        return false;
    }

    @Override
    public boolean handleItemOption1(Item item, int itemId, int slotId) {
        return false;
    }

    @Override
    public boolean canUseItemOnItem(Item itemUsed, Item usedWith) {
        return false;
    }

    @Override
    public boolean processItemOnNPC(NPC npc, Item item) {
        return false;
    }

    @Override
    public boolean processItemOnPlayer(Player player, int itemId) {
        return false;
    }

    @Override
    public boolean processCommand(String s, boolean b, boolean c) {
        return player.isStaff();
    }

    @Override
    public boolean processButtonClick(int interfaceId, int componentId, int slotId, int slotId2, int packetId) {
        return interfaceId != 506 && interfaceId != 193;
    }

    private void sendRandomJail() {
        player.stopAll(true, true, true);
        player.resetWalkSteps();
        player.setRouteEvent(null);
        WorldTile[] jailTiles = {
                new WorldTile(2669, 10387, 0),
                new WorldTile(2669, 10383, 0),
                new WorldTile(2669, 10379, 0),
                new WorldTile(2673, 10379, 0),
                new WorldTile(2673, 10385, 0),
                new WorldTile(2677, 10387, 0),
                new WorldTile(2677, 10383, 0),
        };
        player.setNextWorldTile(jailTiles[ThreadLocalRandom.current().nextInt(jailTiles.length)]);
    }
}