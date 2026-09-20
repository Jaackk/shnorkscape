package com.rs.game.player.client;

import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.NPCCombatDefinition;
import com.rs.game.player.Player;
import com.rs.utils.data.parsers.npcs.pojos.NPCStats;
import com.rs.network.protocol.modern950.Native950NpcMasks;
import io.netty.channel.embedded.EmbeddedChannel;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.Test;
import static org.junit.Assert.*;

/** Independent captured 950 NPC files and authored legacy rows; no live server or fake cache. */
public final class Native950NpcCombatProfileTest {
    private static final String CHICKEN="1f41747461636b007f0c860c016100506200506419800064195f000102436869636b656e00010180017b423c018001dc46f90b00000b210000005a00000b220000004600000b230000003c00000b24000000320000001a000000010000000e0000000300000281000000960000001d0000000a00000b310000006e00000b20000000040000003200000005770389002a00";
    private static final String GOBLIN="7f00560c011f41747461636b0062007a61007a640f5f000202476f626c696e00010680012674800126958001264c8001267a80012656800126643c01800131a7f90b00000b210000005a00000b220000004600000b230000003c00000b24000000320000001a000000010000000e0000000400000281000001900000001d0000001400000b310000007800000b20000000010000003200000002770389002a00";

    @Test public void capturesAuthoredChickenStatsAndActual950Ratings() {
        Native950NpcCombatProfile p=chicken();
        assertEquals(41,p.npcId);assertEquals("Chicken",p.name);assertEquals(1,p.size);assertEquals(1,p.combatLevel);
        assertEquals(30,p.hp);assertEquals(1,p.attackLevel);assertEquals(1,p.defenceLevel);
        assertEquals("maxHit stays in engine units",15,p.maxHit);
        assertEquals("cache param14 takes precedence over authored5",3,p.attackSpeed);
        assertEquals(1,p.deathTicks);assertEquals(60,p.respawnTicks);
        assertEquals(5387,p.attackAnim);assertEquals(5388,p.blockAnim);assertEquals(5389,p.deathAnim);
        assertEquals(1,p.meleeAttackBonus);assertEquals(11,p.meleeDefenceBonus);
        assertTrue(p.cacheAttackSpeed);assertTrue(p.cacheAttackBonus);assertTrue(p.cacheDefenceBonus);
    }

    @Test public void capturesAuthoredGoblinStatsWithoutInventedStrengthLevel() {
        Native950NpcCombatProfile p=Native950NpcCombatCatalog.fromDefinitions(12353,hex(GOBLIN),goblinRow(),stats(8),id->id==6182||id==6183||id==6184);
        assertEquals(50,p.hp);assertEquals(8,p.attackLevel);assertEquals(8,p.defenceLevel);
        assertEquals(16,p.maxHit);assertEquals(2,p.combatLevel);assertEquals(4,p.attackSpeed);
        assertEquals(6184,p.attackAnim);assertEquals(6183,p.blockAnim);assertEquals(6182,p.deathAnim);
        assertEquals(2,p.meleeAttackBonus);assertEquals(12,p.meleeDefenceBonus);
    }

    @Test public void arbitraryIdentitySafeNpcUsesTheSameResolverWithoutIdBranches() {
        Native950NpcCombatCatalog.Resolution result=resolve(765432,hex(CHICKEN),true,chickenRow(),stats(1));
        assertNotNull(result.profile);assertNull(result.reason);
        assertEquals(765432,result.profile.npcId);assertEquals("Chicken",result.profile.name);
        Native950NpcCombatProfile custom=accepted(definition("Custom creature",2,12,4,params(14,6,29,37,2865,85),false));
        assertEquals("Custom creature",custom.name);assertEquals(2,custom.size);assertEquals(12,custom.combatLevel);
        assertEquals(6,custom.attackSpeed);assertEquals(4,custom.meleeAttackBonus);assertEquals(9,custom.meleeDefenceBonus);
    }

    @Test public void absentMalformedAndRepurposedDefinitionsAreRefused() {
        rejected(resolve(765432,null,true,chickenRow(),stats(1)),"missing NPC definition");
        rejected(resolve(765432,hex("02 43"),true,chickenRow(),stats(1)),"undecodable NPC definition");
        rejected(resolve(765432,hex(CHICKEN),false,chickenRow(),stats(1)),"unverified legacy NPC identity");
        rejected(resolve(765432,hex(CHICKEN),true,null,stats(1)),"missing authored combat row");
        rejected(resolve(765432,hex(CHICKEN),true,chickenRow(),null),"missing authored stat row");
    }

