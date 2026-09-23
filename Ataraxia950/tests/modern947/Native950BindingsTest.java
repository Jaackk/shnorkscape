package modern947;

import com.google.gson.JsonElement;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.rs.cache.Cache;
import com.rs.cache.loaders.VarBitDefinitions;
import com.rs.game.WorldTile;
import com.rs.game.player.Player;
import com.rs.game.player.VarsManager;
import com.rs.game.player.client.ui.Native950Bindings;
import com.rs.game.player.client.ui.Native950CacheReader;
import com.rs.game.player.content.VarBitManager;
import io.netty.channel.embedded.EmbeddedChannel;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Validates the 950 UI binding table against a scripted cache: the real JSON
 * must load and every lookup resolve, and every mismatch class must reject the
 * whole table. The real cache is only touched by Native950BindingsProbe.
 */
public class Native950BindingsTest {
    @Test public void combatResourceVarsReachTheRealWriterThroughTheStrictTable() throws Exception {
        String json=table();Native950Bindings bindings=load(json,FakeCache.fromTable(json));
        com.rs.game.player.client.Native950IdMap.Resolver previous=com.rs.game.player.client.Native950IdMap.current();
        EmbeddedChannel channel=new EmbeddedChannel(new com.rs.network.modern.Native950GameTransport(()->0,()->0,Thread.currentThread()));
        try {
            final Native950Bindings.Resolver r=bindings.allowListResolver();
            com.rs.game.player.client.Native950IdMap.install(new com.rs.game.player.client.Native950IdMap.Resolver(){
                public int interfaceId(int id){return r.interfaceId(id);}public int componentId(int id,int c){return r.componentId(id,c);}
                public int varp(int id){return r.varp(id);}public int varbit(int id){return r.varbit(id);}
                public int varc(int id){return -1;}public int script(int id){return r.script(id);}public int container(int id){return r.container(id);}
            });
            Player player=Player.createNative950("combat-wire",new WorldTile(3222,3222,0),channel);
            player.getVarsManager().setNativeVarpSink((id,value)->player.getPackets().sendConfig(id,value));
            for(int[] pair:new int[][]{{4164,14},{5861,1003},{10986,12},{11035,5},{4164,0},{11035,0},{10986,0}}){
                player.getVarsManager().sendVar(pair[0],pair[1]);channel.flushOutbound();
                io.netty.buffer.ByteBuf frame=channel.readOutbound();
                assertNotNull("Runtime binding dropped combat varp "+pair[0],frame);
                try {byte[] actual=new byte[frame.readableBytes()];frame.readBytes(actual);
                    assertArrayEquals((pair[1]>=-128&&pair[1]<=127?com.rs.network.protocol.modern950.Native950Packets.varpSmall(pair[0],pair[1]):com.rs.network.protocol.modern950.Native950Packets.varp(pair[0],pair[1])).frame(()->0),actual);
                }finally{frame.release();}
            }
        }finally{com.rs.game.player.client.Native950IdMap.install(previous);channel.finishAndReleaseAll();}
    }
    private static final int ROOT = 1477;
    /** 910 ids with no group in index 3 of the 950 cache. */
    private static final int[] ABSENT = { 1578, 1607, 1628, 1680, 1929 };
    /**
     * 910 ids the 950 cache does hold, under unrelated content: 1530 is the 910
     * Invention interface and on 950 that group is a neighbourhood-invite popup.
     * They must be rejected exactly like the absent ones, by both policies.
     */
    private static final int[] REASSIGNED = { 1530 };

    /**
     * A cache that answers what the 950 cache answers for the ids this table
     * touches: group counts read from 950RevTest/cache, pins from the table under
     * test, slot structs resolved to the verified 1477 hashes. Anything not
     * registered is absent.
     */
    static final class FakeCache implements Native950CacheReader {
        final Map<Integer, Integer> interfaceCounts = new HashMap<Integer, Integer>();
        final Set<String> files = new HashSet<String>();
        final Map<String, String> hashes = new HashMap<String, String>();
        final Map<Integer, Integer> enumValues = new HashMap<Integer, Integer>();
        final Map<String, Integer> structParams = new HashMap<String, Integer>();
        final Map<Integer, int[]> varbits = new HashMap<Integer, int[]>();
        /** 2/60 on the 950 cache runs to id 13542; 947 stopped at 12797. */
        int varpCount = 13543;

