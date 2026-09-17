package com.rs.game.player.actions.agility;

import com.rs.cores.CoresManager;
import com.rs.cores.FixedLengthRunnable;
import com.rs.game.Animation;
import com.rs.game.ForceTalk;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

import java.util.concurrent.TimeUnit;

/**
 * ataraxia-server
 * paolo 06/06/2019
 * #Shnek6969
 */
public class SerenityPostsHandler {

    //object id of the post
    public static final int SERENITYPOST_OBJECT_ID = 93115;
    //delay when the pose should change, #timeunit = minutes
    private static final int DELAY_TIME = 1;
    //represents the current pose active
    public static SERENINITY_POSE CURRENT_POSE;
    //represents the npc that shows the pose
    private static NPC LADY_HEFIN;
    private static final int JUMP_ONN =24541;

    /**
     * repeater for pose changes
     */
    public static void initSerenPosts(){
        CoresManager.getServiceProvider().scheduleFixedLengthTask(new FixedLengthRunnable() {

        @Override
        public boolean repeat() {
            CURRENT_POSE = getRandomPose();
            changeLadyHefin();
            return true;
        }
         }, 0, DELAY_TIME, TimeUnit.MINUTES);
    }

    /**
     * handles the player jumping on the serenity stones
     * @param player
     * @param object
     */
    public static void jumpOnSereinityPost(Player player, WorldObject object){
        final WorldTile toTile = object;
        player.lock();
        player.setNextFaceWorldTile(object);
        player.setNextAnimation(new Animation(JUMP_ONN));
        final int lookDirection = object.getY() == 3403 ? -2 : +2;
        WorldTasksManager.schedule(new WorldTask() {
            @Override
            public void run() {
                player.setNextWorldTile(toTile);
                player.setNextFaceWorldTile((new WorldTile(player.getX(), player.getY() + lookDirection, player.getPlane())));
                player.getActionManager().setAction(new SerenityPosts());
                player.unlock();
                stop();
            }
        }, 1, 1);

    }

    /**
     * hanles the elve changing pose
     */
    private static void changeLadyHefin(){
        LADY_HEFIN = World.findNPC(20272);
        if(LADY_HEFIN != null) { //Gets init before npcs do, so we have to wait one cycle
            LADY_HEFIN.setNextForceTalk(new ForceTalk(CURRENT_POSE.name() + "."));
            LADY_HEFIN.setNextAnimation(new Animation(CURRENT_POSE.getAnimationId()));
        }
    }

    /**
     * returns a random pose
     * @return
     */
    private static SERENINITY_POSE getRandomPose(){
        return SERENINITY_POSE.values()[Utils.random(0, SERENINITY_POSE.values().length -1)];
    }


}
