package com.rs.game.player.client;

import com.rs.Settings;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.network.modern.Native950GameTransport;
import com.rs.network.protocol.modern950.Native950Protocol;
import io.netty.buffer.ByteBuf;
import io.netty.channel.embedded.EmbeddedChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/** Real original CombatDefinitions/Skills awards and native encrypted packet frames. */
public class Native950CombatExperienceTest {
    private EmbeddedChannel channel;
    private Player player;
    private NPC target;
    private boolean strict;
    @Before public void setup() {
        strict = Native950PacketDispatcher.isStrict();
        Native950PacketDispatcher.setStrict(true);
        Native950IdMap.reset();
        channel = new EmbeddedChannel(new Native950GameTransport(() -> 0, () -> 0, Thread.currentThread()));
        player = Player.createNative950("xp-test", new WorldTile(3217,3258,0), channel);
        target = NPC.createNative950(12353, new WorldTile(3218,3258,0), 1);
        drain();
    }
    @After public void cleanup() {
        Native950PacketDispatcher.setStrict(strict);
        Native950IdMap.reset();
        channel.finishAndReleaseAll();
    }
    private double rate() { return Settings.IRONMAN_XP * 7.0; }
    @Test public void actualDamageReusesTheOriginalSharedMeleeAndConstitutionRatios() {
        double before = player.getSkills().getXp(Skills.HITPOINTS);
        Native950CombatExperience.awardMelee(player, target, 10);
        for(int skill : new int[]{Skills.ATTACK,Skills.STRENGTH,Skills.DEFENCE})
            assertEquals((10 / 2.5) / 3 * rate(),player.getSkills().getXp(skill),1e-9);
        assertEquals((10 / 7.5) * rate(),player.getSkills().getXp(Skills.HITPOINTS)-before,1e-9);
        List<byte[]> frames = drain();
        assertEquals("one real stat update for each original selected skill",4,count(frames,Native950Protocol.ServerPacket.UPDATE_STAT));
        for(byte[] frame:frames)if((frame[0]&255)==Native950Protocol.ServerPacket.UPDATE_STAT.opcode()) {
            int skill=-frame[1]&255;
            int xp=((frame[3]&255)<<24)|((frame[4]&255)<<16)|((frame[5]&255)<<8)|(frame[6]&255);
            assertEquals("wire skill experience is the authoritative Skills total",(int)player.getSkills().getXp(skill),xp);
        }
        assertEquals("no guessed explicit XP script",0,count(frames,Native950Protocol.ServerPacket.RUNCLIENTSCRIPT));
    }
    @Test public void originalSelectionArrayControlsTheSplitWithoutANewStyleSystem() {
        boolean[] selected=player.getCombatDefinitions().getMeleeCombatExperienceGain();
        Arrays.fill(selected,false);selected[0]=true;
        Native950CombatExperience.awardMelee(player,target,3);
        assertEquals((3/2.5)*rate(),player.getSkills().getXp(Skills.ATTACK),1e-9);
        assertEquals(0,player.getSkills().getXp(Skills.STRENGTH),0);
        assertEquals(0,player.getSkills().getXp(Skills.DEFENCE),0);
    }
    @Test public void noSelectedMeleeSkillStillGrantsTheOriginalConstitutionAward() {
        Arrays.fill(player.getCombatDefinitions().getMeleeCombatExperienceGain(),false);
        double before=player.getSkills().getXp(Skills.HITPOINTS);
        Native950CombatExperience.awardMelee(player,target,10);
        assertEquals((10/7.5)*rate(),player.getSkills().getXp(Skills.HITPOINTS)-before,1e-9);
        assertEquals(0,player.getSkills().getXp(Skills.ATTACK),0);
    }
    @Test public void missesInvalidDamageAndXpLockProduceNoExperienceOrDropFrames() {
        double[] before=player.getSkills().getXpCopy();
        Native950CombatExperience.awardMelee(player,target,0);
        Native950CombatExperience.awardMelee(player,target,-1);
        player.setXpLocked(true);
        Native950CombatExperience.awardMelee(player,target,10);
        assertArrayEquals(before,player.getSkills().getXpCopy(),0);
        assertTrue(drain().isEmpty());
    }
    @Test public void combatLevelUpsUseExistingLevelsAndNeverEnterLegacyQuestOrPetManagers() {
        Skills skills=player.getSkills();skills.setXpWithoutRefresh(Skills.ATTACK,82);
        skills.addXp(Skills.ATTACK,1);
        assertEquals(2,skills.getLevelForXp(Skills.ATTACK));
        assertEquals(2,skills.getLevel(Skills.ATTACK));
        assertEquals(82+rate(),skills.getXp(Skills.ATTACK),1e-9);
        assertEquals(1,count(drain(),Native950Protocol.ServerPacket.UPDATE_STAT));
    }
    @Test public void experienceCapRetainsOnlyTheRealGainInTheExistingTracker() {
        Skills skills=player.getSkills();skills.setXpWithoutRefresh(Skills.ATTACK,Skills.MAXIMUM_EXP-1);
        skills.setLevelWithoutRefresh(Skills.ATTACK,skills.getLevelForXp(Skills.ATTACK));
        double tracked=skills.getXpTracksRaw()[0];
        assertEquals(1,skills.addXp(Skills.ATTACK,100),0);
        assertEquals(Skills.MAXIMUM_EXP,skills.getXp(Skills.ATTACK),0);
        assertEquals(tracked+1,skills.getXpTracksRaw()[0],0);
        assertEquals(0,skills.addXp(Skills.ATTACK,100),0);
    }
    @Test public void constitutionLevelUpUsesExistingHealingAndNativeVitalRefresh() {
        Skills skills=player.getSkills();skills.setXpWithoutRefresh(Skills.HITPOINTS,Skills.getXPForLevel(Skills.HITPOINTS,11)-1);
        player.setHitpoints(50);
        skills.addXp(Skills.HITPOINTS,1);
        assertEquals(11,skills.getLevel(Skills.HITPOINTS));
        assertEquals(60,player.getHitpoints());
        assertEquals(110,player.getMaxHitpoints());
        assertEquals(1,count(drain(),Native950Protocol.ServerPacket.UPDATE_STAT));
    }
    @Test public void popupBootstrapUsesCacheSlotAndOrdinaryStatsRatherThanFakeXpMessages() {
        Native950XpDrops.emit(player);
        List<byte[]> frames=drain();
        assertEquals(1,count(frames,Native950Protocol.ServerPacket.IF_OPENSUB));
        assertEquals(4,count(frames,Native950Protocol.ServerPacket.IF_SETHIDE));
        assertEquals(1,count(frames,Native950Protocol.ServerPacket.VARBIT_LARGE));
        assertEquals(0,count(frames,Native950Protocol.ServerPacket.MESSAGE_GAME));
        List<byte[]> scripts=new ArrayList<>();
        for(byte[] frame:frames)if((frame[0]&255)==Native950Protocol.ServerPacket.RUNCLIENTSCRIPT.opcode())scripts.add(frame);
        assertEquals("only verified layout scripts run during bootstrap",5,scripts.size());
        int wrapper=(1477<<16)|666;
        assertScript(scripts.get(0),11145,173,114,0,0,wrapper);
        assertScript(scripts.get(1),13268,374,46,0,0,wrapper);
        assertScript(scripts.get(2),2330,wrapper);
        assertScript(scripts.get(3),8707,1026);
        assertScript(scripts.get(4),8708,1026,8);
    }
    private static void assertScript(byte[] frame,int id,int... values) {
        int position=3; // opcode35 has a u16 length prefix in950.
        for(int ignored:values)assertEquals('i',frame[position++]);
        assertEquals(0,frame[position++]);
        for(int i=values.length-1;i>=0;i--) {
            assertEquals("script argument"+i,values[i],intAt(frame,position));position+=4;
        }
        assertEquals(id,intAt(frame,position));
        assertEquals("no extra XP-award arguments",frame.length,position+4);
    }
    private static int intAt(byte[] bytes,int at) {
        return ((bytes[at]&255)<<24)|((bytes[at+1]&255)<<16)|((bytes[at+2]&255)<<8)|(bytes[at+3]&255);
    }
    private List<byte[]> drain() {
        channel.flush(); // Match the live world's end-of-tick flush before inspecting wire frames.
        List<byte[]> frames=new ArrayList<>();Object message;
        while((message=channel.readOutbound())!=null) {
            if(message instanceof ByteBuf) {
                ByteBuf b=(ByteBuf)message;
                try {
                    byte[] data=new byte[b.readableBytes()];b.readBytes(data);
                    int cursor=0;
                    while(cursor<data.length) {
                        int start=cursor,opcode=data[cursor++]&255,size=Integer.MIN_VALUE;
                        for(Native950Protocol.ServerPacket packet:Native950Protocol.ServerPacket.values())
                            if(packet.opcode()==opcode){size=packet.size();break;}
                        assertNotEquals("unrecognized native opcode",Integer.MIN_VALUE,size);
                        if(size==-1)size=data[cursor++]&255;
                        else if(size==-2){size=((data[cursor]&255)<<8)|(data[cursor+1]&255);cursor+=2;}
                        assertTrue("complete framed native packet",cursor+size<=data.length);
                        cursor+=size;frames.add(Arrays.copyOfRange(data,start,cursor));
                    }
                }
                finally {b.release();}
            }
        }
        return frames;
    }
    private static int count(List<byte[]> frames,Native950Protocol.ServerPacket packet) {
        int count=0;for(byte[] frame:frames)if((frame[0]&255)==packet.opcode())count++;return count;
    }
}
