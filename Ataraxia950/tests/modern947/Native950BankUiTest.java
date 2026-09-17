package com.rs.game.player.client;

import com.rs.network.protocol.modern950.Native950Packets;
import com.rs.network.protocol.modern950.Native950Protocol.ServerPacket;
import org.junit.Test;
import java.util.*;
import static org.junit.Assert.*;

public class Native950BankUiTest {
    @Test public void transferMenusAllowOnlyQuantityActionsAndExamine() {
        Map<Integer,int[]> events=events(Native950BankUi.openControls());
        for(int component:new int[]{15,201}) {
            int[] event=events.get(component);assertNotNull(event);
            for(int operation=1;operation<=10;operation++)
                assertEquals("operation "+operation,operation<=7 || operation==10,(event[2]&(1<<operation))!=0);
            assertEquals("No drag, Use-target or unrelated property flags",0,event[2]&~0x7fe);
            assertEquals(0,event[0]);assertEquals(component==15?27:599,event[1]);
        }
        int[] worn=events.get(35);assertArrayEquals(new int[]{0,18,0},worn);
    }
    @Test public void staticControlsDoNotEnableDynamicActorRanges() {
        Map<Integer,int[]> events=events(Native950BankUi.openControls());
        for(Map.Entry<Integer,int[]> row:events.entrySet()) {
            if(row.getKey()==15 || row.getKey()==35 || row.getKey()==201)continue;
            assertEquals(-1,row.getValue()[0]);assertEquals(-1,row.getValue()[1]);
        }
        for(int component:new int[]{93,96,99,103,106,114,127,237,42})
            assertArrayEquals(new int[]{-1,-1,2},events.get(component));
        assertFalse("Root containers are not buttons",events.containsKey(67));
    }
    @Test public void openingMakesQuantityAndNotesVisibleWithoutEnablingUnsupportedDestinations() {
        Map<Integer,Boolean> hidden=new HashMap<>();
        for(Native950Packets.Packet p:Native950BankUi.openControls())if(p.type()==ServerPacket.IF_SETHIDE) {
            byte[] raw=p.payload();int hash=(u(raw[2])<<24)|(u(raw[3])<<16)|(u(raw[0])<<8)|u(raw[1]);
            assertEquals(517,hash>>>16);hidden.put(hash&65535,raw[4]!=0);
        }
        assertEquals(Boolean.FALSE,hidden.get(91));assertEquals(Boolean.FALSE,hidden.get(126));
        assertEquals(Boolean.TRUE,hidden.get(58));assertEquals(Boolean.TRUE,hidden.get(62));assertEquals(Boolean.TRUE,hidden.get(122));
    }
    @Test public void resettingUnsupportedModesPreservesSearchAndRealPreferences() {
        Set<Integer> fields=new HashSet<>();
        for(Native950Packets.Packet p:Native950BankUi.resetUnsupportedModes()) {
            assertEquals(ServerPacket.VARBIT_SMALL,p.type());byte[] raw=p.payload();
            int id=u(raw[1])|(u(raw[2])<<8);assertTrue(fields.add(id));assertEquals(0,(u(raw[0])-128)&255);
        }
        assertEquals(new HashSet<>(Arrays.asList(45139,45190,45191,45911)),fields);
        assertFalse(fields.contains(45189));assertFalse(fields.contains(45141));
    }
    @Test public void maximumCustomQuantityAndNotePreferenceAreFullWidthAndIndependent() {
        List<Native950Packets.Packet> out=Native950BankUi.sync(5,Integer.MAX_VALUE,true);
        Map<Integer,Integer> vars=new HashMap<>();
        for(Native950Packets.Packet p:out)if(p.type()==ServerPacket.VARP_LARGE) {
            byte[] raw=p.payload();int id=((u(raw[0])-128)&255)|(u(raw[1])<<8);
            int value=(u(raw[4])<<24)|(u(raw[5])<<16)|(u(raw[2])<<8)|u(raw[3]);vars.put(id,value);
        }
        assertEquals(Integer.valueOf(Integer.MAX_VALUE),vars.get(111));assertEquals(Integer.valueOf(1),vars.get(160));assertEquals(2,vars.size());
        Native950Packets.Packet mode=out.get(0);assertEquals(ServerPacket.VARBIT_SMALL,mode.type());assertEquals(5,(u(mode.payload()[0])-128)&255);
    }
    @Test public void countsCannotBeMisinterpretedAsEncodedQuantityModes() {
        for(int mode:new int[]{-1,0,1,6,10,99}) {
            try { Native950BankUi.sync(mode,10,false);fail("accepted mode "+mode); }catch(IllegalArgumentException expected){ }
        }
        for(int x:new int[]{0,-1,Integer.MIN_VALUE}) {
            try { Native950BankUi.sync(2,x,false);fail("accepted amount "+x); }catch(IllegalArgumentException expected){ }
        }
        for(int mode:new int[]{2,3,4,5,7})assertTrue(Native950BankUi.validQuantityMode(mode));
    }
    @Test public void cacheDerivedSelectionAndLocalSearchRecognitionAreExact() {
        assertEquals(2,Native950BankUi.quantityMode(93));assertEquals(3,Native950BankUi.quantityMode(96));
        assertEquals(4,Native950BankUi.quantityMode(99));assertEquals(7,Native950BankUi.quantityMode(103));
        assertEquals(5,Native950BankUi.quantityMode(106));assertEquals(5,Native950BankUi.quantityMode(114));
        for(int component:new int[]{237,238,239}) { assertTrue(Native950BankUi.isControl(component));assertTrue(Native950BankUi.isLocalSearchControl(component));assertFalse(Native950BankUi.isUnsupportedControl(component)); }
        for(int component:new int[]{0,15,35,91,126,201,236,240}) { assertFalse(Native950BankUi.isControl(component));assertEquals(0,Native950BankUi.quantityMode(component)); }
    }
    private static Map<Integer,int[]> events(List<Native950Packets.Packet> packets) {
        Map<Integer,int[]> result=new HashMap<>();
        for(Native950Packets.Packet packet:packets)if(packet.type()==ServerPacket.IF_SETEVENTS) {
            byte[] raw=packet.payload();int mask=(u(raw[2])<<24)|(u(raw[3])<<16)|(u(raw[0])<<8)|u(raw[1]);
            int last=(short)((u(raw[4])<<8)|u(raw[5]));int first=(short)((u(raw[6])<<8)|((u(raw[7])-128)&255));
            int hash=u(raw[8])|(u(raw[9])<<8)|(u(raw[10])<<16)|(u(raw[11])<<24);assertEquals(517,hash>>>16);
            assertNull("No contradictory event writes",result.put(hash&65535,new int[]{first,last,mask}));
        }
        return result;
    }
    private static int u(byte b){return b&255;}
}
