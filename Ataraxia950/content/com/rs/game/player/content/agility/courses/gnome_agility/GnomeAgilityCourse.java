package com.rs.game.player.content.agility.courses.gnome_agility;

import com.rs.game.player.content.agility.courses.AgilityCourse;

public final class GnomeAgilityCourse extends AgilityCourse {

    @Override
    public double getCompletionXp() {
        return 25.0;
    }

    @Override
    public void loadObstacles() {
        obstacles.add(new GnomeLogObstacle(this));
    }

}
