package com.rs.game.player.client;
import com.rs.game.Animation;
import com.rs.game.player.Player;
import com.rs.game.player.actions.Action;

/** A bounded ordinary recipe loop on the original910 ActionManager and Skills pipeline. */
public class Native950ProductionAction extends Action {
    protected final Native950Production.Recipe recipe;
    private int remaining;
    private boolean reported;
    public Native950ProductionAction(Native950Production.Recipe recipe,int repetitions) {
        if(recipe==null||repetitions<1||repetitions>10000)throw new IllegalArgumentException("Invalid production amount");
        this.recipe=recipe;this.remaining=repetitions;
    }
    protected boolean environment(Player player){return true;}
    @Override public boolean start(Player player) {
        String refusal=recipe.refusal(player);
        if(refusal!=null){player.sendMessage(refusal);return false;}
        if(!environment(player))return false;
        setActionDelay(player,1);return true;
    }
    @Override public boolean process(Player player) {
        if(remaining<1||!environment(player))return false;
        String reason=recipe.refusal(player);
        if(reason!=null&&!reported&&Native950Production.Recipe.ready(player)){player.sendMessage(reason);reported=true;}
        return reason==null;
    }
    @Override public int processWithDelay(Player player) {
        if(!process(player))return -1;
        if(!recipe.complete(player)){if(!reported){player.sendMessage("You cannot use these materials here.");reported=true;}return -1;}
        remaining--;
        if(recipe.animation>=0)player.setNextAnimation(new Animation(recipe.animation));
        return recipe.delay;
    }
    @Override public void stop(Player player) {
        player.setNextAnimation(new Animation(-1));setActionDelay(player,1);
    }
}
