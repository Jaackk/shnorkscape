package com.rs.game.activites.quest.root_of_evil;

import com.rs.game.player.Player;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Colors;
import com.rs.utils.InputStringEvent;
import lombok.val;

public final class GeneratorProgramD extends Dialogue {

    private final VaultController controller;

    public GeneratorProgramD(VaultController controller) {
        this.controller = controller;
    }

    @Override
    public void start() {
        if (controller.vaultStage == 4) {
            sendDialogue("The terminal reads:",
                    "\"WELCOME TO EVIL TREE GENERATOR V1.0",
                    "PLEASE ENTER THE PROGRAM CODE...\"");
            stage = 0;
        } else if (controller.vaultStage == 5) {
            sendAwaitingKey();
        } else {
            sendPlayerDialogue(NORMAL, "It doesn't seem like any power is running to the terminal.");
        }
    }

    @Override
    public void run(int interfaceId, int componentId) {
        switch (stage) {
            case -1:
                end();
                break;
            case 0:
                player.sendInputString("ENTER PROGRAM CODE", new InputStringEvent() {
                    @Override
                    public void run(Player player) {
                        handleCode(getString());
                    }
                });
                break;
            case 1:
                sendAwaitingKey();
                break;
            case 2:
                sendPlayerDialogue(NORMAL, "I should look around to see if I can find the self-destruct key anywhere...");
                if (controller.vaultStage != 5) {
                    player.getHintIconsManager().removeUnsavedHintIcon();
                    WorldTasksManager.schedule(new WorldTask() {
                        @Override
                        public void run() {
                            val hintIconPos = controller.instance.getInstanceTile(2393, 9825, 0);
                            player.getHintIconsManager().addHintIcon(hintIconPos.getX(), hintIconPos.getY(), hintIconPos.getPlane(), 85, 5, 0, -1, false);
                        }
                    }, 1);
                }
                controller.vaultStage = 5;
                stage = -1;
                break;
        }
    }

    @Override
    public void finish() {

    }

    private void handleCode(String code) {
        switch (code.toLowerCase()) {
            case "a175z": // start
                sendDialogue("The terminal reads:",
                        "\"PROGRAM FAILED... GENERATOR ALREADY RUNNING.\"");
                stage = -1;
                break;
            case "z542h": // throttle
                sendDialogue("The terminal reads:",
                        "\"PROGRAM FAILED... THROTTLE CODE DISABLED.\"");
                stage = -1;
                break;
            case "1leb4": // self-destruct
                sendDialogue("The terminal reads:",
                        "\"INITIALIZING SELF-DESTRUCT PROGRAM...\"");
                stage = 1;
                break;
            default: // bullshit
                sendDialogue("The terminal reads:",
                        "\"PROGRAM FAILED... COULD NOT FIND CODE " + code.toUpperCase() + ".\"");
                stage = -1;
                break;
        }
    }

    private void sendAwaitingKey() {
        if (player.getInventory().containsItem(RootOfEvil.SELF_DESTRUCT_KEY_ID, 1)) {
            player.lock();
            sendDialogue("The terminal reads:",
                    Colors.DARK_RED + "\"WARNING: INITIATING SELF-DESTRUCT SEQUENCE!\"");
            player.getInventory().deleteItem(RootOfEvil.SELF_DESTRUCT_KEY_ID, 1);
            WorldTasksManager.schedule(new WorldTask() {
                @Override
                public void run() {
                    player.getCutscenesManager().play(new EndQuestCutscene(controller));
                }
            }, 2);
        } else {
            sendDialogue("The terminal reads:",
                    "\"AWAITING KEY TO CONFIRM SELF-DESTRUCT...\"");
            stage = 2;
        }
    }
}
