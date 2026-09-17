package com.rs.game.player.content.agility.courses;


import com.rs.game.player.Player;
import com.rs.game.player.Skills;

import java.util.List;

public interface Course {

    double getCompletionXp();

    List<Obstacle> getObstacles();

    void loadObstacles();

    default void finish(final Player player) {
        player.getSkills().addSkillXpRefresh(Skills.AGILITY, getCompletionXp());
    }


}
