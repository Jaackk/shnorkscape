package com.rs.game.player.controllers.pestcontrol;

import com.rs.cache.loaders.ItemDefinitions;
import com.rs.game.item.Item;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import com.rs.game.player.dialogue.Dialogue;
import lombok.val;

/**
 * @author Kris | 2. okt 2018 : 12:17:52
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>
 * @see <a href="https://rune-status.net/members/kris.354/">Rune-Status profile</a>
 */
public class VoidUpgradeD extends Dialogue {

	public static final boolean upgrade(final Player player, final NPC npc, final Item item) {
		val id = item.getId();
		if (id != 8839 && id != 8840 && id != 31641 && id != 31642) {
			return false;
		}
		npc.faceEntity(player);
		player.getDialogueManager().startDialogue("VoidUpgradeD", item);
		return true;
	}
	
	private Item item;
	private final int npcId = 5956;
	private int ordinal;
	
	@Override
	public void start() {
		if (parameters.length == 0) {
			return;
		}
		val obj = parameters[0];
		if (!(obj instanceof Item)) {
			return;
		}
		item = (Item) obj;
		val id = item.getId();
		if (id != 8839 && id != 8840 && id != 31641 && id != 31642) {
			npc(npcId, "I'm sorry, I cannot upgrade that.");
			stage = 50;
			return;
		}
		npc(npcId, "Would you like to upgrade your " + item.getName() + " for the elite variant? It will cost you 100 commendation points!");
		stage = 0;
	}

	@Override
	public void run(final int interfaceId, final int componentId) {
		switch(stage++) {
		case 0:
			sendOptionsDialogue("Select an Option", "Yes, upgrade it.", "No, keep it.");
			return;
		case 1:
			if (componentId == OPTION_1) {
				player("Yes, upgrade it.");
				stage = 10;
			} else if (componentId == OPTION_2) {
				player("No, keep it.");
				stage = 50;
			}
			return;
		case 10:
			if (player.getPestPoints() < 100) {
				player("Oh wait, it appears I don't have enough commendation points yet.");
				stage = 50;
				return;
			}
			npc(npcId, "Which colour variation would you like?");
			return;
		case 11:
			sendOptionsDialogue("Select an Option", "Guardian", "Justiciar", "Executioner");
			return;
		case 12:
			ordinal = getOrdinal(componentId);
			switch(componentId) {
			case OPTION_1:
				player("I'll take the Guardian version.");
				return;
			case OPTION_2:
				player("I'll take the Justiciar version.");
				return;
			case OPTION_3:
				player("I'll take the Executioner version.");
				return;
			}
			return;
		case 13:
			if (player.getPestPoints() < 100) {
				player("Oh wait, it appears I don't have enough commendation points yet.");
				stage = 50;
				return;
			}
			val id = item.getId();
			if (ordinal < 0 || ordinal > 2 || !player.getInventory().containsItem(item)) {
				return;
			}
			int elitePieceId;
			if (id == 8839 || id == 8840) {
				elitePieceId = 19785 + (ordinal * 2) + (id - 8839);
			} else {
				elitePieceId = 31647 + (ordinal * 2) + (id - 31641);
			}
			player.setPestPoints(player.getPestPoints() - 100);
			player.getInventory().deleteItem(item);
			player.getInventory().addItem(new Item(elitePieceId));
			sendItemDialogue(elitePieceId, 1, "The Void Knight exchanges your " + item.getName() + " for " + ItemDefinitions.getItemDefinitions(elitePieceId).getName() + ".");
			stage = 50;
			return;
			
		case 50:
			end();
			return;
		}
	}

	@Override
	public void finish() {
		
	}

}
