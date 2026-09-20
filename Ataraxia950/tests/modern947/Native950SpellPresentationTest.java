package com.rs.game.player.client;

import org.junit.Test;
import java.util.HashMap;
import java.util.Map;
import static org.junit.Assert.*;

public class Native950SpellPresentationTest {
    @Test public void castingSequenceSurvivesMissingOptionalGraphicLayers() {
        Native950AutoSpells.Presentation p=Native950AutoSpells.presentation(123,null);
        assertEquals(123,p.animation); assertEquals(-1,p.projectile); assertEquals(-1,p.impact);
        assertFalse(p.available());
        Map<Integer,Object> params=new HashMap<>();params.put(2940,456);
        p=Native950AutoSpells.presentation(123,params);
        assertEquals(123,p.animation);assertEquals(456,p.projectile);assertEquals(-1,p.impact);
        assertFalse(p.available());
        params.put(2933,"not-an-integer");
        assertEquals(-1,Native950AutoSpells.presentation(123,params).impact);
        params.put(2933,789);
        p=Native950AutoSpells.presentation(123,params);
        assertTrue(p.available());assertEquals(789,p.impact);
    }
}
