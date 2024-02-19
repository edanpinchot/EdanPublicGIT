package edu.yu.intro;

import java.util.*;

public enum Patrons {
	Singleton;
	Set<Patron> container;
	
	private Patrons() {
		container = new HashSet<Patron>();
	}
	
	public void add(Patron patron) {
		container.add(patron);
	}
	
	public void clear() {
		container.clear();
	}
	
	public Patron get(String uuid) {				//when it says to return null if "no such patron exists", won't that automatically be a NullPointerException
		for (Patron patron : container) {
			if (patron.getId().equals(uuid)) {
				return patron;
			}
		}
		return null;
	}
	
	public int nPatrons() {
		return container.size();
	}
		
	public Set<Patron> byLastNamePrefix(final String prefix) {
		Set<Patron> set = new HashSet<>();
		
		if (prefix == null) {
			throw new IllegalArgumentException("ERROR: Invalid prefix entered.");
		}
		if (prefix.isEmpty()) {
			for (Patron patron : container) {
				set.add(patron);
			}
		}
		else {
			for (Patron patron : container) {
				if (patron.getLastName().startsWith(prefix)) {
					set.add(patron);
				}
			}
		}
		return set;
	}
		
}