package com.rs.game.player.content.bank_highscores;

import com.rs.game.player.Player;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.Utils;

public final class RichestBanksD extends Dialogue {

    public static void showRegular(Player player) {
        RichestBanks banks = GetRichestBanksSql.getRichestBanks();
        if (banks.getRegularUsername() == null) {
            Dialogue.sendSingleDialogue(player, "No one!");
            return;
        }
        Dialogue.sendSingleDialogue(player,
                "Name: " + banks.getRegularUsername(), "Bank value: " + Utils.formatNumber(banks.getRegularValue()));
    }

    public static void showIm(Player player) {
        RichestBanks banks = GetRichestBanksSql.getRichestBanks();
        if (banks.getImUsername() == null) {
            Dialogue.sendSingleDialogue(player, "No one!");
            return;
        }
        Dialogue.sendSingleDialogue(player,
                "<img=17> Name: " + banks.getImUsername() + " <img=17>", "<img=17> Bank value: " + Utils.formatNumber(banks.getImValue()) + " <img=17>");
    }

    public static void showHcim(Player player) {
        RichestBanks banks = GetRichestBanksSql.getRichestBanks();
        if (banks.getHcimUsername() == null) {
            Dialogue.sendSingleDialogue(player, "No one!");
            return;
        }
        Dialogue.sendSingleDialogue(player,
                "<img=18> Name: " + banks.getHcimUsername() + " <img=18>", "<img=18> Bank value: " + Utils.formatNumber(banks.getHcimValue()) + " <img=18>");
    }

    public static void showGim(Player player) {
        RichestBanks banks = GetRichestBanksSql.getRichestBanks();
        if (banks.getGimUsername() == null) {
            Dialogue.sendSingleDialogue(player, "No one!");
            return;
        }
        Dialogue.sendSingleDialogue(player,
                "<img=33> Name: " + banks.getGimUsername() + " <img=33>", "<img=33> Bank value: " + Utils.formatNumber(banks.getGimValue()) + " <img=33>");
    }

    @Override
    public void start() {
        sendOptionsDialogue("Select an option.",
                "Richest Normal Player",
                "<img=17> Richest Ironman <img=17>",
                "<img=18> Richest Hardcore Ironman <img=18>",
                "<img=33> Richest Group Ironman <img=33>");
        stage = 0;
    }

    @Override
    public void run(int interfaceId, int componentId) {
        switch (stage) {
            case -1:
                end();
                break;
            case 0:
                RichestBanks banks = GetRichestBanksSql.getRichestBanks();
                if (banks == null) {
                    sendDialogue("No data available at the moment.");
                    stage = -1;
                    break;
                }
                if (componentId == OPTION_1) {
                    showRegular(player);
                } else if (componentId == OPTION_2) {
                    showIm(player);
                } else if (componentId == OPTION_3) {
                    showHcim(player);
                } else if (componentId == OPTION_4) {
                    showGim(player);
                }
                break;
        }
    }

    @Override
    public void finish() {

    }
}
