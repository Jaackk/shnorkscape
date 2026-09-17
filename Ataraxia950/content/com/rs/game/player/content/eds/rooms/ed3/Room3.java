package com.rs.game.player.content.eds.rooms.ed3;

import java.util.Arrays;

import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.npc.eds.EliteDungeonNPC;
import com.rs.game.npc.eds.SimpleEliteDungeonBoss;
import com.rs.game.npc.eds.SimpleEliteDungeonNPC;
import com.rs.game.player.content.eds.EliteDungeonsConstants;
import com.rs.game.player.content.eds.rooms.EliteDungeonHandledRoom;

public class Room3 extends EliteDungeonHandledRoom {

    private static final long serialVersionUID = 2374251244941732524L;

    private SimpleEliteDungeonBoss boss;

    @Override
    public void load() {
        super.load();
        addNPC(new SimpleEliteDungeonNPC(26158, getTile(new WorldTile(5538, 9056, 3)), this));
        addNPC(new SimpleEliteDungeonNPC(26164, getTile(new WorldTile(5538, 9048, 3)), this));
        addNPC(new SimpleEliteDungeonNPC(26063, getTile(new WorldTile(5546, 9058, 3)), this));
        addNPC(new SimpleEliteDungeonNPC(26066, getTile(new WorldTile(5546, 9046, 3)), this));

        boss = new SimpleEliteDungeonBoss(26062, getTile(new WorldTile(5556, 9052, 3)), this, 40394, new WorldTile(5556, 9052, 3), 20);
        addNPC(boss).setNextFaceWorldTile(boss.transform(-1, 0, 0));
        addBlock(new WorldObject(111701, 10, 0, getTile(new WorldTile(5570, 9052, 3))), 5999, Arrays.<EliteDungeonNPC>asList(boss));
    }

    @Override
    public int getEliteDungeonType() {
        return EliteDungeonsConstants.SHADOW_REEF;
    }

    @Override
    public int getRoomIndex() {
        return 2;
    }

    @Override
    public boolean isInBossFightArea(WorldTile tile) {
        return boss != null && boss.isInsideFightArea(tile);
    }
}
