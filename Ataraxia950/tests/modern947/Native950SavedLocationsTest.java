package com.rs.game.player.client;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import static org.junit.Assert.*;

public class Native950SavedLocationsTest {
    @Rule public TemporaryFolder temp=new TemporaryFolder();

    @Test public void sharedPlacesSurviveReloadAndAnOwnerCanReplaceTheirOwnName() throws Exception {
        Path file=temp.getRoot().toPath().resolve("locations.bin");
        String old=System.getProperty("ataraxia950.locationsFile");
        System.setProperty("ataraxia950.locationsFile",file.toString());
        try {
            Native950SavedLocations.save("jaxa","Max Guild",2276,3315,1);
            Native950SavedLocations.save("nooby","Max Guild",3000,3001,0);
            Native950SavedLocations.save("jaxa","Max Guild",2277,3316,1);
            List<Native950SavedLocations.Place> places=Native950SavedLocations.list();
            assertEquals(2,places.size());
            assertEquals("Max Guild (nooby) - 3000 3001 0",places.get(0).line());
            assertEquals("Max Guild (jaxa) - 2277 3316 1",places.get(1).line());
            assertTrue(Files.size(file)>32);
        } finally { if(old==null)System.clearProperty("ataraxia950.locationsFile");else System.setProperty("ataraxia950.locationsFile",old); }
    }

    @Test public void corruptStoreIsRejectedWithoutReplacingIt() throws Exception {
        Path file=temp.getRoot().toPath().resolve("locations.bin");
        String old=System.getProperty("ataraxia950.locationsFile");
        System.setProperty("ataraxia950.locationsFile",file.toString());
        try {
            Native950SavedLocations.save("jaxa","Max Guild",2276,3315,1);
            byte[] bytes=Files.readAllBytes(file);bytes[12]^=1;Files.write(file,bytes);
            try{Native950SavedLocations.save("nooby","Bank",3200,3200,0);fail("Corrupt store was overwritten");}
            catch(java.io.IOException expected){assertTrue(expected.getMessage().contains("checksum"));}
            assertArrayEquals(bytes,Files.readAllBytes(file));
        } finally { if(old==null)System.clearProperty("ataraxia950.locationsFile");else System.setProperty("ataraxia950.locationsFile",old); }
    }
}
