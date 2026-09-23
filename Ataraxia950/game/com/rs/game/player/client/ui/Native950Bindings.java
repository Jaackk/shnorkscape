package com.rs.game.player.client.ui;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.rs.cache.Cache;
import com.rs.network.protocol.modern950.Native950Protocol.ServerPacket;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Revision-keyed UI binding table for the native 950 client.
 *
 * The 910 engine hard-codes roughly two hundred interface, component, var and
 * cs2 ids; the 947 slice re-verified a handful of them (root 1477, bank 517,
 * backpack 1473:5, equipment 1462:31, chat 137, varbits 45189/45141/45158/18797,
 * varp 8971 and the pinned scripts) in Kotlin literals. This class loads those
 * bindings from {@code resources/native950/ui-bindings-950.json} and, when the
 * read-only flat cache is selected, validates every entry against it:
 * <ul>
 * <li>slots resolve through enum 7716 to a struct whose params 3505 (attach
 *     point) and 3503 (wrapper) are full 32-bit hashes {@code >= 0} whose parent
 *     is the root interface -- never the {@code & 0xfff} mask the legacy
 *     {@code InterfaceManager} applies, which silently drops the parent;</li>
 * <li>interfaces exist in index 3, hold at least the declared number of
 *     components, and every named component is a real file; pinned files match
 *     their SHA-256;</li>
 * <li>scripts exist in index 12 and match their SHA-256 pins;</li>
 * <li>varbits exist in 2/69, are wide enough for the declared maximum and sit on
 *     exactly the base varp and bit range the table declares;</li>
 * <li>the interface ids listed absent really have no group in index 3, and the
 *     ids listed {@code reassignedInterfaces} really do have one -- those are
 *     910 ids that exist on 950 but hold unrelated content, so they are proved
 *     present, pinned, and then rejected by both resolvers.</li>
 * </ul>
 * Any mismatch rejects the whole table with one message listing every problem,
 * so a changed cache can never be driven with stale ids. Op recipes stay
 * symbolic (packet kind + named arguments); a recipe whose packet is not a
 * verified {@link ServerPacket} is kept but marked unavailable rather than
 * inventing an opcode.
 *
 * <p>What the table's entries rest on differs by entry and the JSON {@code _note}
 * headers say so per entry. In short: the ids and recipes are still the 947
 * bindings and have not been exercised against a live 950 client; every SHA-256
 * is a 950 pin re-taken on 2026-09-10, which restores drift detection without
 * re-verifying meaning (the same downgrade {@code cache-pins-950.md} records for
 * the sibling adapter pins); and two entries -- the {@code hitpoints} var and
 * interface 1530 -- were re-derived from the 950 cache because the content moved.
 */
public final class Native950Bindings {
    public static final String RESOURCE = "/native950/ui-bindings-950.json";
    public static final int REVISION = 950;
    /**
     * Every validation message names the revision through this, never a literal.
     * The 947 table shipped messages that said "947 cache" while validating
     * against the 950 one, which is the worst possible place for a stale number:
     * it is the text someone reads at the moment the check fails.
     */
    private static final String REV = Integer.toString(REVISION);
    private static final String LOG = "[Ataraxia950] UI bindings: ";

    /**
     * A named player/client variable. A varbit additionally declares the base varp
     * and inclusive bit range the 950 cache decodes for it, so a re-dumped cache
     * that moved the varbit onto another varp rejects the table instead of quietly
     * rewriting a different player variable. Non-varbit kinds carry -1 there.
     */
    public static final class Var {
        public final String name, kind;
        public final int id, maxValue, varp, startBit, endBit;
        Var(String name, String kind, int id, int maxValue, int varp, int startBit, int endBit) {
            this.name = name; this.kind = kind; this.id = id; this.maxValue = maxValue;
            this.varp = varp; this.startBit = startBit; this.endBit = endBit;
        }
        public boolean isVarp() { return kind.equals("varp"); }
        public boolean isVarbit() { return kind.equals("varbit"); }
        public boolean isVarc() { return kind.equals("varc"); }
        public boolean isVarcString() { return kind.equals("varcstr"); }
        /** Number of bits a varbit occupies, or -1 for other kinds. */
        public int bitWidth() { return isVarbit() ? endBit - startBit + 1 : -1; }
    }

    /** A HUD slot resolved through enum 7716 at load time. */
    public static final class Slot {
        public final String name;
        public final int enumKey, structId, attach, wrapper;
        Slot(String name, int enumKey, int structId, int attach, int wrapper) {
            this.name = name; this.enumKey = enumKey; this.structId = structId;
            this.attach = attach; this.wrapper = wrapper;
        }
    }

    /** One pinned interface group with its named components. */
    public static final class Interface {
        public final String name, sha256;
        public final int id, minComponents;
        public final Map<String, Integer> components;
        public final Map<Integer, String> componentHashes;
        public final Map<String, int[]> options;
        Interface(String name, int id, String sha256, int minComponents, Map<String, Integer> components,
                  Map<Integer, String> componentHashes, Map<String, int[]> options) {
            this.name = name; this.id = id; this.sha256 = sha256; this.minComponents = minComponents;
            this.components = Collections.unmodifiableMap(components);
            this.componentHashes = Collections.unmodifiableMap(componentHashes);
            this.options = Collections.unmodifiableMap(options);
        }
    }

    /** A pinned cs2 script. */
    public static final class Script {
        public final String name, sha256;
        public final int id;
        Script(String name, int id, String sha256) { this.name = name; this.id = id; this.sha256 = sha256; }
    }

    /** An item container definition (index 2 archive 5). */
    public static final class Container {
        public final String name, sha256;
        public final int id;
        Container(String name, int id, String sha256) { this.name = name; this.id = id; this.sha256 = sha256; }
    }

