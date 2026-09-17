package com.rs.game.npc.combat;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.Hit;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.activites.dungeon_architect.DungeonArchitectController;
import com.rs.game.activites.quest.deathsbounty.HusbandMichNPC;
import com.rs.game.activities.aod.npc.Amalgation;
import com.rs.game.activities.aod.npc.AoDNex;
import com.rs.game.activities.aod.npc.PraesulMinion;
import com.rs.game.npc.Ed3boss1.Ed3boss1;
import com.rs.game.npc.NPC;
import com.rs.game.npc.Trex.Trex;
import com.rs.game.npc.dungeonnering.DungeonBoss;
import com.rs.game.npc.dungeonnering.ShadowForgerIhlakhizan;
import com.rs.game.npc.eds.EliteDungeonNPC;
import com.rs.game.npc.eds.MasutaTheAscended;
import com.rs.game.npc.eds.MasutaTheAscended.ThrashingWater;
import com.rs.game.npc.eds.SeiryuTheAzureSerpent;
import com.rs.game.npc.eds.TheSanctumGuardian;
import com.rs.game.npc.familiar.Abyssallurker;
import com.rs.game.npc.familiar.Arcticbear;
import com.rs.game.npc.familiar.Beaver;
import com.rs.game.npc.familiar.Bullant;
import com.rs.game.npc.familiar.Bunyip;
import com.rs.game.npc.familiar.Familiar;
import com.rs.game.npc.familiar.Fruitbat;
import com.rs.game.npc.familiar.Ibis;
import com.rs.game.npc.familiar.Lightcreature;
import com.rs.game.npc.familiar.Macaw;
import com.rs.game.npc.familiar.Nightmaremuspah;
import com.rs.game.npc.familiar.Packmammoth;
import com.rs.game.npc.familiar.Packyak;
import com.rs.game.npc.familiar.Spiritterrorbird;
import com.rs.game.npc.familiar.Wartortoise;
import com.rs.game.npc.glacor.Glacyte;
import com.rs.game.npc.godwars.saradomin.CommanderZilyana;
import com.rs.game.npc.godwars.zaros.Nex;
import com.rs.game.npc.kalphiteking.KalphiteKing;
import com.rs.game.npc.others.DreadNip;
import com.rs.game.npc.others.GemstoneDragon;
import com.rs.game.npc.others.Legios;
import com.rs.game.npc.pest.PestMonsters;
import com.rs.game.npc.pest.PestPortal;
import com.rs.game.npc.pest.Shifter;
import com.rs.game.npc.solak.Solak;
import com.rs.game.npc.spiderboss.Araxxor;
import com.rs.game.npc.spiderboss.AraxxorMinion;
import com.rs.game.npc.telos.ColoredAnimaGolem;
import com.rs.game.npc.telos.Telos;
import com.rs.game.npc.themagister.ImperialAkh;
import com.rs.game.npc.themagister.TheMagister;
import com.rs.game.npc.vorago.Scopulus;
import com.rs.game.npc.vorago.Vorago;
import com.rs.game.player.Player;
import com.rs.game.player.actions.invention.InventionConstants.Perks;
import com.rs.game.player.actions.invention.InventionData.Perk;
import com.rs.game.player.combat.PlayerCombat;
import com.rs.game.player.combat.SimpleNPCRouteEvent;
import com.rs.game.player.content.Combat;

import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.MapAreas;
import com.rs.utils.Utils;

public class NPCCombat {

    protected NPC npc;
    protected int combatDelay;
    protected Entity target;

    public NPCCombat(final NPC npc) {
        this.npc = npc;
    }

    public int getCombatDelay() {
        return combatDelay;
    }
    public static final double NPC_TO_PLAYER_DAMAGE_MULT = 0.50; // 50% damage (50% nerf)
    public void setCombatDelay(final int delay) {
        combatDelay = delay;
    }

    /*
     * returns if under combat
     */
    public boolean process() {
        if (combatDelay > 0) {
            combatDelay--;
        }
        if (target != null) {
            if (!checkAll()) {
                removeTarget();
                return false;
            }
            if (combatDelay <= 0) {
                combatDelay = combatAttack();
            }
            return true;
        }
        return false;
    }

