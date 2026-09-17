package com.rs.game.player.dialogue.impl;

import com.rs.game.item.Item;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.Colors;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.val;

import java.util.HashMap;

/**
 * @author Xenthium/Toby.
 */

public class ImbueItem extends Dialogue {

    public static final int IMBUED_GEAR_ID = 41407;
    private Item uimbuedItem, imbuedGear;

    @Override
    public void start() {
        uimbuedItem = (Item) parameters[0];
        imbuedGear = (Item) parameters[1];
        promptPlayer();
    }

    @Override
    public void run(int interfaceId, int componentId) {
        if (interfaceId == 1183 && componentId == 9) {
            handleImbuingProcess();
        } else {
            end();
        }
    }

    @Override
    public void finish() {

    }

    private void handleImbuingProcess() {
        val newItem = new Item(getImbuedVariant(uimbuedItem.getId()));
        if (uimbuedItem == null || imbuedGear == null || !ItemData.getImbuedItemIdForUnimbuedItemId().containsValue(newItem.getId()) || !player.getInventory().containsItem(uimbuedItem) || !player.getInventory().containsItem(imbuedGear)) {
            player.sendMessage(Colors.RED + "If you see this, something went wrong during the imbuing process. Please report it to a staff member.");
            end();
            return;
        }
        sendItemDialogue(newItem.getId(), 1, "You imbue the " + uimbuedItem.getName() + ", creating the more powerful variant!");
        player.getInventory().deleteItem(uimbuedItem);
        player.getInventory().deleteItem(imbuedGear);
        player.addItem(newItem);
    }

    private void promptPlayer() {
        val interfaceId = 1183;
        player.getInterfaceManager().sendChatBoxInterface(interfaceId);
        player.getPackets().sendItemOnIComponent(interfaceId, 13, uimbuedItem.getId(), 1);
        player.getPackets().sendIComponentText(interfaceId, 7, "Are you sure you want to do this?");
        player.getPackets().sendIComponentText(interfaceId, 22, "Imbue the " + uimbuedItem.getName() + "?");
        player.getPackets().sendIComponentText(interfaceId, 12, Colors.wrap(Colors.RED + Colors.SHAD, "WARNING: ") + "Imbuing the " + uimbuedItem.getName() + " is irreversible and will make the item permanently untradable!");
    }

    private Integer getImbuedVariant(final int itemId) {
        return ItemData.getImbuedItemIdForUnimbuedItemId().get(itemId);
    }

    public static boolean itemIsCompatible(final int itemId) {
        return ItemData.getImbuedItemIdForUnimbuedItemId().containsKey(itemId);
    }


    @AllArgsConstructor
    private enum ItemData {
        OFFHAND_ASCENSION_CROSSBOW(28441, 41413),
        OFFHAND_ASCENSION_CROSSBOW_ALTERNATE(28443, 41413),
        OFFHAND_ASCENSION_CROSSBOW_BARROWS(33321, 41414),
        OFFHAND_ASCENSION_CROSSBOW_BARROWS_ALTERNATE(33322, 41414),
        OFFHAND_ASCENSION_CROSSBOW_BLOOD(36324, 41417),
        OFFHAND_ASCENSION_CROSSBOW_BLOOD_ALTERNATE(36325, 41417),
        OFFHAND_ASCENSION_CROSSBOW_SHADOW(33387, 41415),
        OFFHAND_ASCENSION_CROSSBOW_SHADOW_ALTERNATE(33388, 41415),
        OFFHAND_ASCENSION_CROSSBOW_THIRD_AGE(33453, 41416),
        OFFHAND_ASCENSION_CROSSBOW_THIRD_AGE_ALTERNATE(33454, 41416),

