import com.rs.cache.Cache;
import com.rs.cache.loaders.ItemDefinitions;
import java.nio.file.Path;
import java.util.Arrays;
public final class InventoryMenuMetadataProbe950 {
 public static void main(String[] args) throws Exception {
  Cache.initFlatReadOnly(Path.of(args[0]));
  for(int i=1;i<args.length;i++) {
   int id=Integer.parseInt(args[i]);byte[] b=Cache.STORE.getIndexes()[19].getFile(id>>>8,id&255);
   ItemDefinitions d=ItemDefinitions.decodeStrict947(id,b,null);
   System.out.println(id+"\t"+d.getName()+"\tcategory="+d.itemCategory+"\t6799="+d.getCSOpcode(6799)+"\t4840="+d.getCSOpcode(4840)+"\tcert="+d.certId+":"+d.certTemplateId+"\t"+Arrays.toString(d.inventoryOptions));
  }
 }
}
