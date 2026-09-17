package com.rs.game.player.actions.smithing;

import java.util.HashMap;
import java.util.Map;

import com.rs.cache.loaders.rs3.RS3ClientScriptMap;
import com.rs.game.Animation;
import com.rs.game.Graphics;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.activites.halloween.ClueScrollDistributor;
import com.rs.game.item.Item;
import com.rs.game.npc.NPC;
import com.rs.game.npc.others.randomevent.impl.SmithingRandomEvent;
import com.rs.game.player.Equipment;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.PerkManager.DonationPerk;
import com.rs.game.player.actions.Action;
import com.rs.game.player.actions.invention.InventionConstants.Perks;
import com.rs.game.player.actions.invention.InventionData.Perk;
import com.rs.game.player.actions.smithing.defs.SmeltingBar;
import com.rs.game.player.content.MetalBank;
import com.rs.game.player.content.RS3SkillsDialogue;
import com.rs.game.player.content.RS3SkillsDialogue.SkillDialogueResult;
import com.rs.game.player.content.petperks.PetPerkHandler;
import com.rs.game.player.content.packs.portable.PortableStation;
import com.rs.game.player.content.skillingcontracts.impl.SmithingContractList;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Colors;
import com.rs.utils.Utils;

public class Smelting extends Action {

    private static final String RS3_SMELTING_PRODUCTS_KEY = "SmeltingRs3Products";
    private static final String RS3_SMELTING_ACTION_PRODUCTS_KEY = "SmeltingRs3ActionProducts";
    private static final String RS3_SMELTING_CATEGORY_INDEX_KEY = "SmeltingRs3CategoryIndex";
    private static final int RS3_SMELTING_MENU_MAP = 15093;
    private static final int RS3_SMELTING_MENU_NAMES_MAP = 15092;
    private static final int[] RS3_SMELTING_CATEGORY_MAPS = { 7083, 2412, 7084 };
    private static final int RS3_SMELTING_MAX_QUANTITY = 60;

    private final SmeltingBar bar;
    private final WorldObject object;
    private final boolean portable;

    public int ticks;

    public Smelting(int slotId, WorldObject object, int ticks, boolean portable) {
        this.bar = SmeltingBar.forId(slotId);
        this.object = object;
        this.ticks = ticks;
        this.portable = portable;
    }

    private static int getAvailableRequirementAmount(Player player, int itemId) {
        int amount = player.getInventory().getAmountOf(itemId);
        if (MetalBank.isMetalBankItem(itemId))
            amount += player.getMetalBankAmount(itemId);
        return amount;
    }

    private static void sendMissingRequirement(Player player, Item requirement, SmeltingBar bar, int amount) {
        String name = requirement.getDefinitions().getName().toLowerCase();
        player.sendMessage("You need " + amount + " x " + name + " to create a " + bar.getProducedBar().getDefinitions().getName() + ".");
    }

    private static int getAlchemicCostPerBar(SmeltingBar bar) {
        if (bar.getItemsRequired().length > 1) {
            int amount = (bar.getItemsRequired()[1].getAmount() - 2) / 2;
            return amount < 2 ? 1 : amount;
        }
        return 1;
    }

    private static boolean hasSmeltingRequirements(Player player, SmeltingBar bar, boolean sendMessage) {
        if (!player.getPerkManager().hasPerkActive(DonationPerk.ALCHEMIC_SMITHING)) {
            for (Item requirement : bar.getItemsRequired()) {
                if (getAvailableRequirementAmount(player, requirement.getId()) >= requirement.getAmount())
                    continue;
                if (sendMessage)
                    sendMissingRequirement(player, requirement, bar, requirement.getAmount());
                return false;
            }
            return true;
        }

        if (bar == SmeltingBar.BRONZE) {
            if (getAvailableRequirementAmount(player, 436) > 0 || getAvailableRequirementAmount(player, 438) > 0)
                return true;
            if (sendMessage)
                player.sendMessage("You need either 1 tin or copper ore to smith a bronze bar!");
            return false;
        }

        Item requirement = bar.getItemsRequired()[0];
        int amount = getAlchemicCostPerBar(bar);
        if (getAvailableRequirementAmount(player, requirement.getId()) >= amount)
            return true;
        if (sendMessage)
            sendMissingRequirement(player, requirement, bar, amount);
        return false;
    }

