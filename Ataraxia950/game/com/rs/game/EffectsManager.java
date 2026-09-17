package com.rs.game;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;

import com.rs.game.Hit.HitLook;
import com.rs.game.TemporaryAtributtes.Key;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.combat.NPCCombatDefinition;
import com.rs.game.npc.eds.EliteDungeonNPC;
import com.rs.game.npc.telos.Font;
import com.rs.game.npc.telos.Telos;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.Action;
import com.rs.game.player.combat.PlayerCombat;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

public class EffectsManager implements Serializable {

    private static final long serialVersionUID = -5884310017906704149L;

    private transient Entity entity;
    private transient boolean isPlayer;

    private final List<Effect> effects = new CopyOnWriteArrayList<>();

    public EffectsManager() {
    }

    public void setEntity(Entity entity) {
        this.entity = entity;
        this.isPlayer = entity instanceof Player;
    }

    public Entity getEntity() {
        return entity;
    }

    public void startEffect(Effect effect) {
        EffectType type = effect.type;
        if (!type.canStartEffect(effect, entity))
            return;
        Effect currentEffect = getEffectForType(type);
        if (currentEffect != null)
            effects.set(effects.indexOf(currentEffect), effect);
        else
            effects.add(effect);
        refreshBuffEffect(effect);
    }

