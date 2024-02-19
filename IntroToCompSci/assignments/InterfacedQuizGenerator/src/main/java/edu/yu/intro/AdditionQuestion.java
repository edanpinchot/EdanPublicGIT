package edu.yu.intro;

import java.util.Random;

public class AdditionQuestion implements IntQuestion {
	private int firstNumber;
	private int secondNumber;
	
	public AdditionQuestion() {
		Random random = new Random();
		this.firstNumber = 1 + random.nextInt(50);
		this.secondNumber = 1 + random.nextInt(50);
	}
	
	public String getQuestion() {
		return firstNumber + " + " + secondNumber;
	}
	
	public int getCorrectAnswer() {
		int correctAnswer = firstNumber + secondNumber;
		return correctAnswer;
	}
} 