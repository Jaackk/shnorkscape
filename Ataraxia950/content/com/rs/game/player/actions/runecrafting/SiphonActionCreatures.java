package com.rs.game.player.actions.runecrafting;

import com.rs.cache.loaders.ItemDefinitions;
import com.rs.game.Animation;
import com.rs.game.Graphics;
import com.rs.game.World;
import com.rs.game.activites.halloween.ClueScrollDistributor;
import com.rs.game.npc.NPC;
import com.rs.game.player.PerkManager.DonationPerk;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.Action;
import com.rs.game.player.content.RuneCrafting;
import com.rs.game.player.content.RunespanData;
import com.rs.game.player.controllers.RunespanController;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

import java.util.LinkedList;
import java.util.Map;
import java.util.WeakHashMap;

public class SiphonActionCreatures extends Action {

	private static final Map<Player, LinkedList<Integer>> RECENT_CHIPS = new WeakHashMap<Player, LinkedList<Integer>>();

	private final Creature creatures;
	private final NPC creature;
	private boolean started;
	private int successfulSiphons;

	public SiphonActionCreatures(Creature creatures, NPC creature) {
		this.creatures = creatures;
		this.creature = creature;
	}

	public static boolean chipCreature(Player player, NPC npc) {
		if (!(player.getControlerManager().getControler() instanceof RunespanController))
			return false;
		Creature creature = getCreature(npc.getId());
		if (creature == null)
			return false;
		int chipKey = System.identityHashCode(npc);
		if (hasRecentlyChipped(player, chipKey)) {
			player.getPackets().sendGameMessage("You need to chip three other creatures before chipping this one again.");
			return true;
		}
		boolean freeChip = player.getPerkManager().hasPerkActive(DonationPerk.IMBUED_FOCUS);
		if (!freeChip && !player.getInventory().containsItem(creature.getChippingRunes(), 10)) {
			player.sendMessage("You don't have enough " + ItemDefinitions.getItemDefinitions(creature.getChippingRunes()).getName() + "s to chip away at that creature.");
			return true;
		}
		if (!player.getInventory().containsItem(RunespanData.RUNE_ESSENCE, 1) && !player.getInventory().hasFreeSlots()) {
			player.sendMessage("Inventory full. To make more room, sell, drop or bank something.");
			return true;
		}
		npc.setNextFaceWorldTile(player);
		player.setNextAnimation(new Animation(16596));
		World.sendProjectile(player, npc, player, 3060, 31, 35, 35, 0, 2, 0);
		WorldTasksManager.schedule(new WorldTask() {
			@Override
			public void run() {
				if (!freeChip)
					player.getInventory().deleteItem(creature.getChippingRunes(), 10);
				int amount = 10 + Utils.random(11);
				player.getInventory().addItem(RunespanData.RUNE_ESSENCE, amount);
				recordChip(player, chipKey);
				if (player.getControlerManager().getControler() instanceof RunespanController)
					((RunespanController) player.getControlerManager().getControler()).refreshInventoryPoints();
				player.sendMessage("You chip " + amount + " rune essence from the creature.", true);
				ClueScrollDistributor.givePlayerClueScrollIfProbable(player, ClueScrollDistributor.SKILLING_PERCENT);
				player.setNextAnimation(new Animation(16599));
			}
		}, 1);
		return true;
	}

	private static boolean hasRecentlyChipped(Player player, int chipKey) {
		LinkedList<Integer> recent = RECENT_CHIPS.get(player);
		return recent != null && recent.contains(chipKey);
	}

	private static void recordChip(Player player, int chipKey) {
		LinkedList<Integer> recent = RECENT_CHIPS.get(player);
		if (recent == null) {
			recent = new LinkedList<Integer>();
			RECENT_CHIPS.put(player, recent);
		}
		recent.addFirst(chipKey);
		while (recent.size() > 3)
			recent.removeLast();
	}

	private static Creature getCreature(int id) {
		for (Creature creature : Creature.values())
			if (creature.npcId == id)
				return creature;
		return null;
	}

