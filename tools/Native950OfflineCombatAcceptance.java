package com.rs.game.player.client;

import com.rs.cache.Cache;
import com.rs.game.*;
import com.rs.game.item.Item;
import com.rs.game.npc.NPC;
import com.rs.game.player.*;
import io.netty.channel.embedded.EmbeddedChannel;
import java.lang.reflect.Field;
import java.nio.file.Paths;
import java.util.*;

/** Isolated real-cache world phases; never opens a save or a listening socket. */
public final class Native950OfflineCombatAcceptance {
    private static int checks;
    private static void check(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
    public static void main(String[] args)throws Exception{
        Cache.initFlatReadOnly(Paths.get("cache"));Native950AbilityCatalog.verify();
        cadence(14670,841,new int[]{0,1,2,3,4,5,6,7});
        cadence(19343,1381,new int[]{0,1,2});
        cadence(14731,1381,new int[]{0,2,4,6});
        cadence(14666,841,new int[]{2});
        cadence(14674,841,new int[]{0,1,2,3});
        cadence(14736,1381,new int[]{0});
        try(Fixture f=new Fixture(841)){
            f.start(14670);f.step();f.player.getEquipment().getItems().set(3,new Item(861));
            f.step();check(f.combat.pendingHitCount(f.player)==0,"Weapon swap retained channel hits");
        }
        System.out.println("PASS offline native combat: "+checks+" cache-backed timing, resource, dummy and cancellation checks.");
    }
    private static void cadence(int structure,int weapon,int[] expected)throws Exception{
        try(Fixture f=new Fixture(weapon)){
            f.start(structure);List<Integer> actual=new ArrayList<>();
            for(int tick=0;tick<=expected[expected.length-1];tick++){
                f.step();for(Hit hit:f.npc.getNextHits())actual.add(tick);
                // Suppress later autos without touching committed ability hits.
                if(tick==0)f.player.addFoodDelay(60000);
                check(f.npc.getHitpoints()==100000,"Dummy HP changed");
            }
            check(actual.equals(Arrays.stream(expected).boxed().collect(java.util.stream.Collectors.toList())),
                    structure+" hit ticks "+actual+" != "+Arrays.toString(expected));
            check(f.combat.pendingHitCount(f.player)==0,"Unlanded hits after final scheduled hit");
            check(f.player.getCombatDefinitions().getSpecialAttackPercentage()==100,"Infinite adrenaline spent");
        }
    }
    static final class Fixture implements AutoCloseable {
        final EmbeddedChannel channel=new EmbeddedChannel();
        final Player player=Player.createNative950("offline-combat",new WorldTile(3217,3258,0),channel);
        final NPC npc=NPC.createNative950Diagnostic(16027,new WorldTile(3218,3258,0));
        final Native950MeleeCombat combat;
        final Field animationClock;
        Fixture(int weapon)throws Exception{
            player.setActive(true);player.setIndex(1);npc.setIndex(1);
            for(int skill:new int[]{0,1,2,3,4,5,6}){
                player.getSkills().setXpWithoutRefresh(skill,Skills.getXPForLevel(skill,99));
                player.getSkills().setLevelWithoutRefresh(skill,99);
            }
            player.setHitpoints(990);player.setDevelopmentGodMode(true);
            player.setInfiniteAmmunition(true);player.setInfiniteCombatRunes(true);
            player.getCombatDefinitions().setInfiniteAdrenaline(true);
            player.getEquipment().getItems().set(3,new Item(weapon));
            player.getEquipment().getItems().set(13,new Item(882,100));
            animationClock=Entity.class.getDeclaredField("lastAnimationEnd");animationClock.setAccessible(true);
            combat=new Native950MeleeCombat(Thread.currentThread(),new Native950MeleeCombat.Access(){
                public void activate(NPC n){}public boolean player(Player p){return p==player;}
                public boolean npc(NPC n){return n==npc;}public boolean clear(WorldTile t){return true;}
                public boolean reach(Entity a,Entity b){return true;}public boolean approach(Player p,NPC n){return true;}
            },new Native950MeleeCombat.Rolls(){public boolean accurate(long a,long d){return true;}public int damage(int max){return max;}},
                Native950CombatStyles::loadout,new Native950MeleeCombat.Rewards(){
                    public void hit(Player p,NPC n,int damage){throw new AssertionError("Dummy awarded XP");}
                    public void death(NPC n,Player p){throw new AssertionError("Dummy died");}
                });
            combat.attach(player);combat.registerTraining(npc);
        }
        void start(int structure){check(combat.attack(player,npc)==null,"Attack refused");check(combat.ability(player,structure)==null,"Ability refused: "+structure);}
        void step()throws Exception{
            player.resetMasks();npc.resetMasks();
            // Advance the existing presentation clock by one simulated game tick.
            animationClock.setLong(player,Math.max(0,animationClock.getLong(player)-600));
            combat.beforeMovement();combat.afterMovement();
            channel.checkException();check(channel.isActive(),"Channel failed");
        }
        public void close(){combat.detach(player);combat.clear();channel.finishAndReleaseAll();}
    }
}
