package com.rs.game.player.content;

import com.rs.cache.loaders.GeneralRequirementMap;
import com.rs.game.item.Item;
import com.rs.game.player.Equipment;
import com.rs.game.player.Player;
import com.rs.game.player.CosmeticsManager.PurchaseItem;
import com.rs.game.player.content.distinctioncape.DistinctionCape;
import com.rs.game.player.dialogue.Dialogue;

public class CosmeticsHandler {

    public static final int KEEP_SAKE_KEY = 25430;
    public static final int DEFAULT_PRICE_FULL_OUTFIT = 50;
    public static final int DEFAULT_PRICE_SINGLE_PIECE = 10;

    public static boolean keepSakeItem(Player player, Item itemUsed, Item itemUsedWith) {
        if (itemUsed.getId() != KEEP_SAKE_KEY && itemUsedWith.getId() != KEEP_SAKE_KEY)
            return false;
        if (itemUsed.getId() == KEEP_SAKE_KEY && itemUsedWith.getId() == KEEP_SAKE_KEY)
            return false;
        Item keepSakeKey = itemUsed.getId() == KEEP_SAKE_KEY ? itemUsed : itemUsedWith;
        Item keepSakeItem = itemUsed.getId() == KEEP_SAKE_KEY ? itemUsedWith : itemUsed;
        if (keepSakeItem == null || keepSakeKey == null)
            return false;
        if (player.getEquipment().getKeepSakeItems().size() >= 100) {
            player.getPackets().sendGameMessage("You can only keep sake 100 items.");
            return false;
        }
        int equipSlot = keepSakeItem.getDefinitions().getEquipSlot();
        if (equipSlot == Equipment.SLOT_ARROWS || equipSlot == Equipment.SLOT_AURA || equipSlot == Equipment.SLOT_RING) {
            player.getPackets().sendGameMessage("You can only keep sake items that goes into head, cape, neck, body, legs, gloves, main hand, off-hand, or boots slots.");
            return false;
        }
        if (equipSlot == -1) {
            player.getPackets().sendGameMessage("You can't keep sake this item as its not wearable.");
            return false;
        }
        if (!ItemConstants.canWear(keepSakeItem, player, true)) {
            player.getPackets().sendGameMessage("You don't have enough requirments to keep sake this item.");
            return false;
        }
        if (keepSakeItem.getDefinitions().isBindItem() || keepSakeItem.getDefinitions().isLended() || keepSakeItem.getDefinitions().isStackable())
            return false;
        String name = keepSakeItem.getName().toLowerCase();
        if (name.contains("broken")) {
            player.getPackets().sendGameMessage("You can't keep sake broken items.");
            return false;
        }
        if (keepSakeItem.getAttributes() != null) {
            player.getPackets().sendGameMessage("You can only keepsake stackable items (must have no attributes).");
            return false;
        }
        for (Item item : player.getEquipment().getKeepSakeItems()) {
            if (item == null)
                continue;
            if (item.getId() == keepSakeItem.getId()) {
                player.getPackets().sendGameMessage("You already have that item in your keepsake box.");
                return false;
            }
        }
        player.stopAll();
        player.getDialogueManager().startDialogue(new Dialogue() {

            @Override
            public void start() {
                sendOptionsDialogue("DO YOU WANT TO KEEP SAKE THIS ITEM?", "Yes, keep sake this item.(You won't be able to retrieve key)", "No, I would like to keep it.");
            }

            @Override
            public void run(int interfaceId, int componentId) {
                if (componentId == OPTION_1 && player.getInventory().containsItem(keepSakeItem)) {
                    int index = player.getCosmeticsManager().getNextIndex();
                    if (index == -1)
                        player.getEquipment().getKeepSakeItems().add(keepSakeItem);
                    else
                        player.getEquipment().getKeepSakeItems().set(index, keepSakeItem);
                    player.getPackets().sendItems(675, player.getEquipment().getKeepSakeItems().toArray(new Item[player.getEquipment().getKeepSakeItems().size()]));
                    player.getPackets().sendGameMessage("You have added " + keepSakeItem.getName() + " to keepsakes. It will appear along with other in cosmetic list.");
                    player.getInventory().deleteItem(KEEP_SAKE_KEY, 1);
                    player.getInventory().deleteItem(keepSakeItem);
                }
                end();
            }

            @Override
            public void finish() {

            }

        });
        return true;
    }