        static FakeCache fromTable(String json) {
            FakeCache cache = new FakeCache();
            // File counts read from 950RevTest/cache index 3 on 2026-09-10. Three grew
            // in the 950 recompile (1477 923->924, 1465 43->44, 1430 269->271), which is
            // why a component index the 947 fake accepted as out of range may now be in
            // range; the rejection tests below use the 950 numbers.
            int[][] counts = { {1477, 924}, {1482, 2}, {1465, 44}, {1473, 23}, {1462, 37}, {517, 341}, {137, 263},
                               {1466, 15}, {1430, 271}, {1448, 32}, {1213, 97}, {1919, 3} };
            // Compass1919 was mounted after the initial fixture; actual950 index3 has three files (12 Sep).
            for (int[] c : counts) cache.interfaceCounts.put(c[0], c[1]);
            JsonObject root = JsonParser.parseString(json).getAsJsonObject();
            for (Map.Entry<String, JsonElement> e : root.getAsJsonObject("interfaces").entrySet()) {
                if (e.getKey().startsWith("_")) continue;
                JsonObject o = e.getValue().getAsJsonObject();
                int id = o.get("id").getAsInt();
                if (o.has("sha256")) cache.hashes.put(key(3, id, 0), o.get("sha256").getAsString());
                if (o.has("componentHashes"))
                    for (Map.Entry<String, JsonElement> h : o.getAsJsonObject("componentHashes").entrySet())
                        cache.hashes.put(key(3, id, Integer.parseInt(h.getKey())), h.getValue().getAsString());
            }
            // Reassigned 910 ids: the group must be PRESENT with the pinned file count
            // and digest, the opposite of the absent list.
            for (Map.Entry<String, JsonElement> e : root.getAsJsonObject("reassignedInterfaces").entrySet()) {
                if (e.getKey().startsWith("_")) continue;
                JsonObject o = e.getValue().getAsJsonObject();
                int id = o.get("id").getAsInt();
                cache.interfaceCounts.put(id, o.get("files").getAsInt());
                cache.hashes.put(key(3, id, 0), o.get("sha256").getAsString());
            }
            for (Map.Entry<String, JsonElement> e : root.getAsJsonObject("scripts").entrySet()) {
                if (e.getKey().startsWith("_")) continue;
                JsonObject o = e.getValue().getAsJsonObject();
                cache.hashes.put(key(12, o.get("id").getAsInt(), 0), o.get("sha256").getAsString());
            }
            for (Map.Entry<String, JsonElement> e : root.getAsJsonObject("containers").entrySet()) {
                if (e.getKey().startsWith("_")) continue;
                JsonObject o = e.getValue().getAsJsonObject();
                cache.files.add(key(2, 5, o.get("id").getAsInt()));
                if (o.has("sha256")) cache.hashes.put(key(2, 5, o.get("id").getAsInt()), o.get("sha256").getAsString());
            }
            // Verified slot resolutions (inventory-cache-bindings.md, EQUIPMENT_CONTENT.md,
            // scene-interface-cache.json, ui/UI-EVIDENCE-SUMMARY.md s4.1).
            cache.slot(1000, 21275, 30, 28);
            cache.slot(1004, 21278, 94, 92);
            cache.slot(2, 21285, 103, 101);
            cache.slot(3, 29115, 114, 112);
            cache.slot(18, 21279, 420, 418);   // 21301 here was a stale fixture: enum 7716 key 18 is struct 21279 on both the 947 and 950 caches
            cache.slot(1017, 21308, 695, 693);
            cache.slot(0, 21293, 300, 298);      // skills
            cache.slot(1003, 21277, 70, 67);     // action bar
            cache.slot(1026, 30143, 668, 666);   // paired950 XP popups
            // Base varp and bit range exactly as the 950 cache decodes them: re-read from
            // 950RevTest/cache 2/69 with the loader's own decoder on 2026-09-10. All ten
            // decode identically on the 947 and 950 caches. The table pins these, so a
            // fake with invented bases would reject it.
            // Current950 bank control fields, independently decoded in bank-ui-cache-950-evidence.json.
            cache.varbits.put(45139, new int[] {110, 20, 22});
            cache.varbits.put(45190, new int[] {8958, 4, 4});
            cache.varbits.put(45191, new int[] {8958, 5, 7});
            cache.varbits.put(45911, new int[] {10243, 0, 0});
            cache.varbits.put(45189, new int[] {8958, 0, 3});
            cache.varbits.put(45141, new int[] {110, 27, 30});
            cache.varbits.put(45158, new int[] {7755, 4, 4});
            cache.varbits.put(18797, new int[] {1772, 0, 0});
            cache.varbits.put(16736, new int[] {3274, 0, 14});
            cache.varbits.put(41524, new int[] {8040, 0, 14});
            cache.varbits.put(19007, new int[] {458, 30, 30});
            cache.varbits.put(27168, new int[] {3680, 20, 20});
            cache.varbits.put(27169, new int[] {3680, 21, 21});
            cache.varbits.put(30224, new int[] {5987, 7, 7}); // paired950 Invention pouch unlock
            cache.varbits.put(54934, new int[] {2180, 4, 4});
            cache.varbits.put(22875, new int[] {3814, 5, 11});
            cache.varbits.put(228, new int[] {94, 15, 15});
            // Independently decoded current950 crop loc morph ranges (protocol-analysis/farming-vars-950.tsv).
            for(int i=0;i<8;i++)cache.varbits.put(52+i,new int[]{12+i/4,8*(i%4),8*(i%4)+7});
            for(int i=0;i<4;i++){cache.varbits.put(60+i,new int[]{14,8*i,8*i+7});cache.varbits.put(72+i,new int[]{16,8*i,8*i+7});}
            for(int i=0;i<3;i++)cache.varbits.put(124+i,new int[]{23,8*i,8*i+7});
            // Varbit 1668 is NOT bound any more: on 950 it decodes to varp 13489 bits
            // 15..22 (8 bits), not the 947 varp 659 bits 1..15 the hitpoints binding used,
            // and the life-points readers moved to varp 13537. It is registered here with
            // its real 950 definition so the resolver tests can prove the table turns it
            // down even though the cache still holds it.
            cache.varbits.put(1668, new int[] {13489, 15, 22});
            return cache;
        }

        void slot(int key, int struct, int attach, int wrapper) {
            enumValues.put(key, struct);
            structParams.put(struct + ":3505", ROOT << 16 | attach);
            structParams.put(struct + ":3503", ROOT << 16 | wrapper);
        }

        static String key(int index, int group, int file) { return index + "/" + group + "/" + file; }

        @Override public boolean groupExists(int index, int group) {
            if (index == 3) return interfaceCounts.containsKey(group);
            if (index == 12) return hashes.containsKey(key(12, group, 0));
            if (index == 2) return group == 5 || group == 60 || group == 69;
            return false;
        }

        @Override public int fileCount(int index, int group) {
            if (index == 3) { Integer c = interfaceCounts.get(group); return c == null ? -1 : c; }
            if (index == 2 && group == 60) return varpCount;
            if (index == 2 && group == 69) return 61889;
            return groupExists(index, group) ? 1 : -1;
        }

