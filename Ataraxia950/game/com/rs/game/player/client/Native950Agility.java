package com.rs.game.player.client;

import com.rs.cache.Cache;
import com.rs.game.*;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.Action;
import com.rs.game.player.content.agility.GnomeAgility;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.*;

/** Original Gnome course stages/XP, bound to the actual950 course scenery and movement frames. */
public final class Native950Agility {
    private Native950Agility() { }
    private static final Properties PINS=load();
    private static Object store;
    private static boolean verified;
    public static synchronized void verifyCacheBindings() {
        if(Cache.STORE==store&&verified)return;
        if(Cache.STORE==null||!Cache.isFlatReadOnly())throw new IllegalStateException("Agility requires the paired950 cache");
        for(String key:PINS.stringPropertyNames()) {
            String[] parts=key.split("\\.");int id=Integer.parseInt(parts[1]);
            int index=parts[0].equals("bas")?2:parts[0].equals("object")?16:20;
            int shift=index==16?8:7;
            byte[] raw=Cache.STORE.getIndexes()[index].getFile(index==2?32:id>>>shift,index==2?id:id&((1<<shift)-1));
            try {
                if(raw==null)throw new IllegalStateException("Missing "+key);
                StringBuilder hash=new StringBuilder();for(byte value:MessageDigest.getInstance("SHA-256").digest(raw))hash.append(String.format("%02x",value&255));
                if(!PINS.getProperty(key).equals(hash.toString()))throw new IllegalStateException("Changed950 Agility asset "+key);
            }catch(java.security.NoSuchAlgorithmException failure){throw new IllegalStateException(failure);}
        }
        store=Cache.STORE;verified=true;
    }
    /** Only these verified render overrides can enter the native appearance body. */
    public static boolean renderOverride(int id) {
        if(id!=155&&id!=157&&id!=295)return false;
        verifyCacheBindings();return true;
    }
    private static Properties load(){Properties p=new Properties();try(InputStream in=Native950Agility.class.getResourceAsStream("/native950/agility-assets-950.properties")){
        if(in==null)throw new IllegalStateException("Missing950 Agility pins");p.load(in);return p;
    }catch(java.io.IOException e){throw new IllegalStateException(e);}}

