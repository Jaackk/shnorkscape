package com.rs.game.player;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.rs.Settings;
import com.rs.cache.loaders.ItemDefinitions;
import com.rs.game.item.Item;
import com.rs.game.player.PerkManager.DonationPerk;
import com.rs.game.player.content.ItemConstants;
import com.rs.utils.Colors;
import com.rs.utils.Logger;
import com.rs.utils.Utils;

import lombok.val;

public class ChargesManager implements Serializable {

    public static final String REPLACE = "##";
    private static final long serialVersionUID = -5978513415281726450L;
    private transient Player player;
    private final HashMap<Integer, Integer> charges;

    public ChargesManager() {
        charges = new HashMap<Integer, Integer>();
    }

    public void setPlayer(final Player player) {
        this.player = player;
    }
/*
    public void process() {
        final Item[] items = player.getEquipment().getItems().getItems();
        for (int slot = 0; slot < items.length; slot++) {
            final Item item = items[slot];
            if (item == null) {
                continue;
            }

            if (player.getAttackedByDelay() > Utils.currentTimeMillis()) {
                final int newId = ItemConstants.getDegradedItemVariantForCombat(item.getId());
                if (newId != -1) {
                    player.getPackets().sendGameMessage(item.getDefinitions().getName() + " has degraded slightly!");
                    item.setId(newId);
                    player.getEquipment().refresh(slot);
                    player.getAppearence().generateAppearenceData();
                }
            }
            final int defaultCharges = ItemConstants.getItemDefaultCharges(item.getId());
            if (defaultCharges == -1) {
                continue;
            }
            if (ItemConstants.itemDegradesWhileWearing(item.getId())) {
                degrade(item.getId(), defaultCharges, slot);
            } else if (player.getAttackedByDelay() > Utils.currentTimeMillis() && ItemConstants.itemDegradesWhileCombating(item.getId())) {
                degrade(item.getId(), defaultCharges, slot);
            }
        }
    }

    public void die() {
        final Item[] equipItems = player.getEquipment().getItems().getItems();
        for (int slot = 0; slot < equipItems.length; slot++) {
            if (equipItems[slot] != null && degradeCompletely(equipItems[slot])) {
                player.getEquipment().getItems().set(slot, null);
            }
        }
        final Item[] invItems = player.getInventory().getItems().getItems();
        for (int slot = 0; slot < invItems.length; slot++) {
            if (invItems[slot] != null && degradeCompletely(invItems[slot])) {
                player.getInventory().getItems().set(slot, null);
            }
        }
    }


    public boolean degradeCompletely(final Item item) {
        final int defaultCharges = ItemConstants.getItemDefaultCharges(item.getId());
        if (defaultCharges == -1) {
            return false;
        }
        while (true) {
            if (ItemConstants.itemDegradesWhileWearing(item.getId()) || ItemConstants.itemDegradesWhileCombating(item.getId())) {
                charges.remove(item.getId());
                final int newId = ItemConstants.getBrokenVariantOfItem(item.getId());
                if (newId == -1) {
                    return ItemConstants.getItemDefaultCharges(item.getId()) != -1;
                }
                item.setId(newId);
            } else {
                final int newId = ItemConstants.getBrokenVariantOfItem(item.getId());
                if (newId != -1) {
                    charges.remove(item.getId());
                    item.setId(newId);
                }
                break;
            }
        }
        return false;
    }

    public void wear(final int slot) {
        final Item item = player.getEquipment().getItems().get(slot);
        if (item == null) {
            return;
        }
        final int newId = ItemConstants.getDegradeItemWhenWear(item.getId());
        if (newId == -1) {
            return;
        }

        player.getEquipment().getItems().set(slot, new Item(newId, 1));
        player.getEquipment().refresh(slot);
        player.getAppearence().generateAppearenceData();
        if (isT90PowerArmour(newId) && getCharges(newId) <= -1) {
            player.sendMessage("Your " + item.getDefinitions().getName() + " has degraded.");
        } else {
            player.getPackets().sendGameMessage(item.getDefinitions().getName() + " has degraded slightly!");
        }
    }

    private void degrade(final int itemId, final int defaultCharges, final int slot) {
        Integer c = charges.remove(itemId);
        if (c == null) {
            c = defaultCharges;
        } else {
            if (player.getPerkManager().hasPerkActive(DonationPerk.CHARGE_BEFRIENDER)) {
                boolean highLevel = false;
                final HashMap<Integer, Integer> requiriments = ItemDefinitions.getItemDefinitions(itemId).getWearingSkillRequiriments();
                if (requiriments != null) {
                    for (Map.Entry<Integer, Integer> entry : requiriments.entrySet()) {
                        int skillId = entry.getKey();
                        if (skillId > 24 || skillId < 0) {
                            continue;
                        }
                        final int level = entry.getValue();
                        if (level > 80) {
                            highLevel = true;
                        }
                    }
                }

                final int amountToDegrade = highLevel ? Utils.random(2) : 0;
                if (amountToDegrade > 0) {
                    c -= amountToDegrade;
                }
            } else {
                c--;
            }
            if (c == 0) {
                if (isT90PowerArmour(itemId)) {
                    charges.put(itemId, -1);
                    player.getEquipment().refresh(slot);
                    player.getAppearence().generateAppearenceData();
                    player.sendMessage("Your " + ItemDefinitions.getItemDefinitions(itemId).getName() + " has completely ran out of charges and lost its bonuses.");
                    return;
                }

                final int newId = ItemConstants.getBrokenVariantOfItem(itemId);
                String newItemName = ItemDefinitions.getItemDefinitions(newId).getName().toLowerCase();
                player.getEquipment().getItems().set(slot, newId != -1 ? new Item(newId, 1) : null);
                if (newId == -1) {
                    player.getPackets().sendGameMessage(ItemDefinitions.getItemDefinitions(itemId).getName() + " turned into dust.");
                } else if (newItemName.contains("anima core") && !newItemName.contains("refined")) {
                    player.getPackets().sendGameMessage("Your " + ItemDefinitions.getItemDefinitions(itemId).getName() + " has degraded back to its unrefined state.");
                } else if (newItemName.contains("broken)")) {
                    player.getPackets().sendGameMessage(Colors.RED + "Your " + ItemDefinitions.getItemDefinitions(itemId).getName() + " has ran out of charges and degraded to a broken state!");
                } else {
                    player.getPackets().sendGameMessage(ItemDefinitions.getItemDefinitions(itemId).getName() + " has degraded slightly!");
                }
                player.getEquipment().refresh(slot);
                player.getAppearence().generateAppearenceData();
                return;
            }
        }

        charges.put(itemId, c);
    }

    public static final boolean isT90PowerArmour(final int itemId) {
        val name = ItemDefinitions.getItemDefinitions(itemId).getName();
        return name.startsWith("Sirenic ") || name.startsWith("Tectonic ")
                || name.startsWith("Malevolent ") && !name.contains("kiteshield");
    }

    public void dust(final Item item) {
        final int newId = ItemConstants.getItemDustOnDeath(item.getId());
        final String itemname = item.getDefinitions().getName();
        if (newId != -1) {
            item.setId(newId);
            player.sendMessage(Colors.RED + "Your " + itemname + " has turned into ashes.");
        }
    }

    public void death(final Item item) {
        final int newId = ItemConstants.getDegradeItemOnDeath(item.getId());
        if (newId != -1) {
            item.setId(newId);
        }
    }

    public void checkPercentage(final String message, final int id, final boolean reverse) {
        final int charges = getCharges(id);
        final int maxCharges = ItemConstants.getItemDefaultCharges(id);
        int percentage = reverse ? (charges == 0 ? 0 : (100 - (charges * 100 / maxCharges))) : charges == 0 ? 100 : (charges * 100 / maxCharges);
        if (id == 41069 && charges == 0) {
            percentage = 0;
        }
        player.getPackets().sendGameMessage(message.replace(REPLACE, String.valueOf(percentage)));
        if (Settings.DEBUG || Settings.TEST_SERVER_MODE) {
            player.sendMessage("Charges: " + charges);
        }
    }

    public boolean checkCharges(final Item item) {
        if (item.getDefinitions().containsInventoryOption(2, "Inspect")
                || item.getDefinitions().containsInventoryOption(2, "Check")
                || item.getDefinitions().containsInventoryOption(2, "Check state")
                || item.getDefinitions().containsInventoryOption(2, "Check-charges")
                || item.getDefinitions().containsInventoryOption(2, "Check Charges")
                || item.getDefinitions().containsInventoryOption(2, "Check-Charges")
                || item.getDefinitions().containsInventoryOption(2, "Check charges")
                || item.getDefinitions().containsInventoryOption(2, "Check Charges")) {
            // exeptions.
            if (item.getId() == 11284 || item.getId() == 25559 || item.getId() == 25562) {
                player.getDialogueManager().startDialogue("SimpleMessage",
                        "There are no charges left within this shield.");
            } else if (item.getId() == 11283 || item.getId() == 25558 || item.getId() == 25561) {
                checkCharges("There are " + REPLACE + " charges remaining in your dragonfire shield.", item.getId());
            } else if (item.getId() == 22444) {
                checkCharges("There is " + REPLACE + " doses of neem oil remaining.", item.getId());
            } else if ((item.getId() >= 24450 && item.getId() <= 24454)
                    || (item.getId() >= 22358 && item.getId() <= 22369)) {
                checkPercentage("The gloves are " + REPLACE + "% degraded.", item.getId(), true);
            } else if (item.getId() >= 22458 && item.getId() <= 22497) {
                checkPercentage(item.getName() + ": " + REPLACE + "% remaining.", item.getId(), false);
            } else if (item.getId() == 20171 || item.getId() == 20173) {
                checkPercentage(item.getName() + ": has " + getCharges(item.getId()) + " shots left.", item.getId(),
                        false);
            } else {
                // default, add exception if not same message in rs.
                checkPercentage("Your " + item.getName().toLowerCase() + " has " + REPLACE + "% of its charges left.",
                        item.getId(), false);
            }
            return true;
        }
        return false;
    }

    public void checkCharges(final String message, final int id) {
        player.getPackets().sendGameMessage(message.replace(REPLACE, String.valueOf(getCharges(id))));
    }

    public int getCharges(final int id) {
        final Integer c = charges.get(id);
        return c == null ? 0 : c;
    }

    public void setCharges(final int id, final int amount) {
        charges.put(id, amount);
    }


    public void addCharges(final int id, final int amount, final int wearSlot) {
        final int maxCharges = ItemConstants.getItemDefaultCharges(id);
        if (maxCharges == -1) {
            Logger.getGlobal().info("This item cant get charges atm " + id);
            return;
        }
        final Integer c = charges.get(id);
        int amt = c == null ? maxCharges : amount + c;
        if (amt > maxCharges) {
            amt = maxCharges;
        }
        if (amt <= 0) {
            final int newId = ItemConstants.getBrokenVariantOfItem(id);
            if (newId == -1) {
                if (wearSlot == -1) {
                    player.getInventory().deleteItem(id, 1);
                } else {
                    player.getEquipment().getItems().set(wearSlot, null);
                }
            } else if (wearSlot == -1) {
                player.getInventory().deleteItem(id, 1);
                player.getInventory().addItem(newId, 1);
            } else {
                final Item item = player.getEquipment().getItem(wearSlot);
                if (item == null) {
                    return;
                }
                item.setId(newId);
                player.getEquipment().refresh(wearSlot);
                player.getAppearence().generateAppearenceData();
            }
            resetCharges(id);
        } else {
            charges.put(id, amt);
        }
    }

    public void resetCharges(final int id) {
        charges.remove(id);
    }*/
}