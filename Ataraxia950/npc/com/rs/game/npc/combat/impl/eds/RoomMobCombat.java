package com.rs.game.npc.combat.impl.eds;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

import com.rs.cache.loaders.ItemDefinitions;
import com.rs.cache.loaders.rs3.RS3ClientScriptMap;
import com.rs.cache.loaders.rs3.RS3GeneralRequirementMap;
import com.rs.game.Animation;
import com.rs.game.EffectsManager.Effect;
import com.rs.game.EffectsManager.EffectType;
import com.rs.game.Entity;
import com.rs.game.Graphics;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.combat.NPCCombatDefinition;
import com.rs.game.npc.combat.NPCCombatDefinitionConstants;
import com.rs.game.npc.eds.EliteDungeonNPC;
import com.rs.game.player.Player;
import com.rs.game.player.TemporaryAttributes.Key;
import com.rs.game.player.content.Combat;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

public class RoomMobCombat extends CombatScript {
    public static final int ITEM_SHORTCUT = 0, MELEE_ABILITY_SHORTCUT = 1, STRENGTH_ABILITY_SHORTCUT = 2, RANGED_ABILITY_SHORTCUT = 5, DEFENCE_ABILITY_SHORTCUT = 3, HEAL_ABILITY_SHORTCUT = 4, MAGIC_ABILITY_SHORTCUT = 6, PRAYER_SHORTCUT = 7;
    private EliteDungeonNPC npc;

    private final int[][] RANGE_ABILITIES = { { 1, 4, 5, 6 }, { 7, 9 }, { 10, 12 } };// basic,thresh,ulti
    private final int[][] MAGIC_ABILITIES = { { 1, 3, 4, 5 }, { 9 }, { 12 } };
    private final int[][] MELEE_ABILITIES = { { 1, 3 }, { 7 }, { 10 } };
    private final int[][] STRENGTH_ABILITIES = { { 1, 3, }, { 7 }, { 10 } };

