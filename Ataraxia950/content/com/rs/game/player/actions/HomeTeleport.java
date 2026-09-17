package com.rs.game.player.actions;

import com.rs.game.Animation;
import com.rs.game.Graphics;
import com.rs.game.WorldTile;
import com.rs.game.player.Player;
import com.rs.game.player.content.Magic;
import com.rs.game.player.client.Native950LodestoneTeleport;
import com.rs.utils.Utils;

public class HomeTeleport extends Action {

    public static final WorldTile BANDIT_CAMP = new WorldTile(52661131), LUNAR_ISLE = new WorldTile(34164555), AL_KHARID = new WorldTile(54021233), ARDOUGNE = new WorldTile(43158805), BURTHORPE = new WorldTile(47500761), CATHERBY = new WorldTile(46058874), DRAYNOR_VILLAGE = new WorldTile(50875619), EDGEVILLE = new WorldTile(50253234), FALADOR = new WorldTile(48614732), LUMBRIDGE = new WorldTile(52972694), PORT_SARIM = new WorldTile(49335440), SEERS_VILLAGE = new WorldTile(44060059), TAVERLEY = new WorldTile(47156595), VARROCK = new WorldTile(52661553), YANILLE = new WorldTile(41438231), CANIFIS = new WorldTile(57626044), EAGLES_PEAK = new WorldTile(38768024), FREMENNIK_PROVINCE = new WorldTile(44437086), KARAMJA = new WorldTile(45239372), OOGLOG = new WorldTile(41487160), TIRANNWN = new WorldTile(36932686), WILDERNESS_VOLCANO = new WorldTile(51498548), ASHDALE = new WorldTile(577407637), PRIFDDINAS = new WorldTile(304614689), MENAPHOS = new WorldTile(52693661), ANACHRONIA = new WorldTile(88983843);

	
	private static final int HOME_ANIMATION = 16385, HOME_GRAPHIC = 3017;
	
	private int currentTime;
	private final WorldTile tile;
	private final Native950LodestoneTeleport.Journey nativeJourney;

	public HomeTeleport(WorldTile tile) {
        this(tile, null);
    }

    /** Native caller supplies its validated final arrival; legacy callers retain the stone-offset convention. */
    public HomeTeleport(WorldTile tile, Native950LodestoneTeleport.Journey nativeJourney) {
        this.tile = nativeJourney == null ? tile.transform(0, -1, 0) : new WorldTile(tile);
        this.nativeJourney = nativeJourney;
    }

	@Override
	public boolean process(Player player) {
        if (nativeJourney != null) return nativeJourney.process(player);
		if (player.getAttackedByDelay() + 10000 > Utils.currentTimeMillis()) {
			player.getPackets().sendGameMessage("You can't home teleport until 10 seconds after the end of combat.");
			return false;
		}
		return true;
	}

	@Override
	public int processWithDelay(Player player) {
		if (currentTime++ == 0) {
            if (nativeJourney != null) nativeJourney.departureEffects(player);
            else {
                player.setNextAnimation(new Animation(HOME_ANIMATION));
                player.setNextGraphics(new Graphics(HOME_GRAPHIC));
            }
		} else if (currentTime == 18) {
            if (nativeJourney != null) {
                if (!nativeJourney.teleport(player)) return -1;
            } else {
			player.lock();
			player.setNextWorldTile(tile.transform(0, 1, 0));
			player.getControlerManager().magicTeleported(Magic.MAGIC_TELEPORT);
			if (player.getControlerManager().getControler() == null)
				Magic.teleControlersCheck(player, tile);
			player.setNextFaceWorldTile(new WorldTile(tile.getX(), tile.getY(), tile.getPlane()));
			player.setDirection(6);
            }
		} else if (currentTime == 19) {
            if (nativeJourney != null) nativeJourney.arrivalEffects(player);
            else {
			player.setNextGraphics(new Graphics(HOME_GRAPHIC + 1));
			player.setNextAnimation(new Animation(HOME_ANIMATION + 1));
            }
		} else if (currentTime == 24) {
            if (nativeJourney != null) nativeJourney.land(player);
            else {
			player.setNextWorldTile(tile);
			player.setNextAnimation(new Animation(16393));
			player.unlock();
            }
		} else if (currentTime == 25) {
            if (nativeJourney == null) player.getControlerManager().processMagicTeleport(tile);
			return -1;
		}
		return 0;
	}

	@Override
	public boolean start(final Player player) {
        if (nativeJourney != null) return nativeJourney.start(player);
		if (!player.getControlerManager().processMagicTeleport(tile))
			return false;
		return process(player);
	}

	@Override
	public void stop(Player player) {
        if (nativeJourney != null) { nativeJourney.stop(player); return; }
		player.setNextAnimation(new Animation(-1));
		player.setNextGraphics(new Graphics(-1));
	}
}