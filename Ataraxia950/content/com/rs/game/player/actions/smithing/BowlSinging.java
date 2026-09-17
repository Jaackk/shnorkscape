package com.rs.game.player.actions.smithing;

import com.rs.game.Animation;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.Action;

import java.util.Arrays;
import java.util.HashMap;

/**
 * @author Tom
 * @date April 18, 2017
 */

public class BowlSinging extends Action {
	
	public enum CrystalCreation {

		CRYSAL_HALBERD(75, 750, new Item(32206), new Item(32219)),
		
		CRYSAL_DAGGER(75, 375, new Item(32206), new Item(32222)),
		
		CRYSAL_BOW(75, 750, new Item(32206), new Item(32228)),
		
		CRYSAL_CHAKRAM(75, 375, new Item(32206), new Item(32231)),
		
		CRYSAL_STAFF(75, 750, new Item(32206), new Item(32210)),
		
		CRYSAL_WAND(75, 375, new Item(32206), new Item(32213)),
		
		CRYSAL_SHIELD(75, 750, new Item(32623), new Item(32240)),
		
		CRYSAL_DEFLECTOR(75, 375, new Item(32623), new Item(32243)),
		
		CRYSAL_WARD(75, 375, new Item(32623), new Item(32637)),
		
		ATTUNED_CRYSTAL_HALBERD(90, 2000, new Item(32625), new Item(32647)),
		
		ATTUNED_CRYSTAL_DAGGER(90, 1000, new Item(32625), new Item(32649)),
		
		OFF_HAND_ATTUNED_CRYSTAL_DAGGER(90, 1000, new Item(32625), new Item(32651)),
		
		ATTUNED_CRYSTAL_BOW(90, 2000, new Item(32625), new Item(32653)),
		
		ATTUNED_CRYSTAL_CHAKRAM(90, 1000, new Item(32625), new Item(32655)),
		
		OFF_HAND_ATTUNED_CRYSTAL_CHAKRAM(90, 1000, new Item(32625), new Item(32657)),
		
		ATTUNED_CRYSTAL_STAFF(90, 1000, new Item(32625), new Item(32659)),
		
		ATTUNED_CRYSTAL_SHIELD(90, 1000, new Item(32626), new Item(32627)),
		
		ATTUNED_CRYSTAL_DEFLECTOR(90, 1000, new Item(32626), new Item(32629)),
		
		ATTUNED_CRYSTAL_WARD(90, 1000, new Item(32626), new Item(32631));
		
		
		public static CrystalCreation getCrystal(Player player) {
			for (CrystalCreation crystal : CrystalCreation.values()) {
					if (player.getInventory().containsItem(crystal.getItemsRequired()))
						return crystal;
			}
			return null;
		}
		
		public static final HashMap<Integer, CrystalCreation> VALUES = new HashMap<Integer, CrystalCreation>();

		static {
			for (CrystalCreation v : CrystalCreation.values())
				VALUES.put(v.ordinal(), v);
		}

		private final int levelRequired;
		private final int cost;
		private final Item itemsRequired;
		private final Item producedBar;

		CrystalCreation(int levelRequired, int cost, Item itemsRequired, Item producedBar) {
			this.levelRequired = levelRequired;
			this.cost = cost;
			this.itemsRequired = itemsRequired;
			this.producedBar = producedBar;
		}

		public Item getItemsRequired() {
			return itemsRequired;
		}

		public int getLevelRequired() {
			return levelRequired;
		}

		public int getCost() {
			return cost;
		}

		public Item getProducedBar() {
			return producedBar;
		}
	}

	public CrystalCreation crystal;
	public int ticks;

	public BowlSinging(CrystalCreation crystal, int ticks) {
		this.crystal = crystal;
		this.ticks = ticks;
	}

	public static void revertToCrystal(Player player, Item item) {
		if (player == null || item == null || !player.getInventory().containsItem(item)) {
			return;
		}
		if (Arrays.stream(CrystalCreation.values()).anyMatch(crystal -> crystal.getProducedBar().getId() == item.getId())) {
			Item seed = null;
			for (CrystalCreation value : CrystalCreation.values()) {
				if (value.getProducedBar().getId() == item.getId()) {
					seed = value.getItemsRequired();
				}
			}
			if (seed != null) {
				player.getInventory().deleteItem(item);
				player.addItem(seed);
				player.sendMessage("You revert the " + item.getName() + " back to a " + seed.getName() + ".");
			}
		}
	}

	@Override
	public boolean start(Player player) {
		if (crystal == null || player == null) 
			return false;
		if (!player.getInventory().containsItem(crystal.getItemsRequired().getId(),
				crystal.getItemsRequired().getAmount())){
			player.getPackets().sendGameMessage(
					"You don't have a  " + crystal.getItemsRequired().getName() + " to make this item.");
			return false;
		}
		if (!player.getInventory().containsItem(32622, crystal.getCost())){
			player.getPackets().sendGameMessage(
					"You don't have enough harmonic dust to make this item.");
			return false;
		}
		return true;
	}

	@Override
	public boolean process(Player player) {
		if (crystal == null || player == null)
			return false;
		if (!player.getInventory().containsItem(32622, crystal.getCost())){
			return false;
		}
		if (!player.getInventory().containsItem(crystal.getItemsRequired().getId(),
				crystal.getItemsRequired().getAmount())){
			return false;
		}
		if (player.getSkills().getLevel(Skills.SMITHING) < crystal.getLevelRequired()){
			player.getPackets().sendGameMessage("You need a Smithing level of " + crystal.getLevelRequired() + " in order to make this item.");
			return false;
		}
		return true;
	}

	@Override
	public int processWithDelay(Player player) {
		ticks--;
		int multiplier = 1;
		int cost = crystal.getCost();
		int xp = cost;
		int amount = crystal.getProducedBar().getAmount() * multiplier;
		player.getInventory().deleteItem(crystal.getItemsRequired().getId(), crystal.getItemsRequired().getAmount());
		player.getInventory().addItem(crystal.getProducedBar().getId(), amount);
		player.getInventory().deleteItem(32622, cost);
		player.getSkills().addXp(Skills.SMITHING, xp);
		player.setNextAnimation(new Animation(25031));
		return 2;
	}

	@Override
	public void stop(Player player) {
		setActionDelay(player, 3);
	}

}