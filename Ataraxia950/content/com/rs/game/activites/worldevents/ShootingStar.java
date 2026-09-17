package com.rs.game.activites.worldevents;

import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.npc.others.StarSprite;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.mining.ShootingStarMining;
import com.rs.utils.Colors;
import com.rs.utils.Utils;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Handles the Shooting Star distraction and diversion.
 *
 * @author Noel
 */
public class ShootingStar {

	public static final int SHADOW = 8092, SPRITE = 8091, INVISIBLE = 1957;
	public static final int STARDUST = 13727, STAR_FALL_TIME = 18000;
	public static final int[] STAR_DUST_Q = { 30, 35, 40, 45, 50, 55, 60, 65, 70 };
	public static final int[] STAR_DUST_XP = { 14, 25, 29, 32, 47, 71, 114, 145, 210 };
	private static final String[] NO_STAR_MESSAGES = {
			"Hmm... are the stars really small, or are they just very far away?",
			"One of these stars has... little stars moving around it. Interesting...",
			"Oh no! A giant space spider is eating the moon! Oh, it was just a spider crawling across the lens.",
			"It's overcast; I can't see anything.", "My goodness... it's full of stars!" };
	public static StarSprite star;
	private static Queue<BoardEntry> noticeboard;
	private static StarLocations nextLocation;

	public static final void init() {
		noticeboard = new ConcurrentLinkedQueue<BoardEntry>();
		generateNextLocation();
		star = new StarSprite();
	}

	public static String getLocationName() {
		return nextLocation.name;
	}

	public static void generateNextLocation() {
		nextLocation = StarLocations.values()[Utils.random(StarLocations.values().length)];
	}

	public static WorldTile getNewLocation() {
		return nextLocation.location;
	}

	public static void mine(Player player, WorldObject object) {
		if (!star.isReady())
			return;
		if (!star.isFirstClick()) {
			star.setFirstClick();
			player.setFoundShootingStar();
			player.incrementStarsFound();
			int xp = player.getSkills().getLevelForXp(Skills.MINING) * 75;
			player.getSkills().addXp(Skills.MINING, xp);
			player.getDialogueManager().startDialogue("SimpleMessage", "You were the first person to find this star "
					+ "and so you receive " + xp + " mining experience.");
			player.sendMessage("You've found a Shooting Star; stars found: " + Colors.RED
					+ Utils.getFormattedNumber(player.getStarsFound()) + "</col>.");
			if (noticeboard.size() >= 5)
				noticeboard.poll();
			noticeboard.add(new BoardEntry(player.getDisplayName(), Utils.currentTimeMillis()));
			return;
		}
		player.getActionManager().setAction(new ShootingStarMining(object));
	}

	public static int getLevel() {
		return !star.isReady() ? 1 : star.getMiningLevel();
	}

	public static int getStarSize() {
		return !star.isReady() ? Integer.MAX_VALUE : star.getStarSize();
	}

	public static int getXP() {
		return !star.isReady() ? 1 : STAR_DUST_XP[star.getStarSize() - 1];
	}

	public static void reduceStarLife() {
		star.reduceStarLife();
	}

	public static void prospect(Player player) {
		if (!star.isReady())
			return;
		player.getDialogueManager().startDialogue("SimpleMessage",
				"This is a size " + star.getStarSize() + " star. " + "A Mining level of at least "
						+ star.getMiningLevel() + " is required to mine this layer. " + "It has been mined about "
						+ star.getMinedPerc() + " percent of the way to the "
						+ (star.getStarSize() == 1 ? "core" : "next layer") + ".");
	}

	public static void openNoticeboard(Player player) {
		int c = 0;
		long time = Utils.currentTimeMillis();
		player.getInterfaceManager().sendInterface(787);
		for (BoardEntry entry : noticeboard.toArray(new BoardEntry[noticeboard.size()])) {
			player.getPackets().sendIComponentText(787, 3 + c, ((time - entry.time) / 60000) + " minutes ago");
			player.getPackets().sendIComponentText(787, 8 + c, entry.name);
			c++;
		}
	}

