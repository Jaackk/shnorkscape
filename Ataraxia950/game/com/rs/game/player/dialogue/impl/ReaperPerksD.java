package com.rs.game.player.dialogue.impl;

import com.rs.game.player.content.contracts.ReaperPerks;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.Utils;

import java.util.ArrayList;

public class ReaperPerksD extends Dialogue {
    private int[][] pages;
    private int currentPage;
    private int maxPagesNeeded;
    private int choosenPerk;
    private int choosenOption;

    @Override
    public void start() {
        currentPage = 0;
        sendOptionsDialogue("CHOOSE THE PERK YOU WANT, PAGE: " + (currentPage + 1), getDialogueOptions());
    }

    @Override
    public void run(int interfaceId, int componentId) {
        int itemsCount = getItemsCount();
        switch (stage) {
        case -1:
            switch (componentId) {
            case OPTION_1:
                if (currentPage == 0) {
                    choosenPerk = 0;
                    sendPerkOptions();
                } else {// back
                    currentPage--;
                    sendOptionsDialogue("CHOOSE THE PERK YOU WANT, PAGE: " + (currentPage + 1), getDialogueOptions());
                }
                break;
            case OPTION_2:
                if (itemsCount > 0)
                    choosenPerk = pages[currentPage][0];
                if (choosenPerk != -1) {
                    sendPerkOptions();
                } else
                    end();
                break;
            case OPTION_3:
                if (itemsCount > 1)
                    choosenPerk = pages[currentPage][1];
                if (choosenPerk != -1) {
                    sendPerkOptions();
                } else
                    end();
                break;
            case OPTION_4:
                if (itemsCount > 2)
                    choosenPerk = pages[currentPage][2];
                if (choosenPerk != -1) {
                    sendPerkOptions();
                } else
                    end();
                break;
            case OPTION_5:
                if (currentPage < (maxPagesNeeded - 1) && getItemsCount(currentPage + 1) > 0) {
                    currentPage++;
                    sendOptionsDialogue("CHOOSE THE PERK YOU WANT, PAGE: " + (currentPage + 1), getDialogueOptions());
                } else
                    end();
                break;
            }
            break;
        case 0:
            ReaperPerks perk = ReaperPerks.values()[choosenPerk];
            String perkName = Utils.formatPlayerNameForDisplay(perk.name()).replace("Reapers", "Reaper's");
            if (choosenOption != 0) {
                switch (choosenOption) {
                case 1:
                    sendPerkOptions();
                    choosenOption = 0;
                    break;
                case 2:
                    if (player.reaperPerkUnlocked(perk)) {
                        sendPerkOptions();
                        choosenOption = 0;
                        return;
                    }
                    switch (componentId) {
                    case OPTION_1:
                        stage = 1;
                        if (player.getReaperPoints() >= perk.getPrice()) {
                            player.setReaperPoints(player.getReaperPoints() - perk.getPrice());
                            player.getReaperPerks().add(perk);
                            sendDialogue("Perk Unlocked: " + perkName + "!");
                            return;
                        }
                        sendDialogue("You don't have enough reaper points to purchase " + perkName + ".");
                        break;
                    case OPTION_2:
                        sendPerkOptions();
                        choosenOption = 0;
                        break;
                    }
                    break;
                }
                return;
            }
            switch (componentId) {
            case OPTION_1:
                choosenOption = 1;
                sendDialogue(perkName + " effect: " + perk.getDesc() + (perk.getActivationChance() < 1 ? " Activation Chance: " + perk.getActivationChance() * 100 + "%" : ""));
                break;
            case OPTION_2:
                choosenOption = 2;
                if (player.reaperPerkUnlocked(perk)) {
                    player.togglePerkActivation(perk.ordinal());
                    sendDialogue(perkName + " has been " + (player.reaperPerkActivated(perk) ? "Activated" : "Deactivated") + ".");
                    return;
                }
                sendOptionsDialogue("ARE YOU SURE YOU WANT TO BUY " + perkName.toUpperCase() + "? Current Reaper Points: " + player.getReaperPoints(), "Yes, i am sure.", "Nevermind.");
                break;
            case OPTION_3:
                stage = -1;
                sendOptionsDialogue("CHOOSE THE PERK YOU WANT, PAGE: " + (currentPage + 1), getDialogueOptions());
                break;
            case OPTION_4:
                end();
                break;
            }
            break;
        case 1:
            if (choosenOption != 0) {
                switch (choosenOption) {
                case 2:
                    end();
                    break;
                }
            }
            break;
        }
    }

    public void sendPerkOptions() {
        ReaperPerks perk = ReaperPerks.values()[choosenPerk];
        String perkName = Utils.formatPlayerNameForDisplay(perk.name()).replace("Reapers", "Reaper's");
        String title = "YOU ARE VIEWING OPTIONS FOR : " + perkName;
        stage = 0;
        if (player.reaperPerkUnlocked(perk)) {
            sendOptionsDialogue(title, "Perk info.", (player.reaperPerkActivated(perk) ? "Deactivate" : "Activate") + " " + perkName + " Perk", "Back.", "Cancel.");
        } else {
            sendOptionsDialogue(title, "Perk info.", "Buy " + perkName + " Perk for " + perk.getPrice() + " Reaper Points.", "Back.", "Cancel.");
        }
    }

    private String[] getDialogueOptions() {
        ArrayList<String> dialogueOptions = new ArrayList<String>(5);
        maxPagesNeeded = ((int) Math.ceil(ReaperPerks.values().length / (3.00)));
        maxPagesNeeded = maxPagesNeeded == 0 ? 1 : maxPagesNeeded;
        choosenPerk = -1;
        pages = new int[maxPagesNeeded][3];
        for (int i = 0; i < pages.length; i++) {
            for (int j = 0; j < (pages[i].length); j++) {
                pages[i][j] = -1;
            }
        }
        int index = 1;
        for (int i = 0; i < pages.length; i++) {
            for (int j = 0; j < pages[i].length; j++) {
                if (index > (ReaperPerks.values().length - 1))
                    continue;
                pages[i][j] = index;
                index++;
            }
        }

        String firstName = getOptionName(ReaperPerks.values()[0]);
        dialogueOptions.add(currentPage == 0 ? firstName : "Back");
        int itemsCount = getItemsCount();
        for (int i = 0; i < itemsCount; i++) {
            String name = getOptionName(ReaperPerks.values()[pages[currentPage][i]]);
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

    private String getOptionName(ReaperPerks perk) {
        String perkName = Utils.formatPlayerNameForDisplay(perk.name()).replace("Reapers", "Reaper's");
        return player.reaperPerkUnlocked(perk) ? ("<col=" + (player.reaperPerkActivated(perk) ? "00ff00" : "FFFF00") + ">" + perkName + " " + (player.reaperPerkActivated(perk) ? "(Active)" : "(Inactive)")) : ("<col=ff0000>" + perkName + " (" + perk.getPrice() + ")");
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
