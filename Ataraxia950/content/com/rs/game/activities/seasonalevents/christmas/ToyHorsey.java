package com.rs.game.activities.seasonalevents.christmas;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.rs.game.Animation;
import com.rs.game.ForceTalk;
import com.rs.game.player.Player;
import com.rs.utils.Utils;
import lombok.val;

import java.util.HashMap;
import java.util.Map;

/**
 * @author lare96
 */
public enum ToyHorsey {
    LIGHT_BROWN(2520, 918),
    BROWN(2522, 919),
    WHITE(2524, 920),
    GREY(2526, 921);

    private static final ImmutableList<String> SPEECH = ImmutableList.of("Neaahhhyyy!", "Giddy-up horsey!",
            "Come on Dobbin, we can win the race!", "Yeeeeehaaaaa!", "Cowboyyyyy!", "Hi-ho Silver, and away!");
    public static final ImmutableMap<Integer, ToyHorsey> ALL;
    private final int itemId;
    private final int animationId;

    static {
        Map<Integer, ToyHorsey> idMap = new HashMap<>();
        for (val next : values()) {
            idMap.put(next.itemId, next);
        }
        ALL = ImmutableMap.copyOf(idMap);
    }

    ToyHorsey(int itemId, int animationId) {
        this.itemId = itemId;
        this.animationId = animationId;
    }

    public int getItemId() {
        return itemId;
    }

    public int getAnimationId() {
        return animationId;
    }

    public static boolean playWith(Player player, int itemId) {
        if (player.isUnderCombat()) {
            return false;
        }
        ToyHorsey horsey = ALL.get(itemId);
        if (horsey == null) {
            return false;
        }
        player.resetWalkSteps();
        player.lock(4);
        player.setNextAnimation(new Animation(horsey.animationId));
        player.setNextForceTalk(new ForceTalk(Utils.randomFrom(SPEECH)));
        return true;
    }
}
