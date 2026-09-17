package modern947;

import com.rs.game.player.client.Native950Save;
import com.rs.game.player.client.Native950SaveStore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Map;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import static org.junit.Assert.*;

public class Native950SaveStoreTest {
    @Rule public final TemporaryFolder temporary = new TemporaryFolder();

    @Test public void roundTripAndAtomicReplacementPreserveAllFieldsAndSlotOrder() throws Exception {
        Path directory = temporary.getRoot().toPath().resolve("characters");
        Native950SaveStore store = new Native950SaveStore(directory);
        assertNull(store.load("Alice"));
        assertFalse(Files.exists(directory));
        Native950Save first = sample("Alice", 3200, 1000);
        store.save(first);
        assertSaveEquals(first, store.load("alice"));
        Path profile = onlyFile(directory);
        byte[] firstBytes = Files.readAllBytes(profile);
        Native950Save second = sample("ALICE", 3215, 990);
        store.save(second);
        assertEquals(profile, onlyFile(directory));
        assertFalse(Arrays.equals(firstBytes, Files.readAllBytes(profile)));
        assertSaveEquals(second, new Native950SaveStore(directory).load("  Alice  "));
    }

    @Test public void canonicalAliasesShareOneHashFilenameAndOtherProfilesAreIndependent() throws Exception {
        Path directory = temporary.newFolder().toPath();
        Native950SaveStore store = new Native950SaveStore(directory);
        store.save(sample(" Alice__Bob ", 3200, 1000));
        assertEquals("alice bob", store.load("ALICE BOB").username());
        assertEquals("alice bob", store.load("alice_bob").username());
        assertTrue(onlyFile(directory).getFileName().toString().matches("[a-f0-9]{64}\\.950"));
        store.save(sample("other", 3201, 999));
        assertEquals(3200, store.load("alice bob").x());
        assertEquals(3201, store.load("other").x());
        assertEquals(2, files(directory).size());
    }

    @Test public void unsafeOrEmptyNamesAreRejectedWithoutCreatingFiles() throws Exception {
        Path directory = temporary.getRoot().toPath().resolve("absent");
        Native950SaveStore store = new Native950SaveStore(directory);
        for (String name : new String[] {"", "   ", "___", "---", "../alice", "..\\alice", "C:\\alice", "a/b", "a:b", "a\nb", "a\u0000b", "alice\u0000", "\talice", "caf\u00e9", "1234567890123"}) {
            expectIllegal(() -> store.load(name));
        }
        assertFalse(Files.exists(directory));
    }

    @Test public void snapshotsDefendAgainstCallerMutationAndSupportIntegerLimitAmounts() {
        int[] ids = emptyIds(), amounts = new int[28], bankIds = {1511}, bankAmounts = {Integer.MAX_VALUE};
        ids[27] = 995; amounts[27] = Integer.MAX_VALUE;
        Native950Save save = new Native950Save("alice", 16383, 0, 3, ids, amounts, bankIds, bankAmounts);
        ids[27] = -1; amounts[27] = 0; bankIds[0] = 315; bankAmounts[0] = 1;
        save.inventoryIds()[27] = -1; save.inventoryAmounts()[27] = 0;
        save.bankIds()[0] = 315; save.bankAmounts()[0] = 1;
        assertEquals(995, save.inventoryIds()[27]);
        assertEquals(Integer.MAX_VALUE, save.inventoryAmounts()[27]);
        assertArrayEquals(new int[] {1511}, save.bankIds());
        assertArrayEquals(new int[] {Integer.MAX_VALUE}, save.bankAmounts());
        assertArrayEquals(emptyEquipmentIds(), save.equipmentIds());
        assertArrayEquals(new int[19], save.equipmentAmounts());
        assertFalse(save.equipmentKitClaimed());
    }

    @Test public void equipmentAndClaimRoundTripAndDefendAgainstCallerMutation() throws Exception {
        int[] ids = emptyEquipmentIds(), amounts = new int[19];
        ids[0] = 1155; amounts[0] = 1;
        ids[3] = 1277; amounts[3] = 1;
        ids[5] = 1189; amounts[5] = 1;
        Native950Save base = sample("alice", 3217, 2);
        Native950Save equipped = new Native950Save(base.username(), base.x(), base.y(), base.plane(),
                base.inventoryIds(), base.inventoryAmounts(), base.bankIds(), base.bankAmounts(), ids, amounts, true);
        ids[0] = -1; amounts[0] = 0;
        equipped.equipmentIds()[3] = -1; equipped.equipmentAmounts()[3] = 0;
        assertEquals(1155, equipped.equipmentIds()[0]);
        assertEquals(1277, equipped.equipmentIds()[3]);
        assertEquals(1, equipped.equipmentAmounts()[0]);
        assertEquals(1, equipped.equipmentAmounts()[3]);
        Path directory = temporary.newFolder().toPath();
        Native950SaveStore store = new Native950SaveStore(directory);
        store.save(equipped);
        assertEquals(Native950Save.SCHEMA_VERSION, ByteBuffer.wrap(Files.readAllBytes(onlyFile(directory))).getInt(8)); // P4: schema 3
        assertSaveEquals(equipped, new Native950SaveStore(directory).load("alice"));
        store.save(base);
        assertSaveEquals(base, store.load("alice"));
    }

    @Test public void currentU24IdentitiesAndPositiveEquipmentStacksRoundTrip() throws Exception {
        int[] ids=emptyIds(),amounts=new int[28],equipped=emptyEquipmentIds(),worn=new int[19];
        ids[0]=0;amounts[0]=1;ids[27]=65536;amounts[27]=Integer.MAX_VALUE;
        equipped[13]=877;worn[13]=200;
        Native950Save save=new Native950Save("wide",3217,3256,0,ids,amounts,
                new int[]{0,65535,0xfffffe},new int[]{2,3,4},equipped,worn,false);
        Native950SaveStore store=new Native950SaveStore(temporary.newFolder().toPath());
        store.save(save);assertSaveEquals(save,store.load("wide"));
    }
    @Test public void oldSchemaLoadsWithoutChangingFileAndUpgradesWithoutChangingItems() throws Exception {
        Path directory = temporary.newFolder().toPath();
        Native950SaveStore store = new Native950SaveStore(directory);
        Native950Save original = sample("alice", 3217, 2);
        store.save(original);
        Path path = onlyFile(directory);
        byte[] legacy = legacySchemaOne(original);
        Files.write(path, legacy);
        Native950Save loaded = new Native950SaveStore(directory).load("alice");
        assertSaveEquals(original, loaded);
        assertArrayEquals(legacy, Files.readAllBytes(path));
        assertArrayEquals(emptyEquipmentIds(), loaded.equipmentIds());
        assertFalse(loaded.equipmentKitClaimed());
        store.save(loaded);
        byte[] upgraded = Files.readAllBytes(path);
        assertEquals(Native950Save.SCHEMA_VERSION, ByteBuffer.wrap(upgraded).getInt(8)); // P4: a re-saved schema-1 file becomes schema 3
        assertFalse(Arrays.equals(legacy, upgraded));
        assertSaveEquals(original, store.load("alice"));
        assertEquals(path, onlyFile(directory));
    }

    @Test public void legacyTrailingOrTruncatedStateCannotBeMigratedOrOverwritten() throws Exception {
        Path directory = temporary.newFolder().toPath();
        Native950SaveStore store = new Native950SaveStore(directory);
        Native950Save original = sample("alice", 3217, 2);
        store.save(original);
        Path path = onlyFile(directory);
        byte[] legacy = legacySchemaOne(original);
        byte[] trailing = Arrays.copyOf(legacy, legacy.length + 1);
        trailing[legacy.length - 32] = 0;
        repairChecksum(trailing);
        byte[] truncated = Arrays.copyOf(legacy, legacy.length - 1);
        repairChecksum(truncated);
        byte[] wrongSchema = legacy.clone();
        ByteBuffer.wrap(wrongSchema).putInt(8, 2);
        repairChecksum(wrongSchema);
        for (byte[] invalid : new byte[][] {trailing, truncated, wrongSchema}) {
            Files.write(path, invalid);
            expectIo(() -> store.load("alice"));
            expectIo(() -> store.save(original));
            assertArrayEquals(invalid, Files.readAllBytes(path));
        }
    }