	public static void openTelescope(Player player) {
		String message = "";
		if (star.getStarCycle() > 1000) { // 10min after one spawned
			int time = (int) ((STAR_FALL_TIME - star.getStarCycle()) * 0.6 / 60);
			message = "You see a shooting star! The star looks like it will land in " + nextLocation.name
					+ " in the next " + (time - 2) + " minutes to " + (time + 2) + " minutes.";
		} else
			message = NO_STAR_MESSAGES[Utils.random(NO_STAR_MESSAGES.length)];

		player.getDialogueManager().startDialogue("TelescopeD", message);
	}

	public enum StarLocations {

		MINING_GUILD("Mining guild", new WorldTile(3027, 3349, 0)),
		RIMMINGTON_MINING_SITE("Rimmington mine", new WorldTile(2975, 3234, 0)),
		FALADOR_MINING_SITE("Falador mine", new WorldTile(2926, 3340, 0)),
		KELDAGRIM_ENTRANCE_MINING_SITE("Keldagrim entrance", new WorldTile(2724, 3698, 0)),
		RELLEKKA_MINNING_SITE("Rellekka mine", new WorldTile(2674, 3697, 0)),
		LEGENDS_GUILD_MINNING_SITE("Legends guild", new WorldTile(2702, 3331, 0)),
		SOUTH_EAST_ARDOUGNE_MINNING_SITE("S-E Ardougne mine", new WorldTile(2610, 3220, 0)),
		COAL_TRUCKS("near coal trucks", new WorldTile(2585, 3477, 0)),
		YANNILLE_BANK("Yanille", new WorldTile(2603, 3086, 0)),
		FIGHT_ARENA_MINING_SITE("Fight Arena mine", new WorldTile(2628, 3134, 0)),
		ALKHARID_BANK("Al'kharid", new WorldTile(3285, 3181, 0)),
		ALKHARID_MINING_SITE("Al'kharid mine", new WorldTile(3297, 3297, 0)),
		DUEL_ARENA_BANK_CHEST("Duel Arena", new WorldTile(3388, 3268, 0)),
		SOUTH_EAST_VARROCK_MINING_SITE("S-E Varrock mine", new WorldTile(3293, 3352, 0)),
		LUMBRIDGE_SWAMP_TRAINING_MINING_SITE("Lumbridge mine", new WorldTile(3231, 3155, 0)),
		SOUTH_WEST_VARROCK_MINING_SITE("S-W Varrock mine", new WorldTile(3174, 3361, 0)),
		VARROCK_EAST_BANK("E Varrock Bank", new WorldTile(3257, 3408, 0)),
		GNOME_STRONGHOLD_BANK("Gnome Stronghold", new WorldTile(2449, 3436, 0)),
		NORTH_EDGEVILLE_MINING_SITE("North of Edgeville", new WorldTile(3108,3569,0)), 
		SOUTHERN_WILDERNESS_MINING_SITE("Southern Wilderness mine", new WorldTile(3019,	3594, 0)),
		WILDERNESS_VOLCANO("Wilderness volcano", new WorldTile(3188, 3690,0)),
		BANDIT_CAMP_MINING_SITE("Bandit camp mine", new WorldTile(3031, 3795, 0)), 
		LAVA_MAZE_RUNITE_MINING_SITE("Lava maze mine", new WorldTile(3059, 3888, 0)),
		PIRATES_HIDEOUT_MINING_SITE("Pirates hideout mine", new WorldTile(3048,3944,0)),
		MAGE_ARENA_BANK("Mage Arena", new WorldTile(3091,3962,0));

		private final String name;
		private final WorldTile location;

		StarLocations(String name, WorldTile location) {
			this.name = name;
			this.location = location;
		}
	}

	private static class BoardEntry {
		String name;
		long time;

		public BoardEntry(String name, long time) {
			this.name = name;
			this.time = time;
		}

	}
}