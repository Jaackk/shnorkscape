package com.rs.game.player.client;

import com.rs.game.Hit;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import com.rs.network.protocol.modern950.Native950NpcMasks;
import com.rs.network.protocol.modern950.Native950PlayerMasks;
import com.rs.network.protocol.modern950.Native950PlayerInfo;
import io.netty.channel.embedded.EmbeddedChannel;
import java.util.Arrays;
import java.util.Collections;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;

/** Literal typed hit payloads independently follow native950 smart/count transforms. */
public final class Native950MeleeRenderingTest {
    private final EmbeddedChannel channel=new EmbeddedChannel();
    private final Player attacker=player(1),victim=player(2),observer=player(3);
    private Player player(int index){Player p=Player.createNative950("hit-"+index,new WorldTile(3200,3200,0),channel);p.setIndex(index);return p;}
    @After public void close(){channel.finishAndReleaseAll();}

    @Test public void playerSourceAndVictimSeeStandardDamageWhileObserverGetsObserverStyle(){
        victim.getNextHits().add(new Hit(attacker,50,Hit.HitLook.MELEE_DAMAGE));
        Native950Hits.Snapshot hits=Native950Hits.from(victim);
        //50 engineHP =500 displayedLP, smart500 =0x81f4. Count1+128, then type/damage/delay/barcount.
        assertArrayEquals(hex("40 81 80 85 81 f4 00 00"),playerBytes(hits,1));
        assertArrayEquals(hex("40 81 80 85 81 f4 00 00"),playerBytes(hits,2));
        assertArrayEquals(hex("40 81 80 96 81 f4 00 00"),playerBytes(hits,3));
        assertEquals(0,hits.refusals());
    }
    @Test public void npcDamageUsesTheSameLifePointUnitAndNegatedCount(){
        NPC npc=NPC.createNative950(41,new WorldTile(3200,3201,0),1);
        npc.getNextHits().add(new Hit(attacker,50,Hit.HitLook.MELEE_DAMAGE));
        Native950Hits.Snapshot hits=Native950Hits.from(npc);
        assertArrayEquals(hex("00 00 20 ff 80 85 81 f4 00 00"),npcBytes(hits,1));
        assertArrayEquals(hex("00 00 20 ff 80 96 81 f4 00 00"),npcBytes(hits,3));
    }
    @Test public void zeroDamageUsesTheNativeBlueNumberForEachRecipient(){
        victim.getNextHits().add(new Hit(attacker,0,Hit.HitLook.MELEE_DAMAGE));
        Native950Hits.Snapshot hits=Native950Hits.from(victim);
        // Native direct definitions458/464 contain literal0; smart458=0x81ca,464=0x81d0.
        assertArrayEquals(hex("40 81 81 ca 00 00 00"),playerBytes(hits,1));
        assertArrayEquals(hex("40 81 81 ca 00 00 00"),playerBytes(hits,2));
        assertArrayEquals(hex("40 81 81 d0 00 00 00"),playerBytes(hits,3));
        NPC npc=NPC.createNative950(41,new WorldTile(3200,3201,0),1);
        npc.getNextHits().add(new Hit(attacker,0,Hit.HitLook.MISSED));
        Native950Hits.Snapshot npcMiss=Native950Hits.from(npc);
        assertArrayEquals(hex("00 00 20 ff 81 ca 00 00 00"),npcBytes(npcMiss,1));
        assertArrayEquals(hex("00 00 20 ff 81 d0 00 00 00"),npcBytes(npcMiss,3));
        assertEquals(0,npcMiss.refusals());
    }
    @Test public void eachHitChoosesItsOwnSourceAndSnapshotDoesNotFollowLaterMutations(){
        Hit first=new Hit(attacker,10,Hit.HitLook.MELEE_DAMAGE);
        Hit second=new Hit(observer,20,Hit.HitLook.MELEE_DAMAGE);
        victim.getNextHits().add(first);victim.getNextHits().add(second);
        Native950Hits.Snapshot hits=Native950Hits.from(victim);
        first.setDamage(999);first.setSource(observer);second.setCriticalMark();victim.getNextHits().clear();
        assertArrayEquals(hex("40 82 80 85 64 00 80 96 80 c8 00 00"),playerBytes(hits,1));
        assertArrayEquals(hex("40 82 80 85 64 00 80 85 80 c8 00 00"),playerBytes(hits,2));
        try{hits.playerHits(1).clear();fail();}catch(UnsupportedOperationException expected){}
    }
    @Test public void unsupportedDamageShapesAndOverflowAreRefusedWithoutClamping(){
        victim.getNextHits().add(new Hit(attacker,3276,Hit.HitLook.MELEE_DAMAGE));
        victim.getNextHits().add(new Hit(attacker,3277,Hit.HitLook.MELEE_DAMAGE));
        victim.getNextHits().add(new Hit(attacker,Integer.MAX_VALUE,Hit.HitLook.MELEE_DAMAGE));
        victim.getNextHits().add(new Hit(attacker,-1,Hit.HitLook.MELEE_DAMAGE));
        victim.getNextHits().add(new Hit(attacker,1,Hit.HitLook.MELEE_DAMAGE,1));
        victim.getNextHits().add(new Hit(attacker,1,Hit.HitLook.REGULAR_DAMAGE));
        Hit critical=new Hit(attacker,1,Hit.HitLook.MELEE_DAMAGE);critical.setCriticalMark();victim.getNextHits().add(critical);
        Hit special=new Hit(attacker,1,Hit.HitLook.MELEE_DAMAGE);special.setSpecial(true);victim.getNextHits().add(special);
        Hit soaked=new Hit(attacker,1,Hit.HitLook.MELEE_DAMAGE);soaked.setSoaking(new Hit(1,Hit.HitLook.ABSORB_DAMAGE));victim.getNextHits().add(soaked);
        Native950Hits.Snapshot hits=Native950Hits.from(victim);
        assertEquals(2,hits.size());assertEquals(7,hits.refusals());
        assertArrayEquals(hex("40 82 80 85 ff f8 00 80 86 0a 00 00"),playerBytes(hits,1));
    }
    @Test public void hitListLimitAndAbsentRunningCacheRemainBounded(){
        for(int i=0;i<256;i++)victim.getNextHits().add(new Hit(attacker,1,Hit.HitLook.MELEE_DAMAGE));
        Native950Hits.Snapshot hits=Native950Hits.from(victim);
        assertEquals(255,hits.size());assertEquals(1,hits.refusals());
        // Normal unit process has no cache; runtime bridge must not treat pure conversion as verification.
        if(com.rs.cache.Cache.STORE==null){assertEquals(0,Native950Hits.fromRunningCache(victim).size());assertEquals(256,Native950Hits.fromRunningCache(victim).refusals());}
    }
    @Test public void actualPlayerInfoPassesTheRecipientToTheMaskSource(){
        victim.getNextHits().add(new Hit(attacker,50,Hit.HitLook.MELEE_DAMAGE));
        final Native950Hits.Snapshot hits=Native950Hits.from(victim);
        final int[] recipient={-1};
        Native950PlayerInfo.MaskSource source=new Native950PlayerInfo.MaskSource(){
            @Override public Native950PlayerMasks.Builder masks(){throw new AssertionError("Recipient was lost");}
            @Override public Native950PlayerMasks.Builder masks(int viewer){recipient[0]=viewer;return Native950PlayerMasks.builder().hits(hits.playerHits(viewer),Collections.emptyList());}
        };
        Native950PlayerInfo.Actor[] world=new Native950PlayerInfo.Actor[2048];
        world[2]=Native950PlayerInfo.Actor.builder(2,3200,3200,0).masks(source).build();
        world[3]=Native950PlayerInfo.Actor.builder(3,3201,3200,0).build();
        for(int viewer:new int[]{2,3}){
            Native950PlayerInfo.ViewState view=new Native950PlayerInfo.ViewState(viewer);
            Native950PlayerInfo.initialScene(view,world,7,0,0,0);
            byte[] packet=Native950PlayerInfo.frame(view,world).payload();
            assertEquals(viewer,recipient[0]);
            byte[] expected=hex(viewer==2?"40 81 80 85 81 f4 00 00":"40 81 80 96 81 f4 00 00");
            assertArrayEquals(expected,Arrays.copyOfRange(packet,packet.length-expected.length,packet.length));
        }
    }
    @Test public void rangedMagicAndCriticalHitsUseDistinctPinned950Wrappers(){
        Hit range=new Hit(attacker,12,Hit.HitLook.RANGE_DAMAGE);
        Hit magic=new Hit(attacker,13,Hit.HitLook.MAGIC_DAMAGE);
        Hit rangeCrit=new Hit(attacker,14,Hit.HitLook.RANGE_DAMAGE);rangeCrit.setCriticalMark();
        Hit magicCrit=new Hit(attacker,15,Hit.HitLook.MAGIC_DAMAGE);magicCrit.setCriticalMark();
        victim.getNextHits().add(range);victim.getNextHits().add(magic);
        victim.getNextHits().add(rangeCrit);victim.getNextHits().add(magicCrit);
        Native950Hits.Snapshot hits=Native950Hits.from(victim);
        assertEquals(4,hits.size());assertEquals(0,hits.refusals());
        assertArrayEquals(hex("40 84 80 88 78 00 80 8b 80 82 00 80 89 80 8c 00 80 8c 80 96 00 00"),playerBytes(hits,1));
        assertArrayEquals(hex("40 84 80 99 78 00 80 9c 80 82 00 80 9a 80 8c 00 80 9d 80 96 00 00"),playerBytes(hits,3));
    }
    @Test public void boundedWeaponCatalogUses950MappedSequencesAndDocumentedRs2Cadence(){
        assertTrue(Native950CombatAnimations.supportsWeapon(-1));
        assertFalse(Native950CombatAnimations.supportsWeapon(4151));
        assertEquals(37378,Native950CombatAnimations.attackAnimation(1277));
        assertEquals(37385,Native950CombatAnimations.attackAnimation(1291));
        assertEquals(37385,Native950CombatAnimations.attackAnimation(1321));
        assertEquals(18292,Native950CombatAnimations.blockAnimation(1277));
        assertEquals(5,Native950CombatAnimations.attackStyle(1277));
        assertEquals(6,Native950CombatAnimations.attackStyle(1291));
        assertEquals(7,Native950CombatAnimations.attackStyle(-1));
        assertEquals(5,Native950CombatAnimations.attackSpeed(1291));
        assertEquals(4,Native950CombatAnimations.attackSpeed(1321));
        assertEquals(153,Native950CombatAnimations.durationCycles(5389));
        assertEquals(90,Native950CombatAnimations.durationCycles(6182));
        assertFalse(Native950CombatAnimations.acceptedFromRunningCache(999999));
        try{Native950CombatAnimations.attackAnimation(4151);fail();}catch(IllegalArgumentException expected){}
    }
    private static byte[] playerBytes(Native950Hits.Snapshot hits,int viewer){return Native950PlayerMasks.encode(Native950PlayerMasks.builder().hits(hits.playerHits(viewer),Collections.emptyList()).build());}
    private static byte[] npcBytes(Native950Hits.Snapshot hits,int viewer){return Native950NpcMasks.maskBlock(new Native950NpcMasks.Update().hits(hits.npcHits(viewer).toArray(new Native950NpcMasks.Hit[0]),new Native950NpcMasks.Hitbar[0]));}
    private static byte[] hex(String text){String s=text.replace(" ","");byte[] bytes=new byte[s.length()/2];for(int i=0;i<bytes.length;i++)bytes[i]=(byte)Integer.parseInt(s.substring(i*2,i*2+2),16);return bytes;}
}
