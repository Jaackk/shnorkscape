package com.rs.game.activities.rots.npcs;

import com.rs.game.WorldTile;
import com.rs.game.activities.rots.RiseOfTheSix;
import com.rs.game.activities.rots.effects.Hurricane;
import com.rs.game.activities.rots.effects.Impale;
import com.rs.game.activities.rots.effects.RoTSEffect;
import com.rs.game.activities.rots.effects.WallSlam;
import com.rs.game.player.Player;
import com.rs.utils.Utils;

/**
 * @author Kris | 3. sept 2017 : 23:36.19
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>}
 */
public class GuthanNPC extends RiseOfTheSixNPC {

	private static final long serialVersionUID = 1426379276384849758L;
	
	public GuthanNPC(int id, WorldTile tile, int mapAreaNameHash, boolean canBeAttackFromOutOfArea, boolean spawned, RiseOfTheSix instance) {
		super(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned, instance);
	}
	
	private Player impaledPlayer;
	private RoTSEffect lastEffect;
	
	public void setLastEffect(RoTSEffect lastEffect) {
		this.lastEffect = lastEffect;
	}
	
	public void setImpaledPlayer(Player player) {
		this.impaledPlayer = player;
	}
	
	public Player getImpaledPlayer() {
		return impaledPlayer;
	}
	
	@Override
	public void processNPC() {
		super.processNPC();
	}
	
	@Override
	public double getMeleePrayerMultiplier() {
		return 0.5;
	}

	@Override
	public RoTSEffect generateEffect(Player target) {
		if (lastEffect == null) {
			final int randomEffect = Utils.random(2);
			switch(randomEffect) {
			case 0:
				return new Hurricane(10, this, null);
			default:
				return new Impale(1, this, target);
			}
		}
		if (lastEffect instanceof WallSlam)
			return new Hurricane(10, this, null);
		return new Impale(1, this, null);
	}

}
