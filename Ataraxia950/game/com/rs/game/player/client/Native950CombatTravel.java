package com.rs.game.player.client;
import com.rs.game.*;
import com.rs.game.player.Player;
import java.util.*;

/** SHNORKSCAPE development-world routes, not retail Reaper assignments.
 * Destinations are real, verified map objects; arrival shares ordinary stairs'
 * free-floor, reciprocal reach, stale-source and movement-state validation. */
final class Native950CombatTravel {
    static final class Destination {
        final String label,symbol;final int x,y,plane;
        Destination(String label,String symbol,int x,int y,int plane){this.label=label;this.symbol=symbol;this.x=x;this.y=y;this.plane=plane;}
        WorldObject object(){
            int id=Native950Symbols.require("object",symbol);WorldTile tile=new WorldTile(x,y,plane);
            World.getRegion(tile.getRegionId(),true);return World.getObjectWithId(tile,id);
        }
    }
    static final List<Destination> DESTINATIONS=Collections.unmodifiableList(Arrays.asList(
        new Destination("General Graardor - inside the boss room","shnorkscape_bandos_altar_26289",2869,5372,0),
        new Destination("Dagannoth Kings - lair entrance","shnorkscape_ladder_10229",2899,4446,0),
        new Destination("Slayer Tower - ground floor","shnorkscape_stairs_82481",3432,3557,0),
        new Destination("Taverley Dungeon - entrance","shnorkscape_stairs_74864",2884,9792,0)));
    static boolean handles(WorldObject object,int option){
        return object!=null&&option==1&&object.getId()==114761&&object.getRegionId()==Native950WarsRetreat.REGION&&object.getPlane()==0;
    }
    static List<Native950ProductionMenu.Choice> choices(Player player,WorldObject source){
        if(!handles(source,1)||Native950Symbols.require("object","shnorkscape_reaper_portal_114761")!=source.getId())return Collections.emptyList();
        List<Native950ProductionMenu.Choice> choices=new ArrayList<>();
        for(Destination d:DESTINATIONS)choices.add(new Native950ProductionMenu.Choice(d.label,()->{
            if(!handles(source,1))return;
            try{Native950WorldTraversal.arrive(player,source,d.object());}
            catch(IllegalStateException changed){player.sendMessage("This route's cache binding is unavailable.");}
        }));
        return choices;
    }
}
