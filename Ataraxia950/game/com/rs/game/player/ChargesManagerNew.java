package com.rs.game.player;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import com.rs.cache.loaders.ItemDefinitions;
import com.rs.game.item.Item;
import com.rs.game.player.PerkManager.DonationPerk;
import com.rs.game.player.TemporaryAttributes.Key;
import com.rs.game.player.actions.invention.InventionData;
import com.rs.game.player.actions.invention.InventionData.Gizmo;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.network.packet.impl.ButtonHandler;
import com.rs.utils.Colors;
import com.rs.utils.Utils;

import lombok.Getter;
import lombok.Setter;

public class ChargesManagerNew implements Serializable {

    private static final long serialVersionUID = 5898792861696677039L;

    private transient Player player;

    public void useChargeOnHit(boolean given) {
        for (int i = 0; i < player.getEquipment().getItems().getItems().length; i++) {
            Item item = player.getEquipment().getItems().getItems()[i];
            if (item == null)
                continue;
            String name = item.getName().toLowerCase();
            if (!given && (name.contains("polypore staff") || name.contains("zaryte bow")))
                continue;
            if (item.getDefinitions().getName().startsWith("Elite ") && player.getEliteDungeonsManager().isInside())
                continue;
            if (!player.isUnderCombat())
                continue;
            useCharge(i, item);
        }
    }

    public boolean sendWear(int slotId, int itemId, int charges, boolean wear2) {
        Item item = player.getInventory().getItem(slotId);
        Integer[] degradationData = item.getDefinitions().getItemDegradeData();
        if (item.getChargesData() != null || degradationData == null || degradationData.length != 2 || degradationData[1] != -1 || item.getDefinitions().getMaxCharges() <= 0)
            return true;
        if (player.getInterfaceManager().containsBankInterface()) {
            player.getInventory().refresh();
            player.getTemporaryAttributtes().put(Key.SKIP_BANK_SHIFT_ITEMS, Boolean.TRUE);
            player.getBank().sendConfirmationMessage(item.getId(), item.getAmount(), "Confirm Equip", "Equipping this armour will mean you can no longer trade it.<br>Are you sure you would like to?", "Yes", new Runnable() {

                @Override
                public void run() {
                    initChargesData(item);
                    if (wear2) {
                        ButtonHandler.sendWear2(player, slotId, item.getId(), charges);
                        player.getInventory().refresh();
                        player.getEquipment().refresh(item.getDefinitions().getEquipSlot());
                        player.getAppearence().generateAppearenceData();
                        player.getPackets().sendSound(item.getDefinitions().getCSOpcode(118), 0, 1);
                    } else
                        ButtonHandler.sendWear(player, slotId, item.getId());
                }
            });
        } else
        player.getDialogueManager().startDialogue(new Dialogue() {

            @Override
            public void start() {
                sendOptionsDialogue("EQUIPPING THIS ARMOUR WILL MEAN YOU CAN NO LONGER TRADE IT.<br>ARE YOU SURE YOU'D LIKE TO?", "Yes", "No");
            }

            @Override
            public void run(int interfaceId, int componentId) {
                end();
                if (player.getInventory().getItem(slotId) != item)
                    return;
                if (componentId == OPTION_1) {
                    initChargesData(item);
                    if (wear2) {
                        ButtonHandler.sendWear2(player, slotId, item.getId(), charges);
                        player.getInventory().refresh();
                        player.getEquipment().refresh(item.getDefinitions().getEquipSlot());
                        player.getAppearence().generateAppearenceData();
                        player.getPackets().sendSound(item.getDefinitions().getCSOpcode(118), 0, 1);
                    } else
                        ButtonHandler.sendWear(player, slotId, item.getId());
                }
            }

            @Override
            public void finish() {
            }
        });
        return false;
    }

    public boolean useCharge(int slotId, Item item) {
        return useCharge(slotId, item, false, 0);
    }

    public boolean useCharge(int slotId, Item item, boolean inventory, double drainPercentage) {
        Item t = new Item(item.getId());
        t.setAttributes(item.getAttributes());
        item = t;
        ChargesData data = item.getChargesData();
        if (data == null) {
            data = initChargesData(item);
            if (data == null)
                return false;
        }
        if (item.getId() == data.getBrokenId())
            return false;
        int maxCharges = data.getMaxCharges();
        int chargeDrain = drainPercentage != 0 ? ((int) Math.ceil(drainPercentage * (double) maxCharges) * (data.getBrokenId() == -1 || ItemDefinitions.getItemDefinitions(data.getBrokenId()).getRepairData() == null ? 1 : 2)) : (!ItemDefinitions.getItemDefinitions(data.getOrignalId()).usesDoubleCharges() ? 2 : 1);
        int tier = ItemDefinitions.getItemDefinitions(data.getOrignalId()).getCSOpcode(750);
        int charges50 = (int) (data.getMaxCharges() * 0.5);
        int charges25 = (int) (data.getMaxCharges() * 0.25);
//        chargeDrain = 5000;
        int beforeCharges = data.getChargesLeft();
        if (player.getPerkManager().hasPerkActive(DonationPerk.CHARGE_BEFRIENDER)) {
            if (drainPercentage != 0 || Math.random() > (tier < 80 ? 0.50 : 0.25))
                data.setChargesLeft(data.getChargesLeft() - chargeDrain <= 0 ? 0 : data.getChargesLeft() - chargeDrain);
        } else
            data.setChargesLeft(data.getChargesLeft() - chargeDrain <= 0 ? 0 : data.getChargesLeft() - chargeDrain);
        int afterCharges = data.getChargesLeft();
        if (beforeCharges > afterCharges) {
            if (beforeCharges > charges50 && afterCharges < charges50) {
                player.sendMessage(Colors.RED + "Your " + item.getName() + " has 50% charges left.");
            } else if (beforeCharges > charges25 && afterCharges < charges25) {
                player.sendMessage(Colors.RED + "Your " + item.getName() + " has 25% charges left.");
            }
        }

        if (data.getChargesLeft() > 0 && data.getChargesLeft() <= (data.getMaxCharges() * 0.5) && item.getId() >= 22458 && item.getId() <= 22497) {// double stage items
            if (item.getId() == data.getWornId()) {
                item.setId(data.getWornId() + 1);
                if (inventory)
                    player.getInventory().refresh(slotId);
                else
                    player.getEquipment().refresh(slotId);
                player.getAppearence().generateAppearenceData();
            }
        }
        if (inventory) {
            player.getInventory().set(slotId, item);
            player.getInventory().refresh(slotId);
        } else {
            player.getEquipment().set(slotId, item);
            player.getEquipment().refresh(slotId);
        }
        if (data.getChargesLeft() <= 0) {
            if (data.getBrokenId() == -1) {
                player.getPackets().sendGameMessage("<col=ff0000>Your " + ItemDefinitions.getItemDefinitions(data.getOrignalId()).getName() + " completely vanishes into dust.");
                if (inventory) {
                    player.getInventory().set(slotId, null);
                    player.getInventory().refresh(slotId);
                } else {
                    player.getEquipment().set(slotId, null);
                    player.getEquipment().refresh(slotId);
                }
                player.getAppearence().generateAppearenceData();
                return true;
            }
            ItemDefinitions broken = ItemDefinitions.getItemDefinitions(data.getBrokenId());
            if (!broken.isWearItem()) {
                player.getPackets().sendGameMessage("<col=ff0000>Your " + ItemDefinitions.getItemDefinitions(data.getOrignalId()).getName() + " has run out of charges and reverted to " + broken.getName() + "!");
                if (inventory) {
                    player.getInventory().set(slotId, null);
                    player.getInventory().refresh(slotId);
                } else {
                    player.getEquipment().set(slotId, null);
                    player.getEquipment().refresh(slotId);
                }
                player.getAppearence().generateAppearenceData();
                player.getInventory().addItemDrop(new Item(broken.getId(), 1));
                return true;
            }
            if (item.getInventionData() != null) {
                if (InventionData.getItemLevel(player, item) < 10)
                    return true;
            }
            player.getPackets().sendGameMessage("<col=ff0000>Your " + ItemDefinitions.getItemDefinitions(data.getOrignalId()).getName() + " has run out of charges!");
            item.setId(data.getBrokenId());
            item.setChargesData(null);
            player.getAppearence().generateAppearenceData();
        }
        if (inventory)
            player.getInventory().refresh(slotId);
        else
            player.getEquipment().refresh(slotId);
        return true;
    }

