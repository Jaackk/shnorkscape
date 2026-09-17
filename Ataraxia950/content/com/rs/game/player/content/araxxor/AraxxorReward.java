package com.rs.game.player.content.araxxor;

import com.rs.game.World;
import com.rs.game.activites.gim.GIM;
import com.rs.game.item.Item;
import com.rs.game.item.ItemsContainer;
import com.rs.game.player.PerkManager.DonationPerk;
import com.rs.game.player.Player;
import com.rs.game.player.content.achievementsystem.AchievementList;
import com.rs.game.player.content.dropcollection.DropCollectionConstants;
import com.rs.game.player.content.hcim_news.HcimNewsManager;
import com.rs.utils.Colors;
import com.rs.utils.Utils;
import com.rs.utils.mysql.QueryExecutor;
import com.rs.utils.mysql.impl.News;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Utility class wrapper around the Rewards drop table for Araxxor. <br/>
 * <br/>
 * This project is primarily the work of dlo3, with contributions from Ataraxia devs Kris, Armak1ing, and CJay. It may only be distributed
 * <b>AS-IS</b>, and <b>FREE OF CHARGE</b>, with dlo3's consent. Furthermore, this notice must remain in all distributions.
 * @author David O'Neill (dlo3)
 * @since 1.0 (alpha, 08/12/2017)<br/>
 *        1.1 (beta, 08/19/2017)
 */
final class AraxxorReward {

	private final ItemsContainer<Item> rewards;
	private boolean worldNotified;
	private boolean looted;
	private boolean petNotified;

	AraxxorReward(Player player) {
		int remaining = 5;
		rewards = new ItemsContainer<>(5, false);
		if (Math.random() < 0.1) {
			rewards.add(new Item(33870, 1));
			remaining--;
		}
		rewards.add(new Item(31737, Utils.random(70, 90)));
		remaining--;
		Set<Item> randomItems = Rewards.fill(remaining);
		randomItems.forEach(rewards::add);

		final int petChance = Utils.random(player.getPerkManager().hasPerkActive(DonationPerk.PETSCHANTER) ? 3000 : 4000);
		Item pet = null;
		switch (petChance) {
		case 1:
			pet = new Item(31748);
			break;
		case 2012:
			pet = new Item(31749);
			break;
		case 63:
			pet = new Item(31750);
			break;
		case 4874:
			pet = new Item(31751);
			break;
		case 0:
			pet = new Item(31752);
			break;
		case 3728:
			pet = new Item(31753);
			break;
		case 3729:
			pet = new Item(33809);
			break;
		case 3730:
			pet = new Item(33810);
			break;
		default:
			break;
		}
		
		if (pet != null && !player.hasItem(pet)) {
			rewards.forceAdd(pet);
		}
		for(int i = 0; i < rewards.getSize(); i ++){
			player.getDropCollectionHandler().handleBossKills(rewards.get(i), DropCollectionConstants.ARAXXOR_ID);
		}
	}

	Item[] getItems() {
		return rewards.getItems();
	}

	Item getAt(int slot) {
		return rewards.get(slot);
	}

	void removeItem(Item item) {
		rewards.remove(item);
	}

	void removeItemAndShift(Item item) {
		rewards.remove(item);
		rewards.shift();
	}

	void shift() {
		rewards.shift();
	}

	void discardItems() {
		rewards.clear();
	}

	boolean isEmpty() {
		return rewards.freeSlots() == rewards.getSize();
	}

	int size() {
		return rewards.getSize();
	}
	
	public boolean containsPetReward() {
		return Arrays.stream(rewards.getItems()).filter(i -> i != null).anyMatch(i -> (i.getId() >= 31742 && i.getId() <= 31747) || i.getId() == 31809 || i.getId() == 31810);
	}

	private boolean containsRareReward() {
		return Arrays.stream(rewards.getItems()).filter(i -> i != null).anyMatch(i -> containsPetReward() || i.getId() == Rewards.WEB.item.getId() || i.getId() == Rewards.EYE.item.getId() || i.getId() == Rewards.FANG.item.getId() || i.getId() == Rewards.SPIDER_LEG_TOP.item.getId() || i.getId() == Rewards.SPIDER_LEG_MIDDLE.item.getId() || i.getId() == Rewards.SPIDER_LEG_BOTTOM.item.getId() || i.getId() == 33870);
	}
	
