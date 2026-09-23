package com.rs.game.player.client;
import com.rs.cache.Cache;
import com.rs.cache.loaders.DBRow;
import com.rs.cache.loaders.rs3.RS3ClientScriptMap;
import java.nio.file.Paths;
import java.util.*;
/** Read-only settings membership witness. */
public final class Native950RevolutionProbe {
 public static void main(String[] args)throws Exception{
  Cache.initFlatReadOnly(Paths.get(args[0]));
  for(Map.Entry<Long,Object> entry:RS3ClientScriptMap.getMap(14569).getValues().entrySet()){
   int id=(Integer)entry.getValue();DBRow row=DBRow.getDBRow(id);
   String data=Arrays.deepToString(row.data);
   if(data.contains("41505")||data.contains("41506")||data.contains("41507")||data.contains("41508"))
    System.out.println("category="+entry.getKey()+" row="+id+" data="+data);
  }
  if(args.length>1){
   List<int[]> files=new ArrayList<>();files.add(new int[]{2,41,1306});files.add(new int[]{17,14569>>>8,14569&255});
   for(int id:new int[]{2526,2830,2970,2987,10424,10446,10447,10448,10450,10451,10603})files.add(new int[]{12,id,0});
   for(int id:new int[]{41505,41506,41507,15425,41508})files.add(new int[]{22,id>>>5,id&31});
   for(int id:new int[]{38639,38666,38708,52329,38709})files.add(new int[]{2,69,id});
   StringBuilder pins=new StringBuilder();
   for(int[] f:files){byte[] raw=Cache.STORE.getIndexes()[f[0]].getFile(f[1],f[2]);
    if(raw==null)throw new IllegalStateException(Arrays.toString(f));
    StringBuilder h=new StringBuilder();for(byte b:java.security.MessageDigest.getInstance("SHA-256").digest(raw))h.append(String.format("%02x",b&255));
    pins.append("        pin(").append(f[0]).append(", ").append(f[1]).append(", ").append(f[2]).append(", \"").append(h).append("\");\n");
   }
   java.nio.file.Files.write(Paths.get(args[1]),pins.toString().getBytes("UTF-8"));
  }
 }
}
