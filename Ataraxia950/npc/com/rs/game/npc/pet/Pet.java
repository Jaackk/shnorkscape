package com.rs.game.npc.pet;

import com.google.common.collect.ImmutableList;
import com.rs.cache.loaders.ClientScriptMap;
import com.rs.game.ForceTalk;
import com.rs.game.Graphics;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.npc.PetMorphHandler;
import com.rs.game.npc.familiar.Familiar;
import com.rs.game.player.PerkManager.DonationPerk;
import com.rs.game.player.Player;
import com.rs.game.player.content.InterfaceManager;
import com.rs.game.player.content.pet.PetDetails;
import com.rs.game.player.content.pet.Pets;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.Colors;
import com.rs.utils.Utils;

import lombok.val;

/**
 * Represents a pet.
 *
 * @author Emperor
 */
public final class Pet extends NPC {

    public static final ImmutableList<Integer> CHEERLEADER_DANCE = ImmutableList.of(
        190, 191, 192, 193
    );
    public static final ImmutableList<String> CHEERLEADER_DIALOGUE = ImmutableList.of(
            "Rawr X3 *nuzzles* How are you? *pounces on you* you're so warm UwU",
            "I’m Cheerleader-san and I absolutely luuuv anime and my fav is naruto!!!",
            "Wow!!! :3 you're adorable and I think I should be your new waifu! UwU",
            "UwU I luvvv how you beat the other teams :3 so kawaii!"
    );

    /**
     * The serial UID.
     */
    private static final long serialVersionUID = -2848843157767889742L;

    /**
     * The owner.
     */
    private final Player owner;

    /**
     * The "near" directions.
     */
    private final int[][] checkNearDirs;

    /**
     * The item id.
     */
    private final int itemId;

    /**
     * The pet details.
     */
    private final PetDetails details;
    /**
     * The pets type.
     */
    private final Pets pet;
    /**
     * The growth rate of the pet.
     */
    private double growthRate;

    /**
     * Constructs a new {@code Pet} {@code Object}.
     *
     * @param id The NPC id.
     * @param itemId The item id.
     * @param owner The owner.
     * @param tile The world tile.
     */
    public Pet(int id, int itemId, Player owner, WorldTile tile, PetDetails details) {
        super(id, tile, -1, false);
        this.owner = owner;
        this.itemId = itemId;
        this.checkNearDirs = Utils.getCoordOffsetsNear(super.getSize());
        this.details = details;
        this.pet = Pets.forId(itemId);
        if (pet == Pets.TROLL_BABY && owner.getPetManager().getTrollBabyName() != null)
            setName(owner.getPetManager().getTrollBabyName());
        this.setRun(true);
        sendMainConfigurations();
        sendFollowerDetails();
    }

    public Player getOwner() {
        return owner;
    }

    /**
     * Calls the pet.
     */
    public void call() {
        int size = getSize();
        WorldTile teleTile = null;
        for (int dir = 0; dir < checkNearDirs[0].length; dir++) {
            final WorldTile tile = new WorldTile(new WorldTile(owner.getX() + checkNearDirs[0][dir], owner.getY() + checkNearDirs[1][dir], owner.getPlane()));
            if (World.canMoveNPC(tile.getPlane(), tile.getX(), tile.getY(), size)) {
                teleTile = tile;
                break;
            }
        }
        if (teleTile == null)
            return;
        setNextWorldTile(teleTile);
        if (pet == Pets.ENTLING) {
            setNextGraphics(new Graphics(1638));
        }
    }

    /**
     * Gets the details.
     *
     * @return The details.
     */
    public PetDetails getDetails() {
        return details;
    }

    /**
     * Gets the growthRate.
     *
     * @return The growthRate.
     */
    public double getGrowthRate() {
        return growthRate;
    }

    /**
     * Sets the growthRate.
     *
     * @param growthRate The growthRate to set.
     */
    public void setGrowthRate(double growthRate) {
        this.growthRate = growthRate;
    }

    /**
     * Gets the item id of the pet.
     *
     * @return The item id.
     */
    public int getItemId() {
        return itemId;
    }

