package modern947;

import com.rs.game.player.client.Native950Viewport;
import com.rs.network.protocol.modern950.Native950Packets;
import com.rs.network.protocol.modern950.Native950PlayerInfo;
import com.rs.network.protocol.modern950.Native950PlayerInfo.Actor;
import com.rs.network.protocol.modern950.Native950PlayerMasks;
import com.rs.network.protocol.modern950.Native950Protocol.ServerPacket;
import com.rs.utils.Utils;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.util.Collections;

import static org.junit.Assert.*;

/**
 * Byte-level tests for the generalised PLAYER_INFO encoder.
 *
 * <p>Cache-free and world-free: every input is an immutable {@link Actor} snapshot, so the
 * assertions are about the wire and nothing else. The literal anchors are the two frames pinned
 * in {@code OpenNXT/data/prot/947/generated/native947-3/verified/PLAYER_INFO.md} ("Initial-state
 * byte examples") and the shipped, client-verified single-entity writers in
 * {@link Native950Packets}, which this encoder must reproduce exactly before it may carry a
 * second session. Everything else is written as an explicit bit string in the field order the
 * client parser reads ({@code player-local-movement}, {@code player-external-movement} and
 * {@code player-init} disassembly), so a wrong width fails loudly rather than shifting silently.
 */
public final class Native950PlayerInfoTest {

    private static final int NPC_BITS = 7;
    private static final int AREA_TYPE = 0;
    private static final int HASH1 = 0;
    private static final int HASH2 = 0;

    /** 2046 empty external slots with the previous skip flags clear: selector 3, count 2045. */
    private static final String EMPTY_EXTERNAL_PASS = "0 11 " + bin(2045, 11);
    /** The same run one slot shorter, after the first external slot has been consumed. */
    private static final String EXTERNAL_TAIL_2044 = "0 11 " + bin(2044, 11);

    // ---------------------------------------------------------------- initial scene

    @Test public void initialSceneForIndexOneAloneMatchesTheShippedSinglePlayerScene() {
        Native950Viewport view = new Native950Viewport(1);
        Actor local = Actor.at(1, 3200, 3200, 0);
        Native950Packets.Packet scene = view.initialScene(world(local), NPC_BITS, AREA_TYPE, HASH1, HASH2);
        assertEquals(ServerPacket.REBUILD_NORMAL, scene.type());
        assertArrayEquals(Native950Packets.initialSinglePlayerScene(1, 3200, 3200, 0,
                NPC_BITS, AREA_TYPE, HASH1, HASH2).payload(), scene.payload());
    }

    @Test public void initialSceneKeepsTheStreamLengthForAnyAssignedIndex() {
        // 30 tile-hash bits + 2046 external 20-bit records, whichever slot the local player owns.
        byte[] shipped = Native950Packets.initialSinglePlayerScene(1, 3200, 3200, 0,
                NPC_BITS, AREA_TYPE, HASH1, HASH2).payload();
        for (int index : new int[] {1, 2, 1000, 2047}) {
            Native950Viewport view = new Native950Viewport(index);
            byte[] body = view.initialScene(world(Actor.at(index, 3200, 3200, 0)),
                    NPC_BITS, AREA_TYPE, HASH1, HASH2).payload();
            assertEquals("index " + index, shipped.length, body.length);
            // Only the local player's own slot is missing from the external records, and every
            // other slot is empty, so the whole stream is identical whatever the index is.
            assertArrayEquals("index " + index, shipped, body);
        }
    }

    @Test public void initialSceneCarriesALiveExternalPlayersRegionHashAndSpeed() {
        Native950Viewport view = new Native950Viewport(1);
        Actor local = Actor.at(1, 3200, 3200, 0);
        Actor other = Actor.builder(5, 4000, 4000, 1)
                .movementType(Native950PlayerInfo.MOVEMENT_RUN).build();
        byte[] body = view.initialScene(world(local, other), NPC_BITS, AREA_TYPE, HASH1, HASH2).payload();

        // Record layout (0x140125179 reads 20 bits; 0x140125190 takes (v>>18)&3 as the speed
        // table index; 0x140125280 rebuilds the tile from the low 18 bits).
        int expectedHash = Native950PlayerInfo.regionHash(4000, 4000, 1);
        assertEquals(62 + (62 << 8) + (1 << 16), expectedHash);
        assertEquals(expectedHash | (Native950PlayerInfo.MOVEMENT_RUN << 18), record(body, 5, 1));
        // Every other slot keeps the empty-slot constant the shipped writer emits.
        assertEquals(Native950PlayerInfo.EMPTY_SLOT_SPEED << 18, record(body, 4, 1));
        assertEquals(0xC0000, Native950PlayerInfo.EMPTY_SLOT_SPEED << 18);
        // The viewer's own record is the 30-bit tile hash, not a 20-bit slot.
        assertEquals(3200 + (3200 << 14), readBits(body, 0, 30));
        assertEquals(expectedHash, view.regionHash(5));
    }

    @Test public void aTeleportedExternalPlayerNeverGetsInitSpeedIndexZero() {
        // (record>>18)&3 is a table index into 0x140C9C888 (0x140125189/0x140125190/0x14012519E,
        // saved at 0x1401251C1). At 0x1401252BD..0x1401252DE the client compares that pointer
        // against entry 0 and stores sete into [record+0x27] - the byte the four movement passes
        // test (0x1401253DF / 0x1401254EF / 0x140125603 / 0x1401256F3) and the end-of-tick
        // rotation at 0x1401257FC refills from [record+0x26]. Index 0 would set that flag while
        // this encoder's slotFlags start at zero, so the slot would be encoded in one pass and
        // parsed in another. MOVEMENT_TELEPORT & 3 == 0, so it must not reach the record.
        assertEquals(0, Native950PlayerInfo.MOVEMENT_TELEPORT & 3);
        Native950Viewport view = new Native950Viewport(1);
        Actor local = Actor.at(1, 3200, 3200, 0);
        Actor teleported = Actor.builder(2, 3202, 3201, 0)
                .movementType(Native950PlayerInfo.MOVEMENT_TELEPORT).build();
        Actor[] table = world(local, teleported);
        byte[] body = view.initialScene(table, NPC_BITS, AREA_TYPE, HASH1, HASH2).payload();

        int hash = Native950PlayerInfo.regionHash(3202, 3201, 0);
        assertEquals(hash | (Native950PlayerInfo.EMPTY_SLOT_SPEED << 18), record(body, 2, 1));
        assertEquals("the region hash the record published is still the encoder's belief",
                hash, view.regionHash(2));
        // Every slot's flag agrees with what the client derives from a non-zero speed index.
        for (int index : new int[] {2, 5, 2047}) assertEquals("slot " + index, 0, view.slotFlags(index));

        // The next frame therefore encodes slot 2 in the pass the client will read it in: the
        // "previous skip clear" external pass, which opens this body's second byte with the add.
        assertArrayEquals(movement("0 00",
                        "1 00 0 " + bin(2, 6) + " " + bin(1, 6) + " 0 " + EXTERNAL_TAIL_2044),
                view.frame(table).payload());
        assertTrue(view.tracks(2));
    }

