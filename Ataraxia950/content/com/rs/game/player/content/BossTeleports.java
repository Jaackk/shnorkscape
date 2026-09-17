package com.rs.game.player.content;

import com.rs.Settings;
import com.rs.game.Animation;
import com.rs.game.Graphics;
import com.rs.game.WorldTile;
import com.rs.game.player.Player;

/**
 * Handles everything related to the Boss Teleports interface.
 *
 * @author Thor.
 */
public class BossTeleports {

	/**
	 * Sends the actual interface with all available options.
	 *
	 * @param player
	 *            The player to send the interface to.
	 */
	public static void sendInterface(Player player) {
		player.getInterfaceManager().sendInterface(1156);
		player.getPackets().sendIComponentText(1156, 190, "<col=FFFF00><shad=FFCC00>" + player.getDisplayName() + "'s "
				+ Settings.SERVER_NAME + " Boss Teleports");
		player.getPackets().sendIComponentText(1156, 108, "Godwars Dungeon"); // table
																				// name
		player.getPackets().sendIComponentText(1156, 109, "Fight bandos, sara, nex, zammy, ect here."); // table
																										// name
		player.getPackets().sendIComponentText(1156, 90, "Teleport");

		player.getPackets().sendIComponentText(1156, 113, "Corproeal Beast");
		player.getPackets().sendIComponentText(1156, 114, "Fight the mighty Cororeal Beast here!");
		player.getPackets().sendIComponentText(1156, 206, "Teleport");

		player.getPackets().sendIComponentText(1156, 137, "Kalhpite Queen");
		player.getPackets().sendIComponentText(1156, 138, "Fight the queen, if you dare!!");
		player.getPackets().sendIComponentText(1156, 254, "Teleport");

		player.getPackets().sendIComponentText(1156, 110, "Queen Black Dragon");
		player.getPackets().sendIComponentText(1156, 111, "One of the hardest solo bosses! good luck!");
		player.getPackets().sendIComponentText(1156, 200, "Teleport");

		player.getPackets().sendIComponentText(1156, 116, "King Black Dragon");
		player.getPackets().sendIComponentText(1156, 117, "Most commenly killed for slayer or loot drops!");
		player.getPackets().sendIComponentText(1156, 212, "Teleport");

		player.getPackets().sendIComponentText(1156, 134, "Bork");
		player.getPackets().sendIComponentText(1156, 135, "Get your bork kills here!");
		player.getPackets().sendIComponentText(1156, 248, "Teleport");

		player.getPackets().sendIComponentText(1156, 122, "Barrelchest");
		player.getPackets().sendIComponentText(1156, 123, "Get your anchor wepon here!");
		player.getPackets().sendIComponentText(1156, 230, "Teleport");

		player.getPackets().sendIComponentText(1156, 128, "Dagonnoth Kings");
		player.getPackets().sendIComponentText(1156, 129, "Show the server you have it in you!");
		player.getPackets().sendIComponentText(1156, 236, "Teleport");

		player.getPackets().sendIComponentText(1156, 125, "Chaos elemental (Deep Wild)");
		player.getPackets().sendIComponentText(1156, 126, "Kill the elemental beasts here!");
		player.getPackets().sendIComponentText(1156, 224, "Teleport");

		player.getPackets().sendIComponentText(1156, 143, "Araxyte Cave");
		player.getPackets().sendIComponentText(1156, 144, "Fight the horrific rs3 spider boss here!");
		player.getPackets().sendIComponentText(1156, 266, "Teleport");

		player.getPackets().sendIComponentText(1156, 146, "Tormented Demons");
		player.getPackets().sendIComponentText(1156, 147, "Anyone in need of dragon claws?");
		player.getPackets().sendIComponentText(1156, 272, "Teleport");

		player.getPackets().sendIComponentText(1156, 119, "Kalhpite King");
		player.getPackets().sendIComponentText(1156, 120, "Fight the Rs3 boss Kalphite King here!");
		player.getPackets().sendIComponentText(1156, 218, "Teleport");

		player.getPackets().sendIComponentText(1156, 131, "Vorago (open BETA)");
		player.getPackets().sendIComponentText(1156, 132, "Help us work out the kinks in vorago!");
		player.getPackets().sendIComponentText(1156, 242, "Teleport");

		int[] componentIds = { 140, 141, 260, 278, 149, 150, 152, 153, 284, 167, 167, 168, 308, 155, 157, 156, 290, 159,
				161, 160, 296, 163, 165, 164, 302, 170, 171, 314, 318, 319, 326 };
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
		player.getInterfaceManager().closeChatBoxInterface();
		if (componentId == 88)
			teleportPlayer(player, 2882, 5311, 0, "GodWars");

		if (componentId == 115)
			Magic.vineTeleport(player, new WorldTile(2966, 4383, 2));

		if (componentId == 139)
			Magic.vineTeleport(player, new WorldTile(3479, 9488, 0));

		if (componentId == 4)
			Magic.vineTeleport(player, new WorldTile(1195, 6499, 0));

		if (componentId == 112)
			Magic.vineTeleport(player, new WorldTile(3051, 3518, 0));

		if (componentId == 118)
			Magic.vineTeleport(player, new WorldTile(3051, 3518, 0));

		if (componentId == 136)
			Magic.vineTeleport(player, new WorldTile(3143, 5545, 0));

		if (componentId == 124)
			Magic.vineTeleport(player, new WorldTile(3803, 2844, 0));

		if (componentId == 130)
			Magic.vineTeleport(player, new WorldTile(2900, 4449, 0));

		if (componentId == 127)
			Magic.vineTeleport(player, new WorldTile(3143, 3823, 0));

		if (componentId == 145)
			Magic.vineTeleport(player, new WorldTile(4512, 6289, 1));

		if (componentId == 148)
			Magic.vineTeleport(player, new WorldTile(2571, 5735, 0));

		if (componentId == 121)
			Magic.vineTeleport(player, new WorldTile(2974, 1654, 0));

		if (componentId == 133)
			teleportPlayer(player, 3041, 6125, 0, "VoragoLobbyController");
	}

	public static void teleportPlayer(Player player, final int placeX, final int placeY, final int placePlane,
			String controller) {
		if (player.isLocked())
			return;
		Magic.vineTeleport(player, new WorldTile(placeX, placeY, placePlane));
		final WorldTile teleTile = new WorldTile(placeX, placeY, placePlane);
		if (!player.getControlerManager().processMagicTeleport(teleTile))
			return;
		player.lock(4);
		player.stopAll();
		player.setNextGraphics(new Graphics(1229));
		player.setNextAnimation(new Animation(7082));

	}
}