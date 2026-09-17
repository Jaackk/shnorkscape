package com.rs.utils.data.parsers.npcs.pojos;

import lombok.Data;

@Data
public class NPCSpawn {
    private final int npcId;
    private final NPCDirection direction;
    private final NPCSpawnLocation location;
    private final int mapAreaNameHash;
    private final boolean canBeAttackedFromOutOfArea;
    private final boolean canMove;

    public NPCSpawn() {
        npcId = -1;
        direction = null;
        location = null;
        mapAreaNameHash = -1;
        canBeAttackedFromOutOfArea = true;
        canMove = true;
    }
}
