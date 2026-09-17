package com.rs.game.player.client;

import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;

/** Paired-cache hub interactions only. Boss portals and grimoire are not boss implementations. */
final class Native950WarsRetreat {
    static final int REGION=13214;
    private Native950WarsRetreat(){}
    static boolean bank(int id){return id==114750||id==83634;}
    static boolean handles(WorldObject object,int option){
        if(object==null)return false;
        if(object.getId()==114745)return option==1;
        if(object.getRegionId()!=REGION||object.getPlane()!=0)return false;
        int id=object.getId();
        return option==1&&(id==114748||id==114749||id==79034||id==114746)
                ||id==114746&&option==2;
    }
    static String teleport(Player p,boolean enter){
        if(p.isDead()||p.isLocked()||p.isNative950ForceMovementActive()||p.getNextWorldTile()!=null)return "Wait until your character can teleport.";
        WorldTile destination=enter?new WorldTile(3294,10129,0):new WorldTile(3107,3298,0);
        World.getRegion(destination.getRegionId(),true);
        if(!World.isRegionLoaded(destination.getRegionId())||!World.canMoveNPC(destination,1))return "The destination is unavailable or blocked.";
        if(p.getNative950Combat()!=null)p.getNative950Combat().stop(p);
        p.getActionManager().forceStop();p.resetWalkSteps();p.setRouteEvent(null);p.setNextForceMovement(null);p.setNextWorldTile(destination);
        return enter?"Welcome to War's Retreat. The bank, altar, crystals and dummy dispensers are available.":"Returning to Draynor.";
    }
    static void use(Player p,WorldObject object,int option){
        switch(object.getId()){
            case 114745:p.sendMessage(teleport(p,true));break;
            case 114746:p.sendMessage(teleport(p,false));break;
            case 114748:p.getPrayer().restorePrayer(Skills.getLevelCap(Skills.PRAYER)*10);p.sendMessage("Your prayer is restored.");break;
            case 114749:p.getCombatDefinitions().setSpecialAttackPercentage(100);p.sendMessage("Your adrenaline is restored.");break;
            case 79034:p.sendMessage(Native950DiagnosticSpawns.spawnTrainingDummy(p));break;
            default:break;
        }
    }
}
