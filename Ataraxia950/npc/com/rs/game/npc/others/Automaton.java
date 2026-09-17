package com.rs.game.npc.others;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.NPCCombatDefinition;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;

@SuppressWarnings("serial")
public class Automaton extends NPC {

    public Automaton(final int id, final WorldTile tile) {
        super(id, tile, -1, true, true);
    }

    @Override
    public void processNPC() {
        super.processNPC();
    }

    @Override
    public double getMeleePrayerMultiplier() {
        return 0.4;
    }


    @Override
    public double getMagePrayerMultiplier() {
        return 0.4;
    }

    @Override
    public double getRangePrayerMultiplier() {
        return 0.4;
    }

    @Override
    public void sendDeath(final Entity source) {
        final NPCCombatDefinition defs = getCombatDefinitions();

        WorldTasksManager.schedule(new WorldTask() {
            int loop;

            @Override
            public void run() {
                if (loop == 0) {
                    setNextAnimation(new Animation(defs.getDeathEmote()));
                } else if (loop == 3) {
                    drop();
                    reset();
                    getCombat().removeTarget();
                    setLocation(getRespawnTile());
                    finish();
                    setRespawnTask();
                    stop();
                }
                loop++;
            }
        }, 0, 1);
    }

}