    public ChargesData initChargesData(Item item) {
        return initChargesData(item, true);
    }

    public ChargesData initChargesData(Item item, boolean sendMessage) {
        if (item.getChargesData() == null) {
            Integer[] repairData = item.getDefinitions().getRepairData();
            if (repairData != null) {
                int originalId = repairData[0];
                Integer[] degradationData = ItemDefinitions.getItemDefinitions(originalId).getItemDegradeData();
                if (degradationData != null && degradationData.length == 2 && item.getId() == degradationData[0])
                    item.setId(repairData[0]);
            }
            if (item.getDefinitions().getDegradeToDustOriginalItemId() != -1)
                item.setId(item.getDefinitions().getDegradeToDustOriginalItemId());
        }
        Integer[] degradationData = item.getDefinitions().getItemDegradeData();
        int maxCharges = item.getDefinitions().getMaxCharges();
        if (item.getInventionData() != null)
            maxCharges *= 2;
        if (degradationData == null || maxCharges <= 0)
            return null;
        ChargesData data = new ChargesData(item.getId(), degradationData.length < 2 ? item.getId() : degradationData[0], degradationData.length < 2 ? degradationData[0] : degradationData[1], maxCharges);
        item.setId(data.getWornId());
        item.setChargesData(data);
        if (sendMessage)
            player.getPackets().sendGameMessage("<col=ffa500>Your " + ItemDefinitions.getItemDefinitions(data.getOrignalId()).getName() + " has degraded slightly!");
        return data;
    }

    public boolean checkCharges(Item item) {
        Integer[] degradationData = item.getDefinitions().getItemDegradeData();
        ChargesData data = item.getChargesData();
        if (data == null && degradationData != null && item.getDefinitions().getMaxCharges() > 0) {
            if (item.getName().toLowerCase().contains("polypore staff"))
                player.getPackets().sendGameMessage(item.getDefinitions().getName() + ": 3,000 charges remaining.");
            else
                player.getPackets().sendGameMessage(item.getDefinitions().getName() + ": 100% charges remaining.");
            return true;
        }
        if (data == null)
            return false;
        if (item.getName().toLowerCase().contains("polypore staff"))
            player.getPackets().sendGameMessage(item.getDefinitions().getName() + ": " + Utils.formatNumber(data.getChargesLeft()) + " charges remaining.");
        else
            player.getPackets().sendGameMessage(ItemDefinitions.getItemDefinitions(data.getOrignalId()).getName() + ": " + new DecimalFormat("##.##").format(((double) (data.getChargesLeft() * 100) / (double) data.getMaxCharges())) + "% charges remaining.");
        return true;
    }

    public Item createDegradeableItem(Item item, int... usedItems) {
        if (item.getName().toLowerCase().startsWith("refined anima")) {
            item.setChargesData(new ChargesData(item.getId(), item.getId(), item.getId() - 2, item.getDefinitions().getMaxCharges()));
            return item;
        }
        switch (item.getId()) {
            case 34150:
                int initialCharges = 12000;
                for (int itemId : usedItems)
                    if (ItemDefinitions.getItemDefinitions(itemId).getName().toLowerCase().contains("abyssal vine whip"))
                        initialCharges = 24000;
                Integer[] degradationData = item.getDefinitions().getItemDegradeData();
                item.setChargesData(new ChargesData(item.getId(), degradationData[0], degradationData[1], item.getDefinitions().getMaxCharges()));
                item.getChargesData().setChargesLeft(initialCharges);
                return item;
            case 34155:
                initialCharges = 30000;
                degradationData = item.getDefinitions().getItemDegradeData();
                item.setChargesData(new ChargesData(item.getId(), degradationData[0], degradationData[1], item.getDefinitions().getMaxCharges()));
                item.getChargesData().setChargesLeft(initialCharges);
                return item;
            case 34158:
                initialCharges = 12000;
                degradationData = item.getDefinitions().getItemDegradeData();
                item.setChargesData(new ChargesData(item.getId(), degradationData[0], degradationData[1], item.getDefinitions().getMaxCharges()));
                item.getChargesData().setChargesLeft(initialCharges);
                return item;
        }
        return item;
    }