    public void processEffects() {
        if (entity.isDead() || entity.hasFinished() || effects.isEmpty())
            return;
        for (Effect effect : effects) {
            int action = effect.type.getAction();
            if (effect.cycle != -1) {
                effect.cycle--;
                if (effect.cycle == 0) {
                    removeEffect(effect.type);
                    continue;
                }
            }
            // already refreshes at both remove and add
            if (isPlayer)
                processBuffTimer(effect, false);
            if (action == BUFF) {
                if (isPlayer) {
                    @SuppressWarnings("unused")
                    Player player = (Player) entity;

                }
            } else if (action == DEBUFF) {
                if (effect.type == EffectType.ANIMA_CONSUMPTION) {
                    if (isPlayer) {
                        Player player = (Player) entity;
                        if (player.isDead()) {
                            removeEffect(effect.type);
                        }
                        for (int npcIndex : World.getRegion(player.getRegionId()).getNPCsIndexes()) {
                            NPC font = World.getNPCs().get(npcIndex);
                            if (font == null || font.isDead() || font.hasFinished() || !(font instanceof Font))
                                continue;
                            if (Utils.isOnRange(player, font, 1)) {
                                removeEffect(effect.type);
                                break;
                            }
                        }
                        if (effect.cycle % 2 == 0) {
                            int[] skills = { Skills.ATTACK, Skills.DEFENCE, Skills.STRENGTH, Skills.RANGE, Skills.MAGIC };
                            for (int skill : skills) {
                                int lvl = player.getSkills().getLevel(skill);
                                lvl -= 2;
                                player.getSkills().set(skill, lvl < 0 ? 0 : lvl);
                            }
                            int damage = (int) effect.args[0];
                            player.applyHit(new Hit(player, damage, HitLook.REGULAR_DAMAGE));
                        }
                    }
                } else if (effect.type == EffectType.GREEN_STREAM) {
                    if (isPlayer) {
                        Player player = (Player) entity;
                        if (hasActiveEffect(EffectType.GREEN_VIRUS)) {
                            player.getTemporaryAttributtes().put(Key.FORCE_REMOVE_EFFECT_1, Boolean.TRUE);
                            removeEffect(EffectType.GREEN_VIRUS);
                        }
                        player.getPrayer().drainPrayer(player.getTelosEnrage() >= 1000 ? 20 : 10);
                        player.getCombatDefinitions().restoreSpecialAttack(10);
                    } else {
                        Telos telos = (Telos) entity;
                        telos.increaseAnima(3);
                    }
                } else if (effect.type == EffectType.BLACK_STREAM) {
                    if (isPlayer) {
                        Player player = (Player) entity;
                        if (hasActiveEffect(EffectType.BLACK_VIRUS)) {
                            player.getTemporaryAttributtes().put(Key.FORCE_REMOVE_EFFECT_1, Boolean.TRUE);
                            removeEffect(EffectType.BLACK_VIRUS);
                        }
                    } else {
                        Telos telos = (Telos) entity;
                        telos.increaseAnima(3);
                    }
                } else if (effect.type == EffectType.RED_STREAM) {
                    if (isPlayer) {
                        Player player = (Player) entity;
                        if (hasActiveEffect(EffectType.RED_VIRUS)) {
                            player.getTemporaryAttributtes().put(Key.FORCE_REMOVE_EFFECT_1, Boolean.TRUE);
                            removeEffect(EffectType.RED_VIRUS);
                        }
                    } else {
                        Telos telos = (Telos) entity;
                        telos.increaseAnima(3);
                    }
                }
            } else if (action == COMBO_BUFFS) {
                Player player = null;
                EliteDungeonNPC ed = null;
                if (entity instanceof Player)
                    player = (Player) entity;
                else if (entity instanceof EliteDungeonNPC)
                    ed = (EliteDungeonNPC) entity;

                Action a = null;
                if (ed == null) {
                    a = player.getActionManager().getAction();
                    if (!(a instanceof PlayerCombat)) {
                        effect.cycle = 0;
                        return;
                    }
                }
                Entity target = null;
                if (player != null) {
                    target = ((PlayerCombat) a).getTarget();
                } else {
                    if (ed != null)
                        target = ed.getCombat().getTarget();
                }
                if (effect.type == EffectType.ED_SNIPE) {
                    if (effect.cycle == 1) {
                        World.sendProjectile(ed, target, (int) effect.args[0], 41, 16, 41, 10, 16, 0);
                        NPCCombatDefinition defs = ed.getCombatDefinitions();
                        int attackStyle = defs.getAttackStyle();
                        Hit hit = CombatScript.getRangeHit(ed, CombatScript.getRandomMaxHit(ed, defs.getMaxHit(), attackStyle, 1.25 + ThreadLocalRandom.current().nextDouble(0, 0.94), target));
                        CombatScript.delayHit(ed, 2, target, hit);
                        target.setNextGraphics(new Graphics((int) effect.args[1], 30, 0));
                        ((Player) target).setPrayerDelay(1000);
                    }
                } else if (effect.type == EffectType.ED_ASSAULT) {
                    if (effect.cycle == 7 || effect.cycle == 5 || effect.cycle == 3 || effect.cycle == 1) {
                        NPCCombatDefinition defs = ed.getCombatDefinitions();
                        int attackStyle = defs.getAttackStyle();
                        Hit hit = CombatScript.getRangeHit(ed, CombatScript.getRandomMaxHit(ed, defs.getMaxHit(), attackStyle, 2.19, target));
                        CombatScript.delayHit(ed, 0, target, hit);
                    }
                }
            } else if (action == HIT_MARK) {
                HitLook look = (HitLook) effect.args[0];
                int graphicsId = (int) effect.args[1];//
                int damage = (int) effect.args[2], effectDelay = (int) effect.args[3];

                if (effect.cycle % effectDelay == 0) {
                    if (effect.type == EffectType.FRAGMENTATION || effect.type == EffectType.COMBUST || effect.type == EffectType.SLAUGHTER) {
                        WorldTile tile = (WorldTile) effect.args[5];
                        if (tile.getX() != entity.getX() || tile.getY() != entity.getY() || tile.getPlane() != entity.getPlane())
                            damage *=  2;
                    } else if (effect.type == EffectType.GREEN_VIRUS || effect.type == EffectType.RED_VIRUS || effect.type == EffectType.BLACK_VIRUS) {
                        damage += (25 - (effect.cycle / 2)) * (Utils.random(5, 7));
                        if (damage >= 400)
                            damage = 400;
                    }
                    if (look == HitLook.HEALED_DAMAGE)
                        entity.heal(damage, 0, 0, true);
                    else {
                        Hit hit = new Hit(entity, damage, look);
                        if (effect.args.length >= 5) {
                            hit.setSource((Entity) effect.args[4]);
                        }
                        entity.applyHit(hit);
                        if (hit.getSource() instanceof Player && effect.args.length >= 5)
                            PlayerCombat.autoRelatie((Player) effect.args[4], entity);
                    }
                    if (graphicsId != 0)
                        entity.setNextGraphics(new Graphics(graphicsId));
                }
            } else if (action == SHIELD_BUFF) {
                if (isPlayer) {
                    Player player = (Player) entity;
                    if (!player.getEquipment().hasShield())
                        removeEffect(effect.type);
                }
            }
        }
    }

