package com.rs.game.player.content.eds.rooms.ed2;

import java.util.Arrays;

import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.npc.eds.EliteDungeonNPC;
import com.rs.game.npc.eds.SimpleEliteDungeonBoss;
import com.rs.game.npc.eds.SimpleEliteDungeonNPC;
import com.rs.game.player.content.eds.EliteDungeonsConstants;
import com.rs.game.player.content.eds.rooms.EliteDungeonHandledRoom;

public class Room2 extends EliteDungeonHandledRoom {

    private static final long serialVersionUID = 8333758126204013345L;

    private SimpleEliteDungeonBoss boss;

    @Override
    public void load() {
        super.load();
        addNPC(new SimpleEliteDungeonNPC(25672, getTile(new WorldTile(5070, 9265, 0)), this));
        addNPC(new SimpleEliteDungeonNPC(25673, getTile(new WorldTile(5073, 9255, 0)), this));
        addNPC(new SimpleEliteDungeonNPC(25674, getTile(new WorldTile(5079, 9268, 0)), this));
        addNPC(new SimpleEliteDungeonNPC(25675, getTile(new WorldTile(5082, 9252, 0)), this));

        boss = new SimpleEliteDungeonBoss(25656, getTile(new WorldTile(5090, 9260, 0)), this, 40394, new WorldTile(5090, 9260, 0), 18);
        addNPC(boss).setNextFaceWorldTile(boss.transform(-1, 0, 0));
        addBlock(new WorldObject(111701, 10, 0, getTile(new WorldTile(5100, 9260, 0))), 111763, Arrays.<EliteDungeonNPC>asList(boss));
    }

    @Override
    public int getEliteDungeonType() {
        return EliteDungeonsConstants.DRAGONKIN_LABORATORY;
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
