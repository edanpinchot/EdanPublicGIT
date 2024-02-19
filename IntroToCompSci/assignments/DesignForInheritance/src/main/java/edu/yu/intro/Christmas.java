package edu.yu.intro;

class Christmas extends Holidays {
	private String colors;
	
	public void setColors(String colors) {
		this.colors = colors;
	}
	
	public String getColors() {
		return colors;
	}
	
	public String religion() {
		return "Christian";
	}
}