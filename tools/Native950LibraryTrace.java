import com.rs.cache.Cache;
import java.nio.file.*;
/** Read-only exporter for exact native library UI research. Output is private scratch data. */
public final class Native950LibraryTrace {
 public static void main(String[] args)throws Exception {
  Cache.initFlatReadOnly(Paths.get("cache"));
  Path out=Paths.get(args[0]);Files.createDirectories(out);
  for(int index:new int[]{3,12}) for(int group:Cache.STORE.getIndexes()[index].getTable().getValidArchiveIds()) {
   if(index==3&&group!=517&&group!=579)continue;
   for(int file:Cache.STORE.getIndexes()[index].getTable().getArchives()[group].getValidFileIds()) {
    byte[] raw=Cache.STORE.getIndexes()[index].getFile(group,file);
    if(raw!=null)Files.write(out.resolve(index+"-"+group+"-"+file+".bin"),raw,StandardOpenOption.CREATE_NEW);
   }
  }
 }
}
