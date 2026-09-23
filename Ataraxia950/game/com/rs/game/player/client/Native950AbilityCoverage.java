package com.rs.game.player.client;

import com.google.gson.GsonBuilder;
import com.rs.cache.Cache;
import com.rs.cache.filestore.store.Index;
import com.rs.cache.loaders.rs3.RS3ClientScriptMap;
import com.rs.cache.loaders.rs3.RS3GeneralRequirementMap;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.*;

/** Offline cache-wide inventory, not a gameplay admission list or claim of visual coverage. */
public final class Native950AbilityCoverage {
    private Native950AbilityCoverage() { }

    public static void main(String[] args) throws Exception {
        if (args.length != 2) throw new IllegalArgumentException("Usage: Native950AbilityCoverage <950-cache> <report.json>");
        if (!NativeCacheVerification.isEnforced()) throw new IllegalStateException("Cache verification must remain enabled");
        Cache.initFlatReadOnly(Paths.get(args[0]));
        Native950AbilityAssets.verify();
        if(Boolean.getBoolean("ability.dumpBookScript")) {
            scriptConstants(Paths.get("protocol-analysis/ui-scripts-950-evidence.json"),Integer.getInteger("ability.script",6995),true);
            return;
        }
        Map<Integer, Map<String, Object>> rows = new TreeMap<>();
        Index structures = Cache.STORE.getIndexes()[22];
        for (int id : ids(structures, 5)) {
            RS3GeneralRequirementMap definition = RS3GeneralRequirementMap.getMap(id);
            if (!abilityShaped(definition.getValues())) continue;
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("struct", id);
            row.put("name", definition.getStringValue(2794));
            row.put("key", definition.getIntValue(2793));
            row.put("tier", definition.getIntValue(2799));
            row.put("level", definition.getIntValue(2807));
            row.put("cooldown", definition.getIntValue(2796));
            row.put("sha256", hash(structures.getFile(id >> 5, id & 31)));
            row.put("enumMembership", new ArrayList<Map<String, Object>>());
            row.put("animationEnum", definition.getValue(2915));
            row.put("animation", definition.getValue(2914));
            row.put("casterGraphic", definition.getValue(2920));
            row.put("projectile", definition.getValue(2940));
            row.put("targetGraphic", definition.getValue(2933));
            row.put("typedParameters", new TreeMap<>(definition.getValues()));
            Native950AbilityCatalog.Definition implemented = Native950AbilityCatalog.get(id);
            row.put("status", implemented == null ? "missing" : "partial");
            row.put("serverRoutedBook", implemented == null ? null : implemented.book);
            Map<String,Object> lifecycle=new LinkedHashMap<>();
            lifecycle.put("target",implemented==null?"unimplemented":implemented.effect==Native950AbilityCatalog.Effect.MOVEMENT?"tile-or-direction":implemented.targetRequired()?"entity":"self");
            lifecycle.put("weaponStyle",implemented==null?null:implemented.style());
            lifecycle.put("dualWield",definition.getIntValue(2811)==1);
            lifecycle.put("twoHanded",definition.getIntValue(2812)==1);
            lifecycle.put("shield",definition.getIntValue(2813)==1);
            lifecycle.put("adrenalineCostTenths",definition.getValue(2798));
            lifecycle.put("adrenalineGainTenths",definition.getValue(2800));
            lifecycle.put("effect",implemented==null?null:implemented.effect.name());
            lifecycle.put("channelTicks",implemented==null?null:implemented.channelTicks());
            lifecycle.put("channelHitInterval",definition.getValue(8884));
            lifecycle.put("channelAdditionalHits",definition.getValue(8885));
            lifecycle.put("cooldownStart",implemented==null?null:"activation");
            lifecycle.put("presentationStatus","requires per-ability acceptance; optional IDs are not invented");
            row.put("lifecycle",lifecycle);
            row.put("blockers", implemented == null
                    ? Arrays.asList("No native combat catalog implementation; enum membership alone does not authorize execution")
                    : Arrays.asList("Combat Alpha damage/effect model; not full retail semantics", "Visual and complete effect acceptance not established by this inventory"));
            row.put("liveVerifiedByThisTool", false);
            rows.put(id, row);
        }
        Index enums = Cache.STORE.getIndexes()[17];
        for (int id : ids(enums, 8)) {
            RS3ClientScriptMap map = RS3ClientScriptMap.getMap(id);
            // STRUCT is legacy character J, but numeric ScriptVarType 73 in opcode 102.
            if (!map.hasStructValues() || map.getValues() == null) continue;
            for (Map.Entry<Long, Object> entry : new TreeMap<>(map.getValues()).entrySet()) {
                if (!(entry.getValue() instanceof Integer)) continue;
                Map<String, Object> row = rows.get((Integer) entry.getValue());
                if (row == null) continue;
                Map<String, Object> membership = new LinkedHashMap<>();
                membership.put("enum", id); membership.put("key", entry.getKey());
                membership.put("sha256", hash(enums.getFile(id >> 8, id & 255)));
                @SuppressWarnings("unchecked") List<Map<String, Object>> memberships = (List<Map<String, Object>>) row.get("enumMembership");
                memberships.add(membership);
            }
        }
        java.nio.file.Path opcodeEvidence=Paths.get("protocol-analysis/ui-scripts-950-evidence.json");
        Set<Integer> bookConstants=scriptConstants(opcodeEvidence,6995,false);
        Set<Integer> transformed=scriptConstants(opcodeEvidence,8247,false);
        Map<Integer,String> books=new LinkedHashMap<>();
        books.put(10147,"melee");books.put(6738,"ranged");books.put(6740,"magic");
        books.put(6736,"defence");books.put(6737,"constitution");books.put(16973,"necromancy");
        for(int id:books.keySet())if(!bookConstants.contains(id))throw new IllegalStateException("Missing native book anchor "+id);
        Map<String,Integer> visibilityCounts=new TreeMap<>();
        for(Map<String,Object> row:rows.values()) {
            @SuppressWarnings("unchecked") List<Map<String,Object>> memberships=(List<Map<String,Object>>)row.get("enumMembership");
            List<String> nativeBooks=new ArrayList<>();
            for(Map<String,Object> membership:memberships)if(books.containsKey(membership.get("enum")))nativeBooks.add(books.get(membership.get("enum")));
            String visibility=!nativeBooks.isEmpty()?"native-book-entry":transformed.contains((Integer)row.get("struct"))?"conditional-native-transform":"not-reached-by-reviewed-books";
            row.put("nativeBooks",nativeBooks);row.put("visibilityClass",visibility);
            String membership=!nativeBooks.isEmpty()?"CANONICAL":transformed.contains((Integer)row.get("struct"))?"CONDITIONAL/ALTERNATE":"UNPROVEN PLAYER-FACING MEMBERSHIP";
            row.put("canonicalClass",membership);
            row.put("implementationStatus",Native950AbilityCatalog.get((Integer)row.get("struct"))==null?"MISSING":"PARTIAL");
            row.put("automatedTestStatus","see separate real-cache acceptance keyed by struct; inventory is definition evidence only");
            row.put("liveVisualStatus","NOT VERIFIED BY THIS PASS");
            row.put("conditionalTransformReferenced",transformed.contains((Integer)row.get("struct")));
            row.put("visibilityLimit","Availability/unlock/weapon-mode filters are player-specific; unreferenced does not prove obsolete");
            visibilityCounts.merge(visibility,1,Integer::sum);
        }
        for (Native950AbilityCatalog.Definition d : Native950AbilityCatalog.DEFINITIONS) {
            if (!rows.containsKey(d.struct)) throw new IllegalStateException("Inventory missed implemented ability " + d.struct);
            @SuppressWarnings("unchecked") List<Map<String,Object>> memberships=(List<Map<String,Object>>) rows.get(Native950AbilityCatalog.base(d.struct)).get("enumMembership");
            if (memberships.stream().noneMatch(m -> ((Integer)m.get("enum"))==Native950ActionBar.enumFor(d.book)
                    && ((Long)m.get("key"))==d.key))
                throw new IllegalStateException("Inventory missed known native book membership " + d.struct);
        }
        Map<String, Integer> counts = new TreeMap<>();
        for (Map<String, Object> row : rows.values()) counts.merge((String) row.get("status"), 1, Integer::sum);
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("revision", 950); report.put("schemaVersion", 3);
        report.put("nativeBookEnums",books);report.put("visibilityCounts",visibilityCounts);
        report.put("visibilityEvidence","Exact950 script6995 book dispatch and script8247 conditional transformation; both hash-pinned. Mirrors Undercut AbilityBooks/AbilityTransform architecture without copying its numeric IDs.");
        report.put("scope", "All cache index-22 structures with typed name/key/tier; absent cooldown is retained rather than dropping basic attacks/conjures. Canonical membership is native-book reachability, with conditional alternates separate. Unreached is not proof of non-player-facing.");
        report.put("membershipRule", "All index-17 struct-reference enums: legacy opcode 2 character J=74 or modern opcode 102 ScriptVarType=73; membership is evidence, not executable admission");
        report.put("nativeWrites", false); report.put("playerSavesAccessed", false);
        report.put("counts", counts); report.put("total", rows.size()); report.put("abilities", rows.values());
        Files.write(Paths.get(args[1]), new GsonBuilder().serializeNulls().disableHtmlEscaping().setPrettyPrinting().create().toJson(report).getBytes(StandardCharsets.UTF_8));
        System.out.println("ABILITY INVENTORY: " + rows.size() + " cache definitions; " + counts);
    }

