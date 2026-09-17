package com.rs.game.player.content.newlottery;

import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.Colors;

/**
 * @author lare96 <http://github.com/lare96>
 */
public class DLotteryBet extends Dialogue {

    private final Lottery lottery = Lottery.getSingleton();

    @Override
    public void start() {
        if (lottery.bets.has(player)) {
            dialogue("You have already placed a bet in this lottery game.");
            stage = -2;
        } else if (player.isATypeOfIronman()) {
            dialogue("Sorry, iron men are not allowed to place lottery bets.");
            stage = -7;
        } else {
            dialogue("Of course sir. What type of bet would you like to place?");
        }
    }

    @Override
    public void run(int interfaceId, int componentId) {
        switch (stage) {
            case -7:
                sendPlayerDialogue(ANGRY, "What? This is discrimination! I'll report you to the king!");
                stage = -6;
                break;
            case -6:
                dialogue("You fool... he ordered me to do it.");
                stage = -5;
                break;
            case -5:
                sendDialogue("Royal decree from 'King Jaedmo IV': Iron men have had it easy for too long! Do not let them place lottery bets.");
                stage = -4;
                break;
            case -4:
                sendPlayerDialogue(ANGRY, "Fine, I'll just do some bossing or skilling contracts instead! I don't need you.");
                stage = -3;
                break;
            case -3:
                dialogue(GOOFY_LAUGH, "Sure. That's what they all say.");
                stage = -2;
                break;
            case -2:
                end();
                break;
            case -1:
                sendBetOptions();
                break;
            case 0:
                stage = 1;
                switch (componentId) {
                    case OPTION_1:
                        player.confirmBet = LotteryBetType.NEWBIE;
                        break;
                    case OPTION_2:
                        player.confirmBet = LotteryBetType.REGULAR;
                        break;
                    case OPTION_3:
                        player.confirmBet = LotteryBetType.GAMBLER;
                        break;
                    case OPTION_4:
                        player.confirmBet = LotteryBetType.INVESTOR;
                        break;
                    default:
                        throw new RuntimeException();
                }
                String prepend = player.confirmBet == LotteryBetType.INVESTOR ? "an" : "a";
                dialogue("Are you " + Colors.RED + "absolutely sure</col> you would like to place " + prepend + ' ' + Colors.RED + player.confirmBet + "</col> bet?");
                break;
            case 1:
                stage = 2;
                sendOptionsDialogue("Select an option.", "Yes", "No");
                break;
            case 2:
                switch (componentId) {
                    case OPTION_1:
                        LotteryBet newBet = new LotteryBet(player, player.confirmBet);
                        if (lottery.bets.canAdd(newBet.type)) {
                            if (player.getInventory().deleteAllCoins(player.confirmBet.amount)) {
                                lottery.bets.add(lottery.betsFile, newBet);
                                dialogue("Okay and you're done. Keep an eye out on the chat for lottery status updates!");
                                stage = 3;
                            } else {
                                dialogue("You do not have enough coins to bet this high!");
                                stage = -1;
                            }
                        } else {
                            dialogue("Sorry, the pot is too full for that bet! Please try placing a lower bet.");
                            stage = -1;
                        }
                        break;
                    case OPTION_2:
                        sendBetOptions();
                        break;
                }
                break;
            case 3:
                end();
                break;
        }
    }

    @Override
    public void finish() {
        player.getInterfaceManager().closeChatBoxInterface();
        player.confirmBet = null;
    }

    private void sendBetOptions() {
        String newbieShare = LotteryBetType.NEWBIE.formattedShare;
        String regularShare = LotteryBetType.REGULAR.formattedShare;
        String gamblerShare = LotteryBetType.GAMBLER.formattedShare;
        String investorShare = LotteryBetType.INVESTOR.formattedShare;
        sendOptionsDialogue("Select an option.",
                "Newbie (25m, " + newbieShare + "% of jackpot)",
                "Regular (50m, " + regularShare + "% of jackpot, +1 win chance)",
                "Gambler (100m, " + gamblerShare + "% of jackpot, +2 win chance)",
                "Investor (150m, " + investorShare + "% of jackpot, +3 win chance)");
        stage = 0;
    }

    private void dialogue(String... lines) {
        sendNPCDialogue(lottery.coordinatorId, Dialogue.CALM, lines);
    }

    private void dialogue(int expression, String... lines) {
        sendNPCDialogue(lottery.coordinatorId, expression, lines);
    }
}