    private static void deleteRequirement(Player player, int itemId, int amount) {
        int inventoryAmount = Math.min(amount, player.getInventory().getAmountOf(itemId));
        if (inventoryAmount > 0)
            player.getInventory().deleteItem(itemId, inventoryAmount);
        int remaining = amount - inventoryAmount;
        if (remaining > 0 && MetalBank.isMetalBankItem(itemId))
            player.removeMetalBankItem(itemId, remaining);
    }

    private static void deleteBronzeAlchemicRequirement(Player player, int amount) {
        boolean hasCopper = getAvailableRequirementAmount(player, 436) > 0;
        boolean hasTin = getAvailableRequirementAmount(player, 438) > 0;
        if (hasCopper && hasTin) {
            deleteRequirement(player, Utils.random(1) == 0 ? 436 : 438, amount);
        } else if (hasCopper) {
            deleteRequirement(player, 436, amount);
        } else if (hasTin) {
            deleteRequirement(player, 438, amount);
        }
    }

    public boolean isSuccessFull(Player player) {
        return true;
    }

    @Override
    public boolean process(Player player) {
        if (bar == null || player == null || object == null)
            return false;
        if (player.clickedObject != null) {
            if (!World.containsObjectWithId(player.clickedObject, player.clickedObject.getId()))
                return false;
        }
        if (!hasSmeltingRequirements(player, bar, true))
            return false;

        if (player.getSkills().getLevel(Skills.SMITHING) < bar.getLevelRequired()) {
            player.getDialogueManager().startDialogue("SimpleMessage", "You need a Smithing level of at least " + bar.getLevelRequired() + " to smelt " + bar.getProducedBar().getDefinitions().getName());
            return false;
        }
        if (Utils.random(500) == 0 && player.hasRandomEvent()) {
            if (!player.followedByRandomEventNPC()) {
                NPC npc = new SmithingRandomEvent(player, player);
                npc.setNextAnimation(new Animation(-1));
                if (npc.withinDistance(player, 14)) {
                    player.setCurrentRandomEventNPC(npc);
                    player.sendMessage("<col=ff0000>A Dwarven Miner appears from nowhere.");
                }
            }
        }
        player.faceObject(object);
        return true;
    }


    // 3 ticks at requirement, -1 tick per +5 smithing levels, clamped to [1..3]
    private int getSmeltCycleDelay(Player player) {
        int smith = player.getSkills().getLevel(Skills.SMITHING);
        int req = bar.getLevelRequired();


        int over = Math.max(0, smith - req);
        int reduction = over / 5; // every 5 levels over => -1 tick


        int delay = 3 - reduction; // base 3, gets faster with levels
        if (delay < 1) delay = 0;
        if (delay > 3) delay = 3;


        return delay;
    }


