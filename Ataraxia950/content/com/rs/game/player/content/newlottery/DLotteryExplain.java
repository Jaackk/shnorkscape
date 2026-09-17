package com.rs.game.player.content.newlottery;

import com.rs.game.player.dialogue.Dialogue;

/**
 * @author lare96 <http://github.com/lare96>
 */
public class DLotteryExplain extends Dialogue {

    private final Lottery lottery = new Lottery();


    @Override
    public void start() {
        stage = 0;
        sendOptionsDialogue("Select an option.",
                "Tell me about betting.",
                "Explain how lottery games work.",
                "How do I claim my winnings?",
                "Nevermind.");
    }

    @Override
    public void run(int interfaceId, int componentId) {
        switch (stage) {
            case 0:
                switch (componentId) {
                    case OPTION_1:
                        player.getDialogueManager().startDialogue("DLotteryExplainBet");
                        break;
                    case OPTION_2:
                        player.getDialogueManager().startDialogue("DLotteryExplainGames");
                        break;
                    case OPTION_3:
                        dialogue("Winnings will be automatically placed in the inventory or bank while you're online.",
                                "Otherwise, you can always claim them from me.");
                        stage = 1;
                        break;
                    case OPTION_4:
                        player.getDialogueManager().startDialogue("DLotteryTalk");
                        break;
                }
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
