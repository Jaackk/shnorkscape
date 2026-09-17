package com.rs.game.player.content.newlottery;

import com.rs.game.player.dialogue.Dialogue;

/**
 * @author lare96 <http://github.com/lare96>
 */
public class DLotteryExplainGames extends Dialogue {

    private final Lottery lottery = Lottery.getSingleton();
    @Override
    public void start() {
        stage = 0;
        dialogue("Winners are picked when the jackpot reaches 2B, or 45 minutes after the third bet.",
                "No games will start until at least three people have placed a bet.");

    }

    @Override
    public void run(int interfaceId, int componentId) {
        switch (stage) {
            case 0:
                stage = 1;
                dialogue("Once three people have placed bets, lottery updates will be sent to the chat every 15 minutes.",
                        "Stay on the lookout for them!");
                break;
            case 1:
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
