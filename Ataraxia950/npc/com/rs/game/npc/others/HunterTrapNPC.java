package com.rs.game.npc.others;

import com.rs.game.Animation;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.player.PerkManager.DonationPerk;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.hunter.JadinkoCatch;
import com.rs.game.player.actions.hunter.TrapAction.HunterNPC;
import com.rs.game.player.actions.hunter.TrapAction.Traps;
import com.rs.game.player.content.OwnedObjectManager;
import com.rs.game.player.content.OwnedObjectManager.ConvertEvent;
import com.rs.game.player.content.Pots;
import com.rs.game.player.content.skillingcontracts.impl.HunterContractList;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Colors;
import com.rs.utils.Utils;

import java.util.List;

public class HunterTrapNPC extends NPC {

    private static final long serialVersionUID = -7102640126874184918L;
    private final Traps trap;
    private final HunterNPC hNPC;

    private WorldObject o;
    private int captureTicks;
    private boolean protean;

    public HunterTrapNPC(HunterNPC hNPC, int id, WorldTile tile, int mapAreaNameHash, boolean canBeAttackFromOutOfArea, boolean spawned) {
        super(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
        this.hNPC = hNPC;
        trap = hNPC.getTrap();
    }

    @Override
    public void processNPC() {
        super.processNPC();
        if (protean)
            return;
        if (captureTicks > 0) {
            captureTicks++;
            if (captureTicks == 5) {
                if (hNPC.equals(HunterNPC.CRIMSON_SWIFT) || hNPC.equals(HunterNPC.GOLDEN_WARBLER) || hNPC.equals(HunterNPC.COPPER_LONGTAIL) || hNPC.equals(HunterNPC.CERULEAN_TWITCH) || hNPC.equals(HunterNPC.TROPICAL_WAGTAIL) || hNPC.equals(HunterNPC.WIMPY_BIRD) || hNPC.equals(HunterNPC.GRENWALL) || hNPC.equals(HunterNPC.PAWYA))
                    addWalkSteps(o.getX(), o.getY(), -1, false);
            } else if (captureTicks == 6) {
                setNextAnimation(new Animation(hNPC.getIds()[1]));
            } else if (captureTicks == 7) {// up to five
                if (!OwnedObjectManager.convertIntoObject(o, new WorldObject(hNPC.getIds()[0], o.getType(), o.getRotation(), new WorldTile(o.getTileHash())), new ConvertEvent() {

                    @Override
                    public boolean canConvert(final Player player) {
                        if (player == null || isDead())
                            return false;
                        int currentLevel = player.getSkills().getLevel(Skills.HUNTER), lureLevel = hNPC.getLureLevel();
                        if (currentLevel < lureLevel)
                            return false;
                        if (!HunterTrapNPC.this.consumeRequiredBait(player))
                            return false;
                        double ratio = ((double) (trap.getRequirementLevel() + 20) / lureLevel) * currentLevel;
                        int lureChance = player.getPerkManager().hasPerkActive(DonationPerk.HUNTSMAN) ? 80 : 95;
                        if(player.jujuPotions.isActive(Pots.Effects.HUNTER_JUJU)) {
                            lureChance -= 15;
                        }
                        if (ratio < Utils.random(lureChance))
                            return false;
                        if(trap == Traps.MARASAMAW_PLANT) {
                            player.hunterCatch.put(o, new JadinkoCatch(hNPC));
                        }
                        return true;
                    }
                })) {
                    int anim = hNPC.getIds()[2];
                    if (anim != -1)
                        setNextAnimation(new Animation(anim));
                    OwnedObjectManager.convertIntoObject(o, new WorldObject(trap.getFailedObjectId(this), o.getType(), o.getRotation(), new WorldTile(o.getTileHash())), null);
                } else
                    setRespawnTask();
            } else if (captureTicks == 8) {
                setCantInteract(false);
                setNextAnimation(new Animation(-1));
            } else if (captureTicks == 10) {
                o = null;
                captureTicks = 0;
            }
            return;
        }

        if (o != null || hasFinished())
            return;
        List<WorldObject> objects = World.getRegion(getRegionId()).getSpawnedObjects();
        if (objects == null)
            return;
        for (final WorldObject o : objects) {
            if (o.getId() == 93381) {
                if (withinDistance(o, 1) && Utils.random(1) == 0) {
                    setCantInteract(true);
                    resetWalkSteps();
                    faceObject(o);
                    this.o = o;
                    trapEntity();
                    protean = true;
                    break;
                }
            }
            if (o.getId() != trap.getIds()[1] || !withinDistance(o, 4) || Utils.random(25) != 0)
                continue;
            this.o = o;
            this.captureTicks = 1;
            setCantInteract(true);
            resetWalkSteps();
            calcFollow(o, true);
            faceObject(o);
            break;
        }
    }

    private boolean consumeRequiredBait(Player player) {
        int baitId = getRequiredBait();
        if (baitId == -1)
            return true;
        if (!player.getInventory().containsItem(baitId, 1)) {
            player.sendMessage("You need " + getRequiredBaitName() + " to bait this trap.");
            return false;
        }
        player.getInventory().deleteItem(baitId, 1);
        return true;
    }

    private int getRequiredBait() {
        if (hNPC == HunterNPC.PAWYA)
            return 5972;
        if (hNPC == HunterNPC.GRENWALL)
            return 12535;
        return -1;
    }

    private String getRequiredBaitName() {
        if (hNPC == HunterNPC.PAWYA)
            return "a papaya";
        if (hNPC == HunterNPC.GRENWALL)
            return "raw pawya meat";
        return "bait";
    }

    private boolean success(Player player) {
        if (player == null || isDead())
            return false;
        int currentLevel = (int) (player.getSkills().getLevel(Skills.HUNTER) * player.getAuraManager().getHunterMultiplier()), lureLevel = hNPC.getLureLevel();
        double ratio = ((double) (trap.getRequirementLevel() + 20) / lureLevel) * currentLevel;
        int lureChance = player.getPerkManager().hasPerkActive(DonationPerk.HUNTSMAN) ? 55 : 70;
        if (player.jujuPotions.isActive(Pots.Effects.HUNTER_JUJU)) {
            lureChance -= 10;
        }
        return currentLevel >= lureLevel && !(ratio < Utils.random(lureChance));
    }

    private void trapEntity() {
        NPC npc = this;
        Player owner = OwnedObjectManager.getOwner(o);
        if (owner != null)
            OwnedObjectManager.removeEntry(owner, o);
        WorldTasksManager.schedule(new WorldTask() {
            int ticks;

            @Override
            public void run() {
                if (ticks == 0)
                    setNextAnimation(new Animation(hNPC.getIds()[1]));
                else if (ticks == 1) {
                    World.sendObjectAnimation(o, new Animation(24672));
                    finish();
                    setRespawnTask();
                    setCantInteract(false);
                    reset();
                    setLocation(getRespawnTile());
                } else if (ticks == 2) {
                    if (owner != null) {
                        owner.addCreaturesCaught();
                        HunterNPC captured = null;
                        x:
                        for (HunterNPC entities : HunterNPC.values()) {
                            for (int i = 0; i < entities.getNpcIds().length; i++)
                                if (entities.getNpcIds()[i] == npc.getId()) {
                                    captured = entities;
                                    break x;
                                }
                        }
                        if (captured == null) {
                            stop();
                            return;
                        }
                        HunterContractList.listen(owner, captured, 1);
                        owner.sendMessage("You've caught a " + Utils.formatPlayerNameForDisplay(captured.toString()) + "; creatures caught: " + Colors.RED + Utils.getFormattedNumber(owner.getCreaturesCaught()) + "</col>.", true);
                        owner.getSkills().addXp(Skills.HUNTER, success(owner) ? captured.getExp() * 1.5 : captured.getExp() / 2);
                        protean = false;
                        World.removeObject(o);
                        o = null;
                    }
                    stop();
                    return;
                }
                ticks++;
            }
        }, 0, 1);
    }
}
