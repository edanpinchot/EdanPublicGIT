package edu.yu.intro;

import java.util.Random;
import java.util.Scanner;

public class InterfacedQuizGenerator {
	
	private final static int N_QUIZ_QUESTIONS = 10;

		public static void createQuiz(IntQuestion[] questionsArray) {	
			Random random = new Random();
			
			for (int counter = 0; counter < (N_QUIZ_QUESTIONS); counter++) {
				AdditionQuestion aq = new AdditionQuestion();
				SubtractionQuestion sq = new SubtractionQuestion();
				
				IntQuestion[] addQsubQ = new IntQuestion[2];
				addQsubQ[0] = aq;
				addQsubQ[1] = sq;	
				
				questionsArray[counter] = addQsubQ[random.nextInt(2)];
			}	
		}
		
		public static int[] giveQuiz(IntQuestion[] questionsArray) {
			Scanner input = new Scanner(System.in);
			int[] userAnswers = new int[N_QUIZ_QUESTIONS];
			
			System.out.println("\nWelcome to the integer arithmetic quiz!\n");
			
			for (int counter = 0; counter < (N_QUIZ_QUESTIONS); counter++) {
				//String question = questionsArray[counter].getQuestion();
				System.out.printf("Question %2d: ", counter + 1);
				System.out.printf(" What is %8s ? ", questionsArray[counter].getQuestion());
				userAnswers[counter] = input.nextInt();
			}	
			return userAnswers;
		}
	
		public static void gradeQuiz(IntQuestion[] questionsArray, int[] userAnswers) {
			System.out.println("\n\nHere are the correct answers:");
			int correctAnswers = 0;
			int finalGrade = 0;
		
			for (int counter = 0; counter < (N_QUIZ_QUESTIONS); counter++) {
				System.out.printf("   Question %2d: ", (counter + 1));
				System.out.printf("%8s = %3d.", questionsArray[counter].getQuestion(), questionsArray[counter].getCorrectAnswer());
			
				if ((questionsArray[counter].getCorrectAnswer()) == userAnswers[counter]) {
					System.out.println("  You were CORRECT.");
					correctAnswers++;
					finalGrade += 10;
				}
				else {
					System.out.printf("  You said %d, which is INCORRECT.%n", userAnswers[counter]);
				}
			}
		
			System.out.printf("%nYou got %d question(s) correct.%n", correctAnswers);
			System.out.printf("Your grade on the quiz is %d.%n", finalGrade);
			System.out.println();
		}							
		
		public static void main(String[] args) {
	
			IntQuestion[] questionsArray = new IntQuestion[N_QUIZ_QUESTIONS];
		
			createQuiz(questionsArray);
		
			int[] userAnswers = giveQuiz(questionsArray);
		
			gradeQuiz(questionsArray, userAnswers);
	}
}