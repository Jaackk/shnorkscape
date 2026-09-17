package com.rs.game.activites.dnd.eviltree;

import com.google.common.collect.ImmutableList;

/**
 * @author lare96 <http://github.com/lare96>
 */
public enum EvilTreeInstanceLevel {
    EASY("Easy", 2, EvilTreeType.NORMAL, EvilTreeType.OAK, EvilTreeType.WILLOW),
    AVERAGE("Average", 3, EvilTreeType.WILLOW, EvilTreeType.MAPLE, EvilTreeType.YEW),
    HARD("Hard", 4, EvilTreeType.YEW, EvilTreeType.MAGIC, EvilTreeType.ELDER);

    public final String name;
    public final int maxPlayers;
    public final ImmutableList<EvilTreeType> trees;

    EvilTreeInstanceLevel(String name, int maxPlayers, EvilTreeType... trees) {
        this.name = name;
        this.maxPlayers = maxPlayers;
        this.trees = ImmutableList.copyOf(trees);
    }
}