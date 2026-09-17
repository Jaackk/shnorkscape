package com.rs.game.player.client;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.IntConsumer;

/**
 * Session-owned regional music. Updated from the final tile after movement, never
 * from delayed world tasks. Audio volume and mute remain client preferences.
 */
public final class Native950RegionMusic {
    private final Function<Integer, Native950RegionMusicCatalog.Track> catalog;
    private final IntConsumer play;
    private boolean started;
    private boolean closed;
    private int region = -1;
    private int archive = -1;
    private int replayRequested = -1;
    private Native950RegionMusicCatalog.Track selected;
    private long changes;

    public Native950RegionMusic(Function<Integer, Native950RegionMusicCatalog.Track> catalog,
                                IntConsumer play) {
        this.catalog = Objects.requireNonNull(catalog, "catalog");
        this.play = Objects.requireNonNull(play, "play");
    }

    /** Called only once the game transport and bootstrap are ready. */
    public void start(int regionId) {
        if (closed || started) return;
        started = true;
        update(regionId);
    }

    public void update(int regionId) {
        if (!started || closed || (region == regionId && replayRequested == -1)) return;
        Native950RegionMusicCatalog.Track next = region == regionId ? selected : catalog.apply(regionId);
        int nextArchive = next == null ? -1 : next.archiveId;
        // The first selection also clears any music left over from the lobby.
        // Completion/unmute requests are coalesced until AFTER movement, so an
        // event from the previous region cannot restart its track after teleport.
        if (region == -1 || nextArchive != archive || (nextArchive >= 0 && replayRequested == nextArchive)) {
            play.accept(nextArchive);
            changes++;
        }
        replayRequested = -1;
        region = regionId;
        archive = nextArchive;
        selected = next;
    }

    /** Verified client59 notification, for natural completion or native Audio unmute. */
    public void requestReplay(int archiveId) {
        if (started && !closed && archiveId >= 0 && archiveId == archive)
            replayRequested = archiveId;
    }

    /** Retire synchronously; there are no scheduled callbacks to outlive logout. */
    public void close() {
        closed = true;
        replayRequested = -1;
    }

    public int regionId() { return region; }
    public int archiveId() { return archive; }
    public int trackId() { return selected == null ? -1 : selected.trackId; }
    public String trackName() { return selected == null ? "" : selected.name; }
    public long changes() { return changes; }
}
