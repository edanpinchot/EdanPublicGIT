package edu.yu.intro;

public class BookFilter {
	private String author;
	private String title;
	private long isbn13;
	private String bookType;
	
	private BookFilter(Builder builder) {
		this.author = builder.author;
		this.title = builder.title;
		this.isbn13 = builder.isbn13;
		this.bookType = builder.bookType;
	}
	
	public boolean filter(Book book) {
		boolean authorMatch, titleMatch, isbnMatch, bookTypeMatch;
		
		if ((this.author == null) || (book.getAuthor().equals(this.author))) {
			authorMatch = true;
		}
		else {
			authorMatch = false;
		}
		if ((this.title == null) || (book.getTitle().equals(this.title))) {	
			titleMatch = true;
		}
		else {
			titleMatch = false;
		}
		if ((this.isbn13 == 0) || (book.getISBN13() == (this.isbn13))) {		//dont think i need to test if isbn is null because error will be thrown already if not a 13 digit number
			isbnMatch = true;
		}
		else {
			isbnMatch = false;
		}
		if ((this.bookType == null) || (book.getBookType().equals(this.bookType))) {
			bookTypeMatch = true;
		}
		else {
			bookTypeMatch = false;
		}
		
		if ((authorMatch == true) && (titleMatch == true) && (isbnMatch == true) && (bookTypeMatch == true)) {
			return true;
		}
		else {
			return false;
		}
	}
	
	public static class Builder {
		private String author;
		private String title;
		private long isbn13;
		private String bookType;
			
		public Builder() {
		}
		
		public Builder setAuthor(String author) {
			if ((author == null) || (author.length() == 0)) {
				throw new IllegalArgumentException("ERROR: Invalid book author entered.");
			}
			this.author = author;
			return this;
		}
		
		public Builder setTitle(String title) {
			if ((title == null) || (title.length() == 0)) {
				throw new IllegalArgumentException("ERROR: Invalid book title entered.");
			}
			this.title = title;
			return this;
		}
		
		public Builder setISBN13(long isbn13) {
			String stringISBN = String.valueOf(isbn13);		
			if ((stringISBN.length() == 13) && (isbn13 >= 0)) {
				this.isbn13 = isbn13;
			}
			else {
				throw new IllegalArgumentException("ERROR: ISBN must be a positive, 13-digit number.\n");
			}
			return this;
		}
		
		public Builder setBookType(String bookType) {
			if ((bookType == null) || (bookType.length() == 0)) {
				throw new IllegalArgumentException("ERROR: Invalid book type entered.");
			}
			if (bookType.equals("hardcover") || bookType.equals("paperback") || bookType.equals("ebook")) {
				this.bookType = bookType;
			}
			else {
				throw new IllegalArgumentException("ERROR: Invalid book type entered.\n");
			}
			return this;
		}
		
		public BookFilter build() {
			return new BookFilter(this);
		}
	}	
}