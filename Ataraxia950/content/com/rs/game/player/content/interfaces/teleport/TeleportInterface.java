package com.rs.game.player.content.interfaces.teleport;

import com.rs.game.player.Player;
import com.rs.game.player.content.Magic;
import com.rs.game.player.controllers.RunespanController;
import com.rs.utils.Colors;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * ataraxia-server paolo 26/06/2019 #Shnek6969
 */
public class TeleportInterface implements Serializable {

    /**
     * static data
     */
    public static final int INTERFACE_ID = 128;
    private static final int MAIN_CONTAINER = 38;
    private static final int TEXT_BASE_ID = 42;
    private static final int BUTTON_BASE_ID = 39;
    private static final int BUTTON_AMOUNT = 21;
    private static final long serialVersionUID = -6983315230477897062L;
    private static final int[] INFO_COMPONENT = {125, 126, 131, 132};
    private static final int[] BASE_CONTAINERS = {39, 43, 47, 51, 55, 59, 63, 67, 71, 75, 79, 83, 87, 91, 95, 99, 103, 107, 111, 115, 119, 133, 137, 141, 145, 149, 153, 157, 161, 165, 169, 173, 177, 181, 185, 189, 193, 197, 201, 205, 209, 213, 217, 221, 225, 229, 233, 237, 241, 245, 249, 253, 257, 261, 265, 269, 273, 277, 281, 285, 289, 293, 297, 301, 305, 309, 313, 317, 321, 325, 329, 333, 337, 341, 345, 349, 353, 357, 361, 365, 369};

    /**
     * player data
     */
    private transient Player player;
    @Getter
    @Setter
    private TeleportLocation[] selectedPage = null;
    @Getter
    @Setter
    private TeleportLocation selectedLocation = null;
    @Getter
    @Setter
    private List<TeleportLocation> favorites;

    @Getter
    private TeleportLocation lastTeleport;

    /**
     * more correct than using a "setPlayer" method
     *
     * @param p
     */
    public TeleportInterface(Player p) {
        player = p;
        this.favorites = new ArrayList<>();
    }

    public void setPlayer(Player player) {
        this.player = player;
        if (favorites == null)
            favorites = new ArrayList<>();
    }

    /**
     * sends the interface to the player
     */
    public void sendInterface() {
        player.getInterfaceManager().sendInterface(INTERFACE_ID);
        player.getPackets().sendEmptyTextToComponents(INTERFACE_ID, 127, 128, 129, 130);
        player.getPackets().sendHideComponents(INTERFACE_ID, true, INFO_COMPONENT);
        hideButtons();
        sendTeleport(TeleportData.SKILLING);
        Object previousPage = player.getTemporaryAttributtes().get("TeleportInterfacePage");
        if (previousPage == null) {
            player.getTemporaryAttributtes().put("TeleportInterfacePage", 0);
        } else {
            int pageId = (int) previousPage; // previous page open
            switch (pageId) {
                case 0:
                    sendTeleport(TeleportData.SKILLING);
                    return;
                case 1:
                    sendTeleport(TeleportData.BOSSES);
                    return;
                case 2:
                    sendTeleport(TeleportData.MONSTERS);
                    return;
                case 3:
                    sendTeleport(TeleportData.MINIGAMES);
                    return;
                case 4:
                    sendTeleport(TeleportData.SLAYER);
                    return;
                case 5:
                    TeleportLocation[] locationArray = new TeleportLocation[getFavorites().size()];
                    locationArray = getFavorites().toArray(locationArray); // since toarray isn't castable
                    sendFavorites(locationArray);
                    return;
            }
        }
    }

    /**
     * hides every button
     */
    private void hideButtons() {
        for (int i : BASE_CONTAINERS)
            player.getPackets().sendHideIComponent(INTERFACE_ID, i, true);
    }

    /**
     * sends the buttons to the interface of the selected type
     *
     * @param teleportType
     */
    public void sendTeleport(TeleportData teleportType) {
        hideButtons();
        selectedPage = teleportType.getLocations();
        player.getPackets().sendHideComponents(INTERFACE_ID, true, INFO_COMPONENT);
        for (int i = 0; i < teleportType.getLocations().length; i++) {
            if (i >= BASE_CONTAINERS.length)
                return;
            player.getPackets().sendHideIComponent(INTERFACE_ID, BASE_CONTAINERS[i], false);
            player.getPackets().sendIComponentText(INTERFACE_ID, BASE_CONTAINERS[i] + 3, teleportType.getLocations()[i].getName());
        }

    }