    @Override
    public int processWithDelay(Player player) {
        ticks--;
        if (player.getAnimations().hasArcaneSmelt && player.getAnimations().arcaneSmelt) {
            player.setNextAnimation(new Animation(20292));
            player.setNextGraphics(new Graphics(4000));
        } else
            player.setNextAnimation(new Animation(32626));
        Item weapon = player.getEquipment().getItem(Equipment.SLOT_WEAPON);
        boolean hasAugmentedTool = weapon != null && weapon.getInventionData() != null && (weapon.getName().toLowerCase().contains("hammer-tron") || weapon.getName().toLowerCase().contains("crystal hammer"));
            if (!hasAugmentedTool)
                weapon = null;
            double xp = getExp(player);
            Perk tinker = weapon != null ? player.getInventionManager().hasPerk(weapon, Perks.TINKER) : null;
            boolean tinkerActive = tinker != null && Math.random() <= (0.05 * (double) tinker.getRank() * (tinker.hasIncreasedChance() ? 1.15 : 1.00));
            if (tinkerActive) {
                xp *= 1.25;
                player.getPackets().sendGameMessage("<col=00ff00>Your tinker perk allows you to carry out higher quality work, awarding extra XP.");
            }
        player.getInventionManager().processSkillXp(Skills.SMITHING, xp, weapon);
        player.getSkills().addXp(Skills.SMITHING, xp);
        int alchemAmt = getAlchemicCostPerBar(bar);
        boolean effPerkProc = player.getPerkManager().hasPerkActive(DonationPerk.ALCHEMIC_SMITHING) ? PetPerkHandler.handleEfficiencyExpert(player, new Item(bar.getItemsRequired()[0].getId(), alchemAmt)) : PetPerkHandler.handleEfficiencyExpert(player, bar.getItemsRequired());
        if (player.getPerkManager().hasPerkActive(DonationPerk.ALCHEMIC_SMITHING)) {
            if (!effPerkProc && bar == SmeltingBar.BRONZE)
                deleteBronzeAlchemicRequirement(player, alchemAmt);
            else if (!effPerkProc)
                deleteRequirement(player, bar.getItemsRequired()[0].getId(), alchemAmt);
        } else {
            if (!effPerkProc)
                for (Item required : bar.getItemsRequired())
                    deleteRequirement(player, required.getId(), required.getAmount());
        }
        if (isSuccessFull(player)) {
            SmithingContractList.listenSmelt(player, bar);
            if (bar != SmeltingBar.CORRUPTED_ORE) {
                player.addSmithingActions();
                if (effPerkProc)
                    player.addItem(bar.getProducedBar());
                else
                    player.getInventory().addItem(bar.getProducedBar());
                if (portable && Utils.random(4) == 2) {
                    player.getBank().addItem(new Item(bar.getProducedBar().getId()), true);
                    player.sendMessage(Colors.GOLD + "<shad=000000>You managed to make an extra bar from your ore in this exceptional furnace! It has been sent directly to your bank.", true);
                }
                player.sendMessage("You retrieve a bar of " + bar.getProducedBar().getDefinitions().getName().toLowerCase().replace(" bar", "") + "; " + "smithing actions: " + Colors.RED + Utils.getFormattedNumber(player.getSmithingActions()) + "</col>.", true);
                ClueScrollDistributor.givePlayerClueScrollIfProbable(player, ClueScrollDistributor.SKILLING_PERCENT);
                if (player.getDailyManager().getTask() != null) {
                    if (bar.getProducedBar().getId() == player.getTaskItemId()) {
                        player.getDailyManager().processTask();
                    }
                }

            } else
                player.sendMessage("You've successfully smelt the Corrupted ore.", true);
        } else {
            player.sendMessage("The ore is too impure and you fail to refine it.", true);
        }
        Perk rapid = weapon != null ? player.getInventionManager().hasPerk(weapon, Perks.RAPID) : null;
        boolean rapidActive = rapid != null && Math.random() <= (0.05 * (double) rapid.getRank() * (rapid.hasIncreasedChance() ? 1.15 : 1.00));
        if (rapidActive)
            player.getPackets().sendGameMessage("<col=00FF00>Your rapid perk speeds up the action process.");
        if (ticks > 0) {
            int delay = getSmeltCycleDelay(player);


// Optional: make RAPID actually speed it up by 1 tick (still clamped to min 1)
            if (rapidActive)
                delay = Math.max(1, delay - 1);


            return delay;
        }
        return -1;
    }


    public static int getMaxSmeltingQuantity(Player player, SmeltingBar bar) {
        if (player.getPerkManager().hasPerkActive(DonationPerk.ALCHEMIC_SMITHING)) {
            if (bar == SmeltingBar.BRONZE) {
                int copper = getAvailableRequirementAmount(player, 436);
                int tin = getAvailableRequirementAmount(player, 438);
                return copper + tin;
            }
            int perBar = getAlchemicCostPerBar(bar);
            int available = getAvailableRequirementAmount(player, bar.getItemsRequired()[0].getId());
            return available / Math.max(1, perBar);
        }

        int max = Integer.MAX_VALUE;
        for (Item req : bar.getItemsRequired()) {
            int have = getAvailableRequirementAmount(player, req.getId());
            int need = Math.max(1, req.getAmount());
            max = Math.min(max, have / need);
        }
        return (max == Integer.MAX_VALUE) ? 0 : max;
    }

