package com.rs.game.player.content;

import com.rs.Settings;
import com.rs.game.WorldTile;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;

/**
 * Handles everything related to the training teleports interface.
 *
 * @author Thor.
 */
public class TrainingTeleports {

	/**
	 * Sends the actual interface with all available options.
	 *
	 * @param player
	 *            The player to send the interface to.
	 */
	public static void sendInterface(Player player) {
		player.getInterfaceManager().closeChatBoxInterface();
		player.getInterfaceManager().sendInterface(1156);
		player.getPackets().sendIComponentText(1156, 190, "<col=FFFF00><shad=FFCC00>" + player.getDisplayName() + "'s "
				+ Settings.SERVER_NAME + " Training Teleports"); // title
		player.getPackets().sendIComponentText(1156, 108, "East Rock Crabs"); // table
																				// name
		player.getPackets().sendIComponentText(1156, 109, "New? This is a good place to start!"); // table
																									// name
		player.getPackets().sendIComponentText(1156, 90, "Teleport"); // teleport
																		// option

		player.getPackets().sendIComponentText(1156, 113, "Glacor Cave");
		player.getPackets().sendIComponentText(1156, 114, "Be careful, its cold in here!");
		player.getPackets().sendIComponentText(1156, 206, "Teleport");

		player.getPackets().sendIComponentText(1156, 137, "Dwarf Battlefield");
		player.getPackets().sendIComponentText(1156, 138, "Don't let their size fool you!");
		player.getPackets().sendIComponentText(1156, 254, "Teleport");

		player.getPackets().sendIComponentText(1156, 110, "Frost Dragons");
		player.getPackets().sendIComponentText(1156, 111, "Get them dragon bones!");
		player.getPackets().sendIComponentText(1156, 200, "Teleport");

		player.getPackets().sendIComponentText(1156, 116, "Kuradal's Dungeon");
		player.getPackets().sendIComponentText(1156, 117, "Most slayer tasks are found here!");
		player.getPackets().sendIComponentText(1156, 212, "Teleport");

		player.getPackets().sendIComponentText(1156, 134, "Jadinko Lair");
		player.getPackets().sendIComponentText(1156, 135, "More slayer task monsters here!");
		player.getPackets().sendIComponentText(1156, 248, "Teleport");

		player.getPackets().sendIComponentText(1156, 122, "Polypore Dungeon");
		player.getPackets().sendIComponentText(1156, 123, "Get your polypore staff here!");
		player.getPackets().sendIComponentText(1156, 230, "Teleport");

		player.getPackets().sendIComponentText(1156, 128, "Slayer Tower");
		player.getPackets().sendIComponentText(1156, 129, "More slayer locations here!");
		player.getPackets().sendIComponentText(1156, 236, "Teleport");

		player.getPackets().sendIComponentText(1156, 125, "Ancient Cavern");
		player.getPackets().sendIComponentText(1156, 126, "mith + green dragons and water fiends here!");
		player.getPackets().sendIComponentText(1156, 224, "Teleport");

		player.getPackets().sendIComponentText(1156, 143, "Brimhaven Dungeon");
		player.getPackets().sendIComponentText(1156, 144, "Most commenly killed for slayer tasks.");
		player.getPackets().sendIComponentText(1156, 266, "Teleport");

		player.getPackets().sendIComponentText(1156, 146, "Fremennik Dungeon");
		player.getPackets().sendIComponentText(1156, 147, "Most commenly killed for slayer tasks.");
		player.getPackets().sendIComponentText(1156, 272, "Teleport");

		player.getPackets().sendIComponentText(1156, 119, "Taverley Dungeon");
		player.getPackets().sendIComponentText(1156, 120, "Most commenly killed for slayer tasks.");
		player.getPackets().sendIComponentText(1156, 218, "Teleport");

		player.getPackets().sendIComponentText(1156, 131, "Jungle Strykewyrms");
		player.getPackets().sendIComponentText(1156, 132, "Most commenly killed for slayer tasks.");
		player.getPackets().sendIComponentText(1156, 242, "Teleport");

		player.getPackets().sendIComponentText(1156, 140, "Desert Strykewyrms");
		player.getPackets().sendIComponentText(1156, 141, "Most commenly killed for slayer tasks.");
		player.getPackets().sendIComponentText(1156, 260, "Teleport");

		player.getPackets().sendIComponentText(1156, 149, "Ice Strykewyrms");
		player.getPackets().sendIComponentText(1156, 150, "Most commenly killed for slayer tasks.");
		player.getPackets().sendIComponentText(1156, 278, "Teleport");

		player.getPackets().sendIComponentText(1156, 152, "West Rock Crabs");
		player.getPackets().sendIComponentText(1156, 153, "Most commenly killed for slayer tasks.");
		player.getPackets().sendIComponentText(1156, 284, "Teleport");

		int[] componentIds = { 167, 167, 168, 308, 155, 157, 156, 290, 159, 161, 160, 296, 163, 165, 164, 302, 170, 171,
				314, 318, 319, 326 };
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
		InterfaceManager.setPlayerInterfaceSelected(3);
		player.getInterfaceManager().closeChatBoxInterface();
		if (componentId == 88)
			Magic.vineTeleport(player, new WorldTile(2710, 3710, 0)); // rocks
																		// #1

		if (componentId == 115)
			Magic.vineTeleport(player, new WorldTile(4181, 5726, 0)); // Glacor
																		// Cave

		if (componentId == 139)
			Magic.vineTeleport(player, new WorldTile(1519, 4704, 0)); // dwarf

		if (componentId == 112) {
			if (player.getSkills().getLevel(Skills.DUNGEONEERING) < 85) {
				player.sendMessage("This area requires at least level 85 Dungeoneering to access!");
				return;
			}
			Magic.vineTeleport(player, new WorldTile(1298, 4510, 0)); // frosts
		}

		if (componentId == 118)
			Magic.vineTeleport(player, new WorldTile(1690, 5286, 1)); // kurdals
																		// dung

		if (componentId == 136) {
			if (player.getSkills().getLevel(Skills.SLAYER) < 80) {
				player.sendMessage("You need a slayer level of 80 to use this teleport.");
				return;
			}
			Magic.vineTeleport(player, new WorldTile(3012, 9274, 0)); // jadinko
																		// lair
		}

		if (componentId == 124)
			Magic.vineTeleport(player, new WorldTile(4625, 5457, 3)); // polypore

		if (componentId == 130)
			Magic.vineTeleport(player, new WorldTile(3423, 3543, 0)); // slayer
																		// tower

		if (componentId == 127)
			Magic.vineTeleport(player, new WorldTile(1763, 5365, 1)); // Ancient
																		// cavern.

		if (componentId == 145) // Brimhaven Dungeon
			Magic.vineTeleport(player, new WorldTile(2699, 9564, 0));

		if (componentId == 148) // fremennik Dungeon
			Magic.vineTeleport(player, new WorldTile(2808, 10002, 0));

		if (componentId == 121) // Taverley Dungeon
			Magic.vineTeleport(player, new WorldTile(2884, 9799, 0));

		if (componentId == 133) { // Jungle Strykewyrms
			if (player.getSkills().getLevelForXp(Skills.SLAYER) < 73) {
				player.sendMessage("You need at least a level of 73 Slayer to go there!");
				return;
			}
			Magic.vineTeleport(player, new WorldTile(2452, 2911, 0));
		}

		if (componentId == 142) { // Desert Strykewyrms
			if (player.getSkills().getLevelForXp(Skills.SLAYER) < 77) {
				player.sendMessage("You need at least a level of 77 Slayer to go there!");
				return;
			}
			Magic.vineTeleport(player, new WorldTile(3356, 3160, 0));
		}

		if (componentId == 151) {// ice Strykewyrms
			if (player.getSkills().getLevelForXp(Skills.SLAYER) < 93) {
				player.sendMessage("You need at least a level of 93 Slayer to go there!");
				return;
			}
			Magic.vineTeleport(player, new WorldTile(3435, 5648, 0));
		}
		if (componentId == 154) // other crabs
			Magic.vineTeleport(player, new WorldTile(2672, 3710, 0));
	}

}