	public void notifyPetDrop(Player killer) {
		if (!petNotified && containsPetReward()) {
			World.sendWorldMessage(Colors.ORANGE + "<shad=000000><img=6>News: " + killer.getDisplayName() + " received a Araxxi pet drop.", false);
			HcimNewsManager.getInstance().addNews(killer, "<#player> got an Araxxi pet drop!");
					killer.getAchievements().updateProgress(1, AchievementList.OBTAIN_BOSS_PET);
            QueryExecutor.submit(new News(killer, "<b><img src=\"../bin/images/news/" + "araxx.png" + "\" width=17> " + killer.getDisplayName() + " received a Araxxi pet drop."));
		}
		
		petNotified = true;
	}

	void notifyWorld(String displayName) {
		if (!worldNotified && containsRareReward())
			World.sendWorldMessage(Colors.GREEN + displayName + " just received a rare drop " + "from Araxxi's corpse!", false);
		worldNotified = true;
	}

	/**
	 * Gets the looted.
	 * @return the looted
	 */
	public boolean isLooted() {
		return looted;
	}

	/**
	 * Sets the looted.
	 * @param looted the looted to set
	 */
	public void setLooted(boolean looted) {
		this.looted = looted;
	}

	private enum Rewards {

		COINS(new Item(995, 400000), 25), BLACK_DRAGONHIDE(new Item(28547, Utils.random(70, 91)), 125), CT_FRAGMENT_1(new Item(28547, 1), 25), CT_FRAGMENT_2(new Item(28548, 1), 25), CT_FRAGMENT_3(new Item(28549, 1), 25), SIRENIC_SCALE(new Item(29863, 2, 4), 25), HYDRIX_BOLT_TIPS(new Item(31867, 50, 151), 22), ONYX_BOLTS_E(new Item(9245, 101, 252), 25), RUNEPLATEBODY(new Item(1127, 1), 125), UNCUT_ONYX(new Item(6571, 2), 25), GRIMY_AVANTOE(new Item(212, 45), 125), GRIMY_DWARF_WEED(new Item(218, 45), 125), GRIMY_LANTADYME(new Item(2486, 40, 56), 125), MAGIC_LOGS(new Item(1513, 150, 324), 125), YEW_LOGS(new Item(1516, 600), 125), ADAMANTITE_ORE(new Item(450, 100), 125), COAL(new Item(454, 600), 125), RUNITE_ORE(new Item(452, 50), 25), DWARF_WEED_SEED(new Item(5303, 10), 25), MAGIC_SEED(new Item(5316, 5, 9), 25), WATER_TALISMAN(new Item(1445, 40, 71), 25), BATTLE_STAFF(new Item(1392, 50, 71), 25), SPIDER_LEG_TOP(new Item(31718, 1), 10), SPIDER_LEG_MIDDLE(new Item(31719, 1), 10), SPIDER_LEG_BOTTOM(new Item(31720, 1), 10), FANG(new Item(31722, 1), 3), EYE(new Item(31723, 1), 3), WEB(new Item(31724, 1), 3);

		private final Item item;
		private final int weight;

		Rewards(Item item, int weight) {
			this.item = new Item(item.getId(), item.getCharges() == 0 ? item.getAmount() : Utils.random(item.getAmount(), item.getCharges()));
			this.weight = weight;
		}

		private static Set<Item> fill(int remaining) {

			List<Rewards> filler = new ArrayList<>();
			Set<Rewards> rewards = new HashSet<>(remaining);

			Arrays.stream(values()).forEach(r -> {
				for (int i = 0; i < r.weight; i++)
					filler.add(r);
			});

			Collections.shuffle(filler);

			int i = 0;
			while (rewards.size() < remaining)
				rewards.add(filler.get(i++));

			return rewards.stream().filter(r -> r.item != null).map(r -> r.item).collect(Collectors.toSet());
		}
	}

}
