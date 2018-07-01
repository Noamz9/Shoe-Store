package main.java.bgu.spl.app;

public abstract class Schedule {
	private String shoeType;
	private int tick;

	public Schedule(String shoeType, int tick) {
		this.shoeType = shoeType;
		this.tick = tick;
	}
	
	public String getShoe() {
		return shoeType;
	}

	public int getTick() {
		return tick;
	}
}
