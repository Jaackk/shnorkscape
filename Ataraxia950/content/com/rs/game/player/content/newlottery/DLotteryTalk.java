package com.rs.game.player.content.newlottery;

import com.rs.game.player.Player;
import com.rs.game.player.dialogue.Dialogue;
import lombok.val;

/**
 * @author lare96 <http://github.com/lare96>
 */
public class DLotteryTalk extends Dialogue {

    private final Lottery lottery = Lottery.getSingleton();

    @Override
    public void start() {
        if (!lottery.starting) {
            dialogue("Greetings, noble one. I'm Ataraxia's lottery coordinator, how may I serve you?");
            stage = 0;
        } else {
            dialogue("Hold on, I'm about to announce the lottery winner!");
            stage = 3;
        }
    }

    @Override
    public void run(int interfaceId, int componentId) {
        switch (stage) {
            case 0:
                stage = 1;
                sendOptionsDialogue("Select an option.",
                        "May I place a bet?",
                        "Show me the lottery statistics.",
                        "How does it work?",
                        "Can I claim my winnings?");
                break;
            case 1:
                switch (componentId) {
                    case OPTION_1:
                        if (!placeBet(player)) {
                            stage = 2;
                        }
                        break;
                    case OPTION_2:
                        if (viewStats(player)) {
                            end();
                        } else {
                            stage = 0;
                        }
                        break;
                    case OPTION_3:
                        player.getDialogueManager().startDialogue("DLotteryExplain");
                        break;
                    case OPTION_4:
                        if (claimWinnings(player)) {
                            stage = 3;
                        } else {
                            stage = 0;
                        }
                        break;
                }
                break;
            case 2:
                player.getDialogueManager().startDialogue("DLotteryBet");
                break;
            case 3:
                end();
                break;
        }
    }

    @Override
    public void finish() {
        player.getInterfaceManager().closeChatBoxInterface();
    }

    private void dialogue(String... lines) {
        sendNPCDialogue(lottery.coordinatorId, Dialogue.CALM, lines);
    }

    private static void dialogue(Player player, String... lines) {
        player.getDialogueManager().startDialogue("DLotterySingleStageDialogue", Dialogue.CALM, lines);
    }

    public static boolean placeBet(Player player) {
        val lottery = Lottery.getSingleton();
        if (lottery.winnings.has(player)) {
            dialogue(player, "Before that, I'll need to give you your previous winnings. Congratulations!");
            lottery.winnings.giveAfter(player);
            return false;
        }
        if (lottery.starting) {
            player.sendMessage("I should probably wait until he announces the winner.");
            return false;
        }
        player.getDialogueManager().startDialogue("DLotteryBet");
        return true;
    }

    public static boolean viewStats(Player player) {
        val lottery = Lottery.getSingleton();
        if (lottery.statistics.available) {
            lottery.statistics.display(player);
            return true;
        }
        dialogue(player, "Well... it seems there are no statistics currently available.",
                "Admins must've just cleaned the damn books.");
        return false;
    }

    public static boolean claimWinnings(Player player) {
        val lottery = Lottery.getSingleton();
        if (!lottery.winnings.has(player)) {
            dialogue(player, "Sorry, but it seems you have no winnings to claim.");
            return false;
        }
        dialogue(player, "Here you go. Congratulations!");
        lottery.winnings.giveAfter(player);
        return true;
    }
}
