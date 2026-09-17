
package com.rs.game.player;

import java.io.Serializable;

import com.rs.cache.loaders.ItemDefinitions;
import com.rs.game.World;
import com.rs.game.item.Item;
import com.rs.utils.Colors;
import com.rs.utils.Utils;
import com.rs.utils.mysql.QueryExecutor;
import com.rs.utils.mysql.impl.News;

public class SquealOfFortune implements Serializable {

	private static final long serialVersionUID = -5330047553089876572L;

	private static final int RARITY_COMMON = 0;
	private static final int RARITY_UNCOMMON = 1;
	private static final int RARITY_RARE = 2;
	private static final int RARITY_JACKPOT = 3;

	private static final int SPIN_TYPE_DAILY = 0;
	private static final int SPIN_TYPE_EARNED = 1;
	private static final int SPIN_TYPE_BOUGHT = 2;

	private static final int SOF_STATUS_CLAIMINVOK = 1;
	private static final int SOF_STATUS_CLAIMINVBAD = 2;
	private static final int SOF_STATUS_CLAIMBANKOK = 3;
	private static final int SOF_STATUS_CLAIMBANKBAD = 4;
	private static final int SOF_STATUS_CLAIMPOUCHOK = 5;
	private static final int SOF_STATUS_CLAIMPOUCHBAD = 6;
	private static final int SOF_STATUS_DISABLED = 7;

	private transient Player player;

	private long lastDailySpinsGiveaway;

	private int dailySpins;
	private int earnedSpins;
	private int boughtSpins;

	private int rewardSlot;
	private int jackpotSlot;
	private Item[] rewards;

	public static final int[] SOF_JACKPOT_OTHERS = new int[] {
            995, 995, 995, 995, 962, 23682,
            23697, 23698, 34025, 23699, 23700, 30915, 
            32150, 23674, 35944, 23679, 23680, 23681, 1037, 34028,
            26567, 26568, 26569, 34785, 34786, 39558, 39557,
            35255, 35261, 35257, 35259, 40525, 
            /*
             * 28017, 
            28018, 28019, 40679, 36777, 35908, 30813, 33631, Old SoF Pets
             */
            
    };
    public static final int[] SOF_RARE_OTHERS = new int[] {
            995, 995, 995, 995, 995, 30372, 23671, 23672, 28025,
            29683, 23684, 23685, 23686, 23687, 23688, 23689, 23690,
            23692, 23693, 23694, 23675, 23676, 23677, 23678, 23673,
            24431, 37129, 34037, 30912, 30913, 30914, 30915,
            22992, 22993, 22994, 22995, 36138, 34024, 28145, 6199,
            41451, 41452, 41453, 41454, 41455, // supply boxes
            33732, 33733, 33734, // Snowboards

            
    };
 
    public static final int[] SOF_COMMON_OTHERS = new int[] {
            13666, 11732, 11126, 15271, 384, 537, 1359, 1275, 2364,
            31041, 31042, 31044, 35226, 35227, 35228,
            13958, 13961, 13964, 13967, 13970, 2510, 13979, 29301, 29305,
            29306, 29310, 31080, 31081, 29294, 29295, 29296, 29300,
            23048, 1745, 13103, 2506, 2508, 29304, 14490, 14492, 14494,
            28912, 28913, 28914, 28915, 28916, 37971, 
            28904, 18831,
    };
 
