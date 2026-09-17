package com.rs.game.player.content.distinctioncape;

import java.util.concurrent.TimeUnit;

import com.rs.cache.loaders.ItemDefinitions;
import com.rs.cores.CoresManager;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.player.Equipment;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.content.RuneCrafting;

public class DistinctionCape {

	private static final String C = "<col=00ff00>Complete";
	private static final String IC = "<col=ff0000>Incomplete";
	public static final int[][] LINES = new int[][] { { 10, 11 }, { 12, 13 }, { 14, 15 }, { 16, 17 }, { 18, 19 }, { 20, 139 }, { 28, 29 }, { 30, 31 }, { 32, 33 }, { 34, 35 }, { 36, 37 }, { 38, 39 }, { 40, 41 }, { 43, 44 }, { 45, 46 }, { 47, 48 }, { 49, 50 }, { 51, 52 }, { 53, 54 }, { 55, 56 }, { 57, 58 }, { 136, 137 }, { 60, 61 }, { 62, 63 }, { 64, 65 }, { 66, 67 }, { 69, 70 }, { 71, 72 }, { 73, 74 }, { 75, 76 }, { 77, 138 }, { 79, 80 }, { 81, 82 }, { 83, 84 }, { 86, 87 }, { 88, 89 }, { 90, 91 }, { 92, 93 }, { 99, 100 }, { 101, 102 }, { 103, 104 }, { 105, 106 }, { 107, 108 }, { 109, 110 }, { 141, 142 }, { 143, 144 }, { 145, 146 }, { 147, 148 }, { 6, 7 }, { 8, 9 }, { 97, 111 }, { 112, 113 }, { 114, 115 }, { 116, 117 }, 
	    { 118, 119 }, { 120, 140 }, { 4, 5 }, { 121, 122 }, { 123, 124 }, { 125, 126 }, { 128, 129 }, { 130, 131 }, { 132, 133 }, { 134, 135 } };

	private final Player player;
	private int requirementsLeft, lineId;
	
	public DistinctionCape(Player player) {
		this.player = player;
	}
	
	private final void clearInterface(Player player) {
		lineId = 0;
		for (int i = 0; i < LINES.length; i++)
			sendString(player, "", "", true);
		lineId = 0;
	}

	public final void sendMaxInterface() {
//		requirementsLeft = 0;
//		clearInterface(player);
//		player.getPackets().sendHideIComponent(1082, 159, true);
//		player.getPackets().sendIComponentText(1082, 159, "Max Cape Requirements");
//		player.getPackets().sendIComponentText(1082, 41, "Requirement");
//		player.getPackets().sendIComponentText(1082, 42, "Progress");
//		for (int i = 0; i < Skills.SKILL_NAME.length - 1; i++)  
//			sendString(player, Skills.SKILL_NAME[i], player.getSkills().getLevelForXp(i) >= 99 ? C + " (" + player.getSkills().getLevelForXp(i) + "/99)": IC + " (" + player.getSkills().getLevelForXp(i) + "/99)", player.getSkills().getLevelForXp(i) >= 99);
//		player.getPackets().sendIComponentText(1082, 11, (isMaxed(player) ? "You have no requirements left for Max Cape.<br><col=1BD12A><shad=000000>You are worthy enough to claim this cape!</col>" : "You have " + requirementsLeft + " requirement" + (requirementsLeft == 1 ? "" : "s") + " left for Max Cape.<br><col=ff0000><shad=000000>You are not worthy enough to claim this cape!"));
//		openInterface(player);
	}
	
	private final void sendString(Player player, String task, String completion, boolean progress) {
//		int[] components = LINES[lineId++];
//		player.getPackets().sendIComponentText(1082, components[0], task);
//		player.getPackets().sendIComponentText(1082, components[1], completion);
//		if (!progress)
//			requirementsLeft++;
	}
	
