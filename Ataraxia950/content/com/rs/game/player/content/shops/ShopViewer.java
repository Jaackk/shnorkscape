package com.rs.game.player.content.shops;

import com.rs.Settings;
import com.rs.cache.Cache;
import com.rs.cache.loaders.ItemDefinitions;
import com.rs.game.World;
import com.rs.game.activites.halloween.ClueScrollDistributor;
import com.rs.game.item.Item;
import com.rs.game.player.Equipment;
import com.rs.game.player.Player;
import com.rs.game.player.QuestManager.Quests;
import com.rs.game.player.content.ChimesBuyables;
import com.rs.game.player.content.ItemConstants;
import com.rs.game.player.content.TaskTab;
import com.rs.utils.Colors;
import com.rs.utils.EconomyPrices;
import com.rs.utils.ItemSetsKeyGenerator;
import com.rs.utils.Logger;
import com.rs.utils.Utils;
import com.rs.utils.data.parsers.items.ItemExaminesDataParser;
import com.rs.utils.data.parsers.misc.pojos.ShopCurrency;
import com.rs.utils.data.parsers.misc.pojos.ShopItem;
import com.rs.utils.data.parsers.misc.pojos.ShopPojo;
import com.rs.utils.mysql.QueryExecutor;
import com.rs.utils.mysql.impl.News;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public class ShopViewer {

    public static final int COINS = 995;
    private static final int MAIN_STOCK_ITEMS_KEY = ItemSetsKeyGenerator.generateKey();
    private static final int MAX_SHOP_ITEMS = 40;
    private static final int GP_SHOP_BUYBACK_NUMERATOR = 1;
    private static final int GP_SHOP_BUYBACK_DENOMINATOR = 2;

    private final String name;
    private final ShopItem[] mainStock;
    private final int[] defaultQuantity;
    private ShopItem[] generalStock;
    private ShopCurrency currency;
    private int amount;
    private boolean sellingBlocked;
    private final CopyOnWriteArrayList<Player> viewingPlayers;

    public String getName() {
        return name;
    }

    public ShopViewer(ShopPojo shopPojo) {
        viewingPlayers = new CopyOnWriteArrayList<>();
        name = shopPojo.getName();
        mainStock = shopPojo.getItems();
        if (shopPojo.getCurrency() == ShopCurrency.GP) {
            for (ShopItem shopItem : mainStock) {
                if (shopItem != null) {
                    shopItem.setPrice(getCacheShopPrice(shopItem.getId()));
                }
            }
        }
        currency = shopPojo.getCurrency();
        defaultQuantity = new int[mainStock.length];
        for (int i = 0; i < defaultQuantity.length; i++) {
            defaultQuantity[i] = mainStock[i].getAmount();
        }
        if (shopPojo.isGeneralStore() && mainStock.length < MAX_SHOP_ITEMS)
            generalStock = new ShopItem[MAX_SHOP_ITEMS - mainStock.length];
        sellingBlocked = shopPojo.isSellingBlocked();
    }

    public ShopViewer(String name, int money, ShopItem[] mainStock, boolean isGeneralStore) {
        viewingPlayers = new CopyOnWriteArrayList<Player>();
        this.name = name;
        this.mainStock = mainStock;
        defaultQuantity = new int[mainStock.length];
        for (int i = 0; i < defaultQuantity.length; i++)
            defaultQuantity[i] = mainStock[i].getAmount();
        if (isGeneralStore && mainStock.length < MAX_SHOP_ITEMS)
            generalStock = new ShopItem[MAX_SHOP_ITEMS - mainStock.length];
    }

    private boolean addItem(int itemId, int quantity) {
        for (ShopItem item : mainStock) {
            if (item.getId() == itemId) {
                item.setAmount(item.getAmount() + quantity);
                refreshShop();
                return true;
            }
        }
        if (currency == ShopCurrency.CHIMES) {
            refreshShop();
            return true;
        }
        if (generalStock != null && currency != ShopCurrency.CHIMES) {
            for (ShopItem item : generalStock) {
                if (item == null)
                    continue;
                if (item.getId() == itemId) {
                    item.setAmount(item.getAmount() + quantity);
                    refreshShop();
                    return true;
                }
            }
            for (int i = 0; i < generalStock.length; i++) {
                if (generalStock[i] == null) {
                    generalStock[i] = new ShopItem(itemId, quantity, getCacheShopPrice(itemId));
                    refreshShop();
                    return true;
                }
            }
        }
        return false;
    }

    private int getAmountInShop(int itemId) {
        for (ShopItem item : mainStock) {
            if (item.getId() == itemId)
                return item.getAmount();
        }
        if (currency == ShopCurrency.CHIMES)
            return 0;
        if (generalStock != null && currency != ShopCurrency.CHIMES) {
            for (ShopItem item : generalStock) {
                if (item == null)
                    continue;
                if (item.getId() == itemId)
                    return item.getAmount();
            }
            for (int i = 0; i < generalStock.length; i++) {
                if (generalStock[i] == null) {
                    return 0;
                }
            }
        }
        return -1;
    }

    public void addPlayer(final Player player) {
        viewingPlayers.add(player);
        player.getTemporaryAttributtes().put("Shop", this);
        player.setCloseInterfacesEvent(new Runnable() {
            @Override
            public void run() {
                viewingPlayers.remove(player);
                player.getTemporaryAttributtes().remove("Shop");
                player.getInterfaceManager().closeInventoryInterface();
            }
        });
        player.getPackets().sendConfig(304, generalStock != null ? 139 : MAIN_STOCK_ITEMS_KEY);
        player.getPackets().sendConfig(305, -1);
        sendStore(player);
        sendInventory(player);
        player.getInterfaceManager().sendInterface(1265);
        player.getPackets().sendConfig(306, currency.getItemId() == 995 || currency.getItemId() == 37753 ? currency.getItemId() == 995 ? 995 : 37753 : 995);
        player.getPackets().sendIComponentSettings(1265, 28, 0, 4, 2097406);
        player.getPackets().sendIComponentSettings(1265, 82, 0, 4, 10223616);
        player.getPackets().sendIComponentSettings(1265, 20, 0, getStoreSize(), 2097406);
        String name = this.name + (player.isIronMan() || player.isHCIronMan() || player.isNoviceIronMan() || player.isExpertIronMan() || player.isIntermediateIronMan() ? "(Ironmen)" : "");
        player.getPackets().sendGlobalString(2360, name);
        player.getPackets().sendExecuteScript(8420, 82903048, 82903256, 82903049, 82903257, name, 21218, 1007);
        sendExtraConfigs(player);
    }

    public void buy(Player player, int slotId, int quantity) {
        if (slotId >= getStoreSize())
            return;
        ShopItem item = slotId >= mainStock.length ? generalStock[slotId - mainStock.length] : mainStock[slotId];
        if (item == null)
            return;
        ItemDefinitions definition = ItemDefinitions.getItemDefinitions(item.getId());
        if (definition == null) {
            return;
        }
        if (slotId >= mainStock.length) {
            if (!player.canTrade(null))
                return;
        }
        if (item.getAmount() == 0) {
            player.getPackets().sendGameMessage("There is no stock of that item at the moment.");
            return;
        }
        if (ClueScrollDistributor.isClueScroll(item.getId()) && player.getTreasureTrails().hasClueScrollItem()) {
            player.sendMessage("You already have a clue scroll item! Complete your current one before fetching a new one!");
            return;
        }
        int price = getShopBuyPrice(item);
        if (price < 1) {
            price = 1;
        }
        HashMap<Integer, Integer> requiriments = definition.getWearingSkillRequiriments();
        boolean hasRequiriments = true;
        if (definition.getName().contains("cape (t)") || definition.getName().contains(" master cape") || definition.getName().equalsIgnoreCase("Dungeoneering cape")) {
            int skill = 0, level = 99;
            if (requiriments != null) {
                for (Map.Entry<Integer, Integer> entry : requiriments.entrySet()) {
                    int skillId = entry.getKey();
                    if (skillId > 25 || skillId < 0)
                        continue;
                    level = entry.getValue();
                    skill = skillId;
                    if (player.getSkills().getLevelForXp(skillId) < requiriments.get(skillId))
                        hasRequiriments = false;
                }
            }
            if (!hasRequiriments) {
                player.sendMessage("You need " + Utils.getAorAn(definition.getName()) + " " + player.getSkills().getSkillName(skill) + " of " + level + " to buy " + Utils.getAorAn(definition.getName()) + " " + definition.getName() + ".");
                return;
            }
        }
        if (definition.getName().equalsIgnoreCase("blue cape") || definition.getName().equalsIgnoreCase("red cape")) {
            if (!player.getQuestManager().completedQuest(Quests.NOMADS_REQUIEM)) {
                player.sendMessage("You need to complete Nomad's Requiem to buy a " + definition.getName() + ".");
                return;
            }
        }
        if (quantity > item.getAmount())
            quantity = item.getAmount();
        int amountInInv = player.getInventory().getAmountOf(item.getId());
        if (item.getDefinitions().isStackable() || item.getDefinitions().isNoted()) {
            if (player.getInventory().getFreeSlots() == 0 && amountInInv == 0) {
                player.sendMessage("Inventory full. To make more room, sell, drop or bank something.");
                return;
            }
            if ((long) amountInInv + (long) quantity > Integer.MAX_VALUE) {
                quantity = Integer.MAX_VALUE - amountInInv;
                player.sendMessage("You can't hold anymore " + item.getDefinitions().getName() + ".");
                if (quantity == 0)
                    return;
            }
        } else {
            if (player.getInventory().getFreeSlots() == 0) {
                player.sendMessage("Inventory full. To make more room, sell, drop or bank something.");
                return;
            }
            if (quantity > player.getInventory().getFreeSlots()) {
                quantity = player.getInventory().getFreeSlots();
                player.sendMessage("You do not have enough inventory space to buy this.");
                if (quantity == 0)
                    return;
            }
        }
        long totalPriceOfSale = (long) quantity * (long) price;
        if (currency == ShopCurrency.GP) {
            long coinsAmount = (long) player.getInventory().getItems().getNumberOf(currency.getItemId()) + (long) player.getMoneyPouch().getTotal();
            if (totalPriceOfSale > coinsAmount) {
                quantity = (int) (coinsAmount / (long) price);
                player.getPackets().sendGameMessage("You don't have enough coins.");
                if (quantity == 0)
                    return;
            }
        } else {
            int amountCurrency = currency.getAmountOfCurrency(player);
            if (totalPriceOfSale > amountCurrency) {
                quantity = (int) (amountCurrency / (long) price);
                player.getPackets().sendGameMessage("You don't have enough " + currency.toString() + ", you currently have: " + Utils.getFormattedNumber(currency.getAmountOfCurrency(player)) + " " + currency.toString() + ".");
                if (quantity == 0)
                    return;
            }
        }
        totalPriceOfSale = (long) quantity * (long) price;
        int amountCurrency = currency.getAmountOfCurrency(player);
        int amountToRemove = (int) (totalPriceOfSale > (long) amountCurrency ? amountCurrency : totalPriceOfSale);
        currency.removeCurrency(player, amountToRemove);
        totalPriceOfSale -= amountToRemove;
        if (totalPriceOfSale > 0 && currency == ShopCurrency.GP)
            player.getMoneyPouch().removeMoneyMisc((int) totalPriceOfSale);
        item.setAmount(item.getAmount() - quantity);
        player.getInventory().addItem(item.getId(), quantity);
        if (ClueScrollDistributor.isClueScroll(item.getId())) {
            ClueScrollDistributor.givePlayerClueScroll(player, ClueScrollDistributor.getLevelForId(item.getId()), false);
        }
        if (currency != ShopCurrency.GP)
            buyItem(player, item, quantity);
        if (item.getAmount() <= 0 && slotId >= mainStock.length && generalStock != null)
            generalStock[slotId - mainStock.length] = null;
        refreshShop();
        sendInventory(player);
    }

    private void buyItem(Player player, ShopItem item, int quantity) {
        String itemName = item.getDefinitions().getName();
        player.sendMessage("You have purchased " + quantity + " " + Colors.RED + itemName + " " + "</col>from " + name + "!");
        player.getPackets().sendMusicEffect(28);
        if (currency == ShopCurrency.RARE_ITEM_TOKENS) {
            World.sendWorldMessage(Colors.CYAN + "<shad=000000><img=6>News: " + player.getDisplayName() + " has received a " + itemName + " from the Rare cosmetic store!", false);
            QueryExecutor.submit(new News(player, "<b><img src=\"../bin/images/news/rare.png\" width=17> " + player.getDisplayName() + " has received a " + item.getDefinitions().getName() + " from the Rare cosmetic store!"));
        } else if (currency == ShopCurrency.HYBRID_TOKENS) {
            String formatting = ((itemName.contains("body") || itemName.contains("helm") || (itemName.contains("robe") && !itemName.contains("legs"))) ? "a " : "");
            World.sendWorldMessage(Colors.CYAN + "<shad=000000><img=6>News: " + player.getDisplayName() + " purchased " + formatting + itemName + " from the Hybrid armor store!", false);
            QueryExecutor.submit(new News(player, player.getDisplayName() + " purchased " + formatting + itemName + " from the Hybrid armor store!"));
        } else if (currency == ShopCurrency.CHIMES && !itemName.startsWith("Aggression") && !itemName.startsWith("Mystery")) {
            World.sendWorldMessage(Colors.CYAN + "<shad=000000><img=6>News: " + player.getDisplayName() + " has received a " + itemName + " from the Super Rares store!", false);
            QueryExecutor.submit(new News(player, "<b><img src=\"../bin/images/news/rare.png\" width=17> " + player.getDisplayName() + " has received a " + itemName + " from the Super Rares store!"));
        }
    }

    public int getAmount() {
        return this.amount;
    }

    public int getDefaultQuantity(int itemId) {
        for (int i = 0; i < mainStock.length; i++) {
            if (mainStock[i].getId() == itemId) {
                return defaultQuantity[i];
            }
        }
        return -1;
    }

    public ShopItem[] getMainStock() {
        return this.mainStock;
    }

    public int getStoreSize() {
        return mainStock.length + (generalStock != null ? generalStock.length : 0);
    }

    /**
     * Checks if the player is buying an item or selling it.
     *
     * @param player The player
     * @param slotId The slot id
     * @param amount The amount
     */
    public void handleShop(Player player, int slotId, int amount) {
        boolean isBuying = player.getTemporaryAttributtes().get("shop_buying") != null;
        if (isBuying)
            buy(player, slotId, amount);
        else
            sell(player, slotId, amount);
    }

    public boolean isGeneralStore() {
        return generalStock != null;
    }

    public void refreshLastX() {
        for (Player player : viewingPlayers) {
            if (player == null || player.hasFinished())
                continue;
            if (player.getShopLastViewX() == 0)
                player.getPackets().sendInterSetItemsOptionsScript(1266, 0, 93, 4, 7, "Value", "Sell-1", "Sell-5", "Sell-10", "Sell-X", "Examine");
            else
                player.getPackets().sendInterSetItemsOptionsScript(1266, 0, 93, 4, 7, "Value", "Sell-1", "Sell-5", "Sell-10", "Sell-X", "Sell-" + player.getShopLastViewX());
        }
    }

    public void refreshShop() {
        for (Player player : viewingPlayers) {
            if (player == null || player.hasFinished())
                continue;
            sendStore(player);
            player.getPackets().sendIComponentSettings(620, 25, 0, getStoreSize() * 6, 1150);
        }
    }

    public void restoreItems() {
        boolean needRefresh = false;
        for (int i = 0; i < mainStock.length; i++) {
            if (mainStock[i].getAmount() < defaultQuantity[i]) {
                mainStock[i].setAmount(mainStock[i].getAmount() + 1);
                needRefresh = true;
            } else if (mainStock[i].getAmount() > defaultQuantity[i]) {
                mainStock[i].setAmount(mainStock[i].getAmount() + -1);
                needRefresh = true;
            }
        }
        if (generalStock != null) {
            for (int i = 0; i < generalStock.length; i++) {
                ShopItem item = generalStock[i];
                if (item == null)
                    continue;
                generalStock[i] = null;
                needRefresh = true;
            }
        }
        if (needRefresh)
            refreshShop();
    }

    /**
     * Can we sell items back to shops, non-general store and a distinct item (not already in the shop).
     */
    private boolean sellInvisible() {
        switch (name) {
            case "Hazelmere's Chime Shop":
                return true;
            default:
                return false;
        }
    }


    public static void main(String[] args) throws IOException {
        Cache.init();
        Logger.getGlobal().info(ItemDefinitions.getItemDefinitions(4587).getValue());
        Logger.getGlobal().info(ItemDefinitions.getItemDefinitions(4588).getValue());
    }

    public void sell(Player player, int slotId, int quantity) {
        if (player.getInventory().getItemsContainerSize() < slotId)
            return;
        Item item = player.getInventory().getItem(slotId);
        if (item == null)
            return;
        int originalId = item.getId();
        if (!ItemConstants.isTradeable(item) || item.getId() == 995 || item.getId() == 37753 || item.getId() == currency.getItemId()) {
            player.getPackets().sendGameMessage(item.getDefinitions().getName() + " cannot be sold to shops.");
            return;
        }
        int dq = getDefaultQuantity(item.getId());
        if (dq == -1 && generalStock == null && currency != ShopCurrency.CHIMES) {
            player.sendMessage("You can't sell this item to this shop.");
            return;
        }

        if (sellingBlocked || currency.getItemId() == -1) {
            player.sendMessage("You can't sell items to this shop.");
            return;
        }

        if (currency.getItemId() == 32707) {
            player.sendMessage("You can't sell items to this shop.");
            return;
        }

        if (currency == ShopCurrency.CHIMES) {
            if (!ChimesBuyables.BUYABLES.containsKey(item.getId())) {
                player.sendMessage("You can't sell this item to this shop.");
                return;
            }
        }
        boolean sellInvisible = sellInvisible();
        int amountInShop = getAmountInShop(item.getId());
        if (amountInShop < 0 && !sellInvisible) {
            player.sendMessage("This shop can't hold anymore items.");
            return;
        }
        int price = getShopSellPrice(item);
        if (price < 1)
            price = 1;
        int numberOff = player.getInventory().getItems().getNumberOf(originalId);
        if (quantity > numberOff)
            quantity = numberOff;
        if (((long) quantity + (long) amountInShop) > Integer.MAX_VALUE) {
            quantity = Integer.MAX_VALUE - amountInShop;
            player.sendMessage("The shop can't hold anymore " + item.getName() + ".");
            if (quantity == 0)
                return;
        }
        int numberOffCash = currency.getAmountOfCurrency(player);
        long totalAmountOfSale = (long) quantity * (long) price;
        if (currency == ShopCurrency.GP) {
            int amountInPouch = player.getMoneyPouch().getTotal();
            if (totalAmountOfSale > ((long) (Integer.MAX_VALUE - numberOffCash) + (long) (Integer.MAX_VALUE - amountInPouch))) {
                quantity = (int) (((long) (Integer.MAX_VALUE - numberOffCash) + (long) (Integer.MAX_VALUE - amountInPouch)) / (long) price);
                player.getPackets().sendGameMessage("You can't hold anymore " + currency.toString() + ".");
                if (quantity == 0)
                    return;
            }
        } else {
            if (totalAmountOfSale > (long) (Integer.MAX_VALUE - numberOffCash)) {
                quantity = (int) ((long) (Integer.MAX_VALUE - numberOffCash) / (long) price);
                player.getPackets().sendGameMessage("You can't hold anymore " + currency.toString() + ".");
                if (quantity == 0)
                    return;
            }
        }
        totalAmountOfSale = (long) quantity * (long) price;
        player.sendFilteredMessage("You have sold " + Utils.formatNumber(quantity) + " x " + item.getName() + " for " + Utils.formatNumber(totalAmountOfSale) + " " + currency.toString() + ".");
        addItem(originalId, quantity);
        player.getInventory().deleteItem(originalId, quantity);
        if (currency == ShopCurrency.GP) {
            int amountInPouch = player.getMoneyPouch().getTotal();
            int amountToPouch = (int) (totalAmountOfSale > (long) (Integer.MAX_VALUE - amountInPouch) ? (Integer.MAX_VALUE - amountInPouch) : totalAmountOfSale);
            if (amountToPouch > 0)
                player.getMoneyPouch().addMoneyMisc(amountToPouch);
            totalAmountOfSale -= amountToPouch;
        }
        if (totalAmountOfSale > 0)
            player.getInventory().addItemToInventory(currency.getItemId(), (int) totalAmountOfSale);
        refreshShop();
    }

    public void sendExamine(Player player, int slotId) {
        if (slotId >= getStoreSize())
            return;
        ShopItem item = slotId >= mainStock.length ? generalStock[slotId - mainStock.length] : mainStock[slotId];
        if (item == null)
            return;
        player.getPackets().sendGameMessage(ItemExaminesDataParser.getGEExamine(item.getDefinitions().getName()));
    }

    public void sendExtraConfigs(Player player) {
        player.getPackets().sendConfig(299, -1);
        player.getPackets().sendConfig(306, -1);
        player.getPackets().sendConfig(301, -1);
        player.getTemporaryAttributtes().put("shop_buying", true);
    }

    private void sendItemInfo(Player player, ShopItem item) {
        int price = getShopBuyPrice(item);
        player.sendMessage(Colors.RED + item.getDefinitions().getName() + "</col> costs " + Colors.RED + Utils.getFormattedNumber(price) + "</col> " + currency.toString() + ", you have " + Colors.RED + Utils.getFormattedNumber(currency.getAmountOfCurrency(player)) + "</col>.");
        player.getPackets().sendIComponentText(1265, 137, "" + price);
        player.getPackets().sendHideIComponent(1265, 136, true);
    }

    public void sendInfo(Player player, int slotId, boolean isBuying) {
        if (isBuying) {
            if (slotId >= getStoreSize())
                return;
            ShopItem item = slotId >= mainStock.length ? generalStock[slotId - mainStock.length] : mainStock[slotId];
            if (item == null)
                return;
            if (Settings.DEBUG)
                Logger.getGlobal().info("Item ID: " + item.getId() + ".");
            int price = getShopBuyPrice(item);
            player.getTemporaryAttributtes().put("ShopSelectedSlot", slotId);
            player.getPackets().sendConfig(299, generalStock != null ? 139 : MAIN_STOCK_ITEMS_KEY);
            player.getPackets().sendConfig(306, item.getId());
            player.getPackets().sendConfig(301, item.getId() == 995 || item.getId() == 37753 ? -1 : slotId);
            player.getPackets().sendGlobalString(2361, ItemExaminesDataParser.getGEExamine(new Item(item.getId(), item.getAmount())));
            player.getPackets().sendGlobalConfig(1876, getSubDescription(player, new Item(item.getId(), item.getAmount())));
            player.getPackets().sendIComponentText(1265, 137, "" + price);
            player.getPackets().sendHideIComponent(1265, 136, true);

            if (currency.getItemId() == -1) {
                sendItemInfo(player, item);
            } else {
                player.getPackets().sendGameMessage(item.getDefinitions().getName() + ": shop will " + "sell for " + Utils.getFormattedNumber(price) + " " + ItemDefinitions.getItemDefinitions(currency.getItemId()).getName().toLowerCase() + ".");
            }
        } else if (!isBuying) {
            Item item = player.getInventory().getItem(slotId);
            if (item == null)
                return;
            int price = getShopSellPrice(item);
            player.getTemporaryAttributtes().put("SellSelectedSlot", slotId);
            player.getPackets().sendConfig(299, generalStock != null ? 139 : MAIN_STOCK_ITEMS_KEY);
            player.getPackets().sendConfig(306, item.getId());
            player.getPackets().sendConfig(301, item.getId() == 995 || item.getId() == 37753 ? -1 : slotId);
            player.getPackets().sendGlobalString(2361, ItemExaminesDataParser.getGEExamine(new Item(item.getId(), item.getAmount())));
            player.getPackets().sendGlobalConfig(1876, getSubDescription(player, new Item(item.getId(), item.getAmount())));
            player.getPackets().sendGameMessage(item.getAmount() + " x " + item.getDefinitions().getName() + ": shop will " + (isBuying ? "sell" : "buy") + " for " + Utils.getFormattedNumber(price) + " " + ItemDefinitions.getItemDefinitions(currency.getItemId()).getName().toLowerCase() + ".");
        }
    }

    public void sendInventory(Player player) {
        player.getInterfaceManager().sendInventoryInterface(1266);
        player.getPackets().sendItems(93, player.getInventory().getItems());
        player.getPackets().sendUnlockIComponentOptionSlots(1266, 0, 0, 28, 0, 1, 2, 3, 4, 5);
        player.getPackets().sendInterSetItemsOptionsScript(1266, 0, 93, 4, 7, "Value", "Sell-1", "Sell-5", "Sell-10", "Sell-X", "Examine");
        refreshLastX();
    }

    public void sendStore(Player player) {
        ShopItem[] stock = new ShopItem[mainStock.length + (generalStock != null ? generalStock.length : 0)];
        for (int i = 0; i < mainStock.length; i++) {
            stock[i] = new ShopItem(mainStock[i].getId(), mainStock[i].getAmount(), mainStock[i].getPrice());
        }
        if (generalStock != null) { // todo this is place where mainstock becomes null most likely
            for (int i = mainStock.length; i < stock.length; i++) {
                stock[i] = generalStock[i - (mainStock.length)];
            }
        }
        Item[] items = new Item[stock.length];
        for (int i = 0; i < items.length; i++) {
            items[i] = stock[i] == null ? null : new Item(stock[i].getId(), stock[i].getAmount());
            if(items[i] != null && stock[i].getAmount() == 0)
                items[i].setAmount(0);
        }
        player.getPackets().sendItems(generalStock != null ? 139 : MAIN_STOCK_ITEMS_KEY, items);
    }

    /**
     * Sends Shop item value.
     *
     * @param player The player to send to.
     * @param slotId The Shops item slotID.
     */
    public void sendValue(Player player, int slotId) {
        if (player.getInventory().getItemsContainerSize() < slotId)
            return;
        Item item = player.getInventory().getItem(slotId);
        if (item == null)
            return;
        if (!ItemConstants.isTradeable(item) || item.getId() == 995 || item.getId() == 37753 || item.getId() == currency.getItemId()) {
            player.getPackets().sendGameMessage(item.getDefinitions().getName() + " cannot be sold to shops.");
            return;
        }
        int dq = getDefaultQuantity(item.getId());
        if (dq == -1 && generalStock == null && currency != ShopCurrency.CHIMES) {
            player.getPackets().sendGameMessage("You can't sell " + item.getDefinitions().getName() + " to this shop.");
            return;
        }
        if (currency == ShopCurrency.CHIMES) {
            if (!ChimesBuyables.BUYABLES.containsKey(item.getId())) {
                player.sendMessage("You can't sell this item to this shop.");
                return;
            }
        }
        int price = getShopSellPrice(item);
        if (currency == ShopCurrency.GP || currency == ShopCurrency.CHIMES)
            player.sendMessage((item.getAmount() > 1 ? (Utils.formatNumber(item.getAmount()) + " x ") : "") + item.getDefinitions().getName() + ": shop will buy for: " + Utils.getFormattedNumber(price) + " " + ItemDefinitions.getItemDefinitions(currency.getItemId()).getName().toLowerCase() + " each; right-click to sell.");

    }

    private int getShopBuyPrice(ShopItem item) {
        if (item == null) {
            return 1;
        }
        if (currency == ShopCurrency.GP) {
            return getCacheShopPrice(item.getId());
        }
        return Math.max(1, item.getPrice());
    }

    private int getShopSellPrice(Item item) {
        if (item == null) {
            return 1;
        }
        int itemId = getCachePriceItemId(item);
        if (currency == ShopCurrency.CHIMES) {
            Integer price = ChimesBuyables.BUYABLES.get(itemId);
            return price == null ? 1 : Math.max(1, price);
        }
        if (currency == ShopCurrency.GP) {
            return getGpShopBuybackPrice(itemId);
        }
        int price = getCacheShopPrice(itemId);
        for (ShopItem shopItem : mainStock) {
            if (shopItem != null && shopItem.getId() == itemId) {
                price = shopItem.getPrice();
                break;
            }
        }
        return Math.max(1, price);
    }

    private static int getCacheShopPrice(int itemId) {
        return Math.max(1, EconomyPrices.getPrice(itemId));
    }

    private static int getGpShopBuybackPrice(int itemId) {
        long price = (long) getCacheShopPrice(itemId) * GP_SHOP_BUYBACK_NUMERATOR / GP_SHOP_BUYBACK_DENOMINATOR;
        return price > Integer.MAX_VALUE ? Integer.MAX_VALUE : Math.max(1, (int) price);
    }

    private static int getCachePriceItemId(Item item) {
        if (item.getDefinitions().isNoted() && item.getDefinitions().getCertId() > 0) {
            return item.getDefinitions().getCertId();
        }
        return item.getId();
    }

    private static int getSubDescription(Player player, Item item) {
        ItemDefinitions defs = item.getDefinitions();
        switch (defs.getCSOpcode(2195)) {
            case 12:
                return 33;// Edible Food
            case 6:
                return 34;// Raw Food
            case 35:
                return player.getToolBelt().contains(item.getId()) ? 31
                        : 32;
            case 32:
                return 36;// Runes
            default:// There is another way to do this, but this is the best and
                // simplest
                int slot = defs.getEquipSlot();
                if (slot != -1) {
                    switch (slot) {
                        case Equipment.SLOT_HAT:
                            return 0;
                        case Equipment.SLOT_CAPE:
                            return 1;
                        case Equipment.SLOT_CHEST:
                            return 3;
                        case Equipment.SLOT_LEGS:
                            return 4;
                        case Equipment.SLOT_HANDS:
                            return 5;
                        case Equipment.SLOT_FEET:
                            return 6;
                        case Equipment.SLOT_AMULET:
                            return 2;
                        case Equipment.SLOT_RING:
                            return 7;
                        case Equipment.SLOT_ARROWS:
                            return 8;
                        case Equipment.SLOT_AURA:
                            return 60;
                        case Equipment.SLOT_SHIELD:
                            return 9;
                        case Equipment.SLOT_WEAPON:
                            if (defs.isMeleeTypeWeapon())
                                return Equipment.isTwoHandedWeapon(item) ? 14 : 11;
                            else if (defs.isRangeTypeWeapon())
                                return Equipment.isTwoHandedWeapon(item) ? 15 : 12;
                            else if (defs.isMagicTypeWeapon())
                                return Equipment.isTwoHandedWeapon(item) ? 16 : 13;
                    }
                }
                return -1;
        }
    }
}