    // ---------------------------------------------------------------- index reuse

    @Test public void aReusedIndexIsRemovedBeforeItsNewOccupantIsAdded() {
        // EntityList hands out the lowest free index, so a slot a viewer holds can carry a
        // different character a tick later. Encoding that as the old player continuing to move
        // would leave the client's slot object alive forever, so a changed occupancy identity
        // is a removal: 1 (needs update) 0 (no mask) 00 (type 0, destroy at 0x140125906) and
        // then a clear region-hash bit, because the client's region record is untouched by the
        // removal and this viewer's belief about it has not changed.
        Actor viewer = Actor.builder(1, 3200, 3200, 0).identity(11).build();
        Actor first = Actor.builder(2, 3202, 3201, 0).identity(22).build();
        Native950Viewport view = initialised(1, viewer, first);
        view.frame(world(viewer, first));
        assertTrue(view.tracks(2));
        int believedHash = view.regionHash(2);

        Actor second = Actor.builder(2, 3203, 3203, 0).identity(23).build();
        // Pass 2 carries both local slots this tick (pass 1 is empty because both were written
        // last frame): the viewer's own "no update" plus a zero skip, then slot 2's removal.
        assertArrayEquals(movement("0 00 1 0 00 0", EXTERNAL_TAIL_2044),
                view.frame(world(viewer, second)).payload());
        assertFalse(view.tracks(2));
        assertFalse("the client destroyed the slot object, so its appearance cache died with it",
                view.hasCachedAppearance(2));
        assertEquals("the client's region record survives the removal", believedHash, view.regionHash(2));

        // Only the following frame adds the new occupant, from the external pass.
        assertArrayEquals(movement("0 00", EXTERNAL_TAIL_2044,
                        "1 00 0 " + bin(3203 & 0x3F, 6) + " " + bin(3203 & 0x3F, 6) + " 0"),
                view.frame(world(viewer, second)).payload());
        assertTrue(view.tracks(2));
    }

    @Test public void forgettingAFreedSlotKeepsTheRegionRecordTheClientStillHolds() {
        // The client's local-remove path (0x140125906..0x14012596A) clears only the entity
        // handle at [record+0x30]/[record+0x38]; the plane and region bytes the external parser
        // rewrites at 0x140126489 / 0x140126494 / 0x1401264A1 survive. So must this encoder's
        // mirror of them, or the next add at the index writes a delta against 0 while the client
        // adds it to the hash it kept.
        Actor viewer = Actor.at(1, 3200, 3200, 0);
        Actor other = Actor.at(2, 3202, 3201, 0);
        Native950Viewport view = initialised(1, viewer, other);
        view.frame(world(viewer, other));
        int believedHash = view.regionHash(2);
        assertEquals(Native950PlayerInfo.regionHash(3202, 3201, 0), believedHash);

        view.frame(world(viewer)); // the removal record goes out
        view.forget(2);            // the world freed the slot a tick later
        assertEquals("a freed slot keeps the region record the client did not destroy",
                believedHash, view.regionHash(2));
        assertFalse(view.hasCachedAppearance(2));

        // A new occupant in the same region therefore adds with the "hash unchanged" bit, not
        // with a delta the client would apply on top of the hash it still holds.
        Actor arrival = Actor.at(2, 3203, 3202, 0);
        assertArrayEquals(movement("0 00", EXTERNAL_TAIL_2044,
                        "1 00 0 " + bin(3203 & 0x3F, 6) + " " + bin(3202 & 0x3F, 6) + " 0"),
                view.frame(world(viewer, arrival)).payload());
    }

    @Test public void forgettingASlotKeepsTheIndexListsPairedWithTheLocalTable() {
        // forget() runs between frames, and rotate() - which normally rebuilds the two pass
        // lists - only runs at the end of one. An index left in localIndexes with a null
        // localActors entry made the next frame throw and cost the viewer its connection.
        Actor viewer = Actor.at(1, 3200, 3200, 0);
        Actor other = Actor.at(2, 3202, 3201, 0);
        Native950Viewport view = initialised(1, viewer, other);
        view.frame(world(viewer, other));
        assertEquals(2, view.trackedPlayers());
        view.forget(2); // deliberately before the removal record: the lists must stay paired
        assertEquals(1, view.trackedPlayers());
        assertFalse(view.tracks(2));
        // No throw, and the slot is now an ordinary external one again.
        assertArrayEquals(movement("0 00", EMPTY_EXTERNAL_PASS), view.frame(world(viewer)).payload());
    }

    // ---------------------------------------------------------------- pinned single-player frames

    @Test public void idleFrameReproducesTheVerifiedZeroSevenFFourBody() {
        // PLAYER_INFO.md: "An entirely unchanged player frame has movement body 00 7F F4".
        Native950Viewport view = initialised(1, Actor.at(1, 3200, 3200, 0));
        Native950Packets.Packet frame = view.frame(world(Actor.at(1, 3200, 3200, 0)));
        assertEquals(ServerPacket.PLAYER_INFO, frame.type());
        assertArrayEquals(hex("00 7f f4"), frame.payload());
        assertArrayEquals(Native950Packets.singlePlayerIdle().payload(), frame.payload());
    }

    @Test public void appearanceOnlyFrameReproducesTheVerifiedCZeroSevenFFourBody() {
        // The movement prefix C0 7F F4 is unchanged - the GPI bitstream is the same on 950 -
        // but the block after it is 00 00 20 ((N+128)&255) then N bytes each biased b ^ 0x80.
        // 947 used mask 0x04 and a raw body.
        byte[] appearance = {1, 2, 3, 4};
        Native950Viewport view = initialised(1, Actor.at(1, 3200, 3200, 0));
        byte[] body = view.frame(world(withAppearance(Actor.builder(1, 3200, 3200, 0), appearance))).payload();
        assertArrayEquals(hex("c0 7f f4 00 00 20 84 81 82 83 84"), body);
        assertArrayEquals(Native950Packets.singlePlayerAppearance(appearance).payload(), body);
    }

