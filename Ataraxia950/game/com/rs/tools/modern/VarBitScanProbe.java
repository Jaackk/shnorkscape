package com.rs.tools.modern;

import com.rs.cache.Cache;
import com.rs.cache.filestore.store.Index;
import com.rs.cache.loaders.VarBitDefinitions;
import java.nio.file.Paths;
import java.util.Map;
import java.util.TreeMap;

/**
 * Decodes every varbit definition in the 947 flat cache (index 2 archive 69)
 * with the strict decoder and prints ok/total plus an unknown-opcode histogram.
 * Target: 60,682/60,682. Also prints the bank/chat varbits the bindings table
 * pins so their bit widths can be checked against the verified scripts.
 *
 * Usage: VarBitScanProbe &lt;flat-cache-directory&gt;
 */
public final class VarBitScanProbe {
    private static final int[] REPORT = { 45189, 45141, 45158, 18797 };

    public static void main(String[] args) throws Exception {
        if (args.length != 1) throw new IllegalArgumentException("Usage: VarBitScanProbe <flat-cache-directory>");
        Cache.initFlatReadOnly(Paths.get(args[0]).toAbsolutePath().normalize());
        Index index = Cache.STORE.getIndexes()[2];
        int last = index.getLastFileId(69);
        int total = 0, ok = 0, truncated = 0, trailing = 0, flagged = 0, badRange = 0;
        Map<Integer, Integer> unknown = new TreeMap<Integer, Integer>();
        for (int id = 0; id <= last; id++) {
            if (!index.fileExists(69, id)) continue;
            byte[] data = index.getFile(69, id);
            if (data == null) continue;
            total++;
            VarBitDefinitions.Decode decode = VarBitDefinitions.decode(id, data);
            if (decode.ok()) { ok++; if (decode.definition.opcode16Flag) flagged++; continue; }
            if (decode.unknownOpcode >= 0) {
                Integer count = unknown.get(decode.unknownOpcode);
                unknown.put(decode.unknownOpcode, count == null ? 1 : count + 1);
            } else if (decode.truncated) truncated++;
            else if (decode.trailingBytes != 0) trailing++;
            else badRange++;
        }
        System.out.println("varbit 2/69 decode: " + ok + "/" + total + " ok (last id " + last + ", valid files " + index.getValidFilesCount(69) + ")");
        System.out.println("opcode 16 flagged: " + flagged + ", truncated: " + truncated + ", trailing bytes: " + trailing + ", invalid bit range: " + badRange);
        System.out.println("unknown opcode histogram: " + (unknown.isEmpty() ? "none" : unknown.toString()));
        System.out.println("varplayer 2/60: last id " + index.getLastFileId(60) + ", valid files " + index.getValidFilesCount(60));
        for (int id : REPORT) {
            if (!index.fileExists(69, id)) { System.out.println("varbit " + id + ": ABSENT"); continue; }
            VarBitDefinitions.Decode decode = VarBitDefinitions.decode(id, index.getFile(69, id));
            VarBitDefinitions d = decode.definition;
            System.out.println("varbit " + id + ": domain " + d.varDomain + " base varp " + d.baseVar + " bits " + d.startBit + ".." + d.endBit
                    + " (max " + ((d.endBit - d.startBit) >= 31 ? "2^32-1" : String.valueOf((1 << (d.endBit - d.startBit + 1)) - 1)) + ")"
                    + (decode.ok() ? "" : " PROBLEM " + decode.problem()));
        }
        boolean absentOk = checkAbsentIdThroughTheProductionGetter(index, last);
        if (ok != total || !absentOk) {
            System.out.println("RESULT: FAIL (" + (total - ok) + " definition(s) did not decode"
                    + (absentOk ? "" : "; absent-id marker check failed") + ")");
            System.exit(1);
        }
        System.out.println("RESULT: OK");
    }

    /**
     * The production getter must serve an id with no file in 2/69 as the absent marker
     * (base var -1, dropped and counted by VarsManager), never as {varp 0, bit 0}, and a
     * present id as a real decode. Uses the first gap in the archive plus one id past it.
     */
    private static boolean checkAbsentIdThroughTheProductionGetter(Index index, int last) {
        int absentId = -1;
        for (int id = 0; id <= last + 1; id++) {
            if (!index.fileExists(69, id)) { absentId = id; break; }
        }
        VarBitDefinitions absent = VarBitDefinitions.getClientVarpBitDefinitions(absentId);
        VarBitDefinitions present = VarBitDefinitions.getClientVarpBitDefinitions(REPORT[REPORT.length - 1]);
        boolean pass = absent.isAbsent() && absent.baseVar == VarBitDefinitions.ABSENT_BASE_VAR
                && !present.isAbsent() && present.baseVar >= 0 && VarBitDefinitions.absentCount() == 1;
        System.out.println("absent varbit " + absentId + " via getClientVarpBitDefinitions: baseVar=" + absent.baseVar
                + " isAbsent=" + absent.isAbsent() + "; present varbit " + present.id + ": baseVar=" + present.baseVar
                + " bits " + present.startBit + ".." + present.endBit + "; absentCount=" + VarBitDefinitions.absentCount()
                + " => " + (pass ? "OK" : "FAIL"));
        return pass;
    }

    private VarBitScanProbe() { }
}