    @Test public void actualAttackOptionIsRequiredAndMayOccupyAnyMenuSlot() {
        for(int slot=0;slot<5;slot++) assertNotNull(accepted(definition("Creature",1,1,slot,params(),false)));
        rejected(resolve(9001,definition("Cook",1,0,-1,params(),false),true,chickenRow(),stats(1)),"no Attack option");
    }

    @Test public void transformsAndInvalidMetadataAreRefused() {
        rejected(resolve(9001,definition("Creature",1,1,1,params(),true),true,chickenRow(),stats(1)),"transformed NPC requires a resolved combat form");
        for(int size:new int[]{0,65}) rejected(resolve(9001,definition("Creature",size,1,1,params(),false),true,chickenRow(),stats(1)),"invalid NPC combat metadata");
        for(String name:new String[]{"","null","  "}) rejected(resolve(9001,definition(name,1,1,1,params(),false),true,chickenRow(),stats(1)),"invalid NPC combat metadata");
        rejected(resolve(9001,definition("Creature",1,-1,1,params(),false),true,chickenRow(),stats(1)),"invalid NPC combat metadata");
        assertEquals(64,accepted(definition("Large creature",64,1,1,params(),false)).size);
    }

    @Test public void missingCacheFieldsUseLabeledAuthoredOrNeutralFallbacks() {
        Native950NpcCombatProfile p=accepted(definition("Creature",1,1,1,params(),false));
        assertEquals(5,p.attackSpeed);assertFalse(p.cacheAttackSpeed);
        assertEquals(0,p.meleeAttackBonus);assertFalse(p.cacheAttackBonus);
        assertEquals(0,p.meleeDefenceBonus);assertFalse(p.cacheDefenceBonus);
        p=accepted(definition("Creature",1,1,1,params(14,2,29,0,2865,0),false));
        assertTrue(p.cacheAttackSpeed);assertTrue(p.cacheAttackBonus);assertTrue(p.cacheDefenceBonus);
        assertEquals(0,p.meleeAttackBonus);assertEquals(0,p.meleeDefenceBonus);
    }

    @Test public void malformedPresentCacheParametersDoNotSilentlyFallBack() {
        for(int key:new int[]{14,29,2865}) {
            rejected(resolve(9001,definition("Creature",1,1,1,params(key,"bad"),false),true,chickenRow(),stats(1)),"invalid cache combat parameter");
            rejected(resolve(9001,definition("Creature",1,1,1,params(key,-1),false),true,chickenRow(),stats(1)),"invalid cache combat parameter");
        }
        rejected(resolve(9001,definition("Creature",1,1,1,params(14,0),false),true,chickenRow(),stats(1)),"invalid cache combat parameter");
        rejected(resolve(9001,definition("Creature",1,1,1,params(14,101),false),true,chickenRow(),stats(1)),"invalid cache combat parameter");
        rejected(resolve(9001,definition("Creature",1,1,1,params(29,10000001),false),true,chickenRow(),stats(1)),"invalid cache combat parameter");
    }

    @Test public void positiveAttackBindingMustBeVerifiedAndOptionalAnimationsCanBeOmitted() {
        rejected(Native950NpcCombatCatalog.resolve(41,hex(CHICKEN),true,chickenRow(),stats(1),id->id==5387?-1:60),"unverified attack animation binding");
        Native950NpcCombatProfile p=Native950NpcCombatCatalog.resolve(41,hex(CHICKEN),true,chickenRow(),stats(1),id->id==5387?60:-1).profile;
        assertNotNull(p);assertEquals(5387,p.attackAnim);assertEquals(-1,p.blockAnim);assertEquals(-1,p.deathAnim);assertEquals(0,p.deathAnimationTicks);
        NPCCombatDefinition row=new NPCCombatDefinition(30,422,-1,-1,5,1,60,15,"MELEE",-1,-1,"PASSIVE");
        p=resolve(9001,hex(CHICKEN),true,row,stats(1)).profile;
        assertNotNull(p);assertEquals(422,p.attackAnim);assertEquals(-1,p.blockAnim);assertEquals(-1,p.deathAnim);
    }

