package com.rs.game.player.actions.fletching;

import java.util.HashMap;
import java.util.Map;

import com.rs.cache.loaders.rs3.RS3ClientScriptMap;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.fletching.defs.Fletchables;
import com.rs.game.player.content.RS3SkillsDialogue;
import com.rs.game.player.content.RS3SkillsDialogue.SkillDialogueResult;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.DropDownMenuEvent;

public final class FletchingRs3Dialogue {

    private static final String RS3_FLETCHING_CONTEXT_KEY = "FletchingRs3Context";
    private static final String RS3_FLETCHING_PRODUCTS_KEY = "FletchingRs3Products";
    private static final String RS3_FLETCHING_ACTION_PRODUCTS_KEY = "FletchingRs3ActionProducts";
    private static final String RS3_FLETCHING_CATEGORY_INDEX_KEY = "FletchingRs3CategoryIndex";
    private static final String RS3_FLETCHING_PORTABLE_KEY = "FletchingRs3Portable";

    private static final int RS3_FLETCHING_MAX_QUANTITY = 60;

    private enum FletchingContext {
        CUT_LOGS(6939, 6940, new FletchingCategory[] {
                new FletchingCategory(0, 6947),
                new FletchingCategory(2, 6949),
                new FletchingCategory(3, 6950),
                new FletchingCategory(5, 6952),
                new FletchingCategory(6, 6953),
                new FletchingCategory(7, 6954),
                new FletchingCategory(8, 6955),
                new FletchingCategory(10, 7994),
                new FletchingCategory(11, 6957)
        }),
        ASSEMBLE(6941, 6942, new FletchingCategory[] {
                new FletchingCategory(0, 6958),
                new FletchingCategory(1, 6960),
                new FletchingCategory(2, 6959)
        }),
        FEATHER(6943, 6944, new FletchingCategory[] {
                new FletchingCategory(0, 6966),
                new FletchingCategory(1, 6967),
                new FletchingCategory(2, 6968)
        }),
        TIP(6945, 6946, new FletchingCategory[] {
                new FletchingCategory(0, 6963),
                new FletchingCategory(2, 6962),
                new FletchingCategory(3, 6969),
                new FletchingCategory(4, 6964)
        });

        private final int menuMap;
        private final int namesMap;
        private final FletchingCategory[] categories;

        FletchingContext(int menuMap, int namesMap, FletchingCategory[] categories) {
            this.menuMap = menuMap;
            this.namesMap = namesMap;
            this.categories = categories;
        }
    }

    private static final class FletchingCategory {
        private final int dropdownSlot;
        private final int mapId;

        private FletchingCategory(int dropdownSlot, int mapId) {
            this.dropdownSlot = dropdownSlot;
            this.mapId = mapId;
        }
    }

    private FletchingRs3Dialogue() {

    }

    public static boolean hasRs3FletchingProducts(Player player) {
        return player.getTemporaryAttributtes().get(RS3_FLETCHING_PRODUCTS_KEY) != null;
    }

    public static boolean handleRs3ProductSelection(Player player, int slotId) {
        int[] products = (int[]) player.getTemporaryAttributtes().get(RS3_FLETCHING_PRODUCTS_KEY);
        if (products == null)
            return false;
        int index = (slotId - 1) / 4;
        if (index < 0 || index >= products.length)
            index = slotId;
        if (index < 0 || index >= products.length)
            return false;
        if (!setRs3Product(player, products[index]))
            player.getPackets().sendGameMessage("That item is not wired into fletching yet.");
        return true;
    }

    public static boolean handleRs3CategorySelection(final Player player, int slotId) {
        final FletchingContext context = getContext(player);
        if (context == null)
            return false;
        if (slotId == 65535) {
            player.sendDropDownMenuEvent(new DropDownMenuEvent() {

                @Override
                public void run(Player player) {
                    selectCategoryByDropdownSlot(player, context, getSlotId());
                }
            });
            return true;
        }
        return selectCategoryByDropdownSlot(player, context, slotId);
    }

    public static boolean sendFletchingInterface(Player player, Fletchables preferredFletchable, boolean portable) {
        int preferredAction = getBestActionKey(player, preferredFletchable);
        FletchingContext context = preferredAction == -1 ? getContextForFletchable(preferredFletchable)
                : getContextForAction(preferredAction);
        if (context == null) {
            player.getPackets().sendGameMessage("That item is not wired into the RS3 fletching interface yet.");
            return false;
        }
        sendInterface(player, context, preferredAction, getCategoryIndexForAction(context, preferredAction), portable);
        return true;
    }

