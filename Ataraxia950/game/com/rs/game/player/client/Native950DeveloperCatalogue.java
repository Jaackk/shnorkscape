package com.rs.game.player.client;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** Immutable cache-derived index; queries never decode the cache on the game thread. */
final class Native950DeveloperCatalogue {
    static final class Entry {
        final String kind,name,lowerName;final int id,width,height,level;final int[] types;
        Entry(String[] row){kind=row[0];id=Integer.parseInt(row[1]);name=row[2];lowerName=name.toLowerCase(Locale.ROOT);width=Integer.parseInt(row[3]);height=Integer.parseInt(row[4]);level=Integer.parseInt(row[5]);
            types=row[6].isEmpty()?new int[0]:Arrays.stream(row[6].split(",")).mapToInt(Integer::parseInt).toArray();}
        String details(){return kind+" ID: "+id+"<br>Footprint: "+width+" x "+height+(kind.equals("NPC")?"<br>Cache combat level: "+Math.max(0,level):"<br>Native types: "+Arrays.toString(types));}
    }
    private static final class Index {static final List<Entry> ALL=load();}
    private static final class Ranks {
        static final Map<Integer,Integer> VALUES=loadRanks();
        private static Map<Integer,Integer> loadRanks(){
            Map<Integer,Integer> result=new HashMap<>();
            try(InputStream in=Native950DeveloperCatalogue.class.getResourceAsStream("/native950/developer-npc-ranks-950.tsv")){
                if(in==null)throw new IllegalStateException("Missing NPC ranks");
                BufferedReader reader=new BufferedReader(new InputStreamReader(in,StandardCharsets.UTF_8));String line;
                while((line=reader.readLine())!=null)if(!line.startsWith("#")){String[] p=line.split("\t");result.put(Integer.parseInt(p[0]),Integer.parseInt(p[1]));}
            }catch(IOException e){throw new IllegalStateException(e);}return result;
        }
    }
    static List<Entry> groupedNpcs(String query){
        List<Entry> raw=search("NPC",query);
        if(Native950DeveloperSearch.exactId(query)>=0)return raw;
        List<Entry> result=group(raw,Ranks.VALUES);
        // Exact display-name searches still outrank partial-name families.
        result.sort(Comparator.comparing((Entry e)->!e.name.equalsIgnoreCase(query.trim())));
        return result;
    }
    static List<Entry> group(List<Entry> raw,Map<Integer,Integer> ranks){
        Map<String,Entry> groups=new LinkedHashMap<>();
        for(Entry e:raw){Entry previous=groups.get(e.lowerName);
            if(previous==null||ranks.getOrDefault(e.id,0)>ranks.getOrDefault(previous.id,0))groups.put(e.lowerName,e);
        }
        List<Entry> result=new ArrayList<>(groups.values());
        result.sort(Comparator.comparingInt((Entry e)->ranks.getOrDefault(e.id,0)).reversed()
                .thenComparing(e->e.name,String.CASE_INSENSITIVE_ORDER).thenComparingInt(e->e.id));
        return result;
    }
    static List<Entry> variants(Entry selected){
        List<Entry> result=new ArrayList<>();for(Entry e:search("NPC",selected.name))if(e.lowerName.equals(selected.lowerName))result.add(e);return result;
    }
    private static final Map<String,List<Entry>> RESULTS=new LinkedHashMap<String,List<Entry>>(32,0.75f,true){
        protected boolean removeEldestEntry(Map.Entry<String,List<Entry>> e){return size()>32;}
    };
    static synchronized List<Entry> search(String kind,String query){
        String q=query==null?"":query.trim().toLowerCase(Locale.ROOT);Integer id=null;try{id=Integer.valueOf(q);}catch(NumberFormatException ignored){}
        String key=kind+":"+q;List<Entry> cached=RESULTS.get(key);if(cached!=null)return cached;
        Set<Integer> symbols=new HashSet<>();
        if(!q.isEmpty()&&id==null)for(Native950GamevalLookup.Entry symbol:Native950GamevalLookup.search(q))
            if(symbol.type.equals(kind.toLowerCase(Locale.ROOT)))symbols.add(Integer.parseInt(symbol.id));
        List<Entry> result=new ArrayList<>();for(Entry e:Index.ALL)if(e.kind.equals(kind)&&(id!=null?e.id==id:e.lowerName.contains(q)||symbols.contains(e.id)))result.add(e);
        result.sort(Comparator.comparing((Entry e)->!e.name.equalsIgnoreCase(q)).thenComparing(e->e.name,String.CASE_INSENSITIVE_ORDER).thenComparingInt(e->e.id));
        List<Entry> immutable=Collections.unmodifiableList(result);RESULTS.put(key,immutable);return immutable;
    }
    private static List<Entry> load(){
        List<Entry> result=new ArrayList<>();
        try(InputStream in=Native950DeveloperCatalogue.class.getResourceAsStream("/native950/developer-catalogue-950.tsv")){
            if(in==null)throw new IllegalStateException("Missing developer cache index");
            try(BufferedReader reader=new BufferedReader(new InputStreamReader(in,StandardCharsets.UTF_8))){String line;while((line=reader.readLine())!=null)if(!line.startsWith("#"))result.add(new Entry(line.split("\t",-1)));}
        }catch(IOException e){throw new IllegalStateException("Cannot load developer catalogue",e);}return Collections.unmodifiableList(result);
    }
}
