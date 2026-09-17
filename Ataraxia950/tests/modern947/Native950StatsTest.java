package modern947;

import com.rs.game.WorldTile;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.client.Native950IdMap;
import com.rs.game.player.client.Native950PacketDispatcher;
import com.rs.network.modern.Native950GameTransport;
import com.rs.network.protocol.modern950.Native950Protocol;
import io.netty.buffer.ByteBuf;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * M3 engine path: what the real 910 {@code Skills}, {@code Prayer},
 * {@code CombatDefinitions} and {@code Player} code puts on the wire for a native
 * 947 player.
 *
 * <p>Cache-free and hermetic: {@code Native950IdMap.reset()} in both
 * {@code @Before} and {@code @After} restores identity id resolution, so no
 * binding table and no cache is needed, and the strict flag is restored too.
 * Frames are read straight off an {@link EmbeddedChannel} carrying the real
 * {@link Native950GameTransport} with a zero ISAAC stream, so what these tests
 * see is the byte stream the client would receive.
 *
 * <p>Layout references (all CONFIRMED, {@code OpenNXT/data/prot/947/generated/native947-3/verified}):
 * UPDATE_STAT 66 (WHOLE experience big-endian, {@code 128 - level}, {@code skill + 128};
 * STAT_DEFINITIONS.md 3b.1 corrects UPDATE_STAT.md's "tenths" reading - the live stat
 * table at {@code [[owner+0x198E0]+0x7618]} is built with entry flag 0 at 0x1400CDF25,
 * and setter 0x140369C10 scans a flag-zero entry's level from the raw value);
 * VARBIT_LARGE 71 and VARP_SMALL 10 ({@code vars/*.md}); UPDATE_RUNENERGY 116.
 * VARP_LARGE 111 (adrenaline only, the one value here too wide for VARP_SMALL) is
 * NOT confirmed - {@code PROTOCOL-S-SUMMARY.md} section 3 records it as awaiting a
 * verdict - and VARBIT_SMALL 50, in the same position, is no longer written at all.
 * Var ids from {@code ui/ORBS_AND_VARS.md} and
 * {@code ui/SKILLS_TAB.md}: hitpoints 1668, prayer 16736, summoning 41524,
 * adrenaline varp 679, auto-retaliate varp 462, run toggle varp 463,
 * virtual levels 19007.
 */
public final class Native950StatsTest {

    private static final int HITPOINTS_VARP = 13537;
    private static final int PRAYER_VARBIT = 16736;
    private static final int SUMMONING_VARBIT = 41524;
    private static final int VIRTUAL_LEVELS_VARBIT = 19007;
    private static final int ADRENALINE_VARP = 679;
    private static final int AUTO_RETALIATE_VARP = 462;
    private static final int RUN_TOGGLE_VARP = 463;

    private EmbeddedChannel channel;
    private Player player;
    private Native950PacketDispatcher packets;
    private boolean strictBefore;

    @Before
    public void attach() {
        strictBefore = Native950PacketDispatcher.isStrict();
        Native950PacketDispatcher.setStrict(true);
        Native950IdMap.reset();
        channel = new EmbeddedChannel(new Native950GameTransport(() -> 0, () -> 0, Thread.currentThread()));
        player = Player.createNative950("stats-test", new WorldTile(3222, 3222, 0), channel);
        packets = (Native950PacketDispatcher) player.getPackets();
        drain(); // discard anything admission itself wrote
    }

    @After
    public void detach() {
        Native950IdMap.reset();
        Native950PacketDispatcher.setStrict(strictBefore);
        channel.finishAndReleaseAll();
    }

    // ------------------------------------------------------------------ skills

    /**
     * The whole login burst, and nothing else. One UPDATE_STAT frame per modelled
     * skill - which is now all 29 the cache defines, 27 Archaeology and 28 Necromancy
     * included, because the skill model was rebuilt against the cache stat definitions
     * - plus the two CONFIRMED companion vars. Any other frame here would be an
     * unverified emitter reaching the wire on login.
     */
    @Test
    public void theNativeLoginBurstIsOneUpdateStatPerModelledSkillPlusTheTwoConfirmedVars() {
        Skills skills = player.getSkills();
        skills.setXpWithoutRefresh(Skills.ATTACK, 101333); // level 50
        skills.setLevelWithoutRefresh(Skills.ATTACK, 50);
        drain();

        skills.init();
        List<Frame> frames = drain();

        Set<Integer> statsSeen = new TreeSet<Integer>();
        Set<Integer> varbitsSeen = new TreeSet<Integer>();
        for (Frame frame : frames) {
            if (frame.opcode == opcode(Native950Protocol.ServerPacket.UPDATE_STAT)) {
                assertTrue("UPDATE_STAT sent twice for skill " + frame.statSkill(), statsSeen.add(frame.statSkill()));
            } else if (frame.isVarbit()) {
                varbitsSeen.add(frame.varbitId());
            } else {
                org.junit.Assert.fail("Unexpected packet in the native login burst: " + frame);
            }
        }
        Set<Integer> expectedStats = new TreeSet<Integer>();
        for (int skill = 0; skill < Skills.SKILL_NAME.length; skill++) expectedStats.add(skill);
        assertEquals("one UPDATE_STAT per modelled skill", expectedStats, statsSeen);
        assertEquals("every stat the cache defines is sent", Skills.SKILL_COUNT, statsSeen.size());
        assertTrue("27 Archaeology reaches the wire", statsSeen.contains(Skills.ARCHAEOLOGY));
        assertTrue("28 Necromancy reaches the wire", statsSeen.contains(Skills.NECROMANCY));
        Set<Integer> expectedVarbits = new TreeSet<Integer>();
        expectedVarbits.add(VIRTUAL_LEVELS_VARBIT);
        expectedVarbits.add(SUMMONING_VARBIT);
        assertEquals("only the CONFIRMED companion vars", expectedVarbits, varbitsSeen);

        Frame attack = only(frames, Native950Protocol.ServerPacket.UPDATE_STAT, Skills.ATTACK);
        assertEquals(50, attack.statLevel());
        assertEquals(101333L, attack.statExperience());
    }

    /**
     * A fresh character's two new stats are not merely counted in the burst, they
     * carry the right initial state: current level 1 and no experience, which is
     * also what the client's own stat-table initialiser holds
     * ({@code verified/UPDATE_STAT.md}: the table at
     * {@code [[owner+0x198E0]+0x7618]} is built with xp 0, base 1, current 1).
     * Before the 29-stat model the server sent nothing for them and the tab drew
     * those defaults by accident; now it draws them because they were transmitted,
     * which is what makes an award to either stat visible at all.
     */
    @Test
    public void theLoginBurstCarriesArchaeologyAndNecromancyAtLevelOneWithNoExperience() {
        Skills skills = player.getSkills();
        assertEquals(0.0, skills.getXp(Skills.ARCHAEOLOGY), 0.0);
        assertEquals(0.0, skills.getXp(Skills.NECROMANCY), 0.0);
        drain();

        skills.init();
        List<Frame> frames = drain();
        for (int stat : new int[] {Skills.ARCHAEOLOGY, Skills.NECROMANCY}) {
            Frame frame = only(frames, Native950Protocol.ServerPacket.UPDATE_STAT, stat);
            assertEquals("stat " + stat + " starts at current level 1", 1, frame.statLevel());
            assertEquals("stat " + stat + " starts with no experience", 0L, frame.statExperience());
        }
    }

    /**
     * The two new stats are ordinary stats on the wire: the burst carries their
     * stored experience and their CURRENT (here boosted) level, exactly
     * like Attack. 101,333 is level 50 on the cache default curve, which both
     * Archaeology and Necromancy use (neither stat definition sets
     * {@code flags & 0x4}, so neither carries an explicit table id).
     */
    @Test
    public void theNewStatsCarryTheirOwnExperienceAndBoostedLevel() {
        Skills skills = player.getSkills();
        skills.setXpWithoutRefresh(Skills.ARCHAEOLOGY, 101333);
        skills.setLevelWithoutRefresh(Skills.ARCHAEOLOGY, 55); // a boost above the base 50
        skills.setXpWithoutRefresh(Skills.NECROMANCY, 13034431);
        skills.setLevelWithoutRefresh(Skills.NECROMANCY, 99);
        drain();

        skills.init();
        List<Frame> frames = drain();
        assertEquals(50, skills.getLevelForXp(Skills.ARCHAEOLOGY));
        Frame archaeology = only(frames, Native950Protocol.ServerPacket.UPDATE_STAT, Skills.ARCHAEOLOGY);
        assertEquals("the boosted current level, not the base", 55, archaeology.statLevel());
        assertEquals(101333L, archaeology.statExperience());
        Frame necromancy = only(frames, Native950Protocol.ServerPacket.UPDATE_STAT, Skills.NECROMANCY);
        assertEquals(99, necromancy.statLevel());
        assertEquals(13034431L, necromancy.statExperience());
    }

    /**
     * The client recomputes the base level from the experience (UPDATE_STAT.md,
     * setter 0x140369C10), so the level byte must be the CURRENT one: green when
     * boosted, red when drained. Sending the base instead would hide every boost.
     */
    @Test
    public void updateStatCarriesTheBoostedOrDrainedCurrentLevelNotTheBase() {
        Skills skills = player.getSkills();
        skills.setXpWithoutRefresh(Skills.STRENGTH, 101333); // base level 50
        skills.setLevelWithoutRefresh(Skills.STRENGTH, 50);
        drain();

        skills.set(Skills.STRENGTH, 55); // a boost
        Frame boosted = only(drain(), Native950Protocol.ServerPacket.UPDATE_STAT, Skills.STRENGTH);
        assertEquals(50, skills.getLevelForXp(Skills.STRENGTH));
        assertEquals("the boosted current level is on the wire", 55, boosted.statLevel());
        assertEquals("the experience is unchanged and in whole points", 101333L, boosted.statExperience());

        skills.set(Skills.STRENGTH, 45); // a drain
        Frame drained = only(drain(), Native950Protocol.ServerPacket.UPDATE_STAT, Skills.STRENGTH);
        assertEquals(45, drained.statLevel());
        assertEquals(101333L, drained.statExperience());
    }

    /** One changed skill, one UPDATE_STAT; the experience it carries is the stored one. */
    @Test
    public void setXpEmitsExactlyOneUpdateStatForTheChangedSkill() {
        Skills skills = player.getSkills();
        drain();
        skills.setXp(Skills.MINING, 13034431); // level 99
        List<Frame> frames = drain();
        Frame mining = only(frames, Native950Protocol.ServerPacket.UPDATE_STAT, Skills.MINING);
        assertEquals(13034431L, mining.statExperience());
        assertEquals(1, count(frames, Native950Protocol.ServerPacket.UPDATE_STAT));
    }

    /**
     * An experience award must store the experience and emit for that skill exactly
     * once.
     *
     * <p>{@code Skills.addXp} itself cannot run in a cache-free JVM: its very first
     * multiplier block reaches {@code SkillingPets.rollForPetDrop}, whose class
     * initialiser calls {@code Utils.getItemDefinitionsSize()} and therefore needs
     * an open cache. {@code silentAddXp} is the same award path minus the
     * multipliers and listeners - same experience write, same level-up branch, same
     * single {@code refresh(skill)} tail - so it is what this hermetic test uses.
     * The full {@code addXp} is exercised against the real cache in
     * {@code Native950PersistenceSmoke}, which also proves the award persists.
     */
    @Test
    public void addingExperiencePersistsItAndEmitsOneUpdateStat() {
        Skills skills = player.getSkills();
        double before = skills.getXp(Skills.FISHING);
        drain();

        skills.silentAddXp(Skills.FISHING, 500);
        double gained = skills.getXp(Skills.FISHING) - before;

        assertTrue("the award must add experience, saw " + gained, gained > 0);
        List<Frame> frames = drain();
        assertEquals("exactly one UPDATE_STAT for the changed skill", 1,
                count(frames, Native950Protocol.ServerPacket.UPDATE_STAT));
        Frame fishing = only(frames, Native950Protocol.ServerPacket.UPDATE_STAT, Skills.FISHING);
        assertEquals("the frame carries the stored experience",
                skills.getXp(Skills.FISHING), (double) fishing.statExperience(), 1.0);
        assertEquals("and the current level", skills.getLevel(Skills.FISHING), fishing.statLevel());
    }

    // ------------------------------------------------------------------ vitals

    /** 950 script 16856 still defines level x100; script 8122 now reads varp 13537. */
    @Test
    public void hitpointsUseThe950VarpAndRetainTheDerivedLifePointScale() {
        // Match the real binding table's distinction: the new varp is allowed;
        // old varbit 1668 has been recycled and must never be sent for hitpoints.
        Native950IdMap.install(new Native950IdMap.Resolver() {
            @Override public int interfaceId(int id) { return id; }
            @Override public int componentId(int id, int component) { return component; }
            @Override public int varp(int id) { return id == HITPOINTS_VARP ? id : -1; }
            @Override public int varbit(int id) { return -1; }
            @Override public int varc(int id) { return -1; }
            @Override public int script(int id) { return id; }
            @Override public int container(int id) { return id; }
        });
        long dropped = packets.counters().totalDropped();
        player.setHitpoints(20);
        player.refreshHitPoints();
        List<Frame> small = drain();
        assertEquals(1, small.size());
        assertEquals(200, onlyVarp(small, HITPOINTS_VARP).varpValue());

        player.getSkills().setLevelWithoutRefresh(Skills.HITPOINTS, 99);
        player.setHitpoints(990);
        player.refreshHitPoints();
        List<Frame> full = drain();
        assertEquals(1, full.size());
        assertEquals(9900, onlyVarp(full, HITPOINTS_VARP).varpValue());
        assertEquals("the production allow-list must accept the migrated caller", dropped,
                packets.counters().totalDropped());
    }

    @Test
    public void fullWidthHitpointsDoNotRetainThe947VarbitCeilingOrOverflow() {
        player.setHitpoints(4000);
        player.refreshHitPoints();
        assertEquals(40000, onlyVarp(drain(), HITPOINTS_VARP).varpValue());
        player.setHitpoints(Integer.MAX_VALUE);
        player.refreshHitPoints();
        assertEquals(Integer.MAX_VALUE, onlyVarp(drain(), HITPOINTS_VARP).varpValue());
        player.setHitpoints(0);
        player.refreshHitPoints();
        assertEquals(0, onlyVarp(drain(), HITPOINTS_VARP).varpValue());
    }

    @Test
    public void prayerPointsEmitVarbit16736InTenths() {
        assertNotNull("hydration owns a Prayer", player.getPrayer());
        player.getSkills().setXpWithoutRefresh(Skills.PRAYER, 13034431); // level 99
        player.getSkills().setLevelWithoutRefresh(Skills.PRAYER, 99);
        player.getPrayer().setPrayerpoints(0);
        drain();

        player.getPrayer().restorePrayer(700);
        Frame restored = onlyVarbit(drain(), PRAYER_VARBIT);
        assertEquals(700, player.getPrayer().getPrayerpoints());
        assertEquals(opcode(Native950Protocol.ServerPacket.VARBIT_LARGE), restored.opcode);
        assertEquals("points x10", 7000, restored.varbitValue());

        player.getPrayer().drainPrayer(680, true);
        Frame drained = onlyVarbit(drain(), PRAYER_VARBIT);
        assertEquals(20, player.getPrayer().getPrayerpoints());
        assertEquals(opcode(Native950Protocol.ServerPacket.VARBIT_LARGE), drained.opcode);
        assertEquals(200, drained.varbitValue());
    }

    @Test
    public void summoningPointsEmitVarbit41524WhenTheSummoningLevelChanges() {
        Skills skills = player.getSkills();
        skills.setXpWithoutRefresh(Skills.SUMMONING, 0);
        drain();

        skills.set(Skills.SUMMONING, 2);
        List<Frame> frames = drain();
        assertEquals(1, count(frames, Native950Protocol.ServerPacket.UPDATE_STAT));
        Frame small = onlyVarbit(frames, SUMMONING_VARBIT);
        assertEquals(opcode(Native950Protocol.ServerPacket.VARBIT_LARGE), small.opcode);
        assertEquals("level x100 tenths", 200, small.varbitValue());

        skills.set(Skills.SUMMONING, 5);
        Frame large = onlyVarbit(drain(), SUMMONING_VARBIT);
        assertEquals(opcode(Native950Protocol.ServerPacket.VARBIT_LARGE), large.opcode);
        assertEquals(500, large.varbitValue());
    }

    /** Each 950 varp uses the frame wide enough for its signed current value. */
    @Test
    public void varpFrameWidthTracksEachCurrentValueIncludingLifePoints() {
        player.setHitpoints(20);
        player.getCombatDefinitions().setAutoRetaliate(true);
        player.setRunHidden(false);
        drain();

        player.refreshHitPoints();
        player.getCombatDefinitions().refreshAutoRelatie();
        player.setRun(true);
        List<Frame> frames = drain();
        assertEquals(200, onlyVarp(frames, HITPOINTS_VARP).varpValue());
        assertEquals(0, onlyVarp(frames, AUTO_RETALIATE_VARP).varpValue());
        assertEquals(1, onlyVarp(frames, RUN_TOGGLE_VARP).varpValue());
        for (Frame frame : frames) {
            assertTrue("this state burst contains only varps", frame.isVarp());
            int value = frame.varpValue();
            assertEquals("200 life points needs the large frame; run and retaliate fit a byte",
                    opcode(value >= -128 && value <= 127
                            ? Native950Protocol.ServerPacket.VARP_SMALL
                            : Native950Protocol.ServerPacket.VARP_LARGE), frame.opcode);
        }

        player.setHitpoints(10);
        player.refreshHitPoints();
        Frame smallHp = onlyVarp(drain(), HITPOINTS_VARP);
        assertEquals(100, smallHp.varpValue());
        assertEquals(opcode(Native950Protocol.ServerPacket.VARP_SMALL), smallHp.opcode);

        player.getCombatDefinitions().setSpecialAttackPercentage(75);
        drain();
        player.getCombatDefinitions().refreshSpecialAttackPercentage();
        Frame adrenaline = onlyVarp(drain(), ADRENALINE_VARP);
        assertEquals("750 cannot fit VARP_SMALL's signed byte",
                opcode(Native950Protocol.ServerPacket.VARP_LARGE), adrenaline.opcode);
        assertEquals(750, adrenaline.varpValue());
    }

    /** Adrenaline (varp 679, 0..1000) and auto-retaliate (varp 462, 1 means OFF). */
    @Test
    public void adrenalineAndAutoRetaliateEmitTheirVerifiedVarps() {
        player.getCombatDefinitions().setSpecialAttackPercentage(75);
        drain();
        player.getCombatDefinitions().refreshSpecialAttackPercentage();
        assertEquals(750, onlyVarp(drain(), ADRENALINE_VARP).varpValue());

        player.getCombatDefinitions().setAutoRetaliate(true);
        drain();
        player.getCombatDefinitions().refreshAutoRelatie();
        assertEquals("varp 462 value 1 means auto-retaliate OFF", 0, onlyVarp(drain(), AUTO_RETALIATE_VARP).varpValue());
    }

    /** Run energy is its own fixed-size packet; the run toggle is varp 463. */
    @Test
    public void runEnergyAndTheRunToggleEmitTheirVerifiedPackets() {
        player.setRunEnergyWithoutRefresh(100);
        drain();

        player.setRunEnergy(63);
        Frame energy = only(drain(), Native950Protocol.ServerPacket.UPDATE_RUNENERGY);
        assertEquals(63, energy.body[0] & 0xff);
        assertEquals(63, player.getRunEnergy());

        player.setRunHidden(false);
        drain();
        player.setRun(true);
        assertEquals("0 walk / 1 run / 3 rest", 1, onlyVarp(drain(), RUN_TOGGLE_VARP).varpValue());
    }

    /** The whole drain / restore loop the native tick runs, end to end. */
    @Test
    public void theRunEnergyDrainAndRestoreLoopReachesTheClient() {
        player.setRunEnergyWithoutRefresh(50);
        player.setRunHidden(true);
        drain();

        for (int i = 0; i < 10; i++) player.drainRunEnergy();
        assertTrue("drain must have moved the energy", player.getRunEnergy() < 50);
        assertTrue("and each step emitted", count(drain(), Native950Protocol.ServerPacket.UPDATE_RUNENERGY) > 0);

        int drained = player.getRunEnergy();
        // restoreRunEnergy refuses while the player is mid-run-step. A Player that has
        // never ticked still holds nextRunDirection at its 0 default; Entity.processMovement
        // (which Native950Session runs through processEntityUpdate every tick) is what
        // sets it to -1 for a stationary player.
        player.processMovement();
        for (int i = 0; i < 10; i++) player.restoreRunEnergy();
        assertTrue("restore must have moved it back", player.getRunEnergy() > drained);
        assertTrue(count(drain(), Native950Protocol.ServerPacket.UPDATE_RUNENERGY) > 0);
    }

    // ------------------------------------------------------------------ fail closed

    /**
     * Promotion of these families to REAL writers must not weaken the allow-list:
     * an id the resolver rejects is still a counted drop and never a wire write.
     */
    @Test
    public void anIdTheResolverRejectsIsACountedDropNotAWireWrite() {
        Native950IdMap.install(new Native950IdMap.Resolver() {
            @Override public int interfaceId(int interfaceId) { return interfaceId; }
            @Override public int componentId(int interfaceId, int componentId) { return componentId; }
            @Override public int varp(int id) { return id == RUN_TOGGLE_VARP || id == HITPOINTS_VARP ? -1 : id; }
            @Override public int varbit(int id) { return id; }
            @Override public int varc(int id) { return -1; }
            @Override public int script(int id) { return id; }
            @Override public int container(int id) { return id; }
        });
        long droppedVarbits = packets.counters().dropped("sendConfigByFile");
        long droppedVarps = packets.counters().dropped("sendConfig");
        drain();

        player.refreshHitPoints();
        player.setRunHidden(false);
        player.setRun(true);

        assertTrue("nothing may reach the wire for an unbound id", drain().isEmpty());
        assertEquals(droppedVarbits, packets.counters().dropped("sendConfigByFile"));
        assertEquals(droppedVarps + 2, packets.counters().dropped("sendConfig"));
        assertEquals("a rejected id is a drop, never a strict throw", 0, packets.counters().totalStrictHits());
    }

    // ------------------------------------------------------------------ frame plumbing

    private static int opcode(Native950Protocol.ServerPacket packet) {
        return packet.opcode();
    }

    private static int sizeOf(int opcode) {
        for (Native950Protocol.ServerPacket packet : Native950Protocol.ServerPacket.values())
            if (packet.opcode() == opcode) return packet.size();
        throw new AssertionError("Unknown server opcode " + opcode + " on the wire");
    }

    /** Flushes the channel and decodes every framed packet waiting on it. */
    private List<Frame> drain() {
        channel.flush();
        List<Frame> frames = new ArrayList<Frame>();
        for (ByteBuf buffer = channel.readOutbound(); buffer != null; buffer = channel.readOutbound()) {
            try {
                byte[] bytes = new byte[buffer.readableBytes()];
                buffer.readBytes(bytes);
                decode(bytes, frames);
            } finally {
                buffer.release();
            }
        }
        return frames;
    }

    private static void decode(byte[] bytes, List<Frame> out) {
        int cursor = 0;
        while (cursor < bytes.length) {
            int opcode = bytes[cursor++] & 0xff;
            int size = sizeOf(opcode);
            if (size == -1) size = bytes[cursor++] & 0xff;
            else if (size == -2) {
                size = ((bytes[cursor] & 0xff) << 8) | (bytes[cursor + 1] & 0xff);
                cursor += 2;
            }
            byte[] body = new byte[size];
            System.arraycopy(bytes, cursor, body, 0, size);
            cursor += size;
            out.add(new Frame(opcode, body));
        }
    }

    private static int count(List<Frame> frames, Native950Protocol.ServerPacket packet) {
        int total = 0;
        for (Frame frame : frames) if (frame.opcode == packet.opcode()) total++;
        return total;
    }

    private static Frame only(List<Frame> frames, Native950Protocol.ServerPacket packet) {
        Frame found = null;
        for (Frame frame : frames)
            if (frame.opcode == packet.opcode()) {
                assertTrue(packet + " was emitted more than once: " + frames, found == null);
                found = frame;
            }
        assertNotNull("Expected one " + packet + " frame, saw " + frames, found);
        return found;
    }

    private static Frame only(List<Frame> frames, Native950Protocol.ServerPacket packet, int skill) {
        Frame found = null;
        for (Frame frame : frames)
            if (frame.opcode == packet.opcode() && frame.statSkill() == skill) {
                assertTrue("skill " + skill + " emitted more than once: " + frames, found == null);
                found = frame;
            }
        assertNotNull("Expected an UPDATE_STAT for skill " + skill + ", saw " + frames, found);
        return found;
    }

    private static Frame onlyVarbit(List<Frame> frames, int varbitId) {
        Frame found = null;
        Set<Integer> seen = new HashSet<Integer>();
        for (Frame frame : frames)
            if (frame.isVarbit()) {
                seen.add(frame.varbitId());
                if (frame.varbitId() == varbitId) {
                    assertTrue("varbit " + varbitId + " emitted more than once: " + frames, found == null);
                    found = frame;
                }
            }
        assertNotNull("Expected one write of varbit " + varbitId + ", saw varbits " + seen + " in " + frames, found);
        return found;
    }

    private static Frame onlyVarp(List<Frame> frames, int varpId) {
        Frame found = null;
        Set<Integer> seen = new HashSet<Integer>();
        for (Frame frame : frames)
            if (frame.isVarp()) {
                seen.add(frame.varpId());
                if (frame.varpId() == varpId) {
                    assertTrue("varp " + varpId + " emitted more than once: " + frames, found == null);
                    found = frame;
                }
            }
        assertNotNull("Expected one write of varp " + varpId + ", saw varps " + seen + " in " + frames, found);
        return found;
    }

    /** One decoded server frame; the accessors implement the verified read layouts. */
    private static final class Frame {
        final int opcode;
        final byte[] body;

        Frame(int opcode, byte[] body) { this.opcode = opcode; this.body = body; }

        boolean isVarbit() {
            return opcode == Native950Protocol.ServerPacket.VARBIT_SMALL.opcode()
                    || opcode == Native950Protocol.ServerPacket.VARBIT_LARGE.opcode();
        }

        boolean isVarp() {
            return opcode == Native950Protocol.ServerPacket.VARP_LARGE.opcode()
                    || opcode == Native950Protocol.ServerPacket.VARP_SMALL.opcode();
        }

        /**
         * 950 VARBIT_SMALL: b0 value, then the id little-endian at b1..b2.
         * 950 VARBIT_LARGE: b0..b3 value big-endian, then the id little-endian at b4..b5.
         *
         * Both moved: 947 led with the id in each form, and VARBIT_LARGE biased its id's low
         * byte by +128 and wrote the value little-endian.
         */
        int varbitId() {
            if (opcode == Native950Protocol.ServerPacket.VARBIT_SMALL.opcode())
                return (body[1] & 0xff) | ((body[2] & 0xff) << 8);
            return (body[4] & 0xff) | ((body[5] & 0xff) << 8);
        }

        int varbitValue() {
            if (opcode == Native950Protocol.ServerPacket.VARBIT_SMALL.opcode())
                return (body[0] - 128) & 0xff;
            return ((body[0] & 0xff) << 24) | ((body[1] & 0xff) << 16)
                    | ((body[2] & 0xff) << 8) | (body[3] & 0xff);
        }

        /**
         * 950 VARP_SMALL: b0 value, b1 id>>>8, b2 id+128.
         * 950 VARP_LARGE: b0 id+128, b1 id>>>8 (ushortle128), then the value under intv1.
         */
        int varpId() {
            if (opcode == Native950Protocol.ServerPacket.VARP_SMALL.opcode())
                return ((body[1] & 0xff) << 8) | ((body[2] - 128) & 0xff);
            return ((body[1] & 0xff) << 8) | ((body[0] - 128) & 0xff);
        }

        /** VARP_SMALL carries a plain signed byte on 950; VARP_LARGE keeps the b2 b3 b0 b1 order. */
        int varpValue() {
            if (opcode == Native950Protocol.ServerPacket.VARP_SMALL.opcode())
                return body[0];
            return ((body[4] & 0xff) << 24) | ((body[5] & 0xff) << 16)
                    | ((body[2] & 0xff) << 8) | (body[3] & 0xff);
        }

        /** 950 UPDATE_STAT: b0 -skill, b1 -level, b2..b5 experience big-endian. */
        long statExperience() {
            return ((long) (body[2] & 0xff) << 24) | ((body[3] & 0xff) << 16)
                    | ((body[4] & 0xff) << 8) | (body[5] & 0xff);
        }

        int statLevel() { return -body[1] & 0xff; }

        int statSkill() { return -body[0] & 0xff; }

        @Override public String toString() {
            StringBuilder out = new StringBuilder("opcode ").append(opcode).append(" [");
            for (int i = 0; i < body.length; i++) out.append(String.format("%02x", body[i] & 0xff));
            return out.append(']').toString();
        }
    }
}
