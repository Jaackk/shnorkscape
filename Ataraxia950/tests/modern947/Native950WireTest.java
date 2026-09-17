package modern947;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.Assume;
import org.junit.Test;

/** Runs the same independent wire fixtures in the normal Gradle test lifecycle. */
public final class Native950WireTest {
    @Test public void decodesVerifiedGroundAndMinimapClicks() { Native950ProtocolTest.walkFixtures(); }
    @Test public void preservesFramingAndCipherAcrossEveryTcpSplit() { Native950ProtocolTest.fragmentedFraming(); }
    @Test public void rejectsUnknownFramingAndMalformedOutboundBodies() { Native950ProtocolTest.protocolFailures(); }
    @Test public void emitsVerifiedNativeInterfaceAndFrameBytes() { Native950ProtocolTest.outboundFixtures(); }
    @Test public void emitsVerifiedMusicResourceVolumeAndStopBytes() { Native950ProtocolTest.musicFixtures(); }
    @Test public void emitsInitialSceneAndSinglePlayerUpdates() { Native950ProtocolTest.sceneAndPlayerFixtures(); }

    @Test public void matchesExtractedNativeConstructorSizesWhenReferenceIsAvailable() throws Exception {
        Path reference = Paths.get(System.getProperty("modern950.nativeSizes",
                "../OpenNXT/data/prot/950/clientProtSizes.toml"));
        Assume.assumeTrue("Native extraction artifact is optional outside the migration workspace", Files.isRegularFile(reference));
        Native950ProtocolTest.compareExtractedSizeTable(reference.toString());
    }
}