    @Test public void declaredAbsentAttackAnimationUsesCombatStatsWithoutSubstitution() {
        NPCCombatDefinition row=new NPCCombatDefinition(30,-1,-1,-1,5,1,60,15,"MELEE",-1,-1,"PASSIVE");
        Native950NpcCombatProfile p=Native950NpcCombatCatalog.resolve(9001,hex(CHICKEN),true,row,stats(1),id->{
            fail("Absent animation must not query a sequence binding");return -1;
        }).profile;
        assertNotNull(p);assertEquals(-1,p.attackAnim);assertEquals(-1,p.blockAnim);assertEquals(-1,p.deathAnim);
        assertEquals(30,p.hp);assertEquals(15,p.maxHit);assertEquals(3,p.attackSpeed);
        NPCCombatDefinition invalid=new NPCCombatDefinition(30,-2,-1,-1,5,1,60,15,"MELEE",-1,-1,"PASSIVE");
        assertNull("Only the authored -1 sentinel means absent",resolve(9001,hex(CHICKEN),true,invalid,stats(1)).profile);
    }

    @Test public void corpseDurationUsesVerifiedCyclesAndBoundsLongFinalHolds() {
        Native950NpcCombatProfile p=Native950NpcCombatCatalog.resolve(41,hex(CHICKEN),true,chickenRow(),stats(1),id->id==5389?153:30).profile;
        assertNotNull(p);assertEquals(6,p.deathAnimationTicks);
        p=Native950NpcCombatCatalog.resolve(41,hex(CHICKEN),true,chickenRow(),stats(1),id->id==5389?5119:30).profile;
        assertEquals(10,p.deathAnimationTicks);
    }

    @Test public void malformedAuthoredStatsAndFallbackTimingAreRefused() {
        NPCCombatDefinition timing=chickenRow();timing.setAttackDelay(0);
        rejected(resolve(9001,definition("Creature",1,1,1,params(),false),true,timing,stats(1)),"invalid cache combat parameter");
        NPCCombatDefinition missingHp=chickenRow();missingHp.setHitpoints(0);
        rejected(resolve(9001,hex(CHICKEN),true,missingHp,stats(1)),"invalid authored combat stats");
        rejected(resolve(9001,hex(CHICKEN),true,chickenRow(),stats(0)),"invalid authored combat stats");
        NPCCombatDefinition zeroRespawn=chickenRow();zeroRespawn.setRespawnDelay(0);
        rejected(resolve(9001,hex(CHICKEN),true,zeroRespawn,stats(1)),"invalid authored combat stats");
    }

    @Test public void specialRowsStayExplicitlyRefused() {
        for(String style:new String[]{"SPECIAL","SPECIAL2","UNKNOWN"}) {
            NPCCombatDefinition row=new NPCCombatDefinition(30,5387,5388,5389,5,1,60,15,style,-1,-1,"PASSIVE");
            rejected(resolve(41,hex(CHICKEN),true,row,stats(1)),"special attack style requires its own mechanics");
        }
    }

    @Test public void rangedAndMagicUseAuthoredStyleLevelsAndVerifiedEffects() {
        for(String style:new String[]{"RANGE","MAGE","MAGIC"}) {
            NPCCombatDefinition row=new NPCCombatDefinition(30,5387,5388,5389,5,1,60,15,style,99,100,"PASSIVE");
            NPCStats levels=new NPCStats(2,31,47,5,90,55,45,65);
            Native950NpcCombatProfile p=Native950NpcCombatCatalog.resolve(41,hex(CHICKEN),true,row,levels,id->60,id->id==99||id==100).profile;
            assertNotNull(p);assertEquals(style.equals("RANGE")?1:2,p.attackStyle);
            assertEquals(style.equals("RANGE")?levels.getRangeLevel():levels.getMagicLevel(),p.attackLevel);
            assertEquals(100,p.attackProjectile);assertEquals(99,p.attackGraphic);
            rejected(Native950NpcCombatCatalog.resolve(41,hex(CHICKEN),true,row,levels,id->60,id->false),"unverified ranged or magic effect binding");
        }
    }

    @Test public void nonMeleeAccuracyDoesNotReuseTheMeleeCacheRating() {
        for(String style:new String[]{"RANGE","MAGE"}) {
            NPCCombatDefinition row=new NPCCombatDefinition(30,-1,-1,-1,5,1,60,15,style,-1,-1,"PASSIVE");
            byte[] raw=definition("Creature",1,1,1,params(29,10,4,270,3,430),false);
            Native950NpcCombatProfile p=resolve(9001,raw,true,row,stats(1)).profile;
            assertNotNull(p);assertEquals(style.equals("RANGE")?27:43,p.meleeAttackBonus);
        }
    }

