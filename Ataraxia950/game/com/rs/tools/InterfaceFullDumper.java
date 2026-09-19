package com.rs.tools;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import com.rs.cache.Cache;
import com.rs.cache.loaders.IComponentDefinitions;
import com.rs.network.io.InputStream;
import com.rs.utils.Utils;

/**
 * Comprehensive per-component interface dumper.
 *
 * The existing info/interface-config-dump.txt is filtered: it only lists
 * interfaces that have notable config/script bindings, and inside each
 * interface it only shows components with [Configs] entries. That makes it
 * blind to:
 *   - text components (where strings like "Start skilling to populate the XP
 *     counter..." live)
 *   - hidden / dynamic-visibility components driven by varbits referenced via
 *     CS2 hooks (onLoad / onVarpTransmit / etc.) rather than direct configs
 *   - container interfaces with no configs (e.g. 1465, 1466, 1920)
 *
 * This tool walks every component of a user-specified interface id list and
 * prints absolutely everything that could route data to it: text, varp lists,
 * inv/stat/varc transmit lists, CS2 hooks (script id + args), positioning,
 * type/contentType, hidden default. Output goes to info/interface-full-dump.txt.
 *
 * Run with:
 *   java -cp <project-classpath> com.rs.tools.InterfaceFullDumper [id ...]
 *   java -cp <project-classpath> com.rs.tools.InterfaceFullDumper --flat-cache=C:\path\to\cache [id ...]
 *
 * If no ids are supplied, dumps the XP-counter-suspect set:
 *   137, 228, 1213, 1214, 1215, 1216, 1465, 1466, 1467, 1920
 *
 * Then grep the output for terms like "skilling", "populate", "counter",
 * or for a varp id you've been probing, to find the responsible component.
 */
public class InterfaceFullDumper {

    private static final int[] DEFAULT_IDS = {
            137, 228, 1213, 1214, 1215, 1216, 1465, 1466, 1467, 1920
    };

    public static void main(String[] args) throws IOException {
        int firstInterface = 0;
        if (args != null && args.length > 0 && args[0].startsWith("--flat-cache=")) {
            Cache.initFlatReadOnly(Paths.get(args[0].substring("--flat-cache=".length())));
            firstInterface = 1;
        } else {
            Cache.init();
        }

        int[] ids = parseIds(args, firstInterface);
        File outFile = new File("info/interface-full-dump.txt");
        outFile.getParentFile().mkdirs();

        try (BufferedWriter w = new BufferedWriter(new FileWriter(outFile))) {
            w.write("// Comprehensive interface component dump\n");
            w.write("// Generated for ids: " + Arrays.toString(ids) + "\n\n");
            for (int id : ids) {
                dumpInterface(w, id);
                w.newLine();
            }
        }
        System.out.println("Wrote " + outFile.getAbsolutePath());
    }

    private static int[] parseIds(String[] args, int firstInterface) {
        if (args == null || args.length == firstInterface) return DEFAULT_IDS;
        int[] ids = new int[args.length - firstInterface];
        for (int i = 0; i < ids.length; i++) ids[i] = Integer.parseInt(args[i + firstInterface]);
        return ids;
    }

    private static void dumpInterface(BufferedWriter w, int id) throws IOException {
        if (id < 0 || id >= Utils.getInterfaceDefinitionsSize()) {
            w.write("==== Interface " + id + " : out of range ====\n");
            return;
        }
        // Bypass IComponentDefinitions.getInterface() because it has an
        // overly-strict header check (throws "if1" if the first byte isn't
        // -1/0xFF), which rejects old-format interfaces even though the
        // decoder itself handles both formats. We iterate components manually
        // and call decode() directly.
        int compCount = Utils.getInterfaceDefinitionsComponentsSize(id);
        if (compCount <= 0) {
            w.write("==== Interface " + id + " : no components (size=" + compCount + ") ====\n");
            return;
        }
        w.write("==== Interface " + id + " (" + compCount + " components) ====\n");
        int decoded = 0;
        for (int c = 0; c < compCount; c++) {
            byte[] data;
            try {
                data = Cache.STORE.getIndexes()[3].getFile(id, c);
            } catch (Throwable t) {
                w.write("  [comp " + c + "] fetch failed: " + t.getMessage() + "\n");
                continue;
            }
            if (data == null || data.length == 0) continue;
            IComponentDefinitions def = new IComponentDefinitions();
            def.ihash = c + (id << 16);
            try {
                // The decoder reads the format byte itself (handles both
                // 0xFF/255 for new format and other values for old format),
                // so we don't strip anything.
                def.decode(new InputStream(data));
                decoded++;
            } catch (Throwable t) {
                w.write("  [comp " + c + "] decode failed: " + t.getClass().getSimpleName()
                        + ": " + t.getMessage() + "\n");
                continue;
            }
            dumpComponent(w, id, c, def);
        }
        w.write("  (decoded " + decoded + "/" + compCount + " components)\n");
    }

