package com.rs.game.activities.seasonalevents.christmas;

import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableList;
import com.rs.cache.loaders.ItemDefinitions;
import com.rs.game.activities.seasonalevents.SeasonalEventManager;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.content.ItemConstants;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.Logger;
import com.rs.utils.Utils;
import lombok.Data;
import lombok.val;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author lare96
 */
public final class PresentHandler {

    @Data
    private static final class PresentItem {
        private final int id;
        private final int min;
        private final int max;

        public Item toItem() {
            return new Item(id, min == max ? min : ThreadLocalRandom.current().nextInt(min, max + 1));
        }
    }

    private static final int CHARCOAL_CHANCE = 64;
    private static final ImmutableList<PresentItem> COMMON = ImmutableList.of(
            new PresentItem(4079, 1, 1), // Yo-yo
            new PresentItem(10507, 1, 1), // Reindeer hat
            new PresentItem(20077, 1, 1), // Salty claws hat
            new PresentItem(30395, 1, 1), // Tinsel Scarf
            new PresentItem(30396, 1, 1), // Festive Jumper
            new PresentItem(39275, 1, 1), // Cmas tree kite
            new PresentItem(15426, 1, 1), // Candy cane
            new PresentItem(26493, 1, 1), // Cmas tree hat
            new PresentItem(4084, 1, 1), // Sled
            new PresentItem(6856, 1, 1), // Bobble hat
            new PresentItem(6857, 1, 1), // Bobble scarf
            new PresentItem(11950, 1, 1), // Snow globe
            new PresentItem(6866, 1, 1), // Green marionette
            new PresentItem(6867, 1, 1), // Red marionette
            new PresentItem(6865, 1, 1) // Blue marionette
    );
    private static final ImmutableList<PresentItem> RARE = ImmutableList.of(
            new PresentItem(36118, 1, 1), // Penguin plushie 
            new PresentItem(36083, 1, 1), // Penguin Snowboard 
            new PresentItem(19325, 1, 1), // Penguin staff
            new PresentItem(36082, 1, 1), // Penguin head 
            new PresentItem(13109, 1, 1), // Penguin mask 
            new PresentItem(36084, 1, 1), // Penguin torso
            new PresentItem(36085, 1, 1), // Penguin legs 
            new PresentItem(36086, 1, 1), // Penguin feet 
            new PresentItem(36087, 1, 1), // Penguin gloves
            new PresentItem(33730, 1, 1), // Snowboard tier 3 
            new PresentItem(44523, 1, 1), // Snow parasol
            new PresentItem(15422, 1, 1), // Cmas ghost hood 
            new PresentItem(15423, 1, 1), // Cmas ghost top
            new PresentItem(15425, 1, 1), // Cmas ghost bottoms
            new PresentItem(39390, 1, 1),  // Stringy
            new PresentItem(6858, 1, 1), // Jester hat
            new PresentItem(6859, 1, 1), // Jester scarf
            new PresentItem(14596, 1, 1), // Ice amulet
            new PresentItem(14595, 1, 1), // Santa top
            new PresentItem(14602, 1, 1), // Santa gloves
            new PresentItem(14603, 1, 1), // Santa legs
            new PresentItem(14605, 1, 1) // Santa boots
    );
    private static final ImmutableList<PresentItem> VERY_RARE = ImmutableList.of(
            new PresentItem(30412, 1, 1), // Black santa hat
            new PresentItem(36079, 1, 1), // Black santa hat w / beard
            new PresentItem(13537, 1, 1), // Green santa hat
            new PresentItem(30368, 1, 1), // Rory the reindeer
            new PresentItem(39265, 1, 1), // Reindeer terrorbird mount
            new PresentItem(36117, 1, 1), // Snowverload plushie
            new PresentItem(30380, 1, 1), // San'tar spawnling
            new PresentItem(36165, 1, 1), // Ring of snow
            new PresentItem(6860, 1, 1), // Tri-jester hat
            new PresentItem(6861, 1, 1), // Tre-jester scarf
            new PresentItem(36080, 1, 1) // Santa hat w/ beard

    );

    public static Item givePresentReward(Player player, boolean openPresent) {
        Item item = null;
        if (openPresent) {
            if (!SeasonalEventManager.isActive(ChristmasSeasonalEvent.class)) {
                player.sendMessage("This can only be opened during the month of December.");
                return null;
            }
            if (ThreadLocalRandom.current().nextInt(CHARCOAL_CHANCE) == 0) {
                item = new Item(44612, 1);
            }
        }
        if (item == null) {
            Stopwatch stopwatch = Stopwatch.createStarted();
            List<PresentItem> choices = new ArrayList<>();
            boolean dontCheck = false;
            if (ThreadLocalRandom.current().nextInt(150) == 0) {
                dontCheck = true;
                choices.addAll(VERY_RARE);
            } else if (ThreadLocalRandom.current().nextInt(75) == 0) {
                choices.addAll(RARE);
            } else {
                choices.addAll(COMMON);
            }
            Collections.shuffle(choices);
            PresentItem foundItem = null;
            if(!dontCheck) {
                for (val next : choices) {
                    if(ItemConstants.isTradeable(new Item(next.getId()))) {
                        foundItem = next;
                        break;
                    }
                    if (!player.hasItem(next.getId())) {
                        foundItem = next;
                        break;
                    }
                }
            }
            if (foundItem == null) {
                foundItem = Utils.randomFrom(choices);
            }
            item = foundItem.toItem();
            val elapsed = stopwatch.elapsed().toMillis();
            if (elapsed > 100) {
                Logger.getGlobal().warn("Christmas present took way too long to open! {}ms", elapsed);
            }
        }
        player.addItem(item);
        if (openPresent) {
            Dialogue.sendSingleItemDialogue(player, item.getId(), item.getAmount(), "You open the stolen present.");
            if (ThreadLocalRandom.current().nextInt(256) == 0) {
                player.addItem(new Item(962));
                player.sendMessage("You find a Christmas cracker in there as well!");
            } else if (ThreadLocalRandom.current().nextInt(12) == 0) {
                player.addItem(new Item(19467, ThreadLocalRandom.current().nextInt(2, 8)));
                player.sendMessage("You find some gingersnap cookies in there as well!");

            }
        }
        return item;
    }
}
