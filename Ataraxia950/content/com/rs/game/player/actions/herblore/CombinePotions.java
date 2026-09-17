package com.rs.game.player.actions.herblore;

import com.rs.game.Animation;
import com.rs.game.Graphics;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.Action;
import com.rs.game.player.actions.herblore.CrystalFlask.CrystalPot;
import com.rs.game.player.content.achievementsystem.AchievementList;

public class CombinePotions extends Action {

	private final CrystalPot cpotion;
	private int quantity;

	public CombinePotions(CrystalPot cpotion) {
		this(cpotion, 1);
	}

	public CombinePotions(CrystalPot cpotion, int quantity) {
		this.cpotion = cpotion;
		this.quantity = quantity;
	}

	private double botanistSuit(Player player) {
		double xpBoost = 1.0;
		if (player.getEquipment().getHatId() == 25190)
			xpBoost *= 1.01;
		if (player.getEquipment().getHatId() == 34923)
			xpBoost *= 1.03;
		if (player.getEquipment().getChestId() == 25191)
			xpBoost *= 1.01;
		if (player.getEquipment().getLegsId() == 25192)
			xpBoost *= 1.01;
		if (player.getEquipment().getBootsId() == 25193)
			xpBoost *= 1.01;
		if (player.getEquipment().getGlovesId() == 25194)
			xpBoost *= 1.01;
		if (player.getEquipment().getHatId() == 25190 && player.getEquipment().getChestId() == 25191
				&& player.getEquipment().getLegsId() == 25192 && player.getEquipment().getBootsId() == 25193
				&& player.getEquipment().getGlovesId() == 25194)
			xpBoost *= 1.01;
		if (player.getEquipment().getHatId() == 34923 && player.getEquipment().getChestId() == 25191
				&& player.getEquipment().getLegsId() == 25192 && player.getEquipment().getBootsId() == 25193
				&& player.getEquipment().getGlovesId() == 25194)
			xpBoost *= 1.01;
		return xpBoost;
	}

	@Override
	public boolean process(Player player) {
		if (player == null || cpotion == null)
			return false;
		if (quantity <= 0)
			return false;
		if ((cpotion.getOrder() >= 0 && cpotion.getOrder() <= 15) && !player.meilyrShopSettings[cpotion.getOrder()]
				|| (cpotion.getOrder() >= 16 && cpotion.getOrder() <= 26)
						&& !player.meilyrShopSettings2[cpotion.getOrder() - 16]) {
			player.sendMessage(
					"You don't have the recipe for this potion unlocked. You can purchase recipes from Lady Meilyr in Prifddinas.");
			player.getInterfaceManager().closeChatBoxInterface();
			return false;
		}
		if (player.getInterfaceManager().containsScreenInter()
				|| player.getInterfaceManager().containsInventoryInter()) {
			player.getPackets().sendGameMessage("Please finish what you're doing before doing this action.");
			return false;
		}
		if (!player.getInventory().containsItem(32843, 1)) {
			player.sendMessage("You ran out of crystal flasks.");
			player.getInterfaceManager().closeChatBoxInterface();
			return false;
		}

		for (int i = 0; i < cpotion.getRequiredPotion().length; i++) {
			if (!player.getInventory().containsItem(cpotion.getRequiredPotion()[i])) {
				player.getInterfaceManager().closeChatBoxInterface();
				return false;
			}
		}
		return true;
	}

	@Override
	public int processWithDelay(Player player) {
		quantity--;
		if (player.getAnimations().hasEnhancedPotion && player.getAnimations().enhancedPotion) {
			player.setNextGraphics(new Graphics(3216));
			player.setNextGraphics(new Graphics(3217));
			player.setNextGraphics(new Graphics(3218));
			player.setNextAnimation(new Animation(17097));
		} else
			player.setNextAnimation(new Animation(363));

		player.getSkills().addXp(Skills.HERBLORE, cpotion.getBaseXP() * botanistSuit(player));

		for (int i = 0; i < cpotion.getRequiredPotion().length; i++) {
			player.getInventory().deleteItem(cpotion.getRequiredPotion()[i]);
		}

		player.getInventory().deleteItem(32843, 1);
		player.getInventory().addItem(cpotion.getProducedPotion(), 1);
		player.addPotionsMade();
		if (cpotion == CrystalPot.SUPREME_OVERLOAD)
			player.getAchievements().updateProgress(1, AchievementList.CREATE_100_SUPREME_OVERLOADS);
		player.getInterfaceManager().closeChatBoxInterface();
		return quantity > 0 ? 1 : -1;
	}

	@Override
	public boolean start(Player player) {
        return process(player);
    }

	@Override
	public void stop(Player player) {
		setActionDelay(player, 3);
	}

}
