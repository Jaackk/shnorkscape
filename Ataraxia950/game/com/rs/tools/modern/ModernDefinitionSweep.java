package com.rs.tools.modern;

import com.rs.cache.Cache;
import com.rs.cache.filestore.store.Index;
import com.rs.cache.loaders.AnimationDefinitions;
import com.rs.cache.loaders.ItemDefinitions;
import com.rs.cache.loaders.NPCDefinitions;
import com.rs.cache.loaders.ObjectDefinitions;
import com.rs.cache.loaders.RenderAnimDefinitions;

import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses every definition of every kind in a modern flat cache and reports what the loaders refuse.
 *
 * <p>Five loaders fail closed on an opcode they do not know: objects, NPCs, items, animations and
 * render animations. Each throws {@code Unsupported modern <kind> <id> opcode <n>} and, on the
 * login path, that aborts world admission. Discovering them one login at a time costs a rebuild, a
 * redeploy and a client relaunch per opcode - which is exactly how the object parser was found, and
 * it is not a good way to find four more.
 *
 * <p>This sweeps all five up front. It does not fix anything and deliberately does not weaken any
 * loader: an unsupported opcode still throws, it is just caught here and counted rather than
 * ending a session. The output is the work list - which opcodes are missing, how often each occurs
 * and which definition to look at first.
 *
 * <p><b>A clean sweep is not proof the parsers are correct.</b> It proves only that every record
 * terminates without hitting an unknown opcode. A field read at the wrong WIDTH keeps the stream
 * aligned by luck as often as not, and this cannot see that; only re-deriving the width from the
 * client's own decoder can. Treat a clean result as "nothing is refused", not "everything is right".
 *
 * <p>Usage: {@code ModernDefinitionSweep <flat-cache-directory>}. Exits non-zero if anything was
 * refused, so it can gate a build.
 */
public final class ModernDefinitionSweep {

    private static final Pattern UNSUPPORTED =
            Pattern.compile("Unsupported modern (\\w+(?: \\w+)?) (\\d+) opcode (\\d+)");

