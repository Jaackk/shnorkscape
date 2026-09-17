package com.rs.game.player.client;

import com.rs.game.*;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.utils.Utils;

/** Targetless movement, using the existing950 force-movement scheduler and collision. */
final class Native950Surge {
    interface Steps { boolean clear(WorldTile from,int dx,int dy); }
    static WorldTile destination(WorldTile from,int dx,int dy,Steps steps){
        WorldTile end=new WorldTile(from);
        if(Math.abs(dx)>1||Math.abs(dy)>1||dx==0&&dy==0)return end;
        for(int i=0;i<10&&steps.clear(end,dx,dy);i++)end=new WorldTile(end.getX()+dx,end.getY()+dy,end.getPlane());
        return end;
    }
    static String use(Player p){
        if(!p.isActive()||p.hasFinished()||p.isDead()||p.isLocked()||p.isStunned()
                ||p.getNextWorldTile()!=null||p.isNative950ForceMovementActive()||p.getNextForceMovement()!=null)
            return "You cannot Surge during that action.";
        if(p.getSkills().getLevel(Skills.AGILITY)<5)return "Surge requires Agility level 5.";
        // Instanced/controller movement permission has not been ported. Do not bypass it.
        if(p.getControlerManager().getControler()!=null)return "Surge is not available in this controlled activity yet.";
        byte[] direction=Utils.getDirection(p.getDirection());
        WorldTile start=new WorldTile(p),end=destination(start,direction[0],direction[1],(tile,dx,dy)->{
            WorldTile next=new WorldTile(tile.getX()+dx,tile.getY()+dy,tile.getPlane());
            return World.isRegionLoaded(next.getRegionId())&&World.canMoveNPC(next,p.getSize())
                &&World.checkWalkStep(tile.getPlane(),tile.getX(),tile.getY(),dx,dy,p.getSize());
        });
        if(start.matches(end))return "There is no clear path to Surge.";
        p.getActionManager().forceStop();p.resetWalkSteps();p.setRouteEvent(null);p.setNextFaceEntity(null);
        // Timing uses the proven950 20ms interpolation adapter. No teleport or second scheduler.
        p.setNextForceMovement(new NewForceMovement(start,0,end,1,Utils.getAngle(end.getX()-start.getX(),end.getY()-start.getY())));
        //18358 is the876 Surge candidate, not a proven950 ability->sequence binding. Omit it.
        return null;
    }
    private Native950Surge(){}
}
