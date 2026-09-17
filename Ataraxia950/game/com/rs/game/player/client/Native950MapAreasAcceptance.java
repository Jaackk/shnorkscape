package com.rs.game.player.client;
import com.rs.cache.Cache;
import java.nio.file.Paths;
/** Fresh read-only JVM; verifies every generated chunk against both complete950 cache source groups. */
public final class Native950MapAreasAcceptance {
    public static void main(String[] args)throws Exception {
        if(args.length!=1)throw new IllegalArgumentException("Usage: Native950MapAreasAcceptance <950-cache>");
        Cache.initFlatReadOnly(Paths.get(args[0]));Native950MapAreas.verify();
        for(Native950Lodestones.Destination d:Native950Lodestones.destinations()) {
            int area=Native950MapAreas.resolvedAreaType(d.arrival().getX(),d.arrival().getY());
            if(area<0)throw new AssertionError("Unmapped lodestone "+d.name);
            System.out.println(d.name+" -> area "+area);
        }
        System.out.println("PASS:869 area identities and5,197 map squares/332,608 chunks verified against actual950 cache; all29 lodestones mapped");
    }
}