    @Test public void equipmentShapeQuantitiesAndDuplicateItemsAreRejected() throws Exception {
        Native950Save base = sample("alice", 3200, 1000);
        expectIllegal(() -> withEquipment(base, new int[18], new int[19], false));
        expectIllegal(() -> withEquipment(base, new int[19], new int[18], false));
        expectIllegal(() -> withEquipment(base, new int[20], new int[20], false));
        for (int[] item : new int[][] {{-1,1},{1277,0},{1277,-1},{0xffffff,1},{-2,1}}) {
            int[] ids = emptyEquipmentIds(), amounts = new int[19];
            ids[3] = item[0]; amounts[3] = item[1];
            expectIllegal(() -> withEquipment(base, ids, amounts, false));
        }
        int[] duplicateIds = emptyEquipmentIds(), duplicateAmounts = new int[19];
        duplicateIds[3] = 1277; duplicateAmounts[3] = 1;
        duplicateIds[5] = 1277; duplicateAmounts[5] = 1;
        expectIllegal(() -> withEquipment(base, duplicateIds, duplicateAmounts, true));
    }

    @Test public void invalidShapesPositionsAndItemSlotsFailBeforePersistence() throws Exception {
        for (int[] position : new int[][] {{-1,0,0},{16384,0,0},{0,-1,0},{0,16384,0},{0,0,-1},{0,0,4}})
            expectIllegal(() -> new Native950Save("alice", position[0],position[1],position[2],emptyIds(),new int[28],new int[0],new int[0]));
        expectIllegal(() -> new Native950Save("alice",0,0,0,new int[27],new int[28],new int[0],new int[0]));
        expectIllegal(() -> new Native950Save("alice",0,0,0,emptyIds(),new int[28],new int[601],new int[601]));
        expectIllegal(() -> new Native950Save("alice",0,0,0,emptyIds(),new int[28],new int[]{995},new int[0]));
        expectIllegal(() -> new Native950Save("alice",0,0,0,emptyIds(),new int[28],new int[]{995,995},new int[]{1,2}));
        for (int[] item : new int[][] {{-1,1},{995,0},{995,-1},{0xffffff,1},{-2,1}}) {
            int[] ids = emptyIds(), amounts = new int[28]; ids[0] = item[0]; amounts[0] = item[1];
            expectIllegal(() -> new Native950Save("alice",0,0,0,ids,amounts,new int[0],new int[0]));
        }
        expectIllegal(() -> new Native950Save("alice",0,0,0,emptyIds(),new int[28],new int[]{-1},new int[]{0}));
    }

    @Test public void maximumBankIsStillBoundedAndRoundTrips() throws Exception {
        int[] ids = new int[600], amounts = new int[600];
        for (int i = 0; i < 600; i++) { ids[i] = i; amounts[i] = i + 1; }
        Native950Save save = new Native950Save("maximum",0,16383,3,emptyIds(),new int[28],ids,amounts);
        Path directory = temporary.newFolder().toPath();
        Native950SaveStore store = new Native950SaveStore(directory); store.save(save);
        assertTrue(Files.size(onlyFile(directory)) < Native950SaveStore.MAX_FILE_SIZE);
        assertSaveEquals(save, store.load("maximum"));
    }

    @Test public void checksumCorruptionAndTruncationNeverClobberExistingBytes() throws Exception {
        Path directory = temporary.newFolder().toPath();
        Native950SaveStore store = new Native950SaveStore(directory); store.save(sample("alice",3200,1000));
        Path path = onlyFile(directory); byte[] valid = Files.readAllBytes(path);
        for (byte[] corrupt : new byte[][] {new byte[0], Arrays.copyOf(valid, 12), Arrays.copyOf(valid, valid.length - 1), valid.clone()}) {
            if (corrupt.length == valid.length) corrupt[40] ^= 1;
            Files.write(path, corrupt);
            expectIo(() -> store.load("alice"));
            expectIo(() -> store.save(sample("alice",3201,990)));
            assertArrayEquals(corrupt, Files.readAllBytes(path));
            assertEquals(1, files(directory).size());
        }
    }

    /** Schema4 adds a sixth skill-progress section; a schema3 header on that body must fail. */
    @Test public void strictParserRejectsVersionShapeAndStateEvenWithValidChecksum() throws Exception {
        Path directory = temporary.newFolder().toPath();
        Native950SaveStore store = new Native950SaveStore(directory); store.save(sample("alice",3200,1000));
        Path path = onlyFile(directory); byte[] valid = Files.readAllBytes(path);
        int xOffset = 20 + "alice".length(), inventoryLengthOffset = xOffset + 12;
        int inventoryOffset = inventoryLengthOffset + 4, bankLengthOffset = inventoryOffset + 28 * 8;
        List<Consumer<ByteBuffer>> mutations = Arrays.asList(
            bytes -> bytes.put(0, (byte)'X'),
            bytes -> bytes.putInt(8, 0),
            bytes -> bytes.putInt(8, 1),
            bytes -> bytes.putInt(8, 2), // schema 2 header on a schema-3 body: the sections become trailing data
            bytes -> bytes.putInt(8, 3), // schema3 cannot reinterpret a six-section schema4 body
            bytes -> bytes.putInt(8, Native950Save.SCHEMA_VERSION + 1),
            bytes -> bytes.putInt(12, 910), // a revision this build does not read; see onlyTheCurrentClientRevisionIsAccepted
            bytes -> bytes.putInt(16, Integer.MAX_VALUE),
            bytes -> bytes.put(20, (byte)'b'),
            bytes -> bytes.putInt(xOffset, -1),
            bytes -> bytes.putInt(inventoryLengthOffset, Integer.MAX_VALUE),
            bytes -> bytes.putInt(inventoryOffset + 4, 0),
            bytes -> bytes.putInt(bankLengthOffset, -1),
            bytes -> bytes.putInt(bankLengthOffset, 601),
            bytes -> bytes.putInt(bankLengthOffset + 4 + 8, 1511));
        for (Consumer<ByteBuffer> mutation : mutations) {
            byte[] altered = valid.clone(); mutation.accept(ByteBuffer.wrap(altered)); repairChecksum(altered);
            Files.write(path, altered);
            expectIo(() -> store.load("alice"));
            expectIo(() -> store.save(sample("alice",3201,990)));
            assertArrayEquals(altered, Files.readAllBytes(path));
        }
    }

