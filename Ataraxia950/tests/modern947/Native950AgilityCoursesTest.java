package com.rs.game.player.client;

import com.rs.game.WorldTile;
import com.rs.game.player.Player;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;

public final class Native950AgilityCoursesTest {
    private final EmbeddedChannel channel=new EmbeddedChannel();
    private final Player player=Player.createNative950("agilitytest",new WorldTile(2532,3546,0),channel);
    @After public void cleanup(){channel.finishAndReleaseAll();}
    @Test public void barbarianBonusRequiresBothOrderedWallsAndIsAwardedOnce() {
        int course=Native950AgilityCourses.BARBARIAN,laps=player.getLapsRan();
        double xp=0;for(int stage=0;stage<4;stage++)xp+=Native950AgilityCourses.complete(player,course,stage);
        assertEquals(320,xp,0);assertEquals(3,Native950AgilityCourses.stage(player,course));
        assertEquals(0,Native950AgilityCourses.complete(player,course,-2),0);
        assertEquals(3,Native950AgilityCourses.stage(player,course));
        assertEquals(80,Native950AgilityCourses.complete(player,course,4),0);
        assertEquals(laps,player.getLapsRan());
        assertEquals(380,Native950AgilityCourses.complete(player,course,5),0);
        assertEquals(laps+1,player.getLapsRan());assertEquals(-1,Native950AgilityCourses.stage(player,course));
        assertEquals(80,Native950AgilityCourses.complete(player,course,5),0);
        assertEquals(laps+1,player.getLapsRan());
    }
    @Test public void skippedOrRepeatedBarbarianObstacleCannotCarryLapCredit() {
        int course=Native950AgilityCourses.BARBARIAN,laps=player.getLapsRan();
        Native950AgilityCourses.complete(player,course,0);Native950AgilityCourses.complete(player,course,2);
        assertEquals(-1,Native950AgilityCourses.stage(player,course));
        for(int stage=3;stage<=5;stage++)Native950AgilityCourses.complete(player,course,stage);
        assertEquals(laps,player.getLapsRan());
        for(int stage=0;stage<=4;stage++)Native950AgilityCourses.complete(player,course,stage);
        Native950AgilityCourses.complete(player,course,4);Native950AgilityCourses.complete(player,course,5);
        assertEquals(laps,player.getLapsRan());
    }
    @Test public void wildernessRetainsOriginalFourObstacleAndCompletionRewards() {
        int course=Native950AgilityCourses.WILDERNESS,laps=player.getLapsRan();
        double xp=0;for(int stage=0;stage<5;stage++)xp+=Native950AgilityCourses.complete(player,course,stage);
        assertEquals(2880,xp,0);assertEquals(laps+1,player.getLapsRan());
        assertEquals(0,Native950AgilityCourses.complete(player,course,4),0);
        assertEquals(laps+1,player.getLapsRan());
    }
    @Test public void reversePassageOrInterruptedCourseRetiresOnlyThatCourseProgress() {
        Native950AgilityCourses.stage(player,Native950AgilityCourses.BARBARIAN,4);
        Native950AgilityCourses.stage(player,Native950AgilityCourses.WILDERNESS,3);
        assertEquals(0,Native950AgilityCourses.complete(player,Native950AgilityCourses.WILDERNESS,-1),0);
        assertEquals(-1,Native950AgilityCourses.stage(player,Native950AgilityCourses.WILDERNESS));
        assertEquals(4,Native950AgilityCourses.stage(player,Native950AgilityCourses.BARBARIAN));
        Native950AgilityCourses.clear(player,Native950AgilityCourses.BARBARIAN);
        assertEquals(80,Native950AgilityCourses.complete(player,Native950AgilityCourses.BARBARIAN,5),0);
        assertEquals(0,player.getLapsRan());
    }
}