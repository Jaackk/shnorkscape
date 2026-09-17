package com.rs.game.player.content.skillingcontracts;

import com.google.common.collect.ImmutableList;

/**
 * @author lare96 <http://github.com/lare96>
 */
public enum BlockLevel {
    // Had to be changed to serve a different purpose, bc serialization :/
    T30("Block up to 3 contracts.", 3, 100),
    T40("Block up to 4 contracts.", 4, 200),
    T50("Block up to 5 contracts.", 5, 300),
    T60("Block up to 6 contracts.", 6, 400),
    T70("Block up to 7 contracts.", 7, 500);

    public static final ImmutableList<BlockLevel> ALL = ImmutableList.copyOf(values());
    public final String description;
    public final int level;
    public final int points;

    BlockLevel(String description, int level, int points) {
        this.description = description;
        this.level = level;
        this.points = points;
    }
}