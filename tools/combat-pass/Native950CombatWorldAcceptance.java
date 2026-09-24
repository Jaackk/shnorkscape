package com.rs.game.player.client;
import com.rs.cache.Cache;
import com.rs.game.*;
import com.rs.game.player.Player;
import com.rs.game.route.RouteFinder;
import com.rs.game.route.strategy.ObjectStrategy;
import com.rs.utils.data.parsers.npcs.*;
import io.netty.channel.embedded.EmbeddedChannel;
import java.nio.file.*;
import java.util.*;
/** Real950 map/collision probe in an isolated JVM. No socket, save or live server. */
public final class Native950CombatWorldAcceptance {
 static void check(boolean ok,String why){if(!ok)throw new AssertionError(why);}
 public static void main(String[] args)throws Exception{
  System.setProperty(Native950World.SPAWNS_PROPERTY,"false");System.setProperty(Native950World.LEGACY_SPAWNS_PROPERTY,"false");
  Cache.initFlatReadOnly(Paths.get("cache"));Native950World world=Native950World.getInstance();
  world.execute(()->{
   NPCCombatDefinitionsDataParser.init();NPCStatsDataParser.init();NPCDropsDataParser.init();
   int symbols=0;for(Native950GamevalLookup.Entry e:Native950GamevalLookup.search("")){
    check(e.verify().equals("950 payload SHA-256 MATCH"),"Symbol pin: "+e.name);symbols++;
   }
   for(int id:new int[]{2881,2882,2883,6260})check(Native950NpcCombatCatalog.fromRunningCache(id)!=null,"Boss refused "+id);
   int signatureDrops=0;
   for(int[] row:new int[][]{{2881,6733},{2881,6739},{2882,6731},{2882,6739},{2883,6735},{2883,6737},{2883,6739},{6260,11724},{6260,11726},{6260,11728},{6260,11704}}){
    check(Native950NpcDrops.metadata(row[1])!=null,"Signature item rejected "+row[1]);
    boolean present=false;for(com.rs.utils.data.parsers.npcs.pojos.NPCDrop drop:NPCDropsDataParser.getDrops(row[0]))if(drop.getItemId()==row[1])present=true;
    check(present,"Signature drop missing for boss "+row[0]+": "+row[1]);signatureDrops++;
   }
   int pairs=0,uses=0;EmbeddedChannel c=new EmbeddedChannel();
   Player p=Player.createNative950("travel-probe",new WorldTile(3432,3556,0),c);p.setActive(true);World.addNative950Player(p,1);
   try{
    List<WorldObject> objects=new ArrayList<>(World.getRegion(13623,true).getAllObjects());
    for(WorldObject source:objects){
     if(!Native950WorldTraversal.handles(source,1)||Native950WorldTraversal.door(source,1))continue;
     for(int option=1;option<=5;option++){
      String op=Native950WorldTraversal.option(source,option);
      int delta=op.equalsIgnoreCase("Climb-up")||op.equalsIgnoreCase("Climb up")?1:op.equalsIgnoreCase("Climb-down")||op.equalsIgnoreCase("Climb down")?-1:0;
      if(delta==0||!Native950WorldTraversal.handles(source,option))continue;
      WorldObject destination=Native950WorldTraversal.counterpart(source,source.getPlane()+delta);
      check(destination!=null,"Missing reciprocal stairs: "+source.getId()+" "+source+" "+op);pairs++;
      WorldTile start=null;
      for(int dx=-9;dx<=9&&start==null;dx++)for(int dy=-9;dy<=9;dy++){
       WorldTile tile=new WorldTile(source.getX()+dx,source.getY()+dy,source.getPlane());
       if(World.isFloorFree(tile.getPlane(),tile.getX(),tile.getY())&&RouteFinder.findRoute(RouteFinder.WALK_ROUTEFINDER,tile.getX(),tile.getY(),tile.getPlane(),1,new ObjectStrategy(source),false)==0){start=tile;break;}
      }
      check(start!=null,"Unreachable stair source "+source.getId());p.setNextWorldTile(null);p.setLocation(start);World.updateEntityRegion(p);p.loadMapRegions();p.setClientHasLoadedMapRegion();
      Native950WorldTraversal.use(p,source,option);
      check(p.getNextWorldTile()!=null&&p.getNextWorldTile().getPlane()==destination.getPlane(),"Stair movement failed "+source.getId()+" "+source.getX()+","+source.getY()+","+source.getPlane()+" -> "+destination.getX()+","+destination.getY()+","+destination.getPlane()+" size="+destination.getDefinitions().sizeX+","+destination.getDefinitions().sizeY+" "+op);uses++;
     }
    }
    check(pairs>=20,"Unexpectedly few tower links: "+pairs);
    WorldObject exit=World.getRegion(Native950WarsRetreat.REGION,true).getAllObjects().stream().filter(o->o.getId()==114746).findFirst().get();
    check(Native950WarsRetreat.handles(exit,3),"Death office option refused");p.setNextWorldTile(null);p.setLocation(Native950WarsRetreat.WARS_RETREAT);Native950WarsRetreat.use(p,exit,3);
    check(Native950WarsRetreat.DEATHS_OFFICE.equals(p.getNextWorldTile()),"Death office destination");
    WorldObject portal=World.getRegion(Native950WarsRetreat.REGION,true).getAllObjects().stream().filter(o->o.getId()==114761).findFirst().get();
    WorldTile portalStart=null;
    for(int dx=-8;dx<=8&&portalStart==null;dx++)for(int dy=-8;dy<=8;dy++){
     WorldTile tile=new WorldTile(portal.getX()+dx,portal.getY()+dy,0);
     if(World.isFloorFree(0,tile.getX(),tile.getY())&&RouteFinder.findRoute(RouteFinder.WALK_ROUTEFINDER,tile.getX(),tile.getY(),0,1,new ObjectStrategy(portal),false)==0){portalStart=tile;break;}
    }
    check(portalStart!=null,"Portal unreachable");
    WorldObject altar=Native950CombatTravel.DESTINATIONS.get(0).object();
    check(Native950WarsRetreat.handles(altar,3),"Bandos altar exit unavailable");
    p.setNextWorldTile(null);p.setLocation(new WorldTile(2868,5371,0));Native950WarsRetreat.use(p,altar,3);
    check(Native950WarsRetreat.WARS_RETREAT.equals(p.getNextWorldTile()),"Bandos altar sandbox return failed");
    for(Native950ProductionMenu.Choice route:Native950CombatTravel.choices(p,portal)){
     p.setNextWorldTile(null);p.setLocation(portalStart);World.updateEntityRegion(p);p.loadMapRegions();p.setClientHasLoadedMapRegion();
     route.start(1);check(p.getNextWorldTile()!=null&&!p.getNextWorldTile().equals(portalStart),"Travel failed: "+route.label);
     p.setLocation(new WorldTile(3200,3200,0));p.setNextWorldTile(null);route.start(1);check(p.getNextWorldTile()==null,"Stale route callback moved player");
    }
    System.out.println("PASS: "+symbols+" symbol pins; four boss profiles; "+pairs+" reciprocal stair links; "+uses+" real collision-checked stair movements; War exit option3; four combat portal routes and stale callback protection; "+signatureDrops+" signature loot rows");
   }finally{c.finishAndReleaseAll();}return null;
  }).get(120,java.util.concurrent.TimeUnit.SECONDS);
 }
}

