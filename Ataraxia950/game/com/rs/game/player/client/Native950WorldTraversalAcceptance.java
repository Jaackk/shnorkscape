package com.rs.game.player.client;

import com.rs.cache.Cache;
import com.rs.game.*;
import com.rs.game.player.Player;
import com.rs.game.route.RouteFinder;
import com.rs.game.route.strategy.ObjectStrategy;
import io.netty.channel.embedded.EmbeddedChannel;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

/** Actual map objects/collision and both travel directions in an ephemeral world. */
public final class Native950WorldTraversalAcceptance {
    public static void main(String[] args)throws Exception{
        if(args.length!=1)throw new IllegalArgumentException("Usage: Native950WorldTraversalAcceptance <cache>");
        System.setProperty(Native950World.SPAWNS_PROPERTY,"false");
        System.setProperty(Native950World.LEGACY_SPAWNS_PROPERTY,"false");
        Cache.initFlatReadOnly(Paths.get(args[0]));
        Native950World.getInstance().execute(()->{
            EmbeddedChannel channel=new EmbeddedChannel();
            Player p=Player.createNative950("traversal-probe",new WorldTile(3217,3258,0),channel);
            p.setActive(true);World.addNative950Player(p,1);World.updateEntityRegion(p);p.loadMapRegions();p.setClientHasLoadedMapRegion();
            try{
                route(p,36768,3229,3213,0,1,1);
                route(p,36769,3229,3213,1,3,0);
                route(p,36773,3204,3207,0,1,1);
                route(p,36774,3204,3207,1,3,0);
                route(p,36687,3209,3216,0,1,0);
                check(p.getNextWorldTile().getY()>9600,"Cellar did not lead underground");
                route(p,29355,3209,9616,0,1,0);
                check(p.getNextWorldTile().getY()<3300,"Cellar return failed");
                WorldObject ordinary=object(45476,3219,3241,0);
                check(Native950WorldTraversal.door(ordinary,1),"Ordinary door missing");
                check(!Native950WorldTraversal.handles(new WorldObject(114748,10,0,3219,3241,0),1),"Arbitrary object gained traversal");
                check(Native950WorldTraversal.counterpart(object(36768,3229,3213,0),3)==null,"Missing counterpart guessed");
                System.out.println("PASS: actual950 ladder/stair ascent/descent, cellar round trip, ordinary door admission, unknown destination refusal; no saves or live connection");
            }finally{World.removeNative950Player(p);channel.finishAndReleaseAll();}
            return null;
        }).get(90,TimeUnit.SECONDS);
    }
    private static WorldObject object(int id,int x,int y,int plane){
        WorldTile tile=new WorldTile(x,y,plane);World.getRegion(tile.getRegionId(),true);
        WorldObject o=World.getObjectWithId(tile,id);check(o!=null,"Missing exact950 map object "+id);return o;
    }
    private static void route(Player p,int id,int x,int y,int plane,int option,int expected){
        p.setNextWorldTile(null);p.resetMasks();p.resetWalkSteps();
        WorldObject o=object(id,x,y,plane);boolean reached=false;
        for(int dx=-3;dx<=3&&!reached;dx++)for(int dy=-3;dy<=3&&!reached;dy++){
            WorldTile tile=new WorldTile(x+dx,y+dy,plane);
            if(!World.isFloorFree(plane,tile.getX(),tile.getY()))continue;
            if(RouteFinder.findRoute(RouteFinder.WALK_ROUTEFINDER,tile.getX(),tile.getY(),plane,1,new ObjectStrategy(o),false)==0){
                p.setLocation(tile.getX(),tile.getY(),plane);World.updateEntityRegion(p);reached=true;
            }
        }
        check(reached,"No approach to "+id);check(Native950WorldTraversal.handles(o,option),"Route not admitted "+id);
        Native950WorldTraversal.use(p,o,option);
        WorldTile arrival=p.getNextWorldTile();check(arrival!=null&&arrival.getPlane()==expected,"Wrong or missing arrival "+id);
        check(World.isFloorFree(arrival.getPlane(),arrival.getX(),arrival.getY()),"Arrival is clipped "+id);
        System.out.println("PASS route "+id+" -> "+arrival.getX()+","+arrival.getY()+","+arrival.getPlane());
    }
    private static void check(boolean value,String message){if(!value)throw new IllegalStateException(message);}
}
