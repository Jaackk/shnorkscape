package com.rs.game.player.dialogue.impl;

import java.util.ArrayList;
import java.util.List;

import com.rs.game.player.PerkManager.DonationPerk;
import com.rs.game.player.TemporaryAttributes.Key;
import com.rs.game.player.dialogue.Dialogue;

public class DonationPerksD extends Dialogue {
    private int[][] pages;
    private int currentPage;
    private int maxPagesNeeded;
    private int choosenPerk;
    private int choosenOption;
    private List<DonationPerk> availablePerks;
    public static final int interfaceId = 1312;
    public static final int[] TEXT_COMPONENT_IDS = { 38, 46, 54, 62, 70, 78, 86, 94, 102 };

    @Override
    public void start() {
        player.getTemporaryAttributtes().put(Key.DONATION_PERKS_D, Boolean.TRUE);
        availablePerks = new ArrayList<DonationPerk>();
        for (DonationPerk perk : DonationPerk.values()) {
            if (!perk.isToggleable() || !player.getPerkManager().hasPerk(perk))
                continue;
            availablePerks.add(perk);
        }
        if (availablePerks.isEmpty()) {
            player.getDialogueManager().startDialogue("SimpleMessage", "You don't have any perk that you can toggle.");
            return;
        }
        currentPage = 0;
        sendOptions(getDialogueOptions(), true);
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
            DonationPerk perk = availablePerks.get(choosenPerk);
            String perkName = perk.toString();
            if (choosenOption != 0) {
                switch (choosenOption) {
                case 1:
                    sendPerkOptions();
                    choosenOption = 0;
                    break;
                case 2:
                    player.getInterfaceManager().closeChatBoxInterface();
                    choosenOption = 0;
                    return;
                }
                return;
            }
            switch (componentId) {
            case OPTION_1:
                choosenOption = 1;
                String desc = "";
                for (int i = 0; i < perk.getBenefits().length; i++)
                    desc += perk.getBenefits()[i] + (i == perk.getBenefits().length - 1 ? "" : "<br>");
                sendDialogue(perkName + " benefits: <br>" + desc);
                break;
            case OPTION_2:
                choosenOption = 2;
                player.getPerkManager().togglePerkActivation(perk);
                sendDialogue(perkName + " has been " + (player.getPerkManager().hasPerkActive(perk) ? "Activated" : "Deactivated") + ".");
                sendOptions(getDialogueOptions());
                break;
            case OPTION_3:
                player.getInterfaceManager().closeChatBoxInterface();
                break;
            }
        }
    }

    public void sendPerkOptions() {
        DonationPerk perk = availablePerks.get(choosenPerk);
        String perkName = perk.toString();
        String title = "YOU ARE VIEWING OPTIONS FOR : " + perkName;
        sendOptionsDialogue(title, "Perk info.", (player.getPerkManager().hasPerkActive(perk) ? "Deactivate" : "Activate") + " " + perkName + " Perk", "Cancel.");
    }

    private String[] getDialogueOptions() {
        ArrayList<String> dialogueOptions = new ArrayList<String>(9);
        maxPagesNeeded = ((int) Math.ceil(availablePerks.size() / (7.00)));
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
                if (index > (availablePerks.size() - 1))
                    continue;
                pages[i][j] = index;
                index++;
            }
        }

        String firstName = getOptionName(availablePerks.get(0));
        dialogueOptions.add(currentPage == 0 ? firstName : "Back");
        int itemsCount = getItemsCount();
        for (int i = 0; i < itemsCount; i++) {
            String name = getOptionName(availablePerks.get(pages[currentPage][i]));
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

    private String getOptionName(DonationPerk perk) {
        String perkName = perk.toString();
        return ("<col=" + (player.getPerkManager().hasPerkActive(perk) ? "00ff00" : "FFFF00") + ">" + perkName + " " + (player.getPerkManager().hasPerkActive(perk) ? "(Active)" : "(Inactive)"));
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
        player.getTemporaryAttributtes().remove(Key.DONATION_PERKS_D);
        player.getInterfaceManager().closeScreenInterface();
    }

}
