package modern947;

import com.rs.game.player.client.Native950ItemCatalog;
import org.junit.Test;
import static org.junit.Assert.*;

public class Native950EquipmentCatalogTest {
    @Test public void metadataKeepsCacheWearOperationGapAndDefensivelyCopiesOptions() {
        String[] options = {null, "Wield", null, null, "Drop"};
        Native950ItemCatalog.Entry sword = new Native950ItemCatalog.Entry(1277, "Bronze sword", false, options, 3, 2);
        options[1] = "Drop";
        assertEquals(3, sword.equipSlot);
        assertEquals(2, sword.equipOption);
        assertNull(sword.option(1));
        assertEquals("Wield", sword.option(2));
        Native950ItemCatalog.Entry logs = new Native950ItemCatalog.Entry(1511, "Logs", false, options);
        assertEquals(-1, logs.equipSlot);
        assertEquals(0, logs.equipOption);
    }

    @Test public void invalidMetadataCannotAuthorizeAnEquipmentTransaction() {
        reject(false, 3, 1, new String[] {null, "Wield"});
        reject(false, 3, 2, new String[] {null, "Drop"});
        reject(false, -1, 2, new String[] {null, "Wield"});
        reject(false, 19, 2, new String[] {null, "Wield"});
        assertEquals(13, new Native950ItemCatalog.Entry(90000, "Ammunition", true,
                new String[] {null, "Equip"}, 13, 2).equipSlot);
        reject(false, 3, 7, new String[] {null, null, null, null, null, null, "Wield"});
    }

    private static void reject(boolean stackable, int slot, int operation, String[] options) {
        try {
            new Native950ItemCatalog.Entry(1277, "Bronze sword", stackable, options, slot, operation);
            fail("Invalid equipment metadata was accepted");
        } catch (IllegalArgumentException expected) { }
    }
}
