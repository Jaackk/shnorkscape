package com.rs.game.activites.dungeon_architect;

import com.google.common.collect.ImmutableList;
import com.rs.cache.loaders.NPCDefinitions;

import java.util.Objects;

/**
 * Ordered from highest to lowest level.
 */
public enum DungeonArchitectMonster {
    SKELETAL_WARRIOR(90, 11984),
    ICE_ELEMENTAL(80, 10465),
    ICE_WARRIOR(70, 10235),
    ICEFIEND(60, 10217),
    EARTH_WARRIOR(50, 10186),
    GHOST(40, 10827),
    ARMOURED_ZOMBIE(30, 8164),
    SKELETON(20, 15309),
    DUNGEON_SPIDER(10, 9382),
    DUNGEON_RAT(1, 88);

    public static final ImmutableList<DungeonArchitectMonster> ALL = ImmutableList.copyOf(values());
    public final int level;
    public final int npcId;

    DungeonArchitectMonster(int level, int npcId) {
        this.level = level;
        this.npcId = npcId;
    }

    public String getNpcName() {
        return Objects.requireNonNull(NPCDefinitions.getNPCDefinitions(npcId)).name;
    }
}