    private static void sendInterface(final Player player, final FletchingContext context, final int initialAction,
            final int preferredCategoryIndex, final boolean portable) {
        player.getTemporaryAttributtes().put(RS3_FLETCHING_CONTEXT_KEY, context);
        player.getTemporaryAttributtes().put(RS3_FLETCHING_PORTABLE_KEY, portable);
        player.getDialogueManager().startDialogue(new Dialogue() {

            @Override
            public void start() {
                int displayProduct = prepareProductList(player, context, initialAction, preferredCategoryIndex);
                if (displayProduct == -1) {
                    player.getPackets().sendGameMessage("There are no fletching products available.");
                    end();
                    return;
                }
                Integer categoryIndex = (Integer) player.getTemporaryAttributtes().get(RS3_FLETCHING_CATEGORY_INDEX_KEY);
                RS3SkillsDialogue.sendCustomSkillDialogueWithoutProduct(player, context.menuMap, context.namesMap,
                        context.categories[categoryIndex == null ? 0 : categoryIndex].mapId);
                setRs3Product(player, displayProduct);
                sendTitle(player);
            }

            @Override
            public void run(int interfaceId, int componentId) {
                SkillDialogueResult result = RS3SkillsDialogue.getResult(player,
                        componentId == RS3SkillsDialogue.CONTINUE_OPTION);
                if (componentId != RS3SkillsDialogue.CONTINUE_OPTION)
                    return;
                int actionKey = getActionKey(player, result.getProduce());
                Boolean selectedPortable = (Boolean) player.getTemporaryAttributtes().get(RS3_FLETCHING_PORTABLE_KEY);
                end();
                startFletching(player, actionKey, result.getQuantity(), selectedPortable != null && selectedPortable);
            }

            @Override
            public void finish() {
                player.getTemporaryAttributtes().remove(RS3_FLETCHING_CONTEXT_KEY);
                player.getTemporaryAttributtes().remove(RS3_FLETCHING_PRODUCTS_KEY);
                player.getTemporaryAttributtes().remove(RS3_FLETCHING_ACTION_PRODUCTS_KEY);
                player.getTemporaryAttributtes().remove(RS3_FLETCHING_CATEGORY_INDEX_KEY);
                player.getTemporaryAttributtes().remove(RS3_FLETCHING_PORTABLE_KEY);
            }
        });
    }

    private static boolean selectCategoryByDropdownSlot(Player player, FletchingContext context, int slotId) {
        for (int index = 0; index < context.categories.length; index++) {
            if (context.categories[index].dropdownSlot != slotId)
                continue;
            int displayProduct = prepareCategory(player, context, -1, index);
            if (displayProduct == -1) {
                player.getPackets().sendGameMessage("That fletching category is not wired yet.");
                return true;
            }
            player.getVarBitManager().sendVar(RS3SkillsDialogue.CATEGORY_VAR, context.categories[index].mapId);
            setRs3Product(player, displayProduct);
            sendTitle(player);
            return true;
        }

        player.getPackets().sendGameMessage("That fletching category is not wired yet.");
        return true;
    }

    private static int prepareProductList(Player player, FletchingContext context, int actionKey, int preferredCategoryIndex) {
        int displayProduct = prepareCategory(player, context, actionKey, preferredCategoryIndex);
        if (displayProduct != -1)
            return displayProduct;
        for (int categoryIndex = 0; categoryIndex < context.categories.length; categoryIndex++) {
            if (categoryIndex == preferredCategoryIndex)
                continue;
            displayProduct = prepareCategory(player, context, actionKey, categoryIndex);
            if (displayProduct != -1)
                return displayProduct;
        }
        if (actionKey != -1) {
            for (int categoryIndex = 0; categoryIndex < context.categories.length; categoryIndex++) {
                displayProduct = prepareCategory(player, context, -1, categoryIndex);
                if (displayProduct != -1)
                    return displayProduct;
            }
        }
        return -1;
    }

    private static int prepareCategory(Player player, FletchingContext context, int actionKey, int categoryIndex) {
        if (categoryIndex < 0 || categoryIndex >= context.categories.length)
            return -1;
        int[] products = getProductsFromCategory(context.categories[categoryIndex].mapId);
        Map<Integer, Integer> actionProducts = new HashMap<Integer, Integer>();
        for (int displayProduct : products) {
            int mappedAction = getActionKeyForDisplay(context, displayProduct);
            if (mappedAction != -1)
                actionProducts.put(displayProduct, mappedAction);
        }
        if (actionProducts.isEmpty())
            return -1;
        player.getTemporaryAttributtes().put(RS3_FLETCHING_PRODUCTS_KEY, products);
        player.getTemporaryAttributtes().put(RS3_FLETCHING_ACTION_PRODUCTS_KEY, actionProducts);
        player.getTemporaryAttributtes().put(RS3_FLETCHING_CATEGORY_INDEX_KEY, categoryIndex);
        if (actionKey != -1) {
            int displayProduct = getDisplayProductForAction(actionProducts, actionKey);
            if (displayProduct != -1)
                return displayProduct;
        }
        for (int product : products)
            if (actionProducts.containsKey(product))
                return product;
        return -1;
    }

