package com.rs.game.player.dialogue.impl;

import com.rs.cache.loaders.ItemDefinitions;
import com.rs.game.item.Item;
import com.rs.game.player.content.Pots.Pot;
import com.rs.game.player.dialogue.Dialogue;

import java.util.ArrayList;
import java.util.List;

/**
 * @author armark1ng
 */
public class TeplinMacaganD extends Dialogue {

    private int npcId;
    private int option;

    @Override
    public void start() {
        npcId = (int) parameters[0];
        option = (int) parameters[1];
        switch (option) {
        case 1:
            stage = -1;
            sendOptionsDialogue(SEND_DEFAULT_OPTIONS_TITLE, "Can you convert my flasks into normal vials for me?", "Can you decant my potions into flasks, please?", "Can you decant my flasks back into 6 dosed flasks, please?", "I've got to go.");
            break;
        case 2:
            stage = 0;
            sendNPCDialogue(npcId, NORMAL, "I'll see what i can do. I can make use of the flasks, so I'll give you the vials for free.");
            break;
        case 3:
            stage = 0;
            if (player.getInventory().containsItem(23191, 1) || player.getInventory().containsItem(23191 + 1, 1))
                sendNPCDialogue(npcId, NORMAL, "Lets see what you have. I will decant potion vials and all flasks with up to 4 doses into flasks. You'll need to supply the empty flasks yourself; they're not cheap.");
            else
                sendNPCDialogue(npcId, NORMAL, "You'll need to supply the empty flasks yourself; they're not cheap. You can bring them to me noted or unnoted.");
            break;
        case 4:
            stage = 0;
            sendNPCDialogue(npcId, NORMAL, "Let's see what you have. I will decant your potion flasks back into 6 dosed flasks. I can make use of the flasks, so I'll take those as payment.");
            break;
        }
    }

    @Override
    public void run(int interfaceId, int componentId) {
        switch (stage) {
        case -1:
            switch (componentId) {
            case OPTION_1:
            case OPTION_2:
            case OPTION_3:
                player.getDialogueManager().startDialogue("TeplinMacaganD", npcId, componentId == OPTION_1 ? 2 : componentId == OPTION_2 ? 3 : 4);
                break;
            default:
                end();
                break;
            }
            break;
        case 0:
            if (option == 3) {
                if (player.getInventory().containsItem(23191, 1) || player.getInventory().containsItem(23191 + 1, 1))
                    startDecantingProcess();
                else
                    end();
                return;
            }
            end();
            startDecantingProcess();
            break;
        }
    }

