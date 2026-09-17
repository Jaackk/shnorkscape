package com.rs.game.player.content.dungeoneering;

import com.rs.game.item.Item;
import com.rs.game.player.Equipment;
import com.rs.game.player.Player;

import java.io.Serializable;

public class RingOfKinship implements Serializable {

	private static final long serialVersionUID = -1228327398864675991L;

	private final int[] TIERS = new int[12];
	private int activeClass = -1, quickSwitch = -1;
	private transient Player player;
	
	public RingOfKinship(Player player) {
		this.player = player;
	}
	
	public Item getCurrentRing() {
		if (activeClass == -1)
			return new Item(15707, 1);
		return new Item(18817 + activeClass, 1);
	}
	
	public void setPlayer(Player player) {
		this.player = player;
	}
	
	public static final int TANK = 0, TACTICIAN = 1, BERSERKER = 2, 
			SNIPER = 3, KEEN_EYE = 4, DESPERADO = 5, 
			BLAZER = 6, BLASTER = 7, BLITZER = 8, 
			MEDIC = 9, GATHERER = 10, ARTISAN = 11;
	
	private static final int[] UPGRADE_COSTS = new int[] { 135, 175, 335, 660, 1360, 3400, 6800, 18750, 58600, 233000 };
	
	public void upgradeTier(int componentId, boolean warn) {
		if (warn) {
			int tabId = player.getTemporaryAttributtes().get("ringofkinship") == null ? 0 : (int) player.getTemporaryAttributtes().get("ringofkinship");
			int classId = (tabId * 3) + (componentId == 139 ? 0 : componentId == 46 ? 1 : 2);
			player.getInterfaceManager().closeScreenInterface();
			player.getDialogueManager().startDialogue("RingOfKinshipUpgradeD", classId, UPGRADE_COSTS[TIERS[classId]]);
		} else {
			int cost = UPGRADE_COSTS[TIERS[componentId]];
			if (player.getDungeoneeringManager().getTokens() < cost) {
				player.sendMessage("You need at least " + cost + " dungeoneering tokens to upgrade this class.");
				return;
			}
			player.getDungeoneeringManager().setTokens(player.getDungeoneeringManager().getTokens() - cost);
			TIERS[componentId]++;
			player.sendMessage("You've just upgraded a " + getClassName(componentId) + " tier. You have reached tier " + TIERS[componentId] + ".");
			player.sendMessage("You have " + player.getDungeoneeringManager().getTokens() + " Dungeoneering tokens remaining.");
			refreshTiers();
			openInterface(componentId / 3);
		}
	}
	
	public void openInterface(Item item) {
		if (item.getId() == 15707) 
			openInterface(-1);
		else
			openInterface((item.getId() - 18817) / 3);
	}
	
	private void openInterface(int tabId) {
		refreshTiers();
		player.getInterfaceManager().sendInterface(993);
		if (tabId != -1)
			player.getPackets().sendRunScript(3494, 993 << 16 | (257 - (15 * tabId)));
		player.setCloseInterfacesEvent(new Runnable() {
			@Override
			public void run() {
				player.getTemporaryAttributtes().remove("ringofkinship");
			}
		});
	}
	
	private void refreshTiers() {
		for (int i = 0; i < 12; i++)
			player.getPackets().sendConfigByFile(8053 + i, TIERS[i]);
			player.getPackets().sendIComponentText(993, 138, "Switch-to");
			player.getPackets().sendIComponentText(993, 45, "Switch-to");
			player.getPackets().sendIComponentText(993, 87, "Switch-to");
		int tabId = player.getTemporaryAttributtes().get("ringofkinship") == null ? 0 : (int) player.getTemporaryAttributtes().get("ringofkinship");
		if (tabId == activeClass / 3) {
			if (activeClass - (3 * tabId) == 0)
				player.getPackets().sendIComponentText(993, 138, "In use");
			if (activeClass - (3 * tabId) == 1)
				player.getPackets().sendIComponentText(993, 45, "In use");
			if (activeClass - (3 * tabId) == 2)
				player.getPackets().sendIComponentText(993, 87, "In use");
		}
	}
	