    @Override
    public int attack(NPC n, Entity target) {
        if (!(target instanceof Player))
            return 0;
        if (!(n instanceof EliteDungeonNPC))
            return 0;
        npc = (EliteDungeonNPC) n;
        Player player = (Player) target;
        if (!npc.hasChangedRenderAnimation())
            npc.setNextRenderAnimation(2688);
        int combatType = npc.getId() == 25629 || npc.getId() == 25628 || npc.getId() == 25626 || npc.getId() == 25624 || npc.getId() == 25623 || npc.getId() == 25622 || npc.getId() == 25621 || npc.getId() == 25573 || npc.getId() == 25578 || npc.getId() == 25574 || npc.getId() == 25579 || npc.getDefinitions().getName().equalsIgnoreCase("Eastern mercenary") || npc.getId() == 25607 ? Combat.MELEE_TYPE : npc.getId() == 25576 || npc.getId() == 25581 || npc.getId() == 25614 || npc.getId() == 25615 ? Combat.RANGE_TYPE : Combat.MAGIC_TYPE;
        int abilityBook = (combatType == Combat.MAGIC_TYPE) ? MAGIC_ABILITY_SHORTCUT : combatType == Combat.RANGE_TYPE ? RANGED_ABILITY_SHORTCUT : Utils.random(1) == 0 ? MELEE_ABILITY_SHORTCUT : STRENGTH_ABILITY_SHORTCUT;
        ArrayList<Integer> availableAbilites = getAvailableAbilities(abilityBook);
        int currentWeapon = npc.getWeaponId();
        if (npc.getFreezeDelay() > Utils.currentTimeMillis()) {
            if ((currentWeapon == 25654 || npc.getId() == 25628 || npc.getId() == 25624 || npc.getId() == 25620) && !npc.hasCooldown(2, DEFENCE_ABILITY_SHORTCUT)) {
                int mapId = RS3ClientScriptMap.getMap(EliteDungeonNPC.CS_DATA_ID[DEFENCE_ABILITY_SHORTCUT - 1]).getIntValue(2);
                RS3GeneralRequirementMap data = RS3GeneralRequirementMap.getMap(mapId);
                npc.setCooldown(mapId, data.getIntValue(2796));
                npc.getEffectsManager().startEffect(new Effect(EffectType.FREEDOM, 10));
                return 3;
            }
            return 0;
        }
        if ((npc.getId() == 25577 || npc.getId() == 25582)) {
            EliteDungeonNPC n2 = npc.getNearByNeedsHealing();
            if (n2 != null) {
                npc.setNextFaceEntity(null);
                npc.setNextFaceWorldTile(n2);
                npc.setNextAnimation(new Animation(npc.getAttackEmote()));
                WorldTasksManager.schedule(new WorldTask() {

                    @Override
                    public void run() {
                        n2.heal(500, 0, 0, true);
                        if (target != null && !target.isDead() && !target.hasFinished())
                            npc.setNextFaceEntity(target);
                    }

                }, 2);
                return 5;
            }
        }
        if (!Utils.isOnRange(npc, player, combatType == Combat.MELEE_TYPE ? 0 : combatType == Combat.RANGE_TYPE ? 5 : 6))
            return 0;
        NPCCombatDefinition defs = npc.getCombatDefinitions();
        ItemDefinitions idefs = ItemDefinitions.getItemDefinitions(npc.getWeaponId());
        int attackStyle = defs.getAttackStyle();
        if (availableAbilites == null || availableAbilites.isEmpty()) {
            npc.setNextAnimation(new Animation(npc.getAttackEmote()));
            if (attackStyle == NPCCombatDefinitionConstants.MELEE) {
                delayHit(npc, 0, target, getMeleeHit(npc, getRandomMaxHit(npc, defs.getMaxHit(), attackStyle, target)));
            } else {
                int damage = 0;
                damage = getRandomMaxHit(npc, defs.getMaxHit(), attackStyle, target);
                delayHit(npc, 2, target, attackStyle == NPCCombatDefinitionConstants.RANGE ? getRangeHit(npc, damage) : getMagicHit(npc, damage));
                int projectileGfx = attackStyle == NPCCombatDefinitionConstants.RANGE ? idefs.getCSOpcode(2940) : 2735;
                if (projectileGfx != 0)
                    World.sendProjectile(npc, target, defs.getAttackProjectile(), 41, 16, 41, 35, 16, 0);
            }
            npc.addAdrenaline(5);
            return npc.getAttackSpeed();
        }
        int abilityId = availableAbilites.get(Utils.random(availableAbilites.size() - 1));
        RS3GeneralRequirementMap data = RS3GeneralRequirementMap.getMap(RS3ClientScriptMap.getMap(EliteDungeonNPC.CS_DATA_ID[abilityBook - 1]).getIntValue(abilityId));

        RS3ClientScriptMap map = RS3ClientScriptMap.getMap(data.getIntValue(2915));

        int projectileId = idefs.getCSOpcode(2940);

        if (projectileId == 0)
            projectileId = data.getIntValue(2940);

        boolean twoHWep = idefs.equipType == 5;

        int startTime = twoHWep ? 44 : 32;

        int emoteId = data.getIntValue(twoHWep ? 2919 : 2914);
        if (emoteId == 0)
            emoteId = map.getIntValue(currentWeapon == -1 ? 0 : idefs.getCSOpcode(686));

        Animation attackAnim = new Animation(emoteId);

        final int targetGFX = data.getIntValue(2933);

        int reduce = abilityId >= 10 ? 100 : (abilityId >= 7 && abilityId < 10) ? 15 : 0;

        npc.setNextAnimation(attackAnim);

        if (reduce > 0)
            npc.removeAdrenaline(reduce);
        else
            npc.addAdrenaline(8);

        int mapId = RS3ClientScriptMap.getMap(EliteDungeonNPC.CS_DATA_ID[abilityBook - 1]).getIntValue(abilityId);

        npc.setCooldown(mapId, (abilityId == 7 && abilityBook == MAGIC_ABILITY_SHORTCUT) ? 20 : data.getIntValue(2796));// Cooldown
        // for
        // abilities when used
        // so he doesn't use
        // multible times :P

        switch (abilityBook) {
        case RANGED_ABILITY_SHORTCUT:
            if (projectileId == 0) {
                projectileId = RS3ClientScriptMap.getMap(6722).getIntValue(ItemDefinitions.getItemDefinitions(currentWeapon).getCSOpcode(21));
                if (projectileId == 0)
                    projectileId = RS3ClientScriptMap.getMap(6722).getDefaultIntValue();
            }
            switch (abilityId) {
            case 1:// PIERCING SHOT
                World.sendProjectile(npc, target, projectileId, 41, 16, 41, startTime, 16, 0);
                Hit hit = getRangeHit(npc, getRandomMaxHit(npc, defs.getMaxHit(), attackStyle, target));
                delayHit(npc, 2, player, hit);
                break;
            case 4:// SNIPE
                npc.getEffectsManager().startEffect(new Effect(EffectType.ED_SNIPE, 5, projectileId, targetGFX));
                return 5;
            case 5:// FRAGMENTATION SHOT
                World.sendProjectile(npc, target, projectileId, 41, 16, 41, startTime, 16, 0);
                if (getRandomMaxHit(npc, defs.getMaxHit(), attackStyle, target) == 0) {
                    delayHit(npc, 0, player, new Hit(npc, 0, HitLook.MISSED));
                    break;
                }
                final int minumumDamage = getRandomMaxHit(npc, defs.getMaxHit(), attackStyle, target, 1.0, true), maximumDamage = getRandomMaxHit(npc, defs.getMaxHit(), attackStyle, target, 1.0 + ThreadLocalRandom.current().nextDouble(0, 0.88), true), damage = Utils.random(minumumDamage, maximumDamage + 1) / 5;
                WorldTasksManager.schedule(new WorldTask() {
                    @Override
                    public void run() {
                        player.getEffectsManager().startEffect(new Effect(EffectType.FRAGMENTATION, 10, HitLook.RANGE_DAMAGE, 3574, damage, 2, npc, new WorldTile(player)));
                    }
                });
                break;
            case 6:// RICOCHET
                World.sendProjectile(npc, target, projectileId, 41, 16, 41, startTime, 16, 0);
                hit = getRangeHit(npc, getRandomMaxHit(npc, defs.getMaxHit(), attackStyle, target));
                delayHit(npc, 2, player, hit);
                break;
            case 7:// SNAPSHOT
                World.sendProjectile(npc, target, projectileId, 41, 16, 41, startTime, 16, 0);
                hit = getRangeHit(npc, getRandomMaxHit(npc, defs.getMaxHit(), attackStyle, 1.0 + ThreadLocalRandom.current().nextDouble(0.20), target));
                delayHit(npc, 2, player, hit);
                if (targetGFX != 0)
                    player.setNextGraphics(new Graphics(targetGFX, 30, 130));
                World.sendProjectile(npc, target, projectileId, 41, 16, 41, startTime + 5, 16, 0);
                hit = getRangeHit(npc, getRandomMaxHit(npc, defs.getMaxHit(), attackStyle, 1.0 + ThreadLocalRandom.current().nextDouble(1.10), target));
                delayHit(npc, 2, player, hit);
                if (targetGFX != 0)
                    player.setNextGraphics(new Graphics(targetGFX, 30, 130));
                break;
            case 9:// BOMBARDMENT
                World.sendProjectile(npc, target, projectileId, 41, 16, 41, startTime, 16, 0);
                hit = getRangeHit(npc, getRandomMaxHit(npc, defs.getMaxHit(), attackStyle, 2.19, target));
                delayHit(npc, 2, player, hit);
                player.setNextGraphics(new Graphics(targetGFX, 30, 130));
                break;
            case 10:// INCENRARY SHOT
                World.sendProjectile(npc, target, projectileId, 41, 16, 41, startTime, 16, 0);
                hit = getRangeHit(npc, getRandomMaxHit(npc, defs.getMaxHit(), attackStyle, 2.5 + ThreadLocalRandom.current().nextDouble(1), target));
                delayHit(npc, 2, player, hit);
                player.setNextGraphics(new Graphics(targetGFX, 30, 0, 0));
                player.setNextGraphics(new Graphics(targetGFX + 1, 200, 0, 0));
                break;
            case 12:// DEADSHOT
                npc.setNextGraphics(new Graphics(3519));
                World.sendProjectile(npc, target, projectileId, 41, 16, 41, startTime, 16, 0);
                hit = getRangeHit(npc, getRandomMaxHit(npc, defs.getMaxHit(), attackStyle, 1.88, target));
                delayHit(npc, 2, player, hit);
                if (hit.getDamage() > 0) {
                    int minDamage = getRandomMaxHit(npc, defs.getMaxHit(), attackStyle, target, 1.0, true);
                    int maxDamage = getRandomMaxHit(npc, defs.getMaxHit(), attackStyle, target, 3.13, true);
                    final int constantDmg = Utils.random(minDamage, maxDamage) / 5;
                    WorldTasksManager.schedule(new WorldTask() {
                        @Override
                        public void run() {
                            player.getEffectsManager().startEffect(new Effect(EffectType.DEADSHOT, 10, HitLook.RANGE_DAMAGE, 3527, constantDmg, 2, player));
                        }
                    }, 2);
                }
                break;
            }
            break;
        case MAGIC_ABILITY_SHORTCUT:
            switch (abilityId) {
            case 1:// WRACK
                Hit hit = getMagicHit(npc, getRandomMaxHit(npc, defs.getMaxHit(), attackStyle, player.getFreezeDelay() > Utils.currentTimeMillis() ? 1.25 : 0.94, target));
                delayHit(npc, 0, player, hit);
                if (targetGFX != 0)
                    player.setNextGraphics(new Graphics(targetGFX, 0, 130));
                break;
            case 3:// IMPACT
                World.sendProjectile(npc, target, projectileId, 41, 16, 41, startTime, 16, 0);
                hit = getMagicHit(npc, getRandomMaxHit(npc, defs.getMaxHit(), attackStyle, target));
                delayHit(npc, 2, player, hit);
                player.addFreezeDelay(1800);
                int angle = (int) Math.round(Math.toDegrees(Math.atan2((npc.getX() * 2 + npc.getSize()) - (player.getX() * 2 + player.getSize()), (npc.getY() * 2 + npc.getSize()) - (player.getY() * 2 + player.getSize()))) / 45d) & 0x7;
                player.setNextGraphics(new Graphics(targetGFX, 30 / 2, 130, angle));
                player.setNextGraphics(new Graphics(3488, 30, 92));
                break;
            case 4:// CHAIN
                World.sendProjectile(npc, target, projectileId, 41, 16, 41, startTime, 16, 0);
                hit = getMagicHit(npc, getRandomMaxHit(npc, defs.getMaxHit(), attackStyle, target));
                delayHit(npc, 2, player, hit);
                if (targetGFX != 0)
                    player.setNextGraphics(new Graphics(targetGFX, 30, 130));
                break;
            case 5:// COMBUST
                World.sendProjectile(npc, target, projectileId, 41, 16, 41, startTime, 16, 0);
                if (getRandomMaxHit(npc, defs.getMaxHit(), attackStyle, target) == 0) {
                    delayHit(npc, 0, player, new Hit(npc, 0, HitLook.MISSED));
                    break;
                }
                final int minumumDamage = getRandomMaxHit(npc, defs.getMaxHit(), attackStyle, target, 1.0, true), maximumDamage = getRandomMaxHit(npc, defs.getMaxHit(), attackStyle, target, 1.0 + ThreadLocalRandom.current().nextDouble(0, 0.88), true), damage = Utils.random(minumumDamage, maximumDamage + 1) / 5;
                if (targetGFX != 0)
                    player.setNextGraphics(new Graphics(targetGFX, 0, 130));
                WorldTasksManager.schedule(new WorldTask() {

                    @Override
                    public void run() {
                        player.getEffectsManager().startEffect(new Effect(EffectType.COMBUST, 10, HitLook.MAGIC_DAMAGE, targetGFX, damage, 2, npc, new WorldTile(player)));
                    }
                });
                break;
            case 9:// WILD MAGIC
                World.sendProjectile(npc, target, projectileId, 41, 16, 41, startTime, 16, 0);
                hit = getMagicHit(npc, getRandomMaxHit(npc, defs.getMaxHit(), attackStyle, 0.5 + ThreadLocalRandom.current().nextDouble(1.75), target));
                delayHit(npc, 2, player, hit);
                if (targetGFX != 0)
                    player.setNextGraphics(new Graphics(targetGFX, 30, 130));
                World.sendProjectile(npc, target, projectileId, 41, 16, 41, startTime + 5, 16, 0);
                hit = getMagicHit(npc, getRandomMaxHit(npc, defs.getMaxHit(), attackStyle, 0.5 + ThreadLocalRandom.current().nextDouble(1.75), target));
                delayHit(npc, 2, player, hit);
                if (targetGFX != 0)
                    player.setNextGraphics(new Graphics(targetGFX, 35, 130));
                break;
            case 12:// OMNIPOWER
                World.sendProjectile(npc, target, projectileId, 41, 16, 41, startTime, 16, 0);
                hit = getMagicHit(npc, getRandomMaxHit(npc, defs.getMaxHit(), attackStyle, 2 + ThreadLocalRandom.current().nextDouble(2), target));
                delayHit(npc, 2, player, hit);
                if (targetGFX != 0)
                    player.setNextGraphics(new Graphics(targetGFX, 30, 130));
                break;
            }
            break;
        case MELEE_ABILITY_SHORTCUT:
            switch (abilityId) {
            case 1:// SLICE
                Hit hit = getMeleeHit(npc, getRandomMaxHit(npc, defs.getMaxHit(), attackStyle, 1.1, target));
                delayHit(npc, 0, player, hit);
                break;
            case 3:// SEVER
                hit = getMeleeHit(npc, getRandomMaxHit(npc, defs.getMaxHit(), attackStyle, 1.88, target));
                delayHit(npc, 0, player, hit);
                player.getTemporaryModifiersManager().applyModifier(Key.DAMAGE_DEALT_MODIFIER, 5000, -0.10);
                break;
            case 7:// SLAUGHTER
                if (getRandomMaxHit(npc, defs.getMaxHit(), attackStyle, target) == 0) {
                    delayHit(npc, 0, player, new Hit(npc, 0, HitLook.MISSED));
                    break;
                }
                WorldTasksManager.schedule(new WorldTask() {

                    @Override
                    public void run() {
                        if (npc.getFreezeDelay() > Utils.currentTimeMillis())
                            return;
                        final int minumumDamage = getRandomMaxHit(npc, defs.getMaxHit(), attackStyle, target, 1.0, true), maximumDamage = getRandomMaxHit(npc, defs.getMaxHit(), attackStyle, target, 2.5, true), damage = Utils.random(minumumDamage, maximumDamage + 1) / 5;
                        player.getEffectsManager().startEffect(new Effect(EffectType.SLAUGHTER, 10, HitLook.MELEE_DAMAGE, 3464, damage, 2, npc, new WorldTile(player)));
                    }
                });
                return 4;
            case 10:// OVERPOWER
                hit = getMeleeHit(npc, getRandomMaxHit(npc, defs.getMaxHit(), attackStyle, 2 + ThreadLocalRandom.current().nextDouble(2), target));
                delayHit(npc, 0, player, hit);
                break;
            }
            break;
        case STRENGTH_ABILITY_SHORTCUT:
            switch (abilityId) {
            case 1:// DISMEMBER
                WorldTasksManager.schedule(new WorldTask() {
                    @Override
                    public void run() {

                        final int minumumDamage = getRandomMaxHit(npc, defs.getMaxHit(), attackStyle, target, 1.0, true), maximumDamage = getRandomMaxHit(npc, defs.getMaxHit(), attackStyle, target, 1.8, true), damage = Utils.random(minumumDamage, maximumDamage + 1) / 5;

                        player.getEffectsManager().startEffect(new Effect(EffectType.DISMEMBER, 10, HitLook.MELEE_DAMAGE, 3465, damage, 2, npc));
                    }
                });
                break;
            case 3:// PUNISH
                Hit hit = getMeleeHit(npc, getRandomMaxHit(npc, defs.getMaxHit(), attackStyle, player.getFreezeDelay() > Utils.currentTimeMillis() ? 1.25 : 0.94, target));
                delayHit(npc, 0, player, hit);
                break;
            case 7:// ASSAULT
                npc.getEffectsManager().startEffect(new Effect(EffectType.ED_ASSAULT, 10, 0.0));
                return 8;
            case 10:// berserk
                npc.getEffectsManager().startEffect(new Effect(EffectType.BERSERK, 33));
                return 1;
            }
            break;
        }
        return npc.getAttackSpeed();
    }

