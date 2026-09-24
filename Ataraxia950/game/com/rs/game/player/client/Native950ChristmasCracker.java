package com.rs.game.player.client;
import com.rs.game.item.Item;import com.rs.game.player.Player;import com.rs.network.protocol.modern950.Native950Packets;
/** RS3 cracker interaction: owner receives the hat, partner receives the secondary prize. */
final class Native950ChristmasCracker {
 private static final int[] HATS={1038,1040,1042,1044,1046,1048};
 private static final int[] EXTRAS={1969,2355,1217,1635,1973,1718,950,563,1987};
 static String pull(Player owner,Player partner,int slot){return pull(owner,partner,slot,new java.util.Random());}
 static String pull(Player owner,Player partner,int slot,java.util.Random random){
  if(owner==null||partner==null||owner==partner||!available(owner)||!available(partner))return "Both players must be available to pull a cracker.";
  if(owner.getPlane()!=partner.getPlane()||Math.max(Math.abs(owner.getX()-partner.getX()),Math.abs(owner.getY()-partner.getY()))>1)return "Stand beside the other player to pull the cracker.";
  Native950Containers a=Native950Skilling.containers(owner),b=Native950Skilling.containers(partner);
  if(a==null||b==null||!a.ownsInventory(owner)||!b.ownsInventory(partner))return "Both backpacks must be available.";
  if(slot<0||slot>=28)return "That cracker slot is unavailable.";
  Item source=owner.getInventory().items.get(slot);
  if(source==null||source.getId()!=962||source.getAmount()!=1||source.getCharges()!=0||source.getAttributes()!=null||source.getInventionData()!=null)return "The cracker in that slot has changed.";
  Item hat=new Item(HATS[random.nextInt(HATS.length)],1),extra=new Item(EXTRAS[random.nextInt(EXTRAS.length)],1);
  if(a.itemType(hat.getId())==null||!b.canReceiveItems(new Item[]{extra}))return "Your partner needs room for the prize; nothing was consumed.";
  // The world owner thread executes both commits without yielding. Receiving stages its entire
  // result first; replacing the exact cracker slot then cannot overflow a full owner inventory.
  if(owner.getInventory().items.get(slot)!=source||!b.receiveItems(new Item[]{extra}))return "The backpacks changed; nothing was consumed.";
  owner.getInventory().items.set(slot,hat);
  owner.setNextFaceEntity(partner);partner.setNextFaceEntity(owner);
  if(partner.getRealChannel()!=null)partner.getRealChannel().write(Native950Packets.gameMessage(0,"You help pull a Christmas cracker and receive its other prize."));
  return null;
 }
 private static boolean available(Player p){return p.isNative950()&&p.isActive()&&!p.hasFinished()&&!p.isDead()&&!p.isLocked()
  &&!p.isNative950ForceMovementActive()&&p.getNextWorldTile()==null&&p.getControlerManager().getControler()==null&&!p.isUnderCombat();}
}
