package com.rs.game.player.client;

import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.assertTrue;

/** Ensures the descriptor remains a generated, pinned cache artifact rather than a guessed list. */
public final class Native950WorkspaceIntegerDescriptorEvidenceTest {
    @Test public void generatedDescriptorPinsTheExactWorkspaceEvidence() throws Exception {
        String resource = new String(Files.readAllBytes(Paths.get("resources/native950/workspace-integer-descriptor-950.properties")), "ISO-8859-1");
        assertTrue(resource.contains("bootstrap.count=215"));
        assertTrue(resource.contains("script.8707.sha256="));
        assertTrue(resource.contains("script.8708.sha256="));
        assertTrue(resource.contains("script.8709.sha256="));
        assertTrue(resource.contains("varbit.19037.parent=3296"));
        assertTrue(resource.contains("varbit.19037.domain=2"));
        assertTrue("generic VarClientDefinition enumeration is forbidden", !new String(Files.readAllBytes(Paths.get("game/com/rs/game/player/client/Native950WorkspaceIntegerDescriptorGenerator.java")), "UTF-8").contains("VarClientDefinition"));
    }

}
