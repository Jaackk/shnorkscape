package com.rs.game.player.content.dungeoneering.skills.herblore;

import com.rs.cache.loaders.ItemDefinitions;
import com.rs.game.Animation;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.Action;
import com.rs.utils.Colors;
import com.rs.utils.Utils;

public class DungeoneeringHerblore extends Action {

	public static final int SAGEWORT = 17538, VALERIAN = 17540, ALOE = 17542, WORMWOOD = 17544, MAGEBANE = 17546, 
			FEATHERFOIL = 17548, WINTERS_GRIP = 17550, LYCOPUS = 17552, BUCKTHORN = 17554, 
			VOID_DUST = 17530, MISSHAPEN_CLAW = 17532, RED_MOSS = 17534, FIREBREATH_WHISKEY = 17536;

	public static boolean isValidPotion(Player player, Item first, Item second) {
		for (DungeoneeringHerbloreIngredients data : DungeoneeringHerbloreIngredients.values()) {
			if (data.getItems()[0] == first.getId() && data.getItems()[1] == second.getId() || data.getItems()[0] == second.getId() && data.getItems()[1] == first.getId()) {
				player.getDialogueManager().startDialogue("DungeoneeringHerbloreD", data);
				return true;
			}
		}
		return false;
	}
	
	public DungeoneeringHerblore(DungeoneeringHerbloreIngredients data, int quantity) {
		this.data = data;
		this.quantity = quantity;
	}

	private final DungeoneeringHerbloreIngredients data;
	private int quantity;

	@Override
	public boolean process(Player player) {
		if (quantity <= 0)
			return false;
		if (!player.getInventory().containsItem(data.getItems()[0], 1) || !player.getInventory().containsItem(data.getItems()[1], 1)) {
			player.sendMessage("You've ran out of the necessary ingredients.");
			return false;
		}
		return true;
	}

	@Override
	public int processWithDelay(Player player) {
		quantity--;
		if (data.getExperience() != 0)
			player.getSkills().addXp(Skills.HERBLORE, data.getExperience());
		player.getInventory().deleteItem(data.getItems()[0], 1);
		player.getInventory().deleteItem(data.getItems()[1], 1);
		player.getInventory().addItem(data.getItems()[2], 1);
		player.setNextAnimation(new Animation(363));
		if (data.ordinal() < 9)
			player.sendMessage("You add the " + ItemDefinitions.getItemDefinitions(data.getItems()[1]).getName().replace("Clean ", "").toLowerCase() + " into the " + ItemDefinitions.getItemDefinitions(data.getItems()[0]).getName().replace(" (unf)", "").toLowerCase() + ".", true);
		else {
			player.addPotionsMade();
			player.sendMessage("You add the " + ItemDefinitions.getItemDefinitions(data.getItems()[1]).getName().replace("Clean ", "").toLowerCase() + " into the " + ItemDefinitions.getItemDefinitions(data.getItems()[0]).getName().replace(" (unf)", "").toLowerCase() + "; potions made: " + Colors.RED + Utils.getFormattedNumber(player.getPotionsMade()) + "</col>.", true);
		}
		return 1;
	}

	@Override
	public boolean start(Player player) {
		if (player.getSkills().getLevelForXp(Skills.HERBLORE) < data.getLevel()) {
			player.sendMessage("You need at least " + data.getLevel() + " Herblore to create this potion.");
			return true;
		}
        return process(player);
    }

	@Override
	public void stop(Player player) {

	}

}
