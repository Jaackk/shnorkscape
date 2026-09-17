package com.rs.game.player.client;
import com.rs.game.Animation;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.actions.Action;
import java.util.*;
/** Original leather loop's five-item first thread reel and original later reel cadence, using a single atomic exchange. */
public final class Native950CraftingAction extends Action {
    private final Native950Production.Recipe recipe;private int remaining,threadRemaining=5;private boolean reported;
    public Native950CraftingAction(Native950Production.Recipe recipe,int quantity){if(recipe==null||quantity<1||quantity>10000)throw new IllegalArgumentException();this.recipe=recipe;remaining=quantity;}
    private Native950Production.Recipe current(){
        if(!recipe.label.startsWith("Sew:")||threadRemaining>1)return recipe;
        List<Item> inputs=new ArrayList<>(Arrays.asList(recipe.consumed()));inputs.add(new Item(1734,1));
        return new Native950Production.Recipe(recipe.label,recipe.skill,recipe.level,recipe.xp,recipe.animation,recipe.delay,inputs.toArray(new Item[0]),recipe.produced(),recipe.tools());
    }
    @Override public boolean start(Player p){String refusal=current().refusal(p);if(refusal!=null){p.sendMessage(refusal);return false;}setActionDelay(p,1);return true;}
    @Override public boolean process(Player p){if(remaining<1)return false;String reason=current().refusal(p);if(reason!=null&&!reported&&Native950Production.Recipe.ready(p)){p.sendMessage(reason);reported=true;}return reason==null;}
    @Override public int processWithDelay(Player p){if(!process(p)||!current().complete(p))return -1;remaining--;if(--threadRemaining==0)threadRemaining=com.rs.utils.Utils.random(2,6);p.setNextAnimation(new Animation(recipe.animation));return recipe.delay;}
    @Override public void stop(Player p){p.setNextAnimation(new Animation(-1));setActionDelay(p,1);}
}