    /** Weekly SoF promotion - Dung & Thieving */
    /**
     * Added silverhawk pieces in uncommon for a temporary period of time!
     */
    public static final int[] SOF_UNCOMMON_OTHERS = new int[] {
            34210, 34211, 34212, 34213, 34214, // Burnt Shark
            34205, 34206, 34207, 34208, 34209, // Tiger Shark
            34200, 34201, 34202, 34203, 34204, // Shark
            //39730, 39731, 39732, 39733, 39734, // oaken sentinel
            //39735, 39736, 39737, 39738, 39739, // willow sentinel
            //39740, 39741, 39742, 39743, 39744, // maple sentinel
            //35968, 35969, 35970, 35971, 35972, // divination chronicle
            //35973, 35974, 35975, 35976, 35977, // divination memory
            //35963, 35964, 35965, 35966, 35967, // divination energy
            32342, 32343, 32344, 32345, 32346, // law ethereal
            32347, 32348, 32349, 32350, 32351, // blood ethereal
            32352, 32353, 32354, 32355, 32356, // death ethereal
            //31580, 31581, 31582, 31583, 31584, // emerald golem
            //31585, 31586, 31587, 31588, 31589, // ruby golem
            //31575, 31576, 31577, 31578, 31579, // sapphire golem 
            //41008, 41009, 41010, 41011, 41012, // desert trapper
            //41013, 41014, 41015, 41016, 41017, // jungle trapper
            //41018, 41019, 41020, 41021, 41022, // arctic trapper
            //37343, 37344, 37345, 37346, 37347, // keldagrim camouflage
           // 37348, 37349, 37350, 37351, 37352, // prifdinnas camouflage
           // 37353, 37354, 37355, 37356, 37357, // desert camouflage
           // 38521, 38522, 38523, 38524, 38525, // frozen gorajan trailblazer
            //38526, 38527, 38528, 38529, 38530, // furnished gorajan trailblazer
            //38531, 38532, 38533, 38534, 38535, // abandoned gorajan trailblazer
            //38536, 38537, 38538, 38539, 38540, // occult gorajan trailblazer
            6199,
            35886, 29307, 29312, 31088, 29298, 29299, 29302, 29303,
            31310, 30915, 34528, 30372, 
            4566, 9013, 7158, 22412, 
            13316, 13317, 11235, 34023, 33728, 33729, 33730
    
    };
    
    public static final int[] SOF_JACKPOT_LAMPS = new int[] { 23716, 23720, 23724, 23728, 23732, 23740, 23736, 23744,
            23748, 23752, 23756, 23760, 23764, 23768, 23773, 23781, 23777, 23789, 23785, 23797, 23793, 23805, 23801,
            23813, 23809, 23817 };
 
    public static final int[] SOF_RARE_LAMPS = new int[] { 23715, 23719, 23723, 23727, 23731, 23739, 23735, 23743,
            23747, 23751, 23755, 23759, 23763, 23767, 23771, 23780, 23776, 23788, 23784, 23796, 23792, 23804, 23800,
            23812, 23808, 23816 };
 
    public static final int[] SOF_UNCOMMON_LAMPS = new int[] { 23714, 23718, 23722, 23726, 23730, 23738, 23734, 23742,
            23746, 23750, 23754, 23758, 23762, 23766, 23770, 23779, 23775, 23787, 23783, 23795, 23791, 23803, 23799,
            23811, 23807, 23815 };
 
    public static final int[] SOF_COMMON_LAMPS = new int[] { 23714, 23718, 23722, 23726, 23730, 23738, 23734, 23742,
            23746, 23750, 23754, 23758, 23762, 23766, 23770, 23779, 23775, 23787, 23783, 23795, 23791, 23803, 23799,
            23811, 23807, 23815 };
 
    public static final int[] SOF_JACKPOT_CASH_AMOUNTS = new int[] { 50 * 1000000, 100 * 1000000, 200 * 1000000,
            500 * 1000000 };
 
    public static final int[] SOF_RARE_CASH_AMOUNTS = new int[] { 10000000, 20000000, 30000000, 40000000, 50000000 };
 
    public static final int[] SOF_UNCOMMON_CASH_AMOUNTS = new int[] { 100000, 250000, 500000, 1000000, 5000000 };
 
    public static final int[] SOF_COMMON_CASH_AMOUNTS = new int[] { 100000, 250000, 500000, 1000000, 5000000 };
	// 35% for uncommon
	// 1% for rare
	// 0.2% for jackpot

	public static final double[] SOF_CHANCES = new double[] { 
			
			//1.0D, 0.35D, 0.01D, 0.002D 
			1.0D, 0.5D, 0.02D, 0.01D
			
	};

	public SquealOfFortune() {
		rewardSlot = -1;
		jackpotSlot = -1;
	}

	public void setPlayer(Player player) {
		this.player = player;
	}

