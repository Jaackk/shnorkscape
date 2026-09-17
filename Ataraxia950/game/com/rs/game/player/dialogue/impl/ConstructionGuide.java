package com.rs.game.player.dialogue.impl;

import com.rs.game.player.Player;
import com.rs.game.player.dialogue.Dialogue;

/**
 * Handles the Construction Guide book.
 *
 * @author Noel.
 */
public class ConstructionGuide extends Dialogue {

	/**
	 * Clears the current interface.
	 *
	 * @param player
	 *            The player.
	 */
	private static void clear(Player player) {
		for (int i = 33; i < 72; i++)
			player.getPackets().sendIComponentText(960, i, "");
	}

	/**
	 * Writes a line in the book.
	 *
	 * @param player
	 *            The player.
	 * @param componentId
	 *            The lineID.
	 * @param text
	 *            The text to write.
	 */
	private static void write(Player player, int componentId, String text) {
		player.getPackets().sendIComponentText(960, componentId, text);
	}

	/**
	 * Opens the Guide Book.
	 *
	 * @param player
	 *            The player.
	 */
	public static void open(Player player) {
		player.getInterfaceManager().sendInterface(960);
		clear(player);
		// write(player, 71, "Next page");
		write(player, 69, "Guide to Construction");
		write(player, 49, "<col=A33100>How to build in your house");
		write(player, 61, "In order to build you will");
		write(player, 62, "need to turn <col=A33100>building mode");
		write(player, 54, "on. This can be done on");
		write(player, 63, "entering the house or using");
		write(player, 55, "a button on the options");
		write(player, 51, "interface. If you have a bank");
		write(player, 60, "P I N you must enter it");
		write(player, 58, "when entering building mode.");
		write(player, 50, "In building mode the ghostly");
		write(player, 57, "shapes of furniture and");
		write(player, 59, "doorways you have not built");
		write(player, 52, "yet will appear in your");
		write(player, 33, "house. These are called");
		write(player, 39, "<col=A33100>hotspots</col>. You can use these");
		write(player, 36, "to build furniture and new");
		write(player, 44, "rooms.");
		write(player, 46, "To build a piece of furniture,");
		write(player, 40, "right-click the hotspot and");
		write(player, 42, "select <col=A33100>Build</col>. You will then be");
		write(player, 34, "able to select the piece of");
		write(player, 35, "furniture you want to build");
		write(player, 38, "from the menu. Below each");
		write(player, 43, "furniture icon is a list of");
		write(player, 47, "materials; to build the");
		write(player, 45, "furniture you will need to");
		write(player, 41, "have all the materials.");
	}

	@Override
	public void start() {
		open(player);
	}

	@Override
	public void run(int interfaceId, int componentId) {
		switch (componentId) {
		case 72: /** Previous page **/
			switch (stage) {
			case -1:
				open(player);
				stage = -1;
				break;
			}
			break;
		case 73: /** Next page **/

			break;
		}
	}

	@Override
	public void finish() {
	}
}