package com.rs.game.player.client;

import com.rs.cache.Cache;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.*;

/** Read-only, provenance-labelled research index. Never resolves a symbol for execution. */
final class Native950GamevalLookup {
    static final class Entry {
        final String type,id,name,sha,evidence,referenceSha;final int index,group,file;
        Entry(String[] f){
            if(f.length!=9)throw new IllegalArgumentException("Invalid symbol row");
            type=f[0];id=f[1];name=f[2];index=Integer.parseInt(f[3]);group=Integer.parseInt(f[4]);file=Integer.parseInt(f[5]);sha=f[6];evidence=f[7];referenceSha=f[8];
            if(!Arrays.asList("interface","component","varp","varbit","npc","object","sequence","effect").contains(type)||!id.matches("[0-9]+(:[0-9]+)?")
                ||!name.matches("[a-z0-9_:]+")||!sha.matches("[a-f0-9]{64}")||!referenceSha.matches("[a-f0-9]{64}")||index<0||group<0||file< -1)throw new IllegalArgumentException("Invalid symbol identity");
        }
        String packed(){if(!type.equals("component"))return id;String[] p=id.split(":");return ""+((Integer.parseInt(p[0])<<16)|Integer.parseInt(p[1]));}
        String verify(){
            if(!Cache.isFlatReadOnly())return "UNVERIFIED: paired cache unavailable";
            try{
                if(!hashMatches(referenceSha,Cache.STORE.getIndex255().getArchiveData(index)))return "UNVERIFIED: cache reference changed";
                byte[] raw=file<0?Cache.STORE.getIndexes()[index].getMainFile().getArchiveData(group):Cache.STORE.getIndexes()[index].getFile(group,file);
                return matches(raw)?"950 payload SHA-256 MATCH":"UNVERIFIED: cache payload changed";
            }catch(RuntimeException invalid){return "UNVERIFIED: cache definition unavailable";}
        }
        boolean matches(byte[] raw){return hashMatches(sha,raw);}
        static boolean hashMatches(String expected,byte[] raw){
            if(raw==null)return false;
            try{StringBuilder s=new StringBuilder();for(byte b:MessageDigest.getInstance("SHA-256").digest(raw))s.append(String.format("%02x",b&255));return expected.equals(s.toString());}
            catch(NoSuchAlgorithmException impossible){throw new IllegalStateException(impossible);}
        }
    }
    private static final class Index {static final List<Entry> ALL=load();}
    static List<Entry> search(String query){
        String q=query==null?"":query.trim().toLowerCase(Locale.ROOT);if(q.length()>160)return Collections.emptyList();
        List<Entry> result=new ArrayList<>();boolean numeric=q.matches("[0-9]+(:[0-9]+)?");
        for(Entry e:Index.ALL){
            boolean match=numeric?(e.id.equals(q)||e.packed().equals(q)):
                Arrays.stream(q.split("\\s+")).allMatch(token->(e.type+" "+e.name+" "+e.id).contains(token));
            if(match)result.add(e);
        }
        return result;
    }
    static List<Entry> load(BufferedReader reader)throws IOException{
        List<Entry> result=new ArrayList<>();Set<String> keys=new HashSet<>(),names=new HashSet<>();String line;
        while((line=reader.readLine())!=null){if(line.startsWith("#")||line.isEmpty())continue;Entry e=new Entry(line.split("\t",-1));
            if(!keys.add(e.type+":"+e.id)||!names.add(e.type+":"+e.name))throw new IllegalArgumentException("Duplicate symbol");result.add(e);}
        return Collections.unmodifiableList(result);
    }
    private static List<Entry> load(){
        List<Entry> all=new ArrayList<>();
        for(String resource:new String[]{"gameval-lookup-950.tsv","combat-world-symbols-950.tsv","boss-symbols-950.tsv"}){
            try(InputStream in=Native950GamevalLookup.class.getResourceAsStream("/native950/"+resource)){
                if(in==null)throw new IllegalStateException("Missing symbol index: "+resource);
                try(BufferedReader r=new BufferedReader(new InputStreamReader(in,StandardCharsets.UTF_8))){all.addAll(load(r));}
            }catch(IOException e){throw new IllegalStateException("Cannot load symbol index",e);}
        }
        Set<String> keys=new HashSet<>(),names=new HashSet<>();
        for(Entry e:all)if(!keys.add(e.type+":"+e.id)||!names.add(e.type+":"+e.name))throw new IllegalStateException("Duplicate merged symbol");
        return Collections.unmodifiableList(all);
    }
}
