package com.rs.game.player.content;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.TemporaryAttributes;
import com.rs.utils.Colors;
import lombok.Data;
import lombok.val;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

@Data
public final class TriskKeyBag implements Serializable {

    private static final long serialVersionUID = -8004354520382832635L;

    private static final int DEFAULT_SIZE = 30;

    public static void fill(Player player, Item item) {
        val bag = attr(item);
        int maxSize = bag.size;
        if (bag.fragments.size() >= maxSize) {
            player.sendMessage("Your bag cannot fit anymore key fragments.");
            return;
        }
        List<Integer> fragments = new ArrayList<>();
        for (val next : player.getInventory().getItemArray()) {
            if (next == null) {
                continue;
            }
            if (next.getId() == 28547 ||
                    next.getId() == 28548 ||
                    next.getId() == 28549) {
                fragments.add(next.getId());
            }
        }
        if (fragments.isEmpty()) {
            player.sendMessage("No fragments were found in your inventory.");
            return;
        }
        for (int nextFragment : fragments) {
            if (bag.fragments.size() >= maxSize) {
                player.sendMessage("Your bag could not fit all of the fragments.");
                return;
            }
            if (player.getInventory().deleteOneItem(new Item(nextFragment))) {
                bag.fragments.add(nextFragment);
            }
        }
        player.sendMessage("You fill your bag with the fragments.");
    }

    public static void withdraw(Player player, Item item) {
        TriskKeyBag bag = attr(item);
        if (bag.fragments.isEmpty()) {
            player.sendMessage("There are no fragments to withdraw.");
            return;
        }
        Iterator<Integer> iter = bag.fragments.iterator();
        int spaceLeft = player.getInventory().getFreeSlots();
        while (iter.hasNext()) {
            int nextFragment = iter.next();
            if (spaceLeft <= 0) {
                player.sendMessage("You do not have enough space in your inventory.");
                return;
            }
            spaceLeft--;
            player.getInventory().addItem(nextFragment, 1);
            iter.remove();
        }
    }

    public static void withdrawToBank(Player player, Item item) {
        TriskKeyBag bag = attr(item);
        if (bag.withdrawToBank) {
            Iterator<Integer> iter = bag.fragments.iterator();
            int spaceLeft = player.getBank().getRemainingSlots();
            while (iter.hasNext()) {
                int nextFragment = iter.next();
                if (spaceLeft <= 0) {
                    player.sendMessage("You do not have enough space in your bank for all the fragments.");
                    break;
                }
                spaceLeft--;
                player.getBank().addItem(new Item(nextFragment, 1), false);
                iter.remove();
            }
            player.getBank().refreshItems();
        }
    }

    public static void check(Player player, Item item) {
        int count = getTotalCount(item);
        if (count == 0) {
            player.sendMessage("The bag is empty.");
        } else {
            player.sendMessage("Your bag contains a total of " + count + " key fragments.");
        }
    }

    public static void toggleWithdrawToBank(Player player, Item item) {
        val bag = attr(item);
        if (bag.withdrawToBank) {
            player.sendMessage("The next time you open your bank, deposit all key fragments from this bag [" + Colors.RED + "Disabled</col>].");
            bag.withdrawToBank = false;
        } else {
            player.sendMessage("The next time you open your bank, deposit all key fragments from this bag [" + Colors.GREEN + "Enabled</col>].");
            bag.withdrawToBank = true;
        }
    }

    private static int getTotalCount(Item item) {
        return attr(item).fragments.size();
    }

    public static TriskKeyBag attr(Item item) {
        Optional<TriskKeyBag> bag = item.getAttribute(TemporaryAttributes.Key.TRISK_BAG);
        if (!bag.isPresent()) {
            val newBag = new TriskKeyBag();
            item.getAttributesWithOutReset().put(TemporaryAttributes.Key.TRISK_BAG, newBag);
            return newBag;
        }
        return bag.get();
    }

    private int size = DEFAULT_SIZE;
    private Multiset<Integer> fragments = HashMultiset.create();
    private boolean withdrawToBank;
}
