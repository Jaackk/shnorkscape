package com.rs.game.player.client;

import java.io.ByteArrayOutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.*;

public final class Native950CacheItemsTest {
    @Test public void currentIdentityCarriesMenuLabelsWithoutAuthorizingLegacyEquipment() {
        Map<Integer,byte[]> files = new HashMap<Integer,byte[]>();
        files.put(64000, menu("Current item", new String[]{"Read", "Wield", "Check", null, "Destroy"}));
        assertNull(Native950NpcDrops.resolveMetadata(64000, id -> false, files::get));
        Native950ItemCatalog.Entry item = Native950CacheItems.resolveEntry(64000, files::get);
        assertNotNull(item);
        assertEquals("Current item", item.name);
        assertFalse(item.stackable);
        assertEquals(-1, item.equipSlot);
        assertEquals(0, item.equipOption);
        assertEquals("Read", item.option(1));
        assertEquals("Wield", item.option(2));
        assertEquals("Check", item.option(3));
        assertNull(item.option(4));
        assertEquals("Destroy", item.option(5));
    }

    @Test public void newCatalogCanResolveSavedCurrentIdsAndUsesCurrentStackAndNoteSemantics() {
        Map<Integer,byte[]> files = new HashMap<Integer,byte[]>();
        files.put(64000, raw("Current stack",11));
        files.put(526, raw("Bones",97,2,15));
        files.put(527, raw(null,97,2,14,98,3,31));
        files.put(799, raw("Bank note"));
        for (int login = 0; login < 2; login++) {
            Native950ItemCatalog catalog = new Native950ItemCatalog(Collections.emptyList(),
                    id -> Native950CacheItems.resolveEntry(id, files::get));
            assertTrue(catalog.get(64000).stackable);
            assertFalse(catalog.get(526).stackable);
            assertEquals(527, catalog.get(527).id);
            assertEquals("Bones", catalog.get(527).name);
            assertTrue(catalog.get(527).stackable);
        }
    }

    @Test public void MissingTemplatesMalformedDefinitionsAndInvalidWireIdsStayRefused() {
        Map<Integer,byte[]> files = new HashMap<Integer,byte[]>();
        assertNull(Native950CacheItems.resolveEntry(1, files::get));
        files.put(1, new byte[]{(byte)255,0});
        assertNull(Native950CacheItems.resolveEntry(1, files::get));
        for (int opcode : new int[]{122,140,162}) {
            files.put(1, raw("Transformed item",opcode,3,31));
            assertNull(Native950CacheItems.resolveEntry(1, files::get));
        }
        for (int id : new int[]{-1,0xffffff,Integer.MAX_VALUE})
            assertNull(Native950CacheItems.resolveEntry(id, value -> raw("Invalid range")));
        files.put(527, raw(null,97,2,14,98,3,31));
        assertNull(Native950CacheItems.resolveEntry(527, files::get));
    }

    @Test public void ordinaryItemsKeepDefaultDropAndNotesNeverInheritBaseActions() {
        Map<Integer,byte[]> files = new HashMap<Integer,byte[]>();
        files.put(64000, raw("Ordinary item"));
        Native950ItemCatalog.Entry ordinary = Native950CacheItems.resolveEntry(64000, files::get);
        for (int option = 1; option <= 4; option++) assertNull(ordinary.option(option));
        assertEquals("drop", ordinary.option(5));
        ByteArrayOutputStream base = new ByteArrayOutputStream();
        byte[] menu = menu("Bones", new String[]{"Bury", null, null, null, null});
        base.write(menu, 0, menu.length - 1);
        for (int value : new int[]{201, 0, 2, 15, 0}) base.write(value);
        files.put(526, base.toByteArray());
        files.put(527, raw(null, 201, 0, 2, 14, 202, 0, 3, 31));
        files.put(799, raw("Bank note"));
        Native950ItemCatalog.Entry noted = Native950CacheItems.resolveEntry(527, files::get);
        assertEquals("Bones", noted.name);
        assertTrue(noted.stackable);
        assertNull(noted.option(1));
        assertEquals("drop", noted.option(5));
        assertEquals(-1, noted.equipSlot);
    }

