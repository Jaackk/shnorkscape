package com.rs.game.player.content;

import com.rs.Settings;
import com.rs.game.WorldTile;
import com.rs.game.player.Player;

/**
 * Handles everything related to the PvP Teleports Interface.
 *
 * @author Thor.
 */
public class PvPTeleports {

	/**
	 * Sends the actual interface with all available options.
	 *
	 * @param player
	 *            The player to send the interface to.
	 */
	public static void sendInterface(Player player) {
		player.getInterfaceManager().sendInterface(1156);
		player.getPackets().sendIComponentText(1156, 190, "<col=FFFF00><shad=FFCC00>" + player.getDisplayName() + "'s "
				+ Settings.SERVER_NAME + " PvP Teleports"); // title
		player.getPackets().sendIComponentText(1156, 108, "East Dragons"); // table
																			// name
		player.getPackets().sendIComponentText(1156, 109, "Remeber your anti-d shield."); // table
																							// name
		player.getPackets().sendIComponentText(1156, 90, "Teleport"); // teleport
																		// option

		player.getPackets().sendIComponentText(1156, 113, "Forinthry Dungeon");
		player.getPackets().sendIComponentText(1156, 114, "Revs can be killed here!");
		player.getPackets().sendIComponentText(1156, 206, "Teleport");

		player.getPackets().sendIComponentText(1156, 137, "Agility Course (50 Wildy)");
		player.getPackets().sendIComponentText(1156, 138, "Train your agility here!");
		player.getPackets().sendIComponentText(1156, 254, "Teleport");

		player.getPackets().sendIComponentText(1156, 110, "Mage Bank");
		player.getPackets().sendIComponentText(1156, 111, "");
		player.getPackets().sendIComponentText(1156, 200, "Teleport");

		player.getPackets().sendIComponentText(1156, 116, "New Gates (47 Wildy)");
		player.getPackets().sendIComponentText(1156, 117, "");
		player.getPackets().sendIComponentText(1156, 212, "Teleport");

		int[] componentIds = { 122, 123, 230, 134, 135, 248, 125, 126, 224, 128, 129, 236, 119, 120, 218, 146, 147, 272,
				143, 144, 266, 149, 150, 278, 140, 141, 260, 131, 132, 242, 152, 153, 284, 167, 167, 168, 308, 155, 157,
				156, 290, 159, 161, 160, 296, 163, 165, 164, 302, 170, 171, 314, 318, 319, 326 };
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
		InterfaceManager.setPlayerInterfaceSelected(4);
		player.getInterfaceManager().closeChatBoxInterface();
		if (componentId == 88)
			Magic.vineTeleport(player, new WorldTile(3359, 3671, 0));

		if (componentId == 115)
			Magic.vineTeleport(player, new WorldTile(3071, 3649, 0));

		if (componentId == 139)
			Magic.vineTeleport(player, new WorldTile(2998, 3912, 0));

		if (componentId == 112)
			Magic.vineTeleport(player, new WorldTile(2539, 4715, 0));

		if (componentId == 118)
			Magic.vineTeleport(player, new WorldTile(3337, 3889, 0));
	}
}