package com.rs.game.player.client;

import com.rs.game.*;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.Action;
import com.rs.game.player.content.agility.Agility;
import com.rs.game.player.content.agility.BarbarianOutpostAgility;
import com.rs.game.player.content.agility.WildernessCourseAgility;
import java.util.*;

/** Original910 basic course order/XP over exact950 scenery and collision-checked entrance tiles. */
final class Native950AgilityCourses {
    private Native950AgilityCourses() { }
    static final int BARBARIAN=1,WILDERNESS=2;
    static final int WALK=0,CLIMB=1,STONES=2;
    static final class Obstacle {
        final int id,x,y,plane,type,rotation,course,stage,level,movement,render,animation,duration;
        final String option;
        final WorldTile entry,end;
        Obstacle(int id,int x,int y,int plane,int type,int rotation,String option,int course,int stage,int level,
                 int ex,int ey,int ep,int dx,int dy,int dp,int movement,int render,int animation,int duration) {
            this.id=id;this.x=x;this.y=y;this.plane=plane;this.type=type;this.rotation=rotation;this.option=option;
            this.course=course;this.stage=stage;this.level=level;this.entry=new WorldTile(ex,ey,ep);this.end=new WorldTile(dx,dy,dp);
            this.movement=movement;this.render=render;this.animation=animation;this.duration=duration;
        }
        boolean matches(WorldObject o){return o!=null&&id==o.getId()&&x==o.getX()&&y==o.getY()&&plane==o.getPlane()&&type==o.getType()&&rotation==o.getRotation();}
    }
    private static Obstacle obstacle(int id,int x,int y,int p,int type,int rotation,String option,int course,int stage,int level,
            int ex,int ey,int ep,int dx,int dy,int dp,int movement,int render,int animation,int duration) {
        return new Obstacle(id,x,y,p,type,rotation,option,course,stage,level,ex,ey,ep,dx,dy,dp,movement,render,animation,duration);
    }
    static final List<Obstacle> OBSTACLES=Collections.unmodifiableList(Arrays.asList(
        obstacle(20210,2552,3559,0,10,1,"Squeeze through",BARBARIAN,-1,35,2552,3561,0,2552,3558,0,WALK,295,-1,0),
        obstacle(20210,2552,3559,0,10,1,"Squeeze through",BARBARIAN,-1,1,2552,3558,0,2552,3561,0,WALK,295,-1,0),
        obstacle(43526,2551,3550,0,10,0,"Swing-on",BARBARIAN,0,35,2551,3556,0,2551,3549,0,CLIMB,-1,751,3),
        obstacle(43526,2552,3550,0,10,0,"Swing-on",BARBARIAN,0,35,2552,3556,0,2552,3549,0,CLIMB,-1,751,3),
        obstacle(43595,2550,3546,0,22,3,"Walk-across",BARBARIAN,1,35,2551,3546,0,2541,3546,0,WALK,155,-1,0),
        obstacle(20211,2538,3545,0,10,3,"Climb over",BARBARIAN,2,35,2539,3546,0,2537,3546,1,CLIMB,-1,828,2),
        obstacle(2302,2535,3547,1,22,1,"Walk-across",BARBARIAN,3,35,2536,3547,1,2532,3547,1,WALK,157,753,0),
        obstacle(3205,2532,3545,1,10,2,"Climb down",BARBARIAN,-2,1,2532,3546,1,2532,3546,0,CLIMB,-1,828,2),
        obstacle(1747,2532,3545,0,10,2,"Climb up",BARBARIAN,-1,35,2532,3546,0,2532,3546,1,CLIMB,-1,828,2),
        obstacle(1948,2537,3553,0,10,2,"Climb-over",BARBARIAN,4,35,2536,3553,0,2538,3553,0,CLIMB,-1,4853,2),
        obstacle(1948,2542,3553,0,10,2,"Climb-over",BARBARIAN,5,35,2541,3553,0,2543,3553,0,CLIMB,-1,4853,2),
        obstacle(65365,2998,3916,0,10,1,"Open",WILDERNESS,-1,52,2998,3915,0,2998,3931,0,WALK,155,-1,0),
        obstacle(65367,2997,3930,0,10,1,"Open",WILDERNESS,-1,1,2998,3931,0,2998,3915,0,WALK,155,-1,0),
        obstacle(65362,3004,3938,0,10,3,"Squeeze-through",WILDERNESS,0,52,3004,3937,0,3004,3950,0,WALK,295,-1,0),
        obstacle(65362,3004,3948,0,10,1,"Squeeze-through",WILDERNESS,-1,1,3004,3950,0,3004,3937,0,WALK,295,-1,0),
        obstacle(64696,3005,3952,0,10,2,"Swing-on",WILDERNESS,1,52,3005,3951,0,3005,3958,0,CLIMB,-1,751,3),
        obstacle(64699,3001,3960,0,22,0,"Cross",WILDERNESS,2,52,3002,3960,0,2996,3960,0,STONES,-1,741,1),
        obstacle(64698,3001,3945,0,22,3,"Walk-across",WILDERNESS,3,52,3002,3945,0,2994,3945,0,WALK,155,-1,0),
        obstacle(65734,2993,3936,0,10,1,"Climb",WILDERNESS,4,52,2995,3939,0,2995,3935,0,CLIMB,-1,3378,4)
    ));
    static boolean handles(WorldObject o) {
        for(Obstacle row:OBSTACLES)if(row.matches(o)) {
            Native950Agility.verifyCacheBindings();String[] options=o.getDefinitions().options;
            return options!=null&&options.length>0&&row.option.equalsIgnoreCase(options[0]);
        }
        return false;
    }
    static Obstacle select(Player p,WorldObject o) {
        if(p==null||!handles(o))return null;
        Obstacle best=null;int distance=Integer.MAX_VALUE;
        for(Obstacle row:OBSTACLES)if(row.matches(o)&&row.entry.getPlane()==p.getPlane()) {
            int next=Math.abs(row.entry.getX()-p.getX())+Math.abs(row.entry.getY()-p.getY());
            if(next<distance){best=row;distance=next;}
        }
        return best;
    }
    static WorldTile approach(Player p,WorldObject o){Obstacle row=select(p,o);return row==null?null:new WorldTile(row.entry);}
    static boolean start(Player p,WorldObject o,WorldTile entry) {
        Obstacle row=select(p,o);
        if(row==null||!p.isNative950()||entry==null||!entry.matches(row.entry)||!entry.matches(p)||!Native950Mining.current(o))return false;
        if(!Agility.hasLevel(p,row.level))return false;
        // Failed old coordinates must never strand the player inside either pipe's scenery footprint.
        if(!World.isFloorFree(row.end.getPlane(),row.end.getX(),row.end.getY(),p.getSize()))return false;
        return p.getActionManager().setAction(new CourseAction(o,row));
    }
    static int stage(Player p,int course){return course==BARBARIAN?BarbarianOutpostAgility.getStage(p):WildernessCourseAgility.getStage(p);}
    static void clear(Player p,int course){if(course==BARBARIAN)BarbarianOutpostAgility.removeStage(p);else WildernessCourseAgility.removeStage(p);}
    static void stage(Player p,int course,int stage){if(course==BARBARIAN)BarbarianOutpostAgility.setStage(p,stage);else WildernessCourseAgility.setStage(p,stage);}
    /** Keep original stage attributes and XP values, but require both actual Barbarian walls before the bonus. */
    static double complete(Player p,int course,int step) {
        if(step==-2)return 0; // The platform's ordinary descent is part of the same lap.
        if(step<0){clear(p,course);return 0;}
        int last=course==BARBARIAN?5:4,previous=stage(p,course);
        double xp=course==BARBARIAN?80:(step==last?0:220);
        if(step==last) {
            clear(p,course);
            if(previous==last-1){xp+=course==BARBARIAN?300:2000;p.addLapsRan();p.sendMessage("You have completed the "+(course==BARBARIAN?"Barbarian Outpost":"Wilderness")+" Agility course.");}
        } else if(step==0)stage(p,course,0);
        else if(previous==step-1)stage(p,course,step);
        else clear(p,course);
        return course==WILDERNESS?WildernessCourseAgility.increasedExperience(p,xp):xp;
    }
    private static final class CourseAction extends Action {
        final WorldObject object;final Obstacle row;
        Object controller;boolean running,started,done,ownTeleport;int ticks,stoneX;
        CourseAction(WorldObject object,Obstacle row){this.object=object;this.row=row;stoneX=row.entry.getX();}
        public boolean start(Player p) {
            if(p.isLocked()||p.isDead()||!p.isActive()||p.hasFinished()||p.hasWalkSteps()||p.getNextWalkDirection()!=-1
                    ||p.hasTeleported()||p.getNextWorldTile()!=null||p.isNative950ForceMovementActive()||p.getNextForceMovement()!=null)return false;
            controller=p.getControlerManager().getControler();running=p.getRun();started=true;p.lock();p.setRunHidden(false);
            p.setNextFaceWorldTile(row.end);setActionDelay(p,0);
            if(row.render!=-1)p.getAppearence().setRenderEmote(row.render);
            if(row.animation!=-1)p.setNextAnimation(new Animation(row.animation));
            if(row.movement==WALK)p.addWalkSteps(row.end.getX(),row.end.getY(),32,false);
            return true;
        }
        public boolean process(Player p) {
            if(!p.isActive()||p.hasFinished()||p.isDead()||ticks>50||!Native950Mining.current(object)
                    ||controller!=p.getControlerManager().getControler()||p.isNative950ForceMovementActive()||p.getNextForceMovement()!=null)return false;
            if(p.getNextWorldTile()!=null&&(!ownTeleport||!expectedTile().matches(p.getNextWorldTile())))return false;
            if(p.hasTeleported()&&(!ownTeleport||!expectedTile().matches(p)))return false;
            if(p.getPlane()!=row.entry.getPlane()&&(!ownTeleport||p.getPlane()!=row.end.getPlane()))return false;
            return p.getSkills().getLevel(Skills.AGILITY)>=row.level;
        }
        private WorldTile expectedTile(){return row.movement==STONES?new WorldTile(stoneX,row.entry.getY(),row.entry.getPlane()):row.end;}
        public int processWithDelay(Player p) {
            ticks++;
            if(p.hasWalkSteps()||p.getNextWalkDirection()!=-1||p.hasTeleported()||p.getNextWorldTile()!=null)return 0;
            if(row.movement==CLIMB&&!ownTeleport&&ticks>=row.duration){ownTeleport=true;p.setNextWorldTile(row.end);return 0;}
            if(row.movement==STONES&&stoneX>row.end.getX()) {
                if(!expectedTile().matches(p))return -1;
                stoneX--;ownTeleport=true;p.setNextAnimation(new Animation(741));p.setNextWorldTile(expectedTile());return 0;
            }
            if(!row.end.matches(p))return 0;
            done=true;double xp=complete(p,row.course,row.stage);if(xp>0)p.getSkills().addXp(Skills.AGILITY,xp);return -1;
        }
        private boolean ownedPosition(Player p) {
            if(row.movement==CLIMB)return row.entry.matches(p)||row.end.matches(p);
            if(p.getPlane()!=row.entry.getPlane())return false;
            int minX=Math.min(row.entry.getX(),row.end.getX()),maxX=Math.max(row.entry.getX(),row.end.getX());
            int minY=Math.min(row.entry.getY(),row.end.getY()),maxY=Math.max(row.entry.getY(),row.end.getY());
            return p.getX()>=minX&&p.getX()<=maxX&&p.getY()>=minY&&p.getY()<=maxY;
        }
        public void stop(Player p) {
            if(!started)return;
            // Interrupting a crawl/balance must not strand the player inside blocked scenery.
            // An unrelated teleport or controller transition retains its own destination.
            WorldTile pending=p.getNextWorldTile();
            if(!done&&p.isActive()&&!p.hasFinished()&&!p.isDead()&&controller==p.getControlerManager().getControler()
                    &&ownedPosition(p)&&(pending==null||ownTeleport&&expectedTile().matches(pending)))
                p.setNextWorldTile(row.entry);
            p.resetWalkSteps();if(row.render!=-1)p.getAppearence().setRenderEmote(-1);
            p.setRunHidden(running);p.setNextAnimation(new Animation(-1));p.unlock();
            if(!done)clear(p,row.course);
        }
    }
}