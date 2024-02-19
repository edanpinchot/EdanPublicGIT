package edu.yu.intro;

public class Driver {

	public static void run() {
		
		Library library1 = new Library("New York Public Library", "476 5th Ave", "212-340-0863");
		
		Patrons.Singleton.clear();
		library1.clear();
		
		Book book1 = new Book("The Great Gatsby", "F. Scott Fitzgerald", 8473692837001L, "hardcover");
		Book book2 = new Book("The Tempest", "William Shakespeare", 3309846378273L, "paperback");
		Book book3 = new Book("Exodus", "Leon Uris", 8102937228974L, "ebook");
		
		library1.add(book1);
		library1.add(book2);
		library1.add(book3);
		
		Patron patron1 = new Patron("Nick", "Carraway", "13 6th St");
		Patron patron2 = new Patron("Barb", "Kelner", "3606 Lake Ave");
		
		Patrons.Singleton.add(patron1);
		Patrons.Singleton.add(patron2);
	}
	
}