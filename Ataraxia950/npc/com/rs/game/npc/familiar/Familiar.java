package com.rs.game.npc.familiar;

import java.io.Serializable;

import com.rs.cache.loaders.ClientScriptMap;
import com.rs.cache.loaders.ItemDefinitions;
import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.Graphics;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.npc.NPC;
import com.rs.game.npc.camelwarrior.Mirage;
import com.rs.game.npc.combat.NPCCombatDefinition;
import com.rs.game.npc.glacor.Glacyte;
import com.rs.game.player.PerkManager.DonationPerk;
import com.rs.game.player.Player;
import com.rs.game.player.BuffDebuffTimersManager.Timer;
import com.rs.game.player.actions.summoning.Summoning.Pouches;
import com.rs.game.player.content.InterfaceManager;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

import lombok.Getter;

public abstract class Familiar extends NPC implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -3255206534594320406L;

    private transient Player owner;
    private int ticks;
    private int trackTimer;
    private int specialEnergy, specialSpeed, trackSpecial;
    private final transient boolean finished = false;
    private boolean trackDrain, spiritualised;

    private BeastOfBurden bob;
    @Getter
    public Pouches pouch;

    public Familiar(Player owner, Pouches pouch, WorldTile tile, int mapAreaNameHash, boolean canBeAttackFromOutOfArea) {
        super(pouch.getNpcId(), tile, mapAreaNameHash, canBeAttackFromOutOfArea, false);
        this.owner = owner;
        if (owner.getPerkManager().hasPerkActive(DonationPerk.FAMILIAR_EXPERT)) {
            int hp = getHitpoints();
            setHitpoints((int) (getHitpoints() + (getHitpoints() * 0.25)));
            owner.sendMessage(getName() + "'s hitpoints increased to " + getHitpoints() + " from " + hp + " thanks to Familiar Expert perk.", true);
        }
        this.pouch = pouch;
        resetTickets();
        specialEnergy = 60;
        if (getBOBSize() > 0)
            bob = new BeastOfBurden(getBOBSize());
        call(true);
    }

    public void store() {
        if (bob == null)
            return;
        bob.open();
    }

    public boolean isSpiritualised() {
        return spiritualised;
    }

    public void setSpiritualised(boolean val) {
        this.spiritualised = true;
    }

    public Item getScrolls() {
        return scrolls;
    }

    public int getSpecialSpeed() {
        return specialSpeed;
    }

    public void setSpecialSpeed(int speed) {
        this.specialSpeed = speed;
    }

    public boolean canStoreEssOnly() {
        return pouch.getNpcId() == 6818;
    }

    public int getOriginalId() {
        return pouch.getNpcId();
    }

    public void resetTickets() {
        ticks = (int) (pouch.getTime(owner.getPerkManager().hasPerkActive(DonationPerk.FAMILIAR_EXPERT)) / 1000 / 30);
        owner.getBuffDebuffTimersManager().addTimer(Timer.FAMILIAR_SUMMONED, ticks * 600);
        trackTimer = 0;
    }

    public void setTicks(final int ticks) {
        this.ticks = ticks;
    }

    public int getTicks() {
        return ticks;
    }

    private void sendFollow() {
        setRun(owner.getNextRunDirection() != -1);
        if (getLastFaceEntity() != owner.getClientIndex())
            setNextFaceEntity(owner);
        if (getFreezeDelay() >= Utils.currentTimeMillis())
            return; // if freeze cant move ofc
        int size = getSize();

        int distanceX = owner.getX() - getX();
        int distanceY = owner.getY() - getY();
        // if is under
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

    /**
     * Adds scrolls to the given entity.
     */
    public void addScrolls(Item scroll) {
        Pouches pouch = Pouches.forNpcId(getId());
        if (pouch == null)
            return;
        if (scroll.getId() != pouch.getScrollId()) {
            this.getOwner().sendMessage("You cannot store this item as scrolls.");
            return;
        }
        int scrollsToRemove = 0;
        scrollsToRemove = (this.scrolls == null ? 0 : this.scrolls.getAmount()) + scroll.getAmount() > 200 ? 200 - (this.scrolls == null ? 0 : this.scrolls.getAmount()) : scroll.getAmount();
        this.getOwner().getInventory().deleteItem(new Item(scroll.getId(), scrollsToRemove));
        this.getOwner().sendMessage("You've stored " + scrollsToRemove + " " + ItemDefinitions.getItemDefinitions(scroll.getId()).getName() + " in your familiar.");
        this.scrolls = new Item(pouch.getScrollId(), (scrolls != null ? scrolls.getAmount() : 0) + scrollsToRemove);
        refreshScrolls();
    }

    /**
     * Removes scrolls from the familiar.
     */
    public void removeScrolls() {
        if (!this.getOwner().getInventory().hasFreeSlots() && !this.getOwner().getInventory().containsItem(this.scrolls.getId(), 1)) {
            this.getOwner().sendMessage("You don't have enough free space to withdraw the scrolls.");
            return;
        }
        this.getOwner().getInventory().addItem(scrolls);
        this.getOwner().sendMessage("You withdraw " + scrolls.getAmount() + " " + ItemDefinitions.getItemDefinitions(scrolls.getId()).getName() + ".");
        this.scrolls = null;
        refreshScrolls();
    }

    /**
     * Decrements scrolls by one (Using special by default)
     */
    public boolean decrementScrolls() {
        if (this.scrolls != null && this.scrolls.getAmount() != 0) {
            this.scrolls = new Item(this.scrolls.getId(), this.scrolls.getAmount() - 1);
            refreshScrolls();
            return true;
        }
        return false;
    }

    @Override
    public void processNPC() {
        if (isDead())
            return;
        unlockOrb();
        trackTimer++;
        if (trackTimer == 50) {
            trackTimer = 0;
            ticks--;
            if (trackDrain)
                owner.getSkills().drainSummoning(1);
            trackDrain = !trackDrain;
            if (ticks == 2)
                owner.getPackets().sendGameMessage("You have 1 minute before your familiar vanishes.");
            else if (ticks == 1)
                owner.getPackets().sendGameMessage("You have 30 seconds before your familiar vanishes.");
            else if (ticks == 0) {
                removeFamiliar();
                dissmissFamiliar(false);
                return;
            }
            sendTimeRemaining();
        }
        /*
         * int originalId = getOriginalId() + 1; if (owner.isCanPvp() && getId() ==
         * getOriginalId()) { setNextNPCTransformation(originalId); call(false); return;
         * } else if (!owner.isCanPvp() && getId() == originalId && pouch !=
         * Pouches.MAGPIE && pouch != Pouches.IBIS && pouch != Pouches.BEAVER && pouch
         * != Pouches.MACAW && pouch != Pouches.FRUIT_BAT) {
         * setNextNPCTransformation(originalId - 1); call(false); return; } else
         */
        if (!withinDistance(owner, 12)) {
            call(false);
            return;
        }
        if (!getCombat().process()) {
            if (isAgressive() && owner.getAttackedBy() != null && owner.getAttackedByDelay() > Utils.currentTimeMillis() && canAttack(owner.getAttackedBy()))
                getCombat().setTarget(owner.getAttackedBy());
            else
                sendFollow();
        } else {
            trackSpecial++;
            if (specialSpeed != 0 && this.getOwner().alwaysUseSpecialSpeed != 0) {
                if (this.scrolls == null || this.scrolls.getAmount() == 0)
                    return;
                if (trackSpecial >= Math.round(75 / (specialSpeed == 0 ? this.getOwner().alwaysUseSpecialSpeed : specialSpeed))) {
                    trackSpecial = 0;
                    if (this.getCombat().getTarget() != null && !this.getCombat().getTarget().hasFinished() && !this.getCombat().getTarget().isDead()) {
                        if (specialEnergy < this.getSpecialAmount())
                            return;
                        this.setSpecial(true, true);
                        this.decrementScrolls();
                        submitSpecial(this.getTarget());
                        drainSpecial();
                    }
                }
            }
        }
    }

    public boolean canAttack(Entity target) {
        if (target == this || target == owner)
            return false;
        if (target instanceof Player) {
            Player player = (Player) target;
            if (!owner.isCanPvp() || !player.isCanPvp())
                return false;
        } else if (target instanceof NPC) {
            NPC n = (NPC) target;
            if (n.getId() == 14301 || n.getId() == 14302 || n.getId() == 14303 || n.getId() == 14304) {
                Glacyte glacyte = (Glacyte) n;
                if (glacyte.getGlacor().getTargetIndex() != -1 && getOwner().getIndex() != glacyte.getGlacor().getTargetIndex()) {
                    getOwner().sendMessage("This isn't your target.");
                    return false;
                }
            }

            if (n.getId() == 22001 || n.getId() == 22002 || n.getId() == 22003 || n.getId() == 22004) {
                Mirage mirage = (Mirage) n;
                if (mirage.getCamel().getTargetIndex() != -1 && getOwner().getIndex() != mirage.getCamel().getTargetIndex()) {
                    getOwner().sendMessage("This isn't your target.");
                    return false;
                }
            }
            if (n.isCantInteract())
                return false;
        }
        return !target.isDead() && ((owner.isAtMultiArea() && isAtMultiArea() && target.isAtMultiArea()) || (owner.isForceMultiArea() && target.isForceMultiArea())) && owner.getControlerManager().processPlayerOption1(target);
    }

    public boolean renewFamiliar() {
        if (ticks > 5) {
            owner.getPackets().sendGameMessage("You need to have at least two minutes and fifty seconds remaining before you can renew your familiar.", true);
            return false;
        } else if (!owner.getInventory().getItems().contains(new Item(pouch.getPouchId(), 1))) {
            owner.getPackets().sendGameMessage("You need a " + ItemDefinitions.getItemDefinitions(pouch.getPouchId()).getName().toLowerCase() + " to renew your familiar's timer.");
            return false;
        }
        resetTickets();
        owner.getInventory().deleteItem(pouch.getPouchId(), 1);
        call(true);
        owner.getPackets().sendGameMessage("You use your remaining pouch to renew your familiar.");
        return true;
    }

    public void takeBob() {
        if (bob == null) {
            owner.sendMessage("Only beasts of burden can carry items.");
            return;
        }
        if (bob.getBeastItems().getFreeSlots() == getBOBSize()) {
            owner.getPackets().sendGameMessage("Your familiar currently is not carrying any items.");
            return;
        }
        bob.takeBob();
    }
    
    public void giveBob() {
        if (bob == null)
            return;
        if (owner.getInventory().getFreeSlots() == 28) {
            owner.getPackets().sendGameMessage("You currently aren't carrying any items.");
            return;
        }
        bob.giveBob();
    }
    
    public void sendTimeRemaining() {
        if (owner == null)
            return;
        owner.getPackets().sendExecuteScript(754, (ticks / 2), (ticks % 2));
    }

    public void sendMainConfigs() {
        switchOrb(true);
        refreshSpecialEnergy();
        sendTimeRemaining();
        owner.getPackets().sendConfig(448, pouch.getPouchId());
        owner.getPackets().sendConfigByFile(6051, getSpecialAmount());
        owner.getPackets().sendGlobalString(2516, getSpecialName());
        owner.getPackets().sendGlobalString(2517, getSpecialDescription());
        owner.getPackets().sendGlobalConfig(1436, getSpecialAttack() == SpecialAttack.CLICK ? 1 : 0);
        owner.getPackets().sendConfigByFile(27403, getMaxHitpoints());
        owner.getPackets().sendConfigByFile(19034, getHitpoints());
        unlockOrb();
        sendFollowerDetails();
    }

    public void sendFollowerDetails() {
        
        owner.getInterfaceManager().sendFamiliarInfo();
        owner.getInterfaceManager().openGameTab(InterfaceManager.SUMMONING_TAB);
        for (int i = 0; i < Utils.getInterfaceDefinitionsComponentsSize(662); i++)
            owner.getPackets().sendHideIComponent(662, i, false);
        owner.getPackets().sendIComponentSettings(662, 5, 0, getBOBSize(), 14682110);
        owner.getPackets().sendHideIComponent(662, 89, false);// life points
        owner.getPackets().sendHideIComponent(662, 47, getBOBSize() == 0);
        owner.getPackets().sendHideIComponent(662, 50, true);// life points
        owner.getPackets().sendConfig(1784, getId());
        owner.getPackets().sendConfig(1831, getPouch().getPouchId());
        owner.getPackets().sendConfig(4824, getPouch().getScrollId());
        refreshScrolls();
        owner.getPackets().sendConfig(1786, (int) (getPouch().getTime(owner.getPerkManager().hasPerkActive(DonationPerk.FAMILIAR_EXPERT)) / 468.75));
        refreshFamilarDisplay();
        refreshSpecialEnergy();
        unlock();
        owner.getPackets().sendGlobalConfig(1436, 1);
    }
    
    private void refreshFamilarDisplay() {
        boolean displayFamiliarInv = getBOBSize() != 0 && owner.isDisplayFamilarInventory();
        owner.getPackets().sendHideIComponent(662, 1, !displayFamiliarInv);
        owner.getPackets().sendHideIComponent(662, 34, displayFamiliarInv);
    }
    
    public void switchOrb(boolean on) {
        owner.getPackets().sendConfig(1174, on ? -1 : 0);
        if (on)
            unlock();
        else
            lockOrb();
    }

    public void unlockOrb() {
        if (owner == null)
            return;
        // owner.getPackets().sendHideIComponent(1430, 34, false);
        sendLeftClickOption(owner);
    }

    public static void confirmLeftOption(Player player) {
        player.getInterfaceManager().removeFamiliarInterface();
    }

    public static void selectLeftOption(Player player) {
        sendLeftClickOption(player);
        player.getInterfaceManager().sendInterface(880);
        player.getPackets().sendIComponentSettings(880, 2, 0, 3, 2);
        player.getPackets().sendIComponentSettings(880, 9, 0, 5, 2);
        player.getPackets().sendIComponentSettings(880, 28, 0, 8, 2);
    }

    public static void sendLeftClickOption(Player player) {
        player.refreshSummoningLeftClickOption();
    }

    public static void setLeftclickOption(Player player, int summoningLeftClickOption) {
        if (summoningLeftClickOption == player.getSummoningLeftClickOption())
            return;
        player.setSummoningLeftClickOption(summoningLeftClickOption);
        sendLeftClickOption(player);
    }

    public void unlock() {
        switch (getSpecialAttack()) {
        case CLICK:
            owner.getPackets().sendIComponentSettings(1430, 35, -1, 1, 2);
            owner.getPackets().sendIComponentSettings(1506, 18, -1, 1, 2);
            owner.getPackets().sendIComponentSettings(662, 86, 0, 0, 2);
            owner.getPackets().sendIComponentSettings(662, 27, 0, 0, 2);
            break;
        case ENTITY:
            owner.getPackets().sendIComponentSettings(1430, 35, -1, 1, 20480);
            owner.getPackets().sendIComponentSettings(1506, 18, -1, 1, 20480);
            owner.getPackets().sendIComponentSettings(662, 86, 0, 0, 20480);
            owner.getPackets().sendIComponentSettings(662, 27, 0, 0, 20480);
            break;
        case OBJECT:
            owner.getPackets().sendIComponentSettings(1430, 35, -1, 1, 78321);
            owner.getPackets().sendIComponentSettings(1506, 18, -1, 1, 78321);
            owner.getPackets().sendIComponentSettings(662, 86, 0, 0, 78321);
            owner.getPackets().sendIComponentSettings(662, 27, 0, 0, 78321);
            break;
        case ITEM:
            owner.getPackets().sendIComponentSettings(1430, 35, -1, 1, 65536);
            owner.getPackets().sendIComponentSettings(1506, 18, -1, 1, 65536);
            owner.getPackets().sendIComponentSettings(662, 86, 0, 0, 65536);
            owner.getPackets().sendIComponentSettings(662, 27, 0, 0, 65536);
            break;
        }
        // owner.getPackets().sendHideIComponent(1430, 35, false);
    }

    public void lockOrb() {
//        refreshDefaultPetOptions(owner);
//        owner.getPackets().sendHideIComponent(747, 9, true);
    }

    private transient int[][] checkNearDirs;
    private transient boolean sentRequestMoveMessage;

    public void call() {
        if (getAttackedBy() != null && getAttackedByDelay() > Utils.currentTimeMillis()) {
            owner.getPackets().sendGameMessage("You cannot call your familiar whilst it's under combat.");
            return;
        }
        call(false);
    }

    public void call(boolean login) {
        if (dead)
            return;
        int size = getSize();
        if (login) {
            if (bob != null)
                bob.setEntitys(owner, this);
            checkNearDirs = Utils.getCoordOffsetsNear(size);
            sendMainConfigs();
        } else
            removeTarget();
        WorldTile teleTile = null;
        for (int dir = 0; dir < checkNearDirs[0].length; dir++) {
            final WorldTile tile = new WorldTile(new WorldTile(owner.getX() + checkNearDirs[0][dir], owner.getY() + checkNearDirs[1][dir], owner.getPlane()));
            if (World.canMoveNPC(tile.getPlane(), tile.getX(), tile.getY(), size)) { // if
                                                                                     // found
                                                                                     // done
                teleTile = tile;
                break;
            }
        }
        if (login || teleTile != null)
            WorldTasksManager.schedule(new WorldTask() {
                @Override
                public void run() {
                    setNextGraphics(new Graphics(getDefinitions().size > 1 ? 1315 : 1314));
                }
            });
        if (teleTile == null) {
            if (!sentRequestMoveMessage) {
                owner.getPackets().sendGameMessage("Theres not enough space for your familiar appear.");
                sentRequestMoveMessage = true;
            }
            return;
        }
        sentRequestMoveMessage = false;
        setNextWorldTile(teleTile);
    }

    public void removeFamiliar() {
        owner.getBuffDebuffTimersManager().removeTimer(Timer.FAMILIAR_SUMMONED);
        owner.getInterfaceManager().removeFamiliarInterface();
        owner.setFamiliar(null);
    }

    public void dissmissFamiliar(boolean logged) {
        finish();
        if (!logged && !isFinished()) {
            setFinished(true);
            switchOrb(false);
            owner.getBuffDebuffTimersManager().removeTimer(Timer.FAMILIAR_SUMMONED);
            owner.getPackets().sendConfig(1831, -1);
            owner.getPackets().sendConfigByFile(6051, -1);
            owner.getPackets().sendConfig(4824, -1);
            owner.getPackets().sendConfigByFile(25412, -1);
            owner.getPackets().sendConfig(1784, -1);
            owner.getInterfaceManager().removeFamiliarInterface();
            if (bob != null)
                bob.dropBob();
        }
    }

    private transient boolean dead;

    public boolean callBlocked;


    public void sendDeathNoAnimation(Entity source) {
        if (dead)
            return;
        dead = true;
        removeFamiliar();
        resetWalkSteps();
        setCantInteract(true);
        getCombat().removeTarget();
        setNextAnimation(null);
        NPC npc = this;
        if (getScrolls() != null && getScrolls().getAmount() != 0)
            World.addGroundItem(getScrolls(), new WorldTile(getCoordFaceX(npc.getSize()), getCoordFaceY(npc.getSize()), getPlane()), owner, false, -1);
        dissmissFamiliar(false);
    }
    @Override
    public void sendDeath(Entity source) {
        if (dead)
            return;
        dead = true;
        removeFamiliar();
        final NPCCombatDefinition defs = getCombatDefinitions();
        resetWalkSteps();
        setCantInteract(true);
        getCombat().removeTarget();
        setNextAnimation(null);
        NPC npc = this;
        WorldTasksManager.schedule(new WorldTask() {
            int loop;

            @Override
            public void run() {
                if (loop == 0) {
                    setNextAnimation(new Animation(defs.getDeathEmote()));
                    owner.getPackets().sendGameMessage("Your familiar slowly begins to fade away..");
                    if (!owner.getPerkManager().hasPerkActive(DonationPerk.FAMILIAR_EXPERT))
                        owner.sendMessage("Consider purchasing the Familiar Expert perk " + "to increase Pet timers and health.", true);
                } else if (loop >= defs.getDeathDelay()) {
                    if (getScrolls() != null && getScrolls().getAmount() != 0)
                        World.addGroundItem(getScrolls(), new WorldTile(getCoordFaceX(npc.getSize()), getCoordFaceY(npc.getSize()), getPlane()), owner, false, -1);
                    dissmissFamiliar(false);
                    stop();
                }
                loop++;
            }
        }, 0, 1);
    }

    public void respawnFamiliar(Player owner) {
        this.owner = owner;
        initEntity();
        deserialize();
        call(true);
        owner.getBuffDebuffTimersManager().addTimer(Timer.FAMILIAR_SUMMONED, ticks * 600);
        if (bob != null)
            bob.sendInterItems();
    }

    public abstract String getSpecialName();

    public abstract String getSpecialDescription();

    public abstract int getBOBSize();

    public abstract int getSpecialAmount();

    public abstract SpecialAttack getSpecialAttack();

    public abstract boolean submitSpecial(Object object);

    public boolean isAgressive() {
        return true;
    }

    public enum SpecialAttack {
        ITEM, ENTITY, CLICK, OBJECT
    }

    public BeastOfBurden getBob() {
        return bob;
    }

    public void refreshSpecialEnergy() {
        owner.getPackets().sendConfig(1787, specialEnergy);
    }

    public void restoreSpecialAttack(int energy) {
        if (specialEnergy >= 60)
            return;
        if (owner.perkManager.hasPerkActive(DonationPerk.FAMILIAR_EXPERT))
            energy += (int) Math.ceil((double) energy * 0.25);
        specialEnergy = energy + specialEnergy >= 60 ? 60 : specialEnergy + energy;
        refreshSpecialEnergy();
    }

    public void setSpecial(boolean on, boolean automatic) {
        if (!on)
            owner.getTemporaryAttributtes().remove("FamiliarSpec");
        else {
            if (specialEnergy < getSpecialAmount()) {
                if (!automatic)
                    owner.sendMessage("You familiar doesn't have enough special energy.");
                return;
            }
            owner.getTemporaryAttributtes().put("FamiliarSpec", Boolean.TRUE);
        }
    }

    public void drainSpecial(int specialReduction) {
        int capeId = owner.getEquipment().getCapeId();
        if (capeId == 19893 || owner.infusedSpiritCapeEffect && (capeId == 20769 || capeId == 20771 || capeId == 32152 || capeId == 32153))
            specialReduction = (int) (specialReduction * 0.8);
        specialEnergy -= specialReduction;
        if (specialEnergy < 0) {
            specialEnergy = 0;
        }
        refreshSpecialEnergy();
    }

    public void drainSpecial() {
        int specialReduction = getSpecialAmount();
        int capeId = owner.getEquipment().getCapeId();
        if (capeId == 19893 || owner.infusedSpiritCapeEffect && (capeId == 20769 || capeId == 20771 || capeId == 32152 || capeId == 32153))
            specialReduction = (int) (specialReduction * 0.8);
        specialEnergy -= specialReduction;
        refreshSpecialEnergy();
    }

    public boolean hasSpecialOn() {
        if (owner.getTemporaryAttributtes().remove("FamiliarSpec") != null) {
            if (!owner.getPerkManager().hasPerkActive(DonationPerk.FAVORED_FAMILIARS)) {
                if (!decrementScrolls()) {
                if (!owner.getInventory().containsItem(pouch.getScrollId(), 1)) {
                    owner.getPackets().sendGameMessage("You don't have the scrolls to use this move.");
                    return false;
                }
                owner.getInventory().deleteItem(pouch.getScrollId(), 1);
                }
            }
            drainSpecial();
            return true;
        }
        return false;
    }

    public Player getOwner() {
        return owner;
    }

    public boolean isFinished() {
        return finished;
    }

    /**
     * Defines the scrolls held within the familiar.
     */
    private Item scrolls;

    public void switchDisplayInv() {
        owner.setDisplayFamilarInventory(!owner.isDisplayFamilarInventory());
        refreshFamilarDisplay();
    }
    
    @Override
    public void setHitpoints(int hitpoints) {
        super.setHitpoints(hitpoints);
        if(owner != null)
        owner.getPackets().sendConfigByFile(19034, getHitpoints());
    }
    
    public void refreshScrolls() {
        if (owner == null)
            return;
        owner.getPackets().sendConfig(4824, ClientScriptMap.getMap(1283).getIntValue(getPouch().getPouchId()));
        owner.getPackets().sendConfigByFile(25412, scrolls == null ? 0 : scrolls.getAmount());
    }
}