    private ModernDefinitionSweep() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 1)
            throw new IllegalArgumentException("Usage: ModernDefinitionSweep <flat-cache-directory>");
        Cache.initFlatReadOnly(Paths.get(args[0]).toAbsolutePath().normalize());

        Map<String, Result> results = new LinkedHashMap<String, Result>();
        results.put("object", sweepIndexed(16, id -> {
            ObjectDefinitions.getObjectDefinitions(id);
            return null; // throws instead of recording; the catch below reports it
        }));
        // NPCs are 128 files per group, NOT 256. NPCDefinitions resolves them as
        // `index.getFile(id >>> 134238215, id & 0x7f)`, and Java masks an int shift count to five
        // bits, so 134238215 & 31 == 7: the mapping is `group = id >>> 7, file = id & 0x7f`. An
        // earlier version of this sweep assumed 256 like items and objects, which walked half the
        // index under ids that resolve elsewhere - it under-reported NPC failures by roughly half.
        results.put("npc", sweepWide(18, 7, id -> NPCDefinitions.getNPCDefinitions(id).decodeFailure));
        results.put("item", sweepIndexed(19, id -> ItemDefinitions.getItemDefinitions(id).decodeFailure));
        results.put("animation", sweepAnimations());
        results.put("render animation", sweepRenderAnimations());

        int refusedTotal = 0;
        for (Map.Entry<String, Result> entry : results.entrySet()) {
            Result result = entry.getValue();
            refusedTotal += result.refused;
            System.out.println();
            System.out.println("=== " + entry.getKey() + " ===");
            System.out.println("  parsed " + result.parsed + ", refused " + result.refused
                    + ", other failures " + result.other);
            if (!result.opcodes.isEmpty()) {
                System.out.println("  unsupported opcodes (opcode -> count, first id):");
                for (Map.Entry<Integer, int[]> row : result.opcodes.entrySet())
                    System.out.println("    opcode " + row.getKey() + "  x" + row.getValue()[0]
                            + "  first id " + row.getValue()[1]);
            }
            for (Map.Entry<String, Integer> row : result.otherMessages.entrySet())
                System.out.println("    other: " + row.getKey() + "  x" + row.getValue());
        }

        System.out.println();
        System.out.println(refusedTotal == 0
                ? "SWEEP CLEAN: no definition was refused. (Widths are not checked - see the class javadoc.)"
                : "SWEEP FOUND " + refusedTotal + " refused definitions.");
        System.exit(refusedTotal == 0 ? 0 : 1);
    }

    /**
     * Loads one definition and returns its recorded decode failure, or null when it decoded.
     *
     * <p>Two different failure styles have to be reported together. ObjectDefinitions THROWS on an
     * unknown opcode, which is what blocks a login. The other four fail SOFT: they build a
     * well-defined failed definition, record the reason in their own registry, log one warning and
     * carry on. An earlier version of this sweep only caught the throw, so it printed SWEEP CLEAN
     * while the log beneath it showed hundreds of rejected animations - the failure mode this tool
     * exists to prevent, reproduced by the tool itself.
     */
    private interface Loader {
        String load(int id);
    }

    /**
     * Definitions addressed as {@code group = id >>> shift, file = id & (2^shift - 1)}.
     *
     * <p>The shift is per-kind and is NOT uniform: objects and items use 8 (256 files per group),
     * NPCs and animations use 7 (128). Getting it wrong does not error - it walks the index under
     * ids that resolve to different files - so it silently halves the coverage of a sweep whose
     * whole purpose is coverage.
     */
    private static Result sweepWide(int indexId, int shift, Loader loader) {
        Result result = new Result();
        Index index = Cache.STORE.getIndexes()[indexId];
        int filesPerGroup = 1 << shift;
        for (int group = 0; group < index.getLastArchiveId() + 1; group++) {
            if (!index.archiveExists(group)) continue;
            for (int file = 0; file < filesPerGroup; file++) {
                if (!index.fileExists(group, file)) continue;
                record(result, (group << shift) | file, loader);
            }
        }
        return result;
    }

    /** Definitions addressed as {@code group = id >>> 8, file = id & 255} in one index. */
    private static Result sweepIndexed(int indexId, Loader loader) {
        return sweepWide(indexId, 8, loader);
    }

    /** Animations are {@code group = id >>> 7, file = id & 0x7f} in index 20. */
    private static Result sweepAnimations() {
        return sweepWide(20, 7, id -> AnimationDefinitions.getAnimationDefinitions(id).decodeFailure);
    }

    /** Render animations are files of group 32 in index 2. */
    private static Result sweepRenderAnimations() {
        Result result = new Result();
        Index index = Cache.STORE.getIndexes()[2];
        if (!index.archiveExists(32)) return result;
        for (int file = 0; file < 65536; file++) {
            if (!index.fileExists(32, file)) continue;
            record(result, file,
                    id -> RenderAnimDefinitions.getRenderAnimDefinitions(id).decodeFailure);
        }
        return result;
    }

    private static void record(Result result, int id, Loader loader) {
        String message;
        try {
            message = loader.load(id);
            if (message == null) {
                result.parsed++;
                return;
            }
        } catch (RuntimeException failure) {
            message = failure.getMessage() == null ? failure.toString() : failure.getMessage();
        }
        {
            Matcher matcher = UNSUPPORTED.matcher(message);
            if (matcher.find()) {
                result.refused++;
                int opcode = Integer.parseInt(matcher.group(3));
                int[] row = result.opcodes.get(opcode);
                if (row == null) result.opcodes.put(opcode, new int[] {1, id});
                else row[0]++;
            } else {
                result.other++;
                String key = message.length() > 90 ? message.substring(0, 90) : message;
                result.otherMessages.merge(key, 1, Integer::sum);
            }
        }
    }

    private static final class Result {
        int parsed, refused, other;
        final Map<Integer, int[]> opcodes = new TreeMap<Integer, int[]>();
        final Map<String, Integer> otherMessages = new LinkedHashMap<String, Integer>();
    }
}