    @Test public void anUnchangedAppearanceIsSuppressedByThePerViewerMd5Cache() {
        byte[] appearance = {1, 2, 3, 4};
        Native950Viewport view = initialised(1, Actor.at(1, 3200, 3200, 0));
        assertFalse(view.hasCachedAppearance(1));
        view.frame(world(withAppearance(Actor.builder(1, 3200, 3200, 0), appearance)));
        assertTrue(view.hasCachedAppearance(1));
        // Same body, same hash: the second frame is the plain idle frame again.
        assertArrayEquals(hex("00 7f f4"),
                view.frame(world(withAppearance(Actor.builder(1, 3200, 3200, 0), appearance))).payload());
        // A changed body resends it.
        assertArrayEquals(hex("c0 7f f4 00 00 20 82 89 89"),
                view.frame(world(withAppearance(Actor.builder(1, 3200, 3200, 0), new byte[] {9, 9}))).payload());
    }

    @Test public void oneWalkStepReproducesTheShippedSinglePlayerWalkStepBytes() {
        int[][] steps = {{1, 0}, {0, 1}, {-1, 0}, {0, -1}, {1, 1}, {-1, -1}, {1, -1}, {-1, 1}};
        for (int[] step : steps) {
            int dx = step[0], dy = step[1];
            Native950Viewport view = initialised(1, Actor.at(1, 3200, 3200, 0));
            Actor moved = Actor.builder(1, 3200 + dx, 3200 + dy, 0)
                    .previous(3200, 3200, 0)
                    .moved(true)
                    .movementType(Native950PlayerInfo.MOVEMENT_WALK)
                    .build();
            assertArrayEquals("step " + dx + "," + dy,
                    Native950Packets.singlePlayerWalkStep(dx, dy).payload(),
                    view.frame(world(moved)).payload());
        }
    }

    @Test public void oneWalkStepWithAppearanceReproducesTheShippedCombinedFrame() {
        byte[] appearance = {7, 8};
        Native950Viewport view = initialised(1, Actor.at(1, 3200, 3200, 0));
        Actor moved = withAppearance(Actor.builder(1, 3201, 3200, 0)
                .previous(3200, 3200, 0)
                .moved(true)
                .movementType(Native950PlayerInfo.MOVEMENT_WALK), appearance);
        assertArrayEquals(Native950Packets.singlePlayerWalkStep(1, 0, appearance).payload(),
                view.frame(world(moved)).payload());
    }

    @Test public void aLongMoveUsesTheThirtyBitTeleportForm() {
        // 0x140125D42 reads one selector bit; 1 selects 3 speed bits + 30 position bits.
        Native950Viewport view = initialised(1, Actor.at(1, 3200, 3200, 0));
        Actor teleported = Actor.builder(1, 3300, 3200, 0)
                .previous(3200, 3200, 0)
                .moved(true)
                .movementType(Native950PlayerInfo.MOVEMENT_TELEPORT)
                .build();
        assertArrayEquals(movement("1 0 11 1 " + bin(4, 3) + " " + bin(100 << 14, 30),
                        EMPTY_EXTERNAL_PASS),
                view.frame(world(teleported)).payload());
    }

    // ---------------------------------------------------------------- compact walk and run

    @Test public void theWalkAndRunTablesAgreeWithTheNineTenDirectionHelpers() {
        // Two independent sources for the same sixteen-plus-eight rows: the client's own tables
        // (player-direction-jumptable.txt jump targets 0x140125B25..0x140125B5F, and the literal
        // cmp/jne chain at 0x140125C0C..0x140125CFA dumped in MOVEMENT_TABLES.md section 1) and
        // the 910 encoder's helpers, which the commented-out walk/run branch of
        // LocalPlayerUpdate.java:284-300 would have called. They must agree cell for cell.
        for (int dy = -2; dy <= 2; dy++) {
            for (int dx = -2; dx <= 2; dx++) {
                assertEquals("run " + dx + "," + dy, Utils.getPlayerRunningDirection(dx, dy),
                        Native950PlayerInfo.runDirection(dx, dy));
                if (dx < -1 || dx > 1 || dy < -1 || dy > 1) continue;
                assertEquals("walk " + dx + "," + dy, Utils.getPlayerWalkingDirection(dx, dy),
                        Native950PlayerInfo.walkDirection(dx, dy));
            }
        }
        // The run table is the perimeter of the 5x5 square: no interior entry, so a two-tile
        // move that turns a corner has no run encoding at all.
        assertEquals(-1, Native950PlayerInfo.runDirection(1, 0));
        assertEquals(-1, Native950PlayerInfo.runDirection(0, 0));
        assertEquals(-1, Native950PlayerInfo.walkDirection(0, 0));
        assertEquals(-1, Native950PlayerInfo.walkDirection(2, 0));
    }

    @Test public void theFirstLocalMoveKeepsTheAbsoluteFormBecauseTheSpeedTokenIsUnknown() {
        // The scene's 30 leading bits are a tile hash with no speed field, so nothing has told
        // the client what this slot's speed token is. The compact forms never write it
        // (0x140125B96 and 0x140125D30 only read slot+0x28), so the first move must be the
        // absolute form - which is exactly the shipped, client-verified walk step.
        Native950Viewport view = initialised(1, Actor.at(1, 3200, 3200, 0));
        assertEquals(Native950PlayerInfo.SPEED_TOKEN_UNKNOWN, view.speedToken(1));
        assertArrayEquals(Native950Packets.singlePlayerWalkStep(1, 0).payload(),
                view.frame(world(walk(1, 3200, 3200, 1, 0))).payload());
        // 0x140125E6D stores &table[(v>>12)&7] into slot+0x28, so the token is now known.
        assertEquals(Native950PlayerInfo.MOVEMENT_WALK, view.speedToken(1));
    }

