package com.rs.game.player.client;
import com.rs.cache.Cache;import com.rs.cores.CoresManager;import com.rs.game.*;import com.rs.game.item.Item;import com.rs.game.npc.NPC;import com.rs.game.player.*;
import io.netty.channel.embedded.EmbeddedChannel;import java.nio.file.Paths;import java.util.*;import java.util.concurrent.TimeUnit;
/** Actual950 cache profiles, spell costs, ammunition exhaustion, skill XP and equipment requirements. */
public final class Native950CombatStylesAcceptance {
 static int checks;
 static void check(boolean b,String why){checks++;if(!b)throw new AssertionError(why);}
 public static void main(String[] a)throws Exception{
  System.setProperty(Native950World.SPAWNS_PROPERTY,"false");System.setProperty(Native950World.LEGACY_SPAWNS_PROPERTY,"false");Cache.initFlatReadOnly(Paths.get(a[0]));
  Native950World.getInstance().execute(()->{run();return null;}).get(120,TimeUnit.SECONDS);
  System.out.println("PASS basic combat styles: "+checks+" actual-cache checks; rendered projectiles and special attacks remain later work.");
 }
 static void combatExhaustion(Player p,NPC npc){
  p.getEquipment().getItems().set(3,new Item(806,2));p.getEquipment().getItems().set(13,null);p.getCombatDefinitions().setAutoRetaliate(true);p.setHitpoints(p.getMaxHitpoints());p.resetMasks();
  final int[] hits={0};
  Native950MeleeCombat.Access access=new Native950MeleeCombat.Access(){public void activate(NPC n){}public boolean player(Player q){return true;}public boolean npc(NPC n){return true;}public boolean clear(WorldTile t){return true;}public boolean reach(Entity a,Entity b){return true;}public boolean approach(Player q,NPC n){return true;}};
  Native950MeleeCombat.Rolls rolls=new Native950MeleeCombat.Rolls(){public boolean accurate(long a,long d){return true;}public int damage(int maximum){return maximum==0?0:1;}};
  Native950MeleeCombat combat=new Native950MeleeCombat(Thread.currentThread(),access,rolls,q->q.getEquipment().getWeaponId()<0?new Native950MeleeCombat.Loadout(0,0,0,4,422,424):Native950CombatStyles.loadout(q),new Native950MeleeCombat.Rewards(){public void hit(Player q,NPC n,int d){throw new AssertionError("Loop lost captured loadout");}public void hit(Player q,NPC n,int d,Native950MeleeCombat.Loadout gear){check(gear.profile!=null&&gear.profile.style==1,"Loop XP became melee after final weapon");hits[0]++;}public void death(NPC n,Player q){throw new AssertionError("Probe unexpectedly killed target");}});
  Native950NpcCombatProfile profile=new Native950NpcCombatProfile(npc.getId(),1,1,1000,1,1,0,4,3,10,-1,-1,-1,0,0);combat.register(npc,profile);npc.setHitpoints(1000);combat.attach(p);
  try{check(combat.attack(p,npc)==null,"Actual loop refused ordinary darts");for(int i=0;i<16;i++){combat.beforeMovement();combat.afterMovement();p.resetMasks();npc.resetMasks();}check(hits[0]==2&&p.getEquipment().getWeaponId()<0,"Ammo exhaustion continued unarmed auto-retaliation or lost a throw");}finally{combat.detach(p);combat.clear();}
 }
 static void run(){
  for(int[] row:new int[][]{{841,1,37462},{9174,1,37467},{1381,2,18321},{58486,2,18321},{55502,3,35449},{45445,0,37378}}){
   Native950CombatStyles.Profile p=Native950CombatStyles.profile(row[0]);check(p.style==row[1]&&p.animation==row[2],"Incorrect950 weapon profile "+row[0]);
  }
  EmbeddedChannel channel=new EmbeddedChannel();Player p=Player.createNative950("combat-style",new WorldTile(3217,3258,0),channel);p.setActive(true);p.setIndex(1);
  Native950Containers c=new Native950Containers(p,new Native950ItemCatalog(Collections.emptyList()).withLegacyDrops());Native950Skilling.attach(p,c);
  try{
   p.getEquipment().getItems().set(3,new Item(841,1));p.getEquipment().getItems().set(13,new Item(882,2));
   p.getEquipment().getItems().set(0,new Item(1139,1));
   check(Native950CombatStyles.loadout(p).defenceBonus>0,"Changing attack style discarded armour defence");
   p.getEquipment().getItems().set(0,null);
   Native950CombatStyles.Profile bow=Native950CombatStyles.profile(841);check(bow.costRefusal(p)==null,"Bow refused bronze arrows");
   check(bow.consume(p)&&p.getEquipment().getItem(13).getAmount()==1,"First shot debit");check(bow.consume(p)&&p.getEquipment().getItem(13)==null,"Last shot debit");check(!bow.consume(p),"Empty arrows fired");
   p.getEquipment().getItems().set(13,new Item(877,1));check(bow.costRefusal(p)!=null,"Bow accepted bolts");p.getEquipment().getItems().set(13,null);
   p.getEquipment().getItems().set(3,new Item(1381,1));p.getInventory().items.set(0,new Item(558,2));
   double xp=p.getSkills().getXp(Skills.MAGIC);Native950CombatStyles.Profile air=Native950CombatStyles.profile(1381);
   check(air.consume(p),"Air staff could not supply air rune");check(p.getInventory().items.getNumberOf(558)==2,"RS3 air spell consumed an RS2 mind rune");check(p.getSkills().getXp(Skills.MAGIC)==xp,"Air spell awarded base cast XP in addition to damage XP");
   p.getEquipment().getItems().set(3,new Item(1379,1));Native950CombatStyles.Profile staff=Native950CombatStyles.profile(1379);
   check(!staff.consume(p)&&p.getInventory().items.getNumberOf(558)==2,"Incomplete rune cost consumed mind rune");
   p.getInventory().items.set(1,new Item(556,1));check(staff.consume(p)&&p.getInventory().items.getNumberOf(558)==2&&p.getInventory().items.getNumberOf(556)==0,"RS3 air-rune exchange incorrect");
   p.getEquipment().getItems().set(3,new Item(58486,1));boolean refused=false;try{Native950CombatStyles.loadout(p);}catch(IllegalArgumentException expected){refused=true;}check(refused,"Magic99 weapon admitted without level");
   p.getEquipment().getItems().set(3,new Item(55502,1));xp=p.getSkills().getXp(28);double atk=p.getSkills().getXp(0);
   NPC npc=NPC.createNative950(12353,new WorldTile(3218,3258,0),1);Native950CombatExperience.awardMelee(p,npc,10);check(p.getSkills().getXp(28)>xp&&p.getSkills().getXp(0)==atk,"Necromancy damage trained melee");
   p.getSkills().setXpWithoutRefresh(Skills.MAGIC,Skills.getXPForLevel(Skills.MAGIC,99));p.getSkills().setLevelWithoutRefresh(Skills.MAGIC,99);
   Native950CombatStyles.Profile advanced=Native950CombatStyles.profile(58486);p.getEquipment().getItems().set(3,new Item(58486,1));
   p.getInventory().items.set(1,new Item(556,4));check(!advanced.consume(p)&&p.getInventory().items.getNumberOf(556)==4,"Air Surge accepted fewer than five air runes");
   p.getInventory().items.set(1,new Item(556,5));check(advanced.consume(p)&&p.getInventory().items.getNumberOf(556)==0,"Air Surge did not consume five air runes");
   check(advanced.maxHit(p,100)>staff.maxHit(p,100),"Weapon tier did not cap automatic spell progression");
   p.getSkills().setXpWithoutRefresh(Skills.RANGE,Skills.getXPForLevel(Skills.RANGE,99));p.getSkills().setLevelWithoutRefresh(Skills.RANGE,99);
   p.getEquipment().getItems().set(3,new Item(861,1));p.getEquipment().getItems().set(13,new Item(882,2));
   Native950MeleeCombat.Loadout bronze=Native950CombatStyles.loadout(p);
   p.getEquipment().getItems().set(13,new Item(892,2));Native950MeleeCombat.Loadout rune=Native950CombatStyles.loadout(p);
   check(rune.strengthBonus>bronze.strengthBonus&&rune.attackBonus==bronze.attackBonus,"Ammunition failed to affect damage independently of accuracy");
   p.getEquipment().getItems().set(3,new Item(841,1));check(Native950CombatStyles.loadout(p).strengthBonus==9,"Shortbow did not cap rune ammunition at cache weapon tier5");
   for(int id:new int[]{800,801,806,807,825,826,863,864,865,868,869,30574}){
    Native950CombatStyles.Profile thrown=Native950CombatStyles.profile(id);check(thrown.style==1&&thrown.ammoFamily==3,"Ordinary thrown weapon rejected "+id);
    p.getEquipment().getItems().set(3,new Item(id,2));p.getEquipment().getItems().set(13,new Item(882,2));
    check(thrown.costRefusal(p)==null&&thrown.consume(p)&&p.getEquipment().getItem(3).getAmount()==1&&p.getEquipment().getItem(13).getAmount()==2,"Thrown weapon did not consume its own stack "+id);
    check(thrown.consume(p)&&p.getEquipment().getItem(3)==null&&!thrown.consume(p),"Final thrown weapon depletion incorrect "+id);
   }
   for(int id:new int[]{10033,25202,831}){boolean denied=false;try{Native950CombatStyles.profile(id);}catch(IllegalArgumentException expected){denied=true;}check(denied,"Special/AOE/poison weapon silently admitted "+id);}
   Native950CombatStyles.Profile dart=Native950CombatStyles.profile(806);p.getEquipment().getItems().set(3,new Item(806,2));p.getEquipment().getItem(3).setCharges(1);
   check(!dart.consume(p)&&p.getEquipment().getItem(3).getAmount()==2,"Attributed thrown stack lost metadata");p.getEquipment().getItems().set(3,null);
   double rangeXp=p.getSkills().getXp(Skills.RANGE),defXp=p.getSkills().getXp(Skills.DEFENCE),attackXp=p.getSkills().getXp(Skills.ATTACK);
   Native950CombatExperience.award(p,npc,10,Native950CombatStyles.RANGED);
   check(p.getSkills().getXp(Skills.RANGE)>rangeXp&&p.getSkills().getXp(Skills.DEFENCE)>defXp&&p.getSkills().getXp(Skills.ATTACK)==attackXp,"Final throw after equipment depletion trained melee or lost split XP");
   combatExhaustion(p,npc);
  }finally{Native950Skilling.detach(p);channel.finishAndReleaseAll();}
 }
}
