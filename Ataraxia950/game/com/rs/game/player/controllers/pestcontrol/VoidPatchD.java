package com.rs.game.player.controllers.pestcontrol;

import com.rs.cache.loaders.ItemDefinitions;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.dialogue.Dialogue;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import lombok.val;

/**
 * @author Kris | 2. okt 2018 : 16:00:41
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>
 * @see <a href="https://rune-status.net/members/kris.354/">Rune-Status profile</a>
 */
public class VoidPatchD extends Dialogue {

	public static final Int2IntOpenHashMap MAP = new Int2IntOpenHashMap();
	
	static {
		MAP.put(8839, 31641);
		MAP.put(8840, 31642);
		MAP.put(8842, 31643);
		MAP.put(11663, 31644);
		MAP.put(11674, 31644);
		MAP.put(11664, 31645);
		MAP.put(11675, 31645);
		MAP.put(11665, 31646);
		MAP.put(11676, 31646);
		MAP.put(19712, 32336);
		
		for (int i = 0; i < 6; i++) {
			MAP.put(19785 + i, 31647 + i);
		}
	}
	
	public static final boolean patch(final Player player, final Item used, final Item usedWith) {
		val usedId = used.getId();
		val usedWithId = usedWith.getId();
		if (usedId != 31640 && usedWithId != 31640) {
			return false;
		}
		val patch = usedId == 31640 ? used : usedWith;
		val armourPiece = used == patch ? usedWith : used;
		val respectivePiece = MAP.get(armourPiece.getId());
		if (respectivePiece == 0) {
			return false;
		}
		player.getDialogueManager().startDialogue("VoidPatchD", patch, armourPiece, respectivePiece);
		return true;
	}
	
	private Item patch, armourPiece;
	private int respectivePiece;

	@Override
	public void start() {
		if (parameters == null || parameters.length < 3) {
			return;
		}
		if (!(parameters[0] instanceof Item) || !(parameters[1] instanceof Item) || !(parameters[2] instanceof Integer)) {
			return;
		}
		patch = (Item) parameters[0];
		armourPiece = (Item) parameters[1];
		respectivePiece = (Integer) parameters[2];
		sendOptionsDialogue("Upgrade your " + armourPiece.getName() + "?", "Yes.", "No.");
		stage = 0;
	}

	@Override
	public void run(final int interfaceId, final int componentId) {
		if (stage == 0) {
			if (componentId == OPTION_2) {
				end();
				return;
			} else if (componentId == OPTION_1) {
				if (!player.getInventory().containsItem(patch) || !player.getInventory().containsItem(armourPiece) || respectivePiece == 0) {
					return;
				}
				player.getInventory().deleteItem(patch);
				player.getInventory().deleteItem(armourPiece);
				player.getInventory().addItem(new Item(respectivePiece));
				sendItemDialogue(respectivePiece, 1, "You use the " + patch.getName() + " on the " + armourPiece.getName() + " to upgrade it to " + ItemDefinitions.getItemDefinitions(respectivePiece).getName() + ".");
			}
		} else {
			end();
		}
		stage++;
	}

	@Override
	public void finish() {
		
	}
	
}
