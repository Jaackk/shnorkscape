package com.rs.tools.modern;

import com.rs.cache.Cache;
import com.rs.game.Region;
import com.rs.game.World;
import com.rs.game.route.RouteFinder;
import com.rs.game.route.strategy.FixedTileStrategy;
import java.nio.file.Paths;

/** Real-cache check; opens no accounts, network services, or legacy spawns. */
public final class ModernRegionProbe {
    public static void main(String[] args) throws Exception {
        if (args.length != 1) throw new IllegalArgumentException("Usage: ModernRegionProbe <flat cache directory>");
        Cache.initFlatReadOnly(Paths.get(args[0]));
        int failed = 0;
        for (int x=49; x<=51; x++) for (int y=49; y<=51; y++) {
            int id = x<<8|y;
            try {
                Region region = World.getRegion(id,true);
                int blocked=0;
                for (int tx=0;tx<64;tx++) for(int ty=0;ty<64;ty++) if(region.getMask(0,tx,ty)!=0) blocked++;
                System.out.println("REGION " + id + " objects=" + (region.getObjects()==null?0:region.getObjects().size()) + " maskedTiles="+blocked+" stage="+region.getLoadMapStage());
            } catch (RuntimeException failure) {
                failed++;
                failure.printStackTrace(System.out);
            }
        }
        if (failed>0) throw new IllegalStateException(failed+" modern regions failed strict decoding");
        int route = RouteFinder.findRoute(RouteFinder.WALK_ROUTEFINDER,3200,3200,0,1,new FixedTileStrategy(3201,3200),false);
        System.out.println("ROUTE 3200,3200 -> 3201,3200 result="+route);
        System.out.println("Modern region decoding complete; source cache remained read-only.");
    }
}
