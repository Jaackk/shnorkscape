package com.rs.game.player.client;

import com.rs.cache.Cache;
import com.rs.cache.loaders.ItemDefinitions;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

/** Global safe item universe, independent of editorial tab membership. */
final class Native950DeveloperSearch {
    static final int LIMIT=100, SLOTS=1820;
    private static Object store;
    private static List<Row> cached;
    private static final Map<Integer,Row> byId=new HashMap<>();
    private static final Set<Integer> verified=new HashSet<>();
    static final class Row {
        final Native950EquipmentCatalogue.Entry item;
        final String hash,lower;
        Row(Native950EquipmentCatalogue.Entry item,String hash){this.item=item;this.hash=hash;lower=item.name.toLowerCase(Locale.ROOT);}
    }
    static synchronized List<Row> current(){
        if(Cache.STORE==null||!Cache.isFlatReadOnly())throw new IllegalStateException("Search requires950 cache");
        if(store!=Cache.STORE){
            List<Row> rows=new ArrayList<>();
            try(InputStream in=Native950DeveloperSearch.class.getResourceAsStream("/native950/developer-search-950.tsv")){
                if(in==null)throw new IllegalStateException("Missing global search index");
                BufferedReader reader=new BufferedReader(new InputStreamReader(in,StandardCharsets.UTF_8));String line;
                while((line=reader.readLine())!=null){
                    if(line.startsWith("#")||line.isEmpty())continue;
                    String[] p=line.split("\t",-1);if(p.length!=5)throw new IllegalStateException("Malformed global search row");
                    int id=Integer.parseInt(p[0]),stack=Integer.parseInt(p[1]),slot=Integer.parseInt(p[2]);
                    rows.add(new Row(new Native950EquipmentCatalogue.Entry(id,0,0,slot,stack==1?10000:slot<0?1000:28,stack,p[3],"search"),p[4]));
                }
            }catch(IOException e){throw new IllegalStateException(e);}
            cached=Collections.unmodifiableList(rows);store=Cache.STORE;byId.clear();verified.clear();
            for(Row row:rows)if(byId.put(row.item.id,row)!=null)throw new IllegalStateException("Duplicate global item ID");
        }
        return cached;
    }
    static synchronized boolean verifiedItem(int id){
        current();Row row=byId.get(id);if(row==null)return false;
        if(!verified.contains(id)){
            if(!Native950EquipmentCatalogue.matchesItemHash(id,row.hash,Native950EquipmentCatalogue.hash(Cache.STORE.getIndexes()[19].getFile(id>>>8,id&255))))
                throw new IllegalStateException("Changed developer item "+id);
            verified.add(id);
        }
        return true;
    }
    static List<Native950EquipmentCatalogue.Entry> find(List<Row> rows,String text){
        String query=text.trim().toLowerCase(Locale.ROOT);List<Native950EquipmentCatalogue.Entry> result=new ArrayList<>();
        if(query.isEmpty()||query.length()>80)return result;
        int exact=exactId(query);
        for(Row row:rows)if(exact>=0?row.item.id==exact:row.lower.contains(query)){
            if(!row.hash.isEmpty()&&!Native950EquipmentCatalogue.matchesItemHash(row.item.id,row.hash,Native950EquipmentCatalogue.hash(Cache.STORE.getIndexes()[19].getFile(row.item.id>>>8,row.item.id&255))))
                throw new IllegalStateException("Changed global-search item "+row.item.id);
            result.add(row.item);if(result.size()>LIMIT)break;
        }
        return result;
    }
    static int exactId(String query){
        String value=query.trim();if(value.startsWith("id:"))value=value.substring(3).trim();
        if(!value.matches("[0-9]{1,8}"))return -1;
        try{return Integer.parseInt(value);}catch(NumberFormatException invalid){return -1;}
    }
    static boolean safe(ItemDefinitions d){
        if(d==null||!d.loaded||d.decodeFailure!=null||d.getId()==48447||d.name==null||d.name.trim().isEmpty()
                ||d.name.equalsIgnoreCase("null")||d.name.startsWith("<")||d.stackable<0||d.stackable>2
                ||d.certTemplateId>=0||d.lendTemplateId>=0||d.bindTemplateId>=0||d.shardTemplateId>=0)return false;
        String n=d.name.toLowerCase(Locale.ROOT);
        if(n.contains("placeholder")||n.equals("null")||n.contains("[unused]")||n.contains("broken"))return false;
        return Native950CacheItems.entry(d.getId())!=null && (d.equipSlot<0||Native950EquipmentTypes.resolve(d.getId())!=null);
    }
    /** Offline only. No live all-cache scan; obscure, cosmetic and tool items remain searchable. */
    public static void main(String[] args)throws Exception{
        Cache.initFlatReadOnly(Paths.get(args[0]));List<Row> rows=new ArrayList<>();
        for(int a:Cache.STORE.getIndexes()[19].getTable().getValidArchiveIds())
            for(int f:Cache.STORE.getIndexes()[19].getTable().getArchives()[a].getValidFileIds()){
                ItemDefinitions d=Native950CacheItems.definition(a*256+f);if(!safe(d))continue;
                rows.add(new Row(new Native950EquipmentCatalogue.Entry(d.getId(),0,0,d.equipSlot,1,d.stackable,d.name,""),
                        Native950EquipmentCatalogue.hash(Cache.STORE.getIndexes()[19].getFile(a,f))));
            }
        rows.sort(Comparator.comparing((Row r)->r.lower).thenComparingInt(r->r.item.id));
        List<String> lines=new ArrayList<>();lines.add("# Global safe950 search universe; independent of curated tabs.");
        for(Row r:rows)lines.add(r.item.id+"\t"+r.item.stackMode+"\t"+r.item.slot+"\t"+r.item.name+"\t"+r.hash);
        Files.write(Paths.get(args[1]),lines,StandardCharsets.UTF_8,StandardOpenOption.CREATE_NEW);
        System.out.println("Generated "+rows.size()+" safe globally searchable item definitions");
    }
    /** Never reassign a source slot during one mounted library session, including between queries. */
    static final class Session {
        final List<Native950EquipmentCatalogue.Entry> slots=new ArrayList<>();
        final Map<Integer,Integer> byId=new HashMap<>();
        final Set<Integer> visible=new HashSet<>();
        boolean searching,limited,full;
        Session(Native950EquipmentCatalogue catalogue){
            slots.addAll(catalogue.entries);
            for(int i=0;i<slots.size();i++)byId.putIfAbsent(slots.get(i).id,i);
        }
        void query(List<Native950EquipmentCatalogue.Entry> matches){
            searching=true;visible.clear();limited=matches.size()>LIMIT;full=false;
            for(int i=0;i<Math.min(LIMIT,matches.size());i++){
                Native950EquipmentCatalogue.Entry e=matches.get(i);Integer slot=byId.get(e.id);
                if(slot==null){
                    if(slots.size()==SLOTS){full=true;continue;}
                    slot=slots.size();slots.add(e);byId.put(e.id,slot);
                }
                visible.add(slot);
            }
        }
        Native950EquipmentCatalogue.Entry claim(int slot,int curatedSize){
            if(slot<0||slot>=slots.size()||(searching?!visible.contains(slot):slot>=curatedSize))return null;
            return slots.get(slot);
        }
    }
}
