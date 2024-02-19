package edu.yu.intro;

import java.util.Objects;

public class Book {
	private String title;
	private String author;
	private long ISBN13;
	private String bookType;
	
	public Book (String title, String author, long ISBN13, String bookType) {
		if ((title == null) || (title.length() == 0)) {
			throw new RuntimeException("ERROR: Invalid book title entered.");
		}
		if ((author == null) || (author.length() == 0)) {
			throw new RuntimeException("ERROR: Invalid book author entered.");
		}
		if ((bookType == null) || (bookType.length() == 0)) {
			throw new RuntimeException("ERROR: Invalid book type entered.");
		}
	
		this.title = title;
		this.author = author;
		String stringISBN = String.valueOf(ISBN13);		
		if ((stringISBN.length() == 13) && (ISBN13 >= 0)) {
			this.ISBN13 = ISBN13;
		}
		else {
			throw new RuntimeException("ERROR: ISBN must be a positive, 13-digit number.\n");
		}
		this.ISBN13 = ISBN13;
		if (bookType.equals("hardcover") || bookType.equals("paperback") || bookType.equals("ebook")) {
			this.bookType = bookType;
		}
		else {
			throw new RuntimeException("ERROR: Invalid book type entered.\n");
		}
	}
	
	public String getTitle() {
		return title;
	}
	
	public String getAuthor() {
		return author;
	}
	
	public long getISBN13() {
		return ISBN13;
	}
	
	public String getBookType() {
		return bookType;
	}
	
	@Override
	public boolean equals (Object o) {
		if (o == this) {
			return true;
		}
		if (o == null) {
			return false;
		}
		if (getClass() != o.getClass()) {
			return false;
		}
		
		Book book = (Book) o;
		return (book.ISBN13 == ISBN13);
	}
	
	@Override
	public int hashCode() {
        return Objects.hash(ISBN13);
    }
	
	@Override
	public String toString () {
		return "{" + getClass() + " [title=" + title + ", author=" + author + ", ISBN=" + ISBN13 + ", book type=" + bookType + "]}"; }
}