	public static boolean siphon(Player player, NPC npc) {
		if (!(player.getControlerManager().getControler() instanceof RunespanController))
			return false;
		Creature creature = getCreature(npc.getId());
		if (creature == null)
			return false;
		player.getActionManager().setAction(new SiphonActionCreatures(creature, npc));
		return true;
	}

	public boolean checkAll(final Player player) {
		if (player.isLocked() || creature.hasFinished())
			return false;
		if (!player.withinDistance(creature, 6) || !player.clipedProjectile(creature, true, creature.getDefinitions().size)) {
			player.calcFollow(creature, true);
			started = false;
			return true;
		}
		if (player.getSkills().getLevel(Skills.RUNECRAFTING) < creatures.getLevelRequired()) {
			player.getDialogueManager().startDialogue("SimpleMessage", "This creature requires level " + creatures.getLevelRequired() + " Runecrafting to siphon.");
			return false;
		}
		if (!player.getInventory().hasFreeSlots() && !player.getInventory().containsItem(creatures.getRuneId(), 1)) {
			player.sendMessage("Inventory full. To make more room, sell, drop or bank something.");
			return false;
		}
		if (!player.getInventory().containsItem(RunespanData.RUNE_ESSENCE, 1)) {
			player.sendMessage("You don't have any rune essence to siphon from that creature.", true);
			return false;
		}
		if (!started) {
			player.resetWalkSteps();
			player.setNextAnimation(new Animation(creatures.playerEmoteId));
			successfulSiphons = 0;
			started = true;
		}
		creature.resetWalkSteps();
		return true;
	}

	@Override
	public boolean process(Player player) {
		return checkAll(player);
	}

	public void processEsslingDeath(final Player player) {
		creature.setNextAnimation(new Animation(creatures.getDeathEmote()));
		WorldTasksManager.schedule(new WorldTask() {
			@Override
			public void run() {
				player.getPackets().sendGameMessage("The creature has been broken down.");
				player.getPackets().sendGameMessage("You pick up the essence left by the creature.", true);
				player.setNextAnimation(new Animation(16599));
				creature.setRespawnTask();
				player.getInventory().addItem(RunespanData.RUNE_ESSENCE, 50);
				if (player.getControlerManager().getControler() instanceof RunespanController)
					((RunespanController) player.getControlerManager().getControler()).refreshInventoryPoints();
				stop();
			}
		}, 2);
	}

