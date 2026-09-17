package com.rs.game.player.client;
import com.rs.game.WorldTile;
import com.rs.game.player.*;
import io.netty.channel.embedded.EmbeddedChannel;
import java.net.*;
import org.junit.*;
import static org.junit.Assert.*;
public class Native950SkillLevelCommandTest {
 String previous;EmbeddedChannel channel;Player p;
 @Before public void setup(){previous=System.getProperty(Native950DevelopmentCommands.PROPERTY);System.setProperty(Native950DevelopmentCommands.PROPERTY,"true");
  channel=new EmbeddedChannel(){@Override protected SocketAddress remoteAddress0(){return new InetSocketAddress("127.0.0.1",43650);}};
  p=Player.createNative950("level-test",new WorldTile(3217,3258,0),channel);p.setActive(true);}
 @After public void close(){channel.finishAndReleaseAll();if(previous==null)System.clearProperty(Native950DevelopmentCommands.PROPERTY);else System.setProperty(Native950DevelopmentCommands.PROPERTY,previous);}
 @Test public void explicitLevelWritesMatchingXpOnlyForNamedSkill(){double other=p.getSkills().getXp(Skills.MINING);
  Native950DevelopmentCommands.handle(p,channel,";;nxt level 21 15");assertEquals(15,p.getSkills().getLevel(Skills.HUNTER));assertEquals(Skills.getXPForLevel(Skills.HUNTER,15),p.getSkills().getXp(Skills.HUNTER),0.01);assertEquals(other,p.getSkills().getXp(Skills.MINING),0.01);}
 @Test public void combatRequirementsCanBeTestedOnlyThroughExplicitDeveloperCommands(){
  double untouched=p.getSkills().getXp(Skills.ATTACK);
  Native950DevelopmentCommands.handle(p,channel,";;nxt level 6 99");assertEquals(99,p.getSkills().getLevelForXp(Skills.MAGIC));
  assertEquals(untouched,p.getSkills().getXp(Skills.ATTACK),0.01);
  Native950DevelopmentCommands.handle(p,channel,";;nxt level 28 120");assertEquals(120,p.getSkills().getLevelForXp(Skills.NECROMANCY));
 }
 @Test public void malformedAndDisabledRequestsDoNotChangeLevels(){for(String text:new String[]{";;nxt level",";;nxt level 21 0",";;nxt level 21 256",";;nxt level -1 99",";;nxt level 29 99",";;nxt level 999 1",";;nxt level 21 x"})Native950DevelopmentCommands.handle(p,channel,text);
  System.setProperty(Native950DevelopmentCommands.PROPERTY,"false");Native950DevelopmentCommands.handle(p,channel,";;nxt level 21 15");assertEquals(1,p.getSkills().getLevel(Skills.HUNTER));assertEquals(0,p.getSkills().getXp(Skills.HUNTER),0.01);}
}
