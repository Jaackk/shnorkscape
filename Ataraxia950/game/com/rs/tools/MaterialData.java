package com.rs.tools;

public class MaterialData {

	private final double xp;
	private final PossiblePerk[] possiblePerks;

	public MaterialData(double xp, PossiblePerk[] possiblePerks) {
		this.xp = xp;
		this.possiblePerks = possiblePerks;
	}

	public double getXp() {
		return xp;
	}

	public PossiblePerk[] getPossiblePerks() {
		return possiblePerks;
	}

	public static class PossiblePerk {
		private final int perkId;
		private final boolean[] allowedGizmoTypes;
		private final int[][] possibleRanks;

		public PossiblePerk(int perkId, boolean[] allowedGizmoTypes, int[][] possibleRanks) {
			this.perkId = perkId;
			this.allowedGizmoTypes = allowedGizmoTypes;
			this.possibleRanks = possibleRanks;
		}

		public int getPerkId() {
			return perkId;
		}

		public boolean[] getAllowedGizmoTypes() {
			return allowedGizmoTypes;
		}

		public int[][] getPossibleRanks() {
			return possibleRanks;
		}

	}

}
