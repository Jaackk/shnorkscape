package com.rs.game.player.content.skillingcontracts;

import com.rs.game.item.Item;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.Colors;

/**
 * @author lare96 <http://github.com/lare96>
 */
public final class SkillingTomeD extends Dialogue {
    @Override
    public void start() {
        if (player.oldTomeActivated) {
            sendDialogue("You flip through the tome...");
            stage = 3;
        } else if (player.getContracts().current == null) {
            sendDialogue("You flip through the tome...");
            stage = 1;
        } else {
            sendDialogue("You flip through the tome...");
            player.sendMessage(Colors.RED + "The skilling tome has been activated. You will receive better rewards on completion of your current contract.");
            stage = 2;
            player.getInventory().deleteOneItem(new Item(13593));
            player.oldTomeActivated = true;
        }
    }

    @Override
    public void run(int interfaceId, int componentId) {
        switch (stage) {
            case 0:
                end();
                break;
            case 1:
                sendDialogue("It seems like you need to acquire a skilling contract before you can manipulate this tome.");
                stage = 0;
                break;
            case 2:
                sendDialogue("As you read the incantation 'Muggle wuggle tiny flute, give this adventurer better loot' The tome disappears!",
                        "It seems like you've invoked some kind of spell...");
                stage = 0;
                break;
            case 3:
                sendDialogue("Nothing happens when you read the incantation again. You feel a little silly.");
                stage = 0;
                break;
        }
    }

    @Override
    public void finish() {

    }
}