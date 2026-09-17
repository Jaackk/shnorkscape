package com.rs.game;

import com.rs.game.hitbar.HitBar;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;

public class AdrenalineHitBar extends HitBar {

    public AdrenalineHitBar(NPC npc) {
        this.npc = npc;
    }

    private final NPC npc;

    @Override
    public int getPercentage() {
        return (npc.getCombat().getSpecialAttackPercentage() * 255 / 100);
    }

    @Override
    public int getType() {
        return 7;
    }

    @Override
    public boolean display(Player player) {
        return npc.getCombat().getSpecialAttackPercentage() > 0;
    }
}
