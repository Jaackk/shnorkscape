package com.rs.game.activites.gim.bank;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.Future;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.rs.cores.CoresManager;
import com.rs.game.item.Item;
import com.rs.game.player.Bank;
import com.rs.game.player.DataAnswerInterface;
import com.rs.game.player.DataInterface;
import com.rs.game.player.Player;
import com.rs.game.player.content.pet.Pets;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.Colors;
import com.rs.utils.SerializableFilesManager;

import lombok.val;

/**
 * A class representing a bank for a single GIM group.
 *
 * @author lare96 <http://github.com/lare96>
 */
public final class GIMBank implements Serializable {

    private static final long serialVersionUID = 466674593718642093L;

    static {
        try {
            Files.createDirectories(Paths.get("data", "gim", "banks"));
        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    /**
     * Determines if the item can be deposited.
     */
    public static String canDeposit(Player player, Item item) {
        int id = item.getId();
        if (Pets.getAllPetIds().contains(id)) {
            return "You cannot deposit pets into GIM banks.";
        }
        return null;
    }

    /**
     * The GSON instance.
     */
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    /**
     * The date formatter.
     */
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMMM d, uuuu");

    /**
     * The time formatter.
     */
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("h:mm a");

    /**
     * The bank.
     */
    private final Bank bank;

    /**
     * The group key.
     */
    private String groupKey;

    /**
     * The bank history.
     */
    private final LinkedList<GIMBankHistory> history = new LinkedList<>();

    /**
     * The result of the save task.
     */
    private transient Future<?> saveResult;

    /**
     * The player using this bank.
     */
    private transient Player usingBank;

    /**
     * If the bank changed.
     */
    private transient boolean bankChanged;

    /**
     * Creates a new {@link GIMBank}.
     */
    public GIMBank(String name) {
        bank = new Bank("Bank of " + name);
        groupKey = name;
    }

    /**
     * Opens this bank.
     */
    public void open(Player player) {
        if (usingBank != null && !usingBank.hasFinished()) {
            Dialogue.sendSingleDialogue(player, "Please wait until " + usingBank.getDisplayName() + " is finished using the bank.");
            return;
        }
        if (!isSaveComplete()) {
            Dialogue.sendSingleDialogue(player, "Try again in a moment. The bank is currently being saved.");
            return;
        }
        player.gimBank.setCurrentBank(this);
        usingBank = player;
        bank.setPlayer(usingBank);
        bank.openBank();
        player.setCloseInterfacesEvent(player.gimBank::close);
    }

    /**
     * Did the bank finish saving?
     */
    private boolean isSaveComplete() {
        if (saveResult == null)
            return true;
        return saveResult.isDone();
    }

    /**
     * Save the bank, if needed.
     */
    public void save() {
        if (!bankChanged) {
            return;
        }
        if (!isSaveComplete()) {
            throw new IllegalStateException("Wrong state, save not complete yet new save requested?");
        }
        saveResult = CoresManager.getServiceProvider().executeNow(() -> {
            try {
                Path filePath = GIMBankManager.getBankPath(groupKey);
                if (!Files.exists(filePath)) {
                    Files.createFile(filePath);
                }
                SerializableFilesManager.storeSerializableClass(GIMBank.this, filePath.toFile());
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        });
    }

    /**
     * Displays the bank history with dates.
     */
    public void displayHistory(Player player) {
        if (history.isEmpty()) {
            DataInterface dataInterface = new DataInterface("GIM Bank History");
            dataInterface.add(Colors.DEF_SEARCH_CYAN + "No prior history to display!");
            dataInterface.show(player);
            return;
        }

        Multimap<LocalDate, GIMBankHistory> historyOnDate = HashMultimap.create();
        for (val next : history) {
            historyOnDate.put(next.getTimestamp().toLocalDate(), next);
        }

        Map<String, Runnable> answers = new HashMap<>();
        val now = LocalDate.now();
        for (val next : historyOnDate.asMap().entrySet()) {
            val date = next.getKey();
            val entries = next.getValue();
            answers.put(getFormattedDate(now, date), () -> displayHistoryForDay(player, entries));
        }
        DataAnswerInterface answerInterface = new DataAnswerInterface("GIM Bank History", answers, () -> displayHistory(player));
        answerInterface.show(player);
    }

    /**
     * Displays the bank history for a specific day.
     */
    private void displayHistoryForDay(Player player, Collection<GIMBankHistory> entries) {
        DataInterface dataInterface = new DataInterface("GIM Bank History");
        for (val next : entries) {
            dataInterface.add(TIME_FORMATTER.format(next.getTimestamp().toLocalTime()));
            dataInterface.add(next.toFormatted());
        }
        dataInterface.show(player);
    }

    /**
     * Retrieves the formatted date for the bank history.
     */
    private String getFormattedDate(LocalDate now, LocalDate date) {
        if (now.equals(date)) { // Today
            return "Today";
        } else if (now.minusDays(1).equals(date)) { // Yesterday
            return "Yesterday";
        } else { // Generic date
            return DATE_FORMATTER.format(date);
        }
    }

    /**
     * Renames this bank.
     */
    public void rename(String oldName, String newName) {
        bank.setName("Bank of " + newName);
        setGroupKey(newName);
        if (!isUsingBank()) {
            save();
            setBankChanged(false);
        }
        GIMBankManager.getBanks().invalidate(oldName);
        GIMBankManager.getBanks().put(newName, this);
    }

    Bank getBank() {
        return bank;
    }

    void setGroupKey(String groupKey) {
        this.groupKey = groupKey;
        bankChanged = true;
    }

    LinkedList<GIMBankHistory> getHistory() {
        return history;
    }

    void setUsingBank(Player usingBank) {
        this.usingBank = usingBank;
    }

    Player getUsingBank() {
        return usingBank;
    }

    boolean isUsingBank() {
        return usingBank != null;
    }

    void setBankChanged(boolean bankChanged) {
        this.bankChanged = bankChanged;
    }

    boolean isBankChanged() {
        return bankChanged;
    }

    /* Delegate methods for banking packets. */
    public boolean depositItem(int invSlot, int quantity, boolean refresh, int componentId) {
        bankChanged = true;
        return bank.depositItem(invSlot, quantity, refresh, componentId);
    }

    public boolean withdraw(int invSlot, int quantity) {
        bankChanged = true;
        return bank.withdrawItem(invSlot, quantity);
    }

    public void collapse(int tabId) {
        bankChanged = true;
        bank.collapse(tabId);
    }

    public void depositLastAmount(int bankSlot, int componentId) {
        bankChanged = true;
        bank.depositLastAmount(bankSlot, componentId);
    }

    public void setLastX(int lastX) {
        bankChanged = true;
        bank.setLastX(lastX);
    }

    public void refreshLastX() {
        bank.refreshLastX();
    }

    public void sendExamine(int fakeSlot) {
        bank.sendExamineBankItem(fakeSlot);
    }

    public void switchItem(int fromSlot, int toSlot, int fromComponentId, int toComponentId) {
        bankChanged = true;
        bank.switchItem(fromSlot, toSlot, fromComponentId, toComponentId);
    }

    public void insertItem(int fromSlot, int toSlot, int fromComponentId, int toComponentId) {
        bankChanged = true;
        bank.insertItem(fromSlot, toSlot, fromComponentId, toComponentId);
    }

    public void switchWithdrawNotes() {
        bank.switchWithdrawNotes();
    }

    public boolean withdrawItem(int bankSlot, int quantity) {
        bankChanged = true;
        return bank.withdrawItem(bankSlot, quantity);
    }
    
    public int getDefaultInteractionAmount() {
        return bank.getDefaultInteractionAmount();
    }
    
    public boolean withdrawDefaultAmount(int bankSlot) {
        bankChanged = true;
        return bank.withdrawDefaultAmount(bankSlot);
    }
    
    public void withdrawItemPlaceHolder(int fakeSlot) {
        bankChanged = true;
        bank.withdrawItemPlaceHolder(fakeSlot);
    }
    
    public void withdrawLastAmount(int bankSlot) {
        bankChanged = true;
        bank.withdrawLastAmount(bankSlot);
    }
    
    public boolean isInsertItems() {
        return bank.isInsertItems();
    }

    public void setCurrentTab(int currentTab) {
        bankChanged = true;
        bank.setCurrentTab(currentTab);
    }

    public void switchInsertItems() {
        bank.switchInsertItems();
    }
    
    public boolean isInSearchMode() {
        return bank.isInSearchMode();
    }
    
    public void openSearchMode() {
        bank.setInSearchMode(true);
    }
    
    public void closeSearchMode() {
        bank.setInSearchMode(true);
        usingBank.getPackets().sendGlobalConfig(190, 1);
        usingBank.getPackets().sendConfigByFile(4893, 1);
        usingBank.getPackets().sendExecuteScript(1474);
    }
    
    public void sendExamineInteractionItem(int componentId, int slotId) {
        bank.sendExamineInteractionItem(componentId, slotId);
    }
    
    public int getInteractionTab() {
        return bank.getInteractionTab();
    }
    
    public void doExtraBankAction(int slotId) {
        bank.doExtraBankAction(slotId);
    }

    public void setDefaultInteractionAmount(int defaultInteractionAmount) {
        bankChanged = true;
        bank.setDefaultInteractionAmount(defaultInteractionAmount);
    }
    
    public void toggleLeavePlaceHolders() {
        bankChanged = true;
        bank.toggleLeavePlaceHolders();
    }
    
    public void removeAllPlaceHolders(boolean refreshItems) { 
        bankChanged = true;
        bank.removeAllPlaceHolders(refreshItems);
    }
    
    public boolean isConfigPresetsTab() {
        return bank.isConfigPresetsTab();
    }
    
    public void setConfigPresetsTab(boolean configPresetsTab) { 
        bankChanged = true;
        bank.setConfigPresetsTab(configPresetsTab);
    }
    
    public void setInteractionTab(int interactionTab) {
        bankChanged = true;
        bank.setInteractionTab(interactionTab);
    }
    
    public void switchItem(int fromSlot, int toSlot) { 
        bankChanged = true;
        bank.switchItem(fromSlot, toSlot);
    }
    
    public void switchToInventory(int fromSlot, int toSlot) { 
        bankChanged = true;
        bank.switchToInventory(fromSlot, toSlot);
    }
    
    public void switchToBank(int fromSlot, int toSlot, int toComponentId) { 
        bankChanged = true;
        bank.switchToBank(fromSlot, toSlot, toComponentId);
    }
    
    public void switchTabIndexes(int fromSlot, int toSlot) { 
        bankChanged = true;
        bank.switchTabIndexes(fromSlot, toSlot);
    }
    
    public void insertTab(int fromSlot, int toSlot) { 
        bankChanged = true;
        bank.insertTab(fromSlot, toSlot);
    }
    
    public boolean wearBankItem(int slotId, int quantity) { 
        bankChanged = true;
        return bank.wearBankItem(slotId, quantity);
    }
}
