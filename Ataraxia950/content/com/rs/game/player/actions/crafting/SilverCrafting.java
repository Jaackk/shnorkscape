package com.rs.game.player.actions.crafting;

import com.rs.game.Animation;
import com.rs.game.WorldObject;
import com.rs.game.activites.halloween.ClueScrollDistributor;
import com.rs.game.item.Item;
import com.rs.game.npc.NPC;
import com.rs.game.npc.others.randomevent.impl.SmithingRandomEvent;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.Action;
import com.rs.utils.Colors;
import com.rs.utils.Utils;

import java.util.HashMap;
import java.util.Map;

/**
 * Handles the Crafting of Silver Bar products.
 *
 * @author Noel
 */
public class SilverCrafting extends Action {

    public SmeltingBar bar;
    public WorldObject object;
    public int ticks;

    public SilverCrafting(int slotId, WorldObject object, int ticks) {
        this.bar = SmeltingBar.forId(slotId);
        this.object = object;
        this.ticks = ticks;
    }

    @Override
    public boolean process(Player player) {
        if (bar == null || player == null || object == null)
            return false;
        if (!player.getInventory().containsItem(bar.getItemsRequired()[0].getId(), bar.getItemsRequired()[0].getAmount())) {
            player.sendMessage("You need " + bar.getItemsRequired()[0].getDefinitions().getName() + " to create a " + bar.getProducedBar().getDefinitions().getName() + ".");
            return false;
        }
        if (bar.getItemsRequired().length > 1) {
            if (!player.getInventory().containsItem(bar.getItemsRequired()[1].getId(), bar.getItemsRequired()[1].getAmount())) {
                player.sendMessage("You need " + bar.getItemsRequired()[1].getDefinitions().getName() + " to create a " + bar.getProducedBar().getDefinitions().getName() + ".");
                return false;
            }
        }
        if (player.getSkills().getLevel(Skills.CRAFTING) < bar.getLevelRequired()) {
            player.getDialogueManager().startDialogue("SimpleMessage", "You need a Crafting level of at least " + bar.getLevelRequired() + " to smelt " + bar.getProducedBar().getDefinitions().getName());
            return false;
        }
        if (Utils.random(500) == 0 && player.hasRandomEvent()) {
            if (!player.followedByRandomEventNPC()) {
                NPC npc = new SmithingRandomEvent(player, player);
                npc.setNextAnimation(new Animation(-1));
                if (npc.withinDistance(player, 14)) {
                    player.setCurrentRandomEventNPC(npc);
                    player.sendMessage("<col=ff0000>A Dwarven Miner appears from nowhere.");
                }
            }
        }
        player.faceObject(object);
        return true;
    }

    @Override
    public int processWithDelay(Player player) {
        ticks--;
        player.setNextAnimation(new Animation(32626));
        player.getSkills().addXp(Skills.CRAFTING, bar.getExperience());
        for (Item required : bar.getItemsRequired()) {
            player.getInventory().deleteItem(required.getId(), required.getAmount());
        }
        player.getInventory().addItem(bar.getProducedBar());
        player.addItemsMade();
        player.sendMessage("You make a " + bar.getProducedBar().getDefinitions().getName().toLowerCase() + "; " + "items crafted: " + Colors.RED + Utils.getFormattedNumber(player.getItemsMade()) + "</col>.", true);
        ClueScrollDistributor.givePlayerClueScrollIfProbable(player, ClueScrollDistributor.SKILLING_PERCENT);
        if (ticks > 0)
            return 1;
        return -1;
    }

    @Override
    public boolean start(Player player) {
        if (bar == null || player == null || object == null)
            return false;
        if (!player.getInventory().containsItem(bar.getItemsRequired()[0].getId(), bar.getItemsRequired()[0].getAmount())) {
            player.sendMessage("You need " + bar.getItemsRequired()[0].getDefinitions().getName() + " to create a " + bar.getProducedBar().getDefinitions().getName() + ".");
            return false;
        }
        if (bar.getItemsRequired().length > 1) {
            if (!player.getInventory().containsItem(bar.getItemsRequired()[1].getId(), bar.getItemsRequired()[1].getAmount())) {
                player.sendMessage("You need " + bar.getItemsRequired()[1].getDefinitions().getName() + " to create a " + bar.getProducedBar().getDefinitions().getName() + ".");
                return false;
            }
        }
        if (player.getSkills().getLevel(Skills.CRAFTING) < bar.getLevelRequired()) {
            player.sendMessage("You need a Crafting level of at least " + bar.getLevelRequired() + " to make a " + bar.getProducedBar().getDefinitions().getName());
            return false;
        }
        player.sendMessage("You place the Silver bar and attempt to create a " + bar.getProducedBar().getDefinitions().getName().toLowerCase() + ".", true);
        return true;
    }

    @Override
    public void stop(Player player) {
        this.setActionDelay(player, 3);
    }

    public enum SmeltingBar {

        SILVER_SICKLE(18, 50, new Item[] { new Item(2355, 1) }, new Item(2961, 1), 0),

        HOLY_SYMBOL(16, 50, new Item[] { new Item(2355, 1) }, new Item(1718, 1), 1),

        UNHOLY_SYMBOL(16, 50, new Item[] { new Item(2355, 1) }, new Item(1724, 1), 2),

        UNCHARGED_TIARA(23, 52.5, new Item[] { new Item(2355, 1) }, new Item(5525, 1), 3);

        private static final Map<Integer, SmeltingBar> bars = new HashMap<Integer, SmeltingBar>();

        static {
            for (SmeltingBar bar : SmeltingBar.values()) {
                bars.put(bar.getButtonId(), bar);
            }
        }

        private final int levelRequired;
        private final double experience;
        private final Item[] itemsRequired;
        private final int buttonId;
        private final Item producedBar;

        SmeltingBar(int levelRequired, double experience, Item[] itemsRequired, Item producedBar, int buttonId) {
            this.levelRequired = levelRequired;
            this.experience = experience;
            this.itemsRequired = itemsRequired;
            this.producedBar = producedBar;
            this.buttonId = buttonId;
        }

        public static SmeltingBar forId(int buttonId) {
            return bars.get(buttonId);
        }

        public int getButtonId() {
            return buttonId;
        }

        public double getExperience() {
            return experience;
        }

        public Item[] getItemsRequired() {
            return itemsRequired;
        }

        public int getLevelRequired() {
            return levelRequired;
        }

        public Item getProducedBar() {
            return producedBar;
        }
    }
}