    private int barsPossible(Player player) {
        return getMaxSmeltingQuantity(player, bar);
    }



    @Override
    public boolean start(Player player) {
        if (bar == null || player == null || object == null)
            return false;

        if (!hasSmeltingRequirements(player, bar, true))
            return false;

        if (player.getSkills().getLevel(Skills.SMITHING) < bar.getLevelRequired()) {
            player.sendMessage("You need a Smithing level of at least " + bar.getLevelRequired() + " to smelt " + bar.getProducedBar().getDefinitions().getName());
            return false;
        }

        // Start message
        if (bar != SmeltingBar.CORRUPTED_ORE)
            player.sendMessage("You place the required ore in the furnace and attempt to smelt it.", true);
        else
            player.sendMessage("You place the required ores and attempt to create a bar of " + bar.getProducedBar().getDefinitions().getName().toLowerCase().replace(" bar", "") + ".", true);

        int computed = barsPossible(player);
        if (computed <= 0)
            return false;

        this.ticks = Math.max(1, Math.min(ticks, computed));

        return true;
    }

    public static boolean hasRs3SmeltingProducts(Player player) {
        return player.getTemporaryAttributtes().get(RS3_SMELTING_PRODUCTS_KEY) != null;
    }

    public static boolean handleRs3ProductSelection(Player player, int slotId) {
        int[] products = (int[]) player.getTemporaryAttributtes().get(RS3_SMELTING_PRODUCTS_KEY);
        if (products == null)
            return false;
        int index = (slotId - 1) / 4;
        if (index < 0 || index >= products.length)
            index = slotId;
        if (index < 0 || index >= products.length)
            return false;
        if (!setRs3Product(player, products[index]))
            player.getPackets().sendGameMessage("That item is not wired into smelting yet.");
        return true;
    }

    public static boolean handleRs3CategorySelection(Player player, int slotId) {
        Integer currentIndex = (Integer) player.getTemporaryAttributtes().get(RS3_SMELTING_CATEGORY_INDEX_KEY);
        int nextIndex = currentIndex == null ? 0 : (currentIndex + 1) % RS3_SMELTING_CATEGORY_MAPS.length;
        int displayProduct = prepareRs3Category(player, -1, nextIndex);
        if (displayProduct == -1)
            return false;
        player.getVarBitManager().sendVar(RS3SkillsDialogue.CATEGORY_VAR, RS3_SMELTING_CATEGORY_MAPS[nextIndex]);
        setRs3Product(player, displayProduct);
        sendRs3SmeltingTitle(player);
        return true;
    }

    private static int prepareRs3ProductList(Player player, int actionProduct) {
        for (int categoryIndex = 0; categoryIndex < RS3_SMELTING_CATEGORY_MAPS.length; categoryIndex++) {
            int displayProduct = prepareRs3Category(player, actionProduct, categoryIndex);
            if (displayProduct != -1)
                return displayProduct;
        }
        if (actionProduct != -1) {
            for (int categoryIndex = 0; categoryIndex < RS3_SMELTING_CATEGORY_MAPS.length; categoryIndex++) {
                int displayProduct = prepareRs3Category(player, -1, categoryIndex);
                if (displayProduct != -1)
                    return displayProduct;
            }
        }
        return -1;
    }

