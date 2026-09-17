package com.rs.game.player.content.newlottery;

import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.Colors;

/**
 * @author lare96 <http://github.com/lare96>
 */
public class DLotteryExplainBet extends Dialogue {

    private final Lottery lottery = Lottery.getSingleton();
    @Override
    public void start() {
        stage = 0;
        dialogue("There are four types of bets: Newbie, Regular, Gambler, and Investor.",
                "They will each give varying percentages of the jackpot, with higher bets yielding higher percentages and a better chance at winning.");
    }

    @Override
    public void run(int interfaceId, int componentId) {
        switch (stage) {
            case 0:
                stage = 1;
                dialogue("After you place a bet, you can continue your gameplay completely as normal.",
                        "You may logout, go into the wilderness, play minigames, etc.");
                break;
            case 1:
                stage = 2;
                dialogue("Rest assured that your money will either be safeguarded by me or deposited into your bank.");
                break;
            case 2:
                stage = 3;
                dialogue("Be aware that you can only place one bet at a time.",
                        "Lastly, please note that all bets are " + Colors.RED + "final</col> and cannot be refunded.");
                break;
            case 3:
                player.getDialogueManager().startDialogue("DLotteryExplain");
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
}
