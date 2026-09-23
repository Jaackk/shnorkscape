package com.rs.network.protocol.modern950;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CodingErrorAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * PLAYER_INFO (950 server opcode 36, size -2) update-mask encoder.
 *
 * <p>The ten typed blocks below are derived from the 950 reader at 0x14012C900 and its
 * transform helpers. Field order, transform selectors, sinks and literal wire fixtures are
 * recorded in {@code protocol-analysis/player-masks-950-derived.md}. Appearance is additionally
 * checked against the 950 appearance constructor/decoder and its biased body copy.
 *
 * <p>These are the ten blocks exposed by the inherited server API, not every block the client
 * accepts. Unknown masks remain unavailable through {@link #candidateMask(int, byte[])}.
 * Header markers 0x10, 0x8000 and 0x40000 are added automatically.
 *
 * <p>This class owns the mask block, not the PLAYER_INFO movement prefix. The prefix must mark
 * the player as requiring a mask. {@link #encodeWithSkippedPrefix(Update)} additionally emits
 * the two unused bytes skipped by the 950 PLAYER_INFO dispatcher before each pending player.
 *
 * <p>All builders validate their ranges; nothing here performs I/O or mutates game state.
 */
public final class Native950PlayerMasks {

    /** Hits and hitbars; read at 0x14012D136. */
    public static final int HITS_HITBARS = 0x40; // 947: 0x8
    /**
     * Appearance blob. 947 used 0x4; the 950 client tests {@code test r15b, 0x20} at 0x14012DC31.
     *
     * Note 0x20 was 947's face-entity bit, so the two mask tables are not a relabelling of each
     * other and no other block's 950 bit may be assumed from its 947 value.
     */
    public static final int APPEARANCE = 0x20; // 947: 0x4
    /** Force movement; read at 0x14012DD96. */
    public static final int FORCE_MOVEMENT = 0x1; // 947: 0x10
    /** Face entity; read at 0x14012CE2D. */
    public static final int FACE_ENTITY = 0x80; // 947: 0x20
    /** Four sequence slots and delay; read at 0x14012CBC3. */
    public static final int ANIMATION = 0x8; // 947: 0x40
    /** Face angle, last of the supported blocks in consumption order. */
    public static final int FACE_ANGLE = 0x2; // 947: 0x80
    /** Overhead text, local echo only. */
    public static final int FORCE_TALK_LOCAL = 0x400; // 947: 0x8000
    /** Overhead text plus flags. */
    public static final int FORCE_TALK = 0x400000; // 947: 0x10000
    /** HSL colour overlay; read at 0x14012E0DA. */
    public static final int COLOUR_OVERLAY = 0x200000; // 947: 0x800000
    /** Spotanim removals and additions, first supported block in consumption order. */
    public static final int SPOTANIM_LIST = 0x4000000; // unmoved between revisions

    /** Header extension marker: pulls a second mask byte into bits 8..15 (0x14012C898). */
    public static final int HEADER_MARKER_BYTE2 = 0x10;
    /** Header extension marker: third mask byte, bits 16..23. 950 tests bit 15 (0x14012c8b5). */
    public static final int HEADER_MARKER_BYTE3 = 0x8000;
    /** Header extension marker: fourth mask byte, bits 24..31. 950 tests bit 18 (0x14012c8d3). */
    public static final int HEADER_MARKER_BYTE4 = 0x40000;

    private Native950PlayerMasks() { }

    /**
     * The complete mask block: the 1..4 header bytes followed by every set block in the
     * client's consumption order (the order of the tests in {@code 0x14012C900}, which is not
     * the numeric order of the bits).
     *
     * @throws IllegalArgumentException if no confirmed mask is set
     */
    public static byte[] encode(Update update) {
        Objects.requireNonNull(update, "update");
        requireDerivedBlocks(update);
        Writer out = new Writer();
        writeHeader(out, update.maskBits());
        // 950 consumption order. The client reads these positionally, so this sequence is
        // load-bearing on its own: it is NOT 947's order and it is not bit order.
        //   spotanims, animation, faceEntity, hits, forceTalk, appearance, forceMovement,
        //   colourOverlay, forceTalkLocal, faceAngle
        if (update.spotanims != null) writeSpotanims(out, update.spotanims);
        if (update.animation != null) writeAnimation(out, update.animation);
        if (update.faceEntity != null) out.mediumLE(update.faceEntity.encoded);
        if (update.hits != null) writeHits(out, update.hits, update.hitbars);
        if (update.forceTalk != null) {
            out.string(update.forceTalk);
            out.u8(update.forceTalkFlags);
        }
        if (update.appearance != null) {
            out.u8(update.appearance.length + 128);
            // 950 installs transform script byte 0x02 at .rdata 0x140B5FEC9, so the body copy at
            // 0x14010DE7C adds 0x80 to every byte on receive; the server must pre-bias it. 947
            // installed 0x00 there, a plain memcpy, which is why the raw body worked before.
            byte[] biased = new byte[update.appearance.length];
            for (int index = 0; index < biased.length; index++)
                biased[index] = (byte) (update.appearance[index] ^ 0x80);
            out.raw(biased);
        }
        if (update.forceMovement != null) writeForceMovement(out, update.forceMovement);
        if (update.colourOverlay != null) writeColourOverlay(out, update.colourOverlay);
        if (update.forceTalkLocal != null) out.string(update.forceTalkLocal);
        if (update.faceAngle >= 0) out.u16(update.faceAngle);
        return out.bytes();
    }

    /**
     * {@link #encode(Update)} prefixed with the two zero bytes the PLAYER_INFO dispatch skips
     * before each pending player's mask block ({@code 0x140143660}). The native parser never
     * reads them, so zeros are correct.
     */
    public static byte[] encodeWithSkippedPrefix(Update update) {
        byte[] block = encode(update);
        byte[] result = new byte[block.length + 2];
        System.arraycopy(block, 0, result, 2, block.length);
        return result;
    }

    /**
     * Raw masks are deliberately unavailable. Known blocks require their typed builder and
     * all other blocks still need a complete 950 derivation, including their semantic sink.
     *
     * @throws UnsupportedOperationException always
     */
    public static byte[] candidateMask(int maskBit, byte[] block) {
        throw new UnsupportedOperationException("PLAYER_INFO raw mask 0x"
                + Integer.toHexString(maskBit) + " is unavailable: use a derived typed block,"
                + " otherwise the 950 layout and sink remain unverified"
                + " (protocol-analysis/player-masks-950-derived.md)");
    }

    /** Legacy API retained as a refusal; the 947 head-icon bit is not a 950 contract. */
    public static byte[] headIcons(byte[] blob) {
        return candidateMask(0x1000, blob);
    }

    // ------------------------------------------------------------------ header

    /** Keep unknown future blocks fenced until their complete 950 layout is derived. */
    private static final int DERIVED_BLOCKS = APPEARANCE | FACE_ANGLE | FORCE_TALK | FORCE_TALK_LOCAL
            | ANIMATION | FACE_ENTITY | FORCE_MOVEMENT | COLOUR_OVERLAY | SPOTANIM_LIST | HITS_HITBARS;

    private static void requireDerivedBlocks(Update update) {
        int underived = update.maskBits() & ~DERIVED_BLOCKS;
        if (underived != 0)
            throw new UnsupportedOperationException("Player update block(s) 0x"
                    + Integer.toHexString(underived) + " have no complete 950 derivation; see"
                    + " protocol-analysis/player-masks-950-derived.md");
    }

    /** Write the mask little-endian, adding the 950 extension markers from highest to lowest. */
    private static void writeHeader(Writer out, int base) {
        if (base == 0) throw new IllegalArgumentException("At least one confirmed mask must be set");
        int mask = base;
        if ((mask >>> 24) != 0) mask |= HEADER_MARKER_BYTE4 | HEADER_MARKER_BYTE3 | HEADER_MARKER_BYTE2;
        else if (((mask >>> 16) & 0xFF) != 0) mask |= HEADER_MARKER_BYTE3 | HEADER_MARKER_BYTE2;
        else if (((mask >>> 8) & 0xFF) != 0) mask |= HEADER_MARKER_BYTE2;
        out.u8(mask);
        if ((mask & HEADER_MARKER_BYTE2) != 0) out.u8(mask >>> 8);
        if ((mask & HEADER_MARKER_BYTE3) != 0) out.u8(mask >>> 16);
        if ((mask & HEADER_MARKER_BYTE4) != 0) out.u8(mask >>> 24);
    }

    // ------------------------------------------------------------------ blocks

    /** Bit 0x1; 0x14012DDA0..0x14012DEDC, sink 0x14031DAE0. */
    private static void writeForceMovement(Writer out, ForceMovement move) {
        out.u8(128 - move.deltaX1);
        out.u8(move.deltaY1);
        out.u8(-move.deltaX2);
        out.u8(move.deltaY2 + 128);
        out.u8(-move.planeDelta1);
        out.u8(128 - move.planeDelta2);
        out.u16le(move.arriveTick1);
        out.u16(move.arriveTick2);
        out.u16le(move.direction);
    }

    /** Bit 0x8; four BE big-smarts and a PLAIN speed byte at 0x14012CDDD. */
    private static void writeAnimation(Writer out, Animation animation) {
        for (int i = 0; i < 4; i++) out.animationSmart(animation.sequences[i]);
        out.u8(animation.speed);
    }

    /** Bit 0x200000; 0x14012E0E5..0x14012E185. */
    private static void writeColourOverlay(Writer out, ColourOverlay colour) {
        out.u8(colour.hue);
        out.u8(128 - colour.saturation);
        out.u8(colour.lightness);
        out.u8(128 - colour.strength);
        out.shortLELowPlus128(colour.delay);
        out.u16(colour.duration);
    }

    /** Bit 0x4000000; 0x14012C9BB..0x14012CB48, selector script 0x140B5FEE0. */
    private static void writeSpotanims(Writer out, SpotanimList list) {
        out.u8(list.removals.length);
        for (int id : list.removals) out.u16(id & 0xFFFF);
        out.u8(list.additions.size());
        for (Spotanim spotanim : list.additions) {
            out.u8(128 - spotanim.slot);
            out.shortLELowPlus128(spotanim.spotanimId & 0xFFFF);
            int packed = (spotanim.height << 16) | (spotanim.heightFlag ? 0x8000 : 0) | spotanim.delay;
            out.i32le(packed);
            out.u8(spotanim.rotation | (spotanim.rotationFlag ? 0x80 : 0));
            int offsets = ((spotanim.xOffset + 0x3FF) & 0x7FF)
                    | (((spotanim.yOffset + 0x3FF) & 0x7FF) << 11)
                    | (spotanim.offsetFlag ? 1 << 22 : 0);
            out.mediumLE(offsets);
        }
    }

    /** Bit 0x40; 0x14012D140..0x14012D844, smart hits and conditional hitbar fields. */
    private static void writeHits(Writer out, List<Hit> hits, List<Hitbar> hitbars) {
        out.u8(hits.size() + 128);
        for (Hit hit : hits) {
            switch (hit.form) {
                case Hit.FORM_DUAL:
                    out.hitSmart(0x7FFF);
                    out.hitSmart(hit.type);
                    out.hitSmart(hit.damage);
                    out.hitSmart(hit.type2);
                    out.hitSmart(hit.damage2);
                    break;
                case Hit.FORM_UNTYPED:
                    out.hitSmart(0x7FFE);
                    out.u8(hit.damage);
                    break;
                default:
                    out.hitSmart(hit.type);
                    out.hitSmart(hit.damage);
                    break;
            }
            out.hitSmart(hit.delay);
        }
        out.u8(-hitbars.size());
        for (Hitbar bar : hitbars) {
            out.hitSmart(bar.barId);
            out.hitSmart(bar.cycle);
            if (bar.cycle == Hitbar.REMOVE_CYCLE) continue;
            out.hitSmart(bar.delay);
            out.u8(-bar.percent1);
            if (bar.cycle != 0) out.u8(128 - bar.percent2);
            if (bar.size <= 126) out.u8(bar.size + 1);
            else out.u16(bar.size - 0x7FFF);
            if (bar.size > -1) {
                out.u8(bar.quantity1 + 128);
                if (bar.cycle != 0) out.u8(bar.quantity2 + 128);
            }
        }
    }

    // ------------------------------------------------------------------ values

    /**
     * Mask 0x1. Tile deltas from the actor's current position, two plane deltas, two
     * arrive-tick offsets and a 14-bit direction. Derived from 0x14012DDA0..0x14012DEDC.
     */
    public static final class ForceMovement {
        private final int deltaX1, deltaY1, deltaX2, deltaY2;
        private final int planeDelta1, planeDelta2;
        private final int arriveTick1, arriveTick2;
        private final int direction;

        private ForceMovement(int deltaX1, int deltaY1, int deltaX2, int deltaY2,
                int planeDelta1, int planeDelta2, int arriveTick1, int arriveTick2, int direction) {
            this.deltaX1 = deltaX1; this.deltaY1 = deltaY1;
            this.deltaX2 = deltaX2; this.deltaY2 = deltaY2;
            this.planeDelta1 = planeDelta1; this.planeDelta2 = planeDelta2;
            this.arriveTick1 = arriveTick1; this.arriveTick2 = arriveTick2;
            this.direction = direction;
        }

        /**
         * @param deltaX1 first waypoint x offset in tiles, -128..127
         * @param deltaY1 first waypoint y offset in tiles, -128..127
         * @param deltaX2 second waypoint x offset in tiles, -128..127
         * @param deltaY2 second waypoint y offset in tiles, -128..127
         * @param planeDelta1 plane offset added to the first waypoint, -128..127
         * @param planeDelta2 plane offset added to the second waypoint, -128..127
         * @param arriveTick1 first arrival, ticks from now, 0..65535
         * @param arriveTick2 second arrival, ticks from now, 0..65535
         * @param direction facing direction in 1/16384 turn units, 0..16383
         */
        public static ForceMovement of(int deltaX1, int deltaY1, int deltaX2, int deltaY2,
                int planeDelta1, int planeDelta2, int arriveTick1, int arriveTick2, int direction) {
            signedByte(deltaX1, "deltaX1"); signedByte(deltaY1, "deltaY1");
            signedByte(deltaX2, "deltaX2"); signedByte(deltaY2, "deltaY2");
            signedByte(planeDelta1, "planeDelta1"); signedByte(planeDelta2, "planeDelta2");
            range(arriveTick1, 0, 65535, "arriveTick1");
            range(arriveTick2, 0, 65535, "arriveTick2");
            range(direction, 0, 16383, "direction");
            return new ForceMovement(deltaX1, deltaY1, deltaX2, deltaY2,
                    planeDelta1, planeDelta2, arriveTick1, arriveTick2, direction);
        }
    }

    /**
     * Mask 0x8. Four sequence-id slots and one speed/delay byte. A slot holds -1 for "no
     * sequence". Derived from 0x14012CBCD..0x14012CDEF.
     */
    public static final class Animation {
        private final int[] sequences;
        private final int speed;

        private Animation(int[] sequences, int speed) { this.sequences = sequences; this.speed = speed; }

        /** One sequence in slot 0, the other three empty. */
        public static Animation of(int sequenceId, int speed) {
            return of(new int[] {sequenceId, -1, -1, -1}, speed);
        }

        /**
         * @param sequences exactly four slots, each -1 or 0..0x7FFFFFFF
         * @param speed animation speed/delay byte, 0..255
         */
        public static Animation of(int[] sequences, int speed) {
            Objects.requireNonNull(sequences, "sequences");
            if (sequences.length != 4)
                throw new IllegalArgumentException("Animation requires exactly 4 sequence slots");
            for (int i = 0; i < 4; i++)
                if (sequences[i] < -1) throw new IllegalArgumentException("sequences[" + i + "] must be -1 or positive");
            range(speed, 0, 255, "speed");
            return new Animation(sequences.clone(), speed);
        }
    }

    /**
     * Mask 0x80. A little-endian 3-byte medium whose high byte selects the target kind: 1 = NPC index,
     * 2 = player index, 0xFF = clear. Derived from 0x14012CE2D..0x14012CE93.
     */
    public static final class FaceEntity {
        private final int encoded;
        private FaceEntity(int encoded) { this.encoded = encoded; }

        public static FaceEntity npc(int index) {
            range(index, 0, 65535, "npcIndex");
            return new FaceEntity(0x010000 | index);
        }

        public static FaceEntity player(int index) {
            range(index, 0, 65535, "playerIndex");
            return new FaceEntity(0x020000 | index);
        }

        /** Clears the current face target (high byte 0xFF, sink 0x14012CE7E). */
        public static FaceEntity none() { return new FaceEntity(0xFFFFFF); }
    }

    /**
     * Mask 0x200000. An HSL palette tint applied for a window of ticks (0x14012E0DA). The client forms the palette index as
     * {@code (hue << 10) | (saturation << 7) | lightness}.
     */
    public static final class ColourOverlay {
        private final int hue, saturation, lightness, strength, delay, duration;

        private ColourOverlay(int hue, int saturation, int lightness, int strength, int delay, int duration) {
            this.hue = hue; this.saturation = saturation; this.lightness = lightness;
            this.strength = strength; this.delay = delay; this.duration = duration;
        }

        /**
         * @param hue 0..63
         * @param saturation 0..7
         * @param lightness 0..127
         * @param strength 0..255
         * @param delay ticks before the tint starts, 0..65535
         * @param duration end tick offset from the current tick, 0..65535 (not added to delay)
         */
        public static ColourOverlay of(int hue, int saturation, int lightness, int strength,
                int delay, int duration) {
            range(hue, 0, 63, "hue");
            range(saturation, 0, 7, "saturation");
            range(lightness, 0, 127, "lightness");
            range(strength, 0, 255, "strength");
            range(delay, 0, 65535, "delay");
            range(duration, 0, 65535, "duration");
            return new ColourOverlay(hue, saturation, lightness, strength, delay, duration);
        }
    }

    /**
     * One entry of the mask 0x4000000 add list. The three flag bits (packed bit 15, rotation
     * bit 7, offsets bit 22) are read by the client but their meaning is not established by
     * the evidence, so they default to false.
     */
    public static final class Spotanim {
        private final int slot, spotanimId, delay, height, rotation, xOffset, yOffset;
        private final boolean heightFlag, rotationFlag, offsetFlag;

        private Spotanim(int slot, int spotanimId, int delay, int height, boolean heightFlag,
                int rotation, boolean rotationFlag, int xOffset, int yOffset, boolean offsetFlag) {
            this.slot = slot; this.spotanimId = spotanimId; this.delay = delay;
            this.height = height; this.heightFlag = heightFlag;
            this.rotation = rotation; this.rotationFlag = rotationFlag;
            this.xOffset = xOffset; this.yOffset = yOffset; this.offsetFlag = offsetFlag;
        }

        public static Spotanim of(int slot, int spotanimId, int delay, int height, int rotation,
                int xOffset, int yOffset) {
            return of(slot, spotanimId, delay, height, false, rotation, false, xOffset, yOffset, false);
        }

        /**
         * @param slot spotanim slot, 0..255; slots below 0x80 go to the actor list at +0x1018
         * @param spotanimId spotanim id, or -1 for none
         * @param delay start delay in ticks, 0..32767
         * @param height height field, 0..32767 (the client keeps {@code (v>>14)&~3})
         * @param rotation rotation in 45 degree steps, 0..7
         * @param xOffset x offset, -1023..1024
         * @param yOffset y offset, -1023..1024
         */
        public static Spotanim of(int slot, int spotanimId, int delay, int height, boolean heightFlag,
                int rotation, boolean rotationFlag, int xOffset, int yOffset, boolean offsetFlag) {
            range(slot, 0, 255, "slot");
            range(spotanimId, -1, 65534, "spotanimId");
            range(delay, 0, 32767, "delay");
            range(height, 0, 32767, "height");
            range(rotation, 0, 7, "rotation");
            range(xOffset, -1023, 1024, "xOffset");
            range(yOffset, -1023, 1024, "yOffset");
            return new Spotanim(slot, spotanimId, delay, height, heightFlag,
                    rotation, rotationFlag, xOffset, yOffset, offsetFlag);
        }
    }

    /** Mask 0x4000000: ids to remove followed by spotanims to add. */
    public static final class SpotanimList {
        private final int[] removals;
        private final List<Spotanim> additions;

        private SpotanimList(int[] removals, List<Spotanim> additions) {
            this.removals = removals; this.additions = additions;
        }

        /**
         * @param removals spotanim ids to remove, 0..32767, or a single -1 to remove all;
         *        at most 255 entries
         * @param additions spotanims to add, at most 255 entries
         */
        public static SpotanimList of(int[] removals, List<Spotanim> additions) {
            Objects.requireNonNull(removals, "removals");
            Objects.requireNonNull(additions, "additions");
            if (removals.length > 255) throw new IllegalArgumentException("At most 255 spotanim removals");
            if (additions.size() > 255) throw new IllegalArgumentException("At most 255 spotanim additions");
            if (removals.length == 0 && additions.isEmpty())
                throw new IllegalArgumentException("Spotanim list must remove or add at least one entry");
            for (int id : removals) {
                range(id, -1, 32767, "removal id");
                // 950 0x14012C9FE jumps directly to additions after clear-all. Any following
                // removal bytes would instead be read as the addition count/record payload.
                if (id == -1 && removals.length != 1)
                    throw new IllegalArgumentException("Clear-all must be the only spotanim removal");
            }
            List<Spotanim> copy = new ArrayList<Spotanim>(additions.size());
            for (Spotanim spotanim : additions) copy.add(Objects.requireNonNull(spotanim, "spotanim"));
            return new SpotanimList(removals.clone(), Collections.unmodifiableList(copy));
        }
    }

    /**
     * One entry of the mask 0x40 hit list. Three confirmed forms: a plain typed hit, a dual
     * typed hit behind the 0x7FFF marker, and an untyped hit behind the 0x7FFE marker whose
     * damage is a plain byte.
     */
    public static final class Hit {
        private static final int FORM_SIMPLE = 0;
        private static final int FORM_DUAL = 1;
        private static final int FORM_UNTYPED = 2;

        private final int form, type, damage, type2, damage2, delay;

        private Hit(int form, int type, int damage, int type2, int damage2, int delay) {
            this.form = form; this.type = type; this.damage = damage;
            this.type2 = type2; this.damage2 = damage2; this.delay = delay;
        }

        /**
         * @param type hit type, 0..0x7FFD (0x7FFE and 0x7FFF are the client's form markers)
         * @param damage damage value, 0..32767
         * @param delay ticks before the hit shows, 0..32767
         */
        public static Hit of(int type, int damage, int delay) {
            range(type, 0, 0x7FFD, "type");
            range(damage, 0, 0x7FFF, "damage");
            range(delay, 0, 0x7FFF, "delay");
            return new Hit(FORM_SIMPLE, type, damage, -1, 0, delay);
        }

        /** Two typed damage values in one hit (client marker 0x7FFF). */
        public static Hit dual(int type, int damage, int type2, int damage2, int delay) {
            range(type, 0, 0x7FFF, "type");
            range(damage, 0, 0x7FFF, "damage");
            range(type2, 0, 0x7FFF, "type2");
            range(damage2, 0, 0x7FFF, "damage2");
            range(delay, 0, 0x7FFF, "delay");
            return new Hit(FORM_DUAL, type, damage, type2, damage2, delay);
        }

        /** Type -1, damage as a plain byte (client marker 0x7FFE). */
        public static Hit untyped(int damage, int delay) {
            range(damage, 0, 255, "damage");
            range(delay, 0, 0x7FFF, "delay");
            return new Hit(FORM_UNTYPED, -1, damage, -1, 0, delay);
        }
    }

    /**
     * One entry of the mask 0x40 hitbar list. When {@code cycle} is zero the client reuses the
     * first percentage and quantity for the second, and those bytes are not on the wire, so
     * this class requires the pairs to be equal in that case.
     */
    public static final class Hitbar {
        private static final int REMOVE_CYCLE = 0x7FFF;

        private final int barId, cycle, delay, percent1, percent2, size, quantity1, quantity2;

        private Hitbar(int barId, int cycle, int delay, int percent1, int percent2,
                int size, int quantity1, int quantity2) {
            this.barId = barId; this.cycle = cycle; this.delay = delay;
            this.percent1 = percent1; this.percent2 = percent2;
            this.size = size; this.quantity1 = quantity1; this.quantity2 = quantity2;
        }

        /** Removes the matching bar (cycle 0x7FFF, sink 0x14012D789). */
        public static Hitbar remove(int barId) {
            range(barId, 0, 0x7FFE, "barId");
            return new Hitbar(barId, REMOVE_CYCLE, 0, 0, 0, -1, 0, 0);
        }

        /** A bar with no quantity fields (size -1). */
        public static Hitbar update(int barId, int cycle, int delay, int percent1, int percent2) {
            return sized(barId, cycle, delay, percent1, percent2, -1, 0, 0);
        }

        /**
         * @param barId hitbar id, 0..0x7FFE
         * @param cycle animation cycle, 0..0x7FFE (0x7FFF is the client's remove marker)
         * @param delay ticks before the bar shows, 0..32767
         * @param percent1 first fill byte, 0..255
         * @param percent2 second fill byte, 0..255; must equal percent1 when cycle is zero
         * @param size -1..32766; the quantity bytes are only written when size is above -1
         * @param quantity1 first quantity byte, 0..255; must be zero when size is -1
         * @param quantity2 second quantity byte, 0..255; must equal quantity1 when cycle is zero
         */
        public static Hitbar sized(int barId, int cycle, int delay, int percent1, int percent2,
                int size, int quantity1, int quantity2) {
            range(barId, 0, 0x7FFE, "barId");
            range(cycle, 0, 0x7FFE, "cycle");
            range(delay, 0, 0x7FFF, "delay");
            range(percent1, 0, 255, "percent1");
            range(percent2, 0, 255, "percent2");
            range(size, -1, 32766, "size");
            range(quantity1, 0, 255, "quantity1");
            range(quantity2, 0, 255, "quantity2");
            if (cycle == 0 && percent1 != percent2)
                throw new IllegalArgumentException("percent2 is not on the wire when cycle is zero");
            if (cycle == 0 && quantity1 != quantity2)
                throw new IllegalArgumentException("quantity2 is not on the wire when cycle is zero");
            if (size <= -1 && (quantity1 != 0 || quantity2 != 0))
                throw new IllegalArgumentException("Quantity bytes are not on the wire when size is -1");
            return new Hitbar(barId, cycle, delay, percent1, percent2, size, quantity1, quantity2);
        }
    }

    // ------------------------------------------------------------------ update

    /** An immutable set of confirmed masks and their values. Build one with {@link #builder()}. */
    public static final class Update {
        private final byte[] appearance;
        private final ForceMovement forceMovement;
        private final Animation animation;
        private final SpotanimList spotanims;
        private final FaceEntity faceEntity;
        private final ColourOverlay colourOverlay;
        private final String forceTalk;
        private final int forceTalkFlags;
        private final String forceTalkLocal;
        private final int faceAngle;
        private final List<Hit> hits;
        private final List<Hitbar> hitbars;

        private Update(Builder builder) {
            this.appearance = builder.appearance == null ? null : builder.appearance.clone();
            this.forceMovement = builder.forceMovement;
            this.animation = builder.animation;
            this.spotanims = builder.spotanims;
            this.faceEntity = builder.faceEntity;
            this.colourOverlay = builder.colourOverlay;
            this.forceTalk = builder.forceTalk;
            this.forceTalkFlags = builder.forceTalkFlags;
            this.forceTalkLocal = builder.forceTalkLocal;
            this.faceAngle = builder.faceAngle;
            this.hits = builder.hits;
            this.hitbars = builder.hitbars;
        }

        private Update(Update source, ForceMovement force) {
            this.appearance = source.appearance;
            this.forceMovement = force;
            this.animation = source.animation;
            this.spotanims = source.spotanims;
            this.faceEntity = source.faceEntity;
            this.colourOverlay = source.colourOverlay;
            this.forceTalk = source.forceTalk;
            this.forceTalkFlags = source.forceTalkFlags;
            this.forceTalkLocal = source.forceTalkLocal;
            this.faceAngle = source.faceAngle;
            this.hits = source.hits;
            this.hitbars = source.hitbars;
        }

        /**
         * Return an immutable copy whose force XY offsets use another movement-prefix base.
         * The shifts are oldBase minus newBase; planes, times and other blocks are unchanged.
         * The source update and its builder can safely be reused by another viewer.
         */
        public Update rebaseForceMovement(int deltaX, int deltaY) {
            if (forceMovement == null) throw new IllegalStateException("No force movement to rebase");
            ForceMovement move = forceMovement;
            ForceMovement rebased = ForceMovement.of(
                    Math.addExact(move.deltaX1, deltaX), Math.addExact(move.deltaY1, deltaY),
                    Math.addExact(move.deltaX2, deltaX), Math.addExact(move.deltaY2, deltaY),
                    move.planeDelta1, move.planeDelta2, move.arriveTick1, move.arriveTick2, move.direction);
            return new Update(this, rebased);
        }

        /** The OR of every content bit set on this update, without the header markers. */
        public int maskBits() {
            int mask = 0;
            if (forceMovement != null) mask |= FORCE_MOVEMENT;
            if (appearance != null) mask |= APPEARANCE;
            if (animation != null) mask |= ANIMATION;
            if (spotanims != null) mask |= SPOTANIM_LIST;
            if (faceEntity != null) mask |= FACE_ENTITY;
            if (colourOverlay != null) mask |= COLOUR_OVERLAY;
            if (forceTalk != null) mask |= FORCE_TALK;
            if (faceAngle >= 0) mask |= FACE_ANGLE;
            if (hits != null) mask |= HITS_HITBARS;
            if (forceTalkLocal != null) mask |= FORCE_TALK_LOCAL;
            return mask;
        }

        public static Builder builder() { return new Builder(); }
    }

    /** Mutable builder; every setter validates immediately. */
    public static final class Builder {
        private byte[] appearance;
        private ForceMovement forceMovement;
        private Animation animation;
        private SpotanimList spotanims;
        private FaceEntity faceEntity;
        private ColourOverlay colourOverlay;
        private String forceTalk;
        private int forceTalkFlags;
        private String forceTalkLocal;
        private int faceAngle = -1;
        private List<Hit> hits;
        private List<Hitbar> hitbars;

        private Builder() { }

        /** Mask 0x20. Copies the input blob; encoding biases each byte by 128. */
        public Builder appearance(byte[] blob) {
            Objects.requireNonNull(blob, "appearance");
            if (blob.length < 1 || blob.length > 255)
                throw new IllegalArgumentException("Appearance requires 1..255 bytes");
            this.appearance = blob.clone();
            return this;
        }

        /** Mask 0x1. */
        public Builder forceMovement(ForceMovement move) {
            this.forceMovement = Objects.requireNonNull(move, "forceMovement");
            return this;
        }

        /** Mask 0x8. */
        public Builder animation(Animation value) {
            this.animation = Objects.requireNonNull(value, "animation");
            return this;
        }

        /** Mask 0x8, one sequence in slot 0. */
        public Builder animation(int sequenceId, int speed) {
            return animation(Animation.of(sequenceId, speed));
        }

        /** Mask 0x4000000. */
        /** Compose persistent actor effects with this tick's unrelated spot animations. */
        public Builder appendSpotanims(SpotanimList list) {
            Objects.requireNonNull(list,"spotanims");
            if(spotanims==null)return spotanims(list);
            int[] removals=java.util.Arrays.copyOf(spotanims.removals,spotanims.removals.length+list.removals.length);
            System.arraycopy(list.removals,0,removals,spotanims.removals.length,list.removals.length);
            java.util.List<Spotanim> added=new java.util.ArrayList<>(spotanims.additions);added.addAll(list.additions);
            return spotanims(SpotanimList.of(removals,added));
        }
        public Builder spotanims(SpotanimList list) {
            this.spotanims = Objects.requireNonNull(list, "spotanims");
            return this;
        }

        /** Mask 0x80. */
        public Builder faceEntity(FaceEntity target) {
            this.faceEntity = Objects.requireNonNull(target, "faceEntity");
            return this;
        }

        /** Mask 0x200000. */
        public Builder colourOverlay(ColourOverlay colour) {
            this.colourOverlay = Objects.requireNonNull(colour, "colourOverlay");
            return this;
        }

        /**
         * Mask 0x400000: overhead text plus a flags byte. Flags bit 0 also echoes the text into
         * the chatbox as message type 2 (sink 0x14012E23A).
         *
         * @param text CP1252 text without an embedded NUL
         * @param flags 0..255
         */
        public Builder forceTalk(String text, int flags) {
            Objects.requireNonNull(text, "forceTalk text");
            range(flags, 0, 255, "forceTalk flags");
            noNul(text, "forceTalk text");
            this.forceTalk = text;
            this.forceTalkFlags = flags;
            return this;
        }

        /**
         * Mask 0x400: overhead text only. The client echoes it into the chatbox only when the
         * updated player is the local player (0x14012EB55).
         */
        public Builder forceTalkLocal(String text) {
            Objects.requireNonNull(text, "forceTalkLocal text");
            noNul(text, "forceTalkLocal text");
            this.forceTalkLocal = text;
            return this;
        }

        /** Mask 0x2. Plain big-endian ushort in 1/16384 turn units, 0..16383. */
        public Builder faceAngle(int angle) {
            range(angle, 0, 16383, "faceAngle");
            this.faceAngle = angle;
            return this;
        }

        /**
         * Mask 0x40. Both lists may be empty but at least one entry is required overall; each
         * holds at most 255 entries.
         */
        public Builder hits(List<Hit> hitList, List<Hitbar> hitbarList) {
            Objects.requireNonNull(hitList, "hits");
            Objects.requireNonNull(hitbarList, "hitbars");
            if (hitList.size() > 255) throw new IllegalArgumentException("At most 255 hits");
            if (hitbarList.size() > 255) throw new IllegalArgumentException("At most 255 hitbars");
            if (hitList.isEmpty() && hitbarList.isEmpty())
                throw new IllegalArgumentException("Hit update needs at least one hit or hitbar");
            List<Hit> copiedHits = new ArrayList<Hit>(hitList.size());
            for (Hit hit : hitList) copiedHits.add(Objects.requireNonNull(hit, "hit"));
            List<Hitbar> copiedBars = new ArrayList<Hitbar>(hitbarList.size());
            for (Hitbar bar : hitbarList) copiedBars.add(Objects.requireNonNull(bar, "hitbar"));
            this.hits = Collections.unmodifiableList(copiedHits);
            this.hitbars = Collections.unmodifiableList(copiedBars);
            return this;
        }

        /** Mask 0x40 with a single hit and no hitbars. */
        public Builder hit(Hit hit) {
            return hits(Arrays.asList(hit), Collections.<Hitbar>emptyList());
        }

        /**
         * Refuses a CANDIDATE mask.
         *
         * @throws UnsupportedOperationException always
         */
        public Builder candidateMask(int maskBit, byte[] block) {
            Native950PlayerMasks.candidateMask(maskBit, block);
            return this;
        }

        /**
         * Refuses the legacy raw head-icon API; no 950 layout is claimed.
         *
         * @throws UnsupportedOperationException always
         */
        public Builder headIcons(byte[] blob) { return candidateMask(0x1000, blob); }

        public Update build() {
            Update update = new Update(this);
            if (update.maskBits() == 0)
                throw new IllegalArgumentException("At least one confirmed mask must be set");
            return update;
        }
    }

    /** Convenience for {@code Update.builder()}. */
    public static Builder builder() { return new Builder(); }

    // ------------------------------------------------------------------ helpers

    private static void range(int value, int min, int max, String field) {
        if (value < min || value > max)
            throw new IllegalArgumentException(field + " must be " + min + ".." + max);
    }

    private static void signedByte(int value, String field) {
        range(value, -128, 127, field);
    }

    /** The client's reader is strlen based, so an embedded NUL would truncate the block. */
    private static void noNul(String value, String field) {
        if (value.indexOf('\0') >= 0)
            throw new IllegalArgumentException(field + " must not contain an embedded NUL");
    }

    private static final class Writer {
        private final ByteArrayOutputStream out = new ByteArrayOutputStream();

        void u8(int n) { out.write(n & 0xFF); }
        void u16(int n) { u8(n >>> 8); u8(n); }
        void u16le(int n) { u8(n); u8(n >>> 8); }
        void i32le(int n) { u8(n); u8(n >>> 8); u8(n >>> 16); u8(n >>> 24); }
        void mediumLE(int n) { u8(n); u8(n >>> 8); u8(n >>> 16); }
        void raw(byte[] bytes) { out.write(bytes, 0, bytes.length); }

        /** 950 helper 0x14010D970 selector 03: LE ushort, low byte carries +128. */
        void shortLELowPlus128(int n) { u8(n + 128); u8(n >>> 8); }

        /**
         * The animation smart at 0x14012CBCD: one byte over 0x7F selects a 4-byte big-endian
         * int with bit 31 set on the wire; otherwise a 2-byte big-endian ushort, where 0x7FFF
         * means -1.
         */
        void animationSmart(int value) {
            if (value == -1) { u16(0x7FFF); return; }
            if (value <= 0x7FFE) { u16(value); return; }
            u8((value >>> 24) | 0x80);
            u8(value >>> 16);
            u8(value >>> 8);
            u8(value);
        }

        /**
         * The hit smart at 0x14012D180: one byte below 0x80, otherwise a 2-byte big-endian
         * ushort with the high bit set (the client adds 0x8000 back).
         */
        void hitSmart(int value) {
            if (value < 0 || value > 0x7FFF)
                throw new IllegalArgumentException("Hit smart value must be 0..32767");
            if (value < 0x80) u8(value);
            else u16(value | 0x8000);
        }

        /** NUL-terminated CP1252, matching the client's strlen-based reader at 0x14012E045. */
        void string(String value) {
            if (value.indexOf('\0') >= 0)
                throw new IllegalArgumentException("Embedded NUL in client string");
            try {
                ByteBuffer encoded = Charset.forName("windows-1252").newEncoder()
                        .onMalformedInput(CodingErrorAction.REPORT)
                        .onUnmappableCharacter(CodingErrorAction.REPORT)
                        .encode(CharBuffer.wrap(value));
                while (encoded.hasRemaining()) u8(encoded.get());
                u8(0);
            } catch (CharacterCodingException ex) {
                throw new IllegalArgumentException("Client string is not representable in CP1252", ex);
            }
        }

        byte[] bytes() { return out.toByteArray(); }
    }
}