        @Override public boolean fileExists(int index, int group, int file) {
            if (file < 0) return false;
            if (index == 3) return groupExists(3, group) && file < interfaceCounts.get(group);
            if (index == 2 && group == 60) return file < varpCount;
            if (index == 2 && group == 69) return varbits.containsKey(file);
            if (index == 2 && group == 5) return files.contains(key(2, 5, file));
            if (index == 12) return groupExists(12, group) && file == 0;
            return false;
        }

        @Override public String sha256(int index, int group, int file) {
            if (!fileExists(index, group, file)) return null;
            String pinned = hashes.get(key(index, group, file));
            return pinned != null ? pinned : "0000000000000000000000000000000000000000000000000000000000000000";
        }

        @Override public int enumInt(int enumId, int key) {
            if (enumId != 7716) return -1;
            Integer v = enumValues.get(key);
            return v == null ? -1 : v;
        }

        @Override public int structInt(int structId, int param) {
            Integer v = structParams.get(structId + ":" + param);
            return v == null ? -1 : v;
        }

        @Override public int[] varbit(int varbitId) {
            int[] v = varbits.get(varbitId);
            return v == null ? null : v.clone();
        }
    }

    private static String table() throws Exception {
        InputStream in = Native950Bindings.class.getResourceAsStream(Native950Bindings.RESOURCE);
        assertNotNull("resources/native950/ui-bindings-950.json must be on the classpath", in);
        try {
            java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            for (int n; (n = in.read(buffer)) > 0; ) out.write(buffer, 0, n);
            return new String(out.toByteArray(), StandardCharsets.UTF_8);
        } finally { in.close(); }
    }

    private static Native950Bindings load(String json, FakeCache cache) {
        return Native950Bindings.load(new StringReader(json), cache);
    }

    private static String rejection(String json, FakeCache cache) {
        try {
            load(json, cache);
            fail("A mismatched table must be rejected");
            return null;
        } catch (IllegalStateException expected) {
            return expected.getMessage();
        }
    }

    @Test public void tableLoadsFromTheClasspathAndEveryLookupResolves() throws Exception {
        String json = table();
        Native950Bindings b = load(json, FakeCache.fromTable(json));
        assertEquals(ROOT, b.rootInterfaceId());
        assertEquals(517, b.interfaceId("bank"));
        assertEquals(1473, b.interfaceId("backpack"));
        assertEquals(5, b.component("backpack", "items"));
        assertEquals(31, b.component("worn_equipment", "items"));
        assertEquals(201, b.component("bank", "items"));
        assertEquals(15, b.component("bank", "inventory"));
        assertEquals(317, b.component("bank", "close"));
        assertEquals(39, b.component("bank", "deposit_all"));
        assertEquals(86, b.component("all_chat", "history"));
        assertEquals(45189, b.var("bank_quantity").id);
        assertTrue(b.var("bank_quantity").isVarbit());
        assertEquals(8971, b.var("bank_occupied_count").id);
        assertTrue(b.var("bank_occupied_count").isVarp());
        assertEquals(1362, b.script("chat_init"));
        assertEquals(8471, b.script("equipment_layout"));
        assertEquals(4252, b.script("buff_timer_duration"));
        assertEquals(10624, b.script("buff_timer_visible"));
        assertEquals(ROOT << 16 | 103, b.slotAttach("backpack"));
        assertEquals(ROOT << 16 | 101, b.slotWrapper("backpack"));
        assertEquals(ROOT << 16 | 695, b.slotAttach("bank"));
        assertEquals(ROOT << 16 | 114, b.slotAttach("worn_equipment"));
        assertEquals(1462 << 16 | 3, b.resolve("worn_equipment.root"));
        assertEquals(94, b.resolve("container:equipment"));
        assertEquals(18, b.resolve("slotkey:all_chat"));
        assertEquals(150, b.scriptNames().size());
        for (String name : b.slotNames()) { assertTrue(b.slotAttach(name) >= 0); assertEquals(ROOT, b.slotAttach(name) >>> 16); assertEquals(ROOT, b.slotWrapper(name) >>> 16); }
        for (String name : b.interfaceNames()) for (String comp : b.iface(name).components.keySet()) assertTrue(b.component(name, comp) >= 0);
        for (String name : b.varNames()) assertTrue(b.var(name).id >= 0);
        for (String name : b.scriptNames()) assertTrue(b.script(name) >= 0);
        for (String name : b.opNames()) {
            Native950Bindings.Op op = b.ops(name);
            assertTrue(name + " must only use verified packets", op.available);
            assertFalse(op.steps.isEmpty());
            for (Native950Bindings.Step step : op.steps) assertTrue(op.packetsRequired.contains(step.packet));
        }
        Native950Bindings.Op bootstrapSlots = b.ops("bootstrap.slots");
        int backpackEvents = 0;
        for (Native950Bindings.Step step : bootstrapSlots.steps) {
            if (!"IF_SETEVENTS".equals(step.packet) || !"backpack.items".equals(step.string("component"))) continue;
            backpackEvents++;
            assertEquals(0, step.integer("from")); assertEquals(27, step.integer("to"));
            assertEquals(com.rs.game.player.client.Native950InventoryMenu.EVENT_MASK, step.integer("settings"));
        }
        assertEquals(1, backpackEvents);
        Native950Bindings.Op open = b.ops("bank.open");
        assertEquals(45, open.steps.size());
        assertEquals("VARBIT_SMALL", open.steps.get(0).packet);
        assertEquals(2, open.steps.get(0).integer("value"));
        assertEquals("slot:bank.wrapper", open.steps.get(3).string("parent"));
        assertEquals(8970, b.var("bank_first_empty_slot").id);
        assertFalse(open.steps.get(3).bool("walkable"));
        boolean bankEvents=false;
        for(Native950Bindings.Step step:open.steps) {
            if("IF_SETEVENTS".equals(step.packet) && "bank.items".equals(step.string("component"))) {
                assertEquals(com.rs.game.player.client.Native950BankUi.ITEM_EVENTS,step.integer("settings")); bankEvents=true;
            }
        }
        assertTrue(bankEvents);
        assertEquals(2, b.ops("bank.close").steps.size());
        assertEquals(7, b.ops("equipment.bootstrap").steps.size());
        assertEquals(5, b.ops("equipment.bootstrap").steps.get(0).list("args").size());
        int[] withdraw = b.optionAmounts("bank", "withdraw");
        assertEquals(1, withdraw[1]); assertEquals(5, withdraw[3]); assertEquals(10, withdraw[4]); assertEquals(Integer.MAX_VALUE, withdraw[7]); assertEquals(0, withdraw[5]);
        try { b.interfaceId("hero"); fail(); } catch (IllegalArgumentException expected) { }
        try { b.component("bank", "frame"); fail(); } catch (IllegalArgumentException expected) { }
        try { b.resolve("slot:bank"); fail(); } catch (IllegalArgumentException expected) { }
    }

