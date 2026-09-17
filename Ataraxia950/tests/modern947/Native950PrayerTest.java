package com.rs.game.player.client;

import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.content.Burying.Bone;
import com.rs.game.player.content.items.AshScattering.AshesData;
import com.rs.game.player.controllers.Controller;
import io.netty.channel.embedded.EmbeddedChannel;
import java.util.Arrays;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class Native950PrayerTest {
    private EmbeddedChannel channel;
    private Player player;
    private final Native950ItemCatalog.Entry bones=new Native950ItemCatalog.Entry(526,"Bones",false,new String[]{"Bury"});
    private final Native950ItemCatalog.Entry ashes=new Native950ItemCatalog.Entry(20264,"Impious ashes",false,new String[]{"Scatter"});
    @Before public void setup() {
        channel=new EmbeddedChannel();
        player=Player.createNative950("prayer-test",new WorldTile(3217,3258,0),channel);player.setActive(true);
        Native950Containers containers=new Native950Containers(player,new Native950ItemCatalog(Arrays.asList(bones,ashes)));
        Native950Skilling.attach(player,containers);
        player.getInventory().items.set(0,new Item(526,1));player.getInventory().items.set(1,new Item(526,1));
        player.getInventory().items.set(2,new Item(20264,1));
    }
    @After public void cleanup(){Native950Skilling.detach(player);channel.finishAndReleaseAll();}
    private boolean offer(int slot,int id,int option,Native950ItemCatalog.Entry type){return Native950Prayer.offer(player,slot,id,option,type);}
    private double xp(){return player.getSkills().getXp(Skills.PRAYER);}
    @Test public void buryConsumesOneAwardsOriginalXpAndRejectsRepeatDuringOneTickLock() {
        assertTrue(offer(0,526,1,bones));assertEquals(1,player.getInventory().getAmountOf(526));assertTrue(xp()>0);
        assertEquals(827,player.getNextAnimation().getIds()[0]);double awarded=xp();
        assertFalse(offer(1,526,1,bones));assertEquals(awarded,xp(),0);assertEquals(1,player.getInventory().getAmountOf(526));
        assertEquals(1,player.getBonesOffered());
    }
    @Test public void scatterUsesOriginalAshTableAndNativeAnimation() {
        assertTrue(offer(2,20264,1,ashes));assertEquals(0,player.getInventory().getAmountOf(20264));
        assertTrue(xp()>0);assertEquals(445,player.getNextAnimation().getIds()[0]);
        assertEquals(AshesData.IMPIOUS.getGFX(),player.getNextGraphics1().getId());
        assertEquals(25,AshesData.IMPIOUS.getExp(),0);assertEquals(5,Bone.BONES.getExperience(),0);
    }
    @Test public void staleSlotWrongOptionAndMissingIdentityNeverConsumeOrAward() {
        assertFalse(offer(2,526,1,bones));assertFalse(offer(-1,526,1,bones));assertFalse(offer(28,526,1,bones));
        assertFalse(offer(0,526,2,bones));assertFalse(offer(0,526,1,null));
        assertEquals(2,player.getInventory().getAmountOf(526));assertEquals(0,xp(),0);assertEquals(0,player.getBonesOffered());
    }
    @Test public void deathLogoutAndStagedTeleportNeverConsumeOrAward() {
        player.setActive(false);assertFalse(offer(0,526,1,bones));player.setActive(true);
        player.setNextWorldTile(new WorldTile(3200,3200,0));assertFalse(offer(0,526,1,bones));player.setNextWorldTile(null);
        player.setHitpoints(0);assertFalse(offer(0,526,1,bones));
        assertEquals(2,player.getInventory().getAmountOf(526));assertEquals(0,xp(),0);
    }
    @Test public void absentTransactionOwnerCannotCreateFreeExperience() {
        Native950Skilling.detach(player);assertFalse(offer(0,526,1,bones));
        assertEquals(2,player.getInventory().getAmountOf(526));assertEquals(0,xp(),0);assertFalse(player.isLocked());
    }
    @Test public void controllerVetoLeavesItemsXpAndLockUntouched() {
        player.getControlerManager().startControler(new Controller() {
            public void start() { }
            @Override public boolean canDeleteInventoryItem(int id,int amount) {return false;}
        });
        assertFalse(offer(0,526,1,bones));
        assertEquals(2,player.getInventory().getAmountOf(526));assertEquals(0,xp(),0);assertFalse(player.isLocked());
    }
    @Test public void repurposedCustomDustAndOrdinaryFireAshesStayUnsupported() {
        assertNull(Native950Prayer.itemEntry(3325));assertNull(Native950Prayer.itemEntry(592));
        assertNull(Native950Prayer.itemEntry(527)); // noted bones
        assertFalse(offer(0,3325,1,new Native950ItemCatalog.Entry(3325,"Vampyre dust",false,new String[]{"Scatter"})));
        assertEquals(0,xp(),0);
    }
}
