package com.rs.game.player.client;

import com.rs.game.WorldTile;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import io.netty.channel.embedded.EmbeddedChannel;
import java.net.*;
import org.junit.*;
import static org.junit.Assert.*;

public class Native950AdminCommandsTest {
    private String oldEnabled, oldAccounts;
    private EmbeddedChannel channel;
    private Player p;
    @Before public void setup() {
        oldEnabled=System.getProperty(Native950DevelopmentCommands.PROPERTY);
        oldAccounts=System.getProperty(Native950AdminCommands.ACCOUNTS);
        System.setProperty(Native950DevelopmentCommands.PROPERTY,"true");
        System.setProperty(Native950AdminCommands.ACCOUNTS,"tester");
        channel=new EmbeddedChannel(){@Override protected SocketAddress remoteAddress0(){return new InetSocketAddress("127.0.0.1",43650);}};
        p=Player.createNative950("tester",new WorldTile(3217,3258,0),channel);p.setActive(true);
    }
    @After public void cleanup() {
        channel.finishAndReleaseAll(); restore(Native950DevelopmentCommands.PROPERTY,oldEnabled);
        restore(Native950AdminCommands.ACCOUNTS,oldAccounts);
    }
    private static void restore(String key,String value){if(value==null)System.clearProperty(key);else System.setProperty(key,value);}
    private void run(String text){Native950DevelopmentCommands.handle(p,channel,text);}
    @Test public void almightyTogglesAllModesAndRunConsumptionResumes() {
        run(";;almighty");assertTrue(p.isDevelopmentGodMode());assertTrue(p.getPrayer().isInfinitePrayer());
        assertTrue(p.getCombatDefinitions().isInfiniteAdrenaline());assertTrue(p.isInfiniteRunEnergy());
        assertTrue(p.isInfiniteCombatRunes());assertTrue(p.isInfiniteAmmunition());
        p.setRunEnergy(0);assertEquals(100,p.getRunEnergy());
        run(";;almighty");assertFalse(p.isDevelopmentGodMode());assertFalse(p.getPrayer().isInfinitePrayer());
        assertFalse(p.getCombatDefinitions().isInfiniteAdrenaline());assertFalse(p.isInfiniteRunEnergy());
        assertFalse(p.isInfiniteCombatRunes());assertFalse(p.isInfiniteAmmunition());
        p.setRunEnergy(75);assertEquals(75,p.getRunEnergy());
    }
    @Test public void allDirectoryPagesFitPacketLimitsAndBadArgumentsDoNotToggle() {
        for(int page=1;page<=4;page++)run(";;commands "+page);
        run(";;commands");run(";;commands 0");run(";;commands rubbish");run(";;commands 2 3");
        run(";;almighty nope");assertFalse(p.isDevelopmentGodMode());
        System.clearProperty(Native950AdminCommands.ACCOUNTS);run(";;almighty");assertFalse(p.isInfiniteCombatRunes());
    }
    @Test public void individualSupplyModesToggleIndependently() {
        run(";;infrunes");assertTrue(p.isInfiniteCombatRunes());assertFalse(p.isInfiniteAmmunition());
        run(";;infrunes");assertFalse(p.isInfiniteCombatRunes());
        run(";;infammo");assertTrue(p.isInfiniteAmmunition());run(";;infammo");assertFalse(p.isInfiniteAmmunition());
        run(";;infrun");assertTrue(p.isInfiniteRunEnergy());run(";;infrun");assertFalse(p.isInfiniteRunEnergy());
    }
    @Test public void actionBarAndRevolutionControlsUseTheSavedNativeState() {
        run(";;bar");assertEquals(0,p.getNative950ActionBar().activeBar());
        run(";;bar 3");assertEquals(2,p.getNative950ActionBar().activeBar());
        run(";;bar 0");run(";;bar nope");assertEquals(2,p.getNative950ActionBar().activeBar());
        run(";;revo");assertTrue(p.getNative950ActionBar().isRevolutionEnabled());
        run(";;revolution");assertFalse(p.getNative950ActionBar().isRevolutionEnabled());
    }
    @Test public void permissionRequiresBothLocalDevelopmentAndAccountGrant() {
        System.clearProperty(Native950AdminCommands.ACCOUNTS);run(";;god");assertFalse(p.isDevelopmentGodMode());
        p.setRights(1);run(";;god");assertFalse(p.isDevelopmentGodMode());
        p.setRights(2);run(";;god");assertTrue(p.isDevelopmentGodMode());run(";;god");
        EmbeddedChannel remote=new EmbeddedChannel(){@Override protected SocketAddress remoteAddress0(){return new InetSocketAddress("192.0.2.1",43650);}};
        try { Native950DevelopmentCommands.handle(p,remote,";;god");assertFalse(p.isDevelopmentGodMode()); }
        finally { remote.finishAndReleaseAll(); }
        System.setProperty(Native950DevelopmentCommands.PROPERTY,"false");run(";;god");assertFalse(p.isDevelopmentGodMode());
    }
    @Test public void prayerToggleCoversAllDrainApisAndResumes() {
        run(";;infprayer");int full=p.getPrayer().getPrayerpoints();
        p.getPrayer().drainPrayer(5,true);p.getPrayer().drainPrayer();p.getPrayer().setPrayerpoints(0);
        assertEquals(full,p.getPrayer().getPrayerpoints());run(";;infprayer");
        p.getPrayer().drainPrayer(1,true);assertEquals(full-1,p.getPrayer().getPrayerpoints());
        p.getPrayer().drainPrayer();assertEquals(0,p.getPrayer().getPrayerpoints());
    }
    @Test public void adrenalineUsesRealResourceAndStopsSuppressingCostsWhenDisabled() {
        run(";;adrenaline 25");assertEquals(25,p.getCombatDefinitions().getSpecialAttackPercentage());
        run(";;infadren");p.getCombatDefinitions().decreaseSpecialAttack(50);
        p.getCombatDefinitions().setSpecialAttack(0);p.getCombatDefinitions().setSpecialAttackPercentage(0);
        assertEquals(100,p.getCombatDefinitions().getSpecialAttackPercentage());
        run(";;infadren");p.getCombatDefinitions().decreaseSpecialAttack(25);
        assertEquals(75,p.getCombatDefinitions().getSpecialAttackPercentage());
        run(";;adrenaline 0");assertEquals(0,p.getCombatDefinitions().getSpecialAttackPercentage());
    }
    @Test public void godIsIndependentAndProtectsDirectHpChanges() {
        run(";;god");p.setHitpoints(0);assertFalse(p.isDead());
        assertFalse(p.getPrayer().isInfinitePrayer());assertFalse(p.getCombatDefinitions().isInfiniteAdrenaline());
        run(";;god");p.setHitpoints(1);assertEquals(1,p.getHitpoints());
        p.setInvulnerable(true);run(";;god");run(";;god");assertTrue(p.isInvulnerable());
    }
    @Test public void malformedCommandsDoNotChangeStateAndHelpFitsPackets() {
        run(";;adrenaline 25");
        for(String text:new String[]{";;god nope",";;infprayer off",";;infadren yes",";;adrenaline -1",";;adrenaline 101",";;adrenaline x",";;adrenaline 50 extra"})run(text);
        assertEquals(25,p.getCombatDefinitions().getSpecialAttackPercentage());assertFalse(p.isDevelopmentGodMode());
        assertFalse(p.getPrayer().isInfinitePrayer());assertFalse(p.getCombatDefinitions().isInfiniteAdrenaline());
        run(";;devhelp");run(";;nxt");run(";;devstatus");run(";;coords");
    }
    @Test public void maxRespectsEveryCacheCapAndNeverLowersEarnedXp() {
        p.getSkills().setXpWithoutRefresh(Skills.ATTACK,Skills.MAXIMUM_EXP);run(";;max");
        for(int skill=0;skill<Skills.SKILL_COUNT;skill++)assertEquals(Skills.getLevelCap(skill),p.getSkills().getLevelForXp(skill));
        assertEquals(Skills.MAXIMUM_EXP,p.getSkills().getXp(Skills.ATTACK),0);
        assertEquals(p.getMaxHitpoints(),p.getHitpoints());
    }
    @Test public void deadAndLockedPlayersCannotUseCommandsToEscapeRecovery() {
        p.lock();run(";;god");assertFalse(p.isDevelopmentGodMode());p.unlock();
        p.setHitpoints(0);run(";;heal");run(";;god");assertTrue(p.isDead());assertFalse(p.isDevelopmentGodMode());
    }
}
