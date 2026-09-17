package com.rs.game.route.client;
/* Class336_Sub6 - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */

import com.rs.utils.Logger;

public class ExactStrategy extends PathStrategy {
	
	public boolean method4091(int i, int i_0_, int i_1_) {
		return (-1331662251 * toX == i_0_ && i_1_ == 1517720743 * toY);
	}

	ExactStrategy() {
		
	}

	public boolean method4089(int i, int i_2_, int i_3_) {
		return (-1331662251 * toX == i_2_ && i_3_ == 1517720743 * toY);
	}

	public boolean method4090(int i, int i_4_, int i_5_,
			int i_6_) {
		try {
			return (-1331662251 * toX == i_4_ && i_5_ == 1517720743 * toY);
		} catch (RuntimeException e) {
			Logger.getGlobal().catching(e);
		}
		return false;
	}
}