package modern947;
import com.rs.game.player.client.*;import com.rs.game.player.Player;import com.rs.game.WorldTile;import io.netty.channel.embedded.EmbeddedChannel;import java.io.*;import java.nio.*;import java.nio.file.*;import java.security.*;import java.util.*;
import org.junit.*;import org.junit.rules.TemporaryFolder;import static org.junit.Assert.*;
public class Native950SkillProgressTest {
 @Rule public TemporaryFolder temp=new TemporaryFolder();
 Native950Save sample(){int[] ids=new int[28];Arrays.fill(ids,-1);return new Native950Save("skills",3217,3258,0,ids,new int[28],new int[0],new int[0]);}
 @Test public void allNewProgressSurvivesAtomicSaveAndIsDefensivelyCopied()throws Exception{
  int[] invention=new int[128],arch=new int[64];invention[11]=42;arch[3]=150;
  Native950SkillProgress progress=new Native950SkillProgress(invention,arch,Collections.emptyList(),123,8,true);
  invention[11]=0;arch[3]=0;Native950Save save=sample().withSkillProgress(progress);Native950SaveStore store=new Native950SaveStore(temp.newFolder().toPath());store.save(save);
  Native950Save restored=store.load("skills");assertEquals(42,restored.skillProgress().invention()[11]);assertEquals(150,restored.skillProgress().archaeology()[3]);
  assertEquals(123,restored.skillProgress().dungeonTokens);assertEquals(8,restored.skillProgress().dungeonCompletions);assertTrue(restored.skillProgress().interruptedDungeon);
  assertEquals(EnumSet.of(Native950Save.Section.SKILL_PROGRESS),save.changedSections(sample()));assertTrue(save.changedSections(restored).isEmpty());
 }
 @Test public void realSchemaThreeLoadsAndUpgradesWithoutLosingItemsOrXp()throws Exception{
  Path dir=temp.newFolder().toPath();Native950SaveStore store=new Native950SaveStore(dir);Native950Save save=sample();store.save(save);
  Path file=Files.list(dir).findFirst().get();byte[] bytes=Files.readAllBytes(file);ByteBuffer b=ByteBuffer.wrap(bytes);
  int cursor=20+6+12;for(int container=0;container<3;container++){int count=b.getInt(cursor);cursor+=4+count*8;}cursor++;int sectionCount=cursor;cursor+=4;
  for(int i=0;i<5;i++){int len=b.getInt(cursor+4);cursor+=8+len;}
  byte[] body=Arrays.copyOf(bytes,cursor);ByteBuffer legacy=ByteBuffer.wrap(body);legacy.putInt(8,3);legacy.putInt(sectionCount,5);
  ByteArrayOutputStream out=new ByteArrayOutputStream();out.write(body);out.write(MessageDigest.getInstance("SHA-256").digest(body));Files.write(file,out.toByteArray());
  Native950Save restored=store.load("skills");assertEquals(Native950SkillProgress.EMPTY,restored.skillProgress());assertArrayEquals(save.inventoryIds(),restored.inventoryIds());
  store.save(restored);assertEquals(4,ByteBuffer.wrap(Files.readAllBytes(file)).getInt(8));
 }
 @Test public void actualExcavationCountersSurviveCharacterSaveAndRestore()throws Exception{
  EmbeddedChannel firstChannel=new EmbeddedChannel(),secondChannel=new EmbeddedChannel();
  try{
   Player first=Player.createNative950("skills",new WorldTile(3217,3258,0),firstChannel),second=Player.createNative950("skills",new WorldTile(3217,3258,0),secondChannel);
   first.getTemporaryAttributtes().put("native950.archaeology.progress.CENTURION",7);
   first.getTemporaryAttributtes().put("native950.archaeology.progress.VENATOR",31);
   Native950SkillProgress progress=Native950SkillProgress.capture(first);int[] snapshot=progress.excavation();snapshot[0]=0;
   Native950SaveStore store=new Native950SaveStore(temp.newFolder().toPath());store.save(sample().withSkillProgress(progress));
   Native950SkillProgress restored=store.load("skills").skillProgress();restored.restore(second);
   assertArrayEquals(new int[]{7,31},restored.excavation());assertArrayEquals(new int[]{7,31},Native950Archaeology.excavationProgress(second));
   assertEquals(7,second.getTemporaryAttributtes().get("native950.archaeology.progress.CENTURION"));
   assertEquals(31,second.getTemporaryAttributtes().get("native950.archaeology.progress.VENATOR"));
   int[] detached=Native950Archaeology.excavationProgress(second);detached[1]=0;assertArrayEquals(new int[]{7,31},Native950Archaeology.excavationProgress(second));
   assertEquals(progress,Native950SkillProgress.capture(second));
  }finally{firstChannel.finishAndReleaseAll();secondChannel.finishAndReleaseAll();}
 }
 @Test public void excavationBoundaryAndInvalidRestoreAreAtomic(){
  EmbeddedChannel channel=new EmbeddedChannel();try{
   Player p=Player.createNative950("skills",new WorldTile(3217,3258,0),channel);int[] counters={10,32};Native950Archaeology.restoreExcavationProgress(p,counters);counters[0]=0;
   assertArrayEquals(new int[]{10,32},Native950Archaeology.excavationProgress(p));
   for(int[] bad:new int[][]{{11,32},{1,33},{-1,0},{1}}){try{Native950Archaeology.restoreExcavationProgress(p,bad);fail("Invalid excavation accepted");}catch(IllegalArgumentException expected){}}
   assertArrayEquals(new int[]{10,32},Native950Archaeology.excavationProgress(p));
  }finally{channel.finishAndReleaseAll();}
 }
 @Test public void nestedVersionOneDefaultsOnlyExcavationCounters()throws Exception{
  int[] inv=new int[128],arch=new int[64];inv[1]=12;arch[2]=34;Native950SkillProgress expected=new Native950SkillProgress(inv,arch,Collections.emptyList(),45,6,false);
  byte[] old=legacyEncoding(1,expected);
  DataInputStream in=new DataInputStream(new ByteArrayInputStream(old));Native950SkillProgress restored=Native950SkillProgress.decode(in);assertEquals(0,in.available());assertEquals(expected,restored);assertArrayEquals(new int[2],restored.excavation());
 }
 @Test public void thirdPassProgressRoundTripsAndDefaultsOlderVersionTwo()throws Exception{
  boolean[] boons=new boolean[12];boons[0]=boons[7]=true;int[] familiar={12047,200};
  Native950SkillProgress progress=new Native950SkillProgress(new int[128],new int[64],Collections.emptyList(),13,2,false,new int[]{4,9},5,boons,familiar);
  boons[7]=false;familiar[1]=1;assertTrue(progress.boons()[7]);assertArrayEquals(new int[]{12047,200},progress.familiar());
  Native950SaveStore store=new Native950SaveStore(temp.newFolder().toPath());store.save(sample().withSkillProgress(progress));
  Native950SkillProgress actual=store.load("skills").skillProgress();assertEquals(progress,actual);assertEquals(5,actual.inventionResearch);
  byte[] old=legacyEncoding(2,progress);
  DataInputStream in=new DataInputStream(new ByteArrayInputStream(old));Native950SkillProgress migrated=Native950SkillProgress.decode(in);
  assertEquals(0,in.available());assertEquals(0,migrated.inventionResearch);assertArrayEquals(new boolean[12],migrated.boons());assertArrayEquals(new int[2],migrated.familiar());
  assertEquals(13,migrated.dungeonTokens);assertArrayEquals(new int[]{4,9},migrated.excavation());
 }
 @Test public void rejectsUnknownBoonBitsAndResearchWithoutDroppingOldFields()throws Exception{
  byte[] bytes=Native950SkillProgress.EMPTY.encode();int tail=bytes.length-13;bytes[tail+1]=(byte)0x80;
  try{Native950SkillProgress.decode(new DataInputStream(new ByteArrayInputStream(bytes)));fail("Unknown boon accepted");}catch(IOException expected){}
  bytes=Native950SkillProgress.EMPTY.encode();bytes[tail]=10;
  try{Native950SkillProgress.decode(new DataInputStream(new ByteArrayInputStream(bytes)));fail("Unknown research accepted");}catch(IllegalArgumentException expected){}
 }
 @Test public void toolbeltSurvivesSaveAndLegacyVersionThreeDefaultsOnlyTheBelt()throws Exception{
  int[] tools={946,1267};boolean[] boons=new boolean[12];boons[7]=true;
  Native950SkillProgress progress=new Native950SkillProgress(new int[128],new int[64],Collections.emptyList(),13,2,false,new int[]{4,9},5,boons,new int[]{12047,200},tools);
  tools[0]=1;assertArrayEquals(new int[]{946,1267},progress.toolbelt());
  int[] detached=progress.toolbelt();detached[0]=1;assertArrayEquals(new int[]{946,1267},progress.toolbelt());
  Native950SaveStore store=new Native950SaveStore(temp.newFolder().toPath());store.save(sample().withSkillProgress(progress));
  assertEquals(progress,store.load("skills").skillProgress());
  DataInputStream in=new DataInputStream(new ByteArrayInputStream(legacyEncoding(3,progress)));
  Native950SkillProgress migrated=Native950SkillProgress.decode(in);
  assertEquals(0,in.available());assertArrayEquals(new int[0],migrated.toolbelt());
  assertEquals(5,migrated.inventionResearch);assertTrue(migrated.boons()[7]);assertArrayEquals(new int[]{12047,200},migrated.familiar());
  assertArrayEquals(new int[]{4,9},migrated.excavation());assertEquals(13,migrated.dungeonTokens);
 }
 @Test public void malformedToolbeltCannotOverwriteAValidProfile()throws Exception{
  Native950SaveStore store=new Native950SaveStore(temp.newFolder().toPath());Native950Save save=sample();store.save(save);
  for(int[] tools:new int[][]{{-1},{946,946},{1267,946},new int[257]}){
   try{new Native950SkillProgress(new int[128],new int[64],Collections.emptyList(),0,0,false,new int[2],0,new boolean[12],new int[2],tools);fail("Invalid toolbelt accepted");}catch(IllegalArgumentException expected){}
  }
  assertEquals(save.skillProgress(),store.load("skills").skillProgress());
  byte[] bytes=Native950SkillProgress.EMPTY.encode();bytes[bytes.length-2]=1;bytes[bytes.length-1]=1;
  try{Native950SkillProgress.decode(new DataInputStream(new ByteArrayInputStream(bytes)));fail("Oversized toolbelt accepted");}catch(IOException expected){}
 }
 /** Independent previous-format writer: migration fixtures do not depend on today's encoder tail size. */
 private static byte[] legacyEncoding(int version,Native950SkillProgress progress)throws IOException{
  ByteArrayOutputStream bytes=new ByteArrayOutputStream();DataOutputStream out=new DataOutputStream(bytes);
  out.writeInt(version);for(int value:progress.invention())out.writeInt(value);for(int value:progress.archaeology())out.writeInt(value);
  if(version>=2)for(int value:progress.excavation())out.writeInt(value);
  out.writeInt(progress.dungeonTokens);out.writeInt(progress.dungeonCompletions);out.writeBoolean(progress.interruptedDungeon);
  assertTrue("Fixture intentionally has no Farming patches",progress.plots().isEmpty());out.writeInt(0);
  if(version>=3){out.writeByte(progress.inventionResearch);int bits=0;boolean[] boons=progress.boons();for(int i=0;i<boons.length;i++)if(boons[i])bits|=1<<i;out.writeShort(bits);for(int value:progress.familiar())out.writeInt(value);}
  out.flush();return bytes.toByteArray();
 }
 @Test(expected=IllegalArgumentException.class)public void negativeMaterialCountsAreRejected(){int[] inv=new int[128];inv[5]=-1;new Native950SkillProgress(inv,new int[64],Collections.emptyList(),0,0,false);}
 @Test(expected=IOException.class)public void truncatedProgressIsRejected()throws Exception{Native950SkillProgress.decode(new DataInputStream(new ByteArrayInputStream(new byte[10])));}
}