    /**
     * Grows into the next stage of this pet (if any).
     */
    public void growNextStage() {
        if (details.getStage() == 3)
            return;
        if (pet == null)
            return;
        int npcId = pet.getNpcId(details.getStage() + 1);
        if (npcId < 1)
            return;
        details.setStage(details.getStage() + 1);
        int itemId = pet.getItemId(details.getStage());
        if (pet.getNpcId(details.getStage() + 1) > 0)
            details.updateGrowth(-100.0);
        owner.getPetManager().setItemId(itemId);
        owner.getPetManager().setNpcId(npcId);
        finish();
        Pet newPet = new Pet(npcId, itemId, owner, owner, details);
        newPet.growthRate = growthRate;
        owner.setPet(newPet);
        owner.sendMessage("<col=ff0000>Your pet has grown larger.</col>");
    }

    /**
     * Locks the orb.
     */
    public void lockOrb() {

    }

    /**
     * Picks up the pet.
     */
    public void pickup() {
        owner.setCurrentPet(null);
        owner.setPet(null);
        owner.getVarBitManager().sendVarBit(6051, -1);
        owner.getVarBitManager().sendVar(1784, -1);
        owner.getPackets().sendConfig(1831, -1);// configures
        owner.getPackets().sendConfig(4824, -1); // sets npc emote
        owner.getPetManager().setNpcId(-1);
        owner.getPetManager().setItemId(-1);
        switchOrb(false);
        owner.getInterfaceManager().removeFamiliarInterface();
        owner.getPackets().sendIComponentSettings(747, 17, 0, 0, 0);
        finish();
    }

    @Override
    public void processNPC() {
        unlockOrb();
        if (pet == Pets.TROLL_BABY && details.getHunger() > 0.000) {
            details.resetHunger();
            owner.getPackets().sendConfigByFile(6049, (int) details.getHunger());
        }
        if (!owner.getPerkManager().hasPerkActive(DonationPerk.PETSCHANTER)) {
            if (details.getHunger() >= 90.0 && details.getHunger() < 99.9) {
                if (owner.feedPetCounter >= 10) {
                    owner.sendMessage("<col=ff0000>Your pet is starving, feed it before it runs off.</col>");
                    owner.feedPetCounter = 0;
                } else
                    owner.feedPetCounter++;
            } else if (details.getHunger() == 100.0) {
                owner.getPetManager().setNpcId(-1);
                owner.getPetManager().setItemId(-1);
                owner.setPet(null);
                owner.getVarBitManager().sendVarBit(6051, -1);
                owner.getVarBitManager().sendVar(1784, -1);
                owner.getPackets().sendConfig(1831, -1);// configures
                owner.getPackets().sendConfig(4824, -1); // sets npc emote
                owner.getPetManager().removeDetails(itemId);
                owner.sendMessage("Your pet has ran away to find some food!");
                switchOrb(false);
                owner.getInterfaceManager().removeFamiliarInterface();
                finish();
                return;
            }
        }
        if (growthRate > 0.000) {
            details.updateGrowth(growthRate);
            owner.getPackets().sendConfigByFile(6048, (int) details.getGrowth());
            if (details.getGrowth() == 100.0)
                growNextStage();
        }
        if (!withinDistance(owner, 12)) {
            call();
            return;
        }
        sendFollow();
    }

    /**
     * Follows the owner.
     */
    private void sendFollow() {
        if (getLastFaceEntity() != owner.getClientIndex())
            setNextFaceEntity(owner);
        if (isFrozen())
            return;
        int size = getSize();
        int targetSize = owner.getSize();
        if (Utils.colides(getX(), getY(), size, owner.getX(), owner.getY(), targetSize) && !owner.hasWalkSteps()) {
            resetWalkSteps();
            if (!addWalkSteps(owner.getX() + targetSize, getY())) {
                resetWalkSteps();
                if (!addWalkSteps(owner.getX() - size, getY())) {
                    resetWalkSteps();
                    if (!addWalkSteps(getX(), owner.getY() + targetSize)) {
                        resetWalkSteps();
                        if (!addWalkSteps(getX(), owner.getY() - size))
                            return;
                    }
                }
            }
            return;
        }
        resetWalkSteps();
        if (!clipedProjectile(owner, true) || !Utils.isOnRange(getX(), getY(), size, owner.getX(), owner.getY(), targetSize, 0))
            calcFollow(owner, 2, true, false);
    }