	public void processClick(int packetId, int interfaceId, int componentId) {
//		if (interfaceId == 1139) { // squeal tab
//			if (componentId == 18)
//				openSpinInterface();
//			else {
//				player.getPackets().sendOpenURL(Settings.DONATE);
//			}
//		} else if (interfaceId == 1252) { // squeal overlay
//			if (componentId == 3)
//				openSpinInterface();
//		} else if (interfaceId == 1253) { // squeal main
//			if (componentId == 106 || componentId == 258 || componentId == 2) { // hide/close
//				// button
//				player.getInterfaceManager().sendWindowPane();
//			} else if (componentId == 7 || componentId == 65) { // buy spins on
//				// main/reward
//				player.getPackets().sendOpenURL(Settings.DONATE);
//			} else if (componentId == 93 && jackpotSlot != -1 && rewardSlot == -1) { // spin
//				// button
//				pickReward(false);
//			} else if (componentId == 93 && rewardSlot != -1) {
//				player.getPackets().sendGlobalConfig(1781, -1);
//				player.getPackets().sendRunScript(5906);
//			} else if ((componentId == 192 || componentId == 239) && rewardSlot != -1) { // picking
//				// reward
//				obtainReward(componentId == 239, false);
//			} else if (componentId == 273 && jackpotSlot == -1 && getTotalSpins() > 0) { // play
//				// again
//				generateRewards(getNextSpinType());
//				player.getVarBitManager().forceSendVarBit(11155, jackpotSlot + 1);
//				player.getPackets().sendItems(665, rewards);
//				player.getVarBitManager().forceSendVarBit(10861, 0);
//				player.getPackets().sendRunScript(5879); // sof_setupHooks();
//				sendSpinCounts();
//			}
//		}
	}


	private void pickReward(boolean command) {
		if (!useSpin())
			return;
		int rewardRarity = RARITY_COMMON;
		double roll = Utils.randomDouble();
		if (roll <= SquealOfFortune.SOF_CHANCES[RARITY_JACKPOT])
			rewardRarity = RARITY_JACKPOT;
		else if (roll <= SquealOfFortune.SOF_CHANCES[RARITY_RARE])
			rewardRarity = RARITY_RARE;
		else if (roll <= SquealOfFortune.SOF_CHANCES[RARITY_UNCOMMON])
			rewardRarity = RARITY_UNCOMMON;
		int[] possibleSlots = new int[13];
		int possibleSlotsCount = 0;
		for (int i = 0; i < 13; i++) {
			if (getSlotRarity(i, jackpotSlot) == rewardRarity)
				possibleSlots[possibleSlotsCount++] = i;
		}
		rewardSlot = possibleSlots[Utils.random(possibleSlotsCount)];

		if (rewardRarity >= RARITY_JACKPOT)
			announceWin();
		int id = rewards[rewardSlot].getId();
		if (id >= 38521 && id <= 38540) 
			player.getGorajanTrailblazer().unlockOutfit(id);
		if (!command) {
			player.getPackets().sendConfigByFile(10860, rewardSlot);
			player.getPackets().sendConfigByFile(10861, 1); // block spin & set
			// reward
			player.getPackets().sendGlobalConfig(1790, getRewardStatusType());
			player.getPackets().sendGlobalConfig(1781, getBestRewardSpoofSlot());
		}
	}
	
	public void useCommand(int amount) {
		StringBuilder builder = new StringBuilder();
		builder.append("You've received the following items: ");
		for (int i = 0; i < amount; i++) {
			if (getNextSpinType() == -1) {
				player.sendMessage("You don't have enough spins remaining to do this!");
				return;
			}
			generateRewards(getNextSpinType());
			pickReward(true);
			sendSpinCounts();
			String reward = obtainReward(false, true);
			if (reward == null) {
				player.sendMessage("Was unable to obtain reward after successfully spinning: " + (i + 1) + " out of the desired " + amount + " times.");
				return;
			} else
				builder.append(reward + (i + 1 == amount ? "" : ", "));
		}
		builder.append(".");
		//player.getDialogueManager().startDialogue("SimpleMessage", builder.toString());
	}

