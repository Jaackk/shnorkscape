package com.rs.game.player.content.death;

import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.Colors;

/**
 * @author lare96
 */
public final class DeathWarningD extends Dialogue {

    @Override
    public void start() {
        sendPlayerDialogue(NORMAL, "W-w-where am I?");
        stage = 0;
    }

    @Override
    public void run(int interfaceId, int componentId) {
        switch (stage) {
            case 0:
                sendNPCDialogue(14386, CROOKED_HEAD,
                        "You're in nowhere. This is where I bring dead people and creatures before they respawn.",
                        "I also collect your things and hold on to them for you.");
                stage = 1;
                break;
            case 1:
                sendPlayerDialogue(NORMAL, "Wow, thank you! So I'm not dead? How do I usually get my stuff?");
                stage = 2;
                break;
            case 2:
                sendNPCDialogue(14386, CROOKED_HEAD,
                        "I charge a fee to return them, which is higher for more expensive items.",
                        "It's also higher if you die while I'm already holding onto things for you.",
                        "Any items with charges have their charges reduced.",
                        "The reduction amount depends on how many times you've died.");
                stage = 3;
                break;
            case 3:
                sendNPCDialogue(14386, CROOKED_HEAD,
                        "Cash stacks that aren't in your money pouch are " + Colors.RED + "always</col> lost.",
                        "I keep that as bonus.");
                stage = 4;
                break;
            case 4:
                sendNPCDialogue(14386, CROOKED_HEAD,
                        "I can tell its your first time here... so I have not taken your items.",
                        "If you're new, I will not take your items for your first 3 days of play time.");
                stage = 5;
                break;
            case 5:
                sendPlayerDialogue(NORMAL, "Thanks so much! Am I free to go now?");
                stage = 6;
                player.heardDeathsWarning = true;
                break;
            case 6:
                sendNPCDialogue(14386, CROOKED_HEAD,
                        "Yes. You'll be back...");
                stage = 7;
                break;
            case 7:
                end();
                break;
        }
    }

    @Override
    public void finish() {

    }
}
