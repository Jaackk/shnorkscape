package com.rs.game.player.actions.protean;

import com.rs.game.Animation;
import com.rs.game.World;
import com.rs.game.item.Item;
import com.rs.game.player.PerkManager.DonationPerk;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.Action;
import com.rs.utils.Colors;
import com.rs.utils.Utils;

public class ProteanCrafting extends Action {

	private final boolean portable;
	private int ticks;
	
	public ProteanCrafting(int ticks, boolean portable) {
		this.ticks = ticks;
		this.portable = portable;
	}

	@Override
	public boolean process(Player player) {
		if (ticks == 0)
			return false;
		if (player.clickedObject != null) {
            return World.containsObjectWithId(player.clickedObject, player.clickedObject.getId());
		}
		return true;
	}

	@Override
	public int processWithDelay(Player player) {
		ticks--;
		if (!player.getInventory().containsItem(new Item(33740, 1))) {
			stop(player);
			return 1;
		}
		player.getSkills().addXp(Skills.CRAFTING, ((player.getSkills().getLevelForXp(Skills.CRAFTING) * 2.42) + 29.8) * (portable ? 1.1 : 1));
		if (!player.getPerkManager().hasPerkActive(DonationPerk.DELICATE_CRAFTSMAN)) {
			if (Utils.random(8) == 0) {
				player.getInventory().deleteItem(new Item(1734, 1));
				player.sendMessage("You use up a reel of your thread.", true);
			}
		}
		player.setNextAnimation(new Animation(25459));
		player.getInventory().deleteItem(33740, 1);
		player.addItemsMade();
		if (portable && Utils.random(9) == 4) {
			player.getBank().addItem(new Item(33740, 1), true);
			player.sendMessage(Colors.GOLD + "<shad=000000>The portable crafter saves you some resources. They have been sent to your bank.", true);
		}
		player.sendMessage("You craft the protean hide; items crafted: " + Colors.RED + Utils.getFormattedNumber(player.getItemsMade()) + "</col>.", true);
		return 5;
	}

	@Override
	public boolean start(Player player) {
		if (!player.getInventory().containsItem(1734, 1) && !player.getPerkManager().hasPerkActive(DonationPerk.DELICATE_CRAFTSMAN)) {
			if (!player.getInventory().containsOneItem(1733)) {
				player.sendMessage("You need a needle and some thread to craft protean hides.");
				return false;
			}
			player.sendMessage("You need some thread to craft protean hides.");
			return false;
		}
		if (!player.getInventory().containsOneItem(1733)) {
			player.sendMessage("You need a needle to craft protean hides.");
			return false;
		}
		return true;
	}

	@Override
	public void stop(Player player) {
		player.clickedObject = null;
	}

}