	private String obtainReward(boolean discard, boolean command) {
		int type = getRewardStatusType();
		Item reward = rewards[rewardSlot];
		if (reward.getId() == 30372)
			reward.setAmount(Utils.random(15, 250));
		if ((discard && type == SOF_STATUS_DISABLED) || (!discard && type != SOF_STATUS_CLAIMINVOK
				&& type != SOF_STATUS_CLAIMPOUCHOK && type != SOF_STATUS_CLAIMBANKOK))
			return null;
		if (!command) {
			player.getPackets().sendConfigByFile(10861, 0);
			player.getPackets().sendGlobalConfig(1790, 0);
			player.getPackets().sendItems(665, new Item[13]);
		}
		if (!discard) {
			if (type == SOF_STATUS_CLAIMPOUCHOK && !command)
				player.getInventory().addItem(reward);
			else if (type == SOF_STATUS_CLAIMINVOK && !command)
				player.getInventory().addItem(reward);
			else if (type == SOF_STATUS_CLAIMBANKOK || command) {
				if (reward.getDefinitions().isNoted())
					reward.setId(reward.getDefinitions().getCertId());
				reward.setAmount(rewards[rewardSlot].getAmount());
				player.getBank().addItem(reward, true);
			}
		}
		rewards = null;
		jackpotSlot = -1;
		rewardSlot = -1;
		if (!command)
			openSpinInterface();
		return reward.getAmount() + " x " + reward.getName();
	}

	/**
	 * Announces a rare reward.
	 */
	private void announceWin() {
		Item item = rewards[rewardSlot];
		String message = "News: " + player.getDisplayName() + " has just won " + "x"
				+ Utils.getFormattedNumber(item.getAmount()) + " of " + item.getName() + " on Squeal of Fortune";
		World.sendWorldMessage(Colors.ORANGE + "<img=7>" + message + "!", false);

		QueryExecutor.submit(new News(player, "<b><img src=\"../bin/images/news/sof.png\" width=17> " + message + "."));
	}

	public void openSpinInterface() {

//		if (player.getInterfaceManager().containsInventoryInter()
//				|| player.getInterfaceManager().containsScreenInter()
//				|| player.getTemporaryAttributtes().get(FlowerPokerSession.PLAYING_FLOWER_POKER_KEY) == Boolean.TRUE) {
//			player.getPackets()
//					.sendGameMessage("Please finish what you are doing before opening the Squeal of Fortune.");
//			return;
//		}
//		if (player.getAttackedByDelay() + 5000 > Utils.currentTimeMillis()) {
//			player.sendMessage("You can't play the Squeal of Fortune until 5 seconds after the end of combat.");
//			return;
//		}
//		if (Wilderness.isAtWild(player)) {
//			player.getPackets().sendGameMessage("You can't play the Squeal of Fortune here.");
//			return;
//		}
//		player.stopAll();
//		sendSpinCounts();
//		if (rewardSlot != -1) {
//			openExistingReward();
//		} else if (getTotalSpins() < 1) {
//			openNoSpinsLeft();
//		} else {
//			openSpin();
//		}
	}

	private void openExistingReward() {
		player.getPackets().sendConfigByFile(11155, jackpotSlot + 1); // need to
		// send
		// all
		// items
		// because
		// otherwise
		// it
		// will
		// set
		// wrong
		// color
		// for
		// rarity
		// etc
		player.getPackets().sendItems(665, rewards);
		player.getPackets().sendWindowsPane(1253, 0);
		player.getPackets().sendConfigByFile(10860, rewardSlot);
		player.getPackets().sendConfigByFile(10861, 1); // block spin & set
		// reward
		player.getPackets().sendGlobalConfig(1790, getRewardStatusType());
		player.getPackets().sendRunScript(5906); // force call to
		// sof_displayPrize();

	}

	private void openNoSpinsLeft() {
		player.getPackets().sendConfigByFile(11155, Utils.random(13) + 1);
		player.getPackets().sendItems(665, new Item[13]);
		player.getPackets().sendWindowsPane(1253, 0);
		player.getPackets().sendConfigByFile(10861, 0);
		player.getPackets().sendGlobalConfig(1790, 0);
		player.getPackets().sendRunScript(5906); // force call to
		// sof_displayPrize();
	}

