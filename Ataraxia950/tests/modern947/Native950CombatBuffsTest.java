package com.rs.game.player.client;

import com.rs.game.Hit.HitLook;
import com.rs.game.WorldTile;
import com.rs.game.player.Player;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.Test;
import static org.junit.Assert.*;

public class Native950CombatBuffsTest {
    @Test public void berserkIsMeleeOnlyWithModernisedOutgoingAndIncomingMultipliers(){
        EmbeddedChannel c=new EmbeddedChannel();
        try{
            Player p=Player.createNative950("buff-test",new WorldTile(100,100,0),c);
            Native950CombatBuffs buffs=new Native950CombatBuffs();buffs.apply(p,Native950CombatBuffs.Type.BERSERK,10);
            assertEquals(175,buffs.outgoing(p,HitLook.MELEE_DAMAGE,100,42));
            assertEquals(100,buffs.outgoing(p,HitLook.RANGE_DAMAGE,100,42));
            assertEquals(100,buffs.outgoing(p,HitLook.MAGIC_DAMAGE,100,42));
            assertEquals(125,buffs.incoming(p,100,42));
            assertEquals(100,buffs.outgoing(p,HitLook.MELEE_DAMAGE,100,43));
            assertEquals(100,buffs.incoming(p,100,43));
        }finally{c.finishAndReleaseAll();}
    }
    @Test public void swiftnessIsRangedOnlyAnchoredAndExpiresFromServerTick(){
        EmbeddedChannel c=new EmbeddedChannel();
        try{
            Player p=Player.createNative950("buff-test",new WorldTile(100,100,0),c);
            Native950CombatBuffs buffs=new Native950CombatBuffs();buffs.apply(p,Native950CombatBuffs.Type.DEATHS_SWIFTNESS,10);
            assertEquals(150,buffs.outgoing(p,HitLook.RANGE_DAMAGE,100,59));
            assertEquals(100,buffs.outgoing(p,HitLook.MELEE_DAMAGE,100,59));
            p.setLocation(104,100,0);assertEquals(100,buffs.outgoing(p,HitLook.RANGE_DAMAGE,100,59));
            p.setLocation(103,100,0);assertEquals(150,buffs.outgoing(p,HitLook.RANGE_DAMAGE,100,59));
            assertEquals(100,buffs.outgoing(p,HitLook.RANGE_DAMAGE,100,60));
        }finally{c.finishAndReleaseAll();}
    }
    @Test public void refreshReplacesExpiryAndDeathRemovesEachEffectOnce(){
        EmbeddedChannel c=new EmbeddedChannel();
        try{
            Player p=Player.createNative950("buff-test",new WorldTile(100,100,0),c);
            Native950CombatBuffs buffs=new Native950CombatBuffs();int[] removed={0};
            buffs.apply(p,Native950CombatBuffs.Type.BERSERK,0);buffs.apply(p,Native950CombatBuffs.Type.BERSERK,10);
            buffs.apply(p,Native950CombatBuffs.Type.DEATHS_SWIFTNESS,10);
            buffs.expire(33,(player,type)->removed[0]++);assertEquals(0,removed[0]);
            assertTrue(buffs.active(p,Native950CombatBuffs.Type.BERSERK,33));
            p.setHitpoints(0);buffs.expire(34,(player,type)->removed[0]++);assertEquals(2,removed[0]);
            buffs.expire(60,(player,type)->removed[0]++);assertEquals(2,removed[0]);
        }finally{c.finishAndReleaseAll();}
    }
    @Test public void clearingEffectsRemovesBerserkAndDeathsSwiftnessExactlyOnce(){
        EmbeddedChannel c=new EmbeddedChannel();
        try{
            Player p=Player.createNative950("buff-test",new WorldTile(100,100,0),c);
            Native950CombatBuffs buffs=new Native950CombatBuffs();int[] removed={0};
            buffs.apply(p,Native950CombatBuffs.Type.BERSERK,0);
            buffs.apply(p,Native950CombatBuffs.Type.DEATHS_SWIFTNESS,0);
            buffs.clear((player,type)->removed[0]++);
            assertEquals(2,removed[0]);
            assertFalse(buffs.active(p,Native950CombatBuffs.Type.BERSERK,1));
            assertFalse(buffs.active(p,Native950CombatBuffs.Type.DEATHS_SWIFTNESS,1));
            buffs.clear((player,type)->removed[0]++);
            assertEquals(2,removed[0]);
        }finally{c.finishAndReleaseAll();}
    }
}
