package com.rs.game.player.content.barrows;

public enum BarrowsPuzzleData {

	ARROWS(6713, new int[] { 6716, 6717, 6718 }, new int[] { 6713, 6714, 6715 }),
	SQUARES(6719, new int[] { 6722, 6723, 6724 }, new int[] { 6719, 6720, 6721 }),
	SQUARES_OFFSET(6725, new int[] { 6728, 6729, 6730 }, new int[] { 6725, 6726, 6727 }),
	SHAPES(6731, new int[] { 6734, 6735, 6736 }, new int[] { 6731, 6732, 6733 });

	private final int answer;
	private final int[] sequence, options;
	
	BarrowsPuzzleData(int answer, int[] sequence, int[] options) {
		this.answer = answer;
		this.sequence = sequence;
		this.options = options;
	}
	
	public final int getAnswer() {
		return answer;
	}
	
	public final int getSequenceModel(int index) {
		return sequence[index];
	}
	
	public final int[] getSequence() {
		return sequence;
	}
	
	public final int[] getOptions() {
		return options;
	}
}