    @Test public void everyWalkDirectionWritesTheVerifiedSelectorAndThreeBits() {
        int[][] steps = {{-1, -1}, {0, -1}, {1, -1}, {-1, 0}, {1, 0}, {-1, 1}, {0, 1}, {1, 1}};
        int[] expected = {0, 1, 2, 3, 4, 5, 6, 7};
        for (int i = 0; i < steps.length; i++) {
            int dx = steps[i][0], dy = steps[i][1];
            Native950Viewport view = initialised(1, Actor.at(1, 3200, 3200, 0));
            // One absolute step first, purely to set the client's speed token to the walk index.
            view.frame(world(walk(1, 3200, 3200, dx, dy)));
            assertEquals(Native950PlayerInfo.MOVEMENT_WALK, view.speedToken(1));
            // 0x1401259E2 selects form 1, 0x1401259E8 reads 3 direction bits, 0x1401259F8 reads
            // the extra-step flag. Nothing else is in the form.
            assertArrayEquals("step " + dx + "," + dy,
                    movement("1 0 " + bin(Native950PlayerInfo.FORM_WALK, 2) + " "
                            + bin(expected[i], 3) + " 0", EMPTY_EXTERNAL_PASS),
                    view.frame(world(walk(1, 3200 + dx, 3200 + dy, dx, dy))).payload());
            // The walk form does not write slot+0x28, so the token is unchanged.
            assertEquals(Native950PlayerInfo.MOVEMENT_WALK, view.speedToken(1));
        }
    }

    @Test public void everyRunDirectionWritesTheVerifiedSelectorAndFourBits() {
        int[][] steps = {
            {-2, -2}, {-1, -2}, {0, -2}, {1, -2}, {2, -2},
            {-2, -1}, {2, -1},
            {-2, 0}, {2, 0},
            {-2, 1}, {2, 1},
            {-2, 2}, {-1, 2}, {0, 2}, {1, 2}, {2, 2},
        };
        for (int direction = 0; direction < steps.length; direction++) {
            int dx = steps[direction][0], dy = steps[direction][1];
            Native950Viewport view = initialised(1, Actor.at(1, 3200, 3200, 0));
            view.frame(world(run(1, 3200, 3200, dx, dy)));
            assertEquals(Native950PlayerInfo.MOVEMENT_RUN, view.speedToken(1));
            // 0x140125BA8 selects form 2, 0x140125BB1 reads 4 direction bits, and there is no
            // extra-step flag on this form.
            assertArrayEquals("step " + dx + "," + dy,
                    movement("1 0 " + bin(Native950PlayerInfo.FORM_RUN, 2) + " "
                            + bin(direction, 4), EMPTY_EXTERNAL_PASS),
                    view.frame(world(run(1, 3200 + dx, 3200 + dy, dx, dy))).payload());
            assertEquals(Native950PlayerInfo.MOVEMENT_RUN, view.speedToken(1));
        }
    }

    @Test public void theExtraStepFlagIsWrittenAndAlwaysClear() {
        // The flag exists in the form and must be emitted, but never set: setting it queues a
        // SECOND position (base + cardinal from 2 further bits at 0x140125A6C) and forces the
        // slot to speed table entry 3 (0x140125A78/0x140125A7F). Ataraxia never produces that
        // case - Entity.processMovement:1533-1538 refuses a second run step whose combined delta
        // has no run-table entry - so the form is emitted with the flag clear and no sub-step.
        Native950Viewport view = initialised(1, Actor.at(1, 3200, 3200, 0));
        view.frame(world(walk(1, 3200, 3200, 1, 0)));
        byte[] body = view.frame(world(walk(1, 3201, 3200, 1, 0))).payload();
        assertEquals("needs-update, no mask, form 1", "1001", bitString(body, 0, 4));
        assertEquals("direction 4 = (+1, 0)", bin(4, 3), bitString(body, 4, 3));
        assertEquals("the extra-step flag", "0", bitString(body, 7, 1));
        // The whole local pass is exactly 8 bits, so the external pass starts on byte 1.
        assertArrayEquals(movement("1 0 01 " + bin(4, 3) + " 0", EMPTY_EXTERNAL_PASS), body);
    }

    @Test public void aTwoTileMovePicksRunAndAOneTileMovePicksWalk() {
        Native950Viewport view = initialised(1, Actor.at(1, 3200, 3200, 0));
        // Prime the token to the run index, then take a genuine two-tile step.
        view.frame(world(run(1, 3200, 3200, 2, 0)));
        assertArrayEquals("a two-tile step is the run form",
                movement("1 0 10 " + bin(8, 4), EMPTY_EXTERNAL_PASS),
                view.frame(world(run(1, 3202, 3200, 2, 0))).payload());

        // The same session, now walking: Player.getMovementType() drops back to the walk index,
        // which no longer matches the token, so the absolute form re-states the speed once.
        byte[] absolute = view.frame(world(walk(1, 3204, 3200, 1, 0))).payload();
        assertEquals("form 3", "11", bitString(absolute, 2, 2));
        assertEquals(Native950PlayerInfo.MOVEMENT_WALK, view.speedToken(1));
        assertArrayEquals("and the next one-tile step is the walk form",
                movement("1 0 01 " + bin(4, 3) + " 0", EMPTY_EXTERNAL_PASS),
                view.frame(world(walk(1, 3205, 3200, 1, 0))).payload());
    }

    @Test public void anUnresolvedDirectionFallsBackToTheAbsoluteForm() {
        // A running player that only advanced one tile this tick: Chebyshev 1 is not in the run
        // table (its interior is empty) and the walk form would render at the wrong speed, so
        // there is no compact encoding and the absolute form carries the move.
        Native950Viewport view = initialised(1, Actor.at(1, 3200, 3200, 0));
        view.frame(world(run(1, 3200, 3200, 2, 0)));
        assertEquals(-1, Native950PlayerInfo.runDirection(1, 0));
        byte[] shortForm = view.frame(world(run(1, 3202, 3200, 1, 0))).payload();
        assertEquals("form 3, short", "11", bitString(shortForm, 2, 2));
        assertEquals("short-form selector", "0", bitString(shortForm, 4, 1));
        assertArrayEquals(movement("1 0 11 0 "
                        + bin((0 + (1 << 5)) | (Native950PlayerInfo.MOVEMENT_RUN << 12), 15),
                        EMPTY_EXTERNAL_PASS),
                shortForm);

        // A three-tile step has no entry in either table either, whatever the speed.
        Native950Viewport walker = initialised(1, Actor.at(1, 3200, 3200, 0));
        walker.frame(world(walk(1, 3200, 3200, 1, 0)));
        assertEquals(Native950PlayerInfo.MOVEMENT_WALK, walker.speedToken(1));
        byte[] threeTiles = walker.frame(world(walk(1, 3201, 3200, 3, 0))).payload();
        assertEquals("form 3", "11", bitString(threeTiles, 2, 2));
    }

