package com.rs.game.player.client;

import com.rs.game.*;
import com.rs.game.npc.NPC;
import com.rs.game.player.*;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.Test;
import java.util.*;
import static org.junit.Assert.*;

public class Native950ConjuresTest {
    static final class Fixture implements AutoCloseable,Native950Conjures.Host {
        final EmbeddedChannel a=new EmbeddedChannel(),b=new EmbeddedChannel();
        final Player first=player(a,1),second=player(b,2);
        final NPC target=NPC.createNative950(12353,new WorldTile(3219,3258,0),1);
        final Set<NPC> actors=Collections.newSetFromMap(new IdentityHashMap<NPC,Boolean>());
        final Map<Player,Integer> damage=new IdentityHashMap<>();
        final Native950Conjures manager=new Native950Conjures(this);
        Player noConduit;int follows,spawns,failSpawn=-1;
        static Player player(EmbeddedChannel c,int id){Player p=Player.createNative950("conjure-test-"+id,new WorldTile(3218,3258,0),c);p.setIndex(id);p.setActive(true);p.setInfiniteCombatRunes(true);p.getSkills().set(Skills.NECROMANCY,120);return p;}
        public NPC spawn(Player p,Native950Conjures.Kind k){if(++spawns==failSpawn)throw new IllegalStateException("fixture capacity");NPC actor=NPC.createNative950(k.npc,new WorldTile(p),1);actor.setIndex(spawns);actors.add(actor);return actor;}
        public void remove(NPC n){actors.remove(n);}
        public void follow(NPC n,WorldTile t){follows++;n.setLocation(t);}
        public boolean valid(Player p){return Native950Conjures.ownerLifecycleValid(p);}
        public boolean conduit(Player p){return p!=noConduit;}
        public NPC target(Player p){return target;}
        public boolean reach(NPC a,NPC t,int range){return true;}
        public int strike(Player p,NPC t,int min,int max){damage.put(p,damage.getOrDefault(p,0)+min);return min;}
        public void area(Player p,WorldTile tile,int radius,int min,int max){strike(p,target,min,max);}
        public int abilityDamage(Player p){return 100;}
        public void close(){manager.clear();a.finishAndReleaseAll();b.finishAndReleaseAll();}
    }
    @Test public void fourConjuresSurviveForcedArrivalAndWalkingButNotActualTeleport()throws Exception{
        java.lang.reflect.Field field=com.rs.cores.CoresManager.class.getDeclaredField("native947Scheduler");field.setAccessible(true);
        Object previous=field.get(null);com.rs.cores.Native950TickScheduler wheel=new com.rs.cores.Native950TickScheduler();
        try(Fixture f=new Fixture()){
            field.set(null,wheel);f.manager.cast(f.first,33965,0);
            f.first.setNextForceMovement(new ForceMovement(new WorldTile(3220,3258,0),1,ForceMovement.EAST));
            f.manager.pulse(1);assertEquals(4,f.manager.count(f.first));
            f.first.resetMasks();wheel.tick();
            assertNotNull(f.first.getNextWorldTile());assertFalse(f.first.hasLifecycleTeleport());
            f.manager.pulse(2);assertEquals(4,f.manager.count(f.first));
            f.first.setLocation(f.first.getNextWorldTile());f.first.resetMasks();
            // Complete transport cleanup, then ordinary movement does not terminate ownership.
            f.first.setNextWorldTile(null);f.first.setLocation(new WorldTile(3221,3258,0));
            f.manager.pulse(3);assertEquals(4,f.manager.count(f.first));
            f.first.setNextWorldTile(new WorldTile(3200,3200,0));assertTrue(f.first.hasLifecycleTeleport());
            f.manager.pulse(4);assertEquals(0,f.manager.count(f.first));assertTrue(f.actors.isEmpty());
        }finally{field.set(null,previous);}
    }
    @Test public void armyHasFourDistinctActorsAndPublishesAndClearsNativeTransforms(){
        try(Fixture f=new Fixture()){
            assertNull(f.manager.refusal(f.first,33965));f.manager.cast(f.first,33965,0);
            assertEquals(4,f.manager.count(f.first));assertEquals(4,f.actors.size());
            for(Native950Conjures.Kind k:Native950Conjures.Kind.values())assertEquals(1,f.first.getVarsManager().getValue(k.activeVar));
            f.manager.pulse(7);assertTrue(f.damage.get(f.first)>0);
            f.manager.pulse(70);assertEquals(0,f.manager.count(f.first));assertTrue(f.actors.isEmpty());
            for(Native950Conjures.Kind k:Native950Conjures.Kind.values())assertEquals(0,f.first.getVarsManager().getValue(k.activeVar));
        }
    }
    @Test public void twoOwnersHaveIndependentActorsDamageHauntsAndConduitCleanup(){
        try(Fixture f=new Fixture()){
            f.manager.cast(f.first,48306,0);f.manager.cast(f.second,48306,0);
            f.manager.cast(f.first,48307,1);f.manager.cast(f.second,48307,1);f.manager.pulse(7);
            assertEquals(10,f.manager.hauntedBonus(f.first,f.target,100,8));assertEquals(10,f.manager.hauntedBonus(f.second,f.target,100,8));
            f.noConduit=f.first;f.manager.pulse(8);
            assertEquals(0,f.manager.count(f.first));assertEquals(1,f.manager.count(f.second));
            assertEquals(0,f.manager.hauntedBonus(f.first,f.target,100,8));assertEquals(10,f.manager.hauntedBonus(f.second,f.target,100,8));
            assertEquals(1,f.actors.size());
        }
    }
    @Test public void partialSpawnFailureRollsBackActorsAndDoesNotPublishOwnership(){
        try(Fixture f=new Fixture()){
            f.failSpawn=3;
            try{f.manager.cast(f.first,33965,0);fail("expected spawn failure");}catch(IllegalStateException expected){}
            assertTrue(f.actors.isEmpty());assertEquals(0,f.manager.count(f.first));
            assertEquals(0,f.first.getVarsManager().getValue(10994));
        }
    }
    @Test public void zombieCommandDelaysExplosionAndThenDismissesOnlyThatSpirit(){
        try(Fixture f=new Fixture()){
            f.manager.cast(f.first,33965,0);f.manager.cast(f.first,48305,1);
            f.manager.pulse(4);assertFalse(f.damage.containsKey(f.first));assertEquals(4,f.manager.count(f.first));
            f.manager.pulse(5);assertEquals(Integer.valueOf(360),f.damage.get(f.first));assertEquals(3,f.manager.count(f.first));
            assertEquals(0,f.first.getVarsManager().getValue(11006));
        }
    }
    @Test public void exactCacheLevelLimitsAndActiveCommandTransform(){
        assertEquals(1,Native950Conjures.limit(51));assertEquals(2,Native950Conjures.limit(52));
        assertEquals(3,Native950Conjures.limit(84));assertEquals(4,Native950Conjures.limit(106));
        try(Fixture f=new Fixture()){
            assertEquals(48302,f.manager.effective(f.first,48302));f.manager.cast(f.first,48302,0);
            assertEquals(48303,f.manager.effective(f.first,48302));f.manager.clear(f.first);assertEquals(48302,f.manager.effective(f.first,48302));
        }
    }
}
