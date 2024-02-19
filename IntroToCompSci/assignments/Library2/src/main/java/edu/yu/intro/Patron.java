package edu.yu.intro;

import java.util.UUID;

public class Patron {
	private String firstName;
	private String lastName;
	private String address;
	private String id = UUID.randomUUID().toString();
	
	public Patron(String firstName, String lastName, String address) {
		if ((firstName == null) || (firstName.length()) == 0) {
			throw new RuntimeException("ERROR: Invalid first name entered.");
		}
		if ((lastName == null) || (lastName.length()) == 0) {
			throw new RuntimeException("ERROR: Invalid last name entered.");
		}
		if ((address == null) || (address.length()) == 0) {
			throw new RuntimeException("ERROR: Invalid patron address entered.");
		}
		
		this.firstName = firstName;
		this.lastName = lastName;
		this.address = address;
	}
	
	public String getFirstName() {
		return firstName;
	}
	
	public String getLastName() {
		return lastName;
	}
	
	public String getAddress() {
		return address;
	}
	
	public String getId() {
		return id;
	}		
	
	@Override
	public boolean equals(Object o) {
		if ((o == null) || o.getClass() != getClass()) { 
			return false;
		}
		final Patron that = (Patron) o;
		return that.getId() == (id); 
	}
	
	@Override
	public int hashCode() {
		return id.hashCode(); 
	}

	@Override
	public String toString() {
		return "{" + getClass() + " [first name=" + firstName + ", last name=" + lastName + ", address=" + address + "]}"; 
	}
}