        OFFHAND_DRYGORE_LONGSWORD(26591, 41392),
        OFFHAND_DRYGORE_LONGSWORD_ALTERNATE(26593, 41392),
        OFFHAND_DRYGORE_LONGSWORD_BARROWS(33315, 41393),
        OFFHAND_DRYGORE_LONGSWORD_BARROWS_ALTERNATE(33316, 41393),
        OFFHAND_DRYGORE_LONGSWORD_BLOOD(36318, 41396),
        OFFHAND_DRYGORE_LONGSWORD_BLOOD_ALTERNATE(36319, 41396),
        OFFHAND_DRYGORE_LONGSWORD_SHADOW(33381, 41394),
        OFFHAND_DRYGORE_LONGSWORD_SHADOW_ALTERNATE(33382, 41394),
        OFFHAND_DRYGORE_LONGSWORD_THIRD_AGE(33447, 41395),
        OFFHAND_DRYGORE_LONGSWORD_THIRD_AGE_ALTERNATE(33448, 41395),

        OFFHAND_DRYGORE_MACE(26599, 41402),
        OFFHAND_DRYGORE_MACE_ALTERNATE(26601, 41402),
        OFFHAND_DRYGORE_MACE_BARROWS(33303, 41403),
        OFFHAND_DRYGORE_MACE_BARROWS_ALTERNATE(33304, 41403),
        OFFHAND_DRYGORE_MACE_BLOOD(36306, 41406),
        OFFHAND_DRYGORE_MACE_BLOOD_ALTERNATE(36307, 41406),
        OFFHAND_DRYGORE_MACE_SHADOW(33369, 41404),
        OFFHAND_DRYGORE_MACE_SHADOW_ALTERNATE(33370, 41404),
        OFFHAND_DRYGORE_MACE_THIRD_AGE(33435, 41405),
        OFFHAND_DRYGORE_MACE_THIRD_AGE_ALTERNATE(33436, 41405),

        OFFHAND_DRYGORE_RAPIER(26583, 41397),
        OFFHAND_DRYGORE_RAPIER_ALTERNATE(26585, 41397),
        OFFHAND_DRYGORE_RAPIER_BARROWS(33309, 41398),
        OFFHAND_DRYGORE_RAPIER_BARROWS_ALTERNATE(33310, 41398),
        OFFHAND_DRYGORE_RAPIER_BLOOD(36312, 41401),
        OFFHAND_DRYGORE_RAPIER_BLOOD_ALTERNATE(36313, 41401),
        OFFHAND_DRYGORE_RAPIER_SHADOW(33375, 41399),
        OFFHAND_DRYGORE_RAPIER_SHADOW_ALTERNATE(33376, 41399),
        OFFHAND_DRYGORE_RAPIER_THIRD_AGE(33441, 41400),
        OFFHAND_DRYGORE_RAPIER_THIRD_AGE_ALTERNATE(33442, 41400),

        SEISMIC_SINGULARITY(28621, 41408),
        SEISMIC_SINGULARITY_ALTERNATE(28623, 41408),
        SEISMIC_SINGULARITY_BARROWS(33327, 41409),
        SEISMIC_SINGULARITY_BARROWS_ALTERNATE(33328, 41409),
        SEISMIC_SINGULARITY_BLOOD(36330, 41412),
        SEISMIC_SINGULARITY_BLOOD_ALTERNATE(36331, 41412),
        SEISMIC_SINGULARITY_SHADOW(33393, 41410),
        SEISMIC_SINGULARITY_SHADOW_ALTERNATE(33394, 41410),
        SEISMIC_SINGULARITY_THIRD_AGE(33459, 41411),
        SEISMIC_SINGULARITY_THIRD_AGE_ALTERNATE(33460, 41411),
    	
    	DECIMATION(39053, 39234),
    	DECIMATION_DEGRADED(39055, 39234),
    	OBLITERATION(39057, 39232),
    	OBLITERATION_DEGRADED(39059, 39232),
    	ANNIHILATION(39049, 39236),
    	ANNIHILATION_DEGRADED(39051, 39236);

        @Getter
        private static final HashMap<Integer, Integer> imbuedItemIdForUnimbuedItemId = new HashMap<>();

        static {
            for (ItemData data : values()) {
                imbuedItemIdForUnimbuedItemId.put(data.getUnimbuedItemId(), data.getImbuedItemId());
            }
        }

        @Getter
        private final int unimbuedItemId;
        @Getter
        private final int imbuedItemId;

    }

}
