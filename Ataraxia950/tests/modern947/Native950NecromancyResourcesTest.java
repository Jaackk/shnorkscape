package com.rs.game.player.client;

import com.rs.game.WorldTile;
import com.rs.game.player.Player;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.Test;
import static org.junit.Assert.*;

public class Native950NecromancyResourcesTest {
    @Test public void resourcesPublishNativeAvailabilityWithoutLeakingBetweenPlayers(){
        EmbeddedChannel a=new EmbeddedChannel(),b=new EmbeddedChannel();
        Player first=Player.createNative950("first",new WorldTile(3200,3200,0),a);
        Player second=Player.createNative950("second",new WorldTile(3200,3201,0),b);
        Native950NecromancyResources state=new Native950NecromancyResources();
        try{
            assertEquals(60,state.fingerCost(first));assertNotNull(state.refusal(first,48299));
            for(int i=0;i<4;i++)state.cast(first,48296,i,false);
            assertEquals(12,state.necrosis(first));assertEquals(0,state.fingerCost(first));
            assertEquals(12,first.getVarsManager().getValue(10986));
            assertEquals(0,state.necrosis(second));assertEquals(0,second.getVarsManager().getValue(10986));
            state.cast(first,48297,5,false);assertEquals(6,state.necrosis(first));
            state.cast(first,48297,6,false);assertEquals(0,state.necrosis(first));assertEquals(60,state.fingerCost(first));
            state.cast(first,48298,7,false);assertNull(state.refusal(first,48299));assertNotNull(state.refusal(first,48301));
            for(int i=0;i<5;i++)state.cast(first,48298,8+i,false);
            assertEquals(3,state.souls(first));assertEquals(3,first.getVarsManager().getValue(11035));
            state.cast(first,48299,14,false);assertEquals(2,state.souls(first));
            state.cast(first,48301,15,false);assertEquals(0,state.souls(first));
            state.cast(second,48296,20,false);state.clear(first);
            assertEquals(4,state.necrosis(second));state.pulse(70);assertEquals(0,state.necrosis(second));
            assertEquals(0,second.getVarsManager().getValue(10986));
        }finally{state.clear();a.finishAndReleaseAll();b.finishAndReleaseAll();}
    }
}
