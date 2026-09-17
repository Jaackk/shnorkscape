package com.rs.game.player.content.polls;

import com.rs.game.player.dialogue.Dialogue;

/**
 * @author lare96 <http://github.com/lare96>
 */
public final class PollGuideD extends Dialogue {
    @Override
    public void start() {
        options();
    }

    @Override
    public void run(int interfaceId, int componentId) {
        switch (stage) {
            case -1:
                options();
                break;
            case 0:
                if (componentId == OPTION_1) {
                    sendDialogue("This is where game content will be voted on to determine mechanics or allocation of dev time.",
                            "When it's (open) a poll is running, when it's (closed) there is no poll running.");
                    stage = 1;
                } else if (componentId == OPTION_2) {
                    sendDialogue("You can vote by being a donator or having at least 24h of play time.",
                            "Click on an open poll booth and follow the instructions on the interface. It's an easy way to make your voice heard!");
                    stage = -1;
                } else if (componentId == OPTION_3) {
                    sendDialogue("Yes, there is a good chance you'll be rewarded for voting.",
                            "Rewards can change depending on the theme of the poll, and can even be one-time exclusives!");
                    stage = -1;
                }
                break;
            case 1:
                sendDialogue("We will use the voting data to help us make important decisions about the future of the game.");
                stage = -1;
                break;
        }
    }

    @Override
    public void finish() {

    }

    private void options() {
        sendOptionsDialogue("Select an option.",
                "What is this thing?",
                "How can I vote?",
                "Do I get rewarded for voting?");
        stage = 0;
    }
}