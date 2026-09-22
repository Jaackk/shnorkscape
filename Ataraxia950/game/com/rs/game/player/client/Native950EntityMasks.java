package com.rs.game.player.client;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.ForceTalk;
import com.rs.game.Rectangle;
import com.rs.game.npc.NPC;
import com.rs.game.npc.Transformation;
import com.rs.game.player.Player;
import com.rs.network.protocol.modern950.Native950NpcMasks;
import com.rs.network.protocol.modern950.Native950PlayerInfo;
import com.rs.network.protocol.modern950.Native950PlayerMasks;

import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CodingErrorAction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Adapts queued 910 engine state to the independently derived 950 entity masks.
 * Called after movement and before resetMasks on the native world thread.
 * Player graphics and ordinary health bars require paired-cache identities; force movement
 * uses the same immutable plan as the server's 20 ms client-cycle scheduler adapter.
 * Nontransparent legacy colour overlays, custom graphics flags and custom bar classes remain
 * counted refusals. Ordinary melee hitmarks use native life points and per-viewer standard styles.
 * The typed protocol writers own byte transforms, mask bits and consumption order.
 */
public final class Native950EntityMasks {

    /** 910 {@code Entity}: -2 means "no face-entity request this tick". */
    public static final int NO_FACE_ENTITY = -2;
    /** 910 {@code Entity}: -1 means "clear the current face target". */
    public static final int CLEAR_FACE_ENTITY = -1;
    /** 910 {@code Entity.getClientIndex()} offsets a player index by this; an NPC index is raw. */
    public static final int PLAYER_CLIENT_INDEX_BASE = 32768;
    /**
     * The force-talk flags byte (PLAYER_INFO_MASKS.md mask 0x10000). 0 is overhead text only;
     * bit 0 would additionally echo the text into the chatbox as message type 2, which is M5's
     * public-chat path, not this one.
     */
    public static final int FORCE_TALK_OVERHEAD_ONLY = 0;
    /** Both mask encoders take the animation delay as one byte. */
    public static final int MAX_ANIMATION_SPEED = 255;
    /** The animation block has exactly four sequence slots. */
    public static final int ANIMATION_SLOTS = 4;

    private static final AtomicLong PLAYER_BLOCKS = new AtomicLong();
    private static final AtomicLong NPC_BLOCKS = new AtomicLong();
    private static final AtomicLong REFUSALS = new AtomicLong();

    private Native950EntityMasks() { }

    // ------------------------------------------------------------------ player

    /**
     * The mask source for one character this tick, or null when no CONFIRMED mask applies.
     *
     * <p>{@code Native950PlayerInfo} requires that a declared source always produce at least
     * one confirmed mask, so the emptiness decision is made here, once, and the returned
     * source rebuilds common masks from the frozen world phase. Hit values and source indexes
     * are copied once; each recipient selects involved/observer presentation from that snapshot.
     * No viewer reads mutable Hit instances or changes another viewer's damage value.
     */
    public static Native950PlayerInfo.MaskSource playerSource(final Player character) {
        if(character==null)return null;
        final Native950Hits.Snapshot hits=Native950Hits.fromRunningCache(character);
        if(playerMasks(character,character.getIndex(),hits)==null)return null;
        return new Native950PlayerInfo.MaskSource() {
            @Override public Native950PlayerMasks.Builder masks() { return masks(character.getIndex()); }
            @Override public Native950PlayerMasks.Builder masks(int viewerIndex) {
                Native950PlayerMasks.Builder builder=playerMasks(character,viewerIndex,hits);
                if(builder==null)throw new IllegalStateException("Declared player masks became empty inside one frame");
                PLAYER_BLOCKS.incrementAndGet();
                return builder;
            }
        };
    }

    /**
     * The CONFIRMED player masks for one character, or null when none applies. Never sets the
     * appearance block: that one is owned by {@code Native950PlayerInfo}, which decides per
     * viewer whether the body still needs sending.
     */
    public static Native950PlayerMasks.Builder playerMasks(Player character) {
        return playerMasks(character,character==null?-1:character.getIndex(),Native950Hits.fromRunningCache(character));
    }

