package com.rs.game.player.content.interfaces.combinations;

import com.rs.cache.loaders.ItemDefinitions;
import com.rs.game.World;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.network.packet.PacketDispatcher;

import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

/**
 * ataraxia-server
 * paolo 08/08/2019
 * #Shnek6969
 */
public class CombinationsInterface {

    public static int INTERFACE_ID = 127;
    public static int[] ITEM_LIST_COMPONENTS = {52, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63,
            64, 65, 66, 67, 68, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 91, 92, 93,
            94, 95, 96, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 110,
            111, 112, 113, 114, 115, 116, 117, 118, 119, 120
    };
    private static final int DESCRIPTION_COMPONENT = 70;
    private static final int CHANCE_COMPONENT = 69;
    private static final int REQUIRED_ITEMS_CONTAINER = 71;
    private static final int BUTTON_ID = 44;
    private static final int PREVIEW_CONTAINER = 74;

    public static void sendInterface(Player player) {
        player.getInterfaceManager().sendInterface(INTERFACE_ID);
        player.getPackets().sendHideComponents(INTERFACE_ID, true, ITEM_LIST_COMPONENTS);
        player.getPackets().sendHideIComponent(INTERFACE_ID, 43, true);
        player.getPackets().sendEmptyTextToComponents(INTERFACE_ID, CHANCE_COMPONENT, DESCRIPTION_COMPONENT);
        sendItemsList(player);
    }

    private static void sendItemsList(Player player) {
        for (int i = 0; i < CombinationData.values().length; i++) {
            if (i >= ITEM_LIST_COMPONENTS.length) {
                return;
            }
            player.getPackets().sendHideIComponent(INTERFACE_ID, ITEM_LIST_COMPONENTS[i], false);
            player.getPackets().sendText(INTERFACE_ID, ITEM_LIST_COMPONENTS[i], ItemDefinitions.getItemDefinitions(CombinationData.values()[i].productId).getName());
        }
    }

    private static void sendItemInfo(Player player, CombinationData combinationData) {
        player.getPackets().sendHideIComponent(INTERFACE_ID, 43, false);
        PacketDispatcher.sendItemsFull(player, INTERFACE_ID, 90, REQUIRED_ITEMS_CONTAINER, 3, 2, combinationData.getRequiredItems());
        PacketDispatcher.sendItemsFull(player, INTERFACE_ID, 91, PREVIEW_CONTAINER, 1, 1, combinationData.getProductId());
        player.getPackets().sendText(INTERFACE_ID, DESCRIPTION_COMPONENT, combinationData.getDescription());
        player.getPackets().sendText(INTERFACE_ID, CHANCE_COMPONENT, "Chance: " + combinationData.getChance() + "%");
    }

    public static void combine(Player player) {
        CombinationData combinationData = (CombinationData) player.getTemporaryAttributtes().get("CombinationData");
        if (combinationData != null) {
            if (!player.getInventory().hasItemsAmountOne(combinationData.getRequiredItems())) {
                player.getDialogueManager().startDialogue("SimpleMessage", "You do not have all the required items.");
                return;
            }
            for (int itemId : combinationData.getRequiredItems()) {
                player.getInventory().deleteItem(new Item(itemId, 1));
            }
            player.incrementOmnipotenceRoll(combinationData);
            int creationAttempts = player.getOmnipotenceAttempts(combinationData);
            String itemName = ItemDefinitions.getItemDefinitions(combinationData.getProductId()).getName();
            String formatting = ((itemName.contains("Gloves") || itemName.contains("Boots")) ? " a pair of " : (itemName.contains("Necklace") || itemName.contains("Ring")) ? " a " : " ");
            int roll = ThreadLocalRandom.current().nextInt(100);
            if (roll < combinationData.getChance()) {
                player.getInventory().addItem(combinationData.getProductId(), 1);
                player.getDialogueManager().startDialogue("SimpleMessage", "You have successfully created a " + itemName + ".");
                World.sendNews(player.getDisplayName() + " crafted" + formatting + itemName + "<col=D80000> on attempt #" + creationAttempts + "!", World.WORLD_NEWS);
            } else {
                player.getDialogueManager().startDialogue("SimpleMessage", "The combination failed, your items crumbled to dust.");
                player.sendMessage("You've attempted to create the " + itemName + " " + creationAttempts + (creationAttempts < 2 ? " time." : " times."));
            }
        }
    }


    public static void handleButtons(Player player, int componentId) {
        if (componentId == BUTTON_ID) {
            player.getDialogueManager().startDialogue("CombinationConfirmD");
            return;
        }
        for (int i = 0; i < ITEM_LIST_COMPONENTS.length; i++) {
            if (ITEM_LIST_COMPONENTS[i] == componentId) {
                player.getTemporaryAttributtes().put("CombinationData", CombinationData.values()[i]);
                sendItemInfo(player, CombinationData.values()[i]);
                return;
            }
        }
    }

}
