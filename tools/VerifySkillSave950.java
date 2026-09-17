import com.rs.game.player.client.*;
import java.nio.file.*;import java.util.*;import java.security.*;
/** Read an existing profile, migrate only a disposable copy, and verify all sections and original bytes. */
public final class VerifySkillSave950 {
 public static void main(String[] args)throws Exception{
  Path source=Paths.get(args[0]);String name=args[1];Path destination=Paths.get(args[2]);
  if(source.toAbsolutePath().normalize().equals(destination.toAbsolutePath().normalize()))throw new IllegalArgumentException("Use a separate disposable destination");
  Native950SaveStore original=new Native950SaveStore(source);String canonical=Native950Save.canonicalUsername(name);StringBuilder fileName=new StringBuilder();for(byte value:MessageDigest.getInstance("SHA-256").digest(canonical.getBytes(java.nio.charset.StandardCharsets.US_ASCII)))fileName.append(String.format("%02x",value&255));Path file=source.resolve(fileName+".950");
  byte[] before=Files.readAllBytes(file);Native950Save save=original.load(name);if(save==null)throw new AssertionError("Missing source profile");
  Native950SaveStore copy=new Native950SaveStore(destination);copy.save(save);Native950Save round=copy.load(name);
  if(!round.changedSections(save).isEmpty())throw new AssertionError("Migrating a copy changed an existing section");
  Native950SkillProgress prior=save.skillProgress();boolean[] boons=prior.boons();boons[7]=true;
  Native950SkillProgress progress=new Native950SkillProgress(prior.invention(),prior.archaeology(),prior.plots(),prior.dungeonTokens,prior.dungeonCompletions,prior.interruptedDungeon,prior.excavation(),5,boons,new int[]{12047,200},new int[]{946,1267});
  Native950Save edited=save.withSkillProgress(progress);copy.save(edited);Native950Save reread=copy.load(name);
  if(!reread.changedSections(edited).isEmpty()||!reread.changedSections(save).equals(EnumSet.of(Native950Save.Section.SKILL_PROGRESS)))throw new AssertionError("New progress affected another section");
  if(!Arrays.equals(before,Files.readAllBytes(file)))throw new AssertionError("Source profile changed");
  System.out.println("PASS existing profile migration into a disposable copy; all inventory/bank/equipment/skills/vitals/settings/appearance/identity and old skill progress preserved; new research/boons/familiar/toolbelt roundtrip; original bytes unchanged");
 }
}
