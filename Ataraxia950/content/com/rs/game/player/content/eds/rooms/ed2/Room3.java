package com.rs.game.player.content.eds.rooms.ed2;

import java.util.Arrays;

import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.npc.eds.EliteDungeonNPC;
import com.rs.game.npc.eds.SimpleEliteDungeonBoss;
import com.rs.game.npc.eds.SimpleEliteDungeonNPC;
import com.rs.game.player.content.eds.EliteDungeonsConstants;
import com.rs.game.player.content.eds.rooms.EliteDungeonHandledRoom;

public class Room3 extends EliteDungeonHandledRoom {

    private static final long serialVersionUID = 3122606797712733461L;

    private SimpleEliteDungeonBoss boss;

    @Override
    public void load() {
        super.load();
        addNPC(new SimpleEliteDungeonNPC(25681, getTile(new WorldTile(5124, 9264, 0)), this));
        addNPC(new SimpleEliteDungeonNPC(25682, getTile(new WorldTile(5127, 9256, 0)), this));
        addNPC(new SimpleEliteDungeonNPC(25683, getTile(new WorldTile(5132, 9268, 0)), this));
        addNPC(new SimpleEliteDungeonNPC(25684, getTile(new WorldTile(5135, 9252, 0)), this));

        boss = new SimpleEliteDungeonBoss(25662, getTile(new WorldTile(5144, 9260, 0)), this, 40394, new WorldTile(5144, 9260, 0), 20);
        addNPC(boss).setNextFaceWorldTile(boss.transform(-1, 0, 0));
        addBlock(new WorldObject(111701, 10, 0, getTile(new WorldTile(5155, 9260, 0))), 111940, Arrays.<EliteDungeonNPC>asList(boss));
    }

    @Override
    public int getEliteDungeonType() {
        return EliteDungeonsConstants.DRAGONKIN_LABORATORY;
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
