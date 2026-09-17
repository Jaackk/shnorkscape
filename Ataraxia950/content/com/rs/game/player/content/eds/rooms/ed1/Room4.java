package com.rs.game.player.content.eds.rooms.ed1;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.npc.eds.EliteDungeonMiniBoss;
import com.rs.game.npc.eds.EliteDungeonNPC;
import com.rs.game.npc.eds.MasutaTheAscended;
import com.rs.game.player.content.eds.rooms.EliteDungeonHandledRoom;
import com.rs.utils.Utils;

public class Room4 extends EliteDungeonHandledRoom {

    private static final long serialVersionUID = 3994579360666473150L;
    private List<EliteDungeonMiniBoss> miniBosses;
    private transient MasutaTheAscended boss;

    @Override
    public int getEliteDungeonType() {
        return 1;
    }

    @Override
    public int getRoomIndex() {
        return 3;
    }

    @Override
    public void load() {
        super.load();
        EliteDungeonNPC heal1 = new EliteDungeonNPC(25577, getTile(new WorldTile(4631, 9167, 1)), this);
        EliteDungeonNPC mage1 = new EliteDungeonNPC(25575, getTile(new WorldTile(4633, 9167, 1)), this);
        addNPC(heal1).setNextFaceWorldTile(heal1.transform(-1, 0, 0));
        addNPC(mage1).setNextFaceWorldTile(mage1.transform(0, 1, 0));

        EliteDungeonNPC saka1 = new EliteDungeonNPC(25583, getTile(new WorldTile(4640, 9167, 1)), this);
        EliteDungeonNPC saka2 = new EliteDungeonNPC(25584, getTile(new WorldTile(4638, 9167, 1)), this);
        addNPC(saka1).setNextFaceWorldTile(saka1.transform(0, -1, 0));
        addNPC(saka2).setNextFaceWorldTile(saka2.transform(0, -1, 0));
        List<EliteDungeonNPC> block1NPCs = new ArrayList<EliteDungeonNPC>();
        block1NPCs.add(saka1);
        block1NPCs.add(saka2);
        addBlock(new WorldObject(111701, 10, 0, getTile(new WorldTile(4637, 9168, 1))), block1NPCs);

        EliteDungeonNPC merc1 = new EliteDungeonNPC(25610, getTile(new WorldTile(4639, 9178, 1)), this);
        EliteDungeonNPC merc2 = new EliteDungeonNPC(25610, getTile(new WorldTile(4640, 9178, 1)), this);
        addNPC(merc1).setNextFaceWorldTile(merc1.transform(0, 1, 0));
        addNPC(merc2).setNextFaceWorldTile(merc2.transform(0, 1, 0));

        EliteDungeonNPC merc3 = new EliteDungeonNPC(25610, getTile(new WorldTile(4646, 9190, 1)), this);
        EliteDungeonNPC merc4 = new EliteDungeonNPC(25610, getTile(new WorldTile(4646, 9192, 1)), this);
        EliteDungeonNPC merc5 = new EliteDungeonNPC(25611, getTile(new WorldTile(4648, 9191, 1)), this);
        addNPC(merc3).setNextFaceWorldTile(merc3.transform(-1, 0, 0));
        addNPC(merc4).setNextFaceWorldTile(merc4.transform(-1, 0, 0));
        addNPC(merc5).setNextFaceWorldTile(merc5.transform(-1, 0, 0));
        List<EliteDungeonNPC> block2NPCs = new ArrayList<EliteDungeonNPC>();
        block2NPCs.add(merc3);
        block2NPCs.add(merc4);
        block2NPCs.add(merc5);
        addBlock(new WorldObject(111699, 10, 3, getTile(new WorldTile(4651, 9191, 1))), block2NPCs);

        EliteDungeonNPC rene1 = new EliteDungeonNPC(25607, getTile(new WorldTile(4633, 9194, 1)), this);
        EliteDungeonNPC rene2 = new EliteDungeonNPC(25607, getTile(new WorldTile(4635, 9193, 1)), this);
        EliteDungeonNPC rene3 = new EliteDungeonNPC(25607, getTile(new WorldTile(4635, 9196, 1)), this);
        EliteDungeonNPC rene4 = new EliteDungeonNPC(25607, getTile(new WorldTile(4633, 9199, 1)), this);
        addNPC(rene1).setNextFaceWorldTile(rene1.transform(1, 0, 0));
        addNPC(rene2).setNextFaceWorldTile(rene2.transform(0, 1, 0));
        addNPC(rene3).setNextFaceWorldTile(rene3.transform(-1, 0, 0));
        addNPC(rene4).setNextFaceWorldTile(rene4.transform(0, -1, 0));
        List<EliteDungeonNPC> block3NPCs = new ArrayList<EliteDungeonNPC>();
        block3NPCs.add(rene1);
        block3NPCs.add(rene2);
        block3NPCs.add(rene3);
        block3NPCs.add(rene4);
        block3NPCs.add(merc1);
        block3NPCs.add(merc2);
        addBlock(new WorldObject(111706, 10, 0, getTile(new WorldTile(4636, 9201, 1))), block3NPCs);

        EliteDungeonNPC hells1 = new EliteDungeonNPC(25608, getTile(new WorldTile(4632, 9185, 1)), this);
        EliteDungeonNPC hells2 = new EliteDungeonNPC(25608, getTile(new WorldTile(4632, 9183, 1)), this);
        EliteDungeonNPC hells3 = new EliteDungeonNPC(25608, getTile(new WorldTile(4630, 9184, 1)), this);
        addNPC(hells1).setNextFaceWorldTile(hells1.transform(1, 0, 0));
        addNPC(hells2).setNextFaceWorldTile(hells2.transform(1, 0, 0));
        addNPC(hells3).setNextFaceWorldTile(hells3.transform(1, 0, 0));
        List<EliteDungeonNPC> block4NPCs = new ArrayList<EliteDungeonNPC>();
        block4NPCs.add(hells1);
        block4NPCs.add(hells2);
        block4NPCs.add(hells3);
        addBlock(new WorldObject(111699, 10, 3, getTile(new WorldTile(4627, 9184, 1))), block4NPCs);

        EliteDungeonNPC merc6 = new EliteDungeonNPC(25610, getTile(new WorldTile(4623, 9183, 1)), this);// training
        EliteDungeonNPC merc7 = new EliteDungeonNPC(25610, getTile(new WorldTile(4620, 9183, 1)), this);// training
        EliteDungeonNPC merc8 = new EliteDungeonNPC(25611, getTile(new WorldTile(4616, 9181, 1)), this);// training
        EliteDungeonNPC hells4 = new EliteDungeonNPC(25608, getTile(new WorldTile(4616, 9184, 1)), this);// training
        addNPC(merc6).setNextFaceWorldTile(merc6.transform(-1, 0, 0));
        addNPC(merc7).setNextFaceWorldTile(merc7.transform(-1, 0, 0));
        addNPC(merc8).setNextFaceWorldTile(merc8.transform(-1, 0, 0));
        addNPC(hells4).setNextFaceWorldTile(hells4.transform(-1, 0, 0));

        EliteDungeonNPC rene5 = new EliteDungeonNPC(25607, getTile(new WorldTile(4617, 9188, 1)), this);
        addNPC(rene5).setNextFaceWorldTile(rene5.transform(0, -1, 0));
        List<EliteDungeonNPC> block5NPCs = new ArrayList<EliteDungeonNPC>();
        block5NPCs.add(rene5);
        addBlock(new WorldObject(111699, 10, 0, getTile(new WorldTile(4617, 9191, 1))), block5NPCs);

        EliteDungeonNPC merc9 = new EliteDungeonNPC(25610, getTile(new WorldTile(4617, 9175, 1)), this);
        merc9.walkTiles = new WorldTile[2];
        merc9.walkTiles[0] = getTile(new WorldTile(4617, 9164, 1));
        merc9.walkTiles[1] = getTile(new WorldTile(4617, 9175, 1));
        addNPC(merc9);

        EliteDungeonNPC merc10 = new EliteDungeonNPC(25610, getTile(new WorldTile(4621, 9164, 1)), this);
        EliteDungeonNPC merc11 = new EliteDungeonNPC(25611, getTile(new WorldTile(4618, 9155, 1)), this);
        EliteDungeonNPC merc12 = new EliteDungeonNPC(25611, getTile(new WorldTile(4624, 9157, 1)), this);
        EliteDungeonNPC heal2 = new EliteDungeonNPC(25577, getTile(new WorldTile(4616, 9161, 1)), this);
        addNPC(merc10).setNextFaceWorldTile(merc10.transform(1, 0, 0));
        addNPC(merc11).setNextFaceWorldTile(merc11.transform(0, -1, 0));
        addNPC(merc12).setNextFaceWorldTile(merc12.transform(1, 0, 0));
        addNPC(heal2).setNextFaceWorldTile(heal2.transform(-1, -1, 0));

        EliteDungeonNPC rene6 = new EliteDungeonNPC(25607, getTile(new WorldTile(4627, 9197, 1)), this);
        EliteDungeonNPC rene7 = new EliteDungeonNPC(25607, getTile(new WorldTile(4628, 9198, 1)), this);
        EliteDungeonNPC lotus1 = new EliteDungeonNPC(25614, getTile(new WorldTile(4626, 9205, 1)), this);
        addNPC(rene6).setNextFaceWorldTile(rene6.transform(1, -1, 0));
        addNPC(rene7).setNextFaceWorldTile(rene7.transform(-1, 0, 0));
        addNPC(lotus1).setNextFaceWorldTile(lotus1.transform(0, -1, 0));

        EliteDungeonNPC lotus2 = new EliteDungeonNPC(25615, getTile(new WorldTile(4614, 9204, 1)), this);
        EliteDungeonNPC lotus3 = new EliteDungeonNPC(25614, getTile(new WorldTile(4616, 9204, 1)), this);
        addNPC(lotus2).setNextFaceWorldTile(lotus2.transform(0, 1, 0));
        addNPC(lotus3).setNextFaceWorldTile(lotus3.transform(0, 1, 0));
        List<EliteDungeonNPC> block6NPCs = new ArrayList<EliteDungeonNPC>();
        block6NPCs.add(lotus2);
        block6NPCs.add(lotus3);
        addBlock(new WorldObject(111699, 10, 0, getTile(new WorldTile(4615, 9209, 1))), block6NPCs);

        EliteDungeonNPC merc13 = new EliteDungeonNPC(25610, getTile(new WorldTile(4638, 9220, 1)), this);
        EliteDungeonNPC merc14 = new EliteDungeonNPC(25611, getTile(new WorldTile(4639, 9220, 1)), this);
        EliteDungeonNPC merc15 = new EliteDungeonNPC(25610, getTile(new WorldTile(4640, 9220, 1)), this);
        EliteDungeonNPC mage2 = new EliteDungeonNPC(25575, getTile(new WorldTile(4641, 9221, 1)), this);
        EliteDungeonNPC heal3 = new EliteDungeonNPC(25577, getTile(new WorldTile(4639, 9221, 1)), this);
        EliteDungeonNPC mage3 = new EliteDungeonNPC(25575, getTile(new WorldTile(4637, 9221, 1)), this);
        EliteDungeonNPC heal4 = new EliteDungeonNPC(25577, getTile(new WorldTile(4642, 9225, 1)), this);
        EliteDungeonNPC heal5 = new EliteDungeonNPC(25577, getTile(new WorldTile(4636, 9225, 1)), this);
        addNPC(merc13).setNextFaceWorldTile(merc13.transform(0, -1, 0));
        addNPC(merc14).setNextFaceWorldTile(merc14.transform(0, -1, 0));
        addNPC(merc15).setNextFaceWorldTile(merc15.transform(0, -1, 0));
        addNPC(mage2).setNextFaceWorldTile(mage2.transform(0, -1, 0));
        addNPC(heal3).setNextFaceWorldTile(heal3.transform(0, -1, 0));
        addNPC(mage3).setNextFaceWorldTile(mage3.transform(0, -1, 0));
        addNPC(heal4).setNextFaceWorldTile(heal4.transform(0, -1, 0));
        addNPC(heal5).setNextFaceWorldTile(heal5.transform(0, -1, 0));
        EliteDungeonNPC rene8 = new EliteDungeonNPC(25607, getTile(new WorldTile(4655, 9218, 1)), this);
        EliteDungeonNPC rene9 = new EliteDungeonNPC(25607, getTile(new WorldTile(4653, 9218, 1)), this);
        EliteDungeonNPC rene10 = new EliteDungeonNPC(25607, getTile(new WorldTile(4654, 9217, 1)), this);
        rene8.walkTiles = new WorldTile[2];
        rene8.walkTiles[0] = getTile(new WorldTile(4611, 9218, 1));
        rene8.walkTiles[1] = getTile(new WorldTile(4653, 9218, 1));
        rene9.walkTiles = new WorldTile[2];
        rene9.walkTiles[0] = getTile(new WorldTile(4611, 9217, 1));
        rene9.walkTiles[1] = getTile(new WorldTile(4653, 9218, 1));
        rene10.walkTiles = new WorldTile[2];
        rene10.walkTiles[0] = getTile(new WorldTile(4612, 9216, 1));
        rene10.walkTiles[1] = getTile(new WorldTile(4654, 9217, 1));
        addNPC(rene8);
        addNPC(rene9);
        addNPC(rene10);
        EliteDungeonNPC heal6 = new EliteDungeonNPC(25577, getTile(new WorldTile(4620, 9211, 1)), this);
        EliteDungeonNPC heal7 = new EliteDungeonNPC(25577, getTile(new WorldTile(4620, 9212, 1)), this);
        heal6.walkTiles = new WorldTile[2];
        heal6.walkTiles[0] = getTile(new WorldTile(4635, 9216, 1));
        heal6.walkTiles[1] = getTile(new WorldTile(4620, 9211, 1));
        heal7.walkTiles = new WorldTile[2];
        heal7.walkTiles[0] = getTile(new WorldTile(4636, 9217, 1));
        heal7.walkTiles[1] = getTile(new WorldTile(4620, 9212, 1));
        addNPC(heal6);
        addNPC(heal7);
        EliteDungeonNPC hells5 = new EliteDungeonNPC(25608, getTile(new WorldTile(4612, 9215, 1)), this);
        addNPC(hells5).setNextFaceWorldTile(hells5.transform(1, 0, 0));

        EliteDungeonNPC lotus4 = new EliteDungeonNPC(25615, getTile(new WorldTile(4644, 9209, 1)), this);
        EliteDungeonNPC lotus5 = new EliteDungeonNPC(25614, getTile(new WorldTile(4634, 9209, 1)), this);
        addNPC(lotus4).setNextFaceWorldTile(lotus4.transform(0, 1, 0));
        addNPC(lotus5).setNextFaceWorldTile(lotus5.transform(0, 1, 0));

        EliteDungeonNPC cloak1 = new EliteDungeonNPC(25613, getTile(new WorldTile(4622, 9223, 1)), this);
        EliteDungeonNPC cloak2 = new EliteDungeonNPC(25613, getTile(new WorldTile(4621, 9224, 1)), this);
        EliteDungeonNPC cloak3 = new EliteDungeonNPC(25613, getTile(new WorldTile(4620, 9224, 1)), this);
        EliteDungeonNPC cloak4 = new EliteDungeonNPC(25613, getTile(new WorldTile(4619, 9223, 1)), this);
        addNPC(cloak1).setNextFaceWorldTile(cloak1.transform(0, -1, 0));
        addNPC(cloak2).setNextFaceWorldTile(cloak2.transform(0, -1, 0));
        addNPC(cloak3).setNextFaceWorldTile(cloak3.transform(0, -1, 0));
        addNPC(cloak4).setNextFaceWorldTile(cloak4.transform(0, -1, 0));
        List<EliteDungeonNPC> block7NPCs = new ArrayList<EliteDungeonNPC>();
        block7NPCs.add(cloak1);
        block7NPCs.add(cloak2);
        block7NPCs.add(cloak3);
        block7NPCs.add(cloak4);
        addBlock(new WorldObject(111700, 10, 0, getTile(new WorldTile(4620, 9225, 1))), block7NPCs);

        EliteDungeonNPC cloak5 = new EliteDungeonNPC(25613, getTile(new WorldTile(4659, 9223, 1)), this);
        EliteDungeonNPC cloak6 = new EliteDungeonNPC(25613, getTile(new WorldTile(4658, 9224, 1)), this);
        EliteDungeonNPC cloak7 = new EliteDungeonNPC(25613, getTile(new WorldTile(4657, 9224, 1)), this);
        EliteDungeonNPC cloak8 = new EliteDungeonNPC(25613, getTile(new WorldTile(4656, 9223, 1)), this);
        addNPC(cloak5).setNextFaceWorldTile(cloak5.transform(0, -1, 0));
        addNPC(cloak6).setNextFaceWorldTile(cloak6.transform(0, -1, 0));
        addNPC(cloak7).setNextFaceWorldTile(cloak7.transform(0, -1, 0));
        addNPC(cloak8).setNextFaceWorldTile(cloak8.transform(0, -1, 0));
        List<EliteDungeonNPC> block8NPCs = new ArrayList<EliteDungeonNPC>();
        block8NPCs.add(cloak5);
        block8NPCs.add(cloak6);
        block8NPCs.add(cloak7);
        block8NPCs.add(cloak8);
        addBlock(new WorldObject(111700, 10, 0, getTile(new WorldTile(4656, 9225, 1))), block8NPCs);

        boss = new MasutaTheAscended(25589, getTile(new WorldTile(4639, 9254, 1)), this);
        addNPC(boss).setNextFaceWorldTile(boss.transform(0, 1, 0));
        List<EliteDungeonNPC> bossBlock = new ArrayList<EliteDungeonNPC>();
        bossBlock.add(boss);
        addBlock(new WorldObject(111546, 10, 2, getTile(new WorldTile(4633, 9257, 1))), 111547, bossBlock);
        /*
         * Bosss 25589 - Masuta the Ascended 25590 - Masuta the Ascended 25591 - Masuta
         * the Ascended
         * 
         * new WorldTile(4639, 9254, 1)
         * 
         * face north (0,1,0)
         */

//      25620 - Goku the Unrelenting 
        EliteDungeonMiniBoss mini1 = new EliteDungeonMiniBoss(25620, getTile(new WorldTile(4613, 9158, 1)), this);
        mini1.setForceTalkDialogue(new String[] { "We are many.", "Our ideas will change the world for the better.", "You will not stop the flood.", "You will not stop the cleansing." });
        mini1.setFaceTile(-1, 0, 0);

//      25628 - The Violet Lotus 
        EliteDungeonMiniBoss mini2 = new EliteDungeonMiniBoss(25628, getTile(new WorldTile(4621, 9199, 1)), this);
        mini2.setForceTalkDialogue(new String[] { "You are the dust on the stained glass windows...", "Trying to comprehend in a cathedral.", "You know not the depth of existence.", "You will never get the chance.", "Goodbye." });
        mini2.setFaceTile(0, 1, 0);

//      25629 - Yukimura of Hanto 
        EliteDungeonMiniBoss mini3 = new EliteDungeonMiniBoss(25629, getTile(new WorldTile(4611, 9217, 1)), this);
        mini3.setForceTalkDialogue(new String[] { "The black crystals must be protected!", "Seiryu's power will be ours!", "Hanto stands with the council!", "This is bigger than you...", "Bigger than even this world alone.", "I feel it. Truly." });
        mini3.setFaceTile(0, -1, 0);
        List<EliteDungeonMiniBoss> minis = new ArrayList<EliteDungeonMiniBoss>();
        minis.add(mini1);
        minis.add(mini2);
        minis.add(mini3);
        Collections.shuffle(minis);
        if (miniBosses == null) {
            miniBosses = new ArrayList<EliteDungeonMiniBoss>();
            int size = Utils.getRandom(2);
            for (int i = 0; i < size; i++)
                miniBosses.add(minis.get(i));
        }
        for (int i = 0; i < miniBosses.size(); i++) {
            int miniIndex = getNPCIndex(minis, miniBosses.get(i));
            if (miniIndex == -1) {
                miniBosses.set(i, null);
                continue;
            }
            miniBosses.set(i, minis.get(miniIndex));
        }
        for (EliteDungeonMiniBoss boss : miniBosses) {
            if (boss == null)
                continue;
            addNPC(boss).setNextFaceWorldTile(boss.transform(boss.getFaceTile()[0], boss.getFaceTile()[1], boss.getFaceTile()[2]));
        }
    }

    public static int getNPCIndex(List<EliteDungeonMiniBoss> list, EliteDungeonMiniBoss npc) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).equals(npc))
                return i;
        }
        return -1;
    }

    @Override
    public boolean isInBossFightArea(WorldTile tile) {
        return boss != null && boss.isInsideFightArea(tile);
    }

}
