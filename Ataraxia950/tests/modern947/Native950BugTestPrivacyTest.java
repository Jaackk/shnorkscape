package com.rs.game.player.client;

import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

public class Native950BugTestPrivacyTest {
    @Test public void unknownInputIsCountedWithoutKeepingPayloadAndOnlyFirstOccurrenceNeedsAnEvent(){
        Native950BugTest.UnknownFrameCounts counts=new Native950BugTest.UnknownFrameCounts();
        assertTrue(counts.record(25,7));
        for(int i=1;i<1000;i++)assertFalse(counts.record(25,7));
        assertTrue(counts.record(99,16));
        assertFalse(counts.record(-1,100));assertFalse(counts.record(256,100));
        List<long[]> summary=counts.drain();assertEquals(2,summary.size());
        assertArrayEquals(new long[]{25,1000,7000},summary.get(0));
        assertArrayEquals(new long[]{99,1,16},summary.get(1));
        assertTrue(counts.drain().isEmpty());
        assertTrue(counts.record(25,7));
        assertArrayEquals(new long[]{25,1,7},counts.drain().get(0));
    }
    @Test public void disabledDiagnosticsDoNotReadAnUnknownPayload(){
        Native950BugTest.unhandledFrame(null,25,null);
    }
}
