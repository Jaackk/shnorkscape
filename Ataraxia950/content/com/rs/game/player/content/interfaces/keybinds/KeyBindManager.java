package com.rs.game.player.content.interfaces.keybinds;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import com.rs.game.player.Player;
import com.rs.game.player.commands.Command;
import com.rs.game.player.commands.CommandHandler;
import com.rs.game.player.content.InterfaceManager;
import com.rs.game.player.content.Magic;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.network.packet.impl.ButtonHandler;

/**
 * ataraxia-server
 * paolo 03/10/2019
 * #Shnek6969
 */
public class KeyBindManager {


    public static void setCustomKeyBind(Player player, KeyBindActions keyBind, KeyCombination keyCombination) {
        if (keyBindExist(player, keyCombination)) {
            player.getDialogueManager().startDialogue("SimpleMessage", "An actions with this keybind already exists.");
            return;
        }
        player.getKeyBindActionsMap().put(keyBind, keyCombination);
    }

    public static void restoreDefaultKeyBind(Player player, KeyBindActions keyBindActions) {
        setCustomKeyBind(player, keyBindActions, keyBindActions.getDefaultCombination());
    }

    private static boolean keyBindExist(Player player, KeyCombination keyCombination) {
        AtomicBoolean exist = new AtomicBoolean(false);
        player.getKeyBindActionsMap().entrySet().forEach(entry -> {
            if (entry.getValue().secondKey == keyCombination.secondKey) {
                exist.set(true);
            }
        });
        return exist.get();
    }

    public static void setDefaultBinding(Player player) {
        if (player.getKeyBindActionsMap() == null)
            player.setKeyBindActionsMap(new LinkedHashMap<>());
        for (KeyBindActions keyBind : KeyBindActions.values()) {
            if (player.getKeyBindActionsMap().get(keyBind) == null) // so we can always call it on startup when now actions are added :)
                player.getKeyBindActionsMap().put(keyBind, keyBind.getDefaultCombination());
        }
    }

    private static KeyBindActions getActionByPressedKey(int keypressed, Player player) {
        KeyBindActions action = null;
        for (Map.Entry<KeyBindActions, KeyCombination> entry :  player.getKeyBindActionsMap().entrySet()) {
            if (entry == null)
                continue;
            if(entry.getValue() != null && entry.getValue().getSecondKey() == keypressed) {
                action = entry.getKey();
            }
        }

        return action;
    }

    public static void executeAction(Player player, int keyPressed) {
        KeyBindActions action = getActionByPressedKey(keyPressed, player);
        if (action == null)
            return;
        switch (action) {
            case OPEN_GIM_BANK:
                String commandString = "gimbank";
                Command command = CommandHandler.forSyntax("gimbank");
                if (command != null && command.canExecute(player, commandString)) {
                    command.executeCommand(player, false, commandString);
                    return;
                }
                command.executeCommand(player, false, commandString);
                break;
            case SHEATH:
                player.getCombatDefinitions().switchSheathe();
                break;
            case OPEN_BANK:
                commandString = "bank";
                command = CommandHandler.forSyntax("bank");
                if (command != null && command.canExecute(player, commandString)) {
                    command.executeCommand(player, false, commandString);
                    return;
                }
                command.executeCommand(player, false, commandString);
                break;
            case OPEN_TELEPORT_INTERFACE:
                player.openTeleportInterface();
                break;
            case OPEN_WORLDMAP:
                ButtonHandler.openWorldMap(player);
                break;
            case TELEPORT_HOME:
                Magic.vineTeleport(player, player.getHomeTile());
                break;
            case OPEN_LODESTONE:
                Magic.openLodestoneNetwork(player);
                break;
            case TOGGLE_RUN:
                player.toogleRun(!player.isResting());
                if (player.isResting())
                    player.stopAll();
                break;
            case TOGGLE_QUICKPRAYERS:
                WorldTasksManager.schedule(new WorldTask() {

                    @Override
                    public void run() {
                        player.getPrayer().switchQuickPrayers();
                    }

                });
                break;
            case OPEN_FAMILIAR_DETAIL:
                if (player.getFamiliar() == null)
                    player.sendMessage("You currently do not have a familiar.");
                else
                    player.getFamiliar().sendFollowerDetails();
                break;
//            case TOGGLE_SHIFT_DROP:
//                if (!player.lockedShiftDrop) {
//                    player.switchDropMode = !player.switchDropMode;
//                    if (player.switchDropMode) {
//                        player.getInventory().unlockInventoryOptions(false, true);
//                        player.getPackets().sendMainInterfaceMessage(1, "You have toggled on shift click dropping.", true);
//                    } else {
//                        player.getInterfaceManager().removeInterface(1473);
//                        player.getInterfaceManager().setWindowInterfaceByKey(2, 1473);
//                        player.getInterfaceManager().openGameTab(InterfaceManager.INVENTORY_TAB);
//                        player.getInventory().unlockInventoryOptions(false);
//                        player.getInventory().refresh();
//                        player.getPackets().sendMainInterfaceMessage(1, "You have toggled off shift click dropping.", true);
//                    }
//                }
//                break;
//        case FREEDOM:
//            PlayerCombat.pressFreedom(player);
//            break;
//        case SURGE:
//            PlayerCombat.pressSurgeEscape(player, true);
//            break;
//        case ESCAPE:
//            PlayerCombat.pressSurgeEscape(player, false);
//            break;
        default:
            break;
        }
    }
}
