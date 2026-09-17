package com.rs.game.npc.gwd2.helwyr;

import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.WorldTile;
import com.rs.game.activities.instances.HelwyrInstance;
import com.rs.game.player.Player;
import com.rs.utils.Utils;

public class CMHelwyr extends Helwyr {

    private static final long serialVersionUID = 8176081567945245982L;
    private int howlStage;

    public CMHelwyr(int id, WorldTile tile, int mapAreaNameHash, boolean canBeAttackFromOutOfArea, boolean spawned, HelwyrInstance instance) {
        super(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned, instance);
    }

    public int getHowlStage() {
        return howlStage;
    }

    public void incrementHowlStage() {
        howlStage++;
    }

    @Override
    public void processNPC() {
        super.processNPC();
        for (Player p : getInstance().getPlayers()) {
            if (p == null)
                continue;
            if (p.getTemporaryAttributtes().get("bleed") != null) {
                if (p.getTemporaryAttributtes().remove("skiptick") != null)
                    continue;// This damage is applied every two ticks.
                final int bleed = (int) p.getTemporaryAttributtes().get("bleed");
                p.applyHit(new Hit(this, bleed, HitLook.REGULAR_DAMAGE));
                if (Utils.currentTimeMillis() - (p.getTemporaryAttributtes().get("bleedTime") == null ? 0 : (long) p.getTemporaryAttributtes().get("bleedTime")) > 15000) {
                    if (bleed <= 10) {
                        p.getTemporaryAttributtes().remove("bleed");
                        continue;
                    }
                    p.getTemporaryAttributtes().put("bleed", bleed - 10);
                    p.getTemporaryAttributtes().put("bleedTime", Utils.currentTimeMillis());
                }
                p.getTemporaryAttributtes().put("skiptick", true);
            }
        }
        if (getInstance().getTiles().size() == 0)
            return;
        getInstance().getPlayers().forEach(p -> {
            boolean inGas = false;
            for (int i = 0; i < getInstance().getTiles().size(); i++) {
                if (getInstance().getTiles().get(i) != null && p.withinDistance(getInstance().getTiles().get(i), 2)) {
                    p.applyHit(new Hit(this, Utils.random(20, 50), HitLook.REGULAR_DAMAGE));
                    inGas = true;
                    final long stunDelay = p.getTemporaryAttributtes().get("stunDelay") == null ? 0 : (long) p.getTemporaryAttributtes().get("stunDelay");
                    if (stunDelay == 0) {
                        if (!p.isFrozen())
                            p.getTemporaryAttributtes().put("stunDelay", Utils.currentTimeMillis());
                    } else {
                        if (stunDelay + 5000 < Utils.currentTimeMillis()) {
                            p.addFreezeDelay(5000);
                            p.resetWalkSteps();
                            p.sendMessage("You feel a little dizzy after standing in the gas for too long.");
                            p.getTemporaryAttributtes().remove("stunDelay");
                        }
                    }
                }
            }
            if (!inGas)
                p.getTemporaryAttributtes().remove("stunDelay");
        });
    }

}
