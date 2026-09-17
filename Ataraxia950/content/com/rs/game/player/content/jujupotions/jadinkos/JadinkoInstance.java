package com.rs.game.player.content.jujupotions.jadinkos;

import com.google.common.collect.ImmutableList;
import com.rs.game.MapInstance;
import com.rs.game.WorldTile;
import com.rs.game.player.Player;
import com.rs.game.player.content.Magic;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.Colors;
import lombok.Getter;
import lombok.val;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * @author lare96 <http://github.com/lare96>
 */
public final class JadinkoInstance {

        @Getter
        private final MapInstance map;

        @Getter
        private final WorldTile enterLocation;

        @Getter
        private final ImmutableList<JadinkoSpawn> spawnJadinkos;

    @Getter
    private final Set<Player> players = new HashSet<>();

    public JadinkoInstance(int regionX, int regionY, WorldTile enterLocation, ImmutableList<JadinkoSpawn> spawnJadinkos) {
        this.enterLocation = enterLocation;
        this.spawnJadinkos = spawnJadinkos;
        map = new MapInstance(regionX, regionY);
    }

    public void enter(Player player) {
        if (map.getStage() != MapInstance.Stages.RUNNING) {
            Dialogue.sendSingleNPCDialogue(player, 17508, Dialogue.NORMAL,
                    "Error loading this instance, please report to an Admin/Developer.");
        } else {
            Magic.vineTeleport(player, map.getInstanceTile(enterLocation));
            player.getControlerManager().startControler(new JadinkoInstanceController(this));
        }
    }

    public void kick(Player player) {
        kick(player, false);
    }


    public void kick(Player player, boolean instant) {
        if (player.getControlerManager().getControler() instanceof JadinkoInstanceController) {
            players.remove(player);
            if (instant) {
                player.setLocation(player.getHomeTile());
            } else {
                player.setNextWorldTile(player.getHomeTile());
            }
            player.getControlerManager().forceStop();
        }
    }

    public void kickAll() {
        new ArrayList<>(players).forEach(plr -> {
            plr.sendMessage(Colors.RED + "God Jadinkos have went back into hiding, you have been kicked from the instance!");
            kick(plr);
        });
    }

    public void load() {
        map.loadSafe(() -> {
            for (val jadinko : spawnJadinkos) {
                jadinko.setInstance(JadinkoInstance.this);
                jadinko.spawn();
            }
        });
    }
}
