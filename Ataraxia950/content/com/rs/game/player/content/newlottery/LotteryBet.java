package com.rs.game.player.content.newlottery;

import com.rs.game.player.Player;

import java.util.concurrent.ThreadLocalRandom;

import static java.util.Objects.requireNonNull;

/**
 * @author lare96 <http://github.com/lare96>
 */
public class LotteryBet {
    public final String username;
    public final String displayName;
    public final LotteryBetType type;
    public final String mac;

    public LotteryBet(String username, String displayName, LotteryBetType type, String mac) {
        // For loading from database
        this.username = requireNonNull(username);
        this.displayName = requireNonNull(displayName);
        this.type = type;
        this.mac = mac;
    }

    public LotteryBet(String username, LotteryBetType type) {
        // For debugging only
        this(username, "null", type, Integer.toString(ThreadLocalRandom.current().nextInt()));
    }

    public LotteryBet(Player player, LotteryBetType type) {
        // For loading bet from player
        this(player.getUsername(), player.getDisplayName(), type, player.getCurrentMac());
    }
}
