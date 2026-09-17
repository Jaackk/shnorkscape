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

public class Room1 extends EliteDungeonHandledRoom {

    private static final long serialVersionUID = 4703493471147098633L;
    private List<EliteDungeonMiniBoss> miniBosses;

    @Override
    public void load() {
        super.load();
        EliteDungeonNPC n1 = new EliteDungeonNPC(25576, getTile(new WorldTile(4511, 9188, 0)), this);
        EliteDungeonNPC n2 = new EliteDungeonNPC(25576, getTile(new WorldTile(4511, 9182, 0)), this);
        addNPC(n1).setNextFaceWorldTile(n1.transform(-1, 0, 0));
        addNPC(n2).setNextFaceWorldTile(n2.transform(-1, 0, 0));
        EliteDungeonNPC n3 = new EliteDungeonNPC(25578, getTile(new WorldTile(4521, 9199, 0)), this);
        EliteDungeonNPC n4 = new EliteDungeonNPC(25574, getTile(new WorldTile(4522, 9198, 0)), this);
        EliteDungeonNPC n5 = new EliteDungeonNPC(25575, getTile(new WorldTile(4522, 9195, 0)), this);
        EliteDungeonNPC n6 = new EliteDungeonNPC(25576, getTile(new WorldTile(4520, 9193, 0)), this);
        addNPC(n3).setNextFaceWorldTile(n3.transform(-1, -1, 0));
        addNPC(n4).setNextFaceWorldTile(n4.transform(-1, 0, 0));
        addNPC(n5).setNextFaceWorldTile(n5.transform(-1, 0, 0));
        addNPC(n6).setNextFaceWorldTile(n6.transform(0, 1, 0));
        List<EliteDungeonNPC> block1NPCs = new ArrayList<EliteDungeonNPC>();
        block1NPCs.add(n3);
        block1NPCs.add(n4);
        block1NPCs.add(n5);
        block1NPCs.add(n6);
        addBlock(new WorldObject(111706, 10, 3, getTile(new WorldTile(4523, 9194, 0))), block1NPCs);

        EliteDungeonNPC n7 = new EliteDungeonNPC(25575, getTile(new WorldTile(4521, 9171, 0)), this);
        EliteDungeonNPC n9 = new EliteDungeonNPC(25575, getTile(new WorldTile(4521, 9174, 0)), this);
        EliteDungeonNPC n10 = new EliteDungeonNPC(25576, getTile(new WorldTile(4518, 9177, 0)), this);

        EliteDungeonNPC n11 = new EliteDungeonNPC(25576, getTile(new WorldTile(4516, 9170, 0)), this);
        EliteDungeonNPC n12 = new EliteDungeonNPC(25575, getTile(new WorldTile(4510, 9171, 0)), this);
        addNPC(n7).setNextFaceWorldTile(n7.transform(-1, 1, 0));
        addNPC(n9).setNextFaceWorldTile(n9.transform(-1, 0, 0));
        addNPC(n10).setNextFaceWorldTile(n10.transform(0, 1, 0));
        addNPC(n11).setNextFaceWorldTile(n11.transform(0, 1, 0));
        addNPC(n12).setNextFaceWorldTile(n12.transform(0, 1, 0));
        List<EliteDungeonNPC> block2NPCs = new ArrayList<EliteDungeonNPC>();
        block2NPCs.add(n7);
        block2NPCs.add(n9);
        block2NPCs.add(n10);
        addBlock(new WorldObject(111706, 10, 3, getTile(new WorldTile(4523, 9170, 0))), block2NPCs);
        EliteDungeonNPC n13 = new EliteDungeonNPC(25577, getTile(new WorldTile(4522, 9173, 0)), this);
        n13.walkTiles = new WorldTile[4];
        n13.walkTiles[0] = getTile(new WorldTile(4522, 9173, 0));
        n13.walkTiles[1] = getTile(new WorldTile(4510, 9173, 0));
        n13.walkTiles[2] = getTile(new WorldTile(4510, 9197, 0));
        n13.walkTiles[3] = getTile(new WorldTile(4522, 9197, 0));
        addNPC(n13).setNextFaceWorldTile(n13.transform(-1, 0, 0));

        EliteDungeonNPC n14 = new EliteDungeonNPC(25575, getTile(new WorldTile(4533, 9190, 0)), this);
        EliteDungeonNPC n15 = new EliteDungeonNPC(25576, getTile(new WorldTile(4533, 9193, 0)), this);
        EliteDungeonNPC n16 = new EliteDungeonNPC(25576, getTile(new WorldTile(4532, 9192, 0)), this);
        addNPC(n14).setNextFaceWorldTile(n14.transform(0, 1, 0));
        addNPC(n15).setNextFaceWorldTile(n15.transform(0, -1, 0));
        addNPC(n16).setNextFaceWorldTile(n16.transform(1, -1, 0));

        EliteDungeonNPC n17 = new EliteDungeonNPC(25576, getTile(new WorldTile(4532, 9180, 0)), this);
        addNPC(n17).setNextFaceWorldTile(n17.transform(1, 0, 0));

        EliteDungeonNPC n18 = new EliteDungeonNPC(25575, getTile(new WorldTile(4543, 9181, 0)), this);
        addNPC(n18).setNextFaceWorldTile(n18.transform(0, 1, 0));

        EliteDungeonNPC n19 = new EliteDungeonNPC(25576, getTile(new WorldTile(4548, 9169, 0)), this);
        EliteDungeonNPC n20 = new EliteDungeonNPC(25578, getTile(new WorldTile(4550, 9169, 0)), this);
        addNPC(n19).setNextFaceWorldTile(n19.transform(0, 1, 0));
        addNPC(n20).setNextFaceWorldTile(n20.transform(0, 1, 0));
        List<EliteDungeonNPC> block3NPCs = new ArrayList<EliteDungeonNPC>();
        block3NPCs.add(n19);
        block3NPCs.add(n20);
        addBlock(new WorldObject(111701, 10, 0, getTile(new WorldTile(4547, 9168, 0))), block3NPCs);

        EliteDungeonNPC n21 = new EliteDungeonNPC(25575, getTile(new WorldTile(4572, 9172, 0)), this);
        EliteDungeonNPC n22 = new EliteDungeonNPC(25577, getTile(new WorldTile(4573, 9172, 0)), this);
        EliteDungeonNPC n23 = new EliteDungeonNPC(25578, getTile(new WorldTile(4573, 9173, 0)), this);
        addNPC(n21).setNextFaceWorldTile(n21.transform(0, 1, 0));
        addNPC(n22).setNextFaceWorldTile(n22.transform(-1, 1, 0));
        addNPC(n23).setNextFaceWorldTile(n23.transform(-1, 0, 0));
        List<EliteDungeonNPC> block4NPCs = new ArrayList<EliteDungeonNPC>();
        block4NPCs.add(n21);
        block4NPCs.add(n22);
        block4NPCs.add(n23);
        addBlock(new WorldObject(111701, 10, 0, getTile(new WorldTile(4569, 9168, 0))), block4NPCs);

        EliteDungeonNPC n24 = new EliteDungeonNPC(25576, getTile(new WorldTile(4548, 9177, 0)), this);
        n24.walkTiles = new WorldTile[2];
        n24.walkTiles[0] = getTile(new WorldTile(4548, 9177, 0));
        n24.walkTiles[1] = getTile(new WorldTile(4570, 9177, 0));
        addNPC(n24).setNextFaceWorldTile(n24.transform(1, 0, 0));

        EliteDungeonNPC n25 = new EliteDungeonNPC(25576, getTile(new WorldTile(4548, 9199, 0)), this);
        EliteDungeonNPC n26 = new EliteDungeonNPC(25576, getTile(new WorldTile(4550, 9199, 0)), this);
        addNPC(n25).setNextFaceWorldTile(n25.transform(0, -1, 0));
        addNPC(n26).setNextFaceWorldTile(n26.transform(0, -1, 0));
        List<EliteDungeonNPC> block5NPCs = new ArrayList<EliteDungeonNPC>();
        block5NPCs.add(n25);
        block5NPCs.add(n26);
        addBlock(new WorldObject(111701, 10, 2, getTile(new WorldTile(4547, 9202, 0))), block5NPCs);

        EliteDungeonNPC n27 = new EliteDungeonNPC(25575, getTile(new WorldTile(4570, 9193, 0)), this);
        n27.walkTiles = new WorldTile[2];
        n27.walkTiles[0] = getTile(new WorldTile(4570, 9193, 0));
        n27.walkTiles[1] = getTile(new WorldTile(4550, 9193, 0));
        addNPC(n27).setNextFaceWorldTile(n27.transform(-1, 0, 0));

        EliteDungeonNPC n28 = new EliteDungeonNPC(25575, getTile(new WorldTile(4553, 9207, 0)), this);
        EliteDungeonNPC n29 = new EliteDungeonNPC(25574, getTile(new WorldTile(4551, 9207, 0)), this);
        EliteDungeonNPC n30 = new EliteDungeonNPC(25577, getTile(new WorldTile(4547, 9207, 0)), this);
        EliteDungeonNPC n31 = new EliteDungeonNPC(25578, getTile(new WorldTile(4545, 9207, 0)), this);
        addNPC(n28).setNextFaceWorldTile(n28.transform(0, 1, 0));
        addNPC(n29).setNextFaceWorldTile(n29.transform(0, 1, 0));
        addNPC(n30).setNextFaceWorldTile(n30.transform(0, 1, 0));
        addNPC(n31).setNextFaceWorldTile(n31.transform(0, 1, 0));
        EliteDungeonNPC n32 = new EliteDungeonNPC(25578, getTile(new WorldTile(4553, 9211, 0)), this);
        EliteDungeonNPC n33 = new EliteDungeonNPC(25577, getTile(new WorldTile(4551, 9211, 0)), this);
        EliteDungeonNPC n34 = new EliteDungeonNPC(25575, getTile(new WorldTile(4547, 9211, 0)), this);
        EliteDungeonNPC n35 = new EliteDungeonNPC(25576, getTile(new WorldTile(4545, 9211, 0)), this);
        addNPC(n32).setNextFaceWorldTile(n32.transform(0, 1, 0));
        addNPC(n33).setNextFaceWorldTile(n33.transform(0, 1, 0));
        addNPC(n34).setNextFaceWorldTile(n34.transform(0, 1, 0));
        addNPC(n35).setNextFaceWorldTile(n35.transform(0, 1, 0));
        EliteDungeonNPC n36 = new EliteDungeonNPC(25575, getTile(new WorldTile(4553, 9215, 0)), this);
        EliteDungeonNPC n37 = new EliteDungeonNPC(25576, getTile(new WorldTile(4551, 9215, 0)), this);
        EliteDungeonNPC n38 = new EliteDungeonNPC(25575, getTile(new WorldTile(4547, 9215, 0)), this);
        EliteDungeonNPC n39 = new EliteDungeonNPC(25574, getTile(new WorldTile(4545, 9215, 0)), this);
        addNPC(n36).setNextFaceWorldTile(n36.transform(0, 1, 0));
        addNPC(n37).setNextFaceWorldTile(n37.transform(0, 1, 0));
        addNPC(n38).setNextFaceWorldTile(n38.transform(0, 1, 0));
        addNPC(n39).setNextFaceWorldTile(n39.transform(0, 1, 0));

        EliteDungeonNPC n40 = new EliteDungeonNPC(25575, getTile(new WorldTile(4557, 9209, 0)), this);
        addNPC(n40).setNextFaceWorldTile(n40.transform(-1, 0, 0));

        EliteDungeonNPC n41 = new EliteDungeonNPC(25575, getTile(new WorldTile(4561, 9208, 0)), this);
        EliteDungeonNPC n42 = new EliteDungeonNPC(25577, getTile(new WorldTile(4561, 9210, 0)), this);
        addNPC(n41).setNextFaceWorldTile(n41.transform(1, 0, 0));
        addNPC(n42).setNextFaceWorldTile(n42.transform(1, 0, 0));
        List<EliteDungeonNPC> block6NPCs = new ArrayList<EliteDungeonNPC>();
        block6NPCs.add(n41);
        block6NPCs.add(n42);
        addBlock(new WorldObject(111701, 10, 3, getTile(new WorldTile(4559, 9207, 0))), block6NPCs);

        EliteDungeonNPC n43 = new EliteDungeonNPC(25575, getTile(new WorldTile(4563, 9216, 0)), this);
        EliteDungeonNPC n44 = new EliteDungeonNPC(25577, getTile(new WorldTile(4566, 9216, 0)), this);
        addNPC(n43).setNextFaceWorldTile(n43.transform(1, 0, 0));
        addNPC(n44).setNextFaceWorldTile(n44.transform(-1, 0, 0));

        EliteDungeonNPC leader = new EliteDungeonNPC(25579, getTile(new WorldTile(4576, 9205, 0)), this);
        addNPC(leader).setNextFaceWorldTile(leader.transform(0, -1, 0));

        EliteDungeonNPC west1 = new EliteDungeonNPC(25574, getTile(new WorldTile(4574, 9202, 0)), this);
        EliteDungeonNPC west2 = new EliteDungeonNPC(25574, getTile(new WorldTile(4574, 9200, 0)), this);
        EliteDungeonNPC west3 = new EliteDungeonNPC(25574, getTile(new WorldTile(4574, 9198, 0)), this);
        EliteDungeonNPC west4 = new EliteDungeonNPC(25574, getTile(new WorldTile(4574, 9196, 0)), this);
        addNPC(west1).setNextFaceWorldTile(west1.transform(1, 0, 0));
        addNPC(west2).setNextFaceWorldTile(west2.transform(1, 0, 0));
        addNPC(west3).setNextFaceWorldTile(west3.transform(1, 0, 0));
        addNPC(west4).setNextFaceWorldTile(west4.transform(1, 0, 0));

        EliteDungeonNPC east1 = new EliteDungeonNPC(25574, getTile(new WorldTile(4578, 9202, 0)), this);
        EliteDungeonNPC east2 = new EliteDungeonNPC(25574, getTile(new WorldTile(4578, 9200, 0)), this);
        EliteDungeonNPC east3 = new EliteDungeonNPC(25574, getTile(new WorldTile(4578, 9198, 0)), this);
        EliteDungeonNPC east4 = new EliteDungeonNPC(25574, getTile(new WorldTile(4578, 9196, 0)), this);
        addNPC(east1).setNextFaceWorldTile(east1.transform(-1, 0, 0));
        addNPC(east2).setNextFaceWorldTile(east2.transform(-1, 0, 0));
        addNPC(east3).setNextFaceWorldTile(east3.transform(-1, 0, 0));
        addNPC(east4).setNextFaceWorldTile(east4.transform(-1, 0, 0));
        List<EliteDungeonNPC> block7NPCs = new ArrayList<EliteDungeonNPC>();
        block7NPCs.add(leader);
        block7NPCs.add(west1);
        block7NPCs.add(west2);
        block7NPCs.add(west3);
        block7NPCs.add(west4);
        block7NPCs.add(east1);
        block7NPCs.add(east2);
        block7NPCs.add(east3);
        block7NPCs.add(east4);
        addBlock(new WorldObject(111701, 10, 3, getTile(new WorldTile(4582, 9207, 0))), block7NPCs);

        EliteDungeonNPC allTimeHealer = new EliteDungeonNPC(25577, getTile(new WorldTile(4544, 9173, 0)), this);
        allTimeHealer.walkTiles = new WorldTile[8];
        allTimeHealer.walkTiles[0] = getTile(new WorldTile(4544, 9173, 0));
        allTimeHealer.walkTiles[1] = getTile(new WorldTile(4539, 9178, 0));
        allTimeHealer.walkTiles[2] = getTile(new WorldTile(4539, 9195, 0));
        allTimeHealer.walkTiles[3] = getTile(new WorldTile(4549, 9195, 0));
        allTimeHealer.walkTiles[4] = getTile(new WorldTile(4549, 9197, 0));
        allTimeHealer.walkTiles[5] = getTile(new WorldTile(4572, 9197, 0));
        allTimeHealer.walkTiles[6] = getTile(new WorldTile(4572, 9182, 0));
        allTimeHealer.walkTiles[7] = getTile(new WorldTile(4563, 9173, 0));
        addNPC(allTimeHealer).setNextFaceWorldTile(allTimeHealer.transform(-1, 0, 0));

        EliteDungeonNPC elite1 = new EliteDungeonNPC(25583, getTile(new WorldTile(4593, 9207, 0)), this);
        EliteDungeonNPC elite2 = new EliteDungeonNPC(25584, getTile(new WorldTile(4593, 9209, 0)), this);
        EliteDungeonNPC elite3 = new EliteDungeonNPC(25583, getTile(new WorldTile(4593, 9211, 0)), this);
        addNPC(elite1).setNextFaceWorldTile(elite1.transform(-1, 0, 0));
        addNPC(elite2).setNextFaceWorldTile(elite2.transform(-1, 0, 0));
        addNPC(elite3).setNextFaceWorldTile(elite3.transform(-1, 0, 0));
        List<EliteDungeonNPC> block8NPCs = new ArrayList<EliteDungeonNPC>();
        block8NPCs.add(elite1);
        block8NPCs.add(elite2);
        block8NPCs.add(elite3);
        addBlock(new WorldObject(111707, 10, 3, getTile(new WorldTile(4595, 9205, 0))), block8NPCs);
        List<EliteDungeonMiniBoss> minis = new ArrayList<EliteDungeonMiniBoss>();
//      25619 - Olivia the Chronicler 
        EliteDungeonMiniBoss mini1 = new EliteDungeonMiniBoss(25619, getTile(new WorldTile(4581, 9185, 0)), this);
        mini1.setForceTalkDialogue(new String[] { "Pirates? Inside the temple?", "Well we can't have that...", "We have been warned against your type.", "No one is permitted inside the sanctum!" });
        mini1.setFaceTile(-1, 0, 0);

//      25621 - Sarkhan the Serpentspeaker 
        EliteDungeonMiniBoss mini2 = new EliteDungeonMiniBoss(25621, getTile(new WorldTile(4549, 9222, 0)), this);
        mini2.setForceTalkDialogue(new String[] { "What a mess you've made...", "We protect Seiryu, for the greater good.", "How dare you defile this monastery.", "Is it treasure that you seek?" });
        mini2.setFaceTile(0, -1, 0);

//      25622 - Ahoeitu the Chef
        EliteDungeonMiniBoss mini3 = new EliteDungeonMiniBoss(25622, getTile(new WorldTile(4581, 9161, 0)), this);
        mini3.setForceTalkDialogue(new String[] { "Pirates? In MY kitchen!?", "I'll cleave your grabbers off.", "I've got to prep another fifty meals after this...", "Seiryu, grant me strength!" });
        mini3.setFaceTile(1, 0, 0);

//      25631 - Oyu the Quietest 
        EliteDungeonMiniBoss mini4 = new EliteDungeonMiniBoss(25631, getTile(new WorldTile(4577, 9216, 0)), this);
        mini4.setForceTalkDialogue(new String[] { "..." });
        mini4.setFaceTile(0, -1, 0);

//      25623 - Xiang the Water-shaper 
        EliteDungeonMiniBoss mini5 = new EliteDungeonMiniBoss(25623, getTile(new WorldTile(4566, 9189, 0)), this);
        mini5.setForceTalkDialogue(new String[] { "Perhaps defeating you will allow me to ascend...", "I, Xiang, call upon the waters!", "You!", "Are!", "Not!", "Welcome!" });
        mini5.setFaceTile(0, 1, 0);
        minis.add(mini1);
        minis.add(mini2);
        minis.add(mini3);
        minis.add(mini4);
        minis.add(mini5);
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
        return 0;
    }

    @Override
    public boolean isInBossFightArea(WorldTile tile) {
        return false;
    }

}
