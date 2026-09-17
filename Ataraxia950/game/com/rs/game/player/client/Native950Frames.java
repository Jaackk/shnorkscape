package com.rs.game.player.client;

import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import com.rs.network.protocol.modern950.Native950Packets;
import io.netty.channel.Channel;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * P6 seam between the world-phase tick and the entity encoders.
 *
 * <p>{@code Native950World} owns admission, the movement ordering and the slot table; the
 * installed {@code Native950Frames} owns every bit that reaches the wire for
 * {@code PLAYER_INFO} (opcode 27) and {@code NPC_INFO} (opcode 12), including the
 * {@code REBUILD_NORMAL} login burst, whose initial player bit stream is part of the same
 * viewport state the encoder maintains (verified/PLAYER_INFO.md, "Initial-state byte
 * examples").
 *
 * <p>Exactly one implementation is live at a time - {@link Native950EntityFrames}, which
 * drives {@link Native950Viewport} and {@link Native950NpcViewport}. The interface exists so
 * the world can ask the encoder how many characters it can describe
 * ({@link #playerCapacity()}) instead of carrying a separate multiplayer gate that could
 * drift from what the wire actually supports, and so a probe can substitute a recording
 * encoder without the world growing test hooks.
 *
 * <p>Every method runs on the native world thread.
 */
public interface Native950Frames {

    /**
     * Maximum number of native characters this encoder can describe at once; the world hands
     * out no more player slots than this.
     */
    int playerCapacity();

    /** Maximum number of NPCs one viewer's {@code NPC_INFO} may hold. */
    int npcCapacity();

    /**
     * Registers a newly admitted viewer and returns the packets written to its channel before
     * its first tick - the {@code REBUILD_NORMAL} scene carrying the initial player bit stream.
     * {@code characters} is every native character already in the world, this one included.
     */
    List<Native950Packets.Packet> admit(Player viewer, Native950World.SceneConfig scene,
                                        List<Player> characters);

    /**
     * Releases every per-viewer cache for a session that has left the world, and tells the
     * remaining viewers to forget the freed index so a later occupant cannot inherit its
     * cached appearance hash. The per-slot region hash is kept: it mirrors a client-side
     * record that survives the removal, so the next occupant's add must write its delta
     * against it.
     */
    void release(Player viewer);

    /**
     * Called once per tick, after every entity has moved and before the first
     * {@link #encode(Frame)}, so one immutable snapshot of the world is shared by every
     * viewer's frame.
     */
    void beginFrames(List<Player> characters);

    /**
     * Writes this viewer's {@code PLAYER_INFO} and then its {@code NPC_INFO} for the current
     * tick. NPC offsets are relative to the local character's position after this tick's
     * movement, which is why the player frame is written first
     * (verified/NPC_INFO.md, "Positioning uses the local actor's most recent path position,
     * so the server sends its player movement update first").
     */
    void encode(Frame frame);

    /** Diagnostics for one viewer's player viewport, or null when it has none. */
    Native950Viewport.State viewport(Player viewer);

    /** One viewer's post-movement input for one tick. Immutable; valid only during {@link #encode}. */
    final class Frame {
        /** The character this frame is being built for. */
        public final Player viewer;
        /** The viewer's connection; the encoder writes, the session flushes. */
        public final Channel channel;
        /** Every native character in the world this tick, ordered by player index. */
        public final List<Player> players;
        /** Every native NPC in the world this tick, ordered by NPC index. */
        public final List<NPC> npcs;
        /** This viewer's NPC viewport and the banker the P5 whitelist knows; null for a probe with no content. */
        public final Native950NpcView npcView;
        /** True when a {@code REBUILD_NORMAL} was written to this channel earlier in this tick. */
        public final boolean sceneRebuilt;
        /** The scene header's NPC offset width; {@code NPC_INFO} offsets are signed at this width. */
        public final int npcBits;
        /** The viewer's tile before this tick's movement phase ran. */
        public final int preMoveX, preMoveY, preMovePlane;

        public Frame(Player viewer, Channel channel, List<Player> players, List<NPC> npcs,
                     Native950NpcView npcView, boolean sceneRebuilt, int npcBits,
                     int preMoveX, int preMoveY, int preMovePlane) {
            this.viewer = Objects.requireNonNull(viewer, "viewer");
            this.channel = Objects.requireNonNull(channel, "channel");
            this.players = Collections.unmodifiableList(Objects.requireNonNull(players, "players"));
            this.npcs = Collections.unmodifiableList(Objects.requireNonNull(npcs, "npcs"));
            this.npcView = npcView;
            this.sceneRebuilt = sceneRebuilt;
            this.npcBits = npcBits;
            this.preMoveX = preMoveX;
            this.preMoveY = preMoveY;
            this.preMovePlane = preMovePlane;
        }
    }
}
