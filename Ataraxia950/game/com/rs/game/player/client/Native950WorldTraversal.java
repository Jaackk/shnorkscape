package com.rs.game.player.client;

import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.player.Player;
import com.rs.game.route.RouteFinder;
import com.rs.game.route.strategy.ObjectStrategy;
import com.rs.network.packet.impl.ObjectHandler;
import java.util.*;

/** Restored ordinary travel with actual950 map counterparts, never arbitrary Enter teleports. */
final class Native950WorldTraversal {
    private Native950WorldTraversal() { }
    static String option(WorldObject object,int option){
        if(object==null||option<1||option>5)return "";
        String[] options=object.getDefinitions().options;
        return options==null||option>options.length||options[option-1]==null?"":options[option-1];
    }
    private static boolean ordinaryStairs(WorldObject o){
        // Initial rollout: Lumbridge castle/towers and south-west Varrock. Each
        // use additionally requires a reciprocal map object on the destination
        // plane. Special dungeon/quest stairs need explicit destinations instead.
        if(o==null||(o.getRegionId()!=12850&&o.getRegionId()!=12596))return false;
        String name=o.getDefinitions().name;
        return o.getDefinitions().transforms==null&&("Ladder".equalsIgnoreCase(name)
                ||"Staircase".equalsIgnoreCase(name)||"Stairs".equalsIgnoreCase(name));
    }
    static boolean door(WorldObject o,int option){
        if(o==null||o.getType()!=0||!"Open".equalsIgnoreCase(option(o,option)))return false;
        // Exact950 ordinary door definitions witnessed in the starting-city map.
        // Restricted, locked and quest doors do not enter this general handler.
        int id=o.getId();return id==12348||id==36844||id==36846||id==45476;
    }
    static boolean cellar(WorldObject o){
        return o!=null&&o.getPlane()==0&&o.getX()==3209
                &&((o.getId()==36687&&o.getY()==3216)||(o.getId()==29355&&o.getY()==9616));
    }
    static boolean handles(WorldObject o,int option){
        if(door(o,option))return true;
        String op=option(o,option);
        if(cellar(o))return option==1;
        return ordinaryStairs(o)&&(up(op)||down(op)||"Climb".equalsIgnoreCase(op)
                ||"Climb top floor".equalsIgnoreCase(op)||"Climb bottom floor".equalsIgnoreCase(op));
    }
    private static boolean up(String op){return "Climb-up".equalsIgnoreCase(op)||"Climb up".equalsIgnoreCase(op);}
    private static boolean down(String op){return "Climb-down".equalsIgnoreCase(op)||"Climb down".equalsIgnoreCase(op);}
    static List<Native950ProductionMenu.Choice> choices(Player p,WorldObject o){
        List<Native950ProductionMenu.Choice> result=new ArrayList<>();
        for(int direction:new int[]{1,-1})if(counterpart(o,o.getPlane()+direction)!=null){
            final int delta=direction;
            result.add(new Native950ProductionMenu.Choice(direction>0?"Climb up":"Climb down",()->travel(p,o,o.getPlane()+delta)));
        }
        return result;
    }
    static void use(Player p,WorldObject o,int option){
        if(door(o,option)){
            if(!ObjectHandler.handleDoor(p,o))p.sendMessage("That door is already open.");
            return;
        }
        if(cellar(o)){
            WorldTile target=new WorldTile(o.getX(),o.getId()==36687?9616:3216,0);
            World.getRegion(target.getRegionId(),true);
            WorldObject other=World.getObjectWithId(target,o.getId()==36687?29355:36687);
            arrive(p,o,other);return;
        }
        String op=option(o,option);
        int plane=o.getPlane()+(up(op)?1:-1);
        if("Climb top floor".equalsIgnoreCase(op))plane=2;
        if("Climb bottom floor".equalsIgnoreCase(op))plane=0;
        travel(p,o,plane);
    }
    private static void travel(Player p,WorldObject source,int plane){arrive(p,source,counterpart(source,plane));}
    static WorldObject counterpart(WorldObject source,int plane){
        if(!ordinaryStairs(source)||plane<0||plane>3||plane==source.getPlane())return null;
        com.rs.game.Region region=World.getRegion(source.getRegionId(),true);
        List<WorldObject> objects=region.getAllObjects();if(objects==null)return null;
        WorldObject best=null;int distance=5;
        for(WorldObject o:objects){
            if(o.getPlane()!=plane||!ordinaryStairs(o)||!o.getDefinitions().name.equalsIgnoreCase(source.getDefinitions().name))continue;
            int d=Math.abs(o.getX()-source.getX())+Math.abs(o.getY()-source.getY());
            boolean reciprocal=false;
            for(int option=1;option<=5;option++){
                String op=option(o,option);
                reciprocal|="Climb".equalsIgnoreCase(op)||(plane>source.getPlane()?down(op):up(op));
            }
            if(reciprocal&&d<distance){best=o;distance=d;}
        }
        return best;
    }
    private static void arrive(Player p,WorldObject source,WorldObject target){
        if(p.isDead()||p.isLocked()||p.getNextWorldTile()!=null||p.isNative950ForceMovementActive()
                ||p.getPlane()!=source.getPlane()||!Native950Woodcutting.current(source)
                ||RouteFinder.findRoute(RouteFinder.WALK_ROUTEFINDER,p.getX(),p.getY(),p.getPlane(),p.getSize(),new ObjectStrategy(source),false)!=0)return;
        if(target!=null&&Native950Woodcutting.current(target)){
            for(int radius=0;radius<=3;radius++)for(int dx=-radius;dx<=radius;dx++)for(int dy=-radius;dy<=radius;dy++){
                if(Math.max(Math.abs(dx),Math.abs(dy))!=radius)continue;
                WorldTile tile=new WorldTile(target.getX()+dx,target.getY()+dy,target.getPlane());
                // A wall edge does not occupy its tile. Player landings need a
                // free floor plus reciprocal reach, not the NPC zero-mask rule.
                if(!Native950DiagnosticSpawns.fits(tile,1,1)||!World.isFloorFree(tile.getPlane(),tile.getX(),tile.getY()))continue;
                if(RouteFinder.findRoute(RouteFinder.WALK_ROUTEFINDER,tile.getX(),tile.getY(),tile.getPlane(),1,new ObjectStrategy(target),false)!=0)continue;
                if(p.getNative950Combat()!=null)p.getNative950Combat().stop(p);
                p.getActionManager().forceStop();p.resetWalkSteps();p.setRouteEvent(null);p.setNextWorldTile(tile);return;
            }
        }
        p.sendMessage("This passage's destination is unavailable or blocked.");
    }
}
