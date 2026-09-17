package com.rs.game.player.client;
import com.rs.game.*;
import com.rs.game.player.Player;
import java.util.*;
/** Six ordinary altar gateways; exact950 locations replace obsolete910 generic portal guesses. */
public final class Native950RunecraftingTravel {
 private Native950RunecraftingTravel(){}
 public enum Gateway {
  AIR(2452,3126,3404,11,3,2465,2841,4828,0,2841,4829,3128,3403,1438,5527),
  MIND(2453,2981,3513,11,2,2466,2793,4827,0,2792,4827,2983,3512,1448,5529),
  WATER(2454,3182,3157,11,0,2467,3495,4832,0,3482,4838,3181,3158,1444,5531),
  EARTH(2455,3305,3473,10,3,2468,2655,4829,2,2655,4830,3307,3476,1440,5535),
  FIRE(2456,3312,3254,11,1,2469,2576,4846,3,2574,4848,3311,3256,1442,5537),
  BODY(2457,3052,3444,11,1,2470,2521,4833,0,2522,4833,3051,3445,1446,5533);
  public final int talisman,tiara;private final WorldObject ruins,portal;private final WorldTile inside,outside;
  Gateway(int rid,int rx,int ry,int rt,int rr,int pid,int px,int py,int pr,int ix,int iy,int ox,int oy,int talisman,int tiara){
   ruins=new WorldObject(rid,rt,rr,rx,ry,0);portal=new WorldObject(pid,10,pr,px,py,0);inside=new WorldTile(ix,iy,0);outside=new WorldTile(ox,oy,0);this.talisman=talisman;this.tiara=tiara;
  }
  public WorldObject ruins(){return new WorldObject(ruins);}public WorldObject portal(){return new WorldObject(portal);}
  public WorldTile inside(){return new WorldTile(inside);}public WorldTile outside(){return new WorldTile(outside);}
 }
 private static boolean same(WorldObject a,WorldObject b){return a!=null&&a.getId()==b.getId()&&a.getType()==b.getType()&&a.getRotation()==b.getRotation()&&a.matches(b);}
 private static Gateway gateway(WorldObject object){if(object==null||!Native950RunecraftingDivinationAssets.verified("object",object.getId()))return null;for(Gateway g:Gateway.values())if(same(object,g.ruins)||same(object,g.portal)){
  com.rs.cache.loaders.ObjectDefinitions d=object.getDefinitions();return d.loaded&&d.transforms==null&&d.options!=null&&"Enter".equals(d.options[0])?g:null;
 }return null;}
 public static boolean handles(WorldObject object,int option){return option==1&&gateway(object)!=null;}
 public static boolean enter(Player p,WorldObject object){
  Gateway g=gateway(object);if(g==null||p==null||!p.isNative950()||!Native950Runecrafting.stationary(p)||p.getNextWalkDirection()!=-1||p.hasTeleported()||!Native950Mining.current(object)||!Native950Mining.inReach(p,object))return false;
  boolean inward=same(object,g.ruins);
  if(inward&&!p.getInventory().containsItem(g.talisman,1)&&p.getEquipment().getHatId()!=g.tiara&&p.getEquipment().getHatId()!=13655){p.sendMessage("Carry a matching talisman or wear the matching tiara to enter these ruins.");return false;}
  if(inward&&(Native950RunecraftingDivinationAssets.item(g.talisman)==null||Native950RunecraftingDivinationAssets.item(g.tiara)==null||Native950RunecraftingDivinationAssets.item(13655)==null))return false;
  WorldTile arrival=inward?g.inside():g.outside();World.getRegion(arrival.getRegionId(),true);
  if(Native950MapAreas.resolvedAreaType(arrival.getX(),arrival.getY())<0||!World.isFloorFree(arrival.getPlane(),arrival.getX(),arrival.getY())||!p.getControlerManager().processObjectTeleport(arrival))return false;
  p.getActionManager().forceStop();if(p.getNative950Combat()!=null)p.getNative950Combat().stop(p);p.resetWalkSteps();p.setRouteEvent(null);p.setNextWorldTile(arrival);p.sendMessage("A mysterious force carries you "+(inward?"into":"out of")+" the altar.");return true;
 }
 public static List<Native950ItemCatalog.Entry> itemEntries(){List<Native950ItemCatalog.Entry> entries=new ArrayList<>();for(Gateway g:Gateway.values())for(int id:new int[]{g.talisman,g.tiara}){Native950ItemCatalog.Entry item=Native950RunecraftingDivinationAssets.item(id);if(item!=null)entries.add(item);}Native950ItemCatalog.Entry omni=Native950RunecraftingDivinationAssets.item(13655);if(omni!=null)entries.add(omni);return entries;}
 public static void verifyCacheBindings(){for(Gateway g:Gateway.values())if(!handles(g.ruins,1)||!handles(g.portal,1))throw new IllegalStateException("Unverified950 altar gateway "+g);}
}