    /**
     * One symbolic packet of a recipe. Arguments are kept exactly as written in
     * the table (Integer, Boolean, String reference or List); callers resolve
     * references through {@link Native950Bindings#resolve(String)} so the table
     * never stores a cache-derived hash.
     */
    public static final class Step {
        public final String packet;
        private final Map<String, Object> args;
        Step(String packet, Map<String, Object> args) {
            this.packet = packet; this.args = Collections.unmodifiableMap(args);
        }
        public boolean has(String key) { return args.containsKey(key); }
        public Object raw(String key) { return args.get(key); }
        public Map<String, Object> args() { return args; }
        public String string(String key) {
            Object value = args.get(key);
            if (!(value instanceof String)) throw new IllegalArgumentException("Step " + packet + " has no string '" + key + "'");
            return (String) value;
        }
        public int integer(String key) {
            Object value = args.get(key);
            if (!(value instanceof Integer)) throw new IllegalArgumentException("Step " + packet + " has no integer '" + key + "'");
            return (Integer) value;
        }
        public boolean bool(String key) {
            Object value = args.get(key);
            if (!(value instanceof Boolean)) throw new IllegalArgumentException("Step " + packet + " has no boolean '" + key + "'");
            return (Boolean) value;
        }
        @SuppressWarnings("unchecked")
        public List<Object> list(String key) {
            Object value = args.get(key);
            if (!(value instanceof List)) throw new IllegalArgumentException("Step " + packet + " has no list '" + key + "'");
            return (List<Object>) value;
        }
    }

    /** A named recipe; {@code available} is false when a packet kind is not verified. */
    public static final class Op {
        public final String name;
        public final List<Step> steps;
        public final List<String> packetsRequired, missingPackets;
        public final boolean available;
        Op(String name, List<Step> steps, List<String> packetsRequired, List<String> missingPackets) {
            this.name = name; this.steps = Collections.unmodifiableList(steps);
            this.packetsRequired = Collections.unmodifiableList(packetsRequired);
            this.missingPackets = Collections.unmodifiableList(missingPackets);
            this.available = missingPackets.isEmpty();
        }
    }

    /**
     * How a {@link Resolver} decides whether a 910 id may reach the 950 wire.
     * <ul>
     * <li>{@link #ALLOW_LIST} (the policy the P5 router installs): only ids this
     *     table declares pass -- interfaces, (interface, component) pairs incl.
     *     the root slot attach/wrapper components, vars, scripts and containers.
     *     Anything else returns -1, is counted per kind and the first
     *     {@value #REPORT_LIMIT} distinct offenders per kind are kept for
     *     {@link Resolver#report()}. A 910 id that merely happens to exist in
     *     the 950 cache is NOT enough: an unverified component under a verified
     *     interface would otherwise receive 910 event masks or text and could
     *     desynchronise the client silently.</li>
     * <li>{@link #PERMISSIVE} (probes and the pre-P5 accessor): identity with
     *     cache validation -- an id passes when the 950 cache holds it.</li>
     * </ul>
     */
    public enum Policy { ALLOW_LIST, PERMISSIVE }

    /** Distinct offenders kept per kind for {@link Resolver#report()}. */
    public static final int REPORT_LIMIT = 20;

    /**
     * Id mapping for the 910 engine: known ids pass through unchanged (the table
     * carries no 910-to-950 renumbering; ids that drifted, like backpack 1473:7
     * -> 1473:5, are handled by the op recipes instead), rejected ids return -1
     * and are counted. The P5 router installs the {@link Policy#ALLOW_LIST}
     * instance into the packet facade's id map.
     */
    public interface Resolver {
        int interfaceId(int legacy);
        int componentId(int iface, int comp);
        int varp(int id);
        int varbit(int id);
        /**
         * Client variables (CLIENT_SETVARC_*). The packet family is verified since
         * Protocol S, but a varc still only reaches the wire when this table declares
         * it: the 950 varc space is not the 910 one, and a 910 varc written blind
         * would be read by whatever 950 script happens to own that id.
         */
        int varc(int id);
        int script(int id);
        int container(int id);
        /** Total ids rejected so far, all kinds. */
        long unmatched();
        /** Rejected ids for one kind: interface, component, varp, varbit, varc, script, container. */
        long unmatched(String kind);
        /** The policy this resolver applies. */
        Policy policy();
        /** Distinct rejected ids for one kind (at most {@link #REPORT_LIMIT}), formatted as text. */
        List<String> offenders(String kind);
        String report();
    }

    private final Native950CacheReader reader;
    private final int rootId, slotEnum, attachParam, wrapperParam;
    private final Map<String, Slot> slots = new LinkedHashMap<String, Slot>();
    private final Map<String, Interface> interfaces = new LinkedHashMap<String, Interface>();
    private final Map<String, Var> vars = new LinkedHashMap<String, Var>();
    private final Map<String, Script> scripts = new LinkedHashMap<String, Script>();
    private final Map<String, Container> containers = new LinkedHashMap<String, Container>();
    private final Map<String, Op> ops = new LinkedHashMap<String, Op>();
    private final Set<Integer> absentInterfaces = new HashSet<Integer>();
    /**
     * 910 interface ids that DO have a group in index 3 of the 950 cache but hold
     * different content there, so the id must still never reach the wire. Kept
     * apart from {@link #absentInterfaces} because the two are validated in
     * opposite directions: an absent id must have no group, a reassigned one must
     * have exactly the group the table pins.
     */
    private final Set<Integer> reassignedInterfaces = new HashSet<Integer>();
    private final Set<Integer> presentInterfaceIds = new HashSet<Integer>();
    /** Declared client-variable ids (kind varc or varcstr); shared by both resolvers. */
    private final Set<Integer> varcIds = new HashSet<Integer>();
    private final Resolver resolver;
    private final Resolver allowListResolver;

