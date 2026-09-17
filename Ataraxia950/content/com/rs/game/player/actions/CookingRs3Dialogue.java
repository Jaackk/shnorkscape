package com.rs.game.player.actions;

import java.util.HashMap;
import java.util.Map;

import com.rs.cache.loaders.rs3.RS3ClientScriptMap;
import com.rs.game.WorldObject;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.Cooking.Cookables;
import com.rs.game.player.content.RS3SkillsDialogue;
import com.rs.game.player.content.RS3SkillsDialogue.SkillDialogueResult;
import com.rs.game.player.content.packs.portable.PortableStation;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.DropDownMenuEvent;

public final class CookingRs3Dialogue {

    private static final String RS3_COOKING_PRODUCTS_KEY = "CookingRs3Products";
    private static final String RS3_COOKING_ACTION_PRODUCTS_KEY = "CookingRs3ActionProducts";
    private static final String RS3_COOKING_CATEGORY_INDEX_KEY = "CookingRs3CategoryIndex";
    private static final String RS3_COOKING_OBJECT_KEY = "CookingRs3Object";
    private static final String RS3_COOKING_PORTABLE_KEY = "CookingRs3Portable";

    private static final int RS3_COOKING_MENU_MAP = 6809;
    private static final int RS3_COOKING_NAMES_MAP = 6810;
    private static final int RS3_COOKING_MAX_QUANTITY = 60;

    private static final int[] COOKING_CATEGORY_MAPS = {
            6811, 6797, 6812, 6813, 11460, 6814, 7555, 6815
    };

    private CookingRs3Dialogue() {

    }

    public static boolean hasRs3CookingProducts(Player player) {
        return player.getTemporaryAttributtes().get(RS3_COOKING_PRODUCTS_KEY) != null;
    }

    public static boolean sendCookingInterface(Player player, WorldObject object, Cookables preferredCookable) {
        if (object == null)
            return false;
        int preferredCategoryIndex = preferredCookable == null ? -1
                : getCategoryIndexForProduct(preferredCookable.getProduct().getId());
        if (preferredCookable != null && preferredCategoryIndex == -1)
            return false;
        if (preferredCategoryIndex == -1)
            preferredCategoryIndex = getBestCategoryIndex(player, object);
        if (preferredCategoryIndex == -1)
            return false;
        sendInterface(player, object, preferredCookable, preferredCategoryIndex,
                PortableStation.isPortableObject(object));
        return true;
    }

    public static boolean handleRs3ProductSelection(Player player, int slotId) {
        int[] products = (int[]) player.getTemporaryAttributtes().get(RS3_COOKING_PRODUCTS_KEY);
        if (products == null)
            return false;
        int index = (slotId - 1) / 4;
        if (index < 0 || index >= products.length)
            index = slotId;
        if (index < 0 || index >= products.length)
            return false;
        if (!setRs3Product(player, products[index]))
            player.getPackets().sendGameMessage("That item is not wired into cooking yet.");
        return true;
    }

    public static boolean handleRs3CategorySelection(final Player player, int slotId) {
        if (!hasRs3CookingProducts(player))
            return false;
        if (slotId == 65535) {
            player.sendDropDownMenuEvent(new DropDownMenuEvent() {

                @Override
                public void run(Player player) {
                    selectCategory(player, getSlotId());
                }
            });
            return true;
        }
        return selectCategory(player, slotId);
    }