    /**
     * M3 (stats, vitals, run energy, skills tab). Everything asserted here is a
     * CONFIRMED row of {@code verified/ui/UI-EVIDENCE-SUMMARY.md}; the CANDIDATE
     * rows next to them (skills panel 320, XP tracker1015 and run-click1413)
     * stay absent. XP popups1026 now have paired950 component/script evidence.
     */
    @Test public void m3SlotsInterfacesAndVarsAreBoundExactlyAsTheEvidenceStates() throws Exception {
        String json = table();
        Native950Bindings b = load(json, FakeCache.fromTable(json));

        // Slots: enum 7716 key -> struct -> params 3505 attach / 3503 wrapper.
        assertEquals(0, b.slot("skills").enumKey);
        assertEquals(21293, b.slot("skills").structId);
        assertEquals(ROOT << 16 | 300, b.slotAttach("skills"));
        assertEquals(ROOT << 16 | 298, b.slotWrapper("skills"));
        assertEquals(1003, b.slot("action_bar").enumKey);
        assertEquals(21277, b.slot("action_bar").structId);
        assertEquals(ROOT << 16 | 70, b.slotAttach("action_bar"));
        assertEquals(ROOT << 16 | 67, b.slotWrapper("action_bar"));
        // The wrapper the pre-M3 table omitted for the minimap slot.
        assertEquals(ROOT << 16 | 94, b.slotAttach("minimap"));
        assertEquals(ROOT << 16 | 92, b.slotWrapper("minimap"));

        // Interfaces and their named components.
        assertEquals(1466, b.interfaceId("skills"));
        assertEquals(4, b.component("skills", "current_level"));
        assertEquals(5, b.component("skills", "base_level"));
        assertEquals(7, b.component("skills", "cells"));
        assertEquals(9, b.component("skills", "bottom_bar"));
        assertEquals(11, b.component("skills", "total_level"));
        assertEquals(12, b.component("skills", "combat_level"));
        assertEquals(1430, b.interfaceId("action_bar"));
        assertEquals(7, b.component("action_bar", "hp_bar"));
        assertEquals(14, b.component("action_bar", "prayer_bar"));
        assertEquals(20, b.component("action_bar", "summoning_bar"));
        assertEquals(54, b.component("action_bar", "auto_retaliate_icon"));
        assertEquals(55, b.component("action_bar", "adrenaline_bar"));
        assertEquals(57, b.component("action_bar", "auto_retaliate_button"));
        // 950 cache icon19 onLoad2304 names run15, bar18 and percentage20.
        assertEquals(18, b.component("minimap", "energy_bar"));
        assertEquals(19, b.component("minimap", "run_orb_icon"));
        assertEquals(15, b.component("minimap", "run_orb"));
        assertEquals(16, b.component("minimap", "run_orb_ring"));
        assertEquals(20, b.component("minimap", "energy_text"));

        // Vars, with the base varp and bit range the 950 cache decodes.
        // hitpoints is the one binding the 947->950 port had to re-derive rather than
        // re-pin: 950 script 8122 reads varp 13537 where the 947 build read varbit 1668,
        // and every one of the eleven scripts that read 1668 on 947 reads 13537 on 950.
        assertTrue("hitpoints is a varp on 950, not a varbit", b.var("hitpoints").isVarp());
        assertEquals(13537, b.var("hitpoints").id);
        assertEquals("a varp declares no base varp of its own", -1, b.var("hitpoints").varp);
        assertEquals(-1, b.var("hitpoints").bitWidth());
        assertVarbit(b, "prayer_points", 16736, 3274, 0, 14, 32767);
        assertVarbit(b, "summoning_points", 41524, 8040, 0, 14, 32767);
        assertVarbit(b, "virtual_levels", 19007, 458, 30, 30, 1);
        assertVarbit(b, "interface_mode", 27169, 3680, 21, 21, 1);
        assertVarbit(b, "excalibur_prioritize_wield", 54934, 2180, 4, 4, 1);
        assertVarbit(b, "legacy_combat_mode", 27168, 3680, 20, 20, 1);
        assertVarbit(b, "interface_skin", 22875, 3814, 5, 11, 127);
        assertTrue(b.var("adrenaline").isVarp());
        assertEquals(679, b.var("adrenaline").id);
        assertEquals(1000, b.var("adrenaline").maxValue);
        assertEquals(463, b.var("run_toggle").id);
        assertEquals(4, b.var("run_toggle").maxValue);
        assertEquals(462, b.var("auto_retaliate").id);
        assertEquals(1, b.var("auto_retaliate").maxValue);
        assertEquals(-1, b.var("run_toggle").varp); // only varbits carry a base varp

        // Pinned readers named by the evidence.
        assertEquals(8488, b.script("skills_tab_build"));
        assertEquals(8489, b.script("skills_cell_build"));
        assertEquals(1522, b.script("skills_bottom_bar"));
        assertEquals(11849, b.script("skills_level_from_xp"));
        assertEquals(2141, b.script("skills_slot_table"));
        assertEquals(8391, b.script("skills_slot_install"));
        assertEquals(8122, b.script("orb_hp_update"));
        assertEquals(8123, b.script("orb_prayer_update"));
        assertEquals(8124, b.script("orb_summoning_update"));
        assertEquals(8125, b.script("orb_adrenaline_update"));
        assertEquals(1315, b.script("run_orb_init"));
        assertEquals(1741, b.script("run_orb_redraw"));
        assertEquals(2306, b.script("run_energy_bar"));
        assertEquals(8129, b.script("auto_retaliate_toggle"));

        // CANDIDATE rows stay out of the table until a live test settles them.
        for (String name : b.interfaceNames()) assertNotEquals(320, b.iface(name).id);
        for (String name : b.slotNames()) {
            int key = b.slot(name).enumKey;
            assertNotEquals("XP tracker slot 1015 is CANDIDATE", 1015, key);

        }
        for (String name : b.varNames()) assertNotEquals("run-click gate varc 1413 is CANDIDATE", 1413, b.var(name).id);

        // Every M3 id passes the allow-list resolver; a neighbouring one does not.
        Native950Bindings.Resolver allow = b.allowListResolver();
        assertEquals(1213, allow.interfaceId(1213));
        assertEquals(ROOT << 16 | 668, b.slotAttach("xp_popups"));
        assertEquals(ROOT << 16 | 666, b.slotWrapper("xp_popups"));
        assertEquals(636, allow.componentId(ROOT, 636));
        assertEquals(637, allow.componentId(ROOT, 637));
        assertEquals(228, allow.varbit(228));
        assertEquals(1466, allow.interfaceId(1466));
        assertEquals(1430, allow.interfaceId(1430));
        assertEquals(7, allow.componentId(1430, 7));
        assertEquals(-1, allow.componentId(1430, 8));
        assertEquals(18, allow.componentId(1465, 18));
        assertEquals(-1, allow.componentId(1465, 14));
        assertEquals(13537, allow.varp(13537));
        assertEquals("varbit 1668 exists on 950 but means something else now", -1, allow.varbit(1668));
        assertEquals(16736, allow.varbit(16736));
        assertEquals(41524, allow.varbit(41524));
        assertEquals(19007, allow.varbit(19007));
        assertEquals(463, allow.varp(463));
        assertEquals(462, allow.varp(462));
        assertEquals(679, allow.varp(679));
        assertEquals(4252, allow.script(4252));
        assertEquals(10624, allow.script(10624));
        // The equipment/combat bonus varps and varcs M3 deliberately retired
        // (notes/M3-bindings.md section 3) must stay unbound.
        for (int varp : new int[] {711, 712, 713, 714, 715, 716, 717, 718, 3561, 3562, 3563, 3596})
            assertEquals("bonus varp " + varp + " is retired, not bound", -1, allow.varp(varp));
        for (int varc : new int[] {779, 2911, 5886, 5896, 5906})
            assertEquals("bonus varc " + varc + " is retired, not bound", -1, allow.varc(varc));
        assertEquals("no varc is declared yet, so every varc is rejected", -1, allow.varc(5));
        assertTrue(allow.unmatched("varc") > 0);
    }

