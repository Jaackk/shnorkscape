package com.rs.game.player.client;
import org.junit.Test;
import static org.junit.Assert.*;

public final class Native950MapAreasTest {
    @Test public void resolvesCurrentCacheLabelsForAllLodestones() {
        assertEquals(5197,Native950MapAreas.mappedSquares());
        for(Native950Lodestones.Destination destination:Native950Lodestones.destinations()) {
            int expected=474;
            if(destination.name.equals("City of Um"))expected=814;
            if(destination.name.equals("Wendlewick"))expected=857;
            if(destination.name.equals("Anachronia"))expected=762;
            if(destination.name.equals("Karamja"))expected=674;
            if(destination.name.equals("Ashdale"))expected=4;
            assertEquals(destination.name,expected,Native950MapAreas.areaTypeFor(destination.arrival().getX(),destination.arrival().getY(),999));
        }
    }
    @Test public void aLoginAreaNeverOverridesAnotherMappedLocation() {
        assertEquals(814,Native950MapAreas.areaTypeFor(1084,1768,474));
        assertEquals(474,Native950MapAreas.areaTypeFor(3233,3221,814));
        assertEquals(762,Native950MapAreas.areaTypeFor(5431,2338,814));
    }
    @Test public void resolvesEightTileChunksRatherThanBroadRectangles() {
        // index23/group3/file6828: alternating run1(labelfff9e5),run7(label638fe6).
        assertEquals(682,Native950MapAreas.resolvedAreaType(2816,3392));
        assertEquals(682,Native950MapAreas.resolvedAreaType(2824,3392));
        assertEquals(474,Native950MapAreas.resolvedAreaType(2816,3400));
        assertEquals(474,Native950MapAreas.resolvedAreaType(2878,3442));
    }
    @Test public void invalidCoordinatesNeverAliasARealMapSquare() {
        for(int[] p:new int[][]{{-1,3221},{8192,3221},{3233,-1},{3233,16384}}) {
            assertEquals(-1,Native950MapAreas.resolvedAreaType(p[0],p[1]));
            assertEquals(77,Native950MapAreas.areaTypeFor(p[0],p[1],77));
        }
    }
    @Test public void decodesTheClientImplicitFinalRunAndExplicitRuns() {
        int[] uniform=Native950MapAreas.decodeLabels(new byte[]{0x37,(byte)0xf4,0x4a});
        for(int label:uniform)assertEquals(0x37f44a,label);
        int[] mixed=Native950MapAreas.decodeLabels(new byte[]{0,0,5,8,0,0,6});
        assertEquals(5,mixed[7]);assertEquals(6,mixed[8]);assertEquals(6,mixed[63]);
    }
    @Test public void refusesMalformedCacheRuns() {
        for(byte[] raw:new byte[][]{{},{1},{1,2},{0,0,1,0},{0,0,1,65},{0,0,1,1},{0,0,1,64,0,0,2}}) {
            try{Native950MapAreas.decodeLabels(raw);fail("Accepted malformed run");}
            catch(IllegalArgumentException expected) { }
        }
    }
}