    @Test public void validChecksumCannotHideInvalidEquipmentOrClaimState() throws Exception {
        Path directory = temporary.newFolder().toPath();
        Native950SaveStore store = new Native950SaveStore(directory);
        Native950Save base = sample("alice",3200,1000);
        int[] ids = emptyEquipmentIds(), amounts = new int[19];
        ids[3] = 1277; amounts[3] = 1;
        Native950Save equipped = withEquipment(base, ids, amounts, true);
        store.save(equipped);
        Path path = onlyFile(directory); byte[] valid = Files.readAllBytes(path);
        int equipmentLengthOffset = 20 + "alice".length() + 12 + 4 + 28 * 8 + 4 + 2 * 8;
        int equipmentOffset = equipmentLengthOffset + 4, claimOffset = equipmentOffset + 19 * 8;
        List<Consumer<ByteBuffer>> mutations = Arrays.asList(
            bytes -> bytes.putInt(equipmentLengthOffset, -1),
            bytes -> bytes.putInt(equipmentLengthOffset, 18),
            bytes -> bytes.putInt(equipmentLengthOffset, 20),
            bytes -> bytes.putInt(equipmentOffset + 3 * 8, -2),
            bytes -> bytes.putInt(equipmentOffset + 3 * 8, 0xffffff),
            bytes -> bytes.putInt(equipmentOffset + 3 * 8 + 4, 0),
            bytes -> bytes.putInt(equipmentOffset + 3 * 8 + 4, -1),
            bytes -> bytes.putInt(equipmentOffset + 4, 1),
            bytes -> { bytes.putInt(equipmentOffset, 1277); bytes.putInt(equipmentOffset + 4, 1); },
            bytes -> bytes.put(claimOffset, (byte)2),
            bytes -> bytes.put(claimOffset, (byte)255));
        for (Consumer<ByteBuffer> mutation : mutations) {
            byte[] altered = valid.clone(); mutation.accept(ByteBuffer.wrap(altered)); repairChecksum(altered);
            Files.write(path, altered);
            expectIo(() -> store.load("alice"));
            expectIo(() -> store.save(equipped));
            assertArrayEquals(altered, Files.readAllBytes(path));
            assertEquals(1, files(directory).size());
        }
        byte[] missingClaim = Arrays.copyOf(valid, valid.length - 1);
        repairChecksum(missingClaim);
        Files.write(path, missingClaim);
        expectIo(() -> store.load("alice"));
        expectIo(() -> store.save(equipped));
        assertArrayEquals(missingClaim, Files.readAllBytes(path));
    }

    @Test public void validChecksumDoesNotPermitTrailingPayloadOrOversizedFiles() throws Exception {
        Path directory = temporary.newFolder().toPath();
        Native950SaveStore store = new Native950SaveStore(directory); store.save(sample("alice",3200,1000));
        Path path = onlyFile(directory); byte[] valid = Files.readAllBytes(path);
        byte[] trailing = Arrays.copyOf(valid, valid.length + 1);
        trailing[valid.length - 32] = 42; repairChecksum(trailing);
        Files.write(path, trailing); expectIo(() -> store.load("alice"));
        byte[] oversized = new byte[Native950SaveStore.MAX_FILE_SIZE + 1];
        Files.write(path, oversized); expectIo(() -> store.load("alice"));
        expectIo(() -> store.save(sample("alice",3201,990)));
        assertArrayEquals(oversized, Files.readAllBytes(path));
    }

    @Test public void renamingAValidFileCannotImpersonateAnotherProfile() throws Exception {
        Path directory = temporary.newFolder().toPath();
        Native950SaveStore store = new Native950SaveStore(directory); store.save(sample("alice",3200,1000));
        Path alicePath = onlyFile(directory);
        store.save(sample("bob",3201,990));
        Path bobPath = files(directory).stream().filter(path -> !path.equals(alicePath)).findFirst().get();
        byte[] bobBytes = Files.readAllBytes(bobPath); Files.write(alicePath, bobBytes);
        expectIo(() -> store.load("alice")); expectIo(() -> store.save(sample("alice",3200,1000)));
        assertArrayEquals(bobBytes, Files.readAllBytes(alicePath));
        assertEquals("bob", store.load("bob").username());
    }

    @Test public void failedWriteCannotReplaceAFileWithADirectoryOrDamageParentFile() throws Exception {
        Path directory = temporary.newFolder().toPath();
        Native950SaveStore store = new Native950SaveStore(directory); store.save(sample("alice",3200,1000));
        Path profile = onlyFile(directory); Files.delete(profile); Files.createDirectory(profile);
        Path marker = profile.resolve("keep"); Files.write(marker,new byte[]{4,7});
        expectIo(() -> store.save(sample("alice",3201,990)));
        assertArrayEquals(new byte[]{4,7},Files.readAllBytes(marker));
        Path parentFile = temporary.newFile().toPath(); byte[] preserved = {9,4,7}; Files.write(parentFile,preserved);
        expectIo(() -> new Native950SaveStore(parentFile).save(sample("alice",3200,1000)));
        assertArrayEquals(preserved,Files.readAllBytes(parentFile));
    }

    // ------------------------------------------------------------------ P4: schema 3 sections

    @Test public void schemaThreeSectionsRoundTripExactly() throws Exception {
        Native950Save full = sectioned(sample("alice", 3200, 1000));
        Path directory = temporary.newFolder().toPath();
        Native950SaveStore store = new Native950SaveStore(directory);
        store.save(full);
        assertEquals(4, ByteBuffer.wrap(Files.readAllBytes(onlyFile(directory))).getInt(8));
        Native950Save loaded = new Native950SaveStore(directory).load("alice");
        assertSaveEquals(full, loaded);
        assertEquals(full.skills(), loaded.skills());
        assertEquals(40, loaded.skills().level(0));
        assertEquals(37224.5, loaded.skills().xp(0), 0.0);
        assertEquals(full.vitals(), loaded.vitals());
        assertTrue(loaded.vitals().running);
        assertEquals(full.settings(), loaded.settings());
        assertEquals(Integer.valueOf(1), loaded.settings().get("chatEffects"));
        assertEquals(full.appearance(), loaded.appearance());
        assertFalse(loaded.appearance().male);
        assertEquals(full.identity(), loaded.identity());
        assertEquals("Alice Bob", loaded.identity().displayName);
        assertTrue(loaded.changedSections(full).isEmpty());
        assertEquals(EnumSet.of(Native950Save.Section.SKILLS), full.withSections(Native950Save.Skills.fresh(),
                full.vitals(), full.settings(), full.appearance(), full.identity()).changedSections(full));
    }

    /**
     * M3: the SKILLS and VITALS sections must carry REAL play state, not just the
     * two sample values above - a distinct level and experience for every modelled
     * stat, and vitals at the edges the 947 client actually produces (a full
     * level-99 life bar, a full 990-point prayer bar, empty run energy).
     *
     * <p>A section written at the current width is trusted verbatim, boosts and
     * all: it can only have been written by the current model, so nothing in it
     * needs absorbing. Only the older width is recomputed - see
     * {@link #legacyTwentySevenStatProfileUpgradesInPlaceKeepingEveryExperience()}.
     */
    @Test public void everySkillAndEveryVitalRoundTripsThroughSchemaThree() throws Exception {
        short[] levels = new short[Native950Save.SKILL_COUNT];
        double[] xp = new double[Native950Save.SKILL_COUNT];
        for (int skill = 0; skill < Native950Save.SKILL_COUNT; skill++) {
            levels[skill] = (short) (2 + skill * 3);       // distinct, and different from the xp-derived level
            xp[skill] = 100.5 + skill * 1000.0;
        }
        Native950Save saved = sample("vitals", 3200, 3200).withSections(
                new Native950Save.Skills(levels, xp),
                new Native950Save.Vitals(990, 990, 0, false),
                java.util.Collections.<String, Integer>emptyMap(),
                Native950Save.Appearance.fresh(), new Native950Save.Identity("Vitals", 1L, 2L));
        Path directory = temporary.newFolder().toPath();
        new Native950SaveStore(directory).save(saved);
        Native950Save loaded = new Native950SaveStore(directory).load("vitals");

        for (int skill = 0; skill < Native950Save.SKILL_COUNT; skill++) {
            assertEquals("level of skill " + skill, levels[skill], loaded.skills().level(skill));
            assertEquals("xp of skill " + skill, xp[skill], loaded.skills().xp(skill), 0.0);
        }
        assertEquals(990, loaded.vitals().hitpoints);
        assertEquals(990, loaded.vitals().prayerPoints);
        assertEquals(0, loaded.vitals().runEnergy);
        assertFalse(loaded.vitals().running);
        assertTrue(loaded.changedSections(saved).isEmpty());
    }