    private void startDecantingProcess() {
        switch (option) {
        case 2:
            List<Item> potionsToAdd = new ArrayList<>();
            for (Pot flask : Pot.values()) {
                int flaskDoses = 0;
                if (flask == null || !flask.isFlask())
                    continue;
                for (int i = flask.getId().length - 1; i >= 0; i--) {
                    int id = flask.getId()[i];
                    if (player.getInventory().containsItem(id, 1)) {
                        flaskDoses += player.getInventory().getAmountOf(id) * (flask.getId().length - i);
                    }
                    if (player.getInventory().containsItem(ItemDefinitions.getItemDefinitions(id).getCertId(), 1)) {
                        flaskDoses += player.getInventory().getAmountOf(ItemDefinitions.getItemDefinitions(id).getCertId()) * (flask.getId().length - i);
                    }
                }
                Pot potion = flask.toPotion();
                if (potion == null)
                    continue;
                boolean hasThisFlask = false;
                for (int flaskItemId : flask.getId()) {
                    if (player.getInventory().containsItem(flaskItemId, 1))
                        hasThisFlask = true;
                }
                for (int flaskItemId : flask.getNotedId()) {
                    if (player.getInventory().containsItem(flaskItemId, 1))
                        hasThisFlask = true;
                }
                if (!hasThisFlask)
                    continue;
                int dosesRemoved = 0;
                for (int i = 6; i > 0; i--) {
                    while (dosesRemoved <= flaskDoses && (player.getInventory().containsItem(flask.getIdForDoses(i), 1) || player.getInventory().containsItem(ItemDefinitions.getItemDefinitions(flask.getIdForDoses(i)).getCertId(), 1))) {
                        if (player.getInventory().containsItem(flask.getIdForDoses(i), 1)) {
                            player.getInventory().deleteItem(new Item(flask.getIdForDoses(i), 1));
                            dosesRemoved += i;
                        }
                        if (player.getInventory().containsItem(ItemDefinitions.getItemDefinitions(flask.getIdForDoses(i)).getCertId(), 1)) {
                            player.getInventory().deleteItem(new Item(ItemDefinitions.getItemDefinitions(flask.getIdForDoses(i)).getCertId(), 1));
                            dosesRemoved += i;
                        }
                    }
                }
                for (int i = 4; i > 0; i--) {
                    int amount = (dosesRemoved / i);
                    if (amount == 0 || dosesRemoved <= 0)
                        continue;
                    int itemId = ItemDefinitions.getItemDefinitions(potion.getIdForDoses(i)).getCertId();
                    if (itemId == -1)
                        itemId = potion.getIdForDoses(i);
                    Item item = new Item(itemId, amount);
                    potionsToAdd.add(item);
                    dosesRemoved = (dosesRemoved % i);
                }
            }
            for (Item potion : potionsToAdd) {
                if (potion == null)
                    continue;
                player.getInventory().addItemDrop(potion.getId(), potion.getAmount());
            }
            player.getDialogueManager().startDialogue("SimpleNPCMessage", npcId, potionsToAdd.size() > 0 ? "Here you go. That's the best i can do!" : "Sorry you don't seem to have anything I can help you with! Make sure you have the correct potions and some free space in your inventory and I can try again.");
            break;
        case 3:
            potionsToAdd = new ArrayList<>();
            int maxDoses = (player.getInventory().getAmountOf(23191) * 6) + (player.getInventory().getAmountOf(23191 + 1) * 6);
            for (Pot potion : Pot.values()) {
                int doses = 0;
                if (potion == null || !potion.isPotion())
                    continue;
                for (int i = 3; i >= 0; i--) {
                    int id = potion.getId()[i];
                    if (player.getInventory().containsItem(id, 1)) {
                        doses += player.getInventory().getAmountOf(id) * (potion.getId().length - i);
                    }
                    if (player.getInventory().containsItem(ItemDefinitions.getItemDefinitions(id).getCertId(), 1)) {
                        doses += player.getInventory().getAmountOf(ItemDefinitions.getItemDefinitions(id).getCertId()) * (potion.getId().length - i);
                    }
                }
                Pot flask = potion.toFlask();
                if (flask == null)
                    continue;
                boolean hasThisPotion = false;
                for (int flaskItemId : potion.getId()) {
                    if (player.getInventory().containsItem(flaskItemId, 1))
                        hasThisPotion = true;
                }
                for (int flaskItemId : potion.getNotedId()) {
                    if (player.getInventory().containsItem(flaskItemId, 1))
                        hasThisPotion = true;
                }
                if (!hasThisPotion)
                    continue;
                doses = doses > maxDoses ? maxDoses : doses;
                while (doses % 6 != 0)
                    doses--;
                for (int i = doses; i > 0; i -= 6) {
                    if (player.getInventory().containsItem(23191, 1)) {
                        player.getInventory().deleteItem(23191, 1);
                        continue;
                    }
                    if (player.getInventory().containsItem(23191 + 1, 1)) {
                        player.getInventory().deleteItem(23191 + 1, 1);
                    }
                }
                int dosesRemoved = 0;
                for (int i = 4; i > 0; i--) {
                    while (dosesRemoved <= doses && (player.getInventory().containsItem(potion.getIdForDoses(i), 1) || player.getInventory().containsItem(ItemDefinitions.getItemDefinitions(potion.getIdForDoses(i)).getCertId(), 1))) {
                        if (player.getInventory().containsItem(potion.getIdForDoses(i), 1)) {
                            player.getInventory().deleteItem(new Item(potion.getIdForDoses(i), 1));
                            int toRemove = dosesRemoved + i > doses ? doses - dosesRemoved : i;
                            if (toRemove == i) {
                                potionsToAdd.add(new Item(229 + 1, 1));
                                dosesRemoved += i;
                            } else {
                                potionsToAdd.add(new Item(ItemDefinitions.getItemDefinitions(potion.getIdForDoses(i - toRemove)).certId, 1));
                                dosesRemoved += toRemove;
                            }
                        }
                        if (player.getInventory().containsItem(ItemDefinitions.getItemDefinitions(potion.getIdForDoses(i)).getCertId(), 1)) {
                            player.getInventory().deleteItem(new Item(ItemDefinitions.getItemDefinitions(potion.getIdForDoses(i)).getCertId(), 1));
                            int toRemove = dosesRemoved + i > doses ? doses - dosesRemoved : i;
                            if (toRemove == i) {
                                potionsToAdd.add(new Item(229 + 1, 1));
                                dosesRemoved += i;
                            } else {
                                potionsToAdd.add(new Item(ItemDefinitions.getItemDefinitions(potion.getIdForDoses(i - toRemove)).certId, 1));
                                dosesRemoved += toRemove;
                            }
                        }
                    }
                }
                for (int i = 6; i > 0; i--) {
                    int amount = (dosesRemoved / i);
                    if (amount == 0 || dosesRemoved <= 0)
                        continue;
                    int itemId = ItemDefinitions.getItemDefinitions(flask.getIdForDoses(i)).getCertId();
                    if (itemId == -1)
                        itemId = flask.getIdForDoses(i);
                    Item item = new Item(itemId, amount);
                    potionsToAdd.add(item);
                    dosesRemoved = (dosesRemoved % i);
                }
            }
            for (Item potion : potionsToAdd) {
                if (potion == null)
                    continue;
                player.getInventory().addItemDrop(potion.getId(), potion.getAmount());
            }
            player.getDialogueManager().startDialogue("SimpleNPCMessage", npcId, potionsToAdd.size() > 0 ? "Here you go. That's the best i can do!" : "Sorry you don't seem to have anything I can help you with! Make sure you have the correct potions and some free space in your inventory and I can try again.");
            break;
        case 4:
            potionsToAdd = new ArrayList<>();
            for (Pot flask : Pot.values()) {
                if (flask == null || !flask.isFlask())
                    continue;
                int flaskDoses = 0;
                for (int i = flask.getId().length - 1; i >= 0; i--) {
                    int id = flask.getId()[i];
                    if (player.getInventory().containsItem(id, 1)) {
                        flaskDoses += player.getInventory().getAmountOf(id) * (flask.getId().length - i);
                    }
                    if (player.getInventory().containsItem(ItemDefinitions.getItemDefinitions(id).getCertId(), 1)) {
                        flaskDoses += player.getInventory().getAmountOf(ItemDefinitions.getItemDefinitions(id).getCertId()) * (flask.getId().length - i);
                    }
                }
                boolean hasThisFlask = false;
                for (int flaskItemId : flask.getId()) {
                    if (player.getInventory().containsItem(flaskItemId, 1))
                        hasThisFlask = true;
                }
                for (int flaskItemId : flask.getNotedId()) {
                    if (player.getInventory().containsItem(flaskItemId, 1))
                        hasThisFlask = true;
                }
                if (!hasThisFlask)
                    continue;
                int dosesRemoved = 0;
                for (int i = 6; i > 0; i--) {
                    while (dosesRemoved <= flaskDoses && (player.getInventory().containsItem(flask.getIdForDoses(i), 1) || player.getInventory().containsItem(ItemDefinitions.getItemDefinitions(flask.getIdForDoses(i)).getCertId(), 1))) {
                        if (player.getInventory().containsItem(flask.getIdForDoses(i), 1)) {
                            player.getInventory().deleteItem(new Item(flask.getIdForDoses(i), 1));
                            dosesRemoved += i;
                        }
                        if (player.getInventory().containsItem(ItemDefinitions.getItemDefinitions(flask.getIdForDoses(i)).getCertId(), 1)) {
                            player.getInventory().deleteItem(new Item(ItemDefinitions.getItemDefinitions(flask.getIdForDoses(i)).getCertId(), 1));
                            dosesRemoved += i;
                        }
                    }
                }
                for (int i = 6; i > 0; i--) {
                    int amount = (dosesRemoved / i);
                    if (amount == 0 || dosesRemoved <= 0)
                        continue;
                    int itemId = ItemDefinitions.getItemDefinitions(flask.getIdForDoses(i)).getCertId();
                    if (itemId == -1)
                        itemId = flask.getIdForDoses(i);
                    Item item = new Item(itemId, amount);
                    potionsToAdd.add(item);
                    dosesRemoved = (dosesRemoved % i);
                }
            }
            for (Item potion : potionsToAdd) {
                if (potion == null)
                    continue;
                player.getInventory().addItemDrop(potion.getId(), potion.getAmount());
            }
            player.getDialogueManager().startDialogue("SimpleNPCMessage", npcId, potionsToAdd.size() > 0 ? "Here you go. That's the best i can do!" : "Sorry you don't seem to have anything I can help you with! Make sure you have the correct potions and some free space in your inventory and I can try again.");
            break;
        }
    }

    @Override
    public void finish() {

    }

}