	public final void sendCompletionistInterface() {
//		requirementsLeft = 0;
//		clearInterface(player);
//		player.getPackets().sendHideIComponent(1082, 159, true);
//		player.getPackets().sendIComponentText(1082, 159, "Completionist's Cape Requirements");
//		player.getPackets().sendIComponentText(1082, 41, "Requirement");
//		player.getPackets().sendIComponentText(1082, 42, "Progress");
//		sendString(player, "Unlock Max Cape", isMaxed(player) ? C : IC, isMaxed(player));
//		sendString(player, "Slayer", player.getSkills().getLevelForXp(Skills.SLAYER) >= 120 ? C + " (" + player.getSkills().getLevelForXp(Skills.SLAYER) + "/120)": IC + " (" + player.getSkills().getLevelForXp(Skills.SLAYER) + "/120)", player.getSkills().getLevelForXp(Skills.SLAYER) >= 120);
//		sendString(player, "Dungeoneering", player.getSkills().getLevelForXp(Skills.DUNGEONEERING) >= 120 ? C + " (" + player.getSkills().getLevelForXp(Skills.DUNGEONEERING) + "/120)": IC + " (" + player.getSkills().getLevelForXp(Skills.DUNGEONEERING) + "/120)", player.getSkills().getLevelForXp(Skills.DUNGEONEERING) >= 120);
//        sendString(player, "Invention", player.getSkills().getLevelForXp(Skills.INVENTION) >= 120 ? C + " (" + player.getSkills().getLevelForXp(Skills.INVENTION) + "/120)": IC + " (" + player.getSkills().getLevelForXp(Skills.INVENTION) + "/120)", player.getSkills().getLevelForXp(Skills.INVENTION) >= 120);
//		sendString(player, "Defeat the Queen Black Dragon", player.isKilledQueenBlackDragon() ? C : IC, player.isKilledQueenBlackDragon());
//		sendString(player, "Complete Fight Caves", player.isCompletedFightCaves() ? C : IC, player.isCompletedFightCaves());
//		sendString(player, "Defeat the Culinaromancer", player.isKilledCulinaromancer() ? C : IC, player.isKilledCulinaromancer());
//		sendString(player, "Complete Fight Kiln", player.isCompletedFightKiln() ? C : IC, player.isCompletedFightKiln());
//		sendString(player, "Finish all easy and medium tasks", player.getAchievements().hasCompletionistTasksDone() ? C : IC, player.getAchievements().hasCompletionistTasksDone());
//		//boolean allQuestsComplete = player.quests.isAllCompleted();
//		//sendString(player, "Complete all quests", allQuestsComplete ? C : IC, allQuestsComplete);
//		player.getPackets().sendIComponentText(1082, 11, (isWorthyCompCape(player) ? "You have no requirements left for Completionist's Cape.<br><col=1BD12A><shad=000000>You are worthy enough to claim this cape!</col>" : "You have " + requirementsLeft + " requirement" + (requirementsLeft == 1 ? "" : "s") + " left for Completionist's Cape.<br><col=ff0000><shad=000000>You are not worthy enough to claim this cape!"));
//		openInterface(player);
	}
	
	private final String getCompletionString(int current, int total) {
		String prefix = current >= total ? C : IC;
		return prefix + " (" + current + "/" + total + ")";
	}
	
