package com.rs.game.player.client;
import com.rs.game.Animation;import com.rs.game.player.Player;import com.rs.game.player.Skills;import com.rs.game.player.actions.Action;
/** Original910 heat/progress cadence, with current-cache work and atomic completion. */
public class Native950SmithingAction extends Action {
    private final Native950Production.Recipe recipe;private final int required,tier,bars,upgrade;
    private int remaining,progress,heat,maxHeat;private boolean reported;
    public Native950SmithingAction(Native950Production.Recipe recipe,int quantity,int required,int tier,int bars,int upgrade){
        if(recipe==null||quantity<1||quantity>10000||required<1||required>100000||tier<0||tier>99||bars<1||bars>27||upgrade<0||upgrade>10)throw new IllegalArgumentException();
        this.recipe=recipe;remaining=quantity;this.required=required;this.tier=tier;this.bars=bars;this.upgrade=upgrade;
    }
    protected boolean environment(Player p){return true;}
    private void reset(Player p){progress=0;maxHeat=Math.max(600,Math.min(1200,1000+Math.min(200,Math.max(0,p.getSkills().getLevel(Skills.SMITHING)-recipe.level)*8)-upgrade*20));heat=maxHeat;}
    @Override public boolean start(Player p){String reason=recipe.refusal(p);if(reason!=null){p.sendMessage(reason);return false;}if(!environment(p))return false;reset(p);gauges(p);setActionDelay(p,1);return true;}
    @Override public boolean process(Player p){if(remaining<1||!environment(p))return false;String reason=recipe.refusal(p);if(reason!=null&&!reported&&Native950Production.Recipe.ready(p)){p.sendMessage(reason);reported=true;}return reason==null;}
    @Override public int processWithDelay(Player p){
        if(!process(p))return -1;
        int percent=heat*100/maxHeat;double multiplier=percent>=67?2.0:percent>=34?1.0:percent==0?0.5:0.66;
        int base=28+Math.max(0,p.getSkills().getLevel(Skills.SMITHING)-recipe.level)/2+Math.max(0,tier/20);
        progress+=Math.max(1,(int)Math.round(base*multiplier));heat=Math.max(0,heat-(70+bars*4+Math.max(0,tier-50)/3+upgrade*10));
        if(recipe.animation>=0)p.setNextAnimation(new Animation(recipe.animation));gauges(p);
        if(progress>=required){if(!recipe.complete(p))return -1;remaining--;if(remaining==0)return -1;reset(p);}
        return 2;
    }
    private void gauges(Player p){p.getNextHitBars().removeIf(b->b instanceof Native950SmithingGauge);p.getNextHitBars().add(Native950SmithingGauge.heat(heat*100/maxHeat));p.getNextHitBars().add(Native950SmithingGauge.progress((int)Math.min(100L,progress*100L/required)));}
    @Override public void stop(Player p){p.setNextAnimation(new Animation(-1));p.getNextHitBars().removeIf(b->b instanceof Native950SmithingGauge);p.getNextHitBars().add(Native950SmithingGauge.removeHeat());p.getNextHitBars().add(Native950SmithingGauge.removeProgress());setActionDelay(p,1);}
}