    public void sendFavorites(TeleportLocation... locations) {
        selectedPage = locations;
        hideButtons();
        for (int i = 0; i < selectedPage.length; i++) {
            if (i >= BASE_CONTAINERS.length)
                return;
            player.getPackets().sendHideIComponent(INTERFACE_ID, BASE_CONTAINERS[i], false);
            player.getPackets().sendIComponentText(INTERFACE_ID, BASE_CONTAINERS[i] + 3, selectedPage[i].getName());
        }
    }

    /**
     * button clicking
     *
     * @param buttonId
     */
    public void handleButtons(int buttonId) {
        switch (buttonId) {
            case 27:
                player.getTemporaryAttributtes().put("TeleportInterfacePage", 0);
                sendTeleport(TeleportData.SKILLING);
                return;
            case 29:
                player.getTemporaryAttributtes().put("TeleportInterfacePage", 1);
                sendTeleport(TeleportData.BOSSES);
                return;
            case 31:
                player.getTemporaryAttributtes().put("TeleportInterfacePage", 2);
                sendTeleport(TeleportData.MONSTERS);
                return;
            case 33:
                player.getTemporaryAttributtes().put("TeleportInterfacePage", 3);
                sendTeleport(TeleportData.MINIGAMES);
                return;
            case 35:
                player.getTemporaryAttributtes().put("TeleportInterfacePage", 4);
                sendTeleport(TeleportData.SLAYER);
                return;
            case 37:
                player.getTemporaryAttributtes().put("TeleportInterfacePage", 5);
                TeleportLocation[] locationArray = new TeleportLocation[getFavorites().size()];
                locationArray = getFavorites().toArray(locationArray); // since toarray isn't castable
                sendFavorites(locationArray);
                return;
            case 131: // favorite
                if (selectedLocation == null) {
                    player.getDialogueManager().startDialogue("SimpleMessage", "Please select an location first.");
                    return;
                }
                if (getFavorites().contains(selectedLocation)) {
                    getFavorites().remove(selectedLocation);
                    player.getPackets().sendIComponentSprite(INTERFACE_ID, 131, 3747);
                    player.sm("You deleted" + selectedLocation.getName() + " from your favorite list.");
                } else {
                    getFavorites().add(selectedLocation);
                    player.getPackets().sendIComponentSprite(INTERFACE_ID, 131, 3746);
                    player.sm("You added " + selectedLocation.getName() + " to your favorite list.");
                }
                int pageId = (int) player.getTemporaryAttributtes().get("TeleportInterfacePage");
                if (pageId == 5) {
                    locationArray = new TeleportLocation[getFavorites().size()];
                    locationArray = getFavorites().toArray(locationArray); // since toarray isn't castable
                    selectedPage = locationArray;
                }
                sendFavorites(selectedPage);
                return;
            case 126:
                if (selectedLocation == null) {
                    player.getDialogueManager().startDialogue("SimpleMessage", "Please select an location first.");
                    return;
                }
                teleport();
                break;
        }
        for (int i = 0; i < BASE_CONTAINERS.length; i++) {
            if (BASE_CONTAINERS[i] == buttonId) {
                selectedLocation = selectedPage[i];
                sendSelectedTeleportInfo();
                return;
            }
        }
    }

    private void sendSelectedTeleportInfo() {
        player.getPackets().sendHideComponents(INTERFACE_ID, false, INFO_COMPONENT);
        player.getPackets().sendIComponentText(INTERFACE_ID, 126, "Teleport to " + selectedLocation.getName());
        if (getFavorites().contains(selectedLocation)) {
            player.getPackets().sendIComponentSprite(INTERFACE_ID, 131, 3746);
        } else
            player.getPackets().sendIComponentSprite(INTERFACE_ID, 131, 3747);
    }

    private void teleport() {
        teleportTo(player, selectedLocation);
    }

    public static void teleportTo(Player player, TeleportLocation location) {
        if (location.isWilderness()) {
            player.getDialogueManager().startDialogue("WildernessConfirmationD", location.getTeleTile());
            return;
        }
        if ((location.getName().equals("Edimmu") || location.getName().toLowerCase().contains("prif")) && !player.hasAccessToPrifddinas()) {
            player.sendMessage("You do not meet the requirements to access Priffdinas.");
            return;
        }
        if (location.getName().equalsIgnoreCase("Runespan")) {
            if (player.isCanPvp()) {
                player.sendMessage(Colors.RED + "You can't teleport from the wilderness with this!");
                return;
            }
            RunespanController.enterRunespan(player);
            return;
        }
        Magic.vineTeleport(player, location.getTeleTile());
        player.teleportInterface.lastTeleport = location;
        if (location.getController() != null) { // Starting after teleport otherwise the controller can get cancelled by the
            // "processTeleport"
            player.getControlerManager().startControler(location.getController());
        }
    }
}