    private static int prepareRs3Category(Player player, int actionProduct, int categoryIndex) {
        if (categoryIndex < 0 || categoryIndex >= RS3_SMELTING_CATEGORY_MAPS.length)
            return -1;
        int[] products = getProductsFromCategory(RS3_SMELTING_CATEGORY_MAPS[categoryIndex]);
        Map<Integer, Integer> actionProducts = new HashMap<Integer, Integer>();
        for (int product : products) {
            if (SmeltingBar.forProductId(product) != null)
                actionProducts.put(product, product);
        }
        if (actionProducts.isEmpty())
            return -1;
        player.getTemporaryAttributtes().put(RS3_SMELTING_PRODUCTS_KEY, products);
        player.getTemporaryAttributtes().put(RS3_SMELTING_ACTION_PRODUCTS_KEY, actionProducts);
        player.getTemporaryAttributtes().put(RS3_SMELTING_CATEGORY_INDEX_KEY, categoryIndex);
        if (actionProduct != -1 && actionProducts.containsKey(actionProduct))
            return actionProduct;
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

    private static String getRs3CategoryName(int categoryIndex) {
        switch (categoryIndex) {
            case 1:
                return "Special bars";
            case 2:
                return "Cannonballs";
            default:
                return "Metal bars";
        }
    }

    private static void sendRs3SmeltingTitle(Player player) {
        Integer categoryIndex = (Integer) player.getTemporaryAttributtes().get(RS3_SMELTING_CATEGORY_INDEX_KEY);
        player.getPackets().sendGlobalString(2390, "Smelting - " + getRs3CategoryName(categoryIndex == null ? 0 : categoryIndex));
    }

    @SuppressWarnings("unchecked")
    private static int getActionProduct(Player player, int displayProduct) {
        Map<Integer, Integer> actionProducts = (Map<Integer, Integer>) player.getTemporaryAttributtes().get(RS3_SMELTING_ACTION_PRODUCTS_KEY);
        if (actionProducts == null)
            return displayProduct;
        Integer actionProduct = actionProducts.get(displayProduct);
        return actionProduct == null ? -1 : actionProduct;
    }

    private static boolean setRs3Product(Player player, int displayProduct) {
        int actionProduct = getActionProduct(player, displayProduct);
        SmeltingBar bar = actionProduct == -1 ? null : SmeltingBar.forProductId(actionProduct);
        if (bar == null)
            return false;
        int maxQuantity = Math.max(1, Math.min(RS3_SMELTING_MAX_QUANTITY, getMaxSmeltingQuantity(player, bar)));
        RS3SkillsDialogue.forceSetProduct(player, displayProduct, maxQuantity);
        refreshRs3Product(player, displayProduct, bar);
        return true;
    }

    private static void refreshRs3Product(final Player player, final int displayProduct, final SmeltingBar bar) {
        WorldTasksManager.schedule(new WorldTask() {
            private int refreshes;

            @Override
            public void run() {
                if (!hasRs3SmeltingProducts(player)
                        || player.getVarBitManager().getValue(RS3SkillsDialogue.PRODUCT_VAR) != displayProduct) {
                    stop();
                    return;
                }
                int maxQuantity = Math.max(1, Math.min(RS3_SMELTING_MAX_QUANTITY, getMaxSmeltingQuantity(player, bar)));
                RS3SkillsDialogue.forceSetProduct(player, displayProduct, maxQuantity);
                if (++refreshes >= 2)
                    stop();
            }
        }, 1, 1);
    }

    private static int getBestAvailableProduct(Player player) {
        int selectedProductId = -1;
        int highestLevel = -1;
        for (int categoryMap : RS3_SMELTING_CATEGORY_MAPS) {
            for (int productId : getProductsFromCategory(categoryMap)) {
                SmeltingBar bar = SmeltingBar.forProductId(productId);
                if (bar == null)
                    continue;
                if (player.getSkills().getLevel(Skills.SMITHING) < bar.getLevelRequired())
                    continue;
                if (!hasSmeltingRequirements(player, bar, false))
                    continue;
                if (bar.getLevelRequired() >= highestLevel) {
                    selectedProductId = productId;
                    highestLevel = bar.getLevelRequired();
                }
            }
        }
        if (selectedProductId != -1)
            return selectedProductId;
        for (SmeltingBar bar : SmeltingBar.getFurnaceBars())
            if (bar.getProducedBar().getId() > 0)
                return bar.getProducedBar().getId();
        return -1;
    }

    public static void sendRs3SmeltingInterface(Player player, final WorldObject object) {
        final boolean portable = PortableStation.isPortableObject(object);
        final int initialProduct = getBestAvailableProduct(player);
        player.getDialogueManager().startDialogue(new Dialogue() {

            @Override
            public void start() {
                int displayProduct = prepareRs3ProductList(player, initialProduct);
                if (displayProduct == -1) {
                    player.getPackets().sendGameMessage("There are no smelting products available.");
                    end();
                    return;
                }
                Integer categoryIndex = (Integer) player.getTemporaryAttributtes().get(RS3_SMELTING_CATEGORY_INDEX_KEY);
                RS3SkillsDialogue.sendCustomSkillDialogueWithoutProduct(player, RS3_SMELTING_MENU_MAP, RS3_SMELTING_MENU_NAMES_MAP,
                        RS3_SMELTING_CATEGORY_MAPS[categoryIndex == null ? 0 : categoryIndex]);
                setRs3Product(player, displayProduct);
                sendRs3SmeltingTitle(player);
            }

            @Override
            public void run(int interfaceId, int componentId) {
                SkillDialogueResult result = RS3SkillsDialogue.getResult(player, componentId == RS3SkillsDialogue.CONTINUE_OPTION);
                if (componentId != RS3SkillsDialogue.CONTINUE_OPTION)
                    return;
                int productId = getActionProduct(player, result.getProduce());
                end();
                SmeltingBar bar = SmeltingBar.forProductId(productId);
                if (bar == null) {
                    player.getPackets().sendGameMessage("That item is not wired into smelting yet.");
                    return;
                }
                player.getActionManager().setAction(new Smelting(bar.getButtonId(), object, result.getQuantity(), portable));
            }

            @Override
            public void finish() {
                player.getTemporaryAttributtes().remove(RS3_SMELTING_PRODUCTS_KEY);
                player.getTemporaryAttributtes().remove(RS3_SMELTING_ACTION_PRODUCTS_KEY);
                player.getTemporaryAttributtes().remove(RS3_SMELTING_CATEGORY_INDEX_KEY);
            }
        });
    }



    @Override
    public void stop(Player player) {
        this.setActionDelay(player, 3);
        player.clickedObject = null;
    }

    /**
     * Calculates how much experience should the player receive.
     *
     * @param player The player smelting.
     * @return the EXP as Double.
     */
    private double getExp(Player player) {
        double xp = bar.getExperience();
        xp *= blackSmithSuit(player);
        if (portable)
            xp *= 1.1;
        return xp;
    }

    /**
     * XP modifier by wearing items.
     *
     * @param player The player.
     * @return the XP modifier.
     */
    private double blackSmithSuit(Player player) {
        double xpBoost = 1.0;
        if (player.getEquipment().getHatId() == 25195)
            xpBoost *= 1.01;
        if (player.getEquipment().getChestId() == 25196)
            xpBoost *= 1.01;
        if (player.getEquipment().getHatId() == 32280)
            xpBoost *= 1.03;
        if (player.getEquipment().getHatId() == 32280 && player.getEquipment().getChestId() == 25196 && player.getEquipment().getLegsId() == 25197 && player.getEquipment().getBootsId() == 25198 && player.getEquipment().getGlovesId() == 25199)
            xpBoost *= 1.03;
        if (player.getEquipment().getLegsId() == 25197)
            xpBoost *= 1.01;
        if (player.getEquipment().getBootsId() == 25198)
            xpBoost *= 1.01;
        if (player.getEquipment().getGlovesId() == 25199)
            xpBoost *= 1.01;
        if (player.getEquipment().getHatId() == 25195 && player.getEquipment().getChestId() == 25196 && player.getEquipment().getLegsId() == 25197 && player.getEquipment().getBootsId() == 25198 && player.getEquipment().getGlovesId() == 25199)
            xpBoost *= 1.01;
        return xpBoost;
    }

}
