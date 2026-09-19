package com.rs.game.player.client;

import com.rs.game.WorldTile;
import com.rs.game.player.Player;
import io.netty.channel.embedded.EmbeddedChannel;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class Native950CombatQaSessionTest {
    private File root;
    private EmbeddedChannel channel;
    private Player player;

    @Before public void setup() throws Exception {
        root=Files.createTempDirectory("native950-combatqa-").toFile();
        System.setProperty("ataraxia950.combatQaRoot",root.getAbsolutePath());
        Native950CombatQa.setWindowCapturerForTests(output->{
            try(FileOutputStream stream=new FileOutputStream(output)){stream.write(new byte[]{(byte)137,80,78,71});}
            catch(Exception failure){throw new RuntimeException(failure);}
            return new Native950WindowCapture.Result(true,0,"test-helper","test-helper <redacted>",
                    "windowHandle=0x1;bounds=0,0,800,600","","",output.length());
        });
        channel=new EmbeddedChannel();
        player=Player.createNative950("combatqa-test",new WorldTile(3217,3258,0),channel);
        player.setActive(true);
    }

    @After public void cleanup(){
        Native950CombatQa.close(player,"test-cleanup");
        Native950CombatQa.setWindowCapturerForTests(null);
        System.clearProperty("ataraxia950.combatQaRoot");
        channel.finishAndReleaseAll();
    }

    @Test public void sessionIsObservationalAndFinalizesCorrelatedStoryboardsAndManualMarkers() throws Exception {
        int x=player.getX(),y=player.getY(),plane=player.getPlane(),used=player.getInventory().getItems().getUsedSlots();
        assertTrue(Native950CombatQa.start(player).contains("started"));
        Native950CombatQa.event(player,"combat","ability-request","structure",14708);
        Native950CombatQa.event(player,"combat","ability-executed","name","Backhand","structure",14708,
                "animation",18023,"graphic","none","targetGraphic","none","gcdEndTick",3,
                "channelEndTick",0,"followUpHitTicks","[]");
        Native950CombatQa.marker(player,"animation looked late");
        assertTrue(Native950CombatQa.status(player).contains("active"));
        assertTrue(Native950CombatQa.stop(player,"command").contains("stopped"));
        File latest=new File(root,"latest-completed.txt");
        for(int i=0;i<80&&!latest.isFile();i++)Thread.sleep(50L);
        assertTrue("session did not finalize",latest.isFile());
        File session=new File(new String(Files.readAllBytes(latest.toPath()),StandardCharsets.UTF_8).trim());
        String index=new String(Files.readAllBytes(new File(session,"session-index.json").toPath()),StandardCharsets.UTF_8);
        String timeline=new String(Files.readAllBytes(new File(session,"timeline.jsonl").toPath()),StandardCharsets.UTF_8);
        assertTrue(index.contains("\"persistentStateChanged\":false"));
        assertTrue(index.contains("\"Backhand\""));assertTrue(index.contains("\"manualBugMarkers\":1"));
        assertTrue(timeline.contains("\"correlationId\""));assertTrue(timeline.contains("manual-bug"));
        assertTrue(new File(session,"evidence-lifecycle.tsv").isFile());
        assertEquals("UNREVIEWED",new String(Files.readAllBytes(new File(session,"session-state.txt").toPath()),StandardCharsets.UTF_8).trim());
        assertEquals(x,player.getX());assertEquals(y,player.getY());assertEquals(plane,player.getPlane());
        assertEquals(used,player.getInventory().getItems().getUsedSlots());
    }
}
