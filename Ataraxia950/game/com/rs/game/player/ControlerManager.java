package com.rs.game.player;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.Hit;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.item.floor.FloorItem;
import com.rs.game.npc.NPC;
import com.rs.game.player.ActionBar.Shortcut;
import com.rs.game.player.content.Foods.Food;
import com.rs.game.player.content.Pots.Pot;
import com.rs.game.player.client.ClientProfile;
import com.rs.game.player.controllers.ImpossibleJad;
import com.rs.game.player.controllers.Controller;
import com.rs.game.player.controllers.ControllerHandler;
import lombok.Getter;

import java.io.Serializable;

public final class ControlerManager implements Serializable {

	private static final long serialVersionUID = 2084691334731830796L;

	private transient Player player;
	private transient Controller controler;
	private transient boolean inited;
	public Object[] lastControlerArguments;
	@Getter
	public String lastControler;

	public ControlerManager() {

	}

	public boolean canAddInventoryItem(int itemId, int amount) {
		if (controler == null || !inited)
			return true;
		return controler.canAddInventoryItem(itemId, amount);
	}

	public boolean canTakeItem(final FloorItem item) {
		if (controler == null || !inited)
			return true;
		return controler.canTakeItem(item);
	}

	public boolean processPlayerOption1(Entity entity) {
		if (controler == null || !inited)
			return true;
		return controler.processPlayerOption1(entity);
	}

	public boolean canDeleteInventoryItem(int itemId, int amount) {
		if (controler == null || !inited)
			return true;
		return controler.canDeleteInventoryItem(itemId, amount);
	}

	public boolean canProcessMovement(final int x, final int y) {
		if (controler == null || !inited)
			return true;
		return controler.canProcessMovement(x, y);
	}

	public boolean canDropItem(Item item) {
		if (controler == null || !inited)
			return true;
		return controler.canDropItem(item);
	}

	public boolean canEat(Food food) {
		if (controler == null || !inited)
			return true;
		return controler.canEat(food);
	}

	public boolean canEquip(int slotId, int itemId) {
		if (controler == null || !inited)
			return true;
		return controler.canEquip(slotId, itemId);
	}

	public boolean canHit(Entity entity) {
		if (controler == null || !inited)
			return true;
		return controler.canHit(entity);
	}

	public boolean canMove(int dir) {
		if (controler == null || !inited)
			return true;
		return controler.canMove(dir);
	}

	public boolean canPlayerOption1(Player target) {
		if (controler == null || !inited)
			return true;
		return controler.canPlayerOption1(target);
	}

	public boolean canPot(Pot pot) {
		if (controler == null || !inited)
			return true;
		return controler.canPot(pot);
	}

	public boolean canSummonFamiliar() {
		if (controler == null || !inited)
			return true;
		return controler.canSummonFamiliar();
	}

	public boolean processCommand(String s, boolean b, boolean c) {
		if (controler == null || !inited)
			return true;
		return controler.processCommand(s, b, c);
	}

	public boolean canUseItemOnItem(Item itemUsed, Item usedWith) {
		if (controler == null || !inited)
			return true;
		return controler.canUseItemOnItem(itemUsed, usedWith);
	}

	public boolean processKeyPress(int key) {
		if (controler == null || !inited)
			return true;
		return controler.processKeyPress(key);
	}

	public boolean canWalk() {
		if (controler == null || !inited)
			return true;
		return controler.canWalk();
	}

	public void forceStop() {
		if (controler != null) {
			controler.forceClose();
			controler = null;
		}
		lastControlerArguments = null;
		lastControler = null;
		inited = false;
	}

	public Controller getControler() {
		return controler;
	}

	public Object[] getLastControlerArguments() {
		return lastControlerArguments;
	}

	public void setLastControlerArguments(Object[] lastControlerArguments) {
		this.lastControlerArguments = lastControlerArguments;
	}

	public boolean keepCombating(boolean mainHand, Entity target) {
		if (controler == null || !inited)
			return true;
		return controler.keepCombating(mainHand, target);
	}

	public void login() {
		if (lastControler == null)
			return;
        if (refuseUnportedCookInstance(lastControler)) {
            // No legacy callback may run while discarding an unsupported saved controller.
            controler = null;
            lastControler = null;
            lastControlerArguments = null;
            inited = false;
            return;
        }
		controler = ControllerHandler.getControler(lastControler);
		if (controler == null) {
			forceStop();
			return;
		}
		controler.setPlayer(player);
		if (controler.login())
			forceStop();
		else
			inited = true;
	}

	public void logout() {
		if (controler == null)
			return;
		if (controler.logout())
			forceStop();
	}

	public void magicTeleported(int type) {
		if (controler == null || !inited)
			return;
		controler.magicTeleported(type);
	}

	public void moved() {
		if (controler == null || !inited)
			return;
		controler.moved();
	}

	public void process() {
		if (controler == null || !inited)
			return;
		controler.process();
	}

	public boolean processButtonClick(int interfaceId, int componentId, int slotId, int slotId2, int packetId) {
		if (controler == null || !inited)
			return true;
		return controler.processButtonClick(interfaceId, componentId, slotId, slotId2, packetId);
	}

	public boolean processItemOnNPC(NPC npc, Item item) {
		if (controler == null || !inited)
			return true;
		return controler.processItemOnNPC(npc, item);
	}

	public boolean processItemOnPlayer(Player player, int itemId) {
		if (controler == null || !inited)
			return true;
		return controler.processItemOnPlayer(player, itemId);
	}

	public boolean processItemTeleport(WorldTile toTile) {
		if (controler == null || !inited)
			return true;
		return controler.processItemTeleport(toTile);
	}

