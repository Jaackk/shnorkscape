package com.rs.game.player.client;

import com.rs.cache.Cache;
import com.rs.game.*;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import com.rs.game.route.RouteFinder;
import com.rs.game.route.strategy.EntityStrategy;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.*;

/** The current cache, target identity and movement boundary shared by these two ordinary skills. */
final class Native950ThievingHunterAssets {
    private static final Properties PINS=load();
    private static Object store;
    private static final Map<String,Boolean> VERIFIED=new HashMap<>();
    static String binding(String key){return PINS.getProperty(key);}
    static synchronized boolean verified(String kind,int id){
        if(Cache.STORE==null||!Cache.isFlatReadOnly()||id<1)return false;
        if(store!=Cache.STORE){store=Cache.STORE;VERIFIED.clear();}
        String key=kind+"."+id;Boolean result=VERIFIED.get(key);if(result!=null)return result;
        boolean valid=false;String expected=binding(key);
        if(expected!=null)try{
            int index=kind.equals("item")?19:kind.equals("npc")?18:kind.equals("object")?16:20;
            int shift=index==19||index==16?8:7;
            byte[] raw=Cache.STORE.getIndexes()[index].getFile(id>>>shift,id&((1<<shift)-1));
            if(raw!=null){StringBuilder hash=new StringBuilder();for(byte b:MessageDigest.getInstance("SHA-256").digest(raw))hash.append(String.format("%02x",b&255));valid=expected.equals(hash.toString());}
        }catch(Exception unavailable){valid=false;}
        VERIFIED.put(key,valid);return valid;
    }
    static boolean verifyAll(){for(String kind:new String[]{"item","npc","object","sequence"})for(int id:ids(kind))if(!verified(kind,id))return false;return true;}
    static boolean item(int id){return verified("item",id)&&Native950CacheItems.entry(id)!=null;}
    /** Source loot is admitted only when the established910-to950 identity audit agrees. */
    static boolean legacyLoot(int id){return verified("item",id)&&Native950NpcDrops.metadata(id)!=null;}
    static boolean ready(Player p){return p!=null&&p.isNative950()&&p.isActive()&&!p.hasFinished()&&!p.isDead()&&!p.isLocked()
            &&!p.closeInterfaceLocked&&!p.isNative950ForceMovementActive()&&p.getNextForceMovement()==null
            &&p.getNextWorldTile()==null&&!p.hasTeleported()&&!p.hasWalkSteps()&&p.getNextWalkDirection()==-1;}
    static boolean ready(Player p,WorldTile origin,Object controller){return ready(p)&&origin!=null&&p.matches(origin)
            &&p.getControlerManager().getControler()==controller;}
    static boolean npcCurrent(NPC npc,int id){return npc!=null&&npc.isNative950()&&npc.getId()==id&&!npc.hasFinished()&&!npc.isDead()
            &&npc.getIndex()>0&&World.getNPCs().get(npc.getIndex())==npc&&!npc.isNative950CombatEngaged();}
    static boolean npcReach(Player p,NPC npc){
        if(p.getPlane()!=npc.getPlane()||p.hasWalkSteps())return false;
        int size=npc.getSize(),x=p.getX(),y=p.getY();
        if(x<npc.getX()-1||x>npc.getX()+size||y<npc.getY()-1||y>npc.getY()+size
                ||(x>=npc.getX()&&x<npc.getX()+size&&y>=npc.getY()&&y<npc.getY()+size))return false;
        return RouteFinder.findRoute(RouteFinder.WALK_ROUTEFINDER,x,y,p.getPlane(),p.getSize(),new EntityStrategy(npc),false)==0
                &&!RouteFinder.lastIsAlternative();
    }
    static Set<Integer> ids(String prefix){Set<Integer> ids=new TreeSet<>();for(String key:PINS.stringPropertyNames())
        if(key.startsWith(prefix+"."))ids.add(Integer.parseInt(key.substring(prefix.length()+1)));return Collections.unmodifiableSet(ids);}
    private static Properties load(){Properties p=new Properties();try(InputStream in=Native950ThievingHunterAssets.class.getResourceAsStream("/native950/thieving-hunter-assets-950.properties")){
        if(in==null)throw new IllegalStateException("Missing950 thieving/hunter asset pins");p.load(in);
        if(!"1".equals(p.getProperty("format"))||!"950".equals(p.getProperty("revision")))throw new IllegalStateException("Unexpected thieving/hunter asset format");return p;
    }catch(java.io.IOException e){throw new IllegalStateException(e);}}
}
