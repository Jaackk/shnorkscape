package com.rs.game.player.content;

import com.rs.Settings;
import com.rs.game.WorldTile;
import com.rs.game.player.Player;
import com.rs.game.player.controllers.RunespanController;

/**
 * Handles everything related to the Skilling Teleports Interface.
 *
 * @author Thor.
 */
public class SkillingTeleports {

	/**
	 * Sends the actual interface with all available options.
	 *
	 * @param player
	 *            The player to send the interface to.
	 */
	public static void sendInterface(Player player) {
		player.getInterfaceManager().sendInterface(1156);

		player.getPackets().sendIComponentText(1156, 190, "<col=FFFF00><shad=FFCC00>" + player.getDisplayName() + "'s "
				+ Settings.SERVER_NAME + " Skilling Teleports"); // title

		player.getPackets().sendIComponentText(1156, 108, "Fishing Guild"); // table
																			// name
		player.getPackets().sendIComponentText(1156, 109, "get that fish! save money!"); // table
																							// name
		player.getPackets().sendIComponentText(1156, 90, "Teleport"); // teleport
																		// option

		player.getPackets().sendIComponentText(1156, 113, "Al-Kharid Mine");
		player.getPackets().sendIComponentText(1156, 114, "Desert Mining!");
		player.getPackets().sendIComponentText(1156, 206, "Teleport");

		player.getPackets().sendIComponentText(1156, 137, "Karamja Mining");
		player.getPackets().sendIComponentText(1156, 138, "Jungle Mining!");
		player.getPackets().sendIComponentText(1156, 254, "Teleport");

		player.getPackets().sendIComponentText(1156, 110, "Living Rock Caverns");
		player.getPackets().sendIComponentText(1156, 111, "More mining!");
		player.getPackets().sendIComponentText(1156, 200, "Teleport");

		player.getPackets().sendIComponentText(1156, 116, "Red Sandstone");
		player.getPackets().sendIComponentText(1156, 117, "Mine red Sandstone here!");
		player.getPackets().sendIComponentText(1156, 212, "Teleport");

		player.getPackets().sendIComponentText(1156, 134, "Gnome Agility Course");
		player.getPackets().sendIComponentText(1156, 135, "");
		player.getPackets().sendIComponentText(1156, 248, "Teleport");

		player.getPackets().sendIComponentText(1156, 122, "Barbarian Outpost Agility Course");
		player.getPackets().sendIComponentText(1156, 123, "");
		player.getPackets().sendIComponentText(1156, 230, "Teleport");

		player.getPackets().sendIComponentText(1156, 128, "Wilderness Agility Course");
		player.getPackets().sendIComponentText(1156, 129, "Be careful you can be killed here!");
		player.getPackets().sendIComponentText(1156, 236, "Teleport");

		player.getPackets().sendIComponentText(1156, 125, "Jungle Woodcutting");
		player.getPackets().sendIComponentText(1156, 126, "");
		player.getPackets().sendIComponentText(1156, 224, "Teleport");

		player.getPackets().sendIComponentText(1156, 143, "Seer's Village Woodcutting");
		player.getPackets().sendIComponentText(1156, 144, "");
		player.getPackets().sendIComponentText(1156, 266, "Teleport");

		player.getPackets().sendIComponentText(1156, 146, "Runespan");
		player.getPackets().sendIComponentText(1156, 147, "Runecrafting the fun way!");
		player.getPackets().sendIComponentText(1156, 272, "Teleport");

		player.getPackets().sendIComponentText(1156, 119, "Classic Altars");
		player.getPackets().sendIComponentText(1156, 120, "Commen way of Rune-Crafting!");
		player.getPackets().sendIComponentText(1156, 218, "Teleport");

		player.getPackets().sendIComponentText(1156, 131, "Summoing");
		player.getPackets().sendIComponentText(1156, 132, "Most commenly killed for slayer tasks.");
		player.getPackets().sendIComponentText(1156, 242, "Teleport");

		player.getPackets().sendIComponentText(1156, 140, "Farming");
		player.getPackets().sendIComponentText(1156, 141, "Most commenly killed for slayer tasks.");
		player.getPackets().sendIComponentText(1156, 260, "Teleport");

		player.getPackets().sendIComponentText(1156, 149, "Falconry");
		player.getPackets().sendIComponentText(1156, 150, "");
		player.getPackets().sendIComponentText(1156, 278, "Teleport");

		player.getPackets().sendIComponentText(1156, 152, "Feldip Hills");
		player.getPackets().sendIComponentText(1156, 153, "");
		player.getPackets().sendIComponentText(1156, 284, "Teleport");

		player.getPackets().sendIComponentText(1156, 167, "Impetuous Impulses");
		player.getPackets().sendIComponentText(1156, 168, "");
		player.getPackets().sendIComponentText(1156, 308, "Teleport");

		int[] componentIds = { 155, 157, 156, 290, 159, 161, 160, 296, 163, 165, 164, 302, 170, 171, 314, 318, 319,
				326 };
		// let's clear everything else
		for (int id : componentIds)
			player.getPackets().sendIComponentText(1156, id, "Coming Soon");
	}



	/**
	 * Handles the actual interfaces buttons.
	 *
	 * @param player
	 *            The players interface to handle.
	 * @param componentId
	 *            The players interface pressed component ID.
	 */



	public static void handleInterface(Player player, int componentId) {
		InterfaceManager.setPlayerInterfaceSelected(5);
		player.getInterfaceManager().closeChatBoxInterface();
		if (componentId == 88)
			Magic.vineTeleport(player, new WorldTile(2596, 3410, 0));

		if (componentId == 115)
			Magic.vineTeleport(player, new WorldTile(3300, 3312, 0));

		if (componentId == 139)
			Magic.vineTeleport(player, new WorldTile(2849, 3033, 0));

		if (componentId == 112)
			Magic.vineTeleport(player, new WorldTile(3652, 5122, 0));

		if (componentId == 118)
			Magic.vineTeleport(player, new WorldTile(2590, 2880, 0));

		if (componentId == 136)
			Magic.vineTeleport(player, new WorldTile(2470, 3436, 0));

		if (componentId == 124)
			Magic.vineTeleport(player, new WorldTile(2552, 3563, 0));

		if (componentId == 130)
			Magic.vineTeleport(player, new WorldTile(2998, 3911, 0));

		if (componentId == 127)
			Magic.vineTeleport(player, new WorldTile(2817, 3083, 0));

		if (componentId == 145)
			Magic.vineTeleport(player, new WorldTile(2726, 3477, 0));

		if (componentId == 148) // runespan
			RunespanController.enterRunespan(player);

		if (componentId == 121)// alter rune span
			Magic.vineTeleport(player, new WorldTile(2598, 3157, 0));

		if (componentId == 133) // summoing
			Magic.vineTeleport(player, new WorldTile(2923, 3449, 0));

		if (componentId == 142)// farming
			Magic.vineTeleport(player, new WorldTile(3052, 3304, 0));

		if (componentId == 151)
			Magic.vineTeleport(player, new WorldTile(2526, 2916, 0));

		if (componentId == 154)
			Magic.vineTeleport(player, new WorldTile(2362, 3623, 0));

		if (componentId == 169) {
			if (player.isLocked())
				return;
			player.getControlerManager().startControler("PuroPuro");
		}

	}
}