package modern947;

import com.rs.game.player.client.Native950RegionMusic;
import com.rs.game.player.client.Native950RegionMusicCatalog.Track;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public final class Native950RegionMusicTest {
    private static Map<Integer, Track> tracks() {
        Map<Integer, Track> result = new HashMap<Integer, Track>();
        result.put(1, new Track("Harmony", 58, 36067));
        result.put(2, new Track("Harmony", 58, 36067));
        result.put(3, new Track("Autumn Voyage", 17, 37990));
        return result;
    }

    @Test public void onlyReadySessionsPlayAndNearbyRegionsDoNotRestartTheSameSong() {
        List<Integer> writes = new ArrayList<Integer>();
        Native950RegionMusic music = new Native950RegionMusic(tracks()::get, writes::add);
        music.update(3);
        assertTrue(writes.isEmpty());
        music.start(1);
        music.start(3); // duplicate ready cannot replace the current location
        for (int i = 0; i < 1000; i++) music.update(1);
        music.update(2);
        assertEquals(Arrays.asList(36067), writes);
        assertEquals(2, music.regionId());
        assertEquals("Harmony", music.trackName());
        music.update(3);
        assertEquals(Arrays.asList(36067, 37990), writes);
        assertEquals(2L, music.changes());
    }

    @Test public void unmappedRegionClearsOldMusicOnceAndReentryPlaysAgain() {
        List<Integer> writes = new ArrayList<Integer>();
        Native950RegionMusic music = new Native950RegionMusic(tracks()::get, writes::add);
        music.start(1);
        music.update(90);
        music.update(90);
        music.update(91);
        assertEquals(Arrays.asList(36067, -1), writes);
        assertEquals(-1, music.archiveId());
        assertEquals(-1, music.trackId());
        music.update(1);
        assertEquals(Arrays.asList(36067, -1, 36067), writes);
    }

    @Test public void unknownLoginClearsLobbyAndDisconnectedOwnerCannotWriteToNewSession() {
        List<Integer> writes = new ArrayList<Integer>();
        Native950RegionMusic old = new Native950RegionMusic(tracks()::get, writes::add);
        old.start(90);
        old.close();
        old.update(1);
        old.start(1);
        assertEquals(Arrays.asList(-1), writes);
        Native950RegionMusic next = new Native950RegionMusic(tracks()::get, writes::add);
        next.start(1);
        old.update(3);
        assertEquals(Arrays.asList(-1, 36067), writes);
    }

    @Test public void playersSelectTheirOwnMusicIndependently() {
        List<Integer> first = new ArrayList<Integer>();
        List<Integer> second = new ArrayList<Integer>();
        Map<Integer, Track> catalog = tracks();
        Native950RegionMusic a = new Native950RegionMusic(catalog::get, first::add);
        Native950RegionMusic b = new Native950RegionMusic(catalog::get, second::add);
        a.start(1);
        b.start(3);
        a.update(3);
        b.close();
        a.update(1);
        assertEquals(Arrays.asList(36067, 37990, 36067), first);
        assertEquals(Arrays.asList(37990), second);
    }

    @Test public void completionAndUnmuteReplayOnlyTheCurrentSongOncePerTick() {
        List<Integer> writes = new ArrayList<Integer>();
        Native950RegionMusic music = new Native950RegionMusic(tracks()::get, writes::add);
        music.requestReplay(36067); // not ready
        music.start(1);
        music.requestReplay(-1);
        music.requestReplay(37990); // not the current song
        music.update(1);
        assertEquals(Arrays.asList(36067), writes);
        for (int i = 0; i < 16; i++) music.requestReplay(36067);
        assertEquals("notification waits for authoritative movement", 1, writes.size());
        music.update(1);
        music.update(1);
        assertEquals(Arrays.asList(36067, 36067), writes);
        music.requestReplay(36067);
        music.close();
        music.update(1);
        music.requestReplay(36067);
        assertEquals(2, writes.size());
    }

    @Test public void queuedEndFromOldRegionCannotOverrideFinalTeleportDestination() {
        List<Integer> writes = new ArrayList<Integer>();
        Native950RegionMusic music = new Native950RegionMusic(tracks()::get, writes::add);
        music.start(1);
        music.requestReplay(36067);
        music.update(3); // post-movement final destination wins
        music.update(3);
        music.requestReplay(36067); // old stop/completion arrives a tick late
        music.update(3);
        assertEquals(Arrays.asList(36067, 37990), writes);
        music.requestReplay(37990);
        music.update(90); // an unmapped destination must still stop music
        assertEquals(Arrays.asList(36067, 37990, -1), writes);
        music.requestReplay(37990);
        music.update(90);
        assertEquals(3, writes.size());
    }
}
