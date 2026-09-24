import com.rs.cache.modern.FlatCacheRepository;
import java.nio.file.*;import java.util.*;import java.security.*;
/** Read-only cross-cache evidence, never a name generator or runtime patch. */
public class GamevalIdentityResearch {
 static String sha(byte[] b)throws Exception {StringBuilder s=new StringBuilder();for(byte x:MessageDigest.getInstance("SHA-256").digest(b))s.append(String.format("%02x",x&255));return s.toString();}
 public static void main(String[] args)throws Exception {
  FlatCacheRepository live=new FlatCacheRepository(Paths.get(args[0])),beta=new FlatCacheRepository(Paths.get(args[1]));
  for(int[] pair:new int[][]{{3,91},{3,517},{3,623},{3,938},{3,947},{3,1448},{3,1477},{2,60},{2,69}}){
   Map<Integer,byte[]> a=beta.readGroup(pair[0],pair[1]),b=live.readGroup(pair[0],pair[1]);
   int same=0;for(int id:a.keySet())if(Arrays.equals(a.get(id),b.get(id)))same++;
   System.out.println("GROUP\t"+pair[0]+"\t"+pair[1]+"\t"+a.size()+"\t"+b.size()+"\t"+same+"\t"+a.keySet().equals(b.keySet()));
   if(pair[0]==3&&pair[1]!=1477)continue;
   int[] ids=pair[0]==3?new int[]{291,707,713,714,715,736}:pair[1]==60?new int[]{10986,11035}:new int[]{28041,42886,42889,42892};
   for(int id:ids){byte[] x=a.get(id),y=b.get(id);System.out.println("FILE\t"+pair[0]+"\t"+pair[1]+"\t"+id+"\t"+Arrays.equals(x,y)+"\t"+sha(x)+"\t"+sha(y));}

  }
 }
}
