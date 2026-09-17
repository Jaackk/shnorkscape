package com.rs.cache.loaders;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import com.rs.cache.Cache;
import com.rs.game.player.VarsManager;
import com.rs.network.io.InputStream;
import com.rs.utils.Logger;

/**
 * Varbit (bit-slice of a player variable) definitions from cache index 2 archive 69.
 *
 * The 947 flat cache holds 60,682 definitions with opcodes 1 (domain byte +
 * big-smart base var), 2 (start/end bit) and a payload-less 16 that the legacy
 * decoder did not know; skipping it misaligned the stream for 176 definitions.
 * On the flat 947 cache the decoder is strict (truncation or an unknown opcode
 * throws) because a silently wrong bit range writes the wrong varp bits, and an
 * id with no file at all yields the {@link #defineAbsent(int)} marker (base var
 * -1) so the write is dropped and counted rather than landing in varp 0; the
 * legacy 910 cache keeps the tolerant loop unchanged.
 */
public final class VarBitDefinitions {

    private static final ConcurrentHashMap<Integer, VarBitDefinitions> varpbitDefs = new ConcurrentHashMap<Integer, VarBitDefinitions>();

    public int id;
    public int baseVar;
    public int startBit;
    public int endBit;
    public int varDomain;
    /**
     * Payload-less opcode 16 seen on a minority of 947 definitions. Its meaning
     * is unverified; it is recorded only so the stream stays aligned.
     */
    public boolean opcode16Flag;

    public static final void main(String[] args) throws IOException {
        Cache.init();
        Logger.getGlobal().info("There are currently: " + (Cache.STORE.getIndexes()[2].getLastFileId(69) + 1) + " bitConfigs.");
        // List<BitConfigDefinitions> configs = new
        // ArrayList<BitConfigDefinitions>();s
        List<Integer> baseVars = new ArrayList<Integer>();
        for(int j=0;j<30;j++) {
            int varbitId = j <= 11 ? 190+j : 29488 + (j-12);
            VarBitDefinitions cd = getClientVarpBitDefinitions(varbitId);
            if(!baseVars.contains(cd.baseVar))
            baseVars.add(cd.baseVar);
        }
        System.out.println(baseVars);
        for (int i = 0; i < Cache.STORE.getIndexes()[2].getLastFileId(69); i++) {
            VarBitDefinitions cd = getClientVarpBitDefinitions(i);
            if (cd.baseVar == 5880) {
                int value = 5;
                int v = value >> cd.startBit & VarsManager.masklookup[cd.endBit - cd.startBit];
                Logger.getGlobal().info("BitConfig: " + i + ", from bitshift:" + cd.startBit + ", till bitshift: " + cd.endBit + ", " + cd.baseVar + ", " + v);
            }
        }
    }

    public static final VarBitDefinitions getClientVarpBitDefinitions(int id) {
        VarBitDefinitions script = varpbitDefs.get(id);
        if (script != null)// open new txt document
            return script;
        byte[] data = Cache.STORE.getIndexes()[2].getFile(69, id);
        if (Cache.isFlatReadOnly()) {
            if (data == null)
                return defineAbsent(id);
            // 947: fail closed; a misdecoded bit range would corrupt the whole varp.
            Decode decode = decode(id, data);
            if (!decode.ok())
                throw new IllegalStateException("947 varbit " + id + " did not decode: " + decode.problem());
            script = decode.definition;
        } else {
            script = new VarBitDefinitions();
            script.id = id;
            if (data != null)
                script.readValueLoop(new InputStream(data));
        }
        varpbitDefs.put(id, script);
        return script;

    }

    /**
     * Registers a definition without touching the cache. Used by tests (no cache
     * available) and by tools that already decoded the bytes; a later cache read
     * for the same id is never attempted.
     */
    public static VarBitDefinitions define(int id, int baseVar, int startBit, int endBit) {
        if (baseVar < 0 || startBit < 0 || endBit < startBit || endBit > 31)
            throw new IllegalArgumentException("Invalid varbit definition " + id + ": base " + baseVar + " bits " + startBit + ".." + endBit);
        VarBitDefinitions defs = new VarBitDefinitions();
        defs.id = id; defs.baseVar = baseVar; defs.startBit = startBit; defs.endBit = endBit;
        varpbitDefs.put(id, defs);
        return defs;
    }

    /** Base var of a definition whose id has no file in 2/69; never a valid varp index. */
    public static final int ABSENT_BASE_VAR = -1;

    private static final ConcurrentHashMap<Integer, Boolean> ABSENT = new ConcurrentHashMap<Integer, Boolean>();

