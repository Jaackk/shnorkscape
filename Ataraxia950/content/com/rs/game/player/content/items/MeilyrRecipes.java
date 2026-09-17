package com.rs.game.player.content.items;

import com.rs.cache.loaders.ClientScriptMap;
import com.rs.cache.loaders.ItemDefinitions;
import com.rs.game.player.Player;
import com.rs.game.player.TemporaryAttributes.Key;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.Colors;
import com.rs.utils.Utils;

/**
 * @author Tom
 * @date April 20, 2017
 */

public class MeilyrRecipes {

	public static void openRecipeShop(Player player) {
		player.getInterfaceManager().sendInterface(1555);
		player.getPackets().sendConfig(4902, 0);
        player.getTemporaryAttributtes().put(Key.MEILYR_RECIPE_SHOP_SLOT_ID, 0);
		player.getPackets().sendUnlockIComponentOptionSlots(1555, 8, 0, 30, 0);
	}

	public static void handleShop(Player player, int componentId, int slotId) {
	    if (componentId == 8) {
	        player.getTemporaryAttributtes().put(Key.MEILYR_RECIPE_SHOP_SLOT_ID, slotId);
	    } else if (componentId == 22) {
	        Integer slot = (Integer) player.getTemporaryAttributtes().get(Key.MEILYR_RECIPE_SHOP_SLOT_ID);
	        if (slot == null)
	            slot = 0;
	        int itemId = ClientScriptMap.getMap(9448).getIntValue(slot);
	        ItemDefinitions defs = ItemDefinitions.getItemDefinitions(itemId);
	        int recipeSlot = defs.getCSOpcode(2646);
	        final int settingsSlot = MeilyrRecipes.getIndexForSlot(recipeSlot, false) == -1 ? MeilyrRecipes.getIndexForSlot(recipeSlot, true) : MeilyrRecipes.getIndexForSlot(recipeSlot, false);
	        if (settingsSlot == -1) {
	           player.getPackets().sendGameMessage("This potion is not added yet.");
	           return;
	        }
	        final boolean settings2 = MeilyrRecipes.getIndexForSlot(recipeSlot, true) != -1;
	        int price = defs.getCSOpcode(4665);
	        if (price <= 0)
	            return;
            if (!player.hasMoney(price)) {
                player.sendMessage("You don't have enough coins to purchase this recipe.");
                return;
            }
            final String potionName = defs.getName().substring(0, defs.getName().length() - 3);
            if (settings2 ? player.meilyrShopSettings2[settingsSlot] : player.meilyrShopSettings[settingsSlot]) {
                player.sendMessage("You already unlocked the recipe for the " + potionName + ".");
                return;
            }
            player.getDialogueManager().startDialogue(new Dialogue() {
                
                @Override
                public void start() {
                    sendOptionsDialogue(
                            "Are you sure you would like to buy the recipe for <br> the " + potionName + " for " + Colors.GREEN + Utils.formatNumber(price) + "</col> coins?</br>",
                            "Yes.", "No.");
                }
                
                @Override
                public void run(int interfaceId, int componentId) {
                    end();
                    if (!player.hasMoney(price))
                        return;
                    if (componentId == OPTION_1) {
                        if (settings2)
                            player.meilyrShopSettings2[settingsSlot] = true;
                        else
                            player.meilyrShopSettings[settingsSlot] = true;
                        player.refreshUnlockedRecipes();
                        player.sendMessage(
                                "Congratulations! You have unlocked the " + potionName + " recipe.");
                        player.takeMoney(price);
                    }
                }
                
                @Override
                public void finish() {

                }
            });
	    }
	}
	
    public static int getIndexForSlot(int slotId, boolean index2) {
        if (!index2) {
            if (slotId >= 99 && slotId <= 105)
                return slotId - 99;
            switch (slotId) {
            case 118:
                return 7;
            case 108:
                return 8;
            case 110:
                return 9;
            case 109:
                return 10;
            case 112:
                return 11;
            case 113:
                return 12;
            case 114:
                return 13;
            case 115:
                return 14;
            case 116:
                return 15;
            }
            return -1;
        }
        switch (slotId) {
        case 107:
            return 0;
        case 117:
            return 1;
        case 106:
            return 2;
        case 122:
            return 3;
        case 123:
            return 4;
        case 124:
            return 5;
        case 119:
            return 6;
        case 120:
            return 7;
        case 121:
            return 9;
        }
        return -1;
    }

    public static int getVarbitIdForSlot(int slotId) {
        switch (slotId) {
        case 99:
            return 25954;
        case 100:
            return 25955;
        case 101:
            return 25956;
        case 102:
            return 25957;
        case 103:
            return 25958;
        case 104:
            return 25959;
        case 105:
            return 25960;
        case 106:
            return 25961;
        case 107:
            return 25962;
        case 108:
            return 25963;
        case 109:
            return 25964;
        case 110:
            return 25965;
        case 111:
            return 25966;
        case 112:
            return 25967;
        case 113:
            return 25968;
        case 114:
            return 25969;
        case 115:
            return 25970;
        case 116:
            return 25971;
        case 117:
            return 25972;
        case 118:
            return 25973;
        case 123:
            return 25978;
        case 124:
            return 25979;
        case 119:
            return 25974;
        case 120:
            return 25975;
        case 121:
            return 25976;
        case 122:
            return 25977;
        case 207:
            return 44557;
        }
        return -1;
    }

}