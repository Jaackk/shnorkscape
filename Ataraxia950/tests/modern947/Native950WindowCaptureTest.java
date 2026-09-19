package com.rs.game.player.client;

import java.io.File;
import org.junit.Test;
import static org.junit.Assert.*;

public class Native950WindowCaptureTest {
    @Test public void captureEitherProducesANonemptyPngOrCompleteFailureEvidence() throws Exception {
        File output=File.createTempFile("native950-window-capture-",".png");
        assertTrue(output.delete());
        Native950WindowCapture.Result result=Native950WindowCapture.capture(output);
        assertTrue(result.helper.toLowerCase().endsWith("powershell.exe"));
        assertTrue(result.command.contains("-NoProfile"));
        if(result.saved){assertTrue(output.isFile());assertTrue(output.length()>0);}
        else assertTrue(result.exitCode!=0&&(!result.output.isEmpty()||!result.errorType.isEmpty()||!result.errorMessage.isEmpty()));
        if(output.exists())assertTrue(output.delete());
    }
}
