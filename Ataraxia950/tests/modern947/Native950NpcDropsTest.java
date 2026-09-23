package com.rs.game.player.client;

import com.rs.game.item.Item;
import com.rs.utils.data.parsers.npcs.NPCDropTableRolls;
import com.rs.utils.data.parsers.npcs.pojos.NPCDrop;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.*;

public final class Native950NpcDropsTest {
    @Test public void borkCustomRewardsPreserveAuthoredQuantitiesAndRefuseChangedIdentities(){
        int[] ids={532,995,12163,12160,12159,12158,1618,1620,1622,1624};
        String[] names={"Big bones","Coins","Blue charm","Crimson charm","Green charm","Gold charm",
                "Uncut diamond","Uncut ruby","Uncut emerald","Uncut sapphire"};
        Map<Integer,Native950NpcDrops.ItemMetadata> types=new HashMap<>();
        for(int i=0;i<ids.length;i++)types.put(ids[i],new Native950NpcDrops.ItemMetadata(ids[i],names[i],i!=0,i>=6,ids[i]));
        List<Item> result=Native950NpcDrops.borkDrops(types::get,0);
        assertEquals(10,result.size());assertEquals(1,result.get(0).getAmount());assertEquals(800000,result.get(1).getAmount());
        assertEquals(30,result.get(2).getAmount());assertEquals(50,result.get(9).getAmount());
        types.remove(1618);result=Native950NpcDrops.borkDrops(types::get,0);
        assertEquals(9,result.size());assertFalse(result.stream().anyMatch(item->item.getId()==1618));
    }
    private static final NPCDropTableRolls.Rolls LOW = new NPCDropTableRolls.Rolls() {
        public double percentage(double max){return 1;}
        public void shuffle(List<NPCDrop> rows) { }
        public int inclusive(int max){return 0;}
    };
    @Test public void nativeAdapterUsesGuaranteedAndThreeRandomRulesAndAuthoredAmounts() {
        NPCDrop[] table={new NPCDrop(526,100,1),new NPCDrop(995,50,3,10),new NPCDrop(555,50,2),
                new NPCDrop(556,50,4),new NPCDrop(557,50,5)};
        List<Item> out=Native950NpcDrops.roll(table,1,false,Native950NpcDropsTest::metadata,LOW);
        assertEquals(4,out.size());assertEquals(526,out.get(0).getId());assertEquals(995,out.get(1).getId());
        assertEquals(3,out.get(1).getAmount());assertEquals(4,out.get(3).getAmount());
    }
    @Test public void failedItemValidationAndMalformedAmountsCannotReachAwards() {
        NPCDrop[] table={new NPCDrop(1,100,1),new NPCDrop(995,100,-1,2),
                new NPCDrop(995,100,4,3),new NPCDrop(995,Double.NaN,1),new NPCDrop(995,100,Integer.MAX_VALUE)};
        assertTrue(Native950NpcDrops.roll(table,1,false,id->id==1?null:metadata(id),LOW).isEmpty());
    }
    @Test public void globalDoubleDropsReusesAuthoredQuantityButCannotOverflow() {
        List<Item> out=Native950NpcDrops.roll(new NPCDrop[]{new NPCDrop(995,100,12,20),
                new NPCDrop(995,100,Integer.MAX_VALUE/2+1)},1,true,Native950NpcDropsTest::metadata,LOW);
        assertEquals(1,out.size());assertEquals(24,out.get(0).getAmount());
    }
    @Test public void zeroQuantityTableResultDoesNotCreateAnItem() {
        assertTrue(Native950NpcDrops.roll(new NPCDrop[]{new NPCDrop(995,100,0,1)},1,false,
                Native950NpcDropsTest::metadata,LOW).isEmpty());
    }
    @Test public void metadataReadsCurrentStackFlagAndRejectsUnsafeOrMissingRows() {
        Map<Integer,byte[]> files=new HashMap<Integer,byte[]>();
        files.put(995,raw("Coins",11)); files.put(526,raw("Bones"));
        assertTrue(Native950NpcDrops.resolveMetadata(995,id->true,files::get).stackable);
        assertFalse(Native950NpcDrops.resolveMetadata(526,id->true,files::get).stackable);
        assertNull(Native950NpcDrops.resolveMetadata(526,id->false,files::get));
        assertNull(Native950NpcDrops.resolveMetadata(123,id->true,files::get));
        assertNull(Native950NpcDrops.resolveMetadata(65535,id->true,files::get));
    }
    @Test public void notedRowsRequireReciprocalCurrentBaseAndRetainTheirAuthoredId() {
        Map<Integer,byte[]> files=notedFiles();
        Native950NpcDrops.ItemMetadata note=Native950NpcDrops.resolveMetadata(527,id->true,files::get);
        assertNotNull(note);assertEquals(527,note.id);assertEquals(526,note.baseId);
        assertEquals("Bones",note.name);assertTrue(note.noted);assertTrue(note.stackable);
        assertNull(Native950NpcDrops.resolveMetadata(527,id->id!=526,files::get));
        files.put(526,raw("Bones",97,2,16));
        assertNull(Native950NpcDrops.resolveMetadata(527,id->true,files::get));
    }
    @Test public void missingTemplateAndRecursiveNoteBaseAreRefused() {
        Map<Integer,byte[]> files=notedFiles();files.remove(799);
        assertNull(Native950NpcDrops.resolveMetadata(527,id->true,files::get));
        files=notedFiles();files.put(526,raw("Bones",97,2,15,98,3,31));
        assertNull(Native950NpcDrops.resolveMetadata(527,id->true,files::get));
    }
    @Test public void existingCoinPinAdmitsCurrencyWithoutRelaxingOtherChangedIdentities() {
        byte[] coins=hex("0700030402c60607dc0500b8be0003e40002bf0003e50003c00003e60004c10003e70005c20003e80019c30003e90064c40003ea00fac50003eb03e8c60003ec27102341646420746f20706f7563680024416464205820746f20706f756368000b090109b402436f696e7300f9030000057500000001000002190000105b0000151f000016f890002d00");
        Native950NpcDrops.ItemMetadata metadata=Native950NpcDrops.resolveMetadata(995,id->false,id->coins);
        assertNotNull(metadata);assertEquals("Coins",metadata.name);assertTrue(metadata.stackable);assertFalse(metadata.noted);
        assertNull(Native950NpcDrops.resolveMetadata(994,id->false,id->coins));
        byte[] changed=coins.clone();changed[1]^=1;
        assertNull(Native950NpcDrops.resolveMetadata(995,id->false,id->changed));
        assertFalse(Native950NpcDrops.pinnedCoins(995,null));
    }
    @Test public void malformedOrLentBoundAndShardTemplatesAreRefused() {
        Map<Integer,byte[]> files=new HashMap<Integer,byte[]>();
        files.put(1,new byte[]{(byte)255,0});assertNull(Native950NpcDrops.resolveMetadata(1,id->true,files::get));
        for (int opcode:new int[]{122,140,162}) {
            files.put(1,raw("Item",opcode,3,31));
            assertNull(Native950NpcDrops.resolveMetadata(1,id->true,files::get));
        }
    }
    private static Map<Integer,byte[]> notedFiles() {
        Map<Integer,byte[]> files=new HashMap<Integer,byte[]>();
        files.put(526,raw("Bones",97,2,15)); files.put(527,raw(null,97,2,14,98,3,31));
        files.put(799,raw("Bank note"));return files;
    }
    private static Native950NpcDrops.ItemMetadata metadata(int id) {
        return new Native950NpcDrops.ItemMetadata(id,"Fixture",id!=526,false,id);
    }
    private static byte[] hex(String text) {
        byte[] bytes=new byte[text.length()/2];for(int i=0;i<bytes.length;i++)bytes[i]=(byte)Integer.parseInt(text.substring(i*2,i*2+2),16);return bytes;
    }
    private static byte[] raw(String name,int... bytes) {
        ByteArrayOutputStream out=new ByteArrayOutputStream();
        if(name!=null){out.write(2);for(char c:name.toCharArray())out.write(c);out.write(0);}
        for(int b:bytes)out.write(b);out.write(0);return out.toByteArray();
    }
}