    static boolean abilityShaped(Map<Long, Object> values) {
        return values != null && values.get(2794L) instanceof String && !((String) values.get(2794L)).trim().isEmpty()
                && values.get(2793L) instanceof Integer && values.get(2799L) instanceof Integer;
    }

    /** Reuses the repository's independently proven opcode map; never infers new opcodes. */
    private static Set<Integer> scriptConstants(java.nio.file.Path evidence,int script,boolean print) throws Exception {
        com.google.gson.JsonObject map=new com.google.gson.JsonParser().parse(new String(Files.readAllBytes(evidence),StandardCharsets.UTF_8))
                .getAsJsonObject().getAsJsonObject("opcodeMap947to950");
        Map<Integer,Integer> inverse=new HashMap<>();
        for(Map.Entry<String,com.google.gson.JsonElement> entry:map.entrySet())
            inverse.put(Integer.decode(entry.getValue().getAsJsonObject().get("opcode950").getAsString()),Integer.decode(entry.getKey()));
        byte[] raw=Cache.STORE.getIndexes()[12].getFile(script,0);
        String expected=script==6995?"b371951bd526283bf7d8560faa153a032239bd3db069a47fecbf71bd45e9e274":script==8247?"0fb25f13c1de3cb06d670cc25c20c751cfffc7e03e07a94b8c49631f9dc63b56":null;
        if(!print&&(expected==null||!expected.equals(hash(raw))))throw new IllegalStateException("Changed canonical ability script "+script);
        Set<Integer> constants=new TreeSet<>();
        java.nio.ByteBuffer b=java.nio.ByteBuffer.wrap(raw);
        int end=raw.length-(b.getShort(raw.length-2)&65535)-18;
        while(b.get()!=0){}
        Set<Integer> wide=new HashSet<>(Arrays.asList(0x35e,0x592,0x713,0x647,0x412,0xab,0x454,0x73d,0x3f2,0x25a,0x717,0x895,0x267,0x51a,0x56,0x96,0x639,0x1ca,0x195,0x30,0xa2));
        int count=0;
        while(b.position()<end){
            int offset=b.position(),nativeOp=b.getShort()&65535;
            Integer op=inverse.get(nativeOp);if(op==null)throw new IllegalStateException("Unmapped950 opcode "+nativeOp);
            Object operand;
            if(op==0x511){int kind=b.get()&255;if(kind==0)operand=b.getInt();else if(kind==1)operand=b.getLong();
                else if(kind==2){StringBuilder s=new StringBuilder();byte c;while((c=b.get())!=0)s.append((char)(c&255));operand=s.toString();}
                else throw new IllegalStateException("Unexpected constant kind");
            }else operand=wide.contains(op)?b.getInt():b.get()&255;
            if(op==0x511&&operand instanceof Integer)constants.add((Integer)operand);
            if(print)System.out.println(offset+" op947="+Integer.toHexString(op)+" arg="+operand);count++;
        }
        if(b.position()!=end||count!=b.getInt(end))throw new IllegalStateException("Script boundary/count mismatch");
        if(print)System.out.println("SCRIPT"+script+" SHA256="+hash(raw));
        return constants;
    }

    private static List<Integer> ids(Index index, int shift) {
        if (index == null || index.getTable() == null) throw new IllegalStateException("Missing authoritative cache index");
        List<Integer> result = new ArrayList<>();
        for (int group = 0; group <= index.getLastArchiveId(); group++) {
            if (!index.archiveExists(group)) continue;
            for (int file : index.getTable().getArchives()[group].getValidFileIds()) {
                if (file < 0 || file >= (1 << shift)) throw new IllegalStateException("Unexpected grouped cache ID");
                if (index.getFile(group, file) == null) throw new IllegalStateException("Missing declared cache file");
                result.add((group << shift) | file);
            }
        }
        Collections.sort(result); return result;
    }

    private static String hash(byte[] raw) throws Exception {
        if (raw == null) throw new IllegalStateException("Missing cache evidence");
        StringBuilder out = new StringBuilder();
        for (byte b : MessageDigest.getInstance("SHA-256").digest(raw)) out.append(String.format("%02x", b & 255));
        return out.toString();
    }
}