    private Native950Bindings(JsonObject root, Native950CacheReader reader) {
        this.reader = reader;
        List<String> problems = new ArrayList<String>();
        if (integer(root, "revision", -1) != REVISION)
            problems.add("revision must be " + REVISION);
        slotEnum = integer(root, "slotEnum", 7716);
        attachParam = integer(root, "attachParam", 3505);
        wrapperParam = integer(root, "wrapperParam", 3503);
        loadInterfaces(object(root, "interfaces"), problems);
        String rootName = string(root, "rootInterface");
        Interface rootInterface = interfaces.get(rootName);
        if (rootInterface == null) {
            problems.add("rootInterface '" + rootName + "' is not a declared interface");
            rootId = -1;
        } else {
            rootId = rootInterface.id;
        }
        loadAbsent(root.get("absentInterfaces"), problems);
        loadReassigned(object(root, "reassignedInterfaces"), problems);
        loadContainers(object(root, "containers"), problems);
        loadVars(object(root, "vars"), problems);
        loadScripts(object(root, "scripts"), problems);
        if (rootId >= 0) loadSlots(object(root, "slots"), problems);
        loadOps(object(root, "ops"), problems);
        for (Var v : vars.values()) if (v.isVarc() || v.isVarcString()) varcIds.add(v.id);
        if (!problems.isEmpty()) {
            StringBuilder message = new StringBuilder(REV + " UI binding table rejected (" + problems.size() + " problem(s)); fix the table or re-verify the cache:");
            for (String problem : problems) message.append("\n  - ").append(problem);
            throw new IllegalStateException(message.toString());
        }
        resolver = new IdentityResolver();
        allowListResolver = new AllowListResolver();
    }

    // ---------------------------------------------------------------- loading

    /**
     * Loads the table for the selected read-only 950 cache, or returns null
     * (with a logged reason) when the cache is the legacy one so no 910 path
     * ever consults 950 bindings. A validation failure on the 950 cache is a
     * hard error: the caller must not run with stale ids.
     */
    public static Native950Bindings tryLoad() {
        if (!Cache.isFlatReadOnly()) {
            System.out.println(LOG + "not loaded; the selected cache is not the read-only flat " + REV + " store, legacy 910 ids stay in force");
            return null;
        }
        return load(new Native950CacheReader.Flat());
    }

    /** Loads {@link #RESOURCE} from the classpath and validates it through {@code reader}. */
    public static Native950Bindings load(Native950CacheReader reader) {
        InputStream in = Native950Bindings.class.getResourceAsStream(RESOURCE);
        if (in == null) throw new IllegalStateException(REV + " UI binding table " + RESOURCE + " is not on the classpath (resources/ must be packaged)");
        try {
            Reader text = new InputStreamReader(in, StandardCharsets.UTF_8);
            try { return load(text, reader); } finally { text.close(); }
        } catch (java.io.IOException e) {
            throw new IllegalStateException("Cannot read " + RESOURCE, e);
        }
    }

    /** Loads a table from JSON text and validates it through {@code reader}. */
    public static Native950Bindings load(Reader json, Native950CacheReader reader) {
        if (reader == null) throw new IllegalArgumentException("A cache reader is required; bindings are never trusted unvalidated");
        JsonElement parsed = JsonParser.parseReader(json);
        if (parsed == null || !parsed.isJsonObject()) throw new IllegalStateException(REV + " UI binding table must be a JSON object");
        Native950Bindings bindings = new Native950Bindings(parsed.getAsJsonObject(), reader);
        System.out.println(LOG + "loaded " + bindings.slots.size() + " slots, " + bindings.interfaces.size() + " interfaces, "
                + bindings.vars.size() + " vars, " + bindings.scripts.size() + " scripts, " + bindings.ops.size() + " ops against the " + REV + " cache");
        return bindings;
    }

    private void loadInterfaces(JsonObject section, List<String> problems) {
        for (Map.Entry<String, JsonElement> entry : section.entrySet()) {
            if (skip(entry.getKey())) continue;
            String name = entry.getKey();
            JsonObject o = entry.getValue().getAsJsonObject();
            int id = integer(o, "id", -1);
            String sha = optionalString(o, "sha256");
            int min = integer(o, "minComponents", -1);
            Map<String, Integer> components = new LinkedHashMap<String, Integer>();
            if (o.has("components"))
                for (Map.Entry<String, JsonElement> c : o.getAsJsonObject("components").entrySet())
                    if (!skip(c.getKey())) components.put(c.getKey(), c.getValue().getAsInt());
            Map<Integer, String> hashes = new LinkedHashMap<Integer, String>();
            if (o.has("componentHashes"))
                for (Map.Entry<String, JsonElement> h : o.getAsJsonObject("componentHashes").entrySet())
                    if (!skip(h.getKey())) hashes.put(Integer.parseInt(h.getKey()), h.getValue().getAsString());
            Map<String, int[]> options = new LinkedHashMap<String, int[]>();
            if (o.has("options"))
                for (Map.Entry<String, JsonElement> t : o.getAsJsonObject("options").entrySet()) {
                    if (skip(t.getKey())) continue;
                    int[] amounts = new int[11];
                    for (Map.Entry<String, JsonElement> a : t.getValue().getAsJsonObject().entrySet()) {
                        int option = Integer.parseInt(a.getKey());
                        if (option < 1 || option > 10) { problems.add("interface " + name + " option table " + t.getKey() + " uses option " + option + " outside 1..10"); continue; }
                        JsonElement v = a.getValue();
                        if (v.isJsonPrimitive() && v.getAsJsonPrimitive().isString() && v.getAsString().equals("all")) amounts[option] = Integer.MAX_VALUE;
                        else if (v.isJsonPrimitive() && v.getAsJsonPrimitive().isNumber() && v.getAsInt() > 0) amounts[option] = v.getAsInt();
                        else problems.add("interface " + name + " option table " + t.getKey() + " option " + option + " must be a positive amount or \"all\"");
                    }
                    options.put(t.getKey(), amounts);
                }
            if (id < 0 || id > 65535) { problems.add("interface " + name + " has an invalid id " + id); continue; }
            if (min < 1) { problems.add("interface " + name + " (" + id + ") must declare minComponents >= 1"); continue; }
            if (interfaces.containsKey(name)) { problems.add("interface " + name + " is declared twice"); continue; }
            // Cache validation: group present, component count, named components, SHA-256 pins.
            if (!reader.groupExists(3, id)) { problems.add("interface " + name + " (" + id + ") is absent from index 3 of the " + REV + " cache"); continue; }
            int count = reader.fileCount(3, id);
            if (count < min) problems.add("interface " + name + " (" + id + ") has " + count + " components in the " + REV + " cache, table needs at least " + min);
            for (Map.Entry<String, Integer> c : components.entrySet()) {
                int comp = c.getValue();
                if (comp < 0 || comp >= count || !reader.fileExists(3, id, comp))
                    problems.add("interface " + name + " (" + id + ") component " + c.getKey() + " = " + comp + " is outside the " + REV + " cache's " + count + " components");
            }
            if (sha != null) checkSha(problems, "interface " + name + " (" + id + ") file 0", 3, id, 0, sha);
            for (Map.Entry<Integer, String> h : hashes.entrySet())
                checkSha(problems, "interface " + name + " (" + id + ") component " + h.getKey(), 3, id, h.getKey(), h.getValue());
            interfaces.put(name, new Interface(name, id, sha, min, components, hashes, options));
            presentInterfaceIds.add(id);
        }
    }

