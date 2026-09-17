package com.rs.tools;

import java.io.IOException;

import com.rs.Settings;
import com.rs.cache.filestore.store.Store;
import com.rs.utils.Logger;


/**
 * ataraxia-server
 * paolo 05/09/2019
 * #Shnek6969
 */
public class IndexPacker {

    public static void main(String[] args) throws IOException {
        Store toPack = new Store("C:\\Users\\conno\\Desktop\\cachetest\\");
        Store from = new Store(Settings.CACHE_PATH);
        int[] indexes = {0};
        for(int i :  indexes){
          boolean result =  toPack.getIndexes()[i].packIndex(from);
          Logger.getGlobal().info("Packing index "+i+", pack result ->"+result);
        }
    }
}