    public boolean rechargeItem(Item itemUsed, Item itemUsedWith) {
        if (itemUsed.getId() == 34150 || itemUsedWith.getId() == 34150) {
            Item lavaWhip = itemUsed.getId() == 34150 ? itemUsed : itemUsedWith;
            Item ingriedient = lavaWhip.getId() == itemUsed.getId() ? itemUsedWith : itemUsed;
            if (lavaWhip.getChargesData() == null)
                return false;
            if (!ingriedient.getName().equalsIgnoreCase("whip vine") && !ingriedient.getName().equalsIgnoreCase("abyssal whip") && !ingriedient.getName().equalsIgnoreCase("abyssal vine whip"))
                return false;
            int maxCharges = lavaWhip.getChargesData().getMaxCharges();
            int rechargeAmount = ingriedient.getName().equalsIgnoreCase("abyssal vine whip") ? 24000 : 12000;
            int chargesLeft = lavaWhip.getChargesData().getChargesLeft();
            if (chargesLeft >= maxCharges) {
                player.getPackets().sendGameMessage("Your lava whip is already fully charged.");
                return true;
            }
            player.getInventory().deleteItem(ingriedient);
            lavaWhip.getChargesData().setChargesLeft(rechargeAmount + chargesLeft >= maxCharges ? maxCharges : rechargeAmount + chargesLeft);
            player.sendMessage(Colors.GREEN + Colors.SHAD + "You have added " + (((lavaWhip.getChargesData().getChargesLeft() - chargesLeft) * 100) / maxCharges) + "% charges to your lava whip!", true);
            player.getInventory().refresh();
            return true;
        }
        if (itemUsed.getId() == 34155 || itemUsedWith.getId() == 34155) {
            Item staffofDarkness = itemUsed.getId() == 34155 ? itemUsed : itemUsedWith;
            Item ingriedient = staffofDarkness.getId() == itemUsed.getId() ? itemUsedWith : itemUsed;
            if (staffofDarkness.getChargesData() == null)
                return false;
            if (ingriedient.getId() != 15486)
                return false;
            int maxCharges = staffofDarkness.getChargesData().getMaxCharges();
            int rechargeAmount = 30000;
            int chargesLeft = staffofDarkness.getChargesData().getChargesLeft();
            if (chargesLeft >= maxCharges) {
                player.getPackets().sendGameMessage("Your Staff of darkness is already fully charged.");
                return true;
            }
            player.getInventory().deleteItem(ingriedient);
            staffofDarkness.getChargesData().setChargesLeft(rechargeAmount + chargesLeft >= maxCharges ? maxCharges : rechargeAmount + chargesLeft);
            player.sendMessage(Colors.GREEN + Colors.SHAD + "You have added " + (((staffofDarkness.getChargesData().getChargesLeft() - chargesLeft) * 100) / maxCharges) + "% charges to your Staff of darkness!", true);
            player.getInventory().refresh();
            return true;
        }
        if (itemUsed.getId() == 34158 || itemUsedWith.getId() == 34158) {
            Item strykebow = itemUsed.getId() == 34158 ? itemUsed : itemUsedWith;
            Item ingriedient = strykebow.getId() == itemUsed.getId() ? itemUsedWith : itemUsed;
            if (strykebow.getChargesData() == null)
                return false;
            if (ingriedient.getId() != 11235)
                return false;
            int maxCharges = strykebow.getChargesData().getMaxCharges();
            int rechargeAmount = 12000;
            int chargesLeft = strykebow.getChargesData().getChargesLeft();
            if (chargesLeft >= maxCharges) {
                player.getPackets().sendGameMessage("Your Strykebow is already fully charged.");
                return true;
            }
            player.getInventory().deleteItem(ingriedient);
            strykebow.getChargesData().setChargesLeft(rechargeAmount + chargesLeft >= maxCharges ? maxCharges : rechargeAmount + chargesLeft);
            player.sendMessage(Colors.GREEN + Colors.SHAD + "You have added " + (((strykebow.getChargesData().getChargesLeft() - chargesLeft) * 100) / maxCharges) + "% charges to your Strykebow!", true);
            player.getInventory().refresh();
            return true;
        }
        if (itemUsed.getInventionData() != null && itemUsedWith.getInventionData() != null) {
            int usedUnDyedId = ChargesManagerNew.getUnDyedVersion(itemUsed.getDefinitions().getChargedItemId());
            int usedWithUnDyedId = ChargesManagerNew.getUnDyedVersion(itemUsedWith.getDefinitions().getChargedItemId());
            if (usedUnDyedId != -1 && usedWithUnDyedId != -1)
                return false;
            Item dyedItem = usedUnDyedId != -1 ? itemUsed : usedWithUnDyedId != -1 ? itemUsedWith : itemUsed;
            if (usedUnDyedId == -1 && usedWithUnDyedId == -1) {
                if (itemUsed.getInventionData().getOriginalItemId() != itemUsedWith.getInventionData().getOriginalItemId())
                    return false;
            } else {
                if (!itemUsed.getName().contains(dyedItem.getName().substring(0, dyedItem.getName().toLowerCase().indexOf("(") - 1)) && !itemUsedWith.getName().contains(dyedItem.getName().substring(0, dyedItem.getName().toLowerCase().indexOf("(") - 1)))
                    return false;
            }
            int usedCharges = itemUsed.getDefinitions().getCSOpcode(5527) != 0 ? 0 : itemUsed.getChargesData() == null ? itemUsed.getDefinitions().getMaxCharges() * 2 : itemUsed.getChargesData().getChargesLeft();
            int usedWithCharges = itemUsedWith.getDefinitions().getCSOpcode(5527) != 0 ? 0 : itemUsedWith.getChargesData() == null ? itemUsedWith.getDefinitions().getMaxCharges() * 2 : itemUsedWith.getChargesData().getChargesLeft();
            int maxCharges = itemUsed.getDefinitions().getMaxCharges() * 2;
            if ((usedUnDyedId != -1 && usedCharges >= maxCharges) || (usedWithUnDyedId != -1 && usedWithCharges >= maxCharges)) {
                player.getPackets().sendGameMessage("Your dyed item is already fully charged.");
                return true;
            }
            if (usedCharges >= maxCharges && usedWithCharges >= maxCharges) {
                player.getPackets().sendGameMessage("Both of your items are already fully charged.");
                return true;
            }
            boolean bothHavePerks = (itemUsed.getInventionData().getGizmos()[0] != null || itemUsed.getInventionData().getGizmos()[1] != null) && (itemUsedWith.getInventionData().getGizmos()[0] != null || itemUsedWith.getInventionData().getGizmos()[1] != null);
            if (bothHavePerks) {
                player.getPackets().sendGameMessage("Only one item may have perks on it.");
                return true;
            }
            int itemId = dyedItem.getDefinitions().getCSOpcode(5527) != 0 ? dyedItem.getDefinitions().getCSOpcode(5527) : dyedItem.getId();
            Gizmo[] gizmos = (itemUsed.getInventionData().getGizmos()[0] != null || itemUsed.getInventionData().getGizmos()[1] != null) ? itemUsed.getInventionData().getGizmos() : itemUsedWith.getInventionData().getGizmos();
            double xp = itemUsed.getInventionData().getXp() > itemUsedWith.getInventionData().getXp() ? itemUsed.getInventionData().getXp() : itemUsedWith.getInventionData().getXp();
            player.getDialogueManager().startDialogue(new Dialogue() {

                @Override
                public void start() {
                    sendOptionsDialogue("COMBINING THESE ITEMS WILL DELETE THE ITEM THAT HAVE THE LEAST XP<br> ARE YOU SURE YOU WANT TO DO THIS?", "Yes.", "No.");
                }

                @Override
                public void run(int interfaceId, int componentId) {
                    end();
                    if (!player.getInventory().containsItem(itemUsed) || !player.getInventory().containsItem(itemUsedWith))
                        return;
                    if (componentId == OPTION_1) {
                        int invSlot = player.getInventory().getItemSlot(itemUsedWith);
                        Item item = new Item(itemId);
                        item.setInventionData(new InventionData(xp));
                        item.getInventionData().setOriginalItemId(itemUsedWith.getInventionData().getOriginalItemId());
                        item.getInventionData().setGizmos(gizmos);
                        if (usedWithCharges + usedCharges < maxCharges) {
                            initChargesData(item, false);
                            item.getChargesData().setChargesLeft(usedWithCharges + usedCharges);
                        }
                        player.getInventory().deleteItem(itemUsed);
                        player.getInventory().set(invSlot, item);
                        player.getInventory().refresh();
                        player.getPackets().sendGameMessage("You combine the charges of the two items.");
                    }
                }

                @Override
                public void finish() {
                }
            });
            return true;
        } else if (itemUsed.getChargesData() != null && itemUsedWith.getChargesData() != null && itemUsed.getChargesData().orignalId == itemUsedWith.getChargesData().getOrignalId()) {
            if (itemUsed.getChargesData().getBrokenId() != -1)
                return false;
            int usedCharges = itemUsed.getChargesData().getChargesLeft();
            int usedWithCharges = itemUsedWith.getChargesData().getChargesLeft();
            int maxCharges = itemUsed.getDefinitions().getMaxCharges();
            if (usedCharges >= maxCharges && usedWithCharges >= maxCharges) {
                player.getPackets().sendGameMessage("Both of your items are already fully charged.");
                return true;
            }
            player.getDialogueManager().startDialogue(new Dialogue() {

                @Override
                public void start() {
                    sendOptionsDialogue("COMBINING THESE ITEMS CANNOT BE UNDONE<br> ARE YOU SURE YOU WANT TO DO THIS?", "Yes.", "No.");
                }

                @Override
                public void run(int interfaceId, int componentId) {
                    end();
                    if (!player.getInventory().containsItem(itemUsed) || !player.getInventory().containsItem(itemUsedWith))
                        return;
                    if (componentId == OPTION_1) {
                        int invSlot = player.getInventory().getItemSlot(itemUsedWith);
                        itemUsedWith.getChargesData().setChargesLeft(usedWithCharges + usedCharges >= maxCharges ? maxCharges : usedWithCharges + usedCharges);
                        player.getInventory().deleteItem(itemUsed);
                        player.getInventory().set(invSlot, itemUsedWith);
                        player.getInventory().refresh();
                        player.getPackets().sendGameMessage("You combine the charges of the two items.");
                    }
                }

                @Override
                public void finish() {

                }
            });
            return true;
        } else if (itemUsed.getId() == 24339 || itemUsedWith.getId() == 24339) {
            Item royalCrossbow = itemUsed.getId() == 24339 ? itemUsed : itemUsedWith;
            Item ingredient = itemUsed.getId() == royalCrossbow.getId() ? itemUsedWith : itemUsed;
            if (ingredient.getId() < 24340 || ingredient.getId() > 24346 || ingredient.getDefinitions().isNoted())
                return false;
            for (int requirement = 24340; requirement <= 24346; requirement += 2) {
                if (!player.getInventory().containsItem(requirement, 1)) {
                    player.getPackets().sendGameMessage("You don't have all of the 4 required components to repair broken royal crossbow.");
                    return true;
                }
            }
            player.getInventory().deleteItem(24340, 1);
            player.getInventory().deleteItem(24342, 1);
            player.getInventory().deleteItem(24344, 1);
            player.getInventory().deleteItem(24346, 1);
            royalCrossbow.setId(24338);
            initChargesData(royalCrossbow, false);
            player.getInventory().refresh();
            player.getPackets().sendGameMessage("You repair the broken royal crossbow.");
            return true;
        } else if (itemUsed.getId() == 24338 || itemUsedWith.getId() == 24338) {
            Item royalCrossbow = itemUsed.getId() == 24338 ? itemUsed : itemUsedWith;
            Item ingredient = itemUsed.getId() == royalCrossbow.getId() ? itemUsedWith : itemUsed;
            if (ingredient.getId() < 24340 || ingredient.getId() > 24346 || ingredient.getDefinitions().isNoted())
                return false;
            int maxCharges = royalCrossbow.getDefinitions().getMaxCharges();
            int rechargeAmount = 7500;
            int chargesLeft = royalCrossbow.getChargesData() == null ? maxCharges : royalCrossbow.getChargesData().getChargesLeft();
            if (chargesLeft >= maxCharges) {
                player.getPackets().sendGameMessage("Your royal crossbow is already fully charged.");
                return true;
            }
            player.getInventory().deleteItem(ingredient);
            royalCrossbow.getChargesData().setChargesLeft(rechargeAmount + chargesLeft >= maxCharges ? maxCharges : rechargeAmount + chargesLeft);
            player.sendMessage(Colors.GREEN + Colors.SHAD + "You have added " + (((royalCrossbow.getChargesData().getChargesLeft() - chargesLeft) * 100) / maxCharges) + "% charges to your royal crossbow!", true);
            player.getInventory().refresh();
            return true;
        } else if ((itemUsed.getName().toLowerCase().contains("blood necklace") || itemUsed.getName().toLowerCase().contains("blood amulet")) || (itemUsedWith.getName().toLowerCase().contains("blood necklace") || itemUsedWith.getName().toLowerCase().contains("blood amulet"))) {
            Item bloodItem = (itemUsed.getName().toLowerCase().contains("blood necklace") || itemUsed.getName().toLowerCase().contains("blood amulet")) ? itemUsed : itemUsedWith;
            Item bloodrune = itemUsed.getId() == bloodItem.getId() ? itemUsedWith : itemUsed;
            if (bloodrune.getId() != 565)
                return false;
            int maxCharges = bloodItem.getDefinitions().getMaxCharges();
            int rechargeAmount = 20;
            int chargesLeft = bloodItem.getDefinitions().getName().toLowerCase().contains("empty") ? 0 : bloodItem.getChargesData() == null ? maxCharges : bloodItem.getChargesData().getChargesLeft();
            if (chargesLeft >= maxCharges) {
                player.getPackets().sendGameMessage("Your " + bloodItem.getName() + " is already fully charged.");
                return true;
            }
            int amountToRemove = bloodrune.getAmount();
            int maxAmount = (int) Math.ceil((double) (maxCharges - chargesLeft) / 20.00);
            if (amountToRemove > maxAmount)
                amountToRemove = maxAmount;
            rechargeAmount *= amountToRemove;
            player.getInventory().deleteItem(bloodrune.getId(), amountToRemove);
            if (rechargeAmount + chargesLeft >= maxCharges) {
                if (bloodItem.getChargesData() != null)
                    bloodItem.setId(bloodItem.getChargesData().getOrignalId());
                bloodItem.setChargesData(null);
            } else
                bloodItem.getChargesData().setChargesLeft(rechargeAmount + chargesLeft >= maxCharges ? maxCharges : rechargeAmount + chargesLeft);
            player.sendMessage(Colors.GREEN + Colors.SHAD + "You have added " + rechargeAmount + " charges to your " + bloodItem.getName() + ", its now at " + (bloodItem.getChargesData() == null ? 100 : new DecimalFormat("##.##").format(((double) (bloodItem.getChargesData().getChargesLeft() * 100) / (double) bloodItem.getChargesData().getMaxCharges()))) + "%", true);
            player.getInventory().refresh();
            return true;
        } else if ((itemUsed.getDefinitions().getCustomRepairItemId() != -1 && itemUsedWith.getId() == itemUsed.getDefinitions().getCustomRepairItemId()) || (itemUsedWith.getDefinitions().getCustomRepairItemId() != -1 && itemUsed.getId() == itemUsedWith.getDefinitions().getCustomRepairItemId())) {
            Item repairableItem = itemUsed.getDefinitions().getCustomRepairItemId() != -1 ? itemUsed : itemUsedWith;
            Item repairItem = itemUsed.getId() == repairableItem.getId() ? itemUsedWith : itemUsed;
            int maxCharges = repairableItem.getDefinitions().getMaxCharges();
            int equipSlot = repairableItem.getDefinitions().getEquipSlot();
            int rechargeAmount = equipSlot == Equipment.SLOT_HAT ? 7000 : equipSlot == Equipment.SLOT_CHEST ? 2000 : 3000;
            if (rechargeAmount < 5000)
                rechargeAmount = 5000;
            int chargesLeft = getChargesLeft(repairableItem);
            if (chargesLeft >= maxCharges) {
                player.getPackets().sendGameMessage("Your " + repairableItem.getName() + " is already fully charged.");
                return true;
            }
            int amountToRemove = repairItem.getAmount();
            int maxAmount = (int) Math.ceil((double) (maxCharges - chargesLeft) / (double) rechargeAmount);
            if (amountToRemove > maxAmount)
                amountToRemove = maxAmount;
            rechargeAmount *= amountToRemove;
            player.getInventory().deleteItem(repairItem.getId(), amountToRemove);
            if (rechargeAmount + chargesLeft >= maxCharges) {
                repairableItem.setId(repairableItem.getChargesData() == null ? repairableItem.getDefinitions().getRepairData()[0] : repairableItem.getChargesData().getOrignalId());
                repairableItem.setChargesData(null);
            } else {
                if (repairableItem.getChargesData() == null) {
                    repairableItem.setId(repairableItem.getDefinitions().getRepairData()[0]);
                    initChargesData(repairableItem, false);
                }
                repairableItem.getChargesData().setChargesLeft(rechargeAmount + chargesLeft >= maxCharges ? maxCharges : rechargeAmount + chargesLeft);
            }
            player.sendMessage(Colors.GREEN + Colors.SHAD + "You have added " + rechargeAmount + " charges to your " + repairableItem.getName() + ", its now at " + (repairableItem.getChargesData() == null ? 100 : new DecimalFormat("##.##").format(((double) (repairableItem.getChargesData().getChargesLeft() * 100) / (double) repairableItem.getChargesData().getMaxCharges()))) + "%", true);
            player.getInventory().refresh();
            return true;
        } else if (((itemUsed.getName().toLowerCase().contains("fungal") || itemUsed.getName().toLowerCase().contains("grifolic") || itemUsed.getName().toLowerCase().contains("ganodermic") || itemUsed.getName().toLowerCase().contains("polypore staff")) && itemUsed.getDefinitions().getRepairData() != null) || ((itemUsedWith.getName().toLowerCase().contains("fungal") || itemUsedWith.getName().toLowerCase().contains("grifolic") || itemUsedWith.getName().toLowerCase().contains("ganodermic") || itemUsedWith.getName().toLowerCase().contains("polypore staff")) && itemUsedWith.getDefinitions().getRepairData() != null)) {
            Item repairableItem = ((itemUsed.getName().toLowerCase().contains("fungal") || itemUsed.getName().toLowerCase().contains("grifolic") || itemUsed.getName().toLowerCase().contains("ganodermic") || itemUsed.getName().toLowerCase().contains("polypore staff")) && itemUsed.getDefinitions().getRepairData() != null) ? itemUsed : itemUsedWith;
            Item repairItem = itemUsed.getId() == repairableItem.getId() ? itemUsedWith : itemUsed;
            int equipSlot = repairableItem.getDefinitions().getEquipSlot();
            int tier = repairableItem.getName().toLowerCase().contains("fungal") ? 0 : repairableItem.getName().toLowerCase().contains("grifolic") ? 1 : repairableItem.getName().toLowerCase().contains("ganodermic") ? 2 : 3;
            int requiredItemId = tier <= 2 ? 22449 + tier : -1;
            if ((requiredItemId == -1 && repairItem.getId() != 554 && repairItem.getId() != 22448) || (requiredItemId != -1 && repairItem.getId() != requiredItemId))
                return false;
            if (repairableItem.getChargesData() == null) {
                repairableItem.setId(repairableItem.getDefinitions().getRepairData()[0]);
                player.getInventory().refresh();
                player.getPackets().sendGameMessage("Something is wrong, we replaced ur item with a fully charged one");
                return true;
            }
            int maxCharges = repairableItem.getDefinitions().getMaxCharges();
            int chargesLeft = repairableItem.getChargesData().getChargesLeft();
            if (chargesLeft >= maxCharges) {
                player.getPackets().sendGameMessage("Your " + repairableItem.getName() + " is already fully charged.");
                return true;
            }
            int rechargeAmount = tier == 3 ? (1) : (equipSlot == Equipment.SLOT_HAT ? (100 - (tier * 25)) : equipSlot == Equipment.SLOT_CHEST ? (12 - tier * 3) : (30 - tier * 5));
            if (tier == 3 && (!player.getInventory().containsItem(22448, 1) || !player.getInventory().containsItem(554, 5))) {
                player.getPackets().sendGameMessage("You don't have the minimum required items to recharge " + repairableItem.getName() + ".");
                return true;
            }
            int amountToRemove = tier == 3 ? Math.min(player.getInventory().getAmountOf(554) / 5, player.getInventory().getAmountOf(22448)) : repairItem.getAmount();
            int maxAmount = (int) Math.ceil((double) (maxCharges - chargesLeft) / (double) rechargeAmount);
            if (amountToRemove > maxAmount)
                amountToRemove = maxAmount;
            rechargeAmount *= amountToRemove;
            amountToRemove *= repairItem.getId() == 554 ? 5 : 1;
            player.getInventory().deleteItem(repairItem.getId(), amountToRemove);
            if (tier == 3)
                player.getInventory().deleteItem(repairItem.getId() == 554 ? 22448 : 554, rechargeAmount * (repairItem.getId() == 554 ? 1 : 5));
            if (rechargeAmount + chargesLeft >= maxCharges) {
                repairableItem.setId(repairableItem.getChargesData().getOrignalId());
                repairableItem.setChargesData(null);
            } else {
                if (repairableItem.getChargesData() == null) {
                    repairableItem.setId(repairableItem.getDefinitions().getRepairData()[0]);
                    initChargesData(repairableItem, false);
                }
                repairableItem.getChargesData().setChargesLeft(rechargeAmount + chargesLeft >= maxCharges ? maxCharges : rechargeAmount + chargesLeft);
                repairableItem.setId(repairableItem.getChargesData().getWornId() + (repairableItem.getChargesData().getChargesLeft() < (maxCharges * 0.5) ? 1 : 0));
            }
            player.sendMessage(Colors.GREEN + Colors.SHAD + "You have added " + rechargeAmount + " charges to your " + repairableItem.getName() + ", its now at " + (repairableItem.getChargesData() == null ? 100 : new DecimalFormat("##.##").format(((double) (repairableItem.getChargesData().getChargesLeft() * 100) / (double) repairableItem.getChargesData().getMaxCharges()))) + "%", true);
            player.getInventory().refresh();
            return true;
        } else if (((itemUsed.getName().toLowerCase().contains("emberkeen boots") || itemUsed.getName().toLowerCase().contains("flarefrost boots") || itemUsed.getName().toLowerCase().contains("hailfire boots")) && itemUsed.getDefinitions().getRepairData() != null) || ((itemUsedWith.getName().toLowerCase().contains("emberkeen boots") || itemUsedWith.getName().toLowerCase().contains("flarefrost boots") || itemUsedWith.getName().toLowerCase().contains("hailfire boots")) && itemUsedWith.getDefinitions().getRepairData() != null)) {
            Item repairableItem = (itemUsed.getName().toLowerCase().contains("emberkeen boots") || itemUsed.getName().toLowerCase().contains("flarefrost boots") || itemUsed.getName().toLowerCase().contains("hailfire boots")) && itemUsed.getDefinitions().getRepairData() != null ? itemUsed : itemUsedWith;
            Item repairItem = itemUsed.getId() == repairableItem.getId() ? itemUsedWith : itemUsed;
            int requiredItemId = repairableItem.getName().toLowerCase().contains("emberkeen boots") ? 34972 : repairableItem.getName().toLowerCase().contains("flarefrost boots") ? 34974 : 34976;
            if (repairItem.getId() != requiredItemId)
                return false;
            if (repairableItem.getChargesData() == null) {
                repairableItem.setId(repairableItem.getDefinitions().getRepairData()[0]);
                player.getInventory().refresh();
                player.getPackets().sendGameMessage("Something is wrong, we replaced ur item with a fully charged one");
                return true;
            }
            int rechargeAmount = 75000;
            int maxCharges = repairableItem.getDefinitions().getMaxCharges();
            int chargesLeft = repairableItem.getChargesData().getChargesLeft();
            if (chargesLeft >= maxCharges) {
                player.getPackets().sendGameMessage("Your " + repairableItem.getName() + " is already fully charged.");
                return true;
            }
            int amountToRemove = 1;
            int maxAmount = (int) Math.ceil((double) (maxCharges - chargesLeft) / (double) rechargeAmount);
            if (amountToRemove > maxAmount)
                amountToRemove = maxAmount;
            rechargeAmount *= amountToRemove;
            player.getInventory().deleteItem(repairItem.getId(), amountToRemove);
            if (rechargeAmount + chargesLeft >= maxCharges) {
                repairableItem.setId(repairableItem.getChargesData().getOrignalId());
                repairableItem.setChargesData(null);
            } else {
                if (repairableItem.getChargesData() == null) {
                    repairableItem.setId(repairableItem.getDefinitions().getRepairData()[0]);
                    initChargesData(repairableItem, false);
                }
                repairableItem.getChargesData().setChargesLeft(rechargeAmount + chargesLeft >= maxCharges ? maxCharges : rechargeAmount + chargesLeft);
            }
            player.sendMessage(Colors.GREEN + Colors.SHAD + "You have added " + rechargeAmount + " charges to your " + repairableItem.getName() + ", its now at " + (repairableItem.getChargesData() == null ? 100 : new DecimalFormat("##.##").format(((double) (repairableItem.getChargesData().getChargesLeft() * 100) / (double) repairableItem.getChargesData().getMaxCharges()))) + "%", true);
            player.getInventory().refresh();
            return true;
        } else if (((itemUsed.getName().toLowerCase().contains("amulet of souls") || itemUsed.getName().toLowerCase().contains("deathtouch bracelet") || itemUsed.getName().toLowerCase().contains("reaper necklace") || itemUsed.getName().toLowerCase().contains("ring of death") || itemUsed.getName().toLowerCase().contains("ring of wealth (c)")) && itemUsed.getDefinitions().getRepairData() != null) || ((itemUsedWith.getName().toLowerCase().contains("amulet of souls") || itemUsedWith.getName().toLowerCase().contains("deathtouch bracelet") || itemUsedWith.getName().toLowerCase().contains("reaper necklace") || itemUsedWith.getName().toLowerCase().contains("ring of death") || itemUsedWith.getName().toLowerCase().contains("ring of wealth (c)")) && itemUsedWith.getDefinitions().getRepairData() != null)) {
            Item repairableItem = (itemUsed.getName().toLowerCase().contains("amulet of souls") || itemUsed.getName().toLowerCase().contains("deathtouch bracelet") || itemUsed.getName().toLowerCase().contains("reaper necklace") || itemUsed.getName().toLowerCase().contains("ring of death") || itemUsed.getName().toLowerCase().contains("ring of wealth (c)")) && itemUsed.getDefinitions().getRepairData() != null ? itemUsed : itemUsedWith;
            Item repairItem = itemUsed.getId() == repairableItem.getId() ? itemUsedWith : itemUsed;
            if (repairItem.getId() != 6573)
                return false;
            int rechargeAmount = 50000;
            int maxCharges = repairableItem.getDefinitions().getMaxCharges();
            int chargesLeft = repairableItem.getDefinitions().getName().toLowerCase().contains("broken") ? 0 : repairableItem.getChargesData() == null ? maxCharges : repairableItem.getChargesData().getChargesLeft();
            if (chargesLeft >= maxCharges) {
                player.getPackets().sendGameMessage("Your " + repairableItem.getName() + " is already fully charged.");
                return true;
            }
            int amountToRemove = 1;
            int maxAmount = (int) Math.ceil((double) (maxCharges - chargesLeft) / (double) rechargeAmount);
            if (amountToRemove > maxAmount)
                amountToRemove = maxAmount;
            rechargeAmount *= amountToRemove;
            player.getInventory().deleteItem(repairItem.getId(), amountToRemove);
            if (rechargeAmount + chargesLeft >= maxCharges) {
                repairableItem.setId(repairableItem.getChargesData().getOrignalId());
                repairableItem.setChargesData(null);
            } else {
                if (repairableItem.getChargesData() == null) {
                    repairableItem.setId(repairableItem.getDefinitions().getRepairData()[0]);
                    initChargesData(repairableItem, false);
                }
                repairableItem.getChargesData().setChargesLeft(rechargeAmount + chargesLeft >= maxCharges ? maxCharges : rechargeAmount + chargesLeft);
            }
            player.sendMessage(Colors.GREEN + Colors.SHAD + "You have added " + rechargeAmount + " charges to your " + repairableItem.getName() + ", its now at " + (repairableItem.getChargesData() == null ? 100 : new DecimalFormat("##.##").format(((double) (repairableItem.getChargesData().getChargesLeft() * 100) / (double) repairableItem.getChargesData().getMaxCharges()))) + "%", true);
            player.getInventory().refresh();
            return true;
        }
        return false;
    }

