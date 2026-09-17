package com.rs.game.player.content.trade;

import com.rs.game.World;
import com.rs.game.item.Item;
import com.rs.game.item.ItemsContainer;
import com.rs.game.player.Player;
import com.rs.game.player.content.ItemConstants;
import com.rs.game.player.content.grandExchange.GrandExchange;
import com.rs.network.packet.PacketRepository;
import com.rs.utils.Colors;
import com.rs.utils.EconomyPrices;
import com.rs.utils.InputIntegerEvent;
import com.rs.utils.Logger;
import com.rs.utils.SerializableFilesManager;
import com.rs.utils.Utils;
import com.rs.utils.data.parsers.items.ItemExaminesDataParser;

import java.util.concurrent.CopyOnWriteArrayList;

public abstract class ItemTransaction {
    protected Player player, target;
    protected ItemsContainer<Item> items;
    private boolean transactionModified, accepted;
    
    public static final int PLAYER_OFFER_COMPONENT_ID = 14;
    public static final int OTHER_OFFER_COMPONENT_ID = 17;
    
    public ItemTransaction(Player player) {
        this.player = player;
        items = new ItemsContainer<>(28, false);
    }

    public abstract boolean canAccept(boolean firstStage);

    public Player getTarget() {
        return target;
    }

    public void accept(boolean firstStage) {
        try {
            synchronized (this) {
                if(target == null)
                    return;
                synchronized (target.getItemTransaction()) {
                    if (!canAccept(firstStage)) {
                        return;
                    }
                    if (target.getItemTransaction().accepted) {
                        for (Item item : target.getItemTransaction().items.getItems()) {
                            if (item == null) {
                                continue;
                            }
                            if (item.getId() != 995) {
                                if (player.getInventory().getAmountOf(item.getId()) + item.getAmount() < 0) {
                                    player.setCloseInterfacesEvent(null);
                                    player.closeInterfaces();
                                    closeTransaction(CloseTransactionStage.NO_SPACE);
                                    break;
                                }
                                continue;
                            }
                            if (player.getInventory().getAmountOf(item.getId()) + item.getAmount() < 0) {
                                if (item.getId() == 995) {
                                    if (player.getMoneyPouch().getTotal() + item.getAmount() < 0) {
                                        player.setCloseInterfacesEvent(null);
                                        player.closeInterfaces();
                                        closeTransaction(CloseTransactionStage.NO_SPACE);
                                        break;
                                    }
                                }
                            }
                        }
                        if (firstStage) {
                            if (nextStage()) {
                                target.getItemTransaction().nextStage();
                            }
                        } else {
                            player.setCloseInterfacesEvent(null);
                            player.closeInterfaces();
                            closeTransaction(CloseTransactionStage.DONE);
                        }
                        return;
                    }
                    accepted = true;
                    refreshBothStageMessage(firstStage);
                }
            }
        } catch (Exception e) {
            Logger.getGlobal().catching(e);
        }
    }

    public abstract boolean canAddItem(Item item);

    public void addItem(int slot, int amount) {
        synchronized (this) {
            synchronized (target.getItemTransaction()) {
                Item item = player.getInventory().getItem(slot);
                if (item == null) {
                    return;
                }
                if (!canAddItem(item)) {
                    player.getPackets().sendGameMessage("That item cannot be added.");
                    return;
                }
                Item[] itemsBefore = items.getItemsCopy();
                int maxAmount = player.getInventory().getItems().getNumberOf(item);
                if (amount < maxAmount) {
                    item = new Item(item.getId(), amount).setAttributes(item.getAttributes());
                } else {
                    item = new Item(item.getId(), maxAmount).setAttributes(item.getAttributes());
                }
                if (item.getAmount() + items.getNumberOf(item) < 0 || item.getAmount() + player.getItemTransaction().items.getNumberOf(item) < 0) {
                    return;
                }
                items.add(item);
                player.getInventory().deleteItem(slot, item);
                refreshItems(itemsBefore);
                cancelAccepted();
            }
        }
    }

