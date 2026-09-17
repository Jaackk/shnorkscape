package com.rs.game.player.dialogue.impl;

import java.util.ArrayList;
import java.util.List;

import com.rs.game.item.Item;
import com.rs.game.player.dialogue.Dialogue;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Xenthium.
 */

public class RefinedAnimaCoreRepairD extends Dialogue {

    private final int INTERFACE_ID = 1183;
    private Item armour;

    @Override
    public void start() {
        armour = (Item) parameters[0];
        if (canRepairItem()) {
            player.getInterfaceManager().sendChatBoxInterface(INTERFACE_ID);
            player.getPackets().sendItemOnIComponent(INTERFACE_ID, 8, armour.getId(), 1);
            player.getPackets().sendIComponentText(INTERFACE_ID, 13, "Are you sure you want to do this?");
            player.getPackets().sendIComponentText(INTERFACE_ID, 3, "Repair the " + armour.getName() + "?");
            player.getPackets().sendIComponentText(INTERFACE_ID, 1, "Repairing this item will consume one of each type of essence during the process.");
        } else {
            end();
        }
    }

    @Override
    public void run(int interfaceId, int componentId) {
        if (interfaceId == INTERFACE_ID && componentId == 5) {
            repairItem();
        }
        end();
    }

    @Override
    public void finish() {

    }

    private boolean canRepairItem() {
        if (armour == null || !player.getInventory().containsItem(armour) || armour.getChargesData() == null) {
            return false;
        }
        int maxCharges = armour.getChargesData().getMaxCharges();
        int chargesLeft = armour.getChargesData().getChargesLeft();
        if (chargesLeft >= maxCharges) {
            player.getPackets().sendGameMessage("Your "+armour.getName()+" is already fully charged.");
            return true;
        }
        for (int essence : ConsumableEssenceData.getEssenceItemIds()) {
            if (!player.getInventory().containsItem(new Item(essence))) {
                player.sendMessage("You are missing one or more type of essence required to repair that.");
                return false;
            }
        }
        return true;
    }

    private void repairItem() {
        if (canRepairItem()) {
            for (int essence : ConsumableEssenceData.getEssenceItemIds()) {
                player.getInventory().deleteItem(new Item(essence));
            }
            int maxCharges = armour.getChargesData().getMaxCharges();
            armour.getChargesData().setChargesLeft(maxCharges);
            player.getInventory().refresh();
            player.sendMessage("You've repaired the " + armour.getName() + " to full charges.");
        }
    }

    @AllArgsConstructor
    public enum RefinedArmourData {
        REFINED_ANIMA_CORE_HELM_OF_ZAROS(new Item(37036)), REFINED_ANIMA_CORE_BODY_OF_ZAROS(new Item(37039)), REFINED_ANIMA_CORE_LEGS_OF_ZAROS(new Item(37042)),

        REFINED_ANIMA_CORE_HELM_OF_ZAMORAK(new Item(37045)), REFINED_ANIMA_CORE_BODY_OF_ZAMORAK(new Item(37048)), REFINED_ANIMA_CORE_LEGS_OF_ZAMORAK(new Item(37051)),

        REFINED_ANIMA_CORE_HELM_OF_SEREN(new Item(37054)), REFINED_ANIMA_CORE_BODY_OF_SEREN(new Item(37057)), REFINED_ANIMA_CORE_LEGS_OF_SEREN(new Item(37060)),

        REFINED_ANIMA_CORE_HELM_OF_SLISKE(new Item(37063)), REFINED_ANIMA_CORE_BODY_OF_SLISKE(new Item(37066)), REFINED_ANIMA_CORE_LEGS_OF_SLISKE(new Item(37069));

        private static final List<Integer> refinedItemIds = new ArrayList<>();

        static {
            for (RefinedArmourData data : values()) {
                refinedItemIds.add(data.getItem().getId());
            }
        }

        @Getter
        private final Item item;

        public static List<Integer> getRefinedItemIds() {
            return refinedItemIds;
        }
    }

    @AllArgsConstructor
    public enum ConsumableEssenceData {
        ZAROSIAN_ESSENCE(new Item(37030)), SLISKEAN_ESSENCE(new Item(37031)), ZAMORAKIAN_ESSENCE(new Item(37032)), SIRENIC_ESSENCE(new Item(37033));

        private static final List<Integer> essenceItemIds = new ArrayList<>();

        static {
            for (ConsumableEssenceData data : values()) {
                essenceItemIds.add(data.getItem().getId());
            }
        }

        @Getter
        private final Item item;

        public static List<Integer> getEssenceItemIds() {
            return essenceItemIds;
        }
    }
}
