package com.rs.game.player.actions.crafting;

import java.util.HashMap;
import java.util.Map;

import com.rs.cache.loaders.ItemDefinitions;
import com.rs.cache.loaders.rs3.RS3ClientScriptMap;
import com.rs.game.Animation;
import com.rs.game.WorldObject;
import com.rs.game.activites.halloween.ClueScrollDistributor;
import com.rs.game.item.Item;
import com.rs.game.player.PerkManager.DonationPerk;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.Action;
import com.rs.game.player.actions.crafting.GemCutting.Gem;
import com.rs.game.player.actions.crafting.LeatherCrafting.LeatherData;
import com.rs.game.player.actions.crafting.SilverCrafting.SmeltingBar;
import com.rs.game.player.actions.crafting.Spinning.SpinningItem;
import com.rs.game.player.actions.fletching.BoltTipFletching;
import com.rs.game.player.actions.fletching.defs.BoltTips;
import com.rs.game.player.content.RS3SkillsDialogue;
import com.rs.game.player.content.RS3SkillsDialogue.SkillDialogueResult;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Colors;
import com.rs.utils.DropDownMenuEvent;
import com.rs.utils.Utils;

public final class CraftingRs3Dialogue {

    private static final String RS3_CRAFTING_CONTEXT_KEY = "CraftingRs3Context";
    private static final String RS3_CRAFTING_PRODUCTS_KEY = "CraftingRs3Products";
    private static final String RS3_CRAFTING_ACTION_PRODUCTS_KEY = "CraftingRs3ActionProducts";
    private static final String RS3_CRAFTING_CATEGORY_INDEX_KEY = "CraftingRs3CategoryIndex";
    private static final String RS3_CRAFTING_OBJECT_KEY = "CraftingRs3Object";
    private static final String RS3_CRAFTING_PORTABLE_KEY = "CraftingRs3Portable";

    private static final int RS3_CRAFTING_SHELL_MENU_MAP = 15093;
    private static final int RS3_CRAFTING_SHELL_NAMES_MAP = 15092;
    private static final int RS3_CRAFTING_MAX_QUANTITY = 60;
    private static final int BOLT_TIPS_ACTION_BASE = -10000;

    private static final int[] LEATHER_CATEGORY_MAPS = {
            6991, 13219, 7002, 7001, 7000, 7003, 6992, 6993, 6994, 6995, 6996, 6997
    };
    private static final int[] POTTERY_WHEEL_PRODUCTS = { 1787, 1789, 1791, 5352 };
    private static final int[] POTTERY_WHEEL_LEVELS = { 1, 7, 8, 19 };
    private static final double[] POTTERY_WHEEL_XP = { 6.3, 15, 18, 20 };
    private static final int SOFT_CLAY = 1761;

    private static final int[] POTTERY_FURNACE_INGREDIENTS = { 1787, 1789, 1791, 5352 };
    private static final int[] POTTERY_FURNACE_PRODUCTS = { 1931, 2313, 1923, 5350 };
    private static final int[] POTTERY_FURNACE_LEVELS = { 1, 7, 8, 19 };
    private static final double[] POTTERY_FURNACE_XP = { 6.3, 10, 15, 17.5 };

    private static final int[] GLASS_PRODUCTS = { 1919, 4527, 4525, 229, 6667, 567, 4542, 10973, 23191, 32843 };
    private static final int[] GLASS_DISPLAY_PRODUCTS = { 1919, 4527, 4525, 229, 6667, 567, 4542, 10973, 23191, 32843 };
    private static final int[] GLASS_LEVELS = { 1, 4, 12, 33, 42, 45, 49, 87, 89, 89 };
    private static final double[] GLASS_XP = { 17.5, 19, 25, 35, 42.5, 52.5, 55, 70, 100, 150 };

    private enum CraftingContext {
        GEM_CUTTING("Gem Cutting", 6981, 6982, new int[] { 6983 }),
        BOLT_TIPS("Bolt Tips", 6981, 6982, new int[] { 6961 }),
        LEATHER("Leather Crafting", 6987, 6988, LEATHER_CATEGORY_MAPS),
        SPINNING("Spinning", RS3_CRAFTING_SHELL_MENU_MAP, RS3_CRAFTING_SHELL_NAMES_MAP, new int[] { 7046 }),
        GLASSBLOWING("Glassblowing", RS3_CRAFTING_SHELL_MENU_MAP, RS3_CRAFTING_SHELL_NAMES_MAP, new int[] { 6977 }),
        POTTERY_WHEEL("Pottery", 7004, 7005, new int[] { 7014 }),
        POTTERY_FURNACE("Pottery", 7006, 7007, new int[] { 7015 }),
        GOLD_JEWELLERY("Gold Jewellery", RS3_CRAFTING_SHELL_MENU_MAP, RS3_CRAFTING_SHELL_NAMES_MAP, new int[] { 6985 }),
        SILVER_CRAFTING("Silver Crafting", RS3_CRAFTING_SHELL_MENU_MAP, RS3_CRAFTING_SHELL_NAMES_MAP, new int[] { 6984 });

