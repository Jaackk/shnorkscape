import com.rs.cache.Cache;import com.rs.game.player.client.Native950ProductionUiCatalog;import java.nio.file.Paths;
public class VerifyProductionUiCatalog950 {
 public static void main(String[] args)throws Exception {
  Cache.initFlatReadOnly(Paths.get(args[0]));Native950ProductionUiCatalog.verify();
  if(Native950ProductionUiCatalog.categories().size()!=525)throw new AssertionError("category count");
  for(int item:new int[]{50,52,53,877,9140,1601,1635,1759,2349,2355,1117,44838,12047}) {
   StringBuilder s=new StringBuilder();for(Native950ProductionUiCatalog.Category c:Native950ProductionUiCatalog.forProduct(item))s.append(c.id).append('/').append(c.rootId).append(',');
   System.out.println(item+" categories "+s);
  }
  for(int display:new int[]{54895,52,877,9140})System.out.println("display "+display+" output="+Native950ProductionUiCatalog.outputId(display)+" skill="+Native950ProductionUiCatalog.skill(display));
 }
}