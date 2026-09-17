package com.rs.game.player.content.eds.rooms.ed3;

import java.util.Arrays;

import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.npc.eds.EliteDungeonNPC;
import com.rs.game.npc.eds.SimpleEliteDungeonBoss;
import com.rs.game.npc.eds.SimpleEliteDungeonNPC;
import com.rs.game.player.content.eds.EliteDungeonsConstants;
import com.rs.game.player.content.eds.rooms.EliteDungeonHandledRoom;

public class Room1 extends EliteDungeonHandledRoom {

    private static final long serialVersionUID = 5923056024211413184L;

    private SimpleEliteDungeonBoss boss;

    @Override
    public void load() {
        super.load();
        addNPC(new SimpleEliteDungeonNPC(26149, getTile(new WorldTile(5528, 9118, 3)), this));
        addNPC(new SimpleEliteDungeonNPC(26154, getTile(new WorldTile(5527, 9112, 3)), this));
        addNPC(new SimpleEliteDungeonNPC(26144, getTile(new WorldTile(5518, 9118, 3)), this));
        addNPC(new SimpleEliteDungeonNPC(26157, getTile(new WorldTile(5518, 9112, 3)), this));

        boss = new SimpleEliteDungeonBoss(26050, getTile(new WorldTile(5504, 9115, 3)), this, 40394, new WorldTile(5504, 9115, 3), 18);
        addNPC(boss).setNextFaceWorldTile(boss.transform(1, 0, 0));
        addBlock(new WorldObject(111700, 10, 0, getTile(new WorldTile(5480, 9117, 3))), 2101, Arrays.<EliteDungeonNPC>asList(boss));
    }

    @Override
    public int getEliteDungeonType() {
        return EliteDungeonsConstants.SHADOW_REEF;
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
