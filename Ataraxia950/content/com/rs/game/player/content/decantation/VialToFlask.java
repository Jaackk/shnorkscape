package com.rs.game.player.content.decantation;

import com.rs.cache.loaders.ItemDefinitions;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.content.Pots;

import java.util.Arrays;
import java.util.Map;

/**
 * @author lare96 <http://github.com/lare96>
 */
public class VialToFlask {

    public static void vialsToFlasks(Player player) {
        boolean nothing = true;
        int emptyFlaskId = ItemDefinitions.getItemDefinitions(23191).certId;
        for (Map.Entry<Pots.Pot, Pots.Pot> entry : Pots.Pot.VIAL_TO_FLASK.entrySet()) {
            Pots.Pot vial = entry.getKey();
            Pots.Pot flask = entry.getValue();
            boolean hasVial = player.getInventory().containsOneItem(vial.notedId);
            boolean hasFlask = player.getInventory().containsOneItem(flask.notedId) || player.getInventory().containsOneItem(emptyFlaskId);
            if (!hasVial || !hasFlask) {
                continue;
            }
            int vialDoses = Arrays.stream(vial.notedId).map(it -> player.getInventory().getAmountOf(it) * vial.getNotedVialDose(it)).sum();
            int fullFlasks = player.getInventory().getAmountOf(flask.notedId[0]);

            int flaskSpace = player.getInventory().getAmountOf(emptyFlaskId) * 6;
            for (int flaskIndex = 0; flaskIndex < flask.notedId.length; flaskIndex++) {
                int flaskId = flask.notedId[flaskIndex];
                flaskSpace += player.getInventory().getAmountOf(flaskId) * flaskIndex;
            }
            if (flaskSpace < 6) {
                break;
            }
            int leftoverFlaskSpace = flaskSpace - vialDoses;
            int emptyFlaskIndex = player.getInventory().getItems().lookupSlot(emptyFlaskId);
            if (emptyFlaskIndex != -1)
                player.getInventory().set(emptyFlaskIndex, null);
            Arrays.stream(vial.notedId).
                    map(it -> player.getInventory().getItems().lookupSlot(it)).
                    forEach(it -> {
                        if (it != -1) {
                            player.getInventory().set(it, null);
                        }
                    });
            Arrays.stream(flask.notedId).
                    map(it -> player.getInventory().getItems().lookupSlot(it)).
                    forEach(it -> {
                        if (it != -1) {
                            player.getInventory().set(it, null);
                        }
                    });
            player.addItem(new Item(flask.notedId[0], fullFlasks));
            if (leftoverFlaskSpace > 0) {
                // leftover flasks
                int flaskAmount = leftoverFlaskSpace / 6;
                int remainingDoses = getRemainingDoses(leftoverFlaskSpace % 6);
                if (flaskAmount > 0) {
                    player.addItem(new Item(emptyFlaskId, flaskAmount));
                }
                if (remainingDoses > 0) {
                    int lastFlask = getFlaskDose(flask, remainingDoses);
                    player.addItem(new Item(lastFlask));
                }
                flaskAmount = vialDoses / 6;
                remainingDoses = vialDoses % 6;
                player.addItem(new Item(flask.notedId[0], flaskAmount));
                if (remainingDoses > 0) {
                    int lastFlask = getFlaskDose(flask, remainingDoses);
                    player.addItem(new Item(lastFlask));
                }
            } else {
                // leftover vials
                int leftoverVialDoses = Math.abs(leftoverFlaskSpace);
                int vialAmount = leftoverVialDoses / 4;
                int remainingDoses = getRemainingDoses(leftoverVialDoses % 4);
                if (vialAmount > 0) {
                    player.addItem(new Item(vial.notedId[0], vialAmount));
                }
                if (remainingDoses > 0) {
                    int lastVial = getVialDose(vial, remainingDoses);
                    player.addItem(new Item(lastVial));
                }
                int filledDoses = vialDoses - leftoverVialDoses;
                int flaskAmount = filledDoses / 6;
                remainingDoses = filledDoses % 6;
                player.addItem(new Item(flask.notedId[0], flaskAmount));
                if (remainingDoses > 0) {
                    int lastFlask = getFlaskDose(flask, remainingDoses);
                    player.addItem(new Item(lastFlask));
                }
            }
            nothing = false;
        }

        if (nothing) {
            player.sendMessage("You have no potions to transfer to flasks.");
        }
    }

