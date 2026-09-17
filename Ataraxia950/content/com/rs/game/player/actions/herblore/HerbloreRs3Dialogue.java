package com.rs.game.player.actions.herblore;

import java.util.HashMap;
import java.util.Map;

import com.rs.cache.loaders.rs3.RS3ClientScriptMap;
import com.rs.game.activites.halloween.ClueScrollDistributor;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.Action;
import com.rs.game.player.actions.herblore.CrystalFlask.CrystalPot;
import com.rs.game.player.actions.herblore.HerbCleaning.Herbs;
import com.rs.game.player.actions.herblore.Herblore.Ingredients;
import com.rs.game.player.content.RS3SkillsDialogue;
import com.rs.game.player.content.RS3SkillsDialogue.SkillDialogueResult;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.DropDownMenuEvent;

public final class HerbloreRs3Dialogue {

    private static final String RS3_HERBLORE_PRODUCTS_KEY = "HerbloreRs3Products";
    private static final String RS3_HERBLORE_ACTION_PRODUCTS_KEY = "HerbloreRs3ActionProducts";
    private static final String RS3_HERBLORE_CATEGORY_INDEX_KEY = "HerbloreRs3CategoryIndex";
    private static final String RS3_HERBLORE_PORTABLE_KEY = "HerbloreRs3Portable";

    private static final int RS3_HERBLORE_MENU_MAP = 6838;
    private static final int RS3_HERBLORE_NAMES_MAP = 6839;
    private static final int RS3_HERBLORE_MAX_QUANTITY = 28;

    private static final int[] HERBLORE_CATEGORY_MAPS = {
            6841, 6842, 6843, 6844, 6845, 6846, 6847, 9470
    };

    private static final int[] OVERLOAD_INGREDIENTS = {
            15309, 15313, 15317, 15321, 15325
    };

    private HerbloreRs3Dialogue() {

    }

    public static boolean hasRs3HerbloreProducts(Player player) {
        return player.getTemporaryAttributtes().get(RS3_HERBLORE_PRODUCTS_KEY) != null;
    }

    public static boolean sendHerbloreInterface(Player player, Item first, Item second, boolean portable) {
        Recipe initialRecipe = getRecipeForPair(first.getId(), second.getId());
        if (initialRecipe == null)
            return false;
        int preferredCategoryIndex = getCategoryIndexForProduct(initialRecipe.getProductId());
        if (preferredCategoryIndex == -1)
            return false;
        sendInterface(player, initialRecipe, preferredCategoryIndex, portable);
        return true;
    }

    public static boolean sendCleanHerbInterface(Player player, Herbs herb) {
        if (herb == null)
            return false;
        Recipe initialRecipe = new Recipe(herb);
        int preferredCategoryIndex = getCategoryIndexForProduct(initialRecipe.getProductId());
        if (preferredCategoryIndex == -1)
            return false;
        sendInterface(player, initialRecipe, preferredCategoryIndex, false);
        return true;
    }

    public static boolean sendCombinationPotionInterface(Player player) {
        sendInterface(player, null, 7, false);
        return true;
    }

    public static boolean handleRs3ProductSelection(Player player, int slotId) {
        int[] products = (int[]) player.getTemporaryAttributtes().get(RS3_HERBLORE_PRODUCTS_KEY);
        if (products == null)
            return false;
        int index = (slotId - 1) / 4;
        if (index < 0 || index >= products.length)
            index = slotId;
        if (index < 0 || index >= products.length)
            return false;
        if (!setRs3Product(player, products[index]))
            player.getPackets().sendGameMessage("That item is not wired into herblore yet.");
        return true;
    }