    private void loadAbsent(JsonElement list, List<String> problems) {
        if (list == null) return;
        for (JsonElement e : list.getAsJsonArray()) {
            int id = e.getAsInt();
            if (reader.groupExists(3, id)) problems.add("interface " + id + " is listed as absent but exists in the " + REV + " cache; re-verify it (a 910 id whose group now exists but holds other content belongs in reassignedInterfaces, not here)");
            absentInterfaces.add(id);
        }
    }

    /**
     * 910 ids that exist in this cache but are not the interface the 910 engine
     * means by them. The check runs the opposite way round from
     * {@link #loadAbsent}: the group MUST be there, with the pinned file count and
     * file-0 digest, so that "this id is occupied by something else" stays a
     * statement about a cache the loader actually read. Both resolvers then reject
     * the id, which is the outcome the absent list used to produce -- but on a true
     * premise, and with drift detection over whatever now sits there.
     */
    private void loadReassigned(JsonObject section, List<String> problems) {
        if (section == null) return;
        for (Map.Entry<String, JsonElement> entry : section.entrySet()) {
            if (skip(entry.getKey())) continue;
            String name = entry.getKey();
            JsonObject o = entry.getValue().getAsJsonObject();
            int id = integer(o, "id", -1);
            int files = integer(o, "files", -1);
            String sha = optionalString(o, "sha256");
            if (id < 0 || id > 65535) { problems.add("reassigned interface " + name + " has an invalid id " + id); continue; }
            if (absentInterfaces.contains(id)) { problems.add("interface " + id + " is listed both absent and reassigned (" + name + "); it cannot be both"); continue; }
            if (presentInterfaceIds.contains(id)) { problems.add("interface " + id + " is declared as a binding and also listed reassigned (" + name + "); a reassigned id is one this table refuses to address"); continue; }
            if (!reader.groupExists(3, id)) {
                problems.add("interface " + id + " is listed as reassigned (" + name + ") but is absent from index 3 of the " + REV + " cache; re-verify it (an id with no group belongs in absentInterfaces)");
                continue;
            }
            int count = reader.fileCount(3, id);
            if (files >= 0 && count != files)
                problems.add("reassigned interface " + name + " (" + id + ") has " + count + " components in the " + REV + " cache, table pins " + files + "; re-verify what occupies this id now");
            if (sha != null) checkSha(problems, "reassigned interface " + name + " (" + id + ") file 0", 3, id, 0, sha);
            reassignedInterfaces.add(id);
        }
    }

    private void loadContainers(JsonObject section, List<String> problems) {
        if (section == null) return;
        for (Map.Entry<String, JsonElement> entry : section.entrySet()) {
            if (skip(entry.getKey())) continue;
            JsonObject o = entry.getValue().getAsJsonObject();
            int id = integer(o, "id", -1);
            String sha = optionalString(o, "sha256");
            if (id < 0 || !reader.fileExists(2, 5, id)) { problems.add("container " + entry.getKey() + " (" + id + ") is absent from 2/5 of the " + REV + " cache"); continue; }
            if (sha != null) checkSha(problems, "container " + entry.getKey() + " (" + id + ")", 2, 5, id, sha);
            containers.put(entry.getKey(), new Container(entry.getKey(), id, sha));
        }
    }

    private void loadVars(JsonObject section, List<String> problems) {
        for (Map.Entry<String, JsonElement> entry : section.entrySet()) {
            if (skip(entry.getKey())) continue;
            String name = entry.getKey();
            JsonObject o = entry.getValue().getAsJsonObject();
            String kind = string(o, "kind");
            int id = integer(o, "id", -1);
            int max = integer(o, "maxValue", -1);
            if (id < 0 || max < 0) { problems.add("var " + name + " needs a nonnegative id and maxValue"); continue; }
            int baseVarp = -1, startBit = -1, endBit = -1;
            if (kind.equals("varp")) {
                if (!reader.fileExists(2, 60, id)) problems.add("varp " + name + " (" + id + ") is absent from 2/60 of the " + REV + " cache");
            } else if (kind.equals("varbit")) {
                int[] range = bitRange(o, problems, "varbit " + name + " (" + id + ")");
                baseVarp = integer(o, "varp", -1);
                if (baseVarp < 0) problems.add("varbit " + name + " (" + id + ") must declare the base varp it writes");
                if (range != null) { startBit = range[0]; endBit = range[1]; }
                int[] def = reader.varbit(id);
                if (def == null) { problems.add("varbit " + name + " (" + id + ") is absent from 2/69 of the " + REV + " cache"); }
                else {
                    int width = def[2] - def[1];
                    if (width < 0 || width > 31 || def[0] < 0) problems.add("varbit " + name + " (" + id + ") decodes to an invalid definition base " + def[0] + " bits " + def[1] + ".." + def[2]);
                    else if (max > widthMask(width)) problems.add("varbit " + name + " (" + id + ") is " + (width + 1) + " bit(s) wide in the " + REV + " cache; maxValue " + max + " does not fit");
                    if (baseVarp >= 0 && baseVarp != def[0])
                        problems.add("varbit " + name + " (" + id + ") declares base varp " + baseVarp + " but the " + REV + " cache decodes varp " + def[0] + "; re-verify it");
                    if (range != null && (startBit != def[1] || endBit != def[2]))
                        problems.add("varbit " + name + " (" + id + ") declares bits " + startBit + ".." + endBit + " but the " + REV + " cache decodes " + def[1] + ".." + def[2] + "; re-verify it");
                }
            } else if (!kind.equals("varc") && !kind.equals("varcstr")) {
                problems.add("var " + name + " has unknown kind '" + kind + "' (varp|varbit|varc|varcstr)");
                continue;
            }
            vars.put(name, new Var(name, kind, id, max, baseVarp, startBit, endBit));
        }
    }

