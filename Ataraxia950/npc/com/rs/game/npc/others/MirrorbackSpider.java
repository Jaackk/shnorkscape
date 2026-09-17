package com.rs.game.npc.others;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.Graphics;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

public class MirrorbackSpider extends NPC {

	private static final long serialVersionUID = 1747512557947460714L;
	private final Player owner;
	private int ticks = 17;
	
	public MirrorbackSpider(int id, WorldTile tile, int mapAreaNameHash, boolean canBeAttackFromOutOfArea, boolean spawned, Player owner) {
		super(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
		this.owner = owner;
		setCannotMove(true);
		setNextFaceWorldTile(new WorldTile(owner));
		World.sendGraphics(null, new Graphics(4982), new WorldTile(this));
		setNextAnimation(new Animation(24054));
		WorldTasksManager.schedule(new WorldTask() {
			@Override
			public void run() {
				setCannotMove(false);
			}
		});
	}
	
	@Override
	public void sendDeath(final Entity source) {
		resetWalkSteps();
		owner.mirrorback = null;
		getCombat().removeTarget();
		setNextAnimation(null);	
		WorldTasksManager.schedule(new WorldTask() {
			private int loop;
			@Override
			public void run() {
				if (loop == 0)
					setNextAnimation(new Animation(getCombatDefinitions().getDeathEmote()));
				else if (loop >= 2) {
					reset();
					finish();
					stop();
				}
				loop++;
			}
		}, 0, 1);
	}

	@Override
	public void processNPC() {
		if (isDead() || isCannotMove())
			return;
		ticks--;
		if (ticks == 0)
			this.sendDeath(null);
		if (!getCombat().process())
			sendFollow();
	}
	
	private void sendFollow() {
		setRun(owner.getNextRunDirection() != -1);
		if (getLastFaceEntity() != owner.getClientIndex())
			setNextFaceEntity(owner);
		if (getFreezeDelay() >= Utils.currentTimeMillis())
			return;
		int size = getSize();
		int distanceX = owner.getX() - getX();
		int distanceY = owner.getY() - getY();
		if (distanceX < size && distanceX > -1 && distanceY < size && distanceY > -1 && !owner.hasWalkSteps() && !hasWalkSteps()) {
			resetWalkSteps();
			if (!addWalkSteps(owner.getX() + 1, getY())) {
				resetWalkSteps();
				if (!addWalkSteps(owner.getX() - size, getY())) {
					resetWalkSteps();
					if (!addWalkSteps(getX(), owner.getY() + 1)) {
						resetWalkSteps();
						addWalkSteps(getX(), owner.getY() - size);
					}
				}
			}
			return;
		}
		if ((!clipedProjectile(owner, true)) || distanceX > size || distanceX < -1 || distanceY > size || distanceY < -1) {
			resetWalkSteps();
			addWalkStepsInteract(owner.getX(), owner.getY(), getRun() ? 2 : 1, size, true);
			return;
		} else
			resetWalkSteps();
	}
	
}
