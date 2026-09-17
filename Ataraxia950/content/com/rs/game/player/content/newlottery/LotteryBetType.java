package com.rs.game.player.content.newlottery;

import com.google.common.collect.ImmutableList;
import com.rs.utils.Utils;

/**
 * @author lare96 <http://github.com/lare96>
 */
public enum LotteryBetType {
    NEWBIE(25_000_000, 0.80, 2),
    REGULAR(50_000_000, 0.85, 2),
    GAMBLER(100_000_000, 0.90, 2),
    INVESTOR(150_000_000, 0.95, 3);

    public static final ImmutableList<LotteryBetType> ALL = ImmutableList.copyOf(values());
    public final int amount;
    public final double share;
    public final String formattedShare;
    public final int chance;
    public final String formattedName;

    LotteryBetType(int amount, double share, int chance) {
        this.amount = amount;
        this.share = share;
        this.chance = chance;
        formattedName = Utils.capitalize(name().toLowerCase());
        formattedShare = Integer.toString((int) (share * 100.0));
    }
}