    private static void sendInterface(final Player player, final WorldObject object,
            final Cookables preferredCookable, final int preferredCategoryIndex, final boolean portable) {
        player.getTemporaryAttributtes().put(RS3_COOKING_OBJECT_KEY, object);
        player.getTemporaryAttributtes().put(RS3_COOKING_PORTABLE_KEY, portable);
        player.getDialogueManager().startDialogue(new Dialogue() {

            @Override
            public void start() {
                int displayProduct = prepareProductList(player, object, preferredCookable, preferredCategoryIndex);
                if (displayProduct == -1) {
                    player.getPackets().sendGameMessage("There are no cooking products available.");
                    end();
                    return;
                }
                Integer categoryIndex = (Integer) player.getTemporaryAttributtes()
                        .get(RS3_COOKING_CATEGORY_INDEX_KEY);
                RS3SkillsDialogue.sendCustomSkillDialogueWithoutProduct(player, RS3_COOKING_MENU_MAP,
                        RS3_COOKING_NAMES_MAP, COOKING_CATEGORY_MAPS[categoryIndex == null ? 0 : categoryIndex]);
                setRs3Product(player, displayProduct);
                sendTitle(player);
            }

            @Override
            public void run(int interfaceId, int componentId) {
                SkillDialogueResult result = RS3SkillsDialogue.getResult(player,
                        componentId == RS3SkillsDialogue.CONTINUE_OPTION);
                if (componentId != RS3SkillsDialogue.CONTINUE_OPTION)
                    return;
                Cookables cookable = getActionCookable(player, result.getProduce());
                WorldObject selectedObject = (WorldObject) player.getTemporaryAttributtes()
                        .get(RS3_COOKING_OBJECT_KEY);
                Boolean selectedPortable = (Boolean) player.getTemporaryAttributtes()
                        .get(RS3_COOKING_PORTABLE_KEY);
                end();
                startCooking(player, selectedObject, cookable, result.getQuantity(),
                        selectedPortable != null && selectedPortable);
            }

            @Override
            public void finish() {
                player.getTemporaryAttributtes().remove(RS3_COOKING_PRODUCTS_KEY);
                player.getTemporaryAttributtes().remove(RS3_COOKING_ACTION_PRODUCTS_KEY);
                player.getTemporaryAttributtes().remove(RS3_COOKING_CATEGORY_INDEX_KEY);
                player.getTemporaryAttributtes().remove(RS3_COOKING_OBJECT_KEY);
                player.getTemporaryAttributtes().remove(RS3_COOKING_PORTABLE_KEY);
            }
        });
    }

    private static boolean selectCategory(Player player, int slotId) {
        if (slotId < 0 || slotId >= COOKING_CATEGORY_MAPS.length)
            return true;
        WorldObject object = (WorldObject) player.getTemporaryAttributtes().get(RS3_COOKING_OBJECT_KEY);
        int displayProduct = prepareCategory(player, object, null, slotId);
        if (displayProduct == -1) {
            player.getPackets().sendGameMessage("That cooking category is not wired yet.");
            return true;
        }
        player.getVarBitManager().sendVar(RS3SkillsDialogue.CATEGORY_VAR, COOKING_CATEGORY_MAPS[slotId]);
        setRs3Product(player, displayProduct);
        sendTitle(player);
        return true;
    }

    private static int prepareProductList(Player player, WorldObject object, Cookables preferredCookable,
            int preferredCategoryIndex) {
        int displayProduct = prepareCategory(player, object, preferredCookable, preferredCategoryIndex);
        if (displayProduct != -1)
            return displayProduct;
        for (int categoryIndex = 0; categoryIndex < COOKING_CATEGORY_MAPS.length; categoryIndex++) {
            if (categoryIndex == preferredCategoryIndex)
                continue;
            displayProduct = prepareCategory(player, object, null, categoryIndex);
            if (displayProduct != -1)
                return displayProduct;
        }
        return -1;
    }

