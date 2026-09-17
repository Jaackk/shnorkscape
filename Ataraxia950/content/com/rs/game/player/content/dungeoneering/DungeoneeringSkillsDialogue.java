package com.rs.game.player.content.dungeoneering;

import com.rs.cache.loaders.ItemDefinitions;
import com.rs.game.player.Player;

public final class DungeoneeringSkillsDialogue {

	private static int[] items;

	public static void setItem(int... item) {
		items = item;
	}

	public static int getItem(int slot) {
		return items[slot];
	}

	public static void sendSkillsDialogue(Player player, String explanation, int[] items) {
		player.getInterfaceManager().sendChatBoxInterface(944);
		player.getPackets().sendGlobalString(131, explanation);
		player.getPackets().sendGlobalConfig(754, items.length);
		for (int i = 0; i < 10; i++) {
			if (i >= items.length) {
				player.getPackets().sendGlobalConfig(i >= 6 ? (1139 + i - 6) : 755 + i, -1);
				continue;
			}
			player.getPackets().sendGlobalConfig(i >= 6 ? (1139 + i - 6) : 755 + i, items[i]);
			String name = ItemDefinitions.getItemDefinitions(items[i]).getName();
			player.getPackets().sendGlobalString(i >= 6 ? (280 + i - 6) : 132 + i, name);
		}
		setItem(items);
	}
	
	public static int getItemSlot(int componentId) {
		if (componentId == 81)
			return 0;
		else
			return (componentId - 22) / 6;
	}

	public static int getAmount(int componentId) {
		switch(componentId) {
		case 81:
		case 28:
		case 34:
		case 40:
		case 46:
		case 52:
		case 58:
		case 64:
		case 70:
		case 76:
			return 1;
		case 80:
		case 27:
		case 33:
		case 39:
		case 45:
		case 51:
		case 57:
		case 63:
		case 69:
		case 75:
			return 5;
		case 79:
		case 26:
		case 32:
		case 38:
		case 44:
		case 50:
		case 56:
		case 62:
		case 68:
		case 74:
			return 10;
			default:
				return -1;
		}
	}
}