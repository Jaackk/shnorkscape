package com.rs.game.player.content;

import java.util.ArrayList;
import java.util.List;

import com.rs.cache.loaders.ItemDefinitions;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.content.SkillsDialogue.ItemNameFilter;
import com.rs.game.player.dialogue.Dialogue;

import lombok.Getter;

public class EliteArmourCreation {
    public static final int alg = 29864, scale = 43164, seaUpgrade = 43073, katanaUpgrade = 43071;

    public enum Armour {
        ELITE_SIRENIC_MASK(43155, 91, 500.00, new Item(29854, 1), new Item(alg, 1), new Item(scale, 140)),

        ELITE_SIRENIC_HAUBERK(43158, 93, 1500.00, new Item(29857, 1), new Item(alg, 3), new Item(scale, 420), new Item(27068, 5)),

        ELITE_SIRENIC_CHAPS(43161, 92, 1500.00, new Item(29860, 1), new Item(alg, 2), new Item(scale, 280)),

        ELITE_SEASINGER_KIBA(43081, 1, 0, new Item(seaUpgrade), new Item(33886)),

        ELITE_SEASINGER_MAKIGAI(43084, 1, 0, new Item(seaUpgrade), new Item(33889)),

        ELITE_TETSU_KATANA(43075, 1, 0, new Item(katanaUpgrade), new Item(33879)),

        ELITE_TETSU_WAKIZASHI(43078, 1, 0, new Item(katanaUpgrade), new Item(33882)),

        ;

        @Getter
        private final int productId;
        @Getter
        private final int requiredLevel;
        @Getter
        private final Item[] requiredItems;
        @Getter
        private final double xp;

        Armour(int productId, int requiredLevel, double xp, Item... requiredItems) {
            this.productId = productId;
            this.requiredLevel = requiredLevel;
            this.requiredItems = requiredItems;
            this.xp = xp;
        }

        public Armour getByRequiredItem(int itemId) {
            for (Armour a : Armour.values()) {
                for (Item item : a.requiredItems) {
                    if (item.getId() != alg && item.getId() == itemId)
                        return a;
                }
            }
            return null;
        }

        public boolean containsItem(int id) {
            for (Item item : requiredItems)
                if (item.getId() == id)
                    return true;
            return false;
        }

    }

    public static boolean isEliteArmourCreation(Player player, Item item) {
        if (item.getId() != scale && item.getId() != seaUpgrade && item.getId() != katanaUpgrade)
            return false;
        List<Armour> available = new ArrayList<Armour>();
        for (Armour a : Armour.values()) {
            if (!a.containsItem(item.getId()))
                continue;
            boolean hasReqs = true;
            for (Item req : a.requiredItems) {
                if (!player.getInventory().containsItem(req.getId(), req.getAmount())) {
                    hasReqs = false;
                    break;
                }
            }
            if (!hasReqs)
                continue;
            available.add(a);
        }
        if (available.isEmpty()) {
            player.getDialogueManager().startDialogue("SimpleMessage", "You don't have enough required items to create any elite " + (item.getId() == scale ? "armour" : "weapon") + ".");
            return true;
        }
        player.getDialogueManager().startDialogue(new Dialogue() {

            @Override
            public void start() {
                int[] items = new int[available.size()];
                for (int i = 0; i < items.length; i++)
                    items[i] = available.get(i).getProductId();
                SkillsDialogue.sendSkillsDialogue(player, SkillsDialogue.MAKE, "Choose how many you wish to make,<br>then click on the item to begin.", 28, items, new ItemNameFilter() {
                    int count = 0;

                    @Override
                    public String rename(String name) {
                        Armour a = available.get(count++);
                        int levelToMake = a.requiredLevel;
                        if (player.getSkills().getLevel(Skills.CRAFTING) < levelToMake)
                            name = "<col=ff0000>" + name + "<br>" + "<col=ff0000>Level " + levelToMake;
                        return name;
                    }
                });
            }

            @Override
            public void run(int interfaceId, int componentId) {
                end();
                Armour a = available.get(SkillsDialogue.getItemSlot(componentId));
                int productId = a.productId;
                int levelToMake = a.requiredLevel;
                Item[] requiredItems = a.requiredItems;
                if (player.getSkills().getLevel(Skills.CRAFTING) < levelToMake) {
                    player.getDialogueManager().startDialogue("SimpleMessage", "You need a crafting level of " + levelToMake + " to craft " + ItemDefinitions.getItemDefinitions(productId).getName() + ".");
                    return;
                }
                for (int i = 0; i < requiredItems.length; i++)
                    if (!player.getInventory().containsItem(requiredItems[i].getId(), requiredItems[i].getAmount()))
                        return;
                player.getInventory().removeItems(requiredItems);
                player.getInventory().addItem(productId, 1);
                if (a.xp > 0)
                    player.getSkills().addXp(Skills.CRAFTING, a.xp);
                player.getInventory().refresh();
            }

            @Override
            public void finish() {
            }

        });
        return true;
    }

}
