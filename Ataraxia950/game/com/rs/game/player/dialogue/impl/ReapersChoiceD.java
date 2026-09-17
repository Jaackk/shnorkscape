package com.rs.game.player.dialogue.impl;

import com.rs.game.player.content.contracts.ContractHandler;
import com.rs.game.player.content.contracts.ContractHandler.ContractData;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.Utils;

import java.util.ArrayList;

public class ReapersChoiceD extends Dialogue {
    private boolean fromReaper;
    private int[][] pages;
    private int currentPage;
    private int maxPagesNeeded;
    private int choosenTask;

    @Override
    public void start() {
        if(!player.isChooseTask()) {
            player.getPackets().sendGameMessage("something went wrong.");
            return;
        }
        fromReaper = (Boolean) this.parameters[0];
        sendCorrectDialoge("Thanks to your Reaper's Choice perk, You can now choose your next reaper task.");
    }

    public void sendCorrectDialoge(String... messages) {
        if (fromReaper)
            sendNPCDialogue(ContractDialogue.idNo, CROOKED_HEAD, messages);
        else
            sendDialogue(messages);
    }

    @Override
    public void run(int interfaceId, int componentId) {
        if (stage == -1) {
            stage = 0;
            currentPage = 0;
            sendOptionsDialogue("CHOOSE THE TASK YOU WANT, PAGE: " + (currentPage + 1), getDialogueOptions());
            return;
        }
        int itemsCount = getItemsCount();
        switch (stage) {
        case 0:
            switch (componentId) {
            case OPTION_1:
                if (currentPage == 0) {
                    choosenTask = 0;
                    sendConfirmationOptions();
                } else {// back
                    currentPage--;
                    sendOptionsDialogue("CHOOSE THE TASK YOU WANT, PAGE: " + (currentPage + 1), getDialogueOptions());
                }
                break;
            case OPTION_2:
                if (itemsCount > 0)
                    choosenTask = pages[currentPage][0];
                if (choosenTask != -1) {
                    sendConfirmationOptions();
                } else
                    end();
                break;
            case OPTION_3:
                if (itemsCount > 1)
                    choosenTask = pages[currentPage][1];
                if (choosenTask != -1) {
                    sendConfirmationOptions();
                } else
                    end();
                break;
            case OPTION_4:
                if (itemsCount > 2)
                    choosenTask = pages[currentPage][2];
                if (choosenTask != -1) {
                    sendConfirmationOptions();
                } else
                    end();
                break;
            case OPTION_5:
                if (currentPage < (maxPagesNeeded - 1) && getItemsCount(currentPage + 1) > 0) {
                    currentPage++;
                    sendOptionsDialogue("CHOOSE THE TASK YOU WANT, PAGE: " + (currentPage + 1), getDialogueOptions());
                } else
                    end();
                break;
            }
            break;
        case 1:
            ContractData contract = ContractData.values()[choosenTask];
            switch (componentId) {
            case OPTION_1:
                stage = 2;
                player.setChooseTask(false);
                ContractHandler.assignPlayerNewContract(player, contract.ordinal());
                String npcName = ContractHandler.getFormattedContractName(player);
                sendCorrectDialoge("Your new contract is to kill:<br>", "<col=FF0000>" + player.getContract().getKillAmount() + "x " + npcName + "<br>", "Reward: <col=0000FF>" + player.getContract().getRewardAmount() + " Reaper Points");
                break;
            case OPTION_2:
                stage = 0;
                sendOptionsDialogue("CHOOSE THE TASK YOU WANT, PAGE: " + (currentPage + 1), getDialogueOptions());
                break;
            case OPTION_3:
                end();
                break;
            }
            break;
        case 2:
            end();
            break;
        }
    }

    public void sendConfirmationOptions() {
        ContractData contract = ContractData.values()[choosenTask];
        String contractName = Utils.formatPlayerNameForDisplay(contract.name());
        stage = 1;
        sendOptionsDialogue(SEND_DEFAULT_OPTIONS_TITLE, "Yes set my reaper task to " + contractName + "", "Back", "Cancel");
    }

    private String[] getDialogueOptions() {
        ArrayList<String> dialogueOptions = new ArrayList<String>(5);
        maxPagesNeeded = ((int) Math.ceil((double) ContractData.values().length / (3.00)));
        maxPagesNeeded = maxPagesNeeded == 0 ? 1 : maxPagesNeeded;
        choosenTask = -1;
        pages = new int[maxPagesNeeded][3];
        for (int i = 0; i < pages.length; i++) {
            for (int j = 0; j < (pages[i].length); j++) {
                pages[i][j] = -1;
            }
        }
        int index = 1;
        for (int i = 0; i < pages.length; i++) {
            for (int j = 0; j < pages[i].length; j++) {
                if (index > (ContractData.values().length - 1))
                    continue;
                pages[i][j] = index;
                index++;
            }
        }

        String firstName = getOptionName(ContractData.values()[0]);
        dialogueOptions.add(currentPage == 0 ? firstName : "Back");
        int itemsCount = getItemsCount();
        for (int i = 0; i < itemsCount; i++) {
            String name = getOptionName(ContractData.values()[pages[currentPage][i]]);
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

    private String getOptionName(ContractData contract) {
        return Utils.formatPlayerNameForDisplay(contract.name());
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

    }

}