    @Test public void authoredAggressionDoesNotRequireLegacyCallbacks() {
        NPCCombatDefinition row=new NPCCombatDefinition(30,5387,5388,5389,5,1,60,15,"MELEE",99,100,"AGGRESSIVE");
        assertNotNull(resolve(41,hex(CHICKEN),true,row,stats(1)).profile);
    }

    @Test public void authoredZeroMaxHitSupportsNonDamagingTargets() {
        NPCCombatDefinition row=new NPCCombatDefinition(30,5387,5388,5389,5,1,60,0,"MELEE",-1,-1,"PASSIVE");
        Native950NpcCombatProfile p=resolve(41,hex(CHICKEN),true,row,stats(1)).profile;
        assertNotNull(p);assertEquals(0,p.maxHit);
    }

    @Test public void explicitProfileControlsHpWithoutOpeningLegacyCombat() {
        NPC npc=npc();assertEquals(1,npc.getHitpoints());assertEquals(1,npc.getMaxHitpoints());
        Native950NpcCombatProfile profile=chicken();npc.setNative950CombatProfile(profile);
        assertEquals(30,npc.getHitpoints());assertEquals(30,npc.getMaxHitpoints());assertEquals(1,npc.getCombatLevel());
        assertNull(npc.getCombat());
        npc.setHitpoints(7);npc.setNative950CombatProfile(profile);assertEquals("idempotent install must not heal",7,npc.getHitpoints());
        refused(()->npc.setNative950CombatProfile(chicken()));
        try {npc.getCombatDefinitions();fail("legacy combat escaped");}catch(UnsupportedOperationException expected) { }
        try {npc.processNPC();fail("legacy AI escaped");}catch(UnsupportedOperationException expected) { }
        assertNull(npc.getNative950MenuOption(0));assertNull(npc.getNative950MenuOption(6));
    }

    @Test public void nativeAttackerBookkeepingAndHealingNeedNoLegacyCombatInstance() {
        NPC npc=npc(),attacker=NPC.createNative950(494,new WorldTile(3202,3200,0),1);
        npc.setNative950CombatProfile(chicken());
        assertNull(npc.getCombat());
        npc.setAttackedBy(attacker);assertSame(attacker,npc.getAttackedBy());
        npc.setAttackedByDelay(1234L);npc.setAttackingDelay(5678L);
        assertEquals(1234L,npc.getAttackedByDelay());
        npc.setHitpoints(29);npc.heal(5);assertEquals(30,npc.getHitpoints());
        npc.setAttackedBy(null);assertNull(npc.getAttackedBy());
        npc.resetReceivedHits();npc.resetMasks();assertNull(npc.getCombat());
    }
    @Test public void encounterAndDeathFlagsPersistAcrossMaskReset() {
        NPC npc=npc();refused(()->npc.setNative950CombatEngaged(true));
        npc.setNative950CombatProfile(chicken());npc.setNative950CombatEngaged(true);
        refused(()->npc.setNative950DeathVisible(true));
        npc.setHitpoints(0);npc.setNative950DeathVisible(true);npc.resetMasks();
        assertTrue(npc.isDead());assertTrue(npc.isNative950CombatEngaged());assertTrue(npc.isUnderCombat());assertTrue(npc.isNative950DeathVisible());
        npc.setNative950DeathVisible(false);npc.setNative950CombatEngaged(false);npc.setHitpoints(npc.getMaxHitpoints());
        assertFalse(npc.isDead());assertFalse(npc.isNative950DeathVisible());assertFalse(npc.isNative950CombatEngaged());assertFalse(npc.isUnderCombat());
    }

