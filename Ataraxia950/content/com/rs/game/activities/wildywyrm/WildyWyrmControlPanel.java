package com.rs.game.activities.wildywyrm;

import com.rs.game.player.Player;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.InputIntegerEvent;

public class WildyWyrmControlPanel {

	public static final void sendInterface(Player player) {
		
		player.getPackets().sendHideIComponent(473, 34, true);
		player.getPackets().sendHideIComponent(473, 35, true);
		player.getPackets().sendIComponentText(473, 31, "WildyWyrm Control Panel");
		player.getPackets().sendIComponentText(473, 227, "Attack Styles");
		player.getPackets().sendIComponentText(473, 224, "Misc Options");
		player.getPackets().sendIComponentText(473, 5, "Health:");
		player.getPackets().sendIComponentText(473, 7, "Next attack:");
		player.getPackets().sendIComponentText(473, 6, "Time until next spawn:");
		player.getPackets().sendIComponentText(473, 8, "Players fighting:");
		player.getPackets().sendIComponentText(473, 44, "Dragging burrow special");
		player.getPackets().sendIComponentText(473, 46, "Drags all targets underneath it");
		player.getPackets().sendIComponentText(473, 45, "Default occurrence: Rare");
		player.getPackets().sendIComponentText(473, 58, "Melee attack");
		player.getPackets().sendIComponentText(473, 60, "Uses a deadly melee attack");
		player.getPackets().sendIComponentText(473, 59, "Default occurrence: Uncommon");
		player.getPackets().sendIComponentText(473, 51, "Ranged attack");
		player.getPackets().sendIComponentText(473, 53, "Uses a ranged attack");
		player.getPackets().sendIComponentText(473, 52, "Default occurrence: Common");
		player.getPackets().sendIComponentText(473, 65, "Magic attack");
		player.getPackets().sendIComponentText(473, 67, "Uses a magic attack");
		player.getPackets().sendIComponentText(473, 66, "Default occurrence: Common");
		player.getPackets().sendIComponentText(473, 127, "Set melee attack");
		player.getPackets().sendIComponentText(473, 129, "Sets melee attack to given level");
		player.getPackets().sendIComponentText(473, 128, "Current level:");
		player.getPackets().sendIComponentText(473, 120, "Set melee defence");
		player.getPackets().sendIComponentText(473, 122, "Sets melee defence to given level");
		player.getPackets().sendIComponentText(473, 121, "Current level:");
		player.getPackets().sendIComponentText(473, 113, "Set ranged attack");
		player.getPackets().sendIComponentText(473, 115, "Sets ranged attack to given level");
		player.getPackets().sendIComponentText(473, 114, "Current level:");
		player.getPackets().sendIComponentText(473, 106, "Set ranged defence");
		player.getPackets().sendIComponentText(473, 108, "Sets ranged defence to given level");
		player.getPackets().sendIComponentText(473, 107, "Current level:");
		player.getPackets().sendIComponentText(473, 99, "Set magic attack");
		player.getPackets().sendIComponentText(473, 101, "Sets magic attack to given level");
		player.getPackets().sendIComponentText(473, 100, "Current level:");
		player.getPackets().sendIComponentText(473, 92, "Set magic defence");
		player.getPackets().sendIComponentText(473, 94, "Sets magic defence to given level");
		player.getPackets().sendIComponentText(473, 93, "Current level:");
		player.getPackets().sendIComponentText(473, 86, "Teleport to WildyWyrm");
		player.getPackets().sendIComponentText(473, 88, "Teleports you to current location");
		player.getPackets().sendIComponentText(473, 80, "Heals WildyWyrm");
		player.getPackets().sendIComponentText(473, 82, "Heals WildyWyrm for given amount");
		WorldTasksManager.schedule(new WorldTask() {
			@Override
			public void run() {
				if (!player.getInterfaceManager().containsInterface(473)) {
					stop();
					return;
				}
				player.getPackets().sendIComponentText(473, 9, WildyWyrm.getWildywyrm().getCurrentHealth());
				player.getPackets().sendIComponentText(473, 11, WildyWyrm.getWildywyrm().getNextAttack());
				player.getPackets().sendIComponentText(473, 10, WildyWyrm.getWildywyrm().getTimeUntilNextSpawn());
				player.getPackets().sendIComponentText(473, 12, WildyWyrm.getWildywyrm().getPlayersFighting());
				player.getPackets().sendIComponentText(473, 130, WildyWyrm.getWildywyrm().getBonus(0));
				player.getPackets().sendIComponentText(473, 123, WildyWyrm.getWildywyrm().getBonus(1));
				player.getPackets().sendIComponentText(473, 116, WildyWyrm.getWildywyrm().getBonus(2));
				player.getPackets().sendIComponentText(473, 109, WildyWyrm.getWildywyrm().getBonus(3));
				player.getPackets().sendIComponentText(473, 102, WildyWyrm.getWildywyrm().getBonus(4));
				player.getPackets().sendIComponentText(473, 95, WildyWyrm.getWildywyrm().getBonus(5));
				player.getPackets().sendIComponentText(473, 87, "Location: " + WildyWyrm.getWildywyrm().getWildyWyrmLocation());
				player.getPackets().sendIComponentText(473, 81, WildyWyrm.getWildywyrm().getState());
			}
		}, 0, 1);
		player.getInterfaceManager().sendInterface(473);
	}
		
	public static final boolean handleInterfaceButtons(Player player, int interfaceId, int componentId) {
		if (interfaceId != 473)
			return false;
		String message = null;
		switch(componentId) {
		case 40:
			WildyWyrm.getWildywyrm().setAttackStyle(player, 0);
			break;
		case 54:
			WildyWyrm.getWildywyrm().setAttackStyle(player, 1);
			break;
		case 47:
			WildyWyrm.getWildywyrm().setAttackStyle(player, 2);
			break;
		case 61:
			WildyWyrm.getWildywyrm().setAttackStyle(player, 3);
			break;
		case 69:
			message = "What level would you like to set WildyWyrm's melee attack to?";
			break;
		case 70:
			message = "What level would you like to set WildyWyrm's melee defence to?";
			break;
		case 71:
			message = "What level would you like to set WildyWyrm's ranged attack to?";
			break;
		case 72:
			message = "What level would you like to set WildyWyrm's ranged defence to?";
			break;
		case 73:
			message = "What level would you like to set WildyWyrm's magic attack to?";
			break;
		case 74:
			message = "What level would you like to set WildyWyrm's magic defence to?";
			break;
		case 75:
			WildyWyrm.getWildywyrm().teleportToWildyWyrm(player);
			break;
		case 76:
			message = "How much would you like to heal WildyWyrm for?";
			break;
		}
		if (message != null) {
			player.sendInputInteger(message, new InputIntegerEvent() {
				@Override
				public void run(Player player) {
					WildyWyrm.getWildywyrm().handleBonuses(player, componentId, getInteger());
				}
				
			});
		}
		return true;
	}
}
