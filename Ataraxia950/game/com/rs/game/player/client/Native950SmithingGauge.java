package com.rs.game.player.client;
import com.rs.game.hitbar.HitBar;
/** Exact paired950 Smithing heat/progress gauges; values are immutable, player-only. */
public final class Native950SmithingGauge extends HitBar {
    public static final int HEAT=5, PROGRESS=7;
    private final int type,percent;private final boolean removal;
    private Native950SmithingGauge(int type,int percent,boolean removal){this.type=type;this.percent=Math.max(0,Math.min(100,percent))*255/100;this.removal=removal;}
    public static Native950SmithingGauge heat(int value){return new Native950SmithingGauge(HEAT,value,false);}
    public static Native950SmithingGauge progress(int value){return new Native950SmithingGauge(PROGRESS,value,false);}
    public static Native950SmithingGauge removeHeat(){return new Native950SmithingGauge(HEAT,0,true);}
    public static Native950SmithingGauge removeProgress(){return new Native950SmithingGauge(PROGRESS,0,true);}
    public int getType(){return type;}public int getPercentage(){return percent;}public boolean isRemoval(){return removal;}
}
