package com.rs.game.player.client;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

/**
 * Sparse, shared, developer-only preview framing corrections: {@code subject=zoom}.
 * Presentation metadata only; never changes an NPC definition, the cache, gameplay
 * scale or any account save. Subjects are typed ({@code npc:<id>}) so other preview
 * kinds can be admitted later without a format change.
 */
final class Native950DeveloperPreviewOverrides {
    private static final String VERSION="SHNORKSCAPE-DEV-PREVIEW-1";
    private static final int MAX_ENTRIES=4096,MAX_BYTES=65536;
    private static Map<String,Integer> cache;
    /** Set when an existing file cannot be trusted; saving then refuses rather than overwrite it. */
    private static String unreadable;

    private static Path file(){
        return Paths.get(System.getProperty("ataraxia950.devPreviewOverrides","server-home/developer-preview-overrides.txt"));
    }
    static boolean validSubject(String subject){return subject!=null&&subject.matches("npc:[0-9]{1,6}");}
    static boolean validZoom(int zoom){return zoom>=Native950DeveloperPreview.MIN_ZOOM&&zoom<=Native950DeveloperPreview.MAX_ZOOM;}

    private static synchronized Map<String,Integer> load(){
        if(cache!=null)return cache;
        Map<String,Integer> result=new LinkedHashMap<>();unreadable=null;
        Path file=file();
        try{
            if(Files.exists(file,LinkOption.NOFOLLOW_LINKS)){
                if(!Files.isRegularFile(file,LinkOption.NOFOLLOW_LINKS)||Files.size(file)>MAX_BYTES)throw new IOException("not a small regular file");
                List<String> lines=Files.readAllLines(file,StandardCharsets.UTF_8);
                if(lines.isEmpty()||!lines.get(0).equals(VERSION))throw new IOException("unknown format");
                for(String line:lines.subList(1,lines.size())){
                    int eq=line.indexOf('=');if(eq<1)continue;
                    String subject=line.substring(0,eq);if(!validSubject(subject))continue;
                    try{int zoom=Integer.parseInt(line.substring(eq+1).trim());if(validZoom(zoom)&&result.size()<MAX_ENTRIES)result.put(subject,zoom);}
                    catch(NumberFormatException skipped){/* One bad line never discards the others. */}
                }
            }
        }catch(IOException|RuntimeException invalid){result.clear();unreadable=invalid.getMessage();}
        return cache=result;
    }

    /** Saved default zoom for this subject, or null when the automatic framing applies. */
    static Integer get(String subject){return load().get(subject);}

    static synchronized void save(String subject,int zoom){
        if(!validSubject(subject))throw new IllegalArgumentException("Unsupported preview subject");
        if(!validZoom(zoom))throw new IllegalArgumentException("Zoom out of range");
        Map<String,Integer> current=new LinkedHashMap<>(load());
        if(unreadable!=null)throw new IllegalStateException("Preview overrides file is unreadable ("+unreadable+"); not overwritten");
        current.put(subject,zoom);
        if(current.size()>MAX_ENTRIES)throw new IllegalStateException("Too many saved preview overrides");
        StringBuilder data=new StringBuilder(VERSION).append('\n');
        for(Map.Entry<String,Integer> e:current.entrySet())data.append(e.getKey()).append('=').append(e.getValue()).append('\n');
        Path file=file().toAbsolutePath();
        try{
            Files.createDirectories(file.getParent());
            Path tmp=Files.createTempFile(file.getParent(),".dev-preview-",".tmp");
            try{
                Files.write(tmp,data.toString().getBytes(StandardCharsets.UTF_8));
                Files.move(tmp,file,StandardCopyOption.REPLACE_EXISTING,StandardCopyOption.ATOMIC_MOVE);
            }finally{Files.deleteIfExists(tmp);}
        }catch(IOException e){throw new IllegalStateException("Could not write preview overrides",e);}
        cache=current;
    }

    /** Forget the in-memory copy so the next read re-parses the file. */
    static synchronized void reload(){cache=null;unreadable=null;}
}
