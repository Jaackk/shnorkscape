package com.rs.game.player.client;
import org.junit.Test;
import static org.junit.Assert.*;
import java.util.*;
public class Native950DeveloperSearchTest {
    private Native950EquipmentCatalogue.Entry entry(int id,String name){return new Native950EquipmentCatalogue.Entry(id,0,0,-1,1000,0,name,"");}
    @Test public void globalMatchingIsIndependentOfCuratedMembershipAndCase(){
        List<Native950DeveloperSearch.Row> universe=Arrays.asList(new Native950DeveloperSearch.Row(entry(10,"Obscure test tool"),""),new Native950DeveloperSearch.Row(entry(11,"Ordinary thing"),""));
        assertEquals(10,Native950DeveloperSearch.find(universe,"TeSt TO").get(0).id);
        assertEquals(11,Native950DeveloperSearch.find(universe,"id:11").get(0).id);
        assertTrue(Native950DeveloperSearch.find(universe,"").isEmpty());assertTrue(Native950DeveloperSearch.find(universe,"missing").isEmpty());
    }
    @Test public void queryChangesNeverReuseAnOldSourceSlotAndCancelReturnsToCurated(){
        Native950EquipmentCatalogue cat=new Native950EquipmentCatalogue(Collections.singletonList(entry(1,"Curated")));
        Native950DeveloperSearch.Session s=new Native950DeveloperSearch.Session(cat);
        s.query(Collections.singletonList(entry(10,"Tool")));assertEquals(10,s.claim(1,1).id);
        s.query(Collections.singletonList(entry(11,"Different")));assertNull(s.claim(1,1));assertEquals(11,s.claim(2,1).id);
        s.query(Collections.singletonList(entry(10,"Tool")));assertEquals(10,s.claim(1,1).id);assertNull(s.claim(2,1));
        s.searching=false;assertEquals(1,s.claim(0,1).id);assertNull(s.claim(1,1));assertEquals(1,cat.entries.size());
    }
    @Test public void sessionCapacityAndResultLimitAreBoundedWithoutReassigningSlots(){
        Native950EquipmentCatalogue cat=new Native950EquipmentCatalogue(Collections.singletonList(entry(1,"Curated")));
        Native950DeveloperSearch.Session s=new Native950DeveloperSearch.Session(cat);
        for(int start=2;start<2100;start+=100){List<Native950EquipmentCatalogue.Entry> rows=new ArrayList<>();for(int i=start;i<start+101;i++)rows.add(entry(i,"Item"));s.query(rows);assertTrue(s.limited);}
        assertTrue(s.full);assertEquals(1820,s.slots.size());assertEquals(2,s.slots.get(1).id);
    }
    @Test public void playersHaveIndependentVisibleResults(){
        Native950EquipmentCatalogue cat=new Native950EquipmentCatalogue(Collections.singletonList(entry(1,"Curated")));
        Native950DeveloperSearch.Session a=new Native950DeveloperSearch.Session(cat),b=new Native950DeveloperSearch.Session(cat);
        a.query(Collections.singletonList(entry(10,"Tool")));assertFalse(b.searching);assertEquals(1,b.slots.size());assertNull(b.claim(1,1));
    }
}