    private void loadScripts(JsonObject section, List<String> problems) {
        for (Map.Entry<String, JsonElement> entry : section.entrySet()) {
            if (skip(entry.getKey())) continue;
            String name = entry.getKey();
            JsonObject o = entry.getValue().getAsJsonObject();
            int id = integer(o, "id", -1);
            String sha = optionalString(o, "sha256");
            if (id < 0 || sha == null) { problems.add("script " + name + " needs an id and a sha256 pin"); continue; }
            if (!reader.groupExists(12, id)) { problems.add("script " + name + " (" + id + ") is absent from index 12 of the " + REV + " cache"); continue; }
            checkSha(problems, "script " + name + " (" + id + ")", 12, id, 0, sha);
            scripts.put(name, new Script(name, id, sha));
        }
    }

    /**
     * Slot resolution: enum 7716[key] -> struct id -> params 3505/3503. The values
     * are full {@code interface << 16 | component} hashes; the loader insists on
     * {@code hash >= 0} and {@code hash >>> 16 == root}, which is the check the
     * verified Kotlin handoff performs and the legacy {@code & 0xfff} resolver skips.
     */
    private void loadSlots(JsonObject section, List<String> problems) {
        int rootCount = reader.fileCount(3, rootId);
        for (Map.Entry<String, JsonElement> entry : section.entrySet()) {
            if (skip(entry.getKey())) continue;
            String name = entry.getKey();
            JsonObject o = entry.getValue().getAsJsonObject();
            int key = integer(o, "enumKey", -1);
            if (key < 0) { problems.add("slot " + name + " needs an enumKey"); continue; }
            int structId = reader.enumInt(slotEnum, key);
            if (structId < 0) { problems.add("slot " + name + ": enum " + slotEnum + " has no key " + key + " in the " + REV + " cache"); continue; }
            int attach = reader.structInt(structId, attachParam);
            int wrapper = reader.structInt(structId, wrapperParam);
            boolean ok = true;
            if (attach < 0 || (attach >>> 16) != rootId || (attach & 0xffff) >= rootCount) {
                problems.add("slot " + name + ": struct " + structId + " param " + attachParam + " = " + attach + " is not a component of root " + rootId); ok = false;
            }
            if (wrapper < 0 || (wrapper >>> 16) != rootId || (wrapper & 0xffff) >= rootCount) {
                problems.add("slot " + name + ": struct " + structId + " param " + wrapperParam + " = " + wrapper + " is not a component of root " + rootId); ok = false;
            }
            String pinnedAttach = optionalString(o, "verifiedAttach");
            if (pinnedAttach != null && attach != parseHash(pinnedAttach, problems, "slot " + name + " verifiedAttach")) {
                problems.add("slot " + name + " resolved attach " + hashText(attach) + " but the verified binding is " + pinnedAttach); ok = false;
            }
            String pinnedWrapper = optionalString(o, "verifiedWrapper");
            if (pinnedWrapper != null && wrapper != parseHash(pinnedWrapper, problems, "slot " + name + " verifiedWrapper")) {
                problems.add("slot " + name + " resolved wrapper " + hashText(wrapper) + " but the verified binding is " + pinnedWrapper); ok = false;
            }
            if (ok) slots.put(name, new Slot(name, key, structId, attach, wrapper));
        }
    }

    private void loadOps(JsonObject section, List<String> problems) {
        for (Map.Entry<String, JsonElement> entry : section.entrySet()) {
            if (skip(entry.getKey())) continue;
            String name = entry.getKey();
            JsonObject o = entry.getValue().getAsJsonObject();
            List<String> required = new ArrayList<String>();
            if (o.has("packets_required")) for (JsonElement e : o.getAsJsonArray("packets_required")) required.add(e.getAsString());
            List<String> missing = new ArrayList<String>();
            for (String packet : required) if (!isVerifiedPacket(packet) && !missing.contains(packet)) missing.add(packet);
            List<Step> steps = new ArrayList<Step>();
            JsonArray array = o.has("steps") ? o.getAsJsonArray("steps") : new JsonArray();
            for (int i = 0; i < array.size(); i++) {
                JsonObject s = array.get(i).getAsJsonObject();
                String packet = optionalString(s, "packet");
                String where = "op " + name + " step " + i;
                if (packet == null) { problems.add(where + " has no packet kind"); continue; }
                if (!required.contains(packet)) problems.add(where + " uses " + packet + " which packets_required does not list");
                Map<String, Object> args = new LinkedHashMap<String, Object>();
                for (Map.Entry<String, JsonElement> a : s.entrySet()) {
                    if (skip(a.getKey()) || a.getKey().equals("packet")) continue;
                    args.put(a.getKey(), value(a.getValue()));
                }
                validateStep(where, packet, args, problems);
                steps.add(new Step(packet, args));
            }
            ops.put(name, new Op(name, steps, required, missing));
            if (!missing.isEmpty()) System.out.println(LOG + "op " + name + " unavailable; unverified packet(s) " + missing);
        }
    }

