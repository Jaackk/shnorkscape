package com.rs.game;

import com.rs.utils.Utils;

public class Projectile {

	private final WorldTile from;
    private final WorldTile to;
	private final boolean adjustFlyingHeight;
    private boolean adjustSenderHeight;
	private final int senderBodyPartId;
    private final int graphicId;
    private final int startHeight;
    private final int endHeight;
    private final int startTime;
    private int endTime;
    private final int slope;
    private final int angle;
	private double speed;
	private boolean newProjectile;

	public Projectile(WorldTile from, WorldTile to, boolean adjustFlyingHeight, boolean adjustSenderHeight, int senderBodyPartId, int graphicId, int startHeight, int endHeight, int startTime, int endTime, int slope, int angle, double speed) {
		this.from = from;
		this.to = to;
		this.adjustFlyingHeight = adjustFlyingHeight;
		this.senderBodyPartId = senderBodyPartId;
		this.graphicId = graphicId;
		this.startHeight = startHeight;
		this.endHeight = endHeight;
		this.startTime = startTime;
		this.endTime = endTime;
		this.slope = slope;
		this.angle = angle;
		this.speed = speed;
	}

	public Projectile(WorldTile from, WorldTile to, boolean adjustFlyingHeight, boolean adjustSenderHeight, int senderBodyPartId, int graphicId, int startHeight, int endHeight, int startTime, int endTime, int slope, int angle) {
		this.from = from;
		this.to = to;
		this.adjustFlyingHeight = adjustFlyingHeight;
		this.senderBodyPartId = senderBodyPartId;
		this.graphicId = graphicId;
		this.startHeight = startHeight;
		this.endHeight = endHeight;
		this.startTime = startTime;
		this.endTime = endTime;
		this.slope = slope;
		this.angle = angle;
	}

	public WorldTile getFrom() {
		return from;
	}

	public WorldTile getTo() {
		return to;
	}

	public boolean isAdjustFlyingHeight() {
		return adjustFlyingHeight;
	}

	public boolean isAdjustSenderHeight() {
		return adjustSenderHeight;
	}

	public int getSenderBodyPart() {
		return senderBodyPartId;
	}

	public int getGraphicId() {
		return graphicId;
	}

	public int getStartHeight() {
		return startHeight;
	}

	public int getEndHeight() {
		return endHeight;
	}

	public int getStartTime() {
		return startTime;
	}

	public int getEndTime() {
		return endTime;
	}

	public void setEndTime(int endTime) {
		this.endTime = endTime;
	}

	public int getSlope() {
		return slope;
	}

	public int getAngle() {
		return angle;
	}

	public double getSpeed() {
		return speed;
	}

	public boolean isNewProjectile() {
		return newProjectile;
	}

	public void setNewProjectile(boolean newProjectile) {
		this.newProjectile = newProjectile;
	}

	public int getSpeed2() {
		return (Utils.getDistance(getFrom().getX(), getFrom().getY(), getTo().getX(), getTo().getY()) * 30 / (((getEndTime() - getStartTime()) / 10) < 1 ? 1 : ((getEndTime() - getStartTime()) / 10))) + getStartTime();
	}

	public int getDuration() {
		return getSpeed2() * 10;
	}

}
