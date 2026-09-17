package com.rs.utils.data.parsers.npcs;

import com.rs.utils.data.parsers.npcs.pojos.NPCDrop;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

public final class NPCDropTableRollsTest {
    @Test public void allGuaranteedRowsSurviveTheThreeRandomDropCap() {
        NPCDrop[] table = new NPCDrop[9];
        for (int i = 0; i < table.length; i++) table[i] = new NPCDrop(100+i, i<4?100:50, 1);
        Rolls rolls = new Rolls(25);
        List<NPCDrop> out = roll(table,1,100,rolls);
        assertEquals(7,out.size());
        for (int i=0;i<4;i++) assertSame(table[i],out.get(i));
        assertSame(table[8],out.get(4)); assertSame(table[7],out.get(5)); assertSame(table[6],out.get(6));
        assertEquals(5,rolls.samples); assertEquals(1,rolls.shuffles);
    }
    @Test public void rarityMultiplierAppliesOnlyBelowThirtyPercent() {
        NPCDrop[] table={new NPCDrop(1,20,1),new NPCDrop(2,30,1),new NPCDrop(3,50,1)};
        List<NPCDrop> out=roll(table,2,100,new Rolls(35));
        assertEquals(Arrays.asList(table[2],table[0]),out);
    }
    @Test public void legacyZeroAndHundredEndpointsRemainExcluded() {
        NPCDrop[] table={new NPCDrop(1,99,1),new NPCDrop(2,100,1)};
        assertEquals(Collections.singletonList(table[1]),roll(table,2,100,new Rolls(0)));
        assertEquals(Collections.singletonList(table[1]),roll(table,2,100,new Rolls(100)));
        assertEquals(2,roll(table,2,100,new Rolls(99)) .size());
        assertEquals(Collections.singletonList(table[1]),roll(table,1,100,new Rolls(100.5)));
    }
    @Test public void filtersRunBeforeBothGuaranteedAndRandomAwards() {
        NPCDrop[] table={null,new NPCDrop(1,100,1),new NPCDrop(2,20,1),new NPCDrop(3,100,1)};
        List<NPCDrop> out=new ArrayList<NPCDrop>(); Rolls random=new Rolls(1);
        NPCDropTableRolls.roll(table,1,100,d->d.getItemId()==3,out::add,random);
        assertEquals(Collections.singletonList(table[3]),out);assertEquals(0,random.samples);
    }
    @Test public void contractRangeIsPassedUnchangedToExistingRandomHelper() {
        Rolls random=new Rolls(94);roll(new NPCDrop[]{new NPCDrop(1,99,1)},1,95,random);
        assertEquals(95,random.maximum,0);
    }
    @Test public void amountIncludesBothAuthoredBounds() {
        NPCDrop drop=new NPCDrop(995,50,3,8);Rolls random=new Rolls(1);
        assertEquals(3,NPCDropTableRolls.amount(drop,random));assertEquals(5,random.quantityMaximum);
        random.top=true;assertEquals(8,NPCDropTableRolls.amount(drop,random));
        assertEquals(7,NPCDropTableRolls.amount(new NPCDrop(1,100,7),random));
    }
    @Test public void absentTablesHaveNoAwardsOrRandomSideEffects() {
        Rolls random=new Rolls(1);assertTrue(roll(null,1,100,random).isEmpty());
        assertEquals(0,random.samples);assertEquals(0,random.shuffles);
    }
    private static List<NPCDrop> roll(NPCDrop[] table,double multiplier,double maximum,Rolls random) {
        List<NPCDrop> out=new ArrayList<NPCDrop>();
        NPCDropTableRolls.roll(table,multiplier,maximum,d->true,out::add,random);return out;
    }
    static final class Rolls implements NPCDropTableRolls.Rolls {
        final double result; int samples,shuffles,quantityMaximum; double maximum; boolean top;
        Rolls(double result){this.result=result;}
        public double percentage(double maximum){samples++;this.maximum=maximum;return result;}
        public void shuffle(List<NPCDrop> rows){shuffles++;Collections.reverse(rows);}
        public int inclusive(int maximum){quantityMaximum=maximum;return top?maximum:0;}
    }
}