    @Test public void malformedActionPayloadIsNotUsedAsMetadata() {
        Map<Integer,byte[]> files = new HashMap<Integer,byte[]>();
        files.put(1, new byte[]{2, 'A', 0, 35, 'E', 'a', 't'});
        assertNull(Native950CacheItems.resolveEntry(1, files::get));
    }

    @Test public void cacheIdsUseTheFullU24ItemPlusOneDomainIncludingZero() {
        for(int id:new int[]{0,65535,65536,0xfffffe}) {
            Native950ItemCatalog.Entry item=Native950CacheItems.resolveEntry(id,value->raw("Wide item",11));
            assertNotNull(item);assertEquals(id,item.id);assertTrue(item.stackable);
        }
    }

    @Test public void lentBoundAndShardTemplatesResolveFromAuthoredLinks() {
        Map<Integer,byte[]> files=new HashMap<Integer,byte[]>();
        files.put(900,raw(null));
        files.put(100,menu("Base armour",new String[]{"Wear","Check",null,null,"Drop"}));
        files.put(101,raw(null,203,0,0,100,204,0,3,132));
        files.put(102,raw(null,205,0,0,100,206,0,3,132));
        Native950ItemCatalog.Entry lent=Native950CacheItems.resolveEntry(101,files::get);
        Native950ItemCatalog.Entry bound=Native950CacheItems.resolveEntry(102,files::get);
        assertEquals("Base armour",lent.name);assertEquals("Wear",lent.option(1));assertEquals("Check",lent.option(2));
        assertEquals("Discard",lent.option(5));assertEquals("Destroy",bound.option(5));
        files.put(200,raw("Base shard item",164,'S','h','a','r','d',0,163,0,10));
        files.put(201,raw(null,207,0,0,200,208,0,3,132));
        Native950ItemCatalog.Entry shard=Native950CacheItems.resolveEntry(201,files::get);
        assertEquals("Shard",shard.name);assertTrue(shard.stackable);
        assertEquals("Combine",shard.option(1));assertEquals("Drop",shard.option(5));
        // A cycle in either the base link or the template itself cannot create a usable identity.
        files.put(101,raw(null,203,0,0,102,204,0,3,132));
        files.put(102,raw(null,205,0,0,101,206,0,3,132));
        assertNull(Native950CacheItems.resolveEntry(101,files::get));
        files.put(900,raw(null,205,0,0,100,206,0,3,132));
        assertNull(Native950CacheItems.resolveEntry(201,files::get));
    }

    @Test public void bankRestrictionsUseExactCurrentClientParamTests() {
        com.rs.cache.loaders.ItemDefinitions item=com.rs.cache.loaders.ItemDefinitions.decodeStrict947(0,raw("Item"),null);
        item.clientScriptData=new HashMap<Integer,Object>();
        assertTrue(Native950Banking.bankable(item));
        for(int param:new int[]{59,1047}) {
            item.clientScriptData.put(param,1);assertFalse(Native950Banking.bankable(item));
            item.clientScriptData.put(param,2);assertTrue(Native950Banking.bankable(item));
            item.clientScriptData.remove(param);
        }
        assertFalse(Native950Banking.bankable(null));
    }
    private static byte[] menu(String name, String[] options) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(2); for (char c : name.toCharArray()) out.write(c); out.write(0);
        for (int i = 0; i < options.length; i++) if (options[i] != null) {
            out.write(35 + i); for (char c : options[i].toCharArray()) out.write(c); out.write(0);
        }
        out.write(0);
        return out.toByteArray();
    }
    private static byte[] raw(String name, int... bytes) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        if (name != null) { out.write(2); for (char c : name.toCharArray()) out.write(c); out.write(0); }
        for (int b : bytes) out.write(b);
        out.write(0);
        return out.toByteArray();
    }
}