    public static boolean handles(WorldObject o,int option){return option==1&&(step(o)!=-1||Native950AgilityCourses.handles(o));}
    static int step(WorldObject o) {
        if(o==null||Cache.STORE==null||!Cache.isFlatReadOnly())return -1;
        int x=o.getX(),y=o.getY(),p=o.getPlane(),id=o.getId();
        int stage=-1;
        if(id==69526&&x==2474&&y==3435&&p==0)stage=0;
        else if(id==69383&&(x==2471||x==2473||x==2475)&&y==3425&&p==0)stage=1;
        else if(id==69508&&x==2473&&y==3421&&p==1)stage=2;
        else if((id==2312&&x==2478||id==4059&&x>=2479&&x<=2482)&&y==3420&&p==2)stage=3;
        else if(id==69507&&x==2487&&y==3420&&p==2)stage=4;
        else if(id==69384&&(x==2483||x==2485||x==2487)&&y==3426&&p==0)stage=5;
        else if((id==69378&&x==2483||id==69377&&x==2487)&&(y==3431||y==3435)&&p==0)stage=6;
        if(stage<0)return -1;
        int type=stage==0||stage==3?22:10;
        int rotation=stage==3?1:stage==5||stage==6?2:0;
        if(o.getType()!=type||o.getRotation()!=rotation)return -1;
        verifyCacheBindings();
        String[] options=o.getDefinitions().options;
        String[] names={"Walk-across","Climb-over","Climb","Walk-on","Climb-down","Climb-over","Squeeze-through"};
        return options!=null&&options.length>0&&names[stage].equalsIgnoreCase(options[0])?stage:-1;
    }
    /** Exact entrance tiles prevent animations at an arbitrary side of a large scenery footprint. */
    public static WorldTile approach(Player p,WorldObject o) {
        switch(step(o)) {
            case 0:return new WorldTile(2474,3436,0);
            case 1:return new WorldTile(Math.max(o.getX(),Math.min(o.getX()+1,p.getX())),3426,0);
            case 2:return new WorldTile(2473,3423,1);
            case 3:return new WorldTile(p.getX()>2480?2483:2477,3420,2);
            case 4:return new WorldTile(2486,3420,2);
            case 5:return new WorldTile(Math.max(o.getX(),Math.min(o.getX()+1,p.getX())),3425,0);
            case 6:return new WorldTile(o.getX(),o.getY()==3431?3430:3437,0);
            default:return Native950AgilityCourses.approach(p,o);
        }
    }
    static WorldTile destination(int step,WorldTile from) {
        switch(step){
            case 0:return new WorldTile(2474,3429,0);
            case 1:return new WorldTile(from.getX(),3423,1);
            case 2:return new WorldTile(2473,3420,2);
            case 3:return new WorldTile(from.getX()==2477?2483:2477,3420,2);
            case 4:return new WorldTile(2487,3421,0);
            case 5:return new WorldTile(from.getX(),3428,0);
            case 6:return new WorldTile(from.getX(),from.getY()==3430?3437:3430,0);
            default:throw new IllegalArgumentException("Not a course obstacle");
        }
    }
    public static boolean start(Player p,WorldObject o,WorldTile entry) {
        if(step(o)<0)return Native950AgilityCourses.start(p,o,entry);
        if(p==null||!p.isNative950()||entry==null||step(o)<0||!entry.matches(p)||!entry.matches(approach(p,o))||!Native950Mining.current(o))return false;
        return p.getActionManager().setAction(new CourseAction(o,entry,step(o)));
    }
    /** Ordered lap state remains the original910 GnomeCourse temporary attribute. */
    static double complete(Player p,int step,WorldTile from) {
        int previous=GnomeAgility.getGnomeStage(p),level=p.getSkills().getLevel(Skills.AGILITY);
        if(step==6){
            boolean lap=previous==5&&from.getY()==3430;
            GnomeAgility.removeGnomeStage(p);
            if(lap){p.addLapsRan();return 90;}
            return 12;
        }
        if(step==3&&from.getX()!=2477){GnomeAgility.removeGnomeStage(p);return 12+level;}
        if(step==0)GnomeAgility.setGnomeStage(p,0);
        else if(previous==step-1)GnomeAgility.setGnomeStage(p,step);
        else GnomeAgility.removeGnomeStage(p);
        return (step==2||step==4?12:17)+level;
    }
    private static final class CourseAction extends Action {
        final WorldObject object;final WorldTile entry,end;final int step;final boolean linear;
        boolean running,started,ownTeleport,done;int ticks;Object controller;
        CourseAction(WorldObject o,WorldTile entry,int step){this.object=o;this.entry=new WorldTile(entry);this.step=step;this.end=destination(step,entry);linear=step==0||step==3||step==6;}
        public boolean start(Player p){
            if(p.isLocked()||p.isDead()||!p.isActive()||p.hasFinished()||p.hasWalkSteps()||p.getNextWalkDirection()!=-1||p.hasTeleported()||p.getNextWorldTile()!=null||p.isNative950ForceMovementActive()||p.getNextForceMovement()!=null)return false;
            running=p.getRun();controller=p.getControlerManager().getControler();started=true;p.lock();p.setRunHidden(false);p.setNextFaceWorldTile(end);setActionDelay(p,0);
            if(linear){p.getAppearence().setRenderEmote(step==6?295:155);p.addWalkSteps(end.getX(),end.getY(),16,false);}
            else p.setNextAnimation(new Animation(828));
            return true;
        }
        public boolean process(Player p){
            if(!p.isActive()||p.hasFinished()||p.isDead()||ticks>30||!Native950Mining.current(object)||p.isNative950ForceMovementActive()||p.getNextForceMovement()!=null||controller!=p.getControlerManager().getControler())return false;
            if(p.getNextWorldTile()!=null&&(!ownTeleport||!end.matches(p.getNextWorldTile())))return false;
            return p.getPlane()==entry.getPlane()||ownTeleport&&p.getPlane()==end.getPlane();
        }
        public int processWithDelay(Player p){
            ticks++;
            if(!linear&&ticks==2){ownTeleport=true;p.setNextWorldTile(end);return 0;}
            if(p.hasWalkSteps()||p.getNextWalkDirection()!=-1||p.hasTeleported()||p.getNextWorldTile()!=null)return 0;
            if(!end.matches(p))return ticks>3&&!linear?-1:0;
            done=true;p.getSkills().addXp(Skills.AGILITY,complete(p,step,entry));return -1;
        }
        private boolean ownedPosition(Player p){
            if(!linear)return entry.matches(p)||end.matches(p);
            return p.getPlane()==entry.getPlane()&&p.getX()>=Math.min(entry.getX(),end.getX())&&p.getX()<=Math.max(entry.getX(),end.getX())
                    &&p.getY()>=Math.min(entry.getY(),end.getY())&&p.getY()<=Math.max(entry.getY(),end.getY());
        }
        public void stop(Player p){
            if(!started)return;
            WorldTile pending=p.getNextWorldTile();
            if(!done&&p.isActive()&&!p.hasFinished()&&!p.isDead()&&controller==p.getControlerManager().getControler()
                    &&ownedPosition(p)&&(pending==null||ownTeleport&&end.matches(pending)))p.setNextWorldTile(entry);
            p.resetWalkSteps();if(linear)p.getAppearence().setRenderEmote(-1);p.setRunHidden(running);p.setNextAnimation(new Animation(-1));p.unlock();
            if(!done)GnomeAgility.removeGnomeStage(p);
        }
    }
}