    // ------------------------------------------------ 947 stat model: 27 -> 29 stats

    /**
     * The profile the owner is actually playing on: a genuine schema-3 file whose
     * SKILLS section was written by the 27-stat model. It must still load, and the
     * 947 rebalance (new caps for fifteen stats, two new stats) must be absorbed by
     * treating the stored EXPERIENCE as the durable truth:
     *
     * <ul>
     * <li>every one of the 27 stored experience values survives bit-for-bit;</li>
     * <li>every level is recomputed from that experience with the engine's current
     *     per-stat curve, so a level frozen under the old caps cannot survive - the
     *     fixture stores level 3 everywhere on purpose;</li>
     * <li>Archaeology (27) and Necromancy (28) arrive at level 1 with no
     *     experience, the client's own stat-table initial state;</li>
     * <li>nothing is truncated: re-saving rewrites the section at the full width
     *     and a reload is identical.</li>
     * </ul>
     *
     * <p>The three anchor levels are cache values, not engine values:
     * {@code verified/ui/STAT_DEFINITIONS.md} records the default curve (cache
     * table for enum 716) at 101,333 for level 50, 13,034,431 for level 99 and
     * 104,273,167 for level 120, and 1,154 for level 10.
     */
    @Test public void legacyTwentySevenStatProfileUpgradesInPlaceKeepingEveryExperience() throws Exception {
        short[] storedLevels = new short[Native950Save.LEGACY_SKILL_COUNT];
        double[] storedXp = new double[Native950Save.LEGACY_SKILL_COUNT];
        for (int skill = 0; skill < storedLevels.length; skill++) {
            storedLevels[skill] = 3;                       // deliberately wrong: it must be discarded
            storedXp[skill] = 250.25 + skill * 977.5;      // distinct, fractional, exactly representable
        }
        storedXp[0] = 101333;          // Attack, default curve -> level 50
        storedXp[3] = 1155;            // Constitution -> level 10
        storedXp[8] = 13034431;        // Woodcutting, cap 110 -> level 99
        storedXp[18] = 104273167;      // Slayer, cap 120 -> level 120

        Native950Save carrier = sample("legacy", 3217, 2);
        Path directory = temporary.newFolder().toPath();
        Native950SaveStore store = new Native950SaveStore(directory);
        Path path = directory.resolve(profileFileName(carrier));
        Files.createDirectories(directory);
        Files.write(path, legacySchemaThreeWithStats(carrier, storedLevels, storedXp));

        Native950Save loaded = store.load("legacy");
        assertNotNull("a 27-stat schema-3 profile must still load", loaded);
        Native950Save.Skills skills = loaded.skills();

        for (int skill = 0; skill < storedXp.length; skill++)
            assertEquals("experience of stat " + skill + " must survive the upgrade",
                    storedXp[skill], skills.xp(skill), 0.0);
        assertEquals("Attack, 101333 xp on the cache default curve", 50, skills.level(0));
        assertEquals("Constitution, 1155 xp", 10, skills.level(3));
        assertEquals("Woodcutting, 13034431 xp", 99, skills.level(8));
        assertEquals("Slayer, 104273167 xp", 120, skills.level(18));
        assertNotEquals("the stored level must not be trusted", 3, skills.level(0));
        // Every level, not only the anchors, comes from the engine's current curve.
        for (int skill = 0; skill < storedXp.length; skill++)
            assertEquals("recomputed level of stat " + skill,
                    Native950Save.levelForXp(skill, storedXp[skill]), skills.level(skill));

        for (int skill = storedXp.length; skill < Native950Save.SKILL_COUNT; skill++) {
            assertEquals("new stat " + skill + " starts at level 1", 1, skills.level(skill));
            assertEquals("new stat " + skill + " starts with no experience", 0.0, skills.xp(skill), 0.0);
        }

        // Upgrade in place: the rewritten section is full width and reloads identically.
        store.save(loaded);
        byte[] rewritten = Files.readAllBytes(path);
        assertEquals(Native950Save.SCHEMA_VERSION, ByteBuffer.wrap(rewritten).getInt(8));
        assertEquals("the rewritten SKILLS section carries every modelled stat",
                Native950Save.SKILL_COUNT, rewritten[skillsPayloadOffset(carrier)] & 0xff);
        Native950Save reloaded = new Native950SaveStore(directory).load("legacy");
        assertEquals(skills, reloaded.skills());
        assertSaveEquals(loaded, reloaded);
        assertTrue(reloaded.changedSections(loaded).isEmpty());
    }

    /**
     * The 947 rebalance lowered the experience ceiling from 2,000,000,000 to the
     * client's own 200,000,000 clamp, and a pre-rebalance profile is clamped on load
     * rather than being refused. Because the very next checkpoint rewrites the whole
     * file, the store takes one recoverable copy of the original bytes before its first
     * write over such a profile. The copy is created once and never replaced, so a
     * later checkpoint cannot overwrite it with an already-clamped file.
     *
     * <p>Both routes into "pre-rebalance" are covered: an older-width SKILLS section,
     * and a full-width section carrying experience above the new ceiling.
     */
    @Test public void aPreRebalanceProfileIsCopiedOnceBeforeTheFirstRewrite() throws Exception {
        // Route 1: the older 27-stat width.
        Native950Save carrier = sample("legacy", 3217, 2);
        Path narrow = temporary.newFolder().toPath();
        Native950SaveStore narrowStore = new Native950SaveStore(narrow);
        Path narrowPath = narrow.resolve(profileFileName(carrier));
        Files.createDirectories(narrow);
        short[] levels = new short[Native950Save.LEGACY_SKILL_COUNT];
        double[] xp = new double[Native950Save.LEGACY_SKILL_COUNT];
        for (int skill = 0; skill < levels.length; skill++) { levels[skill] = 1; xp[skill] = skill * 101.5; }
        byte[] original = legacySchemaThreeWithStats(carrier, levels, xp);
        Files.write(narrowPath, original);
        Path narrowBackup = narrow.resolve(profileFileName(carrier) + ".pre29.bak");
        assertFalse("no backup before the first write", Files.exists(narrowBackup));
        narrowStore.save(narrowStore.load("legacy"));
        assertArrayEquals("the backup must be the original bytes", original, Files.readAllBytes(narrowBackup));
        assertEquals("the profile itself is rewritten at the new width",
                Native950Save.SKILL_COUNT, Files.readAllBytes(narrowPath)[skillsPayloadOffset(carrier)] & 0xff);
        // A second checkpoint must not replace the good backup with the rewritten file.
        byte[] rewritten = Files.readAllBytes(narrowPath);
        narrowStore.save(narrowStore.load("legacy"));
        assertArrayEquals("the backup is written once, never replaced", original, Files.readAllBytes(narrowBackup));
        assertFalse("the backup is not the rewritten profile", Arrays.equals(rewritten, original));

        // Route 2: full width, but experience above the new ceiling.
        Path wide = temporary.newFolder().toPath();
        Native950SaveStore wideStore = new Native950SaveStore(wide);
        Native950Save full = sectioned(sample("alice", 3200, 1000));
        wideStore.save(full);
        Path widePath = onlyFile(wide);
        byte[] overCeiling = Files.readAllBytes(widePath);
        // The first stat's experience field: count byte, then short level, then double xp.
        ByteBuffer.wrap(overCeiling).putDouble(skillsPayloadOffset(full) + 3, 2000000000.0);
        repairChecksum(overCeiling);
        Files.write(widePath, overCeiling);
        Path wideBackup = wide.resolve(widePath.getFileName().toString() + ".pre29.bak");
        Native950Save clamped = wideStore.load("alice");
        assertEquals("the load clamps rather than failing", (double) Native950Save.MAX_XP, clamped.skills().xp(0), 0.0);
        assertFalse("loading alone must not copy anything", Files.exists(wideBackup));
        wideStore.save(clamped);
        assertArrayEquals("the pre-clamp bytes stay recoverable", overCeiling, Files.readAllBytes(wideBackup));

        // A profile this build can round-trip is never copied.
        Path clean = temporary.newFolder().toPath();
        Native950SaveStore cleanStore = new Native950SaveStore(clean);
        cleanStore.save(full);
        cleanStore.save(cleanStore.load("alice"));
        assertEquals("an in-model profile leaves exactly one file behind", 1, files(clean).size());
    }

