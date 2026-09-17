package com.rs.game.player.client;

import com.rs.cache.Cache;
import com.rs.cache.loaders.ItemDefinitions;
import com.rs.cache.loaders.rs3.RS3ClientScriptMap;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.*;

/** Paired950 native Make-X categories. Cache ordinal positions are part of the wire contract. */
public final class Native950ProductionUiCatalog {
    private Native950ProductionUiCatalog() { }
    public static final class Category {
        public final int id, rootId, namesId;
        public final String title;
        /** Do not reorder: the client returns these original enum ordinals. */
        public final int[] displayIds;
        private Category(int id,String title,int rootId,int namesId,int[] products) {
            this.id=id;this.title=title;this.rootId=rootId;this.namesId=namesId;this.displayIds=products.clone();
        }
        public int[] displayIds(){return displayIds.clone();}
        public boolean containsDisplay(int item){for(int id:displayIds)if(id==item)return true;return false;}
    }
    private static final Properties DATA=load();
    private static final Map<Integer,Category> CATEGORIES=parse();
    private static final List<Category> LIST=Collections.unmodifiableList(new ArrayList<>(CATEGORIES.values()));
    private static Object verifiedStore;
    private static final Map<Integer,Integer> OUTPUTS=new HashMap<>();
    private static final Map<Integer,List<Category>> PRODUCTS=new HashMap<>();
    private static Properties load() {
        Properties p=new Properties();
        try(InputStream in=Native950ProductionUiCatalog.class.getResourceAsStream("/native950/production-ui-catalog-950.properties")) {
            if(in==null)throw new IllegalStateException("Missing paired950 production UI catalog");
            p.load(in);return p;
        }catch(java.io.IOException ex){throw new IllegalStateException(ex);}
    }
    private static Map<Integer,Category> parse() {
        if(!"1".equals(DATA.getProperty("catalog.version")))throw new IllegalStateException("Unknown production UI catalog");
        Map<Integer,Category> result=new LinkedHashMap<>();
        for(String key:DATA.getProperty("categories","").split(",")) {
            int id=Integer.parseInt(key);String prefix="category."+id+".";
            String[] values=DATA.getProperty(prefix+"products").split(",");int[] ids=new int[values.length];
            for(int i=0;i<ids.length;i++){ids[i]=Integer.parseInt(values[i]);if(ids[i]<1||ids[i]>65534)throw new IllegalStateException("Invalid product "+ids[i]);}
            Category category=new Category(id,DATA.getProperty(prefix+"title"),Integer.parseInt(DATA.getProperty(prefix+"root")),Integer.parseInt(DATA.getProperty(prefix+"names")),ids);
            if(result.put(id,category)!=null)throw new IllegalStateException("Duplicate category "+id);
        }
        return Collections.unmodifiableMap(result);
    }
    /** Verify all pinned enum bytes once per mounted cache, before native ordinal dispatch is admitted. */
    public static synchronized void verify() {
        if(Cache.STORE==null||!Cache.isFlatReadOnly())throw new IllegalStateException("Production UI requires paired950 cache");
        if(verifiedStore==Cache.STORE)return;
        for(String key:DATA.stringPropertyNames())if(key.startsWith("enum.")) {
            int id=Integer.parseInt(key.substring(5));
            byte[] bytes=Cache.STORE.getIndexes()[17].getFile(id>>>8,id&255);
            try {
                if(bytes==null)throw new IllegalStateException("Missing production enum "+id);
                StringBuilder hash=new StringBuilder();for(byte value:MessageDigest.getInstance("SHA-256").digest(bytes))hash.append(String.format("%02x",value&255));
                if(!hash.toString().equals(DATA.getProperty(key)))throw new IllegalStateException("Mismatched production enum "+id);
            }catch(java.security.NoSuchAlgorithmException ex){throw new IllegalStateException(ex);}
        }
        OUTPUTS.clear();PRODUCTS.clear();verifiedStore=Cache.STORE;
    }
    public static List<Category> categories(){if(Cache.STORE==null||!Cache.isFlatReadOnly())return Collections.emptyList();verify();return LIST;}
    public static Category byId(int id){if(Cache.STORE==null||!Cache.isFlatReadOnly())return null;verify();return CATEGORIES.get(id);}
    /** Some UI-only product records point at the actual inventory reward through parameter2989. */
    public static synchronized int outputId(int displayId) {
        if(displayId<1||displayId>65534||Cache.STORE==null||!Cache.isFlatReadOnly())return -1;
        verify();Integer cached=OUTPUTS.get(displayId);if(cached!=null)return cached;
        ItemDefinitions item=ItemDefinitions.getItemDefinitions(displayId);
        int output=item.loaded?item.getCSOpcode(2989,displayId):-1;
        if(output<1||output>65534)output=item.loaded?displayId:-1;
        OUTPUTS.put(displayId,output);return output;
    }
    /** Uses the cache's one-based skill value, never a guessed menu-specific index. */
    public static int skill(int displayId) {
        if(outputId(displayId)<1)return -1;
        int oneBased=ItemDefinitions.getItemDefinitions(displayId).getCSOpcode(2640,0);
        return oneBased<1||oneBased>29?-1:RS3ClientScriptMap.getMap(681).getIntValue(oneBased);
    }
    public static synchronized List<Category> forProduct(int output) {
        if(output<1||output>65534||Cache.STORE==null||!Cache.isFlatReadOnly())return Collections.emptyList();verify();
        List<Category> result=PRODUCTS.get(output);if(result!=null)return result;
        result=new ArrayList<>();for(Category category:LIST)for(int display:category.displayIds)if(display==output||outputId(display)==output){result.add(category);break;}
        result=Collections.unmodifiableList(result);PRODUCTS.put(output,result);return result;
    }
}