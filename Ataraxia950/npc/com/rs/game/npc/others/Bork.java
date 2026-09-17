package com.rs.game.npc.others;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.ForceTalk;
import com.rs.game.Graphics;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import com.rs.game.player.controllers.BorkController;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

import java.util.ArrayList;

/**
 * Handles and Initializes Bork.
 *
 * @author Noel
 */
public class Bork extends NPC {

    /**
     * The Generated serial UID.
     */
    private static final long serialVersionUID = 7598477828536008806L;

    /**
     * Ork Legion messages.
     */
    private static final String[] MINION_MESSAGES = {"Hup! 2.... 3.... 4!", "Resistance is futile!",
            "We are the collective!", "Form a triangle!"};
    private final BorkController controller;
    private boolean spawnedMinions;
    private NPC[] borkMinion;

    public Bork(WorldTile tile, BorkController controller) {
        super(7134, tile, -1, true, true);
        setCantInteract(true);
        setDirection(Utils.getAngle(1, 0));
        setNoDistanceCheck(true);
        setForceAgressive(true);
        this.controller = controller;
    }

    public static boolean atBork(WorldTile tile) {
        return (tile.getX() >= 3083 && tile.getX() <= 3120) && (tile.getY() >= 5522 && tile.getY() <= 5550);
    }

    public boolean isSpawnedMinions() {
        return spawnedMinions;
    }

    @Override
    public void drop() {
        int size = getSize();
        ArrayList<Item> drops = new ArrayList<Item>();
        drops.add(new Item(532, 1)); // big bones
        drops.add(new Item(995, 400000 + Utils.random(200000))); // coins
        drops.add(new Item(12163, 15)); // blue charm
        drops.add(new Item(12160, 18)); // crimson charm
        drops.add(new Item(12159, 20)); // green charm
        drops.add(new Item(12158, 25)); // gold charm
        drops.add(new Item(1618, 10)); // uncut diamond
        drops.add(new Item(1620, 15)); // uncut ruby
        drops.add(new Item(1622, 20)); // uncut emerald
        drops.add(new Item(1624, 25)); // uncut sapphire
        for (Item item : drops) {
            if (item.getDefinitions().isStackable())
                item.setAmount(item.getAmount() * 2);
            World.addGroundItem(item, new WorldTile(getCoordFaceX(size), getCoordFaceY(size), getPlane()));
        }
    }

    public void setMinions() {
        borkMinion = new NPC[3];
        for (int i = 0; i < borkMinion.length; i++) {
            borkMinion[i] = World.spawnNPC(7135, new WorldTile(this, 1), -1, true, true);
            borkMinion[i].setNextForceTalk(new ForceTalk("For bork!"));
            borkMinion[i].setNextGraphics(new Graphics(1314));
            borkMinion[i].setTarget(controller.getPlayer());
            borkMinion[i].setForceMultiArea(true);
        }
        setNextForceTalk(new ForceTalk("Destroy the intruder, my Legions!"));
        spawnedMinions = true;
        setCantInteract(false);
        setTarget(controller.getPlayer());
    }

    @Override
    public void processNPC() {
        if (borkMinion != null && Utils.random(20) == 0) {
            for (NPC n : borkMinion) {
                if (n == null || n.isDead())
                    continue;
                n.setNextForceTalk(new ForceTalk(MINION_MESSAGES[Utils.random(MINION_MESSAGES.length)]));
            }
        }
        super.processNPC();
    }

    @Override
    public void sendDeath(Entity source) {
        if (!spawnedMinions) {
            setHitpoints(1);
            return;
        }
        controller.killBork();
        for (NPC n : borkMinion) {
            if (n == null || n.isDead())
                continue;
            n.sendDeath(source);
        }
        if (source instanceof Player) {
            Player plr = (Player) source;
            if (plr.isGroupIronman()) {
                plr.gimTracker.incrementBpGained(1);
            }
        }
        super.sendDeath(source);
    }

    public void spawnMinions() {
        setCantInteract(true);
        setNextForceTalk(new ForceTalk("Come to my aid, brothers!"));
        setNextAnimation(new Animation(8757));
        setNextGraphics(new Graphics(1315));
        WorldTasksManager.schedule(new WorldTask() {

            @Override
            public void run() {
                controller.spawnMinions();
            }
        }, 2);
    }
}