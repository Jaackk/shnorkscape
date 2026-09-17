package com.rs.game.player.content.agility.courses;

public abstract class AgilityObstacle implements Obstacle {

    protected Course course;

    public AgilityObstacle(Course course) {
        this.course = course;
    }

    @Override
    public final Course getCourse() {
        return course;
    }

}