    public static boolean oneVialToFlask(Player player, Item vialUsedItem, Item flaskUsedItem) {
        int vialUsed = vialUsedItem.getId();
        int flaskUsed = flaskUsedItem.getId();
        int emptyFlaskId = 23191;
        for (Map.Entry<Pots.Pot, Pots.Pot> entry : Pots.Pot.VIAL_TO_FLASK.entrySet()) {
            Pots.Pot vial = entry.getKey();
            Pots.Pot flask = entry.getValue();

            boolean clickedBoth = vial.matches(vialUsed) && (flask.matches(flaskUsed) || flaskUsed == emptyFlaskId) &&
                    player.getInventory().containsOneItem(vialUsed) && player.getInventory().containsOneItem(flaskUsed);
            if (!clickedBoth) {
                continue;
            }
            int vialDose = vial.getVialDose(vialUsed);
            int flaskDose = flaskUsed == emptyFlaskId ? 0 : flask.getFlaskDose(flaskUsed);
            if (flaskDose == 6) {
                return false;
            }
            int newDose = vialDose + flaskDose;
            int remainingDose = newDose - 6;
            player.getInventory().deleteItem(new Item(vialUsed));
            player.getInventory().deleteItem(new Item(flaskUsed));
            if (remainingDose > 0) {
                int newVial = getUnnotedVialDose(vial, remainingDose);
                player.getInventory().addItem(new Item(flask.id[0]));
                player.getInventory().addItem(new Item(newVial));
            } else {
                int newFlask = getUnnotedFlaskDose(flask, newDose);
                player.getInventory().addItem(new Item(newFlask));
            }
            player.sendMessage("You pour from one container into the other.");
            return true;
        }
        return false;
    }

    public static boolean oneFlaskToVial(Player player, Item vialUsedItem, Item flaskUsedItem) {
        int vialUsed = vialUsedItem.getId();
        int flaskUsed = flaskUsedItem.getId();
        int emptyVialId = 229;
        for (Map.Entry<Pots.Pot, Pots.Pot> entry : Pots.Pot.VIAL_TO_FLASK.entrySet()) {
            Pots.Pot vial = entry.getKey();
            Pots.Pot flask = entry.getValue();

            boolean clickedBoth = (vial.matches(vialUsed) || vialUsed == emptyVialId) && flask.matches(flaskUsed) &&
                    player.getInventory().containsOneItem(vialUsed) && player.getInventory().containsOneItem(flaskUsed);
            if (!clickedBoth) {
                continue;
            }
            int vialDose = vial.getVialDose(vialUsed);
            int flaskDose = flask.getFlaskDose(flaskUsed);
            if (vialDose == 4) {
                return false;
            }
            int newDose = vialDose + flaskDose;
            int remainingDose = newDose - 4;
            player.getInventory().deleteItem(new Item(vialUsed));
            player.getInventory().deleteItem(new Item(flaskUsed));
            if (remainingDose > 0) {
                int newFlask = getUnnotedFlaskDose(flask, remainingDose);
                player.getInventory().addItem(new Item(vial.id[0]));
                player.getInventory().addItem(new Item(newFlask));
            } else {
                int newVial = getUnnotedVialDose(vial, newDose);
                player.getInventory().addItem(new Item(newVial));
            }
            player.sendMessage("You pour from one container into the other.");
            return true;
        }
        return false;
    }


    private static int getFlaskDose(Pots.Pot flask, int dose) {
        if (flask.isPotion())
            throw new RuntimeException();
        return flask.notedId[6 - dose];
    }

    private static int getVialDose(Pots.Pot vial, int dose) {
        if (vial.isFlask())
            throw new RuntimeException();
        return vial.notedId[4 - dose];
    }
    private static int getUnnotedFlaskDose(Pots.Pot flask, int dose) {
        if (flask.isPotion())
            throw new RuntimeException();
        return flask.id[6 - dose];
    }

    private static int getUnnotedVialDose(Pots.Pot vial, int dose) {
        if (vial.isFlask())
            throw new RuntimeException();
        return vial.id[4 - dose];
    }
    private static int getRemainingDoses(int space) {
        switch (space) {
            case 0:
                return 0;
            case 1:
                return 5;
            case 2:
                return 4;
            case 3:
                return 3;
            case 4:
                return 2;
            case 5:
                return 1;
            default:
                throw new RuntimeException();
        }
    }
}