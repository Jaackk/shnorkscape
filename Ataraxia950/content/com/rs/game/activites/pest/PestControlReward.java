package com.rs.game.activites.pest;

import com.google.common.collect.Range;
import com.rs.cache.loaders.ItemDefinitions;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.utils.Colors;
import lombok.val;
import lombok.var;

import java.util.concurrent.ThreadLocalRandom;
import java.util.function.IntSupplier;

/**
 * @author lare96 <http://github.com/lare96>
 */
public final class PestControlReward {

    public static final int DROP_TABLE_ROLLS = 2;

    private final IntSupplier id;
    private final Range<Integer> amount;
    private final int chance; // 1 out of <chance>
    private final boolean alwaysRoll;

    private PestControlReward(IntSupplier id, Range<Integer> amount, int chance, boolean alwaysRoll) {
        this.id = id;
        this.amount = amount;
        this.chance = chance;
        this.alwaysRoll = alwaysRoll;
    }

    public PestControlReward(IntSupplier id, int minAmount, int maxAmount, int chance, boolean alwaysRoll) {
        this.id = id;
        this.amount = Range.closed(minAmount, maxAmount);
        this.chance = chance;
        this.alwaysRoll = alwaysRoll;
    }

    public PestControlReward(IntSupplier id, int amount, int chance, boolean alwaysRoll) {
        this.id = id;
        this.amount = Range.singleton(amount);
        this.chance = chance;
        this.alwaysRoll = alwaysRoll;
    }

    public PestControlReward(int id, int minAmount, int maxAmount, int chance, boolean alwaysRoll) {
        this(() -> id, minAmount, maxAmount, chance, alwaysRoll);
    }

    public PestControlReward(int id, int amount, int chance, boolean alwaysRoll) {
        this(() -> id, amount, chance, alwaysRoll);
    }

    public boolean roll(Player player, boolean debug) {
        if (chance == 1 || ThreadLocalRandom.current().nextInt(chance) == 0) {
            Item item = getItem();
            if (!debug) {
                player.sendMessage("You have received an additional reward: " + display(item));
                player.addItem(item);
            } else {
                player.getBank().addItem(item, true);
            }
            return true;
        }
        return false;
    }

    public String display(Item item) {
        return Colors.RED + ItemDefinitions.getItemDefinitions(item.getId()).name + " (x" + item.getAmount() + ")";
    }

    public Item getItem() {
        val newId = id.getAsInt();
        var amt = amount.lowerEndpoint();
        if (amt == -1) {
            amt = ItemDefinitions.getItemDefinitions(newId).isStackable() ? 30 : 1;
            return new Item(newId, amt);
        }
        return new Item(newId, ThreadLocalRandom.current().nextInt(amt, amount.upperEndpoint() + 1));
    }

    public int getChance() {
        return chance;
    }

    public boolean isAlwaysRoll() {
        return alwaysRoll;
    }
}