    private static int[] getProductsFromCategory(int categoryMapId) {
        RS3ClientScriptMap map = RS3ClientScriptMap.getMap(categoryMapId);
        int[] products = new int[map.getSize()];
        for (int index = 0; index < products.length; index++)
            products[index] = map.getIntValue(index);
        return products;
    }

    @SuppressWarnings("unchecked")
    private static int getActionKey(Player player, int displayProduct) {
        Map<Integer, Integer> actionProducts = (Map<Integer, Integer>) player.getTemporaryAttributtes()
                .get(RS3_FLETCHING_ACTION_PRODUCTS_KEY);
        if (actionProducts == null)
            return -1;
        Integer actionKey = actionProducts.get(displayProduct);
        return actionKey == null ? -1 : actionKey;
    }

    private static int getDisplayProductForAction(Map<Integer, Integer> actionProducts, int actionKey) {
        for (Map.Entry<Integer, Integer> entry : actionProducts.entrySet())
            if (entry.getValue() == actionKey)
                return entry.getKey();
        return -1;
    }

    private static boolean setRs3Product(Player player, int displayProduct) {
        FletchingContext context = getContext(player);
        int actionKey = getActionKey(player, displayProduct);
        if (context == null || actionKey == -1)
            return false;
        int maxQuantity = Math.min(RS3_FLETCHING_MAX_QUANTITY, getMaxQuantity(player, actionKey));
        RS3SkillsDialogue.forceSetProduct(player, displayProduct, maxQuantity);
        refreshProduct(player, displayProduct, actionKey);
        return true;
    }

    private static void refreshProduct(final Player player, final int displayProduct, final int actionKey) {
        WorldTasksManager.schedule(new WorldTask() {
            private int refreshes;

            @Override
            public void run() {
                if (!hasRs3FletchingProducts(player)
                        || player.getVarBitManager().getValue(RS3SkillsDialogue.PRODUCT_VAR) != displayProduct) {
                    stop();
                    return;
                }
                RS3SkillsDialogue.forceSetProduct(player, displayProduct,
                        Math.min(RS3_FLETCHING_MAX_QUANTITY, getMaxQuantity(player, actionKey)));
                if (++refreshes >= 2)
                    stop();
            }
        }, 1, 1);
    }

    private static int getActionKeyForDisplay(FletchingContext context, int displayProduct) {
        for (Fletchables fletchable : Fletchables.values()) {
            for (int option = 0; option < fletchable.getProduct().length; option++) {
                if (getContextFor(fletchable, option) == context && getDisplayProduct(fletchable, option) == displayProduct)
                    return encodeActionKey(fletchable, option);
            }
        }
        return -1;
    }

    private static int getBestActionKey(Player player, Fletchables fletchable) {
        if (fletchable == null)
            return -1;
        int fallback = -1;
        int bestAction = -1;
        int bestLevel = -1;
        for (int option = 0; option < fletchable.getProduct().length; option++) {
            FletchingContext context = getContextFor(fletchable, option);
            if (context == null)
                continue;
            int actionKey = encodeActionKey(fletchable, option);
            if (fallback == -1)
                fallback = actionKey;
            int level = fletchable.getLevel()[option];
            if (player.getSkills().getLevel(Skills.FLETCHING) >= level && getMaxQuantity(player, actionKey) > 0
                    && level >= bestLevel) {
                bestAction = actionKey;
                bestLevel = level;
            }
        }
        return bestAction == -1 ? fallback : bestAction;
    }

    private static FletchingContext getContextForFletchable(Fletchables fletchable) {
        if (fletchable == null)
            return null;
        for (int option = 0; option < fletchable.getProduct().length; option++) {
            FletchingContext context = getContextFor(fletchable, option);
            if (context != null)
                return context;
        }
        return null;
    }

    private static FletchingContext getContextForAction(int actionKey) {
        Fletchables fletchable = getFletchable(actionKey);
        int option = getOption(actionKey);
        return fletchable == null || option == -1 ? null : getContextFor(fletchable, option);
    }