    private static void assertVarbit(Native950Bindings b, String name, int id, int varp, int lo, int hi, int max) {
        Native950Bindings.Var v = b.var(name);
        assertTrue(name + " must be a varbit", v.isVarbit());
        assertEquals(id, v.id);
        assertEquals(varp, v.varp);
        assertEquals(lo, v.startBit);
        assertEquals(hi, v.endBit);
        assertEquals(hi - lo + 1, v.bitWidth());
        assertEquals(max, v.maxValue);
    }

    /**
     * A varbit that moved onto a different varp, or onto a different bit range, must
     * reject the whole table: writing the declared bits of the wrong varp would
     * silently corrupt an unrelated player variable rather than fail.
     */
    @Test public void aVarbitThatMovedVarpOrBitRangeRejectsTheWholeTable() throws Exception {
        String json = table();
        // This is the check that caught the real one. On the 947 table hitpoints was
        // varbit 1668 on varp 659; the 950 cache decodes 1668 as varp 13489 bits 15..22,
        // so the table was rejected instead of writing life points into eight bits of an
        // unrelated varp. Re-run here against a varbit that is still bound.
        FakeCache cache = FakeCache.fromTable(json);
        cache.varbits.put(41524, new int[] {8041, 0, 14});
        String message = rejection(json, cache);
        assertTrue(message, message.contains("varbit summoning_points (41524) declares base varp 8040 but the 950 cache decodes varp 8041"));

        cache = FakeCache.fromTable(json);
        cache.varbits.put(16736, new int[] {3274, 1, 15});
        message = rejection(json, cache);
        assertTrue(message, message.contains("varbit prayer_points (16736) declares bits 0..14 but the 950 cache decodes 1..15"));

        // The width check, which is what made the hitpoints move impossible to paper over:
        // a 15-bit maximum does not fit the eight bits 1668 now occupies.
        cache = FakeCache.fromTable(json);
        cache.varbits.put(16736, new int[] {3274, 15, 22});
        message = rejection(json, cache);
        assertTrue(message, message.contains("varbit prayer_points (16736) is 8 bit(s) wide in the 950 cache; maxValue 32767 does not fit"));

        // A varbit with no declared base varp at all is a table bug, not a silent pass.
        JsonObject withoutRange = JsonParser.parseString(json).getAsJsonObject();
        JsonObject virtualLevels = withoutRange.getAsJsonObject("vars").getAsJsonObject("virtual_levels");
        assertEquals(458, virtualLevels.get("varp").getAsInt());
        assertEquals("30..30", virtualLevels.get("bits").getAsString());
        virtualLevels.remove("varp");
        virtualLevels.remove("bits");
        String stripped = withoutRange.toString();
        message = rejection(stripped, FakeCache.fromTable(json));
        assertTrue(message, message.contains("varbit virtual_levels (19007) must declare its bit range"));
        assertTrue(message, message.contains("varbit virtual_levels (19007) must declare the base varp"));
    }