    public void addPouch(int amount) {
        synchronized (this) {
            synchronized (target.getItemTransaction()) {
                Item[] itemsBefore = items.getItemsCopy();
                if (amount >= player.getMoneyPouch().getTotal()) {
                    amount = player.getMoneyPouch().getTotal();
                }
                Item item = new Item(995, amount);
                if (item.getAmount() + items.getNumberOf(item) < 0 || item.getAmount() + player.getItemTransaction().items.getNumberOf(item) < 0) {
                    return;
                }
                if (player.getMoneyPouch().removeMoneyMisc(amount)) {
                    items.add(item);
                    refreshItems(itemsBefore);
                    cancelAccepted();
                } else {
                    closeTransaction(CloseTransactionStage.CANCEL);
                }
            }
        }
    }

    public void cancelAccepted() {
        boolean canceled = false;
        if (accepted) {
            accepted = false;
            canceled = true;
        }
        if (target.getItemTransaction().accepted) {
            target.getItemTransaction().accepted = false;
            canceled = true;
        }
        if (canceled)
            refreshBothStageMessage(canceled);
    }

    public void closeTransaction(CloseTransactionStage stage) {
        synchronized (this) {
            if (target == null) {
                return;
            }
            synchronized (target.getItemTransaction()) {
                Player oldTarget = target;
                target = null;
                transactionModified = false;
                accepted = false;
                if (CloseTransactionStage.DONE != stage) {
                    if (shouldReturnOfferedItems(stage)) {
                        for (Item item : player.getItemTransaction().items.getItems()) {
                            if (item == null) {
                                continue;
                            }
                            if (item.getId() == 995) {
                                player.getMoneyPouch().addMoneyMisc(item.getAmount());
                                continue;
                            }
                            if (player.getInventory().hasFreeSlots()) {
                                player.getInventory().addItem(item);
                            } else {
                                World.addGroundItem(item, player, player, true, 60);
                                player.sendMessage(Colors.RED + "[WARNING]:</col> Your " + item.getName() + " has been dropped due to insufficient inventory space.");
                            }
                        }
                    }
                    player.getInventory().init();
                    items.clear();
                } else {
                    CopyOnWriteArrayList<Item> containedItems = new CopyOnWriteArrayList<>();
                    for (Item item : oldTarget.getItemTransaction().items.getItems()) {
                        if (item == null) {
                            continue;
                        }
                        containedItems.add(item);
                    }
                    postSuccessAction(oldTarget, containedItems);
                    player.getInventory().init();
                    if (shouldSavePlayerAfterTransaction()) {
                        SerializableFilesManager.savePlayer(player);
                    }
                    oldTarget.getItemTransaction().items.clear();
                }
                if (oldTarget.getItemTransaction().isInTransaction()) {
                    oldTarget.setCloseInterfacesEvent(null);
                    oldTarget.closeInterfaces();
                    oldTarget.getItemTransaction().closeTransaction(stage);
                    postTradeAction(oldTarget, stage);
                    if (CloseTransactionStage.CANCEL == stage) {
                        oldTarget.getPackets().sendGameMessage("<col=ff0000>Other player declined trade!");
                    } else if (CloseTransactionStage.NO_SPACE == stage) {
                        player.sendMessage("Inventory full. To make room, sell, drop or bank something.");
                        oldTarget.sendMessage("Other player doesn't have enough space in their inventory for this trade.");
                    }
                }
            }
        }
    }

    public abstract void postTradeAction(Player oldTarget, CloseTransactionStage stage);

    public abstract void postSuccessAction(Player oldTarget, CopyOnWriteArrayList<Item> containedItems);

    protected boolean shouldReturnOfferedItems(CloseTransactionStage stage) {
        return true;
    }

    protected boolean shouldSavePlayerAfterTransaction() {
        return !player.isBot();
    }

    public String getAcceptMessage(boolean firstStage) {
        if (accepted)
            return "Waiting for other player...";
        if (target.getItemTransaction().accepted)
            return "Other player has accepted.";
        return firstStage ? "" : "Are you sure you want to make this trade?";
    }

    public int getTradeWealth() {
        long wealth = 0;
        for (Item item : items.getItems()) {
            if (item == null || item.getAmount() <= 0)
                continue;
            int price = GrandExchange.getPrice(item.getId());
            if (price <= 0) {
                continue;
            }
            wealth += (long) price * item.getAmount();
            if (wealth > Integer.MAX_VALUE) {
                return Integer.MAX_VALUE;
            }
        }
        return (int) wealth;
    }

