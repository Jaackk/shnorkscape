package com.rs.game.player.content;

import com.rs.cores.CoresManager;
import com.rs.cores.FixedLengthRunnable;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.activites.gim.GIM;
import com.rs.game.activities.instances.Instance;
import com.rs.game.item.Item;
import com.rs.game.npc.NPC;
import com.rs.game.npc.gwd2.SerenReinforcement;
import com.rs.game.npc.gwd2.SliskeReinforcement;
import com.rs.game.npc.gwd2.ZamorakReinforcement;
import com.rs.game.npc.gwd2.ZarosReinforcement;
import com.rs.game.player.Player;
import com.rs.game.player.content.distinctioncape.DistinctionCape;
import com.rs.game.player.content.dropcollection.DropCollectionConstants.BOSS_DATA;
import com.rs.game.player.content.hcim_news.HcimNewsManager;
import com.rs.game.player.dialogue.impl.heartofgielinor.HeartOfGielinorInstanceD;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Colors;
import com.rs.utils.Utils;
import com.rs.utils.data.parsers.npcs.pojos.NPCDrop;
import com.rs.utils.mysql.QueryExecutor;
import com.rs.utils.mysql.impl.News;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class HeartOfGielinor implements Serializable {

	private static final long serialVersionUID = -4751687222848550844L;

	public static final int SEREN = 0, SLISKE = 1, ZAROS = 2, ZAMORAK = 3;
	public static final int[] REPUTATION_STAGES = new int[] { 100, 250, 500, 1000, 2000, 2500, 3000, 3500, 4000, 4500, 5000 };
	public static final List<NPC> REINFORCEMENTS = new ArrayList<NPC>();

	/**
	 * TODO: Reinforcements unagressive towards creator.
	 */

	public static final String[] REPUTATION = new String[] {
			"25% drop increase",
			"Champion summon",
			"A protective insignia",
			"50% drop increase",
			"Armour upgrading",
			"Invention materials",
			"Faction specific toy",
			"75% drop increase",
			"Cosmetic override",
			"50% kill count reduction",
			"100% drop increase"
	};

	public final double getDropBoost(final int god) {
		final int rep = reputation[god];
		if (rep >= 5000)
			return 2;
		else if (rep >= 3500)
			return 1.75;
		else if (rep >= 1000)
			return 1.5;
		else if (rep >= 100)
			return 1.25;
		return 1;
	}

	public static final void sendPetDrop(final Player player, final Item pet,BOSS_DATA data) {
		if (pet == null || player.hasItem(pet))
			return;
		World.sendWorldMessage(Colors.ORANGE + "<shad=000000><img=6>News: " + player.getDisplayName() + " received a " + pet.getName() + " pet drop.", false);
		player.addItem(pet);
        player.getDropCollectionHandler().handleBossKills(pet, data.getNpcId());
		HcimNewsManager.getInstance().addNews(player,"<#player> got a " + pet.getName() + " pet drop!");
				QueryExecutor.submit(new News(player, "<b><img src=\"../bin/images/news/drop.png\" width=17> " + player.getDisplayName() + " received a " + pet.getName() + " pet drop."));
	}

	public final double getDropRate(final int npcId, final NPCDrop drop) {
		if (npcId == 22460 || npcId == 22463) {
			switch (drop.getItemId()) {
			case 37009:
			case 37012:
			case 37015:
			case 37018:
			case 37070:
			case 37030:
				return drop.getRate() * getDropBoost(ZAROS);
			default:
				return drop.getRate();
			}
		} else if (npcId == 22438 || npcId == 22440) {
			switch (drop.getItemId()) {
			case 37009:
			case 37012:
			case 37015:
			case 37027:
			case 37033:
			case 37085:
				return drop.getRate() * getDropBoost(SEREN);
			default:
				return drop.getRate();
			}
		} else if (npcId == 22455 || npcId == 22453) {
			switch (drop.getItemId()) {
			case 37009:
			case 37012:
			case 37015:
			case 37090:
			case 37095:
			case 37024:
			case 37032:
				return drop.getRate() * getDropBoost(ZAMORAK);
			default:
				return drop.getRate();
			}
		} else if (npcId == 22442 || npcId == 22443) {
			switch (drop.getItemId()) {
			case 37009:
			case 37012:
			case 37015:
			case 37021:
			case 37075:
			case 37031:
				return drop.getRate() * getDropBoost(SLISKE);
			default:
				return drop.getRate();
			}
		}
		return drop.getRate();
	}

	public static final String getColour(final int god) {
		switch(god) {
		case SEREN:
			return Colors.GREEN;
		case SLISKE:
			return "<col=a7f9be>";
		case ZAROS:
			return Colors.DPURPLE;
		default:
			return Colors.RED;
		}
	}

	public static final String getGod(final int god) {
		switch(god) {
		case SEREN:
			return "Seren";
		case SLISKE:
			return "Sliske";
		case ZAROS:
			return "Zaros";
		default:
			return "Zamorak";
		}
	}

	private static final WorldTile CENTER = new WorldTile(3200, 6944, 1);

	public final void summonReinforcements(final int god, final boolean small) {
		if (!player.withinDistance(HeartOfGielinorInstanceD.CENTER, 200)) {
			player.sendMessage("You can only summon reinforcements inside the Heart.");
			return;
		}
		if (player.withinDistance(CENTER, 15)) {
			player.sendMessage("You cannot summon reinforcements on this platform.");
			return;
		}
		int count = 0;
		for (NPC n : REINFORCEMENTS) {
			if (n == null || n.isDead() || n.hasFinished() || !n.withinDistance(player, 50))
				continue;
			count++;
		}
		if (count > 25) {
			player.sendMessage("There are too many reinforcements in this region.");
			return;
		}
		player.getInventory().deleteItem(37008, small ? 100 : 250);
		setReputation(god, getReputation(god) + (small ? 5 : 15));
		NPC reinforcement = null;
		WorldTile tile = null;
		for (int i = 0; i < 50; i++) {//Not gonna use a while loop incase something glitches up and the loop's never broken.
			tile = new WorldTile(player, 3);
			if (World.canMoveNPC(tile.getPlane(), tile.getX(), tile.getY(), small ? 2 : 3))
				break;
		}
		switch(god) {
		case SEREN:
			reinforcement = new SerenReinforcement(small ? 22474 + Utils.random(3) : 22477, tile, player);
			break;
		case SLISKE:
			reinforcement = new SliskeReinforcement(small ? 22482 + Utils.random(3) : 22485, tile, player);
			break;
		case ZAROS:
			reinforcement = new ZarosReinforcement(small ? 22501 : 22502, tile, player);
			break;
		case ZAMORAK:
			reinforcement = new ZamorakReinforcement(small ? 22494 : 22495, tile, player);
			break;
		}
		REINFORCEMENTS.add(reinforcement);
		player.sendMessage("You summon a " + reinforcement.getName() + " as a reinforcement for the " + getGod(god) + " faction and gain " + (small ? "5" : "15") + " reputation.");
	}

	public static final void sendReputationInterface(Player player, int god) {
		player.getTemporaryAttributtes().put("heart", true);
		for (int i = 0; i < DistinctionCape.LINES.length; i++) {
			player.getPackets().sendIComponentText(1082, DistinctionCape.LINES[i][0], "");
			player.getPackets().sendIComponentText(1082, DistinctionCape.LINES[i][1], "");
		}
		final String COMPLETE = "<col=00ff00>Complete";
		final String INCOMPLETE = "<col=ff0000>Incomplete";
		player.getPackets().sendExecuteScript(8420, 70910101, 70910103, 70910102, 70910104, getGod(god) + " Reputation Rewards", 21218, 1007);
		player.getPackets().sendIComponentText(1082, 22, "Reward");
		player.getPackets().sendIComponentText(1082, 23, "Progress");
		for (int i = 0; i < REPUTATION.length; i++) {
			player.getPackets().sendIComponentText(1082, DistinctionCape.LINES[i][0], REPUTATION[i]);
			player.getPackets().sendIComponentText(1082, DistinctionCape.LINES[i][1], (player.getHeart().getReputation(god) >= Integer.valueOf(REPUTATION_STAGES[i]) ? COMPLETE : INCOMPLETE) + " (" +player.getHeart().getReputation(god) + "/" + REPUTATION_STAGES[i] + ")");
		}
		player.getPackets().sendIComponentText(1082, 1, "Earn reputation for " + getGod(god) + " faction through various activities, such as completing a bounty, killing a general for the first time or handing in enemy boss seals.");
		CoresManager.getServiceProvider().executeWithDelay(new Runnable() {

			@Override
			public void run() {
				player.getInterfaceManager().sendInterface(1082);
		        player.getPackets().sendExecuteScript(8420, 70910101, 70910103, 70910102, 70910104, getGod(god) + " Reputation Rewards", 21218, 1007);
			}

		}, 50, TimeUnit.MILLISECONDS);

		player.setCloseInterfacesEvent(new Runnable() {
			@Override
			public void run() {
				player.getTemporaryAttributtes().remove("heart");
			}
		});
	}

	private transient Player player;
	private final int[] killcount;
	private final int[] reputation;
	private int multiplier, currentBounty, bountyTargetFaction, bountyFaction, quota, lastReceivedQuota, activeInsignia = -1;
	private final boolean[] insigniaSettings;
	private transient int multiplierStage;

	public HeartOfGielinor() {
		killcount = new int[4];
		reputation = new int[4];
		quota = 5;
		bountyFaction = -1;
		bountyTargetFaction = -1;
		insigniaSettings = new boolean [3];
	}

	public int getActiveInsignia() {
		return activeInsignia;
	}

	public void setActiveInsignia(int insignia) {
		activeInsignia = insignia;
	}

	public boolean[] getInsigniaSettings() {
		return insigniaSettings;
	}

	public void setInsigniaSettings(int id, boolean value) {
		insigniaSettings[id] = value;
	}

	public void sendMultiplier() {
		if (multiplierStage > -13) {
			if (multiplier < 300)
				multiplier += 10;
			refreshMultiplierStatus();
			return;
		}
		player.getPackets().sendHideIComponent(945, 6, false);
		if (multiplier < 300)
			multiplier += 10;
		player.getPackets().sendGlobalString(2381, "Multiplier: " + multiplier + "%");
		player.getPackets().sendGlobalConfig(1233, 200);
		refreshMultiplierStatus();
	}

	private void refreshMultiplierStatus() {
		if (multiplierStage > -13) {
			multiplierStage = 200;
			return;
		}
		multiplierStage = 200;
		CoresManager.getServiceProvider().scheduleFixedLengthTask(new FixedLengthRunnable() {
			@Override
			public boolean repeat() {
				if (multiplierStage == -13) {
					multiplier = 100;
					player.getPackets().sendHideIComponent(945, 6, true);
					return false;
				}
				player.getPackets().sendGlobalString(2381, "Multiplier: " + multiplier + "%");
				player.getPackets().sendGlobalConfig(1233, multiplierStage--);
				return true;
			}

		}, 0, 94, TimeUnit.MILLISECONDS);
	}

	public static final void switchInterfaces(final Instance instance, final NPC npc) {
		final int damage = (int) (((double) npc.getHitpoints() / npc.getMaxHitpoints()) * 212) - 12;
		instance.getPlayers().forEach(player -> {
			if (npc.isDead()) {
				player.getPackets().sendHideIComponent(945, 6, true);
				return;
			}
			player.getPackets().sendHideIComponent(945, 6, false);
			player.getPackets().sendGlobalString(2381, getHealthBarString(instance.getBoss()));
			player.getPackets().sendGlobalConfig(1233, damage);
		});
	}

	private static final String getHealthBarString(final int bossId) {
		switch(bossId) {
		case SEREN:
			return "Helwyr's Health";
		case SLISKE:
			return "Gregorovic's Health";
		case ZAROS:
			return "Vindicta & Gorvek's Health";
		default:
			return "Twin Furies' Health";
		}
	}

	public static final void switchInterfaces(final Player player, final Instance instance, final NPC npc, final boolean send) {
		if (!send) {
			player.getPackets().sendHideIComponent(945, 6, true);
			return;
		}
		if (npc == null || npc.isDead())
			return;
		final int damage = (int) (((double) npc.getHitpoints() / npc.getMaxHitpoints()) * 212) - 12;
		player.getPackets().sendHideIComponent(945, 6, false);
		player.getPackets().sendGlobalString(2381, getHealthBarString(instance.getBoss()));
		player.getPackets().sendGlobalConfig(1233, damage);
	}

	public static final void refreshHealth(final Instance instance, final int hp, final int maxhp) {
		final int damage = (int) (((double) hp / maxhp) * 212) - 12;
		instance.getPlayers().forEach(p -> {
			if (hp > 0)
				p.getPackets().sendGlobalConfig(1233, damage);
			else
				p.getPackets().sendHideIComponent(945, 6, true);
		});
	}

	public void checkQuota() {
		Calendar calendar = Calendar.getInstance();
		final int day = calendar.get(Calendar.DATE);
		if (day != lastReceivedQuota) {
			if (quota < 5) {
				quota++;
				player.sendMessage(Colors.BROWN + "Your daily Heart of Gielinor bounty quota has been increased to " + quota + ".");
			}
			lastReceivedQuota = day;
		}
	}

	public int getQuota() {
		return quota;
	}

	public void setPlayer(Player player) {
		this.player = player;
	}

	public int getKillcount(int god) {
		return killcount[god];
	}

	public void setKillcount(int god, int amount) {
		killcount[god] = amount;
		if (killcount[god] < 0)
			killcount[god] = 0;
		refresh();
	}

	public void incrementKillcount(int god) {
		if (killcount[god] >= 200)
			return;
		killcount[god] += multiplier < 100 ? 1 : (multiplier / 100);
		if (killcount[god] > 200)
			killcount[god] = 200;
		refresh();
	}

	public int getReputation(int god) {
		return reputation[god];
	}

	public void setReputation(int god, int amount) {
		final int rep = reputation[god];
		if (rep >= 5000)
			return;
		reputation[god] = amount;
		for (int i = 0; i < REPUTATION_STAGES.length; i++) {
			if (rep < REPUTATION_STAGES[i] && reputation[god] >= REPUTATION_STAGES[i])
				player.sendMessage("Congratulations! You have unlocked a new " + getGod(god) + " reputation reward: " + REPUTATION[i] + ".");
		}
	}

	public int getMultiplier() {
		return multiplier;
	}

	public int getCurrentBounty() {
		return currentBounty;
	}

	public int getBountyTargetFaction() {
		return bountyTargetFaction;
	}

	public int getBountyFaction() {
		return bountyFaction;
	}

	public void setMultiplier(int amount) {
		multiplier = amount;
		refresh();
	}

	public void setMultiplierStage() {
		multiplierStage = -13;
	}

	public final void decrementBounty() {
		currentBounty--;
		if (currentBounty == 0)
			player.sendMessage(Colors.GREEN + "You have completed your bounty! Head back to Fang to claim your reward.");
		refresh();
	}

	public void setRandomBounty(int god) {
		int target = 0;
		for (;;) {
			if ((target = Utils.random(4)) != god)
				break;
		}
		bountyFaction = god;
		bountyTargetFaction = target;
		currentBounty = 100;
		quota--;
		refresh();
	}

	public void abandonBounty() {
		bountyFaction = -1;
		bountyTargetFaction = -1;
		currentBounty = 0;
		refresh();
	}

	public void handBountyIn() {
		setReputation(bountyFaction, getReputation(bountyFaction) + 150);
		final String faction = bountyFaction == SEREN ? "Seren" : bountyFaction == SLISKE ? "Sliske" : bountyFaction == ZAROS ? "Zaros" : "Zamorak";
		player.sendMessage("You hand in the bounty and receive 150 " + faction + " reputation.");
		abandonBounty();
	}

	public void refresh() {
		player.getPackets().sendIComponentText(601, 12, "Killcount");
		for (int i = 0; i < 4; i++)
			player.getPackets().sendIComponentText(601, 18 + i, "" + killcount[i]);
		WorldTasksManager.schedule(new WorldTask() {
			@Override
			public void run() {
				if (player.getHeart().getBountyFaction() == -1)
					player.getPackets().sendIComponentText(945, 4, "Bounty: None");
				else if (currentBounty == 0)
					player.getPackets().sendIComponentText(945, 4, "Bounty: <col=00ff00>Complete");
				else
					player.getPackets().sendIComponentText(945, 4, "Bounty: " + (100 - currentBounty) + "/100");
			}
		});
	}

}