        private final String title;
        private final int menuMap;
        private final int namesMap;
        private final int[] categoryMaps;

        CraftingContext(String title, int menuMap, int namesMap, int[] categoryMaps) {
            this.title = title;
            this.menuMap = menuMap;
            this.namesMap = namesMap;
            this.categoryMaps = categoryMaps;
        }
    }

    private CraftingRs3Dialogue() {

    }

    public static boolean hasRs3CraftingProducts(Player player) {
        return player.getTemporaryAttributtes().get(RS3_CRAFTING_PRODUCTS_KEY) != null;
    }

    public static boolean handleRs3ProductSelection(Player player, int slotId) {
        int[] products = (int[]) player.getTemporaryAttributtes().get(RS3_CRAFTING_PRODUCTS_KEY);
        if (products == null)
            return false;
        int index = (slotId - 1) / 4;
        if (index < 0 || index >= products.length)
            index = slotId;
        if (index < 0 || index >= products.length)
            return false;
        if (!setRs3Product(player, products[index]))
            player.getPackets().sendGameMessage("That item is not wired into crafting yet.");
        return true;
    }

    public static boolean handleRs3CategorySelection(Player player, int slotId) {
        CraftingContext context = getContext(player);
        if (context == null)
            return false;
        if (context == CraftingContext.GEM_CUTTING || context == CraftingContext.BOLT_TIPS)
            return handleGemCuttingCategorySelection(player, context, slotId);
        if (context.categoryMaps.length <= 1)
            return true;
        Integer currentIndex = (Integer) player.getTemporaryAttributtes().get(RS3_CRAFTING_CATEGORY_INDEX_KEY);
        int nextIndex = currentIndex == null ? 0 : (currentIndex + 1) % context.categoryMaps.length;
        int displayProduct = prepareCategory(player, context, -1, nextIndex);
        if (displayProduct == -1)
            return false;
        player.getVarBitManager().sendVar(RS3SkillsDialogue.CATEGORY_VAR, context.categoryMaps[nextIndex]);
        setRs3Product(player, displayProduct);
        sendTitle(player, context);
        return true;
    }

    public static void sendGemCuttingInterface(Player player, Gem gem, boolean portable) {
        int initialProduct = gem == null ? getBestProduct(player, CraftingContext.GEM_CUTTING) : gem.getCut();
        sendInterface(player, CraftingContext.GEM_CUTTING, initialProduct, 0, null, portable);
    }

    public static void sendBoltTipsInterface(Player player, BoltTips tips) {
        int initialProduct = tips == null ? getBestProduct(player, CraftingContext.BOLT_TIPS) : getBoltTipsActionProduct(tips);
        sendInterface(player, CraftingContext.BOLT_TIPS, initialProduct, 0, null, false);
    }

    public static boolean sendLeatherInterface(Player player, int leatherType, boolean portable) {
        int categoryIndex = getLeatherCategoryIndex(leatherType);
        if (categoryIndex == -1)
            return false;
        int initialProduct = getBestLeatherProduct(player, leatherType);
        sendInterface(player, CraftingContext.LEATHER, initialProduct, categoryIndex, null, portable);
        return true;
    }

    public static void sendSpinningInterface(Player player) {
        sendInterface(player, CraftingContext.SPINNING, getBestProduct(player, CraftingContext.SPINNING), 0, null, false);
    }

    public static void sendGlassblowingInterface(Player player) {
        sendGlassblowingInterface(player, false);
    }

    public static void sendGlassblowingInterface(Player player, boolean portable) {
        sendInterface(player, CraftingContext.GLASSBLOWING, getBestProduct(player, CraftingContext.GLASSBLOWING), 0, null, portable);
    }

    public static void sendPotteryWheelInterface(Player player) {
        sendInterface(player, CraftingContext.POTTERY_WHEEL, getBestProduct(player, CraftingContext.POTTERY_WHEEL), 0, null, false);
    }

    public static void sendPotteryFurnaceInterface(Player player) {
        sendInterface(player, CraftingContext.POTTERY_FURNACE, getBestProduct(player, CraftingContext.POTTERY_FURNACE), 0, null, false);
    }

    public static void sendGoldJewelleryInterface(Player player) {
        sendInterface(player, CraftingContext.GOLD_JEWELLERY, getBestProduct(player, CraftingContext.GOLD_JEWELLERY), 0, null, false);
    }

    public static void sendSilverCraftingInterface(Player player, WorldObject object) {
        sendInterface(player, CraftingContext.SILVER_CRAFTING, getBestProduct(player, CraftingContext.SILVER_CRAFTING), 0, object, false);
    }

