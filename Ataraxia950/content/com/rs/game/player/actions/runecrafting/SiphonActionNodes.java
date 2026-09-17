package com.rs.game.player.actions.runecrafting;

import com.rs.game.Animation;
import com.rs.game.Graphics;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.activites.halloween.ClueScrollDistributor;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.Action;
import com.rs.game.player.content.RuneCrafting;
import com.rs.game.player.content.RunespanData;
import com.rs.game.player.controllers.RunespanController;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Logger;
import com.rs.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class SiphonActionNodes extends Action {

	private static final int NODE_TYPE = 10;
	private static final WorldTile[][] NODE_TILES = {
			{ new WorldTile(3919, 6096, 1), new WorldTile(3912, 6083, 1), new WorldTile(3925, 6058, 1), new WorldTile(3917, 6042, 1), new WorldTile(3942, 6043, 1), new WorldTile(3963, 6040, 1), new WorldTile(3967, 6057, 1), new WorldTile(3995, 6052, 1), new WorldTile(4026, 6038, 1), new WorldTile(4018, 6089, 1), new WorldTile(4337, 6076, 1), new WorldTile(4327, 6067, 1), new WorldTile(4338, 6062, 1), new WorldTile(4359, 6117, 1), new WorldTile(4379, 6058, 1), new WorldTile(4385, 6089, 1), new WorldTile(4322, 6091, 1), new WorldTile(4410, 6073, 1) },
			{ new WorldTile(3933, 6122, 1), new WorldTile(3981, 6139, 1), new WorldTile(3958, 6140, 1), new WorldTile(3931, 6139, 1), new WorldTile(3919, 6133, 1), new WorldTile(3953, 6066, 1), new WorldTile(3982, 6064, 1), new WorldTile(4018, 6071, 1), new WorldTile(4136, 6136, 1), new WorldTile(4150, 6134, 1), new WorldTile(4213, 6047, 1), new WorldTile(4190, 6027, 1), new WorldTile(4161, 6027, 1), new WorldTile(4165, 6027, 1), new WorldTile(4149, 6017, 1), new WorldTile(4133, 6022, 1), new WorldTile(4187, 6122, 1), new WorldTile(4172, 6137, 1), new WorldTile(4139, 6035, 1), new WorldTile(4141, 6082, 1), new WorldTile(4302, 6060, 1), new WorldTile(4330, 6044, 1), new WorldTile(4333, 6031, 1), new WorldTile(4359, 6028, 1) }
	};

	private final Nodes nodes;
	private final WorldObject node;
	private boolean started;

	public SiphonActionNodes(Nodes nodes, WorldObject node) {
		this.nodes = nodes;
		this.node = node;
	}

	public static boolean siphion(Player player, WorldObject object) {
		if (!(player.getControlerManager().getControler() instanceof RunespanController))
			return false;
		Nodes node = getNode(object.getId());
		if (node == null)
			return false;
		player.getActionManager().setAction(new SiphonActionNodes(node, object));
		return true;
	}

	public static void init() {
		for (WorldTile[] tiles : NODE_TILES) {
			for (WorldTile tile : tiles)
				spawnRandomNode(tile, Utils.random(20));
		}
		Logger.getGlobal().info("Initiated RuneSpan node spawns.");
	}

	private static void spawnRandomNode(final WorldTile tile, int delay) {
		WorldTasksManager.schedule(new WorldTask() {
			@Override
			public void run() {
				WorldObject existing = World.getObjectWithType(tile, NODE_TYPE);
				if (existing != null && getNode(existing.getId()) != null)
					World.removeObject(existing, true);
				final Nodes node = getRandomNodeForTile(tile);
				final WorldObject object = new WorldObject(node.getObjectId(), NODE_TYPE, 0, tile.getX(), tile.getY(), tile.getPlane());
				World.spawnObject(object);
				WorldTasksManager.schedule(new WorldTask() {
					@Override
					public void run() {
						WorldObject current = World.getObjectWithType(tile, NODE_TYPE);
						if (current != null && current.getId() == object.getId())
							World.removeObject(current, true);
						spawnRandomNode(tile, 25 + Utils.random(35));
					}
				}, node.getLifeTicks());
			}
		}, delay);
	}

	private static Nodes getRandomNodeForTile(WorldTile tile) {
		int floor = getFloor(tile);
		List<Nodes> possible = new ArrayList<Nodes>();
		for (Nodes node : Nodes.values()) {
			if (floor == 1 && node.getLevelRequired() <= 17)
				possible.add(node);
			else if (floor == 2 && node.getLevelRequired() <= 54)
				possible.add(node);
			else if (floor == 3)
				possible.add(node);
		}
		if (possible.isEmpty())
			return Nodes.CYCLONE;
		return possible.get(Utils.random(possible.size()));
	}

	private static int getFloor(WorldTile tile) {
		if (tile.getX() > 4280)
			return 3;
		if (tile.getX() > 4090)
			return 2;
		return 1;
	}

	private static Nodes getNode(int id) {
		for (Nodes node : Nodes.values())
			if (node.objectId == id)
				return node;
		return null;
	}

	@Override
	public boolean start(Player player) {
		return checkAll(player);
	}

	public boolean checkAll(final Player player) {
		if (!(player.getControlerManager().getControler() instanceof RunespanController))
			return false;
		WorldObject current = World.getObjectWithType(node, node.getType());
		if (current == null || current.getId() != node.getId()) {
			player.getPackets().sendGameMessage("That node has faded from the Runespan.");
			return false;
		}
		if (player.getSkills().getLevel(Skills.RUNECRAFTING) < nodes.getLevelRequired()) {
			player.getDialogueManager().startDialogue("SimpleMessage", "You need a Runecrafting level of " + nodes.getLevelRequired() + " to siphon from that node.");
			return false;
		}
		if (!started && (!player.withinDistance(node, 6) || !player.clipedProjectile(node, true))) {
			player.calcFollow(node, true);
			return true;
		}
		if (!player.getInventory().containsItem(RunespanData.RUNE_ESSENCE, 1)) {
			if (!player.getInventory().hasFreeSlots())
				player.sendMessage("Inventory full. To make more room, sell, drop or bank something.");
			else
				player.getPackets().sendGameMessage("You don't have any rune essence to siphon from that node.", true);
			return false;
		}
		if (!started) {
			player.resetWalkSteps();
			player.setNextAnimation(new Animation(16596));
			started = true;
		}
		return true;
	}

	@Override
	public boolean process(Player player) {
		return checkAll(player);
	}

	@Override
	public int processWithDelay(final Player player) {
		if (started) {
			int level = player.getSkills().getLevel(Skills.RUNECRAFTING);
			if (RuneCrafting.rollRunespanSiphonSuccess(level, nodes.getLevelRequired(), true)) {
				Reward reward = nodes.rollReward();
				player.getInventory().deleteItem(RunespanData.RUNE_ESSENCE, 1);
				player.getInventory().addItem(reward.runeId, 1);
				if (player.getControlerManager().getControler() instanceof RunespanController)
					((RunespanController) player.getControlerManager().getControler()).refreshInventoryPoints();
				player.getSkills().addXp(Skills.RUNECRAFTING, reward.xp * RuneCrafting.getRunecraftingXpModifier(player));
				player.setNextGraphics(new Graphics(3071));
			} else {
				player.getSkills().addXp(Skills.RUNECRAFTING, RuneCrafting.getRunespanFailureXp());
			}
			ClueScrollDistributor.givePlayerClueScrollIfProbable(player, ClueScrollDistributor.SKILLING_PERCENT);
			player.setNextAnimation(new Animation(nodes.getEmoteId()));
			player.setNextFaceWorldTile(node);
			World.sendProjectile(node, node, player, 3060, 31, 35, 35, 0, 2, 0);
			WorldTasksManager.schedule(new WorldTask() {
				@Override
				public void run() {
					player.setNextGraphics(new Graphics(3062));
				}
			}, 1);
		}
		return 3;
	}

	@Override
	public void stop(Player player) {
		player.setNextAnimation(new Animation(16599));
		setActionDelay(player, 3);
	}

	private static final class Reward {
		private final int runeId;
		private final double xp;

		private Reward(int runeId, double xp) {
			this.runeId = runeId;
			this.xp = xp;
		}
	}

	public enum Nodes {
		CYCLONE(70455, 16596, 1, reward(RunespanData.AIR_RUNE, 19)),
		MIND_STORM(70456, 16596, 1, reward(RunespanData.MIND_RUNE, 20)),
		WATER_POOL(70457, 16596, 5, reward(RunespanData.WATER_RUNE, 25.3)),
		ROCK_FRAGMENT(70458, 16596, 9, reward(RunespanData.EARTH_RUNE, 28.6)),
		FIRE_BALL(70459, 16596, 14, reward(RunespanData.FIRE_RUNE, 34.8)),
		VINE(70460, 16596, 17, reward(RunespanData.WATER_RUNE, 30.3), reward(RunespanData.EARTH_RUNE, 34.3)),
		FLESHLY_GROWTH(70461, 16596, 20, reward(RunespanData.BODY_RUNE, 46.2)),
		FIRE_STORM(70462, 16596, 27, reward(RunespanData.AIR_RUNE, 22.8), reward(RunespanData.FIRE_RUNE, 41.7)),
		CHAOTIC_CLOUD(70463, 16596, 35, reward(RunespanData.CHAOS_RUNE, 61.6)),
		NEBULA(70464, 16596, 40, reward(RunespanData.COSMIC_RUNE, 63.8), reward(RunespanData.ASTRAL_RUNE, 85.6)),
		SHIFTER(70465, 16596, 44, reward(RunespanData.NATURE_RUNE, 86.8)),
		JUMPER(70466, 16596, 54, reward(RunespanData.LAW_RUNE, 107.8)),
		SKULLS(70467, 16596, 65, reward(RunespanData.DEATH_RUNE, 120)),
		BLOOD_POOL(70468, 16596, 77, reward(RunespanData.BLOOD_RUNE, 146.3)),
		BLOODY_SKULLS(70469, 16596, 83, reward(RunespanData.DEATH_RUNE, 144), reward(RunespanData.BLOOD_RUNE, 175.5)),
		LIVING_SOUL(70470, 16596, 90, reward(RunespanData.SOUL_RUNE, 213)),
		UNDEAD_SOUL(70471, 16596, 95, reward(RunespanData.DEATH_RUNE, 144), reward(RunespanData.SOUL_RUNE, 255.6));

		private final int objectId;
		private final int emoteId;
		private final int levelRequired;
		private final Reward[] rewards;

		Nodes(int objectId, int emoteId, int levelRequired, Reward... rewards) {
			this.objectId = objectId;
			this.emoteId = emoteId;
			this.levelRequired = levelRequired;
			this.rewards = rewards;
		}

		private static Reward reward(int runeId, double xp) {
			return new Reward(runeId, xp);
		}

		public int getObjectId() {
			return objectId;
		}

		public int getEmoteId() {
			return emoteId;
		}

		public int getLevelRequired() {
			return levelRequired;
		}

		private Reward rollReward() {
			return rewards[Utils.random(rewards.length)];
		}

		private int getLifeTicks() {
			return 200 + Utils.random(300);
		}
	}
}
