package com.rs.game.player.client;
import org.junit.Test;
import static org.junit.Assert.*;
import java.util.*;
public class Native950BossCatalogueTest {
    @Test public void curatedSearchKeepsUnsupportedBossesHonestAndActionsUseDispatcher(){
        assertEquals(3,Native950BossCatalogue.search("dagannoth").size());
        assertEquals(1,Native950BossCatalogue.search("6260").size());
        assertEquals("COMBAT BLOCKED",Native950BossCatalogue.find(50).status);
        assertEquals(-1,Native950BossCatalogue.find(50).route);
        assertEquals("PARTIAL",Native950BossCatalogue.find(6260).status);
        assertArrayEquals(new int[]{6261,6263,6265},Native950BossCatalogue.find(6260).minions);
        assertEquals(";;bossfight 6260",Native950DeveloperActions.find("bossfight").command(Arrays.asList("6260")));
        assertTrue(Native950DeveloperActions.find("bossfight").confirmation);
        assertTrue(Native950DevelopmentCommands.preservesGameplay(";;bossinfo 6260"));
        assertFalse(Native950DevelopmentCommands.preservesGameplay(";;bossfight 6260"));
    }
}
