package com.rs.game.player.content.skillingcontracts;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

/**
 * @author lare96 <http://github.com/lare96>
 */
public enum TempContractEffect {
    REDUCE_TASK_AMOUNT("Reduce the duration of all contracts in half.", 250),
    FIFTEEN_PERCENT_MORE_XP("Receive 15% more experience from all contracts.", 100);

    public static final int DURATION = 10; // 10 tasks.
    public static final ImmutableList<TempContractEffect> ALL = ImmutableList.copyOf(values());
    public static final ImmutableMap<TempContractEffect, Integer> EFFECT_OPTIONS;
    public static final ImmutableMap<Integer, TempContractEffect> OPTIONS_EFFECT;

    static {
        BiMap<TempContractEffect, Integer> map = HashBiMap.create();
        for (TempContractEffect e : ALL) {
            map.put(e, e.option);
        }
        EFFECT_OPTIONS = ImmutableMap.copyOf(map);
        OPTIONS_EFFECT = ImmutableMap.copyOf(map.inverse());
    }

    public final String description;
    public final int cost;
    public final int option;

    TempContractEffect(String description, int cost) {
        this.description = description;
        this.cost = cost;
        option = ordinal() + 1;
    }
}