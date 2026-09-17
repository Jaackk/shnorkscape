package com.rs.game.map.bossInstance.impl.pz;

import com.rs.Settings;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Logger;

import lombok.val;

/**
 * @author Kris | 9. sept 2018 : 21:47:15
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server
 *      profile</a>
 * @see <a href="https://rune-status.net/members/kris.354/">Rune-Status
 *      profile</a>
 */
public interface InstancedNPC {

    int RESPAWN_DELAY = InstanceConstants.RESPAWN_DELAY;

    default void setRespawnTask(PZInstance instance) {
        if (!(this instanceof NPC)) {
            return;
        }
        if (instance == null || instance.getPlayers() == null || instance.getPlayers().isEmpty() || instance.isFinished())
            return;
        if(!instance.getSettings().hasTimeRemaining()) {
            if(!instance.isSentNoTimeRemainingMessage())
            for(Player player : instance.getPlayers()) {
                if(player == null || player.hasFinished() || player.isDead())
                    continue;
                player.getPackets().sendMainInterfaceMessage(1, "Monsters will no longer respawn as the instance has run out of time.", true);
            }
            instance.setSentNoTimeRemainingMessage(true);
            return;
        }
        val npc = (NPC) this;
        if (!npc.hasFinished()) {
            npc.reset();
            npc.setLocation(npc.getRespawnTile());
            npc.finish();
        }
        if (Settings.DEBUG) {
            Logger.getGlobal().info("Respawn task initiated: [" + npc.getName() + "]; time: [" + RESPAWN_DELAY + "].");
        }
        WorldTasksManager.schedule(new WorldTask() {
            @Override
            public void run() {
                try {
                    if (instance == null || instance.getPlayers() == null || instance.getPlayers().isEmpty() || instance.isFinished())
                        return;
                    npc.spawn();
                } catch (final Throwable e) {
                    Logger.getGlobal().catching(e);
                }
            }
        }, RESPAWN_DELAY);
    }

}