    /** Every reference inside a step must resolve now; a typo is a table bug, not a runtime surprise. */
    private void validateStep(String where, String packet, Map<String, Object> args, List<String> problems) {
        for (Map.Entry<String, Object> a : args.entrySet()) {
            String key = a.getKey();
            Object v = a.getValue();
            try {
                if (key.equals("interface")) interfaceId((String) v);
                else if (key.equals("parent") || key.equals("component")) resolve((String) v);
                else if (key.equals("var")) {
                    Var var = var((String) v);
                    if (args.containsKey("value")) {
                        int value = (Integer) args.get("value");
                        if (value < 0 || value > var.maxValue) problems.add(where + " writes " + value + " to " + var.name + " whose maxValue is " + var.maxValue);
                        if (packet.equals("VARBIT_SMALL") && (!var.isVarbit() || value > 255)) problems.add(where + " VARBIT_SMALL needs a varbit value 0..255");
                    }
                } else if (key.equals("script")) script((String) v);
                else if (key.equals("args") || key.equals("retain")) {
                    for (Object item : (List<?>) v) if (item instanceof String) resolve((String) item);
                    else if (!(item instanceof Integer)) problems.add(where + " list '" + key + "' holds a non-integer, non-reference value " + item);
                } else if (key.equals("retainSlots")) {
                    for (Object item : (List<?>) v) slot((String) item);
                }
            } catch (RuntimeException e) {
                problems.add(where + " '" + key + "': " + e.getMessage());
            }
        }
    }

    // ---------------------------------------------------------------- lookups

    public int rootInterfaceId() { return rootId; }
    public int slotEnum() { return slotEnum; }

    public int interfaceId(String name) { return iface(name).id; }

    public Interface iface(String name) {
        Interface i = interfaces.get(name);
        if (i == null) throw new IllegalArgumentException("unknown " + REV + " interface binding '" + name + "'");
        return i;
    }

    public int component(String interfaceName, String componentName) {
        Integer c = iface(interfaceName).components.get(componentName);
        if (c == null) throw new IllegalArgumentException("unknown " + REV + " component binding '" + interfaceName + "." + componentName + "'");
        return c;
    }

    /** {@code interface << 16 | component} for a named component. */
    public int componentHash(String interfaceName, String componentName) {
        return iface(interfaceName).id << 16 | component(interfaceName, componentName);
    }

    public Var var(String name) {
        Var v = vars.get(name);
        if (v == null) throw new IllegalArgumentException("unknown " + REV + " var binding '" + name + "'");
        return v;
    }

    public int script(String name) {
        Script s = scripts.get(name);
        if (s == null) throw new IllegalArgumentException("unknown " + REV + " script binding '" + name + "'");
        return s.id;
    }

    public Slot slot(String name) {
        Slot s = slots.get(name);
        if (s == null) throw new IllegalArgumentException("unknown " + REV + " slot binding '" + name + "'");
        return s;
    }

    public int slotAttach(String name) { return slot(name).attach; }
    public int slotWrapper(String name) { return slot(name).wrapper; }

    public int container(String name) {
        Container c = containers.get(name);
        if (c == null) throw new IllegalArgumentException("unknown " + REV + " container binding '" + name + "'");
        return c.id;
    }

    public Op ops(String name) {
        Op op = ops.get(name);
        if (op == null) throw new IllegalArgumentException("unknown " + REV + " op recipe '" + name + "'");
        return op;
    }

    /** Native one-based option amounts for an interface option table; Integer.MAX_VALUE means "all", 0 disabled. */
    public int[] optionAmounts(String interfaceName, String table) {
        int[] amounts = iface(interfaceName).options.get(table);
        if (amounts == null) throw new IllegalArgumentException("unknown " + REV + " option table '" + interfaceName + "." + table + "'");
        return amounts.clone();
    }

    public Set<String> slotNames() { return Collections.unmodifiableSet(slots.keySet()); }
    public Set<String> interfaceNames() { return Collections.unmodifiableSet(interfaces.keySet()); }
    public Set<String> varNames() { return Collections.unmodifiableSet(vars.keySet()); }
    public Set<String> scriptNames() { return Collections.unmodifiableSet(scripts.keySet()); }
    public Set<String> containerNames() { return Collections.unmodifiableSet(containers.keySet()); }
    public Set<String> opNames() { return Collections.unmodifiableSet(ops.keySet()); }
    public Set<Integer> absentInterfaceIds() { return Collections.unmodifiableSet(absentInterfaces); }
    /** 910 ids present in this cache but holding other content; rejected like the absent ones. */
    public Set<Integer> reassignedInterfaceIds() { return Collections.unmodifiableSet(reassignedInterfaces); }
    /** Every 910 interface id this table blocks, whichever reason it blocks it for. */
    public Set<Integer> blockedInterfaceIds() {
        Set<Integer> all = new HashSet<Integer>(absentInterfaces);
        all.addAll(reassignedInterfaces);
        return Collections.unmodifiableSet(all);
    }

    /**
     * Resolves a symbolic reference used by op recipes:
     * {@code slot:<name>.attach}, {@code slot:<name>.wrapper} (32-bit hashes),
     * {@code slotkey:<name>} (the enum 7716 key), {@code container:<name>},
     * {@code interface:<name>} (bare id) or {@code <interface>.<component>} (hash).
     */
    public int resolve(String reference) {
        if (reference.startsWith("slot:")) {
            String rest = reference.substring(5);
            int dot = rest.lastIndexOf('.');
            if (dot < 0) throw new IllegalArgumentException("slot reference '" + reference + "' needs .attach or .wrapper");
            String field = rest.substring(dot + 1);
            Slot s = slot(rest.substring(0, dot));
            if (field.equals("attach")) return s.attach;
            if (field.equals("wrapper")) return s.wrapper;
            throw new IllegalArgumentException("slot reference '" + reference + "' needs .attach or .wrapper");
        }
        if (reference.startsWith("slotkey:")) return slot(reference.substring(8)).enumKey;
        if (reference.startsWith("container:")) return container(reference.substring(10));
        if (reference.startsWith("interface:")) return interfaceId(reference.substring(10));
        int dot = reference.indexOf('.');
        if (dot <= 0) throw new IllegalArgumentException("unresolvable " + REV + " binding reference '" + reference + "'");
        return componentHash(reference.substring(0, dot), reference.substring(dot + 1));
    }

    /**
     * The {@link Policy#PERMISSIVE} resolver (identity with cache validation).
     * Kept as the bare accessor for probes and the pre-P5 callers; the router
     * installs {@link #allowListResolver()} into the packet facade.
     */
    public Resolver resolver() { return resolver; }

    /** The resolver for one policy; both instances count independently. */
    public Resolver resolver(Policy policy) {
        return policy == Policy.PERMISSIVE ? resolver : allowListResolver;
    }