    @Test public void aPlaneChangeCannotUseACompactForm() {
        // Neither compact form touches the record's plane: the walk form re-writes the current
        // one at 0x140125B6D and the run form at 0x140125D16, so a plane offset has no compact
        // encoding at all.
        Native950Viewport view = initialised(1, Actor.at(1, 3200, 3200, 0));
        view.frame(world(walk(1, 3200, 3200, 1, 0)));
        Actor climbed = Actor.builder(1, 3202, 3200, 1).previous(3201, 3200, 0)
                .moved(true).movementType(Native950PlayerInfo.MOVEMENT_WALK).build();
        assertEquals("form 3", "11", bitString(view.frame(world(climbed)).payload(), 2, 2));
    }

    @Test public void aTeleportNeverUsesACompactForm() {
        // Speed index 4 is the only value the client address-compares (0x140125E26 short,
        // 0x140125F59 long) and answers with an instant reposition through vtable+0x160. Only
        // the absolute forms carry a speed field, so a one-tile teleport must still use one.
        Native950Viewport view = initialised(1, Actor.at(1, 3200, 3200, 0));
        view.frame(world(walk(1, 3200, 3200, 1, 0)));
        assertEquals(Native950PlayerInfo.MOVEMENT_WALK, view.speedToken(1));
        Actor blinked = Actor.builder(1, 3202, 3200, 0).previous(3201, 3200, 0)
                .moved(true).movementType(Native950PlayerInfo.MOVEMENT_TELEPORT).build();
        byte[] body = view.frame(world(blinked)).payload();
        assertArrayEquals(movement("1 0 11 0 "
                        + bin((0 + (1 << 5)) | (Native950PlayerInfo.MOVEMENT_TELEPORT << 12), 15),
                        EMPTY_EXTERNAL_PASS),
                body);
        // 0x140125E43 resets the short form's token to &table[0] on the teleport branch.
        assertEquals(0, view.speedToken(1));
    }

    @Test public void runningWithAnAppearanceChangeStillEmitsOneCombinedFrame() {
        byte[] first = {7, 8};
        byte[] changed = {9};
        Native950Viewport view = initialised(1, Actor.at(1, 3200, 3200, 0));
        // Frame 1 primes the speed token and caches the first appearance body.
        view.frame(world(withAppearance(Actor.builder(1, 3202, 3200, 0)
                .previous(3200, 3200, 0).moved(true)
                .movementType(Native950PlayerInfo.MOVEMENT_RUN), first)));
        assertTrue(view.hasCachedAppearance(1));

        byte[] body = view.frame(world(withAppearance(Actor.builder(1, 3204, 3200, 0)
                .previous(3202, 3200, 0).moved(true)
                .movementType(Native950PlayerInfo.MOVEMENT_RUN), changed))).payload();
        // One frame: the run form with the pending-mask bit set, then the one mask block.
        assertArrayEquals(concat(
                        movement("1 1 " + bin(Native950PlayerInfo.FORM_RUN, 2) + " " + bin(8, 4),
                                EMPTY_EXTERNAL_PASS),
                        appearanceBlock(changed)),
                body);
    }

    @Test public void anExternalSlotsSpeedTokenComesFromItsInitRecord() {
        // The 20-bit init record's top two bits are the speed index the client stores at
        // slot+0x28 (0x140125190 / 0x140125220), so an external player's very first compact form
        // is legal without an absolute frame first.
        Actor local = Actor.at(1, 3200, 3200, 0);
        Actor other = Actor.builder(2, 3202, 3200, 0)
                .movementType(Native950PlayerInfo.MOVEMENT_WALK).build();
        Native950Viewport view = new Native950Viewport(1);
        view.initialScene(world(local, other), NPC_BITS, AREA_TYPE, HASH1, HASH2);
        assertEquals(Native950PlayerInfo.MOVEMENT_WALK, view.speedToken(2));
        assertEquals("an empty slot carries the pinned constant",
                Native950PlayerInfo.EMPTY_SLOT_SPEED, view.speedToken(3));
        assertEquals("the viewer's own slot has no record at all",
                Native950PlayerInfo.SPEED_TOKEN_UNKNOWN, view.speedToken(1));

        // Frame 1 adds slot 2; frame 2 is its first local move and may go straight to form 1.
        view.frame(world(local, other));
        assertTrue(view.tracks(2));
        byte[] body = view.frame(world(local, walk(2, 3202, 3200, 0, 1))).payload();
        // Pass 1 holds the viewer (no update, skip 0) then slot 2's walk form.
        assertEquals("viewer: no update, skip selector 0", "000", bitString(body, 0, 3));
        assertEquals("slot 2: needs update, no mask, form 1", "1001", bitString(body, 3, 4));
        assertEquals("direction 6 = (0, +1)", bin(6, 3), bitString(body, 7, 3));
        assertEquals("the extra-step flag", "0", bitString(body, 10, 1));
    }

    @Test public void aStrangerWhoArrivesRunningKeepsTheEmptySlotsOwnSpeedToken() {
        // The stranger's slot is EMPTY in the initial scene, so its 20-bit record carries
        // EMPTY_SLOT_SPEED and the client stores &table[EMPTY_SLOT_SPEED] into slot+0x28 by the
        // same init store (0x140125220) that gives an occupied slot its token. EMPTY_SLOT_SPEED
        // and the run index are the same number, so this is the one path on which the compact
        // run form can be chosen for a player's very first step; every shipped byte anchor
        // covers a session's own first frames instead, so nothing else exercises it.
        //
        // Two independent facts make it legal. (1) An empty slot's cached region hash is 0, so
        // the add ALWAYS emits a region-hash update, and for any real distance that is the
        // type-3 form, whose 20 bits carry the speed index and whose client store at
        // 0x14012647D writes slot+0x28 - the token is sent, not assumed. (2) When no update is
        // emitted the token is still the one the client holds: the removal path (0x140125966)
        // zeroes only slot+0x30 and slot+0x38, and none of the three add-path helpers writes
        // the slot record's +0x28 - 0x14012ADE0's only [reg+0x28] store is inside the
        // 0x38-stride loop it initialises at actor+0x10D0; 0x1401305F0 is a byte-stream reader
        // (its argument's +0x68 is a buffer base and +0x70 a cursor) whose +0x28 store is a
        // DWORD while the slot record's +0x28 is the QWORD pointer 0x140125220 writes; and
        // 0x14012F0A0 touches no +0x28 at all.
        assertEquals("the empty-slot record and the run index are the same value",
                Native950PlayerInfo.MOVEMENT_RUN, Native950PlayerInfo.EMPTY_SLOT_SPEED);
        Actor local = Actor.at(1, 3200, 3200, 0);
        Native950Viewport view = initialised(1, local);
        assertEquals(Native950PlayerInfo.EMPTY_SLOT_SPEED, view.speedToken(2));

        // Frame 1 adds the stranger, who was not in the scene at all.
        Actor stranger = Actor.builder(2, 3202, 3200, 0)
                .movementType(Native950PlayerInfo.MOVEMENT_RUN).build();
        view.frame(world(local, stranger));
        assertTrue(view.tracks(2));
        assertEquals("the add's type-3 region-hash update carries the same index",
                Native950PlayerInfo.MOVEMENT_RUN, view.speedToken(2));

        // Frame 2 is its first step, and it is a two-tile run: form 2 plus four direction bits.
        byte[] body = view.frame(world(local, run(2, 3202, 3200, 2, 0))).payload();
        assertEquals("viewer: no update, skip selector 0", "000", bitString(body, 0, 3));
        assertEquals("slot 2: needs update, no mask, form 2", "1010", bitString(body, 3, 4));
        assertEquals("direction 8 = (+2, 0)", bin(8, 4), bitString(body, 7, 4));
        assertEquals(Native950PlayerInfo.MOVEMENT_RUN, view.speedToken(2));
    }