    /*
     * return combatDelay
     */
    public int combatAttack() {
        final Entity target = this.target; // prevents multithread issues
        if (target == null) {
            return 0;
        }
        if (npc.isLocked()) {
            return 0;
        }
        // check if close to target, if not let it just walk and dont attack
        // this gametick
        final NPCCombatDefinition defs = npc.getCombatDefinitions();
        final int attackStyle = defs.getAttackStyle();
        int maxDistance = attackStyle == NPCCombatDefinitionConstants.MELEE || attackStyle == NPCCombatDefinitionConstants.SPECIAL2 ? 0 : 7;
        if (npc.getMaxDistance() != -1) {
            maxDistance = npc.getMaxDistance();
        }
        if (npc instanceof HusbandMichNPC)
            maxDistance = 16;

        if (npc instanceof Araxxor || npc instanceof AraxxorMinion)
            maxDistance = 5000000;
        if (npc instanceof KalphiteKing) {
            maxDistance = 20;
        }
        if (npc instanceof Ed3boss1) {
            maxDistance = 10;
        }
        if (npc.getId() == 26149 || npc.getId() == 26157 || npc.getId() == 26154) {
            maxDistance = 25;
        }
        if (npc instanceof Solak && npc.getId() == 25529) {
            maxDistance = 50;
        }

        if (npc instanceof DungeonBoss) {
            if (npc.getCombatDefinitions().getAttackStyle() != NPCCombatDefinitionConstants.MELEE || npc instanceof ShadowForgerIhlakhizan) {
                maxDistance = 16;
            }
        }

        if (npc instanceof Vorago) {
            maxDistance = 30;
        }
        if (npc instanceof Trex) {
            maxDistance = 50;
        }
    

        if (npc instanceof Telos || npc instanceof ColoredAnimaGolem || npc instanceof TheMagister || npc instanceof ImperialAkh)
            maxDistance = 50;
        if (npc instanceof TheSanctumGuardian)
            maxDistance = 30;
        if (npc instanceof MasutaTheAscended || npc instanceof ThrashingWater || npc instanceof SeiryuTheAzureSerpent)
            maxDistance = 50;


        /* If NPC is frozen, check if it is within attack distance */
        if (npc.getFreezeDelay() >= Utils.currentTimeMillis()) {
            if (NPCCombatDefinitionConstants.MELEE == attackStyle && !npc.withinDistance(target, 1)) {
                return 0;
            }
            if (attackStyle != NPCCombatDefinitionConstants.MELEE && !npc.withinDistance(target, maxDistance)) {
                return 0;
            }
        }
        if (!(npc instanceof TheMagister  || npc instanceof ImperialAkh || npc instanceof TheSanctumGuardian || npc instanceof MasutaTheAscended || npc instanceof ThrashingWater || npc instanceof SeiryuTheAzureSerpent) && (!(npc instanceof Nex)) && (!(npc instanceof Vorago)) && (!(npc instanceof Solak)) && (!(npc instanceof Trex)) && (!(npc instanceof Telos)) && (!(npc instanceof ColoredAnimaGolem)) && (!(npc instanceof Araxxor || npc instanceof AraxxorMinion)) && !npc.clipedProjectile(target, maxDistance == 0 && !forceCheckClipAsRange(target))) {
            return 0;
        }


        final int size = npc.getSize();
        if (!Utils.isOnRange(npc.getX(), npc.getY(), size, target.getX(), target.getY(), target.getSize(), maxDistance)) {
            return 0;
        }
        if (!(npc instanceof Vorago) && Utils.colides(npc.getX(), npc.getY(), size, target.getX(), target.getY(), target.getSize())) {
            return 0;
        }
        if (!(npc instanceof Trex) && Utils.colides(npc.getX(), npc.getY(), size, target.getX(), target.getY(), target.getSize())) {
            return 2;
        }
        if (!(npc instanceof Solak) && Utils.colides(npc.getX(), npc.getY(), size, target.getX(), target.getY(), target.getSize())) {
            return 0;
        }
        /* combat stuff 1/15 */
        // if(getCombatDelay() > 0 ||
        // Utils.currentTimeMillis()-(getCombatDelay() * 600) < 4200)
        // Logger.getGlobal().info("fuck!");
        // setCombatDelay(defs.getAttackDelay());
        addAttackedByDelay(target);
        return CombatScriptsHandler.specialAttack(npc, target);
    }

