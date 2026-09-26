package com.rs.game.player.client;
import com.rs.cache.Cache;
import com.rs.cache.loaders.NPCDefinitions;
import com.rs.utils.data.parsers.npcs.NPCCombatDefinitionsDataParser;
import com.rs.utils.data.parsers.npcs.NPCStatsDataParser;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

/** Real-cache probe for preview framing and saved overrides. No world, socket, character or live file. */
public final class Native950DeveloperPreviewOverrideAcceptance {
    static void check(boolean ok,String why){if(!ok)throw new AssertionError(why);}
    static Path use(Path file){System.setProperty("ataraxia950.devPreviewOverrides",file.toString());Native950DeveloperPreviewOverrides.reload();return file;}
    static void rejects(Runnable r,String why){try{r.run();check(false,why);}catch(IllegalArgumentException|IllegalStateException expected){}}

    public static void main(String[] args)throws Exception{
        Cache.initFlatReadOnly(Paths.get(args[0]));
        NPCCombatDefinitionsDataParser.init();NPCStatsDataParser.init();
        Path dir=Files.createTempDirectory("dev-preview-override-");
        use(dir.resolve("overrides.txt"));
        int man=1,vorago=17161,prime=2882,chicken=288,kbd=50,kbdVariant=2642;

        // Deterministic automatic tiers.
        check(Native950DeveloperPreview.resolve(man).zoom==650,"Unscaled humanoid framing changed");
        check(Native950DeveloperPreview.resolve(vorago).zoom==5000,"Accepted Vorago automatic framing changed");
        check(Native950DeveloperPreview.resolve(prime).zoom==Native950DeveloperPreview.scaledDistance(1350,175,180),"Dagannoth Prime resize not applied");
        check(Native950DeveloperPreview.resolve(chicken).zoom==325,"Small resized NPC not framed closer");
        Native950DeveloperPreview beast=Native950DeveloperPreview.resolve(kbd),variant=Native950DeveloperPreview.resolve(kbdVariant);
        check(beast.nativeFraming&&beast.zoom==1900&&beast.height==300,"Exact Beasts framing changed");
        check(java.util.Arrays.equals(NPCDefinitions.getNPCDefinitions(kbd).models,NPCDefinitions.getNPCDefinitions(kbdVariant).models)
            ?variant.nativeFraming&&variant.zoom==beast.zoom&&variant.height==beast.height:!variant.nativeFraming,"Identical-geometry variant did not share native framing");
        check(Native950DeveloperPreview.scaledDistance(5000,256,256)==6000&&Native950DeveloperPreview.scaledDistance(650,0,0)==650,"Scaled distance escaped bounds");

        // Override precedence and independence.
        Native950DeveloperPreview before=Native950DeveloperPreview.resolve(vorago),otherBefore=Native950DeveloperPreview.resolve(man);
        Native950DeveloperPreviewOverrides.save("npc:"+vorago,3200);
        Native950DeveloperPreview after=Native950DeveloperPreview.resolve(vorago);
        check(after.zoom==3200,"Saved override did not take effect");
        check(after.npc==before.npc&&after.idle==before.idle&&after.attack==before.attack&&after.height==before.height&&after.nativeFraming==before.nativeFraming,"Override changed more than zoom");
        check(Native950DeveloperPreview.resolve(man).zoom==otherBefore.zoom,"Unrelated NPC inherited an override");
        // Reset restores preview.zoom (bound into CS21147 as the preferred default).
        check(Native950DeveloperPreviewOverrides.get("npc:"+vorago)==3200,"Reset path erased the override");
        Native950DeveloperPreviewOverrides.save("npc:"+kbd,1500);
        check(Native950DeveloperPreview.resolve(kbd).zoom==1500&&Native950DeveloperPreview.resolve(kbd).height==300,"Override did not beat native framing, or changed height");
        check(Native950DeveloperPreview.resolve(kbdVariant).zoom==variant.zoom,"Override leaked to a variant");

        // Persistence: a fresh read of the file sees the same values.
        Native950DeveloperPreviewOverrides.reload();
        check(Native950DeveloperPreviewOverrides.get("npc:"+vorago)==3200&&Native950DeveloperPreviewOverrides.get("npc:"+kbd)==1500,"Overrides not persisted");
        check(Native950DeveloperPreviewOverrides.get("npc:"+man)==null,"Unsaved NPC has an override");

        // Invalid input.
        rejects(()->Native950DeveloperPreviewOverrides.save("npc:"+vorago,49),"Zoom below minimum accepted");
        rejects(()->Native950DeveloperPreviewOverrides.save("npc:"+vorago,6001),"Zoom above maximum accepted");
        rejects(()->Native950DeveloperPreviewOverrides.save("object:1234",1000),"Non-NPC subject accepted");
        rejects(()->Native950DeveloperPreviewOverrides.save("npc:-1",1000),"Negative id accepted");
        check(Native950DeveloperPreviewOverrides.get("npc:"+vorago)==3200,"Rejected save changed state");

        // Malformed lines are skipped individually.
        Path mixed=use(dir.resolve("mixed.txt"));
        Files.write(mixed,("SHNORKSCAPE-DEV-PREVIEW-1\nnpc:1=900\nnpc:2=abc\nnpc:3=99999\ngarbage\nnpc:4=700\n").getBytes(StandardCharsets.UTF_8));
        Native950DeveloperPreviewOverrides.reload();
        check(Native950DeveloperPreviewOverrides.get("npc:1")==900&&Native950DeveloperPreviewOverrides.get("npc:4")==700,"Valid lines lost beside bad ones");
        check(Native950DeveloperPreviewOverrides.get("npc:2")==null&&Native950DeveloperPreviewOverrides.get("npc:3")==null,"Bad lines accepted");

        // An untrusted file is read as empty and never overwritten.
        Path foreign=use(dir.resolve("foreign.txt"));
        byte[] original="SOMETHING-ELSE\nnpc:1=900\n".getBytes(StandardCharsets.UTF_8);Files.write(foreign,original);
        Native950DeveloperPreviewOverrides.reload();
        check(Native950DeveloperPreviewOverrides.get("npc:1")==null,"Unknown-format file was trusted");
        rejects(()->Native950DeveloperPreviewOverrides.save("npc:1",800),"Unreadable file was overwritten");
        check(java.util.Arrays.equals(original,Files.readAllBytes(foreign)),"Unreadable file content changed");
        check(!Files.list(dir).anyMatch(p->p.getFileName().toString().endsWith(".tmp")),"Temporary file left behind");

        System.out.println("Preview framing tiers, override precedence/isolation/persistence, Reset and malformed-file safety PASS; Vulkan rendering remains unverified.");
    }
}
