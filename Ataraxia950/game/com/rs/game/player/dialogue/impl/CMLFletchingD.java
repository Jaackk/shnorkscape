package com.rs.game.player.dialogue.impl;

import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.CMLFletching;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.InputIntegerEvent;

import static com.rs.game.player.actions.CMLFletching.LOG_ID;
import static com.rs.game.player.actions.CMLFletching.LOG_LEVEL;

/**
 * CML = Corrupted magic logs
 *
 * @author lare96 <http://github.com/lare96>
 */
public class CMLFletchingD extends Dialogue {

    private int amount;
    private boolean portable;

    @Override
    public void finish() {
    }

    @Override
    public void run(int interfaceId, int componentId) {
        switch (stage) {
            case -2:
                end();
                break;
            case -1:
                menuOptions();
                break;
            case 0:
                amount = player.getInventory().getAmountOf(LOG_ID);
                if (amount == 0) {
                    sendDialogue("You do not have any corrupted magic logs to fletch.");
                    stage = -1;
                } else {
                    if (componentId == OPTION_1 && amount >= 1) {
                        amount = 1;
                    } else if (componentId == OPTION_2 && amount >= 5) {
                        amount = 5;
                    } else if (componentId == OPTION_3) {
                        player.sendInputInteger("Enter the amount", new InputIntegerEvent() {
                            @Override
                            public void run(Player player) {
                                int value = getInteger();
                                if (value < 1) {
                                    sendDialogue("You have entered an invalid amount.");
                                    stage = -1;
                                } else {
                                    if (value > amount) {
                                        value = amount;
                                    }
                                    player.getActionManager().setAction(new CMLFletching(value, portable));
                                    end();
                                }
                            }
                        });
                    }
                    player.getActionManager().setAction(new CMLFletching(amount, portable));
                    end();
                }
                break;
        }
    }

    @Override
    public void start() {
        portable = (boolean) parameters[0];
        menuOptions();
    }

    private void menuOptions() {
        if (player.getSkills().getLevel(Skills.FLETCHING) >= LOG_LEVEL) {
            sendOptionsDialogue("Select an option.",
                    "Fletch (1) logs.",
                    "Fletch (5) logs.",
                    "Fletch (x) logs.",
                    "Fletch all logs.");
            stage = 0;
        } else {
            sendDialogue("You need a Fletching level of " + LOG_LEVEL + " to fletch corrupted magic logs.");
            stage = -2;
        }
    }
}