package com.rs.game.npc.spiderboss;

import com.rs.game.hitbar.HitBar;
import com.rs.utils.Utils;

public class AcidicSpiderHitBar extends HitBar {
    private final AraxxorMinion minion;

    public AcidicSpiderHitBar(AraxxorMinion minion) {
        this.minion = minion;
    }

    @Override
    public int getType() {
        return 5;
    }

    @Override
    public int getPercentage() {
        long time = Utils.currentTimeMillis() - minion.getAcidicSpiderDeathCycle();
        return (int) ((((double) time / 60.00) * 255.00) / 100.00);
    }

}