    protected void doDefenceEmote(final Entity target) {
        if (target instanceof Player && ((Player) target).getAppearence().getRenderEmote() == 2985) {
            return;
        }
        if (target.isCantDoDefenceEmote()) {
            return;
        }
        target.setNextAnimationNoPriority(new Animation(Combat.getDefenceEmote(target)));
    }

    public Entity getTarget() {
        return target;
    }

    public void setTarget(final Entity target) {
        this.target = target;
        if (npc.isCannotMove()) {
            return;
        }
        npc.setNextFaceEntity(target);
        if (!checkAll()) {
            removeTarget();
            return;
        }
    }

    public void addAttackedByDelay(final Entity target) { // prevents multithread
        // issues
        target.setAttackedBy(npc);
        target.setAttackedByDelay(Utils.currentTimeMillis() + 8000); // 8seconds
        npc.setAttackingDelay(Utils.currentTimeMillis() + 8000);
    }

    public boolean checkAll() {
        final Entity target = this.target; // prevents multithread issues

        if (npc instanceof Familiar) {
            if (npc instanceof Beaver || npc instanceof Bullant || npc instanceof Macaw || npc instanceof Ibis || npc instanceof Abyssallurker || npc instanceof Fruitbat || npc instanceof Bunyip || npc instanceof Arcticbear || npc instanceof Packyak || npc instanceof Spiritterrorbird || npc instanceof Wartortoise || npc instanceof Nightmaremuspah || npc instanceof Packmammoth || npc instanceof Lightcreature) {
                return false;
            }
        }

        if (target == null) {
            return false;
        }
        if (npc.isDead() || npc.hasFinished() || npc.isForceWalking() || target.isDead() || target.hasFinished() || npc.getPlane() != target.getPlane()) {
            return false;
        }

        if (npc.isCantInteract()) {
            return false;
        }

        if (npc.isCannotMove()) {
            return false;
        }

        if (target instanceof DreadNip) {
            return false;
        }
        if (World.getRegion(npc.getRegionId()) != World.getRegion(target.getRegionId()) && !(npc instanceof AoDNex) && !(npc instanceof CommanderZilyana) && npc.getId() != 6248 && npc.getId() != 6250 && npc.getId() != 6252 && !(npc instanceof Legios) && !(npc instanceof Telos) && !(npc instanceof TheMagister || npc instanceof ImperialAkh) && !(npc instanceof ColoredAnimaGolem) && !(npc instanceof Araxxor || npc instanceof AraxxorMinion) && !(npc instanceof GemstoneDragon) && !(npc instanceof Vorago) && !(npc instanceof Solak) && !(npc instanceof Trex) && !(npc instanceof AoDNex)
                && !(npc instanceof PraesulMinion) && !(npc instanceof Amalgation) && !(npc instanceof Glacyte) && !(npc instanceof EliteDungeonNPC))
            return false;
        if (npc.getFreezeDelay() >= Utils.currentTimeMillis()) {
            return true; // if freeze cant move ofc
        }
        int distanceX = npc.getX() - npc.getRespawnTile().getX();
        int distanceY = npc.getY() - npc.getRespawnTile().getY();
        int size = npc.getSize();
        if (target.getX() < npc.getX() || target.getY() < npc.getY()) {
            size = npc.getSize() > target.getSize() ? npc.getSize() : target.getSize();
        }
        int maxDistance;
        if (npc.getMaxDistance() != -1) {
            maxDistance = npc.getMaxDistance();
        }

        if (!npc.isNoDistanceCheck() && !npc.isCantFollowUnderCombat() && !(npc instanceof Vorago) && !(npc instanceof Solak) && !(npc instanceof Trex) && !(npc instanceof Nex) && !(npc instanceof Telos) && !(npc instanceof ColoredAnimaGolem) && !(npc instanceof Araxxor || npc instanceof AraxxorMinion) && !(npc instanceof TheMagister || npc instanceof ImperialAkh) && !(npc instanceof EliteDungeonNPC)) {
            maxDistance = 16;
            if (!(npc instanceof Familiar)) {
                if (npc.getMapAreaNameHash() != -1) {
                    // if out his area
                    if (!MapAreas.isAtArea(npc.getMapAreaNameHash(), npc) || (!npc.canBeAttackFromOutOfArea() && !MapAreas.isAtArea(npc.getMapAreaNameHash(), target))) {
                        npc.forceWalkRespawnTile();
                        return false;
                    }
                } else if (distanceX > size + maxDistance || distanceX < -1 - maxDistance || distanceY > size + maxDistance || distanceY < -1 - maxDistance) {
                    // if more than 16 distance from respawn place
                    npc.forceWalkRespawnTile();
                    return false;
                }
            }
            maxDistance = 16;
            distanceX = target.getX() - npc.getX();
            distanceY = target.getY() - npc.getY();
            if (distanceX > size + maxDistance || distanceX < -1 - maxDistance || distanceY > size + maxDistance || distanceY < -1 - maxDistance) {
                return false; // if target distance higher 16
            }

        } else {
            distanceX = target.getX() - npc.getX();
            distanceY = target.getY() - npc.getY();
        }
        // checks for no multi area :)
        if (npc instanceof Familiar) {
            final Familiar familiar = (Familiar) npc;
            if (!familiar.canAttack(target)) {
                return false;
            }
        } else {
            if (!npc.isForceMultiAttacked()) {
                if (!target.isAtMultiArea() || !npc.isAtMultiArea()) {
                    if (npc.getAttackedBy() != target && npc.getAttackedByDelay() > Utils.currentTimeMillis()) {
                        return false;
                    }
                    if (!npc.getDefinitions().getName().toLowerCase().contains("dread") && target.getAttackedBy() != npc && target.getAttackedByDelay() > Utils.currentTimeMillis()) {
                        return false;
                    }
                }
            }
        }

        if (!npc.isCantFollowUnderCombat()) {
            // if is under

            final int targetSize = target.getSize();
            if ( !(npc instanceof Trex) && !(npc instanceof Vorago) && !(npc instanceof Solak) && !(npc instanceof KalphiteKing) && !(npc instanceof TheMagister || npc instanceof ImperialAkh) && !(npc instanceof Telos) && !(npc instanceof ColoredAnimaGolem) && !(npc instanceof DreadNip) && npc.getId() == 25513) {
                if (distanceX < size && distanceX > -targetSize && distanceY < size && distanceY > -targetSize && !target.hasWalkSteps()) {
                    new SimpleNPCRouteEvent(npc, target).execute();
                    /*
                     * npc.resetWalkSteps(); if (!npc.addWalkSteps(target.getX() + 1, npc.getY())) {
                     * npc.resetWalkSteps(); if (!npc.addWalkSteps(target.getX() - size,
                     * npc.getY())) { npc.resetWalkSteps(); if (!npc.addWalkSteps(npc.getX(),
                     * target.getY() + 1)) { npc.resetWalkSteps(); if (!npc.addWalkSteps(npc.getX(),
                     * target.getY() - size)) { return true; } } } }
                     */
                    return true;
                }
            }

            if (!(npc instanceof Trex) && !(npc instanceof Vorago) && !(npc instanceof Solak) && !(npc instanceof KalphiteKing) && !(npc instanceof TheMagister || npc instanceof ImperialAkh) && !(npc instanceof Telos) && !(npc instanceof ColoredAnimaGolem) && !(npc instanceof Araxxor || npc instanceof AraxxorMinion) && npc.getId() == 25513) {
                if (npc.getCombatDefinitions().getAttackStyle() == NPCCombatDefinitionConstants.MELEE && targetSize == 1 && size == 1 && Math.abs(npc.getX() - target.getX()) == 1 && Math.abs(npc.getY() - target.getY()) == 1 && !target.hasWalkSteps()) {
                    if (!npc.addWalkSteps(target.getX(), npc.getY(), 1)) {
                        npc.addWalkSteps(npc.getX(), target.getY(), 1);
                    }
                    return true;
                }
            }
            final int attackStyle = npc.getCombatDefinitions().getAttackStyle();
            if (npc instanceof Nex) {
                final Nex nex = (Nex) npc;
                maxDistance = nex.isForceFollowClose() ? 0 : 7;
                if (!nex.isFlying() && (!npc.clipedProjectile(target, maxDistance == 0 && !forceCheckClipAsRange(target))) || !Utils.isOnRange(npc.getX(), npc.getY(), size, target.getX(), target.getY(), targetSize, maxDistance)) {
                    npc.resetWalkSteps();
                    if (!Utils.isOnRange(npc.getX(), npc.getY(), size, target.getX(), target.getY(), targetSize, 10)) {
                        final int[][] dirs = Utils.getCoordOffsetsNear(size);
                        for (int dir = 0; dir < dirs[0].length; dir++) {
                            final WorldTile tile = new WorldTile(new WorldTile(target.getX() + dirs[0][dir], target.getY() + dirs[1][dir], target.getPlane()));
                            if (World.isTileFree(tile.getPlane(), tile.getX(), tile.getY(), size)) {
                                npc.setNextAnimation(new Animation(17408));
                                npc.setNextWorldTile(tile);
                                nex.setFlying(false);
                                return true;
                            }
                        }
                    } else {
                        npc.calcFollow(target, 2, true, npc.isIntelligentRouteFinder());
                    }
                    return true;
                } else {
                    // if doesnt need to move more stop moving
                    npc.resetWalkSteps();
                }
            } else {
                if (npc instanceof HusbandMichNPC) {
                    maxDistance = 8;
                    if (!Utils.isOnRange(npc.getX(), npc.getY(), size, target.getX(), target.getY(), targetSize, maxDistance)) {
                        npc.resetWalkSteps();
                        npc.calcFollow(target, 4, true, npc.isIntelligentRouteFinder());
                        return true;
                    }
                }
            }

                if (npc instanceof Vorago) {
                    maxDistance = npc.isForceFollowClose() ? 1 : 7;
                    if (!Utils.isOnRange(npc.getX(), npc.getY(), size, target.getX(), target.getY(), targetSize, maxDistance)) {
                        npc.resetWalkSteps();
                        npc.calcFollow(target, 4, true, npc.isIntelligentRouteFinder());
                        return true;
                    }
                }

           else if (npc instanceof Trex && npc.getId() == 26435) {


                maxDistance = npc.isForceFollowClose()? 1 : 7;
                if (!Utils.isOnRange(npc.getX(), npc.getY(), size, target.getX(), target.getY(), targetSize, maxDistance)) {
                    npc.resetWalkSteps();
                    npc.calcFollow(target, 4, true, npc.isIntelligentRouteFinder());
                    return true;
                }
            }

                else if (npc instanceof EliteDungeonNPC && (npc.getDefinitions().getName().equalsIgnoreCase("Elite Sotapanna") || npc.getDefinitions().getName().equalsIgnoreCase("Elite Sakadagami") || npc.getDefinitions().getName().equalsIgnoreCase("Eastern mercenary") || npc.getId() == 25607 || npc.getId() == 25614 || npc.getId() == 25615 || npc.getId() == 25619 || npc.getId() == 25621 || npc.getId() == 25622 || npc.getId() == 25631 || npc.getId() == 25623 || npc.getId() == 25624 || npc.getId() == 25626 || npc.getId() == 25620 || npc.getId() == 25628 || npc.getId() == 25629)) {
                    int combatType = npc.getId() == 25629 || npc.getId() == 25628 || npc.getId() == 25626 || npc.getId() == 25624 || npc.getId() == 25623 || npc.getId() == 25622 || npc.getId() == 25621 || npc.getId() == 25573 || npc.getId() == 25578 || npc.getId() == 25574 || npc.getId() == 25579 || npc.getDefinitions().getName().equalsIgnoreCase("Eastern mercenary") || npc.getId() == 25607 ? Combat.MELEE_TYPE : npc.getId() == 25576 || npc.getId() == 25581 || npc.getId() == 25614 || npc.getId() == 25615 ? Combat.RANGE_TYPE : Combat.MAGIC_TYPE;
                    maxDistance = npc.isForceFollowClose() ? 0 : combatType == Combat.MELEE_TYPE ? 0 : combatType == Combat.RANGE_TYPE ? 5 : 6;
                    if (!Utils.isOnRange(npc, target, maxDistance)) {
                        npc.resetWalkSteps();
                        npc.calcFollow(target, 2, true, npc.isIntelligentRouteFinder());
                        return true;
                    }
                } else if (npc instanceof EliteDungeonNPC && (npc.getDefinitions().getName().equalsIgnoreCase("Anagami") || npc.getDefinitions().getName().equalsIgnoreCase("Arhat"))) {
                    maxDistance = npc.isForceFollowClose() ? 0 : 6;
                    if (!Utils.isOnRange(npc, target, maxDistance)) {
                        npc.resetWalkSteps();
                        npc.calcFollow(target, 2, true, npc.isIntelligentRouteFinder());
                        return true;
                    }
                } else if (npc instanceof TheSanctumGuardian) {
                    maxDistance = npc.isForceFollowClose() ? 0 : 9;
                    if (!Utils.isOnRange(npc, target, maxDistance)) {
                        npc.resetWalkSteps();
                        npc.calcFollow(target, 2, true, npc.isIntelligentRouteFinder());
                        return true;
                    }
                } else if (npc instanceof MasutaTheAscended) {
                    maxDistance = npc.isForceFollowClose() ? 0 : 7;
                    if (!Utils.isOnRange(npc, target, maxDistance)) {
                        npc.resetWalkSteps();
                        npc.calcFollow(target, 2, true, npc.isIntelligentRouteFinder());
                        return true;
                    }
                }
                else if (npc instanceof Scopulus) {
                    maxDistance = 0;
                    if (!Utils.isOnRange(npc.getX(), npc.getY(), size, target.getX(), target.getY(), targetSize, maxDistance)) {
                        npc.resetWalkSteps();
                        npc.calcFollow(target, 4, true, npc.isIntelligentRouteFinder());
                        return true;
                    }
                } else if (npc instanceof Telos) {
                    maxDistance = npc.isForceFollowClose() ? 0 : 5;
                    npc.resetWalkSteps();
                    // is far from target, moves to it till can attack
                    if ((!npc.clipedProjectile(target, maxDistance == 0 && !forceCheckClipAsRange(target))) || !Utils.isOnRange(npc.getX(), npc.getY(), size, target.getX(), target.getY(), targetSize, maxDistance)) {
                        npc.calcFollow(target, npc.getRun() ? 2 : 1, true, npc.isIntelligentRouteFinder());
                        return true;
                    }
                } else if (npc instanceof ColoredAnimaGolem) {
                    maxDistance = 0;
                    npc.resetWalkSteps();
                    // is far from target, moves to it till can attack
                    if ((!npc.clipedProjectile(target, maxDistance == 0 && !forceCheckClipAsRange(target))) || !Utils.isOnRange(npc.getX(), npc.getY(), size, target.getX(), target.getY(), targetSize, maxDistance)) {
                        npc.calcFollow(target, npc.getRun() ? 2 : 1, true, npc.isIntelligentRouteFinder());
                        return true;
                    }
                }





                else if (npc instanceof KalphiteKing) {
                    maxDistance = npc.isForceFollowClose() || npc.getId() == 16697 ? 0 : 8;
                    if (!Utils.isOnRange(npc.getX(), npc.getY(), size, target.getX(), target.getY(), targetSize, maxDistance)) {
                        npc.resetWalkSteps();
                        npc.calcFollow(target, 4, true, npc.isIntelligentRouteFinder());
                        return true;
                    }
                } else if (npc instanceof Araxxor) {
                    maxDistance = ((Araxxor) npc).getAttackDistance();
                    npc.resetWalkSteps();
                    // is far from target, moves to it till can attack
                    if ((!npc.clipedProjectile(target, maxDistance == 0 && !forceCheckClipAsRange(target))) || !Utils.isOnRange(npc.getX(), npc.getY(), size, target.getX(), target.getY(), targetSize, maxDistance)) {
                        npc.calcFollow(target, 2, true, npc.isIntelligentRouteFinder());
                        return true;
                    }
                } else if (npc instanceof AraxxorMinion) {
                    maxDistance = (npc.getId() == 19458 || npc.getId() == 19468 || npc.getId() == 19470) ? 0 : 7;
                    npc.resetWalkSteps();
                    // is far from target, moves to it till can attack
                    if ((!npc.clipedProjectile(target, maxDistance == 0 && !forceCheckClipAsRange(target))) || !Utils.isOnRange(npc.getX(), npc.getY(), size, target.getX(), target.getY(), targetSize, maxDistance)) {
                        npc.calcFollow(target, 2, true, npc.isIntelligentRouteFinder());
                        return true;
                    }

                }
                else if (npc instanceof DreadNip) {
                    maxDistance = 0;
                    npc.resetWalkSteps();
                    // is far from target, moves to it till can attack
                    if (!Utils.isOnRange(npc.getX(), npc.getY(), size, target.getX(), target.getY(), targetSize, maxDistance)) {
                        npc.calcFollow(target, npc.getRun() ? 2 : 1, true, npc.isIntelligentRouteFinder());
                        return true;
                    }
                } else if (npc instanceof PestMonsters) {
                    maxDistance = attackStyle == NPCCombatDefinitionConstants.MELEE ? 0 : 8;
                    if (npc instanceof Shifter) {
                        npc.resetWalkSteps();
                        if (Utils.getDistance(npc, target) > 6 || (!npc.clipedProjectile(target, true)) && Utils.random(4) == 0) {
                            ((Shifter) npc).teleport(target, 1);
                        } else {
                            npc.calcFollow(target, 4, true, npc.isIntelligentRouteFinder());
                        }
                        return true;
                    } else if (!Utils.isOnRange(npc.getX(), npc.getY(), size, target.getX(), target.getY(), targetSize, maxDistance)) {
                        npc.resetWalkSteps();
                        npc.calcFollow(target, 4, true, npc.isIntelligentRouteFinder());
                    }
                    return true;
                } else if (npc instanceof TheMagister) {
                    maxDistance = npc.isForceFollowClose() ? 0 : 6;
                    npc.resetWalkSteps();
                    // is far from target, moves to it till can attack
                    if ((!npc.clipedProjectile(target, maxDistance == 0 && !forceCheckClipAsRange(target))) || !Utils.isOnRange(npc.getX(), npc.getY(), size, target.getX(), target.getY(), targetSize, maxDistance)) {
                        npc.calcFollow(target, npc.getRun() ? 2 : 1, true, npc.isIntelligentRouteFinder());
                        return true;
                    }
                } else if (npc instanceof ImperialAkh) {
                    maxDistance = npc.isForceFollowClose() ? 0 : (npc.getId() == 24766 ? 0 : 6);
                    npc.resetWalkSteps();
                    // is far from target, moves to it till can attack
                    if ((!npc.clipedProjectile(target, maxDistance == 0 && !forceCheckClipAsRange(target))) || !Utils.isOnRange(npc.getX(), npc.getY(), size, target.getX(), target.getY(), targetSize, maxDistance)) {
                        npc.calcFollow(target, npc.getRun() ? 2 : 1, true, npc.isIntelligentRouteFinder());
                        return true;
                    }
                } else {
                    maxDistance = npc.isForceFollowClose() ? 0 : (attackStyle == NPCCombatDefinitionConstants.MELEE || attackStyle == NPCCombatDefinitionConstants.SPECIAL2) ? 0 : 7;
                    if (npc.getMaxDistance() != -1) {
                        maxDistance = npc.getMaxDistance();
                    }
                    npc.resetWalkSteps();
                    if ((!npc.clipedProjectile(target, maxDistance == 0 && !forceCheckClipAsRange(target))) || distanceX > size + maxDistance || distanceX < -1 - maxDistance || distanceY > size + maxDistance || distanceY < -1 - maxDistance) {
                        if ((!npc.clipedProjectile(target, maxDistance == 0 && !forceCheckClipAsRange(target))) || !Utils.isOnRange(npc.getX(), npc.getY(), size, target.getX(), target.getY(), targetSize, maxDistance)) {
                            if (!npc.calcFollow(target, npc.getRun() ? 2 : 1, true, npc.isIntelligentRouteFinder()) && combatDelay < 3 && maxDistance == 0) {
                                combatDelay = 3;
                            }
                            return true;
                        }
                        return true;
                    }

                }
            }

        return true;
    }

