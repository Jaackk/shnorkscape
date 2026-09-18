package com.rs.game.player.client;

import com.rs.cache.Cache;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.TemporaryAttributes;
import com.rs.game.tasks.WorldTasksManager;
import io.netty.channel.embedded.EmbeddedChannel;
import java.nio.file.Paths;
import java.util.concurrent.ConcurrentHashMap;

/** Isolated cache-backed reproduction: no live connection or character/save access. */
public final class Native950OverloadAcceptance {
    public static void main(String[] args) throws Exception {
        Cache.initFlatReadOnly(Paths.get("cache"));
        EmbeddedChannel channel = new EmbeddedChannel();
        try {
            Player player = Player.createNative950("pot-test", new WorldTile(3294,10129,0), channel);
            player.setActive(true);
            player.setChargesManagerNew(new com.rs.game.player.ChargesManagerNew());
            player.getChargesManagerNew().setPlayer(player);
            player.setAttackingDelay(com.rs.utils.Utils.currentTimeMillis()+60000);
            player.getSkills().set(3,99);
            player.setHitpoints(990);
            Item helm = new Item(20137) {
                @Override public Item setAttributes(ConcurrentHashMap<TemporaryAttributes.Key,Object> attributes) {
                    throw new AssertionError("Unexpected equipment metadata write");
                }
            };
            player.getEquipment().getItems().set(0,helm);
            player.getInventory().getItems().set(0,new Item(23533));
            if(!Native950Potions.drink(player,0,23533))throw new AssertionError("Drink was rejected");
            for(int tick=0;tick<15;tick++) {
                WorldTasksManager.processTasks();
                player.processEntity();
                player.processReceivedHits();
                Item worn=player.getEquipment().getItem(0);
                if(worn!=helm||worn.getAttributes()!=null)throw new AssertionError("Metadata at tick "+tick+": "+worn.getAttributes());
            }
            if(player.getInventory().getItem(0).getId()!=23534)throw new AssertionError("Dose transition");
            if(player.getOverloadDelay()<=0)throw new AssertionError("Missing Overload effect");
            if(player.getHitpoints()!=490)throw new AssertionError("Expected five self-hits: HP="+player.getHitpoints());
            player.setOverloadDelay(0);player.setHitpoints(990);player.addPotDelay(-1);
            if(!Native950Potions.drink(player,0,23534))throw new AssertionError("Second dose was rejected");
            player.setActive(false);int hp=player.getHitpoints();
            for(int tick=0;tick<15;tick++){WorldTasksManager.processTasks();player.processReceivedHits();}
            if(player.getHitpoints()!=hp)throw new AssertionError("Overload pulses survived inactive session");
            player.setActive(true);player.setOverloadDelay(0);player.addPotDelay(-1);
            if(!Native950Potions.drink(player,0,23535))throw new AssertionError("Third dose was rejected");
            player.setHitpoints(0);WorldTasksManager.processTasks();
            player.setHitpoints(990);
            for(int tick=0;tick<15;tick++){WorldTasksManager.processTasks();player.processReceivedHits();}
            if(player.getHitpoints()!=990)throw new AssertionError("Overload pulses resumed after death");
            if(helm.getAttributes()!=null)throw new AssertionError("Metadata was created");
            System.out.println("PASS: Overload dose/effect/five self-hits preserve gear; pending pulses terminate on death/inactive session.");
        } finally { channel.finishAndReleaseAll(); }
    }
}