    private static FletchingContext getContextFor(Fletchables fletchable, int option) {
        if (option < 0 || option >= fletchable.getProduct().length)
            return null;
        int displayProduct = getDisplayProduct(fletchable, option);
        for (FletchingContext context : FletchingContext.values())
            for (FletchingCategory category : context.categories)
                if (contains(getProductsFromCategory(category.mapId), displayProduct))
                    return context;
        return null;
    }

    private static int getCategoryIndexForAction(FletchingContext context, int actionKey) {
        if (context == null || actionKey == -1)
            return 0;
        Fletchables fletchable = getFletchable(actionKey);
        int option = getOption(actionKey);
        if (fletchable == null || option == -1)
            return 0;
        int displayProduct = getDisplayProduct(fletchable, option);
        for (int index = 0; index < context.categories.length; index++)
            if (contains(getProductsFromCategory(context.categories[index].mapId), displayProduct))
                return index;
        return 0;
    }

    private static int getDisplayProduct(Fletchables fletchable, int option) {
        int product = fletchable.getProduct()[option];
        if (product == 52 && isLog(fletchable.getId())) {
            switch (fletchable.getId()) {
                case 1521:
                    return 34672;
                case 1519:
                    return 34673;
                case 1517:
                    return 34674;
                case 1515:
                    return 34675;
                case 1513:
                    return 34676;
                case 29556:
                    return 34677;
                default:
                    return 52;
            }
        }
        return product;
    }

    private static int getMaxQuantity(Player player, int actionKey) {
        Fletchables fletchable = getFletchable(actionKey);
        int option = getOption(actionKey);
        if (fletchable == null || option == -1)
            return 0;
        if (player.getSkills().getLevel(Skills.FLETCHING) < fletchable.getLevel()[option])
            return 0;
        if (Fletching.requiresTool(fletchable) && !player.getInventory().containsOneItem(fletchable.getSelected()))
            return 0;
        int max = Integer.MAX_VALUE;
        int primaryRequired = Fletching.getPrimaryAmountRequired(fletchable, option);
        if (primaryRequired > 0)
            max = Math.min(max, player.getInventory().getAmountOf(fletchable.getId()) / primaryRequired);
        int secondaryRequired = Fletching.getSecondaryAmountRequired(player, fletchable, option);
        if (secondaryRequired > 0)
            max = Math.min(max, player.getInventory().getAmountOf(fletchable.getSelected()) / secondaryRequired);
        return max == Integer.MAX_VALUE ? 0 : max;
    }

    private static void startFletching(Player player, int actionKey, int quantity, boolean portable) {
        Fletchables fletchable = getFletchable(actionKey);
        int option = getOption(actionKey);
        if (fletchable == null || option == -1) {
            player.getPackets().sendGameMessage("That item is not wired into fletching yet.");
            return;
        }
        int max = getMaxQuantity(player, actionKey);
        if (max <= 0) {
            player.getPackets().sendGameMessage("You do not have the requirements to fletch that.");
            return;
        }
        player.getActionManager().setAction(new Fletching(fletchable, option,
                Math.max(1, Math.min(Math.min(quantity, max), RS3_FLETCHING_MAX_QUANTITY)), portable));
    }

    private static int encodeActionKey(Fletchables fletchable, int option) {
        return fletchable.ordinal() * 10 + option;
    }

    private static Fletchables getFletchable(int actionKey) {
        if (actionKey < 0)
            return null;
        int ordinal = actionKey / 10;
        Fletchables[] values = Fletchables.values();
        return ordinal < 0 || ordinal >= values.length ? null : values[ordinal];
    }

    private static int getOption(int actionKey) {
        Fletchables fletchable = getFletchable(actionKey);
        if (fletchable == null)
            return -1;
        int option = actionKey % 10;
        return option < 0 || option >= fletchable.getProduct().length ? -1 : option;
    }

    private static void sendTitle(Player player) {
        player.getPackets().sendGlobalString(2390, "Fletching");
    }

    private static FletchingContext getContext(Player player) {
        Object context = player.getTemporaryAttributtes().get(RS3_FLETCHING_CONTEXT_KEY);
        return context instanceof FletchingContext ? (FletchingContext) context : null;
    }

    private static boolean isLog(int itemId) {
        return itemId == 1511 || itemId == 1521 || itemId == 1519 || itemId == 1517 || itemId == 1515
                || itemId == 1513 || itemId == 29556;
    }

    private static boolean contains(int[] values, int value) {
        for (int candidate : values)
            if (candidate == value)
                return true;
        return false;
    }
}
