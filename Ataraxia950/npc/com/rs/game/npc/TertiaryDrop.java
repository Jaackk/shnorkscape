package com.rs.game.npc;

import com.rs.game.item.Item;
import com.rs.game.npc.familiar.Familiar;
import com.rs.utils.Utils;

import java.util.ArrayList;

public class TertiaryDrop {
	
	public static final int[] BANDOS_WARPRIEST = new int[] {
			30306, 30309, 30312, 30315, 30318, 30321, 32040, 32041, 32042, 32043, 32044, 32045
	};
	
	public static final int[] SARADOMIN_WARPRIEST = new int[] {
			28755, 28758, 28761, 28764, 28767, 28770, 32022, 32023, 32024, 32025, 32026, 32027
	};
	
	public static final int[] ZAMORAK_WARPRIEST = new int[] {
			28773, 28776, 28779, 28782, 28785, 28788, 32028, 32029, 32030, 32031, 32032, 32033
	};
	
	public static final int[] ARMADYL_WARPRIEST = new int[] {
		30288, 30291, 30294, 30297, 30300, 30303, 32034, 32035, 32036, 32037, 32038, 32039
	};
	
	public static Item getTertiaryDrop(int npcId, boolean hard) {
		if (npcId == 6203)
			return new Item(ZAMORAK_WARPRIEST[Utils.getRandom(5) + (hard ? 6 : 0)], 1);
		else if (npcId == 6222)
			return new Item(ARMADYL_WARPRIEST[Utils.getRandom(5) + (hard ? 6 : 0)], 1);
		else if (npcId == 6247)
			return new Item(SARADOMIN_WARPRIEST[Utils.getRandom(5) + (hard ? 6 : 0)], 1);
		else
			return new Item(BANDOS_WARPRIEST[Utils.getRandom(5) + (hard ? 6 : 0)], 1);
	}
	

	public static Item[] getTertiaryDrop(NPC npc) {
		if (npc == null || npc.getName() == null || npc instanceof Familiar)
			return null;
		ArrayList<Item> items = new ArrayList<Item>();
		for (TertiaryLoot loot : TertiaryLoot.values()) {
			if (npc == null)
				break;
			if (npc.getName().equalsIgnoreCase(loot.getNPCName())) {
				for (int i = 0; i < loot.getDrops().length; i++) {
					if (Utils.getRandomDouble(100) <= loot.getRate(i)) {
						items.add(loot.getDrop(i));
					}
				}
			}
		}
		Item[] list = new Item[items.size()];
		return items.size() == 0 ? null : items.toArray(list);
	}
	
}
