package com.rs.game.player.dialogue.impl;

import com.rs.game.item.floor.LootBeamType;
import com.rs.game.player.Player;
import com.rs.game.player.content.AccountInterfaceManager;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.Colors;
import com.rs.utils.InputIntegerEvent;
import com.rs.utils.Utils;

/**
 * Used for handling the Player Settings dialogue.
 *
 * @author Noel
 */
public class SetBeam extends Dialogue {

    @Override
    public void start() {
        sendOptionsDialogue(Colors.CYAN + "Loot Beam Settings",
                "Loot Beam: " + (player.getLootBeamManager().isLootBeamsEnabled() ? Colors.GREEN + "Enabled ("
                        + player.getLootBeamManager().getCurrentLootBeamType().toString() + ")"
                        : Colors.RED + "Disabled") + "</col>.",
                "Trigger price: " + Colors.RED +
                        Utils.getFormattedNumber(player.getLootBeamManager().getLootBeamMinimumValue()) + "</col>.");
    }

    @Override
    public void run(int interfaceId, int componentId) {
        switch (stage) {
            case -1:
                switch (componentId) {
                    case OPTION_1:
                        if (player.getLootBeamManager().isLootBeamsEnabled()) {
                            finish();
                            player.getLootBeamManager().toggleLootBeam();
                            AccountInterfaceManager.sendInterface(player);
                        } else {
                            sendOptionsDialogue(Colors.CYAN + "Loot Beam Settings",
                                    getLootBeamString(LootBeamType.NORMAL),
                                    getLootBeamString(LootBeamType.RAINBOW),
                                    getLootBeamString(LootBeamType.CHRISTMAS),
                                    getLootBeamString(LootBeamType.SUNSHINE),
                                    getLootBeamString(LootBeamType.MUSIC));
                            stage = 0;
                        }
                        break;
                    case OPTION_2:
                        finish();
                        player.sendInputInteger("Current drop price to trigger Loot beam: " +
                                Utils.getFormattedNumber(player.getLootBeamManager().getLootBeamMinimumValue()) +
                                "." + "<br>What price would you like to set?", new InputIntegerEvent() {
                            @Override
                            public void run(Player player) {
                                int value = getInteger();
                                if (value < 1 || value > 2147000000)
                                    value = 50000;
                                player.getLootBeamManager().setLootBeamMinimumValue(value);
                                player.getDialogueManager().startDialogue("SimpleMessage", "You've set your Loot Beam trigger price " + "to: " + Colors.RED + Utils.getFormattedNumber(value) + "</col>.");
                                AccountInterfaceManager.sendInterface(player);
                            }
                        });
                        break;
                }
                break;
            case 0:
                switch (componentId) {
                    case OPTION_1:
                        sendAppropriateDialogueForUnlock(LootBeamType.NORMAL);
                        break;
                    case OPTION_2:
                        sendAppropriateDialogueForUnlock(LootBeamType.RAINBOW);
                        break;
                    case OPTION_3:
                        sendAppropriateDialogueForUnlock(LootBeamType.CHRISTMAS);
                        break;
                    case OPTION_4:
                        sendAppropriateDialogueForUnlock(LootBeamType.SUNSHINE);
                        break;
                    case OPTION_5:
                        sendAppropriateDialogueForUnlock(LootBeamType.MUSIC);
                        break;
                }
                break;
            case 1:
                finish();
                AccountInterfaceManager.sendInterface(player);
                break;
        }
    }

    private String getLootBeamString(LootBeamType lootBeamType) {
        boolean hasLootBeam = player.getLootBeamManager().hasUnlockedLootBeam(lootBeamType);
        String name = lootBeamType.toString() + " Loot Beam";
        return hasLootBeam ? Colors.GREEN + name : Colors.RED + name;
    }

    private void sendAppropriateDialogueForUnlock(LootBeamType lootBeamType) {
        if (player.getLootBeamManager().hasUnlockedLootBeam(lootBeamType)) {
            player.getLootBeamManager().setCurrentLootBeamType(lootBeamType);
            finish();
            player.getLootBeamManager().toggleLootBeam();
            AccountInterfaceManager.sendInterface(player);
        } else {
            sendDialogue("You have not unlocked this loot beam type!");
            stage = 1;
        }
    }

    @Override
    public void finish() {
        player.getInterfaceManager().closeChatBoxInterface();
    }

}