    public void sendRechargeItemDialogue(Item torepair, int npcId) {
        List<Item> toRecharge = new ArrayList<Item>();
        if (torepair != null && torepair.getDefinitions().getRepairData() != null && ItemDefinitions.getItemDefinitions(torepair.getDefinitions().getRepairData()[0]).getRepairPrice() > 0 && torepair.getDefinitions().getCustomRepairItemId() == -1) {
            int maxCharges = ItemDefinitions.getItemDefinitions(torepair.getDefinitions().getRepairData()[0]).getMaxCharges();
            int chargesLeft = getChargesLeft(torepair);
            if (chargesLeft < maxCharges)
                toRecharge.add(torepair);
            else {
                torepair.setId(torepair.getDefinitions().getRepairData()[0]);
                torepair.setChargesData(null);
                player.getInventory().refresh();
            }
        } else if (torepair == null) {
            for (Item item : player.getInventory().getItems().getItems()) {
                if (item != null && item.getDefinitions().getRepairData() != null && ItemDefinitions.getItemDefinitions(item.getDefinitions().getRepairData()[0]).getRepairPrice() > 0 && item.getDefinitions().getCustomRepairItemId() == -1) {
                    int maxCharges = ItemDefinitions.getItemDefinitions(item.getDefinitions().getRepairData()[0]).getMaxCharges();
                    int chargesLeft = getChargesLeft(item);
                    if (chargesLeft < maxCharges)
                        toRecharge.add(item);
                    else {
                        item.setId(item.getDefinitions().getRepairData()[0]);
                        item.setChargesData(null);
                        player.getInventory().refresh();
                    }
                }
            }
        }
        boolean armourStand = npcId == -1;
        if (toRecharge.isEmpty()) {
            if (armourStand)
                player.getPackets().sendGameMessage("Nothing interesting happens.");
            else
                player.getDialogueManager().startDialogue("SimpleNPCMessage", npcId, torepair == null ? "I'm afraid you don't have any items that need repairing." : "I can't repair that item.");
            return;
        }
        player.getDialogueManager().startDialogue(new Dialogue() {
            private int totalRechargePrice;

            @Override
            public void start() {
                totalRechargePrice = 0;
                for (Item item : toRecharge) {
                    int maxCharges = ItemDefinitions.getItemDefinitions(item.getDefinitions().getRepairData()[0]).getMaxCharges();
                    int chargesLeft = getChargesLeft(item);
                    int fullrechargeprice = ItemDefinitions.getItemDefinitions(item.getDefinitions().getRepairData()[0]).getRepairPrice();
                    double armourStandModifier = npcId == -1 ? (1.00 - ((double) player.getSkills().getLevel(Skills.SMITHING) / 200.00)) : 1;
                    double perkModifier = player.getPerkManager().hasPerkActive(DonationPerk.THE_DISCOUNTER) ? (item.getDefinitions().getCSOpcode(750) >= 90 ? 0.75 : 0.5) : 1;
                    totalRechargePrice += Math.ceil((1.00 - ((double) chargesLeft / (double) maxCharges)) * (double) fullrechargeprice * armourStandModifier * perkModifier);
                }
                sendCorrectDialogue("Repairing " + (torepair == null ? "all of the items in your inventory" : torepair.getName()) + " will cost you " + Utils.formatNumber(totalRechargePrice) + " GP, Are you sure you want do this?");
            }

            @Override
            public void run(int interfaceId, int componentId) {
                switch (stage) {
                    case -1:
                        stage = 0;
                        sendOptionsDialogue(SEND_DEFAULT_OPTIONS_TITLE, "Yes, repair " + (torepair == null ? "all of the items" : torepair.getName()) + " for " + Utils.formatNumber(totalRechargePrice) + " GP.", "Nevermind.");
                        break;
                    case 0:
                        if (componentId == OPTION_1) {
                            stage = 1;
                            if (player.getInventory().getCoinsAmount() < totalRechargePrice) {
                                sendCorrectDialogue(!armourStand ? "I'm sorry " + player.getDisplayName() + ", but it appears you don't have enough gp to cover the repair fee." : "You don't have enough money to do that.");
                                return;
                            }
                            player.getInventory().deleteAllCoins(totalRechargePrice);
                            for (Item item : toRecharge) {
                                if (torepair == null)
                                    player.getPackets().sendGameMessage("Your " + item.getName() + " has been repaired fully", true);
                                item.setId(item.getDefinitions().getRepairData()[0]);
                                item.setChargesData(null);
                            }
                            player.getInventory().refresh();
                            sendCorrectDialogue(armourStand ? ("You successfully repair " + (torepair == null ? "all of the items in your inventory" : torepair.getName()) + ".") : "There you go " + player.getDisplayName() + ", pleasure doing business with you.");
                            return;
                        }
                        end();
                        break;
                    case 1:
                        end();
                        break;
                }
            }

            @Override
            public void finish() {
            }

            private void sendCorrectDialogue(String... messages) {
                if (armourStand)
                    sendDialogue(messages);
                else
                    sendNPCDialogue(npcId, NORMAL, messages);
            }
        });
    }

