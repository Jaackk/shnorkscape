package com.rs.cache.loaders;

import com.rs.cache.Cache;
import com.rs.cache.modern.FlatCacheRepository;
import com.google.gson.GsonBuilder;
import java.io.BufferedWriter;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.*;

/** Standalone read-only950 cache name exporter. This is deliberately outside the engine build. */
public final class CacheNameDump950 {
    private static final class Row {
        final int id;
        String name, detail;
        Row(int id, String name, String detail) { this.id=id; this.name=name; this.detail=detail; }
    }
    private final FlatCacheRepository cache;
    private final Path root, output, engine;
    private final String generated = Instant.now().toString();
    private final Map<String,Object> report = new LinkedHashMap<>();
    private final List<Map<String,Object>> errors = new ArrayList<>();
    private final Map<Integer,ItemDefinitions> items = new TreeMap<>();
    private final Map<Integer,String> resolvedNames = new HashMap<>();
    private int failures;
    private CacheNameDump950(Path root, Path output, Path engine) throws Exception {
        this.root=root.toAbsolutePath().normalize(); this.output=output.toAbsolutePath().normalize();
        this.engine=engine.toAbsolutePath().normalize();
        cache=new FlatCacheRepository(this.root);
        Cache.initFlatReadOnly(this.root);
        Files.createDirectories(this.output);
        report.put("generatedUtc", generated);
        report.put("cachePath", this.root.toString());
        report.put("engineJarPath", this.engine.toString());
        report.put("engineJarSha256", sha(Files.readAllBytes(this.engine)));
        report.put("encoding", "UTF-8; tab-separated columns; ascending numeric IDs");
        report.put("method", "Strict950 definitions from the selected flat cache; reference-table membership, container CRC/version, complete record decoding and terminator verified. No910 names or server customizations.");
        report.put("itemNames", "Actual cache names, with note/lent/bound/shard template name references resolved from these same950 definitions. Variant details are a separate column.");
        report.put("unnamedDefinitions", "Retained as <unnamed>; a definition may be a placeholder, scenery or a conditional transform base. Names alone do not prove an ID is usable by every command.");
    }
    private static String sha(byte[] data) throws Exception { return hex(MessageDigest.getInstance("SHA-256").digest(data)); }
    private static String hex(byte[] data) { StringBuilder b=new StringBuilder(); for(byte v:data) b.append(String.format("%02x",v&255)); return b.toString(); }
    private static String clean(String text) {
        if(text==null || text.trim().isEmpty() || "null".equals(text)) return "<unnamed>";
        return text.replace("\t","\\t").replace("\r","\\r").replace("\n","\\n");
    }
    private void error(String kind, int id, String stage, Exception exception) {
        Map<String,Object> e=new LinkedHashMap<>(); e.put("kind",kind); e.put("id",id); e.put("stage",stage);
        StringBuilder message=new StringBuilder(); Throwable cause=exception;
        while(cause!=null) { if(message.length()>0) message.append(" -> "); message.append(cause.getClass().getSimpleName()).append(": ").append(cause.getMessage()); cause=cause.getCause(); }
        e.put("reason",message.toString()); errors.add(e); failures++;
    }
    private String itemName(int id, Set<Integer> chain) {
        if(resolvedNames.containsKey(id)) return resolvedNames.get(id);
        ItemDefinitions d=items.get(id);
        if(d==null) throw new IllegalArgumentException("Missing or undecodable template source item "+id);
        if(!chain.add(id)) throw new IllegalArgumentException("Cyclic item template at "+id);
        String name=d.getName();
        // Match client/template precedence, without invoking legacy custom item initialization.
        if(d.certTemplateId!=-1) { requireItem(d.certTemplateId); name=itemName(d.certId,chain); }
        if(d.lendTemplateId!=-1) { requireItem(d.lendTemplateId); name=itemName(d.lendId,chain); }
        if(d.bindTemplateId!=-1) { requireItem(d.bindTemplateId); name=itemName(d.bindId,chain); }
        if(d.shardTemplateId!=-1) { requireItem(d.shardTemplateId); name=requireItem(d.shardId).shardName; }
        chain.remove(id); resolvedNames.put(id,name); return name;
    }
    private ItemDefinitions requireItem(int id) {
        ItemDefinitions item=items.get(id);
        if(item==null) throw new IllegalArgumentException("Missing or undecodable template item "+id);
        return item;
    }
    private static String variant(ItemDefinitions d) {
        List<String> details=new ArrayList<>();
        if(d.certTemplateId!=-1) details.add("noted; base="+d.certId);
        if(d.lendTemplateId!=-1) details.add("lent; base="+d.lendId);
        if(d.bindTemplateId!=-1) details.add("bound; base="+d.bindId);
        if(d.shardTemplateId!=-1) details.add("shard; base="+d.shardId);
        return String.join("; ",details);
    }
    private void dump(String kind, int indexId, int shift) throws Exception {
        FlatCacheRepository.Index index=cache.getIndexes().get(indexId);
        if(index==null) throw new IllegalArgumentException("Missing cache index "+indexId);
        TreeMap<Integer,Row> rows=new TreeMap<>();
        MessageDigest digest=MessageDigest.getInstance("SHA-256");
        int failureBefore=failures;
        for(FlatCacheRepository.Group group:index.getGroups().values()) {
            Map<Integer,byte[]> files;
            try { files=cache.readGroup(indexId,group.id); }
            catch(Exception e) {
                for(int file:group.getFileIds()) { int id=(group.id<<shift)|file; rows.put(id,new Row(id,"<decode-error>","see export-report.json")); error(kind,id,"container",e); }
                continue;
            }
            for(int file:group.getFileIds()) {
                int id=(group.id<<shift)|file; byte[] raw=files.get(file);
                if(file<0 || file>=(1<<shift)) throw new IllegalArgumentException("Definition file exceeds grouping width: "+indexId+":"+group.id+":"+file);
                digest.update(ByteBuffer.allocate(8).putInt(id).putInt(raw.length).array()); digest.update(raw);
                try {
                    Row row;
                    if(indexId==19) { ItemDefinitions d=ItemDefinitions.decodeStrict947(id,raw,null); items.put(id,d); row=new Row(id,d.getName(),variant(d)); }
                    else if(indexId==18) { NPCDefinitions d=NPCDefinitions.decodeStrict947(id,raw,null); row=new Row(id,d.name,""); }
                    else { ObjectDefinitions d=ObjectDefinitions.getObjectDefinitions(id); row=new Row(id,d.name,""); }
                    rows.put(id,row);
                } catch(Exception e) { rows.put(id,new Row(id,"<decode-error>","see export-report.json")); error(kind,id,"definition",e); }
            }
        }
        if(indexId==19) for(Row row:rows.values()) if(items.containsKey(row.id)) {
            try { row.name=itemName(row.id,new HashSet<Integer>()); }
            catch(Exception e) { row.name="<name-resolution-error>"; row.detail="see export-report.json"; error(kind,row.id,"template-name",e); }
        }
        int unnamed=0, named=0;
        for(Row row:rows.values()) { row.name=clean(row.name); if("<unnamed>".equals(row.name)) unnamed++; else if(!row.name.startsWith("<decode-error>") && !row.name.startsWith("<name-resolution-error>")) named++; }
        String referenceSha=sha(Files.readAllBytes(root.resolve("255").resolve(indexId+".dat")));
        try(BufferedWriter writer=Files.newBufferedWriter(output.resolve(kind+".txt"),StandardCharsets.UTF_8)) {
            writer.write("# RuneScape950 cache "+kind+" ID/name list\n");
            writer.write("# Generated UTC: "+generated+"\n# Source cache: "+root+"\n");
            writer.write("# Index "+indexId+"; definitions="+rows.size()+"; named="+named+"; unnamed="+unnamed+"; errors="+(failures-failureBefore)+"\n");
            writer.write("# Reference table SHA-256: "+referenceSha+"\n");
            writer.write("# All actual reference-defined IDs are included. <unnamed> means no display name; errors are reported, never guessed.\n");
            writer.write("# Names come from this950 cache. Items resolve cache template names; conditional NPC/object transforms depend on game state.\n");
            writer.write("# UTF-8, tab-separated; literal tabs/newlines inside names are escaped.\n");
            writer.write("ID\tName\tDetails\n");
            for(Row row:rows.values()) { writer.write(Integer.toString(row.id)); writer.write('\t'); writer.write(row.name); writer.write('\t'); writer.write(row.detail); writer.write('\n'); }
        }
        Map<String,Object> entry=new LinkedHashMap<>();
        entry.put("index",indexId); entry.put("referenceVersion",index.version); entry.put("referenceSha256",referenceSha);
        entry.put("definitionDigestSha256",hex(digest.digest())); entry.put("definitionDigestFormat","Repeated numeric ID BE int32, byte length BE int32, raw definition bytes, sorted by ID");
        entry.put("groups",index.getGroups().size()); entry.put("definitions",rows.size()); entry.put("named",named); entry.put("unnamed",unnamed); entry.put("errors",failures-failureBefore);
        entry.put("lowestId",rows.firstKey()); entry.put("highestId",rows.lastKey()); entry.put("outputSha256",sha(Files.readAllBytes(output.resolve(kind+".txt"))));
        report.put(kind,entry);
        System.out.println(kind+": "+rows.size()+" definitions ("+named+" named, "+unnamed+" unnamed, "+(failures-failureBefore)+" errors)");
    }
    private void run() throws Exception {
        dump("items",19,8); dump("npcs",18,7); dump("objects",16,8);
        report.put("errors",errors);
        Files.write(output.resolve("export-report.json"),(new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create().toJson(report)+"\n").getBytes(StandardCharsets.UTF_8));
        String readme="These lists were exported directly from the paired950 cache.\n\n"+
            "items.txt, npcs.txt and objects.txt contain ID, name and optional variant details, separated by tabs. Open them in any text editor and search by name. Every definition is included in numeric ID order, including unnamed entries.\n\n"+
            "Item notes and other template variants inherit their names from the matching cache definition. Their variant/base ID is shown in Details. NPC and object base names do not resolve quest/variable-dependent transforms. A listed ID is a cache entry, not a promise that all of its gameplay has been ported.\n\n"+
            "<unnamed> means the cache has no display name. Decode/name-resolution failures, if any, are explicit and detailed in export-report.json. That report also contains counts and SHA-256 fingerprints.\n\n"+
            "To regenerate from the project folder:\n  .\\Export-950CacheNames.ps1\n\n"+
            "The exporter reads the cache without writing to it, uses the packaged950 definition decoders, and does not start or modify the game server.\n";
        Files.write(output.resolve("README.txt"),readme.getBytes(StandardCharsets.UTF_8));
        if(failures>0) throw new IllegalStateException("Export completed with "+failures+" reported failures; see export-report.json");
    }
    public static void main(String[] args) throws Exception {
        if(args.length!=3) throw new IllegalArgumentException("Usage: CacheNameDump950 <950-flat-cache> <output-directory> <engine-jar>");
        new CacheNameDump950(Paths.get(args[0]),Paths.get(args[1]),Paths.get(args[2])).run();
    }
}
