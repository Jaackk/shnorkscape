package com.rs.game.player.client;
import java.io.*;
import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;
public class Native950GamevalLookupTest {
    @Test public void searchFindsNamesIdsHashesTypesAndExcludesUnverifiedFrame(){
        List<Native950GamevalLookup.Entry> sky=Native950GamevalLookup.search("component SKYBOX_TAB");assertEquals(3,sky.size());assertEquals("623:27",sky.get(0).id);
        assertEquals(sky.subList(0,1),Native950GamevalLookup.search("40828955"));assertEquals(sky.subList(0,1),Native950GamevalLookup.search("623:27"));
        assertTrue(Native950GamevalLookup.search("1477:713").isEmpty());assertTrue(Native950GamevalLookup.search("8286").isEmpty());
        assertEquals("combatv2_buff_necromancy_residual_soul_stacks",Native950GamevalLookup.search("11035").get(0).name);
        assertTrue(Native950GamevalLookup.search("interface machinima").stream().allMatch(e->e.type.equals("interface")));
        assertEquals(1438,Native950GamevalLookup.search("").size());
    }
    @Test public void provenanceCanBeSearchedWithoutDefeatingTypeFilter(){
        assertFalse(Native950GamevalLookup.search("sequence 949").isEmpty());
        assertTrue(Native950GamevalLookup.search("sequence 949").stream().allMatch(e->e.type.equals("sequence")));
        assertFalse(Native950GamevalLookup.search("payload match").isEmpty());
        assertEquals("almighty",Native950DeveloperActions.search("Combat","dm",Collections.emptySet()).get(0).id);
    }
    @Test public void changedPayloadNeverClaimsVerification(){
        Native950GamevalLookup.Entry entry=Native950GamevalLookup.search("623:27").get(0);
        assertFalse(entry.matches(null));assertFalse(entry.matches(new byte[]{1,2,3}));
        assertTrue(entry.evidence.contains("identical"));
        assertTrue(Native950GamevalLookup.search("11035").get(0).evidence.contains("payload match"));
    }
    @Test public void duplicatesAreRejectedRatherThanSilentlyRetargeted()throws Exception{
        String row="component\t1:2\tfoo:bar\t3\t1\t-1\t"+String.join("",Collections.nCopies(64,"a"))+"\tfixture\t"+String.join("",Collections.nCopies(64,"b"))+"\n";
        try{Native950GamevalLookup.load(new BufferedReader(new StringReader(row+row)));fail();}catch(IllegalArgumentException expected){}
        assertEquals(1,Native950GamevalLookup.load(new BufferedReader(new StringReader(row))).size());
        assertTrue(Native950DevelopmentCommands.isCommand(";;gameval 623:27"));
        assertEquals(";;gameval 623:27",Native950DeveloperActions.find("gameval").command(Arrays.asList("623:27")));
    }
}
