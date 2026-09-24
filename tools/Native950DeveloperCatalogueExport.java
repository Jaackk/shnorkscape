import com.rs.cache.Cache;
import com.rs.cache.loaders.NPCDefinitions;
import com.rs.cache.loaders.ObjectDefinitions;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** Read-only, offline name/footprint index. No game entities are spawned. */
public final class Native950DeveloperCatalogueExport {
    public static void main(String[] args)throws Exception{
        Cache.initFlatReadOnly(Paths.get("cache"));
        List<String> rows=new ArrayList<>();rows.add("# SHNORKSCAPE 950 developer catalogue: kind,id,name,width,height,level,types");
        int rejected=0;
        for(int index:new int[]{18,16})for(int group:Cache.STORE.getIndexes()[index].getTable().getValidArchiveIds())
            for(int file:Cache.STORE.getIndexes()[index].getTable().getArchives()[group].getValidFileIds()){
                int id=(group<<(index==18?7:8))|file;
                try{
                    String name;int width,height,level;String types="";
                    if(index==18){
                        NPCDefinitions d=NPCDefinitions.decodeStrict947(id,Cache.STORE.getIndexes()[18].getFile(group,file),null);
                        if(id>65534||d.transformTo!=null||d.models==null||d.models.length==0)continue;
                        name=d.name;width=height=d.size;level=d.combatLevel;
                    }else{
                        ObjectDefinitions d=ObjectDefinitions.getObjectDefinitions(id);
                        if(!d.loaded||d.transforms!=null||d.shapes==null||d.models==null||d.shapes.length!=d.models.length)continue;
                        StringJoiner join=new StringJoiner(",");for(int n=0;n<d.shapes.length;n++)if((d.shapes[n]&255)<=22&&d.models[n]!=null&&d.models[n].length>0)join.add(""+(d.shapes[n]&255));
                        types=join.toString();if(types.isEmpty())continue;name=d.name;width=d.sizeX;height=d.sizeY;level=0;
                    }
                    if(name==null||name.trim().isEmpty()||name.equalsIgnoreCase("null")||name.contains("\t")||name.contains("\n")||name.startsWith("<")||width<1||height<1||width>64||height>64)continue;
                    rows.add((index==18?"NPC":"Object")+"\t"+id+"\t"+name+"\t"+width+"\t"+height+"\t"+level+"\t"+types);
                }catch(RuntimeException invalid){rejected++;}
            }
        Files.write(Paths.get(args[0]),rows,StandardCharsets.UTF_8);System.out.println("Exported "+(rows.size()-1)+" concrete definitions; decode failures="+rejected);
    }
}