    private static void dumpComponent(BufferedWriter w, int ifaceId, int compIdx,
                                      IComponentDefinitions def) throws IOException {
        // Decide whether this component is worth printing at all - skip pure
        // visual leaves with no text, no transmits, and no hooks, to keep the
        // file readable.
        if (!isInteresting(def)) {
            return;
        }
        w.write("  [comp " + compIdx + "] type=" + def.type
                + " contentType=" + def.contentType
                + " hidden=" + def.hidden
                + " size=" + def.width + "x" + def.height
                + " pos=" + def.positionX + "," + def.positionY + "\n");
        writeIfNonEmpty(w, "    text       = ", def.text);
        writeIfNonEmpty(w, "    opBase     = ", def.opBase);
        writeIfNonEmpty(w, "    name       = ", def.name);
        writeIfNonEmpty(w, "    targetVerb = ", def.targetVerb);
        if (def.ops != null) {
            for (int i = 0; i < def.ops.length; i++) {
                if (def.ops[i] != null && !def.ops[i].isEmpty()) {
                    w.write("    op[" + i + "]     = " + def.ops[i] + "\n");
                }
            }
        }
        writeIntArray(w, "    varpTx     = ", def.varpTransmitList);
        writeIntArray(w, "    varcTx     = ", def.varcTransmitList);
        writeIntArray(w, "    varcstrTx  = ", def.varcstrTransmitList);
        writeIntArray(w, "    invTx      = ", def.invTransmitList);
        writeIntArray(w, "    statTx     = ", def.statTransmitList);
        writeHook(w, "    onLoad     ", def.onLoadHook);
        writeHook(w, "    onClick    ", def.onClickHook);
        writeHook(w, "    onVarpTx   ", def.onVarpTransmitHook);
        writeHook(w, "    onVarcTx   ", def.onVarcTransmitHook);
        writeHook(w, "    onVarcstrTx", def.onVarcstrTransmitHook);
        writeHook(w, "    onInvTx    ", def.onInvTransmitHook);
        writeHook(w, "    onStatTx   ", def.onStatTransmitHook);
        writeHook(w, "    onMouseOver", def.onMouseOverHook);
        writeHook(w, "    onMouseRpt ", def.onMouseRepeatHook);
        writeHook(w, "    onMouseLv  ", def.onMouseLeaveHook);
        writeHook(w, "    onHold     ", def.onHoldHook);
        writeHook(w, "    onRelease  ", def.onReleaseHook);
        writeHook(w, "    onDrag     ", def.onDragHook);
        writeHook(w, "    onDragDone ", def.onDragCompleteHook);
        writeHook(w, "    onTimer    ", def.onTimerHook);
        writeHook(w, "    onTgtEnter ", def.onTargetEnterHook);
        writeHook(w, "    onTgtLeave ", def.onTargetLeaveHook);
        writeHook(w, "    onUseOnObj ", def.onUseOnObjHook);
        writeHook(w, "    onClickRpt ", def.onClickRepeatHook);
        writeHook(w, "    onScrlWhl  ", def.onScrollWheelHook);
        writeHook(w, "    onOpt      ", def.onOptHook);
    }

    private static boolean isInteresting(IComponentDefinitions def) {
        if (nonEmpty(def.text)) return true;
        if (nonEmpty(def.opBase)) return true;
        if (nonEmpty(def.name)) return true;
        if (nonEmpty(def.targetVerb)) return true;
        if (nonEmptyArray(def.varpTransmitList)) return true;
        if (nonEmptyArray(def.varcTransmitList)) return true;
        if (nonEmptyArray(def.varcstrTransmitList)) return true;
        if (nonEmptyArray(def.invTransmitList)) return true;
        if (nonEmptyArray(def.statTransmitList)) return true;
        if (anyHook(def)) return true;
        if (def.ops != null) {
            for (String op : def.ops) {
                if (nonEmpty(op)) return true;
            }
        }
        return false;
    }

    private static boolean anyHook(IComponentDefinitions def) {
        Object[][] hooks = {
                def.onLoadHook, def.onClickHook, def.onVarpTransmitHook,
                def.onVarcTransmitHook, def.onVarcstrTransmitHook,
                def.onInvTransmitHook, def.onStatTransmitHook,
                def.onMouseOverHook, def.onMouseRepeatHook, def.onMouseLeaveHook,
                def.onHoldHook, def.onReleaseHook, def.onDragHook,
                def.onDragCompleteHook, def.onTimerHook,
                def.onTargetEnterHook, def.onTargetLeaveHook,
                def.onUseOnObjHook, def.onClickRepeatHook,
                def.onScrollWheelHook, def.onOptHook
        };
        for (Object[] h : hooks) {
            if (h != null && h.length > 0) return true;
        }
        return false;
    }

    private static boolean nonEmpty(String s) {
        return s != null && !s.isEmpty();
    }

    private static boolean nonEmptyArray(int[] a) {
        return a != null && a.length > 0;
    }

    private static void writeIfNonEmpty(BufferedWriter w, String label, String value)
            throws IOException {
        if (nonEmpty(value)) {
            w.write(label + quote(value) + "\n");
        }
    }

    private static void writeIntArray(BufferedWriter w, String label, int[] arr)
            throws IOException {
        if (!nonEmptyArray(arr)) return;
        // Dedup repeated ids inline so the line stays readable.
        Set<Integer> seen = new LinkedHashSet<>();
        for (int v : arr) seen.add(v);
        w.write(label + seen + "\n");
    }

    private static void writeHook(BufferedWriter w, String label, Object[] hook)
            throws IOException {
        if (hook == null || hook.length == 0) return;
        StringBuilder sb = new StringBuilder();
        sb.append(label).append(" = script=").append(hook[0]);
        if (hook.length > 1) {
            sb.append(" args=[");
            for (int i = 1; i < hook.length; i++) {
                if (i > 1) sb.append(", ");
                sb.append(hook[i]);
            }
            sb.append("]");
        }
        sb.append("\n");
        w.write(sb.toString());
    }

    private static String quote(String s) {
        return "\"" + s.replace("\n", "\\n").replace("\"", "\\\"") + "\"";
    }
}