	private void openSpin() {
		if (rewards == null)
			generateRewards(getNextSpinType());
		player.getPackets().sendConfigByFile(11155, jackpotSlot + 1);
		player.getPackets().sendItems(665, rewards);
		player.getPackets().sendWindowsPane(1253, 0);
		player.getPackets().sendConfigByFile(10861, 0); // force allow spin
	}

	public void sendSpinCounts() {
//		player.getPackets().sendConfigByFile(10862, dailySpins);
//		player.getPackets().sendConfigByFile(11026, earnedSpins);
//		player.getPackets().sendGlobalConfig(1800, boughtSpins);
		// must send all three otherwise it wont trigger refresh code @ cs2
	}

	private void generateRewards(int spinType) {
		jackpotSlot = Utils.random(13);
		rewards = new Item[13];
		for (int i = 0; i < rewards.length; i++)
			rewards[i] = generateReward(spinType, getSlotRarity(i, jackpotSlot));
	}

	private Item generateReward(int spinType, int rarityType) {
		boolean isLamp = Utils.random(3) == 0; // lamp 1/3 others 2/3
		if (isLamp) {
			int[] lamps = SquealOfFortune.SOF_COMMON_LAMPS;
			if (rarityType == RARITY_JACKPOT)
				lamps = SquealOfFortune.SOF_JACKPOT_LAMPS;
			else if (rarityType == RARITY_RARE)
				lamps = SquealOfFortune.SOF_RARE_LAMPS;
			else if (rarityType == RARITY_UNCOMMON)
				lamps = SquealOfFortune.SOF_UNCOMMON_LAMPS;
			return new Item(lamps[Utils.random(lamps.length)], 1);
		} else {
			int[] items = SquealOfFortune.SOF_COMMON_OTHERS;
			if (rarityType == RARITY_JACKPOT)
				items = SquealOfFortune.SOF_JACKPOT_OTHERS;
			else if (rarityType == RARITY_RARE)
				items = SquealOfFortune.SOF_RARE_OTHERS;
			else if (rarityType == RARITY_UNCOMMON)
				items = SquealOfFortune.SOF_UNCOMMON_OTHERS;

			int itemId = items[Utils.random(items.length)];
			int amount;
			if (itemId == 995) {
				int[] amounts = SquealOfFortune.SOF_COMMON_CASH_AMOUNTS;
				if (rarityType == RARITY_JACKPOT)
					amounts = SquealOfFortune.SOF_JACKPOT_CASH_AMOUNTS;
				else if (rarityType == RARITY_RARE)
					amounts = SquealOfFortune.SOF_RARE_CASH_AMOUNTS;
				else if (rarityType == RARITY_UNCOMMON)
					amounts = SquealOfFortune.SOF_UNCOMMON_CASH_AMOUNTS;
				amount = amounts[Utils.random(amounts.length)];
			} else {
				ItemDefinitions defs = ItemDefinitions.getItemDefinitions(itemId);
				amount = rarityType > RARITY_COMMON || (!defs.isStackable() && !defs.isNoted()) ? 1
						: Utils.random(5, 50);
			}
			if(itemId == 34528) {
				amount = Utils.random(50, 150);
			}
			if (itemId == 30915 || itemId == 18831) {
				switch(rarityType) {
				case 0:
					amount = 50;
					break;
				case 1:
					amount = 100;
					break;
				case 2:
					amount = 250;
					break;
				case 3:
					amount = 500;
					break;
				}
			} else if (itemId == 31041 || itemId == 31042 || itemId == 31044 || itemId == 35226 || itemId == 35227 || itemId == 35228)
				amount = Utils.random(1, 3) * (rarityType + 1);
			return new Item(itemId, amount);
		}
	}

