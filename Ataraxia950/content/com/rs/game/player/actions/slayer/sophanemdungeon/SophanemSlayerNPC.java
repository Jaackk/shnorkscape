package com.rs.game.player.actions.slayer.sophanemdungeon;

import com.rs.game.Entity;
import com.rs.game.Hit;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.utils.Colors;
import com.rs.utils.Utils;
import com.rs.utils.data.parsers.npcs.pojos.NPCDrop;
import lombok.Getter;
import lombok.val;
import org.apache.commons.lang3.ArrayUtils;

import java.util.HashMap;
import java.util.Map;

public class SophanemSlayerNPC extends NPC {
    private static final long serialVersionUID = -6427635570852866168L;
    public static final Item FEATHER_OF_MAAT = new Item(40303);

    public SophanemSlayerNPC(int id, WorldTile tile, int mapAreaNameHash, boolean canBeAttackFromOutOfArea) {
        super(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea);
    }

    @Override
    public void drop() {
        super.drop();
    }

    @Override
    protected void sendDrop(final Player player, NPCDrop drop, boolean lootbeam) {
        if (!ArrayUtils.contains(DroppedItem.VALUES, drop.getItemId())) {
            final Item item = new Item(drop.getItemId(), drop.getMinAmount() + Utils.getRandom(drop.getExtraAmount()));
            if (player.isAutomaticSophanemLootCollection()) {
                player.addSophanemChestLoot(item.getId(), item.getAmount());
                return;
            }
        }
        super.sendDrop(player, drop, lootbeam);
    }

    @Override
    public boolean canBeAttacked(Player attacker) {
        for (SlayerLevelRequirement slayerLevelRequirement : SlayerLevelRequirement.VALUES) {
            if (slayerLevelRequirement.npcId == id) {
                val requirement = slayerLevelRequirement.requirement;
                if (slayerLevelRequirement.levelRequirementType == SlayerLevelRequirement.LevelRequirementType.LEVEL) {
                    val hasRequirements = attacker.getSkills().getLevel(Skills.SLAYER) >= requirement;
                    if (!hasRequirements) {
                        attacker.sendMessage("You require a Slayer level of " + requirement + " to attack this monster!");
                    }
                    return hasRequirements;
                } else if (slayerLevelRequirement.levelRequirementType == SlayerLevelRequirement.LevelRequirementType.EXPERIENCE) {
                    val hasRequirements = attacker.getSkills().getXp(Skills.SLAYER) >= requirement;
                    if (!hasRequirements) {
                        attacker.sendMessage("You require " + Utils.formatNumber(requirement) + " experience in Slayer to attack this monster!");
                    }
                    return hasRequirements;
                }
            }
        }
        return false;
    }

    @Override
    public void sendDeath(Entity source) {
        if (source instanceof Player) {
            Player player = (Player) source;
            if (!player.getInventory().containsItem(FEATHER_OF_MAAT)) {
                player.sendMessage("You need a Feather of Ma'at to finish this creature off.");
                setHitpoints((int) (getMaxHitpoints() * 0.1));
            } else {
                player.getInventory().deleteItem(FEATHER_OF_MAAT);
                super.sendDeath(source);
                player.sophanemKc++;
                player.sendFilteredMessage(Colors.RED + "Sophanem creatures killed:</col> " + player.sophanemKc);
            }
        }
    }

    @Override
    public void handleIngoingHit(final Hit hit) {
        Player player = (Player) hit.getSource();
        if (player.getControlerManager().getControler() instanceof SophanemSlayerDungeon) {
            SophanemSlayerDungeon controller = (SophanemSlayerDungeon) player.getControlerManager().getControler();
            int random = Utils.random(6);
            if (random == 0 && player.getSophanemCorruption() < 25) {
                player.setSophanemCorruption(player.getSophanemCorruption() + 5);
                controller.updateSophanemCorruptionComponent();
            }
            hit.setDamage((int) (hit.getDamage() % (0.01 % player.getSophanemCorruption() + hit.getDamage())));
            super.handleIngoingHit(hit);
        }
    }

