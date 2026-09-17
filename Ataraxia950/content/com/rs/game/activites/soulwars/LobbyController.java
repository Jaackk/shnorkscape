package com.rs.game.activites.soulwars;

import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.activites.soulwars.SoulWarsManager.PlayerType;
import com.rs.game.activites.soulwars.SoulWarsManager.Teams;
import com.rs.game.item.Item;
import com.rs.game.player.Equipment;
import com.rs.game.player.Inventory;
import com.rs.game.player.controllers.Controller;

/**
 * @author Savions Sw
 */
public class LobbyController extends Controller {

	@Override
	public void start() {
		player.getInterfaceManager().sendOverlay(837, false);
		((LobbyTask) World.soulWars.getTasks().get(PlayerType.INSIDE_LOBBY)).getPlayers().add(player);
	}

	@Override
	public void sendInterfaces() {
		player.getInterfaceManager().sendOverlay(837, false);
		final int minutes = SoulWarsManager.MINUTES_BEFORE_NEXT_GAME.get();
		boolean noGame = minutes < 4;
		int blue, red;
		if (noGame) {
			LobbyTask task = (LobbyTask) World.soulWars.getTasks().get(PlayerType.INSIDE_LOBBY);
			blue = task.getPlayers(Teams.BLUE).size();
			red = task.getPlayers(Teams.RED).size();
		} else {
			GameTask task = (GameTask) World.soulWars.getTasks().get(PlayerType.IN_GAME);
			blue = task.getPlayers(Teams.BLUE).size();
			red = task.getPlayers(Teams.RED).size();
		}
		player.getPackets().sendGlobalConfig(632, noGame ? 0 : 1);
		player.getPackets().sendIComponentText(837, 9, "New game: " + minutes + " " + (minutes == 1 ? "min" : "mins"));
		if (noGame) {
			player.getPackets().sendGlobalConfig(633, blue + (10 - SoulWarsManager.REQUIRED_TEAM_MEMBERS));
			player.getPackets().sendGlobalConfig(634, red + (10 - SoulWarsManager.REQUIRED_TEAM_MEMBERS));
		} else {
			player.getPackets().sendIComponentText(837, 3, "" + blue);
			player.getPackets().sendIComponentText(837, 5, "" + red);
		}
	}

	@Override
	public boolean canEquip(int slotId, int itemId) {
		if (slotId == Equipment.SLOT_CAPE) {
			player.getPackets().sendGameMessage("You can't remove your team's colours.");
			return false;
		}
		return true;
	}

	@Override
	public void forceClose() {
		player.getInterfaceManager().closeOverlay(false);
		((LobbyTask) World.soulWars.getTasks().get(PlayerType.INSIDE_LOBBY)).getPlayers().remove(player);
	}

	@Override
	public boolean processMagicTeleport(WorldTile toTile) {
		player.getPackets().sendGameMessage("You can't just leave like that!");
		return false;
	}

	@Override
	public boolean processItemTeleport(WorldTile toTile) {
		player.getPackets().sendGameMessage("You can't just leave like that!");
		return false;
	}

	@Override
	public void magicTeleported(int type) {
		forceClose();
		removeControler();
	}

	@Override
	public boolean processButtonClick(int interfaceId, int componentId, int slotId, int slotId2, int packetId) {
		if (interfaceId == 590 & componentId == 8) {
			player.getPackets().sendGameMessage("This is a battleground, not a circus.");
			return false;
		}
		if (interfaceId == 387 && componentId == 9) {
			player.getPackets().sendGameMessage("You can't remove your team's colours.");
			return false;
		}
		if (interfaceId == 667 && componentId == 9) {
			player.getPackets().sendGameMessage("You can't remove your team's colours.");
			return false;
		}
		if (interfaceId == Inventory.INVENTORY_INTERFACE || interfaceId == 670) {
			Item item = player.getInventory().getItem(slotId);
			if (item != null) {
				if (item.getId() == SoulWarsManager.TEAM_CAPE_INDEX
						|| item.getId() == SoulWarsManager.TEAM_CAPE_INDEX + 1) {
					player.getPackets().sendGameMessage("You can't remove your team's colours.");
					return false;
				}
			}
		}
		return true;
	}

	@Override
	public boolean processObjectClick1(WorldObject object) {
		switch (object.getId()) {
		case 42029:
		case 42030:
		case 42031:
			World.soulWars.passBarrier(PlayerType.INSIDE_LOBBY, player, object);
			return true;
		}
		return false;
	}

	@Override
	public boolean login() {
		World.soulWars.resetPlayer(player, PlayerType.INSIDE_LOBBY, true);
		player.getControlerManager().startControler("AreaController");
		return false;
	}

	@Override
	public boolean sendDeath() {
		World.soulWars.resetPlayer(player, PlayerType.INSIDE_LOBBY, false);
		return super.sendDeath();
	}

	@Override
	public boolean logout() {
		World.soulWars.resetPlayer(player, PlayerType.INSIDE_LOBBY, true);
		return false;
	}
}