	private int getRewardStatusType() {
		Item reward = rewards[rewardSlot];
		if (reward.getId() == 995) { // coins go to pouch
			long amt = player.getInventory().getNumberOf(995) + reward.getAmount();
			return amt > Integer.MAX_VALUE || amt <= 0 ? SOF_STATUS_CLAIMPOUCHBAD : SOF_STATUS_CLAIMPOUCHOK;
		} else if (reward.getAmount() == 1 || reward.getDefinitions().isNoted()
				|| reward.getDefinitions().isStackable()) {
			return player.getInventory().hasFreeSlots() ? SOF_STATUS_CLAIMINVOK : SOF_STATUS_CLAIMINVBAD;
		} else { // other items go to bank
			if (player.getBank().getItemIncludingPlaceHolders(reward.getId()) != null) {
				long amt = player.getBank().getItemIncludingPlaceHolders(reward.getId()).getAmount() + reward.getAmount();
				return amt > Integer.MAX_VALUE || amt <= 0 ? SOF_STATUS_CLAIMBANKBAD : SOF_STATUS_CLAIMBANKOK;
			} else
				return player.getBank().hasBankSpace() ? SOF_STATUS_CLAIMBANKOK : SOF_STATUS_CLAIMBANKBAD;
		}
	}

	private int getBestRewardSpoofSlot() {
		int wonRarity = getSlotRarity(rewardSlot, jackpotSlot);
		if (wonRarity == RARITY_JACKPOT)
			return rewardSlot; // nothing to spoof
		int spoofMinType = wonRarity == RARITY_RARE ? RARITY_JACKPOT : RARITY_RARE;
		int bestSlot = -1;
		int bestDistance = Integer.MAX_VALUE;
		for (int i = 0; i < 13; i++) {
			if (i == rewardSlot || getSlotRarity(i, jackpotSlot) < spoofMinType)
				continue; // skip self & not rare
			int distance = distanceTo(i, rewardSlot);
			if (bestSlot == -1 || distance < bestDistance) {
				bestSlot = i;
				bestDistance = distance;
			}
		}
		return bestSlot;
	}

	private int distanceTo(int from, int to) {
		if (from == to)
			return 0;
		else if (from > to)
			return (13 - from) + to;
		else // (from < to)
			return (to - from);
	}

	private int getSlotRarity(int slot, int jackpotSlot) {
		if (slot == jackpotSlot) // jackpot overrides the slot
			return RARITY_JACKPOT;
		switch (slot) {
		case 1:
		case 3:
		case 5:
		case 7:
		case 10:
		case 12:
			return RARITY_COMMON;
		case 2:
		case 6:
		case 9:
		case 11:
			return RARITY_UNCOMMON;
		case 0:
		case 4:
		case 8:
			return RARITY_RARE;
		default: // default case added so compiler can add tableswitch
			// instruction instead of lookupswitch
			throw new RuntimeException("Bad slot");
		}
	}

	private int getNextSpinType() {
		if (dailySpins > 0)
			return SPIN_TYPE_DAILY;
		else if (earnedSpins > 0)
			return SPIN_TYPE_EARNED;
		else if (boughtSpins > 0)
			return SPIN_TYPE_BOUGHT;
		else
			return -1;
	}

	private boolean useSpin() {
		int type = getNextSpinType();
        return type != -1;
//		if (type == SPIN_TYPE_DAILY)
//			setDailySpins(dailySpins - 1);
//		else if (type == SPIN_TYPE_EARNED)
//			setEarnedSpins(earnedSpins - 1);
//		else if (type == SPIN_TYPE_BOUGHT)
//			setBoughtSpins(boughtSpins - 1);
    }
	
	public int getTotalSpins() {
	    return dailySpins + earnedSpins + boughtSpins;
	}
	
    public int getDailySpins() {
        return dailySpins;
    }

    public void setDailySpins(int dailySpins) {
        this.dailySpins = dailySpins;
    }

    public int getEarnedSpins() {
        return earnedSpins;
    }

    public void setEarnedSpins(int earnedSpins) {
        this.earnedSpins = earnedSpins;
    }

    public int getBoughtSpins() {
        return boughtSpins;
    }

    public void setBoughtSpins(int boughtSpins) {
        this.boughtSpins = boughtSpins;
    }


}
