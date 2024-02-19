package edu.yu.intro;

public class Holidays {
	private String name;
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public String religion() {
		return "Jewish";
	}
	
	public int numberOfDays() {
		return 1;
	}
	
	@Override
	public String toString() {
		return "{Holiday details: name is " + getName() + ", religion is " + religion() + ", " + numberOfDays() + " total day(s)}";
	}
}	
