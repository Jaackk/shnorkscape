package com.rs.game.player.content.items;

import com.rs.game.player.Player;
import com.rs.game.player.content.barrows.Barrows;
import com.rs.game.player.content.barrows.BarrowsConstants;
import com.rs.utils.Colors;

/**
 * @author lare96 <http://github.com/lare96>
 */
public final class BarrowsAmulet {

    public static final int BARROWS_AMULET_ID = 29759;
    private static final int BARROWS_CHEST = 6775;

    public static void activate(Player player) {
        if (player.barrowsAmuletActivated) {
            player.sendMessage(Colors.GREEN + "Your barrows amulet(s) will no longer take effect automatically.");
            player.barrowsAmuletActivated = false;
        } else {
            player.sendMessage(Colors.GREEN + "Your barrows amulet(s) will now take effect automatically.");
            player.barrowsAmuletActivated = true;
        }
    }

    public static boolean quickUse(Player player, int objectId) {
        if (player.barrowsAmuletActivated && player.getInventory().containsItem(BARROWS_AMULET_ID, 1)) {
            return use(player, objectId, false);
        }
        return false;
    }

    public static boolean use(Player player, int objectId, boolean sendMessages) {
        if (player.barrowsAmuletUses >= 3) {
            if (sendMessages)
                player.sendMessage("A magical force prevents you from shattering the amulet.");
            return false;
        }
        if (objectId == 6775) {
            int hidden = player.getHiddenBrother();
            if (hidden == -1) {
                return false;
            }
            if (player.getKilledBarrowBrothers()[hidden]) {
                if (sendMessages)
                    player.sendMessage("You've already killed this brother.");
                return false;
            }
            player.getKilledBarrowBrothers()[hidden] = true;
        } else {
            boolean found = false;
            for (BarrowsConstants bro : BarrowsConstants.values()) {
                int index = bro.ordinal();
                if (bro.getSaprcophagusId() != objectId) {
                    continue;
                }
                if (player.getKilledBarrowBrothers()[index]) {
                    if (sendMessages)
                        player.sendMessage("You've already killed this brother.");
                    return false;
                }
                if (player.getHiddenBrother() == index) {
                    if (sendMessages)
                        player.sendMessage("I should use this on the chest instead.");
                    return false;
                }
                player.getKilledBarrowBrothers()[index] = true;
                found = true;
            }

            if (!found) {
                if (sendMessages)
                    player.sendMessage("You need to use this on sarcophagus or chest at barrows.");
                return false;
            }
        }
        player.sendMessage("A cry echoes around the tomb as you shatter the amulet.");
        player.barrowsAmuletUses++;
        player.getInventory().deleteItem(BARROWS_AMULET_ID, 1);
        Barrows.updateInterface(player, false);
        return true;
    }


    public static void resetUses(Player player) {
        player.barrowsAmuletUses = 0;
    }
}