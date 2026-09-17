package com.rs.game.player.dialogue.impl;

import static com.rs.game.player.content.titles.PlayerTitle.THE_WARDEN;
import static com.rs.game.player.content.titles.PlayerTitle.THE_WARDEN_2;
import static com.rs.game.player.content.titles.PlayerTitle.THE_WARDEN_3;

import com.rs.cache.loaders.ItemDefinitions;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.content.TelosEnrageRanks;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.InputIntegerEvent;

public class Soothsayer extends Dialogue {

    private int npcId;
    private Item item;

    @Override
    public void start() {
        npcId = (int) this.parameters[0];
        item = this.parameters.length == 2 ? (Item) parameters[1] : null;
        if (item != null) {
            stage = 8;
            String[] options = new String[3];
            int index = 0;
            for (int i = 0; i < 3; i++) {
                int checkItemId = 37619 + i;
                if (checkItemId == item.getId())
                    continue;
                options[index] = "Replace my " + item.getName() + " with " + ItemDefinitions.getItemDefinitions(checkItemId).name + ".";
                index++;
            }
            options[2] = "Nevermind.";
            sendOptionsDialogue(SEND_DEFAULT_OPTIONS_TITLE, options);
            return;
        }
        sendOptionsDialogue(SEND_DEFAULT_OPTIONS_TITLE, "Claim loot", "Reset my enrage", "Set enrage", "Check Current Enrage/ KillStreak", "Next page...");
    }

    @Override
    public void run(int interfaceId, int componentId) {
        switch (stage) {
        case -1:
            if (componentId == OPTION_1) {
                stage = 0;
                sendNPCDialogue(npcId, NORMAL, "Are you sure you want to claim your loot? (This will reset your enrage and streak)");
            } else if (componentId == OPTION_2) {
                stage = 2;
                sendNPCDialogue(npcId, NORMAL, "Are you sure you want to reset your enrage? ");
            } else if (componentId == OPTION_3) {
                stage = 4;
                sendNPCDialogue(npcId, NORMAL, "I can set your enrage to your maximum reached enrage(" + player.getMaxEnrage() + "). Warning: Doing this will also reset your killstreak.");
            } else if (componentId == OPTION_4) {
                stage = 5;
                sendNPCDialogue(npcId, NORMAL, "Your current killstreak is " + player.getTelosStreak() + ". Current Enrage is " + player.getTelosEnrage() + "%.");
            } else if (componentId == OPTION_5) {
                sendOptionsDialogue(SEND_DEFAULT_OPTIONS_TITLE, "Check enrage ranks", "Set title to " + player.theWarden(), "Previous page...");
                stage = 6;
            }
            break;
        case 0:
            stage = 1;
            sendOptionsDialogue(SEND_DEFAULT_OPTIONS_TITLE, "Yes, give me my loot.", "No, nevermind");
            break;
        case 1:
            end();
            if (componentId == OPTION_1) {
                if (player.getCurrentTelosReward() != null) {
                    player.getDialogueManager().startDialogue("SimpleNPCMessage", npcId, "You have an unclaimed reward left please claim it from font before coming here.");
                    return;
                }
                if (player.getTelosRewards().isEmpty()) {
                    player.getDialogueManager().startDialogue("SimpleNPCMessage", npcId, "You don't have any reward to claim.");
                    return;
                }
                player.claimTelosRewards();
            }
            break;
        case 2:
            stage = 3;
            sendOptionsDialogue(SEND_DEFAULT_OPTIONS_TITLE, "Yes, reset my enrage(This will also reset your killstreak).", "No, nevermind");
            break;
        case 3:
            end();
            if (componentId == OPTION_1) {
                player.setTelosStreak(0);
                player.setTelosEnrage(0);
                player.getPackets().sendGameMessage("Your telos enrage have been reset.");
            }
            break;
        case 4:
            end();
            player.sendInputInteger("Enter enrage (max=" + player.getMaxEnrage() + "):", new InputIntegerEvent() {

                @Override
                public void run(Player player) {
                    int value = this.getInteger();
                    int maxEnrage = player.getMaxEnrage();
                    if (value > maxEnrage) {
                        player.getPackets().sendGameMessage("You can't set your enrage over your maximum reached enrage.");
                        return;
                    }
                    if (value < 0)
                        return;
                    player.setTelosStreak(0);
                    player.setTelosEnrage(value);
                    player.getPackets().sendGameMessage("You have set your enrage to " + value + ".");
                }
                
            });
            break;
        case 5:
            stage = -1;
            sendOptionsDialogue(SEND_DEFAULT_OPTIONS_TITLE, "Claim loot", "Reset my enrage", "Set enrage", "Check Current Enrage/ KillStreak", "Next page...");
            break;
        case 6:
            if (componentId == OPTION_1) {
                end();
                TelosEnrageRanks.showRanks(player);
            } else if (componentId == OPTION_2) {
                if (player.getMaxEnrage() >= 500 && player.getMaxEnrage() < 2000) {
                    player.getAppearence().setTitle(THE_WARDEN.getTitleId());
                    player.sendMessage("You've set your title to " + player.theWarden() + ".");
                    end();
                } else if (player.getMaxEnrage() >= 2000 && player.getMaxEnrage() < 4000) {
                    player.getAppearence().setTitle(THE_WARDEN_2.getTitleId());
                    player.sendMessage("You've set your title to " + player.theWarden() + ".");
                    end();
                } else if(player.getMaxEnrage() >= 4000){
                    player.getAppearence().setTitle(THE_WARDEN_3.getTitleId());
                    player.sendMessage("You've set your title to " + player.theWarden() + ".");
                    end();
                }else {
                    sendNPCDialogue(npcId, NORMAL, "You need to achieve a minimum of 500% enrage before you can use this title.");
                    stage = 7;
                }
            } else if (componentId == OPTION_3) {
                stage = -1;
                sendOptionsDialogue(SEND_DEFAULT_OPTIONS_TITLE, "Claim loot", "Reset my enrage", "Set enrage", "Check Current Enrage/ KillStreak", "Next page...");
            }
            break;
        case 7:
            sendOptionsDialogue(SEND_DEFAULT_OPTIONS_TITLE, "Check enrage ranks", "Set title to " + player.theWarden(), "Previous page...");
            stage = 6;
            break;
        case 8:
            if (componentId == OPTION_3) {
                end();
                return;
            }
            end();
            int choosenIndex = componentId == OPTION_1 ? 0 : 1;
            int index = 0;
            for (int i = 0; i < 3; i++) {
                int checkItemId = 37619 + i;
                if (checkItemId == item.getId())
                    continue;
                if (index == choosenIndex) {
                    item.setId(37619 + i);
                    player.getInventory().refresh();
                    player.getPackets().sendGameMessage("The soothsayer replaced your orb with " + item.getName() + ".");
                    break;
                }
                index++;
            }
            break;
        }
    }

    @Override
    public void finish() {
        // TODO Auto-generated method stub

    }

}
