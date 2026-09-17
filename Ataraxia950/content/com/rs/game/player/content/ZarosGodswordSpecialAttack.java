package com.rs.game.player.content;

import com.rs.game.Graphics;
import com.rs.game.Hit;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Colors;
import com.rs.utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Xenthium.
 */


public class ZarosGodswordSpecialAttack {

    public static final String SPECIAL_ATTACK_KEY = "ZAROS_GODSWORD_SPECIAL_ATTACK";
    private static final int specialAttackDrainAmount = 60; // The percentage of special attack energy to drain.
    private final List<NPC> applicableEntities = new ArrayList<>();
    private final int blackHoleRadius = 2; // The default radius of the black hole's AoE damage, not including the center tile.
    private final Player player;
    private final WorldTile blackHoleTile;

    public ZarosGodswordSpecialAttack(Player player, WorldTile activationTile) {
        this.player = player;
        this.blackHoleTile = activationTile;
        initBlackHole();
    }

    /**
     * Checks to see if the player meets the conditions required for summoning a black hole.
     * Must not have a currently active black hole.
     * Their special attack energy must be >= specialAttackDrainAmount.
     */
    public static boolean canSummonBlackHole(Player player) {
        int currentSpecialAmount = player.getCombatDefinitions().getSpecialAttackPercentage();
        if (player.zarosGodswordSpecialAttack != null) {
            player.sendMessage(Colors.RED + "You must wait until your current black hole collapses before summoning another one.");
            return false;
        }
        if (currentSpecialAmount < specialAttackDrainAmount) {
            player.sendMessage(Colors.RED + "You need at least " + specialAttackDrainAmount + "% special attack energy to summon a black hole.");
            return false;
        }
        return true;
    }

    /**
     * Initiates the black hole special attack.
     */
    private void initBlackHole() {
        final int openBlackHoleGfxId = 6285, closeBlackHoleGfxId = 6286;
        WorldTasksManager.schedule(new WorldTask() {
            int loop;
            final int finalLoop = 17;

            @Override
            public void run() {
                if (loop < finalLoop) {
                    findApplicableEntities();
                }
                if (loop == 0) {
                    player.getTemporaryAttributtes().put(SPECIAL_ATTACK_KEY, true);
                    player.getCombatDefinitions().decreaseSpecialAttack(specialAttackDrainAmount);
                    World.sendGraphics(null, new Graphics(openBlackHoleGfxId), blackHoleTile);
                    initBackHoleDamageCycle();
                }
                if (loop >= finalLoop) {
                    player.getTemporaryAttributtes().remove(SPECIAL_ATTACK_KEY);
                    World.sendGraphics(null, new Graphics(-1), blackHoleTile); // Don't remove otherwise black hole never despawns.
                    World.sendGraphics(null, new Graphics(closeBlackHoleGfxId), blackHoleTile);
                    player.zarosGodswordSpecialAttack = null;
                    stop();
                }
                loop++;
            }
        }, 0, 1);
    }

    /**
     * Handles the world task responsible for the black hole's AoE damage.
     */
    private void initBackHoleDamageCycle() {
        WorldTasksManager.schedule(new WorldTask() {
            int totalDamage = 0;

            @Override
            public void run() {
                if (player.getTemporaryAttributtes().containsKey(SPECIAL_ATTACK_KEY) && player.zarosGodswordSpecialAttack != null) {
                    for (NPC npc : applicableEntities) {
                        if (npc != null) {
                            if (npc.getTarget() == null) {
                                npc.setTarget(player);
                            }
                            if (npc.getCombat().checkAll() && npc.withinDistance(blackHoleTile, finalRadius(npc))) {
                                int aoeDamage = Utils.random(50, npc.getCombatLevel() >= 300 ? 300 : 150);
                                totalDamage = totalDamage + aoeDamage;
                                npc.applyHit(new Hit(player, aoeDamage, Hit.HitLook.MELEE_DAMAGE));
                            }
                        }
                    }
                } else {
                    if (totalDamage > 0) {
                        player.sendMessage(Colors.DCYAN + "Total black hole AoE damage: " + Utils.formatNumber(totalDamage) + ".", true); // Thought this might be fun to leave in so players can compete.
                    }
                    stop();
                }
            }
        }, 1, 2);
    }

    /**
     * Adds nearby NPCs to an array used for the black hole's AoE damage.
     */
    private void findApplicableEntities() {
        for (NPC npc : World.getNPCs()) {
            if (npc != null) {
                if (npc.withinDistance(blackHoleTile, finalRadius(npc)) && npc.getDefinitions().hasAttackOption() && npc.canBeAttacked(player) && !applicableEntities.contains(npc)) {
                    applicableEntities.add(npc);
                }
            }
        }
    }

    /**
     * Increases the blackHoleRadius for the specific NPC if it's size is >= 3
     * to account for larger NPCs which appear to be standing next to the black hole,
     * yet unaffected due to their true tile.
     */
    private int finalRadius(NPC npc) {
        int size = npc.getSize();
        return blackHoleRadius + (size >= 5 ? 3 : size >= 3 ? 2 : 0);
    }

}
