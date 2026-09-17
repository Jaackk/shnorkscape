package com.rs.game.activities.aod;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.Graphics;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.NewProjectile;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.activities.aod.npc.AoDNex;
import com.rs.game.item.Item;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import com.rs.game.player.controllers.Controller;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class AoDController extends Controller {
	
	private AngelOfDeath aod;
	private static final Animation LAND = new Animation(30122);
	private static final Graphics EXPLOSION = new Graphics(6529);
	
	@Override
	public void start() {
		aod = (AngelOfDeath) player.getTemporaryAttributtes().get("aodinstance");
	}
	
	/**
	 * Starts the fight, sets the stage to one to stop any further players from entering the fight.
	 * Sends a projectile from all four corners of the instance to the middle, causing
	 * an explosion after which Nex flies down from the sky. The fight initiates after Nex finishes talking.
	 * @param player entity who started the fight.
	 */
	public static final void startFight(final Player player) {
		final AngelOfDeath aod = (AngelOfDeath) player.getTemporaryAttributtes().get("aodinstance");
		if (aod == null) 
			return;
		if (aod.getStage() != 0)
			return;
		World.removeObject(new WorldObject(100804, 10, 0, aod.getWorldTile(2848, 1795)), true);
		aod.setStage(1);
		aod.getAllPlayers().forEach(p -> {
		    p.getInterfaceManager().sendOverlay(1073, true);
			p.getPackets().sendIComponentText(1073, 2, "Lifepoints remaining:");
			p.getPackets().sendIComponentText(1073, 3, Utils.formatNumber(AoDNex.calculateMaxHitpoints(aod)));
		});
		WorldTasksManager.schedule(new WorldTask() {
			private int stage;
			@Override
			public void run() {
				switch(stage++) {
				case 0:
					aod.sendProjectile(new NewProjectile(aod.SOUTH_WEST, aod.CENTER, 6523, 100, 3));
					aod.sendProjectile(new NewProjectile(aod.SOUTH_EAST, aod.CENTER, 6525, 100, 3));
					aod.sendProjectile(new NewProjectile(aod.NORTH_WEST, aod.CENTER, 3371, 100, 3));
					aod.sendProjectile(new NewProjectile(aod.NORTH_EAST, aod.CENTER, 6524, 100, 3));
					break;
				case 5:
					aod.sendGraphics(EXPLOSION, aod.CENTER);
					break;
				case 6:
					aod.getNex().setNextNPCTransformation(24004);
					aod.getNex().setNextAnimation(LAND);
					break;
				case 8:
					aod.getNex().sendMessage("Witness the true power of the elements. The pure power I have wrought into my will.");
					aod.getNex().setCantInteract(false);
					stop();
					return;
				}
			}
		}, 0, 0);
		return;
	}
	
	@Override
	public boolean sendDeath() {
		player.lock();
		aod.removePlayer(player);
		if (player.isGroupIronman()) {
			player.gimTracker.incrementDeaths("<#player> died from Nex: Angel of Death");
		}
		player.getInterfaceManager().closeOverlay(true);
		player.getReceivedDamage().clear();
		player.getReceivedHits().clear();
		player.stopAll();
		WorldTasksManager.schedule(new WorldTask() {
			int loop;

			@Override
			public void run() {
				if (loop == 0) {
					player.setNextAnimation(new Animation(836));
					player.sendMessage("Oh dear, you have died.");
					player.deathItemsManager.handleDeath();
				}
				if (loop == 3) {
					player.setNextAnimation(new Animation(-1));
					player.getPackets().sendMusicEffect(90);
					player.reset();
					player.unlock();
					player.setNextAnimation(new Animation(-1));
					stop();
				}
				loop++;
			}
		}, 0, 1);
		return false;
	}
	
	@Override
	public boolean processPlayerOption1(Entity target) {
		if (target instanceof NPC) {
			final NPC n = (NPC) target;
			if (n.getId() >= 24016 && n.getId() <= 24019) {
				if (aod.getAvailableCrystal() != n.getId()) {
					player.sendMessage("The crystal is currently invulnerable to damage.");
					return false;
				}
			} else if (aod.getStage() == 4 && n.getId() == 24004 && n instanceof AoDNex) {
				final boolean vulnerable = ((AoDNex) n).isVulnerable();
				if (!vulnerable)
					player.sendMessage("Nex is currently being shielded by the crystals contained within the pillars.", true);
				return vulnerable;
			}
		}
		return true;
	}
	
	@Override
	public boolean canHit(Entity entity) {
		if (entity instanceof NPC) {
			final NPC n = (NPC) entity;
			if (n.getId() >= 24016 && n.getId() <= 24019) {
				if (aod.getAvailableCrystal() != n.getId()) {
					player.sendMessage("The crystal is currently invulnerable to damage.");
					return false;
				}
			} else if (aod.getStage() == 4 && n.getId() == 24004 && n instanceof AoDNex) {
				final boolean vulnerable = ((AoDNex) n).isVulnerable();
				if (!vulnerable)
					player.sendMessage("Nex is currently being shielded by the crystals contained within the pillars.", true);
				return vulnerable;
			}
		}
		return true;
	}
	
	@Override
	public boolean processObjectClick1(final WorldObject object) {
		if (object.getId() == 100804) {
			player.getDialogueManager().startDialogue("AoDFightEnterD");
			return false;
		} else if (object.getId() == 100803 || object.getId() == 100823) {
			aod.removePlayer(player);
			removeControler();
			player.getControlerManager().startControler("GodWars");
			player.setNextWorldTile(new WorldTile(2904, 5200, 0));
            return false;
        }
        return true;
    }
    
    @Override
    public boolean processObjectClick2(final WorldObject object) {
        if (object.getId() == 100823) {
            player.getDialogueManager().startDialogue("SimpleItemMessage", 39595, 2, "You find a few odd looking books inside the shard.");
            if (player.getInventory().hasFreeSlots())
                player.getInventory().addItem(new Item(39594, 1));
            else
                World.addGroundItem(new Item(39594, 1), new WorldTile(player), player, true, 180);
            if (player.getInventory().hasFreeSlots())
                player.getInventory().addItem(new Item(39595, 1));
            else
                World.addGroundItem(new Item(39595, 1), new WorldTile(player), player, true, 180);
            return false;
		}
		return true;
	}
    
	
	@Override
	public boolean logout() {
		player.setLocation(new WorldTile(2904, 5200, 0));
		if (aod != null)
			aod.removePlayer(player);
		player.getControlerManager().forceStop();
		player.getControlerManager().startControler("GodWars");
		return true;
	}
	
    @Override
    public boolean processMagicTeleport(WorldTile toTile) {
        player.sendMessage("The only way out is victory or death!");
        return false;
    }

	@Override
	public boolean processCommand(String s, boolean b, boolean c) {
		final String cmd = s.toLowerCase();
		if (player.isOwner())
		return true;
		return !cmd.equals("bank") && !cmd.equals("b") && !cmd.equals("b1") && !cmd.equals("b2") && !cmd.equals("b3") && !cmd.equals("b4") && !cmd.equals("b5") && !cmd.equals("b6") && !cmd.equals("b7") && !cmd.equals("b8") && !cmd.equals("b9") && !cmd.equals("b10");
	}

	@Override
	public boolean processNPCClick1(NPC npc) {
		if (npc.getId() == 24015) {
			npc.setCantInteract(true);
			aod.sendGraphics(AngelOfDeath.RESET_GFX, new WorldTile(npc));
			npc.finish();
			final List<Player> players = new ArrayList<Player>();
			aod.getPlayers().forEach(p -> {
				if (p.withinDistance(npc, 2))
					players.add(p);
			});
			final int damage = 150 / players.size();
			players.forEach(p -> p.applyHit(new Hit(null, damage, HitLook.REGULAR_DAMAGE)));
			aod.getAllPlayers().forEach(p -> p.sendMessage(player.getDisplayName() + " has unleashed the power of the unstable smoke."));
		}
		return true;
	}

    @Override
    public boolean processNPCClick2(NPC npc) {
        if (npc.getId() == 24003) {
            aod.removePlayer(player);
            removeControler();
            player.getControlerManager().startControler("GodWars");
            player.setNextWorldTile(new WorldTile(2904, 5200, 0));
            return false;
        }
        return true;
    }

	
}