    private static Native950PlayerMasks.Builder playerMasks(Player character,int viewerIndex,Native950Hits.Snapshot snapshot) {
        if (character == null) return null;
        Native950PlayerMasks.Builder builder = Native950PlayerMasks.builder();
        boolean any = false;

        // 950 bit 3 - deliberate animation. 910 LocalPlayerUpdate.applyAnimationMask writes the
        // first four of Animation.getIds() and the speed byte; so does this.
        Animation animation = character.getNextAnimation();
        if (animation != null) {
            builder.animation(Native950PlayerMasks.Animation.of(
                    animationSlots(animation), animationSpeed(animation)));
            any = true;
        }

        // 950 bit 7 - face entity. 910 keeps one int: -2 nothing, -1 clear, otherwise
        // Entity.getClientIndex(), which offsets a player index by 32768 and leaves an NPC
        // index raw. The 947 block names the kind explicitly (1 = NPC, 2 = player, 0xFF clear).
        int faceEntity = character.getNextFaceEntity();
        if (faceEntity != NO_FACE_ENTITY) {
            Native950PlayerMasks.FaceEntity target = faceTarget(faceEntity);
            if (target == null) REFUSALS.incrementAndGet();
            else { builder.faceEntity(target); any = true; }
        }

        // 950 bit 8 - face angle. The 910 rule (LocalPlayerUpdate mask 0x10) is: a face
        // rectangle is set and nothing else is already turning the model. The "added" half of
        // the 910 condition is per viewer and cannot be expressed by a snapshot shared across
        // viewers, so it is left out: this encoder never sends an angle just because a viewer
        // is adding the player.
        if (facesTile(character)) {
            builder.faceAngle(character.getDirection() & 0x3FFF);
            any = true;
        }

        // 950 bit 9 - overhead text.
        ForceTalk talk = character.getNextForceTalk();
        if (talk != null) {
            String text = talk.getText();
            if (text == null || !isClientText(text)) REFUSALS.incrementAndGet();
            else {
                boolean publicChat = talk.isPublicChat();
                Player viewer = viewerIndex == character.getIndex() ? character
                        : com.rs.game.World.getPlayers().get(viewerIndex);
                // Keep a declared mask nonempty without leaking ignored/filtered text.
                boolean visible = !publicChat || Native950Social.receives(character, viewer);
                builder.forceTalk(visible ? text : "", publicChat && visible ? 1 : FORCE_TALK_OVERHEAD_ONLY);
                any = true;
            }
        }

        Native950PlayerEffects.Result effects = Native950PlayerEffects.append(character, builder);
        REFUSALS.addAndGet(effects.refusedCount());
        any |= effects.hasMasks();

        Native950ForceMovement.Plan movement = character.getNextNative950ForceMovement();
        if (movement != null) {
            try { builder.forceMovement(movement.mask(character)); any = true; }
            catch (IllegalArgumentException invalidFrameBase) { REFUSALS.incrementAndGet(); }
        }

        List<Native950PlayerMasks.Hit> hits = snapshot.playerHits(viewerIndex);
        REFUSALS.addAndGet(snapshot.refusals());
        Native950Hitbars.Result bars = Native950Hitbars.fromRunningCache(character);
        REFUSALS.addAndGet(bars.refusals());
        if (!hits.isEmpty() || !bars.playerBars().isEmpty()) {
            builder.hits(hits, bars.playerBars());
            any = true;
        }

        return any ? builder : null;
    }

    /**
     * 910 {@code LocalPlayerUpdate}: the face-angle block is written when a face rectangle is
     * set and no walk step, run step, force movement or face-entity request is already
     * deciding where the model looks.
     */
    private static boolean facesTile(Entity entity) {
        return entity.getNextFaceWorldTile() != null
                && entity.getNextWalkDirection() == -1
                && entity.getNextRunDirection() == -1
                && entity.getNextForceMovement() == null
                && entity.getNextFaceEntity() < 0;
    }

    /** Null when the 910 value names neither a player nor an NPC the 947 block can address. */
    private static Native950PlayerMasks.FaceEntity faceTarget(int clientIndex) {
        if (clientIndex == CLEAR_FACE_ENTITY) return Native950PlayerMasks.FaceEntity.none();
        if (clientIndex < 0) return null;
        if (clientIndex >= PLAYER_CLIENT_INDEX_BASE) {
            int index = clientIndex - PLAYER_CLIENT_INDEX_BASE;
            return index > 65535 ? null : Native950PlayerMasks.FaceEntity.player(index);
        }
        return Native950PlayerMasks.FaceEntity.npc(clientIndex);
    }

    // ------------------------------------------------------------------ npc

