package com.rs.game.player.actions;

import com.rs.game.player.Player;

public final class ActionManager {

	private final Player player;
	private Action action;
	private int actionDelay;

	public ActionManager(Player player) {
		this.player = player;
	}

	public void addActionDelay(int skillDelay) {
		this.actionDelay += skillDelay;
	}

	public void forceStop() {
		if (action == null)
			return;
		action.stop(player);
		action = null;
		if(player.stringJewelleryAction != null) {
			player.stringJewelleryAction.stop();
			player.stringJewelleryAction = null;
		}
	}

	public Action getAction() {
		return action;
	}

	public int getActionDelay() {
		return actionDelay;
	}

	public void setActionDelay(int skillDelay) {
		this.actionDelay = skillDelay;
	}

	public boolean hasSkillWorking() {
		return action != null;
	}

	public void process() {
		if (action != null) {
			if (player.isDead() || player.isAFK())
				forceStop();
			else if (!action.process(player))
				forceStop();
		}
		if (actionDelay > 0) {
			actionDelay--;
			return;
		}
		if (action == null)
			return;
		int delay = action.processWithDelay(player);
		if (delay == -1) {
			forceStop();
			return;
		}
		actionDelay += delay;
	}

	public boolean setAction(Action skill) {
		forceStop();
		if (!skill.start(player))
			return false;
		this.action = skill;
		return true;
	}
}