    public static boolean handleRs3CategorySelection(final Player player, int slotId) {
        if (!hasRs3HerbloreProducts(player))
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

    private static void sendInterface(final Player player, final Recipe initialRecipe,
            final int preferredCategoryIndex, final boolean portable) {
        player.getTemporaryAttributtes().put(RS3_HERBLORE_PORTABLE_KEY, portable);
        player.getDialogueManager().startDialogue(new Dialogue() {

            @Override
            public void start() {
                int displayProduct = prepareProductList(player, initialRecipe, preferredCategoryIndex);
                if (displayProduct == -1) {
                    player.getPackets().sendGameMessage("There are no herblore products available.");
                    end();
                    return;
                }
                Integer categoryIndex = (Integer) player.getTemporaryAttributtes().get(RS3_HERBLORE_CATEGORY_INDEX_KEY);
                RS3SkillsDialogue.sendCustomSkillDialogueWithoutProduct(player, RS3_HERBLORE_MENU_MAP,
                        RS3_HERBLORE_NAMES_MAP, HERBLORE_CATEGORY_MAPS[categoryIndex == null ? 0 : categoryIndex]);
                setRs3Product(player, displayProduct);
                sendTitle(player);
            }

            @Override
            public void run(int interfaceId, int componentId) {
                SkillDialogueResult result = RS3SkillsDialogue.getResult(player,
                        componentId == RS3SkillsDialogue.CONTINUE_OPTION);
                if (componentId != RS3SkillsDialogue.CONTINUE_OPTION)
                    return;
                Recipe recipe = getActionRecipe(player, result.getProduce());
                Boolean selectedPortable = (Boolean) player.getTemporaryAttributtes().get(RS3_HERBLORE_PORTABLE_KEY);
                end();
                startHerblore(player, recipe, result.getQuantity(), selectedPortable != null && selectedPortable);
            }

            @Override
            public void finish() {
                player.getTemporaryAttributtes().remove(RS3_HERBLORE_PRODUCTS_KEY);
                player.getTemporaryAttributtes().remove(RS3_HERBLORE_ACTION_PRODUCTS_KEY);
                player.getTemporaryAttributtes().remove(RS3_HERBLORE_CATEGORY_INDEX_KEY);
                player.getTemporaryAttributtes().remove(RS3_HERBLORE_PORTABLE_KEY);
            }
        });
    }

    private static boolean selectCategory(Player player, int slotId) {
        if (slotId < 0 || slotId >= HERBLORE_CATEGORY_MAPS.length)
            return true;
        int displayProduct = prepareCategory(player, null, slotId);
        if (displayProduct == -1) {
            player.getPackets().sendGameMessage("That herblore category is not wired yet.");
            return true;
        }
        player.getVarBitManager().sendVar(RS3SkillsDialogue.CATEGORY_VAR, HERBLORE_CATEGORY_MAPS[slotId]);
        setRs3Product(player, displayProduct);
        sendTitle(player);
        return true;
    }

    private static int prepareProductList(Player player, Recipe initialRecipe, int preferredCategoryIndex) {
        int displayProduct = prepareCategory(player, initialRecipe, preferredCategoryIndex);
        if (displayProduct != -1)
            return displayProduct;
        for (int categoryIndex = 0; categoryIndex < HERBLORE_CATEGORY_MAPS.length; categoryIndex++) {
            if (categoryIndex == preferredCategoryIndex)
                continue;
            displayProduct = prepareCategory(player, null, categoryIndex);
            if (displayProduct != -1)
                return displayProduct;
        }
        return -1;
    }

    private static int prepareCategory(Player player, Recipe preferredRecipe, int categoryIndex) {
        if (categoryIndex < 0 || categoryIndex >= HERBLORE_CATEGORY_MAPS.length)
            return -1;
        int[] products = getProductsFromCategory(HERBLORE_CATEGORY_MAPS[categoryIndex]);
        Map<Integer, Recipe> actionProducts = new HashMap<Integer, Recipe>();
        for (int displayProduct : products) {
            Recipe recipe = getRecipeForDisplayProduct(player, displayProduct, preferredRecipe);
            if (recipe != null)
                actionProducts.put(displayProduct, recipe);
        }
        if (actionProducts.isEmpty())
            return -1;
        player.getTemporaryAttributtes().put(RS3_HERBLORE_PRODUCTS_KEY, products);
        player.getTemporaryAttributtes().put(RS3_HERBLORE_ACTION_PRODUCTS_KEY, actionProducts);
        player.getTemporaryAttributtes().put(RS3_HERBLORE_CATEGORY_INDEX_KEY, categoryIndex);
        if (preferredRecipe != null) {
            for (int product : products)
                if (product == preferredRecipe.getProductId() && actionProducts.containsKey(product))
                    return product;
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
    private static Recipe getActionRecipe(Player player, int displayProduct) {
        Map<Integer, Recipe> actionProducts = (Map<Integer, Recipe>) player.getTemporaryAttributtes()
                .get(RS3_HERBLORE_ACTION_PRODUCTS_KEY);
        if (actionProducts == null)
            return null;
        return actionProducts.get(displayProduct);
    }

    private static boolean setRs3Product(Player player, int displayProduct) {
        Recipe recipe = getActionRecipe(player, displayProduct);
        if (recipe == null)
            return false;
        int maxQuantity = Math.min(RS3_HERBLORE_MAX_QUANTITY, getMaxQuantity(player, recipe));
        RS3SkillsDialogue.forceSetProduct(player, displayProduct, maxQuantity);
        refreshProduct(player, displayProduct, recipe);
        return true;
    }

    private static void refreshProduct(final Player player, final int displayProduct, final Recipe recipe) {
        WorldTasksManager.schedule(new WorldTask() {
            private int refreshes;

            @Override
            public void run() {
                if (!hasRs3HerbloreProducts(player)
                        || player.getVarBitManager().getValue(RS3SkillsDialogue.PRODUCT_VAR) != displayProduct) {
                    stop();
                    return;
                }
                RS3SkillsDialogue.forceSetProduct(player, displayProduct,
                        Math.min(RS3_HERBLORE_MAX_QUANTITY, getMaxQuantity(player, recipe)));
                if (++refreshes >= 2)
                    stop();
            }
        }, 1, 1);
    }

    private static void startHerblore(Player player, Recipe recipe, int quantity, boolean portable) {
        if (recipe == null) {
            player.getPackets().sendGameMessage("That item is not wired into herblore yet.");
            return;
        }
        int max = getMaxQuantity(player, recipe);
        if (max <= 0) {
            player.getPackets().sendGameMessage("You do not have the requirements to mix that.");
            return;
        }
        int amount = Math.max(1, Math.min(Math.min(quantity, max), RS3_HERBLORE_MAX_QUANTITY));
        if (recipe.isCleanHerb()) {
            player.getActionManager().setAction(new CleanHerbAction(recipe.getHerb(), amount));
        } else if (recipe.isCombinationPotion()) {
            player.getActionManager().setAction(new CombinePotions(recipe.getCrystalPotion(), amount));
        } else {
            player.getActionManager().setAction(new Herblore(new Item(recipe.getIngredientId()),
                    new Item(recipe.getOtherItemId()), amount, portable));
        }
    }

    private static Recipe getRecipeForPair(int firstId, int secondId) {
        Ingredients ingredient = Ingredients.forId(firstId);
        int otherId = secondId;
        if (ingredient == null) {
            ingredient = Ingredients.forId(secondId);
            otherId = firstId;
        }
        if (ingredient == null)
            return null;
        int slot = ingredient.getSlot(otherId);
        return slot == -1 ? null : new Recipe(ingredient, slot);
    }

    private static Recipe getRecipeForDisplayProduct(Player player, int displayProduct, Recipe preferredRecipe) {
        Recipe fallback = null;
        Recipe bestAvailable = null;
        int bestLevel = -1;
        Recipe herbRecipe = getCleanHerbRecipe(displayProduct);
        if (herbRecipe != null) {
            if (preferredRecipe != null && preferredRecipe.matches(herbRecipe))
                return preferredRecipe;
            fallback = herbRecipe;
            if (player.getSkills().getLevel(Skills.HERBLORE) >= herbRecipe.getLevel()
                    && getMaxQuantity(player, herbRecipe) > 0) {
                bestAvailable = herbRecipe;
                bestLevel = herbRecipe.getLevel();
            }
        }
        Recipe combinationRecipe = getCombinationRecipe(displayProduct);
        if (combinationRecipe != null) {
            if (preferredRecipe != null && preferredRecipe.matches(combinationRecipe))
                return preferredRecipe;
            if (fallback == null)
                fallback = combinationRecipe;
            if (player.getSkills().getLevel(Skills.HERBLORE) >= combinationRecipe.getLevel()
                    && getMaxQuantity(player, combinationRecipe) > 0 && combinationRecipe.getLevel() >= bestLevel) {
                bestAvailable = combinationRecipe;
                bestLevel = combinationRecipe.getLevel();
            }
        }
        for (Ingredients ingredient : Ingredients.values()) {
            int[] rewards = ingredient.getRewards();
            for (int slot = 0; slot < rewards.length; slot++) {
                if (rewards[slot] != displayProduct)
                    continue;
                Recipe recipe = new Recipe(ingredient, slot);
                if (preferredRecipe != null && preferredRecipe.matches(recipe))
                    return preferredRecipe;
                if (fallback == null)
                    fallback = recipe;
                int level = recipe.getLevel();
                if (player.getSkills().getLevel(Skills.HERBLORE) >= level
                        && getMaxQuantity(player, recipe) > 0 && level >= bestLevel) {
                    bestAvailable = recipe;
                    bestLevel = level;
                }
            }
        }
        return bestAvailable == null ? fallback : bestAvailable;
    }

    private static Recipe getCleanHerbRecipe(int displayProduct) {
        for (Herbs herb : Herbs.values())
            if (herb.getCleanId() == displayProduct)
                return new Recipe(herb);
        return null;
    }

    private static Recipe getCombinationRecipe(int displayProduct) {
        CrystalPot crystalPotion = CrystalPot.getCrystalPotion(displayProduct);
        return crystalPotion == null ? null : new Recipe(crystalPotion);
    }

    private static int getCategoryIndexForProduct(int productId) {
        for (int categoryIndex = 0; categoryIndex < HERBLORE_CATEGORY_MAPS.length; categoryIndex++)
            for (int product : getProductsFromCategory(HERBLORE_CATEGORY_MAPS[categoryIndex]))
                if (product == productId)
                    return categoryIndex;
        return -1;
    }

    private static int getMaxQuantity(Player player, Recipe recipe) {
        if (player.getSkills().getLevel(Skills.HERBLORE) < recipe.getLevel())
            return 0;
        if (recipe.isCleanHerb())
            return player.getInventory().getAmountOf(recipe.getHerb().getHerbId());
        if (recipe.isCombinationPotion()) {
            if (!hasCombinationRecipeUnlocked(player, recipe.getCrystalPotion()))
                return 0;
            int max = player.getInventory().getAmountOf(32843);
            for (Item item : recipe.getCrystalPotion().getRequiredPotion())
                max = Math.min(max, player.getInventory().getAmountOf(item.getId()) / item.getAmount());
            return max;
        }
        if (recipe.isOverload()) {
            int max = player.getInventory().getAmountOf(Ingredients.TORSTOL.getItemId());
            for (int itemId : OVERLOAD_INGREDIENTS)
                max = Math.min(max, player.getInventory().getAmountOf(itemId));
            return max;
        }
        int primaryAmount = getPrimaryAmount(recipe.getIngredientId());
        int primaryMax = player.getInventory().getAmountOf(recipe.getIngredientId()) / primaryAmount;
        int otherMax = player.getInventory().getAmountOf(recipe.getOtherItemId());
        return Math.min(primaryMax, otherMax);
    }

    private static boolean hasCombinationRecipeUnlocked(Player player, CrystalPot crystalPotion) {
        int order = crystalPotion.getOrder();
        if (order >= 0 && order <= 15)
            return player.meilyrShopSettings[order];
        if (order >= 16 && order <= 26)
            return player.meilyrShopSettings2[order - 16];
        return true;
    }

    private static int getPrimaryAmount(int itemId) {
        switch (itemId) {
            case 12539:
                return 5;
            case 35721:
                return 10;
            case 35722:
                return 7;
            case 35723:
                return 5;
            default:
                return 1;
        }
    }

    private static void sendTitle(Player player) {
        player.getPackets().sendGlobalString(2390, "Herblore");
    }

    private static final class CleanHerbAction extends Action {
        private final Herbs herb;
        private int quantity;

        private CleanHerbAction(Herbs herb, int quantity) {
            this.herb = herb;
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
            if (player.getSkills().getLevel(Skills.HERBLORE) < herb.getLevel()) {
                player.sendMessage("You do not have the required level to clean this.", true);
                return false;
            }
            return player.getInventory().containsItem(herb.getHerbId(), 1);
        }

        @Override
        public int processWithDelay(Player player) {
            quantity--;
            player.getInventory().deleteItem(new Item(herb.getHerbId(), 1));
            player.getInventory().addItem(new Item(herb.getCleanId(), 1));
            player.getInventory().refresh();
            player.getSkills().addXp(Skills.HERBLORE, herb.getExperience());
            player.sendMessage("You clean the herb.", true);
            ClueScrollDistributor.givePlayerClueScrollIfProbable(player, ClueScrollDistributor.SKILLING_PERCENT);
            return quantity > 0 ? 1 : -1;
        }

        @Override
        public void stop(Player player) {
            setActionDelay(player, 3);
        }
    }

    private static final class Recipe {
        private final RecipeType type;
        private final Ingredients ingredient;
        private final int slot;
        private final Herbs herb;
        private final CrystalPot crystalPotion;

        private Recipe(Ingredients ingredient, int slot) {
            this.type = RecipeType.POTION;
            this.ingredient = ingredient;
            this.slot = slot;
            this.herb = null;
            this.crystalPotion = null;
        }

        private Recipe(Herbs herb) {
            this.type = RecipeType.CLEAN_HERB;
            this.ingredient = null;
            this.slot = -1;
            this.herb = herb;
            this.crystalPotion = null;
        }

        private Recipe(CrystalPot crystalPotion) {
            this.type = RecipeType.COMBINATION_POTION;
            this.ingredient = null;
            this.slot = -1;
            this.herb = null;
            this.crystalPotion = crystalPotion;
        }

        private int getIngredientId() {
            return ingredient.getItemId();
        }

        private int getOtherItemId() {
            return ingredient.getOtherItems()[slot];
        }

        private int getProductId() {
            if (isCleanHerb())
                return herb.getCleanId();
            if (isCombinationPotion())
                return crystalPotion.getProducedPotion();
            return ingredient.getRewards()[slot];
        }

        private int getLevel() {
            if (isCleanHerb())
                return herb.getLevel();
            if (isCombinationPotion())
                return crystalPotion.getLevelRequired();
            return ingredient.getLevels()[slot];
        }

        private Herbs getHerb() {
            return herb;
        }

        private CrystalPot getCrystalPotion() {
            return crystalPotion;
        }

        private boolean isCleanHerb() {
            return type == RecipeType.CLEAN_HERB;
        }

        private boolean isCombinationPotion() {
            return type == RecipeType.COMBINATION_POTION;
        }

        private boolean isOverload() {
            return type == RecipeType.POTION && ingredient == Ingredients.TORSTOL && getOtherItemId() != Herblore.VIAL;
        }

        private boolean matches(Recipe other) {
            return other != null && type == other.type && ingredient == other.ingredient && slot == other.slot
                    && herb == other.herb && crystalPotion == other.crystalPotion;
        }
    }

    private enum RecipeType {
        POTION, CLEAN_HERB, COMBINATION_POTION
    }
}