    /**
     * The CONFIRMED NPC masks for one NPC this tick, or null when none applies.
     *
     * @param added true when this NPC is an addition in this frame, whose record already
     *              carries the definition id, so the transform block would be redundant
     */
    public static Native950NpcMasks.Update npcMasks(NPC npc, boolean added) {
        return npcMasks(npc,added,null);
    }

    public static Native950NpcMasks.Update npcMasks(NPC npc, boolean added,Player viewer) {
        if (npc == null) return null;
        Native950NpcMasks.Update update = new Native950NpcMasks.Update();
        boolean any = false;

        // 950 bit 3: four sequence slots and a plain delay; proven in the parser tail.
        Animation animation = npc.getNextAnimation();
        if (animation != null) {
            int[] slots = animationSlots(animation);
            update.animation(slots[0], slots[1], slots[2], slots[3], animationSpeed(animation));
            any = true;
        }

        // 910 client indexes become the explicit target-kind/index pair in 950 bit 1.
        int faceEntity = npc.getNextFaceEntity();
        if (faceEntity != NO_FACE_ENTITY) {
            if (faceEntity == CLEAR_FACE_ENTITY) { update.clearFaceEntity(); any = true; }
            else if (faceEntity < 0 || faceEntity > PLAYER_CLIENT_INDEX_BASE + 65535) REFUSALS.incrementAndGet();
            else {
                if (faceEntity >= PLAYER_CLIENT_INDEX_BASE) update.facePlayer(faceEntity - PLAYER_CLIENT_INDEX_BASE);
                else update.faceNpc(faceEntity);
                any = true;
            }
        }

        // 950's list replaces the old four fixed graphics blocks. Keep only the source
        // fields whose meaning is established; force-refresh/custom flags remain refused.
        com.rs.game.Graphics[] graphics = {npc.getNextGraphics1(), npc.getNextGraphics2(),
                npc.getNextGraphics3(), npc.getNextGraphics4()};
        List<Native950NpcMasks.Spotanim> spots = new ArrayList<Native950NpcMasks.Spotanim>();
        for (int slot = 0; slot < graphics.length; slot++) {
            com.rs.game.Graphics graphic = graphics[slot];
            if (graphic == null) continue;
            if (graphic.getId() == -1) {
                spots.add(Native950NpcMasks.Spotanim.of(slot, -1, 0, 0, 0, 0, 0));
                continue;
            }
            if ((graphic.getSettings2Hash() & ~7) != 0
                    || !Native950PlayerEffects.isVerifiedGraphic(graphic.getId())) {
                REFUSALS.incrementAndGet(); continue;
            }
            try {
                spots.add(Native950NpcMasks.Spotanim.of(slot, graphic.getId(), graphic.getSpeed(),
                        graphic.getHeight(), graphic.getSettings2Hash() & 7, 0, 0));
            } catch (IllegalArgumentException invalid) { REFUSALS.incrementAndGet(); }
        }
        if (!spots.isEmpty()) {
            // Null id in an ADD record clears exactly this slot. The remove list matches
            // definition ids (client spot +0x84), not slot numbers.
            update.spotanims(new int[0], spots.toArray(new Native950NpcMasks.Spotanim[0]));
            any = true;
        }

        // Face coordinate: 947 bit 3, 950 bit 7 (0x80). 910 Entity.faceEntity() sets the face
        // rectangle rather than
        // the face-entity field, so a banker turning to a player arrives here. The two
        // direction clauses are the 910 encoder's own condition (LocalNPCUpdate.java:227 for
        // the mask bit and :305 for the block): a step already decides where the model looks,
        // and Entity.resetMasks (Entity.java:1732) keeps nextFaceWorldTile alive across a walk,
        // so without them a rectangle set on a walking tick would be re-sent, stale, every
        // tick of that walk and fight the step for the facing. facesTile() below carries the
        // same rule for the player.
        Rectangle facing = npc.getNextFaceWorldTile();
        if (facing != null && npc.getNextWalkDirection() == -1 && npc.getNextRunDirection() == -1
                && npc.getNextForceMovement() == null && npc.getNextFaceEntity() < 0) {
            if (facing.getX() < 0 || facing.getX() > 32767 || facing.getY() < 0 || facing.getY() > 32767) {
                REFUSALS.incrementAndGet();
            } else {
                update.faceCoordinate(facing.getX(), facing.getY());
                any = true;
            }
        }

        // Transform: 947 bit 5, 950 bit 2 (0x4). Skipped on an addition, whose record already
        // names the type.
        // The id is a RAW 910 npc id straight out of 910 content (NPC.setNextNPCTransformation
        // -> Transformation), and this block feeds the client's definition setter (virtual slot
        // 0x218, the same setter the addition record uses). So it goes through the same gate
        // the spawner uses: only an id the 910 -> 947 validity table calls "same" may name a
        // 947 definition. A repurposed id loads cleanly and is the wrong creature, which is
        // exactly what this milestone must never put on the wire. The lower bound is the one
        // Native950NpcMasks.smart2or4null actually enforces (-1 or a non-negative id); -1 is
        // the encoder's null and names no definition at all, so it is refused too.
        Transformation transformation = npc.getNextTransformation();
        if (transformation != null && !added) {
            int type = transformation.getToNPCId();
            if (type < 0 || !Native950IdValidity.get().isSafe(Native950IdValidity.Kind.NPC, type))
                REFUSALS.incrementAndGet();
            else { update.transform(type); any = true; }
        }

        // Forced overhead speech: 947 bit 0, 950 bit 6 (0x40). 947's bit 0 is a live but
        // DIFFERENT block on 950, so carrying the old constant here would have selected it.
        ForceTalk talk = npc.getNextForceTalk();
        if (talk != null) {
            String text = talk.getText();
            if (text == null || !isClientText(text)) REFUSALS.incrementAndGet();
            else { update.say(text); any = true; }
        }

        // Typed standard melee, with source/victim and observer presentation selected per viewer.
        Native950Hits.Snapshot hitSnapshot=Native950Hits.fromRunningCache(npc);
        List<Native950NpcMasks.Hit> hits=hitSnapshot.npcHits(viewer==null?-1:viewer.getIndex());
        REFUSALS.addAndGet(hitSnapshot.refusals());
        Native950Hitbars.Result bars = Native950Hitbars.fromRunningCache(npc);
        REFUSALS.addAndGet(bars.refusals());
        if (!hits.isEmpty() || !bars.npcBars().isEmpty()) {
            update.hits(hits.toArray(new Native950NpcMasks.Hit[0]),
                    bars.npcBars().toArray(new Native950NpcMasks.Hitbar[0]));
            any = true;
        }

        if (!any) return null;
        NPC_BLOCKS.incrementAndGet();
        return update;
    }