    /** The M3 component pins and component ranges reject the same way the bank ones do. */
    @Test public void m3ComponentPinsAndRangesRejectTheWholeTable() throws Exception {
        String json = table();
        String hpPin = "beed8d642f8209c08a1002715ba48be7568321863cc1e737482107176ac97709"; // 1430:7, 950 pin
        assertTrue(json.contains(hpPin));
        String message = rejection(json.replace(hpPin, "0eed8d642f8209c08a1002715ba48be7568321863cc1e737482107176ac97709"),
                FakeCache.fromTable(json));
        assertTrue(message, message.contains("interface action_bar (1430) component 7 changed"));
        assertTrue(message, message.contains("re-verify"));

        String skillsPin = "6933909c87c4008b08f0e09118316d6b76b8e80a875dfe4eec95f515f0d0a5fa"; // 1466 file 0
        assertTrue(json.contains(skillsPin));
        message = rejection(json.replace(skillsPin, "1933909c87c4008b08f0e09118316d6b76b8e80a875dfe4eec95f515f0d0a5fa"),
                FakeCache.fromTable(json));
        assertTrue(message, message.contains("interface skills (1466) file 0 changed"));

        // A component index past the group's file count is a layout change.
        assertTrue(json.contains("\"combat_level\": 12"));
        message = rejection(json.replace("\"combat_level\": 12", "\"combat_level\": 15"), FakeCache.fromTable(json));
        assertTrue(message, message.contains("interface skills (1466) component combat_level = 15 is outside the 950 cache's 15 components"));
        // 1430 grew from 269 to 271 files in the 950 recompile, so the out-of-range index
        // this test uses had to move with it -- 269 is a real component on 950.
        message = rejection(json.replace("\"auto_retaliate_button\": 57", "\"auto_retaliate_button\": 271"), FakeCache.fromTable(json));
        assertTrue(message, message.contains("interface action_bar (1430) component auto_retaliate_button = 271 is outside the 950 cache's 271 components"));

        // A slot whose struct resolves somewhere else than the verified hash.
        FakeCache cache = FakeCache.fromTable(json);
        cache.structParams.put("21293:3503", ROOT << 16 | 299);
        message = rejection(json, cache);
        assertTrue(message, message.contains("slot skills resolved wrapper 1477:299 but the verified binding is 1477:298"));
        cache = FakeCache.fromTable(json);
        cache.enumValues.remove(1003);
        assertTrue(rejection(json, cache).contains("enum 7716 has no key 1003"));
    }

    @Test public void modifiedScriptHashRejectsTheWholeTable() throws Exception {
        String json = table();
        FakeCache cache = FakeCache.fromTable(json);
        String pin = "8c1dd80480365074a78744de4ca397bb7d809936b0b87811dab26b9398705ac1"; // chat_init 1362, 950 pin
        assertTrue(json.contains(pin));
        String message = rejection(json.replace(pin, "9c1dd80480365074a78744de4ca397bb7d809936b0b87811dab26b9398705ac1"), cache);
        assertTrue(message, message.contains("script chat_init (1362) changed"));
        assertTrue(message, message.contains("re-verify"));
        String interfacePin = "0693acb772e6db8cd24787c393e3831316d21614b7cae484b1e8aea0c0547ecd"; // bank 517 file 0
        message = rejection(json.replace(interfacePin, "1693acb772e6db8cd24787c393e3831316d21614b7cae484b1e8aea0c0547ecd"), cache);
        assertTrue(message, message.contains("interface bank (517) file 0 changed"));
    }

    @Test public void outOfRangeComponentRejectsTheWholeTable() throws Exception {
        String json = table();
        FakeCache cache = FakeCache.fromTable(json);
        assertTrue(json.contains("\"close\": 317"));
        String message = rejection(json.replace("\"close\": 317", "\"close\": 341"), cache);
        assertTrue(message, message.contains("interface bank (517) component close = 341 is outside the 950 cache's 341 components"));
        // A smaller cache group than the table needs is a layout change, not a lookup miss.
        cache.interfaceCounts.put(1473, 22);
        message = rejection(json, cache);
        assertTrue(message, message.contains("interface backpack (1473) has 22 components"));
    }

    @Test public void slotResolutionMustBeAFullRootHashNotAMaskedComponent() throws Exception {
        String json = table();
        FakeCache cache = FakeCache.fromTable(json);
        cache.structParams.put("21285:3505", 103); // what the legacy & 0xfff resolver would keep
        String message = rejection(json, cache);
        assertTrue(message, message.contains("slot backpack: struct 21285 param 3505 = 103 is not a component of root 1477"));
        cache = FakeCache.fromTable(json);
        cache.structParams.put("21308:3505", -1);
        assertTrue(rejection(json, cache).contains("slot bank: struct 21308 param 3505 = -1"));
        cache = FakeCache.fromTable(json);
        cache.structParams.put("29115:3505", ROOT << 16 | 115);
        message = rejection(json, cache);
        assertTrue(message, message.contains("slot worn_equipment resolved attach 1477:115 but the verified binding is 1477:114"));
        cache = FakeCache.fromTable(json);
        cache.enumValues.remove(1017);
        assertTrue(rejection(json, cache).contains("enum 7716 has no key 1017"));
    }

