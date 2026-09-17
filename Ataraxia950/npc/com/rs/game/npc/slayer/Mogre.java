package com.rs.game.npc.slayer;

import com.rs.cores.CoresManager;
import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.ForceTalk;
import com.rs.game.Graphics;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

import java.util.concurrent.TimeUnit;

public class Mogre extends NPC {

	private static final long serialVersionUID = 7913197895674432949L;

	public Mogre(final int id, final WorldTile tile, final int mapAreaNameHash, final boolean canBeAttackFromOutOfArea, final boolean spawned) {
		super(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
		setForceAgressive(true);
	}

	public static boolean spawnMogre(final Player player, final Item item, final WorldObject object) {
		if (object.getId() == 10089 || object.getId() == 10088 || object.getId() == 10087) {
			useExplosive(player, item, object);
			return true;
		}
		return false;
	}

	private static void useExplosive(final Player player, final Item item, final WorldObject object) {
		if (item.getId() == 6660 || item.getId() == 6664 || item.getId() == 12633) {
			player.setNextAnimation(new Animation(11227));
			player.getInventory().deleteItem(item.getId(), 1);
			player.sendMessage("You throw the " + item.getName() + " into the omnious fishing spot.", true);
			CoresManager.getServiceProvider().executeWithDelay(() -> {
				NPC mogre;
				if (object.getId() == 10087) {
					mogre = new NPC(114, new WorldTile(2987, 3113, 0), -1, true);
				} else if (object.getId() == 10088) {
					mogre = new NPC(114, new WorldTile(2995, 3109, 0), -1, true);
				} else {
					mogre = new NPC(114, new WorldTile(3002, 3118, 0), -1, true);
				}
				mogre.setNextGraphics(new Graphics(1028));
				mogre.applyHit(new Hit(player, item.getId() == 12633 ? Utils.random(150) : Utils.random(50), HitLook.REGULAR_DAMAGE));
				player.sendMessage("The explosives explode on the Mogre.", true);
				mogre.setTarget(player);
				mogre.setNextForceTalk(new ForceTalk("Death to who disturbs me!"));
			}, 3000, TimeUnit.MILLISECONDS);
		}
	}

	@Override
	public void sendDeath(final Entity source) {
		resetWalkSteps();
		WorldTasksManager.schedule(new WorldTask() {
			int loop;

			@Override
			public void run() {
				if (loop == 0) {
					setNextAnimation(new Animation(8576));
				} else if (loop == 2) {
					getCombat().removeTarget();
					drop();
					reset();
					finish();
					stop();
				}
				loop++;
			}
		}, 0, 1);
	}

}