package com.rs.game.npc;

import com.rs.game.WorldTile;

public class StillNPC extends NPC {
    public StillNPC(int id, WorldTile tile) {
        super(id, tile, -1, false);
        setRandomWalk(0);
        setCannotMove(true);
    }
}
