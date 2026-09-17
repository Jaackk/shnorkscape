package com.rs.game.player.client;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * One-process local save store. Writes force a complete temporary file before an
 * atomic same-directory replacement. An invalid existing profile is never reset
 * or overwritten, and a filesystem without atomic replacement fails closed.
 *
 * <p>File layout (big-endian, SHA-256 of the body appended):
 * <pre>
 * magic "ANXT950\n" | int schema | int clientRevision | int nameLength | name | int x,y,plane
 * | items(backpack 28) | items(bank n) | [schema>=2: items(equipment 19) | byte kit]
 * | [schema>=3: int sectionCount, then per section: int tag | int length | payload]
 * </pre>
 * Schema-3 sections must appear exactly once, in tag order SKILLS(1) VITALS(2)
 * SETTINGS(3) APPEARANCE(4) IDENTITY(5). Schema4 adds SKILL_PROGRESS(6),
 * containing bounded Farming, Invention, Archaeology and Dungeoneering state.
 * A wrong tag, a length that does not
 * match the payload consumed, a payload above its cap or an unknown tag fails
 * the whole load. Schema 1 and 2 files decode as before with default sections.
 *
 * <p><b>schema and clientRevision are independent.</b> {@code schema} selects which
 * body layout is present; {@code clientRevision} records which client
 * protocol produced the state and selects nothing at all. This build writes
 * {@link Native950Save#SCHEMA_VERSION} / {@link Native950Save#CLIENT_REVISION} and
 * reads a schema of 1..SCHEMA_VERSION at revision 950 only.
 *
 * <p>Keeping the two axes separate still matters even with one accepted revision: the
 * schema axis is the one that has to keep working, because it is the migration path for
 * the next LAYOUT change. Conflating them would mean a protocol bump forcing a layout
 * bump, or the reverse.
 *
 * <p>The magic ({@code ANXT950}) and the {@code .950} extension are the CONTAINER's
 * identity. They moved with the port, unlike the 947 -> 950 change where they were
 * deliberately held back: there, orphaning every file on disk would have bricked real
 * characters for no gain. No character predating this format was ever in use, so there
 * is nothing left to stay compatible with, and a 950 server writing ANXT947 files was
 * its own source of confusion.
 *
 * <p>The SKILLS section is self-describing (a leading count byte), so widening
 * the model from 27 stats to the 947 cache's 29 is not a schema bump: a section
 * written at either width loads, and the older one is widened on the spot by
 * {@link Native950Save.Skills#upgraded}. See {@link #decodeSkills} and
 * {@code notes/SKILLS-persistence.md}.
 *
 * <p>Size caps: the schema-2 body is at most ~5.3 KB (600-slot bank); the
 * sections are capped individually ({@link #MAX_SKILLS_SECTION} 512,
 * {@link #MAX_VITALS_SECTION} 64, {@link #MAX_SETTINGS_SECTION} 2048,
 * {@link #MAX_APPEARANCE_SECTION} 128, {@link #MAX_IDENTITY_SECTION} 128) and
 * skill progress at {@link Native950SkillProgress#MAX_BYTES} 12 KB, and the whole
 * file at {@link #MAX_FILE_SIZE} 32 KB so a full bank and every section fit.
 */
public final class Native950SaveStore {
    /**
     * Container identity. Moved from ANXT947 with the format: this build reads and writes only
     * 950 profiles, so a file whose first eight bytes still say 947 is not an older version of
     * this container, it is a different one, and the magic says so before the revision int does.
     */
    private static final byte[] MAGIC = new byte[] {'A', 'N', 'X', 'T', '9', '5', '0', '\n'};
    private static final int DIGEST_SIZE = 32;
    public static final int MAX_FILE_SIZE = 32768;
    /**
     * The SKILLS section is one count byte plus ten bytes per stat, so the 29-stat
     * 947 model needs 1 + 29 * 10 = 291 bytes. 512 already covers that (it covered
     * the 27-stat 271 too) and is left alone deliberately: raising it would change
     * nothing on disk and would only widen what a corrupt length header can claim.
     * {@code Native950SaveStoreTest.maxSkillsSectionHoldsTheWholeStatTable} pins the
     * arithmetic so a future stat cannot silently overflow it.
     */
    public static final int MAX_SKILLS_SECTION = 512;
    public static final int MAX_VITALS_SECTION = 64;
    public static final int MAX_SETTINGS_SECTION = 2048;
    public static final int MAX_APPEARANCE_SECTION = 128;
    public static final int MAX_IDENTITY_SECTION = 128;

    static final int TAG_SKILLS = 1, TAG_VITALS = 2, TAG_SETTINGS = 3, TAG_APPEARANCE = 4, TAG_IDENTITY = 5;
    private static final int TAG_SKILL_PROGRESS=6;
    private static final int[] SECTION_ORDER_V3={TAG_SKILLS,TAG_VITALS,TAG_SETTINGS,TAG_APPEARANCE,TAG_IDENTITY};
    private static final int[] SECTION_ORDER = {TAG_SKILLS, TAG_VITALS, TAG_SETTINGS, TAG_APPEARANCE, TAG_IDENTITY,TAG_SKILL_PROGRESS};

    /** Stats already reported as over the engine experience ceiling, so the line is printed once each. */
    private static final java.util.Set<Integer> CLAMPED =
            java.util.Collections.newSetFromMap(new java.util.concurrent.ConcurrentHashMap<Integer, Boolean>());


    /** Suffix of the one-shot copy taken before the first rebalanced write over a pre-rebalance profile. */
    static final String PRE_REBALANCE_BACKUP_SUFFIX = ".pre29.bak";

    /**
     * Set by {@link #decodeSkills} while a decode on this thread is running when the file it
     * is reading predates the 947 skill rebalance in a way this build cannot round-trip: an
     * older-width SKILLS section, or a stored experience above the new ceiling that had to be
     * clamped. {@link #save} reads it straight after its protective {@link #load} - same
     * thread, same synchronized call - and takes a one-shot copy of the file before the
     * atomic replacement, so the original experience stays recoverable. A ThreadLocal rather
     * than a field on the returned save because the decoded value is the CLAMPED one; what is
     * being recorded is a property of the bytes on disk, not of the profile.
     */
    private static final ThreadLocal<Boolean> PRE_REBALANCE = new ThreadLocal<Boolean>();

    private final Path directory;

    public Native950SaveStore(Path directory) {
        this.directory = Objects.requireNonNull(directory, "directory").toAbsolutePath().normalize();
    }

    public synchronized Native950Save load(String username) throws IOException {
        // Cleared here, set by decodeSkills, read by save's backUpPreRebalanceProfile. A load
        // that never reaches a SKILLS section therefore reports no backup rather than a stale
        // one left by an earlier load on this thread.
        PRE_REBALANCE.remove();
        String canonical = Native950Save.canonicalUsername(username);
        validateDirectory(false);
        Path path = profilePath(canonical);
        BasicFileAttributes attributes;
        try { attributes = Files.readAttributes(path, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS); }
        catch (NoSuchFileException absent) { return null; }
        if (!attributes.isRegularFile())
            throw new IOException("Local character save is not a regular file");
        long size = attributes.size();
        if (size < MAGIC.length + DIGEST_SIZE || size > MAX_FILE_SIZE)
            throw new IOException("Local character save size is invalid");
        // The bound is enforced while reading too, in case a file changes after stat.
        ByteBuffer bytes = ByteBuffer.allocate(MAX_FILE_SIZE + 1);
        try (FileChannel channel = FileChannel.open(path, StandardOpenOption.READ, LinkOption.NOFOLLOW_LINKS)) {
            while (bytes.hasRemaining() && channel.read(bytes) != -1) { }
        }
        if (bytes.position() > MAX_FILE_SIZE) throw new IOException("Local character save is too large");
        return decode(Arrays.copyOf(bytes.array(), bytes.position()), canonical);
    }

    public synchronized void save(Native950Save save) throws IOException {
        Objects.requireNonNull(save, "save");
        byte[] bytes = encode(save);
        validateDirectory(true);
        // Do not turn a corrupt/incompatible save into a fresh profile implicitly.
        load(save.username());
        Path destination = profilePath(save.username());
        backUpPreRebalanceProfile(destination);
        Path temporary = Files.createTempFile(directory, ".native950-", ".tmp");
        try {
            try (FileChannel channel = FileChannel.open(temporary, StandardOpenOption.WRITE,
                    StandardOpenOption.TRUNCATE_EXISTING, LinkOption.NOFOLLOW_LINKS)) {
                ByteBuffer buffer = ByteBuffer.wrap(bytes);
                while (buffer.hasRemaining()) channel.write(buffer);
                channel.force(true);
            }
            Files.move(temporary, destination, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    /**
     * Takes one recoverable copy of a profile written before the 947 skill rebalance, the
     * first time this build is about to overwrite it. The flag comes from the protective
     * load {@link #save} has just performed on this thread.
     *
     * <p>Why it exists: the rebalance lowered {@link Native950Save#MAX_XP} from 2,000,000,000
     * to the client's own 200,000,000 clamp, and {@link #decodeSkills} clamps a stored value
     * above it on load rather than failing. The very next checkpoint rewrites the whole file,
     * so without this copy the pre-clamp experience would be gone from disk with nothing to
     * restore it from if the 200,000,000 ceiling ever turns out to be wrong.
     *
     * <p>This is about LOST DATA, not about version numbers, which is why a 947 -> 950 client
     * revision migration deliberately does NOT trigger it: that rewrite carries every field
     * across unchanged and only restamps the revision int, so there is nothing to recover. Both
     * existing triggers destroy something the rewrite cannot reproduce - a narrowed stat table
     * loses its recomputed levels, a clamped value loses the experience above the ceiling.
     *
     * <p>The copy is created, never replaced, so a second checkpoint cannot overwrite the good
     * backup with an already-clamped file, and a backup that already exists is left alone. A
     * failure to copy is reported and does not stop the save: refusing to write would lock the
     * owner out of a profile that loads perfectly well.
     */
    private void backUpPreRebalanceProfile(Path destination) {
        Boolean flagged = PRE_REBALANCE.get();
        PRE_REBALANCE.remove();
        if (flagged == null || !flagged.booleanValue()) return;
        Path backup = destination.resolveSibling(destination.getFileName().toString() + PRE_REBALANCE_BACKUP_SUFFIX);
        try {
            if (Files.exists(backup, LinkOption.NOFOLLOW_LINKS)) return;
            Files.copy(destination, backup, StandardCopyOption.COPY_ATTRIBUTES);
            System.out.println("[Ataraxia950] Copied the pre-rebalance profile to " + backup
                    + " before the first 29-stat write; the original experience is recoverable there");
        } catch (java.nio.file.FileAlreadyExistsException present) {
            // Another writer got there first; the original is already preserved.
        } catch (IOException failure) {
            System.out.println("[Ataraxia950] Could not back up the pre-rebalance profile to " + backup
                    + " (" + failure + "); saving anyway");
        }
    }

    private void validateDirectory(boolean create) throws IOException {
        if (create) Files.createDirectories(directory);
        try {
            if (!Files.readAttributes(directory, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS).isDirectory())
                throw new IOException("Local character save directory is not a real directory");
        } catch (NoSuchFileException absent) {
            if (create) throw absent;
        }
    }

    private Path profilePath(String canonical) {
        byte[] hash = digest(canonical.getBytes(StandardCharsets.US_ASCII));
        StringBuilder name = new StringBuilder(64 + 4);
        for (byte value : hash) name.append(Character.forDigit((value >>> 4) & 15, 16)).append(Character.forDigit(value & 15, 16));
        return directory.resolve(name.append(".950").toString());
    }

    // ------------------------------------------------------------------ encode

    static byte[] encode(Native950Save save) throws IOException {
        ByteArrayOutputStream payload = new ByteArrayOutputStream();
        try (DataOutputStream data = new DataOutputStream(payload)) {
            data.write(MAGIC);
            data.writeInt(Native950Save.SCHEMA_VERSION);
            data.writeInt(Native950Save.CLIENT_REVISION);
            byte[] name = save.username().getBytes(StandardCharsets.US_ASCII);
            data.writeInt(name.length); data.write(name);
            data.writeInt(save.x()); data.writeInt(save.y()); data.writeInt(save.plane());
            writeItems(data, save.inventoryIds(), save.inventoryAmounts());
            writeItems(data, save.bankIds(), save.bankAmounts());
            writeItems(data, save.equipmentIds(), save.equipmentAmounts());
            data.writeByte(save.equipmentKitClaimed() ? 1 : 0);
            data.writeInt(SECTION_ORDER.length);
            writeSection(data, TAG_SKILLS, encodeSkills(save.skills()), MAX_SKILLS_SECTION);
            writeSection(data, TAG_VITALS, encodeVitals(save.vitals()), MAX_VITALS_SECTION);
            writeSection(data, TAG_SETTINGS, encodeSettings(save.settings()), MAX_SETTINGS_SECTION);
            writeSection(data, TAG_APPEARANCE, encodeAppearance(save.appearance()), MAX_APPEARANCE_SECTION);
            writeSection(data, TAG_IDENTITY, encodeIdentity(save.identity()), MAX_IDENTITY_SECTION);
            writeSection(data,TAG_SKILL_PROGRESS,save.skillProgress().encode(),Native950SkillProgress.MAX_BYTES);
        }
        byte[] body = payload.toByteArray();
        if (body.length + DIGEST_SIZE > MAX_FILE_SIZE) throw new IOException("Local character save is too large");
        payload.write(digest(body));
        return payload.toByteArray();
    }

    private static void writeSection(DataOutputStream data, int tag, byte[] section, int cap) throws IOException {
        if (section.length > cap) throw new IOException("Local character save section " + tag + " exceeds " + cap + " bytes");
        data.writeInt(tag);
        data.writeInt(section.length);
        data.write(section);
    }

    private static void writeItems(DataOutputStream data, int[] ids, int[] amounts) throws IOException {
        data.writeInt(ids.length);
        for (int slot = 0; slot < ids.length; slot++) { data.writeInt(ids[slot]); data.writeInt(amounts[slot]); }
    }

    private static byte[] encodeSkills(Native950Save.Skills skills) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream(Native950Save.SKILL_COUNT * 10 + 1);
        try (DataOutputStream data = new DataOutputStream(bytes)) {
            data.writeByte(Native950Save.SKILL_COUNT);
            short[] levels = skills.levels();
            double[] xp = skills.xp();
            for (int skill = 0; skill < Native950Save.SKILL_COUNT; skill++) {
                data.writeShort(levels[skill]);
                data.writeDouble(xp[skill]);
            }
        }
        return bytes.toByteArray();
    }

    private static byte[] encodeVitals(Native950Save.Vitals vitals) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream(16);
        try (DataOutputStream data = new DataOutputStream(bytes)) {
            data.writeInt(vitals.hitpoints);
            data.writeInt(vitals.prayerPoints);
            data.writeInt(vitals.runEnergy);
            data.writeByte(vitals.running ? 1 : 0);
        }
        return bytes.toByteArray();
    }

    private static byte[] encodeSettings(Map<String, Integer> settings) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream(64);
        try (DataOutputStream data = new DataOutputStream(bytes)) {
            data.writeInt(settings.size());
            for (Map.Entry<String, Integer> entry : settings.entrySet()) {
                byte[] key = entry.getKey().getBytes(StandardCharsets.US_ASCII);
                data.writeByte(key.length);
                data.write(key);
                data.writeInt(entry.getValue());
            }
        }
        return bytes.toByteArray();
    }

    private static byte[] encodeAppearance(Native950Save.Appearance appearance) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream(48);
        try (DataOutputStream data = new DataOutputStream(bytes)) {
            data.writeByte(appearance.male ? 1 : 0);
            for (int colour : appearance.colours()) data.writeByte(colour);
            for (int kit : appearance.bodyKits()) data.writeInt(kit);
        }
        return bytes.toByteArray();
    }

    private static byte[] encodeIdentity(Native950Save.Identity identity) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream(64);
        try (DataOutputStream data = new DataOutputStream(bytes)) {
            byte[] name = identity.displayName.getBytes(StandardCharsets.US_ASCII);
            data.writeInt(name.length);
            data.write(name);
            data.writeLong(identity.created);
            data.writeLong(identity.lastLogin);
        }
        return bytes.toByteArray();
    }

    // ------------------------------------------------------------------ decode

    private static Native950Save decode(byte[] bytes, String expectedUsername) throws IOException {
        if (bytes.length < MAGIC.length + DIGEST_SIZE) throw new IOException("Truncated local character save");
        byte[] body = Arrays.copyOf(bytes, bytes.length - DIGEST_SIZE);
        byte[] checksum = Arrays.copyOfRange(bytes, body.length, bytes.length);
        if (!MessageDigest.isEqual(digest(body), checksum)) throw new IOException("Local character save checksum mismatch");
        try (DataInputStream data = new DataInputStream(new ByteArrayInputStream(body))) {
            byte[] magic = new byte[MAGIC.length]; data.readFully(magic);
            if (!Arrays.equals(magic, MAGIC)) throw new IOException("Unrecognized local character save format");
            int schema = data.readInt();
            if (schema < 1 || schema > Native950Save.SCHEMA_VERSION)
                throw new IOException("Unsupported local character save schema");
            // Two version axes, checked separately. The schema above says how these bytes are
            // arranged; this says which client protocol captured the state, and selects nothing.
            // One revision, no migration. Refusing here is unrecoverable by design - save()
            // loads protectively first, so a profile this rejects is unreadable AND unwritable -
            // and that is acceptable only because no character predating this format was ever in
            // use. See Native950Save.acceptsClientRevision.
            int revision = data.readInt();
            if (!Native950Save.acceptsClientRevision(revision))
                throw new IOException("Local character save client revision " + revision
                        + " is not one this build reads (expected "
                        + Native950Save.CLIENT_REVISION + ")");
            int nameLength = data.readInt();
            if (nameLength < 1 || nameLength > 12) throw new IOException("Invalid saved character name length");
            byte[] nameBytes = new byte[nameLength]; data.readFully(nameBytes);
            String name = new String(nameBytes, StandardCharsets.US_ASCII);
            if (!name.equals(expectedUsername)) throw new IOException("Local character save belongs to a different profile");
            int x = data.readInt(), y = data.readInt(), plane = data.readInt();
            int inventoryLength = data.readInt();
            if (inventoryLength != Native950Save.INVENTORY_SIZE) throw new IOException("Invalid saved backpack length");
            int[] inventoryIds = new int[inventoryLength], inventoryAmounts = new int[inventoryLength];
            readItems(data, inventoryIds, inventoryAmounts);
            int bankLength = data.readInt();
            if (bankLength < 0 || bankLength > Native950Save.BANK_CAPACITY) throw new IOException("Invalid saved bank length");
            int[] bankIds = new int[bankLength], bankAmounts = new int[bankLength];
            readItems(data, bankIds, bankAmounts);
            int[] equipmentIds = new int[Native950Save.EQUIPMENT_SIZE];
            int[] equipmentAmounts = new int[Native950Save.EQUIPMENT_SIZE];
            Arrays.fill(equipmentIds, -1);
            boolean equipmentKitClaimed = false;
            if (schema >= 2) {
                if (data.readInt() != Native950Save.EQUIPMENT_SIZE)
                    throw new IOException("Invalid saved equipment length");
                readItems(data, equipmentIds, equipmentAmounts);
                int claimed = data.readUnsignedByte();
                if (claimed != 0 && claimed != 1) throw new IOException("Invalid saved equipment kit claim");
                equipmentKitClaimed = claimed == 1;
            }
            Native950Save.Skills skills = null;
            Native950Save.Vitals vitals = null;
            Map<String, Integer> settings = null;
            Native950Save.Appearance appearance = null;
            Native950Save.Identity identity = null;
            Native950SkillProgress progress=Native950SkillProgress.EMPTY;
            if (schema >= 3) {
                int[] order=schema>=4?SECTION_ORDER:SECTION_ORDER_V3;
                if (data.readInt() != order.length) throw new IOException("Invalid saved section count");
                for (int expectedTag : order) {
                    int tag = data.readInt();
                    if (tag != expectedTag) throw new IOException("Unexpected saved section " + tag + " (wanted " + expectedTag + ")");
                    int length = data.readInt();
                    if (length < 0 || length > sectionCap(tag)) throw new IOException("Invalid saved section " + tag + " length " + length);
                    byte[] section = new byte[length];
                    data.readFully(section);
                    try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(section))) {
                        switch (tag) {
                        case TAG_SKILLS: skills = decodeSkills(in); break;
                        case TAG_VITALS: vitals = decodeVitals(in); break;
                        case TAG_SETTINGS: settings = decodeSettings(in); break;
                        case TAG_APPEARANCE: appearance = decodeAppearance(in); break;
                        case TAG_IDENTITY: identity = decodeIdentity(in); break;
                        case TAG_SKILL_PROGRESS: progress=Native950SkillProgress.decode(in);break;
                        default: throw new IOException("Unknown saved section " + tag);
                        }
                        if (in.read() != -1) throw new IOException("Trailing bytes in saved section " + tag);
                    }
                }
            }
            if (data.read() != -1) throw new IOException("Unexpected trailing local character save data");
            return new Native950Save(name, x, y, plane, inventoryIds, inventoryAmounts, bankIds, bankAmounts,
                    equipmentIds, equipmentAmounts, equipmentKitClaimed, skills, vitals, settings, appearance, identity,progress);
        } catch (IllegalArgumentException invalid) {
            throw new IOException("Invalid local character save state", invalid);
        }
    }

    private static int sectionCap(int tag) throws IOException {
        switch (tag) {
        case TAG_SKILLS: return MAX_SKILLS_SECTION;
        case TAG_VITALS: return MAX_VITALS_SECTION;
        case TAG_SETTINGS: return MAX_SETTINGS_SECTION;
        case TAG_APPEARANCE: return MAX_APPEARANCE_SECTION;
        case TAG_IDENTITY: return MAX_IDENTITY_SECTION;
        case TAG_SKILL_PROGRESS:return Native950SkillProgress.MAX_BYTES;
        default: throw new IOException("Unknown saved section " + tag);
        }
    }

    private static void readItems(DataInputStream data, int[] ids, int[] amounts) throws IOException {
        for (int slot = 0; slot < ids.length; slot++) { ids[slot] = data.readInt(); amounts[slot] = data.readInt(); }
    }

    /**
     * The SKILLS section, at the current width or at the older
     * {@link Native950Save#LEGACY_SKILL_COUNT} one. No other count is accepted,
     * so a truncated or invented width is still a hard failure.
     *
     * <p>An older-width section is widened by {@link Native950Save.Skills#upgraded},
     * which keeps every stored experience value exactly and recomputes each level
     * from the current per-stat curve. An experience value above the current
     * engine ceiling ({@link Native950Save#MAX_XP}, which the 947 rebalance lowers
     * to the client's own 200,000,000 clamp) is clamped to it with a line naming
     * the stat rather than failing the load, because a profile written under the
     * old 2,000,000,000 ceiling is exactly the profile this upgrade exists for.
     */
    private static Native950Save.Skills decodeSkills(DataInputStream in) throws IOException {
        int count = in.readUnsignedByte();
        if (count != Native950Save.SKILL_COUNT && count != Native950Save.LEGACY_SKILL_COUNT)
            throw new IOException("Invalid saved skill count " + count);
        // An older-width section is by definition pre-rebalance; a clamped value marks the
        // rest. Either way the next write cannot reproduce these bytes, so flag the copy.
        if (count != Native950Save.SKILL_COUNT) PRE_REBALANCE.set(Boolean.TRUE);
        short[] levels = new short[count];
        double[] xp = new double[count];
        for (int skill = 0; skill < count; skill++) {
            levels[skill] = in.readShort();
            xp[skill] = clampExperience(skill, in.readDouble());
        }
        if (count == Native950Save.SKILL_COUNT) return new Native950Save.Skills(levels, xp);
        return Native950Save.Skills.upgraded(levels, xp);
    }

    /** Clamps a stored experience value to the engine ceiling, saying so once per stat. */
    private static double clampExperience(int skill, double stored) {
        if (Double.isNaN(stored) || Double.isInfinite(stored) || stored <= Native950Save.MAX_XP) return stored;
        PRE_REBALANCE.set(Boolean.TRUE);
        String message = "[Ataraxia950] Saved experience for stat " + skill + " (" + stored
                + ") is above the 947 ceiling " + Native950Save.MAX_XP + "; clamping it on load";
        if (CLAMPED.add(Integer.valueOf(skill))) System.out.println(message);
        return Native950Save.MAX_XP;
    }

    private static Native950Save.Vitals decodeVitals(DataInputStream in) throws IOException {
        int hitpoints = in.readInt(), prayer = in.readInt(), energy = in.readInt();
        int running = in.readUnsignedByte();
        if (running != 0 && running != 1) throw new IOException("Invalid saved run toggle");
        return new Native950Save.Vitals(hitpoints, prayer, energy, running == 1);
    }

    private static Map<String, Integer> decodeSettings(DataInputStream in) throws IOException {
        int count = in.readInt();
        if (count < 0 || count > Native950Save.MAX_SETTINGS) throw new IOException("Invalid saved settings count");
        Map<String, Integer> settings = new LinkedHashMap<String, Integer>();
        for (int i = 0; i < count; i++) {
            int keyLength = in.readUnsignedByte();
            if (keyLength < 1 || keyLength > Native950Save.MAX_SETTING_KEY) throw new IOException("Invalid saved setting key length");
            byte[] key = new byte[keyLength];
            in.readFully(key);
            String name = new String(key, StandardCharsets.US_ASCII);
            if (settings.put(name, in.readInt()) != null) throw new IOException("Duplicate saved setting " + name);
        }
        return settings;
    }

    private static Native950Save.Appearance decodeAppearance(DataInputStream in) throws IOException {
        int male = in.readUnsignedByte();
        if (male != 0 && male != 1) throw new IOException("Invalid saved gender");
        int[] colours = new int[Native950Save.COLOUR_COUNT];
        for (int i = 0; i < colours.length; i++) colours[i] = in.readUnsignedByte();
        int[] kits = new int[Native950Save.BODY_KIT_COUNT];
        for (int i = 0; i < kits.length; i++) kits[i] = in.readInt();
        return new Native950Save.Appearance(male == 1, colours, kits);
    }

    private static Native950Save.Identity decodeIdentity(DataInputStream in) throws IOException {
        int length = in.readInt();
        if (length < 1 || length > Native950Save.MAX_DISPLAY_NAME) throw new IOException("Invalid saved display name length");
        byte[] name = new byte[length];
        in.readFully(name);
        return new Native950Save.Identity(new String(name, StandardCharsets.US_ASCII), in.readLong(), in.readLong());
    }

    private static byte[] digest(byte[] input) {
        try { return MessageDigest.getInstance("SHA-256").digest(input); }
        catch (NoSuchAlgorithmException impossible) { throw new AssertionError(impossible); }
    }
}
