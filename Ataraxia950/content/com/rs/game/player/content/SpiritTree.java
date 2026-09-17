package com.rs.game.player.content;

import com.rs.game.WorldTile;
import com.rs.game.player.Player;

/**
 * Handles the Spirit Tree transportation method.
 *
 * @author Noel
 */
public class SpiritTree {

	private static final int TREE_INTERFACE = 864;

	private static final WorldTile[] TELEPORTS = {
			new WorldTile(2541, 3170, 0), // Tree gnome village
			new WorldTile(2462, 3445, 0), // Tree gnome stronghold
			new WorldTile(2554, 3255, 0), // Battlefield of Khazard
			new WorldTile(3187, 3507, 0), // North-east of the Grand Exchange in Varrock
			new WorldTile(2416, 2852, 0), // Mobilising armies
			new WorldTile(2339, 3108, 0), // Mountains east of Poison waste
	};

	public static void openInterface(Player player, boolean isMini) {
		player.getVarBitManager().sendVarBit(3959, 3);
		player.getInterfaceManager().sendInterface(TREE_INTERFACE);
		player.getPackets().sendUnlockIComponentOptionSlots(TREE_INTERFACE, 6, 0, 7, 0);
		if (player.getRegionId() == 10033 || player.getRegionId() == 12102)
			player.getVarBitManager().sendVarBit(1469, 0x27b8c61);
		else if (player.getRegionId() == 9781)
			player.getVarBitManager().sendVarBit(1469, 0x2678d74);
		else
			sendTeleport(player, TELEPORTS[0]);
	}

	private static void sendTeleport(Player player, WorldTile tile) {
		player.sendMessage("You place your hands on the dry tough bark of the spirit tree, "
				+ "and feel a surge of energy run through your veins.");
		Magic.sendTeleportSpell(player, 7082, 7084, 1229, 1229, 1, 0, tile, 4, true, Magic.OBJECT_TELEPORT);
	}

	public static void handleSpiritTree(Player player, int slot) {
		if (player.getRegionHash() == TELEPORTS[slot].getRegionHash())
			player.sendMessage("You're already here.");
		 else
		sendTeleport(player, TELEPORTS[slot]);
	}
}