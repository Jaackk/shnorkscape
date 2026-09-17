package com.rs.game.player.content.dungeoneering;

import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.mining.MiningBase.PickaxeDefinitions;
import com.rs.game.player.actions.woodcutting.WoodcuttingDefinitions.HatchetDefinitions;
import com.rs.utils.Utils;

import java.io.Serializable;

public class DungeoneeringToolbelt implements Serializable {

	private static final long serialVersionUID = -6709241151963353181L;

	private static final int[] BITCONFIGS = new int[] { 4293, 11047, 11051, 11048, 11053, 11050, 11052, 11049 };
	private static final int[] VALID_TOOLS = new int[] { 17754, 17883, 17444, 17794, 17446, 17678 };
	
	private final int[] TOOLS = new int[8];
	
	private transient Player player;
	
	public DungeoneeringToolbelt(Player player) {
		this.player = player;
	}
	
	public void setPlayer(final Player player) {
		this.player = player;
	}
	
	public void switchToolbelt(boolean dungeoneering) {
		player.getPackets().sendGlobalConfig(1725, dungeoneering ? 11 : 1);
	}
	
	public boolean containsTool(int itemId) {
		if (itemId >= 16295 && itemId <= 16315 && TOOLS[0] != 0) {
			return TOOLS[0] >= itemId;
		} else if (itemId >= 16361 && itemId <= 16381 && TOOLS[1] != 0) {
			return TOOLS[1] >= itemId;
		} else {
			int index = 2;
			for (int tools : VALID_TOOLS) {
				if (tools == itemId) {
					return TOOLS[index] != 0;
				}
				index++;
			}
		}
		return false;
	}
	
	public boolean addTool(Item item) {
		if (item.getId() >= 16295 && item.getId() <= 16315) {
			if (TOOLS[0] > item.getId()) {
				player.sendMessage("Your toolbelt already contains a superior pickaxe.");
				return true;
			} else if (TOOLS[0] == item.getId()) {
				player.sendMessage("Your toolbelt already contains this pickaxe.");
				return true;
			} else {
				PickaxeDefinitions definitions = null;
				for (PickaxeDefinitions defs : PickaxeDefinitions.values()) {
					if (defs.getPickAxeId() == item.getId()) {
						definitions = defs;
						break;
					}
				}
				if (definitions == null)
					return true;
				if (player.getSkills().getLevelForXp(Skills.MINING) < definitions.getLevelRequried())
					player.sendMessage("Your Mining level is too low to use this pickaxe. It will act as the highest tier pickaxe your Mining level allows until you are able to use it.");
				else
					player.sendMessage("You add the " + item.getDefinitions().getName().toLowerCase() + " to your toolbelt.");
				player.getInventory().deleteItem(item);
				if (TOOLS[0] != 0)
					player.getInventory().addItem(TOOLS[0], 1);
				TOOLS[0] = item.getId();
				return true;
			}
		} else if (item.getId() >= 16361 && item.getId() <= 16391) {
			if (TOOLS[1] > item.getId()) {
				player.sendMessage("Your toolbelt already contains a superior hatchet.");
				return true;
			} else if (TOOLS[1] == item.getId()) {
				player.sendMessage("Your toolbelt already contains this hatchet.");
				return true;
			} else {
				HatchetDefinitions definitions = null;
				for (HatchetDefinitions defs : HatchetDefinitions.values()) {
					if (defs.getItemId() == item.getId()) {
						definitions = defs;
						break;
					}
				}
				if (definitions == null)
					return true;
				if (player.getSkills().getLevelForXp(Skills.WOODCUTTING) < definitions.getLevelRequired()) 
					player.sendMessage("Your Woodcutting level is too low to use this hatchet. It will act as the highest tier hatchet your Woodcutting level allows until you are able to use it.");
				else
					player.sendMessage("You add the " + item.getDefinitions().getName().toLowerCase() + " to your toolbelt.");
				player.getInventory().deleteItem(item);
				if (TOOLS[1] != 0)
					player.getInventory().addItem(TOOLS[1], 1);
				TOOLS[1] = item.getId();
				return true;
			}
		} else {
			int index = 2;
			for (int tool : VALID_TOOLS) {
				if (item.getId() == tool) {
					if (TOOLS[index] != item.getId()) {
						player.sendMessage("You add the " + item.getName().toLowerCase() + " to your toolbelt.");
						TOOLS[index] = item.getId();
						player.getInventory().deleteItem(item);
					} else 
						player.sendMessage("Your toolbelt already contains " + Utils.formatAorAn(item) + item.getName().toLowerCase() + ".");
					break;
				}
				index++;
			}
		}
		return false;
	}
	
	public void openToolbelt() {
		refreshToolbelt();
		player.getInterfaceManager().sendInterface(1178);
	}
	
	private void refreshToolbelt() {
		for (int i = 0; i < TOOLS.length; i++) {
			if (i < 2) {
				if (TOOLS[i] == 0)
					player.getPackets().sendConfigByFile(BITCONFIGS[i], 0);
				else
					player.getPackets().sendConfigByFile(BITCONFIGS[i], i == 0 ? (TOOLS[0] - 16293) / 2 : (TOOLS[1] - 16359) / 2);
			} else
				player.getPackets().sendConfigByFile(BITCONFIGS[i], TOOLS[i] == 0 ? 0 : 1);
		}
	}
	
}