    public void addCombatDelay(final int delay) {
        combatDelay += delay;
    }

    public boolean underCombat() {
        return target != null;
    }

    public void removeTarget() {
        target = null;
        npc.setNextFaceEntity(null);
        if (npc.hasChangedRenderAnimation() && npc.getDefinitions().getName().equalsIgnoreCase("Hanto sellsword")) {
            npc.setNextRenderAnimation(0);
            npc.setChangedRenderAnimation(false);
            npc.setNextAnimation(new Animation(18025));
        }
    }

    public void reset() {
        combatDelay = 0;
        target = null;
        specialAttackPercentage = 0;
    }

    public void delayHit(final NPC npc, final int delay, final Entity target, final Hit... hits) {
        npc.getCombat().addAttackedByDelay(target);
        WorldTasksManager.schedule(new WorldTask() {

            @Override
            public void run() {
                for (final Hit hit : hits) {
                    final NPC npc = (NPC) hit.getSource();
                    if (npc.isDead() || npc.hasFinished() || target.isDead() || target.hasFinished()) {
                        return;
                    }

                    int before = hit.getDamage();
                    if (before > 0 && target instanceof Player) {
                        int after = (int) Math.round(before * NPC_TO_PLAYER_DAMAGE_MULT);



                            //((Player) target).getPackets().sendGameMessage(
                           //         "NPC dmg scale: " + before + " -> " + after + " (mult=" + NPC_TO_PLAYER_DAMAGE_MULT + ")"
                         //  );



                        hit.setDamage(after);
                    }

                    target.applyHit(hit);
                    npc.getCombat().doDefenceEmote(target);
                    if (target instanceof Player) {
                        final Player p2 = (Player) target;
                        /*
                         * if (p2.getControlerManager().getControler() instanceof Barrows)
                         * p2.closeInterfaces();
                         */
                        Perk cautious = p2.getInventionManager().hasPerk(Perks.CAUTIOUS);
                        if (p2.getCombatDefinitions().isAutoRetaliate() && !p2.getActionManager().hasSkillWorking() && !p2.hasWalkSteps() && cautious == null) {
                            p2.getActionManager().setAction(new PlayerCombat(npc));
                        }
                    } else {
                        final NPC n = (NPC) target;
                        if (!n.isUnderCombat() || n.canBeAttackedByAutoRetaliate()) {
                            n.setTarget(npc);
                        }
                    }

                }
                stop();
            }

        }, delay);
    }

    private boolean forceCheckClipAsRange(final Entity target) {
        return target instanceof TheSanctumGuardian || target instanceof PestPortal || (target instanceof Telos && ((Telos) target).getPhase() == 4);
    }

    private byte specialAttackPercentage;

    public int getSpecialAttackPercentage() {
        return specialAttackPercentage;
    }

    public void increaseSpecialAttack(int amount) {
        if (amount != 0) {
            int spec = specialAttackPercentage + amount;
            specialAttackPercentage = (byte) (spec < 0 ? 0 : spec > 100 ? 100 : spec);
        }
    }

    public void decreaseSpecialAttack(int amount) {
        if (amount > 0)
            specialAttackPercentage -= amount;
    }
}