    private static int prepareCategory(Player player, WorldObject object, Cookables preferredCookable,
            int categoryIndex) {
        if (categoryIndex < 0 || categoryIndex >= COOKING_CATEGORY_MAPS.length)
            return -1;
        int[] products = getProductsFromCategory(COOKING_CATEGORY_MAPS[categoryIndex]);
        Map<Integer, Cookables> actionProducts = new HashMap<Integer, Cookables>();
        for (int displayProduct : products) {
            Cookables cookable = getCookableForDisplayProduct(player, object, displayProduct, preferredCookable);
            if (cookable != null)
                actionProducts.put(displayProduct, cookable);
        }
        if (actionProducts.isEmpty())
            return -1;
        player.getTemporaryAttributtes().put(RS3_COOKING_PRODUCTS_KEY, products);
        player.getTemporaryAttributtes().put(RS3_COOKING_ACTION_PRODUCTS_KEY, actionProducts);
        player.getTemporaryAttributtes().put(RS3_COOKING_CATEGORY_INDEX_KEY, categoryIndex);
        if (preferredCookable != null) {
            for (int product : products) {
                Cookables cookable = actionProducts.get(product);
                if (cookable == preferredCookable)
                    return product;
            }
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
    private static Cookables getActionCookable(Player player, int displayProduct) {
        Map<Integer, Cookables> actionProducts = (Map<Integer, Cookables>) player.getTemporaryAttributtes()
                .get(RS3_COOKING_ACTION_PRODUCTS_KEY);
        if (actionProducts == null)
            return null;
        return actionProducts.get(displayProduct);
    }

    private static boolean setRs3Product(Player player, int displayProduct) {
        Cookables cookable = getActionCookable(player, displayProduct);
        if (cookable == null)
            return false;
        int maxQuantity = Math.min(RS3_COOKING_MAX_QUANTITY, getMaxQuantity(player, cookable,
                (WorldObject) player.getTemporaryAttributtes().get(RS3_COOKING_OBJECT_KEY)));
        RS3SkillsDialogue.forceSetProduct(player, displayProduct, maxQuantity);
        refreshProduct(player, displayProduct, cookable);
        return true;
    }

    private static void refreshProduct(final Player player, final int displayProduct, final Cookables cookable) {
        WorldTasksManager.schedule(new WorldTask() {
            private int refreshes;

            @Override
            public void run() {
                if (!hasRs3CookingProducts(player)
                        || player.getVarBitManager().getValue(RS3SkillsDialogue.PRODUCT_VAR) != displayProduct) {
                    stop();
                    return;
                }
                int maxQuantity = Math.min(RS3_COOKING_MAX_QUANTITY, getMaxQuantity(player, cookable,
                        (WorldObject) player.getTemporaryAttributtes().get(RS3_COOKING_OBJECT_KEY)));
                RS3SkillsDialogue.forceSetProduct(player, displayProduct, maxQuantity);
                if (++refreshes >= 2)
                    stop();
            }
        }, 1, 1);
    }

    private static void startCooking(Player player, WorldObject object, Cookables cookable, int quantity,
            boolean portable) {
        if (cookable == null || object == null) {
            player.getPackets().sendGameMessage("That item is not wired into cooking yet.");
            return;
        }
        int max = getMaxQuantity(player, cookable, object);
        if (max <= 0) {
            player.getPackets().sendGameMessage("You do not have the requirements to cook that.");
            return;
        }
        int amount = Math.max(1, Math.min(Math.min(quantity, max), RS3_COOKING_MAX_QUANTITY));
        player.getActionManager().setAction(new Cooking(object, cookable.getRawItem(), portable, amount));
    }

    private static Cookables getCookableForDisplayProduct(Player player, WorldObject object, int displayProduct,
            Cookables preferredCookable) {
        if (preferredCookable != null && preferredCookable.getProduct().getId() == displayProduct
                && isValidStation(preferredCookable, object))
            return preferredCookable;
        Cookables fallback = null;
        Cookables bestAvailable = null;
        int bestLevel = -1;
        for (Cookables cookable : Cookables.values()) {
            if (cookable.getProduct().getId() != displayProduct || !isValidStation(cookable, object))
                continue;
            if (fallback == null)
                fallback = cookable;
            int level = cookable.getLvl();
            if (getMaxQuantity(player, cookable, object) > 0 && level >= bestLevel) {
                bestAvailable = cookable;
                bestLevel = level;
            }
        }
        return bestAvailable == null ? fallback : bestAvailable;
    }

    private static int getCategoryIndexForProduct(int productId) {
        for (int categoryIndex = 0; categoryIndex < COOKING_CATEGORY_MAPS.length; categoryIndex++)
            for (int product : getProductsFromCategory(COOKING_CATEGORY_MAPS[categoryIndex]))
                if (product == productId)
                    return categoryIndex;
        return -1;
    }

    private static int getBestCategoryIndex(Player player, WorldObject object) {
        int bestCategory = -1;
        int bestLevel = -1;
        for (int categoryIndex = 0; categoryIndex < COOKING_CATEGORY_MAPS.length; categoryIndex++) {
            for (int product : getProductsFromCategory(COOKING_CATEGORY_MAPS[categoryIndex])) {
                Cookables cookable = getCookableForDisplayProduct(player, object, product, null);
                if (cookable == null)
                    continue;
                int level = cookable.getLvl();
                if (getMaxQuantity(player, cookable, object) > 0 && level >= bestLevel) {
                    bestCategory = categoryIndex;
                    bestLevel = level;
                }
            }
        }
        return bestCategory;
    }

    private static int getMaxQuantity(Player player, Cookables cookable, WorldObject object) {
        if (cookable == null || !isValidStation(cookable, object))
            return 0;
        if (player.getSkills().getLevel(Skills.COOKING) < cookable.getLvl())
            return 0;
        if (cookable.getRawItem().getId() == 15272 && player.getPorts().spice == 0)
            return 0;
        return player.getInventory().getAmountOf(cookable.getRawItem().getId());
    }

    private static boolean isValidStation(Cookables cookable, WorldObject object) {
        if (cookable == null || object == null)
            return false;
        if (object.getId() == 81736 && !cookable.isCookingStation())
            return false;
        String name = object.getDefinitions().getName();
        if (cookable.isFireOnly() && !"Fire".equals(name))
            return false;
        if (cookable.isSpitRoast() && object.getId() != 11363)
            return false;
        return !cookable.isCookingStation() || object.getId() == 81736;
    }

    private static void sendTitle(Player player) {
        player.getPackets().sendGlobalString(2390, "Cooking");
    }
}
