package com.rs.game.player.client;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** Immutable cache-derived index; queries never decode the cache on the game thread. */
final class Native950DeveloperCatalogue {
    static final class Entry {
        final String kind,name;final int id,width,height,level;final int[] types;
        Entry(String[] row){kind=row[0];id=Integer.parseInt(row[1]);name=row[2];width=Integer.parseInt(row[3]);height=Integer.parseInt(row[4]);level=Integer.parseInt(row[5]);
            types=row[6].isEmpty()?new int[0]:Arrays.stream(row[6].split(",")).mapToInt(Integer::parseInt).toArray();}
        String details(){return kind+" ID: "+id+"<br>Footprint: "+width+" x "+height+(kind.equals("NPC")?"<br>Cache combat level: "+Math.max(0,level):"<br>Native types: "+Arrays.toString(types));}
    }
    private static final class Index {static final List<Entry> ALL=load();}
    static List<Entry> search(String kind,String query){
        String q=query==null?"":query.trim().toLowerCase(Locale.ROOT);Integer id=null;try{id=Integer.valueOf(q);}catch(NumberFormatException ignored){}
        List<Entry> result=new ArrayList<>();for(Entry e:Index.ALL)if(e.kind.equals(kind)&&(id!=null?e.id==id:e.name.toLowerCase(Locale.ROOT).contains(q)))result.add(e);
        result.sort(Comparator.comparing((Entry e)->!e.name.equalsIgnoreCase(q)).thenComparing(e->e.name,String.CASE_INSENSITIVE_ORDER).thenComparingInt(e->e.id));
        return result;
    }
    private static List<Entry> load(){
        List<Entry> result=new ArrayList<>();
        try(InputStream in=Native950DeveloperCatalogue.class.getResourceAsStream("/native950/developer-catalogue-950.tsv")){
            if(in==null)throw new IllegalStateException("Missing developer cache index");
            try(BufferedReader reader=new BufferedReader(new InputStreamReader(in,StandardCharsets.UTF_8))){String line;while((line=reader.readLine())!=null)if(!line.startsWith("#"))result.add(new Entry(line.split("\t",-1)));}
        }catch(IOException e){throw new IllegalStateException("Cannot load developer catalogue",e);}return Collections.unmodifiableList(result);
    }
}