    public void handleButtons(Player player, int interfaceId, int packetId, int componentId, int slotId) {
        if (interfaceId == 334) {
            if (componentId == 49) {
                this.closeTransaction(CloseTransactionStage.CANCEL);
                player.closeInterfaces();
            } else if (componentId == 44) {
                player.getItemTransaction().accept(false);
            }
        } else if (interfaceId == 335) {
            if (componentId == 48) {
                player.getItemTransaction().accept(true);
            } else if (componentId == 27) {
                for (int x = 0; x < 28; x++)
                    player.getItemTransaction().addItem(x, Integer.MAX_VALUE);
            } else if (componentId == 50) {
                this.closeTransaction(CloseTransactionStage.CANCEL);
                player.closeInterfaces();
            } else if (componentId == 29) {
                if (player.getItemTransaction().items.getUsedSlots() == 28 && !player.getItemTransaction().items.contains(new Item(995))) {
                    player.sm(Colors.RED + "You do not have room in the trade. " + player.getItemTransaction().items.getUsedSlots());
                    return;
                }
                player.sendInputInteger("Your money pouch contains " + Utils.getFormattedNumber(player.getMoneyPouch().getTotal()) + " coins.<br>" + "How many would you like to add?", new InputIntegerEvent() {
                    @Override
                    public void run(Player player) {
                        Integer trade_X_money = 995;
                        int value = getInteger();
                        if (value <= 0 || trade_X_money == null || player.getMoneyPouch().getTotal() == 0) {
                            return;
                        }
                        if (value >= player.getMoneyPouch().getTotal()) {
                            value = player.getMoneyPouch().getTotal();
                        }
                        if (canAddItem(new Item(995, value))) {
                            player.getItemTransaction().addPouch(value);
                        }
                    }
                });
            } else if (componentId == 14) {
                if (packetId == PacketRepository.ACTION_BUTTON1_PACKET) {
                    player.getItemTransaction().removeItem(slotId, 1);
                } else if (packetId == PacketRepository.ACTION_BUTTON2_PACKET) {
                    player.getItemTransaction().removeItem(slotId, 5);
                } else if (packetId == PacketRepository.ACTION_BUTTON3_PACKET) {
                    player.getItemTransaction().removeItem(slotId, 10);
                } else if (packetId == PacketRepository.ACTION_BUTTON4_PACKET) {
                    player.getItemTransaction().removeItem(slotId, Integer.MAX_VALUE);
                } else if (packetId == PacketRepository.ACTION_BUTTON5_PACKET) {
                    player.sendInputInteger("Enter Amount:", new InputIntegerEvent() {
                        @Override
                        public void run(Player player) {
                            final int value = getInteger();
                            if (value < 0)
                                return;
                            player.getItemTransaction().removeItem(slotId, value);
                        }
                    });
                } else if (packetId == PacketRepository.ACTION_BUTTON9_PACKET) {
                    player.getItemTransaction().sendValue(slotId, false);
                } else if (packetId == PacketRepository.ACTION_BUTTON8_PACKET)
                    player.getItemTransaction().sendExamine(slotId, false);
            } else if (componentId == 17) {
                if (packetId == PacketRepository.ACTION_BUTTON1_PACKET) {
                    player.getItemTransaction().handleOtherOfferPrimary(slotId);
                } else if (packetId == PacketRepository.ACTION_BUTTON2_PACKET) {
                    player.getItemTransaction().handleOtherOfferSecondary(slotId);
                } else if (packetId == PacketRepository.ACTION_BUTTON3_PACKET) {
                    player.getItemTransaction().sendValue(slotId, true);
                } else if (packetId == PacketRepository.ACTION_BUTTON8_PACKET) {
                    player.getItemTransaction().sendExamine(slotId, true);
                }
            }
        } else if (interfaceId == 336) {
            if (componentId == 0) {
                if (packetId == PacketRepository.ACTION_BUTTON1_PACKET) {
                    player.getItemTransaction().addItem(slotId, 1);
                } else if (packetId == PacketRepository.ACTION_BUTTON2_PACKET) {
                    player.getItemTransaction().addItem(slotId, 5);
                } else if (packetId == PacketRepository.ACTION_BUTTON3_PACKET) {
                    player.getItemTransaction().addItem(slotId, 10);
                } else if (packetId == PacketRepository.ACTION_BUTTON4_PACKET) {
                    player.getItemTransaction().addItem(slotId, Integer.MAX_VALUE);
                } else if (packetId == PacketRepository.ACTION_BUTTON5_PACKET) {
                    player.sendInputInteger("Enter Amount:", new InputIntegerEvent() {
                        @Override
                        public void run(Player player) {
                            final int value = getInteger();
                            if (value < 0)
                                return;
                            player.getItemTransaction().addItem(slotId, value);
                        }
                    });
                } else if (packetId == PacketRepository.ACTION_BUTTON9_PACKET) {
                    player.getItemTransaction().sendValue(slotId);
                } else if (packetId == PacketRepository.ACTION_BUTTON8_PACKET) {
                    player.getInventory().sendExamine(slotId);
                }
            }

        }
    }

