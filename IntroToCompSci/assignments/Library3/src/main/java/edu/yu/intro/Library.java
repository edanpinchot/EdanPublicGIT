package edu.yu.intro;

import java.util.*;

public class Library {
	private String name;
	private String address;
	private String phoneNumber;
	List<Book> holdings = new ArrayList<>();
	List<Patron> patronList = new ArrayList<>();
	Map<Book, Patron> booksToPatrons = new HashMap<>();

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
	
	public void add(Patron patron) {
		patronList.add(patron);
	}
	
	public Patron get(String uuid) {
		for (Patron patron : patronList) {
			if (uuid.equals(patron.getId())) {
				return patron;
			}
		}
		return null;
	}
	
	public int nPatrons() {
		return patronList.size();
	}
	
	public Set<Patron> byLastNamePrefix(final String prefix) {
		Set<Patron> set = new HashSet<>();
		
		if (prefix == null) {
			throw new IllegalArgumentException("ERROR: Invalid prefix entered.");
		}
		if (prefix.isEmpty()) {
			for (Patron patron : patronList) {
				set.add(patron);
			}
		}
		else {
			for (Patron patron : patronList) {
				if (patron.getLastName().startsWith(prefix)) {
					set.add(patron);
				}
			}
		}
		return set;
	}
	
	public void borrow(Patron patron, Book book) {
		if (patronList.contains(patron) == false) {
			throw new IllegalArgumentException("ERROR: Patron is not a member of the specified library.");
		}
		if (holdings.contains(book) == false) {
			throw new IllegalArgumentException("ERROR: Book is not in library's holdings.");
		}
	
		booksToPatrons.put(book, patron);
	}
	
	public Collection<Book> onLoan(Patron patron) {
		List<Book> booksOnLoan = new ArrayList<>();
		
		if (patronList.contains(patron) == false) {
			throw new IllegalArgumentException("ERROR: Patron is not a member of the specified library.");
		}
		
		for (Map.Entry<Book, Patron> pair : booksToPatrons.entrySet()) {
			if (pair.getValue().equals(patron)) {
				booksOnLoan.add(pair.getKey());
			}
		}
		return booksOnLoan;
	}
	
	public Collection<Book> search(BookFilter filter) {
		List<Book> filteredBooks = new ArrayList<>();
		for (Book book : holdings) {
			if (filter.filter(book) == true) {
				filteredBooks.add(book);
			}
		}
		return filteredBooks;
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
	public int hashCode() {
		return name.hashCode(); 
	}
	
	@Override
	public String toString() {
		return "{" + getClass() + " [name=" + name + ", address=" + address + ", phone number=" + phoneNumber + "]}"; 
	}


	public static void main(String[] args) {
		Library library1 = new Library("Manhattan Public Library", "11300 6th Ave", "847-767-0495");	
				
		Book book1 = new Book("Anthem", "Ayn Rand", 1234567890987L, "hardcover");
		Book book2 = new Book("Hamlet", "Shakespeare", 4576567890276L, "ebook");
		Book book3 = new Book("Creep", "Scott Singer", 9473628890987L, "hardcover");
		Book book4 = new Book("The Great Gatsby", "F. Scott Fitzgerald", 1233049587696L, "paperback");
		Book book5 = new Book("The Fountainhead", "Ayn Rand", 9948373347100L, "paperback");
		
		Patron patron1 = new Patron("Edan", "Pinchot", "3606 Grove");
		Patron patron2 = new Patron("Barb", "Kelner", "56 Lawndale");
		Patron patron3 = new Patron("Michel", "Angelo", "53 Amsterdam");
		Patron patron4 = new Patron("Carry", "Pinarks", "55 6th");
		
		library1.add(book1);
		library1.add(book2);
		library1.add(book3);
		library1.add(book4);
		library1.add(book5);
		
		library1.add(patron1);
		library1.add(patron2);
		library1.add(patron3);
		library1.add(patron4);
		
		library1.borrow(patron1, book1);
		library1.borrow(patron1, book2);
		library1.borrow(patron1, book3);
		library1.borrow(patron1, book4);
		
		//library1.borrow(patron2, book4);					//When i do this, it removes book4 from being "on loan" to patron1 ???
		
		Collection<Book> patron1Books = new ArrayList<>();
		//Collection<Book> patron2Books = new ArrayList<>();
		
		patron1Books = library1.onLoan(patron1);
		//patron2Books = library1.onLoan(patron2);
		
		for (Book book : patron1Books) {
			System.out.println(book.getTitle());
		}	
		System.out.println();
		//for (Book book : patron2Books) {
		//	System.out.println(book.getTitle());
		//}
		
		//final BookFilter filter = new BookFilter.Builder()
			//.setAuthor(book1.getAuthor())
			//.setTitle(book1.getTitle())
			//.setISBN13(book1.getISBN13())
			//.setBookType(book1.getBookType())
			//.build();
			
		//Collection<Book> filteredBooks = new ArrayList<>();
		//filteredBooks = library1.search(filter);
		//for (Book book : filteredBooks) {
		//	System.out.println(book);
		//}
	}
	
}


