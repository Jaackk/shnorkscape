package com.rs.game.player.dialogue.impl;

import com.rs.cache.loaders.ItemDefinitions;
import com.rs.game.player.Skills;
import com.rs.game.player.content.SkillsDialogue;
import com.rs.game.player.content.SkillsDialogue.ItemNameFilter;
import com.rs.game.player.dialogue.Dialogue;

import java.util.ArrayList;
import java.util.List;

public class BlessingsCreateD extends Dialogue {
    int[] products;

    @Override
    public void start() {
        List<Integer> availableProducts = new ArrayList<Integer>();
        if (player.getInventory().getAmountOf(40651) >= 100) {
            for (int i = 0; i < 3; i++)
                availableProducts.add(40652 + i);
        }
        if (availableProducts.isEmpty()) {
            player.getDialogueManager().startDialogue("SimpleMessage", "You don't have enough Scraps of scripture to do this.");
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

    @Override
    public void run(int interfaceId, int componentId) {
        end();
        int productId = products[SkillsDialogue.getItemSlot(componentId)];
        int quantity = SkillsDialogue.getQuantity(player);
        int levelToMake = 92;
        if (player.getSkills().getLevel(Skills.CRAFTING) < levelToMake) {
            player.getDialogueManager().startDialogue("SimpleMessage", "You need a crafting level of " + levelToMake + " to combine " + ItemDefinitions.getItemDefinitions(40651).getName() + " into " + ItemDefinitions.getItemDefinitions(productId).getName() + ".");
            return;
        }
        int maxCreateAmount = player.getInventory().getAmountOf(40651) / 100;
        if (quantity > maxCreateAmount)
            quantity = maxCreateAmount;
        if (quantity == 0)
            return;
        int cost = quantity * 100;
        if (player.getInventory().getAmountOf(40651) < cost) {
            player.getDialogueManager().startDialogue("SimpleMessage", "You don't have enough Scraps of scripture to do this.");
            return;
        }
        player.getInventory().deleteItem(40651, cost);
        player.getInventory().addItem(productId, quantity);
        player.getDialogueManager().startDialogue("SimpleItemMessage", productId, 1, "You combine " + cost + " Scraps of scripture into " + ItemDefinitions.getItemDefinitions(productId).getName() + ".");
        player.getInventory().refresh();
    }

    @Override
    public void finish() {
    }

}
