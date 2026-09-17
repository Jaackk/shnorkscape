package com.rs.game.activities.rots;

import com.rs.game.World;
import com.rs.game.activites.gim.GIM;
import com.rs.game.item.Item;
import com.rs.game.item.ItemsContainer;
import com.rs.game.player.Player;
import com.rs.game.player.content.contracts.ContractHandler.ContractData;
import com.rs.game.player.content.contracts.ReaperPerks;
import com.rs.game.player.content.dropcollection.DropCollectionConstants;
import com.rs.game.player.content.hcim_news.HcimNewsManager;
import com.rs.utils.Utils;

/**
 * @author Kris | 3. sept 2017 : 23:33.56
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>}
 */
public class RiseOfTheSixReward {

	private static final int[][] DROPS = new int[][] { 
		{ 1748, 60, 80 }, { 29759, 3, 10 }, { 9245, 50, 65 }, { 1128, 6, 10 }, 
		{ 1514, 190, 210 }, { 452, 30, 34 }, { 15271, 120, 150 }, { 5303, 6, 10 }, 
		{ 5302, 6, 10 }, { 5316, 2, 4 }, { 1392, 35, 45 } 
	};

	/**
	 * Using "Quantity" variable to define the actual chance of receiving
	 * charms, because quantity is always 4. Easiest way to do this efficiency
	 * wise too.
	 */
	private static final Item[] CHARMS = new Item[] { 
			new Item(12158, 4), new Item(12159, 4), 
			new Item(12160, 4), new Item(12163, 4) 
	};
	
	
	/**
	 * Generates a container with a random set of rewards.
	 * @param player who gets the loot.
	 * @return container with loot.
	 */
	public static final ItemsContainer<Item> getRewards(final Player player) {
		final ItemsContainer<Item> rewards = new ItemsContainer<Item>(10, true);
		Item energies = new Item(30026, Utils.randomBool() ? 2 : 1);
		rewards.add(energies);
	    player.getDropCollectionHandler().handleBossKills(energies, DropCollectionConstants.ROTS_ID);
		final int[] MD = DROPS[Utils.random(DROPS.length)];
		final Item mainDrop = new Item(MD[0], Utils.random(MD[1], MD[2]));
		final Item charms = CHARMS[Utils.random(CHARMS.length)];
		rewards.add(mainDrop);
		player.getDropCollectionHandler().handleBossKills(mainDrop, DropCollectionConstants.ROTS_ID);
		if (Utils.random(player.reaperPerkActivated(ReaperPerks.REAPERS_BLESSING) && player.getContract() != null && player.getContract().getNpcId() == ContractData.BARROWS_RISE_OF_THE_SIX.getNpcId() ? 85 : 90) == 0) {
			final int random = Utils.random(3);
			final Item shield = new Item(random == 0 ? 30014 : random == 1 ? 30018 : 30022, 1);
			rewards.add(shield);
			player.getDropCollectionHandler().handleBossKills(shield, DropCollectionConstants.ROTS_ID);
			World.sendWorldMessage("<img=6><col=ff9900>News: " + player.getDisplayName() + " has received a " + shield.getName() + " drop!", false);
			HcimNewsManager.getInstance().addNews(player, "<#player> received a " + shield.getName() + " drop!", 8);
		}
		if (Utils.random(player.reaperPerkActivated(ReaperPerks.REAPERS_BLESSING) && player.getContract() != null && player.getContract().getNpcId() == ContractData.BARROWS_RISE_OF_THE_SIX.getNpcId() ? 95 : 100) < 67) {
			rewards.add(charms);
		}
        boolean hasAllPets = true;
        for (int i = 0; i < 6; i++) {
            if (!player.hasItem(30031+i)) {
                hasAllPets = false;
                break;
            }
        }
		if (!hasAllPets && Math.random() <= 0.0004d) {//pets
		    int random = Utils.random(6);
		    while (player.hasItem(30031+random)) {
		        random = Utils.random(6);
		    }
		    Item pet = new Item(30031+random, 1);
	        rewards.add(pet);
	        player.getDropCollectionHandler().handleBossKills(pet, DropCollectionConstants.ROTS_ID);
	        World.sendWorldMessage("<img=6><col=ff9900>News: " + player.getDisplayName() + " has received a " + pet.getName() + " drop!", false);
			HcimNewsManager.getInstance().addNews(player,"<#player> received a " + pet.getName() + " drop!", 8);
		}
		return rewards;
	}

}
