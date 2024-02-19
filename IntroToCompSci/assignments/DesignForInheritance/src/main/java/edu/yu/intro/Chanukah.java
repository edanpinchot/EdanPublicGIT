package edu.yu.intro;

public class Chanukah extends Holidays {
	private String minhagim;
	
	public void setMinhagim(String minhagim) {
		this.minhagim = minhagim;
	}
	
	public String getMinhagim() {
		return minhagim;
	}
	
	public int numberOfDays() {			//overrides method in superclass
		return 8;
	}
	
	public static void main(String[] args) {
	
		Holidays a = new Holidays();
		a.setName("Hannukah");
		
		Chanukah b = new Chanukah();
		b.setName("Chanukah");
		b.setMinhagim("Spinning the dreidel & eating sufganiyot and latkes");
		
		Christmas c = new Christmas();
		c.setName("Christmas");
		c.setColors("red & green");
		
		System.out.println();
		System.out.println(a.toString() + " -- when using 'Holidays' base class");
		System.out.println(b.toString() + " -- when using 'Chanukah' subclass");
		System.out.println(c.toString() + " -- when using 'Christmas' subclass");
		System.out.println();
		
		System.out.println("Base class method numberOfDays(): " + a.numberOfDays());
		System.out.println("Subclass method numberOfDays(): " + b.numberOfDays() + " (override)");
		System.out.println();
		
		System.out.println("Subclass specific method getMinhagim() of chanukah: " + b.getMinhagim());
		System.out.println("Subclass specific method getColors() of christmas: " + c.getColors());
		System.out.println();
	}
}