    /**
     * Registers the marker for a varbit id that has no file in 2/69 (60,917 ids but only
     * 60,682 files on the 947 cache, and 910 content still names dozens of literal 910 ids).
     * The marker carries {@link #ABSENT_BASE_VAR} so {@code VarsManager.updateVarBit} drops and
     * counts the write through its range check instead of silently writing bit 0 of varp 0
     * the way an all-zero tolerant definition would. Cached like any other definition; public
     * so tests without a cache can install the same marker the flat path produces.
     */
    public static VarBitDefinitions defineAbsent(int id) {
        VarBitDefinitions defs = new VarBitDefinitions();
        defs.id = id;
        defs.baseVar = ABSENT_BASE_VAR;
        VarBitDefinitions previous = varpbitDefs.putIfAbsent(id, defs);
        if (previous != null) return previous;
        if (ABSENT.putIfAbsent(id, Boolean.TRUE) == null)
            Logger.getGlobal().warn("947 varbit " + id + " is absent from 2/69; writes to it are dropped and counted");
        return defs;
    }

    /** True when the definition is the absent-file marker rather than a decoded varbit. */
    public boolean isAbsent() {
        return baseVar == ABSENT_BASE_VAR;
    }

    /** Number of distinct varbit ids requested that had no file in 2/69. */
    public static int absentCount() {
        return ABSENT.size();
    }

    /** Outcome of a strict decode; {@code unknownOpcode} is -1 when every opcode was known. */
    public static final class Decode {
        public final VarBitDefinitions definition;
        public final int unknownOpcode, trailingBytes;
        public final boolean truncated;
        Decode(VarBitDefinitions definition, int unknownOpcode, int trailingBytes, boolean truncated) {
            this.definition = definition; this.unknownOpcode = unknownOpcode;
            this.trailingBytes = trailingBytes; this.truncated = truncated;
        }
        public boolean ok() { return unknownOpcode < 0 && trailingBytes == 0 && !truncated && definition.endBit >= definition.startBit && definition.endBit <= 31; }
        public String problem() {
            if (unknownOpcode >= 0) return "unknown opcode " + unknownOpcode;
            if (truncated) return "truncated stream";
            if (trailingBytes != 0) return trailingBytes + " trailing byte(s) after the terminator";
            return "bit range " + definition.startBit + ".." + definition.endBit + " is invalid";
        }
    }

    /**
     * Strict decode of one 2/69 file: the stream must end exactly at the opcode-0
     * terminator and contain only opcodes 1, 2 and 16. Never throws; the probe
     * turns the result into a histogram and the flat-cache getter into an error.
     */
    public static Decode decode(int id, byte[] data) {
        VarBitDefinitions defs = new VarBitDefinitions();
        defs.id = id;
        InputStream stream = new InputStream(data, true);
        try {
            for (;;) {
                int opcode = stream.readUnsignedByte();
                if (opcode == 0)
                    break;
                if (opcode == 1) {
                    defs.varDomain = stream.readUnsignedByte();
                    defs.baseVar = stream.readBigSmart();
                } else if (opcode == 2) {
                    defs.startBit = stream.readUnsignedByte();
                    defs.endBit = stream.readUnsignedByte();
                } else if (opcode == 16) {
                    defs.opcode16Flag = true;
                } else {
                    return new Decode(defs, opcode, stream.getRemaining(), false);
                }
            }
        } catch (IllegalArgumentException truncated) {
            return new Decode(defs, -1, 0, true);
        }
        return new Decode(defs, -1, stream.getRemaining(), false);
    }

    private void readValueLoop(InputStream stream) {
        for (;;) {
            int opcode = stream.readUnsignedByte();
            if (opcode == 0)
                break;
            readValues(stream, opcode);
        }
    }

    private void readValues(InputStream stream, int opcode) {
        if (opcode == 1) {
            varDomain = stream.readUnsignedByte();
            baseVar = stream.readBigSmart();
            // System.out.println(varDomain);
        } else if (opcode == 2) {
            startBit = stream.readUnsignedByte();
            endBit = stream.readUnsignedByte();
        } else if (opcode == 16) {
            opcode16Flag = true;
        }
    }

    private VarBitDefinitions() {

    }

    public static int[] getVarbitsForVar(int varId) {
        int[] varbits = new int[32];
        for (int i = 0; i < Cache.STORE.getIndexes()[2].getLastFileId(69) + 1; i++) {
            VarBitDefinitions cd = getClientVarpBitDefinitions(i);
            if (cd.baseVar == varId && cd.varDomain == 0) {
                varbits[cd.startBit] = i;
            }
        }
        return varbits;
    }
}
