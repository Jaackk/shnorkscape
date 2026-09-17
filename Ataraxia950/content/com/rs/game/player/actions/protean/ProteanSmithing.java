package com.rs.game.player.actions.protean;

import com.rs.game.Animation;
import com.rs.game.Graphics;
import com.rs.game.World;
import com.rs.game.item.Item;
import com.rs.game.player.Equipment;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.Action;
import com.rs.game.player.actions.invention.InventionConstants.Perks;
import com.rs.game.player.actions.invention.InventionData.Perk;
import com.rs.utils.Colors;
import com.rs.utils.Utils;

public class ProteanSmithing extends Action {

	private static final int[] DATA = new int[] { 1, 15, 30, 50, 70, 85 };

	private int ticks;
	private final int itemId;
	private final boolean portable;
	
	public ProteanSmithing(int itemId, int ticks, boolean portable) {
		this.itemId = itemId;
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
		if (!player.getInventory().containsItem(new Item(31350, 1))) {
			stop(player);
			return 1;
		}
        Item weapon = player.getEquipment().getItem(Equipment.SLOT_WEAPON);
        boolean hasAugmentedTool = weapon != null && weapon.getInventionData() != null && (weapon.getName().toLowerCase().contains("hammer-tron") || weapon.getName().toLowerCase().contains("crystal hammer"));
            if (!hasAugmentedTool)
                weapon = null;
        double xp = ((itemId - 31350) * 75) * (portable ? 1.1 : 1);
        Perk tinker = weapon != null ? player.getInventionManager().hasPerk(weapon, Perks.TINKER) : null;
        boolean tinkerActive = tinker != null && Math.random() <= (0.05 * (double) tinker.getRank() * (tinker.hasIncreasedChance() ? 1.15 : 1.00));
        if (tinkerActive) {
            xp *= 1.25;
            player.getPackets().sendGameMessage("<col=00ff00>Your tinker perk allows you to carry out higher quality work, awarding extra XP.");
        }
        player.getInventionManager().processSkillXp(Skills.SMITHING, xp / 2, weapon);
		player.getSkills().addXp(Skills.SMITHING, xp);
		if (player.getAnimations().hasIronSmith && player.getAnimations().ironSmith) {
			player.setNextAnimation(new Animation(17309));
			player.setNextGraphics(new Graphics(3305));
		} else
			player.setNextAnimation(new Animation(weapon != null ? ( weapon.getName().toLowerCase().contains("hammer-tron") ? 30204 : 30203) :  22143));
		player.getInventory().deleteItem(31350, 1);
		player.addSmithingActions();
		if (Utils.random(100) <= 1 && player.hasEfficiencyActivated())
			player.sendMessage(Colors.ORANGE + "<shad=000000>Wasteless smithing: protean bar saved!", true);
		if (portable && Utils.random(9) == 4) {
			player.getBank().addItem(new Item(31350, 1), true);
			player.sendMessage(Colors.GOLD + "<shad=000000>The portable forge saves you some resources. They have been sent to your bank.", true);
		}
		player.sendMessage("You smith a protean bar; smithing actions: " + Colors.RED + Utils.getFormattedNumber(player.getSmithingActions()) + "</col>.", true);
        Perk rapid = weapon != null ? player.getInventionManager().hasPerk(weapon, Perks.RAPID) : null;
        boolean rapidActive = rapid != null && Math.random() <= (0.05 * (double) rapid.getRank() * (rapid.hasIncreasedChance() ? 1.15 : 1.00));
        if (rapidActive)
            player.getPackets().sendGameMessage("<col=00FF00>Your rapid perk speeds up the action process.");
		return rapidActive ? 6 : 8;
	}

	@Override
	public boolean start(Player player) {
		if (!player.getInventory().containsItem(2347, 1) && !player.getToolBelt().contains(2347)) {
			player.sendMessage("You need a hammer to work with a protean bar.");
			return false;
		}
		if (player.getSkills().getLevelForXp(Skills.SMITHING) < DATA[itemId - 31351]) {
			player.sendMessage("You need at least level " + DATA[itemId - 31351] + " Smithing to smith that.");
			return false;
		}
		return true;
	}

	@Override
	public void stop(Player player) {
		player.clickedObject = null;
	}

}
