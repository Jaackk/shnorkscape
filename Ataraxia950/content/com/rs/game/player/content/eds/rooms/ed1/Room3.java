package com.rs.game.player.content.eds.rooms.ed1;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.npc.eds.EliteDungeonMiniBoss;
import com.rs.game.npc.eds.EliteDungeonNPC;
import com.rs.game.player.content.eds.rooms.EliteDungeonHandledRoom;
import com.rs.utils.Utils;

public class Room3 extends EliteDungeonHandledRoom {

    private static final long serialVersionUID = -5051840274588165607L;
    private List<EliteDungeonMiniBoss> miniBosses;

    @Override
    public void load() {
        super.load();
        EliteDungeonNPC cloak1 = new EliteDungeonNPC(25613, getTile(new WorldTile(4489, 9125, 1)), this);
        EliteDungeonNPC cloak2 = new EliteDungeonNPC(25613, getTile(new WorldTile(4488, 9124, 1)), this);
        EliteDungeonNPC hells1 = new EliteDungeonNPC(25608, getTile(new WorldTile(4485, 9129, 1)), this);
        EliteDungeonNPC rene1 = new EliteDungeonNPC(25607, getTile(new WorldTile(4490, 9116, 1)), this);

        hells1.walkTiles = new WorldTile[2];
        hells1.walkTiles[0] = getTile(new WorldTile(4485, 9118, 1));
        hells1.walkTiles[1] = getTile(new WorldTile(4485, 9129, 1));
        addNPC(cloak1).setNextFaceWorldTile(cloak1.transform(-1, -1, 0));
        addNPC(cloak2).setNextFaceWorldTile(cloak2.transform(1, 1, 0));
        addNPC(rene1).setNextFaceWorldTile(rene1.transform(-1, -1, 0));
        addNPC(hells1);
        EliteDungeonNPC cloak3 = new EliteDungeonNPC(25613, getTile(new WorldTile(4497, 9116, 1)), this);
        EliteDungeonNPC merc1 = new EliteDungeonNPC(25610, getTile(new WorldTile(4498, 9115, 1)), this);
        EliteDungeonNPC merc2 = new EliteDungeonNPC(25610, getTile(new WorldTile(4498, 9117, 1)), this);
        addNPC(cloak3).setNextFaceWorldTile(cloak3.transform(1, 1, 0));
        addNPC(merc1).setNextFaceWorldTile(merc1.transform(0, 1, 0));
        addNPC(merc2).setNextFaceWorldTile(merc2.transform(0, -1, 0));

        EliteDungeonNPC cloak4 = new EliteDungeonNPC(25613, getTile(new WorldTile(4516, 9128, 1)), this);
        EliteDungeonNPC rene2 = new EliteDungeonNPC(25607, getTile(new WorldTile(4515, 9128, 1)), this);
        EliteDungeonNPC rene3 = new EliteDungeonNPC(25607, getTile(new WorldTile(4513, 9128, 1)), this);
        EliteDungeonNPC hells2 = new EliteDungeonNPC(25608, getTile(new WorldTile(4514, 9126, 1)), this);
        hells2.setExactWalkTiles(true);
        hells2.walkTiles = new WorldTile[4];
        hells2.walkTiles[0] = getTile(new WorldTile(4525, 9126, 1));
        hells2.walkTiles[1] = getTile(new WorldTile(4525, 9118, 1));
        hells2.walkTiles[2] = getTile(new WorldTile(4525, 9126, 1));
        hells2.walkTiles[3] = getTile(new WorldTile(4514, 9126, 1));
        EliteDungeonNPC hells3 = new EliteDungeonNPC(25609, getTile(new WorldTile(4500, 9116, 1)), this);
        hells3.setExactWalkTiles(true);
        hells3.walkTiles = new WorldTile[4];
        hells3.walkTiles[0] = getTile(new WorldTile(4524, 9116, 1));
        hells3.walkTiles[1] = getTile(new WorldTile(4524, 9125, 1));
        hells3.walkTiles[2] = getTile(new WorldTile(4500, 9125, 1));
        hells3.walkTiles[3] = getTile(new WorldTile(4500, 9116, 1));
        addNPC(cloak4).setNextFaceWorldTile(cloak4.transform(-1, 1, 0));
        addNPC(rene2).setNextFaceWorldTile(rene2.transform(-1, 1, 0));
        addNPC(rene3).setNextFaceWorldTile(rene3.transform(1, 1, 0));
        addNPC(hells2);
        addNPC(hells3);
        EliteDungeonNPC hells4 = new EliteDungeonNPC(25608, getTile(new WorldTile(4530, 9117, 1)), this);
        EliteDungeonNPC hells5 = new EliteDungeonNPC(25608, getTile(new WorldTile(4530, 9115, 1)), this);
        addNPC(hells4).setNextFaceWorldTile(hells4.transform(0, -1, 0));
        addNPC(hells5).setNextFaceWorldTile(hells5.transform(0, 1, 0));

        EliteDungeonNPC cloak5 = new EliteDungeonNPC(25613, getTile(new WorldTile(4513, 9113, 1)), this);
        EliteDungeonNPC cloak6 = new EliteDungeonNPC(25613, getTile(new WorldTile(4514, 9113, 1)), this);

        addNPC(cloak5).setNextFaceWorldTile(cloak5.transform(0, 1, 0));
        addNPC(cloak6).setNextFaceWorldTile(cloak6.transform(0, 1, 0));

        List<EliteDungeonNPC> block1NPCs = new ArrayList<EliteDungeonNPC>();
        block1NPCs.add(cloak5);
        block1NPCs.add(cloak6);
        addBlock(new WorldObject(111700, 10, 0, getTile(new WorldTile(4513, 9112, 1))), block1NPCs);

        EliteDungeonNPC hells6 = new EliteDungeonNPC(25608, getTile(new WorldTile(4500, 9105, 1)), this);
        EliteDungeonNPC hells7 = new EliteDungeonNPC(25609, getTile(new WorldTile(4499, 9105, 1)), this);
        EliteDungeonNPC hells8 = new EliteDungeonNPC(25608, getTile(new WorldTile(4498, 9105, 1)), this);
        EliteDungeonNPC hells9 = new EliteDungeonNPC(25609, getTile(new WorldTile(4502, 9093, 1)), this);
        EliteDungeonNPC hells10 = new EliteDungeonNPC(25609, getTile(new WorldTile(4501, 9092, 1)), this);
        addNPC(hells6).setNextFaceWorldTile(hells6.transform(0, 1, 0));
        addNPC(hells7).setNextFaceWorldTile(hells7.transform(0, 1, 0));
        addNPC(hells8).setNextFaceWorldTile(hells8.transform(0, 1, 0));
        addNPC(hells9).setNextFaceWorldTile(hells9.transform(-1, -1, 0));
        addNPC(hells10).setNextFaceWorldTile(hells10.transform(1, 1, 0));

        EliteDungeonNPC merc3 = new EliteDungeonNPC(25610, getTile(new WorldTile(4493, 9079, 1)), this);
        merc3.walkTiles = new WorldTile[2];
        merc3.walkTiles[0] = getTile(new WorldTile(4493, 9088, 1));
        merc3.walkTiles[1] = getTile(new WorldTile(4493, 9079, 1));
        EliteDungeonNPC merc4 = new EliteDungeonNPC(25611, getTile(new WorldTile(4492, 9079, 1)), this);
        EliteDungeonNPC merc5 = new EliteDungeonNPC(25611, getTile(new WorldTile(4491, 9079, 1)), this);
        merc4.walkTiles = new WorldTile[2];
        merc4.walkTiles[0] = getTile(new WorldTile(4492, 9093, 1));
        merc4.walkTiles[1] = getTile(new WorldTile(4492, 9079, 1));
        merc5.walkTiles = new WorldTile[2];
        merc5.walkTiles[0] = getTile(new WorldTile(4491, 9093, 1));
        merc5.walkTiles[1] = getTile(new WorldTile(4491, 9079, 1));
        addNPC(merc3);
        addNPC(merc4);
        addNPC(merc5);
        EliteDungeonNPC hells11 = new EliteDungeonNPC(25608, getTile(new WorldTile(4495, 9079, 1)), this);
        EliteDungeonNPC hells12 = new EliteDungeonNPC(25608, getTile(new WorldTile(4501, 9073, 1)), this);
        EliteDungeonNPC hells13 = new EliteDungeonNPC(25609, getTile(new WorldTile(4505, 9072, 1)), this);
        EliteDungeonNPC hells14 = new EliteDungeonNPC(25609, getTile(new WorldTile(4515, 9080, 1)), this);
        EliteDungeonNPC hells15 = new EliteDungeonNPC(25608, getTile(new WorldTile(4517, 9080, 1)), this);
        EliteDungeonNPC merc6 = new EliteDungeonNPC(25610, getTile(new WorldTile(4502, 9080, 1)), this);
        EliteDungeonNPC merc7 = new EliteDungeonNPC(25610, getTile(new WorldTile(4502, 9076, 1)), this);
        addNPC(merc6).setNextFaceWorldTile(merc6.transform(0, 1, 0));
        addNPC(merc7).setNextFaceWorldTile(merc7.transform(0, -1, 0));
        addNPC(hells11).setNextFaceWorldTile(hells11.transform(1, 0, 0));
        addNPC(hells12).setNextFaceWorldTile(hells12.transform(0, 1, 0));
        addNPC(hells13).setNextFaceWorldTile(hells13.transform(0, -1, 0));
        addNPC(hells14).setNextFaceWorldTile(hells14.transform(0, -1, 0));
        addNPC(hells15).setNextFaceWorldTile(hells15.transform(0, -1, 0));

        EliteDungeonNPC merc8 = new EliteDungeonNPC(25610, getTile(new WorldTile(4516, 9072, 1)), this);
        merc8.setExactWalkTiles(true);
        merc8.walkTiles = new WorldTile[6];
        merc8.walkTiles[0] = getTile(new WorldTile(4527, 9072, 1));
        merc8.walkTiles[1] = getTile(new WorldTile(4516, 9072, 1));
        merc8.walkTiles[2] = getTile(new WorldTile(4517, 9072, 1));
        merc8.walkTiles[3] = getTile(new WorldTile(4516, 9072, 1));
        merc8.walkTiles[4] = getTile(new WorldTile(4519, 9072, 1));
        merc8.walkTiles[5] = getTile(new WorldTile(4516, 9072, 1));

        EliteDungeonNPC merc9 = new EliteDungeonNPC(25610, getTile(new WorldTile(4516, 9073, 1)), this);
        merc9.setExactWalkTiles(true);
        merc9.walkTiles = new WorldTile[6];
        merc9.walkTiles[0] = getTile(new WorldTile(4519, 9073, 1));
        merc9.walkTiles[1] = getTile(new WorldTile(4516, 9073, 1));
        merc9.walkTiles[2] = getTile(new WorldTile(4527, 9073, 1));
        merc9.walkTiles[3] = getTile(new WorldTile(4516, 9073, 1));
        merc9.walkTiles[4] = getTile(new WorldTile(4517, 9073, 1));
        merc9.walkTiles[5] = getTile(new WorldTile(4516, 9073, 1));
        EliteDungeonNPC merc10 = new EliteDungeonNPC(25611, getTile(new WorldTile(4516, 9074, 1)), this);
        merc10.setExactWalkTiles(true);
        merc10.walkTiles = new WorldTile[6];
        merc10.walkTiles[0] = getTile(new WorldTile(4517, 9074, 1));
        merc10.walkTiles[1] = getTile(new WorldTile(4516, 9074, 1));
        merc10.walkTiles[2] = getTile(new WorldTile(4519, 9074, 1));
        merc10.walkTiles[3] = getTile(new WorldTile(4516, 9074, 1));
        merc10.walkTiles[4] = getTile(new WorldTile(4527, 9074, 1));
        merc10.walkTiles[5] = getTile(new WorldTile(4516, 9074, 1));
        addNPC(merc8);
        addNPC(merc9);
        addNPC(merc10);
        EliteDungeonNPC cloak7 = new EliteDungeonNPC(25613, getTile(new WorldTile(4531, 9079, 1)), this);
        EliteDungeonNPC cloak8 = new EliteDungeonNPC(25612, getTile(new WorldTile(4531, 9078, 1)), this);
        EliteDungeonNPC cloak9 = new EliteDungeonNPC(25613, getTile(new WorldTile(4531, 9077, 1)), this);
        addNPC(cloak7).setNextFaceWorldTile(cloak7.transform(-1, 0, 0));
        addNPC(cloak8).setNextFaceWorldTile(cloak8.transform(-1, 0, 0));
        addNPC(cloak9).setNextFaceWorldTile(cloak9.transform(-1, 0, 0));
        List<EliteDungeonNPC> block2NPCs = new ArrayList<EliteDungeonNPC>();
        block2NPCs.add(cloak7);
        block2NPCs.add(cloak8);
        block2NPCs.add(cloak9);
        addBlock(new WorldObject(111701, 10, 3, getTile(new WorldTile(4532, 9076, 1))), block2NPCs);
        EliteDungeonNPC lotus1 = new EliteDungeonNPC(25614, getTile(new WorldTile(4530, 9072, 1)), this);
        EliteDungeonNPC lotus2 = new EliteDungeonNPC(25615, getTile(new WorldTile(4529, 9072, 1)), this);
        EliteDungeonNPC lotus3 = new EliteDungeonNPC(25614, getTile(new WorldTile(4528, 9072, 1)), this);
        lotus1.walkTiles = new WorldTile[2];
        lotus1.walkTiles[0] = getTile(new WorldTile(4530, 9108, 1));
        lotus1.walkTiles[1] = getTile(new WorldTile(4530, 9072, 1));
        lotus2.walkTiles = new WorldTile[2];
        lotus2.walkTiles[0] = getTile(new WorldTile(4529, 9108, 1));
        lotus2.walkTiles[1] = getTile(new WorldTile(4529, 9072, 1));
        lotus3.walkTiles = new WorldTile[2];
        lotus3.walkTiles[0] = getTile(new WorldTile(4528, 9108, 1));
        lotus3.walkTiles[1] = getTile(new WorldTile(4528, 9072, 1));
        addNPC(lotus1);
        addNPC(lotus2);
        addNPC(lotus3);
        EliteDungeonNPC wfiend1 = new EliteDungeonNPC(25603, getTile(new WorldTile(4552, 9077, 1)), this);
        EliteDungeonNPC wfiend2 = new EliteDungeonNPC(25603, getTile(new WorldTile(4551, 9088, 1)), this);
        EliteDungeonNPC wfiend3 = new EliteDungeonNPC(25603, getTile(new WorldTile(4552, 9065, 1)), this);
        EliteDungeonNPC wfiend4 = new EliteDungeonNPC(25603, getTile(new WorldTile(4557, 9059, 1)), this);
        EliteDungeonNPC wfiend5 = new EliteDungeonNPC(25603, getTile(new WorldTile(4564, 9054, 1)), this);
        EliteDungeonNPC wfiend6 = new EliteDungeonNPC(25603, getTile(new WorldTile(4579, 9052, 1)), this);
        EliteDungeonNPC wfiend7 = new EliteDungeonNPC(25603, getTile(new WorldTile(4592, 9054, 1)), this);
        EliteDungeonNPC wfiend8 = new EliteDungeonNPC(25603, getTile(new WorldTile(4600, 9058, 1)), this);
        EliteDungeonNPC wfiend9 = new EliteDungeonNPC(25603, getTile(new WorldTile(4603, 9064, 1)), this);
        EliteDungeonNPC wfiend10 = new EliteDungeonNPC(25603, getTile(new WorldTile(4608, 9069, 1)), this);
        EliteDungeonNPC wfiend11 = new EliteDungeonNPC(25603, getTile(new WorldTile(4608, 9084, 1)), this);
        EliteDungeonNPC wfiend12 = new EliteDungeonNPC(25603, getTile(new WorldTile(4606, 9090, 1)), this);
        EliteDungeonNPC wfiend13 = new EliteDungeonNPC(25603, getTile(new WorldTile(4604, 9095, 1)), this);
        EliteDungeonNPC wfiend14 = new EliteDungeonNPC(25603, getTile(new WorldTile(4598, 9101, 1)), this);
        EliteDungeonNPC wfiend15 = new EliteDungeonNPC(25603, getTile(new WorldTile(4581, 9104, 1)), this);
        EliteDungeonNPC wfiend16 = new EliteDungeonNPC(25603, getTile(new WorldTile(4566, 9103, 1)), this);
        EliteDungeonNPC wfiend17 = new EliteDungeonNPC(25603, getTile(new WorldTile(4558, 9098, 1)), this);
        EliteDungeonNPC croc1 = new EliteDungeonNPC(25606, getTile(new WorldTile(4552, 9070, 1)), this);
        EliteDungeonNPC croc2 = new EliteDungeonNPC(25606, getTile(new WorldTile(4561, 9057, 1)), this);
        EliteDungeonNPC croc3 = new EliteDungeonNPC(25606, getTile(new WorldTile(4582, 9052, 1)), this);
        EliteDungeonNPC croc4 = new EliteDungeonNPC(25606, getTile(new WorldTile(4597, 9056, 1)), this);
        EliteDungeonNPC croc5 = new EliteDungeonNPC(25606, getTile(new WorldTile(4606, 9065, 1)), this);
        EliteDungeonNPC croc6 = new EliteDungeonNPC(25606, getTile(new WorldTile(4606, 9089, 1)), this);
        EliteDungeonNPC croc7 = new EliteDungeonNPC(25606, getTile(new WorldTile(4602, 9099, 1)), this);
        EliteDungeonNPC croc8 = new EliteDungeonNPC(25606, getTile(new WorldTile(4575, 9104, 1)), this);
        EliteDungeonNPC croc9 = new EliteDungeonNPC(25606, getTile(new WorldTile(4555, 9092, 1)), this);
        addNPC(wfiend1);
        addNPC(wfiend2);
        addNPC(wfiend3);
        addNPC(wfiend4);
        addNPC(wfiend5);
        addNPC(wfiend6);
        addNPC(wfiend7);
        addNPC(wfiend8);
        addNPC(wfiend9);
        addNPC(wfiend10);
        addNPC(wfiend11);
        addNPC(wfiend12);
        addNPC(wfiend13);
        addNPC(wfiend14);
        addNPC(wfiend15);
        addNPC(wfiend16);
        addNPC(wfiend17);
        addNPC(croc1);
        addNPC(croc2);
        addNPC(croc3);
        addNPC(croc4);
        addNPC(croc5);
        addNPC(croc6);
        addNPC(croc7);
        addNPC(croc8);
        addNPC(croc9);
        EliteDungeonNPC cloak10 = new EliteDungeonNPC(25613, getTile(new WorldTile(4612, 9082, 1)), this);
        addNPC(cloak10).setNextFaceWorldTile(cloak10.transform(-1, 0, 0));
        List<EliteDungeonNPC> block3NPCs = new ArrayList<EliteDungeonNPC>();
        block3NPCs.add(cloak10);
        addBlock(new WorldObject(111700, 10, 3, getTile(new WorldTile(4613, 9081, 1))), block3NPCs);

        EliteDungeonNPC cloak11 = new EliteDungeonNPC(25613, getTile(new WorldTile(4612, 9074, 1)), this);
        addNPC(cloak11).setNextFaceWorldTile(cloak11.transform(-1, 0, 0));
        List<EliteDungeonNPC> block4NPCs = new ArrayList<EliteDungeonNPC>();
        block4NPCs.add(cloak11);
        addBlock(new WorldObject(111700, 10, 3, getTile(new WorldTile(4613, 9073, 1))), block4NPCs);

        EliteDungeonNPC croc10 = new EliteDungeonNPC(25606, getTile(new WorldTile(4618, 9094, 1)), this);
        EliteDungeonNPC croc11 = new EliteDungeonNPC(25606, getTile(new WorldTile(4620, 9096, 1)), this);
        EliteDungeonNPC croc12 = new EliteDungeonNPC(25606, getTile(new WorldTile(4618, 9098, 1)), this);
        EliteDungeonNPC croc13 = new EliteDungeonNPC(25606, getTile(new WorldTile(4620, 9104, 1)), this);
        EliteDungeonNPC croc14 = new EliteDungeonNPC(25606, getTile(new WorldTile(4616, 9105, 1)), this);
        EliteDungeonNPC croc15 = new EliteDungeonNPC(25606, getTile(new WorldTile(4620, 9108, 1)), this);
        EliteDungeonNPC croc16 = new EliteDungeonNPC(25606, getTile(new WorldTile(4613, 9116, 1)), this);
        addNPC(croc10);
        addNPC(croc11);
        addNPC(croc12);
        addNPC(croc13);
        addNPC(croc14);
        addNPC(croc15);
        addNPC(croc16);
        EliteDungeonNPC rene4 = new EliteDungeonNPC(25607, getTile(new WorldTile(4618, 9123, 1)), this);
        EliteDungeonNPC rene5 = new EliteDungeonNPC(25607, getTile(new WorldTile(4619, 9123, 1)), this);
        EliteDungeonNPC rene6 = new EliteDungeonNPC(25607, getTile(new WorldTile(4620, 9123, 1)), this);
        addNPC(rene4).setNextFaceWorldTile(rene4.transform(0, -1, 0));
        addNPC(rene5).setNextFaceWorldTile(rene5.transform(0, -1, 0));
        addNPC(rene6).setNextFaceWorldTile(rene6.transform(0, -1, 0));
        EliteDungeonNPC rene7 = new EliteDungeonNPC(25607, getTile(new WorldTile(4630, 9097, 1)), this);
        EliteDungeonNPC rene8 = new EliteDungeonNPC(25607, getTile(new WorldTile(4630, 9096, 1)), this);
        EliteDungeonNPC rene9 = new EliteDungeonNPC(25607, getTile(new WorldTile(4630, 9095, 1)), this);
        addNPC(rene7).setNextFaceWorldTile(rene7.transform(-1, 0, 0));
        addNPC(rene8).setNextFaceWorldTile(rene8.transform(-1, 0, 0));
        addNPC(rene9).setNextFaceWorldTile(rene9.transform(-1, 0, 0));

        EliteDungeonNPC cloak12 = new EliteDungeonNPC(25612, getTile(new WorldTile(4632, 9091, 1)), this);
        cloak12.setExactWalkTiles(true);
        cloak12.walkTiles = new WorldTile[4];
        cloak12.walkTiles[0] = getTile(new WorldTile(4631, 9111, 1));
        cloak12.walkTiles[1] = getTile(new WorldTile(4631, 9121, 1));
        cloak12.walkTiles[2] = getTile(new WorldTile(4631, 9111, 1));
        cloak12.walkTiles[3] = getTile(new WorldTile(4632, 9091, 1));
        EliteDungeonNPC cloak13 = new EliteDungeonNPC(25613, getTile(new WorldTile(4658, 9134, 1)), this);
        cloak13.walkTiles = new WorldTile[2];
        cloak13.walkTiles[0] = getTile(new WorldTile(4634, 9135, 1));
        cloak13.walkTiles[1] = getTile(new WorldTile(4658, 9134, 1));

        EliteDungeonNPC cloak14 = new EliteDungeonNPC(25612, getTile(new WorldTile(4639, 9126, 1)), this);
        cloak14.walkTiles = new WorldTile[2];
        cloak14.walkTiles[0] = getTile(new WorldTile(4665, 9126, 1));
        cloak14.walkTiles[1] = getTile(new WorldTile(4639, 9126, 1));

        EliteDungeonNPC cloak15 = new EliteDungeonNPC(25612, getTile(new WorldTile(4639, 9096, 1)), this);
        cloak15.walkTiles = new WorldTile[2];
        cloak15.walkTiles[0] = getTile(new WorldTile(4639, 9125, 1));
        cloak15.walkTiles[1] = getTile(new WorldTile(4639, 9096, 1));
        addNPC(cloak12);
        addNPC(cloak13);
        addNPC(cloak14);
        addNPC(cloak15);

        EliteDungeonNPC merc11 = new EliteDungeonNPC(25610, getTile(new WorldTile(4668, 9127, 1)), this);
        EliteDungeonNPC merc12 = new EliteDungeonNPC(25611, getTile(new WorldTile(4667, 9125, 1)), this);
        EliteDungeonNPC merc13 = new EliteDungeonNPC(25611, getTile(new WorldTile(4661, 9122, 1)), this);
        EliteDungeonNPC merc14 = new EliteDungeonNPC(25610, getTile(new WorldTile(4660, 9122, 1)), this);
        EliteDungeonNPC merc15 = new EliteDungeonNPC(25610, getTile(new WorldTile(4661, 9124, 1)), this);
        EliteDungeonNPC merc16 = new EliteDungeonNPC(25611, getTile(new WorldTile(4661, 9125, 1)), this);
        addNPC(merc11).setNextFaceWorldTile(merc11.transform(-1, -1, 0));
        addNPC(merc12).setNextFaceWorldTile(merc12.transform(1, 1, 0));
        addNPC(merc13).setNextFaceWorldTile(merc13.transform(1, 0, 0));
        addNPC(merc14).setNextFaceWorldTile(merc14.transform(-1, 0, 0));
        addNPC(merc15).setNextFaceWorldTile(merc15.transform(1, 0, 0));
        addNPC(merc16).setNextFaceWorldTile(merc16.transform(0, 1, 0));

        EliteDungeonNPC cloak16 = new EliteDungeonNPC(25612, getTile(new WorldTile(4641, 9107, 1)), this);
        EliteDungeonNPC cloak17 = new EliteDungeonNPC(25613, getTile(new WorldTile(4641, 9106, 1)), this);
        EliteDungeonNPC cloak18 = new EliteDungeonNPC(25613, getTile(new WorldTile(4641, 9105, 1)), this);
        addNPC(cloak16).setNextFaceWorldTile(cloak16.transform(-1, 0, 0));
        addNPC(cloak17).setNextFaceWorldTile(cloak17.transform(-1, 0, 0));
        addNPC(cloak18).setNextFaceWorldTile(cloak18.transform(-1, 0, 0));
        List<EliteDungeonNPC> block5NPCs = new ArrayList<EliteDungeonNPC>();
        block5NPCs.add(cloak16);
        block5NPCs.add(cloak17);
        block5NPCs.add(cloak18);
        addBlock(new WorldObject(111701, 10, 3, getTile(new WorldTile(4644, 9104, 1))), block5NPCs);
        EliteDungeonNPC cloak19 = new EliteDungeonNPC(25612, getTile(new WorldTile(4646, 9113, 1)), this);
        cloak19.setExactWalkTiles(true);
        cloak19.walkTiles = new WorldTile[4];
        cloak19.walkTiles[0] = getTile(new WorldTile(4654, 9113, 1));
        cloak19.walkTiles[1] = getTile(new WorldTile(4653, 9098, 1));
        cloak19.walkTiles[2] = getTile(new WorldTile(4646, 9098, 1));
        cloak19.walkTiles[3] = getTile(new WorldTile(4646, 9113, 1));
        EliteDungeonNPC cloak20 = new EliteDungeonNPC(25612, getTile(new WorldTile(4654, 9098, 1)), this);
        cloak20.walkTiles = new WorldTile[2];
        cloak20.walkTiles[0] = getTile(new WorldTile(4666, 9098, 1));
        cloak20.walkTiles[1] = getTile(new WorldTile(4654, 9098, 1));
        EliteDungeonNPC cloak21 = new EliteDungeonNPC(25612, getTile(new WorldTile(4655, 9114, 1)), this);
        cloak21.walkTiles = new WorldTile[2];
        cloak21.walkTiles[0] = getTile(new WorldTile(4666, 9114, 1));
        cloak21.walkTiles[1] = getTile(new WorldTile(4655, 9114, 1));
        addNPC(cloak19);
        addNPC(cloak20);
        addNPC(cloak21);
        EliteDungeonNPC cloak22 = new EliteDungeonNPC(25613, getTile(new WorldTile(4658, 9106, 1)), this);
        EliteDungeonNPC cloak23 = new EliteDungeonNPC(25612, getTile(new WorldTile(4659, 9107, 1)), this);
        EliteDungeonNPC cloak24 = new EliteDungeonNPC(25612, getTile(new WorldTile(4659, 9105, 1)), this);
        addNPC(cloak22).setNextFaceWorldTile(cloak22.transform(1, 0, 0));
        addNPC(cloak23).setNextFaceWorldTile(cloak23.transform(-1, -1, 0));
        addNPC(cloak24).setNextFaceWorldTile(cloak24.transform(-1, 1, 0));
        EliteDungeonNPC cloak25 = new EliteDungeonNPC(25612, getTile(new WorldTile(4666, 9104, 1)), this);
        EliteDungeonNPC cloak26 = new EliteDungeonNPC(25613, getTile(new WorldTile(4666, 9105, 1)), this);
        EliteDungeonNPC cloak27 = new EliteDungeonNPC(25613, getTile(new WorldTile(4666, 9106, 1)), this);
        EliteDungeonNPC cloak28 = new EliteDungeonNPC(25612, getTile(new WorldTile(4666, 9107, 1)), this);
        EliteDungeonNPC cloak29 = new EliteDungeonNPC(25612, getTile(new WorldTile(4666, 9108, 1)), this);
        EliteDungeonNPC cloak30 = new EliteDungeonNPC(25613, getTile(new WorldTile(4668, 9106, 1)), this);
        addNPC(cloak25).setNextFaceWorldTile(cloak25.transform(1, 0, 0));
        addNPC(cloak26).setNextFaceWorldTile(cloak26.transform(1, 0, 0));
        addNPC(cloak27).setNextFaceWorldTile(cloak27.transform(1, 0, 0));
        addNPC(cloak28).setNextFaceWorldTile(cloak28.transform(1, 0, 0));
        addNPC(cloak29).setNextFaceWorldTile(cloak29.transform(1, 0, 0));
        addNPC(cloak30).setNextFaceWorldTile(cloak30.transform(-1, 0, 0));
        List<EliteDungeonNPC> block6NPCs = new ArrayList<EliteDungeonNPC>();
        block6NPCs.add(cloak25);
        block6NPCs.add(cloak26);
        block6NPCs.add(cloak27);
        block6NPCs.add(cloak28);
        block6NPCs.add(cloak29);
        block6NPCs.add(cloak30);
        addBlock(new WorldObject(111700, 10, 3, getTile(new WorldTile(4669, 9105, 1))), block6NPCs);

        EliteDungeonNPC rene10 = new EliteDungeonNPC(25607, getTile(new WorldTile(4655, 9045, 1)), this);
        EliteDungeonNPC rene11 = new EliteDungeonNPC(25607, getTile(new WorldTile(4655, 9044, 1)), this);
        EliteDungeonNPC rene12 = new EliteDungeonNPC(25607, getTile(new WorldTile(4655, 9043, 1)), this);
        rene10.walkTiles = new WorldTile[2];
        rene10.walkTiles[0] = getTile(new WorldTile(4641, 9045, 1));
        rene10.walkTiles[1] = getTile(new WorldTile(4655, 9045, 1));
        rene11.walkTiles = new WorldTile[2];
        rene11.walkTiles[0] = getTile(new WorldTile(4641, 9044, 1));
        rene11.walkTiles[1] = getTile(new WorldTile(4655, 9044, 1));
        rene12.walkTiles = new WorldTile[2];
        rene12.walkTiles[0] = getTile(new WorldTile(4641, 9043, 1));
        rene12.walkTiles[1] = getTile(new WorldTile(4655, 9043, 1));
        addNPC(rene10);
        addNPC(rene11);
        addNPC(rene12);
        EliteDungeonNPC lotus4 = new EliteDungeonNPC(25615, getTile(new WorldTile(4655, 9049, 1)), this);
        EliteDungeonNPC lotus5 = new EliteDungeonNPC(25614, getTile(new WorldTile(4656, 9049, 1)), this);
        addNPC(lotus4).setNextFaceWorldTile(lotus4.transform(0, 1, 0));
        addNPC(lotus5).setNextFaceWorldTile(lotus5.transform(0, 1, 0));
        EliteDungeonNPC cloak31 = new EliteDungeonNPC(25613, getTile(new WorldTile(4659, 9056, 1)), this);
        EliteDungeonNPC cloak32 = new EliteDungeonNPC(25612, getTile(new WorldTile(4658, 9062, 1)), this);
        EliteDungeonNPC cloak33 = new EliteDungeonNPC(25612, getTile(new WorldTile(4652, 9062, 1)), this);
        EliteDungeonNPC cloak34 = new EliteDungeonNPC(25613, getTile(new WorldTile(4652, 9056, 1)), this);
        addNPC(cloak31).setNextFaceWorldTile(cloak31.transform(-1, 1, 0));
        addNPC(cloak32).setNextFaceWorldTile(cloak32.transform(-1, -1, 0));
        addNPC(cloak33).setNextFaceWorldTile(cloak33.transform(1, -1, 0));
        addNPC(cloak34).setNextFaceWorldTile(cloak34.transform(1, 1, 0));

        EliteDungeonNPC lotus6 = new EliteDungeonNPC(25615, getTile(new WorldTile(4655, 9074, 1)), this);
        EliteDungeonNPC lotus7 = new EliteDungeonNPC(25615, getTile(new WorldTile(4653, 9074, 1)), this);
        addNPC(lotus6).setNextFaceWorldTile(lotus6.transform(0, -1, 0));
        addNPC(lotus7).setNextFaceWorldTile(lotus7.transform(0, -1, 0));

        EliteDungeonNPC rene13 = new EliteDungeonNPC(25607, getTile(new WorldTile(4666, 9060, 1)), this);
        EliteDungeonNPC rene14 = new EliteDungeonNPC(25607, getTile(new WorldTile(4667, 9060, 1)), this);
        EliteDungeonNPC rene15 = new EliteDungeonNPC(25607, getTile(new WorldTile(4668, 9060, 1)), this);
        rene13.walkTiles = new WorldTile[2];
        rene13.walkTiles[0] = getTile(new WorldTile(4666, 9074, 1));
        rene13.walkTiles[1] = getTile(new WorldTile(4666, 9060, 1));
        rene14.walkTiles = new WorldTile[2];
        rene14.walkTiles[0] = getTile(new WorldTile(4667, 9074, 1));
        rene14.walkTiles[1] = getTile(new WorldTile(4667, 9060, 1));
        rene15.walkTiles = new WorldTile[2];
        rene15.walkTiles[0] = getTile(new WorldTile(4668, 9074, 1));
        rene15.walkTiles[1] = getTile(new WorldTile(4668, 9060, 1));
        addNPC(rene13);
        addNPC(rene14);
        addNPC(rene15);

        EliteDungeonNPC rene16 = new EliteDungeonNPC(25607, getTile(new WorldTile(4659, 9082, 1)), this);
        EliteDungeonNPC rene17 = new EliteDungeonNPC(25607, getTile(new WorldTile(4659, 9084, 1)), this);
        EliteDungeonNPC rene18 = new EliteDungeonNPC(25607, getTile(new WorldTile(4657, 9084, 1)), this);
        EliteDungeonNPC rene19 = new EliteDungeonNPC(25607, getTile(new WorldTile(4656, 9085, 1)), this);
        EliteDungeonNPC rene20 = new EliteDungeonNPC(25607, getTile(new WorldTile(4659, 9086, 1)), this);
        addNPC(rene16).setNextFaceWorldTile(rene16.transform(0, -1, 0));
        addNPC(rene17).setNextFaceWorldTile(rene17.transform(1, 1, 0));
        addNPC(rene18).setNextFaceWorldTile(rene18.transform(0, -1, 0));
        addNPC(rene19).setNextFaceWorldTile(rene19.transform(-1, 0, 0));
        addNPC(rene20).setNextFaceWorldTile(rene20.transform(1, 0, 0));

//      25625 - Ol' Sawtooth croc moves randomly
        EliteDungeonMiniBoss mini1 = new EliteDungeonMiniBoss(25625, getTile(new WorldTile(4593, 9054, 1)), this);
        mini1.setFaceTile(0, -1, 0);

//      25624 - Jade the Unruly 
        EliteDungeonMiniBoss mini2 = new EliteDungeonMiniBoss(25624, getTile(new WorldTile(4523, 9093, 1)), this);
        mini2.setForceTalkDialogue(new String[] { "Hello little cutie! Time to die.", "We've been told to keep everyone away.", "Acolytes. Pirates. Heroes.", "In these halls, you die by my hand." });
        mini2.setFaceTile(-1, 0, 0);

//      25626 - Menaphite guard captain
        EliteDungeonMiniBoss mini3 = new EliteDungeonMiniBoss(25626, getTile(new WorldTile(4523, 9093, 1)), this);
        mini3.setForceTalkDialogue(new String[] { "Keep them from messing with the refinery!", "The black stone. The crystals.", "They hold great power. A key to yet more.", "All hope was lost in Menaphos.", "We escaped the corruption. We are pure.", "You'll hamper us no longer!" });
        mini3.setFaceTile(1, 0, 0);

//      25627 - Horrific crassian
        EliteDungeonMiniBoss mini4 = new EliteDungeonMiniBoss(25627, getTile(new WorldTile(4655, 9059, 1)), this);
        mini4.setForceTalkDialogue(new String[] { "Reeeeeeeeeeeeeeeeeeek!", "Kranon sees you!", "He beckonssssss...", "Reeeeeeeeeeek!" });
        mini4.setFaceTile(0, 1, 0);
        List<EliteDungeonMiniBoss> minis = new ArrayList<EliteDungeonMiniBoss>();
        minis.add(mini1);
        minis.add(mini2);
        minis.add(mini3);
        minis.add(mini4);
        Collections.shuffle(minis);
        if (miniBosses == null) {
            miniBosses = new ArrayList<EliteDungeonMiniBoss>();
            int size = Utils.getRandom(3);
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
    public int getEliteDungeonType() {
        return 1;
    }

    @Override
    public int getRoomIndex() {
        return 2;
    }

    @Override
    public boolean isInBossFightArea(WorldTile tile) {
        return false;
    }
}
