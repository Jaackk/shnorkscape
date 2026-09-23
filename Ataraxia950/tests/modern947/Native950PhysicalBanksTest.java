package com.rs.game.player.client;
import org.junit.Test;
import static org.junit.Assert.*;
public class Native950PhysicalBanksTest {
    @Test public void bankOperationsDoNotTurnCollectOrDepositOnlyObjectsIntoFullBanks(){
        assertTrue(Native950PhysicalBanks.accepts("Bank booth","Bank"));
        assertTrue(Native950PhysicalBanks.accepts("Bank chest","Use"));
        assertTrue(Native950PhysicalBanks.accepts("Bank counter","Bank"));
        assertFalse(Native950PhysicalBanks.accepts("Bank booth","Collect"));
        assertFalse(Native950PhysicalBanks.accepts("Bank chest","Load Last Preset from"));
        assertFalse(Native950PhysicalBanks.accepts("Bank deposit box","Deposit"));
        assertFalse(Native950PhysicalBanks.accepts("Chest","Use"));
        assertFalse(Native950PhysicalBanks.accepts(null,"Bank"));
    }
}