    public boolean isInTransaction() {
        return target != null && player != null;
    }

    public boolean nextStage() {
        if (target == null || player == null) {
            if (player != null) {
                player.setCloseInterfacesEvent(null);
                player.closeInterfaces();
            }
            if (target != null) {
                target.setCloseInterfacesEvent(null);
                target.closeInterfaces();
            }
            closeTransaction(CloseTransactionStage.CANCEL);
            return false;
        }
        player.getPackets().sendRunScript(1548, 0);
        target.getPackets().sendRunScript(1548, 0);
        if (!canContinueTrade()) {
            return false;
        }
        accepted = false;
        player.getInterfaceManager().sendInterface(334);
        player.getInterfaceManager().closeInventoryInterface();
        player.getPackets().sendHideIComponent(334, 19, !(transactionModified || target.getItemTransaction().transactionModified));
        refreshBothStageMessage(false);
        return true;
    }

    public abstract boolean canContinueTrade();

    /*
     * called to both players
     */
    public void openTransaction(Player target) {
        synchronized (this) {
            synchronized (target.getItemTransaction()) {
                player.stopAll(true);
                this.target = target;
                if (!canPerformTransaction()) {
                    return;
                }
                player.lock();
                target.lock();
                player.getPackets().sendItems(541, false, new Item[]{new Item(-1, -1)});
                target.getPackets().sendItems(541, true, new Item[]{new Item(-1, -1)});
//                player.getPackets().sendGlobalString(2504, target.getDisplayName());
                sendInterItems();
                sendOptions();
                sendTradeModified();
                refreshTradeWealth();
                refreshStageMessage(true);
                player.getInterfaceManager().sendInventoryInterface(336);
                player.getInterfaceManager().sendInterface(335);
                openTransactionAction();
                player.setCloseInterfacesEvent(() -> closeTransaction(CloseTransactionStage.CANCEL));
            }
        }
    }

    public abstract void openTransactionAction();

    public abstract boolean canPerformTransaction();

    public void refresh(int... slots) {
        player.getPackets().sendUpdateItems(90, items, slots);
        target.getPackets().sendUpdateItems(90, true, items.getItems(), slots);
    }

    public void refreshBothStageMessage(boolean firstStage) {
        refreshStageMessage(firstStage);
        target.getItemTransaction().refreshStageMessage(firstStage);
    }

    public void refreshItems(Item[] itemsBefore) {
        int[] changedSlots = new int[itemsBefore.length];
        int count = 0;
        for (int index = 0; index < itemsBefore.length; index++) {
            Item item = items.getItems()[index];
            if (itemsBefore[index] != item) {
                if (itemsBefore[index] != null && (item == null || item.getId() != itemsBefore[index].getId() || item.getAmount() < itemsBefore[index].getAmount())) {
                    sendFlash(index);
                }
                changedSlots[count++] = index;
            }
        }
        int[] finalChangedSlots = new int[count];
        System.arraycopy(changedSlots, 0, finalChangedSlots, 0, count);
        refresh(finalChangedSlots);
        refreshTradeWealth();

        postAddItem();
    }

    public abstract void postAddItem();

    private void refreshStageMessage(boolean firstStage) {
        player.getPackets().sendIComponentText(firstStage ? 335 : 334, firstStage ? 21 : 12, getAcceptMessage(firstStage));
    }

    private void refreshTradeWealth() {
        int wealth = getTradeWealth();
        player.getPackets().sendGlobalConfig(729, wealth);
        target.getPackets().sendGlobalConfig(697, wealth);
    }

