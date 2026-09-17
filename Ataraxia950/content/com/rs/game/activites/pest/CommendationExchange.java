package com.rs.game.activites.pest;

import com.rs.cache.loaders.ClientScriptMap;
import com.rs.cache.loaders.GeneralRequirementMap;
import com.rs.cache.loaders.ItemDefinitions;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.TemporaryAttributes.Key;
import com.rs.utils.Utils;

public class CommendationExchange {// 1875 TODO

	public static final double XP_MULTIPLIER = 3;
	// interface related stuff
	private static final int INTERFACE = 1011;


	private static void addXPForSkill(Player player, int skill, int rate) {
		if (player.getSkills().getLevelForXp(skill) < 25) {
			player.getPackets().sendGameMessage("You need a " + Skills.SKILL_NAME[skill] + " of at least 25 in order to gain experience.");
			return;
		}
		for (int i = 0; i < rate; i++) {
			if (!exchangeCommendation(player, 1)) {
				rate = i;
				break;
			}
		}
		double experience = calculateExperience(player, skill) * rate;
		player.getSkills().addXp(skill, (experience / 18) * XP_MULTIPLIER);
		player.getDialogueManager().startDialogue("SimpleMessage", "You gain " + Utils.getFormattedNumber((int) experience) + " experience in " + Skills.SKILL_NAME[skill] + ".");
	}

	private static double calculateExperience(Player player, int skill) {
		int level = player.getSkills().getLevelForXp(skill);
		int constant = 35;
		if (skill == Skills.MAGIC || skill == Skills.RANGE)
			constant = 32;
		else if (skill == Skills.PRAYER)
			constant = 18;
		return (Math.ceil(((level + 25) * (level - 24)) / 606) * constant) + constant;
	}

	private static boolean exchangeCommendation(Player player, int price) {
		int currentPoints = player.getPestPoints();
		if (currentPoints - price < 0) {
			player.sendMessage("You don't have enough Commendations remaining to complete this exchange.");
			return false;
		}
		player.setPestPoints(currentPoints - price);
		refreshPoints(player);
		return true;
	}
	
    public static void openExchangeShop(Player player) {
        player.getInterfaceManager().sendInterface(INTERFACE);
        player.getPackets().sendUnlockIComponentOptionSlots(INTERFACE, 12, 0, 500, 0, 1);
        openTab(player, 0);
        refreshPoints(player);
        if (player.getPestPoints() > 4000) {
            player.getPackets().sendMainInterfaceMessage(1, "You currently have "+player.getPestPoints() +" commendations, but interface can only show up to 4k.", true);
        }
    }
    
    public static void handleButton(Player player, int componentId, int slotId) {
        if (componentId >= 14 && componentId <= 16) {
            openTab(player, componentId - 14);
            selectItem(player, -1);
        } else if (componentId == 12) {
            selectItem(player, slotId);
        } else if (componentId == 3 || componentId >= 39 && componentId <= 41) {
            Integer tabIndex = (Integer) player.getTemporaryAttributtes().get(Key.COMMENDATION_TAB_INDEX);
            Integer itemSlotId = (Integer) player.getTemporaryAttributtes().get(Key.COMMENDATION_SELECTED_ITEM);
            if (itemSlotId == null || tabIndex == null)
                return;
            int amount = componentId == 3 ? 1 : componentId == 39 ? 10 : componentId == 40 ? 100 : Integer.MAX_VALUE;
            GeneralRequirementMap struct = GeneralRequirementMap.getMap(ClientScriptMap.getMap(ClientScriptMap.getMap(14121).getIntValue(tabIndex)).getIntValue(itemSlotId));
            switch(tabIndex) {
                case 0:
                    int skillId = struct.getIntValue(5998);
                    if (amount == Integer.MAX_VALUE) {
                        player.getPackets().sendGameMessage("You can't buy more than 100 at a time.");
                        return;
                    }
                    addXPForSkill(player, skillId, amount);
                    break;
                case 1:
                    if (itemSlotId > 7) {
                        player.getPackets().sendGameMessage("This item is not available atm.");
                        return;
                    }
                    if (!player.getSkills().hasRequiriments(Skills.ATTACK, 42, Skills.STRENGTH, 42, Skills.DEFENCE, 42, Skills.HITPOINTS, 42, Skills.RANGE, 42, Skills.MAGIC, 42, Skills.PRAYER,
                            22)) {
                        player.getPackets().sendGameMessage(
                                "You need an attack, strength, defence, constitution, range, and magic level of 42, and a prayer level of 22 in order to purchase void equipment.");
                        return;
                    }
                    int cost = struct.getIntValue(4850);
                    if (!exchangeCommendation(player, cost))
                        return;
                    int voidItem = struct.getIntValue(4851);
                    player.addItem(new Item(voidItem));
                    player.getDialogueManager().startDialogue("ItemMessage",
                            "You exchange " + cost + " commendation points for a " + ItemDefinitions.getItemDefinitions(voidItem).getName().toLowerCase() + ".", voidItem);
                    break;
                case 2:
                    cost = struct.getIntValue(4850);
                    if (amount == Integer.MAX_VALUE)
                        amount = player.getPestPoints() / cost;
                    int totalCost = cost * amount;
                    if (!exchangeCommendation(player, totalCost))
                        return;
                    if (itemSlotId >= 1 && itemSlotId <= 3) {
                        Item r = packs[itemSlotId - 1][Utils.random(packs[itemSlotId - 1].length)];
                        voidItem = r.getId();
                        amount = r.getAmount();
                    } else
                        voidItem = struct.getIntValue(4851);
                    player.addItem(new Item(voidItem, amount));
                    player.getDialogueManager().startDialogue("SimpleItemMessage", ItemDefinitions.getItemDefinitions(voidItem).isNoted() ? ItemDefinitions.getItemDefinitions(voidItem).getCertId() : voidItem, amount, 
                            "You exchange " + totalCost + " commendation points for "+amount+" x " + ItemDefinitions.getItemDefinitions(voidItem).getName().toLowerCase() + ".");
                    break;
            }
        }
    }
    
    public static final Item[][] packs = { { new Item(216, 25), new Item(208, 20), new Item(210, 20), new Item(214, 20), new Item(3050, 16), new Item(212, 16), new Item(3052, 16), new Item(218, 8), new Item(2486, 8)}
    
     , { new Item(454, 100), new Item(441, 100), new Item(445, 75), new Item(448, 30), new Item(450, 20), new Item(452, 5)}, 
     { new Item(5295, 16), new Item(5100, 16), new Item(5301, 16), new Item(5296, 16), new Item(5300, 16), new Item(5298, 12), new Item(5321, 12), new Item(5302, 1), new Item(5303, 1)}};
    
	private static void refreshPoints(Player player) {
		player.getPackets().sendConfigByFile(4861, player.getPestPoints() > 4000 ? 4000 : player.getPestPoints());
	}
	
	public static void openTab(Player player, int tabIndex) {
	    player.getTemporaryAttributtes().put(Key.COMMENDATION_TAB_INDEX, tabIndex);
	    player.getPackets().sendExecuteScriptReverse(14276, tabIndex);
	}
	
	public static void selectItem(Player player, int slotId) {
	    if (slotId == -1) {
	        player.getTemporaryAttributtes().remove(Key.COMMENDATION_SELECTED_ITEM); 
	        return;
	    }
	    player.getTemporaryAttributtes().put(Key.COMMENDATION_SELECTED_ITEM, slotId);
	    player.getPackets().sendConfig(7992, slotId);
	}
}