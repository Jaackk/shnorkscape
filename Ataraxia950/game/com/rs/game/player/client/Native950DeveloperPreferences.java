package com.rs.game.player.client;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

/** Small, bounded sidecar; never changes the player's bank, presets or workspace save. */
final class Native950DeveloperPreferences {
    private static Path file(String account){
        String canonical=Native950Save.canonicalUsername(account);
        if(!canonical.matches("[a-z0-9 _-]{1,32}"))throw new IllegalArgumentException("Invalid developer account");
        return Paths.get(System.getProperty("ataraxia950.devPreferences","server-home/developer-preferences")).resolve(canonical+".txt");
    }
    static boolean valid(String id){
        if(Native950DeveloperActions.find(id)!=null)return true;
        // Persist typed references without requiring a live cache at account load.
        // The page catalogue resolves existence; stale references cannot execute actions.
        return id.matches("(npc|object|item):[0-9]{1,6}");
    }
    static Set<String> load(String account)throws IOException{
        Path file=file(account);Set<String> result=new LinkedHashSet<>();
        if(!Files.exists(file)) {result.addAll(Arrays.asList("heal","almighty","items","dummy","wars","bugtest"));return result;}
        if(!Files.isRegularFile(file,LinkOption.NOFOLLOW_LINKS)||Files.size(file)>8192)throw new IOException("Invalid developer preferences");
        List<String> lines=Files.readAllLines(file,StandardCharsets.UTF_8);
        if(lines.isEmpty()||!lines.get(0).equals("SHNORKSCAPE-DEV-1"))throw new IOException("Unknown preferences version");
        for(String id:lines.subList(1,lines.size()))if(valid(id)&&result.size()<128)result.add(id);
        return result;
    }
    static void save(String account,Set<String> favourites)throws IOException{
        if(favourites.size()>128)throw new IOException("Too many favourites");
        StringBuilder data=new StringBuilder("SHNORKSCAPE-DEV-1\n");
        for(String id:favourites){if(!valid(id))throw new IOException("Unknown favourite");data.append(id).append('\n');}
        Path file=file(account);Files.createDirectories(file.getParent());Path tmp=Files.createTempFile(file.getParent(),".dev-",".tmp");
        try{Files.write(tmp,data.toString().getBytes(StandardCharsets.UTF_8));Files.move(tmp,file,StandardCopyOption.REPLACE_EXISTING,StandardCopyOption.ATOMIC_MOVE);}finally{Files.deleteIfExists(tmp);}
    }
}
