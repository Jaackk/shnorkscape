package com.rs.game.player.client;

import com.rs.cache.Cache;
import java.nio.file.*;
import java.util.*;

/** Offline exact950 inspection. Only the caller's output directory is written. */
public final class Native950CombatCacheProbe {
    public static void main(String[] args)throws Exception{
        if(args.length<4)throw new IllegalArgumentException("cache index archive output-directory");
        Cache.initFlatReadOnly(Paths.get(args[0]));
        int index=Integer.parseInt(args[1]),archive=Integer.parseInt(args[2]);
        Path out=Paths.get(args[3]);Files.createDirectories(out);
        for(int file:Cache.STORE.getIndexes()[index].getTable().getArchives()[archive].getValidFileIds()){
            byte[] raw=Cache.STORE.getIndexes()[index].getFile(archive,file);
            Files.write(out.resolve(index+"-"+archive+"-"+file+".bin"),raw);
        }
        System.out.println("Exported index="+index+" archive="+archive+" to "+out);
    }
}
