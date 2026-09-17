package com.rs.game.npc.familiar;

import com.rs.game.WorldTile;
import com.rs.game.player.Player;
import com.rs.game.player.actions.summoning.Summoning.Pouches;

public class Phoenix extends Familiar {

		/**
	 * 
	 */
	private static final long serialVersionUID = -4002874057813723199L;

		public Phoenix(Player owner, Pouches pouch, WorldTile tile,
	                      int mapAreaNameHash, boolean canBeAttackFromOutOfArea) {
	        super(owner, pouch, tile, mapAreaNameHash, canBeAttackFromOutOfArea);
	    }

	    @Override
	    public String getSpecialName() {
	        return "Rise from the ashes";
	    }

	    @Override
	    public String getSpecialDescription() {
	        return "Regenerate your pheonix through the ashes around it.";
	    }

	    @Override
	    public int getBOBSize() {
	        return 0;
	    }

	    @Override
	    public int getSpecialAmount() {
	        return 3;
	    }

	    @Override
	    public SpecialAttack getSpecialAttack() {
	        return SpecialAttack.CLICK;
	    }

	    @Override
	    public boolean submitSpecial(Object object) {
	    	/*final Region region = World.getRegion(this.getRegionId());//World.getRegion(getOwner().getRegionId());
	    	if (region == null)
	    		return false;
	    	WorldTile tile = null;
	    	boolean possible = false;
	    	for (Iterator<FloorItem> it = region.getFloorItems().iterator(); it.hasNext();) {
	    		FloorItem item = (FloorItem) it.next();
	    		if (item == null)
	    			continue;
	    		if (item.getId() == 592) {
	    			tile = new WorldTile(item.getTile());
	    			possible = true;
	    			break;
	    		}
	    	}
	    	if (!possible || tile == null)
	    		return false;
	    	Entity fam = this;
	    	WorldTasksManager.schedule(new WorldTask() {
	    		@Override
	    		public void run() {
	    			
	    		}
	    	}, 0, 0);*/
	        return false;//to prevent
	    }
	}

