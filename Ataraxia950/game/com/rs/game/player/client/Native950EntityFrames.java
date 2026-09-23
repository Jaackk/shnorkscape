package com.rs.game.player.client;

import com.rs.game.WorldTile;
import com.rs.game.player.Player;
import com.rs.network.protocol.modern950.Native950NpcInfo;
import com.rs.network.protocol.modern950.Native950Packets;
import com.rs.network.protocol.modern950.Native950PlayerInfo;
import com.rs.network.protocol.modern950.Native950PlayerInfo.Actor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * The live P6 frame encoder: one {@link Native950Viewport} per session over
 * {@code Native950PlayerInfo}, and one {@link Native950NpcViewport} per session over
 * {@code Native950NpcInfo}. It replaces the literal single-entity writers
 * ({@code Native950Packets.initialSinglePlayerScene}, {@code singlePlayerAppearance},
 * {@code singlePlayerIdle}, {@code singlePlayerWalkStep}, {@code staticNpcAdd},
 * {@code singleNpcRetain}, {@code singleNpcRemove}), which are deprecated and now exist only
 * as wire fixtures for the tests that pinned those bytes.
 *
 * <p><b>One world snapshot per tick.</b> {@link #beginFrames(List)} converts every live
 * character into exactly one {@link Actor} and publishes them in an {@code Actor[2048]} table
 * indexed by player index; every viewer's frame in that tick reads the same table. That is
 * what makes the world-phase order observable on the wire: a viewer encoded from a table built
 * before another character moved would describe a world that never existed.
 *
 * <p><b>Visibility.</b> {@code Native950PlayerInfo.DISTANCE_ONLY} (same plane, Chebyshev 24) is
 * installed. The 910 test also requires {@code getMapRegionsIds().contains(getRegionId())};
 * that half cannot be expressed through the {@code Visibility} interface, which sees only
 * {@link Actor} snapshots, and is left to M4 together with the region-aware scene work. Inside a
 * 104-tile scene the radius is the binding constraint, so the looser policy adds no player the
 * client has nowhere to draw.
 *
 * <p><b>Masks.</b> M4 installs {@link Native950EntityMasks} as the player mask source, so the
 * CONFIRMED blocks (animation 0x40, face entity 0x20, face angle 0x80, force talk 0x10000,
 * hits 0x8) are read from real 910 entity state alongside the appearance block. Every block is
 * still composed through {@code Native950PlayerMasks}, which owns the widths, the header
 * extension markers and the client's consumption order, and which refuses every CANDIDATE
 * builder; nothing here can reach the wire around it. The masks are read once per tick into
 * the shared snapshot. Hit values are captured once and their standard involved/observer
 * presentation is selected using the recipient's player index at encoding.
 */
public final class Native950EntityFrames implements Native950Frames {
    private final Map<Integer, Native950Viewport> viewports = new HashMap<Integer, Native950Viewport>();
    /** Slots freed since the last tick; their removal record is written by the next frame. */
    private final List<Integer> pendingForget = new ArrayList<Integer>();
    /** Slots whose removal record went out last tick; their caches are dropped this tick. */
    private final List<Integer> forgetAfterThisTick = new ArrayList<Integer>();
    /**
     * One never-reused serial per character object, so a reused player index carries a
     * different {@link Actor#identity} for its new occupant. {@code EntityList} hands out the
     * lowest free index, and a login can take a freed one in the same inter-tick gap the
     * previous session's close ran in, which is well before any viewer has written the
     * removal record. The encoder turns the changed identity into that removal itself, so no
     * viewer is ever asked to describe a new character as the old one continuing to move.
     */
    private final Map<Player, Long> identities = new IdentityHashMap<Player, Long>();
    private long nextIdentity = 1;
    private Actor[] world = new Actor[Native950PlayerInfo.SLOTS];

    @Override public int playerCapacity() { return Native950PlayerInfo.MAX_INDEX; }

    @Override public int npcCapacity() { return Native950NpcInfo.MAX_LOCAL_NPCS; }

    @Override
    public List<Native950Packets.Packet> admit(Player viewer, Native950World.SceneConfig scene,
                                               List<Player> characters) {
        Native950Viewport viewport = new Native950Viewport(scene.playerIndex);
        viewports.put(Integer.valueOf(scene.playerIndex), viewport);
        Actor[] table = snapshot(characters);
        List<Native950Packets.Packet> burst = new ArrayList<Native950Packets.Packet>(2);
        burst.add(viewport.initialScene(table, scene.npcBits, scene.areaType, scene.hash1, scene.hash2));
        // The login burst ends in its own SERVER_TICK_END, so it needs the one PLAYER_INFO
        // that tick: the local character's appearance block, which the initial bit stream
        // does not carry. Sending it here instead of on the first world tick is what the
        // retired singlePlayerAppearance writer did, and it keeps the client from drawing
        // 600ms of default model.
        burst.add(viewport.frame(table));
        return Collections.unmodifiableList(burst);
    }

    @Override
    public void release(Player viewer) {
        viewports.remove(Integer.valueOf(viewer.getIndex()));
        // EntityList reuses the lowest free index, so every remaining viewer must drop the
        // cached appearance for the freed slot before anyone else takes it - but not yet. The
        // freed index is still in every other viewer's local list, and the removal record is
        // written from the actor cached there; forgetting it now would both strand the entity
        // on those clients and leave the encoder with a local index whose actor has vanished.
        // The forget is therefore deferred until after the tick in which the removal is emitted
        // (see beginFrames), and is skipped entirely once the index has a new occupant, whose
        // arrival the encoder already reports as a removal plus a later add. The per-slot
        // region hash is never dropped: it mirrors a client record that survives the removal.
        pendingForget.add(Integer.valueOf(viewer.getIndex()));
    }

    @Override
    public void beginFrames(List<Player> characters) {
        Actor[] table = snapshot(characters);
        for (Integer index : forgetAfterThisTick) {
            // A slot the world already handed to another character is NOT forgotten. Its new
            // occupant is live in this table, every viewer that still holds the old one will be
            // sent a removal from the changed occupancy identity, and dropping the viewer's
            // local entry here instead would leave the client holding a slot the encoder
            // believes is external - the next frame would then add it a second time.
            if (table[index.intValue()] != null) continue;
            for (Native950Viewport viewport : viewports.values())
                if (viewport.localIndex() != index.intValue()) viewport.forget(index.intValue());
        }
        forgetAfterThisTick.clear();
        // A slot released since the previous tick has its removal written by the frames below,
        // so its caches can be dropped at the start of the tick after that.
        forgetAfterThisTick.addAll(pendingForget);
        pendingForget.clear();
        world = table;
    }

    @Override
    public void encode(Frame frame) {
        Native950Viewport viewport = viewports.get(Integer.valueOf(frame.viewer.getIndex()));
        if (viewport == null)
            throw new IllegalStateException("No PLAYER_INFO viewport for native 947 player index "
                    + frame.viewer.getIndex());
        frame.channel.write(viewport.frame(world));
        // NPC_INFO after PLAYER_INFO: NPC offsets are read against the local actor's
        // post-movement tile, which the client only has once this tick's player frame is parsed.
        if (frame.npcView != null)
            frame.npcView.synchronize(frame.viewer, frame.channel, frame.npcBits, frame.sceneRebuilt);
    }

    @Override
    public Native950Viewport.State viewport(Player viewer) {
        Native950Viewport viewport = viewports.get(Integer.valueOf(viewer.getIndex()));
        return viewport == null ? null : viewport.snapshot();
    }

    /** One immutable {@link Actor} per live character, indexed by player index. */
    private Actor[] snapshot(List<Player> characters) {
        Actor[] table = new Actor[Native950PlayerInfo.SLOTS];
        Map<Player, Long> kept = new IdentityHashMap<Player, Long>();
        for (Player character : characters) {
            if (character == null) continue;
            int index = character.getIndex();
            if (index < Native950PlayerInfo.MIN_INDEX || index > Native950PlayerInfo.MAX_INDEX) continue;
            table[index] = actor(character, identityOf(character));
            kept.put(character, identities.get(character));
        }
        // Keeps the map bounded by the characters still in the world. A character that comes
        // back is a new Player object, so it takes a fresh serial - exactly the right answer.
        identities.clear();
        identities.putAll(kept);
        return table;
    }

    /** A never-reused serial per character object; see {@link #identities}. */
    private long identityOf(Player character) {
        Long identity = identities.get(character);
        if (identity == null) {
            identity = Long.valueOf(nextIdentity++);
            identities.put(character, identity);
        }
        return identity.longValue();
    }

    /**
     * The 910 {@code LocalPlayerUpdate} inputs for one player: its post-movement tile, the tile
     * it stood on before this tick ({@code getLastWorldTile}), whether it moved
     * ({@code hasTeleported() || getNextWalkDirection() != -1}), its speed index
     * ({@code getMovementType()}, or the teleport index when it teleported), whether it is
     * visible at all ({@code isRunning() && !hasFinished()}), and its appearance body with the
     * MD5 the per-viewer cache compares.
     */
    private static Actor actor(Player character, long identity) {
        Actor.Builder builder = Actor.builder(character.getIndex(),
                character.getX(), character.getY(), character.getPlane()).identity(identity);
        WorldTile previous = character.getLastWorldTile();
        if (previous != null) builder.previous(previous.getX(), previous.getY(), previous.getPlane());
        builder.moved(character.hasTeleported() || character.getNextWalkDirection() != -1);
        builder.movementType(character.hasTeleported()
                ? Native950PlayerInfo.MOVEMENT_TELEPORT : character.getMovementType());
        builder.visible(character.isRunning() && !character.hasFinished());
        byte[] body = character.getAppearence().getAppeareanceData();
        byte[] hash = character.getAppearence().getMD5AppeareanceDataHash();
        // A withheld body (an item the verified decoder could not resolve) is counted in the
        // appearance serializer and simply not offered here; the player is still drawn, with
        // whatever appearance the client already holds.
        if (body != null && hash != null && body.length >= 1 && body.length <= 255)
            builder.appearance(body, hash);
        // M4: the non-appearance masks, read once here from the character's post-movement
        // state and shared by every viewer's frame in this tick. Null when no CONFIRMED mask
        // applies, which is the overwhelmingly common case and leaves the frame exactly as P6
        // encoded it. Locomotion needs no animation mask: the Base Animation Set in the
        // appearance block supplies stand, walk and run.
        builder.masks(Native950EntityMasks.playerSource(character));
        long forceGeneration = character.getNative950ForceMaskGeneration();
        Native950ForceMovement.Plan forcePlan = character.getNextNative950ForceMovement();
        if (forceGeneration != 0 && forcePlan != null) {
            WorldTile endpoint = forcePlan.finalTile();
            builder.forceMovement(forceGeneration, endpoint.getX(), endpoint.getY());
            Native950BugTest.event(character,"combat","force-frame-published","generation",forceGeneration,"x",endpoint.getX(),"y",endpoint.getY());
        }
        builder.forceMovementArrival(character.getNative950ForceArrivalGeneration());
        return builder.build();
    }

    /** The world table this tick's frames are being built from; diagnostics only. */
    List<Integer> trackedIndices() {
        List<Integer> indices = new ArrayList<Integer>(viewports.keySet());
        Collections.sort(indices);
        return indices;
    }
}
