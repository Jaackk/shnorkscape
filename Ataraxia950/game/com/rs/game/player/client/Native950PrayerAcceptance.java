package com.rs.game.player.client;
import com.rs.cache.Cache;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.content.Burying.Bone;
import com.rs.game.player.content.items.AshScattering.AshesData;
import com.rs.network.modern.Native950GameTransport;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.util.ReferenceCountUtil;
import java.nio.file.Paths;
import java.util.Collections;
public final class Native950PrayerAcceptance {
 public static void main(String[] args)throws Exception {
  Cache.initFlatReadOnly(Paths.get(args[0]));int offered=0;
  for(Bone bone:Bone.values()){check(bone.getId());offered++;}
  for(AshesData ashes:AshesData.values())if(ashes.getItemId()!=3325){check(ashes.getItemId());offered++;}
  require(Native950Prayer.itemEntry(3325)==null && Native950Prayer.itemEntry(592)==null && Native950Prayer.itemEntry(527)==null,"Nonoffering admitted");
  System.out.println("PASS: "+offered+" actual 950 bone/ash definitions, original XP, atomic consumption, native animation/effects and healthy framed channel. Custom dust, notes and fire ashes refused.");
 }
 private static void check(int id) {
  Native950ItemCatalog.Entry entry=Native950Prayer.itemEntry(id);require(entry!=null,"Missingoffering "+id);
  Native950GameTransport transport=new Native950GameTransport(()->0,()->0,Thread.currentThread());
  EmbeddedChannel channel=new EmbeddedChannel(transport);
  try {
   Player player=Player.createNative950("prayer-probe",new WorldTile(3217,3258,0),channel);player.setActive(true);
   Native950World.installVarpSink(player);
   Native950ItemCatalog catalog=new Native950ItemCatalog(Collections.singletonList(entry));
   Native950Skilling.attach(player,new Native950Containers(player,catalog));
   player.getInventory().items.set(0,new Item(id,1));
   require(Native950Prayer.offer(player,0,id,1),"Offering refused "+id);
   require(player.getInventory().getAmountOf(id)==0 && player.getSkills().getXp(Skills.PRAYER)>0,"No consumedXP "+id);
   boolean bone=Bone.forId(id)!=null;
   require(player.getNextAnimation()!=null && player.getNextAnimation().getIds()[0]==(bone ? 827 : 445),"Wrong offering animation "+id);
   if(!bone)require(player.getNextGraphics1()!=null && player.getNextGraphics1().getId()==AshesData.forId(id).getGFX(),"Wrong ash effect "+id);
   double xp=player.getSkills().getXp(Skills.PRAYER);require(!Native950Prayer.offer(player,0,id,1),"Repeatedoffering "+id);
   require(player.getSkills().getXp(Skills.PRAYER)==xp,"RepeatXP "+id);
   channel.flushOutbound();channel.runPendingTasks();
   require(transport.terminalFailure()==null,"Transport failed "+id+": "+transport.terminalFailure());
   require(channel.isActive(),"Channel closed "+id);
   Object value;while((value=channel.readOutbound())!=null)ReferenceCountUtil.release(value);
   Native950Skilling.detach(player);
  }finally{channel.finishAndReleaseAll();}
 }
 private static void require(boolean value,String msg){if(!value)throw new IllegalStateException(msg);}
}