    public static int getUnDyedVersion(int id) {
        switch (id) {
            case 38204:
                return 36519;
            case 38206:
                return 36521;
            case 38208:
                return 36519;
            case 38210:
                return 36521;
            case 38212:
                return 36519;
            case 38214:
                return 36521;
            case 38216:
                return 36523;
            case 38218:
                return 36525;
            case 38220:
                return 36523;
            case 38222:
                return 36525;
            case 38224:
                return 36523;
            case 38226:
                return 36525;
            case 38228:
                return 36529;
            case 38230:
                return 36527;
            case 38232:
                return 36527;
            case 38234:
                return 36529;
            case 38236:
                return 36527;
            case 38238:
                return 36529;
            case 38240:
                return 36519;
            case 38242:
                return 36521;
            case 38244:
                return 36523;
            case 38246:
                return 36525;
            case 38248:
                return 36527;
            case 38250:
                return 36529;
            case 42134:
                return 36519;
            case 42136:
                return 36521;
            case 42138:
                return 36527;
            case 42140:
                return 36529;
            case 42142:
                return 36523;
            case 42144:
                return 36525;
            case 43115:
                return 43185;
            case 43117:
                return 43187;
            case 43119:
                return 43185;
            case 43121:
                return 43187;
            case 43123:
                return 43185;
            case 43125:
                return 43187;
            case 43127:
                return 43183;
            case 43129:
                return 43181;
            case 43131:
                return 43181;
            case 43133:
                return 43183;
            case 43135:
                return 43181;
            case 43137:
                return 43183;
            case 43139:
                return 43185;
            case 43141:
                return 43187;
            case 43143:
                return 43181;
            case 43145:
                return 43183;
            case 43147:
                return 43181;
            case 43149:
                return 43183;
            case 43151:
                return 43185;
            case 43153:
                return 43187;
        }
        return -1;
    }