	public final void sendTrimmedCompletionistInterface() {
//		requirementsLeft = 0;
//		clearInterface(player);
//		player.getPackets().sendHideIComponent(1082, 159, true);
//		player.getPackets().sendIComponentText(1082, 159, "Trimmed Completionist's Cape Requirements");
//		player.getPackets().sendIComponentText(1082, 41, "Requirement");
//		player.getPackets().sendIComponentText(1082, 42, "Progress");
//		sendString(player, "Unlock Completionist's Cape", isWorthyCompCape(player) ? C : IC, isWorthyCompCape(player));
//		sendString(player, "Mine ores", getCompletionString(player.getOresMined(), 5000), player.getOresMined() >= 5000);
//		sendString(player, "Smelt bars", getCompletionString(player.getSmithingActions(), 5000), player.getSmithingActions() >= 5000);
//		sendString(player, "Chop logs", getCompletionString(player.getLogsChopped(), 5000), player.getLogsChopped() >= 5000);
//		sendString(player, "Burn logs", getCompletionString(player.getLogsBurned(), 5000), player.getLogsBurned() >= 5000);
//		sendString(player, "Sacrifice bones", getCompletionString(player.getBonesOffered(), 5000), player.getBonesOffered() >= 5000);
//		sendString(player, "Create potions", getCompletionString(player.getPotionsMade(), 5000), player.getPotionsMade() >= 5000);
//		sendString(player, "Steal", getCompletionString(player.getTimesStolen(), 5000), player.getTimesStolen() >= 5000);
//		sendString(player, "Craft objects", getCompletionString(player.getItemsMade(), 5000), player.getItemsMade() >= 5000);
//		sendString(player, "Fletch objects", getCompletionString(player.getItemsFletched(), 5000), player.getItemsFletched() >= 5000);
//		sendString(player, "Catch creatures", getCompletionString(player.getCreaturesCaught(), 5000), player.getCreaturesCaught() >= 5000);
//		sendString(player, "Catch fish", getCompletionString(player.getFishCaught(), 5000), player.getFishCaught() >= 5000);
//		sendString(player, "Cook food", getCompletionString(player.getFoodCooked(), 5000), player.getFoodCooked() >= 5000);
//		sendString(player, "Harvest products", getCompletionString(player.getProduceGathered(), 5000), player.getProduceGathered() >= 5000);
//		sendString(player, "Infuse pouches", getCompletionString(player.getPouchesMade(), 2500), player.getPouchesMade() >= 2500);
//		sendString(player, "Run laps", getCompletionString(player.getLapsRan(), 1000), player.getLapsRan() >= 1000);
//		sendString(player, "Collect memories", getCompletionString(player.getMemoriesCollected(), 5000), player.getMemoriesCollected() >= 5000);
//		sendString(player, "Craft runes", getCompletionString(player.getRunesMade(), 5000), player.getRunesMade() >= 5000);
//		sendString(player, "Reach Ports score", getCompletionString(player.getPorts().portScore, 1000), player.getPorts().portScore >= 1000);
//		sendString(player, "Slay Demon Flash Mobs", getCompletionString(player.demonFlashMobsKills, 5), player.demonFlashMobsKills >= 5);
//		//sendString(player, "Unlock Rigour prayer", player.hasRigourActivated() ? C : IC, player.hasRigourActivated());
//		//sendString(player, "Unlock Augury prayer", player.hasAuguryActivated() ? C : IC, player.hasAuguryActivated());
//		//sendString(player, "Unlock all Meilyr recipes", player.unlockedAllRecipes() ? C : IC, player.unlockedAllRecipes());
//		//sendString(player, "Win Soul Wars games", getCompletionString(player.getSoulWarsWins(), 10), player.getSoulWarsWins() >= 10);
//		sendString(player, "Win Pest Control games", getCompletionString(player.getPestControlGames(), 25), player.getPestControlGames() >= 25);
//		sendString(player, "Complete Slayer tasks", getCompletionString(player.getSlayerTasks(), 100), player.getSlayerTasks() >= 100);
//		sendString(player, "Complete Reaper tasks", getCompletionString(player.getTotalContract(), 50), player.getTotalContract() >= 50);
//		sendString(player, "Unlock Barrows chests", getCompletionString(player.getBarrowsRunsDone(), 100), player.getBarrowsRunsDone() >= 100);
//		sendString(player, "Upgrade to a T70 defender", player.hasUpgradedBarrowsDefender() ? C : IC, player.hasUpgradedBarrowsDefender());
//		//sendString(player, "Reach floor 50 in Daemonheim", player.hasReachedFloor50() ? C : IC, player.hasReachedFloor50());
//		sendString(player, "Defeat bosses", getCompletionString(player.getBossKillcount(), 1000), player.getBossKillcount() >= 1000);
//		sendString(player, "Slay WildyWyrms", player.getKillStatistics(111) >= 5 ? C + " (" + player.getKillStatistics(111) + "/5)" : IC + " (" + player.getKillStatistics(111) + "/5)", player.getKillStatistics(111) >= 5);
//		sendString(player, "Obtain an enhanced fire cape", player.hasUnlockedEFC() ? C : IC, player.hasUnlockedEFC());
//		sendString(player, "Vote for Ataraxia 50 times", getCompletionString(player.getVotes(), 50), player.getVotes() >= 50);
//		sendString(player, "Finish all achievement tasks", player.getAchievements().hasCompletionistTrimmedTasksDone() && player.getAchievements().hasCompletionistTasksDone() ? C : IC, player.getAchievements().hasCompletionistTrimmedTasksDone() && player.getAchievements().hasCompletionistTasksDone());
//		player.getPackets().sendIComponentText(1082, 11, (isWorthyCompCapeT(player) ? "You have no requirements left for Trimmed Completionist's Cape.<br><col=1BD12A><shad=000000>You are worthy enough to claim this cape!</col>" : "You have " + requirementsLeft + " requirement" + (requirementsLeft == 1 ? "" : "s") + " left for Trimmed Completionist's Cape.<br><col=ff0000><shad=000000>You are not worthy enough to claim this cape!"));
//		openInterface(player);
	}
	
