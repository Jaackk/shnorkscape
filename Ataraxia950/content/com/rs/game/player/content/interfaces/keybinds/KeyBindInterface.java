package com.rs.game.player.content.interfaces.keybinds;

import com.rs.game.player.Player;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.InputNameEvent;
import com.rs.utils.Utils;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import static com.rs.game.player.content.KeyActionMapper.INDEX_MAP;

/**
 * ataraxia-server
 * paolo 03/10/2019
 * #Shnek6969
 */
public class KeyBindInterface {

    public static final int INTERFACE_ID = 1932;
    public static final int CTRL_KEY = 82;
    public static final int[] MAIN_DISPLAY_COMPONENTS = {
            20,31,42,51,60,69,78,87,96,105,114
    };

    public static final int[] BUTTON_COMPONENTS = {
        30,41,124,125,126,127,128,129,132,130,131
    };

    
    public static void sendInterface(Player player) {
        sendList(player);
        player.getInterfaceManager().sendInterface(INTERFACE_ID);
    }
    
    public static KeyBindActions[] displayed = {KeyBindActions.OPEN_BANK, KeyBindActions.TELEPORT_HOME, KeyBindActions.OPEN_TELEPORT_INTERFACE, KeyBindActions.OPEN_LODESTONE, KeyBindActions.SHEATH, KeyBindActions.OPEN_WORLDMAP, KeyBindActions.TOGGLE_QUICKPRAYERS, KeyBindActions.OPEN_FAMILIAR_DETAIL, KeyBindActions.TOGGLE_RUN, KeyBindActions.OPEN_GIM_BANK};
    
    private static void sendList(Player player){
        AtomicInteger componentIndex = new AtomicInteger(0);
        for(KeyBindActions action : displayed) {
            KeyCombination value = player.getKeyBindActionsMap().get(action);
            if (value == null) 
                continue;
            if (componentIndex.get() >= MAIN_DISPLAY_COMPONENTS.length)
                return;
            player.getPackets().sendHideIComponent(INTERFACE_ID,MAIN_DISPLAY_COMPONENTS[componentIndex.get()],false);
            player.getPackets().sendText(INTERFACE_ID, MAIN_DISPLAY_COMPONENTS[componentIndex.get()] + 5, Utils.formatString(action.toString()));
            player.getPackets().sendText(INTERFACE_ID, MAIN_DISPLAY_COMPONENTS[componentIndex.get()] + 7, value.toString());
            componentIndex.getAndIncrement();
        }
    }

    public static void handleButtons(Player player, int componentId){
        for(int i = 0; i < BUTTON_COMPONENTS.length; i++){
            if(componentId == BUTTON_COMPONENTS[i]){
                KeyBindActions keybind = displayed[i];
                player.getDialogueManager().startDialogue(new Dialogue() {
                    @Override
                    public void start() {
                        sendOptionsDialogue("Edit keybind: "+Utils.formatString(keybind.toString()), "Restore to default", "Set custom key", "Cancel");
                        stage= 0;
                    }

                    @Override
                    public void run(int interfaceId, int componentId) {
                    if(stage == 0) {
                        if (componentId == OPTION_1) {
                            KeyBindManager.restoreDefaultKeyBind(player, keybind);
                            sendDialogue("Keybind succesfully restored to default.");
                            KeyBindInterface.sendList(player);
                            stage = 1;
                        } else if (componentId == OPTION_2) {
                            player.sendInputName("Press a key you would like to bind", new InputNameEvent() {
                                @Override
                                public void run(Player player) {
                                    final String value = getString();
                                    if (value.equals(""))
                                        return;
                                    if (value.length() > 1) {
                                        sendDialogue("You can only use a single character as bind. example : A");
                                        stage = 1;
                                        return;
                                    }
                                    KeyBindManager.setCustomKeyBind(player, keybind, new KeyCombination(CTRL_KEY,  INDEX_MAP.get(value.toUpperCase())));
                                    //player.getKeyBindActionsMap().get(keybind).secondKey = value.toCharArray()[0];
                                    KeyBindInterface.sendList(player);
                                    stage = 1;
                                }
                            });
                        } else
                            end();
                    } else if(stage == 1)
                        end();
                    }

                    @Override
                    public void finish() {

                    }
                });
                return;
            }

        }
    }

}
