import com.rs.cache.Cache;
import com.rs.cache.loaders.ItemDefinitions;
import com.rs.game.item.Item;
import com.rs.game.player.client.*;
import java.nio.file.*;
import java.util.*;
public class ProductionMenuProducts950 {
 public static void main(String[] args) throws Exception {
  Cache.initFlatReadOnly(Paths.get(args[0]));
  List<Native950Production.Recipe> all=new ArrayList<>();
  all.addAll(Native950Production.recipes());all.addAll(Native950Crafting.recipes());all.addAll(Native950Smithing.recipes());all.addAll(Native950Smelting.recipes());
  List<String> lines=new ArrayList<>();lines.add("product\tskill\tlevel\txp\tquantity\tparameter2640\tparameter2641\tlabel");
  for(Native950Production.Recipe r:all)for(Item out:r.produced()) {
   ItemDefinitions d=ItemDefinitions.getItemDefinitions(out.getId());
   lines.add(out.getId()+"\t"+r.skill+"\t"+r.level+"\t"+r.xp+"\t"+out.getAmount()+"\t"+d.getCSOpcode(2640,-1)+"\t"+d.getCSOpcode(2641,-1)+"\t"+r.label);
  }
  Files.write(Paths.get(args[1]),lines,java.nio.charset.StandardCharsets.UTF_8);
  System.out.println("Wrote "+(lines.size()-1)+" production products");
 }
}