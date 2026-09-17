package com.rs.game.player.controllers.bossInstance;

import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.map.bossInstance.BossInstance;
import com.rs.game.map.bossInstance.BossInstanceHandler.Boss;
import com.rs.game.map.bossInstance.impl.KalphiteKingInstance;
import com.rs.game.npc.kalphiteking.KalphiteKing;
import com.rs.utils.Utils;

public class KalphiteKingInstanceController extends BossInstanceController {

	@Override
	public void start() {
		super.start();
		sendInterfaces();
	}

	@Override
	public void sendInterfaces() {
	    
        player.getInterfaceManager().sendOverlay(945, true);
        for (int i = 23; i < Utils.getInterfaceDefinitionsComponentsSize(945); i++)
            player.getPackets().sendHideIComponent(945, i, true);
        for (int i = 0; i < 6; i++)
            player.getPackets().sendHideIComponent(945, i, i != 4);
        player.getPackets().sendHideIComponent(945, 6, true);
		
		player.getPackets().sendGlobalString(2381, "Kalphite King Health");
	}
	
	@Override
	public boolean processCommand(String s, boolean b, boolean c) {
		final String cmd = s.toLowerCase();
		if (player.isOwner())
			return true;
		return !cmd.equals("b") && !cmd.equals("bank") && !cmd.equals("b1") && !cmd.equals("b2") && !cmd.equals("b3") && !cmd.equals("b4") && !cmd.equals("b5") && !cmd.equals("b6") && !cmd.equals("b7") && !cmd.equals("b8") && !cmd.equals("b9") && !cmd.equals("b10");
	}

	@Override
	public void process() {
		KalphiteKing kk = getKKInstance().getKalphiteKing();
		boolean hideStatus = kk == null || kk.isDead();
		player.getPackets().sendHideIComponent(945, 6, hideStatus);
		if (!hideStatus) {
			player.getPackets().sendGlobalConfig(1233, (int) (((double) kk.getHitpoints() / 26000) * 200));
		}
		super.process();
	}

	@Override
	public boolean processObjectClick5(WorldObject object) {
		if (object.getId() == 82016) {
			getKKInstance().leaveInstance(player, BossInstance.EXITED);
			removeControler();
			return false;
		}
		return true;
	}

	@Override
	public void magicTeleported(int type) {
		getKKInstance().leaveInstance(player, BossInstance.TELEPORTED);
		removeControler();
	}

	@Override
	public boolean processMagicTeleport(WorldTile toTile) {
		player.getPackets().sendGameMessage("You can't leave just like that!");
		return false;
	}

	@Override
	public boolean processItemTeleport(WorldTile toTile) {
		player.getPackets().sendGameMessage("You can't leave just like that!");
		return false;
	}

	@Override
	public boolean processObjectTeleport(WorldTile toTile) {
		player.getPackets().sendGameMessage("You can't leave just like that!");
		return false;
	}

	public KalphiteKingInstance getKKInstance() {
		return (KalphiteKingInstance) getInstance();
	}

	@Override
	public boolean login() {
		player.setNextWorldTile(new WorldTile(Boss.Kalphite_King.getOutsideTile()));
		removeControler();
		return false;
	}

	@Override
	public boolean logout() {
		getKKInstance().leaveInstance(player, BossInstance.LOGGED_OUT);
		removeControler();
		return false;
	}

}