    @Test public void aSpeedIndexOutsideTheClientsTableIsRefused() {
        // The table at 0x140C9C888 is five int32 entries and 0x140C9C89C is an unrelated OggS
        // datum, so 5, 6 and 7 would make the client form a pointer into foreign memory.
        assertEquals(4, Native950PlayerInfo.MAX_SPEED_INDEX);
        rejects(() -> Actor.builder(1, 1, 1, 0).movementType(5));
        rejects(() -> Actor.builder(1, 1, 1, 0).movementType(7));
        rejects(() -> Actor.builder(1, 1, 1, 0).movementType(-1));
    }

    // ---------------------------------------------------------------- two sessions

    @Test public void twoPlayersInViewOfEachOtherAddOnBothSides() {
        Actor one = withAppearance(Actor.builder(1, 3200, 3200, 0), new byte[] {1});
        Actor two = withAppearance(Actor.builder(2, 3202, 3201, 0), new byte[] {2});
        Actor[] world = world(one, two);

        Native950Viewport a = new Native950Viewport(1);
        Native950Viewport b = new Native950Viewport(2);
        a.initialScene(world, NPC_BITS, AREA_TYPE, HASH1, HASH2);
        b.initialScene(world, NPC_BITS, AREA_TYPE, HASH1, HASH2);

        // Both records already agree with the scene's 20-bit init entry, so the add carries the
        // "region hash unchanged" bit and no delta (0x140126000 type 0, then 6 X and 6 Y bits).
        byte[] expectedA = concat(
                movement("1 1 00",
                        "1 00 0 " + bin(2, 6) + " " + bin(1, 6) + " 1 " + EXTERNAL_TAIL_2044),
                appearanceBlock(new byte[] {1}), appearanceBlock(new byte[] {2}));
        assertArrayEquals(expectedA, a.frame(world).payload());

        byte[] expectedB = concat(
                movement("1 1 00",
                        "1 00 0 " + bin(0, 6) + " " + bin(0, 6) + " 1 " + EXTERNAL_TAIL_2044),
                appearanceBlock(new byte[] {2}), appearanceBlock(new byte[] {1}));
        assertArrayEquals(expectedB, b.frame(world).payload());

        assertTrue(a.tracks(2));
        assertTrue(b.tracks(1));
        assertEquals(2, a.trackedPlayers());
        assertEquals(1, a.addedLastFrame());
    }

    @Test public void anAddWithNoMaskContentClearsThePendingMaskBit() {
        // Nothing forces a mask block onto an add: the client reads one bit at 0x140126056 and
        // only registers a pending index when it is set. Emitting less is always safe.
        Actor one = Actor.at(1, 3200, 3200, 0);
        Actor two = Actor.at(2, 3200, 3200, 0);
        Native950Viewport a = initialised(1, one, two);
        assertArrayEquals(movement("0 00",
                        "1 00 0 " + bin(0, 6) + " " + bin(0, 6) + " 0 " + EXTERNAL_TAIL_2044),
                a.frame(world(one, two)).payload());
    }

    @Test public void regionHashDeltasUseTheThreeVerifiedSelectors() {
        // Selector 1 (0x14012631B, 2 plane bits): same region, plane +1.
        assertEquals("01 01", regionDelta(3202, 3201, 1, 3202, 3201, 0, 1));
        // Selector 2 (0x140126349, 5 bits plane<<3 | opcode): the player arrives one region east
        // of where the scene recorded it, so the delta opcode is 4 (dx +1, dy 0).
        assertEquals("10 " + bin(4, 5), regionDelta(3202, 3201, 0, 3138, 3200, 0, 0));
        // Selector 3 (0x14012641F, 20 bits y | x<<8 | plane<<16 | speed<<18): twelve regions away.
        assertEquals("11 " + bin((-12 & 0xFF) + ((-12 & 0xFF) << 8)
                        + (Native950PlayerInfo.MOVEMENT_WALK << 18), 20),
                regionDelta(3202, 3201, 0, 4000, 4000, 0, 0));
    }

    @Test public void aPlayerLeavingViewProducesARemove() {
        Actor one = Actor.at(1, 3200, 3200, 0);
        Actor twoNear = Actor.at(2, 3210, 3200, 0);
        Native950Viewport a = initialised(1, one, twoNear);
        a.frame(world(one, twoNear));
        assertTrue(a.tracks(2));

        // Out of the 24-tile radius: local pass writes 1 (update) 0 (no mask) 00 (type 0 remove),
        // then the region-hash flag; the previous tile was one region west of the new one.
        Actor twoFar = Actor.builder(2, 3300, 3200, 0).previous(3210, 3200, 0).build();
        byte[] body = a.frame(world(one, twoFar)).payload();
        assertArrayEquals(movement("",
                        "0 00 1 0 00 1 10 " + bin(4, 5),
                        "0 11 " + bin(2044, 11),
                        ""),
                body);
        assertFalse(a.tracks(2));
        assertFalse("a re-add must resend the appearance the destroyed slot cached",
                a.hasCachedAppearance(2));
        assertEquals(1, a.trackedPlayers());
    }

