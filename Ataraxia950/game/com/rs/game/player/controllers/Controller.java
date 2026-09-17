package com.rs.game.player.controllers;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.Hit;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.item.floor.FloorItem;
import com.rs.game.npc.NPC;
import com.rs.game.player.ActionBar.Shortcut;
import com.rs.game.player.Player;
import com.rs.game.player.content.Foods.Food;
import com.rs.game.player.content.Pots.Pot;

public abstract class Controller {

	// private static final long serialVersionUID = 8384350746724116339L;

	protected Player player;


	public boolean canAddInventoryItem(int itemId, int amount) {
		return true;
	}

	/**
	 * after the normal checks, extra checks, only called when you start trying to attack//Used in magic and shit like multi-hitting spells.
	 */
	public boolean processPlayerOption1(Entity target) {
		return true;
	}

	public boolean canDeleteInventoryItem(int itemId, int amount) {
		return true;
	}

	public boolean canProcessMovement(final int x, final int y) {
		return true;
	}

	public boolean canDropItem(Item item) {
		return true;
	}

	public boolean canEat(Food food) {
		return true;
	}

	public boolean canEquip(int slotId, int itemId) {
		return true;
	}

	/**
	 * hits as ice barrage and that on multi areas
	 */
	public boolean canHit(Entity entity) {
		return true;
	}

	/**
	 * return can move that step
	 */
	public boolean canMove(int dir) {
		return true;
	}

	/**
	 * Processes key press.
	 */
	public boolean processKeyPress(int key) {
		return true;
	}

	public boolean canPlayerOption1(Player target) {
		return true;
	}

	public boolean canPot(Pot pot) {
		return true;
	}

	public boolean canSummonFamiliar() {
		return true;
	}

	/**
	 * check if you can use commands in the controller
	 */
	public boolean processCommand(String s, boolean b, boolean c) {
		return true;
	}

	public boolean canUseItemOnItem(Item itemUsed, Item usedWith) {
		return true;
	}

	public boolean canWalk() {
		return true;
	}

	/**
	 * return can set that step
	 */
	public boolean checkWalkStep(int lastX, int lastY, int nextX, int nextY) {
		return true;
	}

	public void forceClose() {
		player.getActivityTimersManager().resetTimers();
	}

	public final Object[] getArguments() {
		return player.getControlerManager().getLastControlerArguments();
	}

	public final void setArguments(Object[] objects) {
		player.getControlerManager().setLastControlerArguments(objects);
	}

	public Player getPlayer() {
		return player;
	}

	public final void setPlayer(Player player) {
		this.player = player;
	}


	/**
	 * after the normal checks, extra checks, only called when you attacking
	 */
	public boolean keepCombating(boolean mainHand, Entity target) {
		return true;
	}

	/**
	 * return remove controler
	 */
	public boolean login() {
		return true;
	}

	/**
	 * return true of you want to call the forceClose() method, else return false
	 */
	public boolean logout() {
		return true;
	}

	/**
	 * called once teleport is performed
	 */
	public void magicTeleported(int type) {
		player.getActivityTimersManager().resetTimers();
	}

	public void moved() {

	}

	/**
	 * processes every game tick, usualy not used
	 */
	public void process() {

	}

	public boolean processItemOnNPC(NPC npc, Item item) {
		return true;
	}

	public boolean processItemOnPlayer(Player player, int itemId) {
		return true;
	}

	/**
	 * return can teleport
	 */
	public boolean processItemTeleport(WorldTile toTile) {
		return true;
	}

	/**
	 * return can teleport
	 */
	public boolean processMagicTeleport(WorldTile toTile) {

		return true;
	}

	public boolean processMoneyPouch() {
		return true;
	}

	/**
	 * return process normaly
	 */
	public boolean processNPCClick1(NPC npc) {
		return true;
	}

	/**
	 * return process normaly
	 */
	public boolean processNPCClick2(NPC npc) {
		return true;
	}

	/**
	 * return process normaly
	 */
	public boolean processNPCClick3(NPC npc) {
		return true;
	}

	/**
	 * return process normaly
	 */
	public boolean processNPCClick4(NPC npc) {
		return true;
	}

	public boolean processNPCExamine(NPC npc) {
		return true;
	}

	/**
	 * return process normaly
	 */
	public boolean processObjectClick1(WorldObject object) {
		return true;
	}

	/**
	 * return process normaly
	 */
	public boolean processObjectClick2(WorldObject object) {
		return true;
	}

	/**
	 * return process normaly
	 */
	public boolean processObjectClick3(WorldObject object) {
		return true;
	}

	public boolean processObjectClick5(WorldObject object) {
		return true;
	}

	public boolean processObjectExamine(WorldObject object) {
		return true;
	}

	/**
	 * return can teleport
	 */
	public boolean processObjectTeleport(WorldTile toTile) {
		return true;
	}

	public final void removeControler() {
		player.getControlerManager().removeControlerWithoutCheck();
		player.getActivityTimersManager().resetTimers();
	}

	/**
	 * return let default death
	 */
	public boolean sendDeath() {
		return true;
	}

	public void sendInterfaces() {

	}

	public abstract void start();

	public void trackXP(int skillId, int addedXp) {

	}

	/**
	 * return can use script
	 */
	public boolean useDialogueScript(Object key) {
		return true;
	}

	public boolean handleItemOnObject(WorldObject object, Item item) {
		return true;
	}

	public boolean processObjectClick4(WorldObject object) {
		return true;
	}

	/**
	 * return process normaly
	 */
	public boolean canTakeItem(FloorItem item) {
		return true;
	}

	/**
	 * return process normaly
	 *
	 * @param slotId2 TODO
	 */
	public boolean processButtonClick(int interfaceID, int componentID, int slotId, int itemID, int packetID) {
		return true;
	}

	public boolean canRemoveEquip(int slotId, int itemId) {
		return true;
	}

	public void processNPCDeath(NPC npc) {

	}

	public void processIncommingHit(Hit hit, Entity target) {

	}

	public void processIngoingHit(Hit hit) {

	}

	public boolean processPlayerOption4(Player p2) {
		return true;
	}

	public boolean handleItemOption1(Item item, int itemId, int slotId) {
		return true;
	}

	public boolean canDoAnimation(Animation animation) {
		return true;
	}

	@Override
	public String toString() {
		return getClass().getCanonicalName();
	}

    public boolean canUseAbility(Shortcut shortcut) {
        return true;
    }

    public boolean canLogout() {
        return true;
    }


}