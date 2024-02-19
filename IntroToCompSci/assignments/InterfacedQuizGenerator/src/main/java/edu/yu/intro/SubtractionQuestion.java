package edu.yu.intro;

import java.util.Random;

public class SubtractionQuestion implements IntQuestion {
	private int firstNumber;
	private int secondNumber;
	
	public SubtractionQuestion() {
		Random random = new Random();
		this.firstNumber = 1 + random.nextInt(50);
		this.secondNumber = 1 + random.nextInt(50);
	}
	
	public String getQuestion() {
		if (firstNumber < secondNumber) {
			int sub;
			sub = firstNumber;
			firstNumber = secondNumber;
			secondNumber = sub;
		}
		return firstNumber + " - " + secondNumber;
	}
	
	public int getCorrectAnswer() {
		int correctAnswer = firstNumber - secondNumber;
		return correctAnswer;
	}
} 