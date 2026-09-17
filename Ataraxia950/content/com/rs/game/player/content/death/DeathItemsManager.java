package com.rs.game.player.content.death;

import com.rs.cores.CoresManager;
import com.rs.game.item.Item;
import com.rs.game.map.bossInstance.BossInstanceHandler;
import com.rs.game.map.bossInstance.BossInstanceHandler.Boss;
import com.rs.game.map.bossInstance.InstanceSettings;
import com.rs.game.player.Equipment;
import com.rs.game.player.Player;
import com.rs.game.player.actions.invention.InventionConstants.Perks;
import com.rs.game.player.actions.invention.InventionData.Perk;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Colors;
import com.rs.utils.SerializableFilesManager;
import com.rs.utils.Utils;
import lombok.val;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Handles the items on death -> coins conversion and all other functionality
 * related to it.
 *
 * @author lare96
 */
public final class DeathItemsManager implements Serializable {

    private static final long serialVersionUID = -2715364791548900958L;
    private transient Player player;
    private volatile List<Item> items;
    private volatile List<Item> claimItems;
    private double tax = 1.0;
    private int totalDeaths;

    public DeathItemsManager(Player player) {
        this.player = player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public void resetTax() {
        tax = 1.0;
    }

    public void addClaimedItems() {
        if (items == null || items.isEmpty())
            return;
        if (claimItems == null)
            claimItems = new ArrayList<>();
        claimItems.addAll(items);
        items.clear();
    }

    public void withdrawClaimedItems() {
        if (claimItems == null || claimItems.isEmpty()) {
            player.sendMessage("You have no items to claim.");
            return;
        }
        boolean breakLoop = false;
        Iterator<Item> iter = claimItems.iterator();
        while (iter.hasNext()) {
            if (player.getInventory().isFull()) {
                breakLoop = true;
                break;
            }
            Item next = iter.next();
            val def = next.getDefinitions();
            boolean hasSpaceFor = player.getInventory().hasSpaceFor(next.getId(), next.getAmount());
            if (def != null && !def.isStackable() && !def.isNoted() && !hasSpaceFor) {
                next = new Item(def.getCertId(), next.getAmount());
            } else if(!hasSpaceFor) {
                breakLoop = true;
                break;
            }

            player.addItem(next);
            iter.remove();
        }
        if (breakLoop) {
            player.sendMessage(Colors.RED + "You still have " + claimItems.size() + " item(s) left to claim. Free up some inventory space first.");
        } else {
            player.sendMessage(Colors.RED + "You have claimed all your items!");
        }
    }

    public int getTotalCost() {
        if (items == null || items.isEmpty()) {
            return 0;
        }
        int totalCost = 0;
        for (Item item : items) {
            if (item == null)
                continue;
            totalCost += item.getDefinitions().getValue();
        }
        if (totalCost < 0) {
            totalCost = Integer.MAX_VALUE;
        }
        double cost;
        if (totalCost < 50_000_000) {
            cost = totalCost * 0.50 + 250_000;
        } else if (totalCost < 100_000_000) {
            cost = (totalCost * 0.40) + 2_500_000.0;
        } else if (totalCost < 300_000_000) {
            cost = (totalCost * 0.25) + 5_000_000.0;
        } else if (totalCost < 750_000_000) {
            cost = (totalCost * 0.05) + 7_500_000.0;
        } else if (totalCost < 1_500_000_000) {
            cost = (totalCost * 0.02) + 10_000_000.0;
        } else {
            cost = (totalCost * 0.02) + 12_500_000.0;
        }
        cost *= tax;
        return (int) cost;
    }

    public boolean degradeItems(int index, Item item, double percentage, boolean inventory, boolean skipElite) {
        if (!player.heardDeathsWarning) {
            return false;
        }
        if (item != null && item.getDefinitions().getName().startsWith("Elite ") && skipElite)
            return false;
        return player.getChargesManagerNew().useCharge(index, item, inventory, percentage);
    }

    public void handleDeath() {
        boolean skipElite = player.getTemporaryAttributtes().remove("skipElite") != null;
        WorldTasksManager.schedule(new WorldTask() {
            @Override
            public void run() {
                BossInstanceHandler.createInstance(player, new InstanceSettings(Boss.Death));
            }
        }, 3);
        int hoursPlayedDiff = 72 - Utils.getHoursPlayed(player.getCreationDate());
        if (!player.heardDeathsWarning) {
            player.sendMessage(Colors.RED + "Death would like to speak with you...");
            return;
        }
        if (hoursPlayedDiff > 0) {
            player.sendMessage(Colors.RED + "You have " + hoursPlayedDiff + " hours of free deaths remaining.");
            return;
        }
        DeathStatistics.getInstance().totalDeaths.incrementAndGet();
        totalDeaths++;
        int percentageInt = getDegradePercentageInt();
        double percentageDouble = percentageInt / 100.0;
        if (items == null) {
            items = new ArrayList<>();
        }
        boolean skulled = player.hasSkull();
        boolean protectingItem = player.getPrayer().isProtectingItem();
        int keepAmount = 3;
        if (skulled) {
            keepAmount = 0;
        }
        if (protectingItem) {
            keepAmount++;
            Perk hoarding = player.getInventionManager().hasPerk(Perks.HOARDING);
            if (!player.isCanPvp() && hoarding != null)
                keepAmount++;
        }
        boolean hasDegradableItem = false;
        LinkedList<Item> newItems = new LinkedList<>();
        for (int index = 0; index < player.getInventory().items.getItems().length; index++) {
            Item item = player.getInventory().getItem(index);
            if (item != null) {
                if (item.getId() == 995) {
                    player.getInventory().set(index, null);
                    continue;
                }
                if (degradeItems(index, item, percentageDouble, true, skipElite)) {
                    hasDegradableItem = true;
                    continue;
                }
                newItems.add(item);
                player.getInventory().set(index, null);
                player.getInventory().refresh(index);
            }
        }
        for (int index = 0; index < player.getEquipment().getItems().getItems().length; index++) {
            if (index == Equipment.SLOT_AURA)
                continue;
            Item item = player.getEquipment().getItem(index);
            if (item != null) {
                if (item.getId() == 41069 || item.getId() == 31871)
                    continue;
                if (degradeItems(index, item, percentageDouble, false, skipElite)) {
                    hasDegradableItem = true;
                    continue;
                }
                newItems.add(item);
                player.getEquipment().set(index, null);
                player.getEquipment().refresh(index);
            }
        }
        if (hasDegradableItem) {
            player.sendMessage(Colors.RED + "Your Repairable items have degraded by " + (percentageInt * 2) + "% of their max charges, items that degrade to dust have degraded by " + percentageInt + "% of their max charges.");
        }
        if (!items.isEmpty()) {
            tax += 0.25;
            player.sendMessage(Colors.RED + "Death already has a set of items from you! Tax increased to " + getFormattedTax() + "%.");
        }

        Comparator<Item> itemComparator = Comparator.comparingInt(o -> o.getDefinitions().getDeathValue());
        newItems.sort(Collections.reverseOrder(itemComparator));

        for (int loop = 0; loop < keepAmount; loop++) {
            Item keepItem = newItems.pollFirst();
            if (keepItem == null) {
                break;
            }
            player.addItem(keepItem);
        }
        items.addAll(newItems);
        player.getInventory().refresh();
        player.getEquipment().refresh();
        CoresManager.getServiceProvider().executeNow(() -> SerializableFilesManager.savePlayer(player));
    }

    public void resetDeaths() {
        totalDeaths = 0;
    }

    public int getItemCount() {
        if (items == null)
            return 0;
        return items.size();
    }

    public int getFormattedTax() {
        return (int) ((tax - 1.0) * 100.0);
    }

    public int getDegradePercentageInt() {
        int value = (totalDeaths / 10);
        if (value < 1)
            value = 1;
        else if (value > 10)
            value = 10;
        return value;
    }

    public int getClaimableItemCount() {
        if (claimItems == null)
            return 0;
        return claimItems.size();
    }

    public List<Item> getClaimableItems() {
        if (claimItems == null)
            claimItems = new ArrayList<>();
        return claimItems;
    }

}
