package com.rs.game.player.dialogue.impl;

import com.rs.cache.loaders.ItemDefinitions;
import com.rs.game.player.Skills;
import com.rs.game.player.content.SkillsDialogue;
import com.rs.game.player.content.SkillsDialogue.ItemNameFilter;
import com.rs.game.player.dialogue.Dialogue;

import java.util.ArrayList;
import java.util.List;

public class KhopeshCraftingD extends Dialogue {

    int[] products;

    @Override
    public void start() {
        List<Integer> availableProducts = new ArrayList<Integer>();
        if (hasAllBlessings() && player.getInventory().containsItem(40312, 1))
            availableProducts.add(40655);
        if (hasAllBlessings() && player.getInventory().containsItem(40316, 1))
            availableProducts.add(40659);
        if (availableProducts.isEmpty()) {
            player.getDialogueManager().startDialogue("SimpleMessage", "You don't have the required items to craft this.");
            return;
        }
        products = new int[availableProducts.size()];
        for (int i = 0; i < products.length; i++)
            products[i] = availableProducts.get(i);
        SkillsDialogue.sendSkillsDialogue(player, SkillsDialogue.MAKE, "How many would you like to make?<br>Choose a number, then click the item to begin.", 1, products, new ItemNameFilter() {
            @Override
            public String rename(String name) {
                int levelToMake = 92;
                if (player.getSkills().getLevel(Skills.CRAFTING) < levelToMake)
                    name = "<col=ff0000>" + name + "<br>" + "<col=ff0000>Level " + levelToMake;
                return name;
            }
        });
    }

    private boolean hasAllBlessings() {
        for (int i = 40652; i <= 40654; i++)
            if (!player.getInventory().containsItem(i, 1))
                return false;
        return true;
    }

    @Override
    public void run(int interfaceId, int componentId) {
        end();
        int productId = products[SkillsDialogue.getItemSlot(componentId)];
        int quantity = SkillsDialogue.getQuantity(player);
        int levelToMake = 92;
        if (player.getSkills().getLevel(Skills.CRAFTING) < levelToMake) {
            player.getDialogueManager().startDialogue("SimpleMessage", "You need a crafting level of " + levelToMake + " to create " + ItemDefinitions.getItemDefinitions(productId).getName() + ".");
            return;
        }
        int maxCreateAmount = (hasAllBlessings() && ((productId == 40655 && player.getInventory().containsItem(40312, 1)) || (productId == 40659 && player.getInventory().containsItem(40316, 1)))) ? 1 : 0;
        if (quantity > maxCreateAmount)
            quantity = maxCreateAmount;
        if (quantity == 0) {
            player.getDialogueManager().startDialogue("SimpleMessage", "You don't have the required items to craft this.");
            return;
        }
        for (int i = 40652; i <= 40654; i++)
            player.getInventory().deleteItem(i, 1);
        player.getInventory().deleteItem(productId == 40655 ? 40312 : 40316, 1);
        player.getInventory().addItem(productId, quantity);
        player.getDialogueManager().startDialogue("SimpleItemMessage", productId, 1, "You create " + ItemDefinitions.getItemDefinitions(productId).getName() + ".");
        player.getInventory().refresh();
    }

    @Override
    public void finish() {
    }

}
