package com.rs.game.player.content.packs.portable;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.rs.game.WorldObject;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.actions.CMLFletching;
import com.rs.game.player.actions.Cooking;
import com.rs.game.player.actions.crafting.CraftingRs3Dialogue;
import com.rs.game.player.actions.crafting.GemCutting;
import com.rs.game.player.actions.crafting.LeatherCrafting;
import com.rs.game.player.actions.firemaking.Bonfire;
import com.rs.game.player.actions.fletching.Fletching;
import com.rs.game.player.actions.herblore.Herblore;
import com.rs.game.player.actions.smithing.Smithing.ForgingInterface;
import com.rs.game.player.content.HideTanning;
import com.rs.game.player.dialogue.impl.ProteanCraftingD;
import com.rs.game.player.dialogue.impl.ProteanFletchingD;
import com.rs.game.player.dialogue.impl.ProteanSmithingD;
import com.rs.game.player.dialogue.impl.SmeltingD;
import com.rs.utils.DialogueOptionEvent;
import lombok.val;

import java.util.Arrays;

/**
 * @author _jordan <jordan.abraham1997@gmail.com>
 * <p>
 * Created on Oct 9, 2018.
 */
public enum PortableType implements Portable {

    BRAZIER(35228, 106601) {
        @Override
        public void handleObjectClick1(Player player, WorldObject object) {
            Bonfire.addLogs(player, object);
        }

        @Override
        public void handleObjectClick2(Player player, WorldObject object) {
            // TODO Auto-generated method stub

        }

        @Override
        public void handleObjectClick3(Player player, WorldObject object) {
            // TODO Auto-generated method stub

        }

        @Override
        public void handleObjectClick4(Player player, WorldObject object) {
            // TODO Auto-generated method stub

        }

    },

    RANGE(31042, 89768) {
        @Override
        public void handleObjectClick1(Player player, WorldObject object) {
            Cooking.performPortableAction(player, object);
        }

        @Override
        public void handleObjectClick2(Player player, WorldObject object) {
            // TODO Auto-generated method stub

        }

        @Override
        public void handleObjectClick3(Player player, WorldObject object) {
            // TODO Auto-generated method stub

        }

        @Override
        public void handleObjectClick4(Player player, WorldObject object) {
            // TODO Auto-generated method stub

        }

    },

    FORGE(31041, 114206) {
        @Override
        public void handleObjectClick1(Player player, WorldObject object) {
            if (player.getInventory().containsItem(new Item(31350))) {
                player.getDialogueManager().startDialogue(ProteanSmithingD.class.getSimpleName(), player.getInventory().getAmountOf(31350) > 60 ? 60 : player.getInventory().getAmountOf(31350), PortableType.isPortableObject(object.getId()));
                return;
            }
            ForgingInterface.sendSmithingBarSelection(player, object);
        }

        @Override
        public void handleObjectClick2(Player player, WorldObject object) {
            player.getDialogueManager().startDialogue(SmeltingD.class.getSimpleName(), object);
        }

        @Override
        public void handleObjectClick3(Player player, WorldObject object) {
            // TODO Auto-generated method stub

        }

        @Override
        public void handleObjectClick4(Player player, WorldObject object) {
            // TODO Auto-generated method stub

        }

    },

    WELL(31044, 89770) {
        @Override
        public void handleObjectClick1(Player player, WorldObject object) {
            Herblore.performPortableAction(player, object);
        }

        @Override
        public void handleObjectClick2(Player player, WorldObject object) {
            int amount = player.getInventory().getFreeSlots();
            if (amount < 1) {
                player.sendMessage("Inventory too full. Sell, drop or bank something for more space.");
                return;
            }

            player.getInventory().addItem(Herblore.VIAL, amount);
        }

        @Override
        public void handleObjectClick3(Player player, WorldObject object) {
            // TODO Auto-generated method stub

        }

        @Override
        public void handleObjectClick4(Player player, WorldObject object) {
            // TODO Auto-generated method stub

        }

    },

