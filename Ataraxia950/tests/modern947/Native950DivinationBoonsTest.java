package com.rs.game.player.client;
import com.rs.game.WorldTile;import com.rs.game.player.Player;import io.netty.channel.embedded.EmbeddedChannel;import org.junit.*;import static org.junit.Assert.*;
public class Native950DivinationBoonsTest {
 @Test public void snapshotsAndRestoreCopyAllLegacySlots(){EmbeddedChannel c=new EmbeddedChannel();try{Player p=Player.createNative950("boon-unit",new WorldTile(3217,3258,0),c);boolean[] flags=new boolean[12];flags[0]=true;flags[1]=true;flags[11]=true;Native950Divination.restoreBoons(p,flags);flags[1]=false;assertTrue(Native950Divination.boons(p)[1]);boolean[] snapshot=Native950Divination.boons(p);snapshot[11]=false;assertTrue(Native950Divination.boons(p)[11]);assertTrue(Native950Divination.boons(p)[0]);Native950Divination.restoreBoons(p,null);for(boolean unlocked:Native950Divination.boons(p))assertFalse(unlocked);}finally{c.finishAndReleaseAll();}}
 @Test(expected=IllegalArgumentException.class)public void truncatedSaveCannotSilentlyShiftBoonTiers(){Native950Divination.validateBoons(new boolean[11]);}
 @Test(expected=IllegalArgumentException.class)public void extraSaveFlagsNeedExplicitMigration(){Native950Divination.validateBoons(new boolean[13]);}
}
