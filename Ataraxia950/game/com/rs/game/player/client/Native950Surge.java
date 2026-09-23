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
        return use(p,false);
    }
    static String use(Player p,boolean backwards){
        String name=backwards?"Escape":"Surge";
        if(!p.isActive()||p.hasFinished()||p.isDead()||p.isLocked()||p.isStunned()||p.isFrozen()
                ||p.getNextWorldTile()!=null||p.isNative950ForceMovementActive()||p.getNextForceMovement()!=null)
            return "You cannot "+name+" during that action.";
        if(p.getSkills().getLevel(Skills.AGILITY)<5)return name+" requires Agility level 5.";
        // Instanced/controller movement permission has not been ported. Do not bypass it.
        if(p.getControlerManager().getControler()!=null)return name+" is not available in this controlled activity yet.";
        byte[] direction=Utils.getDirection(p.getDirection()).clone();
        if(backwards){direction[0]=(byte)-direction[0];direction[1]=(byte)-direction[1];}
        WorldTile start=new WorldTile(p),end=destination(start,direction[0],direction[1],(tile,dx,dy)->{
            WorldTile next=new WorldTile(tile.getX()+dx,tile.getY()+dy,tile.getPlane());
            return World.isRegionLoaded(next.getRegionId())&&World.canMoveNPC(next,p.getSize())
                &&World.checkWalkStep(tile.getPlane(),tile.getX(),tile.getY(),dx,dy,p.getSize());
        });
        if(start.matches(end))return "There is no clear path to "+name+".";
        p.getActionManager().forceStop();p.resetWalkSteps();p.setRouteEvent(null);p.setNextFaceEntity(null);
        // Timing uses the proven950 20ms interpolation adapter. No teleport or second scheduler.
        p.setNextForceMovement(new NewForceMovement(start,0,end,1,Utils.getAngle(end.getX()-start.getX(),end.getY()-start.getY())));
        //18358 is the876 Surge candidate, not a proven950 ability->sequence binding. Omit it.
        return null;
    }
    /** Walk the straight tile ray to the chosen point, never through a clipped diagonal. */
    static WorldTile targetedDestination(WorldTile from,WorldTile target,Steps steps){
        if(from.getPlane()!=target.getPlane())return new WorldTile(from);
        int dx=target.getX()-from.getX(),dy=target.getY()-from.getY();
        int distance=Math.max(Math.abs(dx),Math.abs(dy));
        if(distance==0||distance>10)return new WorldTile(from);
        WorldTile end=new WorldTile(from);
        for(int i=1;i<=distance;i++){
            int x=from.getX()+(int)Math.round((double)dx*i/distance),y=from.getY()+(int)Math.round((double)dy*i/distance);
            int stepX=x-end.getX(),stepY=y-end.getY();
            if(!steps.clear(end,stepX,stepY))break;
            end=new WorldTile(x,y,from.getPlane());
        }
        return end;
    }
    static String dive(Player player,WorldTile target){
        return dive(player,target,(tile,dx,dy)->{
            WorldTile next=new WorldTile(tile.getX()+dx,tile.getY()+dy,tile.getPlane());
            return World.isRegionLoaded(next.getRegionId())&&World.canMoveNPC(next,player.getSize())
                    &&World.checkWalkStep(tile.getPlane(),tile.getX(),tile.getY(),dx,dy,player.getSize());
        });
    }
    static String dive(Player p,WorldTile target,Steps steps){
        if(!p.isActive()||p.hasFinished()||p.isDead()||p.isLocked()||p.isStunned()||p.isFrozen()
                ||p.getNextWorldTile()!=null||p.isNative950ForceMovementActive()||p.getNextForceMovement()!=null)
            return "You cannot Dive during that action.";
        if(p.getControlerManager().getControler()!=null)return "Dive is not available in this controlled activity yet.";
        if(target==null||target.getPlane()!=p.getPlane()||Math.max(Math.abs(target.getX()-p.getX()),Math.abs(target.getY()-p.getY()))>10)
            return "Choose a tile within10 tiles on your current plane.";
        WorldTile start=new WorldTile(p),end=targetedDestination(start,target,steps);
        if(start.matches(end))return "There is no clear path to that tile.";
        p.getActionManager().forceStop();p.resetWalkSteps();p.setRouteEvent(null);p.setNextFaceEntity(null);
        p.setNextForceMovement(new NewForceMovement(start,0,end,1,Utils.getAngle(end.getX()-start.getX(),end.getY()-start.getY())));
        return null;
    }
    private Native950Surge(){}
}
