package com.rs.game.player.content.fistofguthix;

/**
 * The manager of the fist of guthix minigame.
 * 
 * @author _Waterfiend <skype:alco-wahidi>
 *
 * Created in Apr 30, 2017 at 8:16:03 PM.
 */
public class FOGManager {
	
	/**
	 * The fist of guthix minigame.
	 */
	private FistOfGuthix fistOfGuthix;
	
	/**
	 * The instance of the fist of guthix manager.
	 */
	private static final FOGManager INSTANCE = new FOGManager();

	/**
	 * Gets the fist of guthix instance.
	 * @return the fistOfGuthix
	 */
	public FistOfGuthix getFOGInstance() {
		if (fistOfGuthix == null)
			setFOGInstance(new FistOfGuthix(60));
		return fistOfGuthix;
	}

	/**
	 * Sets the fist of guthix.
	 * @param fistOfGuthix The instance to set.
	 */
	public void setFOGInstance(FistOfGuthix fistOfGuthix) {
		this.fistOfGuthix = fistOfGuthix;
	}

	/**
	 * Gets the fist of guthix manager.
	 * @return the INSTANCE
	 */
	public static FOGManager get() {
		return INSTANCE;
	}

}

