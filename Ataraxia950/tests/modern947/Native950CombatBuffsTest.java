package com.rs.game.player.client;

import com.rs.game.Hit.HitLook;
import com.rs.game.WorldTile;
import com.rs.game.player.Player;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.Test;
import static org.junit.Assert.*;

public class Native950CombatBuffsTest {
    @Test public void sunshineIsMagicOnlyAndBoundToItsOriginalAreaAndPlane(){
        EmbeddedChannel c=new EmbeddedChannel();
        try{
            Player p=Player.createNative950("sun-test",new WorldTile(100,100,0),c);
            Native950CombatBuffs b=new Native950CombatBuffs();b.apply(p,Native950CombatBuffs.Type.SUNSHINE,5);
            assertEquals(150,b.outgoing(p,HitLook.MAGIC_DAMAGE,100,54));
            assertEquals(100,b.outgoing(p,HitLook.RANGE_DAMAGE,100,54));
            p.setLocation(104,100,0);assertEquals(100,b.outgoing(p,HitLook.MAGIC_DAMAGE,100,54));
            p.setLocation(100,100,1);assertEquals(100,b.outgoing(p,HitLook.MAGIC_DAMAGE,100,54));
            p.setLocation(100,100,0);assertEquals(100,b.outgoing(p,HitLook.MAGIC_DAMAGE,100,55));
        }finally{c.finishAndReleaseAll();}
    }
    @Test public void defensiveEffectsAreBoundedAndConsumedOnlyOnce(){
        EmbeddedChannel c=new EmbeddedChannel();
        try{
            Player p=Player.createNative950("def-test",new WorldTile(100,100,0),c);
            Native950CombatBuffs b=new Native950CombatBuffs();
            b.apply(p,Native950CombatBuffs.Type.ANTICIPATION,0);
            assertTrue(p.isStunImmune());assertEquals(90,b.incoming(p,100,1));
            b.apply(p,Native950CombatBuffs.Type.FREEDOM,5);assertTrue(p.isFreezeImmune());
            b.expire(15,(who,type)->{});assertFalse(p.isFreezeImmune());assertTrue(p.isStunImmune());
            b.expire(16,(who,type)->{});assertFalse(p.isStunImmune());
            b.apply(p,Native950CombatBuffs.Type.RESONANCE,20);
            assertTrue(b.consume(p,Native950CombatBuffs.Type.RESONANCE,21));
            assertFalse(b.consume(p,Native950CombatBuffs.Type.RESONANCE,21));
            b.apply(p,Native950CombatBuffs.Type.BARRICADE,20,8,null);
            assertEquals(0,b.incoming(p,10000,27));assertEquals(10000,b.incoming(p,10000,28));
            b.apply(p,Native950CombatBuffs.Type.IMMORTALITY,30);
            assertEquals(75,b.incoming(p,100,31));
            assertTrue(b.consume(p,Native950CombatBuffs.Type.IMMORTALITY,31));
            assertEquals(100,b.incoming(p,100,31));
        }finally{c.finishAndReleaseAll();}
    }
    @Test public void debilitateOnlyReducesDamageFromTheDebilitatedAttacker(){
        EmbeddedChannel c=new EmbeddedChannel();
        try{
            Player p=Player.createNative950("def-test",new WorldTile(100,100,0),c);
            Player attacker=Player.createNative950("source-test",new WorldTile(101,100,0),c);
            Native950CombatBuffs b=new Native950CombatBuffs();
            b.apply(p,Native950CombatBuffs.Type.DEBILITATE,1,13,attacker);
            assertEquals(50,b.incoming(p,attacker,100,2));
            assertEquals(100,b.incoming(p,p,100,2));assertEquals(100,b.incoming(p,attacker,100,14));
        }finally{c.finishAndReleaseAll();}
    }
    @Test public void equipmentRemovalCancelsShieldBuffsButNotSunshine(){
        EmbeddedChannel c=new EmbeddedChannel();
        try{
            Player p=Player.createNative950("def-test",new WorldTile(100,100,0),c);
            Native950CombatBuffs b=new Native950CombatBuffs();
            b.apply(p,Native950CombatBuffs.Type.BARRICADE,1);b.apply(p,Native950CombatBuffs.Type.SUNSHINE,1);
            b.checkEquipment(who->false,(who,type)->{});
            assertFalse(b.active(p,Native950CombatBuffs.Type.BARRICADE,2));assertTrue(b.active(p,Native950CombatBuffs.Type.SUNSHINE,2));
        }finally{c.finishAndReleaseAll();}
    }
    @Test public void revengeStacksCapAndNaturalInstinctAndDevotionExpire(){
        EmbeddedChannel c=new EmbeddedChannel();
        try{
            Player p=Player.createNative950("def-test",new WorldTile(100,100,0),c);
            Native950CombatBuffs b=new Native950CombatBuffs();b.apply(p,Native950CombatBuffs.Type.REVENGE,0);
            assertEquals(100,b.outgoing(p,HitLook.MELEE_DAMAGE,100,0));
            for(int i=0;i<20;i++)b.receivedAttack(p,1);
            assertEquals(300,b.outgoing(p,HitLook.MELEE_DAMAGE,100,31));assertEquals(100,b.outgoing(p,HitLook.MELEE_DAMAGE,100,32));
            b.apply(p,Native950CombatBuffs.Type.NATURAL_INSTINCT,0);
            assertEquals(18,b.adrenalineGain(p,9,33));assertEquals(9,b.adrenalineGain(p,9,34));
            b.apply(p,Native950CombatBuffs.Type.DEVOTION,0);
            for(int i=0;i<10;i++)b.onKill(p,1);
            assertTrue(b.active(p,Native950CombatBuffs.Type.DEVOTION,31));assertFalse(b.active(p,Native950CombatBuffs.Type.DEVOTION,32));
            b.apply(p,Native950CombatBuffs.Type.FREEDOM,35);b.clear((who,type)->{});
            assertFalse(p.isStunImmune());assertFalse(p.isFreezeImmune());
        }finally{c.finishAndReleaseAll();}
    }
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
