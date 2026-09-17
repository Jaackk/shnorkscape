package com.rs.game.player.controllers.pestcontrol;

import com.rs.cache.loaders.ItemDefinitions;
import com.rs.game.item.Item;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import com.rs.game.player.dialogue.Dialogue;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import lombok.val;
import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;

/**
 * @author Kris | 2. okt 2018 : 16:31:50
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>
 * @see <a href="https://rune-status.net/members/kris.354/">Rune-Status profile</a>
 */
public class VoidSwitchD extends Dialogue {

	public static boolean switchColour(final Player player, final NPC npc, final Item item) {
		val id = item.getId();
		if ((id >= 19785 && id <= 19790) || (id >= 31647 && id <= 31652)) {
			npc.faceEntity(player);
			player.getDialogueManager().startDialogue("VoidSwitchD", item);
			return true;
		}
		return false;
	}
	
	private static final int[] ELITE_TOPS = new int[] { 19785, 19787, 19789 };
	private static final int[] ELITE_BOTTOMS = new int[] { 19786, 19788, 19790 };
	private static final int[] SUPERIOR_ELITE_TOPS = new int[] { 31647, 31649, 31651 };
	private static final int[] SUPERIOR_ELITE_BOTTOMS = new int[] { 31648, 31650, 31652 };

	private static final String[] COLOUR_NAMES = new String[] {
			"Guardian", "Justiciar", "Executioner"
	};
	
	private Item item;
	private final IntArrayList list = new IntArrayList(3);
	
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
		val name = item.getName();
		int[] array = null;
		if (name.equals("Elite void knight top")) {
			array = ELITE_TOPS;
		} else if (name.equals("Elite void knight robe")) {
			array = ELITE_BOTTOMS;
		} else if (name.equals("Superior elite void knight top")) {
			array = SUPERIOR_ELITE_TOPS;
		} else if (name.equals("Superior elite void knight robe")) {
			array = SUPERIOR_ELITE_BOTTOMS;
		}
		if (array == null) {
			return;
		}
		list.addElements(0, array);
		list.rem(item.getId());
		if (list.size() != 2) {
			return;
		}
		val names = new ArrayList<String>();
		for (final int id : list) {
			val index = ArrayUtils.indexOf(array, id);
			if (index == -1) {
				return;
			}
			names.add(COLOUR_NAMES[index]);
		}
		sendOptionsDialogue("Select the variant you'd like.", names.toArray(new String[names.size()]));
		stage = 0;
	}

	@Override
	public void run(final int interfaceId, final int componentId) {
		switch(stage++) {
		case 0:
			val ordinal = getOrdinal(componentId);
			if (ordinal < 0 || ordinal >= list.size() || !player.getInventory().containsItem(item)) {
				return;
			}
			player.getInventory().deleteItem(item);
			val id = list.getInt(ordinal);
			player.getInventory().addItem(new Item(id));
			sendItemDialogue(id, 1, "You switch your " + ItemDefinitions.getItemDefinitions(id).getName() + " to a different colour.");
			return;
		case 1:
			end();
			return;
		}
	}

	@Override
	public void finish() {
		
	}

}