    // ---------------------------------------------------------------- skip runs

    @Test public void skipRunsCollapseAtTheFiveEightAndElevenBitBoundaries() {
        // The run counts the SUBSEQUENT eligible slots, so a visible player parked at index N
        // ends a run of N-3 slots that started at index 3.
        assertSkipRun(3, "00");                        // run 0: no count bits at all
        assertSkipRun(4, "01 " + bin(1, 5));           // run 1
        assertSkipRun(34, "01 " + bin(31, 5));         // run 31: the last 5-bit count
        assertSkipRun(35, "10 " + bin(32, 8));         // run 32: first 8-bit count
        assertSkipRun(258, "10 " + bin(255, 8));       // run 255: the last 8-bit count
        assertSkipRun(259, "11 " + bin(256, 11));      // run 256: first 11-bit count
    }

    // ---------------------------------------------------------------- mask composition

    @Test public void maskHeaderSelectsTheExtensionMarkersForBitsAboveEachByte() {
        // Every block is composed through Native950PlayerMasks, so 950's markers - 0x10 for
        // byte 2, 0x8000 for byte 3, 0x40000 for byte 4 - are ORed in by the same header writer.
        // faceAngle is 0x2: one mask byte, no marker.
        assertArrayEquals(hex("00 00 02 20 00"),
                maskBlockOf(() -> Native950PlayerMasks.builder().faceAngle(8192)));
        // forceTalk is 0x400000: byte 3, which drags the byte-2 marker in with it.
        assertArrayEquals(hex("00 00 10 80 40 48 69 00 01"),
                maskBlockOf(() -> Native950PlayerMasks.builder().forceTalk("Hi", 1)));
        // The 947 version of this test finished with a spotanim block for the byte-4 marker.
        // Spotanims are refused on 950 until their byte transform is derived, so that row moves
        // to Native950PlayerMasksTest's refusal set and its coverage returns with the block.
    }

    @Test public void appearanceJoinsTheOtherMasksInTheClientConsumptionOrder() {
        // 950 order still puts appearance before faceAngle, but the bits are 0x20 and 0x2, so
        // the header is a single byte 0x22 rather than 947's 0x84.
        assertArrayEquals(hex("00 00 22 82 85 86 20 00"),
                maskBlockOf(new byte[] {5, 6},
                        () -> Native950PlayerMasks.builder().faceAngle(8192)));
    }

    @Test public void aCandidateMaskIsRefusedAndNamesTheEvidenceGap() {
        Actor actor = Actor.builder(1, 3200, 3200, 0)
                .masks(() -> Native950PlayerMasks.builder().headIcons(new byte[] {1, 2, 3}))
                .build();
        Native950Viewport view = initialised(1, actor);
        try {
            view.frame(world(actor));
            fail("A CANDIDATE mask reached the wire");
        } catch (UnsupportedOperationException expected) {
            assertTrue(expected.getMessage(), expected.getMessage().contains("0x1000"));
            assertTrue(expected.getMessage(), expected.getMessage().contains("player-masks-950-derived.md"));
        }
    }

    // ---------------------------------------------------------------- guards

    @Test public void aFrameBeforeTheSceneIsRefused() {
        Native950Viewport view = new Native950Viewport(1);
        try {
            view.frame(world(Actor.at(1, 3200, 3200, 0)));
            fail("A frame was built before the scene");
        } catch (IllegalStateException expected) {
            assertTrue(expected.getMessage(), expected.getMessage().contains("initialScene"));
        }
    }

    @Test public void aMissingLocalPlayerIsRefused() {
        Native950Viewport view = initialised(1, Actor.at(1, 3200, 3200, 0));
        try {
            view.frame(world());
            fail("A frame was built without the local player");
        } catch (IllegalStateException expected) {
            assertTrue(expected.getMessage(), expected.getMessage().contains("index 1"));
        }
    }

    @Test public void theViewportRefusesAnyThreadButTheOneThatOwnsIt() throws Exception {
        final Native950Viewport view = new Native950Viewport(1, new Thread("not-this-one"));
        final Throwable[] caught = new Throwable[1];
        Thread other = new Thread(() -> {
            try { view.frame(world(Actor.at(1, 3200, 3200, 0))); }
            catch (Throwable failure) { caught[0] = failure; }
        });
        other.start();
        other.join();
        assertTrue(String.valueOf(caught[0]), caught[0] instanceof IllegalStateException);
        assertTrue(caught[0].getMessage(), caught[0].getMessage().contains("world-thread state"));
    }

    @Test public void anOutOfRangeIndexIsRefused() {
        rejects(() -> new Native950Viewport(0));
        rejects(() -> new Native950Viewport(2048));
        rejects(() -> Actor.at(0, 1, 1, 0));
        rejects(() -> Actor.at(1, 16384, 1, 0));
        rejects(() -> Actor.builder(1, 1, 1, 0).movementType(8));
        rejects(() -> Actor.builder(1, 1, 1, 0).appearance(new byte[0]));
        rejects(() -> Actor.builder(1, 1, 1, 0).appearance(new byte[256]));
    }

    // ---------------------------------------------------------------- fixtures

    private static Native950Viewport initialised(int localIndex, Actor... actors) {
        Native950Viewport view = new Native950Viewport(localIndex);
        view.initialScene(world(actors), NPC_BITS, AREA_TYPE, HASH1, HASH2);
        return view;
    }

    private static Actor withAppearance(Actor.Builder builder, byte[] appearance) {
        return builder.appearance(appearance).build();
    }

    /**
     * A player that walked from {@code (fromX, fromY)} by {@code (dx, dy)} this tick: the 910
     * {@code hasTeleported() || getNextWalkDirection() != -1} moved flag with
     * {@code getMovementType() == WALK}.
     */
    private static Actor walk(int index, int fromX, int fromY, int dx, int dy) {
        return step(index, fromX, fromY, dx, dy, Native950PlayerInfo.MOVEMENT_WALK);
    }

    /** The same for a running player, whose {@code getMovementType()} is the run index. */
    private static Actor run(int index, int fromX, int fromY, int dx, int dy) {
        return step(index, fromX, fromY, dx, dy, Native950PlayerInfo.MOVEMENT_RUN);
    }

    private static Actor step(int index, int fromX, int fromY, int dx, int dy, int movementType) {
        return Actor.builder(index, fromX + dx, fromY + dy, 0)
                .previous(fromX, fromY, 0)
                .moved(true)
                .movementType(movementType)
                .build();
    }

