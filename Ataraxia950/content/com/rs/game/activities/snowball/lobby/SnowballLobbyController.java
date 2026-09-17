package com.rs.game.activities.snowball.lobby;

import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.player.Equipment;
import com.rs.game.player.controllers.Controller;
import com.rs.utils.Colors;
import lombok.val;

public class SnowballLobbyController extends Controller {

    private final SnowballLobby lobby = World.getSnowballLobby();

    @Override
    public void start() {
        initializeTimeLeftComponentUpdateTask();
        addToLobby();
        player.sendMessage(Colors.GREEN+"You have been placed in the snowball fight lobby!");
        lobby.sendLobbyInterface(player);
    }

    private void initializeTimeLeftComponentUpdateTask() {
        val amountOfPlayersBefore = lobby.getPlayers().size();
        if (amountOfPlayersBefore == 0) {
            lobby.initializeTimeLeftComponentUpdateTask();
        }
    }

    private void addToLobby() {
        if(!lobby.getPlayers().contains(player)) {
            lobby.getPlayers().add(player);
            lobby.sendPlayerCount(player, true);
        }
    }

    public void removeFromLobby(final boolean startXmas) {
        lobby.getPlayers().remove(player);
        removeControler();
        player.getInterfaceManager().closeOverlay(false);
        if(startXmas) {
            player.getControlerManager().startControler("XmasController");
        }
    }

    @Override
    public boolean sendDeath() {
        removeFromLobby(true);
        return super.sendDeath();
    }

    @Override
    public void magicTeleported(final int type) {
        removeFromLobby(false);
    }

    @Override
    public boolean processMagicTeleport(final WorldTile tile) {
        removeFromLobby(false);
        return super.processMagicTeleport(tile);
    }

    @Override
    public boolean processItemTeleport(final WorldTile tile) {
        removeFromLobby(false);
        return super.processItemTeleport(tile);
    }

    @Override
    public boolean processObjectTeleport(final WorldTile tile) {
        removeFromLobby(false);
        return super.processObjectTeleport(tile);
    }

    @Override
    public boolean logout() {
        removeFromLobby(true);
        return false;
    }

    @Override
    public boolean login() {
        removeFromLobby(true);
        return false;
    }

    @Override
    public boolean canEquip(int slotId, int itemId) {
        if (slotId == Equipment.SLOT_CAPE) {
            player.sendMessage("You can't wear capes in the lobby!");
            return false;
        }
        return super.canEquip(slotId, itemId);
    }
}
