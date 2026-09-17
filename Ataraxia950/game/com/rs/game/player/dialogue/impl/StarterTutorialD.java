package com.rs.game.player.dialogue.impl;

import com.rs.Settings;
import com.rs.game.Animation;
import com.rs.game.Graphics;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import com.rs.game.player.content.interfaces.Starter.StarterInterface;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;

/**
 * Handles the Starter tutorial for new players.
 *
 * @author Noel
 */
public class StarterTutorialD extends Dialogue {

	private static final WorldTile startAnim = new WorldTile(3223, 3218, 0);
	
	/**
	 * Teleports the player.
	 *
	 * @param player
	 *            The player.
	 */
	public static void teleport(final Player player) {
		player.setNextWorldTile(Settings.START_PLAYER_LOCATION);
		player.setNextFaceWorldTile(new WorldTile(3223, 3218, 0));
		player.lock(5);
		WorldTasksManager.schedule(new WorldTask() {
			int tick;

			@Override
			public void run() {
				tick++;
				if (tick == 1) {
					player.setNextGraphics(new Graphics(3018));
					player.setNextAnimation(new Animation(16386));
				} else if (tick == 3)
					player.setNextAnimation(new Animation(16393));
				else if (tick == 4) {
					player.setNextWorldTile(Settings.START_PLAYER_LOCATION);
					player.setNextAnimation(new Animation(-1));
					//PlayerDesign.open(player);
					StarterTutorialD.addNPCHintIcon(player);
					player.setLogedIn();
					player.unlock();
					stop();
					player.setNextWorldTile(startAnim);
				}
			}
		}, 0, 1);
		
	}

	/**
	 * The starter area square coords.
	 *
	 * @param tile
	 *            The tiles.
	 * @param player
	 *            The player.
	 * @return if Inside square.
	 */
	private static boolean starterArea(WorldTile tile, Player player) {
		int destX = player.getX();
		int destY = player.getY();
		return (destX >= 3210 && destY >= 3210 && destX <= 3230 && destY <= 3230);
	}

	/**
	 * Checks the starter area.
	 *
	 * @param player
	 *            The player to check.
	 * @return if In area.
	 */
	public static boolean checkStarterArea(Player player) {
		if (!player.hasCompleted()) {
			if (!starterArea(player, player)) {
				player.setNextWorldTile(Settings.START_PLAYER_LOCATION);
				return true;
			}
		}
		return false;
	}

	/**
	 * Adds hint icon to the players map.
	 *
	 * @param player
	 *            The player.
	 */
	public static void addNPCHintIcon(Player player) {
		NPC guide = World.findNPC(player, 6139);
		if (guide != null) {
			player.getHintIconsManager().addHintIcon(guide, 0, -1, false);
			guide.faceEntity(player);
		}
		Dialogue.sendNPCDialogueNoContinue(player, 6139, Dialogue.NORMAL, "'Ey there! Come talk to me for a second.");
	}

	@Override
	public void start() {
		if (player.hasCompleted()) {
			sendNPCDialogue(6139, SAD, "These shoes will kill me...");
			Item shardBag = new Item(33262);
			if (!player.hasItem(shardBag))
				player.addItem(shardBag);
			// TODO add the actual town crier dialogue.
			stage = 99;
			return;
		}
		sendNPCDialogue(6139, NORMAL, "Hello, and Welcome to " + Settings.SERVER_NAME + "!");
	}

	@Override
	public void run(int interfaceId, int componentId) {
		switch (stage) {
		case -1:
			StarterInterface.sendInterface(player);
			end();
			break;

		}
	}

	@Override
	public void finish() {
		player.getInterfaceManager().closeChatBoxInterface();
	}


}