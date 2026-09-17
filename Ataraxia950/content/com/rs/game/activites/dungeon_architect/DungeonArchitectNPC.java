package com.rs.game.activites.dungeon_architect;

import com.rs.game.Entity;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;

public final class DungeonArchitectNPC extends NPC {

    private final DungeonArchitectInstance instance;
    private final DungeonArchitectMonster type;

    public DungeonArchitectNPC(DungeonArchitectInstance instance, DungeonArchitectMonster type, WorldTile tile) {
        super(type.npcId, tile, -1, false);
        this.instance = instance;
        this.type = type;
        setForceMultiAttacked(true);
        setCanBeAttackFromOutOfArea(true);
        getCombatDefinitions().setRespawnDelay(4);
    }

    @Override
    public void spawn() {
        super.spawn();
        instance.spawnedMonsters.add(this);
    }

    @Override
    public void sendDeath(Entity source) {
        super.sendDeath(source);
        instance.spawnedMonsters.remove(this);
    }
}