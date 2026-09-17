package com.rs.game.npc.eds;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.rs.game.Entity;
import com.rs.game.ForceTalk;
import com.rs.game.WorldTile;
import com.rs.game.player.PerkManager.DonationPerk;
import com.rs.game.player.Player;
import com.rs.game.player.content.eds.EliteDungeonsConstants;
import com.rs.game.player.content.eds.rooms.EliteDungeonHandledRoom;
import com.rs.game.player.content.items.Ectoplasmator;
import com.rs.utils.Utils;
import com.rs.utils.data.parsers.npcs.pojos.NPCDrop;

import lombok.Getter;
import lombok.Setter;

public class EliteDungeonMiniBoss extends EliteDungeonNPC {

    private static final long serialVersionUID = -2181682916189130318L;

    private transient int talkIndex;
    private transient long talkCooldown;
    @Getter
    @Setter
    private transient String[] forceTalkDialogue;
    @Getter
    private transient int[] faceTile;

    public EliteDungeonMiniBoss(int id, WorldTile tile, EliteDungeonHandledRoom room) {
        super(id, tile, room);
    }

    @Override
    public void processNPC() {
        if (isDead() || hasFinished())
            return;
        super.processNPC();
        processForceTalk();
    }

    @Override
    public void spawn() {
        super.spawn();
        talkIndex = 0;
    }

    @Override
    public void reset() {
        talkIndex = 0;
        super.reset();
    }

    public void processForceTalk() {
        if (getCombat().getTarget() == null || forceTalkDialogue == null)
            return;
        if (talkCooldown != 0 && talkCooldown >= Utils.currentTimeMillis())
            return;
        if (talkIndex >= forceTalkDialogue.length)
            return;
        setNextForceTalk(new ForceTalk(forceTalkDialogue[talkIndex]));
        talkIndex++;
        talkCooldown = Utils.currentTimeMillis() + 3000;
    }

    @Override
    public void setNextForceTalk(ForceTalk nextForceTalk) {
        super.setNextForceTalk(nextForceTalk);
        if (nextForceTalk == null || getRoom() == null || getRoom().getRoom() == null)
            return;
        for (Player player : getRoom().getRoom().getPlayers()) {
            if (player == null || player.hasFinished() || player.isDead() || !player.withinDistance(this, 14))
                continue;
            player.getPackets().sendGameMessage("<col=FBF689>" + getName() + " </col><col=99FF99>" + nextForceTalk.getText() + "</col>");
        }
    }

    @Override
    public double getMagePrayerMultiplier() {
        return 0.40;
    }

    @Override
    public double getMeleePrayerMultiplier() {
        return 0.40;
    }

    @Override
    public double getRangePrayerMultiplier() {
        return 0.40;
    }

    public List<Player> getDropPlayers() {
        List<Player> players = new ArrayList<Player>();
        if (getReceivedDamage() == null)
            return players;
        for (Map.Entry<Entity, Integer> entry : getReceivedDamage().entrySet()) {
            Entity source = entry.getKey();
            if (!(source instanceof Player) || !getPossibleTargets().contains(source)) {
                continue;
            }
            final Integer d = entry.getValue();
            if (d == null || source.hasFinished())
                continue;
            players.add((Player) source);
        }
        return players;
    }

    @Override
    public void processRemoveAgro() {

    }

    @Override
    public void drop() {
        drop(null);
    }

    public void drop(Player killer) {
        String name = getName().toLowerCase();
        if (name == null)
            return;
        List<Player> players = getDropPlayers();
        if (killer != null && !players.contains(killer))
            players.add(killer);
        for (Player player : players) {
            if (player == null || player.hasFinished() || player.isDead())
                continue;
            int extra = player.getPerkManager().hasPerkActive(DonationPerk.DUNGEONS_MASTER) ? 1250 : 0;
            player.getDungeoneeringManager().setTokens(player.getDungeoneeringManager().getTokens() + 5000 + extra);
            player.getPackets().sendGameMessage(!player.getPerkManager().hasPerkActive(DonationPerk.DUNGEONS_MASTER) ? "You received 5,000 dungeoneering tokens." : "You received 6,250 dungeoneering tokens, An extra 1250 tokens for having the dungeons master perk.");
            player.getInventionManager().processScavengingPerk();
            increaseKillStatistics(player, getName());
            handleRingOfDeath(player);
            final NPCDrop[] possibleDrops = EliteDungeonsConstants.mobDrops;
            for (final NPCDrop drop : possibleDrops) {
                if (drop == null || drop.getRate() != 100)
                    continue;
                if (!Ectoplasmator.scatterAshes(player, this, drop.getItemId()))
                    sendDrop(player, drop, false);
            }
            int amountCharms = player.getInventory().getAmountOf(43066);
            NPCDrop luckyCharm = Math.random() <= (amountCharms == 0 ? 0.005 : 0.04) ? NPCDrop.selectRandomNPCDrop(EliteDungeonsConstants.luckyCharmDrops) : null;
            NPCDrop mobDrop = luckyCharm != null ? luckyCharm : NPCDrop.selectRandomNPCDrop(possibleDrops);
            if (luckyCharm != null && amountCharms > 0)
                player.getInventory().deleteItem(43066, 1);
            sendDrop(player, mobDrop, false);
            if (Math.random() <= 0.005)
                sendDrop(player, new NPCDrop(18778, 5, 1, 1), false);
        }
    }

    public void setFaceTile(int x, int y, int plane) {
        if (faceTile == null)
            faceTile = new int[3];
        faceTile[0] = x;
        faceTile[1] = y;
        faceTile[2] = plane;
    }
    
    @Override
    public boolean isFreezeImmune() {
        return true;
    }

    @Override
    public boolean isStunImmune() {
        return true;
    }

}