    // ------------------------------------------------ 950 port: the client-revision axis

    /**
     * A 947 profile is REFUSED, and refused at the container before the revision int is even
     * reached.
     *
     * <p>This used to be the migration test: a 947-stamped file loaded and the next checkpoint
     * restamped it 950. The migration existed because refusing a profile is unrecoverable -
     * {@code save} performs a protective {@code load} first, so a rejected file is unreadable AND
     * unwritable, and a port that changed no byte of the format would have bricked every existing
     * character. That cost was real; the benefit turned out not to be. No 947 or 910 character was
     * ever in real use, so the format is now simply 950's own.
     *
     * <p>What that buys: the appearance section no longer has to assume that a stored identity-kit
     * id means the same thing in the 947 and 950 caches. It probably does - they are cache idk ids
     * under both - but every profile on disk held exactly the hardcoded default, so nothing there
     * ever tested the assumption, and a character's face is a poor place to rely on "probably".
     *
     * <p>Two independent gates reject the old file and this pins both, because either alone would
     * let the other rot: the magic differs (ANXT947 vs ANXT950), and the revision int differs.
     */
    @Test public void aProfileFromTheOlderClientRevisionIsRefusedAtTheContainer() throws Exception {
        short[] levels = new short[Native950Save.SKILL_COUNT];
        double[] xp = new double[Native950Save.SKILL_COUNT];
        for (int skill = 0; skill < levels.length; skill++) {
            levels[skill] = (short) (2 + skill);
            xp[skill] = 500.0 + skill * 313.75;
        }
        Native950Save carrier = sample("legacy", 3217, 2);
        Path directory = temporary.newFolder().toPath();
        Files.createDirectories(directory);
        Path path = directory.resolve(profileFileName(carrier));
        byte[] original = oldContainerSchemaThree(carrier, levels, xp);
        assertEquals("the fixture must carry the older revision, or this proves nothing",
                947, ByteBuffer.wrap(original).getInt(12));
        Files.write(path, original);

        Native950SaveStore store = new Native950SaveStore(directory);
        try {
            store.load("legacy");
            fail("a 947-stamped profile must not load into the 950 build");
        } catch (IOException refused) {
            assertNotNull(refused.getMessage());
        }
        assertArrayEquals("a refused load must not rewrite the file", original,
                Files.readAllBytes(path));

        // The revision gate on its own, with the container made valid, so that neither check can
        // quietly stop working behind the other.
        byte[] rightMagic = original.clone();
        byte[] magic950 = new byte[] {'A', 'N', 'X', 'T', '9', '5', '0', '\n'};
        System.arraycopy(magic950, 0, rightMagic, 0, magic950.length);
        Path second = temporary.newFolder().toPath();
        Files.createDirectories(second);
        Files.write(second.resolve(profileFileName(carrier)), rightMagic);
        try {
            new Native950SaveStore(second).load("legacy");
            fail("revision 947 must be refused even inside a well-formed 950 container");
        } catch (IOException refused) {
            assertNotNull(refused.getMessage());
        }

        assertFalse("947 is not an accepted revision", Native950Save.acceptsClientRevision(947));
        assertTrue("950 is", Native950Save.acceptsClientRevision(Native950Save.CLIENT_REVISION));
    }

    /**
     * Exactly one client revision is accepted, and every other value - including the immediate
     * neighbours 947 and 951 - is a hard failure rather than a silent reinterpretation of somebody
     * else's bytes.
     *
     * <p>This used to be an allowlist of {947, 950} because the port carried 947 profiles across.
     * It no longer does: no character predating the format was ever in use, so accepting a second
     * revision bought nothing and cost an unevidenced assumption about appearance values meaning
     * the same thing in both caches.
     *
     * <p>Offset 12 is the documented header position: 8 bytes of magic, {@code int schema} at 8,
     * {@code int clientRevision} at 12.
     */
    @Test public void onlyTheCurrentClientRevisionIsAccepted() throws Exception {
        assertTrue(Native950Save.acceptsClientRevision(950));
        assertFalse("the 947 migration path was removed", Native950Save.acceptsClientRevision(947));
        assertFalse("910 profiles were written by a different serializer", Native950Save.acceptsClientRevision(910));

        Path directory = temporary.newFolder().toPath();
        Native950SaveStore store = new Native950SaveStore(directory);
        Native950Save saved = sample("alice", 3200, 1000);
        store.save(saved);
        Path path = onlyFile(directory);
        byte[] valid = Files.readAllBytes(path);
        assertEquals("this build stamps the current revision",
                Native950Save.CLIENT_REVISION, ByteBuffer.wrap(valid).getInt(12));

        for (int revision : new int[] {0, 1, -1, 910, 946, 947, 948, 949, 951, 1000, Integer.MIN_VALUE, Integer.MAX_VALUE}) {
            byte[] altered = valid.clone();
            ByteBuffer.wrap(altered).putInt(12, revision);
            repairChecksum(altered);
            Files.write(path, altered);
            expectIo(() -> store.load("alice"));
            expectIo(() -> store.save(saved));
            assertArrayEquals("a refused profile is never overwritten", altered, Files.readAllBytes(path));
            assertEquals(1, files(directory).size());
        }
        byte[] accepted = valid.clone();
        ByteBuffer.wrap(accepted).putInt(12, Native950Save.CLIENT_REVISION);
        repairChecksum(accepted);
        Files.write(path, accepted);
        assertSaveEquals(saved, store.load("alice"));
    }

    /**
     * schema and clientRevision are independent axes, and the parser must treat them that way: an
     * old LAYOUT is not an old PROTOCOL. Only revision 950 is accepted now, so what remains to
     * prove is that schema 1 still loads underneath it - the schema axis has to keep working on
     * its own, or the next layout bump has no migration path either.
     */
    @Test public void theSchemaAndClientRevisionFieldsAreIndependent() throws Exception {
        Path directory = temporary.newFolder().toPath();
        Native950SaveStore store = new Native950SaveStore(directory);
        Native950Save original = sample("alice", 3217, 2);
        store.save(original);
        Path path = onlyFile(directory);

        byte[] schemaOneAt950 = legacySchemaOne(original);
        assertEquals("the fixture must carry the OLD layout", 1, ByteBuffer.wrap(schemaOneAt950).getInt(8));
        assertEquals("under the CURRENT protocol", Native950Save.CLIENT_REVISION,
                ByteBuffer.wrap(schemaOneAt950).getInt(12));
        Files.write(path, schemaOneAt950);
        assertSaveEquals(original, store.load("alice"));
        assertArrayEquals("neither load rewrote anything", schemaOneAt950, Files.readAllBytes(path));
    }

