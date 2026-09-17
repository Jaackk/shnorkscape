package com.rs.game.activites.creations;

import java.util.List;

public final class StealingCreationScore {

	private final String name;
	private final boolean redTeam;
	private int gathering;
	private int processing;
	private int depositing;
	private int withdrawing;
	private int damaging;
	private int kills;
	private int deaths;

	public StealingCreationScore(String name, boolean redTeam) {
		this.name = name == null ? "Unknown" : name;
		this.redTeam = redTeam;
	}

	public StealingCreationScore copy() {
		StealingCreationScore copy = new StealingCreationScore(name, redTeam);
		copy.gathering = gathering;
		copy.processing = processing;
		copy.depositing = depositing;
		copy.withdrawing = withdrawing;
		copy.damaging = damaging;
		copy.kills = kills;
		copy.deaths = deaths;
		return copy;
	}

	public void updateGathering(int delta) {
		gathering = addScore(gathering, delta);
	}

	public void updateProcessing(int delta) {
		processing = addScore(processing, delta);
	}

	public void updateDepositing(int delta) {
		depositing = addScore(depositing, delta);
	}

	public void updateWithdrawing(int delta) {
		withdrawing = addScore(withdrawing, delta);
	}

	public void updateDamaging(int delta) {
		damaging = addScore(damaging, delta);
	}

	public void updateKills(int delta) {
		kills = addScore(kills, delta);
	}

	public void updateDeaths(int delta) {
		deaths = addScore(deaths, delta);
	}

	public int total(boolean winner) {
		long total = (long) gathering + processing + getNetDepositScore() + damaging;
		if (winner) {
			total = Math.round(total * 1.1D);
		}
		return clampScore(total);
	}

	public String getName() {
		return name;
	}

	public boolean isRedTeam() {
		return redTeam;
	}

	public int getGathering() {
		return gathering;
	}

	public int getProcessing() {
		return processing;
	}

	public int getDepositing() {
		return depositing;
	}

	public int getWithdrawing() {
		return withdrawing;
	}

	public int getNetDepositScore() {
		return clampScore(((long) depositing - withdrawing) * 2L);
	}

	public int getDamaging() {
		return damaging;
	}

	public int getKills() {
		return kills;
	}

	public int getDeaths() {
		return deaths;
	}

	public static int total(List<StealingCreationScore> scores, boolean redTeam, boolean boosted) {
		long total = 0;
		for (StealingCreationScore score : scores) {
			if (score != null && score.isRedTeam() == redTeam) {
				total += score.total(boosted);
			}
		}
		return clampScore(total);
	}

	public static StealingCreationScore highestTotal(List<StealingCreationScore> scores, final int winnerTeam) {
		return highest(scores, new ScoreValue() {
			@Override
			public int value(StealingCreationScore score) {
				return score.total((score.isRedTeam() ? 2 : 1) == winnerTeam);
			}
		});
	}

	public static StealingCreationScore lowestTotal(List<StealingCreationScore> scores, final int winnerTeam) {
		StealingCreationScore lowest = null;
		int lowestValue = Integer.MAX_VALUE;
		for (StealingCreationScore score : scores) {
			if (score == null) {
				continue;
			}
			int value = score.total((score.isRedTeam() ? 2 : 1) == winnerTeam);
			if (lowest == null || value < lowestValue) {
				lowest = score;
				lowestValue = value;
			}
		}
		return lowest;
	}

	public static StealingCreationScore mostGathered(List<StealingCreationScore> scores) {
		return highest(scores, new ScoreValue() {
			@Override
			public int value(StealingCreationScore score) {
				return score.getGathering();
			}
		});
	}

	public static StealingCreationScore mostProcessed(List<StealingCreationScore> scores) {
		return highest(scores, new ScoreValue() {
			@Override
			public int value(StealingCreationScore score) {
				return score.getProcessing();
			}
		});
	}

	public static StealingCreationScore mostDeposited(List<StealingCreationScore> scores) {
		return highest(scores, new ScoreValue() {
			@Override
			public int value(StealingCreationScore score) {
				return score.getNetDepositScore();
			}
		});
	}

	public static StealingCreationScore mostDamaged(List<StealingCreationScore> scores) {
		return highest(scores, new ScoreValue() {
			@Override
			public int value(StealingCreationScore score) {
				return score.getDamaging();
			}
		});
	}

	public static StealingCreationScore mostKills(List<StealingCreationScore> scores) {
		return highest(scores, new ScoreValue() {
			@Override
			public int value(StealingCreationScore score) {
				return score.getKills();
			}
		});
	}

	public static StealingCreationScore mostDeaths(List<StealingCreationScore> scores) {
		return highest(scores, new ScoreValue() {
			@Override
			public int value(StealingCreationScore score) {
				return score.getDeaths();
			}
		});
	}

	private static StealingCreationScore highest(List<StealingCreationScore> scores, ScoreValue value) {
		StealingCreationScore highest = null;
		int highestValue = Integer.MIN_VALUE;
		for (StealingCreationScore score : scores) {
			if (score == null) {
				continue;
			}
			int scoreValue = value.value(score);
			if (highest == null || scoreValue > highestValue) {
				highest = score;
				highestValue = scoreValue;
			}
		}
		return highest;
	}

	private static int addScore(int current, int delta) {
		return clampScore((long) current + Math.max(0, delta));
	}

	private static int clampScore(long value) {
		if (value <= 0) {
			return 0;
		}
		return value > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) value;
	}

	private interface ScoreValue {
		int value(StealingCreationScore score);
	}
}