    public void refreshAllBuffs() {
        for (Effect effect : effects)
            refreshBuffEffect(effect);
    }

    public void refreshBuffEffect(Effect effect) {
        if (!isPlayer)
            return;
        @SuppressWarnings("unused")
        Player player = (Player) entity;
        @SuppressWarnings("unused")
        EffectType type = effect.type;
    }

    public void processBuffTimer(Effect effect, boolean refresh) {
        if (!isPlayer)
            return;
        @SuppressWarnings("unused")
        Player player = (Player) entity;
        @SuppressWarnings("unused")
        EffectType type = effect.type;
    }

    public void resetEffects() {
        Effect[] e = effects.toArray(new Effect[effects.size()]);
        effects.clear();
        for (Effect effect : e) {
            effect.setCycle(0);
            refreshBuffEffect(effect);
        }
    }

    public boolean removeEffect(EffectType type) {
        Effect effect = getEffectForType(type);
        if (effect == null)
            return false;
        if (effect.getCycle() > 0)
            effect.setCycle(0);
        type.onRemoval(entity);
        boolean removedEffect = effects.remove(effect);
        refreshBuffEffect(effect);
        return removedEffect;
    }

    public void removeEffectsWithAction(int action) {
        for (Effect effect : effects) {
            EffectType type = effect.getType();
            if (type.getAction() == action)
                removeEffect(type);
        }
    }

    public boolean hasActiveEffect(EffectType type) {
        Effect effect = getEffectForType(type);
        if (effect == null)
            return false;
        return effects.contains(effect);
    }

    public boolean hasActiveEffect(int action) {
        return getEffectForAction(action) != null;
    }

    public Effect getEffectForType(EffectType type) {
        for (Effect effect : effects) {
            if (effect.type == type)
                return effect;
        }
        return null;
    }

    public Effect getEffectForAction(int action) {
        for (Effect effect : effects) {
            EffectType type = effect.getType();
            if (type.getAction() == action)
                return effect;
        }
        return null;
    }

    public static byte BUFF = 0, DEBUFF = 1, HIT_MARK = 2, COMBO_BUFFS = 3, SHIELD_BUFF = 4;

    public enum EffectType {
        GREEN_VIRUS(HIT_MARK, -1, -1, -1) {

            @Override
            public void onRemoval(Entity e) {
                boolean skipHit = e.getTemporaryAttributtes().remove(Key.FORCE_REMOVE_EFFECT_1) != null;
                Effect currentEffect = e.getEffectsManager().getEffectForType(this);
                if (currentEffect != null) {
                    if (!skipHit)
                        e.applyHit(new Hit(e, (((int) currentEffect.args[2]) * 10), HitLook.REGULAR_DAMAGE));
                    else
                        ((Player) e).getPackets().sendEntityMessage(1, 15263739, e, "The anima stream cleanses you.", true);
                }
            }
        },

        BLACK_VIRUS(HIT_MARK, -1, -1, -1) {
            @Override
            public void onRemoval(Entity e) {
                boolean skipHit = e.getTemporaryAttributtes().remove(Key.FORCE_REMOVE_EFFECT_1) != null;
                Effect currentEffect = e.getEffectsManager().getEffectForType(this);
                if (currentEffect != null) {
                    if (!skipHit)
                        e.applyHit(new Hit(e, (((int) currentEffect.args[2]) * 10), HitLook.REGULAR_DAMAGE));
                    else
                        ((Player) e).getPackets().sendEntityMessage(1, 15263739, e, "The anima stream cleanses you.", true);
                }
            }
        },
        RED_VIRUS(HIT_MARK, -1, -1, -1) {
            @Override
            public void onRemoval(Entity e) {
                boolean skipHit = e.getTemporaryAttributtes().remove(Key.FORCE_REMOVE_EFFECT_1) != null;
                Effect currentEffect = e.getEffectsManager().getEffectForType(this);// cuz effects are serilizable like rs3
                if (currentEffect != null) {
                    if (!skipHit)
                        e.applyHit(new Hit(e, (((int) currentEffect.args[2]) * 10), HitLook.REGULAR_DAMAGE));
                    else
                        ((Player) e).getPackets().sendEntityMessage(1, 15263739, e, "The anima stream cleanses you.", true);
                }
            }
        },
        GREEN_STREAM(DEBUFF, -1, -1, -1),

