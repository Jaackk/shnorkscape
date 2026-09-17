package com.rs.game.player.content.agility.courses;

import com.rs.game.WorldTile;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.content.RouteEvent;
import com.rs.game.player.content.agility.AgilityManager;
import com.rs.game.player.content.agility.Failable;
import com.rs.game.player.content.agility.Succeedable;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;

public interface Obstacle extends Succeedable {

    int getStage();

    int getLevelReq();

    double getExperience();

    int getObjectId();

    WorldTile getStartTile();

    WorldTile getToTile();

    Course getCourse();

    default void process(final Player player) {
        final WorldTile start = getStartTile();
        final boolean success = AgilityManager.calculateSuccess(player, this);
        player.setRouteEvent(new RouteEvent(start, () ->
            WorldTasksManager.schedule(new WorldTask() {
                @Override
                public void run() {
                    if(success)
                        succeed(player);
                    else ((Failable) Obstacle.this).fail(player);
                    final double xp = getExperience();
                    player.getSkills().addSkillXpRefresh(Skills.AGILITY, success ? xp : xp / 2);
                    if(success)
                        player.getCourseManager().update(getCourse(), Obstacle.this);
                }
            })));
    }

}
