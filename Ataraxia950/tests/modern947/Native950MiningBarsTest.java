package modern947;

import com.rs.game.hitbar.HitBar;
import com.rs.game.hitbar.impl.MiningHitBar;
import com.rs.game.player.client.Native950Hitbars;
import com.rs.network.protocol.modern950.Native950PlayerMasks;
import java.util.Arrays;
import java.util.Collections;
import org.junit.Test;
import static org.junit.Assert.*;

public class Native950MiningBarsTest {
    @Test public void staminaAndProgressHaveDistinctColoursAndNativeFillScale() {
        Native950Hitbars.Result bars = Native950Hitbars.from(Arrays.asList(MiningHitBar.stamina(100), MiningHitBar.progress(50)));
        assertEquals(0, bars.refusals());
        assertTrue(bars.npcBars().isEmpty());
        // Player mask 0x40; no hits; negative barcount; smart id/cycle/delay; negative fill; size=-1.
        assertArrayEquals(hex("40 80 fe 07 00 00 01 00 31 00 00 81 00"), encode(bars));
    }
    @Test public void cancellationExplicitlyRemovesBothGauges() {
        assertArrayEquals(hex("40 80 fe 07 ff ff 31 ff ff"), encode(Native950Hitbars.from(Arrays.asList(
                MiningHitBar.removeStamina(), MiningHitBar.removeProgress()))));
    }
    @Test public void sameNumericIdDoesNotAdmitAnUnverifiedBarImplementation() {
        HitBar arbitrary = new HitBar() {
            public int getType() { return MiningHitBar.PROGRESS; }
            public int getPercentage() { return 255; }
        };
        Native950Hitbars.Result result=Native950Hitbars.from(Collections.singletonList(arbitrary));
        assertEquals(1,result.refusals());assertTrue(result.playerBars().isEmpty());
    }
    @Test public void valuesAreImmutableAndBoundedAtTheirOrigin() {
        MiningHitBar empty=MiningHitBar.progress(-1),full=MiningHitBar.stamina(101);
        assertEquals(0,empty.getPercentage());assertEquals(255,full.getPercentage());
        assertFalse(empty.isRemoval());assertTrue(MiningHitBar.removeProgress().isRemoval());
    }
    private static byte[] encode(Native950Hitbars.Result bars) { return Native950PlayerMasks.encode(
            Native950PlayerMasks.builder().hits(Collections.<Native950PlayerMasks.Hit>emptyList(), bars.playerBars()).build()); }
    private static byte[] hex(String s) { s=s.replace(" ", "");byte[] b=new byte[s.length()/2];for(int i=0;i<b.length;i++)b[i]=(byte)Integer.parseInt(s.substring(i*2,i*2+2),16);return b; }
}
