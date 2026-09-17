package com.rs.game.activites.gim.guide;

import com.rs.game.player.dialogue.Dialogue;

/**
 * The GIM info dialogue.
 *
 * @author lare96 <http://github.com/lare96>
 */
public final class DGIMIntro extends Dialogue {

    /**
     * If this dialogue should loop.
     */
    private final boolean loop;

    /**
     * Creates a new {@link DGIMIntro}.
     */
    public DGIMIntro(boolean loop) {
        this.loop = loop;
    }

    @Override
    public void start() {
        sendNPCDialogue(12320, NORMAL, "Group Ironman is a competitive game mode where you are an Iron man, but you can interact with your group like they're regular players.",
                "There can be anywhere from 2-4 members in a group, and you can choose a name for your group.");
        stage = 0;
    }

    @Override
    public void run(int interfaceId, int componentId) {
        switch (stage) {
            case 0:
                sendNPCDialogue(12320, NORMAL, "The game mode runs in 'seasons' that last 3 months.",
                        "At the end of the season, the group with the highest score wins and will receive a unique reward.",
                        "Score is calculated through XP, levels, and boss points gained during that period.");
                stage = 1;
                break;
            case 1:
                sendNPCDialogue(12320, NORMAL, "Keep in mind that dying will reduce your total score.");
                stage = 2;
                break;
            case 2:
                sendNPCDialogue(12320, NORMAL, "Lastly, in this game mode, you will have access to a shared bank where you can deposit items useful to your group.",
                        "It functions like a normal bank, and you can view its history to see what was recently deposited/withdrawn.");
                stage = 3;
                break;
            case 3:
                sendPlayerDialogue(NORMAL, "Thank you for the explanation!");
                stage = 4;
                break;
            case 4:
                if(loop) {
                    player.getDialogueManager().startDialogue(new DGIMGuide());
                }else {
                    end();
                }
                break;
        }
    }

    @Override
    public void finish() {

    }
}
