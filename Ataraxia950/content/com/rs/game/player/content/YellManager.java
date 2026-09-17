package com.rs.game.player.content;

import com.rs.game.World;
import com.rs.game.player.Player;
import com.rs.utils.Colors;
import com.rs.utils.Icons;
import com.rs.utils.Utils;

/**
 * Handles the player sent World Messages (Yell).
 *
 * @author Xenthium - rewritten 11/04/20
 */
public class YellManager {

    public static void sendYell(Player player, String yellMessage) {
        if (!canYell(player, yellMessage)) {
            return;
        }
        if (yellMessage.length() > 80) {
            yellMessage = yellMessage.substring(0, 80);
        }
        if (!player.isStaff()) {
            player.setYellDelay(getYellDelay(player));
        }
        World.sendWorldYellMessage(getYellPrefix(player) + Colors.wrap(getYellColor(player), Utils.fixChatMessage(yellMessage)), player);
    }

    private static boolean canYell(Player player, String yellMessage) {
        if (!player.isStaff()) {
            if (player.getMoneySpent() < 20) {
                player.sendMessage("You need to have donated a minimum of $20 to use the yell command.");
                return false;
            }
            if (player.isPermMuted() || player.getMuted() > System.currentTimeMillis()) {
                player.sendMessage(Colors.RED + "You're not able to use this feature while muted.");
                return false;
            }
            if (player.getYellDelay() > Utils.currentTimeMillis()) {
                player.sendMessage("You must wait another " + ((player.getYellDelay() - Utils.currentTimeMillis()) / 1000) + " seconds before sending another yell message.");
                return false;
            }
        }
        if (!player.isOwner()) {
            String[] restrictedPrefixes = {"<euro", "<img=", "<col=", "<shad=", "<str>", "<u>"};
            for (String prefix : restrictedPrefixes) {
                if (yellMessage.toLowerCase().contains(prefix)) {
                    player.sendMessage(Colors.RED + "Your yell cannot contain any message-altering prefixes.");
                    return false;
                }
            }
        }
        return true;
    }

    private static String getYellPrefix(Player player) {
        String prefix = "Player";
        if (player.getUsername().equalsIgnoreCase("xhybrid")) {
            prefix = Colors.wrap(Colors.ORANGE + Colors.SHAD, "Owner");
        } else if (player.getUsername().equalsIgnoreCase("kirita")) {
            prefix = Colors.wrap(Colors.CMT + Colors.SHAD, "Co-Owner");
        } else if (player.isFakeDev()) {
            prefix = Colors.wrap(Colors.DCYAN + Colors.SHAD, "Developer");
        } else if (player.getRights() == 2) {
            prefix = Colors.wrap(Colors.YELLOW, "Administrator");
        } else if (player.getRights() == 1) {
            prefix = Colors.wrap(Colors.GREEN, "Moderator");
        } else if (player.isSupport()) {
            prefix = Colors.wrap(Colors.PBLUE, "Support");
        } else if (player.isPlayerOfTheMonth() && player.isDisplayPlayerOfTheMonthIcon()) {
            prefix = "Player of the Month";
        } else if (player.isMasterMember()) {
            prefix = "Master";
        } else if (player.isDiamondMember()) {
            prefix = "Diamond";
        } else if (player.isPlatinumMember()) {
            prefix = "Platinum";
        } else if (player.isGoldMember()) {
            prefix = "Gold";
        } else if (player.isSilverMember()) {
            prefix = "Silver";
        } else if (player.isBronzeMember()) {
            prefix = "Bronze";
        }
        return "[" + (!prefix.contains("<col=") ? Colors.wrap(Colors.RED + Colors.SHAD, prefix) : prefix) + "] " + getYellIcon(player) + player.getDisplayName() + ": ";
    }

    private static String getYellIcon(Player player) {
        int id = -1;
        if (player.getUsername().equalsIgnoreCase("xhybrid")) {
            id = Icons.ORANGE_CROWN;
        } //else if (player.getUsername().equalsIgnoreCase("kirita")) {
            //id = Icons.CYAN_CROWN;
        //}

        else if (player.isFakeDev()) {
            id = Icons.DEVELOPER;
        } else if (player.getRights() == 2) {
            id = Icons.ADMINISTRATOR;
        } else if (player.getRights() == 1) {
            id = Icons.MODERATOR;
        } else if (player.isSupport()) {
            id = Icons.SUPPORT;
        } else if (player.isPlayerOfTheMonth() && player.isDisplayPlayerOfTheMonthIcon()) {
            id = Icons.PLAYER_OF_THE_MONTH;
        } else if (player.isMasterMember()) {
            id = Icons.MASTER_DONATOR;
        } else if (player.isDiamondMember()) {
            id = Icons.DIAMOND_DONATOR;
        } else if (player.isPlatinumMember()) {
            id = Icons.PLATINUM_DONATOR;
        } else if (player.isGoldMember()) {
            id = Icons.GOLD_DONATOR;
        } else if (player.isSilverMember()) {
            id = Icons.SILVER_DONATOR;
        } else if (player.isBronzeMember()) {
            id = Icons.BRONZE_DONATOR;
        }
        return id != -1 ? ("<img=" + id + ">") : "";
    }

    private static String getYellColor(Player player) {
        if (player.getUsername().equalsIgnoreCase("xhybrid")) {
            return Colors.ORANGE + Colors.SHAD;
        }
        if (player.getUsername().equalsIgnoreCase("kirita")) {
            return Colors.CMT + Colors.SHAD;
        }
        if (player.isFakeDev()) {
            return Colors.DCYAN + Colors.SHAD;
        }
        if (player.getRights() == 2) {
            return Colors.YELLOW + Colors.SHAD;
        }
        if (player.getRights() == 1) {
            return Colors.GREEN + Colors.SHAD;
        }
        if (player.isSupport()) {
            return Colors.PBLUE + Colors.SHAD;
        }
        return Colors.RED + Colors.SHAD;
    }

    private static long getYellDelay(Player player) {
        long cooldown = 300_000;
        if (player.isMasterMember()) {
            cooldown = 30_000;
        } else if (player.isDiamondMember()) {
            cooldown = 60_000;
        } else if (player.isPlatinumMember()) {
            cooldown = 90_000;
        } else if (player.isGoldMember()) {
            cooldown = 120_000;
        } else if (player.isSilverMember()) {
            cooldown = 150_000;
        }
        return cooldown;
    }
}