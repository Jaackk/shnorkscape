package com.rs.game.player.client;

import com.rs.cache.Cache;
import com.rs.cache.loaders.AnimationDefinitions;
import com.rs.cache.loaders.ItemDefinitions;
import com.rs.network.io.InputStream;
import com.rs.cache.loaders.RenderAnimDefinitions;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.IntFunction;

/** Cache-owned movement sets used in native950 appearance, without legacy weapon lists. */
public final class Native950EquipmentAnimations {
    private Native950EquipmentAnimations() { }
    public static final int UNARMED_BAS = 2699;
    private static Object store;
    private static final Map<Integer, Boolean> VALID = new HashMap<>();

    /** Presence, strict BAS parsing and every declared sequence must match the current flat cache. */
    public static synchronized boolean validateBas(int id) {
        if (id < 0 || id >= 65535 || Cache.STORE == null || !Cache.isFlatReadOnly()) return false;
        if (store != Cache.STORE) { VALID.clear(); store = Cache.STORE; }
        Boolean previous = VALID.get(id);
        if (previous != null) return previous;
        boolean valid;
        try {
            byte[] bytes = Cache.STORE.getIndexes()[2].getFile(32, id);
            valid = validate(id, bytes, sequence -> Cache.STORE.getIndexes()[20].getFile(sequence >>> 7, sequence & 127));
        } catch (RuntimeException failure) { valid = false; }
        VALID.put(id, valid);
        return valid;
    }

    /** Pure validation surface for malformed/missing dependency regression tests. */
    static boolean validate(int id, byte[] bytes, IntFunction<byte[]> sequences) {
        if (id < 0 || id >= 65535 || bytes == null || bytes.length == 0 || sequences == null) return false;
        try {
            StringBuilder trace = new StringBuilder();
            RenderAnimDefinitions bas = RenderAnimDefinitions.decodeStrict947(id, bytes, trace);
            Set<Integer> ids = sequenceIds(bas, trace.toString());
            // An empty definition cannot provide a character's movement/idle state.
            if (ids.isEmpty()) return false;
            for (int sequence : ids) {
                byte[] animation = sequences.apply(sequence);
                if (animation == null || animation.length == 0) return false;
                AnimationDefinitions.decodeStrict947(sequence, animation, null);
            }
            return true;
        } catch (RuntimeException failure) { return false; }
    }

    /** Resolve the original equipment framework's modern stance through current-cache combat metadata. */
    public static int resolveBas(ItemDefinitions item, boolean combat) {
        if (item == null || Cache.STORE == null || !Cache.isFlatReadOnly()) return -1;
        try {
            int key = combat ? 2955 : 2954;
            Object selected = item.clientScriptData == null ? null : item.clientScriptData.get(key);
            if (selected == null) {
                int profile = item.getCSOpcode(3000, -1);
                if (profile < 0) profile = item.getCSOpcode(686, -1);
                if (profile >= 0) {
                    byte[] bytes = Cache.STORE.getIndexes()[22].getFile(profile >>> 5, profile & 31);
                    if (bytes == null) return -1;
                    selected = structParams(bytes).get(key);
                }
            }
            if (selected == null && item.clientScriptData != null) selected = item.clientScriptData.get(644);
            int id = selected == null || Integer.valueOf(-1).equals(selected)
                    ? (combat ? 2688 : UNARMED_BAS) : selected instanceof Integer ? (Integer) selected : -1;
            return validateBas(id) ? id : -1;
        } catch (RuntimeException malformed) { return -1; }
    }

    /** Strict current-cache struct reader; no global legacy struct cache or synthetic defaults. */
    private static Map<Integer, Object> structParams(byte[] bytes) {
        InputStream in = new InputStream(bytes, true);
        Map<Integer, Object> params = new HashMap<>();
        for (;;) {
            int opcode = in.readUnsignedByte();
            if (opcode == 0) break;
            if (opcode != 249) throw new IllegalArgumentException("Unknown equipment combat struct opcode " + opcode);
            int count = in.readUnsignedByte();
            for (int n = 0; n < count; n++) {
                int type = in.readUnsignedByte();
                if (type != 0 && type != 1) throw new IllegalArgumentException("Invalid combat struct parameter type");
                int key = in.read24BitInt();
                params.put(key, type == 1 ? in.readString() : in.readInt());
            }
        }
        if (in.getRemaining() != 0) throw new IllegalArgumentException("Trailing equipment combat struct bytes");
        return params;
    }

    /** Only explicitly declared BAS sequence fields; the legacy constructor defaults some absent IDs to0. */
    static Set<Integer> sequenceIds(RenderAnimDefinitions bas, String trace) {
        Set<Integer> ids = new LinkedHashSet<>();
        for (String token : trace.trim().split(" +")) {
            if (token.isEmpty()) continue;
            int op = Integer.parseInt(token.substring(0, token.indexOf('@')));
            int value = -1;
            switch (op) {
                case 1: if (bas.standAnimation >= 0) ids.add(bas.standAnimation); value=bas.walkAnimation; break;
                case 2: value=bas.moveType1Anim; break;
                case 3: value=bas.type1_180; break;
                case 4: value=bas.type1_90; break;
                case 5: value=bas.type1_90_counter; break;
                case 6: value=bas.runAnimation; break;
                case 7: value=bas.runRotate180Animation; break;
                case 8: value=bas.runRotate90Animation; break;
                case 9: value=bas.runRotate90CounterAnimation; break;
                case 38: value=bas.walkUpwardsAnimation; break;
                case 39: value=bas.anInt7046; break;
                case 40: value=bas.rotate180Animation; break;
                case 41: value=bas.rotate90Animation; break;
                case 42: value=bas.rotate90CounterAnimation; break;
                case 46: value=bas.anInt7033; break;
                case 47: value=bas.anInt7034; break;
                case 48: value=bas.anInt7035; break;
                case 49: value=bas.anInt7036; break;
                case 50: value=bas.anInt7041; break;
                case 51: value=bas.anInt7038; break;
                case 52: for (int id : bas.loopAnimations) if (id >= 0) ids.add(id); break;
                default: break;
            }
            if (value >= 0) ids.add(value);
        }
        return ids;
    }
}
