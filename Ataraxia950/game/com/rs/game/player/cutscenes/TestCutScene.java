package com.rs.game.player.cutscenes;

import com.rs.cores.CoresManager;
import com.rs.game.Animation;
import com.rs.game.WorldTile;
import com.rs.game.player.Player;
import com.rs.game.player.cutscenes.actions.CutsceneAction;
import com.rs.game.player.cutscenes.actions.LookCameraAction;
import com.rs.game.player.cutscenes.actions.PosCameraAction;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class TestCutScene extends Cutscene  {

//	public static final NPC ARAXXOR = new NPC(20998, new WorldTile(4520, 6247, 1), -1, true);
	
	@Override
	public CutsceneAction[] getActions(Player player) {
		List<CutsceneAction> actions = new ArrayList<>();
//		ARAXXOR.setNextFaceWorldTile(new WorldTile(player));
//		ARAXXOR.setNextAnimation(new Animation(24111));
		CoresManager.getServiceProvider().executeWithDelay(new Runnable() {

			@Override
			public void run() {
				player.setNextAnimation(new Animation(24112));
			}
			
		}, 750, TimeUnit.MILLISECONDS);
		
		WorldTile initialFaceTile = new WorldTile(4535, 6249, 1);
		WorldTile switchLookTile = new WorldTile(4514, 6249, 1);
		actions.add(new PosCameraAction(getX(player, initialFaceTile.getX()), getY(player, initialFaceTile.getY()), 2800, -1));
		actions.add(new LookCameraAction(getX(player, switchLookTile.getX()), getY(player, switchLookTile.getY()), 2000, 10));
		return actions.toArray(new CutsceneAction[actions.size()]);
	}

	@Override
	public boolean hiddenMinimap() {
		
		return false;
	}

}
