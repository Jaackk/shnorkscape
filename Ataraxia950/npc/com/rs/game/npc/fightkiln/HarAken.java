package com.rs.game.npc.fightkiln;

import com.rs.game.Entity;
import com.rs.game.Graphics;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import com.rs.game.player.controllers.FightKiln;
import com.rs.utils.Utils;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial")
public class HarAken extends NPC {

	private long time;
	private long spawnTentacleTime;
	private boolean underLava;
	private final List<HarAkenTentacle> tentacles;

	private final FightKiln controler;

	public HarAken(int id, WorldTile tile, FightKiln controler) {
		super(id, tile, -1, true, true);
		setForceMultiArea(true);
		this.controler = controler;
		tentacles = new ArrayList<HarAkenTentacle>();
	}

	public void process() {
		if (isDead())
			return;
		if (time != 0) {
			if (time < Utils.currentTimeMillis()) {
				if (underLava) {
					controler.showHarAken();
					resetTimer();
				} else
					controler.hideHarAken();
			}
			if (spawnTentacleTime < Utils.currentTimeMillis())
				spawnTentacle();

		}
	}

	@Override
	public void processNPC() {
		if (isDead())
			return;
		cancelFaceEntityNoCheck();
	}

	public void removeTentacle(HarAkenTentacle tentacle) {
		tentacles.remove(tentacle);

	}

	public void removeTentacles() {
		for (HarAkenTentacle t : tentacles)
			t.finish();
		tentacles.clear();
	}

	public void resetTimer() {
		underLava = !underLava;
		if (time == 0)
			spawnTentacleTime = Utils.currentTimeMillis() + 9000;
		time = Utils.currentTimeMillis() + (underLava ? 45000 : 30000);
	}

	@Override
	public void sendDeath(Entity source) {
		setNextGraphics(new Graphics(2924 + getSize()));
		if (time != 0) {
			removeTentacles();
			controler.removeNPC();
			time = 0;
		}
		super.sendDeath(source);
		if (source instanceof Player) {
			((Player) source).getActivityTimersManager().finishBossTimer(HarAken.this);
		}
	}

	public void spawnTentacle() {
		HarAkenTentacle tentacle = new HarAkenTentacle(Utils.random(2) == 0 ? 15209 : 15210, controler.getTentacleTile(), this);
		tentacles.add(tentacle);
		tentacle.setTarget(getTarget());
		spawnTentacleTime = Utils.currentTimeMillis() + Utils.random(15000, 25000);
	}

}