    public void removeItem(final int slot, int amount) {
        synchronized (this) {
            synchronized (target.getItemTransaction()) {
                Item item = items.get(slot);
                if (item == null)
                    return;
                Item[] itemsBefore = items.getItemsCopy();
                int maxAmount = items.getNumberOf(item);
                if (amount < maxAmount)
                    item = new Item(item.getId(), amount).setAttributes(item.getAttributes());
                else
                    item = new Item(item.getId(), maxAmount).setAttributes(item.getAttributes());
                items.remove(slot, item);
                player.getInventory().addItemMoneyPouch(item);
                refreshItems(itemsBefore);
                cancelAccepted();
                setTransactionModified(true);
            }
        }
    }

    public void sendExamine(int slot, boolean traders) {
        Item item = traders ? target.getItemTransaction().items.get(slot) : items.get(slot);
        if (item == null)
            return;
        player.getPackets().sendGameMessage(ItemExaminesDataParser.getExamine(item));
        player.getPackets().sendGameMessage("Guide price: " + Utils.getFormattedNumber(GrandExchange.getPrice(item)) + ".");
    }

    public void sendFlash(int slot) {
        player.getPackets().sendInterFlashScript(335, PLAYER_OFFER_COMPONENT_ID + 1, 4, 7, slot);
        target.getPackets().sendInterFlashScript(335, OTHER_OFFER_COMPONENT_ID + 1, 4, 7, slot);
    }

    public void sendInterItems() {
        player.getPackets().sendItems(90, items);
        target.getPackets().sendItems(90, true, items);
    }

    public void sendOptions() {
        player.getPackets().sendInterSetItemsOptionsScript(336, 0, 93, 4, 7, "Offer", "Offer-5", "Offer-10",
                "Offer-All", "Offer-X", "Value<col=FF9040>");
        player.getPackets().sendIComponentSettings(336, 0, 0, 27, 1278);
        player.getPackets().sendInterSetItemsOptionsScript(335, PLAYER_OFFER_COMPONENT_ID, 90, 4, 7, "Remove", "Remove-5", "Remove-10",
                "Remove-All", "Remove-X", "Value");
        player.getPackets().sendIComponentSettings(335, PLAYER_OFFER_COMPONENT_ID, 0, 27, 1150);
        player.getPackets().sendInterSetItemsOptionsScript(335, OTHER_OFFER_COMPONENT_ID, 90, true, 4, 7, "Value");
        player.getPackets().sendIComponentSettings(335, OTHER_OFFER_COMPONENT_ID, 0, 27, 1026);
    }

    protected void handleOtherOfferPrimary(int slot) {
        sendValue(slot, true);
    }

    protected void handleOtherOfferSecondary(int slot) {
        sendValue(slot, true);
    }

    public void sendTradeModified() {
        player.getPackets().sendConfig(1042, transactionModified ? 1 : 0);
        target.getPackets().sendConfig(1043, transactionModified ? 1 : 0);
    }

    public void sendValue(int slot) {
        Item item = player.getInventory().getItem(slot);
        if (item == null)
            return;
        if (!ItemConstants.isTradeable(item)) {
            player.getPackets().sendGameMessage("That item isn't tradeable.");
            return;
        }
        int price = EconomyPrices.getPrice(item.getId());
        player.getPackets().sendGameMessage(item.getDefinitions().getName() + ": market price is " + price + " coins.");
    }

    public void sendValue(int slot, boolean traders) {
        Item item = traders ? target.getItemTransaction().items.get(slot) : items.get(slot);
        if (item == null)
            return;
        if (!ItemConstants.isTradeable(item)) {
            player.getPackets().sendGameMessage("That item isn't tradeable.");
            return;
        }
        int price = GrandExchange.getPrice(item.getId());
        player.getPackets().sendGameMessage(item.getDefinitions().getName() + ": market price is " + price + " coins.");
    }

    public void setTransactionModified(boolean modified) {
        if (modified == transactionModified)
            return;
        transactionModified = modified;
        sendTradeModified();
    }

    public enum CloseTransactionStage {
        CANCEL, NO_SPACE, DONE
    }

    public ItemsContainer<Item> getItemsContainer() {
        return items;
    }
}
