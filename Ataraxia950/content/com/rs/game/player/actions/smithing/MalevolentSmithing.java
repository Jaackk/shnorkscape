package com.rs.game.player.actions.smithing;

import com.rs.game.Animation;
import com.rs.game.Graphics;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.item.Item;
import com.rs.game.player.Equipment;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.PerkManager.DonationPerk;
import com.rs.game.player.actions.Action;
import com.rs.game.player.actions.firemaking.Firemaking;
import com.rs.game.player.actions.invention.InventionConstants.Perks;
import com.rs.game.player.actions.invention.InventionData.Perk;

public class MalevolentSmithing extends Action {

	private final WorldObject object;
	private final Malevolent piece;
	
	public MalevolentSmithing(final WorldObject object, final Malevolent piece) {
		this.object = object;
		this.piece = piece;
	}
	
	@Override
	public boolean process(Player player) {
		return true;
	}

	@Override
	public int processWithDelay(Player player) {
		for (Item required : piece.itemsRequired)
			player.getInventory().deleteItem(required.getId(), required.getAmount());
		Item weapon = player.getEquipment().getItem(Equipment.SLOT_WEAPON);
	    boolean hasAugmentedTool = weapon != null && weapon.getInventionData() != null && (weapon.getName().toLowerCase().contains("hammer-tron") || weapon.getName().toLowerCase().contains("crystal hammer"));
	        if (!hasAugmentedTool)
	            weapon = null;
		player.getInventory().addItem(new Item(piece.product));
		double xp = piece.experience;
        Perk tinker = weapon != null ? player.getInventionManager().hasPerk(weapon, Perks.TINKER) : null;
        boolean tinkerActive = tinker != null && Math.random() <= (0.05 * (double) tinker.getRank() * (tinker.hasIncreasedChance() ? 1.15 : 1.00));
        if (tinkerActive) {
            xp *= 1.25;
            player.getPackets().sendGameMessage("<col=00ff00>Your tinker perk allows you to carry out higher quality work, awarding extra XP.");
        }
        player.getInventionManager().processSkillXp(Skills.SMITHING, xp, weapon);
		player.getSkills().addXp(Skills.CRAFTING, xp);
		player.setNextAnimation(new Animation(weapon != null ? ( weapon.getName().toLowerCase().contains("hammer-tron") ? 30204 : 30203) :  22143));//30204 30203
		player.sendMessage("You combine the energy with the plates and smith a " + piece.product.getName() + ".");
		if (object != null)
			World.sendGraphics(player, new Graphics(2123), object);
		return -1;
	}

	@Override
	public boolean start(Player player) {
		if (player.getSkills().getLevel(Skills.SMITHING) < piece.levelRequired) {
			player.sendMessage("You need a Smithing level of at least " + piece.levelRequired + " to smith a " + piece.product.getName() + ".");
			return false;
		}
		if (!player.getInventory().containsItem(piece.itemsRequired[0])) {
			player.sendMessage("You need at least " + piece.itemsRequired[0].getAmount() + " malevolent energy to smith a " + piece.product.getName() + ".");
			return false;
		}
		if (!player.getInventory().containsItem(piece.itemsRequired[1])) {
			player.sendMessage("You need at least " + piece.itemsRequired[1].getAmount() + " reinforcing plate" + (piece.itemsRequired[1].getAmount() == 1 ? "" : "s") + " to smith a " + piece.product.getName() + ".");
			return false;
		}
		return true;
	}

	@Override
	public void stop(Player player) {}
	
	public enum Malevolent {
		SIRENIC_MASK(91, 500, new Item[] { new Item(30027, 14), new Item(30028, 1) }, new Item(30005)),
		SIRENIC_HAUBERK(93, 1500, new Item[] { new Item(30027, 42), new Item(30028, 3) }, new Item(30008)),
		SIRENIC_CHAPS(92, 1000, new Item[] { new Item(30027, 28), new Item(30028, 2) }, new Item(30011));

		private final int levelRequired;
		private final double experience;
		private final Item[] itemsRequired;
		private final Item product;

		Malevolent(int levelRequired, double experience, Item[] itemsRequired, Item energyProduce) {
			this.levelRequired = levelRequired;
			this.experience = experience;
			this.itemsRequired = itemsRequired;
			this.product = energyProduce;
		}
		
		public Item[] getItemsRequired() {
			return itemsRequired;
		}

		public int getLevelRequired() {
			return levelRequired;
		}

		public Item getProduceEnergy() {
			return product;
		}

		public double getExperience() {
			return experience;
		}
	}

}
