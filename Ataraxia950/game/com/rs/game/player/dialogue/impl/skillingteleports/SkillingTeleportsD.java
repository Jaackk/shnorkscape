package com.rs.game.player.dialogue.impl.skillingteleports;

import com.rs.game.WorldTile;
import com.rs.game.player.content.Magic;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.Colors;

public final class SkillingTeleportsD extends Dialogue {
    @Override
    public void start() {
        sendOptionsDialogue("Select an option", "Fishing", "Mining", "Agility", "Woodcutting", Colors.RED + "Next...");
        stage = 0;
    }

    @Override
    public void run(int interfaceId, int componentId) {
        switch (stage) {
        case 0:
            if (componentId == OPTION_1) {
                player.getDialogueManager().startDialogue("FishingTeleportsD");
            } else if (componentId == OPTION_2) {
                player.getDialogueManager().startDialogue("MiningTeleportsD");
            } else if (componentId == OPTION_3) {
                player.getDialogueManager().startDialogue("AgilityTeleportsD");
            } else if (componentId == OPTION_4) {
                player.getDialogueManager().startDialogue("WoodcuttingTeleportsD");
            } else if (componentId == OPTION_5) {
                sendOptionsDialogue("Select an option", "Runecrafting", "Summoning", "Farming", "Hunter", Colors.RED + "Next...");
                stage = 1;
            }
            break;
        case 1:
            if (componentId == OPTION_1) {
                player.getDialogueManager().startDialogue("RunecraftingTeleportsD");
            } else if (componentId == OPTION_2) {
                Magic.vineTeleport(player, new WorldTile(2923, 3449, 0));
                end();
            } else if (componentId == OPTION_3) {
                player.getDialogueManager().startDialogue("FarmingTeleportsD");
            } else if (componentId == OPTION_4) {
                player.getDialogueManager().startDialogue("HunterTeleportsD");
            } else if (componentId == OPTION_5) {
                sendOptionsDialogue("Select an option", "Crafting", "Prayer", "Divination", "Invention Guild", Colors.RED + "Home...");
                stage = 2;
            }
            break;
        case 2:
            if (componentId == OPTION_1) {
                player.getDialogueManager().startDialogue("CraftingTeleportsD");
            } else if (componentId == OPTION_2) {
                if (player.hasAccessToPrifddinas()) {
                    Magic.vineTeleport(player, new WorldTile(2190, 3445, 1));
                } else {
                    player.sendMessage("You do not meet the requirements to access Priffdinas.");
                }
            } else if (componentId == OPTION_3) {
                player.getDialogueManager().startDialogue("DivinationTeleportsD");
            } else if (componentId == OPTION_4) {
                Magic.vineTeleport(player, new WorldTile(2997, 3438, 0));
            } else if (componentId == OPTION_5) {
                start();
            }
            break;
        }
    }

    @Override
    public void finish() {
        player.getInterfaceManager().closeChatBoxInterface();
    }
}