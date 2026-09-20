package com.rs.game.player.client;
import com.rs.game.*;import com.rs.game.item.Item;import com.rs.game.npc.NPC;import com.rs.game.player.*;
import io.netty.channel.embedded.EmbeddedChannel;import java.util.*;import org.junit.*;import static org.junit.Assert.*;
public class Native950CombatStylesTest {
 EmbeddedChannel channel;Player p;NPC npc;Native950MeleeCombat combat;boolean adjacent,line=true;int follows;
 @Before public void setup(){channel=new EmbeddedChannel();p=Player.createNative950("style-test",new WorldTile(3217,3258,0),channel);p.setActive(true);p.setIndex(1);
  npc=NPC.createNative950(12353,new WorldTile(3222,3258,0),1);npc.setIndex(1);
  Native950CombatStyles.Profile style=new Native950CombatStyles.Profile(3,28,1,4,6,-1,-1,0,false);
  combat=new Native950MeleeCombat(Thread.currentThread(),new Native950MeleeCombat.Access(){
   public void activate(NPC n){}public boolean player(Player v){return v==p;}public boolean npc(NPC n){return n==npc;}
   public boolean clear(WorldTile t){return true;}public boolean reach(Entity a,Entity b){return adjacent;}
   public boolean rangedReach(Player a,NPC b,int range){return line;}public boolean approach(Player a,NPC b){return true;}
   public boolean follow(NPC n,WorldTile t){follows++;return true;}
  },new Native950MeleeCombat.Rolls(){public boolean accurate(long a,long d){return true;}public int damage(int max){return 1;}},
    v->new Native950MeleeCombat.Loadout(0,0,0,4,-1,-1,style));
  combat.attach(p);combat.register(npc,new Native950NpcCombatProfile(12353,1,1,50,1,1,10,4,1,4,-1,-1,-1,0,0));
 }
 @After public void close(){combat.clear();channel.finishAndReleaseAll();}
 void tick(){combat.beforeMovement();combat.afterMovement();}
 @Test public void nativeArmourIsNotDiscardedByTheClassicMagicStyleFallback(){
  assertEquals(Integer.valueOf(123),Native950CombatStyles.nativeArmourBonus(Collections.<Integer,Object>singletonMap(2870,12345)));
  assertEquals(Integer.valueOf(0),Native950CombatStyles.nativeArmourBonus(Collections.<Integer,Object>singletonMap(2870,0)));
  assertNull(Native950CombatStyles.nativeArmourBonus(Collections.emptyMap()));
  for(Object bad:new Object[]{"12345",-1})try{
   Native950CombatStyles.nativeArmourBonus(Collections.singletonMap(2870,bad));fail("Malformed armour must not use fallback");
  }catch(IllegalArgumentException expected){}
 }
 @Test public void infiniteRunesPermitCastingWithoutRunesAndResumeRealCosts(){
  Native950CombatStyles.Profile magic=new Native950CombatStyles.Profile(Native950CombatStyles.MAGIC,Skills.MAGIC,1,4,6,-1,-1,0,false);
  Native950Containers items=new Native950Containers(p,new Native950ItemCatalog(Arrays.asList(new Native950ItemCatalog.Entry(556,"Air rune",true,new String[5]))));
  Native950Skilling.attach(p,items);
  try {
   assertFalse(magic.consume(p));p.setInfiniteCombatRunes(true);assertTrue(magic.consume(p));
   assertEquals(0,p.getInventory().getAmountOf(556));p.getInventory().items.set(0,new Item(556,2));
   assertTrue(magic.consume(p));assertEquals(2,p.getInventory().getAmountOf(556));
   p.setInfiniteCombatRunes(false);assertTrue(magic.consume(p));assertEquals(1,p.getInventory().getAmountOf(556));
  }finally{Native950Skilling.detach(p);}
 }
 @Test public void infiniteAmmoBypassesOnlyAmmunitionAvailability(){
  Native950CombatStyles.Profile ranged=new Native950CombatStyles.Profile(Native950CombatStyles.RANGED,Skills.RANGE,1,4,6,-1,-1,1,false);
  assertFalse(ranged.consume(p));
  p.setInfiniteAmmunition(true);assertTrue(ranged.consume(p));
  p.setInfiniteAmmunition(false);assertFalse(ranged.consume(p));
 }
 @Test public void rangedHitDoesNotGiveNpcMeleeReach(){assertNull(combat.attack(p,npc));tick();assertEquals(40,npc.getHitpoints());assertEquals(100,p.getHitpoints());tick();assertTrue(follows>0);}
 @Test public void blockedLineOfSightCannotDamage(){line=false;assertNull(combat.attack(p,npc));tick();assertEquals(50,npc.getHitpoints());}
 @Test public void npcRetaliatesOnceAdjacent(){combat.attack(p,npc);tick();adjacent=true;tick();assertEquals(90,p.getHitpoints());}
 @Test public void equippedAmmoCannotConsumeAnotherSlotOrMoreThanItsStack(){
  Native950ItemCatalog catalog=new Native950ItemCatalog(Arrays.asList(new Native950ItemCatalog.Entry(882,"Bronze arrow",true,new String[]{null,"Equip",null,null,"Drop"},13,2)));
  Native950Containers containers=new Native950Containers(p,catalog);p.getEquipment().getItems().set(13,new Item(882,2));
  assertFalse(containers.consumeEquipment(13,884,1));assertFalse(containers.consumeEquipment(13,882,3));
  assertTrue(containers.consumeEquipment(13,882,1));assertEquals(1,p.getEquipment().getItem(13).getAmount());
  assertTrue(containers.consumeEquipment(13,882,1));assertNull(p.getEquipment().getItem(13));assertFalse(containers.consumeEquipment(13,882,1));
 }
}