    /**
     * The {@link Policy#ALLOW_LIST} resolver: only ids declared in this table
     * pass. This is what the P5 router installs into {@code Native950IdMap} so
     * a 910 emitter can only ever address a component, var, script or container
     * that this table declares; everything else is a counted drop.
     */
    public Resolver allowListResolver() { return allowListResolver; }

    // ---------------------------------------------------------------- resolver

    /** Shared rejection bookkeeping: per-kind counters plus the first distinct offenders. */
    private abstract class CountingResolver implements Resolver {
        private final Map<String, java.util.concurrent.atomic.AtomicLong> counts = new ConcurrentHashMap<String, java.util.concurrent.atomic.AtomicLong>();
        private final Map<String, Set<String>> offenders = new ConcurrentHashMap<String, Set<String>>();
        private final Set<String> logged = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());

        final int reject(String kind, int id, String why) {
            java.util.concurrent.atomic.AtomicLong count = counts.get(kind);
            if (count == null) {
                counts.putIfAbsent(kind, new java.util.concurrent.atomic.AtomicLong());
                count = counts.get(kind);
            }
            count.incrementAndGet();
            String text = kind.equals("component") ? hashText(id) : String.valueOf(id);
            Set<String> kept = offenders.get(kind);
            if (kept == null) {
                offenders.putIfAbsent(kind, Collections.synchronizedSet(new java.util.LinkedHashSet<String>()));
                kept = offenders.get(kind);
            }
            synchronized (kept) {
                if (kept.size() < REPORT_LIMIT) kept.add(text);
            }
            if (logged.add(kind + ":" + id)) System.out.println(LOG + kind + " " + text + " has no " + REV + " binding (" + why + "); rejected");
            return -1;
        }

        @Override public long unmatched() {
            long total = 0;
            for (java.util.concurrent.atomic.AtomicLong c : counts.values()) total += c.get();
            return total;
        }

        @Override public long unmatched(String kind) {
            java.util.concurrent.atomic.AtomicLong c = counts.get(kind);
            return c == null ? 0 : c.get();
        }

        @Override public List<String> offenders(String kind) {
            Set<String> kept = offenders.get(kind);
            if (kept == null) return Collections.emptyList();
            synchronized (kept) { return new ArrayList<String>(kept); }
        }

