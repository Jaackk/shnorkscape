package com.rs.game.player.content.agility.courses;

import com.rs.game.player.Player;
import com.rs.game.player.content.agility.AgilityManager;

import java.util.HashMap;
import java.util.Map;

public final class CourseManager {

    private final Player player;
    private final Map<Course, Integer> stageMap;

    public CourseManager(Player player) {
        this.player = player;
        stageMap = new HashMap<>();
        AgilityManager.HANDLED_COURSES.forEach(c -> stageMap.put(c, 1));
    }

    void update(Course course, Obstacle obstacle) {

        int stage = stageMap.get(course);
        if (stage != obstacle.getStage()) {
            stageMap.put(course, 1);
            return;
        }
        if(stage == course.getObstacles().size()) {
            course.finish(player);
            stageMap.put(course, 1);
        } else {
            stageMap.put(course, ++stage);
        }


    }

}
