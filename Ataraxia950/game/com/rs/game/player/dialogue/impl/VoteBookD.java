package com.rs.game.player.dialogue.impl;

import com.rs.Settings;
import com.rs.game.item.Item;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.Colors;

/**
 * Handles opening of Vote Books.
 *
 * @author Noel
 */
public class VoteBookD extends Dialogue {

    @Override
    public void start() {
        sendItemDialogue(11640, 1, "Opening this Vote Book will give you a reward of your choice. To get "
                + "more Vote Books simply ::vote for Ataraxia on the most popular Gaming Top Sites.");
    }

    @Override
    public void run(int interfaceId, int componentId) {
        String[] amounts = {player.gWeekendBooks() > 0 ? "500,000" : "250,000",
                player.gWeekendBooks() > 0 ? "1,000,000" : "500,000",
                player.gWeekendBooks() > 0 ? "1,500,000" : "750,000",
                player.gWeekendBooks() > 0 ? "2,000,000" : "1,000,000"};
        switch (stage) {
            case -1:
                sendOptionsDialogue(Colors.CYAN + "Select your Reward",
                        "No coins and " + getPercentage() + "% bonus experience for 2 hours.",
                        amounts[0] + " coins and " + getPercentage() + "% bonus experience for 1.5 hours.",
                        amounts[1] + " coins and " + getPercentage() + "% bonus experience for 1 hour.",
                        amounts[2] + " coins and " + getPercentage() + "% bonus experience for 30 minutes.",
                        amounts[3] + " coins and no bonus experience.");
                stage = 0;
                break;
            case 0:
                if (componentId != OPTION_5) {
                    long time = player.getBonusXpTimer();
                    switch (componentId) {
                        case OPTION_1:
                            time += 12_000;
                        case OPTION_2:
                            time += 9000;
                        case OPTION_3:
                            time += 6000;
                        case OPTION_4:
                            time += 3000;
                    }
                    if (time > 12_000) {
                        sendDialogue("Are you sure you would like to continue?");
                        stage = 1;
                        player.xpBookOption = componentId;
                    } else {
                        giveReward(componentId, amounts);
                    }
                } else {
                    giveReward(OPTION_5, amounts);
                }
                break;
            case 1:
                sendOptionsDialogue("Select an option.", "Yes", "No");
                stage = 2;
                break;
            case 2:
                switch (componentId) {
                    case OPTION_1:
                        giveReward(player.xpBookOption, amounts);
                        player.xpBookOption = -1;
                        break;
                    case OPTION_2:
                        finish();
                        break;
                }
                break;
            case 3:
                finish();
                break;
        }
    }

    @Override
    public void finish() {
        player.getInterfaceManager().closeChatBoxInterface();
        player.xpBookOption = -1;
    }

    private void giveReward(int option, String[] amounts) {
        int amount;
        switch (option) {
            case OPTION_1:
                amount = 0;
                handleReward(7200000, amount);
                sendDialogue("You've chosen no coins and " + getPercentage() + "% bonus experience for 2 hours.");
                break;
            case OPTION_2:
                amount = player.gWeekendBooks() > 0 ? 500000 : 250000;
                handleReward(5400000, amount);
                sendDialogue("You've chosen " + amounts[0] + " coins and " + getPercentage()
                        + "% bonus experience for 1.5 hour.");
                if (player.gWeekendBooks() > 0)
                    player.sWeekendBooks(-1);
                break;
            case OPTION_3:
                amount = player.gWeekendBooks() > 0 ? 1000000 : 500000;
                handleReward(3600000, amount);
                sendDialogue("You've chosen " + amounts[1] + " coins and " + getPercentage()
                        + "% bonus experience for 1 hour.");
                if (player.gWeekendBooks() > 0)
                    player.sWeekendBooks(-1);
                break;
            case OPTION_4:
                amount = player.gWeekendBooks() > 0 ? 1500000 : 750000;
                handleReward(1800000, amount);
                sendDialogue("You've chosen " + amounts[2] + " coins and " + getPercentage()
                        + "% bonus experience for 30 minutes.");
                if (player.gWeekendBooks() > 0)
                    player.sWeekendBooks(-1);
                break;
            case OPTION_5:
                amount = player.gWeekendBooks() > 0 ? 2000000 : 1000000;
                handleReward(0, amount);
                sendDialogue("You've chosen " + amounts[3] + " coins and no bonus experience.");
                if (player.gWeekendBooks() > 0)
                    player.sWeekendBooks(-1);
                break;
        }
        stage = 3;
    }

    public String getPercentage() {
        if (player.isMasterDonator())
            return Settings.expBoosts[6][1];
        if (player.isUltimateDonator())
            return Settings.expBoosts[5][1];
        else if (player.isSupremeDonator())
            return Settings.expBoosts[4][1];
        else if (player.isLegendaryDonator())
            return Settings.expBoosts[3][1];
        else if (player.isExtremeDonator())
            return Settings.expBoosts[2][1];
        else if (player.isDonator())
            return Settings.expBoosts[1][1];
        else
            return "0";
    }

    /**
     * Handles the chosen vote book reward.
     *
     * @param xpBonus The xpBonus in millisecs.
     * @param coins The amount of coins.
     */
    private boolean handleReward(long xpBonus, int coins) {
        if (player.getInventory().containsItem(new Item(11640, 1))) {
            if (xpBonus > 0)
                player.setBonusXpTimer(player.getBonusXpTimer() + (xpBonus / 600));
            if (coins > 0)
                player.addMoney(coins);
            player.getInventory().deleteItem(new Item(11640, 1));
            return true;
        }
        return false;
    }
}