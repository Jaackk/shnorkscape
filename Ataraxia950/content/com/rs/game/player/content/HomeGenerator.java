package com.rs.game.player.content;

import com.rs.cores.CoresManager;
import com.rs.game.MapBuilder;
import com.rs.game.WorldTile;
import com.rs.game.player.Player;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * really shit code
 * HomeGenerator.java | 4:06:01 PM
 * @author nigga.land
 * @date Aug 9, 2018
 */
public class HomeGenerator {
	
	public static int[] index, chunks;
	
	public static void generate(final Player player, final String name) {
		final Block block = Block.ALL.get(name);
		if(block == null) {
			return;
		}
		
		chunks = MapBuilder.findEmptyChunkBound(24, 24);
		index = new int[] { chunks[0]+1, chunks[1]+1};

		MapBuilder.copy2RatioSquare(block.getX(), block.getY(), index[0]*8, index[1]*8, 2);
		CoresManager.getServiceProvider().executeWithDelay(() -> player.setLocation(new WorldTile(index[0]*8, index[1]*8, 0)), 6000, TimeUnit.MILLISECONDS);
	}
	
	public enum Block {
		
		LANTERN("lantern", new int[] { 406, 814, 2 }),
		B("1", new int[] { 1, 1, 1}),
		;
		
		public static final Map<String, Block> ALL = new HashMap<String, Block>();
		private static final Block[] VALUES = values();
		
		@Getter
		private final String name;
		@Getter
		private final int[] config;
		
		Block(final String name, final int[] config) {
			this.name = name;
			this.config = config;
		}
		
		public int getX() {
			return config[0];
		}
		
		public int getY() {
			return config[1];
		}

		public int getBlocks() {
			return config[2];
		}

		
		static { 
			for(final Block block : VALUES) {
				ALL.put(block.getName(), block);
			}
		}
		
	}

}