    /**
     * Sends the follower details.
     */
    public void sendFollowerDetails() {
        owner.getInterfaceManager().sendFamiliarInfo();
        owner.getInterfaceManager().openGameTab(InterfaceManager.SUMMONING_TAB);
        owner.getVarBitManager().sendVar(1784, getId());
        owner.getPackets().sendConfigByFile(6048, (int) details.getGrowth());
        owner.getPackets().sendConfigByFile(6049, (int) details.getHunger());
        owner.getPackets().sendHideIComponent(662, 89, true);// life points
        owner.getPackets().sendHideIComponent(662, 47, true);
        owner.getPackets().sendHideIComponent(662, 1, true);
        unlock();
    }

    /**
     * Sends the main configurations for the Pet interface (+ summoning orb).
     */
    public void sendMainConfigurations() {
        switchOrb(true);
        owner.getPackets().sendConfig(1831, itemId);// configures
        owner.getPackets().sendConfig(4824, ClientScriptMap.getMap(1283).getIntValue(itemId)); // sets npc emote
        owner.getVarsManager().sendVar(1784, getId());
        owner.getPackets().sendGlobalConfig(1436, 0);
        unlockOrb(); // temporary
    }

    /**
     * Switch the Summoning orb state.
     *
     * @param enable If the orb should be enabled.
     */
    public void switchOrb(boolean enable) {
        owner.getPackets().sendConfig(1174, enable ? getId() : 0);
        if (enable) {
            unlock();
            return;
        }
        lockOrb();
    }

    /**
     * Unlocks the interfaces.
     */
    public void unlock() {
        owner.getPackets().sendIComponentSettings(1430, 35, -1, 1, 2);
        owner.getPackets().sendIComponentSettings(1506, 18, -1, 1, 2);
        owner.getPackets().sendIComponentSettings(662, 86, 0, 0, 2);
        owner.getPackets().sendIComponentSettings(662, 27, 0, 0, 2);
    }

    /**
     * Unlocks the orb.
     */
    public void unlockOrb() {
        owner.getPackets().sendHideIComponent(747, 9, false);
        Familiar.sendLeftClickOption(owner);
    }

    public Pets getType() {
        return pet;
    }

    public void sendInteract() {
        if (getId() == 20390) {
            Dialogue.sendSingleNPCDialogue(owner, 20390, Dialogue.HAPPY, Utils.randomFrom(Pet.CHEERLEADER_DIALOGUE));
            return;
        }
        // do pet interactions here petinteract interact
        if (getId() == 21817) {
            setNextForceTalk(new ForceTalk(Colors.PINK + Utils.formatPlayerNameForDisplay(owner.getDisplayName()) + " is an esteemed member of Ataraxia!"));
        } else {
            PetMorphHandler.Morphs morph = PetMorphHandler.Morphs.getMorphByNPCId(getId());
            if (morph != null) {
                PetMorphHandler.sendMorphOptions(morph, owner);
            } else
                owner.getDialogueManager().startDialogue("SimplePlayerMessage", "I better not touch it..");

            if (getId() != 23206 && getId() >= 23176 && getId() <= 23212 && !owner.getInteractedNoncombatPets().contains(getId())) {
                owner.getInteractedNoncombatPets().add(getId());
                val size = owner.getInteractedNoncombatPets().size();
                owner.sendMessage("You've now interacted with " + size + " non-combat pets.");

                if (size == 5) {
                    World.sendWorldMessage(Colors.RED + "<img=7>News: " + owner.getDisplayName() + " has unlocked the 'Jack of Trades' title!", false);
                } else if (size == 18) {
                    World.sendWorldMessage(Colors.RED + "<img=7>News: " + owner.getDisplayName() + " has unlocked the 'Jack of all Trades' title!", false);
                }
            }
        }
    }
}