package com.rs.game.player.client;

import com.rs.cache.loaders.ItemDefinitions;
import com.rs.game.Projectile;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import org.junit.Test;
import java.util.HashMap;
import static org.junit.Assert.*;

public final class Native950RangedPresentationTest {
    @Test public void projectileBelongsToWeaponOrActualAmmoNotAHardCodedArrow(){
        ItemDefinitions weapon=item(0),ammo=item(123);
        assertEquals(123,Native950RangedPresentation.graphic(weapon,ammo));
        assertEquals(456,Native950RangedPresentation.graphic(item(456),ammo));
        assertEquals(-1,Native950RangedPresentation.graphic(weapon,null));
        assertEquals(-1,Native950RangedPresentation.graphic(null,item(-1)));
    }
    @Test public void absoluteEndCyclesDriveHitTickAndPreserveEntityEndpoints(){
        NPC from=NPC.createNative950(1,new WorldTile(3200,3200,0),1);
        NPC to=NPC.createNative950(2,new WorldTile(3205,3200,0),1);
        int[][] expected={{1,0,40,50,2},{2,0,20,27,1},{2,1,30,37,2},{3,0,23,30,1}};
        for(int[] row:expected){
            Native950RangedPresentation r=new Native950RangedPresentation(123,row[0],row[1]==1);
            Projectile p=r.projectile(from,to);
            assertSame(from,p.getFrom());assertSame(to,p.getTo());
            assertEquals(row[2],p.getStartTime());assertEquals(row[3],p.getEndTime());assertEquals(row[4],r.hitDelay());
        }
    }
    @Test public void animationResolutionPreservesTypedAbsenceAndNativePrecedence(){
        HashMap<Long,Object> map=new HashMap<>();map.put(42L,123);
        assertEquals(123,Native950AbilityCatalog.keyedAnimation(456,map,789,42));
        assertEquals(789,Native950AbilityCatalog.keyedAnimation(456,map,789,43));
        assertEquals(456,Native950AbilityCatalog.keyedAnimation(456,map,-1,43));
        assertEquals(-1,Native950AbilityCatalog.keyedAnimation(null,null,-1,43));
        assertEquals(-1,Native950AbilityCatalog.keyedAnimation("456",null,-1,43));
        assertEquals(0,Native950AbilityCatalog.keyedAnimation(0,null,-1,43));
    }
    private static ItemDefinitions item(int graphic){
        ItemDefinitions d=ItemDefinitions.decodeStrict947(1,new byte[]{0},null);
        d.clientScriptData=new HashMap<>();d.clientScriptData.put(2940,graphic);return d;
    }
}