        BLACK_STREAM(DEBUFF, -1, -1, -1),

        RED_STREAM(DEBUFF, -1, -1, -1),

        ANIMA_CONSUMPTION(DEBUFF, -1, -1, -1) {

            @Override
            public boolean canStartEffect(Effect effect, Entity e) {
                Player player = (Player) e;
                player.setNextColour(new Colour(0, 50000, 70, 110, 50, 130));
                player.getPackets().sendEntityMessage(1, 15263739, player, "Your body absorbs the anima. Stand in a font to cleanse yourself!", true);
                return true;
            }

            @Override
            public void onRemoval(Entity e) {
                Player player = (Player) e;
                player.setNextColour(new Colour(0, 5, 0, 0, 0, 0));
                player.getPackets().sendEntityMessage(1, 15263739, player, "The anima is cleansed from your body!", true);
            }

        },
        ED_SNIPE(COMBO_BUFFS, -1, -1, -1),
        
        ED_ASSAULT(COMBO_BUFFS, -1, -1, -1),
        
        FRAGMENTATION(HIT_MARK, -1, 2089, -1),
        
        DISMEMBER(HIT_MARK, -1, 2073, -1),
        
        SLAUGHTER(HIT_MARK, -1, 2059, -1),
        
        COMBUST(HIT_MARK, -1, 2079, -1),
        
        DEADSHOT(HIT_MARK, -1, 2092, -1),
        
        BERSERK(BUFF, -1, 2076, -1) {

            @Override
            public boolean canStartEffect(Effect effect, Entity e) {
                e.setNextAnimation(new Animation(18597));
                e.setNextGraphics(new Graphics(3475));
                e.setNextGraphics(new Graphics(3476));
                e.getTemporaryModifiersManager().applyModifier(com.rs.game.player.TemporaryAttributes.Key.DAMAGE_DEALT_MODIFIER, 20000, 1.00);
                e.getTemporaryModifiersManager().applyModifier(com.rs.game.player.TemporaryAttributes.Key.DAMAGE_RECIEVED_MODIFIER, 20000, 0.5);
                // TODO if someone atk you first you lose :)
                return true;
            }
        },
        FREEDOM(BUFF, -1, -1, -1) {
            @Override
            public boolean canStartEffect(Effect effect, Entity e) {
                e.setNextAnimation(new Animation(18070));
                e.setFreezeImmune(true);
                e.unFreeze();
                return true;
            }
            
            @Override
            public void onRemoval(Entity e) {
                e.setFreezeImmune(false);
            }
            
        };

        public boolean canStartEffect(Effect effect, Entity e) {
            return true;
        }

        public void onRemoval(Entity e) {

        }

        private final byte action;
        @SuppressWarnings("unused")
        private final int var;
        @SuppressWarnings("unused")
        private final int varbit;
        @SuppressWarnings("unused")
        private final int grMap;

        EffectType(byte action, int var, int varbit, int grMap) {
            this.action = action;
            this.var = var;
            this.varbit = varbit;
            this.grMap = grMap;
        }

        EffectType(byte action) {
            this(action, -1, -1, -1);
        }

        public byte getAction() {
            return action;
        }

        public int getVar() {
            return var;
        }

        public int getGrMap() {
            return grMap;
        }
    }

    public static class Effect implements Serializable {

        private static final long serialVersionUID = 9217587656136559938L;

        private final EffectType type;
        private int cycle;
        private final Object[] args;

        public Effect(EffectType type, int count, Object... args) {
            this.type = type;
            this.cycle = count;
            this.args = args;
        }

        public EffectType getType() {
            return type;
        }

        public int getCycle() {
            return cycle;
        }

        public void setCycle(int cycle) {
            this.cycle = cycle;
        }

        public Object[] getArguments() {
            return args;
        }
    }

    public boolean isEmpty() {
        return effects.isEmpty();
    }
}
