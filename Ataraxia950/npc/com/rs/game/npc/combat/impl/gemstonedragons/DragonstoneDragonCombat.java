package com.rs.game.npc.combat.impl.gemstonedragons;

import com.rs.cores.CoresManager;
import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.Graphics;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.NewProjectile;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.combat.NPCCombatDefinitionConstants;
import com.rs.game.npc.others.GemstoneDragon;
import com.rs.game.player.Player;
import com.rs.game.player.content.Combat;
import com.rs.utils.Utils;

import java.util.concurrent.TimeUnit;

public class DragonstoneDragonCombat extends CombatScript {

	private static final int DOUBLE_TRAIL_PROJ = 6571,
			MAGIC_PROJ = 2730;
	private static final int MELEE_MAX = 156, 
			MAGIC_MAX = 156, 
			DRAGONFIRE_MAX = 600, 
			SPECIAL_MAX = 450;
	private static final int MAGIC_TYPE = 0, 
			MELEE_TYPE = 1, 
			DRAGONFIRE_TYPE = 2,
			SPECIAL_TYPE = 3;
	private static final int ANTIFIRE_PROTECTION_DRAIN = 20000;
	private static final Animation MELEE_ANIMATION = new Animation(12252), 
			MAGIC_PROJ_ANIMATION = new Animation(14244), 
			MAGIC_DF_ANIMATION = new Animation(14245),
			SPECIAL_ANIMATION = new Animation(30321);
	private static final Graphics DRAGONFIRE_GFX = new Graphics(5549),
			IMPACT_GFX = new Graphics(6576);
	
	@Override
	public int attack(NPC npc, Entity target) {
		final GemstoneDragon dragon = (GemstoneDragon) npc;
		final boolean withinDistance = withinDistance(npc, target);
		final int type = Utils.random(10) == 0 ? DRAGONFIRE_TYPE : Utils.random(15) == 0 && !withinDistance && dragon.canUseSpecial() ? SPECIAL_TYPE : withinDistance ? Utils.random(2) : MAGIC_TYPE;
		switch(type) {
		case MELEE_TYPE:
			npc.setNextAnimation(MELEE_ANIMATION);
			delayHit(npc, 0, target, getMeleeHit(npc, getRandomMaxHit(npc, MELEE_MAX, NPCCombatDefinitionConstants.MELEE, target)));
			return 5;
		case MAGIC_TYPE:
			npc.setNextAnimation(MAGIC_PROJ_ANIMATION);
			final NewProjectile proj = new NewProjectile(new WorldTile(npc.getCoordFaceX(npc.getSize()), npc.getCoordFaceY(npc.getSize()), npc.getPlane()), target, MAGIC_PROJ, 10, 35, 45, 5, 60, 0);
			World.sendProjectile(proj);
			delayHit(npc, proj.getTime() / 335, target, getMagicHit(npc, getRandomMaxHit(npc, MAGIC_MAX, NPCCombatDefinitionConstants.MAGE, target)));
			return 5;
		case DRAGONFIRE_TYPE:
			npc.setNextAnimation(MAGIC_DF_ANIMATION);
			npc.setNextGraphics(DRAGONFIRE_GFX);
			delayHit(npc, 1, target, new Hit(npc, getDragonfire(target), HitLook.REGULAR_DAMAGE));
			return 5;
		case SPECIAL_TYPE:
			npc.setNextAnimation(SPECIAL_ANIMATION);
			dragon.addSpecialUsageDelay(15000);
			final WorldTile impactTile = new WorldTile(target);
			final NewProjectile projectile = new NewProjectile(new WorldTile(npc.getCoordFaceX(npc.getSize()), npc.getCoordFaceY(npc.getSize()), npc.getPlane()), impactTile, DOUBLE_TRAIL_PROJ, 0, 0, 30, 5, 15, 0);
			World.sendProjectile(projectile);
			CoresManager.getServiceProvider().executeWithDelay(new Runnable() {
				@Override
				public void run() {
					if (npc.isDead() || npc.hasFinished())
						return;
					World.sendGraphics(npc, IMPACT_GFX, impactTile);
					if (target.withinDistance(impactTile, 1)) {
						target.applyHit(getMagicHit(npc, getRandomMaxHit(npc, SPECIAL_MAX, NPCCombatDefinitionConstants.MAGE, target)));
						if (target instanceof Player) {
							Player p = (Player) target;
							if (p.getSuperAntiFire() > Utils.currentTimeMillis())
								p.addSuperAntiFire((Utils.currentTimeMillis() - p.getSuperAntiFire()) - ANTIFIRE_PROTECTION_DRAIN);
							if (p.getFireImmune() > Utils.currentTimeMillis())
								p.addFireImmune((Utils.currentTimeMillis() - p.getFireImmune()) - ANTIFIRE_PROTECTION_DRAIN);
							p.getPackets().sendPlayerMessage(1, 15263739, "The attack reduces your immunity to dragonfire!", true);
						}
					}
				}
			}, (int) (projectile.getTime() * 1.7), TimeUnit.MILLISECONDS);
			return 5;
		}
		return 5;
	}
	
	private final boolean withinDistance(final NPC npc, final Entity target) {
		int distanceX = target.getX() - npc.getX();
		int distanceY = target.getY() - npc.getY();
		int size = npc.getSize();
		return distanceX <= size && distanceX >= -1 && distanceY <= size && distanceY >= -1;
	}
	
	private final int getDragonfire(final Entity target) {
		int damage = Utils.random(DRAGONFIRE_MAX);
		if (target instanceof Player) {
			Player player = (Player) target;
			String message = Combat.getProtectMessage(player);
			if (message != null) {
				player.sendMessage(message, true);
				if (message.contains("fully"))
					damage *= 0;
				else if (message.contains("most"))
					damage *= 0.5;
				else if (message.contains("some"))
					damage *= 0.2;
			} else
				player.sendMessage("You are hit by the dragon's fiery breath!", true);
		}
		return damage;
	}

	@Override
	public Object[] getKeys() {
		return new Object[] { "Dragonstone dragon" };
	}

}
