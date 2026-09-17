package com.rs.utils;

import java.io.IOException;

import com.rs.cache.Cache;
import com.rs.cache.filestore.utils.Constants;

public class WorldMapAdder {
	
	public static void main(String[] args) {
		try {
			Cache.init();
		} catch (IOException e) {
			Logger.getGlobal().catching(e);
		}
		
		try {
			Cache.STORE.addIndex(false, true, Constants.GZIP_COMPRESSION);
		} catch (IOException e) {
			Logger.getGlobal().catching(e);
		}
	} 

}
