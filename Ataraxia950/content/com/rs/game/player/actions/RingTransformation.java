package com.rs.game.player.actions;

import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.rs.game.Animation;
import com.rs.game.player.Player;
import com.rs.utils.Utils;

import lombok.val;

/**
 * @author lare96
 */
public enum RingTransformation {
    EASTER_RING(7927, ImmutableList.of(3689, 3690, 3691, 3692, 3693, 3694)),
    RING_OF_STONE(6583, 2626),
    RING_OF_SNOW(36165, ImmutableList.of(23769, 6742, 6743, 6744, 6745, 13637));

    static {
        Map<Integer, RingTransformation> idMap = new HashMap<>();
        for(val next : values()) {
            idMap.put(next.itemId, next);
        }
        RINGS =ImmutableMap.copyOf(idMap);
    }

    public static final ImmutableMap<Integer, RingTransformation> RINGS;
    private final int itemId;
    private final ImmutableList<Integer> transformIds;

    RingTransformation(int itemId, ImmutableList<Integer> transformIds) {
        this.itemId = itemId;
        this.transformIds = transformIds;
    }
    RingTransformation(int itemId, int transformId) {
        this(itemId, ImmutableList.of(transformId));
    }

    public ImmutableList<Integer> getTransformIds() {
        return transformIds;
    }

    public int computeTransformId() {
        return Utils.randomFrom(transformIds);
    }

    public static boolean tryTransform(Player player, int itemId) {
        if (player.getActionManager().getAction() != null) {
            return false;
        }
        RingTransformation transformation = RINGS.get(itemId);
        if(transformation == null) {
            return false;
        }
        player.getActionManager().setAction(new Action() {

            @Override
            public boolean start(Player player) {
                player.stopAll(true);
                player.getActionManager().forceStop();
                player.lock(3);
                int transformationId = transformation.computeTransformId();
                player.getAppearence().transformIntoNPC(transformationId);
                player.getInterfaceManager().sendInventoryInterface(375);
                return true;
            }

            @Override
            public boolean process(Player player) {
                return true;
            }

            @Override
            public int processWithDelay(Player player) {
                return 0;
            }

            @Override
            public void stop(Player player) {
                setActionDelay(player, 3);
                resetTransformation(player);
            }
        });
        return true;
    }

    public  static void resetTransformation(Player player) {
        player.lock(3);
        player.getInterfaceManager().closeInventoryInterface();
        player.getInventory().init();
        player.setNextAnimation(new Animation(14884));
        player.getAppearence().transformIntoNPC(-1);
    }
}
