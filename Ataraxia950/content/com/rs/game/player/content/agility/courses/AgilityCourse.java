package com.rs.game.player.content.agility.courses;

import java.util.List;

public abstract class AgilityCourse implements Course {

    protected List<Obstacle> obstacles;

    public AgilityCourse() {
        loadObstacles();
        sortObstacles();
    }

    @Override
    public final List<Obstacle> getObstacles() {
        return obstacles;
    }

    private void sortObstacles() {
        obstacles.sort((o1, o2) ->
                Integer.compare(o2.getStage(), o1.getStage()));
    }

}