	@Override
	public int processWithDelay(final Player player) {
		if (started) {
			int level = player.getSkills().getLevel(Skills.RUNECRAFTING);
			if (RuneCrafting.rollRunespanSiphonSuccess(level, creatures.getLevelRequired(), false)) {
				if (!player.getPerkManager().hasPerkActive(DonationPerk.IMBUED_FOCUS) || Utils.getRandom(3) == 1)
					successfulSiphons++;
				player.getInventory().deleteItem(RunespanData.RUNE_ESSENCE, 1);
				player.getInventory().addItem(creatures.getRuneId(), 1);
				if (player.getControlerManager().getControler() instanceof RunespanController)
					((RunespanController) player.getControlerManager().getControler()).refreshInventoryPoints();
				player.getSkills().addXp(Skills.RUNECRAFTING, creatures.xp * RuneCrafting.getRunecraftingXpModifier(player));
				player.setNextGraphics(new Graphics(3071));
			} else {
				player.getSkills().addXp(Skills.RUNECRAFTING, RuneCrafting.getRunespanFailureXp());
			}
			if (successfulSiphons >= creatures.getNpcLife()) {
				processEsslingDeath(player);
				return -1;
			}
			player.setNextAnimation(new Animation(16596));
			creature.setNextAnimation(new Animation(creatures.getNpcEmoteId()));
			creature.setNextFaceWorldTile(player);
			creature.resetWalkSteps();
			creature.lock(600 * 5);
			player.setNextFaceWorldTile(creature);
			World.sendProjectile(creature, creature, player, 3060, 31, 35, 35, 0, 2, 0);
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
	public boolean start(Player player) {
		return checkAll(player);
	}

	@Override
	public void stop(Player player) {
		player.setNextAnimation(new Animation(16599));
		setActionDelay(player, 3);
	}

	private enum Creature {
		AIR_ESSLING(15403, 9.5, 16596, RunespanData.AIR_RUNE, 16634, 5, 1, 16571, RunespanData.AIR_RUNE),
		MIND_ESSLING(15404, 10, 16596, RunespanData.MIND_RUNE, 16634, 5, 1, 16571, RunespanData.AIR_RUNE),
		WATER_ESSLING(15405, 12.6, 16596, RunespanData.WATER_RUNE, 16634, 5, 5, 16571, RunespanData.AIR_RUNE),
		EARTH_ESSLING(15406, 14.3, 16596, RunespanData.EARTH_RUNE, 16634, 5, 9, 16571, RunespanData.WATER_RUNE),
		FIRE_ESSLING(15407, 17.4, 16596, RunespanData.FIRE_RUNE, 16634, 5, 14, 16571, RunespanData.WATER_RUNE),
		BODY_ESSHOUND(15408, 23.1, 16596, RunespanData.BODY_RUNE, 16650, 5, 20, 16661, RunespanData.MIND_RUNE),
		COSMIC_ESSHOUND(15409, 26.6, 16596, RunespanData.COSMIC_RUNE, 16650, 5, 27, 16661, RunespanData.EARTH_RUNE),
		CHAOS_ESSHOUND(15410, 30.8, 16596, RunespanData.CHAOS_RUNE, 16650, 5, 35, 16661, RunespanData.FIRE_RUNE),
		ASTRAL_ESSHOUND(15411, 35.7, 16596, RunespanData.ASTRAL_RUNE, 16650, 5, 40, 16661, RunespanData.COSMIC_RUNE),
		NATURE_ESSHOUND(15412, 43.4, 16596, RunespanData.NATURE_RUNE, 16650, 5, 44, 16661, RunespanData.CHAOS_RUNE),
		LAW_ESSHOUND(15413, 53.9, 16596, RunespanData.LAW_RUNE, 16650, 5, 54, 16661, RunespanData.ASTRAL_RUNE),
		DEATH_ESSWRAITH(15414, 60, 16596, RunespanData.DEATH_RUNE, 16644, 5, 65, 16641, RunespanData.NATURE_RUNE),
		BLOOD_ESSWRAITH(15415, 73.1, 16596, RunespanData.BLOOD_RUNE, 16644, 5, 77, 16641, RunespanData.LAW_RUNE),
		SOUL_ESSWRAITH(15416, 106.5, 16596, RunespanData.SOUL_RUNE, 16644, 5, 90, 16641, RunespanData.BLOOD_RUNE);

		private final int npcId;
		private final int runeId;
		private final int playerEmoteId;
		private final int npcEmoteId;
		private final int npcLife;
		private final int levelRequired;
		private final int deathEmote;
		private final int chipRune;
		private final double xp;

		Creature(int npcId, double xp, int playerEmoteId, int runeId, int npcEmoteId, int npcLife, int levelRequired, int deathEmote, int chipRune) {
			this.npcId = npcId;
			this.xp = xp;
			this.playerEmoteId = playerEmoteId;
			this.runeId = runeId;
			this.npcEmoteId = npcEmoteId;
			this.npcLife = npcLife;
			this.levelRequired = levelRequired;
			this.deathEmote = deathEmote;
			this.chipRune = chipRune;
		}

		public int getChippingRunes() {
			return chipRune;
		}

		public int getDeathEmote() {
			return deathEmote;
		}

		public int getLevelRequired() {
			return levelRequired;
		}

		public int getNpcEmoteId() {
			return npcEmoteId;
		}

		public int getNpcLife() {
			return npcLife;
		}

		public int getRuneId() {
			return runeId;
		}
	}
}
