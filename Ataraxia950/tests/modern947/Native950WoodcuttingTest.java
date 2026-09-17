package com.rs.game.player.client;

import com.rs.game.player.actions.woodcutting.Woodcutting;
import com.rs.game.player.actions.woodcutting.WoodcuttingDefinitions.TreeDefinitions;
import org.junit.Test;
import static org.junit.Assert.*;

/** Shared original skill policy and cache-name admission, independent of installed cache files. */
public class Native950WoodcuttingTest {
    @Test public void originalOrdinaryTreeNamesSelectTheirExistingDefinitions() {
        String[] names={"Tree","Evergreen","Dead tree","Oak","Willow","Maple tree","Mahogany","Teak","Yew","Magic tree","Elder tree"};
        TreeDefinitions[] types={TreeDefinitions.NORMAL,TreeDefinitions.EVERGREEN,TreeDefinitions.DEAD,TreeDefinitions.OAK,
                TreeDefinitions.WILLOW,TreeDefinitions.MAPLE,TreeDefinitions.MAHOGANY,TreeDefinitions.TEAK,
                TreeDefinitions.YEW,TreeDefinitions.MAGIC,TreeDefinitions.ELDER};
        for(int i=0;i<names.length;i++)assertSame(types[i],Native950Woodcutting.definition(names[i],new String[]{"Chop down",null,null,null,null}));
    }
    @Test public void aNameWithoutACacheChopOperationCannotRunTheAction() {
        assertNull(Native950Woodcutting.definition("Tree",new String[]{"Inspect"}));
        assertNull(Native950Woodcutting.definition("Oak",null));
        assertNull(Native950Woodcutting.definition(null,new String[]{"Chop down"}));
    }
    @Test public void repurposedObjectsAndQuestResourcesDoNotFallThroughToOrdinaryLogs() {
        for(String name:new String[]{"Target","Abyssal portal","Dramen tree","Dream tree","Divine tree","Tree stump","Crystal tree shard"})
            assertNull(name,Native950Woodcutting.definition(name,new String[]{"Chop down"}));
    }
    @Test public void treeMenuMayMoveSlotsWithoutInventingAnOperation() {
        assertSame(TreeDefinitions.NORMAL,Native950Woodcutting.definition("TREE",new String[]{null,"Chop down"}));
        assertNull(Native950Woodcutting.definition("Tree",new String[]{"Chop"}));
    }
    @Test public void originalBronzeNormalTreeCurveRetainsItsEndpointsAndLevelCap() {
        assertEquals(80,Woodcutting.ordinaryChopChance(TreeDefinitions.NORMAL,1,100));
        assertEquals(160,Woodcutting.ordinaryChopChance(TreeDefinitions.NORMAL,99,100));
        assertEquals(160,Woodcutting.ordinaryChopChance(TreeDefinitions.NORMAL,120,100));
        assertEquals(80,Woodcutting.ordinaryChopChance(TreeDefinitions.NORMAL,0,100));
    }
    @Test public void higherToolsImproveOrdinarySuccessWithoutChangingTheTreeXpOrRequirements() {
        int bronze=Woodcutting.ordinaryChopChance(TreeDefinitions.OAK,41,100);
        int rune=Woodcutting.ordinaryChopChance(TreeDefinitions.OAK,41,600);
        assertTrue(rune>bronze);
        assertEquals(15,TreeDefinitions.OAK.getLevel());
        assertEquals(37.5,TreeDefinitions.OAK.getXp(),0);
        assertEquals(1521,TreeDefinitions.OAK.getLogsId());
        assertEquals(15,TreeDefinitions.OAK.getRespawnDelay());
    }
    @Test public void lateTierTreesStaySlowerAndProbabilityRemainsBounded() {
        for(TreeDefinitions type:TreeDefinitions.values())for(int level:new int[]{-1,1,50,99,120})for(int power:new int[]{-1,100,800,100000}) {
            int chance=Woodcutting.ordinaryChopChance(type,level,power);
            assertTrue(type+" invalid probability",chance>=1&&chance<=254);
        }
        assertTrue(Woodcutting.ordinaryChopChance(TreeDefinitions.MAGIC,75,600)
                <Woodcutting.ordinaryChopChance(TreeDefinitions.NORMAL,75,600));
    }
}
