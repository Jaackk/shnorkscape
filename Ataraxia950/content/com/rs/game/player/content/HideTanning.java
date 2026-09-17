package com.rs.game.player.content;

import com.rs.cache.loaders.ItemDefinitions;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.content.SkillsDialogue.ItemNameFilter;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class HideTanning {

    public static int[][] hides = { { 1739, 1741, 1743 }, { 1753, 1745 }, { 1751, 2505 }, { 1749, 2507 }, { 1747, 2509 }, { 6287, 6289 }, { 24372, 24374 } };

    public static void tanHides(Player player, boolean portable) {
        List<Integer> availableProducts = new ArrayList<Integer>();
        for (int i = 0; i < hides.length; i++) {
            if (player.getInventory().containsItem(hides[i][0], 1)) {
                if (hides[i].length > 2)
                    for (int j = 1; j < hides[i].length; j++)
                        availableProducts.add(hides[i][j]);
                else
                    availableProducts.add(hides[i][1]);
            }
        }
        if (availableProducts.isEmpty()) {
            if (portable)
                player.sendMessage("You have no hides in your inventory that need tanning.");
            else
                player.getDialogueManager().startDialogue("SimpleNPCMessage", 2824, "You don't have any hides on you, come back later when you do.");
            return;
        }
        player.getDialogueManager().startDialogue(new Dialogue() {
            int[] products;

            @Override
            public void start() {
                products = new int[availableProducts.size()];
                for (int i = 0; i < products.length; i++)
                    products[i] = availableProducts.get(i);
                SkillsDialogue.sendSkillsDialogue(player, SkillsDialogue.MAKE, "How many would you like to make?<br>Choose a number, then click the item to begin.", 28, products, new ItemNameFilter() {
                    int count = 0;

                    @Override
                    public String rename(String name) {
                        int productId = products[count++];
                        int levelToMake = productId == 1741 ? 1 : 2;
                        int cost = productId == 1741 ? 0 : productId == 1743 ? 3 : productId == 6289 ? 15 : 20;
                        if (player.getSkills().getLevel(Skills.CRAFTING) < levelToMake)
                            name = "<col=ff0000>" + name + "<br>" + "<col=ff0000>Level " + levelToMake;
                        name += "<br>" + (cost == 0 ? "Free" : ((!player.hasMoney(cost) ? "<col=ff0000>Cost " : "Cost ") + cost));
                        return name;

                    }
                });
            }

            @Override
            public void run(int interfaceId, int componentId) {
                end();
                int productId = products[SkillsDialogue.getItemSlot(componentId)];
                int quantity = SkillsDialogue.getQuantity(player);
                int hide = getHideByLeather(productId);
                int cost = productId == 1741 ? 0 : productId == 1743 ? 3 : productId == 6289 ? 15 : 20;
                int levelToMake = productId == 1741 ? 1 : 2;
                if (player.getSkills().getLevel(Skills.CRAFTING) < levelToMake) {
                    if (portable)
                        player.getDialogueManager().startDialogue("SimpleMessage", "You need a crafting level of " + levelToMake + " to tan " + ItemDefinitions.getItemDefinitions(productId).getName() + ".");
                    else
                        player.getDialogueManager().startDialogue("SimpleNPCMessage", 2824, "You need a crafting level of " + levelToMake + " to tan " + ItemDefinitions.getItemDefinitions(productId).getName() + ".");
                    return;
                }
                if (quantity > player.getInventory().getAmountOf(hide))
                    quantity = player.getInventory().getAmountOf(hide);
                if (quantity == 0)
                    return;
                int totalPrice = cost * quantity;
                if (!player.hasMoney(totalPrice)) {
                    if (portable)
                        player.getDialogueManager().startDialogue("SimpleMessage", "It seems that you don't have enough coins on you, " + "you'll need " + Utils.getFormattedNumber(cost) + " coins to tan " + quantity + " of your hides.");
                    else
                        player.getDialogueManager().startDialogue("SimpleNPCMessage", 2824, "It seems that you don't have enough coins on you, " + "you'll need " + Utils.getFormattedNumber(cost) + " coins to tan " + quantity + " of your hides.");
                    return;
                }
                player.getInventory().deleteItem(hide, quantity);
                player.getInventory().addItem(productId, quantity);
                if (!portable) {
                    player.sendMessage("The Tanner tans " + quantity + " hides for you.", true);
                    player.getDialogueManager().startDialogue("SimpleNPCMessage", 2824, "There you go, sir!");
                }
                player.getInventory().refresh();
                player.takeMoney(totalPrice);
            }

            @Override
            public void finish() {
            }

        });
        return;
    }

    public static int getHideByLeather(int productId) {
        for (int i = 0; i < hides.length; i++) {
            for (int j = 1; j < hides[i].length; j++) {
                if (hides[i][j] == productId)
                    return hides[i][0];
            }
        }
        return 0;
    }

}