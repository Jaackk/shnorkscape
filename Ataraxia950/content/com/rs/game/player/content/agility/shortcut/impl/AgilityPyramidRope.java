package com.rs.game.player.content.agility.shortcut.impl;

import com.rs.game.WorldTile;
import com.rs.game.player.Player;
import com.rs.game.player.content.FadingScreen;
import com.rs.game.player.content.agility.shortcut.Shortcut;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;

public class AgilityPyramidRope implements Shortcut {

	private static final WorldTile START = new WorldTile(3382, 2823, 1);
	private static final WorldTile END = new WorldTile(3382, 2826, 0);
	
	@Override
	public void succeed(Player player, boolean start) {
		player.lock();
		player.setNextFaceWorldTile(start ? END : START);
		player.sendMessage("You climb " + (start ? "down" : "up") + " the rope...", true);
		player.getPackets().sendSound(start ? 6823 : 6824, 0, 2);
		WorldTasksManager.schedule(new WorldTask() {
            @Override
            public void run() {
                final long time = FadingScreen.fade(player, 1000);
                FadingScreen.unfade(player, time, 500, new Runnable() {
                    @Override
                    public void run() {
                        player.setNextWorldTile(start ? END : START);
                        player.unlock();
                    }
                });
            }

        });
	}

	@Override
	public int getLevel() {
		return 40;
	}

	@Override
	public int[] getObjectIds() {
		return new int[] { 28488, 28490 };
	}

	@Override
	public WorldTile getStart() {
		return START;
	}

	@Override
	public WorldTile getEnd() {
		return END;
	}

}