    @Override
    public Object[] getKeys() {
        return new Object[] { "Elite Sotapanna", "Elite Sakadagami", "Eastern mercenary", 25607, "Death Lotus rogue", 25619, 25621, 25622, 25631, 25623, 25624, 25626, 25620, 25628, 25629 };
    }

    public ArrayList<Integer> getAvailableAbilities(int abilityBook) {
        int spec = npc.getCombat().getSpecialAttackPercentage();
        int index = (spec == 100) ? 2 : (spec >= 50 && spec < 100 && Utils.random(2) == 0) ? 1 : 0;// ulti,thresh,basic
        int[] abilities = (abilityBook == MAGIC_ABILITY_SHORTCUT) ? MAGIC_ABILITIES[index] : (abilityBook == RANGED_ABILITY_SHORTCUT) ? RANGE_ABILITIES[index] : (abilityBook == MELEE_ABILITY_SHORTCUT) ? MELEE_ABILITIES[index] : STRENGTH_ABILITIES[index];
        if (index == 2)
            if (!canUseAbilitiesIn(index, abilityBook))
                index--;
        if (index == 1)
            if (!canUseAbilitiesIn(index, abilityBook))
                index--;
        if (index == 0)
            if (!canUseAbilitiesIn(index, abilityBook))
                return null;
        ArrayList<Integer> availableAbilities = new ArrayList<Integer>();
        for (int i = 0; i < abilities.length; i++) {
            if (npc.hasCooldown(abilities[i], abilityBook))
                continue;
            availableAbilities.add(abilities[i]);
        }
        return availableAbilities;
    }

    private boolean canUseAbilitiesIn(int index, int abilityBook) {
        int[] abilities = (abilityBook == MAGIC_ABILITY_SHORTCUT) ? MAGIC_ABILITIES[index] : (abilityBook == RANGED_ABILITY_SHORTCUT) ? RANGE_ABILITIES[index] : (abilityBook == MELEE_ABILITY_SHORTCUT) ? MELEE_ABILITIES[index] : STRENGTH_ABILITIES[index];
        for (int i = 0; i < abilities.length; i++) {
            if (!npc.hasCooldown(abilities[i], abilityBook))
                return true;
        }
        return false;
    }

}
