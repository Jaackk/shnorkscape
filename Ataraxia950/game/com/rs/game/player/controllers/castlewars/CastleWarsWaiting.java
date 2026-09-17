package com.rs.game.player.controllers.castlewars;

import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.activites.CastleWars;
import com.rs.game.player.Equipment;
import com.rs.game.player.controllers.Controller;

public class CastleWarsWaiting extends Controller {

	private int team;

	@Override
	public boolean canEquip(int slotId, int itemId) {
		if (slotId == Equipment.SLOT_CAPE || slotId == Equipment.SLOT_HAT) {
			player.getPackets().sendGameMessage("You can't remove your team's colours.");
			return false;
		}
		return true;
	}

	// You can't leave just like that!

	@Override
	public void forceClose() {
		leave();
	}

	public void leave() {
	    player.getInterfaceManager().removeMinigameHudInterface();
		CastleWars.removeWaitingPlayer(player, team);
	}

	@Override
	public boolean logout() {
		player.setLocation(new WorldTile(CastleWars.LOBBY, 2));
		return true;
	}

	@Override
	public void magicTeleported(int type) {
		removeControler();
		leave();
	}

	@Override
	public boolean processButtonClick(int interfaceId, int componentId, int slotId, int slotId2, int packetId) {
		if (interfaceId == 387) {
			if (componentId == 37)
				return false;
			if (componentId == 9 || componentId == 6) {
				player.getPackets().sendGameMessage("You can't remove your team's colours.");
				return false;
			}
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
	public boolean processObjectClick1(WorldObject object) {
		int id = object.getId();
		if (id == 4389 || id == 4390) {
			removeControler();
			leave();
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
		removeControler();
		leave();
		return true;
	}

	@Override
	public void sendInterfaces() {
	    player.getInterfaceManager().sendMinigameHudInterface(57);
	}

	@Override
	public void start() {
		team = (int) getArguments()[0];
		sendInterfaces();
	}
}