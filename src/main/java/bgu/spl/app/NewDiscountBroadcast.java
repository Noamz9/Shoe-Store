package main.java.bgu.spl.app;

import main.java.bgu.spl.mics.Broadcast;

public class NewDiscountBroadcast implements Broadcast{
	private String shoeType;
	
	public NewDiscountBroadcast(String shoeType) {
		this.shoeType = shoeType;
	}
	
	public String getShoe() {
		return shoeType;
	}
	
}