    /**
     * The stored body-kit ceiling is the 950 client's own wearpos boundary, not a round
     * number. {@code GlobalPlayerUpdater.buildNative947AppearanceData} sends a stored
     * kit {@code k} as the varint {@code Native950Appearance.KIT_BASE + k}; the 950
     * decoder takes the kit branch only below {@code ITEM_BASE} (950 VA 0x140131ccd),
     * and a value at or above it is read as an ITEM and dereferenced with no null check
     * at 950 VA 0x140131d4f-0x140131d53 - an access violation that kills the client
     * outright ({@code protocol-analysis/appearance-950.md}). So the highest kit that
     * can safely be persisted is {@code 0x800 - KIT_BASE - 1}.
     *
     * <p>The field previously accepted 65535, i.e. this store would persist, and later
     * hand back, a profile whose next appearance broadcast deterministically crashes
     * the client it was loaded for, with no server-side error to point at.
     */
    @Test public void aBodyKitThatWouldFaultTheClientIsRefusedOnTheWayInAndOnTheWayOut() throws Exception {
        assertEquals("derived from the 950 classification constants, not chosen",
                0x800 - 2 - 1, Native950Save.Appearance.MAX_BODY_KIT);
        final int[] colours = Native950Save.Appearance.fresh().colours();
        int[] highest = Native950Save.Appearance.fresh().bodyKits();
        highest[2] = Native950Save.Appearance.MAX_BODY_KIT;
        assertEquals("the last kit below the item base is still a kit", Native950Save.Appearance.MAX_BODY_KIT,
                new Native950Save.Appearance(true, colours, highest).bodyKits()[2]);
        for (int kit : new int[] {Native950Save.Appearance.MAX_BODY_KIT + 1, 0x800, 0x7FFF, 65535, -2}) {
            final int[] kits = Native950Save.Appearance.fresh().bodyKits();
            kits[2] = kit;
            expectIllegal(() -> new Native950Save.Appearance(true, colours, kits));
        }

        // A file that already holds one is refused, not handed out.
        Path directory = temporary.newFolder().toPath();
        Native950SaveStore store = new Native950SaveStore(directory);
        Native950Save full = sectioned(sample("alice", 3200, 1000));
        store.save(full);
        Path path = onlyFile(directory);
        byte[] altered = Files.readAllBytes(path);
        int appearance = appearancePayloadOffset(full);
        // APPEARANCE payload: gender byte, COLOUR_COUNT colour bytes, then BODY_KIT_COUNT
        // big-endian int kits. sectioned() stores a female look whose third kit is 57.
        assertEquals("appearance offset check: gender byte", 0, altered[appearance] & 0xff);
        int kitsOffset = appearance + 1 + Native950Save.COLOUR_COUNT;
        assertEquals("appearance offset check: third kit", 57, ByteBuffer.wrap(altered).getInt(kitsOffset + 2 * 4));
        ByteBuffer.wrap(altered).putInt(kitsOffset + 2 * 4, Native950Save.Appearance.MAX_BODY_KIT + 1);
        repairChecksum(altered);
        Files.write(path, altered);
        expectIo(() -> store.load("alice"));
        expectIo(() -> store.save(full));
        assertArrayEquals(altered, Files.readAllBytes(path));
        assertEquals(1, files(directory).size());
    }

    /**
     * The section cap must hold the whole stat table with room to spare, so a new
     * stat can never silently overflow it: one count byte plus ten bytes per stat.
     */
    @Test public void maxSkillsSectionHoldsTheWholeStatTable() throws Exception {
        assertTrue("MAX_SKILLS_SECTION " + Native950SaveStore.MAX_SKILLS_SECTION
                        + " must hold " + Native950Save.SKILL_COUNT + " stats",
                1 + Native950Save.SKILL_COUNT * 10 <= Native950SaveStore.MAX_SKILLS_SECTION);
        Native950Save full = sectioned(sample("alice", 3200, 1000));
        Path directory = temporary.newFolder().toPath();
        new Native950SaveStore(directory).save(full);
        byte[] bytes = Files.readAllBytes(onlyFile(directory));
        int lengthOffset = skillsPayloadOffset(full) - 4;
        assertEquals(1 + Native950Save.SKILL_COUNT * 10, ByteBuffer.wrap(bytes).getInt(lengthOffset));
    }

    /** Byte offset of the SKILLS payload (its count byte) in a schema-3 file. */
    private static int skillsPayloadOffset(Native950Save save) {
        return 20 + save.username().length() + 12
                + 4 + 28 * 8
                + 4 + save.bankIds().length * 8
                + 4 + 19 * 8 + 1
                + 4      // section count
                + 4 + 4; // SKILLS tag and length
    }

    /**
     * Byte offset of the APPEARANCE payload (its gender byte) in a schema-3 file,
     * walked from the documented section layout rather than from the encoder: SKILLS
     * is one count byte plus ten bytes per stat, VITALS is three ints and a byte (13),
     * SETTINGS is a count int then one length byte, key and int per entry, and every
     * section is preceded by its tag and length ints.
     */
    private static int appearancePayloadOffset(Native950Save save) {
        int settingsPayload = 4;
        for (Map.Entry<String, Integer> entry : save.settings().entrySet())
            settingsPayload += 1 + entry.getKey().length() + 4;
        return skillsPayloadOffset(save)
                + 1 + Native950Save.SKILL_COUNT * 10 // SKILLS payload
                + 8 + 13                             // VITALS tag, length, payload
                + 8 + settingsPayload                // SETTINGS tag, length, payload
                + 8;                                 // APPEARANCE tag and length
    }

    /** The file name the store gives a profile, without asking the store to write one. */
    private static String profileFileName(Native950Save save) throws Exception {
        byte[] hash = MessageDigest.getInstance("SHA-256")
                .digest(save.username().getBytes(StandardCharsets.US_ASCII));
        StringBuilder name = new StringBuilder(68);
        for (byte value : hash)
            name.append(Character.forDigit((value >>> 4) & 15, 16)).append(Character.forDigit(value & 15, 16));
        return name.append(".950").toString();
    }

    /**
     * Independent schema-three fixture with a caller-chosen stat count: the exact
     * pre-29-stat encoder layout, never the current encoder.
     */
    /**
     * A schema-3 profile in the OLD container: ANXT947 magic, clientRevision 947.
     *
     * Separate from {@link #legacySchemaThreeWithStats} on purpose. That one carries an old LAYOUT
     * in the CURRENT container, because the tests using it are about the schema axis and would
     * otherwise be refused at the magic before reaching the layout they exist to exercise. This
     * one is the file the 950 build must refuse, so both of its gates differ.
     */
    private static byte[] oldContainerSchemaThree(Native950Save save, short[] levels, double[] xp) throws Exception {
        byte[] current = legacySchemaThreeWithStats(save, levels, xp);
        System.arraycopy(new byte[] {'A', 'N', 'X', 'T', '9', '4', '7', '\n'}, 0, current, 0, 8);
        ByteBuffer.wrap(current).putInt(12, 947);
        repairChecksum(current);
        return current;
    }