    @Test public void hpZeroDeathRemainsVisibleForItsMaskThenHidesAndRespawns() {
        assertTrue(World.getNPCs().isEmpty());
        NPC npc=npc();npc.setNative950CombatProfile(chicken());World.addNative950Npc(npc);
        EmbeddedChannel channel=new EmbeddedChannel();Player viewer=Player.createNative950("npcdeath",new WorldTile(3200,3200,0),channel);
        AtomicBoolean death=new AtomicBoolean(false);
        Native950NpcViewport view=new Native950NpcViewport(24,250,(p,n,added)->death.get()?new Native950NpcMasks.Update().animation(5389,5389,5389,5389,0):null);
        try {
            view.frame(viewer,7,false,Arrays.asList(npc));
            npc.setHitpoints(0);npc.setNative950DeathVisible(true);death.set(true);
            assertArrayEquals(hex("01 9f ff e0 00 00 08 15 0d 15 0d 15 0d 15 0d 00"),view.frame(viewer,7,false,Arrays.asList(npc)).payload());
            assertArrayEquals(new int[]{npc.getIndex()},view.snapshot().indices);
            npc.setNative950DeathVisible(false);death.set(false);
            assertArrayEquals(hex("01 e0"),view.frame(viewer,7,false,Arrays.asList(npc)).payload());
            assertEquals(0,view.snapshot().indices.length);
            int sameIndex=npc.getIndex();npc.setHitpoints(npc.getMaxHitpoints());
            view.frame(viewer,7,false,Arrays.asList(npc));
            assertArrayEquals(new int[]{sameIndex},view.snapshot().indices);
        } finally {World.removeNative950Npc(npc);channel.finishAndReleaseAll();}
    }

    private static Native950NpcCombatCatalog.Resolution resolve(int id,byte[] raw,boolean safe,NPCCombatDefinition combat,NPCStats stats) {
        return Native950NpcCombatCatalog.resolve(id,raw,safe,combat,stats,sequence->60);
    }
    private static Native950NpcCombatProfile accepted(byte[] raw) {
        Native950NpcCombatCatalog.Resolution result=resolve(765432,raw,true,chickenRow(),stats(1));
        assertNull(result.reason);assertNotNull(result.profile);return result.profile;
    }
    private static void rejected(Native950NpcCombatCatalog.Resolution result,String reason) {
        assertNull(result.profile);assertEquals(reason,result.reason);
    }
    private static Map<Integer,Object> params(Object... pairs) {
        Map<Integer,Object> values=new LinkedHashMap<>();
        for(int i=0;i<pairs.length;i+=2)values.put((Integer)pairs[i],pairs[i+1]);
        return values;
    }
    /** Minimal independent NpcType encoding, including parameter types and variable Attack slots. */
    private static byte[] definition(String name,int size,int level,int attackSlot,Map<Integer,Object> params,boolean transform) {
        try {
            ByteArrayOutputStream bytes=new ByteArrayOutputStream();DataOutputStream out=new DataOutputStream(bytes);
            out.writeByte(2);out.writeBytes(name);out.writeByte(0);
            out.writeByte(12);out.writeByte(size);
            if(level>=0){out.writeByte(95);out.writeShort(level);}
            if(attackSlot>=0){out.writeByte(30+attackSlot);out.writeBytes("Attack");out.writeByte(0);}
            if(transform){out.writeByte(106);out.writeShort(65535);out.writeShort(65535);out.writeByte(0);out.writeShort(1);}
            if(!params.isEmpty()) {
                out.writeByte(249);out.writeByte(params.size());
                for(Map.Entry<Integer,Object> entry:params.entrySet()) {
                    boolean string=entry.getValue() instanceof String;out.writeByte(string?1:0);
                    int key=entry.getKey();out.writeByte(key>>>16);out.writeShort(key);
                    if(string){out.writeBytes((String)entry.getValue());out.writeByte(0);}else out.writeInt((Integer)entry.getValue());
                }
            }
            out.writeByte(0);return bytes.toByteArray();
        }catch(IOException impossible){throw new AssertionError(impossible);}
    }
    private static NPC npc(){return NPC.createNative950(41,new WorldTile(3201,3200,0),1);}
    private static NPCCombatDefinition chickenRow(){return new NPCCombatDefinition(30,5387,5388,5389,5,1,60,15,"MELEE",-1,-1,"PASSIVE");}
    private static NPCCombatDefinition goblinRow(){return new NPCCombatDefinition(50,6184,6183,6182,5,1,60,16,"MELEE",-1,-1,"PASSIVE");}
    private static NPCStats stats(int level){return new NPCStats(level,1,1,level,90,55,45,65);}
    private static Native950NpcCombatProfile chicken(){return Native950NpcCombatCatalog.fromDefinitions(41,hex(CHICKEN),chickenRow(),stats(1),id->id==5387||id==5388||id==5389);}
    private static void refused(Runnable operation){try{operation.run();fail("Unverified combat profile was accepted");}catch(IllegalStateException expected){}}
    private static byte[] hex(String text){String s=text.replace(" ","");byte[] bytes=new byte[s.length()/2];for(int i=0;i<bytes.length;i++)bytes[i]=(byte)Integer.parseInt(s.substring(i*2,i*2+2),16);return bytes;}
}
