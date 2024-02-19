package edu.yu.intro;

public class Driver {

	public static Library run() {
	
		Library library1 = new Library("New York Public Library", "476 5th Ave", "212-340-0863");
		
		Book book1 = new Book("Anthem", "Ayn Rand", 1234567890987L, "hardcover");
		Book book2 = new Book("The Great Gatsby", "F. Scott Fitzgerald", 1233049587696L, "paperback");
		Book book3 = new Book("The Fountainhead", "Ayn Rand", 9948373347100L, "paperback");
		
		library1.add(book1);
		library1.add(book2);
		library1.add(book3);
		
		Patron patron1 = new Patron("Nick", "Carraway", "13 6th St");
		Patron patron2 = new Patron("Barb", "Kelner", "3606 Lake Ave");
		
		library1.add(patron1);
		library1.add(patron2);
		
		return library1;
	}
}