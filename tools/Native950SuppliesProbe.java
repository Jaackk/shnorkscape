package com.rs.game.player.client;

import com.rs.cache.Cache;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import io.netty.channel.embedded.EmbeddedChannel;
import java.nio.file.Paths;
import java.util.Collections;

/** Isolated real-cache ammunition checks; does not connect to or mutate the running world. */
public final class Native950SuppliesProbe {
    public static void main(String[] args) throws Exception {
        Cache.initFlatReadOnly(Paths.get(args[0]));
        EmbeddedChannel channel=new EmbeddedChannel();
        Player p=Player.createNative950("supply-probe",new WorldTile(3217,3258,0),channel);p.setActive(true);
        Native950Skilling.attach(p,new Native950Containers(p,new Native950ItemCatalog(Collections.emptyList()).withLegacyDrops()));
        try {
            Native950CombatStyles.Profile bow=new Native950CombatStyles.Profile(Native950CombatStyles.RANGED,Skills.RANGE,1,4,6,-1,-1,1,false);
            p.getEquipment().getItems().set(13,new Item(882,2));p.setInfiniteAmmunition(true);
            if(!bow.consume(p)||p.getEquipment().getItem(13).getAmount()!=2)throw new AssertionError("Infinite ammo consumed an arrow");
            p.setInfiniteAmmunition(false);
            if(!bow.consume(p)||p.getEquipment().getItem(13).getAmount()!=1)throw new AssertionError("Normal arrow consumption did not resume");
            p.setInfiniteAmmunition(true);p.getEquipment().getItems().set(13,new Item(9244,2));
            if(bow.consume(p))throw new AssertionError("Infinite ammo admitted bolts in a bow");
            p.getEquipment().getItems().set(13,null);
            if(bow.consume(p))throw new AssertionError("Infinite ammo admitted an empty slot");
            System.out.println("PASS: infinite ammo preserves arrows, disabling consumes arrows, and wrong/missing ammunition is refused.");
        } finally { Native950Skilling.detach(p);channel.finishAndReleaseAll(); }
    }
}
