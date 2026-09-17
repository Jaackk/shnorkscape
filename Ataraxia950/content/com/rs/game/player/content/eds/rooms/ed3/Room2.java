package com.rs.game.player.content.eds.rooms.ed3;

import java.util.Arrays;

import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.npc.eds.EliteDungeonNPC;
import com.rs.game.npc.eds.SimpleEliteDungeonBoss;
import com.rs.game.npc.eds.SimpleEliteDungeonNPC;
import com.rs.game.player.content.eds.EliteDungeonsConstants;
import com.rs.game.player.content.eds.rooms.EliteDungeonHandledRoom;

public class Room2 extends EliteDungeonHandledRoom {

    private static final long serialVersionUID = -2755792740409245069L;

    private SimpleEliteDungeonBoss boss;

    @Override
    public void load() {
        super.load();
        addNPC(new SimpleEliteDungeonNPC(26163, getTile(new WorldTile(5500, 9089, 3)), this));
        addNPC(new SimpleEliteDungeonNPC(26165, getTile(new WorldTile(5502, 9081, 3)), this));
        addNPC(new SimpleEliteDungeonNPC(26170, getTile(new WorldTile(5511, 9090, 3)), this));
        addNPC(new SimpleEliteDungeonNPC(26175, getTile(new WorldTile(5513, 9080, 3)), this));

        boss = new SimpleEliteDungeonBoss(26053, getTile(new WorldTile(5520, 9085, 3)), this, 40394, new WorldTile(5520, 9085, 3), 18);
        addNPC(boss).setNextFaceWorldTile(boss.transform(-1, 0, 0));
        addBlock(new WorldObject(111701, 10, 0, getTile(new WorldTile(5528, 9085, 3))), 112746, Arrays.<EliteDungeonNPC>asList(boss));
    }

    @Override
    public int getEliteDungeonType() {
        return EliteDungeonsConstants.SHADOW_REEF;
    }

    @Override
    public int getRoomIndex() {
        return 1;
    }

    @Override
    public boolean isInBossFightArea(WorldTile tile) {
        return boss != null && boss.isInsideFightArea(tile);
    }
}
