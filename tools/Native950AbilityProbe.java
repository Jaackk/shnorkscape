package com.rs.game.player.client;

import com.rs.cache.Cache;
import com.rs.cache.loaders.*;
import com.rs.cache.loaders.rs3.*;
import java.nio.file.*;
import java.util.*;
import java.lang.reflect.Field;

/** Read-only definition inspection; never connects to the live world. */
public final class Native950AbilityProbe {
    public static void main(String[] args) throws Exception {
        Cache.initFlatReadOnly(Paths.get("cache"));
        if(args[0].equals("assets")) {
            List<String> pins=new ArrayList<>();pins.add("# Paired revision950 hub/action-bar cache assets.");
            for(int face:new int[]{1430,1436,1460,1452,1461,1450,1456,1459})for(int file:Cache.STORE.getIndexes()[3].getTable().getArchives()[face].getValidFileIds())pin(pins,3,face,file);
            for(int script:new int[]{6992,6995,11797,8423,8426,8437,7001,5900,1580,7974,7964})pin(pins,12,script,0);
            for(int id:new int[]{10147,6738,6740})pin(pins,17,id>>>8,id&255);
            for(int id:new int[]{14682,14664,14727})pin(pins,22,id>>>5,id&31);
            for(int id:new int[]{14212,14244,14234})pin(pins,20,id>>>7,id&127);
            for(int id:new int[]{1747,1748,1892,1893})pin(pins,2,69,id);
            for(int id:new int[]{114745,114746,114748,114749,114750,83634,79034})pin(pins,16,id>>>8,id&255);
            pin(pins,18,16027>>>7,16027&127);
            Files.write(Paths.get("Ataraxia950/resources/native950/ability-hub-assets.properties"),pins,java.nio.charset.StandardCharsets.UTF_8);
            System.out.println("Wrote "+(pins.size()-1)+" cache asset pins");return;
        }
        if(args[0].equals("findstruct")) {
            for(int group:Cache.STORE.getIndexes()[22].getTable().getValidArchiveIds())
                for(int file:Cache.STORE.getIndexes()[22].getTable().getArchives()[group].getValidFileIds()) {
                    int id=group*32+file;Map<Long,Object> values=RS3GeneralRequirementMap.getMap(id).getValues();
                    if(values!=null)for(Object value:values.values())if(value instanceof String&&((String)value).toLowerCase(Locale.ROOT).contains(args[1].toLowerCase(Locale.ROOT))){System.out.println(id+" "+values);break;}
                }
            return;
        }
        for(int a=1;a<args.length;a++) {
            int id=Integer.parseInt(args[a]);
            if(args[0].equals("npc")){byte[] raw=Cache.STORE.getIndexes()[18].getFile(id>>>7,id&127);NPCDefinitions d=NPCDefinitions.decodeStrict947(id,raw,null);System.out.println(id+" "+d.name+" size="+d.size+" options="+Arrays.toString(d.menuOptions)+" transforms="+Arrays.toString(d.transformTo));continue;}
            if(args[0].equals("bit")) {VarBitDefinitions d=VarBitDefinitions.getClientVarpBitDefinitions(id);System.out.println(id+" base="+d.baseVar+" bits="+d.startBit+".."+d.endBit);continue;}
            if(args[0].equals("region")) {
                int rx=id>>>8,ry=id&255;int archive=com.rs.utils.Utils.getMapArchiveId(rx,ry);
                byte[] data=Cache.STORE.getIndexes()[5].getFile(archive,0);
                if(data==null){System.out.println("Missing region "+id);continue;}
                com.rs.network.io.InputStream in=new com.rs.network.io.InputStream(data,true);
                int object=-1,delta;
                while((delta=in.readSmart2())!=0){object+=delta;int loc=0,step;
                    while((step=in.readUnsignedSmart())!=0){loc+=step-1;new ObjectData(in);
                        ObjectDefinitions d=ObjectDefinitions.getObjectDefinitions(object);
                        if(d.options!=null&&Arrays.stream(d.options).anyMatch(Objects::nonNull))System.out.println(object+" "+d.name+" "+(rx*64+((loc>>>6)&63))+","+(ry*64+(loc&63))+","+(loc>>>12)+" "+Arrays.toString(d.options));
                    }
                }
                continue;
            }
            if(args[0].equals("script")){script(id);continue;}
            if(args[0].equals("enum"))System.out.println(id+" "+RS3ClientScriptMap.getMap(id).getValues());
            else if(args[0].equals("struct"))System.out.println(id+" "+RS3GeneralRequirementMap.getMap(id).getValues());
            else if(args[0].equals("raw")) {
                int file=Integer.parseInt(args[++a]);
                System.out.println(Base64.getEncoder().encodeToString(Cache.STORE.getIndexes()[3].getFile(id,file)));
            } else if(args[0].equals("object")) {
                ObjectDefinitions d=ObjectDefinitions.getObjectDefinitions(id);
                System.out.println(id+" "+d.name+" "+Arrays.toString(d.options));
            } else if(args[0].equals("iface")) {
                for(int file:Cache.STORE.getIndexes()[3].getTable().getArchives()[id].getValidFileIds()) {
                    try {
                        IComponentDefinitions d=new IComponentDefinitions();d.ihash=id<<16|file;
                        d.decode(new com.rs.network.io.InputStream(Cache.STORE.getIndexes()[3].getFile(id,file)));
                        System.out.print(id+":"+file+" type="+d.type+" parent="+d.parentLayer+" ops="+Arrays.toString(d.ops));
                        for(Field f:IComponentDefinitions.class.getFields())if(f.getType()==Object[].class) {
                            Object[] value=(Object[])f.get(d);if(value!=null)System.out.print(" "+f.getName()+"="+Arrays.toString(value));
                        }
                        System.out.println();
                    }catch(Exception e){System.out.println(id+":"+file+" ERROR "+e);}
                }
            }
        }
    }
    private static void pin(List<String> pins,int index,int group,int file)throws Exception {
        byte[] raw=Cache.STORE.getIndexes()[index].getFile(group,file);if(raw==null)throw new IllegalStateException("Missing "+index+"/"+group+"/"+file);
        StringBuilder hash=new StringBuilder();for(byte b:java.security.MessageDigest.getInstance("SHA-256").digest(raw))hash.append(String.format("%02x",b&255));
        pins.add(index+"/"+group+"/"+file+"="+hash);
    }
    private static void script(int id)throws Exception {
        com.google.gson.JsonObject mapping=new com.google.gson.JsonParser().parse(new String(Files.readAllBytes(Paths.get("protocol-analysis/ui-scripts-950-evidence.json")),"UTF-8")).getAsJsonObject().getAsJsonObject("opcodeMap947to950");
        Map<Integer,Integer> inverse=new HashMap<>();
        for(Map.Entry<String,com.google.gson.JsonElement> e:mapping.entrySet())inverse.put(Integer.decode(e.getValue().getAsJsonObject().get("opcode950").getAsString()),Integer.decode(e.getKey()));
        byte[] data=Cache.STORE.getIndexes()[12].getFile(id,0);java.nio.ByteBuffer b=java.nio.ByteBuffer.wrap(data);
        int end=data.length-(b.getShort(data.length-2)&65535)-18;
        while(b.get()!=0){}
        Set<Integer> four=new HashSet<>(Arrays.asList(0x35e,0x592,0x713,0x647,0x412,0xab,0x454,0x73d,0x3f2,0x25a,0x717,0x895,0x267,0x51a,0x56,0x96,0x639,0x1ca,0x195,0x30,0xa2));
        int pc=0;
        while(b.position()<end){int wire=b.getShort()&65535;Integer op=inverse.get(wire);if(op==null)throw new IllegalArgumentException("Unknown opcode "+wire);
            Object arg;
            if(op==0x511){int type=b.get()&255;if(type==2){StringBuilder s=new StringBuilder();byte v;while((v=b.get())!=0)s.append((char)(v&255));arg=s.toString();}else arg=type==1?b.getLong():b.getInt();}
            else arg=four.contains(op)?b.getInt():b.get()&255;
            System.out.println(id+" "+pc+++" "+Integer.toHexString(op)+" "+arg);
        }
        b.position(end+16);int tables=b.get()&255;
        for(int t=0;t<tables;t++){int n=b.getShort()&65535;Map<Integer,Integer> entries=new LinkedHashMap<>();for(int k=0;k<n;k++)entries.put(b.getInt(),b.getInt());System.out.println(id+" SWITCH "+t+" "+entries);}
    }
}
