package com.rs.game.player.dialogue.impl;

import com.rs.game.player.Player;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.data.parsers.misc.ShopsDataParser;
import com.rs.game.player.dialogue.impl.OptionSelectionD.OptionSelector;
import com.rs.game.player.dialogue.impl.OptionSelectionD.SelectionEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UziPerkShop {
    public static void openShop(Player player) {
       // List<String> options = new ArrayList<>(Arrays.asList("Combat Perks", "Skilling Perks", "Utility Perks", "Perk Packages", "Squeal of Fortune Spins"));
        String[] options = {"Combat Perks", "Skilling Perks", "Utility Perks", "Packages", "Pets", "Pet Perks", "Miscellaneous", "Treasure Hunter"};
        player.getDialogueManager().startDialogue("OptionSelectionD", "Which shop would you like to view?", options, options, (SelectionEvent) (selector, option) -> {
            int page = selector.getPage();
            switch (page) {
                case 0:
                    selector.close();
                    switch (option) {
                        case OptionSelector.OPTION_1:
                            ShopsDataParser.openShop(player, player.isGroupIronman() ? 663 : 160);
                            break;
                        case OptionSelector.OPTION_2:
                            ShopsDataParser.openShop(player, player.isGroupIronman() ? 664 : 165);
                            break;
                        case OptionSelector.OPTION_3:
                            ShopsDataParser.openShop(player, player.isGroupIronman() ? 665 : 166);
                            break;
                        case OptionSelector.OPTION_4:
                            ShopsDataParser.openShop(player, player.isGroupIronman() ? 666 : 167);
                            break;
                        case OptionSelector.OPTION_5:
                            if (player.isGroupIronman()) {
                                player.getDialogueManager().startDialogue("SimpleMessage", "Group Ironmen cannot buy Pets.");
                                return;
                            }
                            ShopsDataParser.openShop(player, 667);
                            break;
                        case OptionSelector.OPTION_6:
                            if (player.isGroupIronman()) {
                                player.getDialogueManager().startDialogue("SimpleMessage", "Group Ironmen cannot buy Pet perks.");
                                return;
                            }
                            ShopsDataParser.openShop(player, 669);
                            break;
                        case OptionSelector.OPTION_7:
                            ShopsDataParser.openShop(player,668);
                            break;
                        case OptionSelector.OPTION_8:
                            if (player.isATypeOfIronman()) {
                                player.getDialogueManager().startDialogue("SimpleMessage", "Ironmen cannot buy Treasure Hunter Keys.");
                                return;
                            }
                            ShopsDataParser.openShop(player, 168);
                            break;
                    }
                    break;
            }
        });
    }
//public class UziPerkShop extends Dialogue {
//    private int npcId = 756;
//
//    @Override
//    public void start() {
//        sendOptionsDialogue("Choose an Option", "Combat Perks", "Skilling Perks", "Utility Perks", "Perk Packages", "Squeal of Fortune Spins");
//    }
//
//    @Override
//    public void run(int interfaceId, int componentId) {
//        switch (stage) {
//            case -1:
//                switch (componentId) {
//                    case OPTION_1:
//                        finish();
//                        ShopsDataParser.openShop(player, player.isGroupIronman() ? 663 : 160);
//                        break;
//                    case OPTION_2:
//                        finish();
//                        ShopsDataParser.openShop(player, player.isGroupIronman() ? 664 : 165);
//                        break;
//                    case OPTION_3:
//                        finish();
//                        ShopsDataParser.openShop(player, player.isGroupIronman() ? 665 : 166);
//                        break;
//                    case OPTION_4:
//                        finish();
//                        ShopsDataParser.openShop(player, player.isGroupIronman() ? 666 : 167);
//                        break;
//                    case OPTION_5:
//                        finish();
//                        if (!player.isATypeOfIronman()) {
//                            ShopsDataParser.openShop(player, 168);
//                        } else {
//                            player.sendMessage("Iron accounts can't purchase Squeal of Fortune spins.");
//                        }
//                        break;
//                }
//        }
//    }
//
//    @Override
//    public void finish() {
//        player.getInterfaceManager().closeChatBoxInterface();
//    }
}
