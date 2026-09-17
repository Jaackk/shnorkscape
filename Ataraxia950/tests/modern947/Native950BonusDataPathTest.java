package com.rs.game.player.combat.rs2;

import com.rs.utils.DataPaths;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import static org.junit.Assert.*;

/** Exercises the real bonus/override readers from staged and classic data roots. */
public final class Native950BonusDataPathTest {
    @Rule public TemporaryFolder temporary=new TemporaryFolder();
    @Test public void stagedDataRootLoadsBonusesAndOverridesWithoutDependingOnJvmCwd() throws Exception {
        // Initialize the real immutable startup tables before installing a temporary test root.
        RS2BonusDatabase.lookup2009scape("Bronze sword");
        Path previous=DataPaths.installed();
        Path staged=temporary.newFolder("staged-data").toPath();
        write(staged,"items_2009scape.json","{\"Fixture sword\":[11,2,3,4,5,6,7,8,9,10,999,12,13,14,15]}");
        write(staged,"items_overrides.json","{\"1277\":[21,2,3,4,5,6,7,8,9,10,999,22,13,14,15]}");
        try {
            DataPaths.install(staged);
            ClassicBonuses loaded=RS2BonusDatabase.loadOrEmpty("rs2_combat/items_2009scape.json").get("fixture sword");
            assertNotNull(loaded);assertEquals(11,loaded.stabAtk);assertEquals(12,loaded.strBonus);
            Map<Integer,ClassicBonuses> byId=new HashMap<>();Map<String,ClassicBonuses> byName=new HashMap<>();
            RS2BonusDatabase.loadOverridesInto("rs2_combat/items_overrides.json",byId,byName);
            assertEquals(21,byId.get(1277).stabAtk);assertEquals(22,byId.get(1277).strBonus);
            assertTrue(byName.isEmpty());
        } finally {restore(previous);}
    }
    @Test public void classicLaunchStillFindsItsDataDirectoryAndMissingOptionalSourceIsEmpty() throws Exception {
        RS2BonusDatabase.lookup2009scape("Bronze sword");
        Path previous=DataPaths.installed();
        Path classic=temporary.newFolder("classic-server").toPath();Path data=classic.resolve("data");
        Files.createDirectories(data.resolve("npcs"));Files.write(data.resolve(DataPaths.MARKER_FILE),"[]".getBytes(StandardCharsets.UTF_8));
        write(data,"items_2009scape.json","{\"Bronze sword\":[4,3,2,0,0,0,0,0,0,0,0,3,0,0,0]}");
        try {
            Path resolved=DataPaths.resolveRoot(null,classic);
            assertEquals(data.toAbsolutePath().normalize(),resolved);DataPaths.install(resolved);
            assertEquals(4,RS2BonusDatabase.loadOrEmpty("rs2_combat/items_2009scape.json").get("bronze sword").stabAtk);
            assertTrue(RS2BonusDatabase.loadOrEmpty("rs2_combat/items_osrsbox.json").isEmpty());
        } finally {restore(previous);}
    }
    private static void restore(Path previous){DataPaths.reset();if(previous!=null)DataPaths.install(previous);}
    private static void write(Path root,String name,String json) throws Exception {
        Files.createDirectories(root.resolve("rs2_combat"));Files.write(root.resolve("rs2_combat").resolve(name),json.getBytes(StandardCharsets.UTF_8));
    }
}