    private static void sendInterface(final Player player, final CraftingContext context, final int initialProduct,
            final int preferredCategoryIndex, final WorldObject object, final boolean portable) {
        player.getTemporaryAttributtes().put(RS3_CRAFTING_CONTEXT_KEY, context);
        if (object == null)
            player.getTemporaryAttributtes().remove(RS3_CRAFTING_OBJECT_KEY);
        else
            player.getTemporaryAttributtes().put(RS3_CRAFTING_OBJECT_KEY, object);
        player.getTemporaryAttributtes().put(RS3_CRAFTING_PORTABLE_KEY, portable);
        player.getDialogueManager().startDialogue(new Dialogue() {

            @Override
            public void start() {
                int displayProduct = prepareProductList(player, context, initialProduct, preferredCategoryIndex);
                if (displayProduct == -1) {
                    player.getPackets().sendGameMessage("There are no crafting products available.");
                    end();
                    return;
                }
                Integer categoryIndex = (Integer) player.getTemporaryAttributtes().get(RS3_CRAFTING_CATEGORY_INDEX_KEY);
                RS3SkillsDialogue.sendCustomSkillDialogueWithoutProduct(player, context.menuMap, context.namesMap,
                        context.categoryMaps[categoryIndex == null ? 0 : categoryIndex]);
                setRs3Product(player, displayProduct);
                sendTitle(player, context);
            }

            @Override
            public void run(int interfaceId, int componentId) {
                SkillDialogueResult result = RS3SkillsDialogue.getResult(player, componentId == RS3SkillsDialogue.CONTINUE_OPTION);
                if (componentId != RS3SkillsDialogue.CONTINUE_OPTION)
                    return;
                int productId = getActionProduct(player, result.getProduce());
                CraftingContext selectedContext = getContext(player);
                WorldObject selectedObject = (WorldObject) player.getTemporaryAttributtes().get(RS3_CRAFTING_OBJECT_KEY);
                Boolean selectedPortable = (Boolean) player.getTemporaryAttributtes().get(RS3_CRAFTING_PORTABLE_KEY);
                end();
                startCrafting(player, selectedContext, productId, result.getQuantity(), selectedObject,
                        selectedPortable != null && selectedPortable);
            }

            @Override
            public void finish() {
                player.getTemporaryAttributtes().remove(RS3_CRAFTING_CONTEXT_KEY);
                player.getTemporaryAttributtes().remove(RS3_CRAFTING_PRODUCTS_KEY);
                player.getTemporaryAttributtes().remove(RS3_CRAFTING_ACTION_PRODUCTS_KEY);
                player.getTemporaryAttributtes().remove(RS3_CRAFTING_CATEGORY_INDEX_KEY);
                player.getTemporaryAttributtes().remove(RS3_CRAFTING_OBJECT_KEY);
                player.getTemporaryAttributtes().remove(RS3_CRAFTING_PORTABLE_KEY);
            }
        });
    }

    private static int prepareProductList(Player player, CraftingContext context, int actionProduct, int preferredCategoryIndex) {
        int displayProduct = prepareCategory(player, context, actionProduct, preferredCategoryIndex);
        if (displayProduct != -1)
            return displayProduct;
        for (int categoryIndex = 0; categoryIndex < context.categoryMaps.length; categoryIndex++) {
            if (categoryIndex == preferredCategoryIndex)
                continue;
            displayProduct = prepareCategory(player, context, actionProduct, categoryIndex);
            if (displayProduct != -1)
                return displayProduct;
        }
        if (actionProduct != -1) {
            for (int categoryIndex = 0; categoryIndex < context.categoryMaps.length; categoryIndex++) {
                displayProduct = prepareCategory(player, context, -1, categoryIndex);
                if (displayProduct != -1)
                    return displayProduct;
            }
        }
        return -1;
    }

