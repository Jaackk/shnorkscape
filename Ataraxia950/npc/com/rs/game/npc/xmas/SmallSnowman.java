package com.rs.game.npc.xmas;

import com.rs.game.Entity;
import com.rs.game.Hit;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Colors;
import com.rs.utils.Utils;

public class SmallSnowman extends NPC {

    private static final long serialVersionUID = -4135756918435060516L;
    public transient Player attacker;

    public SmallSnowman(int id, WorldTile tile) {
        super(id, tile, -1, true, true);
        super.setNextFaceWorldTile(new WorldTile(Utils.random(0, 2) + tile.getX() - Utils.random(0, 2), Utils.random(0, 2) + tile.getY() - Utils.random(0, 2), tile.getPlane()));
    }

    @Override
    public void handleIngoingHit(Hit hit) {

        if (hit.getSource() instanceof Player)
            attacker = (Player) hit.getSource();

        if (attacker == null)
            return;

        faceEntity(attacker);
        attacker.getXmas().inThrow = false;
        super.handleIngoingHit(hit);
    }

    @Override
    public void sendDeath(Entity source) {
        Player killer = getMostDamageReceivedSourcePlayer();
        if (killer == null) {
            finish();
            setRespawnTask();
            super.sendDeath(source);
            return;
        }

        for (Entity t : getReceivedDamageSources()) {
            if (t instanceof Player) {
                ((Player) t).getXmas().inThrow = false;
            }
        }

        killer.getXmas().snowEnergy += 15;
        super.setNextAnimation(null);
        WorldTasksManager.schedule(new WorldTask() {

            @Override
            public void run() {
                if (killer.getInventory().hasFreeSlots())
                    killer.getInventory().addItem(new Item(33590, 10));
                killer.sendMessage("Total snow energy: " + Colors.RCYAN + Colors.SHAD + killer.getXmas().snowEnergy + "</shad></col> and total snowman kills: " + Colors.SHAD + Colors.DCYAN + killer.getXmas().snowmenKilled + "!", true);
                killer.getXmas().snowmenKilled += 1;
                finish();
                setRespawnTask();
            }

        }, 1);
        super.sendDeath(source);
    }

    @Override
    public void setRespawnTask() {
        final NPC npc = this;
        if (!hasFinished()) {
            reset();
            setLocation(getRespawnTile());
            finish();
        }
        WorldTasksManager.schedule(new WorldTask() {

            @Override
            public void run() {
                setFinished(false);
                World.addNPC(npc);
                npc.setLastRegionId(0);
                npc.reset();
                npc.setNextFaceWorldTile(new WorldTile(Utils.random(0, 2) + npc.getX() - Utils.random(0, 2), Utils.random(0, 2) + npc.getY() - Utils.random(0, 2), npc.getPlane()));
                World.updateEntityRegion(npc);
                loadMapRegions();
            }
        }, 4);
    }

    @Override
    public void processNPC() {
        super.processNPC();
        if (isDead())
            return;
    }
}
