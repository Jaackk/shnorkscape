package com.rs.game.player.content.grandExchange;

import com.google.common.base.Stopwatch;
import com.rs.cache.loaders.ItemDefinitions;
import com.rs.game.item.Item;
import com.rs.game.player.DataInterface;
import com.rs.game.player.Player;
import com.rs.game.player.content.items.VirtualValues;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Colors;
import com.rs.utils.InputStringEvent;
import com.rs.utils.SerializableFilesManager;
import com.rs.utils.Utils;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GrandExchange {

    private static final Object LOCK = new Object();
    private static HashMap<Long, Offer> OFFERS;
    private static ArrayList<OfferHistory> OFFERS_TRACK;
    private static HashMap<Integer, Integer> PRICES;

    private static boolean edited;

    public static final int MAX_GE_PRICE = 2_000;
    // GrandExchange.java (top of class)
    private static final boolean DYNAMIC_PRICING = false;
    private static final boolean INSTANT_GUIDE_PRICE_FULFILLMENT = false;


    public static void viewLastOffer(Player player) {
        if (player.offerSearchCooldown != null && player.offerSearchCooldown.isRunning() &&player.offerSearchCooldown.elapsed().toMillis() < 5000) {
            Dialogue.sendSingleDialogue(player, "Please wait a bit before searching again.");
            return;
        }
        if (player.offerSearchCooldown == null) {
            player.offerSearchCooldown = Stopwatch.createUnstarted();
        }
        if (player.lastSearchedOffer == null) {
            viewOffersFor(player);
        } else {
            viewOffer(player, player.lastSearchedOffer);
        }
    }

    public static void viewOffersFor(Player player) {
        if (player.offerSearchCooldown != null && player.offerSearchCooldown.isRunning() &&player.offerSearchCooldown.elapsed().toMillis() < 5000) {
            Dialogue.sendSingleDialogue(player, "Please wait a bit before searching again.");
            return;
        }
        if (player.offerSearchCooldown == null) {
            player.offerSearchCooldown = Stopwatch.createUnstarted();
        }
        player.sendInputString("Type the item name", new InputStringEvent() {
            @Override
            public void run(Player player) {
                player.lock();
                player.lastSearchedOffer = getString().toLowerCase().trim();
                viewOffer(player, player.lastSearchedOffer);
            }
        });
    }


    public static void loadPrices() {
        PRICES = DYNAMIC_PRICING ? SerializableFilesManager.loadGEPrices() : new HashMap<Integer, Integer>();
        if (PRICES == null) {
            PRICES = new HashMap<>();
        }
    }

    private static void viewOffer(Player player, String itemName) {
        List<Offer> offerList = new ArrayList<>();
        for (Offer offer : OFFERS.values()) {
            if(offer.isCompleted())
                continue;
            ItemDefinitions def = ItemDefinitions.getItemDefinitions(offer.getId());
            if (def.name.equalsIgnoreCase(itemName)) {
                offerList.add(offer);
            }
        }
        player.offerSearchCooldown.reset().start();
        if (offerList.isEmpty()) {
            Dialogue.sendSingleDialogue(player, "There were no offers found for item '" + itemName + "'.");
        } else {
            DataInterface inter = new DataInterface(itemName + " ~ Offers");
            for (Offer o : offerList) {
                String color = o.isBuying() ? Colors.DARK_GREEN : Colors.DARK_RED;
                inter.add((o.isBuying() ? color + "BUYING" : color + "SELLING") + "</col> ~ " + o.getName() + "(x" + o.getAmount() + ") @ " + color + Utils.formatPrice(o.getPrice())+ "</col> ea");
            }
            inter.show(player);
        }
        WorldTasksManager.schedule(new WorldTask() {
            @Override
            public void run() {
                player.unlock();
                stop();
            }
        }, 2);
    }

    // On startup: just load; don't mutate prices in static mode
    public static void init() {
        OFFERS       = SerializableFilesManager.loadGEOffers();
        OFFERS_TRACK = SerializableFilesManager.loadGEHistory();
        PRICES       = DYNAMIC_PRICING ? SerializableFilesManager.loadGEPrices() : new HashMap<Integer, Integer>();

        if (OFFERS == null)       OFFERS = new HashMap<>();
        if (OFFERS_TRACK == null) OFFERS_TRACK = new ArrayList<>();
        if (PRICES == null)       PRICES = new HashMap<>();

        // If UnlimitedGEReader modifies prices, guard it too:
        if (DYNAMIC_PRICING) {
            UnlimitedGEReader.init();
            // Optionally: recalcPrices();
        }
    }


    // Only recalc/clear prices if dynamic pricing is enabled
    public static void reset(boolean track, boolean price) {
        if (track) OFFERS_TRACK.clear();
        if (price) {
            if (DYNAMIC_PRICING) {
                recalcPrices();
            } else {
                PRICES = new HashMap<>();
            }
        }
    }


    public static void recalcPrices() {
        // If you're running static prices, skip any mutation.
        if (!DYNAMIC_PRICING) {
            return;
        }

        if (OFFERS_TRACK == null || OFFERS_TRACK.isEmpty()) {
            // Nothing to recalc; still ensure the price file exists.
            savePrices();
            return;
        }

        ArrayList<OfferHistory> track = new ArrayList<>(OFFERS_TRACK);
        HashMap<Integer, BigInteger> totalPrice = new HashMap<>();
        HashMap<Integer, BigInteger> totalQty   = new HashMap<>();

        for (OfferHistory o : track) {
            if (o == null) continue;
            int id = o.getId();
            totalPrice.put(id, totalPrice.getOrDefault(id, BigInteger.ZERO).add(BigInteger.valueOf(o.getPrice())));
            totalQty.put(id,   totalQty.getOrDefault(id,   BigInteger.ZERO).add(BigInteger.valueOf(o.getQuantity())));
        }

        for (int id : totalPrice.keySet()) {
            BigInteger price = totalPrice.get(id);
            BigInteger quantity = totalQty.get(id);
            if (quantity == null || quantity.signum() <= 0) continue;

            long oldPrice = getPrice(id);
            long newPrice = price.divide(quantity).longValue();

            long min = (long) (oldPrice * 0.95) - 1;
            long max = (long) (oldPrice * 1.05) + 1;

            if (newPrice < min) newPrice = min;
            else if (newPrice > max) newPrice = max;

            if (newPrice < 1) newPrice = 1;
            else if (newPrice > Integer.MAX_VALUE) newPrice = Integer.MAX_VALUE;

            int shopValue = ItemDefinitions.getItemDefinitions(id).value;
            if (newPrice < shopValue) newPrice = shopValue;

            PRICES.put(id, (int) newPrice);
        }

        VirtualValues.setValues();
        savePrices();
    }


    public static void savePrices() {
        if (!DYNAMIC_PRICING) {
            return;
        }
        synchronized (LOCK) {
            SerializableFilesManager.saveGEPrices(PRICES);
        }
    }

    public static final void save() {
        if (!edited)
            return;
        SerializableFilesManager.saveGEOffers(OFFERS);
        SerializableFilesManager.saveGEHistory(OFFERS_TRACK);
        edited = false;
    }

    public static void linkOffers(Player player) {
        boolean itemsWaiting = false;
        for (int slot = 0; slot < player.getGEManager().getOfferUIds().length; slot++) {
            Offer offer = getOffer(player, slot);
            if (offer == null)
                continue;
            offer.link(slot, player, System.currentTimeMillis());
            offer.update();
            if (!itemsWaiting && offer.hasItemsWaiting()) {
                itemsWaiting = true;
                player.getPackets().sendGameMessage(Colors.CYAN + "You have items from the Grand Exchange waiting in your collection box.");
            }
        }
    }

    public static Offer getOffer(Player player, int slot) {
        synchronized (LOCK) {
            long uid = player.getGEManager().getOfferUIds()[slot];
            if (uid == 0)
                return null;
            Offer offer = OFFERS.get(uid);
            if (offer == null) {
                player.getGEManager().getOfferUIds()[slot] = 0;
                return null;
            }
            return offer;
        }
    }

    public static void sendOffer(Player player, int slot, int itemId, int amount, int price, boolean buy) {
        if (price > 1000000000) {
            player.getPackets().sendGameMessage(
                    "You cannot " + (buy ? "buy" : "sell") + " items for more than 1,000,000,000 coins each on the Grand Exchange.");
            return;
        }

        synchronized (LOCK) {
            Offer offer = new Offer(itemId, amount, price, buy);
            player.getGEManager().getOfferUIds()[slot] = createOffer(offer);
            offer.link(slot, player, System.currentTimeMillis());

            if (offer.isBuying()) {
                if (INSTANT_GUIDE_PRICE_FULFILLMENT && offer.getPrice() >= Math.round(GrandExchange.getPrice(itemId) * 1.0) && GrandExchange.getPrice(itemId) != 1) {
                    if (getBestOffer(offer) == null)
                        offer.buyOffer(offer);
                }
                findBuyerSeller(offer, itemId, amount, price, buy);
            } else {
                if (INSTANT_GUIDE_PRICE_FULFILLMENT && offer.getPrice() <= Math.round(GrandExchange.getPrice(itemId) * 1.0) && GrandExchange.getPrice(itemId) != 1) {
                    if (getBestOffer(offer) == null)
                        offer.sellOffer();
                }
                findBuyerSeller(offer, itemId, amount, price, buy);
            }
        }
    }


    public static void abortOffer(Player player, int slot) {
        synchronized (LOCK) {
            Offer offer = getOffer(player, slot);
            if (offer == null)
                return;
            edited = true;
            if (offer.cancel() && offer.forceRemove()) {
                deleteOffer(player, slot); // shouldnt here happen anyway
            }
        }
    }

    public static void collectItems(Player player, int slot, int invSlot, int option) {
        synchronized (LOCK) {
            Offer offer = getOffer(player, slot);
            if (offer == null)
                return;
            edited = true;
            if (offer.collectItems(invSlot, option) && offer.forceRemove()) {
                deleteOffer(player, slot); // should happen after none left and
                // offer completed
                if (offer.getTotalAmountSoFar() != 0) {
                    OfferHistory o = new OfferHistory(offer.getId(), offer.getTotalAmountSoFar(), offer.getTotalPriceSoFar(), offer.isBuying());
                    try {
                        OFFERS_TRACK.add(new OfferHistory(offer.getId(), offer.getTotalAmountSoFar(), offer.getTotalPriceSoFar(), offer.isBuying()));
                    } catch (NullPointerException e) {
                    }
                    player.getGEManager().addOfferHistory(o);
                }
            }
        }
    }

    private static void deleteOffer(Player player, int slot) {
        player.getGEManager().cancelOffer(); // sends back to original screen if
        // seeing an offer
        OFFERS.remove(player.getGEManager().getOfferUIds()[slot]);
        player.getGEManager().getOfferUIds()[slot] = 0;
    }

    private static Offer getBestOffer(Offer offer) {
        Offer bestOffer = null;
        for (Offer o : OFFERS.values()) {
            if (o.isBuying() == offer.isBuying() || o.getId() != offer.getId() || o.isCompleted() || (offer.isBuying() && o.getPrice() > offer.getPrice()) || (!offer.isBuying() && o.getPrice() < offer.getPrice()) || offer.isOfferTooHigh(o))
                continue;
            if (bestOffer == null || (offer.isBuying() && o.getPrice() < bestOffer.getPrice()) || (!offer.isBuying() && o.getPrice() > bestOffer.getPrice()))
                bestOffer = o;
        }
        return bestOffer;
    }


    private static void findBuyerSeller(Offer offer, int itemId, int amount, int price, boolean buy) {
        while (!offer.isCompleted()) {
            Offer bestOffer = null;
            for (Offer o : OFFERS.values()) {
                if (o.isBuying() == offer.isBuying() || o.getId() != offer.getId() || o.isCompleted() || (offer.isBuying() && o.getPrice() > offer.getPrice()) || (!offer.isBuying() && o.getPrice() < offer.getPrice()) || offer.isOfferTooHigh(o))
                    continue;
                if (bestOffer == null || (offer.isBuying() && o.getPrice() < bestOffer.getPrice()) || (!offer.isBuying() && o.getPrice() > bestOffer.getPrice()))
                    bestOffer = o;
            }
            if (bestOffer == null)
                break;
            offer.updateOffer(bestOffer);
        }
        offer.update();
    }


    private static long createOffer(Offer offer) {
        edited = true;
        long uid = getUId();
        OFFERS.put(uid, offer);
        return uid;
    }

    private static long getUId() {
        while (true) {
            long uid = Utils.RANDOM.nextLong();
            if (OFFERS.containsKey(uid))
                continue;
            return uid;
        }
    }

    public static int getPrice(int id) {
        if (!DYNAMIC_PRICING) {
            return getCachePrice(id);
        }
        synchronized (LOCK) {
            int priceId = getCachePriceItemId(id);
            Integer price = PRICES == null ? null : PRICES.get(priceId);
            return price != null && price > 0 ? price : getCachePrice(priceId);
        }
    }

    public static boolean usesCachePrices() {
        return !DYNAMIC_PRICING;
    }

    public static void unlinkOffers(Player player) {
        for (int slot = 0; slot < player.getGEManager().getOfferUIds().length; slot++) {
            Offer offer = getOffer(player, slot);
            if (offer == null)
                continue;
            offer.unlink();
        }
    }

    public static List<OfferHistory> getHistory() {
        return OFFERS_TRACK;
    }

    public static int getTotalBuyQuantity(int itemId) {
        int quantity = 0;
        for (Offer offer : OFFERS.values()) {
            if (!offer.isBuying() || offer.getId() != itemId || offer.isCompleted())
                continue;
            quantity += offer.getAmount() - offer.getTotalAmountSoFar();
        }
        return quantity;
    }

    public static int getTotalSellQuantity(int itemId) {
        synchronized (LOCK) {
            int quantity = 0;

            for (Offer offer : OFFERS.values()) {
                if (offer.isBuying() || offer.getId() != itemId || offer.isCompleted())
                    continue;
                quantity += offer.getAmount() - offer.getTotalAmountSoFar();
            }

            return quantity;
        }
    }

    public static Integer getGEPriceByName(String itemName) {
        if (itemName == null) {
            return null;
        }
        for (int id = 0; id < Utils.getItemDefinitionsSize(); id++) {
            ItemDefinitions defs = ItemDefinitions.getItemDefinitions(id);
            if (defs != null && itemName.equalsIgnoreCase(defs.getName())) {
                return getPrice(id);
            }
        }
        return null;
    }


    public static int getBestBuyPrice(int itemId) {
        return getPrice(itemId);
    }

    public static int getBuyQuantity(int itemId) {
        int quantity = 0;
        for (Offer offer : OFFERS.values()) {
            if (!offer.isBuying() || offer.getId() != itemId || offer.isCompleted())
                continue;
            if (offer.getPrice() == getBestBuyPrice(itemId))
                quantity += offer.getAmount() - offer.getTotalAmountSoFar();
        }
        return quantity;
    }








    public static int getCheapestSellPrice(int itemId) {
        return getPrice(itemId);
    }

    public static int getSellQuantity(int itemId) {
        int quantity = 0;
        for (Offer offer : OFFERS.values()) {
            if (offer.isBuying() || offer.getId() != itemId || offer.isCompleted())
                continue;
            if (offer.getPrice() == getCheapestSellPrice(itemId))
                quantity += offer.getAmount() - offer.getTotalAmountSoFar();
        }
        return quantity;
    }

    public static void setPrice(int id, int price) {
        if (price < 0) price = 0;
        if (!DYNAMIC_PRICING) {
            return;
        }
        synchronized (LOCK) {
            if (PRICES == null) {
                PRICES = new HashMap<>();
            }
            PRICES.put(getCachePriceItemId(id), price);
        }
    }

    public static int getPrice(Item item) {
        return getPrice(item.getId());
    }

    private static int getCachePrice(int id) {
        if (id == 995) {
            return 1;
        }
        ItemDefinitions defs = ItemDefinitions.getItemDefinitions(id);
        if (defs == null) {
            return 1;
        }
        if (defs.isNoted() && defs.getCertId() > 0 && defs.getCertId() != id) {
            return getCachePrice(defs.getCertId());
        }
        if (defs.isLended() && defs.getLendId() > 0 && defs.getLendId() != id) {
            return getCachePrice(defs.getLendId());
        }
        return Math.max(1, defs.getValue());
    }

    private static int getCachePriceItemId(int id) {
        ItemDefinitions defs = ItemDefinitions.getItemDefinitions(id);
        if (defs == null) {
            return id;
        }
        if (defs.isNoted() && defs.getCertId() > 0 && defs.getCertId() != id) {
            return getCachePriceItemId(defs.getCertId());
        }
        if (defs.isLended() && defs.getLendId() > 0 && defs.getLendId() != id) {
            return getCachePriceItemId(defs.getLendId());
        }
        return id;
    }
}
