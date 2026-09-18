package com.rs.game.player.client;

import com.rs.game.WorldTile;
import org.junit.Test;
import static org.junit.Assert.*;

public final class Native950DiagnosticSpawnsTest {
    private static int[][] models(int count) {
        int[][] models = new int[count][];
        for (int i = 0; i < count; i++) models[i] = new int[]{42};
        return models;
    }
    @Test public void automaticPlacementChoosesVisibleSceneryOrActualWallShape() {
        assertEquals(10, Native950DiagnosticSpawns.selectShape(new byte[]{0,10}, models(2), -1));
        assertEquals(0, Native950DiagnosticSpawns.selectShape(new byte[]{0}, models(1), -1));
        assertEquals(22, Native950DiagnosticSpawns.selectShape(new byte[]{22}, models(1), -1));
        assertEquals(4, Native950DiagnosticSpawns.selectShape(new byte[]{4}, models(1), -1));
    }
    @Test public void explicitShapesRequireTheRequestedCacheModelShape() {
        assertEquals(10, Native950DiagnosticSpawns.selectShape(new byte[]{0,10}, models(2), 10));
        assertEquals(0, Native950DiagnosticSpawns.selectShape(new byte[]{0,10}, models(2), 0));
        for (int type : new int[]{-2,23,100,10}) {
            try { Native950DiagnosticSpawns.selectShape(new byte[]{0}, models(1), type); fail(); }
            catch (IllegalArgumentException expected) { }
        }
    }
    @Test public void missingEmptyAndInternalModelsCannotClaimSuccessfulVisibleSpawns() {
        for (byte[] shapes : new byte[][]{null,new byte[0],new byte[]{23},new byte[]{24}}) {
            try { Native950DiagnosticSpawns.selectShape(shapes,models(shapes == null ? 0 : shapes.length),-1); fail(); }
            catch (IllegalArgumentException expected) { }
        }
        try { Native950DiagnosticSpawns.selectShape(new byte[]{10},new int[][]{new int[0]},-1); fail(); }
        catch (IllegalArgumentException expected) { }
        try { Native950DiagnosticSpawns.selectShape(new byte[]{10},new int[0][],-1); fail(); }
        catch (IllegalArgumentException expected) { }
    }
    @Test public void footprintIsCheckedWithoutMovingOrClampingTheRequestedTile() {
        WorldTile tile = new WorldTile(16382, 16381, 2);
        assertTrue(Native950DiagnosticSpawns.fits(tile,2,3));
        assertFalse(Native950DiagnosticSpawns.fits(tile,3,2));
        assertFalse(Native950DiagnosticSpawns.fits(tile,2,4));
        assertFalse(Native950DiagnosticSpawns.fits(tile,0,1));
        assertFalse(Native950DiagnosticSpawns.fits(new WorldTile(3200,3200,0),65,1));
        assertEquals(16382,tile.getX()); assertEquals(16381,tile.getY()); assertEquals(2,tile.getPlane());
    }
    @Test public void unavailableCharacterDoesNotMutateWorldOrOpenTheWorldSingleton() {
        assertTrue(Native950DiagnosticSpawns.spawnNpc(null,42).contains("stand normally"));
        assertTrue(Native950DiagnosticSpawns.spawnObject(null,1).contains("stand normally"));
    }
    @Test public void trainingDummiesUseAHardPerOwnerCapWithoutAcceptingBadCounts() {
        assertEquals(1,Native950DiagnosticSpawns.trainingDummySpawnCount(1,0));
        assertEquals(5,Native950DiagnosticSpawns.trainingDummySpawnCount(5,0));
        assertEquals(2,Native950DiagnosticSpawns.trainingDummySpawnCount(5,3));
        assertEquals(0,Native950DiagnosticSpawns.trainingDummySpawnCount(1,5));
        assertEquals(-1,Native950DiagnosticSpawns.trainingDummySpawnCount(0,0));
        assertEquals(-1,Native950DiagnosticSpawns.trainingDummySpawnCount(6,0));
        assertEquals(-1,Native950DiagnosticSpawns.trainingDummySpawnCount(1,-1));
    }
}
