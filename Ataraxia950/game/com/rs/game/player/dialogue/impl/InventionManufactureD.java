package com.rs.game.player.dialogue.impl;

import java.util.ArrayList;
import java.util.List;

import com.rs.cache.loaders.ItemDefinitions;
import com.rs.game.player.Player;
import com.rs.game.player.TemporaryAttributes.Key;
import com.rs.game.player.actions.invention.Manufacture;
import com.rs.game.player.actions.invention.Manufacture.ManufactureData;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.InputIntegerEvent;

public class InventionManufactureD extends Dialogue {
    private int[][] pages;
    private int currentPage;
    private int maxPagesNeeded;
    private int choosenPerk;
    public static final int interfaceId = 1312;
    public static final int[] TEXT_COMPONENT_IDS = { 38, 46, 54, 62, 70, 78, 86, 94, 102 };

    private List<ManufactureData> dataList;

    @SuppressWarnings("unchecked")
    @Override
    public void start() {
        dataList = (List<ManufactureData>) parameters[0];
        player.getTemporaryAttributtes().put(Key.MANUFACTURE_D, Boolean.TRUE);
        currentPage = 0;
        sendOptions(getDialogueOptions(), true);
    }

    private String[] getDialogueOptions() {
        ArrayList<String> dialogueOptions = new ArrayList<String>(9);
        maxPagesNeeded = ((int) Math.ceil(dataList.size() / (7.00)));
        maxPagesNeeded = maxPagesNeeded == 0 ? 1 : maxPagesNeeded;
        pages = new int[maxPagesNeeded][7];
        for (int i = 0; i < pages.length; i++) {
            for (int j = 0; j < (pages[i].length); j++) {
                pages[i][j] = -1;
            }
        }
        int index = 1;
        for (int i = 0; i < pages.length; i++) {
            for (int j = 0; j < pages[i].length; j++) {
                if (index > (dataList.size() - 1))
                    continue;
                pages[i][j] = index;
                index++;
            }
        }

        String firstName = getOptionName(dataList.get(0));
        dialogueOptions.add(currentPage == 0 ? firstName : "Back");
        int itemsCount = getItemsCount();
        for (int i = 0; i < itemsCount; i++) {
            String name = getOptionName(dataList.get(pages[currentPage][i]));
            dialogueOptions.add(name);
        }
        if (currentPage < (maxPagesNeeded - 1) && getItemsCount(currentPage + 1) > 0)
            dialogueOptions.add("More");
        else
            dialogueOptions.add("Cancel");

        String[] options = new String[dialogueOptions.size()];
        for (int i = 0; i < options.length; i++) {
            String option = dialogueOptions.get(i);
            if (option == null)
                continue;
            options[i] = option;
        }
        return options;
    }

    @Override
    public void run(int interfaceId, int componentId) {
        if (interfaceId == 1312) {
            int itemsCount = getItemsCount();
            for (int option = 0; option < TEXT_COMPONENT_IDS.length; option++) {
                if (componentId != TEXT_COMPONENT_IDS[option] - 3)
                    continue;
                switch (option) {
                case 0:
                    if (currentPage == 0) {
                        choosenPerk = 0;
                        sendPerkOptions();
                    } else {// back
                        player.getInterfaceManager().closeChatBoxInterface();
                        currentPage--;
                        sendOptions(getDialogueOptions());
                    }
                    break;
                case 1:
                case 2:
                case 3:
                case 4:
                case 5:
                case 6:
                case 7:
                    if (itemsCount > option - 1)
                        choosenPerk = pages[currentPage][option - 1];
                    else
                        choosenPerk = -1;
                    if (choosenPerk != -1) {
                        sendPerkOptions();
                    } else
                        end();
                    break;
                case 8:
                    if (currentPage < (maxPagesNeeded - 1) && getItemsCount(currentPage + 1) > 0) {
                        currentPage++;
                        sendOptions(getDialogueOptions());
                        player.getInterfaceManager().closeChatBoxInterface();
                    } else
                        end();
                    break;
                }
            }
        } else {
            player.getInterfaceManager().closeChatBoxInterface();
            ManufactureData data = dataList.get(choosenPerk);
            boolean startAction = Manufacture.checkAll(player, data, false);
            switch (componentId) {
            case OPTION_1:
            case OPTION_2:
            case OPTION_4:
                if (startAction)
                    end();
                int amount = componentId == OPTION_1 ? 1 : componentId == OPTION_2 ? 10 : 60;
                player.getActionManager().setAction(new Manufacture(data, data.getProductId() == 36389 ? 1 : amount));
                break;
            case OPTION_3:
                player.sendInputInteger("How many would you like to manufacture?", new InputIntegerEvent() {
                    @Override
                    public void run(Player player) {
                        int value = getInteger();
                        if (value <= 0)
                            return;
                        if (value > 60)
                            value = 60;
                        if (startAction)
                            end();
                        player.getActionManager().setAction(new Manufacture(data, data.getProductId() == 36389 ? 1 : value));
                    }
                });
                break;
            case OPTION_5:
                player.getInterfaceManager().closeChatBoxInterface();
                break;
            }
        }
    }