    private enum DroppedItem {
        KEY_TO_THE_CROSSING(40310), CORRUPTED_GEM(40334), KHOPESH_OF_THE_KHARIDIAN(40312), COINS(995);

        public static final DroppedItem[] VALUES = values();

        private final int itemId;

        DroppedItem(int itemId) {
            this.itemId = itemId;
        }
    }

    public enum SlayerLevelRequirement {
        CORRUPTED_SCORPION(SlayerMonsterType.CORRUPTED_CREATURE, 24592, LevelRequirementType.LEVEL, 88), CORRUPTED_SCARAB(SlayerMonsterType.CORRUPTED_CREATURE, 24593, LevelRequirementType.LEVEL, 91), CORRUPTED_LIZARD(SlayerMonsterType.CORRUPTED_CREATURE, 24594, LevelRequirementType.LEVEL, 94), CORRUPTED_DUST_DEVIL(SlayerMonsterType.CORRUPTED_CREATURE, 24595, LevelRequirementType.LEVEL, 97), CORRUPTED_KALPHITE_MARAUDER(SlayerMonsterType.CORRUPTED_CREATURE, 24597, LevelRequirementType.EXPERIENCE, 14_391_160), CORRUPTED_KALPHITE_GUARDIAN(SlayerMonsterType.CORRUPTED_CREATURE, 24596, LevelRequirementType.EXPERIENCE, 14_391_160), CORRUPTED_WORKER(SlayerMonsterType.CORRUPTED_CREATURE, 24598, LevelRequirementType.EXPERIENCE, 19_368_992), SALAWA_AKH(SlayerMonsterType.SOUL_DEVOURER, 24599, LevelRequirementType.EXPERIENCE, 23_611_006), FELINE_AKH(SlayerMonsterType.SOUL_DEVOURER, 24600, LevelRequirementType.EXPERIENCE, 28_782_069), SCARAB_AKH(SlayerMonsterType.SOUL_DEVOURER, 24601, LevelRequirementType.EXPERIENCE, 35_085_654), CROCODILE_AKH(SlayerMonsterType.SOUL_DEVOURER, 24602, LevelRequirementType.EXPERIENCE, 42_769_801), GORILLA_AKH(SlayerMonsterType.SOUL_DEVOURER, 24603, LevelRequirementType.EXPERIENCE, 52_136_869), IMPERIAL_WARRIOR_AKH(SlayerMonsterType.SOUL_DEVOURER, 24604, LevelRequirementType.EXPERIENCE, 77_474_828), IMPERIAL_MAGE_AKH(SlayerMonsterType.SOUL_DEVOURER, 24605, LevelRequirementType.EXPERIENCE, 77_474_828), IMPERIAL_RANGER_AKH(SlayerMonsterType.SOUL_DEVOURER, 24606, LevelRequirementType.EXPERIENCE, 77_474_828),;

        public static final SlayerLevelRequirement[] VALUES = values();
        private static final Map<Integer, SlayerLevelRequirement> all = new HashMap<>();

        static {
            for (SlayerLevelRequirement req : VALUES) {
                all.put(req.npcId, req);
            }
        }

        public static SlayerLevelRequirement forId(int npcId) {
            return all.get(npcId);
        }

        @Getter
        private final SlayerMonsterType slayerMonsterType;
        @Getter
        private final int npcId;
        private final LevelRequirementType levelRequirementType;
        private final int requirement;

        SlayerLevelRequirement(SlayerMonsterType slayerMonsterType, int npcId, LevelRequirementType levelRequirementType, int requirement) {
            this.slayerMonsterType = slayerMonsterType;
            this.npcId = npcId;
            this.levelRequirementType = levelRequirementType;
            this.requirement = requirement;
        }

        public LevelRequirementType getLevelRequirementType() {
            return levelRequirementType;
        }

        public int getRequirement() {
            return requirement;
        }

        public enum LevelRequirementType {
            LEVEL, EXPERIENCE
        }

        public enum SlayerMonsterType {
            SOUL_DEVOURER, CORRUPTED_CREATURE
        }
    }
}