    @Test public void absentInterfacesVarbitWidthsAndMissingGroupsAreChecked() throws Exception {
        String json = table();
        FakeCache cache = FakeCache.fromTable(json);
        cache.interfaceCounts.put(1607, 10);
        assertTrue(rejection(json, cache).contains("interface 1607 is listed as absent but exists"));
        // The reassigned list is checked the other way round: the group must be there.
        // This is what stops 1530 from being quietly downgraded back to "absent" once the
        // group really exists, and it is the failure the 947 table produced on 950.
        cache = FakeCache.fromTable(json);
        cache.interfaceCounts.remove(1530);
        assertTrue(rejection(json, cache).contains("interface 1530 is listed as reassigned (invention_910) but is absent from index 3"));
        cache = FakeCache.fromTable(json);
        cache.interfaceCounts.put(1530, 8);
        assertTrue(rejection(json, cache).contains("reassigned interface invention_910 (1530) has 8 components in the 950 cache, table pins 7"));
        cache = FakeCache.fromTable(json);
        cache.hashes.put(FakeCache.key(3, 1530, 0), "f60d0263599cb980aba2c2c2c9af1828a22816b01e64518a9e3b654115c0b259");
        assertTrue(rejection(json, cache).contains("reassigned interface invention_910 (1530) file 0 changed"));
        cache = FakeCache.fromTable(json);
        cache.varbits.put(45189, new int[] {8970, 0, 1});
        assertTrue(rejection(json, cache).contains("varbit bank_quantity (45189) is 2 bit(s) wide"));
        cache = FakeCache.fromTable(json);
        cache.varbits.remove(18797);
        assertTrue(rejection(json, cache).contains("varbit chat_all_filter (18797) is absent from 2/69"));
        cache = FakeCache.fromTable(json);
        cache.interfaceCounts.remove(137);
        assertTrue(rejection(json, cache).contains("interface all_chat (137) is absent from index 3"));
        cache = FakeCache.fromTable(json);
        cache.hashes.remove(FakeCache.key(12, 8471, 0));
        assertTrue(rejection(json, cache).contains("script equipment_layout (8471) is absent from index 12"));
        cache = FakeCache.fromTable(json);
        cache.hashes.put(FakeCache.key(12, 8471, 0), "ffcd128ff71cfbc1a2456d886b62049bc37669d0cc64933150b90997c63fc4ce");
        assertTrue(rejection(json, cache).contains("script equipment_layout (8471) changed"));
    }

    @Test public void unverifiedPacketMarksTheOpUnavailableInsteadOfInventingAnOpcode() throws Exception {
        String json = table();
        JsonObject changed = JsonParser.parseString(json).getAsJsonObject();
        JsonObject rootOp = changed.getAsJsonObject("ops").getAsJsonObject("bootstrap.root");
        assertEquals(1, rootOp.getAsJsonArray("steps").size());
        assertEquals("IF_OPENTOP", rootOp.getAsJsonArray("packets_required").get(0).getAsString());
        rootOp.getAsJsonArray("packets_required").add("IF_SETNPCHEAD");
        JsonObject extra = new JsonObject();
        extra.addProperty("packet", "IF_SETNPCHEAD");
        extra.addProperty("component", "all_chat.history");
        rootOp.getAsJsonArray("steps").add(extra);
        String patched = changed.toString();
        assertNotEquals(json, patched);
        Native950Bindings b = load(patched, FakeCache.fromTable(json));
        Native950Bindings.Op op = b.ops("bootstrap.root");
        assertFalse(op.available);
        assertEquals(java.util.Collections.singletonList("IF_SETNPCHEAD"), op.missingPackets);
        assertEquals(2, op.steps.size());
        assertTrue(b.ops("bank.open").available);
        // A step whose packet is not declared in packets_required is a table bug.
        JsonObject undeclaredTable = JsonParser.parseString(json).getAsJsonObject();
        JsonObject undeclaredStep = new JsonObject();
        undeclaredStep.addProperty("packet", "IF_SETHIDE");
        undeclaredStep.addProperty("component", "slot:bank.wrapper");
        undeclaredStep.addProperty("hidden", true);
        undeclaredTable.getAsJsonObject("ops").getAsJsonObject("bootstrap.root").getAsJsonArray("steps").add(undeclaredStep);
        String undeclared = undeclaredTable.toString();
        assertTrue(rejection(undeclared, FakeCache.fromTable(json)).contains("uses IF_SETHIDE which packets_required does not list"));
        String badReference = json.replace("\"component\": \"bank.close\"", "\"component\": \"bank.frame\"");
        assertTrue(rejection(badReference, FakeCache.fromTable(json)).contains("unknown 950 component binding 'bank.frame'"));
    }