    public void sendPerkOptions() {
        ManufactureData data = dataList.get(choosenPerk);
        String dataName = ItemDefinitions.getItemDefinitions(data.getProductId()).getName();
        String title = "YOU ARE VIEWING OPTIONS FOR : " + dataName;
        boolean hasRequirement = Manufacture.checkAll(player, data, false);
        if (hasRequirement)
            sendOptionsDialogue(title, "Manufacture 1", "Manufacture 10", "Manufacture X", "Manufacture All", "Cancel.");
        else {
            player.getInterfaceManager().closeChatBoxInterface();
            Manufacture.checkAll(player, data, true);
        }
    }

    private void sendOptions(String[] dialogueOptions) {
        sendOptions(dialogueOptions, false);
    }

    private void sendOptions(String[] dialogueOptions, boolean sendInterface) {
        for (int i = 0; i < TEXT_COMPONENT_IDS.length; i++) {
            player.getPackets().sendHideIComponent(interfaceId, TEXT_COMPONENT_IDS[i], i >= dialogueOptions.length);
            player.getPackets().sendHideIComponent(interfaceId, TEXT_COMPONENT_IDS[i] - 3, i >= dialogueOptions.length);
            player.getPackets().sendHideIComponent(interfaceId, TEXT_COMPONENT_IDS[i] - 5, i >= dialogueOptions.length);
            player.getPackets().sendHideIComponent(interfaceId, TEXT_COMPONENT_IDS[i] - 6, i >= dialogueOptions.length);
            player.getPackets().sendHideIComponent(interfaceId, TEXT_COMPONENT_IDS[i] - 7, i >= dialogueOptions.length);
            if (i < dialogueOptions.length)
                player.getPackets().sendIComponentText(interfaceId, TEXT_COMPONENT_IDS[i], dialogueOptions[i]);
        }
        if (sendInterface)
            player.getInterfaceManager().sendInterface(interfaceId);
    }

    private String getOptionName(ManufactureData data) {
        String dataName = ItemDefinitions.getItemDefinitions(data.getProductId()).getName();
        return ("<col=" + (Manufacture.checkAll(player, data, false) ? "00ff00" : "FF0000") + ">" + dataName + " " + ((Manufacture.checkAll(player, data, false) ? "" : "(Missing ingredients)")));
    }

    public int getItemsCount() {
        int itemsCount = 0;
        for (int i = 0; i < (pages[currentPage].length); i++) {
            if (pages[currentPage][i] != -1)
                itemsCount++;
        }
        return itemsCount;
    }

    public int getItemsCount(int page) {
        int itemsCount = 0;
        for (int i = 0; i < (pages[page].length - 1); i++) {
            if (pages[page][i] != -1)
                itemsCount++;
        }
        return itemsCount;
    }

    @Override
    public void finish() {
        player.getTemporaryAttributtes().remove(Key.MANUFACTURE_D);
        player.getInterfaceManager().closeScreenInterface();
    }
}