	public boolean processMagicTeleport(WorldTile toTile) {
		if (controler == null || !inited)
			return true;
		return controler.processMagicTeleport(toTile);
	}

	public boolean processMoneyPouch() {
		if (controler == null || !inited)
			return true;
		return controler.processMoneyPouch();
	}

	public boolean processNPCClick1(NPC npc) {
		if (controler == null || !inited)
			return true;
		return controler.processNPCClick1(npc);
	}

	public boolean processNPCClick2(NPC npc) {
		if (controler == null || !inited)
			return true;
		return controler.processNPCClick2(npc);
	}

	public boolean processNPCClick3(NPC npc) {
		if (controler == null || !inited)
			return true;
		return controler.processNPCClick3(npc);
	}

	public boolean processNPCClick4(NPC npc) {
		if (controler == null || !inited)
			return true;
		return controler.processNPCClick4(npc);
	}

	public boolean processNPCExamine(NPC npc) {
		if (controler == null || !inited)
			return true;
		return controler.processNPCExamine(npc);
	}

	public boolean processObjectClick1(WorldObject object) {
		if (controler == null || !inited)
			return true;
		return controler.processObjectClick1(object);
	}

	public boolean processObjectClick2(WorldObject object) {
		if (controler == null || !inited)
			return true;
		return controler.processObjectClick2(object);
	}

	public boolean processObjectClick3(WorldObject object) {
		if (controler == null || !inited)
			return true;
		return controler.processObjectClick3(object);
	}

	public boolean processObjectClick4(WorldObject object) {
		if (controler == null || !inited)
			return true;
		return controler.processObjectClick4(object);
	}

	public boolean processObjectClick5(WorldObject object) {
		if (controler == null || !inited)
			return true;
		return controler.processObjectClick5(object);
	}

	public boolean processObjectExamine(WorldObject object) {
		if (controler == null || !inited)
			return true;
		return controler.processObjectExamine(object);
	}

	public boolean processObjectTeleport(WorldTile toTile) {
		if (controler == null || !inited)
			return true;
		return controler.processObjectTeleport(toTile);
	}

	public boolean processPlayerOption4(Player p2) {
		if (controler == null || !inited)
			return true;
		return controler.processPlayerOption4(p2);
	}

	public void removeControlerWithoutCheck() {
		controler = null;
		lastControlerArguments = null;
		lastControler = null;
		inited = false;
	}

	public boolean sendDeath() {
		if (controler == null || !inited)
			return true;
		return controler.sendDeath();
	}

	public void sendInterfaces() {
		if (controler == null || !inited)
			return;
		controler.sendInterfaces();
	}

	public void setPlayer(Player player) {
		this.player = player;
	}

	public void startControler(Object key, Object... parameters) {
		if (refuseUnportedCookInstance(key)) return;
		if (controler != null)
			forceStop();
		controler = (Controller) (key instanceof Controller ? key : ControllerHandler.getControler(key));
		if (controler == null)
			return;
		controler.setPlayer(player);
		lastControlerArguments = parameters;
		if(key instanceof Controller) {
			lastControler = key.getClass().getSimpleName();
		} else {
			lastControler = (String) key;
		}
		controler.start();
		inited = true;
	}

    private boolean refuseUnportedCookInstance(Object key) {
        if (player == null || player.getClientProfile() != ClientProfile.NATIVE_950
                || !("ImpossibleJadControler".equals(key) || "ImpossibleJad".equals(key)
                    || key instanceof ImpossibleJad)) return false;
        // Reject before replacing the current controller, allocating a dynamic map, or spawning NPCs.
        player.getPackets().sendGameMessage("That adventure is not available yet.");
        System.out.println("[Ataraxia950] Refused unported Cook combat instance before controller admission");
        return true;
    }

	public void trackXP(int skillId, int addedXp) {
		if (controler == null || !inited)
			return;
		controler.trackXP(skillId, addedXp);
	}

	public boolean useDialogueScript(Object key) {
		if (controler == null || !inited)
			return true;
		return controler.useDialogueScript(key);
	}

	public boolean addWalkStep(int lastX, int lastY, int nextX, int nextY) {
		if (controler == null || !inited)
			return true;
		return controler.checkWalkStep(lastX, lastY, nextX, nextY);
	}

	public boolean handleItemOnObject(WorldObject object, Item item) {
		if (controler == null || !inited)
			return true;
		return controler.handleItemOnObject(object, item);
	}

	public boolean canRemoveEquip(int slotId, int itemId) {
		if (controler == null || !inited)
			return true;
		return controler.canRemoveEquip(slotId, itemId);
	}

	public void processNPCDeath(NPC npc) {
		if (controler == null || !inited)
			return;
		controler.processNPCDeath(npc);
	}

	public void processIncomingHit(Hit hit, Entity target) {
		if (controler == null || !inited)
			return;
		controler.processIncommingHit(hit, target);
	}

	public void processIngoingHit(Hit hit) {
		if (controler == null || !inited)
			return;
		controler.processIngoingHit(hit);
	}

	public boolean handleItemOption1(Item item, int itemId, int slotId) {
		if (controler == null || !inited)
			return true;
		return controler.handleItemOption1(item, itemId, slotId);
	}

	public boolean canDoAnimation(Animation animation) {
		if (controler == null || !inited)
			return true;
		return controler.canDoAnimation(animation);
	}

    public boolean canUseAbility(Shortcut shortcut) {
        if (controler == null || !inited)
            return true;
        return controler.canUseAbility(shortcut);
    }

    public boolean canLogout() {
        if (controler == null || !inited)
            return true;
        return controler.canLogout();
    }
}