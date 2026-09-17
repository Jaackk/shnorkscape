package com.rs.game.player.content.crystaltriskellion;

import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.activites.halloween.ClueScrollDistributor;
import com.rs.game.item.Item;
import com.rs.game.player.PerkManager.DonationPerk;
import com.rs.game.player.Player;
import com.rs.utils.Utils;

public class CrystalTriskelion {
	
	public static final int COMMON = 1, UNCOMMON = 2, RARE = 3, VERY_RARE = 4;
	
	/**
	 * @param location - if location is null, it will add the piece to inventory.
	 */
	public static void sendTriskelionDrop(Player receiver, int npcId, WorldTile location) {
		x : for (TriskelionNPCs triskelion : TriskelionNPCs.values()) {
			for (int ids : triskelion.getNPCs()) {
				if (npcId == ids) {
					boolean drop = triskelion.getRarity() == COMMON ? Utils.random(100) <= 40 : triskelion.getRarity() == UNCOMMON ? Utils.random(100) <= 25 : triskelion.getRarity() == RARE ? Utils.random(100) <= 10 : Utils.random(100) <= 5;
					if (drop) {
						Item loot = new Item(28547 + Utils.random(3), 1);
						receiver.sendMessage("You find a " + loot.getName() + ".");
						if (location == null || receiver.getPerkManager().hasPerkActive(DonationPerk.DROP_CATCHER)) {
							if (receiver.getInventory().hasFreeSlots())
								receiver.getInventory().addItem(loot);
							else
								World.addGroundItem(loot, new WorldTile(receiver), receiver, true, 180);
						} else
							World.addGroundItem(loot, location, receiver, true, 180);
					}
					break x;
				}
			}
		}
	}
	
	public boolean chanceCheck(int chance) {
        return Utils.random(100) < chance;
    }
	
	public int selectPossibility() {
		int randomNumber = Utils.random(100);
		
		if(randomNumber <= 100 && randomNumber > 10) {
			switch(Utils.random(9)) {
				case 0:
					return 1;
				case 1:
					return 2;
				case 2:
					return 4;
				case 3:
					return 5;
				case 4:
					return 6;
				case 5:
					return 7;
				case 6:
					return 8;
				case 7:
					return 10;
				case 8:
					return 11;
			}
		} else if(randomNumber <= 10 && randomNumber > 1) {
			switch(Utils.random(2)) {
				case 0:
					return 3;
				case 1:
					return 9;
			}
				
		} else if(randomNumber <= 1 && randomNumber >= 0) {
			return 0;
		}	
		return -1;
	}
	
	public void generateRewards(Player player) {
		int possibilityId = selectPossibility();
				
		TRISKELION_REWARDS trward = TRISKELION_REWARDS.matchID(possibilityId);
		
		if(trward != null) {
			for(int i=0; i < trward.getReward().length; i++) {
				if(chanceCheck(trward.getChance()[i])) {
					Item item = trward.getReward()[i];
					if (!item.getDefinitions().isNoted() && item.getAmount() > 1)
						if (item.getDefinitions().getCertId() != -1)
							item.setId(item.getDefinitions().getCertId());
						player.addItem(trward.getReward()[i]);
				}
			}
			if (!player.getTreasureTrails().hasClueScrollItem() && Utils.random(100) <= 50) {
				ClueScrollDistributor.givePlayerClueScroll(player, 3);
				player.sendMessage("You find an elite clue scroll!");
			}
		}
	}
	
	public enum TRISKELION_REWARDS {
		
		DRAGONSTONE_ARMOUR(0, new Item[] { new Item(1632, 5), new Item(28537, 1), new Item(28539, 1), new Item(28541, 1), new Item(28543,1), new Item(28545, 1) }, new int[] {100, 40, 40, 40, 40, 40}), // RARE
		
		HERB_SEEDS(1, new Item[] { new Item(1632, 20), new Item(5303, 20), new Item(5302, 20), new Item(5304, 20) }, new int[] {100, 100, 100, 100}), // COMMON
		
		FRUIT_SEEDS(2, new Item[] { new Item(1632, 20), new Item(5290, 8), new Item(5289, 8), new Item(5288, 8) }, new int[] {100, 100, 100, 100}), //COMMON
		
		DRAGON_ITEMS(3, new Item[] { new Item(1632, 20), new Item(6739, 1), new Item(15259, 1) }, new int[] {100, 35, 35}), //RARE
		
		CRYSTAL_KEYS(4, new Item[] { new Item(1632, 20), new Item(1514, 250), new Item(989, 3), new Item(987, 4), new Item(985, 4) }, new int[] {100, 40, 40, 40, 40}), // Common
		
		WOOD_SEEDS(5, new Item[] { new Item(1632, 20), new Item(5316, 10), new Item(5315, 15) }, new int[] {100, 100, 100}), //COMMON
		
		ADAMANTINE_ORE(6, new Item[] { new Item(1632, 20), new Item(450, 250), new Item(454, 1000) }, new int[] {100, 100, 100}), //COMMON
		
		RUNITE_ORE(7, new Item[] { new Item(1632, 20), new Item(452, 40), new Item(454, 320) }, new int[] {100, 100, 100}), //COMMON,
		
		GRIMY_HERBS(8, new Item[] { new Item(1632, 20), new Item(212, 50), new Item(218, 50), new Item(2486, 50), new Item(3052, 50), new Item(220, 10) }, new int[] {100, 100, 100, 100, 100, 100}), //COMMON
		
		ONYX(9, new Item[] { new Item(1632, 20), new Item(6571, 2) }, new int[] {100, 100}), //COMMON
		
		TRISKELION(10, new Item[] { new Item(1632, 20), new Item(1618, 50), new Item(1620, 65), new Item(1632, 30), new Item(28547, 1), new Item(28548, 1), new Item(28549, 1) }, new int[] {100, 100, 100, 100, 40, 40, 40}), //COMMON
		
		CHARMS(11, new Item[] { new Item(1632, 20), new Item(12160, 100), new Item(12163, 80) }, new int[] {100, 100, 100}), //COMMON
		;
		
		private final int possibilityId;
		private final Item[] rewards;
		private final int[] chance;
		
		TRISKELION_REWARDS(int possibilityId, Item[] rewards, int[] chance) {
			this.possibilityId = possibilityId;
			this.rewards = rewards;
			this.chance = chance;
		}
		

		public static TRISKELION_REWARDS matchID(int id) {
			for (TRISKELION_REWARDS treward : TRISKELION_REWARDS.values()) {
					if (treward.getPossibilityId() == id)
						return treward;
			}
			return null;
		}
		
		public int getPossibilityId() {
			return possibilityId;
		}
		
		public Item[] getReward() {
			return rewards;
		}
		
		public int[] getChance() {
			return chance;
		}
	}
	
}
