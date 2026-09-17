package com.rs.game.player.actions.protean;

import com.rs.game.Animation;
import com.rs.game.Graphics;
import com.rs.game.World;
import com.rs.game.item.Item;
import com.rs.game.player.PerkManager.DonationPerk;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.Action;
import com.rs.utils.Colors;
import com.rs.utils.Utils;

public class ProteanFletching extends Action {

	private final boolean fletchOnly;
	private final boolean portable;
	
	private int ticks;
	
	public ProteanFletching(int amount, boolean fletchOnly, boolean portable) {
		this.ticks = amount;
		this.fletchOnly = fletchOnly;
		this.portable = portable;
	}
	
	@Override
	public boolean process(Player player) {
		if (ticks == 0)
			return false;
		if (!player.getInventory().containsItem(34528, 1)) {
			player.sendMessage("You need some more protean logs to do this.");
			return false;
		}
		if (player.clickedObject != null) {
            return World.containsObjectWithId(player.clickedObject, player.clickedObject.getId());
		}
		return true;
	}

	@Override
	public int processWithDelay(Player player) {
		ticks--;
		if (!player.getInventory().containsItem(new Item(34528, 1))) {
			stop(player);
			return 1;
		}
		player.addItemsFletched();
		double experience = (player.getSkills().getLevelForXp(Skills.FLETCHING) * 3.92) * (portable ? 1.1 : 1);
		if (fletchOnly) {
			player.setNextAnimation(new Animation(26269));
			player.getSkills().addXp(Skills.FLETCHING, experience);
			player.getInventory().deleteItem(new Item(34528, 1));
			if (portable && Utils.random(9) == 4) {
				player.getBank().addItem(new Item(34528, 1), true);
				player.sendMessage(Colors.GOLD + "<shad=000000>The portable fletcher saves you some resources. They have been sent to your bank.", true);
			}
			player.sendMessage("You fletch the protean logs; items fletched: " + Colors.RED + Utils.getFormattedNumber(player.getItemsFletched()) + "</col>.", true);
		} else {
			player.addLogsBurned();
			player.setNextAnimation(new Animation(26267));
			player.setNextGraphics(new Graphics(5460));
			player.getSkills().addXp(Skills.FLETCHING, experience / 2);
			player.getSkills().addXp(Skills.FIREMAKING, (player.getSkills().getLevelForXp(Skills.FIREMAKING) * 3.92) / 2);
			player.getInventory().deleteItem(new Item(34528, 1));
			player.sendMessage("You fletch the protean logs and light them on fire afterwards; items fletched: " + Colors.RED + Utils.getFormattedNumber(player.getItemsFletched()) + "</col>; logs burned " + Colors.RED + Utils.getFormattedNumber(player.getLogsBurned()) + "</col>.", true);
		}
		return player.getPerkManager().hasPerkActive(DonationPerk.MASTER_FLEDGER) ? 7 : 8;
	}

	@Override
	public boolean start(Player player) {
		if (fletchOnly) {
			if (!player.getInventory().containsItem(946, 1) && !player.getToolBelt().contains(946)) {
				player.sendMessage("You need a knife to fletch the Protean logs.");
				return false;
			}
		} else {
			if (!player.getInventory().containsItem(946, 1) && !player.getToolBelt().contains(946)) {
				if (!player.getInventory().containsItem(590, 1) && !player.getToolBelt().contains(590)) {
					player.sendMessage("You need a knife and a tinderbox to fletch and burn the Protean logs.");
					return false;
				}
				player.sendMessage("You need a knife to fletch the Protean logs.");
				return false;
			}
			if (!player.getInventory().containsItem(590, 1) && !player.getToolBelt().contains(590)) {
				player.sendMessage("You need a tinderbox to fletch and burn the Protean logs.");
				return false;
			}
		}
		return true;
	}

	@Override
	public void stop(Player player) {
		player.clickedObject = null;
	}
}
