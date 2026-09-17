package com.rs.game.player.content.lootshare;

import com.rs.game.item.Item;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import com.rs.game.player.content.FriendChatsManager;
import com.rs.utils.Colors;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author lare96 <http://github.com/lare96>
 */
public final class LootShare {

    /**
     * The LootShare radius (must be within {@code x} spaces to get shards).
     */
    public static final int LOOTSHARE_RADIUS = 20;

    public static boolean shareLoot(Player player, NPC killed, Item item) {
        if (killed.getId() == 17182 ||
                killed.getId() == 17182 ||
                killed.getId() == 17182) {
            player.sendFilteredMessage("LootShare in battles with Vorago have been disabled.");
            return false;
        }
        List<Player> sharing = FriendChatsManager.getLootSharingPeople(player, killed);
        if (sharing == null || sharing.isEmpty()) {
            return false;
        }
        giveLoot(sharing, item);
        return true;
    }

    private static void giveLoot(List<Player> sharing, Item item) {
        int totalLsp = 0;
        for (Player player : sharing) {
            totalLsp += player.lsp;
        }

        int roll = ThreadLocalRandom.current().nextInt(totalLsp) + 1;
        int mod = 0;
        Player receiver = null;
        for (Player player : sharing) {
            mod += player.lsp;
            if (roll <= mod) {
                receiver = player;
                player.sendMessage(Colors.GREEN + "You received: " + formatLoot(item));
                player.getInventory().addItemDrop(item.getId(), item.getAmount());
                player.lsp = 1;
                break;
            }
        }

        if (receiver == null) {
            throw new IllegalStateException("Algorithm could not compute LootShare receiver.");
        }

        for (Player player : sharing) {
            if (receiver.equals(player)) {
                continue;
            }
            player.lsp++;
            player.sendMessage(receiver.getDisplayName() + " received: " + formatLoot(item));
            player.sendFilteredMessage("Your chance of receiving loot has improved.");
        }
    }

    private static String formatLoot(Item item) {
        StringBuilder sb = new StringBuilder(item.getName());
        if (item.getAmount() > 1) {
            sb.append(' ').append("(x").append(item.getAmount()).append(").");
        } else {
            sb.append(".");
        }
        return sb.toString();
    }
}