        @Override public String report() {
            StringBuilder out = new StringBuilder(REV + " id resolver[" + policy() + "]: " + unmatched() + " rejected");
            for (Map.Entry<String, java.util.concurrent.atomic.AtomicLong> e : new java.util.TreeMap<String, java.util.concurrent.atomic.AtomicLong>(counts).entrySet()) {
                out.append(", ").append(e.getKey()).append('=').append(e.getValue().get());
                List<String> kept = offenders(e.getKey());
                if (!kept.isEmpty()) out.append(' ').append(kept);
            }
            return out.toString();
        }
    }

    /** {@link Policy#PERMISSIVE}: identity for every id the 950 cache holds. */
    private final class IdentityResolver extends CountingResolver {
        @Override public Policy policy() { return Policy.PERMISSIVE; }

        @Override public int interfaceId(int legacy) {
            if (legacy < 0 || absentInterfaces.contains(legacy)) return reject("interface", legacy, "absent from the cache");
            // A reassigned id would otherwise sail through here: the group exists, so the
            // permissive "does the cache hold it" test says yes while the content behind it
            // is not what the 910 caller means.
            if (reassignedInterfaces.contains(legacy)) return reject("interface", legacy, "the group exists on " + REV + " but holds different content; see reassignedInterfaces");
            if (presentInterfaceIds.contains(legacy) || reader.groupExists(3, legacy)) return legacy;
            return reject("interface", legacy, "absent from the cache");
        }

        @Override public int componentId(int iface, int comp) {
            if (interfaceId(iface) < 0) return -1;
            int count = reader.fileCount(3, iface);
            if (comp < 0 || comp >= count || !reader.fileExists(3, iface, comp)) return reject("component", iface << 16 | comp, "absent from the cache");
            return comp;
        }

        @Override public int varp(int id) { return id >= 0 && reader.fileExists(2, 60, id) ? id : reject("varp", id, "absent from the cache"); }
        @Override public int varbit(int id) { return id >= 0 && reader.fileExists(2, 69, id) ? id : reject("varbit", id, "absent from the cache"); }
        /**
         * Client variables have no cache archive to check against (they live only in
         * the client's own store), so even the permissive resolver can do no better
         * than the declared set; an undeclared varc is rejected in both policies.
         */
        @Override public int varc(int id) { return varcIds.contains(id) ? id : reject("varc", id, "not declared in the binding table"); }
        @Override public int script(int id) { return id >= 0 && reader.groupExists(12, id) ? id : reject("script", id, "absent from the cache"); }
        @Override public int container(int id) { return id >= 0 && reader.fileExists(2, 5, id) ? id : reject("container", id, "absent from the cache"); }
    }

    /**
     * {@link Policy#ALLOW_LIST}: identity for declared ids only. The sets are
     * built once from the validated table: every interface id, every named
     * component hash plus the root slot attach/wrapper hashes (the components
     * {@code sendInterface}/{@code closeInterface} address when the 910
     * InterfaceManager opens a sub-interface in a HUD slot), every var id by
     * kind, every script id and every container id.
     */
    private final class AllowListResolver extends CountingResolver {
        private final Set<Integer> interfaceIds = new HashSet<Integer>();
        private final Set<Integer> componentHashes = new HashSet<Integer>();
        private final Set<Integer> varpIds = new HashSet<Integer>();
        private final Set<Integer> varbitIds = new HashSet<Integer>();
        private final Set<Integer> scriptIds = new HashSet<Integer>();
        private final Set<Integer> containerIds = new HashSet<Integer>();

        AllowListResolver() {
            for (Interface i : interfaces.values()) {
                interfaceIds.add(i.id);
                for (int comp : i.components.values()) componentHashes.add(i.id << 16 | comp);
            }
            for (Slot s : slots.values()) { componentHashes.add(s.attach); componentHashes.add(s.wrapper); }
            // One set per kind. The pre-M3 expression sent varc/varcstr ids into a
            // throwaway HashSet, so a declared varc could never have been resolved.
            for (Var v : vars.values()) {
                if (v.isVarp()) varpIds.add(v.id);
                else if (v.isVarbit()) varbitIds.add(v.id);
            }
            for (Script s : scripts.values()) scriptIds.add(s.id);
            for (Container c : containers.values()) containerIds.add(c.id);
        }

        @Override public Policy policy() { return Policy.ALLOW_LIST; }
        @Override public int interfaceId(int legacy) {
            if (interfaceIds.contains(legacy)) return legacy;
            if (reassignedInterfaces.contains(legacy)) return reject("interface", legacy, "the group exists on " + REV + " but holds different content; see reassignedInterfaces");
            return reject("interface", legacy, "not declared in the binding table");
        }
        @Override public int componentId(int iface, int comp) {
            if (interfaceId(iface) < 0) return -1;
            return componentHashes.contains(iface << 16 | comp) ? comp : reject("component", iface << 16 | comp, "not declared in the binding table");
        }
        @Override public int varp(int id) { return varpIds.contains(id) ? id : reject("varp", id, "not declared in the binding table"); }
        @Override public int varbit(int id) { return varbitIds.contains(id) ? id : reject("varbit", id, "not declared in the binding table"); }
        @Override public int varc(int id) { return varcIds.contains(id) ? id : reject("varc", id, "not declared in the binding table"); }
        @Override public int script(int id) { return scriptIds.contains(id) ? id : reject("script", id, "not declared in the binding table"); }
        @Override public int container(int id) { return containerIds.contains(id) ? id : reject("container", id, "not declared in the binding table"); }
    }

    // ---------------------------------------------------------------- helpers

    private static boolean isVerifiedPacket(String name) {
        for (ServerPacket packet : ServerPacket.values()) if (packet.name().equals(name)) return true;
        return false;
    }

    static int widthMask(int width) { return width >= 31 ? -1 >>> 1 : (1 << (width + 1)) - 1; }

    /** Parses a {@code "lo..hi"} inclusive bit range, or null (with a problem recorded) when it is missing or malformed. */
    private static int[] bitRange(JsonObject o, List<String> problems, String what) {
        String text = optionalString(o, "bits");
        if (text == null) { problems.add(what + " must declare its bit range as \"lo..hi\""); return null; }
        int dots = text.indexOf("..");
        try {
            if (dots < 0) throw new NumberFormatException();
            int lo = Integer.parseInt(text.substring(0, dots).trim());
            int hi = Integer.parseInt(text.substring(dots + 2).trim());
            if (lo < 0 || hi < lo || hi > 31) throw new NumberFormatException();
            return new int[] {lo, hi};
        } catch (NumberFormatException e) {
            problems.add(what + " bit range '" + text + "' must look like lo..hi within 0..31");
            return null;
        }
    }

    private void checkSha(List<String> problems, String what, int index, int group, int file, String expected) {
        String actual = reader.sha256(index, group, file);
        if (actual == null) { problems.add(what + " is missing from the " + REV + " cache (" + index + "/" + group + "/" + file + ")"); return; }
        if (!actual.equalsIgnoreCase(expected)
                && !com.rs.game.player.client.Native950LibraryBridge.matches(index+"/"+group+"/"+file, expected, actual))
            problems.add(what + " changed: " + REV + " cache " + index + "/" + group + "/" + file + " is " + actual + ", table pins " + expected + "; re-verify its bindings");
    }

    public static String sha256(byte[] data) {
        try {
            StringBuilder out = new StringBuilder();
            for (byte b : MessageDigest.getInstance("SHA-256").digest(data)) out.append(String.format("%02x", b & 255));
            return out.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private static int parseHash(String text, List<String> problems, String what) {
        int colon = text.indexOf(':');
        try {
            if (colon < 0) throw new NumberFormatException();
            int iface = Integer.parseInt(text.substring(0, colon));
            int comp = Integer.parseInt(text.substring(colon + 1));
            if (iface < 0 || iface > 65535 || comp < 0 || comp > 65535) throw new NumberFormatException();
            return iface << 16 | comp;
        } catch (NumberFormatException e) {
            problems.add(what + " '" + text + "' must look like interface:component");
            return Integer.MIN_VALUE;
        }
    }

    public static String hashText(int hash) { return (hash >>> 16) + ":" + (hash & 0xffff); }

    private static boolean skip(String key) { return key.startsWith("_"); }

    private static JsonObject object(JsonObject o, String key) {
        JsonElement e = o.get(key);
        if (e == null) return new JsonObject();
        if (!e.isJsonObject()) throw new IllegalStateException(REV + " UI binding table section '" + key + "' must be an object");
        return e.getAsJsonObject();
    }

    private static int integer(JsonObject o, String key, int fallback) {
        JsonElement e = o.get(key);
        return e == null || !e.isJsonPrimitive() || !e.getAsJsonPrimitive().isNumber() ? fallback : e.getAsInt();
    }

    private static String string(JsonObject o, String key) {
        String s = optionalString(o, key);
        if (s == null) throw new IllegalStateException(REV + " UI binding table entry lacks string '" + key + "': " + o);
        return s;
    }

    private static String optionalString(JsonObject o, String key) {
        JsonElement e = o.get(key);
        return e == null || !e.isJsonPrimitive() || !e.getAsJsonPrimitive().isString() ? null : e.getAsString();
    }

    private static Object value(JsonElement e) {
        if (e.isJsonPrimitive()) {
            if (e.getAsJsonPrimitive().isBoolean()) return e.getAsBoolean();
            if (e.getAsJsonPrimitive().isNumber()) return e.getAsInt();
            return e.getAsString();
        }
        if (e.isJsonArray()) {
            List<Object> list = new ArrayList<Object>();
            for (JsonElement item : e.getAsJsonArray()) list.add(value(item));
            return Collections.unmodifiableList(list);
        }
        throw new IllegalStateException(REV + " UI binding step arguments must be scalars or lists: " + e);
    }
}