	private static final void openInterface(Player player) {
		player.getTemporaryAttributtes().put("distinctionreq", true);
		player.setCloseInterfacesEvent(new Runnable() {
			@Override
			public void run() {
				player.getTemporaryAttributtes().remove("distinctionreq");
			}
		});
		CoresManager.getServiceProvider().executeWithDelay(new Runnable() {
			@Override
			public void run() {
				if (player != null)
					player.getInterfaceManager().sendInterface(1082);
			}
		}, 50, TimeUnit.MILLISECONDS);
	}
	

	public static final boolean isWorthyCompCape(Player player) {
		if (player.isOwner()) {
			return true;
		}
		return (isMaxed(player) && player.getSkills().getLevelForXp(Skills.DUNGEONEERING) == 120
		        && player.getSkills().getLevelForXp(Skills.SLAYER) == 120
		        && player.getSkills().getLevelForXp(Skills.INVENTION) == 120
				&& player.isKilledQueenBlackDragon() && player.isCompletedFightCaves()
				&& player.isKilledCulinaromancer()
				&& player.isCompletedFightKiln() && player.getAchievements().hasCompletionistTasksDone());
	}
	public static final boolean isWorthyCompCapeT(Player player) {
		if (player.isOwner()) {
			return true;
		}
		return (isWorthyCompCape(player)
				&& player.getOresMined() >= 5000
				&& player.getSmithingActions() >= 5000
				&& player.getLogsChopped() >= 5000
				&& player.getLogsBurned() >= 5000
				&& player.getBonesOffered() >= 5000
				&& player.getPotionsMade() >= 5000
				&& player.getTimesStolen() >= 5000
				&& player.getItemsMade() >= 5000
				&& player.getItemsFletched() >= 5000
				&& player.getCreaturesCaught() >= 5000
				&& player.getFishCaught() >= 5000
				&& player.getFoodCooked() >= 5000
				&& player.getProduceGathered() >= 5000
				&& player.getPouchesMade() >= 2500
				&& player.getLapsRan() >= 1000
				&& player.getMemoriesCollected() >= 5000
				&& player.getRunesMade() >= 5000 * RuneCrafting.BASE_RC_MULTIPLIER
				&& player.demonFlashMobsKills >= 5
				&& player.getPorts().portScore >= 1000)
				//&& player.getSoulWarsWins() >= 10
				&& player.getPestControlGames() >= 25
				&& player.getSlayerTasks() >= 100
				&& player.getTotalContract() >= 50
				&& player.getBarrowsRunsDone() >= 100
				&& player.hasUpgradedBarrowsDefender()
				//&& player.hasReachedFloor50()
				&& player.getBossKillcount() >= 1000
		       // && player.unlockedAllRecipes()
		        && player.getKillStatistics(111) >= 5
		        && player.hasUnlockedEFC()
				&& player.getAchievements().hasCompletionistTrimmedTasksDone();
				//&& player.getTotalTrivia() >= 50;
	}
	
	public static final boolean isMaxed(Player player) {
		return skillsBelowCap(player) == 0;
	}

	/**
	 * How many of the 947 cache's stats are still below their own level cap.
	 *
	 * <p>This used to be a hand-written list of 27 {@code >= 99} checks. The 947 cache
	 * caps 20 of the 29 stats above 99 (110 for Woodcutting, Fletching, Firemaking,
	 * Crafting, Smithing, Mining, Runecrafting and Hunter; 120 for Attack, Strength,
	 * Ranged, Magic, Herblore, Thieving, Slayer, Farming, Dungeoneering, Invention,
	 * Archaeology and Necromancy), so "maxed" now means every stat at ITS cap - which is
	 * what {@link Skills#getLevelCap(int)} answers, straight out of the stat definitions
	 * in cache index 28 group 9. Archaeology and Necromancy count like any other skill.
	 */
	public static final int skillsBelowCap(Player player) {
		int remaining = 0;
		for (int skill = 0; skill < Skills.SKILL_COUNT; skill++)
			if (!isAtCap(player, skill))
				remaining++;
		return remaining;
	}