    public int getChargesLeft(Item item) {
        String name = item.getDefinitions().getName().toLowerCase();
        int maxCharge = item.getDefinitions().getMaxCharges();
        return name.contains("broken") || name.contains("empty") ? 0 : item.getChargesData() == null ? maxCharge : item.getChargesData().getChargesLeft();
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public static Integer[] getStaticItemDegradeData(int itemId) {
        ItemDefinitions defs = ItemDefinitions.getItemDefinitions(itemId);
        if (defs.isNoted())
            return null;
        String name = defs.getName().toLowerCase();
        if (((itemId >= 18349 && itemId <= 18374) || (itemId >= 25991 && itemId <= 25996) || (itemId >= 27069 && itemId <= 27072) || (itemId >= 31463 && itemId <= 31464) || (itemId >= 34069 && itemId <= 34096) || (itemId >= 36869 && itemId <= 36880) || (itemId >= 43416 && itemId <= 43437)) && !defs.getName().toLowerCase().contains(" (broken)"))
            return new Integer[]{itemId, itemId + 1};
        if (itemId >= 22458 && itemId <= 22497 && !name.contains(" (degraded)"))
            return new Integer[]{itemId + 2, name.contains("visor") ? 22452 : name.contains("leggings") ? 22454 : name.contains("poncho") ? 22456 : 22498};
        switch (itemId) {
            case 34150:
                return new Integer[]{34150, 34151};
            case 34155:
                return new Integer[]{34155, 34153};
            case 34158:
                return new Integer[]{34158, 34156};
            case 24338:
                return new Integer[]{24338, 24339};
            case 41069:
                return new Integer[]{41069, 41007};
            case 26322:// sup tetsu helm
            case 26323:// sup tetsu body
            case 26324:// sup tetsu legs
            case 26334:// sup seasinger hood
            case 26335:// sup seasinger top
            case 26336:// sup seasinger bottom
                return new Integer[]{itemId, itemId + 9};
            case 26352:// sup death lotus
            case 26353:// sup death lotus
            case 26354:// sup death lotus
            case 38805:
            case 38806:
            case 38807:
            case 38811:
            case 38812:
            case 38813:
                return new Integer[]{itemId, itemId + 3};
        }
        return null;
    }

    public static Integer[] getStaticItemRepairData(int itemId) {
        ItemDefinitions defs = ItemDefinitions.getItemDefinitions(itemId);
        if (defs.isNoted())
            return null;
        String name = defs.getName().toLowerCase();
        if (((itemId >= 18349 && itemId <= 18374) || (itemId >= 25991 && itemId <= 25996) || (itemId >= 27069 && itemId <= 27072) || (itemId >= 31463 && itemId <= 31464) || (itemId >= 34069 && itemId <= 34096) || (itemId >= 36869 && itemId <= 36880) || (itemId >= 43416 && itemId <= 43437)))
            return new Integer[]{itemId - (name.contains(" (broken)") ? 1 : 0)};
        if (itemId >= 22458 && itemId <= 22497 && name.contains(" (degraded)"))
            return new Integer[]{itemId - (itemId % 2 == 0 ? 2 : 3)};
        switch (itemId) {
            case 26322:// sup tetsu helm
            case 26323:// sup tetsu body
            case 26324:// sup tetsu legs
            case 26334:// sup seasinger hood
            case 26335:// sup seasinger top
            case 26336:// sup seasinger bottom
                return new Integer[]{itemId};
            case 26331:
            case 26332:
            case 26333:
            case 26343:
            case 26344:
            case 26345:
                return new Integer[]{itemId - 9};
            case 26352:// sup death lotus
            case 26353:// sup death lotus
            case 26354:// sup death lotus
            case 38805:
            case 38806:
            case 38807:
            case 38811:
            case 38812:
            case 38813:
                return new Integer[]{itemId};
            case 26355:// sup death lotus
            case 26356:// sup death lotus
            case 26357:// sup death lotus
            case 38808:
            case 38809:
            case 38810:
            case 38814:
            case 38815:
            case 38816:
                return new Integer[]{itemId - 3};
            case 31871:
            case 35378:
                return new Integer[]{31869};
            case 31874:
            case 35379:
                return new Integer[]{31872};
            case 31877:
            case 35380:
                return new Integer[]{31875};
            case 31880:
            case 35381:
                return new Integer[]{31878};
            case 32212:
                return new Integer[]{32210};
            case 32215:
                return new Integer[]{32213};
            case 32218:
                return new Integer[]{32216};
            case 32221:
                return new Integer[]{32219};
            case 32224:
                return new Integer[]{32222};
            case 32227:
                return new Integer[]{32225};
            case 32230:
                return new Integer[]{32228};
            case 32233:
                return new Integer[]{32231};
            case 32236:
                return new Integer[]{32234};
            case 32239:
                return new Integer[]{32237};
            case 32242:
                return new Integer[]{32240};
            case 32245:
                return new Integer[]{32243};
            case 32628:
                return new Integer[]{32627};
            case 32630:
                return new Integer[]{32629};
            case 32632:
                return new Integer[]{32631};
            case 32634:
                return new Integer[]{32633};
            case 32636:
                return new Integer[]{32635};
            case 32638:
                return new Integer[]{32637};
            case 32641:
                return new Integer[]{32640};
            case 32643:
                return new Integer[]{32642};
            case 32648:
                return new Integer[]{32647};
            case 32650:
                return new Integer[]{32649};
            case 32652:
                return new Integer[]{32651};
            case 32654:
                return new Integer[]{32653};
            case 32656:
                return new Integer[]{32655};
            case 32658:
                return new Integer[]{32657};
            case 32660:
                return new Integer[]{32659};
            case 32662:
                return new Integer[]{32661};
            case 32664:
                return new Integer[]{32663};
            case 34980:
                return new Integer[]{34978};
            case 34983:
                return new Integer[]{34981};
            case 34986:
                return new Integer[]{34984};
            case 35720:
                return new Integer[]{35719};
            case 39063:
            case 39064:
                return new Integer[]{39061};
//        case 41584: hex hunter bow
//        case 41585:
//            return new Integer[] { 41582 };
            case 41959:
            case 41960:
                return new Integer[]{41958};
            case 41964:
            case 41965:
                return new Integer[]{41963};
            case 44562:
            case 44563:
                return new Integer[]{44561};
            case 26581:
            case 26582:
                return new Integer[]{26579};
            case 26585:
            case 26586:
                return new Integer[]{26583};
            case 26589:
            case 26590:
                return new Integer[]{26587};
            case 26593:
            case 26594:
                return new Integer[]{26591};
            case 26597:
            case 26598:
                return new Integer[]{26595};
            case 26601:
            case 26602:
                return new Integer[]{26599};
            case 33301:
            case 33302:
                return new Integer[]{33300};
            case 33304:
            case 33305:
                return new Integer[]{33303};
            case 33307:
            case 33308:
                return new Integer[]{33306};
            case 33310:
            case 33311:
                return new Integer[]{33309};
            case 33313:
            case 33314:
                return new Integer[]{33312};
            case 33316:
            case 33317:
                return new Integer[]{33315};
            case 33367:
            case 33368:
                return new Integer[]{33366};
            case 33370:
            case 33371:
                return new Integer[]{33369};
            case 33373:
            case 33374:
                return new Integer[]{33372};
            case 33376:
            case 33377:
                return new Integer[]{33375};
            case 33379:
            case 33380:
                return new Integer[]{33378};
            case 33382:
            case 33383:
                return new Integer[]{33381};
            case 33433:
            case 33434:
                return new Integer[]{33432};
            case 33436:
            case 33437:
                return new Integer[]{33435};
            case 33439:
            case 33440:
                return new Integer[]{33438};
            case 33442:
            case 33443:
                return new Integer[]{33441};
            case 33445:
            case 33446:
                return new Integer[]{33444};
            case 33448:
            case 33449:
                return new Integer[]{33447};
            case 36304:
            case 36305:
                return new Integer[]{36303};
            case 36307:
            case 36308:
                return new Integer[]{36306};
            case 36310:
            case 36311:
                return new Integer[]{36309};
            case 36313:
            case 36314:
                return new Integer[]{36312};
            case 36316:
            case 36317:
                return new Integer[]{36315};
            case 36319:
            case 36320:
                return new Integer[]{36318};
            case 42023:
            case 42024:
                return new Integer[]{42022};
            case 42026:
            case 42027:
                return new Integer[]{42025};
            case 42029:
            case 42030:
                return new Integer[]{42028};
            case 42032:
            case 42033:
                return new Integer[]{42031};
            case 42035:
            case 42036:
                return new Integer[]{42034};
            case 42038:
            case 42039:
                return new Integer[]{42037};
            case 41069:
            case 41007:
                return new Integer[]{41069};
        }
        return null;
    }

    public static class ChargesData implements Serializable {

        private static final long serialVersionUID = 7034522491433094660L;
        @Getter
        @Setter
        private int orignalId, wornId, brokenId, maxCharges, chargesLeft;

        public ChargesData(int orignalId, int wornId, int brokenId, int maxCharges) {
            this.orignalId = orignalId;
            this.wornId = wornId;
            this.brokenId = brokenId;
            this.maxCharges = maxCharges;
            chargesLeft = maxCharges;
        }

        public ChargesData(int orignalId, int wornId, int brokenId, int maxCharges, int chargesLeft) {
            this.orignalId = orignalId;
            this.wornId = wornId;
            this.brokenId = brokenId;
            this.maxCharges = maxCharges;
            this.chargesLeft = chargesLeft;
        }

        @Override
        public String toString() {
            return "ChargesData [orignalId=" + orignalId + ", wornId=" + wornId + ", brokenId=" + brokenId + ", maxCharges=" + maxCharges + ", chargesLeft=" + chargesLeft + "]";
        }

    }

}
