package com.rs.game.player.client;

import org.junit.Test;
import java.util.HashMap;
import java.util.Map;
import static org.junit.Assert.*;

public class Native950AbilityCoverageTest {
    @Test public void inventoryRequiresTypedAbilityShapeRatherThanAnArbitraryNamedStruct() {
        Map<Long,Object> values = new HashMap<>();
        assertFalse(Native950AbilityCoverage.abilityShaped(null));
        values.put(2794L,"Example"); assertFalse(Native950AbilityCoverage.abilityShaped(values));
        values.put(2793L,1); values.put(2799L,1); values.put(2796L,5);
        assertTrue(Native950AbilityCoverage.abilityShaped(values));
        values.put(2793L,"1"); assertFalse(Native950AbilityCoverage.abilityShaped(values));
        values.put(2793L,1); values.put(2794L," "); assertFalse(Native950AbilityCoverage.abilityShaped(values));
    }
}
