package com.rs.game.player.content.eds.rooms.ed1;

import java.util.ArrayList;
import java.util.List;

import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.npc.eds.EliteDungeonMiniBoss;
import com.rs.game.npc.eds.EliteDungeonNPC;
import com.rs.game.npc.eds.TheSanctumGuardian;
import com.rs.game.player.content.eds.rooms.EliteDungeonHandledRoom;
import com.rs.utils.Utils;

import lombok.Getter;

public class Room2 extends EliteDungeonHandledRoom {
    private static final long serialVersionUID = 2569482513529364326L;
    @Getter
    private transient CombinationBlock bossBlock1, bossBlock2;

    private transient TheSanctumGuardian boss;

    private EliteDungeonMiniBoss miniBoss;

    @Override
    public void load() {
        super.load();
        EliteDungeonNPC elite1 = new EliteDungeonNPC(25583, getTile(new WorldTile(4746, 9240, 1)), this);
        EliteDungeonNPC elite2 = new EliteDungeonNPC(25583, getTile(new WorldTile(4744, 9239, 1)), this);
        EliteDungeonNPC elite3 = new EliteDungeonNPC(25583, getTile(new WorldTile(4742, 9239, 1)), this);
        EliteDungeonNPC elite4 = new EliteDungeonNPC(25583, getTile(new WorldTile(4740, 9240, 1)), this);
        EliteDungeonNPC n1 = new EliteDungeonNPC(25577, getTile(new WorldTile(4743, 9241, 1)), this);
        addNPC(elite1).setNextFaceWorldTile(elite1.transform(0, 1, 0));
        addNPC(elite2).setNextFaceWorldTile(elite2.transform(0, 1, 0));
        addNPC(elite3).setNextFaceWorldTile(elite3.transform(0, 1, 0));
        addNPC(elite4).setNextFaceWorldTile(elite4.transform(0, 1, 0));
        addNPC(n1).setNextFaceWorldTile(n1.transform(0, 1, 0));
        List<EliteDungeonNPC> block1NPCs = new ArrayList<EliteDungeonNPC>();
        block1NPCs.add(elite1);
        block1NPCs.add(elite2);
        block1NPCs.add(elite3);
        block1NPCs.add(elite4);
        block1NPCs.add(n1);
        addBlock(new WorldObject(111706, 10, 0, getTile(new WorldTile(4740, 9234, 1))), block1NPCs);
        EliteDungeonNPC west1 = new EliteDungeonNPC(25583, getTile(new WorldTile(4738, 9225, 1)), this);
        EliteDungeonNPC west2 = new EliteDungeonNPC(25583, getTile(new WorldTile(4738, 9227, 1)), this);
        EliteDungeonNPC west3 = new EliteDungeonNPC(25583, getTile(new WorldTile(4738, 9229, 1)), this);
        addNPC(west1).setNextFaceWorldTile(west1.transform(1, 0, 0));
        addNPC(west2).setNextFaceWorldTile(west2.transform(1, 0, 0));
        addNPC(west3).setNextFaceWorldTile(west3.transform(1, 0, 0));
        List<EliteDungeonNPC> block2NPCs = new ArrayList<EliteDungeonNPC>();
        block2NPCs.add(west1);
        block2NPCs.add(west2);
        block2NPCs.add(west3);
        addBlock(new WorldObject(111702, 10, 0, getTile(new WorldTile(4731, 9223, 1))), 111704, block2NPCs);

        EliteDungeonNPC east1 = new EliteDungeonNPC(25583, getTile(new WorldTile(4748, 9225, 1)), this);
        EliteDungeonNPC east2 = new EliteDungeonNPC(25583, getTile(new WorldTile(4748, 9227, 1)), this);
        EliteDungeonNPC east3 = new EliteDungeonNPC(25583, getTile(new WorldTile(4748, 9229, 1)), this);
        addNPC(east1).setNextFaceWorldTile(east1.transform(-1, 0, 0));
        addNPC(east2).setNextFaceWorldTile(east2.transform(-1, 0, 0));
        addNPC(east3).setNextFaceWorldTile(east3.transform(-1, 0, 0));
        List<EliteDungeonNPC> block3NPCs = new ArrayList<EliteDungeonNPC>();
        block3NPCs.add(east1);
        block3NPCs.add(east2);
        block3NPCs.add(east3);
        addBlock(new WorldObject(111703, 10, 0, getTile(new WorldTile(4751, 9223, 1))), 111705, block3NPCs);

        EliteDungeonNPC serpant1 = new EliteDungeonNPC(25586, getTile(new WorldTile(4743, 9208, 1)), this);
        EliteDungeonNPC serpant2 = new EliteDungeonNPC(25586, getTile(new WorldTile(4743, 9204, 1)), this);
        EliteDungeonNPC serpant3 = new EliteDungeonNPC(25585, getTile(new WorldTile(4743, 9200, 1)), this);
        EliteDungeonNPC serpant4 = new EliteDungeonNPC(25585, getTile(new WorldTile(4743, 9197, 1)), this);
        EliteDungeonNPC serpant5 = new EliteDungeonNPC(25586, getTile(new WorldTile(4743, 9194, 1)), this);
        EliteDungeonNPC serpant6 = new EliteDungeonNPC(25585, getTile(new WorldTile(4743, 9192, 1)), this);
        EliteDungeonNPC serpant7 = new EliteDungeonNPC(25586, getTile(new WorldTile(4743, 9189, 1)), this);
        EliteDungeonNPC serpant8 = new EliteDungeonNPC(25585, getTile(new WorldTile(4743, 9186, 1)), this);
        EliteDungeonNPC serpant9 = new EliteDungeonNPC(25586, getTile(new WorldTile(4743, 9184, 1)), this);
        EliteDungeonNPC serpant10 = new EliteDungeonNPC(25586, getTile(new WorldTile(4743, 9170, 1)), this);
        EliteDungeonNPC serpant11 = new EliteDungeonNPC(25586, getTile(new WorldTile(4743, 9167, 1)), this);
        EliteDungeonNPC serpant12 = new EliteDungeonNPC(25586, getTile(new WorldTile(4743, 9165, 1)), this);
        EliteDungeonNPC serpant13 = new EliteDungeonNPC(25586, getTile(new WorldTile(4743, 9161, 1)), this);
        EliteDungeonNPC serpant14 = new EliteDungeonNPC(25586, getTile(new WorldTile(4743, 9157, 1)), this);
        addNPC(serpant1).setNextFaceWorldTile(serpant1.transform(-1, 0, 0));
        addNPC(serpant2).setNextFaceWorldTile(serpant2.transform(-1, 0, 0));
        addNPC(serpant3).setNextFaceWorldTile(serpant3.transform(-1, 0, 0));
        addNPC(serpant4).setNextFaceWorldTile(serpant4.transform(-1, 0, 0));
        addNPC(serpant5).setNextFaceWorldTile(serpant5.transform(-1, 0, 0));
        addNPC(serpant6).setNextFaceWorldTile(serpant6.transform(-1, 0, 0));
        addNPC(serpant7).setNextFaceWorldTile(serpant7.transform(-1, 0, 0));
        addNPC(serpant8).setNextFaceWorldTile(serpant8.transform(-1, 0, 0));
        addNPC(serpant9).setNextFaceWorldTile(serpant9.transform(-1, 0, 0));
        addNPC(serpant10).setNextFaceWorldTile(serpant10.transform(-1, 0, 0));
        addNPC(serpant11).setNextFaceWorldTile(serpant11.transform(-1, 0, 0));
        addNPC(serpant12).setNextFaceWorldTile(serpant12.transform(-1, 0, 0));
        addNPC(serpant13).setNextFaceWorldTile(serpant13.transform(-1, 0, 0));
        addNPC(serpant14).setNextFaceWorldTile(serpant14.transform(-1, 0, 0));

        EliteDungeonNPC west4 = new EliteDungeonNPC(25583, getTile(new WorldTile(4732, 9154, 1)), this);
        EliteDungeonNPC west5 = new EliteDungeonNPC(25583, getTile(new WorldTile(4731, 9155, 1)), this);
        EliteDungeonNPC west6 = new EliteDungeonNPC(25583, getTile(new WorldTile(4730, 9154, 1)), this);
        addNPC(west4).setNextFaceWorldTile(west4.transform(0, 1, 0));
        addNPC(west5).setNextFaceWorldTile(west5.transform(0, 1, 0));
        addNPC(west6).setNextFaceWorldTile(west6.transform(0, 1, 0));
        List<EliteDungeonNPC> bossBlock1NPCs = new ArrayList<EliteDungeonNPC>();
        bossBlock1NPCs.add(west4);
        bossBlock1NPCs.add(west5);
        bossBlock1NPCs.add(west6);
        bossBlock1 = addBlock(new WorldObject(111703, 10, 0, getTile(new WorldTile(4730, 9152, 1))), 111705, bossBlock1NPCs);
        EliteDungeonNPC east4 = new EliteDungeonNPC(25583, getTile(new WorldTile(4754, 9154, 1)), this);
        EliteDungeonNPC east5 = new EliteDungeonNPC(25583, getTile(new WorldTile(4756, 9154, 1)), this);
        EliteDungeonNPC east6 = new EliteDungeonNPC(25583, getTile(new WorldTile(4755, 9155, 1)), this);
        addNPC(east4).setNextFaceWorldTile(east4.transform(0, 1, 0));
        addNPC(east5).setNextFaceWorldTile(east5.transform(0, 1, 0));
        addNPC(east6).setNextFaceWorldTile(east6.transform(0, 1, 0));
        List<EliteDungeonNPC> bossBlock2NPCs = new ArrayList<EliteDungeonNPC>();
        bossBlock2NPCs.add(east4);
        bossBlock2NPCs.add(east5);
        bossBlock2NPCs.add(east6);

        bossBlock2 = addBlock(new WorldObject(111702, 10, 0, getTile(new WorldTile(4752, 9152, 1))), 111704, bossBlock2NPCs);

        boss = new TheSanctumGuardian(25587, getTile(new WorldTile(4739, 9135, 1)), this);
        addNPC(boss).setNextFaceWorldTile(boss.transform(4, boss.getSize() + 1, 0));
        List<EliteDungeonNPC> bossBlock = new ArrayList<EliteDungeonNPC>();
        bossBlock.add(boss);
        addBlock(new WorldObject(111700, 10, 0, getTile(new WorldTile(4742, 9127, 1))), bossBlock);

        if (miniBoss == null && Utils.random(1) == 0) {
            miniBoss = new EliteDungeonMiniBoss(25630, getTile(new WorldTile(4743, 9216, 1)), this);
            miniBoss.setCantFollowUnderCombat(true);
            miniBoss.setFaceTile(0, -5, 0);
        }
        if (miniBoss != null) {
            miniBoss = new EliteDungeonMiniBoss(25630, getTile(new WorldTile(4743, 9216, 1)), this);
            miniBoss.setCantFollowUnderCombat(true);
            miniBoss.setFaceTile(0, -5, 0);
            addNPC(miniBoss).setNextFaceWorldTile(miniBoss.transform(miniBoss.getFaceTile()[0], miniBoss.getFaceTile()[1], miniBoss.getFaceTile()[2]));
        }

    }

    @Override
    public int getEliteDungeonType() {
        return 1;
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
