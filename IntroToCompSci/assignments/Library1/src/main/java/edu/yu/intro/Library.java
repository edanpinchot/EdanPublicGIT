package edu.yu.intro;

import java.util.*;

public class Library {
	private String name;
	private String address;
	private String phoneNumber;
	
	ArrayList<Book> holdings = new ArrayList<>();	

	public Library(String name, String address, String phoneNumber) {
		if ((name == null) || (name.length()) == 0) {
			throw new RuntimeException("ERROR: Invalid library name entered.");
		}
		if ((address == null) || (address.length()) == 0) {
			throw new RuntimeException("ERROR: Invalid library address entered.");
		}
		if ((phoneNumber == null) || (phoneNumber.length()) == 0) {
			throw new RuntimeException("ERROR: Invalid library phone number entered.");
		}
		
		this.name = name;
		this.address = address;
		this.phoneNumber = phoneNumber;
	}
	
	public String getName() {
		return name;
	}
	
	public String getAddress() {
		return address;
	}
	
	public String getPhoneNumber() {
		return phoneNumber;
	}
	
	public void add(Book b) {
		holdings.add(b);
	}

	public boolean isTitleInHoldings(String title) {
		if ((title == null) || (title.length() == 0)) {
			throw new IllegalArgumentException("ERROR: Title not found.");
		}
		for (int counter = 0; counter < holdings.size(); counter ++) {
			if (holdings.get(counter).getTitle().equals(title)) {
				return true;
			}
			else {
			}
		}
		return false;
	}
	
	public boolean isISBNInHoldings(long isbn13) {
		String stringIsbn13 = String.valueOf(isbn13);
		if ((stringIsbn13.length() != 13) || (isbn13 < 0)) {
			throw new IllegalArgumentException("ERROR: ISBN must be a 13-digit, positive number.");
		}
		for (int counter = 0; counter < holdings.size(); counter ++) {
			if (holdings.get(counter).getISBN13() == isbn13) {
				return true;
			}
			else {
			}
		}
		return false;
	}
	
	public Book getBook(long isbn13) {
		String stringIsbn = String.valueOf(isbn13);
		if ((stringIsbn.length() != 13) || (isbn13 < 0)) {
			throw new IllegalArgumentException("ERROR: ISBN must be a 13-digit, positive number.");
		}
		for (int counter = 0; counter < holdings.size(); counter++) {
			if (holdings.get(counter).getISBN13() == isbn13) {
				return holdings.get(counter);
			}
			else {
			}
		}
		return null;
	}

	public int nBooks() {
		return holdings.size();
	}
	
	@Override
	public boolean equals(Object o) {
		if ((o == null) || o.getClass() != getClass()) { 
			return false;
		}
		final Library that = (Library) o;
		return name.equals(that.name); 
	}
	
	@Override
	public int hashCode () {
		return name.hashCode(); 
	}
	
	@Override
	public String toString () {
		return "{ " + getClass() + " [name=" + name + ", address=" + address + ", phone number=" + phoneNumber + "] }"; 
	}


	public static void main(String[] args) {
			
		Library library1 = new Library("Manhattan Public Library", "11300 6th Ave", "847-767-0495");	
		Library library2 = new Library("NYC Library", "369 4th Street", "847-904-3744");	
		
		Book book1 = new Book("Anthem", "Ayn Rand", 1234567890987L, "hardcover");
		Book book2 = new Book("Hamlet", "Shakespeare", 4576567890276L, "ebook");
		Book book3 = new Book("Creep", "Scott Singer", 9473628890987L, "hardcover");
		Book book4 = new Book("The Great Gatsby", "F. Scott Fitzgerald", 1233049587696L, "paperback");

		library1.add(book1);
		library1.add(book2);
		library1.add(book3);
		library2.add(book4);
		
		System.out.println(library1.isTitleInHoldings("Hamlet"));
		System.out.println(library1.isISBNInHoldings(9473628890987L));
		System.out.println(library1.nBooks());
		System.out.println(library2.nBooks());
		System.out.println(library2.getBook(1233049587696L).getTitle());
		//System.out.println(book1.equals(book2));
	}

}