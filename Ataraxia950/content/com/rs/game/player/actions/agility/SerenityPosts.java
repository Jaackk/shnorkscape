package com.rs.game.player.actions.agility;

import com.rs.game.Animation;
import com.rs.game.WorldTile;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.Action;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import lombok.Getter;

/**
 * ataraxia-server
 * paolo 06/06/2019
 * #Shnek6969
 */
public class SerenityPosts extends Action {

    //represents the animation Id of jumping of the stone
    private final int JUMP_OFF = 24542;
    private final int REQUIRED_LEVEL = 75;
    //represents the current pose the playing is doing
    private SERENINITY_POSE currentPose;

    @Override
    public boolean process(Player player) {
        if(currentPose == null)
            return false;
        player.getDialogueManager().startDialogue(new Dialogue() { //need to re-open the dialogue in order to make it work

            @Override
            public void start() {
                sendOptionsDialogue("Select a pose", "Bom","Crane", "Lotus", "Ward", "Stop");
                stage = 1;
            }

            @Override
            public void run(int interfaceId, int componentId) {
                if(stage == 1){
                    if(componentId == OPTION_1){
                        player.setNextAnimation(new Animation(SERENINITY_POSE.BOM.getAnimationId()));
                        currentPose = SERENINITY_POSE.BOM;
                    } else if(componentId == OPTION_2){
                        player.setNextAnimation(new Animation(SERENINITY_POSE.CRANE.getAnimationId()));
                        currentPose = SERENINITY_POSE.CRANE;
                    } else if(componentId == OPTION_3){
                        player.setNextAnimation(new Animation(SERENINITY_POSE.LOTUS.getAnimationId()));
                        currentPose = SERENINITY_POSE.LOTUS;
                    }else if(componentId == OPTION_4){
                        player.setNextAnimation(new Animation(SERENINITY_POSE.WARD.getAnimationId()));
                        currentPose = SERENINITY_POSE.WARD;
                    }else if(componentId == OPTION_5){
                        currentPose = null;
                        stop(player);
                        end();

                    }
                }

            }

            @Override
            public void finish() {} });
        return true;
    }


    @Override
    public int processWithDelay(Player player) {
        if(currentPose == SerenityPostsHandler.CURRENT_POSE)
            player.getSkills().addXp(Skills.AGILITY,40);
        else
            player.getSkills().addXp(Skills.AGILITY,20);
        return 4;
    }

    @Override
    public boolean start(Player player) {
        if (player.getSkills().getLevels()[Skills.AGILITY] < REQUIRED_LEVEL) {
            player.sm("You need an agility level of " + REQUIRED_LEVEL + " to use this.");
            return false;
        }
        player.setNextAnimation(new Animation(SERENINITY_POSE.CRANE.getAnimationId())); //looks like this is the start pose in rs3
        currentPose = SERENINITY_POSE.CRANE;
        return true;
    }

    @Override
    public void stop(Player player) {
        player.lock();
        player.setNextAnimation(new Animation(JUMP_OFF));
        int tileDifference = player.getY() == 3403 ? -1 : +1;
        WorldTasksManager.schedule(new WorldTask() {
            @Override
            public void run() {
                player.setNextWorldTile(new WorldTile(player.getX(), player.getY() +tileDifference,player.getPlane()));
                player.setNextAnimation(new Animation(-1));
                player.unlock();
                stop();
            }
        }, 3, 1);


    }
}

/**
 * add poses here
 */
 enum SERENINITY_POSE {

     BOM(25008),
     LOTUS(25009),
     WARD(25010),
     CRANE(25006);

     @Getter
     private final int animationId;

     SERENINITY_POSE(int animationId){
         this.animationId = animationId;
     }


}