	public void quickSwitch(Item ring) {
		if (quickSwitch == -1) {
			player.sendMessage("You have no ring selected to quick-switch to. Select 'Customise' and pick a secondary ring to quick-switch to.");
			return;
		}
		int ringId = ring.getId();
		ring.setId(quickSwitch);
		player.getInventory().refresh();
		player.sendMessage("You quick-switch your ring of kinship.");
		quickSwitch = ringId;
		activeClass = ring.getId() - 18817;
	}
	
	private static final int[] SKILLER_BOOSTS = new int[] { 0, 20, 23, 27, 30, 33, 37, 40, 43, 47, 50 };
	private static final int[] KEEN_EYE_BOOSTS = new int[] { 0, 40, 47, 53, 60, 67, 73, 80, 87, 93, 100 };
	private static final int[] SNIPER_BOOSTS = new int[] { 0, 17, 20, 23, 27, 30, 33, 37, 40, 43, 47 };
	
	public int getTier(int classId) {
		return TIERS[classId];
	}
	
	public int getBoost(int classId) {
		if (activeClass != classId)
			return 0;
			if (TIERS[classId] == 0)
				return 0;
			switch(classId) {
			case TANK:
				return 5 + TIERS[classId];
			case SNIPER:
				return SNIPER_BOOSTS[TIERS[classId]];
			case KEEN_EYE:
				return KEEN_EYE_BOOSTS[TIERS[classId]];
			case DESPERADO:
			case TACTICIAN:
			case BERSERKER:
				return 10 + TIERS[classId];
			case BLAZER:
				return 5 * TIERS[classId];
			case BLITZER:
				return 10 * TIERS[classId];
			case BLASTER:
				return 10 + (2 * TIERS[classId]);
				default:
					return SKILLER_BOOSTS[TIERS[classId]];
				
			}
	}
	
	public String getClassName(int tier) {
		switch(tier) {
		case 0:
			return "Tank";
		case 1:
			return "Tactician";
		case 2:
			return "Berserker";
		case 3:
			return "Sniper";
		case 4:
			return "Keen-eye";
		case 5:
			return "Desperado";
		case 6:
			return "Blazer";
		case 7:
			return "Blaster";
		case 8:
			return "Blitzer";
		case 9:
			return "Medic";
		case 10:
			return "Gatherer";
			default:
				return "Artisan";
		}
	}
	
	public boolean handleButtons(Player player, int interfaceId, int componentId, int slotId, int slotId2, int packetId) {
		if (interfaceId == 993) {
			if (componentId == 1)
				player.getTemporaryAttributtes().put("ringofkinship", 0);
			else if (componentId == 242)
				player.getTemporaryAttributtes().put("ringofkinship", 1);
			else if (componentId == 227)
				player.getTemporaryAttributtes().put("ringofkinship", 2);
			else if (componentId == 212)
				player.getTemporaryAttributtes().put("ringofkinship", 3);
			refreshTiers();
			if (componentId == 46 || componentId == 88 || componentId == 139) {
				upgradeTier(componentId, true);
				return true;
			} else if (componentId == 137 || componentId == 44 || componentId == 86) {
				int tabId = player.getTemporaryAttributtes().get("ringofkinship") == null ? 0 : (int) player.getTemporaryAttributtes().get("ringofkinship");
				int classId = (tabId * 3) + (componentId == 137 ? 0 : componentId == 44 ? 1 : 2);
				if (packetId == 67) {
					if (quickSwitch == 18817 + classId) {
						player.sendMessage("You already have " + getClassName(classId) + " selected as your quick-switch.");
					} else {
						quickSwitch = 18817 + classId;
						player.sendMessage("You've set your quick-switch ring class to " + getClassName(classId) + ".");
					}
				} else {
					if (activeClass == classId) {
						player.sendMessage("You already have " + getClassName(classId) + " selected as your ring of kinship class.");
					} else {
						for (Item items : player.getInventory().getItems().getItems()) {
							if (items == null)
								continue;
							if (items.getId() == 18817 + activeClass)
								items.setId(18817 + classId);
						}
						if (player.getEquipment().getRingId() == 18817 + activeClass)
							player.getEquipment().set(Equipment.SLOT_RING, new Item(18817 + activeClass));
						player.getInventory().refresh();
						player.getEquipment().refresh(Equipment.SLOT_RING);
						activeClass = classId;
						player.sendMessage("You've selected " + getClassName(classId) + " as your ring of kinship class.");
					}
				}
				refreshTiers();
			}
			return true;
		}
		return false;
	}
	
}
