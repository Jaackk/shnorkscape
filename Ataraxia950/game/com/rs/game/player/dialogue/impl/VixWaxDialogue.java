package com.rs.game.player.dialogue.impl;

import com.google.common.collect.Iterables;
import com.rs.cache.loaders.ItemDefinitions;
import com.rs.game.player.AuraManager;
import com.rs.game.player.Player;
import com.rs.game.player.VisWaxManager;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.Colors;
import com.rs.utils.InputIntegerEvent;
import com.rs.utils.Pair;
import com.rs.utils.Utils;
import lombok.val;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * @author _jordan <jordan.abraham1997@gmail.com>
 * <p>
 * Created on Sep 26, 2018.
 */
public class VixWaxDialogue extends Dialogue {

    private final LinkedList<Pair<Integer, String>> auraOptions = new LinkedList<>();
    private final Runnable[] optionActions = new Runnable[5];

    @Override
    public void start() {
        sendOptionsDialogue("Select an option.",
                "Redeem",
                "Nevermind.");
        stage = -2;
    }

    @Override
    public void run(int interfaceId, int componentId) {
        switch (stage) {
            case -2:
                if (componentId == OPTION_1) {
                    sendOptionsDialogue("Select an option",
                            "Purchase 'Quick teleports'",
                            "Purchase 'Skilling contract reset'",
                            "Purchase '" + VisWaxManager.SKILLING_TICKETS_BUY_AMT + " Skilling tickets'",
                            "Purchase 'Divine location reset'");
                    stage = -1;
                } else if (componentId == OPTION_2) {
                    end();
                }
                break;
            case -1:
                if (componentId == OPTION_1) {
                    player.sendInputInteger("How many Vis Wax would you like to use?", new InputIntegerEvent() {

                        @Override
                        public void run(Player player) {
                            int input = getInteger();
                            if (input < 1) {
                                end();
                                return;
                            }

                            if (player.getVisWaxManager().purchaseQuickTeleports(input))
                                player.sendMessage("You recieve the knowledge and wisdom of 1000 flying demon lava birds. You now have " + player.getVisWaxManager().getQuickTeleports() + " magical quick teleports.");

                            end();
                        }

                    });
                } else if (componentId == OPTION_2) {
                    sendOptionsDialogue("This purchase will cost you " + VisWaxManager.SKILLING_CONTRACT_PURCHASE_AMOUNT + " Vis Wax. Do you wish to continue?", "Yes.", "No.");
                    stage = 1;
                } else if (componentId == OPTION_3) {
                    sendOptionsDialogue("This purchase will cost you " + VisWaxManager.SKILLING_CONTRACT_POINTS_PURCHASE_AMOUNT + " Vis Wax. Do you wish to continue?", "Yes.", "No.");
                    stage = 3;
                } else if (componentId == OPTION_4) {
                    sendOptionsDialogue("This purchase will cost you " + VisWaxManager.DIVINE_LOCATION_PURCHASE_AMOUNT + " Vis Wax. Do you wish to continue?", "Yes.", "No.");
                    stage = 2;
                }
                break;
            case 0:
                end();
                break;
            case 1:
                if (componentId == OPTION_1) {
                    if (player.getContracts().hasCoOpContract()) {
                        if (player.getVisWaxManager().purchaseCoOpContractReset())
                            player.sendMessage("You successfully purchase a co-op skilling contract reset for " + VisWaxManager.CO_OP_SKILLING_CONTRACT_PURCHASE_AMOUNT + " Vis Wax.");
                    } else if (player.getContracts().hasContract()) {
                        if (player.getVisWaxManager().purchaseContractReset())
                            player.sendMessage("You successfully purchase a skilling contract reset for " + VisWaxManager.SKILLING_CONTRACT_PURCHASE_AMOUNT + " Vis Wax.");
                    } else {
                        player.sendMessage("You do not have a skilling contract to reset.");
                    }
                }
                end();
                break;
            case 2:
                if (componentId == OPTION_1 && player.getVisWaxManager().purchaseDivinationDailyReset()) {
                    player.sendMessage("You successfully purchase a Divination divine location reset for " + VisWaxManager.DIVINE_LOCATION_PURCHASE_AMOUNT + " Vis Wax.");
                }
                end();
                break;
            case 3:
                if (componentId == OPTION_1) {
                    if (player.getVisWaxManager().purchaseSkillingTickets())
                        player.sendMessage("You successfully purchase " + VisWaxManager.SKILLING_TICKETS_BUY_AMT + " Skilling tickets for " + VisWaxManager.SKILLING_CONTRACT_POINTS_PURCHASE_AMOUNT + " Vis Wax.");
                }
                end();
                break;
            case 4:
                if (componentId == OPTION_1) {
                    if (player.lastResetWisdomAura != null) {
                        val throttleDate = player.lastResetWisdomAura.plusHours(24);
                        if (LocalDateTime.now().isBefore(throttleDate)) {
                            sendDialogue("You can only do this once per 24h.");
                            stage = 0;
                            break;
                        }
                    }
                    auraOptions.clear();
                    for (val next : player.getAuraManager().cooldowns.entrySet()) {
                        val name = ItemDefinitions.getItemDefinitions(next.getKey()).name;
                        if (!name.toLowerCase().contains("wisdom")) {
                            continue;
                        }
                        if(Utils.currentTimeMillis() > next.getValue()) {
                            continue;
                        }
                        auraOptions.add(new Pair<>(next.getKey(), name + " (" + AuraManager.getFormatedTime((next.getValue() - Utils.currentTimeMillis()) / 1000) + " time left)"));
                    }
                    if (auraOptions.isEmpty()) {
                        sendDialogue("You don't have any wisdom auras cooling down.");
                        stage = 0;
                        break;
                    }

                    sendOptionsDialogue("Select an option.",getNextOptions());
                    stage = 5;
                } else if (componentId == OPTION_2) {
                    end();
                }
                break;
            case 5:
                if (componentId == OPTION_1) {
                    optionActions[0].run();
                } else if (componentId == OPTION_2) {
                    optionActions[1].run();
                } else if (componentId == OPTION_3) {
                    optionActions[2].run();
                } else if (componentId == OPTION_4) {
                    optionActions[3].run();
                } else if (componentId == OPTION_5) {
                    optionActions[4].run();
                }
                break;
        }
    }

    @Override
    public void finish() {
        // TODO Auto-generated method stub

    }

    private String[] getNextOptions() {
        List<String> optionsList = new ArrayList<>();
        int maxOptions = 0;
        for (Pair<Integer, String> next : auraOptions) {
            if (maxOptions == 4) {
                optionsList.add(Colors.DARK_RED + "Next...");
                optionActions[maxOptions] = () -> {
                    sendOptionsDialogue("Select an option.", getNextOptions());
                    stage = 5;
                };
                break;
            }
            optionsList.add(next.right);
            optionActions[maxOptions] = () -> {
                if (player.getVisWaxManager().purchaseWisdomAuraReset(next.left))
                    player.sendMessage("You successfully reset '" + ItemDefinitions.getItemDefinitions(next.left).name + "' for " + VisWaxManager.WISDOM_AURA_RESET_PURCHASE_AMOUNT + " Vis Wax.");
                end();
            };
            maxOptions++;
        }
        if(optionsList.size() == 1) {
            optionsList.add(".");
            optionActions[1] = () -> {};
        }
        return Iterables.toArray(optionsList, String.class);
    }
}
