package com.rs.game.activities.aod.npc;

import com.rs.game.Entity;
import com.rs.game.ForceTalk;
import com.rs.game.WorldTile;
import com.rs.game.activities.aod.AngelOfDeath;
import com.rs.game.activities.aod.ability.praesul.Asphyxiate;
import com.rs.game.activities.aod.ability.praesul.Chain;
import com.rs.game.activities.aod.ability.praesul.Freedom;
import com.rs.game.activities.aod.ability.praesul.Metamorphosis;
import com.rs.game.activities.aod.ability.praesul.Omnipower;
import com.rs.game.activities.aod.ability.praesul.PraesulAbility;
import com.rs.game.activities.aod.ability.praesul.WildMagic;
import com.rs.game.activities.aod.ability.praesul.Wrack;
import com.rs.game.hitbar.impl.AdrenalineHitBar;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A NPC class handling the Praesul minions spawned by Nex.
 * @author Kris | 30. sept 2017 : 17:20.08
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>}
 */
public final class PraesulMinion extends NPC {

	private static final long serialVersionUID = 8710411960921690859L;

	private static final ForceTalk[] FORCE_TALK = new ForceTalk[] {
			new ForceTalk("So be it. I'll do what I can."),
			new ForceTalk("Too soon, but more than enough to crush you."),
			new ForceTalk("I do not feel complete master, my contact with the elements have been cut short.")
	};
	
	public PraesulMinion(final int id, final WorldTile tile, final AngelOfDeath instance, final boolean finished) {
		super(id, tile, -1, true, true);
		this.instance = instance;
		if (!finished) {
			this.setHitpoints(getMaxHitpoints() / 2);
		} else {
			this.setNextForceTalk(FORCE_TALK[Utils.random(FORCE_TALK.length)]);
		}
		setForceMultiArea(true);
		setForceTargetDistance(100);
		setNextRenderAnimation(2687);
		setNoDistanceCheck(true);
		setRun(true);
	}

	@Override
	public boolean isIntelligentRouteFinder() {
		return true;
	}
	
	private final AngelOfDeath instance;
	private PraesulAbility ability;
	private int adrenaline;
	private double hitBoost = 1;
	private int abilityStage = -1;
	private final Map<Class<?>, Long> cooldowns = new HashMap<Class<?>, Long>();
	
	public boolean canUseAbility(final Class<?> c, final int cooldown) {
		long time = 0;
		if (cooldowns.containsKey(c))
			time = cooldowns.get(c);
		if (time > Utils.currentTimeMillis())
			return false;
		cooldowns.put(c, Utils.currentTimeMillis() + (cooldown * 1000));
		return true;
	}
	
	public PraesulAbility generateNextAbility(final Player player) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		if (abilityStage != -1)
			abilityStage++;
		if (adrenaline >= 100) {
			abilityStage = -1;
			if (Utils.random(2) == 0)
				return new Metamorphosis(player, this, instance);
			else
				return new Omnipower(player, this, instance);
		}
		if (this.isFrozen())
			return new Freedom(player, this, instance);
		if (abilityStage == 15 || abilityStage == 65)
			abilityStage = -1;
		if (abilityStage == -1) {
			if (Utils.random(5) == 0)
				abilityStage = 50;
			else
				abilityStage = 0;
		}
		final List<Class<? extends PraesulAbility>> abilities = new ArrayList<Class<? extends PraesulAbility>>();
		if (abilityStage < 50 && adrenaline >= 15) {
			abilities.add(WildMagic.class);
			abilities.add(Asphyxiate.class);
		}
		abilities.add(Chain.class);
//		abilities.add(Combust.class);
		abilities.add(Wrack.class);
		final Class<? extends PraesulAbility> chosen = abilities.get(Utils.random(abilities.size()));
		@SuppressWarnings("rawtypes")
		final Class[] args = new Class[3];
		args[0] = Player.class;
		args[1] = PraesulMinion.class;
		args[2] = AngelOfDeath.class;
		return chosen.getDeclaredConstructor(args).newInstance(player, this, instance);
	}
	
	@Override
	public void processNPC() {
		super.processNPC();
		getNextHitBars().add(new AdrenalineHitBar(adrenaline));
		if (isUnderCombat())
			addHitBars();
	}
	
	/**
	 * Sets damage boost of the Praesul, after using Metamorphism ability.
	 * @param boost damage boost double.
	 */
	public void setHitBoost(double boost) {
		this.hitBoost = boost;
	}
	
	/**
	 * Gets the damage boost of the Praesul. By default, it's 1.
	 * @return damage boost.
	 */
	public double getHitBoost() {
		return hitBoost;
	}
	
	/**
	 * Sets the Praesul ability percentage.
	 * @param amount to set to.
	 */
	public void setAdrenaline(int amount) {
		if (amount > 100)
			amount = 100;
		adrenaline = amount;
	}
	
	/**
	 * Gets the Praesul adrenaline percentage.
	 * @return adrenaline percentage.
	 */
	public int getAdrenaline() {
		return adrenaline;
	}
	
	/**
	 * Uses a Praesul ability
	 * @param ability to use.
	 */
	public void useAbility(final PraesulAbility ability) {
		this.ability = ability;
		WorldTasksManager.schedule(ability, 0, 0);
	}
	
	/**
	 * Gets the instance of the fight.
	 * @return instance.
	 */
	public AngelOfDeath getInstance() {
		return instance;
	}
	
	@Override
	public void sendDeath(final Entity source) {
		instance.setOrder(getId());
		super.sendDeath(source);
	}
	
	@Override
	public boolean canWalkNPC(int toX, int toY) {
		return true;
	}
	
	@Override
	public double getMeleePrayerMultiplier() {
		return 0.5;
	}
	
	@Override
	public double getMagePrayerMultiplier() {
		return 0.5;
	}
	
	@Override
	public double getRangePrayerMultiplier() {
		return 0.5;
	}
	
	@Override
	public int getCapDamage() {
		return 10000;
	}
	
	/**
	 * Gets the current running Ability of the Praesul.
	 * @return current ability.
	 */
	public PraesulAbility getAbility() {
		return ability;
	}

}
