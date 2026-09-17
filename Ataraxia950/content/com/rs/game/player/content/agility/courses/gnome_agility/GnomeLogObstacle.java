package com.rs.game.player.content.agility.courses.gnome_agility;


import com.rs.game.WorldTile;
import com.rs.game.player.Player;
import com.rs.game.player.content.agility.courses.AgilityObstacle;
import com.rs.game.player.content.agility.courses.Course;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;

final class GnomeLogObstacle extends AgilityObstacle {

    GnomeLogObstacle(Course course) {
        super(course);
    }

    @Override
    public void succeed(Player player, boolean start) {
        final WorldTile to = getToTile();
        player.lock();
        player.sendMessage("You start to walk across the log...");
        player.addWalkSteps(to.getX(), to.getY(), -1, false);
        WorldTasksManager.schedule(new WorldTask() {
            boolean secondLoop;

            @Override
            public void run() {
                if(!secondLoop) {
                    secondLoop = true;
                    player.getAppearence().setRenderEmote(155);
                } else {
                    player.getAppearence().setRenderEmote(-1);
                    player.sendMessage("... and make it safely to the other side.", true);
                    player.unlock();
                    stop();
                }
            }
        }, 0, 6);
    }

    @Override
    public int getStage() {
        return 1;
    }

    @Override
    public int getLevelReq() {
        return 1;
    }

    @Override
    public double getExperience() {
        return 7.5;
    }

    @Override
    public int getObjectId() {
        return 69526;
    }

    @Override
    public WorldTile getStartTile() {
        return new WorldTile(2474, 3436, 1);
    }

    @Override
    public WorldTile getToTile() {
        return new WorldTile(2474, 3249, 10);
    }
}