    private static byte[] legacySchemaThreeWithStats(Native950Save save, short[] levels, double[] xp) throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (DataOutputStream data = new DataOutputStream(bytes)) {
            data.write(new byte[] {'A','N','X','T','9','5','0','\n'});
            data.writeInt(3); data.writeInt(Native950Save.CLIENT_REVISION);
            byte[] name = save.username().getBytes(StandardCharsets.US_ASCII);
            data.writeInt(name.length); data.write(name);
            data.writeInt(save.x()); data.writeInt(save.y()); data.writeInt(save.plane());
            for (int[][] items : new int[][][] {{save.inventoryIds(), save.inventoryAmounts()},
                    {save.bankIds(), save.bankAmounts()}, {save.equipmentIds(), save.equipmentAmounts()}}) {
                data.writeInt(items[0].length);
                for (int i = 0; i < items[0].length; i++) { data.writeInt(items[0][i]); data.writeInt(items[1][i]); }
            }
            data.writeByte(save.equipmentKitClaimed() ? 1 : 0);
            data.writeInt(5);
            ByteArrayOutputStream skills = new ByteArrayOutputStream();
            try (DataOutputStream section = new DataOutputStream(skills)) {
                section.writeByte(levels.length);
                for (int skill = 0; skill < levels.length; skill++) {
                    section.writeShort(levels[skill]); section.writeDouble(xp[skill]);
                }
            }
            writeFixtureSection(data, 1, skills.toByteArray());
            ByteArrayOutputStream vitals = new ByteArrayOutputStream();
            try (DataOutputStream section = new DataOutputStream(vitals)) {
                section.writeInt(100); section.writeInt(10); section.writeInt(100); section.writeByte(0);
            }
            writeFixtureSection(data, 2, vitals.toByteArray());
            ByteArrayOutputStream settings = new ByteArrayOutputStream();
            try (DataOutputStream section = new DataOutputStream(settings)) { section.writeInt(0); }
            writeFixtureSection(data, 3, settings.toByteArray());
            ByteArrayOutputStream appearance = new ByteArrayOutputStream();
            try (DataOutputStream section = new DataOutputStream(appearance)) {
                Native950Save.Appearance look = Native950Save.Appearance.fresh();
                section.writeByte(look.male ? 1 : 0);
                for (int colour : look.colours()) section.writeByte(colour);
                for (int kit : look.bodyKits()) section.writeInt(kit);
            }
            writeFixtureSection(data, 4, appearance.toByteArray());
            ByteArrayOutputStream identity = new ByteArrayOutputStream();
            try (DataOutputStream section = new DataOutputStream(identity)) {
                byte[] display = save.username().getBytes(StandardCharsets.US_ASCII);
                section.writeInt(display.length); section.write(display);
                section.writeLong(0L); section.writeLong(0L);
            }
            writeFixtureSection(data, 5, identity.toByteArray());
        }
        byte[] body = bytes.toByteArray();
        bytes.write(MessageDigest.getInstance("SHA-256").digest(body));
        return bytes.toByteArray();
    }

    private static void writeFixtureSection(DataOutputStream data, int tag, byte[] payload) throws IOException {
        data.writeInt(tag); data.writeInt(payload.length); data.write(payload);
    }

    @Test public void schemaOneAndTwoFilesLoadWithDefaultSectionsAndUpgradeInPlace() throws Exception {
        Path directory = temporary.newFolder().toPath();
        Native950SaveStore store = new Native950SaveStore(directory);
        Native950Save original = sample("alice", 3217, 2);
        store.save(original);
        Path path = onlyFile(directory);
        int[] ids = emptyEquipmentIds(), amounts = new int[19];
        ids[3] = 1277; amounts[3] = 1;
        Native950Save equipped = withEquipment(original, ids, amounts, true);
        for (byte[] legacy : new byte[][] {legacySchemaOne(original), legacySchemaTwo(equipped)}) {
            Files.write(path, legacy);
            Native950Save loaded = new Native950SaveStore(directory).load("alice");
            assertArrayEquals(legacy, Files.readAllBytes(path));
            assertEquals(Native950Save.Skills.fresh(), loaded.skills());
            assertEquals(10, loaded.skills().level(3));
            assertEquals(1155, loaded.skills().xp(3), 0.0);
            assertEquals(Native950Save.Vitals.fresh(), loaded.vitals());
            assertTrue(loaded.settings().isEmpty());
            assertEquals(Native950Save.Appearance.fresh(), loaded.appearance());
            assertEquals(Native950Save.Identity.fresh("alice"), loaded.identity());
            store.save(loaded);
            assertEquals(4, ByteBuffer.wrap(Files.readAllBytes(path)).getInt(8));
            Native950Save upgraded = store.load("alice");
            assertSaveEquals(loaded, upgraded);
            assertEquals(loaded.skills(), upgraded.skills());
        }
        assertEquals(1277, store.load("alice").equipmentIds()[3]);
        assertTrue(store.load("alice").equipmentKitClaimed());
    }

    @Test public void corruptSectionsFailClosedWithoutOverwriting() throws Exception {
        Path directory = temporary.newFolder().toPath();
        Native950SaveStore store = new Native950SaveStore(directory);
        Native950Save full = sectioned(sample("alice", 3200, 1000));
        store.save(full);
        Path path = onlyFile(directory); byte[] valid = Files.readAllBytes(path);
        int sectionCountOffset = 20 + "alice".length() + 12 + 4 + 28 * 8 + 4 + 2 * 8 + 4 + 19 * 8 + 1;
        int skillsTag = sectionCountOffset + 4, skillsLength = skillsTag + 4, skillsPayload = skillsLength + 4;
        int vitalsTag = skillsPayload + 1 + Native950Save.SKILL_COUNT * 10, vitalsPayload = vitalsTag + 8;
        int settingsTag = vitalsPayload + 13, settingsPayload = settingsTag + 8;
        List<Consumer<ByteBuffer>> mutations = new java.util.ArrayList<>(Arrays.asList(
            bytes -> bytes.putInt(sectionCountOffset, 4),
            bytes -> bytes.putInt(sectionCountOffset, 7),
            bytes -> bytes.putInt(skillsTag, 2),                       // sections out of order
            bytes -> bytes.putInt(skillsTag, 9),                       // unknown tag
            bytes -> bytes.putInt(skillsLength, Native950Save.SKILL_COUNT * 10), // length disagrees with the payload
            bytes -> bytes.putInt(skillsLength, 100000),               // above the section cap
            bytes -> bytes.put(skillsPayload, (byte) 26),              // wrong skill count, below both accepted widths
            bytes -> bytes.put(skillsPayload, (byte) 28),              // between the two accepted widths
            bytes -> bytes.put(skillsPayload, (byte) 30),              // above the model
            bytes -> bytes.putShort(skillsPayload + 1, (short) -1),    // negative level
            bytes -> bytes.putDouble(skillsPayload + 3, -1.0),         // negative xp
            bytes -> bytes.putDouble(skillsPayload + 3, Double.NaN),
            bytes -> bytes.putInt(vitalsPayload + 8, 101),             // run energy above 100
            bytes -> bytes.putInt(vitalsPayload, -1),                  // negative hitpoints
            bytes -> bytes.put(vitalsPayload + 12, (byte) 2),          // run toggle not 0/1
            bytes -> bytes.putInt(settingsPayload, 33)));              // too many settings
        // The older 27-stat width is accepted, but only with a 27-stat payload:
        // claiming it on a full-width section must leave trailing bytes behind.
        if (Native950Save.LEGACY_SKILL_COUNT != Native950Save.SKILL_COUNT)
            mutations.add(bytes -> bytes.put(skillsPayload, (byte) Native950Save.LEGACY_SKILL_COUNT));
        for (Consumer<ByteBuffer> mutation : mutations) {
            byte[] altered = valid.clone(); mutation.accept(ByteBuffer.wrap(altered)); repairChecksum(altered);
            Files.write(path, altered);
            expectIo(() -> store.load("alice"));
            expectIo(() -> store.save(full));
            assertArrayEquals(altered, Files.readAllBytes(path));
            assertEquals(1, files(directory).size());
        }
    }

    @Test public void sectionValuesAreValidatedBeforePersistence() throws Exception {
        int stats = Native950Save.SKILL_COUNT;
        expectIllegal(() -> new Native950Save.Skills(new short[stats - 1], new double[stats]));
        expectIllegal(() -> new Native950Save.Skills(new short[stats], new double[stats - 1]));
        expectIllegal(() -> new Native950Save.Skills(new short[stats + 1], new double[stats + 1]));
        double[] overCap = new double[stats];
        overCap[0] = Native950Save.MAX_XP + 1;
        expectIllegal(() -> new Native950Save.Skills(new short[stats], overCap));
        // A narrower table is widened, never accepted verbatim, and nothing wider is accepted at all.
        expectIllegal(() -> Native950Save.Skills.upgraded(new short[stats + 1], new double[stats + 1]));
        expectIllegal(() -> Native950Save.Skills.upgraded(new short[stats], new double[stats - 1]));
        expectIllegal(() -> new Native950Save.Vitals(0, 0, 101, false));
        expectIllegal(() -> new Native950Save.Vitals(65536, 0, 0, false));
        expectIllegal(() -> new Native950Save.Appearance(true, new int[9], new int[7]));
        expectIllegal(() -> new Native950Save.Appearance(true, new int[] {256,0,0,0,0,0,0,0,0,0}, new int[7]));
        expectIllegal(() -> new Native950Save.Appearance(true, new int[10], new int[] {-2,0,0,0,0,0,0}));
        expectIllegal(() -> new Native950Save.Identity("", 0, 0));
        expectIllegal(() -> new Native950Save.Identity("a/b", 0, 0));
        expectIllegal(() -> new Native950Save.Identity("alice", -1, 0));
        Map<String, Integer> tooMany = new java.util.LinkedHashMap<>();
        for (int i = 0; i <= Native950Save.MAX_SETTINGS; i++) tooMany.put("k" + i, i);
        Native950Save base = sample("alice", 3200, 1000);
        expectIllegal(() -> base.withSections(null, null, tooMany, null, null));
        expectIllegal(() -> base.withSections(null, null, java.util.Collections.singletonMap("bad key", 1), null, null));
        expectIllegal(() -> base.withSections(null, null, java.util.Collections.singletonMap("", 1), null, null));
        assertEquals(Native950Save.Skills.fresh(), base.withSections(null, null, null, null, null).skills());
    }

    private static Native950Save sectioned(Native950Save base) {
        short[] levels = Native950Save.Skills.fresh().levels();
        double[] xp = Native950Save.Skills.fresh().xp();
        levels[0] = 40; xp[0] = 37224.5;
        levels[26] = 120; xp[26] = 200000000d;
        Map<String, Integer> settings = new java.util.LinkedHashMap<>();
        settings.put("chatEffects", 1); settings.put("profanityFilter", 0); settings.put("mouse.buttons", 2);
        return base.withSections(new Native950Save.Skills(levels, xp), new Native950Save.Vitals(990, 45, 73, true), settings,
                new Native950Save.Appearance(false, new int[] {3, 16, 16, 7, 0, 0, 0, 0, 0, 255}, new int[] {48, -1, 57, 65, 68, 77, 80}),
                new Native950Save.Identity("Alice Bob", 1_700_000_000_000L, 1_800_000_000_000L));
    }

    /** Independent schema-two fixture: the exact pre-P4 encoder layout, never the current encoder. */
    private static byte[] legacySchemaTwo(Native950Save save) throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (DataOutputStream data = new DataOutputStream(bytes)) {
            // Current CONTAINER, old LAYOUT - this fixture is about the schema axis.
            data.write(new byte[] {'A','N','X','T','9','5','0','\n'});
            data.writeInt(2); data.writeInt(Native950Save.CLIENT_REVISION);
            byte[] name = save.username().getBytes(StandardCharsets.US_ASCII);
            data.writeInt(name.length); data.write(name);
            data.writeInt(save.x()); data.writeInt(save.y()); data.writeInt(save.plane());
            for (int[][] items : new int[][][] {{save.inventoryIds(), save.inventoryAmounts()}, {save.bankIds(), save.bankAmounts()},
                    {save.equipmentIds(), save.equipmentAmounts()}}) {
                data.writeInt(items[0].length);
                for (int i = 0; i < items[0].length; i++) { data.writeInt(items[0][i]); data.writeInt(items[1][i]); }
            }
            data.writeByte(save.equipmentKitClaimed() ? 1 : 0);
        }
        byte[] body = bytes.toByteArray();
        bytes.write(MessageDigest.getInstance("SHA-256").digest(body));
        return bytes.toByteArray();
    }

    private static Native950Save sample(String username, int x, int coins) {
        int[] ids = emptyIds(), amounts = new int[28];
        ids[0] = 995; amounts[0] = coins; ids[7] = 1511; amounts[7] = 1; ids[27] = 315; amounts[27] = 1;
        return new Native950Save(username,x,3256,0,ids,amounts,new int[]{1511,315},new int[]{4,4});
    }
    private static int[] emptyIds() { int[] ids = new int[28]; Arrays.fill(ids,-1); return ids; }
    private static int[] emptyEquipmentIds() { int[] ids = new int[19]; Arrays.fill(ids,-1); return ids; }
    private static Native950Save withEquipment(Native950Save base, int[] ids, int[] amounts, boolean claimed) {
        return new Native950Save(base.username(), base.x(), base.y(), base.plane(),
                base.inventoryIds(), base.inventoryAmounts(), base.bankIds(), base.bankAmounts(), ids, amounts, claimed);
    }
    /** Independent schema-one fixture: never calls the current encoder. */
    private static byte[] legacySchemaOne(Native950Save save) throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (DataOutputStream data = new DataOutputStream(bytes)) {
            // The current CONTAINER with an OLD LAYOUT, which is the combination this fixture
            // exists to exercise. The 947 container is a separate case and is covered by
            // aProfileFromTheOlderClientRevisionIsRefusedAtTheContainer.
            data.write(new byte[] {'A','N','X','T','9','5','0','\n'});
            data.writeInt(1); data.writeInt(Native950Save.CLIENT_REVISION);
            byte[] name = save.username().getBytes(StandardCharsets.US_ASCII);
            data.writeInt(name.length); data.write(name);
            data.writeInt(save.x()); data.writeInt(save.y()); data.writeInt(save.plane());
            int[] ids = save.inventoryIds(), amounts = save.inventoryAmounts();
            data.writeInt(ids.length);
            for (int i = 0; i < ids.length; i++) { data.writeInt(ids[i]); data.writeInt(amounts[i]); }
            ids = save.bankIds(); amounts = save.bankAmounts();
            data.writeInt(ids.length);
            for (int i = 0; i < ids.length; i++) { data.writeInt(ids[i]); data.writeInt(amounts[i]); }
        }
        byte[] body = bytes.toByteArray();
        bytes.write(MessageDigest.getInstance("SHA-256").digest(body));
        return bytes.toByteArray();
    }
    private static List<Path> files(Path directory) throws IOException {
        try (Stream<Path> paths = Files.list(directory)) { return paths.collect(Collectors.toList()); }
    }
    private static Path onlyFile(Path directory) throws IOException { List<Path> paths = files(directory); assertEquals(1,paths.size()); return paths.get(0); }
    private static void repairChecksum(byte[] bytes) throws Exception {
        int bodyLength = bytes.length - 32;
        byte[] digest = MessageDigest.getInstance("SHA-256").digest(Arrays.copyOf(bytes,bodyLength));
        System.arraycopy(digest,0,bytes,bodyLength,32);
    }
    private static void assertSaveEquals(Native950Save expected, Native950Save actual) {
        assertNotNull(actual); assertEquals(expected.username(),actual.username());
        assertEquals(expected.x(),actual.x()); assertEquals(expected.y(),actual.y()); assertEquals(expected.plane(),actual.plane());
        assertArrayEquals(expected.inventoryIds(),actual.inventoryIds()); assertArrayEquals(expected.inventoryAmounts(),actual.inventoryAmounts());
        assertArrayEquals(expected.bankIds(),actual.bankIds()); assertArrayEquals(expected.bankAmounts(),actual.bankAmounts());
        assertArrayEquals(expected.equipmentIds(),actual.equipmentIds()); assertArrayEquals(expected.equipmentAmounts(),actual.equipmentAmounts());
        assertEquals(expected.equipmentKitClaimed(),actual.equipmentKitClaimed());
    }
    private interface ThrowingAction { void run() throws Exception; }
    private static void expectIo(ThrowingAction action) throws Exception {
        try { action.run(); fail("Expected save I/O rejection"); } catch (IOException expected) { }
    }
    private static void expectIllegal(ThrowingAction action) throws Exception {
        try { action.run(); fail("Expected invalid save state rejection"); } catch (IllegalArgumentException expected) { }
    }
}
