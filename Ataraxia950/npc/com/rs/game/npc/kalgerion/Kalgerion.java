package com.rs.game.npc.kalgerion;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.Hit;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.NPCCombatDefinition;
import com.rs.game.player.Player;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

@SuppressWarnings("serial")
public class Kalgerion extends NPC {

    public boolean truePower;
    public boolean drag;

    public Kalgerion(final int id, final WorldTile tile) {
        super(id, tile, -1, true, true);

    }

    @Override
    public void handleIngoingHit(final Hit hit) {
        handlePrayers(hit);
        if(!drag && !truePower) {
            final Entity target = hit.getSource();

            // player handling for bonus damage w/ silverlight
            if(target instanceof Player) {
                final Player player = (Player) target;

                if(player != null && player instanceof Player) {
                    if(player.getEquipment().getWeaponId() == 6746) {
                        hit.setDamage(hit.getDamage() * 2);
                    }
                }
            }
        }

    }

    @Override
    public double getMeleePrayerMultiplier() {
        return 0.5;
    }


    @Override
    public double getMagePrayerMultiplier() {
        return 0.5;
    }

    @Override
    public double getRangePrayerMultiplier() {
        return 0.5;
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

    public WorldTile getDragWorldTile(final Entity target) {
        WorldTile tile;
        final NPC npc = this;

        final int dir = Utils.getDirectionBetweenTiles(npc, target);
        if (dir < 4097) {
            tile = new WorldTile(npc.getX() - 1, npc.getY(), npc.getPlane());
        } else if (dir < 8192) {
            tile = new WorldTile(npc.getX(), npc.getY() - 1, npc.getPlane());
        } else if (dir < 12288) {
            tile = new WorldTile(npc.getX(), npc.getY() + 1, npc.getPlane());
        } else {
            tile = new WorldTile(npc.getX() + 1, npc.getY(), npc.getPlane());
        }
        return tile;
    }

}