    @Test public void resolverPassesKnownIdsAndCountsEveryAbsentOne() throws Exception {
        String json = table();
        Native950Bindings b = load(json, FakeCache.fromTable(json));
        assertEquals(new HashSet<Integer>(java.util.Arrays.asList(1578, 1607, 1628, 1680, 1929)), b.absentInterfaceIds());
        assertEquals(java.util.Collections.singleton(1530), b.reassignedInterfaceIds());
        assertEquals(6, b.blockedInterfaceIds().size());
        Native950Bindings.Resolver r = b.resolver();
        for (int id : ABSENT) assertEquals(-1, r.interfaceId(id));
        // 1530 has a group in this cache, so only the reassigned declaration stops the
        // permissive resolver from waving it through on "the cache holds it".
        for (int id : REASSIGNED) assertEquals(-1, r.interfaceId(id));
        assertEquals(6, r.unmatched("interface"));
        assertEquals(517, r.interfaceId(517));
        assertEquals(1466, r.interfaceId(1466));
        assertEquals(-1, r.interfaceId(1200));
        assertEquals(5, r.componentId(1473, 5));
        assertEquals(22, r.componentId(1473, 22));
        assertEquals(-1, r.componentId(1473, 23));
        assertEquals(-1, r.componentId(1607, 0));
        assertEquals(8971, r.varp(8971));
        assertEquals("hitpoints is a varp on 950", 13537, r.varp(13537));
        assertEquals(13542, r.varp(13542));
        assertEquals(-1, r.varp(13543));
        assertEquals(45189, r.varbit(45189));
        assertEquals(-1, r.varbit(1));
        assertEquals(1362, r.script(1362));
        assertEquals(-1, r.script(19999));
        assertEquals(94, r.container(94));
        assertEquals(-1, r.container(96));
        assertEquals(1, r.unmatched("component"));
        assertEquals(1, r.unmatched("varp"));
        assertEquals(1, r.unmatched("script"));
        assertEquals(13, r.unmatched());
        assertTrue(r.report().contains("interface=8"));
        // repeated misses keep counting, log once
        for (int id : ABSENT) assertEquals(-1, r.interfaceId(id));
        for (int id : REASSIGNED) assertEquals(-1, r.interfaceId(id));
        assertEquals(14, r.unmatched("interface"));
        assertEquals(19, r.unmatched());
    }

    @Test public void tryLoadIsNullWithoutTheFlatCache() {
        assertFalse(Cache.isFlatReadOnly());
        assertNull(Native950Bindings.tryLoad());
    }

    @Test public void varBitManagerAndVarsManagerShareOneCacheAndOneForceRule() {
        EmbeddedChannel channel = new EmbeddedChannel();
        try {
            Player player = Player.createNative950("varsprobe", new WorldTile(3217, 3258, 0), channel);
            VarBitDefinitions.define(777001, 1234, 4, 7);
            VarsManager vars = player.getVarsManager();
            assertEquals(VarsManager.LEGACY_CAPACITY, vars.size()); // no flat cache in tests
            final List<int[]> sent = new ArrayList<int[]>();
            VarBitManager vbm = new VarBitManager(player);
            assertTrue(vbm.sendVarBit(777001, 5));
            assertEquals(1, vars.nativeUnsentVarps()); // no sink yet: cached and counted, never sent blind
            vars.setNativeVarpSink(new VarsManager.VarpSink() {
                @Override public void varp(int id, int value) { sent.add(new int[] { id, value }); }
            });
            assertEquals(5, vbm.getBitValue(777001));
            assertEquals(5, vars.getBitValue(777001));
            assertEquals(5 << 4, vars.getValue(1234));
            assertEquals(vars.getValue(1234), vbm.getValue(1234));
            assertFalse(vbm.sendVarBit(777001, 5)); // unchanged: nothing sent
            assertTrue(sent.isEmpty());
            vbm.forceSendVar(1234, 5 << 4); // the historic no-op now really sends
            assertEquals(1, sent.size());
            assertEquals(1234, sent.get(0)[0]);
            assertEquals(80, sent.get(0)[1]);
            vbm.sendVarBit(777001, 9);
            assertEquals(9, vars.getBitValue(777001));
            assertEquals(2, sent.size());
            assertEquals(9 << 4, sent.get(1)[1]);
            vars.sendVarBit(777001, 3);
            assertEquals(3, vbm.getBitValue(777001));
            assertEquals(3, sent.size());
            vbm.forceSendVarBit(777001, 3); // force pushes even when unchanged
            assertEquals(4, sent.size());
            vbm.sendVarBit(777001, 16); // above the 4-bit mask: legacy rule zeroes it
            assertEquals(0, vbm.getBitValue(777001));
            vars.sendVar(20000, 1); // outside the legacy range: dropped and counted
            assertEquals(1, vars.droppedWrites());
            assertEquals(0, vars.getValue(20000));
        } finally { channel.finishAndReleaseAll(); }
    }

    /**
     * A varbit id with no file in 2/69 (the flat getter installs the same marker via
     * defineAbsent) must never decode to {varp 0, bit 0}: the write is dropped, counted
     * and nothing reaches the native VARP_LARGE sink.
     */
    @Test public void absentVarbitIsDroppedAndCountedInsteadOfWritingVarpZero() {
        EmbeddedChannel channel = new EmbeddedChannel();
        try {
            Player player = Player.createNative950("absentvarbit", new WorldTile(3217, 3258, 0), channel);
            VarBitDefinitions marker = VarBitDefinitions.defineAbsent(777002);
            assertTrue(marker.isAbsent());
            assertEquals(VarBitDefinitions.ABSENT_BASE_VAR, marker.baseVar);
            assertSame("the marker is cached like any definition", marker, VarBitDefinitions.getClientVarpBitDefinitions(777002));
            assertSame("a second registration keeps the first marker", marker, VarBitDefinitions.defineAbsent(777002));
            assertTrue(VarBitDefinitions.absentCount() >= 1);
            VarsManager vars = player.getVarsManager();
            final List<int[]> sent = new ArrayList<int[]>();
            vars.setNativeVarpSink(new VarsManager.VarpSink() {
                @Override public void varp(int id, int value) { sent.add(new int[] { id, value }); }
            });
            long droppedBefore = vars.droppedWrites();
            assertFalse(vars.updateVarBit(777002, 1, true, true));
            assertFalse(new VarBitManager(player).sendVarBit(777002, 1));
            assertEquals(droppedBefore + 2, vars.droppedWrites());
            assertTrue("nothing may reach the sink", sent.isEmpty());
            assertEquals("varp 0 is untouched", 0, vars.getValue(0));
            assertEquals(0, vars.getBitValue(777002));
            assertEquals(0, vars.nativeUnsentVarps());
        } finally { channel.finishAndReleaseAll(); }
    }
}

