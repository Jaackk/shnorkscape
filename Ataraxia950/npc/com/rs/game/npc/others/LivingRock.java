package com.rs.game.npc.others;

import com.rs.cores.CoresManager;
import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.NPCCombatDefinition;
import com.rs.game.player.Equipment;
import com.rs.game.player.Player;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Logger;
import com.rs.utils.Utils;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("serial")
public class LivingRock extends NPC {

    private Entity source;
    private long deathTime;

    public LivingRock(final int id, final WorldTile tile, final int mapAreaNameHash, final boolean canBeAttackFromOutOfArea, final boolean spawned) {
        super(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
        setForceTargetDistance(4);
    }

    public boolean canMine(final Player player) {
        return Utils.currentTimeMillis() - deathTime > 60000 || player == source;
    }

    @Override
    public ArrayList<Entity> getPossibleTargets() {
        final ArrayList<Entity> targets = getPossibleTargets(true, true);
        final ArrayList<Entity> targetsCleaned = new ArrayList<Entity>();
        for (final Entity t : targets) {
            if (t instanceof Player && hasItems((Player) t)) {
                continue;
            }
            targetsCleaned.add(t);
        }
        return targetsCleaned;
    }

    /**
     * Checks if the player has full skilling outfits.
     *
     * @param player The player to check.
     * @return if has required items.
     */
    private boolean hasItems(final Player player) {
        if (player == null) {
            return false;
        }
        final Equipment equipment = player.getEquipment();
        return (equipment.getHatId() == 20789 || equipment.getHatId() == 24427) && (equipment.getChestId() == 20791 || equipment.getChestId() == 24428) && (equipment.getLegsId() == 20790 || equipment.getLegsId() == 24429) && (equipment.getBootsId() == 20788 || equipment.getBootsId() == 24430);
    }

    @Override
    public void sendDeath(final Entity source) {
        final NPCCombatDefinition defs = getCombatDefinitions();
        resetWalkSteps();
        getCombat().removeTarget();
        setNextAnimation(null);
        WorldTasksManager.schedule(new WorldTask() {
            int loop;

            @Override
            public void run() {
                if (loop == 0) {
                    setNextAnimation(new Animation(defs.getDeathEmote()));
                } else if (loop >= defs.getDeathDelay()) {
                    drop();
                    reset();
                    transformIntoRemains(source);
                    stop();
                }
                loop++;
            }
        }, 0, 1);
    }

    public void takeRemains() {
        setNPC(8832 + (getId() - 8928));
        setLocation(getRespawnTile());
        setRandomWalk(1);
        finish();
        if (!isSpawned()) {
            setRespawnTask();
        }
    }

    public void transformIntoRemains(final Entity source) {
        this.source = source;
        deathTime = Utils.currentTimeMillis();
        final int remainsId = 8928 + (getId() - 8832);
        transformIntoNPC(remainsId);
        setRandomWalk(0);
        CoresManager.getServiceProvider().executeWithDelay(() -> {
            try {
                if (remainsId == getId()) {
                    takeRemains();
                }
            } catch (final Throwable e) {
                Logger.getGlobal().catching(e);
            }
        }, 3, TimeUnit.MINUTES);
    }
}