    public static String getEarnedMessageRequirement(Player player, int itemId) {
        switch (itemId) {// Non-Buyable Costumes
            case 33712:// Torso of Omens
                if (player.getMauledWeeksHM()[0])
                    return null;
                return "You need to maul hard mode vorago in Ceiling Collapse rotation to unlock this.";
            case 33711:// Helm of Omens
                if (player.getMauledWeeksHM()[1])
                    return null;
                return "You need to maul hard mode vorago in Scopulus rotation to unlock this.";
            case 33713:// Legs of Omens
                if (player.getMauledWeeksHM()[2])
                    return null;
                return "You need to maul hard mode vorago in Vitalis rotation to unlock this.";
            case 33714:// Boots of Omens
                if (player.getMauledWeeksHM()[3])
                    return null;
                return "You need to maul hard mode vorago in Green bomb rotation to unlock this.";
            case 33709:// Maul of Omens
                if (player.getMauledWeeksHM()[4])
                    return null;
                return "You need to maul hard mode vorago in TeamSplit rotation to unlock this.";
            case 33715:// Gloves of Omens
                if (player.getMauledWeeksHM()[5])
                    return null;
                return "You need to maul hard mode vorago in The End rotation to unlock this.";
            case 37117:
            case 37118:
                if (player.getHeart().getReputation(HeartOfGielinor.SEREN) < 4000)
                    return "You need at least 4000 Serenic reputation to unlock this.";
                return null;
            case 37113:
            case 37114:
                if (player.getHeart().getReputation(HeartOfGielinor.SLISKE) < 4000)
                    return "You need at least 4000 Sliskean reputation to unlock this.";
                return null;
            case 37112:
                if (player.getHeart().getReputation(HeartOfGielinor.ZAMORAK) < 4000)
                    return "You need at least 4000 Zamorakian reputation to unlock this.";
                return null;
            case 37115:
            case 37116:
                if (player.getHeart().getReputation(HeartOfGielinor.ZAROS) < 4000)
                    return "You need at least 4000 Zarosian reputation to unlock this.";
                return null;
            case 47885:
            case 47886:
            case 47887:
            case 47888:
            case 47889:
            case 47890:
                if(!DistinctionCape.canWear(player, new Item(itemId)))
                    return "You are not worthy to wear this.";
                return null;
        }
        return null;
    }

    public static void checkRequirementUnlockedCosmetics(Player player) {
        if (player.getMauledWeeksHM()[0])
            player.getCosmeticsManager().unlockItem(33712);
        if (player.getMauledWeeksHM()[1])
            player.getCosmeticsManager().unlockItem(33711);
        if (player.getMauledWeeksHM()[2])
            player.getCosmeticsManager().unlockItem(33713);
        if (player.getMauledWeeksHM()[3])
            player.getCosmeticsManager().unlockItem(33714);
        if (player.getMauledWeeksHM()[4])
            player.getCosmeticsManager().unlockItem(33709);
        if (player.getMauledWeeksHM()[5])
            player.getCosmeticsManager().unlockItem(33715);
        if (player.getHeart().getReputation(HeartOfGielinor.SEREN) >= 4000) {
            player.getCosmeticsManager().unlockItem(37117);
            player.getCosmeticsManager().unlockItem(37118);
        }
        if (player.getHeart().getReputation(HeartOfGielinor.SLISKE) >= 4000) {
            player.getCosmeticsManager().unlockItem(37113);
            player.getCosmeticsManager().unlockItem(37114);
        }
        if (player.getHeart().getReputation(HeartOfGielinor.ZAMORAK) >= 4000) {
            player.getCosmeticsManager().unlockItem(37112);
        }
        if (player.getHeart().getReputation(HeartOfGielinor.ZAROS) >= 4000) {
            player.getCosmeticsManager().unlockItem(37115);
            player.getCosmeticsManager().unlockItem(37116);
        }
        for(int itemId=47885;itemId<=47890;itemId++)
            if(DistinctionCape.canWear(player, new Item(itemId)))
                player.getCosmeticsManager().unlockItem(itemId);
    }

    public static void transferCostumes(Player from, Player to) {
        for (int unlockedCostumesId : from.getUnlockedCostumesIds()) {
            if (to.getUnlockedCostumesIds().contains(unlockedCostumesId)) {
                continue;
            }
            to.getUnlockedCostumesIds().add(unlockedCostumesId);
        }
        for (int unlockedCostumesId : from.getCosmeticsManager().getUnlockedCosmetics()) {
            if (to.getCosmeticsManager().getUnlockedCosmetics().contains(unlockedCostumesId)) {
                continue;
            }
            to.getCosmeticsManager().getUnlockedCosmetics().add(unlockedCostumesId);
        }
    }


    public enum CosmeticType {
        APPEARENCE(1), WARDROBE(2), TITLE(3), ANIMATION(4), PET(5);

        public int type;

        CosmeticType(int type) {
            this.type = type;
        }
    }



}

