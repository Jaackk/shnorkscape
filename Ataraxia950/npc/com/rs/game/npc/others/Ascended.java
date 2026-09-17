package com.rs.game.npc.others;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.NPCCombatDefinition;
import com.rs.game.player.Player;
import com.rs.game.player.content.contracts.ContractHandler;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

@SuppressWarnings("serial")
public class Ascended extends NPC {

    private long healDelay;

    private final Legios legio;

    public Ascended(int id, WorldTile tile, Legios legio) {
        super(id, tile, -1, true, true);
        this.legio = legio;
        setForceMultiArea(true);
        setIntelligentRouteFinder(true);
        setForceAgressive(true);
        setForceTargetDistance(64);
    }

    @Override
    public boolean checkAgressivity() {
        if (getId() == 17146)
            return false;
        return super.checkAgressivity();
    }

    @Override
    public void processNPC() {
        if (getId() == 17146 && legio != null && !legio.hasFinished() && Utils.currentTimeMillis() > healDelay) {
            calcFollow(legio.transform(1, 0, 0), getRun() ? 2 : 1, true, isIntelligentRouteFinder());
            if (Utils.isOnRange(legio, this, 0)) {
                this.faceEntity(legio);
                legio.heal(10, 0, 0, true);
                healDelay = Utils.currentTimeMillis() + 1800;
            }
            return;
        }
        super.processNPC();
    }

    @Override
    public void sendDeath(Entity source) {
        final NPCCombatDefinition defs = getCombatDefinitions();
        resetWalkSteps();
        getCombat().removeTarget();
        if (source instanceof Player) {
            source.deathResetCombat();
        }
        setNextAnimation(null);
        WorldTasksManager.schedule(new WorldTask() {
            int loop;

            @Override
            public void run() {
                if (loop == 0) {
                    setNextAnimation(new Animation(defs.getDeathEmote()));
                } else if (loop >= defs.getDeathDelay()) {
                    if (source instanceof Player) {
                        ((Player) source).getControlerManager().processNPCDeath(Ascended.this);
                        ContractHandler.updateContract(((Player) source), Ascended.this);
                    }
                    reset();
                    finish();
                    stop();
                }
                loop++;
            }
        }, 0, 1);
    }

}
