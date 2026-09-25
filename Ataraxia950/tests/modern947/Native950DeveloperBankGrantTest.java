package com.rs.game.player.client;

import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import io.netty.channel.embedded.EmbeddedChannel;
import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class Native950DeveloperBankGrantTest {
    @Test public void bankGrantPreservesInventoryEquipmentOtherPlayerAndRefusesOverflow(){
        EmbeddedChannel channel=new EmbeddedChannel();
        try{
            Player p=Player.createNative950("one",new WorldTile(3200,3200,0),channel);
            Player q=Player.createNative950("two",new WorldTile(3200,3200,0),channel);
            Native950ItemCatalog catalog=new Native950ItemCatalog(Arrays.asList(new Native950ItemCatalog.Entry(995,"Coins",true,new String[]{"Add to pouch"})));
            Native950Containers a=new Native950Containers(p,catalog),b=new Native950Containers(q,catalog);
            p.getInventory().items.set(0,new Item(995,123));
            assertEquals(100,a.receiveDeveloperBankItem(995,100).moved);
            assertEquals(100,p.getBank().bankTabs[0][0].getAmount());
            assertEquals(123,p.getInventory().items.get(0).getAmount());
            assertEquals(0,q.getBank().bankTabs[0].length);
            assertEquals(0,a.receiveDeveloperBankItem(995,Integer.MAX_VALUE).moved);
            assertEquals(100,p.getBank().bankTabs[0][0].getAmount());
            assertEquals(0,a.receiveDeveloperBankItem(995,-1).moved);
            assertEquals(101,a.receiveDeveloperBankItem(995,101).moved);
            assertEquals(201,p.getBank().bankTabs[0][0].getAmount());
        }finally{channel.finishAndReleaseAll();}
    }
}