    CRAFTER(35226, 106594) {
        @Override
        public void handleObjectClick1(Player player, WorldObject object) {
            player.sendOptionsDialogue("What type of crafting would you like to do?", new String[]{"Leather", "Glassblowing"}, new DialogueOptionEvent() {

                @Override
                public void run(Player player) {
                    int option = getOption();

                    if (option == OPTION_1) {
                        if (player.getInventory().containsItem(new Item(33740))) {
                            player.getDialogueManager().startDialogue(ProteanCraftingD.class.getSimpleName(), 33740, player.getInventory().getAmountOf(33740) > 60 ? 60 : player.getInventory().getAmountOf(33740), PortableType.isPortableObject(object.getId()));
                            return;
                        }

                        LeatherCrafting.performPortableAction(player, object);
                    } else if (option == OPTION_2)
                        CraftingRs3Dialogue.sendGlassblowingInterface(player, PortableType.isPortableObject(object.getId()));
                }
            });
        }

        @Override
        public void handleObjectClick2(Player player, WorldObject object) {
            GemCutting.performPortableAction(player, object);
        }

        @Override
        public void handleObjectClick3(Player player, WorldObject object) {
            // TODO Auto-generated method stub

        }

        @Override
        public void handleObjectClick4(Player player, WorldObject object) {
            HideTanning.tanHides(player, PortableType.isPortableObject(object.getId()));
        }

    },

    FLETCHER(35227, 106598) {
        @Override
        public void handleObjectClick1(Player player, WorldObject object) {
            if (player.getInventory().containsItem(new Item(34528))) {
                player.getDialogueManager().startDialogue(ProteanFletchingD.class.getSimpleName(), player.getInventory().getAmountOf(34528) > 60 ? 60 : player.getInventory().getAmountOf(34528), PortableType.isPortableObject(object.getId()));
            } else if (player.getInventory().containsItem(new Item(CMLFletching.LOG_ID))) {
                player.getDialogueManager().startDialogue("CMLFletchingD", true);
            } else {
                Fletching.performPortableAction(player, object, 1);
            }
        }

        @Override
        public void handleObjectClick2(Player player, WorldObject object) {
            Fletching.performPortableAction(player, object, 2);
        }

        @Override
        public void handleObjectClick3(Player player, WorldObject object) {
            Fletching.performPortableAction(player, object, 3);
        }

        @Override
        public void handleObjectClick4(Player player, WorldObject object) {
            // TODO Auto-generated method stub

        }

    };
    public static final ImmutableMap<Integer, PortableType> ITEM_MAP;
    public static final ImmutableMap<Integer, PortableType> OBJECT_MAP;

    static {
        val itemBuilder = ImmutableMap.<Integer, PortableType>builder();
        val objectBuilder = ImmutableMap.<Integer, PortableType>builder();
        for (PortableType type : values()) {
            itemBuilder.put(type.itemId, type);
            objectBuilder.put(type.objectId, type);
        }
        ITEM_MAP = itemBuilder.build();
        OBJECT_MAP = objectBuilder.build();
    }

    private final int itemId;
    private final int objectId;

    /**
     * Constructs a new class.
     */
    PortableType(int itemId, int objectId) {
        this.itemId = itemId;
        this.objectId = objectId;
    }

    public static PortableType getPortable(int itemId) {
        return ITEM_MAP.get(itemId);
    }

    public static PortableType getPortableObject(int objectId) {
        return OBJECT_MAP.get(objectId);
    }

    public static boolean isPortableItem(int itemId) {
        return ITEM_MAP.containsKey(itemId);
    }

    public static boolean isPortableObject(int objectId) {
        return OBJECT_MAP.containsKey(objectId);
    }

    /**
     * Gets the itemId.
     *
     * @return the itemId
     */
    public int getItemId() {
        return itemId;
    }

    /**
     * Gets the objectId.
     *
     * @return the objectId
     */
    public int getObjectId() {
        return objectId;
    }

}
