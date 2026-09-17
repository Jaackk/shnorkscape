package com.rs.game.player.content;

public final class TriviaQuestion {

	private final String question;
	private final int difficulty;
	private final String[] answers;
	
	public TriviaQuestion(String question, int difficulty, String... answers) {
		this.question = question;
		this.difficulty = difficulty;
		this.answers = answers;
	}
	
	public final String getQuestion()  {
		return question;
	}
	
	public final int getDifficulty() {
		return difficulty;
	}
	
	public final String[] getAnswers() {
		return answers;
	}
	
}
