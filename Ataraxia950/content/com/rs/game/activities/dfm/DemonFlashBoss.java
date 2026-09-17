package com.rs.game.activities.dfm;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.activites.gim.GIM;
import com.rs.game.item.Item;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.NPCCombatDefinition;
import com.rs.game.player.Player;
import com.rs.game.player.content.hcim_news.HcimNewsManager;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;
import com.rs.utils.data.parsers.npcs.pojos.NPCDrop;

import java.util.ArrayList;

public class DemonFlashBoss extends NPC {

    private static final long serialVersionUID = -2173254072748472960L;

    public DemonFlashBoss(int id, WorldTile tile, int mapAreaNameHash, boolean canBeAttackFromOutOfArea, boolean spawned) {
        super(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
    }

    private int processTicks;
    private int darklightDamage = 0;
    private final ArrayList<Player> eligiblePlayers = new ArrayList<Player>();

    public void addDarklightDamage(int amount) {
        this.darklightDamage += amount;
    }

    public int getDarklightDamage() {
        return darklightDamage;
    }

    public int getCapDamage() {
        return 2000;
    }

    @Override
    public void handleIngoingHit(final Hit hit) {
        super.handleIngoingHit(hit);
        if (hit.getDamage() >= 50 && hit.getSource() instanceof Player && (this.getHitpoints() > 4000 || DemonFlashMobs.getDemonFlashMobs().getRewardPlayers().isEmpty()))
            DemonFlashMobs.getDemonFlashMobs().addDamage((Player) hit.getSource(), hit.getDamage());
    }

    @Override
    public double getMagePrayerMultiplier() {
        return 1;
    }

    @Override
    public double getMeleePrayerMultiplier() {
        return 1;
    }

    @Override
    public void sendDeath(final Entity source) {
        final NPCCombatDefinition defs = getCombatDefinitions();
        resetWalkSteps();

        DemonFlashMobs.getDemonFlashMobs().getRewardPlayers().forEach((k, v) -> {
            if (k.withinDistance(this, 15))
                eligiblePlayers.add(k);
        });
        this.getCombat().removeTarget();
        if (source instanceof Player)
            source.deathResetCombat();
        setNextAnimation(null);
        WorldTasksManager.schedule(new WorldTask() {
            int loop;

            @Override
            public void run() {
                if (loop == 0)
                    setNextAnimation(new Animation(defs.getDeathEmote()));
                else if (loop >= defs.getDeathDelay()) {
                    sendDrops();
                    reset();
                    finish();
                    stop();
                }
                loop++;
            }
        }, 0, 1);
    }

    private void sendDrops() {
        ArrayList<NPCDrop> possibleDrops = new ArrayList<NPCDrop>();
        ArrayList<Item> drops = new ArrayList<Item>();
        int totalDropRate = 0;
        for (DemonFlashDrops allDrops : DemonFlashDrops.values()) {
            if (allDrops.equals(DemonFlashDrops.INFERNAL_ASHES))
                continue;
            if (allDrops.isDroppedInWildernessOnly()) {
                if (DemonFlashMobs.getDemonFlashMobs().isInWilderness()) {
                    possibleDrops.add(new NPCDrop(allDrops.getItemId(), allDrops.getRarity(), allDrops.getWildernessAmount()));
                    totalDropRate += allDrops.getRarity();
                }
            } else {
                possibleDrops.add(new NPCDrop(allDrops.getItemId(), allDrops.getRarity(), allDrops.getAmount()[Utils.random(allDrops.getAmount().length)]));
                totalDropRate += allDrops.getRarity();
            }
        }
        for (int i = 0; i < 4; i++) {
            int currentRate = 0;
            int randomRate = Utils.random(totalDropRate);
            a:
            for (int x = 0; x < possibleDrops.size(); x++) {
                if (currentRate + possibleDrops.get(x).getRate() >= randomRate) {
                    drops.add(new Item(possibleDrops.get(x).getItemId(), possibleDrops.get(x).getMinAmount()));
                    possibleDrops.remove(x);
                    break a;
                }
                currentRate += possibleDrops.get(x).getRate();
            }
        }
        Player one = null, two = null;
        int o;
        if (eligiblePlayers.size() > 0) {
            o = Utils.random(eligiblePlayers.size());
            one = eligiblePlayers.get(o);
            if (eligiblePlayers.size() > 1) {
                int x = 0;
                for (int i = 0; i < 10; i++) {
                    x = Utils.random(eligiblePlayers.size());
                    if (x != 0)
                        break;
                }
                two = eligiblePlayers.get(x);
            }
        }
        int chance = 10;
        if (DemonFlashMobs.getDemonFlashMobs().isInWilderness())
            chance = chance * 2;
        if (Utils.random(100) < chance)
            drops.add(new Item(DemonFlashMobs.DEMON_SLAYER_PIECES[Utils.random(DemonFlashMobs.DEMON_SLAYER_PIECES.length)], 1));
        if (Utils.random(DemonFlashMobs.getDemonFlashMobs().isInWilderness() ? 12 : 15) == 0) {
            int[] possibleScrolls = new int[2];
            String px = DemonFlashMobs.getDemonFlashMobs().getPrefix();
            possibleScrolls[0] = px.equals("General") ? 33936 : px.equals("Executioner") ? 33938 : px.equals("Castellan") ? 33940 : 33942;
            possibleScrolls[1] = px.equals("Blazing") ? 33944 : px.equals("Corrupting") ? 33946 : px.equals("Frostborn") ? 33948 : px.equals("Glorious") ? 33950 : px.equals("Infernal") ? 33952 : px.equals("Obscured") ? 33954 : px.equals("Pestilent") ? 33956 : px.equals("Rending") ? 33958 : px.equals("Shattering") ? 33960 : 33962;
            int random = Utils.random(2);
            drops.add(new Item(random == 0 ? possibleScrolls[0] : possibleScrolls[1], 1));
        }
        if (one == null)
            return;
        for (int i = 0; i < drops.size(); i++) {
            Player player = i == 0 && two != null ? two : one;
            World.addGroundItem(drops.get(i), this, player, true, 180);
            if (drops.get(i).getName().contains("Demonic title")) {
                World.sendWorldMessage("<img=6><col=ff9900>News: " + player.getDisplayName() + " has received a " + drops.get(i).getName() + " as a drop!", false);
                HcimNewsManager.getInstance().addNews(player, "<#player> got a " + drops.get(i).getName() + " drop!", 2);
            }
        }
        eligiblePlayers.forEach(player -> {
            World.addGroundItem(new Item(DemonFlashDrops.INFERNAL_ASHES.getItemId(), DemonFlashMobs.getDemonFlashMobs().isInWilderness() ? 5 : 1), this, player, true, 180);
            player.demonFlashMobsKills++;
        });
    }

    @Override
    public void processEntity() {
        processTicks++;
        if (processTicks == 100 && this.getId() == 16731) {
            boolean lesser = false;
            int damage = 0;
            if (DemonFlashMobs.getDemonFlashMobs() != null) {
                if (DemonFlashMobs.getDemonFlashMobs().getLesserDemons() != null)
                    for (NPC lessers : DemonFlashMobs.getDemonFlashMobs().getLesserDemons()) {
                        if (!lessers.isDead() && !lessers.hasFinished()) {
                            damage += lessers.getHitpoints() > 300 ? 300 : lessers.getHitpoints();
                            lessers.applyHit(new Hit(null, 300, HitLook.REGULAR_DAMAGE));
                            lesser = true;
                        }
                    }
                if (!lesser) {
                    if (DemonFlashMobs.getDemonFlashMobs().getBlackDemons() != null)
                        for (NPC blacks : DemonFlashMobs.getDemonFlashMobs().getBlackDemons()) {
                            if (!blacks.isDead() && !blacks.hasFinished()) {
                                damage += blacks.getHitpoints() > 600 ? 600 : blacks.getHitpoints();
                                blacks.applyHit(new Hit(null, 600, HitLook.REGULAR_DAMAGE));
                            }
                        }
                }
            }
            if (damage != 0)
                this.applyHit(new Hit(null, damage, HitLook.HEALED_DAMAGE));
            processTicks = 0;
        }
        super.processEntity();
        processNPC();
    }

    public ArrayList<Entity> getPossibleTargets() {
        return getPossibleTargets(false, true);
    }

}
