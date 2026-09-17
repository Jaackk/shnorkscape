package com.rs.game.player.client;

import com.rs.cache.Cache;
import com.rs.cache.loaders.rs3.RS3ClientScriptMap;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.util.*;

/** Read-only catalogue from the paired950 quest definitions and the native list's sparse enum keys. */
public final class Native950QuestCatalog {
    private static Object loadedStore;
    private static Map<Integer,Quest> quests=Collections.emptyMap();
    public static final class Quest {
        public final int key,id;
        public String name="",sortName="";
        public int points,difficulty,pointRequirement;
        public boolean members;
        final List<int[]> varps=new ArrayList<>(),varbits=new ArrayList<>();
        final List<int[]> skills=new ArrayList<>();
        final List<Integer> prerequisites=new ArrayList<>();
        final Map<Integer,Object> params=new HashMap<>();
        private Quest(int key,int id){this.key=key;this.id=id;}
        public String text(int param){Object v=params.get(param);return v instanceof String?(String)v:"";}
        public boolean miniquest(){return number(7889,0)==1;}
        public int number(int param,int fallback){Object v=params.get(param);return v instanceof Integer?(Integer)v:fallback;}
    }
    private Native950QuestCatalog(){}
    public static synchronized void verify(){
        if(Cache.STORE==null||!Cache.isFlatReadOnly())throw new IllegalStateException("Quest UI requires the paired950 cache");
        if(loadedStore==Cache.STORE)return;
        Properties pins=new Properties();
        try(InputStream in=Native950QuestCatalog.class.getResourceAsStream("/native950/quest-ui-950.properties")){
            if(in==null)throw new IllegalStateException("Missing950 quest bindings");pins.load(in);
            for(String key:pins.stringPropertyNames())if(key.startsWith("pin.")){
                String[] k=key.split("\\.");byte[] raw=Cache.STORE.getIndexes()[Integer.parseInt(k[1])].getFile(Integer.parseInt(k[2]),Integer.parseInt(k[3]));
                if(raw==null)throw new IllegalStateException("Missing quest binding "+key);
                StringBuilder hash=new StringBuilder();for(byte b:MessageDigest.getInstance("SHA-256").digest(raw))hash.append(String.format("%02x",b&255));
                if(!hash.toString().equals(pins.getProperty(key)))throw new IllegalStateException("Changed quest binding "+key);
            }
        }catch(java.io.IOException|java.security.NoSuchAlgorithmException ex){throw new IllegalStateException(ex);}
        Map<Integer,Quest> out=new TreeMap<>();RS3ClientScriptMap map=RS3ClientScriptMap.getMap(2252);
        if(map.getValues()==null)throw new IllegalStateException("Missing native quest enum");
        for(Map.Entry<Long,Object> e:map.getValues().entrySet()){
            if(!(e.getValue() instanceof Integer))throw new IllegalStateException("Invalid native quest row");
            int key=Math.toIntExact(e.getKey()),id=(Integer)e.getValue();if(key<0||key>65534)throw new IllegalStateException("Invalid quest key");
            Quest q=decode(key,id,Cache.STORE.getIndexes()[2].getFile(35,id));
            if(q.name.isEmpty()||q.number(1345,-1)!=key)throw new IllegalStateException("Quest identity differs for "+key);
            out.put(key,q);
        }
        if(out.size()!=362)throw new IllegalStateException("Paired950 quest catalogue changed");
        quests=Collections.unmodifiableMap(out);loadedStore=Cache.STORE;
    }
    public static Collection<Quest> all(){verify();return quests.values();}
    public static Quest get(int key){verify();return quests.get(key);}
    static Quest byDefinition(int id){for(Quest q:all())if(q.id==id)return q;return null;}
    public static int maximumPoints(){int result=0;for(Quest q:all())result+=q.points;return result;}
    static Quest decode(int key,int id,byte[] raw){
        Reader r=new Reader(raw);Quest q=new Quest(key,id);
        while(true){int op=r.u(1);if(op==0){if(r.p!=raw.length)throw new IllegalStateException("Trailing quest bytes "+id);return q;}
            switch(op){
            case 1:q.name=r.prefixed();break;case 2:q.sortName=r.prefixed();break;
            case 3:case 4:case 22:{List<int[]> rows=op==3?q.varps:q.varbits;int n=r.u(1);for(int i=0;i<n;i++)rows.add(new int[]{r.u(op==22?3:2),r.u(4),r.u(4)});break;}
            case 5:case 15:{int v=r.u(2);if(op==15)q.pointRequirement=v;break;}
            case 6:r.u(1);break;case 7:q.difficulty=r.u(1);break;case 8:q.members=true;break;case 9:q.points=r.u(1);break;
            case 10:{int n=r.u(1);for(int i=0;i<n;i++)r.u(4);break;}case 12:r.u(4);break;
            case 13:{int n=r.u(1);for(int i=0;i<n;i++)q.prerequisites.add(r.u(2));break;}
            case 14:{int n=r.u(1);for(int i=0;i<n;i++)q.skills.add(new int[]{r.u(1),r.u(1)});break;}
            case 17:r.u((r.raw[r.p]&255)>=128?4:2);break;
            case 249:{int n=r.u(1);for(int i=0;i<n;i++){boolean str=r.u(1)==1;int param=r.u(3);q.params.put(param,str?r.string():r.u(4));}break;}
            default:throw new IllegalStateException("Unknown950 quest opcode "+op+" for "+id);
            }
        }
    }
    private static final class Reader {
        final byte[] raw;int p;
        Reader(byte[] raw){if(raw==null)throw new IllegalStateException("Missing quest definition");this.raw=raw;}
        int u(int n){if(p+n>raw.length)throw new IllegalStateException("Truncated quest");int v=0;while(n-->0)v=(v<<8)|(raw[p++]&255);return v;}
        String string(){int start=p;while(p<raw.length&&raw[p]!=0)p++;if(p==raw.length)throw new IllegalStateException("Unterminated quest string");String s=new String(raw,start,p-start,Charset.forName("windows-1252"));p++;return s;}
        String prefixed(){if(u(1)!=0)throw new IllegalStateException("Wrong quest string prefix");return string();}
    }
}
