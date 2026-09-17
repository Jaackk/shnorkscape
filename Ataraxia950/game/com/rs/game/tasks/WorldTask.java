package com.rs.game.tasks;

import com.rs.game.tasks.WorldTasksManager.WorldTaskInformation;

public abstract class WorldTask implements Runnable {

	protected boolean needRemove;
	protected WorldTaskInformation taskInfo;

	public void onStop() {

	}
	public void onProcess() {

	}
	public final void stop() {
		if(!needRemove) {
			needRemove = true;
			onStop();
		}
	}

	public boolean isCancelled() {
		return needRemove;
	}
	public WorldTaskInformation getTaskInfo() {
		return taskInfo;
	}
}