    private static int prepareCategory(Player player, CraftingContext context, int actionProduct, int categoryIndex) {
        if (categoryIndex < 0 || categoryIndex >= context.categoryMaps.length)
            return -1;
        int[] products = getProductsFromCategory(context.categoryMaps[categoryIndex]);
        Map<Integer, Integer> actionProducts = new HashMap<Integer, Integer>();
        for (int displayProduct : products) {
            int mappedProduct = getActionProductForDisplay(context, displayProduct);
            if (mappedProduct != -1)
                actionProducts.put(displayProduct, mappedProduct);
        }
        if (actionProducts.isEmpty())
            return -1;
        player.getTemporaryAttributtes().put(RS3_CRAFTING_PRODUCTS_KEY, products);
        player.getTemporaryAttributtes().put(RS3_CRAFTING_ACTION_PRODUCTS_KEY, actionProducts);
        player.getTemporaryAttributtes().put(RS3_CRAFTING_CATEGORY_INDEX_KEY, categoryIndex);
        if (actionProduct != -1) {
            int displayProduct = getDisplayProductForAction(actionProducts, actionProduct);
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

    private static int getActionProductForDisplay(CraftingContext context, int displayProduct) {
        switch (context) {
            case GEM_CUTTING:
                return getGemByCutProduct(displayProduct) == null ? -1 : displayProduct;
            case BOLT_TIPS:
                return getBoltTipsActionProductForDisplay(displayProduct);
            case LEATHER:
                return LeatherData.forId(displayProduct) == null ? -1 : displayProduct;
            case SPINNING:
                return hasSpinningProduct(displayProduct) ? displayProduct : -1;
            case GLASSBLOWING:
                return getGlassActionProduct(displayProduct);
            case POTTERY_WHEEL:
                return contains(POTTERY_WHEEL_PRODUCTS, displayProduct) ? displayProduct : -1;
            case POTTERY_FURNACE:
                return contains(POTTERY_FURNACE_PRODUCTS, displayProduct) ? displayProduct : -1;
            case GOLD_JEWELLERY:
                return JewellerySmithing.isProduct(displayProduct) ? displayProduct : -1;
            case SILVER_CRAFTING:
                return getSilverBarByProduct(displayProduct) == null ? -1 : displayProduct;
            default:
                return -1;
        }
    }

    @SuppressWarnings("unchecked")
    private static int getActionProduct(Player player, int displayProduct) {
        Map<Integer, Integer> actionProducts = (Map<Integer, Integer>) player.getTemporaryAttributtes().get(RS3_CRAFTING_ACTION_PRODUCTS_KEY);
        if (actionProducts == null)
            return displayProduct;
        Integer actionProduct = actionProducts.get(displayProduct);
        return actionProduct == null ? -1 : actionProduct;
    }

    private static int getDisplayProductForAction(Map<Integer, Integer> actionProducts, int actionProduct) {
        for (Map.Entry<Integer, Integer> entry : actionProducts.entrySet())
            if (entry.getValue() == actionProduct)
                return entry.getKey();
        return -1;
    }

    private static boolean setRs3Product(Player player, int displayProduct) {
        CraftingContext context = getContext(player);
        int actionProduct = getActionProduct(player, displayProduct);
        if (context == null || actionProduct == -1)
            return false;
        int maxQuantity = Math.max(1, Math.min(RS3_CRAFTING_MAX_QUANTITY, getMaxQuantity(player, context, actionProduct)));
        RS3SkillsDialogue.forceSetProduct(player, displayProduct, maxQuantity);
        refreshProduct(player, context, displayProduct, actionProduct);
        return true;
    }

    private static void refreshProduct(final Player player, final CraftingContext context, final int displayProduct, final int actionProduct) {
        WorldTasksManager.schedule(new WorldTask() {
            private int refreshes;

            @Override
            public void run() {
                if (!hasRs3CraftingProducts(player)
                        || player.getVarBitManager().getValue(RS3SkillsDialogue.PRODUCT_VAR) != displayProduct) {
                    stop();
                    return;
                }
                int maxQuantity = Math.max(1, Math.min(RS3_CRAFTING_MAX_QUANTITY, getMaxQuantity(player, context, actionProduct)));
                RS3SkillsDialogue.forceSetProduct(player, displayProduct, maxQuantity);
                if (++refreshes >= 2)
                    stop();
            }
        }, 1, 1);
    }

    private static int getBestProduct(Player player, CraftingContext context) {
        int fallback = -1;
        int bestProduct = -1;
        int bestLevel = -1;
        for (int categoryMap : context.categoryMaps) {
            for (int displayProduct : getProductsFromCategory(categoryMap)) {
                int actionProduct = getActionProductForDisplay(context, displayProduct);
                if (actionProduct == -1)
                    continue;
                if (fallback == -1)
                    fallback = actionProduct;
                int level = getLevel(context, actionProduct);
                if (player.getSkills().getLevel(getSkill(context)) >= level
                        && getMaxQuantity(player, context, actionProduct) > 0 && level >= bestLevel) {
                    bestProduct = actionProduct;
                    bestLevel = level;
                }
            }
        }
        return bestProduct == -1 ? fallback : bestProduct;
    }

    private static int getBestLeatherProduct(Player player, int leatherType) {
        int bestProduct = -1;
        int bestLevel = -1;
        for (LeatherData data : LeatherData.values()) {
            if (data.getLeatherId() != leatherType)
                continue;
            if (bestProduct == -1)
                bestProduct = data.getFinalProduct();
            if (player.getSkills().getLevel(Skills.CRAFTING) >= data.getRequiredLevel()
                    && player.getInventory().getItems().getNumberOf(data.getLeatherId()) >= data.getLeatherAmount()
                    && data.getRequiredLevel() >= bestLevel) {
                bestProduct = data.getFinalProduct();
                bestLevel = data.getRequiredLevel();
            }
        }
        return bestProduct;
    }

    private static int getMaxQuantity(Player player, CraftingContext context, int productId) {
        switch (context) {
            case GEM_CUTTING:
                Gem gem = getGemByCutProduct(productId);
                return gem == null ? 0 : player.getInventory().getAmountOf(gem.getUncut());
            case BOLT_TIPS:
                BoltTips tips = getBoltTipsByActionProduct(productId);
                return tips == null ? 0 : player.getInventory().getAmountOf(tips.getGemId());
            case LEATHER:
                LeatherData data = LeatherData.forId(productId);
                return data == null ? 0 : player.getInventory().getAmountOf(data.getLeatherId()) / data.getLeatherAmount();
            case SPINNING:
                return getSpinningIngredientCount(player, productId);
            case GLASSBLOWING:
                return player.getInventory().getAmountOf(getGlassMaterial(productId));
            case POTTERY_WHEEL:
                return player.getInventory().getAmountOf(SOFT_CLAY);
            case POTTERY_FURNACE:
                int potteryIndex = indexOf(POTTERY_FURNACE_PRODUCTS, productId);
                return potteryIndex == -1 ? 0 : player.getInventory().getAmountOf(POTTERY_FURNACE_INGREDIENTS[potteryIndex]);
            case GOLD_JEWELLERY:
                return JewellerySmithing.getMaxQuantity(player, productId);
            case SILVER_CRAFTING:
                SmeltingBar bar = getSilverBarByProduct(productId);
                return bar == null ? 0 : getMaxForItems(player, bar.getItemsRequired());
            default:
                return 0;
        }
    }

    private static int getLevel(CraftingContext context, int productId) {
        switch (context) {
            case GEM_CUTTING:
                Gem gem = getGemByCutProduct(productId);
                return gem == null ? 1 : gem.getLevelRequired();
            case BOLT_TIPS:
                BoltTips tips = getBoltTipsByActionProduct(productId);
                return tips == null ? 1 : tips.getLevelRequired();
            case LEATHER:
                LeatherData data = LeatherData.forId(productId);
                return data == null ? 1 : data.getRequiredLevel();
            case SPINNING:
                SpinningItem item = getFirstSpinningItem(productId);
                return item == null ? 1 : item.getSkillRequirement();
            case GLASSBLOWING:
                int glassIndex = indexOf(GLASS_PRODUCTS, productId);
                return glassIndex == -1 ? 1 : GLASS_LEVELS[glassIndex];
            case POTTERY_WHEEL:
                int wheelIndex = indexOf(POTTERY_WHEEL_PRODUCTS, productId);
                return wheelIndex == -1 ? 1 : POTTERY_WHEEL_LEVELS[wheelIndex];
            case POTTERY_FURNACE:
                int furnaceIndex = indexOf(POTTERY_FURNACE_PRODUCTS, productId);
                return furnaceIndex == -1 ? 1 : POTTERY_FURNACE_LEVELS[furnaceIndex];
            case GOLD_JEWELLERY:
                return JewellerySmithing.getLevel(productId);
            case SILVER_CRAFTING:
                SmeltingBar bar = getSilverBarByProduct(productId);
                return bar == null ? 1 : bar.getLevelRequired();
            default:
                return 1;
        }
    }

    private static void startCrafting(Player player, CraftingContext context, int productId, int quantity,
            WorldObject object, boolean portable) {
        if (context == null || productId == -1) {
            player.getPackets().sendGameMessage("That item is not wired into crafting yet.");
            return;
        }
        quantity = Math.max(1, Math.min(RS3_CRAFTING_MAX_QUANTITY, quantity));
        switch (context) {
            case GEM_CUTTING:
                Gem gem = getGemByCutProduct(productId);
                if (gem == null) {
                    player.getPackets().sendGameMessage("That item is not wired into gem cutting yet.");
                    return;
                }
                player.getActionManager().setAction(new GemCutting(gem, quantity, portable));
                return;
            case BOLT_TIPS:
                BoltTips tips = getBoltTipsByActionProduct(productId);
                if (tips == null) {
                    player.getPackets().sendGameMessage("That item is not wired into bolt tip cutting yet.");
                    return;
                }
                int maxBoltTips = player.getInventory().getAmountOf(tips.getGemId());
                player.getActionManager().setAction(new BoltTipFletching(tips, Math.min(quantity, maxBoltTips)));
                return;
            case LEATHER:
                LeatherData data = LeatherData.forId(productId);
                if (data == null) {
                    player.getPackets().sendGameMessage("That item is not wired into leather crafting yet.");
                    return;
                }
                int max = player.getInventory().getAmountOf(data.getLeatherId()) / data.getLeatherAmount();
                player.getActionManager().setAction(new LeatherCrafting(data, Math.min(quantity, max), portable));
                return;
            case SPINNING:
                player.getActionManager().setAction(new SpinningAction(productId, quantity));
                return;
            case GLASSBLOWING:
                player.getActionManager().setAction(new GlassblowingAction(productId, quantity, portable));
                return;
            case POTTERY_WHEEL:
                player.getActionManager().setAction(new PotteryWheelAction(productId, quantity));
                return;
            case POTTERY_FURNACE:
                player.getActionManager().setAction(new PotteryFurnaceAction(productId, quantity));
                return;
            case GOLD_JEWELLERY:
                JewellerySmithing.startProduct(player, productId, quantity);
                return;
            case SILVER_CRAFTING:
                SmeltingBar bar = getSilverBarByProduct(productId);
                if (bar == null || object == null) {
                    player.getPackets().sendGameMessage("That item is not wired into silver crafting yet.");
                    return;
                }
                player.getActionManager().setAction(new SilverCrafting(bar.getButtonId(), object, quantity));
                return;
            default:
                player.getPackets().sendGameMessage("That item is not wired into crafting yet.");
        }
    }

    private static void sendTitle(Player player, CraftingContext context) {
        player.getPackets().sendGlobalString(2390, context.title);
    }

    private static boolean handleGemCuttingCategorySelection(final Player player, CraftingContext currentContext, int slotId) {
        if (slotId == 65535) {
            player.sendDropDownMenuEvent(new DropDownMenuEvent() {

                @Override
                public void run(Player player) {
                    switchGemCuttingCategory(player, getContext(player), getSlotId());
                }
            });
            return true;
        }
        return switchGemCuttingCategory(player, currentContext, slotId);
    }

    private static boolean switchGemCuttingCategory(Player player, CraftingContext currentContext, int slotId) {
        if (currentContext == null)
            return false;
        CraftingContext nextContext;
        if (slotId == 0)
            nextContext = CraftingContext.GEM_CUTTING;
        else if (slotId == 1)
            nextContext = CraftingContext.BOLT_TIPS;
        else
            nextContext = currentContext == CraftingContext.GEM_CUTTING
                    ? CraftingContext.BOLT_TIPS : CraftingContext.GEM_CUTTING;
        int displayProduct = prepareProductList(player, nextContext, getBestProduct(player, nextContext), 0);
        if (displayProduct == -1)
            return false;
        player.getTemporaryAttributtes().put(RS3_CRAFTING_CONTEXT_KEY, nextContext);
        player.getVarBitManager().sendVar(RS3SkillsDialogue.CATEGORY_VAR, nextContext.categoryMaps[0]);
        setRs3Product(player, displayProduct);
        sendTitle(player, nextContext);
        return true;
    }

    private static CraftingContext getContext(Player player) {
        Object context = player.getTemporaryAttributtes().get(RS3_CRAFTING_CONTEXT_KEY);
        return context instanceof CraftingContext ? (CraftingContext) context : null;
    }

    private static int getSkill(CraftingContext context) {
        return context == CraftingContext.BOLT_TIPS ? Skills.FLETCHING : Skills.CRAFTING;
    }

    private static int getLeatherCategoryIndex(int leatherType) {
        switch (leatherType) {
            case 1741:
                return 0;
            case LeatherCrafting.STRIP_OF_CLOTH:
                return 1;
            case 25545:
                return 2;
            case 25547:
                return 3;
            case 25549:
                return 4;
            case 25551:
                return 5;
            case 6289:
                return 6;
            case 1745:
                return 7;
            case 2505:
                return 8;
            case 2507:
                return 9;
            case 2509:
                return 10;
            case 24374:
                return 11;
            default:
                return -1;
        }
    }

    private static Gem getGemByCutProduct(int productId) {
        for (Gem gem : Gem.values())
            if (gem.getCut() == productId)
                return gem;
        return null;
    }

    private static int getBoltTipsActionProductForDisplay(int displayProduct) {
        if (displayProduct == 25480)
            return getBoltTipsActionProduct(BoltTips.PEARLS);
        for (BoltTips tips : BoltTips.values())
            if (tips.gettipId() == displayProduct)
                return getBoltTipsActionProduct(tips);
        return -1;
    }

    private static int getBoltTipsActionProduct(BoltTips tips) {
        return BOLT_TIPS_ACTION_BASE - tips.ordinal();
    }

    private static BoltTips getBoltTipsByActionProduct(int actionProduct) {
        int index = BOLT_TIPS_ACTION_BASE - actionProduct;
        BoltTips[] tips = BoltTips.values();
        return index < 0 || index >= tips.length ? null : tips[index];
    }

    private static boolean hasSpinningProduct(int productId) {
        return getFirstSpinningItem(productId) != null;
    }

    private static SpinningItem getFirstSpinningItem(int productId) {
        for (SpinningItem item : SpinningItem.values())
            if (item.getAfterId() == productId)
                return item;
        return null;
    }

    private static int getSpinningIngredientCount(Player player, int productId) {
        int count = 0;
        for (SpinningItem item : SpinningItem.values()) {
            if (item.getAfterId() != productId)
                continue;
            for (int beforeId : item.getBeforeId())
                count += player.getInventory().getAmountOf(beforeId);
        }
        return count;
    }

    private static int getAvailableSpinningIngredient(Player player, int productId) {
        for (SpinningItem item : SpinningItem.values()) {
            if (item.getAfterId() != productId)
                continue;
            for (int beforeId : item.getBeforeId())
                if (player.getInventory().containsItem(beforeId, 1))
                    return beforeId;
        }
        return -1;
    }

    private static int getGlassActionProduct(int displayProduct) {
        int displayIndex = indexOf(GLASS_DISPLAY_PRODUCTS, displayProduct);
        return displayIndex == -1 ? -1 : GLASS_PRODUCTS[displayIndex];
    }

    private static int getGlassMaterial(int productId) {
        if (productId == 23191)
            return 23193;
        if (productId == 32843)
            return 32845;
        return 1775;
    }

    private static SmeltingBar getSilverBarByProduct(int productId) {
        for (SmeltingBar bar : SmeltingBar.values())
            if (bar.getProducedBar().getId() == productId)
                return bar;
        return null;
    }

    private static int getMaxForItems(Player player, Item[] items) {
        int max = Integer.MAX_VALUE;
        for (Item item : items) {
            int count = player.getInventory().getAmountOf(item.getId()) / item.getAmount();
            if (count < max)
                max = count;
        }
        return max == Integer.MAX_VALUE ? 0 : max;
    }

    private static boolean contains(int[] values, int value) {
        return indexOf(values, value) != -1;
    }

    private static int indexOf(int[] values, int value) {
        for (int index = 0; index < values.length; index++)
            if (values[index] == value)
                return index;
        return -1;
    }

    private static final class SpinningAction extends Action {
        private final int productId;
        private int quantity;

        private SpinningAction(int productId, int quantity) {
            this.productId = productId;
            this.quantity = quantity;
        }

        @Override
        public boolean start(Player player) {
            return check(player);
        }

        @Override
        public boolean process(Player player) {
            return quantity > 0 && check(player);
        }

        private boolean check(Player player) {
            SpinningItem item = getFirstSpinningItem(productId);
            if (item == null)
                return false;
            if (player.getSkills().getLevel(Skills.CRAFTING) < item.getSkillRequirement()) {
                player.getPackets().sendGameMessage("You need a Crafting level of " + item.getSkillRequirement()
                        + " to craft this.");
                return false;
            }
            int ingredient = getAvailableSpinningIngredient(player, productId);
            if (ingredient == -1) {
                player.getPackets().sendGameMessage("You need a piece of "
                        + ItemDefinitions.getItemDefinitions(item.getBeforeId()[0]).getName().toLowerCase()
                        + " in order to make a "
                        + ItemDefinitions.getItemDefinitions(productId).getName().toLowerCase() + ".");
                return false;
            }
            return true;
        }

        @Override
        public int processWithDelay(Player player) {
            SpinningItem item = getFirstSpinningItem(productId);
            int ingredient = getAvailableSpinningIngredient(player, productId);
            if (item == null || ingredient == -1)
                return -1;
            quantity--;
            player.setNextAnimation(new Animation(896));
            player.getSkills().addXp(Skills.CRAFTING,
                    item.getExp() * (player.getPerkManager().hasPerkActive(DonationPerk.DELICATE_CRAFTSMAN) ? 1.25 : 1));
            player.getInventory().deleteItem(new Item(ingredient, 1));
            player.getInventory().addItem(new Item(productId, 1));
            return quantity > 0 ? 3 : -1;
        }

        @Override
        public void stop(Player player) {
            setActionDelay(player, 3);
        }
    }

    private static final class GlassblowingAction extends Action {
        private final int productId;
        private final boolean portable;
        private int quantity;

        private GlassblowingAction(int productId, int quantity, boolean portable) {
            this.productId = productId;
            this.quantity = quantity;
            this.portable = portable;
        }

        @Override
        public boolean start(Player player) {
            return check(player);
        }

        @Override
        public boolean process(Player player) {
            return quantity > 0 && check(player);
        }

        private boolean check(Player player) {
            int glassIndex = indexOf(GLASS_PRODUCTS, productId);
            if (glassIndex == -1)
                return false;
            int levelReq = GLASS_LEVELS[glassIndex];
            if (player.getSkills().getLevel(Skills.CRAFTING) < levelReq) {
                player.getPackets().sendGameMessage("You need a Crafting level of " + levelReq + " to create this.");
                return false;
            }
            if (!player.getInventory().containsItem(1785, 1)) {
                player.getPackets().sendGameMessage("You need a glassblowing pipe in order to create glass items.");
                return false;
            }
            int material = getGlassMaterial(productId);
            if (!player.getInventory().containsItem(material, 1)) {
                player.getPackets().sendGameMessage("You have no glass to work with.");
                return false;
            }
            return true;
        }

        @Override
        public int processWithDelay(Player player) {
            int glassIndex = indexOf(GLASS_PRODUCTS, productId);
            if (glassIndex == -1)
                return -1;
            quantity--;
            player.getInventory().deleteItem(new Item(getGlassMaterial(productId), 1));
            player.getInventory().addItem(new Item(productId, 1));
            player.getSkills().addXp(Skills.CRAFTING, GLASS_XP[glassIndex] * (portable ? 1.1 : 1));
            player.addItemsMade();
            player.sendMessage("You form a figure out of the glass; items crafted: " + Colors.RED
                    + Utils.getFormattedNumber(player.getItemsMade()) + "</col>.", true);
            player.setNextAnimation(new Animation(productId == 23191 || productId == 32843 ? 24890 : 884));
            return quantity > 0 ? 2 : -1;
        }

        @Override
        public void stop(Player player) {
            setActionDelay(player, 3);
        }
    }

    private static final class PotteryWheelAction extends Action {
        private final int productId;
        private int quantity;

        private PotteryWheelAction(int productId, int quantity) {
            this.productId = productId;
            this.quantity = quantity;
        }

        @Override
        public boolean start(Player player) {
            return check(player);
        }

        @Override
        public boolean process(Player player) {
            return quantity > 0 && check(player);
        }

        private boolean check(Player player) {
            int index = indexOf(POTTERY_WHEEL_PRODUCTS, productId);
            if (index == -1)
                return false;
            if (!player.getInventory().containsItem(SOFT_CLAY, 1)) {
                player.getPackets().sendGameMessage("You need soft clay in order to spin any pot.");
                return false;
            }
            if (player.getSkills().getLevel(Skills.CRAFTING) < POTTERY_WHEEL_LEVELS[index]) {
                player.getPackets().sendGameMessage("You need a crafting level of " + POTTERY_WHEEL_LEVELS[index]
                        + " in order to spin this.");
                return false;
            }
            return true;
        }

        @Override
        public int processWithDelay(Player player) {
            int index = indexOf(POTTERY_WHEEL_PRODUCTS, productId);
            if (index == -1)
                return -1;
            quantity--;
            player.setNextAnimation(new Animation(896));
            player.getSkills().addXp(Skills.CRAFTING, POTTERY_WHEEL_XP[index]);
            player.getInventory().deleteItem(SOFT_CLAY, 1);
            player.getInventory().addItem(productId, 1);
            return quantity > 0 ? 4 : -1;
        }

        @Override
        public void stop(Player player) {
            setActionDelay(player, 3);
        }
    }

    private static final class PotteryFurnaceAction extends Action {
        private final int productId;
        private int quantity;

        private PotteryFurnaceAction(int productId, int quantity) {
            this.productId = productId;
            this.quantity = quantity;
        }

        @Override
        public boolean start(Player player) {
            return check(player);
        }

        @Override
        public boolean process(Player player) {
            return quantity > 0 && check(player);
        }

        private boolean check(Player player) {
            int index = indexOf(POTTERY_FURNACE_PRODUCTS, productId);
            if (index == -1)
                return false;
            int ingredient = POTTERY_FURNACE_INGREDIENTS[index];
            if (!player.getInventory().containsItem(ingredient, 1)) {
                player.getPackets().sendGameMessage("You need a "
                        + ItemDefinitions.getItemDefinitions(ingredient).getName().toLowerCase()
                        + " in order to use the furnace.");
                return false;
            }
            if (player.getSkills().getLevel(Skills.CRAFTING) < POTTERY_FURNACE_LEVELS[index]) {
                player.getPackets().sendGameMessage("You need a crafting level of " + POTTERY_FURNACE_LEVELS[index]
                        + " in order to spin this.");
                return false;
            }
            return true;
        }

        @Override
        public int processWithDelay(Player player) {
            int index = indexOf(POTTERY_FURNACE_PRODUCTS, productId);
            if (index == -1)
                return -1;
            quantity--;
            player.setNextAnimation(new Animation(32626));
            player.getSkills().addXp(Skills.CRAFTING, POTTERY_FURNACE_XP[index]);
            player.getInventory().deleteItem(POTTERY_FURNACE_INGREDIENTS[index], 1);
            player.getInventory().addItem(productId, 1);
            ClueScrollDistributor.givePlayerClueScrollIfProbable(player, ClueScrollDistributor.SKILLING_PERCENT);
            return quantity > 0 ? 4 : -1;
        }

        @Override
        public void stop(Player player) {
            setActionDelay(player, 3);
        }
    }
}
