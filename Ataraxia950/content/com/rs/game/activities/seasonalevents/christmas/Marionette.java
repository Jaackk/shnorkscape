package com.rs.game.activities.seasonalevents.christmas;

import com.google.common.collect.ImmutableMap;
import com.rs.game.Animation;
import com.rs.game.Graphics;
import com.rs.game.player.Player;
import lombok.val;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * @author lare96
 */
public enum Marionette {
    RED(6867, 507, 508, 509, 510),
    BLUE(6865, 511, 512, 513, 514),
    GREEN(6866, 515, 516, 517, 518);

    private static final int JUMP_ID = 3003;
    private static final int WALK_ID  = 3004;
    private static final int BOW_ID = 3005;
    private static final int DANCE_ID = 3006;
    private static final ImmutableMap<Integer, Marionette> ALL;
    static {
        Map<Integer, Marionette> all = new HashMap<>();
        for(val next :values()) {
            all.put(next.itemId, next);
        }
        ALL = ImmutableMap.copyOf(all);
    }
    private final int itemId;
    private final int jumpId;
    private final int walkId;
    private final int bowId;
    private final int danceId;

    Marionette(int itemId, int jumpId, int walkId, int bowId, int danceId) {
        this.itemId = itemId;
        this.jumpId = jumpId;
        this.walkId = walkId;
        this.bowId = bowId;
        this.danceId = danceId;
    }

    public static boolean doJump(Player player, int itemId) {
        return forMarionette(player, itemId, JUMP_ID, marionette -> marionette.jumpId, 4);
    }

    public static boolean doWalk(Player player, int itemId) {
        return forMarionette(player, itemId, WALK_ID, marionette -> marionette.walkId, 8);
    }

    public static boolean doBow(Player player, int itemId) {
        return forMarionette(player, itemId, BOW_ID, marionette -> marionette.bowId, 4);
    }

    public static boolean doDance(Player player, int itemId) {
        return forMarionette(player, itemId, DANCE_ID, marionette -> marionette.danceId, 7);
    }

    private static boolean forMarionette(Player player, int itemId, int emoteId, Function<Marionette, Integer> gfxId, int lockDelay) {
        Marionette marionette = ALL.get(itemId);
        if(marionette == null) {
            return false;
        }
        player.setNextAnimation(new Animation(emoteId));
        player.setNextGraphics(new Graphics(gfxId.apply(marionette)));
        player.lock(lockDelay);
        return true;
    }
 }
