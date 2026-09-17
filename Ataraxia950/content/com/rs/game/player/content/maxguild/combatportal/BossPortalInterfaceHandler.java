package com.rs.game.player.content.maxguild.combatportal;

import com.rs.game.player.Player;
import com.rs.utils.Utils;

/**
 * @author Xenthium.
 */

public class BossPortalInterfaceHandler {

    private static final int interfaceId = 1312;

    public static void sendInterface(Player player, int page) {
        final int[] buttonComponentIds = {5, 6, 7, 8, 9, 10, 11, 12};
        int loop = 0;
        player.getTemporaryAttributtes().put(BossPortal.RETUNING_COMBAT_PORTAL_KEY, page);
        player.getInterfaceManager().sendInterface(interfaceId);
        player.getPackets().sendHideIComponent(interfaceId, 26, true); // Hides the "Choose your Motto" text
        //player.getPackets().sendIComponentText(interfaceId, 27, "Select a portal");
        for (int id : buttonComponentIds) {
            player.getPackets().sendHideIComponent(interfaceId, id, true); // Hides all of the buttons
        }
        for (BossPortalType portal : BossPortalType.values()) {
            if (portal.getInterfacePage() == page) {
                player.getPackets().sendHideIComponent(interfaceId, buttonComponentIds[loop++], false); // Unhides the buttons as they become populated
                player.getPackets().sendIComponentText(interfaceId, portal.getInterfaceTextComponentId(), Utils.formatPlayerNameForDisplay(portal.name()));
            }
        }
        player.getPackets().sendIComponentText(interfaceId, 102, page == BossPortalType.getLastPage() ? "First page" : "Next page");
    }

    public static void handleButtons(Player player, int componentId) {
        int currentPage = (int) player.getTemporaryAttributtes().get(BossPortal.RETUNING_COMBAT_PORTAL_KEY);
        if (componentId == 99) { // sends next/first page
            sendInterface(player, currentPage == BossPortalType.getLastPage() ? 1 : currentPage + 1);
            return;
        }
        for (BossPortalType portal : BossPortalType.values()) {
            if (portal.getInterfacePage() == currentPage && portal.getInterfaceButtonComponentId() == componentId) {
                if (BossPortal.canRetune(player)) {
                    player.getDialogueManager().startDialogue("BossPortalDialogue", portal);
                    break;
                }
            }
        }
    }

}