    // ------------------------------------------------------------------ shared helpers

    /** The first four sequence slots, with anything below -1 normalised to "no sequence". */
    private static int[] animationSlots(Animation animation) {
        int[] ids = animation.getIds();
        int[] slots = new int[ANIMATION_SLOTS];
        for (int slot = 0; slot < ANIMATION_SLOTS; slot++) {
            int id = ids != null && slot < ids.length ? ids[slot] : -1;
            slots[slot] = id < 0 ? -1 : id;
        }
        return slots;
    }

    /** Both animation blocks carry the speed as one byte; 910 truncates, this clamps. */
    private static int animationSpeed(Animation animation) {
        int speed = animation.getSpeed();
        if (speed < 0) return 0;
        return speed > MAX_ANIMATION_SPEED ? MAX_ANIMATION_SPEED : speed;
    }

    /**
     * True when the text can reach the client's NUL-terminated CP1252 reader intact. Both mask
     * encoders throw on an embedded NUL or an unmappable character; checking here turns that
     * into a counted drop instead of a dead frame for every viewer.
     */
    public static boolean isClientText(String text) {
        if (text == null || text.indexOf('\0') >= 0) return false;
        try {
            Charset.forName("windows-1252").newEncoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .encode(CharBuffer.wrap(text));
            return true;
        } catch (CharacterCodingException notRepresentable) {
            return false;
        }
    }

    /** Player mask blocks handed to the encoder since this JVM started. */
    public static long playerBlocks() { return PLAYER_BLOCKS.get(); }

    /** NPC mask blocks handed to the encoder since this JVM started. */
    public static long npcBlocks() { return NPC_BLOCKS.get(); }

    /**
     * Mask content this port refused to publish: a hit bar, an NPC hit, a text that is not
     * representable in CP1252, or a value that does not fit its confirmed field. Every one of
     * these is a deliberate silence, never a truncated block.
     */
    public static long refusals() { return REFUSALS.get(); }
}
