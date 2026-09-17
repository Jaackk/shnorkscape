package com.rs.game.player.content.eds.rooms.ed2;

import java.util.Arrays;

import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.npc.eds.EliteDungeonNPC;
import com.rs.game.npc.eds.SimpleEliteDungeonBoss;
import com.rs.game.npc.eds.SimpleEliteDungeonNPC;
import com.rs.game.player.content.eds.EliteDungeonsConstants;
import com.rs.game.player.content.eds.rooms.EliteDungeonHandledRoom;

public class Room1 extends EliteDungeonHandledRoom {

    private static final long serialVersionUID = -7493254101820321147L;

    private SimpleEliteDungeonBoss boss;

    @Override
    public void load() {
        super.load();
        addNPC(new SimpleEliteDungeonNPC(25666, getTile(new WorldTile(5030, 9264, 0)), this));
        addNPC(new SimpleEliteDungeonNPC(25669, getTile(new WorldTile(5032, 9256, 0)), this));
        addNPC(new SimpleEliteDungeonNPC(25670, getTile(new WorldTile(5037, 9266, 0)), this));
        addNPC(new SimpleEliteDungeonNPC(25671, getTile(new WorldTile(5038, 9254, 0)), this));

        boss = new SimpleEliteDungeonBoss(25653, getTile(new WorldTile(5041, 9260, 0)), this, 40394, new WorldTile(5041, 9260, 0), 18);
        addNPC(boss).setNextFaceWorldTile(boss.transform(-1, 0, 0));
        addBlock(new WorldObject(111700, 10, 0, getTile(new WorldTile(5048, 9260, 0))), 111747, Arrays.<EliteDungeonNPC>asList(boss));
    }

    @Override
    public int getEliteDungeonType() {
        return EliteDungeonsConstants.DRAGONKIN_LABORATORY;
    }

    @Override
    public int getRoomIndex() {
        return 0;
    }

    @Override
    public boolean isInBossFightArea(WorldTile tile) {
        return boss != null && boss.isInsideFightArea(tile);
    }
}
