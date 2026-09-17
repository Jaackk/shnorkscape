package com.rs.game.player.content.eds.rooms.ed1;

import java.util.ArrayList;
import java.util.List;

import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.npc.eds.EliteDungeonNPC;
import com.rs.game.npc.eds.SeiryuTheAzureSerpent;
import com.rs.game.player.content.eds.rooms.EliteDungeonHandledRoom;

import lombok.Getter;

public class Room5 extends EliteDungeonHandledRoom {

    private static final long serialVersionUID = 3960070877027698952L;
    @Getter
    private transient CombinationBlock bossBlock1, bossBlock2;
    private transient SeiryuTheAzureSerpent boss;

    @Override
    public int getEliteDungeonType() {
        return 1;
    }

    @Override
    public int getRoomIndex() {
        return 4;
    }

    @Override
    public void load() {
        super.load();
        EliteDungeonNPC defp1 = new EliteDungeonNPC(25597, getTile(new WorldTile(4734, 9070, 1)), this);
        EliteDungeonNPC defp2 = new EliteDungeonNPC(25597, getTile(new WorldTile(4739, 9068, 1)), this);
        EliteDungeonNPC defp3 = new EliteDungeonNPC(25597, getTile(new WorldTile(4746, 9068, 1)), this);
        EliteDungeonNPC defp4 = new EliteDungeonNPC(25597, getTile(new WorldTile(4751, 9070, 1)), this);
        addNPC(defp1).setNextFaceWorldTile(defp1.transform(0, -1, 0));
        addNPC(defp2).setNextFaceWorldTile(defp2.transform(0, -1, 0));
        addNPC(defp3).setNextFaceWorldTile(defp3.transform(0, -1, 0));
        addNPC(defp4).setNextFaceWorldTile(defp4.transform(0, -1, 0));

        EliteDungeonNPC defpp1 = new EliteDungeonNPC(25596, getTile(new WorldTile(4745, 9050, 1)), this);
        EliteDungeonNPC defpp2 = new EliteDungeonNPC(25596, getTile(new WorldTile(4743, 9050, 1)), this);
        EliteDungeonNPC defpp3 = new EliteDungeonNPC(25596, getTile(new WorldTile(4741, 9050, 1)), this);
        EliteDungeonNPC defpp4 = new EliteDungeonNPC(25596, getTile(new WorldTile(4743, 9043, 1)), this);

        EliteDungeonNPC defpp5 = new EliteDungeonNPC(25596, getTile(new WorldTile(4741, 9036, 1)), this);

        EliteDungeonNPC defpp6 = new EliteDungeonNPC(25596, getTile(new WorldTile(4745, 9036, 1)), this);

        addNPC(defpp1).setNextFaceWorldTile(defpp1.transform(0, -1, 0));
        addNPC(defpp2).setNextFaceWorldTile(defpp2.transform(0, -1, 0));
        addNPC(defpp3).setNextFaceWorldTile(defpp3.transform(0, -1, 0));
        addNPC(defpp4).setNextFaceWorldTile(defpp4.transform(0, -1, 0));
        addNPC(defpp5).setNextFaceWorldTile(defpp5.transform(0, -1, 0));
        addNPC(defpp6).setNextFaceWorldTile(defpp6.transform(0, -1, 0));
        List<EliteDungeonNPC> bossBlock1NPCs = new ArrayList<EliteDungeonNPC>();
        bossBlock1NPCs.add(defpp1);
        bossBlock1NPCs.add(defpp2);
        bossBlock1NPCs.add(defpp3);
        bossBlock1NPCs.add(defpp4);
        bossBlock1NPCs.add(defpp5);
        List<EliteDungeonNPC> bossBlock2NPCs = new ArrayList<EliteDungeonNPC>();
        bossBlock2NPCs.add(defpp1);
        bossBlock2NPCs.add(defpp2);
        bossBlock2NPCs.add(defpp3);
        bossBlock2NPCs.add(defpp4);
        bossBlock2NPCs.add(defpp6);
        bossBlock1 = addBlock(new WorldObject(111703, 10, 0, getTile(new WorldTile(4732, 9020, 1))), 111705, bossBlock1NPCs);
        bossBlock2 = addBlock(new WorldObject(111702, 10, 0, getTile(new WorldTile(4750, 9020, 1))), 111704, bossBlock2NPCs);
        boss = new SeiryuTheAzureSerpent(25593, getTile(new WorldTile(4742, 8995, 1)), this);
        addNPC(boss).setNextFaceWorldTile(boss.transform(2, boss.getSize() + 5, 0));
        // final boss
    }

    @Override
    public boolean isInBossFightArea(WorldTile tile) {
        return boss != null && boss.isInsideFightArea(tile);
    }

}
