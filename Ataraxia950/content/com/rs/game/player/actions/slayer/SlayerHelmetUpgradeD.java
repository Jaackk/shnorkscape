package com.rs.game.player.actions.slayer;

import com.rs.cache.loaders.ItemDefinitions;
import com.rs.game.item.Item;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import com.rs.game.player.dialogue.Dialogue;
import lombok.val;

/**
 * @author Kris | 2. okt 2018 : 20:25:53
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>
 * @see <a href="https://rune-status.net/members/kris.354/">Rune-Status profile</a>
 */
public class SlayerHelmetUpgradeD extends Dialogue {

	private final int npcId = 9085;
	
	public static final boolean upgrade(final Player player, final Item item, final NPC npc) {
		if (npc.getId() != 9085) {
			return false;
		}
		val helm = SlayerHelmet.MAP.get(item.getId());
		if (helm == null) {
			return false;
		}
		player.getDialogueManager().startDialogue("SlayerHelmetUpgradeD", item);
		return true;
	}
	
	private Item item;
	private SlayerHelmet currentTier;
	private SlayerHelmet nextTier;
	
	@Override
	public void start() {
		if (parameters == null || parameters.length == 0) {
			return;
		}
		if (!(parameters[0] instanceof Item)) {
			return;
		}
		item = (Item) parameters[0];
		currentTier = SlayerHelmet.MAP.get(item.getId());
		if (currentTier == null) {
			return;
		}
		nextTier = currentTier.getNextHelmet();
		if (nextTier == null) {
			return;
		}
		npc(npcId, "Would you like to upgrade your " + item.getName() + " to " + (ItemDefinitions.getItemDefinitions(nextTier.getId()).getName() + " for " + nextTier.getCost() + " slayer points?"));
		stage = 0;
	}

	@Override
	public void run(final int interfaceId, final int componentId) {
		switch(stage++) {
		case 0:
			sendOptionsDialogue("Select an Option", "Yes, upgrade it.", "No, don't upgrade it.");
			return;
		case 1:
			if (componentId == OPTION_2) {
				end();
				return;
			} else if (componentId == OPTION_1) {
				if (player.getSlayerPoints() < nextTier.getCost()) {
					end();
					player.sendMessage("You don't have enough slayer points to upgrade your slayer helmet.");
					return;
				}
				val inventory = player.getInventory();
				if (!inventory.containsItem(item)) {
					return;
				}
				player.setSlayerPoints(player.getSlayerPoints() - nextTier.getCost());
				inventory.deleteItem(item);
				inventory.addItem(new Item(nextTier.getId()));
				sendItemDialogue(nextTier.getId(), 1, "You upgrade your " + item.getName() + " into a " + ItemDefinitions.getItemDefinitions(nextTier.getId()).getName() + ".");
			}
			return;
		case 2:
			end();
			return;
		}
	}

	@Override
	public void finish() {
		
	}

}