    /** The mask block one actor's masks produce inside a real frame, skipped bytes included. */
    private static byte[] maskBlockOf(Native950PlayerInfo.MaskSource masks) {
        return maskBlockOf(null, masks);
    }

    private static byte[] maskBlockOf(byte[] appearance, Native950PlayerInfo.MaskSource masks) {
        Actor.Builder builder = Actor.builder(1, 3200, 3200, 0).masks(masks);
        if (appearance != null) builder.appearance(appearance);
        Actor actor = builder.build();
        Native950Viewport view = initialised(1, actor);
        byte[] body = view.frame(world(actor)).payload();
        // Movement section for a mask-only local update in the initial state: C0 7F F4.
        byte[] prefix = hex("c0 7f f4");
        for (int i = 0; i < prefix.length; i++) assertEquals(prefix[i], body[i]);
        byte[] block = new byte[body.length - prefix.length];
        System.arraycopy(body, prefix.length, block, 0, block.length);
        return block;
    }

    /** The bit string of the region-hash delta an add emits after the player moved regions. */
    private static String regionDelta(int viewerX, int viewerY, int viewerPlane,
            int initialX, int initialY, int initialPlane, int finalPlane) {
        Actor viewer = Actor.at(1, viewerX, viewerY, viewerPlane);
        Native950Viewport view = new Native950Viewport(1);
        view.initialScene(world(viewer, Actor.at(2, initialX, initialY, initialPlane)),
                NPC_BITS, AREA_TYPE, HASH1, HASH2);
        Actor arrived = Actor.at(2, viewerX, viewerY, finalPlane);
        byte[] body = view.frame(world(viewer, arrived)).payload();
        // Pass 1 is one byte (no local update, skip 0); pass 4 starts at bit 8 with the add's
        // "needs update" bit, the 2-bit type 0 and the "region hash changed" bit.
        assertEquals("1001", bitString(body, 8, 4));
        String selector = bitString(body, 12, 2);
        return selector + " " + bitString(body, 14, deltaWidth(selector));
    }

    private static int deltaWidth(String selector) {
        if (selector.equals("01")) return 2;
        if (selector.equals("10")) return 5;
        if (selector.equals("11")) return 20;
        throw new AssertionError("Selector 00 is not a region-hash delta");
    }

    private static void assertSkipRun(int breakerIndex, String expectedBits) {
        Actor local = Actor.at(1, 3200, 3200, 0);
        Actor breaker = Actor.at(breakerIndex, 3200, 3200, 0);
        Actor[] table = world(local, breaker);
        Native950Viewport view = new Native950Viewport(1);
        view.initialScene(table, NPC_BITS, AREA_TYPE, HASH1, HASH2);
        byte[] body = view.frame(table).payload();
        String expected = expectedBits.replace(" ", "");
        // Pass 1 fills byte 0. Pass 4 opens with slot 2's "no update" bit, then the skip run.
        assertEquals("run to index " + breakerIndex, "0", bitString(body, 8, 1));
        assertEquals("run to index " + breakerIndex, expected, bitString(body, 9, expected.length()));
    }

    // ---------------------------------------------------------------- helpers

    private static Actor[] world(Actor... actors) {
        Actor[] table = new Actor[Native950PlayerInfo.SLOTS];
        for (Actor actor : actors) table[actor.index] = actor;
        return table;
    }

    /** The 2 zero bytes, the 0x4 header and the (N+128)&255 length the appearance block carries. */
    /**
     * The 950 appearance block: two skipped bytes, mask 0x20, length+128, then the body biased
     * b ^ 0x80. Written out by hand here rather than delegated to Native950PlayerMasks so these
     * frames stay an independent check on the encoder rather than a restatement of it.
     */
    private static byte[] appearanceBlock(byte[] appearance) {
        byte[] block = new byte[4 + appearance.length];
        block[2] = 0x20;
        block[3] = (byte) (appearance.length + 128);
        for (int i = 0; i < appearance.length; i++) block[4 + i] = (byte) (appearance[i] ^ 0x80);
        return block;
    }

    /** Each argument is one movement pass, padded to a byte boundary on its own. */
    private static byte[] movement(String... passes) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        for (String pass : passes) {
            byte[] bytes = bits(pass);
            out.write(bytes, 0, bytes.length);
        }
        return out.toByteArray();
    }

    private static byte[] bits(String pattern) {
        String compact = pattern.replace(" ", "");
        byte[] out = new byte[(compact.length() + 7) / 8];
        for (int i = 0; i < compact.length(); i++) {
            char bit = compact.charAt(i);
            if (bit != '0' && bit != '1') throw new IllegalArgumentException("Not a bit: " + bit);
            if (bit == '1') out[i >>> 3] |= 1 << (7 - (i & 7));
        }
        return out;
    }

    private static String bin(int value, int width) {
        StringBuilder text = new StringBuilder(width);
        for (int bit = width - 1; bit >= 0; bit--) text.append((value >>> bit) & 1);
        return text.toString();
    }

    private static String bitString(byte[] body, int offset, int count) {
        StringBuilder text = new StringBuilder(count);
        for (int i = 0; i < count; i++) {
            int bit = offset + i;
            text.append((body[bit >>> 3] >> (7 - (bit & 7))) & 1);
        }
        return text.toString();
    }

    private static int readBits(byte[] body, int offset, int count) {
        int value = 0;
        for (int i = 0; i < count; i++) {
            int bit = offset + i;
            value = (value << 1) | ((body[bit >>> 3] >> (7 - (bit & 7))) & 1);
        }
        return value;
    }

    /** The 20-bit init record for {@code index} in a scene whose local player is {@code local}. */
    private static int record(byte[] body, int index, int local) {
        int slot = index < local ? index - 1 : index - 2;
        return readBits(body, 30 + slot * 20, 20);
    }

    private static byte[] concat(byte[]... parts) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        for (byte[] part : parts) out.write(part, 0, part.length);
        return out.toByteArray();
    }

    private static byte[] hex(String value) {
        String compact = value.replace(" ", "");
        byte[] bytes = new byte[compact.length() / 2];
        for (int i = 0; i < bytes.length; i++)
            bytes[i] = (byte) Integer.parseInt(compact.substring(i * 2, i * 2 + 2), 16);
        return bytes;
    }

    private static void rejects(Runnable action) {
        try { action.run(); fail("Invalid input accepted"); }
        catch (IllegalArgumentException expected) { }
    }
}