	/** True when this one stat has reached the cap its cache stat definition declares. */
	public static final boolean isAtCap(Player player, int skill) {
		return player.getSkills().getLevelForXp(skill) >= Skills.getLevelCap(skill);
	}
	
	public static final boolean canWear(Player player, Item item) {
		if (player.isDev())
			return true;
		if (player.isOwner())
			return true;
		player.setMax(isMaxed(player));
		player.setComp(isWorthyCompCape(player));
		player.setCompT(isWorthyCompCapeT(player));
		String check = item.getName().toLowerCase();
		if (check.contains("max cape") || check.contains("max hood")) {
			if(!player.isMax() && isMaxed(player)) {
				player.setMax(true);
			}
			return player.isMax();
		} else if (check.contains("(t)") && check.contains("completionist"))
            return player.isCompT() && DistinctionCape.isWorthyCompCapeT(player);
		else if (check.contains("completionist cape") || check.contains("completionist hood"))
			return player.isComp() && DistinctionCape.isWorthyCompCape(player);
		else if (check.contains("master quest cape"))
			return player.getAchievements().hasCompletionistTasksDone() && player.getAchievements().hasCompletionistTrimmedTasksDone();
		return false;
	}
	
	public static final void checkRequirementsUponLogin(Player player) {
		if (player.getEquipment().getCapeId() != -1) {
			switch (player.getEquipment().getCapeId()) {
			case 20747:
			case 20767:
			case 32151:
			case 20748:
			case 20749:
			case 20769:
			case 20771:
			case 32152:
			case 32153:
			case 36166:
			case 36167:
				if (!canWear(player, new Item(player.getEquipment().getCapeId(), 1))) {
					int capeId = player.getEquipment().getCapeId();
					player.getEquipment().set(Equipment.SLOT_CAPE, null);
					if (player.getInventory().hasFreeSlots()) {
						player.sendMessage("The " + ItemDefinitions.getItemDefinitions(capeId).getName() + " has been added to your inventory due to requirements not being met.");
						player.getInventory().addItem(new Item(capeId, 1));
					} else if (player.getBank().hasBankSpace()) {
						player.sendMessage("The " + ItemDefinitions.getItemDefinitions(capeId).getName() + " has been added to your bank due to requirements not being met.");
						player.getBank().addItem(new Item(capeId, 1), true);
					} else {
						player.sendMessage("The " + ItemDefinitions.getItemDefinitions(capeId).getName() + " has been placed underneath you due to requirements not being met.");
						World.addGroundItem(new Item(capeId, 1), new WorldTile(player), player, true, 180);
					}
				}
				break;
			}
		}
		if (player.getEquipment().getHatId() != -1) {
			switch (player.getEquipment().getCapeId()) {
			case 20752:
			case 20753:
			case 20770:
			case 20751:
			case 20768:
				if (!canWear(player, new Item(player.getEquipment().getHatId(), 1))) {
					int hatId = player.getEquipment().getHatId();
					player.getEquipment().set(Equipment.SLOT_HAT, null);
					if (player.getInventory().hasFreeSlots()) {
						player.sendMessage("The " + ItemDefinitions.getItemDefinitions(hatId).getName() + " has been added to your inventory due to requirements not being met.");
						player.getInventory().addItem(new Item(hatId, 1));
					} else if (player.getBank().hasBankSpace()) {
						player.sendMessage("The " + ItemDefinitions.getItemDefinitions(hatId).getName() + " has been added to your bank due to requirements not being met.");
						player.getBank().addItem(new Item(hatId, 1), true);
					} else {
						player.sendMessage("The " + ItemDefinitions.getItemDefinitions(hatId).getName() + " has been placed underneath you due to requirements not being met.");
						World.addGroundItem(new Item(hatId, 1), new WorldTile(player), player, true, 180);
					}
				}
				break;
			}
		}
	}
}