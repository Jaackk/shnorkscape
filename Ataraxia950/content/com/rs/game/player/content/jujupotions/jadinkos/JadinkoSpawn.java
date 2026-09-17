package com.rs.game.player.content.jujupotions.jadinkos;

import com.rs.game.World;
import com.rs.game.npc.NPC;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.val;

import java.util.concurrent.ThreadLocalRandom;

import static com.rs.game.npc.NPC.NORMAL_WALK;

@RequiredArgsConstructor
@Getter
public final class JadinkoSpawn {
    private final int id;
    private final int x;
    private final int y;
    private final int radius;
    private final int amount;
    @Setter
    private JadinkoInstance instance;

    public void spawn() {
        if (instance == null)
            throw new IllegalStateException("instance == null");
        for (int i = 0; i < amount; i++) {
            val tile = instance.getMap().getInstanceTile(x + ThreadLocalRandom.current().nextInt(radius), y + ThreadLocalRandom.current().nextInt(radius), 0);
                World.spawnNPC(new NPC(id, tile, -1, false, false, false)).setRandomWalk(NORMAL_WALK);
        }
    }
}
