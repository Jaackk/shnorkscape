package com.rs.game.player.content;

import com.rs.Settings;
import com.rs.game.WorldTile;
import com.rs.game.player.Player;

/**
 * Handles everything related to the Minigame Teleports Interface.
 *
 * @author Thor.
 */
public class MinigameTeleports {

	/**
	 * Sends the actual interface with all available options.
	 *
	 * @param player
	 *            The player to send the interface to.
	 */
	public static void sendInterface(Player player) {
		player.getInterfaceManager().sendInterface(1156);
		player.getPackets().sendIComponentText(1156, 190, "<col=FFFF00><shad=FFCC00>" + player.getDisplayName() + "'s "
				+ Settings.SERVER_NAME + " Minigame Teleports"); // title
		player.getPackets().sendIComponentText(1156, 108, "Barrows"); // table
																		// name
		player.getPackets().sendIComponentText(1156, 109, "Barrows armors can be obtained here!"); // table
																									// name
		player.getPackets().sendIComponentText(1156, 90, "Teleport"); // teleport
																		// option

		player.getPackets().sendIComponentText(1156, 113, "Clan Wars");
		player.getPackets().sendIComponentText(1156, 114, "Teleport to the clan battle grounds!");
		player.getPackets().sendIComponentText(1156, 206, "Teleport");

		player.getPackets().sendIComponentText(1156, 137, "Pest Control");
		player.getPackets().sendIComponentText(1156, 138, "Complete the minigame for points!");
		player.getPackets().sendIComponentText(1156, 254, "Teleport");

		player.getPackets().sendIComponentText(1156, 110, "Dungeoneering");
		player.getPackets().sendIComponentText(1156, 111, "Train the skill �sungeoneering here!");
		player.getPackets().sendIComponentText(1156, 200, "Teleport");

		player.getPackets().sendIComponentText(1156, 116, "Fight Kiln");
		player.getPackets().sendIComponentText(1156, 117, "Get your advanced firecape here!");
		player.getPackets().sendIComponentText(1156, 212, "Teleport");

		player.getPackets().sendIComponentText(1156, 134, "Fight Caves");
		player.getPackets().sendIComponentText(1156, 135, "Get your firecape here!");
		player.getPackets().sendIComponentText(1156, 248, "Teleport");

		player.getPackets().sendIComponentText(1156, 122, "Recipe for Disaster");
		player.getPackets().sendIComponentText(1156, 123, "Start the quest here!");
		player.getPackets().sendIComponentText(1156, 230, "Teleport");

		player.getPackets().sendIComponentText(1156, 128, "Duel Arena");
		player.getPackets().sendIComponentText(1156, 129, "Show your friends whos boss!");
		player.getPackets().sendIComponentText(1156, 236, "Teleport");

		player.getPackets().sendIComponentText(1156, 125, "Warriors Guild");
		player.getPackets().sendIComponentText(1156, 126, "Get your defenders here!");
		player.getPackets().sendIComponentText(1156, 224, "Teleport");

		player.getPackets().sendIComponentText(1156, 143, "Soul Wars");
		player.getPackets().sendIComponentText(1156, 144, "Fight for some points here!");
		player.getPackets().sendIComponentText(1156, 266, "Teleport");

		player.getPackets().sendIComponentText(1156, 146, "Dominion Tower");
		player.getPackets().sendIComponentText(1156, 147, "Complete all the levels!");
		player.getPackets().sendIComponentText(1156, 272, "Teleport");

		int[] componentIds = { 119, 120, 218, 131, 132, 242, 140, 141, 260, 278, 149, 150, 152, 153, 284, 167, 167, 168,
				308, 155, 157, 156, 290, 159, 161, 160, 296, 163, 165, 164, 302, 170, 171, 314, 318, 319, 326 };
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
		InterfaceManager.setPlayerInterfaceSelected(2);
		player.getInterfaceManager().closeChatBoxInterface();
		if (componentId == 88)
			Magic.vineTeleport(player, new WorldTile(3563, 3288, 0));

		if (componentId == 115)
			Magic.vineTeleport(player, new WorldTile(2994, 9679, 0));

		if (componentId == 139)
			Magic.vineTeleport(player, new WorldTile(2652, 2655, 0));

		if (componentId == 112)
			Magic.daemonheimTeleport(player, new WorldTile(3972, 5562, 0));

		if (componentId == 118)
			Magic.vineTeleport(player, new WorldTile(4743, 5170, 0));

		if (componentId == 136)
			Magic.vineTeleport(player, new WorldTile(4613, 5129, 0));

		if (componentId == 124)
			Magic.vineTeleport(player, new WorldTile(1866, 5346, 0));

		if (componentId == 130)
			Magic.vineTeleport(player, new WorldTile(3325, 3232, 0));

		if (componentId == 127)
			Magic.vineTeleport(player, new WorldTile(2879, 3542, 0));

		if (componentId == 145)
			Magic.vineTeleport(player, new WorldTile(3081, 3475, 0));

		if (componentId == 148)
			Magic.vineTeleport(player, new WorldTile(3367, 3083, 0));

	}
}